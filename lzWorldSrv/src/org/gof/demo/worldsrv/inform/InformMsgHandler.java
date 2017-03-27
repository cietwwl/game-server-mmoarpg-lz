package org.gof.demo.worldsrv.inform;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSInformChat;
import org.gof.demo.worldsrv.support.Log;
import org.slf4j.Logger;

public class InformMsgHandler {
	Logger logInform = Log.inform;
	Logger logChat = Log.chat;
	
	/**
	 * 聊天
	 * @param param
	 */
	@MsgReceiver(CSInformChat.class)
	public void onChat(MsgParam param){
		CSInformChat msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		
		//禁言
//		ReasonResult result = HumanManager.inst().isChatNotForbit(humanObj);
//		if(!result.success) {
//			Inform.user(humanObj.id, Inform.提示错误, result.reason);
//			return ;
//		}
		
		int channel = msg.getChannel();			//频道
		String content = msg.getContent();		//内容	
		
		InformManager.inst().chat(humanObj, channel, content);
	}

}