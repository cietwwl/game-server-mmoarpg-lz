package org.gof.demo.worldsrv.quest;

public interface QuestTypeKey {
	
	/* 任务组*/
	public static final int QUEST_主线 = 1;					//主线
	public static final int QUEST_支线 = 2;					//支线
	public static final int QUEST_日常 = 3;					//日常
	public static final int QUEST_活动 = 4;					//活动
	
	/* 普通任务类型*/
	public static final int QUEST_TYPE_1 = 1;				//任务类型1：提升玩家等级任务
	public static final int QUEST_TYPE_h2 = 2;				//任务类型2：指定数量的伙伴等级达到N级
	public static final int QUEST_TYPE_3 = 3;				//任务类型3：指定数量的伙伴品质达到要求品质
	public static final int QUEST_TYPE_4 = 4;				//任务类型4：伙伴数量
	public static final int QUEST_TYPE_5 = 5;				//任务类型5：指定个数的技能等级升到N级
	
	public static final int QUEST_TYPE_6 = 6;				//任务类型6：强化M件装备到N级
	public static final int QUEST_TYPE_7 = 7;				//任务类型7：冲星S段M件装备到N级
	public static final int QUEST_TYPE_8 = 8;				//任务类型8：镶嵌宝石M件到N级
	public static final int QUEST_TYPE_10 = 10;				//任务类型10：累计登陆奖励
	public static final int QUEST_TYPE_11 = 11;				//任务类型11：连续登陆奖励
	
	/* 每日任务类型*/
	public static final int QUEST_TYPE_101 = 101;				//任务类型101：强化任意装备N次
	public static final int QUEST_TYPE_102 = 102;				//任务类型102：冲星装备位N次
	public static final int QUEST_TYPE_103 = 103;				//任务类型103：镶嵌宝石N次
	public static final int QUEST_TYPE_104 = 104;				//任务类型104：使用伙伴经验丹
	public static final int QUEST_TYPE_105 = 105;				//任务类型105：金币抽奖
	public static final int QUEST_TYPE_106 = 106;				//任务类型106：商店购买
	public static final int QUEST_TYPE_107 = 107;				//任务类型107：升级角色技能N次
	
	/*普通任务和 */
	public static final int QUEST_TYPE_9 = 9;					//任务类型9：使用某个指定道具N次
	
	
	/** “副本”任务类型*/
	public static final int QUEST_INST_TYPE_1 = 1;				//任务类型1：血量判断类型（击杀BOSS）
	public static final int QUEST_INST_TYPE_2 = 2;				//任务类型2：全部击杀判断（击杀副本中的所有怪物）
	public static final int QUEST_INST_TYPE_3 = 3;				//任务类型3：触发指定某些机关（触发副本中所有机关）
	public static final int QUEST_INST_TYPE_4 = 4;				//任务类型4：生存
	public static final int QUEST_INST_TYPE_5 = 5;				//任务类型5：指定位置
	public static final int QUEST_INST_TYPE_6 = 6;				//任务类型6：NPC生存
	public static final int QUEST_INST_TYPE_7 = 7;				//任务类型7：怪物逃跑
	
	public static final int QUEST_MAIL_AWARD_TYPE_1 = 1;	//任务奖励发邮件，邮件中的物品类型

}
