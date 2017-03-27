package org.gof.demo.worldsrv.support.enumKey;

public enum MoneyReduceLogKey {
	未设置(0),
	测试接口(1),
	剧情副本(2),
	武将忠诚度(3),
	伙伴升星(4),
	商城(5),
	改名(6),
	购买体力(7),
	装备合成(8),
	购买技能(9),
	神秘商店(10),
	技能升级(11),
	摇钱树(12),
	离线竞技场挑战次数增加(13),
	离线竞技场清除冷却CD(14),
	副本命魂(15),
	爬塔扫荡(16),
	离线竞技场商店刷新(17),
	剑庐山门升级(18),
	剑庐山门雇佣(19),
	经络升级(20),
	激活羁绊(21),
	山门搜索(22),
	抽奖(23),
	弟子房升级(24),
	
	伙伴招募(25),
	伙伴升品质(26),
	伙伴装备升品质(27),

	扩包(28),
	装备位强化(29),
	装备位冲星(30),
	远征BUFF刷新(31),
	;
	
	private int type;
	
	private MoneyReduceLogKey(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}