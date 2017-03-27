package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.worldsrv.config.ConfSkillEffect;

/**
 * 死亡重生的被动技能
 *
 */
public class SkillLogic109 extends AbstractSkillLogicPassive{

	public int hpRecoverPct;	    //恢复hp百分比
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		hpRecoverPct = Utils.intValue(conf.param1);
	}
	
	@Override
	public boolean canTrigger(SkillEventKey key, boolean isAtker) {
		//必须是死亡时
		if(key != SkillEventKey.EVENT_ON_UNIT_DYING) {
			return false;
		}
		
		return true;
	}

	@Override
	public void trigger(SkillEventKey key, UnitObject tarUnit,
			SkillParam position, SkillParamVO skillParamVO) {
		//重生，回复HP
		UnitObject uo = skill.unitObj;
		int hpAdd = (uo.getUnit().getHpMax() - uo.getUnit().getHpCur()) * hpRecoverPct / 100;
		UnitManager.inst().addHp(uo, HpLostKey.BUFF, hpAdd, null);
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitDef) {
		
	}

}
