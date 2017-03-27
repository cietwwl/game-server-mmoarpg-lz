package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;

/**
 * AI：将主角的敌人设置为自己的目标
 * 
 * @author GaoZhangCheng
 */
public class AIBevActActTarHum extends AIBevLeaf {
	
	public AIBevActActTarHum(AI ai) {
		this.ai = ai;
	}

	@Override
	public boolean execute(Param param) {
		UnitObject unitObj = ai.humanObj;
		if(unitObj == null) {
			return false;
		}
		
		if(unitObj.beAttacked.isEmpty()) {
			return false;
		}
		
		ai.targetObj = ai.humanObj.beAttacked.get(0);
		return true;
		
	}

}
