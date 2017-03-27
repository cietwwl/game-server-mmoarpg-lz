package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;

/**
 * AI：判断是否在目标点范围内
 * 
 * @author GaoZhangCheng
 */
public class AIBevCondDisTarPos extends AIBevLeaf{
	private double dis = 0;
	public AIBevCondDisTarPos(AI ai, double dis, boolean logic) {
		this.ai = ai;
		nonLogic = logic;
		this.dis = dis;
	}
	public AIBevCondDisTarPos(AI ai, double dis) {
		this.ai = ai;
		this.dis = dis;
	}
	@Override
	public boolean execute(Param param) {
		boolean result = false;
		
		UnitObject uniObj = ai.unitObj;
		
		if(uniObj.posNow.distance(ai.tarMovePos) < dis) {
			return true;
		} 
		
		return result;
	}

}
