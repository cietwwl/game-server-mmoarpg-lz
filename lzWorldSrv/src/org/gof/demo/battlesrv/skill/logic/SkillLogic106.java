package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillExcute;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfSkillEffect;

/**
 * (被动)打断也能释放技能 见大法师的推波。 直接调用后摇缓冲的技能， 一般在打断逻辑里面 如果直接作为被动技能可以释放所有
 * @author rattler
 *
 */
public class SkillLogic106  extends AbstractSkillLogicPassive{
	public int skillPrepSn;	//前置技能SN
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		
		skillPrepSn = Utils.intValue(conf.param1);
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitObjDef) {

	}

	@Override
	public boolean canTrigger(SkillEventKey key, boolean isAtker) {
		//必须是技能打断
		if(key != SkillEventKey.EVENT_ON_SKILL_INTERRUPT) {
			return false;
		}
		
		//必须是被攻击方检查
		if(isAtker) {
			return false;
		}
		
		UnitObject uo = skill.unitObj;
		if(uo == null || uo.isDie()) {
			return false;
		}
		
		SkillExcute skillToExcute = uo.skillTempInfo.skillToExcute;
		// 如果没有前摇技能，则不管
		if (skillToExcute == null)
			return false;
		
		
//		//判断技能是否匹配 如果本effect 的skill 有主动技能那么释放主动技能。如果没有主动技能那么认为是所有的技能都可以用
//		boolean canActive = false;
//		for(AbstractSkillLogic logic : skill.logics) {
//			//主动效果略过
//			if(logic instanceof AbstractSkillLogicActive) {
//				canActive = true;
//				break;
//			}
//		}
		
		//本技能sn 不是缓存SN 不释放
		if(skillPrepSn != skillToExcute.sn) {
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
		SkillExcute skillToExcute = uo.skillTempInfo.skillToExcute;
		
		// 如果没有前摇技能，则不管
		if (skillToExcute == null)
			return;

		
		SkillCommon skill = null;

		skill = uo.skills.get(skillToExcute.sn);

		// 技能不存在
		if (skill == null) {
			throw new SysException("技能不存在，攻击者id={},技能Sn={}", uo.id, skillToExcute.sn);
		}

		if (uo.skillTempInfo.skillToExcute != null 
				&& !uo.skillTempInfo.skillToExcute.tickTimer.isOnce(skill.unitObj.getTime())) {
			long leftTime = uo.skillTempInfo.skillToExcute.tickTimer.getTimeLeft(skill.unitObj.getTime());
			long interval = uo.skillTempInfo.skillToExcute.tickTimer.getInterval();
			skill.skillShakePct = 1 - ((double)leftTime / (double)interval);
		}
		skill.castSecond(skillToExcute.tarPos);
		
	}


}
