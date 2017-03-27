package org.gof.demo.battlesrv.ai.arpg;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;

/**
 * AI：判断敌人是否在区域内，锁定敌人
 * 
 * @author GaoZhangCheng
 */
public class AIBevCondAreaEnemy extends AIBevLeaf{
	private double dis = 0;
	
	public AIBevCondAreaEnemy(AI ai, double dis, boolean logic) {
		this.ai = ai;
		nonLogic = logic;
		this.dis = dis;
	}
	public AIBevCondAreaEnemy(AI ai, double dis) {
		this.ai = ai;
		this.dis = dis;
	}
	@Override
	public boolean execute(Param param) {
		boolean result = false;
		
		UnitObject uniObj = ai.unitObj;
		
		// 不在了，就返回
		if(!uniObj.isInWorld() || uniObj.isDie()){
			return false;
		}
		
		Vector2D pos = uniObj.posNow;
		double disMax = 99999;
		
		//搜索自己范围以内的所有信息 
		List<UnitObject> resultList = new ArrayList<>();
		for(UnitObject uo : uniObj.stageObj.getUnitObjs().values()) {
			
			if(!uo.isInWorld() || uo.isDie()) {
				continue;
			}
			//判断距离是否符合要求
			Vector2D p = uo.posNow;
			//只保留敌对的信息
			if(uo.teamBundleID != uniObj.teamBundleID) {
				double temp = pos.distance(p);
				if(pos.distance(p) < dis) {
					resultList.add(uo);
					if(temp < disMax) {
						disMax = temp;
						ai.targetObj = uo;
						ai.behavior = AIBehaviorKey.ATTACKED;
					}
				}
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
