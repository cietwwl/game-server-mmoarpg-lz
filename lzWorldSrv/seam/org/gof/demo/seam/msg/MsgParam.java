package org.gof.demo.seam.msg;

import org.gof.core.support.observer.MsgParamBase;
import org.gof.demo.worldsrv.character.HumanObject;

import com.google.protobuf.GeneratedMessage;

/**
 * 游戏中的客户端消息都通过这个类封装
 */
public class MsgParam extends MsgParamBase {
	private HumanObject humanObj;		//发送消息的玩家
	
	public MsgParam(GeneratedMessage msg) {
		super(msg);
	}

	public HumanObject getHumanObject() {
		return humanObj;
	}
	
	public void setHumanObject(HumanObject humanObj) {
		this.humanObj = humanObj;
	}
}