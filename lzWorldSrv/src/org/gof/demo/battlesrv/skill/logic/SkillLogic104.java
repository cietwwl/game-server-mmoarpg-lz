package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfSkillEffect;

/**
 * 被攻击概率触发技能 反击 防御者
 * @author rattler
 *
 */
public class SkillLogic104  extends AbstractSkillLogicPassive{
	public int skillSn;	//技能SN
//	public int Prop;		//产生的概率
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		skillSn = Utils.intValue(conf.param1);
//		Prop = Utils.intValue(conf.param2);
//		if(Prop == 0) {
//			Prop = 100;
//		}
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitObjDef) {

	}

	@Override
	public boolean canTrigger(SkillEventKey key, boolean isAtker) {
		//必须是放技能结束前
		if(key != SkillEventKey.EVENT_ON_SKILL_END) {
			return false;
		}
		
		//必须是攻击方检查
		if(isAtker) {
			return false;
		}
		
		return true;
	}

	@Override
	public void trigger(SkillEventKey key, UnitObject tarUnit, SkillParam position, SkillParamVO skillParamVO) {
		UnitObject uo = skill.unitObj;
		if(uo == null || uo.isDie()) {
			return;
		}
		SkillManager.inst().shakeOrCastSkill(skill.unitObj, skillSn, position);
	}


}
