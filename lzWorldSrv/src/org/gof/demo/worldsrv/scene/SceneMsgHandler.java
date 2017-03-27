package org.gof.demo.worldsrv.scene;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSSceneEvent;
import org.gof.demo.worldsrv.msg.Msg.CSSceneTrigger;

/**
 * 场景剧情接口
 * 
 * @author GaoZhangCheng
 */
public class SceneMsgHandler {
	
	/**
	 * 前端激活触发
	 * @param param
	 */
	@MsgReceiver(CSSceneTrigger.class)
	public void onCSSceneTrigger(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		CSSceneTrigger msg = param.getMsg();
		SceneManager.inst().clientDoTrigger(humanObj.stageObj, msg.getPoltId(), msg.getTriggerId());
	}
	
	/**
	 * 前端完成事件
	 * @param param
	 */
	@MsgReceiver(CSSceneEvent.class)
	public void onCSSceneEvent(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		CSSceneEvent msg = param.getMsg();
		SceneManager.inst().finishEvent(msg.getEventId(), msg.getPoltId(), humanObj.stageObj);
	}
}
