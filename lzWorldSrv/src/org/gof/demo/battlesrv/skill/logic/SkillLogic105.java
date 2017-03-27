package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.support.Log;

/**
 * 死亡后释放技能(被动)
 * @author rattler
 *
 */
public class SkillLogic105  extends AbstractSkillLogicPassive{
	public int skillSn;	//技能SN
//	public int executeProp;		//技能产生的概率
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		skillSn = Utils.intValue(conf.param1);
//		executeProp = Utils.intValue(conf.param2);		//技能 产生的概率
//		if(executeProp == 0) {
//			executeProp = 100;
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
		SkillCommon skillNew = uo.skills.get(skillSn);
		if(skillNew == null) {
			Log.fight.error("SkillLogic105技能不存在，攻击者name={} 攻击者id={},技能id={},skillSize={}", uo.name, uo.id, skillSn,  uo.skills.size());
			return;
		}
		SkillManager.inst().shakeOrCastSkill(skill.unitObj, skillSn, position);
	}


}
