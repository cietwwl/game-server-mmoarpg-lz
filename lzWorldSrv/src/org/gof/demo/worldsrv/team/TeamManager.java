package org.gof.demo.worldsrv.team;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfTeamRep;
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import org.gof.demo.worldsrv.human.HumanGlobalServiceProxy;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DTeamMemberItem;
import org.gof.demo.worldsrv.msg.Msg.DTeamRepItem;
import org.gof.demo.worldsrv.msg.Msg.SCTeamApply;
import org.gof.demo.worldsrv.msg.Msg.SCTeamInvite;
import org.gof.demo.worldsrv.msg.Msg.SCTeamMine;
import org.gof.demo.worldsrv.msg.Msg.SCTeamRepKick;
import org.gof.demo.worldsrv.msg.Msg.SCTeamRepLeave;
import org.gof.demo.worldsrv.msg.Msg.SCTeamReqOpenUIResult;
import org.gof.demo.worldsrv.msg.Msg.SCTeamReqSelfResult;
import org.gof.demo.worldsrv.msg.Msg.SCTeamReqUpdateRealTime;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.google.protobuf.Message;
/**
 * 组队
 * 需要考虑的情况：
 * 		更换队长时掉线
 * 		队长离线后，补队长	
 *      玩家离线自动退出队伍
 * 需要通不在线的情况：
 *      添加队友，对方不在线
 * 删除不需通知
 */
public class TeamManager extends ManagerBase {
	/* 队伍变化 */
	protected static final int CREATE_DEFAULT = 0;		//建立队伍，默认
	protected static final int CREATE_REQ = 1;			//建立副本队伍
	protected static final int ADD_REQ = 8;			//建立副本队伍
	protected static final int LEAVE_REQ_QUEQUE = 2;		//队长退出副本队列
	protected static final int TEAM_DISBANDED = 3;		//解散
	
	protected static final int KICK = 4;		//踢人
	protected static final int LEAVE = 5;		//离队			
	protected static final int JOIN = 6; 		//入队
	
	protected static final int DEFAULT = 7;		//其他(自己主动查询队伍变化时候的情况)
	protected static final int ON_LINE = 8;	//上线
	protected static final int OFF_LINE = 9;	//下线
	protected static final int ATTR_CHANGE = 10;	//属性变化
		
	/**
	 * 获取实例
	 */
	public static TeamManager inst() {
		return inst(TeamManager.class);
	}
	
	/**
	 * 开战
	 * @param humanObj
	 * @param reqId
	 */
	public void launch(HumanObject leader, int reqId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.launchReqTeam(leader, reqId);
		prx.listenResult(this::_result_launchReqTeam, "humanObj", leader);
	}
	
	private void _result_launchReqTeam(Param results, Param context){
		TeamResult rs = results.get();
		if(rs.isSuccess()){//可以开战, 则开战
//			InstanceManager.inst().create(humanObj, confRep);
		}
		HumanObject humanObj = context.get("humanObj");
		Inform.user(humanObj.id, Inform.提示操作, rs.getInfo());
	}
	
