package org.gof.demo.battlesrv.ai;

/**
 * 怪物AI的一些状态
 */
public enum AIBehaviorKey {
	INACTIVE,			//未激活，只是被创建，还不能活动
	FREEZE,				//冻结，什么都不做
	NORMAL,				//普通状态，一般在巡逻
	SEEK,					//搜寻敌人
	FIGHT,					//战斗
	ATTACKED,			//被攻击
	ESCAPE,				//逃跑
	BACK,					//回巢
	DIE,						//死亡
	;
}
