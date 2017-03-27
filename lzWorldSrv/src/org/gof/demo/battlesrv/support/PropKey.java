package org.gof.demo.battlesrv.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Gaozhangcheng
 * 所有Pct属性都是原来属性的百分比加成，主要用于装备和天赋的百分比提升
 *
 */
public enum PropKey {
	
	/* 游戏暂定属性 */
	hpMax			("生命值上限"),
	hpMaxPct	("生命值上限百分比"),
	mpMax		("法力值上限"),
	mpMaxPct	("法力值上限百分比"),
	atkPhy			("阳属性攻击(物理攻击)"),
	atkPhyPct	("阳属性攻击(物理攻击)百分比"),
	defPhy			("阳属性防御(物理防御)"),
	defPhyPct	("阳属性防御(物理防御)百分比"),
	atkMag		("阴属性攻击(魔法攻击)"),
	atkMagPct	("阴属性攻击(魔法攻击)百分比"),
	defMag		("阴属性防御(魔法防御)"),
	defMagPct	("阴属性防御(魔法防御)百分比"),
	hit				("命中"),
	hitPct			("命中百分比"),
	dodge			("闪避"),
	dodgePct		("闪避百分比"),
	crit				("暴击概率(暴击)"),
	critPct			("暴击概率(暴击)百分比"),
	critAdd		("暴击比例(暴击加成)"),
	critAddPct	("暴击比例(暴击加成)百分比"),
	tough			("坚韧"),
	toughPct		("坚韧百分比"),
	
	elem1Atk			("元素1伤害"),
	elem1AtkPct	("元素1伤害百分比"),
	elem1Def			("元素1防御"),
	elem1DefPct	("元素1防御百分比"),
	elem2Atk			("元素2伤害"),
	elem2AtkPct	("元素2伤害百分比"),
	elem2Def			("元素2防御"),
	elem2DefPct	("元素2防御百分比"),
	elem3Atk			("元素3伤害"),
	elem3AtkPct	("元素3伤害百分比"),
	elem3Def			("元素3防御"),
	elem3DefPct	("元素3防御百分比"),
	elem4Atk			("元素4伤害"),
	elem4AtkPct	("元素4伤害百分比"),
	elem4Def			("元素4防御"),
	elem4DefPct	("元素4防御百分比"),
	
	suck					("吸血概率"),
	suckPct			("吸血概率百分比"),
	suckRatio			("吸血比率"),
	suckRatioPct	("吸血比率百分比"),
	avoidAtk			("免伤比例"),
	avoidAtkPct	("免伤比例百分比"),
	skillHealth		("治疗效果"),
	skillHealthPct	("治疗效果百分比"),
	speed				("移动速度"),
	speedPct			("移动速度百分比"),
	hpRecov			("生命恢复比例"),
	hpRecovPct		("生命恢复比例百分比"),
	mpRecov			("魔法恢复比例"),
	mpRecovPct	("魔法恢复比例百分比"),
	mpReduce		("能耗降低(魔法使用降低)"),
	mpReducePct	("能耗降低(魔法使用降低)百分比"),
	aggro				("仇恨值"),
	;
	
	private PropKey(String content) {
		this.content = content;
	}

	private String content;
	private static Set<String> keys = new HashSet<>();
	
	static {
		for(PropKey key : values()) {
			keys.add(key.name());
		}
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * 是否包含给定的Key
	 * @param key
	 * @return
	 */
	public static boolean contains(String key) {
		return keys.contains(key);
	}
	
	public static PropKey getEnumByType(String name) {
		for(PropKey k : values()) {
			if(k.name().equals(name))
				return k;
		}
		return null;
	}
	
	/**
	 * 获取枚举关键字List
	 * @return
	 */
	public static List<String> toList() {
		List<String> result = new ArrayList<>();
		for (PropKey k : PropKey.values()) {
			result.add(k.name());
		}
		return result;
	}
}
