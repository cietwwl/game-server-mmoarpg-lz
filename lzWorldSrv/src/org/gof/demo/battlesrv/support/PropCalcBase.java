package org.gof.demo.battlesrv.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.gof.core.support.Utils;

/**
 * 通用属性累加
 *
 */
public abstract class PropCalcBase<K, V> {
	protected final Map<K, V> datas = new HashMap<>();
	
	protected abstract K toKey(String key);
	protected abstract V toValue(Object value);
	protected abstract boolean canDiscard(Object value);
	
	public PropCalcBase() {

	}
	
	public PropCalcBase(String json) {
		if (StringUtils.isBlank(json)) {
			return;
		}
		JSONObject data = JSON.parseObject(json);
		for (Entry<String, Object> entry : data.entrySet()) {
			K k = this.toKey(entry.getKey());
			V v = this.toValue(entry.getValue());

			datas.put(k, v);
		}
	}
	
	/**
	 * 转换为可储存的文本格式
	 * @return
	 */
	public String toJSONStr() {
		//去除所有可以忽略的选择
		Iterator<Map.Entry<K, V>> it = datas.entrySet().iterator();  
        while(it.hasNext()){  
        	Map.Entry<K, V> entry=it.next();
        	if(canDiscard(entry.getValue())) {
        		it.remove();
        	}
        }

		return Utils.toJSONString(datas);
	}
	
	/**
	 * 累加属性加成
	 * @param plus
	 * @return
	 */
	public PropCalcBase<K, V> plus(PropCalcBase<K, V> plus) {
		Map<K, V> datasNew = plus.datas;
		for(Entry<K, V> entry : datasNew.entrySet()) {
			K k = entry.getKey();
			V v = entry.getValue();
			
			//累加属性
			plus(k, v);
		}
		
		return this;
	}
	
	/**
	 * 累加属性加成
	 * @param plus
	 * @return
	 */
	public PropCalcBase<K, V> plus(K key, V value) {
		Double valueNew = getDouble(key) + Utils.doubleValue(value.toString());
		datas.put(key, this.toValue(valueNew));
		return this;
	}
	
	/**
	 * 累加属性加成
	 * @param plus
	 * @return
	 */
	public PropCalcBase<K, V> plus(String json) {
		if(StringUtils.isBlank(json)) return this;
		JSONObject data = JSON.parseObject(json);
		for (Entry<String, Object> entry : data.entrySet()) {
			K k = this.toKey(entry.getKey());
			V v = this.toValue(entry.getValue());
			plus(k, v);
		}
		return this;
	}
	
	public PropCalcBase<K, V> plus(Map<String, Double> map) {
		if(map == null || map.size() <= 0) return this;
		for (Entry<String, Double> entry : map.entrySet()) {
			K k = this.toKey(entry.getKey());
			V v = this.toValue(entry.getValue());
			plus(k, v);
		}
		return this;
	}
	
	public PropCalcBase<K, V> plus(String[] propKey, int[] propValue) {
		if(propKey != null && propValue != null && propKey.length == propValue.length) {
			for(int i = 0 ; i < propKey.length ; i++) {
				K k = this.toKey(propKey[i]);
				V v = this.toValue(propValue[i]);
				plus(k, v);
			}
		}
		return this;
	}
	
	/**属性系数
	 * @param key
	 * @param value
	 * @return
	 */
	public PropCalcBase<K, V> mul(K key, float value) {
		Double valueNew = getDouble(key) * value;
		datas.put(key, this.toValue(valueNew));
		return this;
	}
	
	/**
	 * 属性系数
	 * @param propKey
	 * @param propValue
	 * @return
	 */
	public PropCalcBase<K, V> mul(String[] propKey, float[] propValue) {
		if(propKey != null && propValue != null && propKey.length == propValue.length) {
			for(int i = 0 ; i < propKey.length ; i++) {
				K k = this.toKey(propKey[i]);
				float v = propValue[i];
				mul(k, v);
			}
		}
		return this;
	}
	/**
	 * 减去属性加成
	 * @param plus
	 * @return
	 */
	public PropCalcBase<K, V> minus(PropCalcBase<K, V> plus) {
		Map<K, V> datasNew = plus.datas;
		for(Entry<K, V> entry : datasNew.entrySet()) {
			K k = entry.getKey();
			V v = entry.getValue();
			
			//累加属性
			minus(k, v);
		}
		
		return this;
	}
	
	/**
	 * 减去属性加成
	 * @param plus
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public PropCalcBase<K, V> minus(K key, V value) {
		Double valueNew = getDouble(key) - Utils.doubleValue(value.toString());
		
		datas.put(key, (V)valueNew);
		
		return this;
	}	
	
	/**
	 * 移除某个属性
	 * @param key
	 * @return
	 * 2013-5-22
	 */
	public PropCalcBase<K, V> remove(K key) {
		datas.remove(key);
		return this;
	}
	
	public PropCalcBase<K, V> removeAll() {
		datas.clear();
		return this;
	}
	
	/**
	 * 设置累加属性
	 * @param plus
	 * @return
	 */
	public PropCalcBase<K, V> put(K key, V value) {
		datas.put(key, value);
		
		return this;
	}
	

	/**
	 * 获取int型数值
	 * @param key
	 * @return
	 */
	public int getInt(K key) {
		V v = datas.get(key);
		if(v == null) return 0;
		
		return Utils.intValue(v.toString());
	}
	
	/**
	 * 获取Double型数值
	 * @param key
	 * @return
	 */
	public double getDouble(K key) {
		V v = datas.get(key);
		if(v == null) return 0.0;
		
		return Utils.doubleValue(v.toString());
	}
	
	/**
	 * 获取当前键值的Map对象
	 * @return
	 */
	public Map<K,V> getDatas() {
		return datas;
	}
	
	@Override
	public String toString() {
		return toJSONStr();
	}
}
