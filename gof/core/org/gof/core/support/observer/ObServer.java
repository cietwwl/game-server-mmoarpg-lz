package org.gof.core.support.observer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gof.core.support.SysException;
import org.gof.core.support.function.CommonFunction;


/**
 * 观察者模式基类
 * 职责
 * 1.初始化
 * 2.发布事件
 * 3.通知观察者
 */
public abstract class ObServer<K, P> {
	//<事件KEY, <类, 接收方法>>
	//private final Map<Object, Map<Class<?>, Set<CommonFunction>>> eventMethod = new HashMap<>();
	
	/**
	 * 获取监听这个事件的注解
	 * @return
	 */
	protected abstract Class<? extends Annotation> getListenerAnnotation();
	
	/**
	 * 获取事件接收对象的实例
	 * @param targetClass
	 * @return
	 */
	public abstract <T> T getTargetBean(Class<T> targetClass);
	
	/**
	 * 初始化
	 * 遍历全部给定包 找到观察者的处理函数 并缓存起来待用
	 * @param packageNames
	 */
	/*
	protected final void initHandler(String...packageNames) {
		//监听这个事件的注解
		Class<? extends Annotation> listenerAnnotation = getListenerAnnotation();
		
		//构建需要扫描的包
		Set<String> packages = new HashSet<>();
		for(String p : packageNames) {
			packages.add(p);
		}
		
		//需要扫描的类
		Set<Class<?>> classSet = PackageClass.find(packages);
		
		//扫描类方法 找到各方法监听的目标
		for(Class<?> c : classSet) {
			//忽略抽象类
			if(Modifier.isAbstract(c.getModifiers())) {
				continue;
			}
			
			//遍历函数
			for(Method method : c.getMethods()) {
				//根据注解进行判断是否关注
				if(!method.isAnnotationPresent(listenerAnnotation)) continue;
				
				//查看类关注的事件KEY				
				Set<String> keys = getListenerKey(method, listenerAnnotation);
				for(String key : keys) {
					//根据Key来获取类监听
					Map<Class<?>, Set<CommonFunction>> eventClass = eventMethod.get(key);
					if(eventClass == null) {
						eventClass = new HashMap<>();
						eventMethod.put(key, eventClass);
					}
					
					//找到下属事件
					Set<CommonFunction> methods = eventClass.get(c);
					if(methods == null) {
						methods = new LinkedHashSet<CommonFunction>();
						eventClass.put(c, methods);
					}
					
					//增加监听函数
					//TODO:
					//methods.add(method);
				}
			}
		}
	}
	*/

	private final Map<Object, Set<CommonFunction>> eventMethod = new HashMap<>();
	
