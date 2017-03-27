package org.gof.core.support;

import org.gof.core.Call;
import org.gof.core.Port;
import org.gof.core.Service;

/**
 * 整合世界服务器时 需要实现的接口
 * CORE会通过接口的返回值进行通信
 */
public abstract class SeamServiceBase extends Service {
	public abstract int methodAccountMsg();		//登陆阶段接收消息函数
	public abstract int methodWorldMsg();			//游戏阶段接收消息函数
	public abstract int methodAccountLost();		//登陆阶段连接中断消息函数
	public abstract int methodWorldLost();			//游戏阶段连接中断消息函数
	public abstract int methodAccountCheck();		//登陆阶段状态验证消息函数
	public abstract int methodWorldCheck();		//游戏阶段状态验证消息函数
	
	public SeamServiceBase(Port port) {
		super(port);
	}
	
	@Override
	public Object getId() {
		return Distr.SERV_SEAM;
	}
	
	public void handler(CallSeamMethodKey callMixKey, Call call) {
		//根据不同消息类型调用不同的函数进行处理
		int m = -1;
		if(callMixKey == CallSeamMethodKey.ACCOUNT_MSG) {
			m = methodAccountMsg();
		} else if(callMixKey == CallSeamMethodKey.WORLD_MSG) {
			m = methodWorldMsg();
		} else if(callMixKey == CallSeamMethodKey.ACCOUNT_LOST) {
			m = methodAccountLost();
		} else if(callMixKey == CallSeamMethodKey.WORLD_LOST) {
			m = methodWorldLost();
		} else if(callMixKey == CallSeamMethodKey.ACCOUNT_CHECK) {
			m = methodAccountCheck();
		} else if(callMixKey == CallSeamMethodKey.WORLD_CHECK) {
			m = methodWorldCheck();
		}
		
		//未能找到消息处理函数
		if(m < 0 ) {
			throw new SysException("未能找到正确的消息处理函数: callMixKey={}, call={}", callMixKey, call);
		}
		
		//设置请求的目标函数
		call.methodKey = m;

		//加入请求队列
		Port p = port.getNode().getPort(call.to.portId);
		try {
			p.addCall(call);
		} catch (Exception e) {
			throw e;
		}
		
	}
}