package org.gof.demo.battlesrv.msgHandler;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSStageMove;
import org.gof.demo.worldsrv.msg.Msg.CSStageMove2;
import org.gof.demo.worldsrv.msg.Msg.CSStageMoveStop;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class StageBattleMsgHandler {
	/**
	 * 玩家移动
	 * @param param
	 */
	@MsgReceiver(CSStageMove.class)
	public void onCSStageMove(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSStageMove msg = param.getMsg();
		long sendId = msg.getObjId();
		
		//获得指定角色
		if(humanObj.stageObj == null) {
			return;
		}
		UnitObject unitObj = humanObj.stageObj.getUnitObj(sendId);
//		if(unitObj.teamBundleID != humanObj.teamBundleID) {
//			Log.common.info("onCSStageMove unitObj = null");
//			return;
//		}
		if(unitObj == null) {
			return;
		}
		
        if (unitObj.isHumanObj())
        {
//        	Log.common.info("onCSStageMove {} {} {}", unitObj.posNow,msg.getPosBegin(), msg.getPosEndList());
        
        }
        Vector3D dir = new Vector3D();
        if (msg.getDir() != null)
        {
            dir = new Vector3D(msg.getDir());
        }
        
        unitObj.move(new Vector3D(msg.getPosBegin()), Vector3D.parseFrom(msg.getPosEndList()), dir, true);
		
		Event.fire(EventKey.STAGE_INTANCE_START, humanObj);
		//处理玩家的移动
	}
	
	/**
	 * 手机地图玩家移动
	 * @param param
	 */
	@MsgReceiver(CSStageMove2.class)
	public void onCSStageMove2(MsgParam param) {
//		HumanObject humanObj = param.getHumanObject();
//		CSStageMove2 msg = param.getMsg();
//		
//		float x1 = msg.getPosBegin().getX();
//		float y1 = msg.getPosBegin().getY();
//		float x2 = msg.getDirection().getX();		//当前位置走向方向上的某一个点的横坐标
//		float y2 = msg.getDirection().getY();		
//		
//		double distanceTemp = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
//		double cosA = (x2 - x1)/distanceTemp;
//		double sinA = (y2 - y1)/distanceTemp;
//		
//		Log.temp.debug("遥感移动");
//		double xEnd = x1 + 10001 * cosA;
//		double yEnd = y1 + 10001 * sinA;
//		
//		//处理玩家的移动
//		humanObj.move(new Vector2D(msg.getPosBegin()), Utils.ofList(new Vector2D(xEnd, yEnd)));
	}
	
	/**
	 * 玩家移动停止
	 * @param param
	 * @return
	 */
	@MsgReceiver(CSStageMoveStop.class)
	public void onCSStageMoveStop(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSStageMoveStop msg = param.getMsg();
		long sendId = msg.getSendId();
		Vector2D posEnd = new Vector2D(msg.getPosEnd());
		
		if(humanObj.stageObj == null) {
			return;
		}
		
		UnitObject unitObj = humanObj.stageObj.getUnitObj(sendId);
//		if(unitObj.teamBundleID != humanObj.teamBundleID) {
//				Log.common.info("CSStageMoveStop unitObj = null");
//				return;
//		}
//		if(!unitObj.isHumanObj()) {
//			Log.common.info("CSStageMoveStop !isHumanObj");
//		}
		if(unitObj == null) {
			return;
		}
		
		//停止
		unitObj.stop();
		
		if(!(posEnd.x == 0 && posEnd.y == 0)) {
//			if(unitObj.isHumanObj()) {
//				Log.common.info("CSStageMoveStop unitObj:{} error {}", unitObj.name, unitObj.posNow.distance(posEnd) );
//			}
			if(unitObj.posNow.distance(posEnd) < (unitObj.getUnit().getSpeed() / 100f) * 0.2) {
				unitObj.posNow = posEnd;
//				if(unitObj.isHumanObj()) {
//					Log.common.info("CSStageMoveStop unitObj:{} {}", unitObj.name, posEnd);
//				}
			} else {
				Log.common.info("CSStageMoveStop unitObj:{} id:{} {} pos error!", unitObj.name, unitObj.id, unitObj.posNow.distance(posEnd));
				Log.common.info("CSStageMoveStop {} {} {}", unitObj.posNow, posEnd, unitObj.posNow.distance(posEnd));
//				unitObj.posNow = posEnd;
			}
		} else {
//			Log.common.info("CSStageMoveStop posEnd:{}", posEnd);
		}
		
//		Log.common.info("CSStageMoveStop unitObj:{} id:{} {} pos error!", unitObj.name, unitObj.id, unitObj.posNow.distance(posEnd));
	}
}
