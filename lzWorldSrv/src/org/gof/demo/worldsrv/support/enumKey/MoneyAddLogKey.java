package org.gof.demo.worldsrv.support.enumKey;

public enum MoneyAddLogKey {
	未设置(0),
	测试接口(1),
	充值(2),
	充值赠送(3),
	月卡返还(4),
	出售物品(5),
	副本(6),
	获取邮件附件(7),
	任务(8),
	礼包(12),
	每日登陆(13),
	购买体力(14),
	购买技能点(15),
	武将羁绊(16),
	山门进攻奖励(17),
	摇钱树(18),
	离线竞技(20),
	副本宝箱(22),
	每日任务(23),
	弟子炼金(24),
	弟子采集(25),
	宝藏抽奖(26),
	出售伙伴碎片(27),
	远征宝箱(28),
	爬塔(29),
	;
	
	private int type;
	
	private MoneyAddLogKey(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}