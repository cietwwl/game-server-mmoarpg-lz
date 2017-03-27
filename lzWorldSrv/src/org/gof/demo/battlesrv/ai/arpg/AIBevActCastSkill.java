package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.stageObj.UnitObject;

/**
 * AI：释放技能
 * 
 * @author GaoZhangCheng
 */
public class AIBevActCastSkill extends AIBevLeaf {

	public AIBevActCastSkill(AI ai) {
		this.ai = ai;
	}

	@Override
	public boolean execute(Param param) {
		
//		if(ai.unitObj instanceof GeneralObject){
//			System.out.println(" AI：释放技能" + ai.unitObj);
//		}
		
		// 不在了，就返回
		UnitObject uniObj = ai.unitObj;
	
		if(!uniObj.isInWorld() || uniObj.isDie()){
			return false;
		}
		
		SkillParam skillParam = new SkillParam();
		UnitObject unitObj = ai.targetObj;
		if(unitObj == null) {
			ai.behavior = AIBehaviorKey.NORMAL;
			return false;
		}
		skillParam.tarUo = unitObj;
		skillParam.tarPos = unitObj.posNow;
		skillParam.finalAtk = false;
		
		SkillManager.inst().shakeOrCastSkill(ai.unitObj, ai.curSkill.confSkill.sn, skillParam);
		
		//添加被攻击者
		unitObj.beAttacked.put(ai.unitObj.id, ai.unitObj);
		
		//增加技能计数器
		ai.skillIndex ++ ;
		
		//添加被攻击者的目标
		return true;
	}

}
