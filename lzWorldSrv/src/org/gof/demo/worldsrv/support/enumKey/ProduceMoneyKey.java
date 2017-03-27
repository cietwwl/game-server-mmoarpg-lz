package org.gof.demo.worldsrv.support.enumKey;

import java.util.ArrayList;
import java.util.List;

public enum ProduceMoneyKey {
	coin(1),					//铜币
	gold(2),				//元宝
	expCur(3),			//经验
	actValue(4),			//活力
		
	genExpCur(5),     //伙伴经验 (上阵伙伴)
	competMoney(6);     //演武币 (竞技场商店专用)
	private ProduceMoneyKey(int type){
		this.type = type;
	} 
	
	private int type;
	
	public int getType() {
		return type;
	}
	
	public boolean keyEquals(String key) {
		return this.name().equals(key);
	}
	
	/**
	 * 根据int类型的type，获取Key
	 * @param type
	 * @return
	 */
	public static String getKeyByType(int type) {
		for(ProduceMoneyKey k : values()) {
			if(k.type == type)
				return k.name();
		}
		return null;
	}
	
	public static ProduceMoneyKey getEnumByType(int type) {
		for(ProduceMoneyKey k : values()) {
			if(k.type == type)
				return k;
		}
		return null;
	}
	
	/**
	 * 给定的字符串是否为货币的KEY值
	 * @param key
	 * @return
	 */
	public static boolean keyContains(String key) {
		for(ProduceMoneyKey k : values()) {
			if(k.keyEquals(key)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 返回枚举的 name 的集合
	 * @return
	 */
	public static List<String> toList() {
		List<String> result = new ArrayList<>();
		for (ProduceMoneyKey k : ProduceMoneyKey.values()) {
			result.add(k.name());
		}
		return result;
	}
}
