package org.gof.demo.worldsrv.character;

import java.util.List;

import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.worldsrv.competition.CompetitionManager;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.DStageHuman;
import org.gof.demo.worldsrv.msg.Msg.DStageObject;
import org.gof.demo.worldsrv.msg.Msg.DStageObject.Builder;
import org.gof.demo.worldsrv.msg.Msg.DVector3;
import org.gof.demo.worldsrv.msg.Msg.EWorldObjectType;
import org.gof.demo.worldsrv.stage.StageObject;

public class HumanObjectMirr extends CharacterObject{
	public long attackerId;
	private boolean isOver;
	public HumanObjectMirr() {
		super(null);
	}
	
	public HumanObjectMirr(StageObject stageObj) {
		super(stageObj);
	}
	
	public Human getHuman() {
		return (Human)dataPers.unit;
	}
	
	@Override
	public void startup() {
		super.startup();
		if(stageObj!=null){
			this.stageEnter(stageObj);
		}
	}
	
	@Override
	public void pulse(int deltaTime) {
		
		super.pulse(deltaTime);
		
		if(ai != null) ai.pulse(timeCurr);
		
		if(isOver)return;
		HumanObject humanObj = stageObj.getHumanObj(attackerId);
		
		if(this.isDie()){
			//挑战成功
			isOver = true;
			CompetitionManager.inst().competEnd(humanObj, true);
		}
	}

	@Override
	public Builder createMsg() {
		Human human = getHuman();
		
		//移动中的目标路径
		List<DVector3> runPath = running.getRunPathMsg();
		
		//玩家信息单元
		DStageHuman.Builder h = DStageHuman.newBuilder();
		h.addAllPosEnd(runPath);
		h.setLevel(human.getLevel());
		h.setHpCur(human.getHpCur());
		h.setHpMax(human.getHpMax());
		h.setMpCur(human.getMpCur());
		h.setMpMax(human.getMpMax());
		h.setSex(human.getSex());
		h.setSpeed(human.getSpeed());
		h.setProfession(human.getProfession());
		h.setPvpMode(human.getPvpMode());
		h.setSn(human.getSn());
		h.setInFighting(human.isInFighting());
		h.setTeamBundleID(teamBundleID);
		h.setCanAttack(canAttack);
		h.setCanCastSkill(canCastSkill);
		h.setCanMove(canMove);
		h.setPropJson(getPropPlus().toJSONStr());
		
		List<DSkill> skills = SkillManager.inst().getSkills(this);
		h.addAllSkill(skills);
		
		DStageObject.Builder objInfo = DStageObject.newBuilder();
		objInfo.setObjId(id);
		objInfo.setType(EWorldObjectType.HUMAN);
		objInfo.setName(name);
		
		objInfo.setModelSn(human.getModelSn());
		objInfo.setPos(posNow.toMsg());
		objInfo.setHuman(h);
		
		return objInfo;
	}
	
}
