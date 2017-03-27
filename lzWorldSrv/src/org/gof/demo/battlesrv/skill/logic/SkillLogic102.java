package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfSkillEffect;

/**
 * 攻击方吸血(被动)
 * @author rattler
 *
 */
public class SkillLogic102 extends AbstractSkillLogicPassive{
	public double bloodSuckPct;	//吸血百分比
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		bloodSuckPct = Utils.doubleValue(conf.param1) / 10000D;
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
		
		
		return true;
	}

	@Override
	public void trigger(SkillEventKey key, UnitObject tarUnit, SkillParam position, SkillParamVO skillParamVO) {
		UnitObject uo = skill.unitObj;
		if(uo == null || uo.isDie()) {
			return;
		}
		skillParamVO.bloodSuckPct += bloodSuckPct;
	}


}
