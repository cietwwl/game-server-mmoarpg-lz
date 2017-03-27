package org.gof.demo.battlesrv.ai.arpg;

import java.util.List;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.pathFinding.PathFinding;

/**
 * AI：移动到目标点
 * 
 * @author GaoZhangCheng
 */
public class AIBevActMoveTarPos extends AIBevLeaf {

	public AIBevActMoveTarPos(AI ai) {
		this.ai = ai;
	}

	@Override
	public boolean execute(Param param) {
		
//		if(ai.unitObj instanceof GeneralObject){
//			System.out.println("AI：移动到目标点" + ai.unitObj);
//		}
		
		// 不在了，就返回
		UnitObject uniObj = ai.unitObj;
		if(!uniObj.isInWorld() || uniObj.isDie()){
			return false;
		}
				
		if(ai.DEBUG) {
			Log.fight.info("AIBevActionMoveTarPos {} {}", ai.unitObj.name, ai.unitObj.id);
		}
		
		if( ai.tarMovePos == null || ai.unitObj.posNow.distance(ai.tarMovePos) < 0.01) {
			return true;
		} 
		
		if(ai.unitObj.running.isRunning() && ai.tarMoveLastPos != null && ai.tarMovePos.distance(ai.tarMoveLastPos) < 0.1) {
			return true;
		}
		
		if(ai.unitObj.running.isRunning()) {
			ai.unitObj.running._pulse(ai.unitObj.getTime(), true);
		}
		
		int mapSn = ai.unitObj.stageObj.sn;
		Vector3D startPos = StageManager.getHeight(mapSn, ai.unitObj.posNow);
		Vector3D endPos = StageManager.getHeight(mapSn,  ai.tarMovePos);
		
		List<Vector3D> pathr = PathFinding.findPaths(mapSn, startPos, endPos);
		if(pathr.size() <= 1 || (pathr.size() >= 2 && pathr.get(0).distance(pathr.get(pathr.size() - 1)) < 0.01 )) {
			ai.unitObj.posNow = ai.tarMovePos;
			return true;
		}
		double posX = ai.tarMovePos.x - ai.unitObj.posNow.x;
		double posY = ai.tarMovePos.y - ai.unitObj.posNow.y;
		Vector3D dir = new Vector3D(posX, posY, 0);
		if (ai.DEBUG) {
	    	   Log.fight.info("sendCSStageMove {} {} : {} : {} {} {}", ai.unitObj.getTime(),pathr.size(), ai.unitObj.name, pathr,
	    			   ai.tarMovePos.distance(ai.tarMoveLastPos), ai.unitObj.running.isRunning() );
	    }
		ai.unitObj.move(startPos, pathr, dir, false);
//		AISendMsg.sendCSStageMove(ai.unitObj, ai.unitObj.id, startPos, pathr, dir);
		
		return true;
	}

}
