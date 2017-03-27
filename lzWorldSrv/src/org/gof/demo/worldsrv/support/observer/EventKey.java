package org.gof.demo.worldsrv.support.observer;

public final class EventKey {
	public static final int GAME_STARTUP_BEFORE = 0x1001;			//游戏启动前
	public static final int GAME_STARTUP_FINISH = 0x1002;				//游戏启动完毕
	
	public static final int STAGE_REGISTER = 0x2001;						//地图被注册
	public static final int STAGE_CANCEL = 0x2002;							//地图被注销
	public static final int STAGE_INTANCE_START = 0x2003;				//地图副本启动
	
	public static final int HUMAN_UPGRADE = 0x3001;							//用户升级，每次升级抛出一次，传递开始和结束等级
	public static final int HUMAN_BE_KILLED = 0x3002;						//用户死亡
	public static final int HUMAN_CHANGE_NAME = 0x3003;					//用户改名
	public static final int UPDATE_QUEST = 0x3004;     					// 用户升级，更新任务列表
	
	public static final int OPEN_TEMP_SHOP = 0x3005;					//打开临时商店
	
	public static final int HUMAN_CREATE = 0x4001;							//创建角色
	public static final int HUMAN_LOGIN = 0x4002;								//玩家登录
	public static final int HUMAN_LOGIN_FINISH = 0x4003;					//玩家登录结束，可以开始接收消息。
	public static final int HUMAN_LOGIN_FINISH_FIRST_TODAY = 0x4004;		//玩家今日首次登录结束，可以开始接收消息。
	public static final int HUMAN_LOGOUT = 0x4005;							//玩家登出游戏
	public static final int GENERAL_CREATE = 0x4006;							//创建武将
	public static final int GENERAL_UPGRADE  = 0x4007;						//武将升级
	public static final int GENERAL_STAR_LEVELUP  = 0x4008;				//武将升星
	public static final int GENERAL_QUILITY_LEVELUP = 0x4009;			//武将品质升级
	public static final int GENERAL_BORN = 0x4010;							//武将出生
	public static final int HUMAN_BUY_MONTH_CARD = 0x4021;			//玩家购买月卡
	public static final int HUMAN_FIRST_LOGIN = 0x4022;						//玩家这辈子首次登陆
	public static final int HUMAN_PRESTIGE_CHANGE = 0x4023;				//玩家威望值发生变化
	public static final int HUMAN_HOME_BUILD_LEVELUP = 0x4024;		//玩家剑庐某个建筑升级
	public static final int PROP_ADD_EXP = 0x4025;		//玩家使用任意伙伴经验丹
	public static final int BUG_GOODS = 0x4026;		//购买商品
	public static final int LOGIN_AWARD = 0x4027;		//登陆奖励
	
	public static final int HUMAN_RESET_ZERO = 0x5001;					//玩家进行零时所需清理
	public static final int HUMAN_RESET_FIVE = 0x5002;						//玩家进行五时所需清理
	public static final int HUMAN_RESET_12ST = 0x5003;					//玩家进行12点处理
	public static final int HUMAN_RESET_18ST = 0x5004;					//玩家进行18点处理
	public static final int HUMAN_RESET_21ST = 0x5005;					//玩家进行21点处理
	public static final int HUMAN_RESET_6MIN = 0x5006;					//玩家每6分钟恢复一点活力
	
	public static final int HUMAN_STAGE_ENTER_BEFORE = 0x6001;	//玩家进入地图之前一点点（切换地图时会触发）
	public static final int HUMAN_STAGE_ENTER = 0x6002;					//玩家进入地图（切换地图时会触发）
	public static final int HUMAN_STAGE_REGISTER = 0x6003;				//玩家注册到场景中的时候
	
	public static final int HUMAN_DATA_MAIN_LOAD_BEGIN = 0x7001;		//用户玩家主要数据加载开始
	public static final int HUMAN_DATA_LOAD_BEGIN = 0x7002;					//用户玩家数据加载开始
	public static final int HUMAN_DATA_LOAD_BEGIN_ONE = 0x7003;		//用户玩家数据加载开始一个
	public static final int HUMAN_DATA_LOAD_FINISH_ONE = 0x7004;		//用户玩家数据加载完成一个
	
