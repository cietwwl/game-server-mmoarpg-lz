package org.gof.demo.battlesrv.ai.arpg;

import java.util.List;

import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.worldsrv.stage.StageManager;

/**
 * AI：移动到目标对象
 * 
 * @author GaoZhangCheng
 */
public class AIBevActMoveTarObj extends AIBevLeaf {

	private double dis = 0;
	public AIBevActMoveTarObj(AI ai, double dis) {
		this.ai = ai;
		this.dis = dis;
	}

	@Override
	public boolean execute(Param param) {
		UnitObject unitObj = ai.targetObj;
		if(unitObj != null) {
			double dis = ai.unitObj.posNow.distance(unitObj.posNow);
			Vector2D tarPos = Vector2D.lookAtDis(ai.unitObj.posNow, unitObj.posNow, ai.unitObj.posNow, dis - this.dis);
			if(ai.unitObj.posNow.distance(tarPos) < 0.1) {
				return true;
			}

			Vector3D dir = new Vector3D();
			List<Vector3D> path = Utils.ofList(StageManager.getHeight(ai.unitObj.stageObj.sn, tarPos));
			ai.unitObj.move(StageManager.getHeight(ai.unitObj.stageObj.sn, ai.unitObj.posNow), path, dir, false);
			return true;
			
			
		} else {
			return false;
		}
	}

}
