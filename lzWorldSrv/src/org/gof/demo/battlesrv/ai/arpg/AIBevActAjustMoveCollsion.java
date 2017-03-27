package org.gof.demo.battlesrv.ai.arpg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIMoveDirFromKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.MathUtils;

/**
 * AI：根据阻挡动态调整目标
 * 
 * @author GaoZhangCheng
 */
public class AIBevActAjustMoveCollsion  extends AIBevLeaf {
	
	public AIBevActAjustMoveCollsion(AI ai) {
		this.ai = ai;
	}
	
	@Override
	public boolean execute(Param param) {
		
		if(ai.unitObj instanceof GeneralObject){
			System.out.println(" AI：根据阻挡动态调整目标" + ai.unitObj);
		}
		
		//已经获得了可以的移动的目标位置
		if(ai.tarMovePos == null) {
			Log.stageMove.info("t1arMovePos 不能为 NULL AIBevActAjustMoveCollsion{}", ExceptionUtils.getStackTrace(new Throwable()));
			return true;
		}
		List<Double> angleArr = new ArrayList<Double>();
		
		//判断目标位置是否是被碰撞
		boolean isCollide = StageManager.inst().stageCollsionDetectAll(ai.unitObj, MathUtils.getDir(ai.unitObj.posNow, ai.tarMovePos), angleArr);
		
		if(!isCollide) {
			//如果目标点碰点不存在， 那么取消左右趋势
			ai.moveDirFrom = AIMoveDirFromKey.FromNon;
		} else {
			//如果是目标点碰撞存在， 那么判断目标点的位置，如果没有左右趋势上下搜索使用趋势，如果有趋势那么直接使用，使用左右趋势
			
			double aMinx1 = 999;
			double aMinx2 = -999;
			boolean isPlus = false;
			//找到最小的角度，然后向上+ 45度查看是否有碰撞
			
			//角度归一化 180 - 360 转换到 -180 - 0 并找到最小的
			for (Double d1 : angleArr) {
				if(d1 < 0) { 
					//找最大的
//					d1 = d1 - 360;
					if(aMinx2 < d1) {
						aMinx2 = d1;
					}
				} else {
					//找最小的
					if(aMinx1 > d1) {
						aMinx1 = d1;
					}
				}
			}
			
			//判断最小值是在0- 180 还是 180 -360；
			if(aMinx1 < Math.abs(aMinx2)) {
				isPlus = true;
			}
			
			List<Double> passAngleArr;
			//找到可以通过的所有角度队列
			if(isPlus) {
				passAngleArr = getPassAngle(angleArr, aMinx1);
			} else {
				passAngleArr = getPassAngle(angleArr, aMinx2);
			}
			
//			boolean canAdjust = false;
//			double passAngle = 9999;
//			//找到绝对值最小的 并且小于100度的 可以通过的角度 按照趋势查找
//			if(ai.moveDirFrom == AIMoveDirFromKey.FromNon) {
//				for (Double d1 : passAngleArr) {
//					double temp = Math.abs(d1);
//					if(temp < 300 && passAngle > temp) {
//						canAdjust = true;
//						passAngle = d1;
//						if(d1 > 0)
//							ai.moveDirFrom = AIMoveDirFromKey.FromLeft;
//						} else {
//							ai.moveDirFrom = AIMoveDirFromKey.FromRight;
//					}
//				}
//			} else if(ai.moveDirFrom == AIMoveDirFromKey.FromLeft) {
//				for (Double d1 : passAngleArr) {
//					if(d1 < 0) continue;
//					
//					double temp = Math.abs(d1);
//					if(temp < 300 && passAngle > temp) {
//						canAdjust = true;
//						passAngle = d1;
//					}
//				}
//			} else if(ai.moveDirFrom == AIMoveDirFromKey.FromRight) {
//				for (Double d1 : passAngleArr) {
//					if(d1 > 0) continue;
//					double temp = Math.abs(d1);
//					if(temp < 300 && passAngle > temp) {
//						canAdjust = true;
//						passAngle = d1;
//					}
//				}
//			}
			
			
//			if(canAdjust) {
//				double rad = (Math.PI * passAngle / 180);
//				//计算按照新的向量可以走的路
//				double tempSin = Math.sin(rad);
//				double tempCos = Math.cos(rad);
//				Vector2D newPos = new Vector2D();
//				double deltaX = ai.tarMovePos.x - ai.unitObj.posNow.x;
//				double deltaY = ai.tarMovePos.y - ai.unitObj.posNow.y;
//				newPos.x = ai.unitObj.posNow.x + (deltaX * tempCos - deltaY * tempSin); 
//				newPos.y = ai.unitObj.posNow.y + (deltaX * tempSin + deltaY * tempCos);
//				
//				//如果newPos 是一个不可通过点， 那么没有找到 继续查找
//				
//				//获得新的位置
				ai.tarMoveLastPos = ai.tarMovePos;
				ai.tarMovePos = getPassSingleAngle(passAngleArr);
//			}
//			else {
//				if(ai.moveDirFrom == AIMoveDirFromKey.FromRight) {
//					ai.moveDirFrom = AIMoveDirFromKey.FromLeft;
//				} else if(ai.moveDirFrom == AIMoveDirFromKey.FromLeft) {
//					ai.moveDirFrom = AIMoveDirFromKey.FromRight;
//				}
//				
//			}
	
		}
		
		return true;
		
	}
	