	public static final int HUMAN_PRODUCE_PROP_CHANGE = 0x8001;					//玩家produce属性变化
	public static final int HUMAN_PRODUCE_PROP_ADD = 0x8002;						//玩家produce属性增加
	public static final int HUMAN_PRODUCE_PROP_REDUCE = 0x8003;					//玩家produce属性减少
	
	public static final int HUMAN_PROP_ = 0x8003;					//玩家属性计算
	
	public static final int UNIT_MOVE_START = 0x9001;					//可移动单元每次开始移动
	public static final int UNIT_HPLOSS = 0x9002;								//战斗单元受到伤害
	public static final int UNIT_ATTACK = 0x9003;								//战斗单元攻击
	public static final int UNIT_ACT = 0x9004;									//地图单元有动作，移动，攻击，施法等
	public static final int UNIT_BE_ATTACKED = 0x9005;					//战斗单元受攻击
	public static final int UNIT_BE_KILLED = 0x9006;							//战斗单元死亡
	public static final int UNIT_DO_SKILL = 0x9007;							//释放技能
	
	public static final int MONSTER_MOVE_START = 0xA001;					//怪物每次开始移动
	public static final int MONSTER_HPLOSS = 0xA002;						//怪物受到伤害
	public static final int MONSTER_HPLOSS_BY_NO_HUMAN = 0xA003;			//怪物受到伤害 = 100;攻击者为null
	public static final int MONSTER_ATTACK = 0xA004;						//怪物攻击
	public static final int MONSTER_BE_KILLED_BEFORE = 0xA005;		//怪物被击杀前一刻
	public static final int MONSTER_BE_KILLED = 0xA006;					//怪物被击杀
	public static final int MONSTER_BE_ATTACKED = 0xA007;				//怪物被攻击
	public static final int MONSTER_ACT = 0xA008;						//怪物有动作，移动，攻击，施法等
	public static final int MONSTER_EAT = 0xA009;						//怪物吞噬了其他怪
	public static final int MONSTER_BORN = 0xA00A;						//怪物出生
	public static final int MONSTER_SKILL_CAST = 0xA00B;					//怪物施放技能
	public static final int MONSTER_CLEAN = 0xA00C;					//怪物施放技能
	
	public static final int GENERAL_BE_ATTACKED = 0xB001;				//武将被攻击
	public static final int HUMAN_MOVE_START_BEFORE = 0xB002;			//玩家每次开始移动 之前抛出
	public static final int HUMAN_MOVE_START = 0xB003;					//玩家每次开始移动
	public static final int HUMAN_HPLOSS = 0xB004;						//玩家受到伤害
	public static final int HUMAN_ATTACK = 0xB005;						//玩家攻击
	public static final int HUMAN_ACTS_BEFORE = 0xB006;					//玩家有动作，移动，攻击，施法等 之前抛出
	public static final int HUMAN_ACT = 0xB007;							//玩家有动作，移动，攻击，施法等
	public static final int HUMAN_BE_ATTACKED = 0xB008;					//玩家受攻击
	public static final int HUMAN_MOVE_FINISH = 0xB009;					// 玩家停止移动
	public static final int HUMAN_REVIVE = 0xB00A;						//玩家复活
	
	public static final int ITEM_CHANGE = 0xC001;					//物品变动
	public static final int ITEM_CHANGE_ADD = 0xC002;				//物品增加
	public static final int ITEM_CHANGE_DEL = 0xC003;				//物品删除
	public static final int ITEM_CHANGE_MOD = 0xC004;				//物品修改
	public static final int ITEM_INIT = 0xC005;						//物品创建
	public static final int ITEM_USE = 0xC008;						//物品使用
	public static final int ITEM_BE_USED_SUCCESS = 0xC009;			//物品使用成功
	public static final int ITEM_BAG_ARRANGE = 0xC00A;				//整理背包
	public static final int ITEM_BAG_EXPAND = 0xC00B;				//扩背包
	public static final int ITEM_BAG_SELL = 0xC00C;					//物品出售
	
