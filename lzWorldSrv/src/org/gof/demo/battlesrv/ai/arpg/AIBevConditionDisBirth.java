package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;

/**
 * AI：是否触发回巢
 * 
 * @author GaoZhangCheng
 */
public class AIBevConditionDisBirth extends AIBevLeaf{
	private double dis = 0;
	public AIBevConditionDisBirth(AI ai, double dis, boolean logic) {
		this.ai = ai;
		nonLogic = logic;
		this.dis = dis;
	}
	public AIBevConditionDisBirth(AI ai, double dis) {
		this.ai = ai;
		this.dis = dis;
	}
	
	@Override
	public boolean execute(Param param) {
		boolean result = false;
		
		UnitObject monObj = ai.unitObj;
		
		if(ai.behavior == AIBehaviorKey.BACK) {
			return true;
		}
		
		if(monObj.posNow.distance(monObj.posBegin) > dis) {
			this.ai.tarMovePos = monObj.posBegin;
			return true;
		}
		
		return result;
	}
}
