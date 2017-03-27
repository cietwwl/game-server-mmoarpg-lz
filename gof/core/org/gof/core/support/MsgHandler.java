package org.gof.core.support;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;

public abstract class MsgHandler {
	private static final Map<Class<?>, MsgHandler> instances = new ConcurrentHashMap<>();
	private final Logger log = LogCore.msg;
	
	//发布消息
	protected abstract void fire(GeneratedMessage msg, Param param);
	//通过消息ID获取消息类型
	protected abstract GeneratedMessage parseFrom(int type, CodedInputStream s) throws IOException;

	/**
	 * 获取唯一实例
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends MsgHandler> T getInstance(Class<?> clazz) {
		try {
			Object inst = instances.get(clazz);
			if(inst == null) {
				Constructor<?> constr = clazz.getDeclaredConstructor();
				constr.setAccessible(true);
				inst = constr.newInstance();
			}
			
			return (T)inst;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 消息处理
	 * @param buffer
	 */
	public void handle(byte[] buffer, Object...params) {
		//取出消息头
		@SuppressWarnings("unused")
		int len = Utils.bytesToInt(buffer, 0);		//消息长度
		int msgId = Utils.bytesToInt(buffer, 4);		//消息ID
		
		//记录日志
		if(log.isDebugEnabled() && msgId != 1211 && msgId != 1301) {
			log.debug("接收到客户端消息：id={}", msgId);
		}

		//取出消息体
		CodedInputStream in = CodedInputStream.newInstance(buffer, 8, buffer.length - 8);
		
		try {
			//利用反射解析协议
			GeneratedMessage msg = parseFrom(msgId, in);
			//发送接到消息事件
			fire(msg, new Param(params));
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
}
