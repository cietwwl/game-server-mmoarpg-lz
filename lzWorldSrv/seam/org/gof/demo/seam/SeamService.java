package org.gof.demo.seam;

import org.gof.core.Port;
import org.gof.core.support.SeamServiceBase;
/*
import java.lang.reflect.Method;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.gof.core.support.ConnectionStatus;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.HumanObjectService;
import org.gof.demo.seam.account.AccountService;
*/
import org.gof.demo.seam.account.AccountServiceProxy;
import org.gof.demo.worldsrv.character.HumanObjectServiceProxy;

/**
 * 继承父类 CORE会通过接口的返回值进行通信
 * 
 * @author GaoZhangCheng
 */
public class SeamService extends SeamServiceBase  {

	public SeamService(Port port) {
		super(port);
	}
	
	/**
	 * 登陆阶段接收消息函数
	 */
	@Override
	public int methodAccountMsg() {
		return AccountServiceProxy.EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_MSGHANDLER_LONG_CONNECTIONSTATUS_BYTES;
	}

	/**
	 * 游戏阶段接收消息函数
	 */
	@Override
	public int methodWorldMsg() {
		return HumanObjectServiceProxy.EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_MSGHANDLER_LONG_BYTES;
	}

	/**
	 * 登陆阶段连接中断消息函数
	 */
	@Override
	public int methodAccountLost() {
		return AccountServiceProxy.EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_CONNCLOSED_LONG;
	}

	/**
	 * 游戏阶段连接中断消息函数
	 */
	@Override
	public int methodWorldLost() {
		return HumanObjectServiceProxy.EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CONNCLOSED_LONG;
	}
	
	/**
	 * 登陆阶段连接验证消息函数
	 */
	@Override
	public int methodAccountCheck() {
		return AccountServiceProxy.EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_CONNCHECK_LONG;
	}

	/**
	 * 游戏阶段连接验证消息函数
	 */
	@Override
	public int methodWorldCheck() {
		return HumanObjectServiceProxy.EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CONNCHECK_LONG;
	}
}
