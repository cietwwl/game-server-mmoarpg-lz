package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.manager.StageBattleManager;

/**
 * AI：随机找到巡逻范围内的一个店
 * 
 * @author GaoZhangCheng
 */
public class AIBevActGetRandPos extends AIBevLeaf {
	private double radius = 0;
	public AIBevActGetRandPos(AI ai, double radius) {
		this.ai = ai;
		this.radius = radius;
	}

	@Override
	public boolean execute(Param param) {
		ai.tarMovePos = StageBattleManager.inst().randomPosInCircle(ai.unitObj.stageObj, ai.unitObj.posBegin, 0, radius);
		return true;
	}

}
