package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.manager.FightManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfSkillEffect;

public class SkillLogic107  extends AbstractSkillLogicPassive{
	public int prop1;	//2重施法概率
	public int prop2;	//3重施法概率
	public int prop3;	//4重施法概率
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		prop1 = Utils.intValue(conf.param1);
		prop2 = Utils.intValue(conf.param2);
		prop3 = Utils.intValue(conf.param3);
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitObjDef) {

	}

	@Override
	public boolean canTrigger(SkillEventKey key, boolean isAtker) {
		//必须是放技能结束前
		if(key != SkillEventKey.EVENT_ON_SKILL_BEFORE) {
			return false;
		}
		
		//必须是防守者方检查
		if(isAtker) {
			return false;
		}
		
		//必须是死亡了
		UnitObject uo = skill.unitObj;
		if(uo == null || !uo.isDie()) {
			return false;
		}
		
		return true;
	}

	@Override
	public void trigger(SkillEventKey key, UnitObject tarUnit, SkillParam position, SkillParamVO skillParamVO) {
		UnitObject uo = skill.unitObj;
		int result = 0;
		if(rollRandom(uo, prop3)) {
			result = 3;
		} else if(rollRandom(uo, prop2)) {
			result = 2;
		} else if(rollRandom(uo, prop1)) {
			result = 1;
		}
		tarUnit.skillTempInfo.mutilMagic = result;
	}
	
	private boolean rollRandom(UnitObject uoAtk, int seed) {
		if(seed == 0) {
			return false;
		}
		int roll = uoAtk.nextInt((int)FightManager.PERINT);
		if(roll < seed) {
			return true;
		}
		return false;
	}
	
}
