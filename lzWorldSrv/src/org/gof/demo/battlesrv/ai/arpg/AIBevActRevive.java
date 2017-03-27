package org.gof.demo.battlesrv.ai.arpg;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.support.Log;

/**
 * AI：复活
 * 
 * @author GaoZhangCheng
 */
public class AIBevActRevive extends AIBevLeaf {

	public AIBevActRevive(AI ai) {
		this.ai = ai;
	}
	
	@Override
	public boolean execute(Param param) {
		rebirth(ai.unitObj);
		return true;
	}	
	
	public void rebirth(UnitObject unitObj) {
		// 重设出生点，血量，攻击目标等
		unitObj.getUnit().setHpCur(unitObj.getUnit().getHpMax());
		unitObj.posNow.set(unitObj.posBegin);

		unitObj.stageShow();
		
		ai.behavior = AIBehaviorKey.NORMAL;

		if (unitObj.isInWorld() == false) {
			Log.temp.info(ExceptionUtils.getStackTrace(new Throwable()));
		}
		
	}
}
