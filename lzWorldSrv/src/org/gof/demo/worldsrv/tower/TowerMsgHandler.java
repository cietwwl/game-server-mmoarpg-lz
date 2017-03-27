package org.gof.demo.worldsrv.tower;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfTower;
import org.gof.demo.worldsrv.entity.Tower;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.CSLayerAward;
import org.gof.demo.worldsrv.msg.Msg.CSLayerEnd;
import org.gof.demo.worldsrv.msg.Msg.CSLayerEnter;
import org.gof.demo.worldsrv.msg.Msg.CSLayerLeave;
import org.gof.demo.worldsrv.msg.Msg.CSLayerRefreshBuff;
import org.gof.demo.worldsrv.msg.Msg.CSRefreshTower;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;

public class TowerMsgHandler {

	// 进入关卡
	@MsgReceiver(CSLayerEnter.class)
	public void onCSLayerEnter(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		Tower tower = humanObj.dataPers.tower;
		int layerSn = tower.getCurLayer();
		ConfTower confTower = null;
		
		//判断副本配置是否合法
		confTower = ConfTower.get(layerSn);
		if(confTower == null) {
			Log.stageCommon.info("切换地图错误！");
			Inform.user(humanObj.id, Inform.提示错误, I18n.get("instance.msghandler.onCSInstanceEnter.wrongmap"));
			return;
		}
		
		//判断是否满足副本进入条件
//		boolean canRep = TowerManager.inst().canEnterRep(humanObj, confRep);
//		if(!canRep) {
//			Inform.user(humanObj.id, Inform.提示错误, I18n.get("instance.enter.error.notInCommon"));
//			return;
//		}

		//建立关卡
		TowerManager.inst().create(humanObj, confTower);
	}
	
	// 关卡结束
	@MsgReceiver(CSLayerEnd.class)
	public void onCSLayerEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst().layerEnd(humanObj);
	}
	
	// 离开关卡
	@MsgReceiver(CSLayerLeave.class)
	public void onCSLayerLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst().leave(humanObj);
	}	
	
	// 领取关卡奖励
	@MsgReceiver(CSLayerAward.class)
	public void onCSLayerAward(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSLayerAward msg = param.getMsg();
		int layerSn = msg.getLayerSn();
		TowerManager.inst().award(humanObj, layerSn);
	}
	
	// 刷新BUFF
	@MsgReceiver(CSLayerRefreshBuff.class)
	public void onCSRefreshBuff(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst().refreshBuff(humanObj);
	}
	
	// 刷新所有关卡
	@MsgReceiver(CSRefreshTower.class)
	public void onCSRefreshTower(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		TowerManager.inst().refreshTower(humanObj);
	}
}
