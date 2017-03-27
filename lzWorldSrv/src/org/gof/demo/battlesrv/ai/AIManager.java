package org.gof.demo.battlesrv.ai;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.support.observer.EventKey;

/**
 * 怪物AI功能类
 */
public class AIManager extends ManagerBase {

	public static AIManager inst() {
		return inst(AIManager.class);
	}
	
	@Listener(EventKey.UNIT_BE_ATTACKED)
	public void onUnitAttacked(Param param) {
		UnitObject unObject = param.get("unitObj");
		UnitObject unitAtk = param.get("attacker");
		if(unObject != null  && unitAtk != null && unObject.teamBundleID != unitAtk.teamBundleID) {
//			unObject.ai.behavior = AIBehaviorKey.ATTACKED;
//			if(unObject.ai.targetObj != 0) {
//				unObject.ai.targetId = unitAtk.id;
//			}
		}
		
	}
	
	
	@Listener(EventKey.UNIT_BE_KILLED)
	public void dispelByUnitBeKilled(Param param) {
		UnitObject unitObjDead = param.get("dead");
		for (UnitObject unitObj : unitObjDead.beAttacked.values()) {
			if(unitObj != null ) {
				AI ai = unitObj.getAI();
				if(ai != null) {
					ai.targetObj = null;
				}
			}
		}
		//清楚被攻击者
		unitObjDead.beAttacked.clear();
	}
	
	
	public List<UnitObject> getUnitObjsTarPosInCircle(StageObject stageObj, Vector2D pos, double radius) {
		List<UnitObject> result = new ArrayList<>();
		for(UnitObject uo : stageObj.getUnitObjs().values()) {
			//判断距离是否符合要求
			Vector2D p;
			if(uo.ai != null && uo.ai.tarMovePos != null) {
				p = uo.ai.tarMovePos;
				if(pos.distance(p) < radius) {
					result.add(uo);
				}
			}
			
		}
		
		return result;
	}
}
