package org.gof.demo.battlesrv.ai.arpg;

import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevNodeControl;
import org.gof.demo.battlesrv.ai.bevTree.AIBevSelectorNode;
import org.gof.demo.battlesrv.ai.bevTree.AIBevSequenceNode;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfAI;

/**
 * 武将AI
 * 
 * @author GaoZhangCheng
 */
public class AIGen extends AI{

	public AIGen(UnitObject genObj, int aiSn) {
		this.unitObj = genObj;
		this.humanObj = genObj.getHumanObj();
		this.conf = ConfAI.get(aiSn);
		
		skillList.addAll(unitObj.skillOrder);
		timerPulse.start(unitObj.getTime(), PULSE_TIME);
		// 创建时间
		timeCreate = unitObj.getTime();
		behavior = AIBehaviorKey.NORMAL;
		initAIBev();
	}

	/**
	 * 自己添加一个行为树
	 */
	public void initAIBev() {
		if(null == aiBevRoot) {
			aiBevRoot = new AIBevSelectorNode();
			
			/* 向武将移动 */
			//距离到达一定距离停止移动
			AIBevNodeControl ai2 = new AIBevSequenceNode();
			//判断与主角的距离大于最大值
			AIBevCondDisTarObj leaf21 = new AIBevCondDisTarObj(this, conf.genMaxDis, humanObj, true, 500);
			ai2.addChild(leaf21);
			//向主角移动
			AIBevActMoveTarHum leaf22 = new AIBevActMoveTarHum(this, conf.genMaxDis, conf.genStaDis, 0.2);
			ai2.addChild(leaf22);
			//添加节点
			aiBevRoot.addChild(ai2);
			
			/* 选取仇恨值最高的敌人，直接开干 */
			//如果有攻击敌人
			AIBevNodeControl ai4 = new AIBevSequenceNode();
			//设置攻击状态
			AISetLeavAttack(ai4);
			//添加节点
			aiBevRoot.addChild(ai4);
//			
//			/* 附近一定距离有没有敌人 */
//			//如果有攻击
//			AIBevNodeControl ai6 = new AIBevSeqenceNode();
//			//附近一定距离有没有敌人
//			AIBevCondAreaEnemy leaf61 = new AIBevCondAreaEnemy(this, conf.genMaxDis);
//			ai6.addChild(leaf61);
//			//设置攻击状态
//			AISetLeavAttack(ai6);
//			//添加节点
//			aiBevRoot.addChild(ai6);
//			
//			/* 主角有没有攻击敌人 */
//			//如果有攻击
//			AIBevNodeControl ai8 = new AIBevSeqenceNode();
//			//将主角的敌人设置为自己的敌人
//			AIBevActActTarHum leaf81 = new AIBevActActTarHum(this);
//			ai8.addChild(leaf81);
//			//设置攻击状态
//			AISetLeavAttack(ai8);
//			//添加节点
//			aiBevRoot.addChild(ai8);
		}
	}
	
	/**
	 * 设置攻击状态
	 * @param aiNC
	 */
	private void AISetLeavAttack(AIBevNodeControl aiLeaf) {
		
		//判断敌人是否在可攻击范围内
		AIBevCondAreaEnemy leaf30 = new AIBevCondAreaEnemy(this, conf.radiusAlert);
		aiLeaf.addChild(leaf30);
		
		/* 判断和目标的距离，如果再范围内直接攻击，如果不在范围内，追击然后攻击 */
		AIBevNodeControl a42 = new AIBevSelectorNode();
		aiLeaf.addChild(a42);
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
//		AIBevActAjustMoveCollsion leaf49 = new AIBevActAjustMoveCollsion(this);
//		a43.addChild(leaf49);
		//向目标移动
		AIBevActMoveTarPos leaf491 = new AIBevActMoveTarPos(this);
		a43.addChild(leaf491);
		
		//选择技能
		AIBevActSelectSkill leaf492 = new AIBevActSelectSkill(this);
		a44.addChild(leaf492);
		//距离OK, 发射技能
		AIBevActCastSkill leaf46 = new AIBevActCastSkill(this);
		a44.addChild(leaf46);
		
	}
	/**
	 * 定时更新怪物的状态
	 */
	@Override
	public void pulse(long curr) {
		if (!timerPulse.isPeriod(curr))
			return;

		//执行行为树
		if(aiBevRoot != null && unitObj.isInWorld()) {
			aiBevRoot.execute(null);
		}
	}
}
