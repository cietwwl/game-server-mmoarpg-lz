package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.GeneralObject;

/**
 * AI：判断自己和目标单位的距离是否大于dis
 * 
 * @author GaoZhangCheng
 */
public class AIBevCondDisTarObj extends AIBevLeaf{

	private double dis = 0;
	private UnitObject tarUnitOjb;
	private long delay = 0;			//延时检测时间
	
	public AIBevCondDisTarObj(AI ai, double dis, UnitObject tarUnitOjb, boolean logic, long delay ) {
		this.ai = ai;
		nonLogic = logic;
		this.tarUnitOjb = tarUnitOjb;
		this.dis = dis;
		this.delay = delay;
	}
	public AIBevCondDisTarObj(AI ai, double dis) {
		this.ai = ai;
		this.dis = dis;
	}
	@Override
	public boolean execute(Param param) {
		boolean result = false;
		UnitObject unitObj = ai.targetObj;
		
		if(unitObj != null) {
			
			//检测时间有一定间隔，少于间隔就返回
			if(ai.activeMoveTime + delay > unitObj.getTime()){
				return false;
			}
			
			//如果对象为空，则按照距离计算，否则按照对象距离计算
			if(tarUnitOjb == null) {
				if(unitObj.posNow.distance(ai.unitObj.posNow) > dis) {
					result = true;
				} 
				
				//向目标对象移动
			} else {
				if(unitObj.posNow.distance(tarUnitOjb.posNow) > dis) {
					result = true;
				} 
			}
			
			ai.activeMoveTime = unitObj.getTime();
		}
		
		return result;
	}
}
