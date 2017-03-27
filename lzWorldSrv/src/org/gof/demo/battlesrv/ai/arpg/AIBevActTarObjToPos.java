package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.GeneralObject;

/**
 * AI：把目标单位转换为坐标
 * 
 * @author GaoZhangCheng
 */
public class AIBevActTarObjToPos extends AIBevLeaf {
	private double dis = 0;
	public AIBevActTarObjToPos(AI ai, double dis) {
		this.ai = ai;
		this.dis = dis;
	}

	@Override
	public boolean execute(Param param) {
		
		UnitObject unitObj = ai.targetObj;
		if(unitObj != null) {
			double dis = ai.unitObj.posNow.distance(unitObj.posNow);
			ai.tarMovePos = Vector2D.lookAtDis(ai.unitObj.posNow, unitObj.posNow, ai.unitObj.posNow, dis - this.dis);
			return true;
			
			
		} else {
			return false;
		}
	}
}
