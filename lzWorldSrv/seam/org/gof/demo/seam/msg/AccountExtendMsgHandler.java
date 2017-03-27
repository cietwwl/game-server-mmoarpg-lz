package org.gof.demo.seam.msg;

import java.io.IOException;

import org.gof.core.CallPoint;
import org.gof.core.support.ConnectionStatus;
import org.gof.core.support.MsgHandler;
import org.gof.core.support.Param;
import org.gof.core.support.observer.MsgSender;
import org.gof.demo.seam.account.AccountMsgHandler;
import org.gof.demo.seam.account.AccountService;
import org.gof.demo.worldsrv.msg.MsgIds;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

public class AccountExtendMsgHandler extends MsgHandler {
	private AccountExtendMsgHandler() { }
	
	@Override
	protected void fire(GeneratedMessage msg, Param param) {
		//忽略不是本阶段要关心的协议
		if(!AccountMsgHandler.methods.contains(msg.getClass())) {
			return;
		}
		
		//拼写参数
		MsgParamAccount mp = new MsgParamAccount(msg);
		mp.setConnPoint(param.<CallPoint>get("connPoint"));
		mp.setService(param.<AccountService>get("serv"));
		mp.setConnId(param.<Long>get("connId"));
		mp.setConnStatus(param.<ConnectionStatus>get("connStatus"));
		
		//输出当登陆人物
//		if(LogCore.conn.isDebugEnabled()) {
//			LogCore.conn.debug("msg={}, connId={}, msgStr={}", msg.getClass(), param.<Long>get("connId"), msg.toString());
//		}
		
		MsgSender.fire(mp);
	}

	@Override
	protected GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException {
		return MsgIds.parseFrom(type, s);
	}

}
