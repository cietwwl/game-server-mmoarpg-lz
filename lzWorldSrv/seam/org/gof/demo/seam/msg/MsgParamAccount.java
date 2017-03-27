package org.gof.demo.seam.msg;

import org.gof.core.CallPoint;
import org.gof.core.support.ConnectionStatus;
import org.gof.core.support.observer.MsgParamBase;
import org.gof.demo.seam.account.AccountService;

import com.google.protobuf.GeneratedMessage;

/**
 * 登陆阶段中的客户端消息都通过这个类封装
 */
public class MsgParamAccount extends MsgParamBase {
	private CallPoint connPoint;			//发送消息的连接点
	private AccountService service;			//所属服务
	private long connId;					//连接ID
	private ConnectionStatus connStatus;	//连接状态
	
	public MsgParamAccount(GeneratedMessage msg) {
		super(msg);
	}

	public CallPoint getConnPoint() {
		return connPoint;
	}

	public void setConnPoint(CallPoint connPoint) {
		this.connPoint = connPoint;
	}

	public AccountService getService() {
		return service;
	}

	public void setService(AccountService service) {
		this.service = service;
	}

	public long getConnId() {
		return connId;
	}

	public void setConnId(long connId) {
		this.connId = connId;
	}

	public ConnectionStatus getConnStatus() {
		return connStatus;
	}

	public void setConnStatus(ConnectionStatus connStatus) {
		this.connStatus = connStatus;
	}
}