	/**
	 * 发布队伍更新的消息
	 * 通过远程获得变化的队伍的成员信息
	 * @param humanList 打开此副本面板的角色列表
	 * @param t 那个队伍发生变化
	 * @param changeType 变化的类型
	 * @date 2015年5月25日 下午6:57:44
	 */
	public void updateTeams(List<Long>humanList, Team t, int changeType){
		if(t == null){
			return;
		}
//		if(t.getType() != TeamCreateType.REP_TEAM.getType()){
		if(t.reqId == 0 && LEAVE_REQ_QUEQUE != changeType){//t.reqId == 0 && LEAVE_REQ_QUEQUE == changeType的情况为 队伍离开副本队伍
			return;
		}
		
		SCTeamReqUpdateRealTime.Builder msg = SCTeamReqUpdateRealTime.newBuilder();
		DTeamRepItem repItem = null;
		switch (changeType) {
		case LEAVE_REQ_QUEQUE:
		case TEAM_DISBANDED:
			repItem = getDTeamReqTeamEmpty(t.teamId);
			break;
		case LEAVE:
			if(t.members.isEmpty()){
				repItem = getDTeamReqTeamEmpty(t.teamId);
			}else{
				repItem = getDTeamReqTeam(t);
			}
			break;
		default:
			repItem = getDTeamReqTeam(t);
			break;
		}
		msg.setRep(repItem);
		//调用远程服务发送消息
		sendAll(humanList, msg.build());
	}
	/**
	 * 更新自己队伍的信息
	 * 队长和所有的队员都会收到
	 * @param ids 要发给谁
	 * @param t	队伍
	 * @param changeType
	 * @date 2015年6月5日 下午6:23:49
	 */
	public void updateTeamMine(List<Long> ids, Team t, int changeType){
		if(t == null){
			return;
		}
		if(changeType == LEAVE_REQ_QUEQUE){
			return;
		}
//		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
//		prx.getInfos(t.members);
//		prx.listenResult(this::_result_getInfos_updateTeamMine, "ids", ids, "team", t, "changeType", changeType);
//	}
//	/**
//	 * 通知队伍更新的条件：
//	 * ①玩家打开副本队伍的面板
//	 * ②队伍为副本队伍
//	 * @param results
//	 * @param context
//	 * @date 2015年5月25日 下午8:38:17
//	 */
//	private void _result_getInfos_updateTeamMine(Param results, Param context) {
//		//需要发给的角色列表
//		Team t = context.get("team");
//		int changeType = context.get("changeType");
//		List<Long> ids = context.get("ids");
//		List<HumanGlobalInfo> infos = results.get();
		
		SCTeamMine.Builder msg = SCTeamMine.newBuilder();
		msg.setLeaderId(t.leader.humanId);
		if(t == null || t.isEmpty()){
			//调用远程服务发送消息
			sendAll(t.getMemberIds(), msg.build());
			return;
		}
		for(TeamMemberObject h : t.members){
			DTeamMemberItem.Builder mmb = DTeamMemberItem.newBuilder();
			mmb.setHumanId(h.humanId);
			mmb.setHeadSn(h.modelSn);
			mmb.setName(h.name);
			mmb.setLevel(h.level);
			mmb.setStatus(h.status);
			mmb.setLevel(h.level);
			mmb.setHpCur(h.hpCur);
			mmb.setHpMax(h.hpMax);
			mmb.setMpCur(h.mpCur);
			mmb.setMpMax(h.mpMax);
			msg.addMembers(mmb.build());
		}
		msg.setTeamId(t.teamId);
		//调用远程服务发送消息
		sendAll(ids, msg.build());
	}
	private void sendAll(List<Long> humanList, Message msg) {
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendMsgToAll(humanList, msg);
	}
	/*  跟新单个人的队伍信息 */
	/**
	 * 3304:获得玩家所在队伍信息
	 * 获得玩家的队伍ID
	 * 获得队伍
	 * （未使用）
	 */
	public void getTeam(HumanObject humanObj){
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.getTeam(humanObj);
		prx.listenResult(this::_result_getTeam,  "humanObj", humanObj);
	}
	
