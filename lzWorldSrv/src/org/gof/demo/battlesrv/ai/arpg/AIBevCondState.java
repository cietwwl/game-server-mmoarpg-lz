package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;

/**
 * AI：判断状态，状态标识如下：
 * 
 *		 	INACTIVE,		//未激活，只是被创建，还不能活动
 *			FREEZE,		//冻结，什么都不做
 *			NORMAL,		//普通状态，一般在巡逻
 *			SEEK,			//搜寻敌人
 *			FIGHT,			//战斗
 *			ATTACKED,	//被攻击
 *			ESCAPE,		//逃跑
 *			BACK,			//回巢
 *			DIE,				//死亡
 * 
 * @author GaoZhangCheng
 */
public class AIBevCondState extends AIBevLeaf{

	AIBehaviorKey key = AIBehaviorKey.NORMAL;
	public AIBevCondState(AI ai, AIBehaviorKey key, boolean logic) {
		this.ai = ai;
		nonLogic = logic;
		this.key = key;
	}
	public AIBevCondState(AI ai, AIBehaviorKey key) {
		this.ai = ai;
		this.key = key;
	}
	@Override
	public boolean execute(Param param) {
		boolean result = false;
		
		if(ai.behavior == key) {
			return true;
		}
		
		return result;
	}

}
