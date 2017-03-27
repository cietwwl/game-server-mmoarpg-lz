package org.gof.demo.worldsrv;

import java.util.Map;
import java.util.Map.Entry;

import org.gof.core.db.FieldSet;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.Param;

/**
 * 初始化系统数据
 */
public class InitFieldSet {
	private boolean completed = false;
	
	public boolean isCompleted() {
		return completed;
	}
	
	public void init() {
		//请求获取FieldSet
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findFieldSet();
		
		//等待返回值
		Param result = prx.waitForResult();
		Map<String, FieldSet> results = result.get();
		
		//缓存信息
		for(Entry<String, FieldSet> e : results.entrySet()) {
			FieldSet.put(e.getKey(), e.getValue());
		}
		
		completed = true;
	}
}
