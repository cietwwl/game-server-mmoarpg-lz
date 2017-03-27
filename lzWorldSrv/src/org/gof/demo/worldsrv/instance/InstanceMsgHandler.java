package org.gof.demo.worldsrv.instance;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.competition.CompetitionManager;
import org.gof.demo.worldsrv.config.ConfRep;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.CSBoxAward;
import org.gof.demo.worldsrv.msg.Msg.CSInstanceAuto;
import org.gof.demo.worldsrv.msg.Msg.CSInstanceEnd;
import org.gof.demo.worldsrv.msg.Msg.CSInstanceEnter;
import org.gof.demo.worldsrv.msg.Msg.CSInstanceLeave;
import org.gof.demo.worldsrv.msg.Msg.CSInstanceLottery;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.tower.TowerManager;

public class InstanceMsgHandler {
	
	/**
	 * 副本进入
	 * @param param
	 */
	@MsgReceiver(CSInstanceEnter.class)
	public void onCSInstanceEnter(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSInstanceEnter msg = param.getMsg();
		int instanceSn = msg.getInstSn(); //对应的repConfig索引
		ConfRep confRep = null;
		
		//判断副本配置是否合法
		confRep = ConfRep.get(instanceSn);
		if(confRep == null) {
			Log.stageCommon.info("切换地图错误！");
			Inform.user(humanObj.id, Inform.提示错误, "切换地图错误！");
			return;
		}

		//建立副本
		InstanceManager.inst().create(humanObj, confRep);
	}
	
	/**
	 * 离开副本
	 * @param param
	 */
	@MsgReceiver(CSInstanceLeave.class)
	public void onCSInstanceLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		InstanceManager.inst().leave(humanObj);
//		CompetitionManager.inst().competEnd(humanObj,false);
//		TowerManager.inst().layerEnd(humanObj);
	}
	
	/**
	 * 副本结束的时候由前端发过来的消息
	 * @param param
	 */
	@MsgReceiver(CSInstanceEnd.class)
	public void onCSBattleEnd(MsgParam param) {
		//这个接口只在副本完成的时候发送
		HumanObject humanObj = param.getHumanObject();
		
		InstanceManager.inst().repEnd(humanObj);
		
	}
	
	/**
	 * 副本扫荡
	 * @param param
	 */
	@MsgReceiver(CSInstanceAuto.class)
	public void onCSInstanceAuto(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSInstanceAuto msg = param.getMsg();
		
		InstanceManager.inst().instanceAuto(humanObj, msg.getInstSn(), msg.getNum());
		
	}
	
	/**
	 * 领取副本的箱子
	 * @param param
	 */
	@MsgReceiver(CSBoxAward.class)
	public void onCSInstanceBox(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSBoxAward msg = param.getMsg();
		
		InstanceManager.inst().instanceBoxAward(humanObj, msg.getChapterId(), msg.getIndex());
		
	}
	
	/**
	 * 抽奖
	 * @param param
	 */
	@MsgReceiver(CSInstanceLottery.class)
	public void onCSInstanceLottery(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSInstanceLottery msg = param.getMsg();
		
		InstanceManager.inst().instanceLottery(humanObj);
	}

}
