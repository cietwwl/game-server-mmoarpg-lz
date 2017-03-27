package org.gof.demo.battlesrv.support;

import java.util.ArrayList;
import java.util.List;


/**
 * 一些有别于propkey的字段，各个模块可能都要用到。可以理解为扩展的字段
 */
public enum PropExtKey {
	hpLossPct("扣血系数"),	
	hpLoss("扣血值"),	
	hpCur("当前血量值"),
	hpCurPct("当前血量系数"),
	mpCur("当前魔量值"),
	mpCurPct("当前魔量系数"),	
	
	hpMaxPct("最大血量系数"),		
	mpMaxPct("最大魔量系数"),		
	atkPct	("攻击系数"),	
	atkPhyPct	("物理攻击系数"),	
	atkMagPct	("法术攻击系数"),	
	defPct	("防御系数"),	
	defPhyPct	("物理防御系数"),	
	defMagPct	("法术防御系数"),	
	
	hitPct  ("命中系数"),		
	dodgePct("闪避系数"),		
	critPct	("暴击系数"),	
	toughPct("坚韧系数"), 
	speedPct("速度系数"),
	;
	
	private PropExtKey(String content) {
		this.content = content;
	}

	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public static boolean contains(PropExtKey key) {
		for(PropExtKey k : PropExtKey.values()) {
			if(k == key) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean contains(String key) {
		for(PropExtKey k : PropExtKey.values()) {
			if(k.toString().equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取枚举关键字List
	 * @return
	 */
	public static List<String> toList() {
		List<String> result = new ArrayList<>();
		for(PropExtKey k : PropExtKey.values()) {
			result.add(k.name());
		}
		return result;
	}
}