	private Vector2D getPassSingleAngle(List<Double> passAngleArr) {
		
		Vector2D result = ai.tarMovePos;
		
		boolean canAdjust = false;
		double passAngle = 9999;
		//找到绝对值最小的 并且小于100度的 可以通过的角度 按照趋势查找
		if(ai.moveDirFrom == AIMoveDirFromKey.FromNon) {
			for (Double d1 : passAngleArr) {
				double temp = Math.abs(d1);
				if(temp < 300 && passAngle > temp) {
					canAdjust = true;
					passAngle = d1;
			}
			if(canAdjust) {
				if(d1 > 0)
					ai.moveDirFrom = AIMoveDirFromKey.FromLeft;
				} else {
					ai.moveDirFrom = AIMoveDirFromKey.FromRight;
				}
			}
				
		} else if(ai.moveDirFrom == AIMoveDirFromKey.FromLeft) {
			for (Double d1 : passAngleArr) {
				if(d1 < 0) continue;
				
				double temp = Math.abs(d1);
				if(temp < 300 && passAngle > temp) {
					canAdjust = true;
					passAngle = d1;
				}
			}
		} else if(ai.moveDirFrom == AIMoveDirFromKey.FromRight) {
			for (Double d1 : passAngleArr) {
				if(d1 > 0) continue;
				double temp = Math.abs(d1);
				if(temp < 300 && passAngle > temp) {
					canAdjust = true;
					passAngle = d1;
				}
			}
		}
		
		if(canAdjust) {
			double rad = (Math.PI * passAngle / 180);
			//计算按照新的向量可以走的路
			double tempSin = Math.sin(rad);
			double tempCos = Math.cos(rad);
			Vector2D newPos = new Vector2D();
			double deltaX = ai.tarMovePos.x - ai.unitObj.posNow.x;
			double deltaY = ai.tarMovePos.y - ai.unitObj.posNow.y;
			newPos.x = ai.unitObj.posNow.x + (deltaX * tempCos - deltaY * tempSin); 
			newPos.y = ai.unitObj.posNow.y + (deltaX * tempSin + deltaY * tempCos);
			
//			Vector3D posBegin = StageManager.getHeight(ai.unitObj.stageObj.sn, ai.unitObj.posNow);
//			Vector3D posEnd = StageManager.getHeight(ai.unitObj.stageObj.sn, newPos);
//			Vector3D endReal = PathFinding.raycast(ai.unitObj.stageObj.sn, posBegin, posEnd, ai.unitObj.stageObj.pathFindingFlag);
//			//如果newPos 是一个不可通过点， 那么没有找到 继续查找
//			if(endReal.toVector2D().distance(ai.unitObj.posNow) < 0.3) {
//				passAngleArr.remove(passAngle);
//				result = getPassSingleAngle(passAngleArr);
//			} else {
//				result = newPos;
//			}
			
			return newPos;
			
		}
		else {
			if(ai.moveDirFrom == AIMoveDirFromKey.FromRight) {
				ai.moveDirFrom = AIMoveDirFromKey.FromLeft;
			} else if(ai.moveDirFrom == AIMoveDirFromKey.FromLeft) {
				ai.moveDirFrom = AIMoveDirFromKey.FromRight;
			}
			
		}
		
		
		return result;
		
	}
	
	private List<Double> getPassAngle(List<Double> angleArr, double min) {
		List<Double> result = new ArrayList<Double>();
		
		int count = (int)(360 / (MathUtils.COLLSION_ANGLE + 1));
		double gap = MathUtils.COLLSION_ANGLE  + 1;
		double plusAngle = 0;
		if(min > 0) {
			gap = -gap;
		} 
		
		for(int i = 0 ; i < count ; i++) {
			//1 -1 2 -2
			plusAngle = gap * ((int)(i / 2) + 1);
			if((i % 2) == 1) {
				plusAngle = -plusAngle;
			}
			
			double testAngle = min + plusAngle;
			if(!inAngleArr(angleArr, testAngle)) {
				result.add(testAngle);
			}
		}
		
		
		return result;
	}
	
	private boolean inAngleArr(List<Double> angleArr, double testAngle) {
		for (Double d1 : angleArr) {
			if(d1 > (testAngle - MathUtils.COLLSION_ANGLE) 
					&& d1 < (testAngle + MathUtils.COLLSION_ANGLE) ) {
				return true;
			}
		}
		return false;
	}
	
	
}