	private void _result_getTeam(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");
		Team t = results.get();
		if(t == null){
			return;
		}
		SCTeamReqSelfResult.Builder  msg = SCTeamReqSelfResult.newBuilder();
		DTeamRepItem reqItem = getDTeamReqTeam(t);
		msg.setRep(reqItem);
		//发送消息
		humanObj.sendMsg(msg);
	}
	/**
	 * 不在用globalInfo的信息，不再使用此方法
	 */
	@Deprecated
	private DTeamRepItem getDTeamReqTeam(List<HumanGlobalInfo> infos, long teamId) {
		if(infos == null || infos.isEmpty()){
			return null;
		}
		DTeamRepItem.Builder reqItem = DTeamRepItem.newBuilder();
		reqItem.setTeamId(teamId);
		for(HumanGlobalInfo h : infos){
			DTeamMemberItem.Builder mmb = DTeamMemberItem.newBuilder();
			mmb.setHumanId(h.id);
			mmb.setName(h.name);
			mmb.setLevel(h.level);
//			mmb.setHpCur(value)
//			mmb.setStatus(value)
			if(h.sn != null && !"".equals(h.sn))
				mmb.setHeadSn(h.sn);
			reqItem.addMembers(mmb.build());
		}
		return reqItem.build();
	
	}
	/**
	 * @param infos
	 * @param teamId 队伍ID
	 * @return
	 * @date 2015年5月25日 下午8:50:43
	 */
	private DTeamRepItem getDTeamReqTeam(Team team) {
		if(team == null || team.isEmpty()){
			return DTeamRepItem.newBuilder().build();//返回空
		}
		DTeamRepItem.Builder reqItem = DTeamRepItem.newBuilder();
		reqItem.setTeamId(team.teamId);
		for(TeamMemberObject h : team.members){
			DTeamMemberItem.Builder mmb = DTeamMemberItem.newBuilder();
			mmb.setHumanId(h.humanId);
			mmb.setHeadSn(h.modelSn);
			mmb.setName(h.name);
			mmb.setLevel(h.level);
			mmb.setStatus(h.status);
			mmb.setLevel(h.level);
			mmb.setHpCur(h.hpCur);
			mmb.setHpMax(h.hpMax);
			mmb.setMpCur(h.mpCur);
			mmb.setMpMax(h.mpMax);
			reqItem.addMembers(mmb.build());
		}
		return reqItem.build();
		
	}
	/**
	 * 队伍置空
	 * @param teamId
	 * @date 2015年5月25日 下午8:46:32
	 */
	private DTeamRepItem getDTeamReqTeamEmpty(long teamId) {
		DTeamRepItem.Builder reqItem = DTeamRepItem.newBuilder();
		reqItem.setTeamId(teamId);
		return reqItem.build();
	}
	/**
	 * 3302: 获得参加某个副本的所有队伍信息 
	 * @param humanObj
	 * @param reqId
	 * @date 2015年5月26日 上午9:54:44
	 */
	public void OpenUI(HumanObject humanObj, int reqId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.opemReqTeamUI(humanObj, reqId);
		prx.listenResult(this::_result_opemReqTeamUI,  "humanObj", humanObj);
	}
	private void _result_opemReqTeamUI(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");
		//3302
		SCTeamReqOpenUIResult.Builder  msg = SCTeamReqOpenUIResult.newBuilder();
		Map<Long, Team> map = results.get();
		if(map == null || map.isEmpty()){
//			msg.setTeamId(humanObj.teamId);
			//发送消息
			humanObj.sendMsg(msg);
			return;
		}
		
		Team myTeam = null;
		
		for (Team t : map.values()) {
			if(t == null){
				continue;
			}
			if(t.isMember(humanObj.id))//寻找自己的队伍
			{
				myTeam = t;
			}
		}
		
		//3302
	    for(Map.Entry<Long, Team> entry: map.entrySet()){
	        Object key = entry.getKey();
	        if(key == null){
	        	continue;
	        }
	        Long teamId = Long.valueOf(key.toString());
	        Team team = entry.getValue();
	        DTeamRepItem reqItem = getDTeamReqTeam(team);
	        if(reqItem == null){
	        	continue;
	        }
	        msg.addReps(reqItem);
	        //设置自己的队伍信息
	        if(team.contains(humanObj.id)){
	        	msg.setTeamId(teamId);
	        	System.out.println("找到自己的TEAMID");
	        }
	    }
	    if(myTeam != null && !myTeam.members.isEmpty()){
	    	msg.setTeamCreateType(myTeam.type);
	    }
		//发送消息
		humanObj.sendMsg(msg);
	}
	
