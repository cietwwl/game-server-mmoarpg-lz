package org.gof.demo.worldsrv.human;

import org.gof.core.CallPoint;
import org.gof.core.Chunk;
import org.gof.core.connsrv.ConnectionProxy;
import org.gof.core.support.ManagerBase;
import org.gof.demo.worldsrv.msg.MsgIds;

import com.google.protobuf.Message;

public class HumanGlobalManager extends ManagerBase {
	/**
	 * 获取实例
	 * @return
	 */
	public static HumanGlobalManager inst() {
		return inst(HumanGlobalManager.class);
	}
	
	/**
	 * 发送消息
	 * @param connPoint
	 * @param msg
	 */
	public void sendMsg(CallPoint connPoint, Message msg) {
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(MsgIds.getIdByClass(msg.getClass()), new Chunk(msg));
	}
}