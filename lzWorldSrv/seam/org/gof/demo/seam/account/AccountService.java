package org.gof.demo.seam.account;

import java.util.HashMap;
import java.util.Map;

import org.gof.core.CallPoint;
import org.gof.core.Port;
import org.gof.core.Service;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.ConnectionStatus;
import org.gof.core.support.Distr;
import org.gof.core.support.MsgHandler;
import org.gof.demo.seam.msg.AccountExtendMsgHandler;

@DistrClass(
	importClass = {ConnectionStatus.class}
)
public class AccountService extends Service {
	//消息处理类
	private AccountExtendMsgHandler msgHandler = MsgHandler.getInstance(AccountExtendMsgHandler.class);
	
	//存储选人阶段的信息
	public Map<Long, AccountObject> datas = new HashMap<>();
	
	/**
	 * 构造函数
	 * @param port
	 */
	public AccountService(Port port) {
		super(port);
	}

	@DistrMethod
	public void msgHandler(long connId, ConnectionStatus status, byte[] msgbuf) {
		CallPoint connPoint = new CallPoint();
		connPoint.nodeId = port.getCallFromNodeId();
		connPoint.portId = port.getCallFromPortId();
		connPoint.servId = connId;
		
		msgHandler.handle(msgbuf, "connPoint", connPoint, "serv", this, "connId", connId, "connStatus", status);
	}
	
	@DistrMethod
	public void connClosed(long connId) {
		datas.remove(connId);
	}
	
	@DistrMethod
	public void connCheck(long connId) {
		port.returns(datas.containsKey(connId));
	}

	@Override
	public Object getId() {
		return Distr.SERV_GATE;
	}

	public Port getPort() {
		return port;
	}
}
