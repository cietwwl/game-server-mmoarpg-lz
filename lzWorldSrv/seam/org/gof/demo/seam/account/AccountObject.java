package org.gof.demo.seam.account;

import org.gof.core.CallPoint;
import org.gof.core.support.ConnectionStatus;

public class AccountObject {
	protected ConnectionStatus status;		//当前连接状态
	protected AccountService serv;				//当前所属线程
	protected long id;						//主键ID等于连接ID
	protected long humanId;				//玩家ID
	protected CallPoint connPoint;			//连接点
	
	public AccountObject(long id, AccountService serv, ConnectionStatus status, CallPoint connPoint) {
		this.id = id;
		this.serv = serv;
		this.status = status; 
		this.connPoint = connPoint;
	}
	
	public long getId() {
		return id;
	}
}