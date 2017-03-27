package org.gof.demo.worldsrv.character;

import java.util.List;

import org.gof.demo.battlesrv.ai.arpg.AIGenMirr;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.worldsrv.config.ConfCharacterGeneral;
import org.gof.demo.worldsrv.config.ConfCharacterModel;
import org.gof.demo.worldsrv.entity.General;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.DStageGeneral;
import org.gof.demo.worldsrv.msg.Msg.DStageObject;
import org.gof.demo.worldsrv.msg.Msg.DStageObject.Builder;
import org.gof.demo.worldsrv.msg.Msg.DVector3;
import org.gof.demo.worldsrv.msg.Msg.EWorldObjectType;
import org.gof.demo.worldsrv.stage.StageObject;

public class GeneralObjectMirr extends CharacterObject{
	public HumanObjectMirr parentObjMirr;
	public ConfCharacterGeneral conf; //武将配置
	public GeneralObjectMirr(HumanObjectMirr tempParent, StageObject stageObj) {
		super(stageObj);
		this.parentObjMirr = tempParent;
		this.teamBundleID = tempParent.id;
	}
	
	public GeneralObjectMirr(StageObject stageObj) {
		super(stageObj);
	}
	
	@Override
	public void startup() {
		super.startup();
		
		//进入场景
		if(stageObj != null) {
			this.stageEnter(stageObj);
			
			//加入AI
			if(this.ai == null) {
				this.conf = ConfCharacterGeneral.get(sn);
				this.confModel = ConfCharacterModel.get(modelSn);
				this.ai = new AIGenMirr(parentObjMirr,this, this.conf.ai);
			}
		}
		
	}
	
	@Override
	public void pulse(int deltaTime) {
		
		super.pulse(deltaTime);
		
		if(parentObjMirr!=null && parentObjMirr.isDie())return;
		if(ai != null) ai.pulse(timeCurr);
	}

	@Override
	public Builder createMsg() {
		General general = getGeneral();
		
		//移动中的目标路径
		List<DVector3> runPath = running.getRunPathMsg();
		
		//玩家信息单元
		DStageGeneral.Builder h = DStageGeneral.newBuilder();
//		DStageHuman.Builder h = DStageHuman.newBuilder();
		h.addAllPosEnd(runPath);
		h.setLevel(general.getLevel());
		h.setHpCur(general.getHpCur());
		h.setHpMax(general.getHpMax());
		h.setMpCur(general.getMpCur());
		h.setMpMax(general.getMpMax());
		h.setSex(general.getSex());
		h.setSpeed(general.getSpeed());
		h.setProfession(general.getProfession());
		h.setPvpMode(general.getPvpMode());
		h.setInFighting(general.isInFighting());
		h.setTeamBundleID(teamBundleID);
		h.setCanAttack(canAttack);
		h.setCanCastSkill(canCastSkill);
		h.setCanMove(canMove);
		h.setPropJson(getPropPlus().toJSONStr());
		h.setSn(general.getSn());
		
		List<DSkill> skills = SkillManager.inst().getSkills(this);
		h.addAllSkill(skills);
		
		DStageObject.Builder objInfo = DStageObject.newBuilder();
		objInfo.setObjId(id);
		objInfo.setType(EWorldObjectType.GENERAL);
		objInfo.setName(name);
		objInfo.setModelSn(general.getModelSn());
		objInfo.setPos(posNow.toMsg());
		objInfo.setDir(dirNow.toMsg());
		objInfo.setGeneral(h);
		
		return objInfo;
	}
	
	public long getHumanId() {
		return parentObjMirr.id;
	}
	
	public General getGeneral() {
		return (General)dataPers.unit;
	}

}
