package org.gof.demo.battlesrv.ai.arpg;

import java.util.List;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.pathFinding.PathFinding;

/**
 * AI：移动到自己主角身边
 * 
 * @author GaoZhangCheng
 */
public class AIBevActMoveTarHum extends AIBevLeaf {

	private double staDis = 0;
	private double maxDis = 0;
	private double fixFeet = 0.1;		//修正距离，就是移动忽略距离
	public AIBevActMoveTarHum(AI ai, double maxDis, double staDis, double fixFeet) {
		this.ai = ai;
		this.staDis = staDis;
		this.maxDis = maxDis;
		this.fixFeet = fixFeet;
	}

	@Override
	public boolean execute(Param param) {
		UnitObject unitObj = ai.humanObj;
		
		if(unitObj != null) {
			Vector2D goal=new Vector2D();//目标点
			goal=unitObj.posNow;	
			if(ai.unitObj.isGeneralObj()){   //武将根据站位选点
			  GeneralObject gen= (GeneralObject) ai.unitObj;
			  double condis=0;     //选点距离主句的距离
			  goal=Vector2D.genFindDis(unitObj.posNow, unitObj.dirNow, gen.conf.order, condis);
			}
			
			double dis = ai.unitObj.posNow.distance(goal);
			if(dis < this.staDis) {
				return false;
			}
			
			//记录上次移动的目标
			Vector2D tarLastPos = ai.tarLastPos;
			
			//新的目标点
//			ai.tarLastPos = Vector2D.lookAtDis(ai.unitObj.posNow, unitObj.posNow, ai.unitObj.posNow, dis - this.staDis);
//			根据武将站位选点，，	dis是距离主句的距离
			ai.tarLastPos = Vector2D.genFindDis(unitObj.posNow, unitObj.posNow, ai.unitObj.getUnit().getAttingIndex(), 1.5);
			//新旧目标点距离不超过设定值米，返回
			if(ai.tarLastPos.distance(tarLastPos) < fixFeet){
				return false;
			}
			
			//当前距离在范围之内，返回
			if(ai.unitObj.posNow.distance(ai.tarLastPos) < staDis) {
				return false;
			}
			
			//记录和修正坐标，然后移动
			int mapSn = ai.unitObj.stageObj.sn;
			Vector3D startPos = StageManager.getHeight(mapSn, ai.unitObj.posNow);
			Vector3D endPos = StageManager.getHeight(mapSn,  ai.tarLastPos);
			List<Vector3D> pathr = PathFinding.findPaths(mapSn, startPos, endPos);
			ai.unitObj.move(StageManager.getHeight(ai.unitObj.stageObj.sn, ai.unitObj.posNow), pathr,  new Vector3D(), false);
			return true;
			
		} else {
			return false;
		}
	}

}
