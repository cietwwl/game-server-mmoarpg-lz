package org.gof.core.connsrv;

import java.util.Iterator;

import org.gof.core.Port;
import org.gof.core.connsrv.netty.ServerHandler;

public class ConnPort extends Port {
	public ConnPort(String name) {
		super(name);
	}
	
	public void openConnection(Connection connection) {
		addService(connection);
	}
	
	public void closeConnection(Connection connection) {
		delServiceBySafe(connection.getId());
	}
	
	@Override
	public void pulseOverride() {
		super.pulseOverride();

		//接收数据
		for(Iterator<Connection> iter = ServerHandler.conns.iterator(); iter.hasNext();) {
			iter.next().handleInput();
		}
	}
}
