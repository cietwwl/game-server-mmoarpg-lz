package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.bullet.BulletManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfSkillEffect;

/**
 * @author rattler
 *子弹
 */
public class SkillLogic004 extends AbstractSkillLogicActive{
public int bulletSn;					
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		//父类方法初始化了范围前三个参数
		super.init(skillCommon, conf);
		bulletSn = Utils.intValue(conf.param1);
	}
	
	@Override
	public void doSkillEffect(SkillParam position) {
		UnitObject uo = skill.unitObj;
		if(uo == null || uo.isDie()) {
			return;
		}
		
		Vector2D vec = position.tarPos;
		UnitObject unitObj = position.tarUo;
		
		if(conf.targetSelf) {
			vec = skill.unitObj.posNow;
			unitObj = skill.unitObj;
		}
		
		BulletManager.inst().create(skill.unitObj.stageObj, bulletSn, skill.confSkill.sn ,skill.unitObj, skill.unitObj, unitObj, vec);
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitDef) {
		
	}
}
