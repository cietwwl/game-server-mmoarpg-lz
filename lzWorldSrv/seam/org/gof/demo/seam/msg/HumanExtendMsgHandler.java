package org.gof.demo.seam.msg;

import java.io.IOException;

import org.gof.core.support.MsgHandler;
import org.gof.core.support.Param;
import org.gof.core.support.observer.MsgSender;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSStageMove;
import org.gof.demo.worldsrv.msg.Msg.CSStageMoveStop;
import org.gof.demo.worldsrv.msg.MsgIds;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

public class HumanExtendMsgHandler extends MsgHandler {
	private HumanExtendMsgHandler() {}
	
	@Override
	protected void fire(GeneratedMessage msg, Param param) {
		//如果用户正在切换地图中，则不接受任何请求
		HumanObject humanObj = param.get("humanObj");
		if(humanObj.isStageSwitching) {
			return ;
		}
		
		MsgParam mp = new MsgParam(msg);
		mp.setHumanObject(humanObj);
		
		//输出当前人物动作 刨除移动
		if(!(msg instanceof CSStageMove) && !(msg instanceof CSStageMoveStop)) {
			humanObj.startMsgTimer();
		}
		
		MsgSender.fire(mp);
	}
	
	@Override
	protected GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException {
		return MsgIds.parseFrom(type, s);
	}
}