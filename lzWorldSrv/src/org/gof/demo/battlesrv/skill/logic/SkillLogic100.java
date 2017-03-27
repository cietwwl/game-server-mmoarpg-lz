package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.entity.Unit;
import org.gof.demo.worldsrv.support.ReasonResult;


/**
 * 被动技能，被攻击方释放各种dot buff bullet
 * @author rattler
 *
 */
public class SkillLogic100 extends AbstractSkillLogicPassive{
	public int skillSnArr[];			//skillSn	
	public int triggerHpPct = 0;			//伤血百分比 到了触发。
	public Param param = null;
	public boolean isTrigger = false;			//是否已经触发
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		skillSnArr = Utils.arrayStrToInt(conf.param1);
		
		triggerHpPct = Utils.intValue(conf.param2);			//伤血百分比
		param = new Param("skill", skill, "hpLostKey", HpLostKey.SKILL);
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitObjDef) {
		
	}

	@Override
	public boolean canTrigger(SkillEventKey key, boolean isAtker) {
		//必须是技能施放结束前
		if(key != SkillEventKey.EVENT_ON_TAR_EFFECT_END) {
			return false;
		}
		
		//必须是防御方检查
		if(isAtker) {
			return false;
		}
		
		//只能触发1次
		if(isTrigger) {
			return false;
		}
		
		//血的判断
		if(triggerHpPct > 0) {
			//判断血量是否达到触发点
			Unit unit = skill.unitObj.getUnit();
			double hpPct = (unit.getHpCur() * 1.0) / unit.getHpMax();
			if(hpPct * 100 > triggerHpPct) {
//				Log.fight.info("triggerHpPct {}", hpPct * 100);
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void trigger(SkillEventKey key, UnitObject tarUnit, SkillParam position, SkillParamVO skillParamVO) {
		UnitObject uo = skill.unitObj;
		if(uo == null || uo.isDie()) {
			return;
		}
		
		Long useId = uo.id;
		
		if(useId != null) {
			if(skillSnArr != null && skillSnArr.length > 0 ) {
				for (int i : skillSnArr) {
					SkillParam skillParam = new SkillParam();
					skillParam.tarUo = tarUnit;
					skillParam.tarPos = tarUnit.posNow;
					ReasonResult result = SkillManager.inst().shakeOrCastSkill(skill.unitObj, i, skillParam);
					if(result.success) {
						isTrigger = true;
					}
				}
			}
		}
	}


}