	/**  
	 * 3307 创建副本队伍 
	 * 如果已经有在队伍里则不能创建
	 * */
	public void create(HumanObject humanObj, int reqId) {
		//判断是否可以创建队伍
		int level = humanObj.getHuman().getLevel();
		ConfTeamRep cf  = ConfTeamRep.getBy("insId", reqId);
		if(cf.levelMin > level){
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.manager.create.fail.level", cf.levelMin));
			return;
		}
		
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.createReqTeam(humanObj, reqId);
		prx.listenResult(this::_result_createReqTeam,  "humanObj", humanObj, "reqId", reqId);
	}
	private void _result_createReqTeam(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");
		int reqId = context.get("reqId");
		//3307
		TeamResult rs = results.get();
		if(!rs.isSuccess()){
			Log.team.info("{}创建组队副本失败!reqId={}",humanObj.name, reqId);
			
//			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.manager.create.fail"));
			Inform.user(humanObj.id, Inform.提示操作, rs.getInfo());
			return;
		}
		Team t = rs.getTeam();
		if(t == null){
			Log.team.info("{}创建组队副本失败!reqId={}",humanObj.name, reqId);
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.manager.create.fail"));
			return;
		}
		Log.team.info("{}创建组队副本成功!reqId={}",humanObj.name, reqId);
		Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.manager.create.succees"));
		//触发更新面板消息
		OpenUI(humanObj, reqId);
	}
	
