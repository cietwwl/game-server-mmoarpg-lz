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
import org.gof.demo.worldsrv.support.Log;

/**
 * 用于掉血连续触发buf  dot bullet 比如 5% 10% 15%等  80,60,40,20
 * @author rattler
 *
 */
public class SkillLogic101  extends AbstractSkillLogicPassive{
	public int skillLogicArr[];			//logicSn根据掉血比列来对应	
	public int triggerHpPct[];		//掉血比列数组
	public int index = -1;			//判断血量在哪个技能区间内	
	public int lastIndex = -1; 		//判断血量在哪个技能区间内	只有技能释放以后改变
	private Param param;
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		skillLogicArr = Utils.arrayStrToInt(conf.param1);    //逻辑库的数组
		triggerHpPct = Utils.arrayStrToInt(conf.param2);		//掉血比列数组
		param = new Param("skill", skill, "hpLostKey", HpLostKey.SKILL);
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitObjDef) {
		
	}

	@Override
	public boolean canTrigger(SkillEventKey key, boolean isAtker) {
		//必须是技能施放结束前
		if(key != SkillEventKey.EVENT_ON_AFT_HPLOST) {
			return false;
		}
		
		//必须是防御方检查
		if(isAtker) {
			return false;
		}
		
		if(triggerHpPct == null || skillLogicArr == null || triggerHpPct.length <= 0 || skillLogicArr.length <= 0) {
			Log.fight.info("SkillLogic101参数不对{} : {}", skillLogicArr, triggerHpPct);
		}
		
		Unit unit = skill.unitObj.getUnit();
		double hpPct = (unit.getHpCur() * 1.0) / unit.getHpMax();
		
		//获得当前血量所处于的区间段
		if(triggerHpPct.length == 1) {
			//判断血量是否达到触发点
			if(hpPct * 100 > triggerHpPct[0]) {
				return false;
			} else {
				index = 0;
			}
		} else {
			int temp = -1;
			for(int i = 0 ; i < triggerHpPct.length ; i++) {
				if(hpPct * 100 < triggerHpPct[i]) {
					temp = i;
				}
			}
			if(temp == -1) {
				return false;
			}
			
			index = temp;
			//只触发在改变的时候
			if(index == lastIndex) {
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
		
		if(index == -1) {
			return;
		}
		SkillManager.inst().executeSkillLogic(skillLogicArr[index], uo, uo, tarUnit, uo.posNow, param);
	}

}
