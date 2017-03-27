package org.gof.demo.worldsrv.team;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;

/**
 * admin
 */
public class Team implements ISerilizable{
	public static final int MEMBER_STATUS_LEAVE = -1;	//暂离
	public static final int MEMBER_STATUS_ONLINE = 0;	//在线
	
	
	public long teamId; 										// 队伍ID
	public byte type = TeamCreateType.DEFAULT_TEAM.getType(); 		//创建的时候队伍类型
	public byte status;											//队伍状态
	public TeamMemberObject leader;											// 队长
	public List<TeamMemberObject> members = new ArrayList<TeamMemberObject>();			//保存队伍人员
	
	/* 副本队伍ID，副本队伍使用 */
	public int reqId;	
	
	public boolean isEmpty(){
		return members == null || members.isEmpty();
	}
	
	public List<Long> getMemberIds(){
		List<Long> ids = new ArrayList<Long>();
		for (TeamMemberObject memb : members) {
			ids.add(memb.humanId);
		}
		return ids;
	}
	//是否是队员
	public boolean isMember(Long humanId){
		for (TeamMemberObject o : members) {
			if(o.humanId == humanId){
				return true;
			}
		}
		return false;
	}
	//获得队员
	public TeamMemberObject getMember(Long humanId){
		for (TeamMemberObject o : members) {
			if(o.humanId == humanId){
				return o;
			}
		}
		return null;
	}
	
	public Team(){}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(teamId);
		out.write(type);
		out.write(status);
		out.write(leader);
		out.write(members);
		out.write(reqId);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		teamId = in.read();
		type = in.read();
		status = in.read();
		leader = in.read();
		members = in.read();
		reqId = in.read();
	}
	
	/** 增加队员 */
	public boolean addMembers(TeamMemberObject recipient){
		if(isFull()){
			return false;
		}
		this.members.add(recipient);
		recipient.setTeamId(this.teamId);
		return true;
	}
	/** 设置队长 */
	public boolean setLeader(TeamMemberObject leader){
		if(addMembers(leader)){
			this.leader = leader;
			leader.setLeader(true);
			return true;
		}
		return false;
	}
	/** 删除队员 */
	public void rmvMember(TeamMemberObject mmb){
		this.members.remove(mmb);
		mmb.clearTeam();
	}
	
	
	public boolean hasOfflineMember(){
		for(TeamMemberObject member : members){
			if(member.status == MEMBER_STATUS_LEAVE){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param index，不能传入humanID
	 * @return
	 * @date 2015年5月22日 下午8:10:27
	 */
	public TeamMemberObject rmvMember(int index){
		return this.members.remove(index);
	}

	/** 队伍类型 **/
	public void setType(byte teamType){
		this.type = teamType;
	}

	public byte getType(){
		return this.type;
	}
	
	/** 队伍状态 */
	public void setStatus(byte status) {
		this.status = status;
	}
	
	public byte getStatus(){
		return this.status;
	}
	
	/** 队伍人数 */
	public int getPlayerNum() {
		return members.size();
	}
	
	/** 队伍在线人数 **/
	public int getOnlinePlayerNum(){
		return this.members.size();
	}
	/** 队长邀请组队 */
	public Team(TeamMemberObject leader, TeamMemberObject recipient, long teamId){
		this.teamId = teamId;
		this.leader = leader;
		this.leader.setLeader(true);
		addMembers(leader);
		addMembers(recipient);
		this.setStatus(TeamStatus.PUBLIS.getType());
	}
	/** 队长邀请组队 */
	public Team(TeamMemberObject leader, TeamMemberObject recipient, long teamId, byte teamType){
		this.teamId = teamId;
		this.leader = leader;
		this.leader.setLeader(true);
		addMembers(leader);
		addMembers(recipient);
		this.setStatus(TeamStatus.PUBLIS.getType());
		this.setType(teamType);
	}
	
	public Team(TeamMemberObject leader, long teamId){
		this.teamId = teamId;
		this.leader = leader;
		this.leader.setLeader(true);
		addMembers(leader);
		setStatus(TeamStatus.PUBLIS.getType());
	}
	
	public Team(TeamMemberObject leader, long teamId, byte teamType){
		this.teamId = teamId;
		this.leader = leader;
		this.leader.setLeader(true);
		addMembers(leader);
		this.setType(teamType);
		this.setStatus(TeamStatus.PUBLIS.getType());
	}
	
	/** 队伍是否满员 **/
	public boolean isFull(){
		return members.size() >= TeamCreateType.getTeamType(this.type).getMaxNum();
	}
	
	/** 能否离开队伍 **/
	public boolean canLeave(){
		return true;
	}
	
	/** 能否解散队伍 **/
	public boolean canDisband(){
		return true;
	}
	
	/** 能否更换队长 **/
	public boolean canChangeLeader(){
		return true;
	}
	
	/** 离开队伍(离队者，离队类型) service调用 **/
	TeamMemberObject leaveTeam(long humanId){
		TeamMemberObject h = null;
		Iterator<TeamMemberObject> iterator = members.iterator();  
		while(iterator.hasNext()){  
			h = iterator.next();  
			if(h.humanId == humanId){  
				iterator.remove(); 
				h.clearTeam();
				return h;
			}  
		} 
		return h;
	}
	
	/** 队伍最大人数 */
	public int getMaxPlayerNum(){
		return TeamCreateType.getTeamType(this.type).getMaxNum();
	}
	
	/** 强制解散队伍 **/
	public boolean enforceDisband(){
		this.leader = null;
		this.members.clear();
		return true;
	}

	/** 加入队伍 * */
	public void memberJoins(TeamMemberObject human) {
		if(!addMembers(human)){
			return;
		}
		if (leader == null) {
			this.leader = human;
			this.leader.setLeader(true);
		}
	}
	
	/** 是否为队长  **/
	public boolean isLeader(TeamMemberObject human){
		if(human == null){
			return false;
		}
		return isLeader(human.humanId);
	}
	/** 是否为队长 **/
	public boolean isLeader(long humanId){
		if(humanId == 0){
			return false;
		}
		if(leader == null){
			return false;
		}
		if(this.leader.humanId == humanId){
			return true;
		}
		return false;
	}
	
	/** 解散队伍 **/
	public boolean disband(){
		if(canDisband()){
			return enforceDisband();
		}
		return false;
	}
	
	/** 更换队长 **/
	public boolean changeLeader(TeamMemberObject human){
		if (!canChangeLeader()) {
			return false;
		}
		if (human == null) {
			return false;
		}
		this.leader = human;
		return true;
	}
	

	/**
	 * 队伍中是否包含某个角色
	 */
	public boolean contains(long humanId){
		try {
			for(TeamMemberObject member : this.members){
				if(member.humanId == humanId){
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public void setMemberOnline(long id) {
		setMemberStatus(id, Team.MEMBER_STATUS_ONLINE);
	}
	
	public void setMemberOffline(long id) {
		setMemberStatus(id, Team.MEMBER_STATUS_LEAVE);
	}

	private void setMemberStatus(long id, int status) {
		for (TeamMemberObject teamMemberObject : members) {
			if(teamMemberObject.humanId == id){
				teamMemberObject.status = status;
				break;
			}
		}
	}
}
