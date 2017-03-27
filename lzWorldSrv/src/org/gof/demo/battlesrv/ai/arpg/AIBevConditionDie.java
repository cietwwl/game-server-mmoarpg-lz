package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;

/**
 * AI：是否死亡
 * 
 * @author GaoZhangCheng
 */
public class AIBevConditionDie extends AIBevLeaf{

	public AIBevConditionDie(AI ai, boolean logic) {
		this.ai = ai;
		nonLogic = logic;
	}
	public AIBevConditionDie(AI ai) {
		this.ai = ai;
	}
	

	@Override
	public boolean execute(Param param) {
		
		UnitObject monObj = ai.unitObj;
		return monObj.isDie();
		
	}
}