	public static final int POCKET_LINE_HANDLE = 0xD001;				//按模块发送待办事件
	public static final int POCKET_LINE_HANDLE_ONE = 0xD002;			//用户待办事件 = 100;逐条发送
	public static final int POCKET_LINE_HANDLE_END = 0xD003;			//待办事件处理结束 
	
	public static final int SKILL_PASSIVE_CHECK = 0xE001;			//被动技能检查
	public static final int SKILL_UPGRADE = 0xE002;					//技能升级
	
	public static final int INSTANCE_PASS = 0xF001;					//副本通关
	public static final int GENERAL_RELATION_PASS = 0xF002;	//将星录通关
	public static final int COMPETE_OFFLINE_PASS = 0xF003;		//竞技场通过
	public static final int TOWER_PASS = 0xF004;				//爬塔通过当前层
	public static final int TOWER_ENTER = 0xF005;				//爬塔进入
	public static final int INSTANCE_AUTO_PASS = 0xF006;		//副本扫荡
	public static final int HOME_GUART_SET_TEAM = 0xF007;	 //玩家家园设置防守阵型
	public static final int HOME_GUART_SEARTCH_ATTACK =	 0xF008;	//家园攻击
	
	public static final int HUMAN_COMBAT_CHANGE = 0x10001;				//玩家战斗力变化\
	
	public static final int COMPETE_OFFLINE_RANK_CHANGE = 0x11001;		// 离线竞技排行变化
	public static final int COMPETE_RESET = 0x11002;						// 竞技场整点结算
	
	public static final int USUAL_PROMPT_TASK_INIT = 0x12001;				//待做事项初始化
	public static final int USUAL_PROMPT_TASK_CHANGE = 0x12002;		//待做事项改变
	
	public static final int LOTTERY_SELECT = 0x13001;			//抽卡
	public static final int MONEY_TREE_SHARK = 0x13002;	//摇钱树

	public static final int BODY_ITEM_CHANGE = 0x14001;					//穿戴物品变化
	public static final int BODY_PART_QIANGHUA = 0x14002;				//装备位强化
	public static final int BODY_PART_CHONGXING = 0x14003;				//装备位冲星
	public static final int BODY_GEM_CHANGE = 0x14004;					//宝石变动
	public static final int BODY_GEM_COMPOSITE = 0x14005;				//宝石合成
	
	public static final int DAILY_QUEST_PASS = 0x14006; //每日任务完成
	public static final int UPDATE_LIVENESS = 0x14007; //每日任务完成
	public static final int MALL_BUG_GOODS_BY_GOLD = 0x14008;		//在商城成功购买商品
	public static final int LOAD_TOWER_DATA = 0x14009;                    //加载爬塔数据
	
	//开始场景触发器定义
	public static final int SCENE_TRIGGER_01 = 0x15001;		//移动
	public static final int SCENE_TRIGGER_02 = 0x15002;		//怪物剩余血量
	public static final int SCENE_TRIGGER_03 = 0x15003;		//怪物死亡
	public static final int SCENE_TRIGGER_04 = 0x15004;		//释放技能触发
	public static final int SCENE_TRIGGER_05 = 0x15005;		//敌对目标剩余数量
	public static final int SCENE_TRIGGER_06 = 0x15006;		//副本倒计时
	public static final int SCENE_TRIGGER_07 = 0x15007;		//进入副本
	public static final int SCENE_TRIGGER_08 = 0x15008;		//完成事件
	//开始场景事件定义
	public static final int SCENE_EVENT_01 = 0x15021;		//情景对话
	public static final int SCENE_EVENT_02 = 0x15022;		//QTE操作
	public static final int SCENE_EVENT_03 = 0x15023;		//播放多媒体
	public static final int SCENE_EVENT_04 = 0x15024;		//改变镜头
	public static final int SCENE_EVENT_05 = 0x15025;		//刷怪
	public static final int SCENE_EVENT_06 = 0x15026;		//技能
	public static final int SCENE_EVENT_07 = 0x15027;		//机关
	public static final int SCENE_EVENT_08 = 0x15028;		//玩家移动
	public static final int SCENE_EVENT_09 = 0x15029;		//道具
	public static final int SCENE_EVENT_10 = 0x15030;		//警告/通知
	public static final int SCENE_EVENT_11 = 0x15031;		//挑战结果
	

}