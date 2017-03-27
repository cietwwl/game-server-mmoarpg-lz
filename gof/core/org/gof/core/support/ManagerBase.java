package org.gof.core.support;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ManagerBase {
	public static final Map<String, ManagerBase> instances = new ConcurrentHashMap<>();
	
	static {
		init();
	}
	/**
	 * 获取唯一实例, 并且记录引用
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ManagerBase> T inst(Class<?> clazz, Object refClazz) {
		Object inst = instances.get(clazz.getName());
		if(inst == null) {
			throw new SysException("获取Manager实例时出错：未能找到对应实例，class={}", clazz);
		}
		
		return (T)inst;
	}
	
	/**
	 * 获取唯一实例
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected static <T extends ManagerBase> T inst(Class<?> clazz) {
		Object inst = instances.get(clazz.getName());
		if(inst == null) {
			throw new SysException("获取Manager实例时出错：未能找到对应实例，class={}", clazz);
		}
		return (T)inst;
	}
	
	public static void init()  {
		instances.clear();
		
		try {
			Set<Class<?>> classSet = PackageClass.find();
			
			//从ClassLoader中读取所有的Manager
			for (Class<?> clazz : classSet) {
				// 只需要加载ManagerClass注解数据
				if (!Utils.isInstanceof(clazz, ManagerBase.class) || Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}
			
				GofClassLoader.newInstance(Utils.getClassPath(), clazz.getName());
			}
			
			//等待1秒
			Thread.sleep(1000);
			
			//创建实例
			for (Class<?> clazz : classSet) {
				// 只需要加载ManagerClass注解数据
				if (!Utils.isInstanceof(clazz, ManagerBase.class) || Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}
				
				// 创建实例
				Object inst = clazz.newInstance();
				instances.put(clazz.getName(), (ManagerBase) inst);
			}
			
		} catch (InterruptedException | InstantiationException | IllegalAccessException e) {
			throw new SysException(e);
		} 
	}
	
}