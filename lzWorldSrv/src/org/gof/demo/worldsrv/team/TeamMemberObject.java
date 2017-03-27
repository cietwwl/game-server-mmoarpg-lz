package org.gof.demo.worldsrv.team;

import java.io.IOException;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.DTeamMemberItem;

public class TeamMemberObject  implements ISerilizable {
	public long teamId;			//队伍ID	
	public boolean isLeader;	//是否为队长
	/* 以下是组队消息需要的字段 */
	public long humanId;     	//队员ID(包含队长)
	public String modelSn;     	//队员头像
	public String name;       	//队员名字
	public int level;  			//等级
	
	public int status;  		//-1:离线 0：正常
	
	public int hpCur;			//当前血量	
	public int hpMax;			//最大血量
	public int mpCur;			//当前法力
	public int mpMax;			//最大法力
	/**
	 * 必须有默认的无参构造方法才会成功序列化
	 */
	public TeamMemberObject(){}
	public boolean isLeader(){
		return isLeader;
	}
	
	public boolean hasTeam(){
		return teamId != 0;
	}
	
	public void setLeader(boolean isLeader){
		this.isLeader = isLeader;
	}
	public void setTeamId(long teamId){
		this.teamId = teamId;
	}
	
	public void clearTeam(){
		this.teamId = 0;
		setLeader(false);
	}
	/**
	 * 只有角色上线的时候可以调用此方法，其他时候都是从TeamService的 getTeamMember()中取，切记！
	 * @param h
	 */
	public TeamMemberObject(HumanObject h) {
		humanId = h.id;
		modelSn = h.getHuman().getModelSn();
		name = h.getHuman().getName();
		level = h.getHuman().getLevel();
		
//		status = //默认值为0，表示在线
		
		hpCur = h.getHuman().getHpCur();
		hpMax = h.getHuman().getHpMax();
		mpCur = h.getHuman().getMpCur();
		mpMax = h.getHuman().getMpMax();
	}
	/**
	 * 当血量变化,魔法变化的时候，要获取最新的血量可调用此方法
	 */
	public void resetData(HumanObject h) {
		if(h == null){
			return;
		}
		level = h.getHuman().getLevel();
		hpCur = h.getHuman().getHpCur();
		hpMax = h.getHuman().getHpMax();
		mpCur = h.getHuman().getMpCur();
		mpMax = h.getHuman().getMpMax();
	}
	public TeamMemberObject(HumanObject h, long teamId, boolean isLeader) {
		this(h);
		this.teamId = teamId;
		this.isLeader = isLeader;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(teamId);
		out.write(isLeader);
		out.write(humanId);
		out.write(modelSn);
		out.write(name);
		out.write(level);
		out.write(status);
		out.write(hpCur);
		out.write(hpMax);
		out.write(mpCur);
		out.write(hpMax);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		teamId = in.read();
		isLeader = in.read();
		
		humanId = in.read();
		modelSn = in.read();
		name = in.read();
		level = in.read();
		
		status = in.read();
		
		hpCur = in.read();
		hpMax = in.read();
		mpCur = in.read();
		mpMax = in.read();
	}

	/**
	 * DTeamMemberItem 队员信息
	 * @return
	 */
	public DTeamMemberItem createDTeamMemberItem() {
		DTeamMemberItem.Builder msg = DTeamMemberItem.newBuilder();
		msg.setHumanId(humanId);
		msg.setHeadSn(modelSn);
		msg.setName(name);
		msg.setLevel(level);
		msg.setStatus(status);
		msg.setHpCur(hpCur);
		msg.setHpMax(hpMax);
		msg.setMpCur(mpCur);
		msg.setMpMax(mpMax);
		return msg.build();
	}
	//设置状态为上线
	public void setMemberOnline() {
		status =  Team.MEMBER_STATUS_ONLINE;
	}
	//设置状态为下线
	public void setMemberOffline() {
		status = Team.MEMBER_STATUS_LEAVE;
	}
}
