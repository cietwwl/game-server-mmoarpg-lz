package org.gof.demo.worldsrv.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.gof.core.gen.GenBase;
import org.gof.core.support.ConfigJSON;
import org.gof.core.support.ManagerBase;
import org.gof.demo.worldsrv.support.Log;

public class TestManager extends ManagerBase{
	public static boolean DEBUG = false;
	public static TestManager inst() {
		return inst(TestManager.class);
	}
	
	public void testManager() {
		Log.game.info("TestManager.testManager() @@");
	}
	
	public void reLoadConf(){
		
		Set<Class<?>> sources = GenBase.getSources("");
		// 遍历所有类，取出类中有@DistrClass注解的方法
//		ClassPool pool = ClassPool.getDefault();
		for(Class<?> clazz : sources) {
			// 如果没有@DistriClass注解, 则不处理
			if(!clazz.isAnnotationPresent(ConfigJSON.class)) {
				continue; 
			}
			Method m = null;
			if(true) {
				try {
					m = clazz.getMethod("reLoad");
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
				if(m != null) {
					try {
						m.invoke(clazz);
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
	}

}