	/***
	 * 注册时间侦听函数，此函数一般由自动生成的代码调用
	 * @param key
	 * @param function
	 * @param paramSize
	 */
	public final void reg(String key, Object function, int paramSize){
		Set<CommonFunction> funcs = eventMethod.get(key);
		if(null == funcs){
			funcs = new LinkedHashSet<>();
			eventMethod.put(key, funcs);
		}
		funcs.add(new CommonFunction(function ,paramSize));
	}
	/*
	 * 最终真正的执行
	 */
	private void _fireHandler(String fullKey, P param){
		Set<CommonFunction> funcs = eventMethod.get(fullKey);
		if(null != funcs){
			for(CommonFunction f : funcs){
				f.apply(param);
			}
		}
	}
	/**
	 * 发布无参事件
	 * @param key
	 */
	protected final void fireHandler(K key, Object subKey) {
		try {
			fireHandler(key, subKey, null);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	

	
	/**
	 * 发布事件
	 * 如果有子事件 那么会创建两个事件(主事件 + 子事件)
	 * @param key
	 * @param param
	 */
	protected final void fireHandler(K key, Object subKey, P param) {
			//创建事件关键字
			//如果有子事件 那么会创建两个事件(主事件 + 子事件)
			_fireHandler(createMethodKey(key, null), param);
			if(subKey != null) {
				_fireHandler(createMethodKey(key, subKey), param);
			}

			
			/*
			//根据事件名 寻找监听者
			for(String k : mks) {
				//监听了这个事件的类
				Map<Class<?>, Set<CommonFunction>> mthCls = eventMethod.get(k);
				if(mthCls == null) continue;
				
				//遍历类的函数
				for(Entry<Class<?>, Set<CommonFunction>> entry : mthCls.entrySet()) {
					Class<?> cls = entry.getKey();
					Set<CommonFunction> mth = entry.getValue();
					
					//观察者主体
					Object bean = getTargetBean(cls);
					if(bean == null) {
						LogCore.core.warn("获取观察者主体时失败，无法获得实例：cls={}", cls);
						continue;
					}
					
					for(CommonFunction m : mth) {
						//参数类型
						
						//参数个数
						int argNum = m.getParamCount();
						
						try {
							//不接受参数
							if(argNum == 0) {
								m.apply(bean);
							} else {	//接受参数
								m.apply(bean, param);
							}
						} catch (Exception e) {
							//吞掉异常 避免因为某个接收者报错 造成其他模块出问题
							LogCore.core.error("调用观察者函数时发生错误：bean={}, method={}, param={}", bean, m, param, e);
						}
					}
				}
			}*/
	}
	
	/**
	 * 获取被监听的事件关键字
	 * @param method
	 * @param listenerAnnotation
	 * @return
	 */
	public static Set<String> getListenerKey(Method method, Class<? extends Annotation> listenerAnnotation) {
		try {
			Annotation annotation = method.getAnnotation(listenerAnnotation);
			Method mKey = listenerAnnotation.getMethod("value");
			Object oKey= mKey.invoke(annotation);
			Object[] vKey;
			if(oKey instanceof int[]){
				int[] keys = (int[]) mKey.invoke(annotation);
				vKey = new Object[keys.length];
				for(int i = 0; i < keys.length; i++)
					vKey[i] = keys[i];
			}else{
				vKey = (Object[]) oKey ;
			}
			//获取注解设置的主事件
			//int[] vKey = (int[]) mKey.invoke(annotation);
			
			//获取注解设置的子事件
			Object[] vSubStr = {};
			int[] vSubInt = {};
			long[] vSubLong = {};
			for(Method m : listenerAnnotation.getMethods()) {
				String mName = m.getName();
				
				if("subStr".equals(mName)) vSubStr = (Object[]) m.invoke(annotation);
				else if("subInt".equals(mName)) vSubInt = (int[]) m.invoke(annotation);
				else if("subLong".equals(mName)) vSubLong = (long[]) m.invoke(annotation);
			}
			
			//多个子事件Key 只允许设置一个 这里确认和检查下
			int vSubCount = 0;
			if(vSubStr.length > 0) vSubCount++;
			if(vSubInt.length > 0) vSubCount++;
			if(vSubLong.length > 0) vSubCount++;
			
			//设置监听了多个不同类型的子事件
			if(vSubCount > 1) {
				throw new SysException("Observer监听参数设置错误，不允许同事设置多种不同参数类型的子事件："
						+ "mthod={}, anno={}, subStr={}, subInt={}, subLong={}",
						method, annotation, vSubStr, vSubInt, vSubLong);
			}
			
			//获取子类型设置
			String[] vSubKey = getSubKeysFromValue(vSubStr, vSubInt, vSubLong);
			
			//监听关键字
			Set<String> results = new HashSet<>();
			
			//需要监听的事件关键字
			for(Object k : vKey) {
				//如果主事件与子事件都有多个 那么会出现乘积 暂时先允许
				for(String sk : vSubKey) {
					String smk = createMethodKey(k, sk);
					results.add(smk);
				}
				
				//如果没有设置子事件 那么就生成主事件
				if(vSubKey.length == 0) {
					String smk = createMethodKey(k, null);
					results.add(smk);
				}
			}
			
			return results;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 根据不用的参数来获取最终配置
	 * 一段很恶心的判断
	 * @param vSubStr
	 * @param vSubInt
	 * @param vSubLong
	 * @return
	 */
	private static String[] getSubKeysFromValue(Object[] vSubStr, int[] vSubInt, long[] vSubLong) {
		//获取子类型设置
		String[] vSubKey = {};
		
		//字符参数
		if(vSubStr.length > 0) {
			vSubKey = new String[vSubStr.length];

			for(int i = 0; i < vSubStr.length; i++) {
				vSubKey[i] = vSubStr[i].toString();
			}
		
		//Int型参数
		} else if(vSubInt.length > 0) {
			vSubKey = new String[vSubInt.length];

			for(int i = 0; i < vSubInt.length; i++) {
				vSubKey[i] = ((Integer)vSubInt[i]).toString();
			}
		
		//Long型参数
		} else if(vSubLong.length > 0) {
			vSubKey = new String[vSubLong.length];

			for(int i = 0; i < vSubLong.length; i++) {
				vSubKey[i] = ((Long)vSubLong[i]).toString();
			}
		}
		
		return vSubKey;
	}
	
	/**
	 * 创建事件关键字
	 * 
	 * 虽然fire时是生成了两个事件，但是这里不能
	 * 这个函数不能改成
	 * @param key
	 * @param subKey
	 * @return
	 */
	private static String createMethodKey(Object key, Object subKey) {
		String mk = key.toString();
		
		//如果有 那么拼装子事件		
		if(subKey != null && !subKey.toString().equals("")) {
			mk = mk + "$" + subKey.toString();
		}
		
		return mk;
	}
}