	/** 3303 加入副本队伍 */
	public void join(HumanObject humanObj, int reqId, long teamId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.joinReqTeam(humanObj, reqId, teamId);
		prx.listenResult(this::_result_joinReqTeam,  "humanObj", humanObj, "reqId", reqId);
	}
	private void _result_joinReqTeam(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");
		int reqId = context.get("reqId");
		//3309
		TeamResult rs = results.get("result");
		if(rs == null || !rs.isSuccess()){
			Log.team.info("{}加入副本队伍错误!{}",humanObj.name);
			Inform.user(humanObj.id, Inform.提示操作, rs.getInfo());
//			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.common.error"));
			return;
		}
		Inform.user(humanObj.id, Inform.提示操作, rs.getInfo());
		//触发更新面板消息
		OpenUI(humanObj, reqId);
	}
	
	
	/** 
	 * 3309 退出队伍
	 * 获得HumanGlobalInfo  
	 */
	public void leaveReq(HumanObject humanObj) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.leaveReqTeam(humanObj.id);
		prx.listenResult(this::_result_leaveReq,  "humanObj", humanObj);
	}
	private void _result_leaveReq(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");
		//3309
		TeamResult rs = results.get();
		if(rs == null || !rs.isSuccess()){
			Log.team.info("{}踢人错误!{}",humanObj.name);
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.common.error"));
			return;
		}
		Inform.user(humanObj.id, Inform.提示操作, rs.getInfo());
		SCTeamRepLeave.Builder msg = SCTeamRepLeave.newBuilder();
		msg.setInfo(rs.getInfo());
		msg.setResult(rs.getResult());
		humanObj.sendMsg(msg);
		//退出队伍需要清空主界面的队伍信息，用发送空的队伍来实现
		updateMineTeamEmpty(humanObj.id);
	}

	private void updateMineTeamEmpty(long humanId) {
		SCTeamMine.Builder m = SCTeamMine.newBuilder();
		HumanGlobalServiceProxy.newInstance().sendMsg(humanId, m.build());
	}
	
	/** 3311 踢人 */
	public void kick(HumanObject humanObj, long memberId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		long teamId = humanObj.teamId;
		prx.kickMember(teamId, humanObj, memberId);
		prx.listenResult(this::_result_kickReqTeam,  "humanObj", humanObj);
	}
	
	private void _result_kickReqTeam(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");
		//3311
		TeamResult rs = results.get();
		if(rs == null || !rs.isSuccess()){
			Log.team.info("{}踢人错误!{}",humanObj.name);
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.common.error"));
			return;
		}
		Inform.user(humanObj.id, Inform.提示操作, rs.getInfo());
		SCTeamRepKick.Builder msg = SCTeamRepKick.newBuilder();
		msg.setInfo(rs.getInfo());
		msg.setResult(rs.getResult());
		humanObj.sendMsg(msg);
	}
	public void CloseUI(long humanId, int reqId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.closeReqTeamUI(humanId, reqId);
	}
	public void CloseUI(long humanId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.closeReqTeamUI(humanId);
	}

	
	/**
	 * 召唤队友参加副本队伍 
	 */
	public void call(HumanObject humanObj, int reqId) {
		//获得自己的队伍
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.getHumanReqTeam(humanObj, reqId);
		prx.listenResult(this::_result_getHumanReqTeam_call, "humanObj", humanObj, "reqId", reqId);	
	}
	
	private void _result_getHumanReqTeam_call(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		int reqId = context.get("reqId");
		long humanId = humanObj.getHumanId();
		//队伍已满，提示
		Team t = results.get();
		//没有队伍
		if(t == null){
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.common.noTeam"));
			return;
		}
		//只有队长可以召唤队友
		if(!t.isLeader(humanId)){
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.manager.handle.notLeader"));
			return;
		}
		if(t.isFull()){
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.manager.agreeInvite.teamFull"));
			return;
		}
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.getTeamMembersWithOutTeamDistr();
		prx.listenResult(this::_result_getTeamMembersWithOutTeamDistr, "humanObj", humanObj, "team", t, "reqId", reqId);	
	}
	/**
	 * 召唤潜在的好友参加副本组队！
	 * @param results
	 * @param context
	 * @date 2015年6月10日 下午3:44:15
	 */
	private void _result_getTeamMembersWithOutTeamDistr(Param results, Param context){
		List<TeamMemberObject> mms = results.get();
		HumanObject humanObj = context.get("humanObj");
		Team team = context.get("team");
		int reqId = context.get("reqId");
		String msg = MessageFormat.format("玩家[url=humanId:{0}] [u]{1}[/u] [/url]等级{2}级，在XXX副本战斗，[url=teamId:{3}:{4}][u]加入队伍[/u][/url]和他并肩作战吧！", 
				humanObj.id, humanObj.name, humanObj.getHuman().getLevel(), String.valueOf(team.teamId), reqId);
		//召唤没有组队的玩家参加副本队伍
		for (TeamMemberObject m : mms) {
			Inform.user(m.humanId, Inform.征召, msg);
			
		}
		Inform.user(humanObj.id, Inform.征召, msg);
	}
	
	/**
	 * 通队伍·邀请对方和自己组队，自己为队长 
	 * 只有自己是队长或者没有队伍的时候可以邀请对方
	 */
	public void invite(HumanObject humanObj, long humanId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		ArrayList<Long> ids = new ArrayList<Long>();
		ids.add(humanObj.id);
		ids.add(humanId);
		prx.getTeamMembersDistr(ids);//获取自己和对方的信息
		prx.listenResult(this::_result_getTeamMembersDistr, "humanObj", humanObj, "humanId", humanId);	
	}
	private void _result_getTeamMembersDistr(Param results, Param context){
		List<TeamMemberObject> mms = results.get();
		TeamMemberObject mInfo = mms.get(0); //邀请者
		TeamMemberObject pInfo = mms.get(1); //被邀请者
		
		//"我"有队伍但不是队长，不能邀请对方
		if(mInfo.teamId != 0 && !mInfo.isLeader()){
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.handle.notLeader"));//"team.common.noTeam"
			return;
		}
		//对方已经有队伍
		if(pInfo.teamId != 0){
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.invite.haveteam", pInfo.name));
			return;
		}
		//给对方发送邀请组队消息
		SCTeamInvite.Builder msg = SCTeamInvite.newBuilder();
		msg.setName(mInfo.name);
		msg.setMemberId(mInfo.humanId);//邀请者
		HumanGlobalServiceProxy.newInstance().sendMsg(pInfo.humanId, msg.build());
	}
	
	/**
	 * 接受对方的组队，对方为队长
	 * 获得双方的信息，判断条件后进行组队
	 */
	public void inviteConfirm(HumanObject humanObj, long leaderId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.inviteConfirm(humanObj.id, leaderId);
	}
	
	private void _result_createTeam_getInfo_invite(Param results, Param context){
		HumanGlobalInfo mInfo = context.get("mInfo");
		HumanGlobalInfo leaderInfo = context.get("leaderInfo");//leader
		Team t = results.get();
		if(t== null){
			Inform.user(leaderInfo.id, Inform.提示操作, leaderInfo.name + "邀请对方组队失败");
			Inform.user(mInfo.id, Inform.提示操作, mInfo.name + "接受对方对方组队邀请失败");
			return;
		}
		updateTeamMine(t.getMemberIds(), t, CREATE_DEFAULT);
	}
	
	/**
	 * 普通组队·申请加入到对方的队伍
	 */
	public void apply(HumanObject humanObj, long humanId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		ArrayList<Long> ids = new ArrayList<Long>();
		ids.add(humanObj.id);
		ids.add(humanId);
		prx.getTeamMembersDistr(ids);//获取自己和对方的信息
		prx.listenResult(this::_result_getInfo_apply, "humanObj", humanObj, "humanId", humanId);	
	}
	private void _result_getInfo_apply(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");
		List<TeamMemberObject> mms = results.get();
		if(mms == null || mms.size() != 2){
			Inform.user(humanObj.id, Inform.提示操作, humanObj.name + "邀请对方组队失败");
			return;
		}
		TeamMemberObject mInfo = mms.get(0); //申请者“我”
		TeamMemberObject pInfo = mms.get(1); //拥有队伍人员
		
		//"我"有队伍
		if(mInfo.teamId != 0){
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.manager.join.haveTeam"));
			return;
		}
		//对方没有有队伍
		if(pInfo.teamId == 0){
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("team.manager.join.haveNoTeam"));
			return;
		}
		//找到队长发送组队消息面板
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.getTeam(pInfo.teamId);
		prx.listenResult(this::_result_getTeam_getInfo_apply, "mInfo", mInfo, "pInfo", pInfo);	
	}
	
	private void _result_getTeam_getInfo_apply(Param results, Param context){
		Team team = results.get();
		TeamMemberObject mInfo = context.get("mInfo");//申请者“我”
		TeamMemberObject pInfo = context.get("pInfo");
		if(team == null || team.isFull()){
			Inform.user(mInfo.humanId, Inform.提示操作, I18n.get("team.manager.join.applyEnough"));
			return;
		}
		//给对方队长发送邀请组队消息
		SCTeamApply.Builder msg = SCTeamApply.newBuilder();
		msg.setName(I18n.get("team.manager.join.otherJoin", mInfo.name));
		msg.setMemberId(mInfo.humanId);//申请者
		HumanGlobalServiceProxy.newInstance().sendMsg(team.leader.humanId, msg.build());
	}
	
	/**
	 * 接受对方的组队，我为队长
	 * 获得双方的信息，判断条件后进行组队
	 */
	public void applyConfirm(HumanObject leader, long recipient) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		ArrayList<Long> ids = new ArrayList<Long>();
		ids.add(leader.id);	//队长，我
		ids.add(recipient);	//申请者
		prx.getTeamMembersDistr(ids);	//获取自己和对方的信息
		prx.listenResult(this::_result_getInfo_applyConfirm, "humanObj", leader);	
	}
	
	private void _result_getInfo_applyConfirm(Param results, Param context){
		List<TeamMemberObject> mms = results.get();
		HumanObject leader = context.get("humanObj");
		if(mms == null || mms.size() != 2){
			Inform.user(leader.id, Inform.提示操作, leader.name + "接受对方对方组队申请失败");
			return;
		}
		TeamMemberObject mInfo = mms.get(0); //队长，我
		TeamMemberObject pInfo = mms.get(1); //申请者
		
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.joinTeam(pInfo.humanId, mInfo.teamId);
		prx.listenResult(this::_result_joinTeam_getInfo_apply, "mInfo", mInfo, "pInfo", pInfo);
	}
	private void _result_joinTeam_getInfo_apply(Param results, Param context){
		HumanGlobalInfo mInfo = context.get("mInfo");//我，队长
		HumanGlobalInfo pInfo = context.get("pInfo");//l
		TeamResult rs = results.get();
		if(!rs.isSuccess() || rs.getTeam() == null || rs.getTeam().isEmpty()){
			Inform.user(pInfo.id, Inform.提示操作, pInfo.name + "向对方申请组队失败");
			Inform.user(mInfo.id, Inform.提示操作, mInfo.name + "接受对方对方组队申请失败");
			return;
		}
		Inform.user(pInfo.id, Inform.提示操作, pInfo.name + "申请组队成功");
		Inform.user(mInfo.id, Inform.提示操作, mInfo.name + "组队成功");
//		updateTeamMine(t.members, t, CREATE_DEFAULT);//此处可能重复
	}
	
	/**
	 * 获取自己的队伍信息
	 * @param humanObj
	 * @date 2015年6月5日 下午6:36:20
	 */
	public void getMyTeam(HumanObject humanObj) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.getTeam(humanObj);
		prx.listenResult(this::_result_getInfo_getMyTeam, "humanObj", humanObj);	
	}
	private void _result_getInfo_getMyTeam(Param results, Param context){
		Team t = results.get();
		HumanObject humanObj = context.get("humanObj");
		List<Long> ids = new ArrayList<Long>();
		ids.add(humanObj.id);
		updateTeamMine(ids, t, DEFAULT);
	}
	/**
	 * 队长点击“离开副本队列”，副本队伍离开副本序列
	 * 如果是用普通队伍创建的，整队人离开副本队伍
	 * 如果是副本队伍，则走离开队伍的逻辑
	 * @param humanObj
	 * @param reqId
	 */
	public void leaveQueue(HumanObject humanObj, int reqId) {
		TeamGlobalServiceProxy prx = TeamGlobalServiceProxy.newInstance();
		prx.leaveQueue(humanObj, reqId);
		prx.listenResult(this::_result_leaveQueue, "humanObj", humanObj);
	}
	
	private void _result_leaveQueue(Param results, Param context){
		TeamResult rs  = results.get();
		HumanObject humanObj = context.get("humanObj");
		Inform.user(humanObj.id, Inform.提示操作, rs.getInfo());
//		updateTeamMine(ids, t, DEFAULT);
	}
	
	/**
	 * 玩家下线触发离开队伍
	 * @param param
	 * @date 2015年5月29日 下午5:49:15
	 */
	@Listener(EventKey.HUMAN_LOGOUT)
	public void onLogout(Param param) {
  		HumanObject humanObj = param.get("humanObj");
  		//玩家下线，将玩家从打开副本队伍的UI中清除
		this.CloseUI(humanObj.id);
		//玩家所在的队伍里将玩家标记为"暂离"
		TeamGlobalServiceProxy.newInstance().on_logout(humanObj);
	}
	/**
	 * 确保玩家上线无队伍
	 * @param param
	 */
	@Listener(EventKey.HUMAN_LOGIN)
	public void onHumanLogin(Param param) {
		HumanObject humanObj = param.get("humanObj");
		//玩家所在的队伍里将玩家状态从"暂离"改为"在线"
		TeamGlobalServiceProxy.newInstance().on_login(humanObj);
	}
	/*  血量变化，魔法变化，玩家上下线 都会改变队伍成员的状态信息 **/
	/** 掉血 */
	@Listener(EventKey.HUMAN_HPLOSS)
	public void _listener_HUMAN_HPLOSS(Param param) {
//		UnitObject o = param.get("humanObj");
//		if(o instanceof HumanObject){
//			HumanObject humanObj = (HumanObject)o;
//			TeamGlobalServiceProxy.newInstance().onHumanAttrChange(humanObj);
//		}
	}
	/** 死亡 */
	@Listener(EventKey.HUMAN_BE_KILLED)
	public void _listener_HUMAN_BE_KILLED(Param param) {
		HumanObject humanObj = param.get("dead");
		TeamGlobalServiceProxy.newInstance().onKilled(humanObj);
	}
}