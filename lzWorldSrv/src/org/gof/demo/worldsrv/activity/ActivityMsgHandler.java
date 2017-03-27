package org.gof.demo.worldsrv.activity;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSSignIn;



public class ActivityMsgHandler {

	/**
	 * 每日签到
	 * @param param
	 */
	@MsgReceiver(CSSignIn.class)
	public void onCSSignIn(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		ActivityManager.inst().signIn(humanObj);
	}
	
}