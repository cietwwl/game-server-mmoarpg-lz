package org.gof.demo.worldsrv.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gof.core.Port;
import org.gof.core.entity.EntityBase;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.scheduler.ScheduleMethod;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import org.gof.demo.worldsrv.human.HumanGlobalServiceProxy;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.SCTeamRepLeave;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.I18n;

	@DistrClass(
		servId = D.SERV_TEAM,
		importClass = {EntityBase.class, List.class, HumanGlobalInfo.class, Map.class, HumanObject.class}
	)
	/**
	 * 队伍服务
	 */
	public class TeamGlobalService extends GameServiceBase {
	
	/** 
	 * 组队, K:humanId，V:队员信息，需要角色上线生成，当角色血量，法力，等级改变的时候都会改变这里的信息
	 */
	private HashMap<Long, TeamMemberObject> teamMemberMap = new HashMap<Long, TeamMemberObject>();
	
	/* 组队, K:队伍ID，V:队伍 */
	private HashMap<Long, Team> map = new HashMap<Long, Team>();
	
	/* 副本组队， K:副本ID，V:队伍 */
	private HashMap<Integer, Map<Long, Team>> reqMap = new HashMap<Integer, Map<Long,Team>>();
	
	/* 通知列表， K:副本ID，V:角色ID */
	private HashMap<Integer, Set<Long>> openedSet = new HashMap<Integer, Set<Long>>();
	
	/* 队伍已经满的 */
	private HashMap<Long, Integer> fullMap = new HashMap<Long, Integer>();
	public TeamGlobalService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() { }
	
	/** 远程返回队员 */
	@DistrMethod
	public void getTeamMemberDistr(long humanId) {
		port.returns(teamMemberMap.get(humanId));
	}
	/** 远程返回多个队员 */
	@DistrMethod
	public void getTeamMembersDistr(List<Long> humanIds) {
		List<TeamMemberObject> mms = new ArrayList<TeamMemberObject>();
		for(Long humanId:humanIds){
			TeamMemberObject mmo = teamMemberMap.get(humanId);
			if(mmo != null){
				mms.add(mmo);
			}
		}
		port.returns(mms);
	}
	/** 
	 * 远程所有没有队伍的"在线"的角色ID
	 * 没有队伍：teamId = 0
	 * 离线：       Status = 0
	 **/
	@DistrMethod
	public void getTeamMembersWithOutTeamDistr() {
		List<TeamMemberObject> mms = new ArrayList<TeamMemberObject>();
	    for(Map.Entry<Long, TeamMemberObject> entry:teamMemberMap.entrySet()){
//	        Long awardKey = entry.getKey();
	        TeamMemberObject mmb = entry.getValue();
	        if(mmb.status == 0 && mmb.teamId == 0){
	        	mms.add(mmb);
	        }
	    }
		port.returns(mms);
	}
	
	/** 返回队伍 */
	@DistrMethod
	public void getTeam(long teamId) {
		port.returns(map.get(teamId));
	}
	
	/** 返回队伍 */
	@DistrMethod
	public void getTeam(HumanObject humanObj) {
		long humanId = humanObj.getHumanId();
		Team team = getTeamByHumanId(humanId);
		port.returns(team);
	}
	
	/** 根据角色ID，返回队伍 */
	@DistrMethod
	public void getTeamByHumanIdDistr(long humanId) {
		Team team = getTeamByHumanId(humanId);
		port.returns(team);
	}
	public Team getTeamByHumanId(long humanId) {
		for (Team t : map.values()) {
			if(t.isMember(humanId)){
				return t;
			}
		}
		return null;
	}
	
	
	/** 返回角色的副本队伍 */
	@DistrMethod
	public void getHumanReqTeam(HumanObject humanObj, int reqId) {
		long humanId = humanObj.getHumanId();
		Team t = null;
		Map<Long, Team> teamMap = reqMap.get(reqId);
		if(teamMap == null || teamMap.isEmpty()){
			port.returns(t);
			return;
		}
		for(Team team : teamMap.values()){
			if(team.isMember(humanId)){
				t = team;
				break;
			}
		}
		port.returns(t);
	}
	
	/** 更新队伍 */
	@DistrMethod
	public void updateTeam(Team team) {
		if(team == null){
			return;
		}
		map.put(team.teamId, team);
	}
	
	/** 踢出队友逻辑 */
	@DistrMethod
	public void kickMember(long teamId, HumanObject human, long memberId) {
		Team t = map.get(teamId);
		TeamResult rs = new TeamResult().failure();
		if(t == null){
			rs.setInfo(I18n.get("team.common.noTeam"));//队伍不存在
			port.returns(rs);
			return;
		}
		//不是队长不能踢人
		if(!t.isLeader(human.id)){
			rs.setInfo(I18n.get("team.manager.leave.notLeader"));//队伍不存在
			port.returns(rs);
			return;
		}
		TeamMemberObject member = t.leaveTeam(memberId);
		if(member == null){
			rs.setInfo(I18n.get("team.common.notMem"));//{}不是队伍成员！
			port.returns(rs);
			return;
		}
		port.returns(rs);
		//通知队长和队员更新
		updateTeamsRealTime(t, TeamManager.KICK);
	}
	/** 离开队伍，暂时没有用到这个方法，可以用来做普通队伍的离开 */
	@DistrMethod
	public void leave(long humanId) {
		TeamResult rs = new TeamResult().failure();
		TeamMemberObject mmb = getTeamMember(humanId);
		if(!mmb.hasTeam()){
			rs.setInfo(I18n.get("team.common.noTeam"));//队伍不存在
			port.returns(rs);
			return;
		}
		Team t = map.get(mmb.teamId);
		t.leaveTeam(humanId);//离队
		if(t.isEmpty()){
			rmvMap(t);
		}
		/* 如果是队长离开：如果是副本队伍重选队长，默认创建类型则解散 */
		if(t.isLeader(humanId)){
			if(!t.members.isEmpty()){
				t.leader = t.members.get(0);//晋升队长
				t.leader.setLeader(true);
			}
		}
		rs.setInfo(I18n.get("team.manager.leave.other", mmb.name));
		rs.success();
		port.returns(rs);
		//通知队长和队员更新
		updateTeamsRealTime(t, TeamManager.LEAVE);
	}
	/** 离开副本队伍 目前离开队伍和离开组队队伍都是用的这个 */
	@DistrMethod
	public void leaveReqTeam(long humanId) {
		TeamMemberObject mmb = getTeamMember(humanId);
		Team t = map.get(mmb.teamId);
		TeamResult rs = new TeamResult().failure();
		if(t == null){
			rs.setInfo(I18n.get("team.common.noTeam"));//队伍不存在
			port.returns(rs);
			return;
		}
		t.leaveTeam(humanId);//离开队伍
		if(t.isEmpty()){
			rmvMap(t);
		}
		/* 如果是队长离开：重选队长 */
		if(t.isLeader(humanId)){
			if(!t.members.isEmpty()){
				t.leader = t.members.get(0);//晋升队长
				t.leader.setLeader(true);
			}
		}
		rs.setInfo(I18n.get("team.manager.leave.other", mmb.name));
		rs.success();
		port.returns(rs);
		//通知队长和队员更新
		updateTeamsRealTime(t, TeamManager.LEAVE);
	}
	
	/** 玩家上线触发 */
	@DistrMethod
	public void on_login(HumanObject human) {
		if(human == null){
			return;
		}
		TeamMemberObject mmb = getTeamMember(human.id);
		if(mmb == null)//说明自服务启动以来，玩家第一次登陆
		{
			mmb = new TeamMemberObject(human);
			updateTeamMemberMap(mmb);
			return;
		}
		mmb.setMemberOnline();
		Team t = getTeamByHumanId(human.id);
		if(t == null) return;
		
		//将“暂离”改为“上线”
		TeamManager.inst().updateTeamMine(t.getMemberIds(), t, TeamManager.ON_LINE);//更新主界面的自己的队伍信息
	}
	
	/** 更新角色的队伍信息 */
	private void updateTeamMemberMap(TeamMemberObject mmb) {
		teamMemberMap.put(mmb.humanId, mmb);
	}
	
	/** 获得角色的队伍信息,不应返回null */
	private TeamMemberObject getTeamMember(long humanId) {
		return teamMemberMap.get(humanId);
	}

	/** 玩家下线触发 */
	@DistrMethod
	public void on_logout(HumanObject human) {
		TeamMemberObject member = getTeamMember(human.id);
		if(member != null){
			member.setMemberOffline();
		}
		
		Team t = getTeamByHumanId(human.id);
		if(t == null) return;
		
		//将“暂离”改为“上线”
		TeamManager.inst().updateTeamMine(t.getMemberIds(), t, TeamManager.OFF_LINE);//更新主界面的自己的队伍信息
	}
	/** 系统离开队伍 */
	@DistrMethod
	public void leaveTeamSys(long humanId) {
		for(Team t : map.values()){
			if(t.contains(humanId)){
				this.leaveTeamSys(t.teamId, humanId);
			}
		}
	}
	/** 系统离开队伍 */
	@DistrMethod
	public void leaveTeamSys(long teamId, long humanId) {
		Team t = map.get(teamId);
		TeamResult rs = new TeamResult().failure();
		if(t == null){
			rs.setInfo(I18n.get("team.common.noTeam"));//队伍不存在
			port.returns(rs);
			return;
		}
		/* 如果是队长离开：如果是副本队伍重选队长，默认创建类型则解散 */
		if(t.isLeader(humanId)){
			if(t.getType() == TeamCreateType.REP_TEAM.getType()){
				t.leaveTeam(humanId);
				if(t.isEmpty()){
					rmvMap(t);
				}
				if(!t.members.isEmpty()){
					t.leader = t.members.get(0);//晋升队长
				}
			}else{
				t.disband();
			}
		}
		
		rs.setInfo(I18n.get("team.manager.leave.other"));
		rs.success();
		port.returns(rs);
		//通知队长和队员更新
		updateTeamsRealTime(t, TeamManager.LEAVE);
	}
	/** 系统将组员离开队伍 */
	@DistrMethod
	public void leaveReqTeamSys(long teamId, TeamMemberObject mmb) {
		TeamResult rs = new TeamResult().failure();
		Team t = map.get(teamId);
		if(t == null){
			port.returns(rs);
			return;
		}
		if(t.getType() != TeamCreateType.REP_TEAM.getType()){
			port.returns(rs);
			return;
		}
		t.leaveTeam(mmb.humanId);	//从队伍中移除个人
		if(t.isEmpty()){
			rmvMap(t);
		}
//		mmb.clearTeam();			//从个人信息中移除队伍
		/* 重选队长 */
		if(mmb.isLeader()){
			if(!t.members.isEmpty()){
				t.leader = t.members.get(0);//晋升队长
				t.leader.setLeader(true);
			}
		}
		
		//发送离开(系统)消息
		sendSysLeaveMsg(mmb.humanId);
		
		//通知队长和队员更新
		updateTeamsRealTime(t, TeamManager.LEAVE);
		
	}

	private void sendSysLeaveMsg(long humanId) {
		SCTeamRepLeave.Builder msg = SCTeamRepLeave.newBuilder();
		msg.setResult(1);
		msg.setInfo(I18n.get("team.instance.sysleave"));
		HumanGlobalServiceProxy.newInstance().sendMsg(humanId, msg.build());
	}
	/** 
	 * 离开副本队伍序列 
	 * 普通组队队长离开副本组队保留原来的队伍
	 **/
	@DistrMethod
	public void leaveQueue(HumanObject human, int reqId) {
		long humanId = human.id;
		Team t = getTeamByHumanId(humanId);
		TeamResult rs = new TeamResult().failure();
		//队伍不存在
		if(t == null){
			rs.setInfo(I18n.get("team.common.noTeam"));
			port.returns(rs);
			return;
		}
		//队伍还未参加副本
		Map<Long, Team> reqTeams = reqMap.get(reqId);
		if(reqTeams == null || !reqTeams.containsKey(t.teamId)){
			rs.setInfo(I18n.get("team.instance.notreqTeam"));	
			port.returns(rs);
			return;
		}
		//不是队长不可以退出队列
		if(!t.isLeader(human.id)){
			rs.setInfo(I18n.get("team.instance.leaveQueque.onlyleader"));	
			port.returns(rs);
			return;
		}
		
		//清除副本中的队伍
		t = reqTeams.remove(t.teamId);
		t.reqId = 0;
//		t.type = TeamCreateType.DEFAULT_TEAM.getType();
//		
//		long humanId = human.id;
//		Long member = t.leaveTeam(humanId);
//		if(member == null){
//			rs.setInfo(I18n.get("team.common.noTeam"));//队伍不存在
//			port.returns(rs);
//			return;
//		}
//		clearTeamId(member);
		rs.success();
		rs.setInfo(I18n.get("team.common.leave.queue.ok"));
		port.returns(rs);
		//通知队长和队员更新
		updateTeamsRealTime(t, TeamManager.LEAVE_REQ_QUEQUE, reqId);
	}

	/** 获得新的队伍ID */
	public static long makeNewTeamId(){
		return Port.applyId();
	}
	
	/** 建立队伍 */
	@DistrMethod
	public void createTeam(HumanObject leader){
		long teamId = makeNewTeamId();
		Team t = null;
		if(leader != null){
			TeamMemberObject tm = getTeamMember(leader.id);
			t = new Team(tm, teamId, TeamCreateType.DEFAULT_TEAM.getType());
		}
		putMap(t);
		port.returns(t);
	}
	
	/**
	 * 目前通过建立组队副本建立组队
	 * 建立组队副本 
	 * 已经在副本组队不能创建
	 * 已经有普通队伍的直接转为副本队伍
	 * 已经有其他副本队伍给出提示
	 **/
	@DistrMethod
	public void createReqTeam(HumanObject leader, int reqId){
		long humanId = leader.id; 
		Team t = null;//注意
		TeamResult rs = new TeamResult().failure();
		//已经有副本队伍
		if(hasReqTeam(humanId)){
			rs.setInfo(I18n.get("team.instance.haveReqTeam"));
			port.returns(rs);
			return;
		}
		//已经有普通队伍 
		for(Team team: map.values()){
			if(team.getType() != TeamCreateType.DEFAULT_TEAM.getType()){
				continue;
			}
			//是队长则直接创建
			if(team.isLeader(humanId)){
				t = team;
				team.reqId = reqId;//必须在putReqMap(team);之前
				putReqMap(team);
				rs.success();
				rs.setTeam(t);
				port.returns(rs);
				updateTeamsRealTime(t, TeamManager.CREATE_DEFAULT);
				return;
			}
			//是队员则提示不能创建
			if(team.isMember(humanId)){
				rs.setInfo(I18n.get("team.instance.create.haveTeam"));
				port.returns(rs);
				return;
			}
		}
		//创建副本队伍
		long teamId = makeNewTeamId();
		if(leader != null){
			TeamMemberObject tm = getTeamMember(leader.id);
			t = new Team(tm, teamId, TeamCreateType.REP_TEAM.getType());
			t.reqId = reqId;
		}
		putMap(t);
		rs.success().setTeam(t);
		port.returns(rs);
		updateTeamsRealTime(t, TeamManager.CREATE_REQ);
	}
	private boolean hasReqTeam(long memberId){
		for(Map<Long, Team> map : reqMap.values()){
			for(Team t: map.values()){
				if(t.isMember(memberId)){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 根据队伍的副本ID来更新
	 * 除了副本队伍脱离队列，都可以调用此方法
	 */
	private void updateTeamsRealTime(Team t, int changeType) {
		updateTeamsRealTime(t, changeType, t.reqId);
	}
	/**
	 * @param t 新的队伍信息，可能
	 * @param reqId 副本ID，因为有的队伍退出队列，会清空自己的reqId，所以不能从team里边取
	 * @param changeType
	 */
	private void updateTeamsRealTime(Team t, int changeType, int reqId) {
		//目前只有组队副本会有队伍列表
//		if(t.getType() != TeamType.REP_TEAM.getType()){
//			return;
//		}
		Set<Long> set = openedSet.get(reqId);			//打开此副本面板的角色列表
		if(set== null){
			set = new HashSet<Long>();
		}
		List<Long> list = new ArrayList<Long>(set);
		TeamManager.inst().updateTeams(list, t, changeType);
		TeamManager.inst().updateTeamMine(t.getMemberIds(), t, changeType);//更新主界面的自己的队伍信息
	}
	
	/**
	 * 确认对方的邀请组队消息，对方为队长
	 * 返回组队结果
	 * @date 2015年6月2日 下午7:37:46
	 */
	@DistrMethod
	public void inviteConfirm(long mId, long leaderId){
		TeamResult rs = new TeamResult().failure();
		TeamMemberObject mInfo = getTeamMember(mId);
		TeamMemberObject leaderInfo = getTeamMember(leaderId);
		//"我"已经有队伍
		if(mInfo.teamId != 0){
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.haveTeam"));
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.invite.err"));
			port.returns(rs);
			return;
		}
		//对方已经有队伍但不是队长
		if(leaderInfo.hasTeam() && !leaderInfo.isLeader()){
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.haveTeam"));
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.invite.err"));
			port.returns(rs);
			return;
		}
		
		long teamId = makeNewTeamId();
		Team team = map.get(leaderInfo.teamId);
		//新建队伍
		if(leaderInfo.teamId == 0 || team == null || team.isEmpty()){			//邀请者没有队伍
			team = new Team(leaderInfo, mInfo, teamId);
			putMap(team);
			rs.success().setTeam(team);
			port.returns(rs);
			updateTeamsRealTime(team, TeamManager.CREATE_DEFAULT);
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.success", leaderInfo.name));	    //您成功加入了队伍！
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.otherSuccess", mInfo.name));	//玩家{}加入了队伍！
			return;
		}
		
		//加入旧的队伍
		if(team.isFull()){//队伍已满
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.agreeInvite.teamFull"));
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.invite.err"));
			port.returns(rs);
			return;
		}
		
		if(team.addMembers(mInfo)){		//加入成功
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.success", leaderInfo.name));				//您成功加入了队伍！
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.create.succees"));	//玩家{}加入了队伍！
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.otherSuccess", mInfo.name));	//玩家{}加入了队伍！
			updateTeamsRealTime(team, TeamManager.JOIN);
			port.returns(rs);
			return;
		}
		Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.haveTeam"));
		Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.invite.err"));
		port.returns(rs);
		return;
	}
	/**
	 * 确认对方的申请组队消息，对方为准队员
	 */
	@DistrMethod
	public void applyConfirm(long mId, long leaderId){
		TeamResult rs = new TeamResult().failure();
		
		TeamMemberObject mInfo = getTeamMember(mId);
		TeamMemberObject leaderInfo = getTeamMember(leaderId);
		
		//"对方"已经有队伍
		if(mInfo.teamId != 0){
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.haveTeam"));
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.OtherErr"));
			port.returns(rs);
			return;
		}
		//对方已经有队伍但不是队长
		if(leaderInfo.teamId != 0 && !leaderInfo.isLeader()){
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.haveTeam"));
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.OtherErr"));
			port.returns(rs);
			return;
		}
		
		long teamId = makeNewTeamId();
		Team team = map.get(leaderInfo.teamId);
		//新建队伍
		if(leaderInfo.teamId == 0 || team == null || team.isEmpty()){			//邀请者没有队伍
			team = new Team(leaderInfo, teamId, TeamCreateType.DEFAULT_TEAM.getType());
			putMap(team);
			rs.success().setTeam(team);
			port.returns(rs);
			updateTeamsRealTime(team, TeamManager.CREATE_REQ);
			return;
		}
		
		//加入旧的队伍
		if(team.isFull()){//队伍已满
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.teamFull"));
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.OtherErr"));
			port.returns(rs);
			return;
		}
		
		if(team.addMembers(mInfo)){		//加入成功
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.success", mInfo.name));				//您成功加入了队伍！
			Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.otherSuccess", leaderInfo.name));	//玩家{}加入了队伍！
			updateTeamsRealTime(team, TeamManager.JOIN);
			port.returns(rs);
			return;
		}
		Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.err"));
		Inform.user(leaderInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.OtherErr"));
		port.returns(rs);
		return;
	}
	

	/** 
	 * 加入副本队伍 
	 * 返回加入结果，加入成功，队伍不存在，队伍已解散，已经在队伍中
	 */
	@DistrMethod
	public void joinReqTeam(HumanObject recipient, int reqId, long teamId){
		
		long humanId = recipient.id;
		TeamMemberObject mmb = getTeamMember(humanId);
		Team t = map.get(teamId);
		TeamResult rs = new TeamResult().failure();
		rs.setTeam(t);
		if(t == null){
			rs.setInfo(I18n.get("team.common.noTeam"));//队伍不存在
			port.returns("result", rs, "team", t);
			return;
		}
		if(t.contains(humanId)){
			rs.setInfo(I18n.get("team.manager.join.haveThisTeam"));//已经在队伍中
			port.returns("result", rs, "team", t);
			return;
		}
//		if(recipient.id != 0){
//			rs.setInfo(I18n.get("team.manager.join.haveTeam"));//已经有队伍
//		}
		if(!t.addMembers(mmb)){					  //队伍已满
			rs.setInfo(I18n.get("team.instance.full"));       //队伍已满，加入失败
			port.returns("result", rs, "team", t);
		}
		rs.setInfo(I18n.get("team.manager.join.success"));//成功组队
		port.returns("result", rs.success(), "team", t);
		//通知队长和队员更新
		updateTeamsRealTime(t, TeamManager.JOIN);
	}
	/**
	 * 把某个人加入已有的队伍
	 * 用于： ①申请组队，队长同意后将其加入自己的队伍
	 * 		②邀请组队，自己是队长，把对方加进来
	 * 返回加入结果，加入成功，队伍不存在，队伍已解散，已经在队伍中
	 * @param recipient 
	 * @param reqId
	 * @param teamId
	 * @date 2015年6月6日 下午2:29:46
	 */
	@DistrMethod
	public void joinTeam(long recipient, long teamId){
		long humanId = recipient;
		TeamMemberObject mmb = getTeamMember(humanId);
		Team t = map.get(teamId);
		TeamResult rs = new TeamResult().failure();
		if(t == null){
			rs.setInfo(I18n.get("team.common.noTeam"));//队伍不存在
			port.returns(rs);
			return;
		}
		if(t.contains(humanId)){
			rs.setInfo(I18n.get("team.manager.join.haveThisTeam"));//已经在队伍中
			port.returns(rs);
			return;
		}
		if(!t.addMembers(mmb)){					  //队伍已满
			rs.setInfo(I18n.get("team.instance.full"));       //队伍已满，加入失败
			port.returns(rs);
		}
		rs.setTeam(t);
		rs.success();
		rs.setInfo(I18n.get("team.manager.join.success"));//成功组队
		port.returns(rs);
		//通知队长和队员更新
		updateTeamsRealTime(t, TeamManager.JOIN);
	}
	
	/** 
	 * 得到副本队伍 
	 * 返回参加此副本的队伍列表
	 */
	@DistrMethod
	public void opemReqTeamUI(HumanObject humanObj, int reqId){
		Set<Long> set = openedSet.get(reqId);
		if(set == null){
			set = new HashSet<Long>();
			openedSet.put(reqId, set);
		}
 		set.add(humanObj.id);
		port.returns(reqMap.get(reqId));
	}
	
	/** 
	 * 关闭副本组队面板,停止发送自动刷新的消息
	 */
	@DistrMethod
	public void closeReqTeamUI(long humanId, int reqId){
		Set<Long> set = openedSet.get(reqId);
		if(set != null)
			set.remove(humanId);
	}
	
	/** 
	 * 关闭副本组队面板,停止发送自动刷新的消息,用户在打开副本队伍界面退出的时候
	 */
	@DistrMethod
	public void closeReqTeamUI(long humanId){
		for(Set<Long> set : openedSet.values()){
			set.remove(humanId);
		}
	}
	
	/** 添加缓存*/
	private void putMap(Team t) {
		if(t == null){
			return;
		}
		map.put(t.teamId, t);
		/* 副本队伍特殊添加到一个MAP */
		if(t.getType() == TeamCreateType.REP_TEAM.getType()){
			putReqMap(t);
		}
	}
	
	/**
	 * @param t 要保存的队伍
	 * @date 2015年5月28日 下午1:56:47
	 */
	private void putReqMap(Team t) {
		Map<Long, Team> rmap = reqMap.get(t.reqId);
		if(rmap == null){
			rmap = new HashMap<Long, Team>();
		}
		rmap.put(t.teamId, t);
		reqMap.put(t.reqId, rmap);
	}
	/**
	 * 当队伍没有队员的时候必须调用此方法
	 * 清除队伍,从reqMap和map中清除队伍
	 */
	private void rmvMap(Team t) {
		if(t == null){
			return;
		}
		map.remove(t.teamId);
		if(t.reqId == 0){
			return;
		}
		Map<Long, Team> rmap = reqMap.get(t.reqId);
		if(rmap == null){
			return;
		}
		rmap.remove(t.teamId);
	}

	/** 邀请他人组成队伍 */
	@DistrMethod
	public void createTeam(long leader, long recipient){
		TeamMemberObject leaderInfo = getTeamMember(leader);
		TeamMemberObject recipientInfo = getTeamMember(recipient);
		long teamId = makeNewTeamId();
		Team t = null;
		if(leader != 0 && recipient!= 0){
			t = new Team(leaderInfo, recipientInfo, teamId);
		}
		putMap(t);
		port.returns(t);
		updateTeamsRealTime(t, TeamManager.CREATE_DEFAULT);
	}
	
	/** 开战 */
	@DistrMethod
	public void launchReqTeam(HumanObject leader, int req){
		TeamResult rs = new TeamResult().failure();
		Map<Long, Team> teams = reqMap.get(req);
		for(Team team : teams.values()){
			if(team.isLeader(leader.id) && !team.members.isEmpty()){
				rs.setInfo(I18n.get("team.instance.launch.success"));
				rs.success();
				port.returns(rs);
				team.setStatus(TeamStatus.RUN.getType());
				return;
			}
		}
		rs.setInfo(I18n.get("team.instance.launch.err"));
		port.returns(rs);
	}
	
	
	/**
	 * 定时计数   当满员的队伍30秒后满员的队伍未参加副本时
	 */
	@ScheduleMethod("0/1 * * * * ?")
	public void deleteTimeoutMail() {
		for(Map<Long, Team> teamMap : reqMap.values()){
			for (Team t : teamMap.values()) {
//				System.err.println(t.leader.humanId+"is leaader");
//				System.err.println(t.members.size()+"队伍大小");
				if(!t.isFull()){
					fullMap.remove(t.teamId);
					continue;
				}
				Integer count = fullMap.get(t.teamId);
				if(count == null){
					count = 0;
				}
				if(count >= 30){
					leaveReqTeamSys(t.teamId, t.leader);
					fullMap.remove(t.teamId);
					continue;
				}
				fullMap.put(t.teamId, count+1 );
			}
		}
	}
	/*  血量变化，魔法变化，玩家上下线 都会改变队伍成员的状态信息 **/
	@DistrMethod
	public void onHumanAttrChange(HumanObject humanObj) {
		if(humanObj == null){
			return;
		}
		TeamMemberObject mmb = getTeamMember(humanObj.id);
		mmb.resetData(humanObj);
		Team t = getTeamByHumanId(humanObj.id);
		TeamManager.inst().updateTeamMine(t.getMemberIds(), t, TeamManager.ATTR_CHANGE);//更新主界面的自己的队伍信息
	}
	/** 死亡 */
	@DistrMethod
	public void onKilled(HumanObject humanObj) {
		if(humanObj == null){
			return;
		}
		TeamMemberObject mmb = getTeamMember(humanObj.id);
		mmb.resetData(humanObj);
		Team t = getTeamByHumanId(humanObj.id);
		TeamManager.inst().updateTeamMine(t.getMemberIds(), t, TeamManager.ATTR_CHANGE);//更新主界面的自己的队伍信息
	}
}
