package org.gof.core.connsrv;

import org.gof.core.Port;
import org.gof.core.Service;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.Distr;

@DistrClass
public class ConnService extends Service {

	public ConnService(Port port) {
		super(port);
	}

	@Override
	public Object getId() {
		return Distr.SERV_CONN;
	}
	
	/**
	 * 检查连接是否还存在
	 * 因为各种特殊原因，会出现连接已断开，但是游戏服务器没有清理的BUG。
	 * 出现这种问题后，发送的消息会不停报错，影响系统稳定。
	 * @param connId
	 */
	@DistrMethod
	public void check(long connId) {
		Service serv = port.getService(connId);
		
		port.returns(serv != null);
	}
}
