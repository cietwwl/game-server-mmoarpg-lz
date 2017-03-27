package org.gof.demo.worldsrv.name;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.msg.Msg.CSChangeName;
import org.gof.demo.worldsrv.msg.Msg.CSChangeNameRandom;

public class NameMsgHandler {
	
	/**
	 * 改名字
	 * 
	 * @param param
	 */
	@MsgReceiver(CSChangeName.class)
	public void onCSChangeName(MsgParam param) {
		CSChangeName msg = param.getMsg();
		
		NameManager.inst().changeName(param.getHumanObject(), msg.getName());
	}
	
	/**
	 * 获取一个随机名字
	 * 
	 * @param param
	 */
	@MsgReceiver(CSChangeNameRandom.class)
	public void CSChangeNameRandom(MsgParam param) {
		
		NameManager.inst().randomName(param.getHumanObject());
	}
}
