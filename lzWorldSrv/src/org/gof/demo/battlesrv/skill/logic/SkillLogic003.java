package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.buff.BuffManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfSkillEffect;


/**
 * 技能效果：buff
 * @author rattler
 *
 */
public class SkillLogic003 extends AbstractSkillLogicActive{
	public int buffSn;			//buffSn	
	public int buffProp;		//buff 产生的概率
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		//父类方法初始化了范围前三个参数
		super.init(skillCommon, conf);
		buffSn = Utils.intValue(conf.param1);
		buffProp = Utils.intValue(conf.param2);
		if(buffProp == 0) {
			buffProp = 100;
		}
		
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitDef) {
		//目标加buff
		UnitObject uo = skill.unitObj;
		if(uo == null || uo.isDie()) {
			return;
		}
		
		Long useId = uo.id;
		
		//buff 等级概率压制
		int prop = conf.levelParam;
		if(skill.skillLevel >= unitDef.getUnit().getLevel()) {
			if(prop > 0) {
				buffProp = prop;
			}
		}
		if(useId != null) {
			if(buffProp >= unitDef.nextInt(100)) {
				BuffManager.inst().add(unitDef, useId, buffSn, skill.confSkill.sn, conf.targetFriend);
			}
		}
	}
}
