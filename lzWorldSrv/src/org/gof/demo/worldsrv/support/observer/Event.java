package org.gof.demo.worldsrv.support.observer;

import java.lang.annotation.Annotation;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.core.support.observer.ObServer;

public class Event extends ObServer<Integer, Param> {
	public static final Event instance = new Event();
	
	/**
	 * 获取事件接收对象的实例
	 * @param targetClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getTargetBean(Class<T> targetClass) {
		try {
			if(!Utils.isInstanceof(targetClass, ManagerBase.class)) {
				throw new SysException("只有ManagerBase的子类才能监听Event事件，当前错误Class={}", targetClass);
			}
			
			return (T)ManagerBase.inst(targetClass, this);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 初始化
	 * 遍历全部给定包 找到观察者的处理函数 并缓存起来待用
	 * @param packageNames
	 */
	//public static void init() {
	//	init("");
	//}
	
	/**
	 * 初始化
	 * 遍历全部给定包 找到观察者的处理函数 并缓存起来待用
	 * @param packageNames
	 */
	//public static void init(String...packageNames) {
	//	instance.initHandler(packageNames);
	//}
	
	/**
	 * 发布无参事件
	 * @param key
	 */
	public static void fire(int key) {
		instance.fireHandler(key, null);
	}
	
	/**
	 * 发布无参事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey) {
		instance.fireHandler(key, subKey);
	}
	
	/**
	 * 发布事件
	 * @param key
	 */
	public static void fire(int key, Param param) {
		instance.fireHandler(key, null, param);
	}
	
	/**
	 * 发布事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey, Param param) {
		instance.fireHandler(key, subKey, param);
	}
	
	/**
	 * 发布事件
	 * @param key
	 */
	public static void fire(int key, Object...params) {
		instance.fireHandler(key, null, new Param(params));
	}
	
	/**
	 * 发布事件
	 * @param key
	 */
	public static void fireEx(int key, Object subKey, Object...params) {
		instance.fireHandler(key, subKey, new Param(params));
	}

	@Override
	protected Class<? extends Annotation> getListenerAnnotation() {
		return Listener.class;
	}
}