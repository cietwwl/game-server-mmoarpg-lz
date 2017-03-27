package org.gof.demo.worldsrv.stage;

import java.util.List;

import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.CSStageEnter;
import org.gof.demo.worldsrv.msg.Msg.CSStageSwitch;
import org.gof.demo.worldsrv.msg.Msg.SCStageEnterEnd;
import org.gof.demo.worldsrv.msg.Msg.SCStageEnterResult;
import org.gof.demo.worldsrv.scene.SceneManager;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class StageMsgHandler {

	/**
	 * 玩家准备好 进入地图
	 * @param param
	 */
	@MsgReceiver(CSStageEnter.class)
	public void onCSStageEnter(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		StageObject stageObj = humanObj.stageObj;

		Event.fireEx(EventKey.HUMAN_STAGE_ENTER_BEFORE, stageObj.sn, "humanObj", humanObj);
		
		//通知本人 本区域的地图单元信息
		SCStageEnterResult.Builder msgER = SCStageEnterResult.newBuilder();
		List<WorldObject> list = StageManager.inst().getWorldObjsInArea(humanObj.stageObj, humanObj.posNow);
		for(WorldObject o : list) {
//		for(WorldObject o : humanObj.stageObj.getUnitObjs().values()) {
			if(!o.isInWorld()) continue;
//			if(o.equals(humanObj)) continue;

			msgER.addObj(o.createMsg());
		}
		humanObj.sendMsg(msgER);
		
		//死亡的时候不发stageEnter
		if(!humanObj.isDie()) {
			humanObj.stageShow();
		}
		
		//玩家登录到地图中的事件（切换地图是不会触发）
		if(humanObj.loginStageState == 1) {
			Event.fire(EventKey.HUMAN_LOGIN_FINISH, "humanObj", humanObj);
			humanObj.loginStageState = 0;
		} else if(humanObj.loginStageState == 2) {
			Event.fire(EventKey.HUMAN_LOGIN_FINISH_FIRST_TODAY, "humanObj", humanObj);
			Event.fire(EventKey.HUMAN_LOGIN_FINISH, "humanObj", humanObj);
			humanObj.loginStageState = 0;
		}
		
		//发送进入地图事件
		Event.fireEx(EventKey.HUMAN_STAGE_ENTER, stageObj.sn, "humanObj", humanObj);
		
		//发送场景单元完成的消息
		stageObj.getPort().addQueue(new PortPulseQueue("humanObj", humanObj) {
			@Override
			public void execute(Port port) {
				HumanObject humanObj = param.get("humanObj");
				SCStageEnterEnd.Builder msg = SCStageEnterEnd.newBuilder();
				if(stageObj.randUtils != null) {
					for (int seek : stageObj.randUtils.seek) {
						msg.addSeek(seek);
					}
				}
				
				StageManager.inst().sendMsgToArea(msg, humanObj.stageObj, humanObj.posNow);
			}
		});
		
		//发送副本结构
		SceneManager.inst().sendScenePlot(stageObj, humanObj);
		
		//如果是副本，那么处理剧情事件
		if(stageObj.conf.type.equals(StageMapTypeKey.rep.getContent())) {
			Event.fire(EventKey.SCENE_TRIGGER_07, "stageObj", stageObj);
		}
	}
	
	/**
	 * 切换地图
	 * @param param
	 */
	@MsgReceiver(CSStageSwitch.class)
	public void onCSStageSwitch(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSStageSwitch msg = param.getMsg();
		int mapSn = msg.getMapSn();
		
		ConfMap conf = ConfMap.get(mapSn);
		if(conf == null) {
			Log.stageCommon.info("切换地图错误！");
			Inform.user(humanObj.id, Inform.提示操作, "切换地图错误！");
			return;
		}
		
		StageObject stageObj = humanObj.stageObj;
		
		//取出目标点
		Vector2D posAppear = StageManager.inst().getBirthPosFromMapSn(mapSn);
		
		
		//如果是本地图内传
		if(stageObj.sn == mapSn) {
			StageManager.inst().pullTo(humanObj, posAppear);
		} else if(humanObj.stageObj.conf.type.equals(StageMapTypeKey.rep.getContent())) {
			posAppear = HumanManager.inst().stageHistoryCommon(humanObj);
			StageManager.inst().switchTo(humanObj, mapSn, posAppear);
		}
		else {
			StageManager.inst().switchTo(humanObj, mapSn, posAppear);
		}
	}
	
}