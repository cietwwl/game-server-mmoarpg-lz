package org.gof.demo.battlesrv.skill.logic;

import java.util.Arrays;
import java.util.List;

import org.gof.demo.battlesrv.buff.BuffManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfSkillEffect;

/**
 * 技能效果：驱散
 * @author new
 *
 */
public class SkillLogic007 extends AbstractSkillLogicActive{
	public String dispelSNs;		//驱散效果集合
	
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		//父类方法初始化了范围前三个参数
		super.init(skillCommon, conf);
		dispelSNs = conf.param1;
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitDef) {
		//驱散各种buff
		String[] sns = dispelSNs.split(",");
		List<String> types = Arrays.asList(sns);
		
		for(String type : types) {
			BuffManager.inst().dispel(unitDef, type);
		}
	}
}