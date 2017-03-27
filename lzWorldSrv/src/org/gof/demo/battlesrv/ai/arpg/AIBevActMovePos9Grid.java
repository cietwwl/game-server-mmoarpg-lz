package org.gof.demo.battlesrv.ai.arpg;

import java.util.List;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.manager.StageBattleManager;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;

/**
 * AI：根据9宫格范围的设置进行移动
 * 
 * @author GaoZhangCheng
 */
public class AIBevActMovePos9Grid extends AIBevLeaf {

	private double[][] arr1 = {{-0.707,-0.707},{0,-1},{0.707,-0.707},{1,0},{0.707,0.707},{0,1},{-0.707,0.707},{-1,0}};
	private double[][] arr2 = {{-1.414,-1.414},{-1,-1.414},{0,-2},{1,-1.414},{1.414,-1.414},{1.414,-1},{2,0},{1.414,1},
			{1.414,1.414},{1,1.414},{0,2},{-1,1.414},{-1.414,1.414},{-1.414,1},{-2,0},{-1.414,-1}};
	private Vector2D tarPos = new Vector2D();
	private double dis = 2;
	private double radius = 1.2;
	public AIBevActMovePos9Grid(AI ai, Vector2D tarPos, double dis, double radius) {
		this.ai = ai;
		this.tarPos = tarPos;
		this.dis = dis;
		this.radius = radius;
	}
	public AIBevActMovePos9Grid(AI ai, double dis, double radius) {
		this.ai = ai;
		this.dis = dis;
		this.radius = radius;
		
		
	}
	@Override
	public boolean execute(Param param) {
		//根据9宫的方式设置移动		
		//建立数组设置坐标
		//找到自己最近的位置索引
		//从这个方块的索引开始向数组两边开始查找是否有可以进入的位置
		UnitObject unitObj = ai.targetObj;
		if(unitObj != null) {
			this.tarPos = unitObj.posNow;
		} else {
			this.tarPos = null;
		}
		
		if(this.tarPos == null) {
			return true;
		}
		int resultIndex = -1;
		Vector2D vecPos = new Vector2D();
		resultIndex = getMoveGridIndex(arr1, vecPos);
		if(resultIndex == -1) {
			resultIndex = getMoveGridIndex(arr2, vecPos);
		}
		ai.tarMovePos = vecPos;
		
		//如果没有找到移动的点 或者 找到点但是距离很小
		if(resultIndex == -1 || vecPos.distance(ai.unitObj.posNow) < 0.2) {
			return true;
		} else {
			return true;
		}
	}
	
	private int getMoveGridIndex(double[][] arr, Vector2D result ) {
//		Vector2D result = new Vector2D();
		result.set(ai.unitObj.posNow);
		
		int resultIndex = -1;
		double disResult = 9999999;
		//获得离目标位置最近的点
		for (int i = 0 ; i < arr.length ; i++) {
			double elemX = tarPos.x + arr[i][0] * dis;
			double elemY = tarPos.y + arr[i][1] * dis;
			double temp = Math.sqrt(Math.pow((ai.unitObj.posNow.x - elemX), 2) + Math.pow((ai.unitObj.posNow.y - elemY) , 2));
			if(temp < disResult) {
				disResult = temp;
				resultIndex = i;
			}
		}
		if(resultIndex == -1) {
			return resultIndex;
		}
		
		int indexLeft = resultIndex % arr.length;   //左边 --
		int indexRight = (resultIndex + 1) % arr.length; //右边++
		//从索引开始向2边宽展搜索
		for(int i = 0 ; i < arr.length ; i += 2) {
			//判断区域内是否有怪物
			double elemX = tarPos.x + arr[indexLeft][0] * dis;
			double elemY = tarPos.y + arr[indexLeft][1] * dis;
			if(canMoveCircle(elemX, elemY)) {
				result.x = elemX;
				result.y = elemY;
				return resultIndex;
			}
			elemX = tarPos.x + arr[indexRight][0] * dis;
			elemY = tarPos.y + arr[indexRight][1] * dis;
			if(canMoveCircle(elemX, elemY)) {
				result.x = elemX;
				result.y = elemY;
				return resultIndex;
			}
			
			//继续查找
			indexLeft = (indexLeft - 1 + arr.length) % arr.length;
			indexRight = (indexRight + 1) % arr.length;
		}
		
		return -1;
	}
	
	private boolean canMoveCircle(double elemX, double elemY) {
		boolean result = false;
		
		List<UnitObject> lis = StageBattleManager.inst().getUnitObjsInCircle(ai.unitObj.stageObj, new Vector2D(elemX, elemY), radius);
		if(lis.size() <= 0) {
			return true;
		} else if(lis.size() == 1 && lis.get(0).id == ai.unitObj.id) {
			return true;
		}
		
		return result;
	}

}
