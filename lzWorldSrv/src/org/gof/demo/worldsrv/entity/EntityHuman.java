package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="Human", tableName="demo_human", superEntity=EntityUnit.class)
public enum EntityHuman {
	@Column(type=int.class, comment="服务器编号")
	serverId,
	@Column(type=String.class, comment="账号", index=true)
	account,
	@Column(type=int.class, comment="在线时间")
	timeSecOnline,
	@Column(type=long.class, comment="最后一次登录时间")
	timeLogin,
	@Column(type=long.class, comment="最后一次登出时间")
	timeLogout,
	@Column(type=long.class, comment="角色创建时间")
	timeCreate,
	@Column(type=long.class, comment="角色SessionKey", index = true)
	sessionKey,
	@Column(type=String.class, comment="游戏设置", length=5120)
	gameOptions,
	@Column(type=String.class, comment="玩家挂机设置", length=1024)
	fightAutoOption,
	@Column(type = String.class, comment = "不同玩法的阵容[{t,r},{}...]", length=1024, defaults = "[]")
	fightGeneralJSON,
	@Column(type=int.class, comment="VIP等级", defaults="0")
	vipLevel,
	
	/* 货币 */
	@Column(type=long.class, comment="元宝")
	gold,
	@Column(type=long.class, comment="铜币")
	coin,
	@Column(type=long.class, comment="活力")
	actValue,
	@Column(type=long.class, comment="演武币")
	competMoney,
	
	/* 地图 */
	@Column(type=String.class, comment="地图位置信息，{{id, sn ,x ,y ,common},{}}")
	stageHistory,
	
	//副本相关
	@Column(type=long.class, comment="活力最大值")
	actValueMax,
	@Column(type=int.class, comment="现在命魂的层次")
	minghunIndex,
	
	/*商店相关*/
	@Column(type = String.class, comment = "商店相关信息", defaults = "[]", length=2048)
	shopJson,
	
	/* 武将羁绊相关 */
	@Column(type = String.class, comment = "玩家招募过的所有武将", defaults = "")
	allGeneral,
	
	/* 背包额外信息 */
	@Column(type = int.class, comment = "主背包容量")
	bag1Cap,
	@Column(type = int.class, comment = "次背包容量")
	bag2Cap,
	
	/* 物品使用次数限制 */
	@Column(type = String.class, comment = "物品每日使用限制次数", defaults = "")
	propUseLimitCounts,
	
	/* 抽奖相关信息 */
	@Column(type = String.class, comment = "抽奖次数记录" , defaults = "")
	trCounts,
	@Column(type = String.class, comment = "免费抽奖时间记录", defaults = "")
	freeTrTime,
	@Column(type = String.class, comment = "高级宝藏三个物件SN", defaults = "[]")
	tr3ItemSn,
	
	/* 任务相关信息 */
	@Column(type=String.class, comment="已完成任务", defaults = "[]" , length=1024)
	questIdCompletedJSON,
	@Column(type=String.class, comment="成长任务", defaults = "[]" , length=2048)
	questNormalJSON,
	@Column(type=String.class, comment="日常任务", defaults = "[]" , length=1024)
	questDailyJSON,
	@Column(type=String.class, comment="副本任务", defaults = "[]" , length=512)
	questInstDailyJSON,
	@Column(type=int.class, comment="副本任务刷新剩余次数")
	questInstCount,
	
	/* 伙伴碎片信息*/
	@Column(type=String.class, comment="伙伴碎片  {碎片sn：数量}", defaults = "{}")
	fragInfo,
	/*伙伴副本挑战次数     {副本类型：当日挑战次数}*/
	@Column(type=String.class, comment="伙伴副本挑战次数{副本类型：当日挑战次数}", defaults = "{}")
	genTaskFightTimes,
	/* 每日活跃度*/
	@Column(type=String.class, comment="每日活跃度", defaults = "[]" , length=512)
	liveness,
	/* 每日活跃度*/
	@Column(type=String.class, comment="已经领取的每日活跃度奖励", defaults = "[]" , length=512)
	livenessAwardsRsvd,
	
	/* 竞技场相关 */
	@Column(type=int.class, comment="竞技场剩余次数")
	competeCount,
	@Column(type=int.class, comment="竞技场刷新次数")
	competeRefreshCount,
	@Column(type = long.class, comment="上次竞技时间") 
	competeLastTime,
	;
}