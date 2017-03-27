package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.entity.Buff;

/**
 * 技能效果：直接伤害
 * 
 * @author rattler
 */
public class SkillLogic001 extends AbstractSkillLogicActive{
	//范围加上
	public double pctCommon;		//技能伤害比列
	public int addCommon;		//普攻附加纯数值
	public int[] buffSn;			//对于BUFF 的伤害叠加
	public double buffPct;		//对于指定BUFF 的叠加伤害
	Param param;
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		pctCommon = Utils.doubleValue(conf.param1);
		addCommon = Utils.intValue(conf.param2);
		buffSn = Utils.arrayStrToInt(conf.param3);
		buffPct = Utils.doubleValue(conf.param4);
		param = new Param("skill", skill, "hpLostKey", HpLostKey.SKILL, "isHit", true);
	}

	@Override
	public int calcHurt(UnitObject unitObjDef) {
		double buffPct = 0;
		if(buffSn != null && buffSn.length > 0) {
			for (Buff buff : unitObjDef.dataPers.buffs.values()) {
				for(int i = 0 ; i < buffSn.length ; i++) {
					if(buff.getSn() == buffSn[i]) {
						buffPct = this.buffPct;
						break;
					}
				}
			}
		}
		//等级加成
		return SkillManager.inst().calcHurt(skill.unitObj, unitObjDef, skill.confSkill.sn, pctCommon + buffPct, addCommon, param);
	}
	
	@Override
	public void doSkillEffectToTar(UnitObject unitObjDef) {
		//前摇百分比
		UnitObject uo = skill.unitObj;
		if(uo == null || uo.isDie()) {
			return ;
		}
		double buffPct = 0;
		if(buffSn != null && buffSn.length > 0) {
			for (Buff buff : unitObjDef.dataPers.buffs.values()) {
				for(int i = 0 ; i < buffSn.length ; i++) {
					if(buff.getSn() == buffSn[i]) {
						buffPct = this.buffPct;
						break;
					}
				}
			}
		}

		//等级加成
		int add = skill.skillLevel * conf.levelParam;
		int hpLost =  SkillManager.inst().doSkillEffectToTar(skill.unitObj, unitObjDef,  param, addCommon + add, pctCommon + buffPct);
		
		skill.addTemp("hurt", hpLost);
		uo.skillTempInfo.mutilMagic = 0;
	}

}
