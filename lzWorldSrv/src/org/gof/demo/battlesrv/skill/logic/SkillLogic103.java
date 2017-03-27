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
 * 攻击概率触发技能 连击 攻击者
 * @author rattler
 *
 */
public class SkillLogic103  extends AbstractSkillLogicPassive{
	public int skillSn;	    //技能SN
	public int skillPrepSn;	//前置技能SN
//	public int Prop;		//产生的概率
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		skillPrepSn = Utils.intValue(conf.param1);
		skillSn = Utils.intValue(conf.param2);
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
		if(!isAtker) {
			return false;
		}
		
		UnitObject uo = skill.unitObj;
		if(uo == null || uo.isDie()) {
			return false;
		}
		
		//如果有主动技能 并且前置技能是上次释放的技能 不是缓存SN 不释放
		if(skillPrepSn != uo.skillTempInfo.lastSkillSn) {
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
//		Log.fight.info("trigger {}", skillSn);
	}


}
