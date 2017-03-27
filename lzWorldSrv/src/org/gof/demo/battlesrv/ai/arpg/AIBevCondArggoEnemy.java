package org.gof.demo.battlesrv.ai.arpg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.MonsterObject;

/**
 * AI：判断一定区域内敌人，找到仇恨值最高的
 * 
 * @author GaoZhangCheng
 */
public class AIBevCondArggoEnemy extends AIBevLeaf{
	private double dis = 0;
	
	public AIBevCondArggoEnemy(AI ai, double dis, boolean logic) {
		this.ai = ai;
		nonLogic = logic;
		this.dis = dis;
	}
	public AIBevCondArggoEnemy(AI ai, double dis) {
		this.ai = ai;
		this.dis = dis;
	}
	@Override
	public boolean execute(Param param) {
		boolean result = false;
		
		UnitObject uniObj = ai.unitObj;
		
		// 不在了，就返回
		if(!uniObj.isInWorld() || uniObj.isDie()){
			uniObj.aggroList.clear();
			return false;
		}
		
		Vector2D pos = uniObj.posNow;
		int arggoMax = -1;
		
		//搜索自己范围以内的所有信息 
		//List<UnitObject> resultList = new ArrayList<>();
		Map<Long, UnitObject> resultList = new HashMap<>();
		for(UnitObject uo : uniObj.stageObj.getUnitObjs().values()) {
			
			if(!uo.isInWorld() || uo.isDie()) {
				continue;
			}
			//判断距离是否符合要求
			Vector2D p = uo.posNow;
			//找到仇恨值最高的敌人
			if(uo.teamBundleID != uniObj.teamBundleID) {
				int arggo = uo.getUnit().getAggro();
				if(pos.distance(p) < dis) {
					//resultList.add(uo);
					resultList.put(uo.id, uo);
					if(arggo > arggoMax) {
						arggoMax = arggo;
						ai.targetObj = uo;
						ai.behavior = AIBehaviorKey.ATTACKED;
					}
				}
				
			}
		}
		//更新怪物仇恨值列表，并选取仇恨值最高的
		for(long id : uniObj.aggroList.keySet()){ 
			if(uniObj.stageObj.getUnitObjs().get(id) == null){
				uniObj.aggroList.remove(id);
				continue;
			}
			if(resultList.get(id) != null  && uniObj.aggroList.get(id) > arggoMax){
				arggoMax = uniObj.aggroList.get(id);
				ai.targetObj = resultList.get(id);
				ai.behavior = AIBehaviorKey.ATTACKED;
			}
		}
		
		if(resultList.size() > 0) {
			result = true;
		} else {
			result =false;
		}
		return result;
	}
		
}
