package org.gof.demo.battlesrv.ai.arpg;

import java.util.HashSet;
import java.util.Set;

import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.ai.bevTree.AIBevNodeControl;
import org.gof.demo.battlesrv.ai.bevTree.AIBevSelectorNode;
import org.gof.demo.battlesrv.ai.bevTree.AIBevSequenceNode;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfAI;

/**
 * ARPG 怪物主AI入口
 * 
 * @author GaoZhangCheng
 */
public class AIRpg extends AI{

	public AIRpg(UnitObject unitObject , int aiSn) {
		this.unitObj = unitObject;
		this.conf = ConfAI.get(aiSn);
		
		//添加所有的技能
		Set<Integer> skillSet = new HashSet<>();
		for (Integer i : conf.attack) {
			skillSet.add(i);
		}
		for (Integer i : conf.triggerSkill) {
			skillSet.add(i);
		}
		skillList.addAll(skillSet); 
		
		// 创建时间
		timerPulse.start(unitObj.getTime(), PULSE_TIME);
		timeCreate = unitObj.getTime();
		behavior = AIBehaviorKey.NORMAL;
		initAIBev();

	}

	/**
	 * 自己添加一个行为树
	 */
	public void initAIBev() {
		timerPulse.setTimeNext(unitObj.getTime() + 1000);
		if(null == aiBevRoot && conf != null) {
			aiBevRoot = new AIBevSelectorNode();
			//死亡复活
			//回巢
			//攻击
			//巡逻
			//发呆
			
			/* 死亡复活：任意节点返回false跳过，返回true执行 */
			if(conf.timeRefresh > 0) {
				//添加顺序节点
				AIBevNodeControl ai1 = new AIBevSequenceNode();
				//添加死亡判断
				AIBevLeaf leaf11 = new AIBevConditionDie(this);
				ai1.addChild(leaf11);
				//添加延迟
				AIBevActTrigger leaf12 = new AIBevActTrigger(this, conf.timeRefresh);
				ai1.addChild(leaf12);
				//添加复活行为
				AIBevLeaf leaf13 = new AIBevActRevive(this);
				ai1.addChild(leaf13);
				
				aiBevRoot.addChild(ai1);
			}
			
			/* 如果超出了出生点很远的距离那么回到出生地啊 */
			AIBevNodeControl ai2 = new AIBevSequenceNode();
			//是否触发回巢
			AIBevConditionDisBirth leaf21 = new AIBevConditionDisBirth(this, conf.radiusChase);
			ai2.addChild(leaf21);
			//启动回巢状态
			AIBevActionToState leaf25 = new AIBevActionToState(this, AIBehaviorKey.BACK);
			ai2.addChild(leaf25);
			//移动
			AIBevActMoveTarPos leaf22 = new AIBevActMoveTarPos(this);
			ai2.addChild(leaf22);
			
			//选择节点 如果达到1米范围内 执行状态装换
			AIBevNodeControl ai22 = new AIBevSelectorNode();
			ai2.addChild(ai22);
			//判断是否离目标点很近了
			AIBevCondDisTarPos leaf23 = new AIBevCondDisTarPos(this, 1, true);
			ai22.addChild(leaf23);
			//回到一般状态
			AIBevActionToState leaf24 = new AIBevActionToState(this, AIBehaviorKey.NORMAL);
			ai22.addChild(leaf24);
			aiBevRoot.addChild(ai2);
			
			/* 判断敌人是否在可攻击范围内 攻击敌人 */
			if(conf.isCounterattackAuto) {
				AIBevNodeControl ai4 = new AIBevSequenceNode();
				//判断敌人是否在可攻击范围内
				AIBevCondArggoEnemy leaf40 = new AIBevCondArggoEnemy(this, conf.radiusAlert);
				ai4.addChild(leaf40);
				//如果是被攻击，切换状态
				AIBevCondState leaf41 = new AIBevCondState(this, AIBehaviorKey.ATTACKED);
				ai4.addChild(leaf41);
				
				/* 判断和目标的距离，如果再范围内直接攻击，如果不在范围内，追击然后攻击 */
				AIBevNodeControl a42 = new AIBevSelectorNode();
				ai4.addChild(a42);
				AIBevNodeControl a43 = new AIBevSequenceNode();
				a42.addChild(a43);
				AIBevNodeControl a44 = new AIBevSequenceNode();
				a42.addChild(a44);
				//判断自己和目标的距离是否大于DIS
				AIBevCondDisTarObj leaf44 = new AIBevCondDisTarObj(this, conf.fightDis + 0.1);
				a43.addChild(leaf44);
				//把目标单位转换为坐标
				AIBevActTarObjToPos leaf48 = new AIBevActTarObjToPos(this, conf.fightDis);
				a43.addChild(leaf48);
				//根据阻挡动态调整目标
				AIBevActAjustMoveCollsion leaf49 = new AIBevActAjustMoveCollsion(this);
				a43.addChild(leaf49);
				//向目标移动
				AIBevActMoveTarPos leaf491 = new AIBevActMoveTarPos(this);
				a43.addChild(leaf491);
				
				//选择技能
				AIBevActSelectSkill leaf492 = new AIBevActSelectSkill(this);
				a44.addChild(leaf492);
				//距离OK, 发射技能
				AIBevActCastSkill leaf46 = new AIBevActCastSkill(this);
				a44.addChild(leaf46);
				
				aiBevRoot.addChild(ai4);
			}
			
			/* 如果没有被攻击，没有敌人在范围内，那么巡逻 */
			if(true) {
				//如果没有被攻击那么巡逻玩
				AIBevNodeControl ai3 = new AIBevSequenceNode();
				//等一个随机秒数 改变tick的频率
//				AIBevActChangeTick leaf31 = new AIBevActChangeTick(this);
//				ai3.addChild(leaf31);
				
				AIBevActTrigger leaf31 = new AIBevActTrigger(this);
				ai3.addChild(leaf31);
				//找到一个随机点
				AIBevActGetRandPos leaf32 = new AIBevActGetRandPos(this, conf.radiusMove);
				ai3.addChild(leaf32);
				//然后开始移动
				AIBevActMoveTarPos leaf33 = new AIBevActMoveTarPos(this);
				ai3.addChild(leaf33);
				
				aiBevRoot.addChild(ai3);
			}
		}
	}
	/**
	 * 定时更新怪物的状态
	 */
	@Override
	public void pulse(long curr) {
		if (!timerPulse.isPeriod(curr))
			return;

//		Log.stageCommon.info("pulsepulsepulsepulse: {}", unitObj.name);
		//执行行为树
		aiBevRoot.execute(null);
	}
}
