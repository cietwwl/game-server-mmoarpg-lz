package org.gof.demo.battlesrv.ai.arpg;

import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.AIBehaviorKey;
import org.gof.demo.battlesrv.ai.bevTree.AIBevNodeControl;
import org.gof.demo.battlesrv.ai.bevTree.AIBevSelectorNode;
import org.gof.demo.battlesrv.ai.bevTree.AIBevSequenceNode;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.config.ConfAI;
import org.gof.demo.worldsrv.config.ConfCharacterHuman;

/**
 * 玩家竞技场AI
 * @author lixiaofang
 *
 */
public class HumanCompeteAI extends AI{
	
	public HumanCompeteAI(UnitObject selfObj, UnitObject enemyObj) {
		this.unitObj = selfObj;
		this.targetObj = enemyObj;
		ConfCharacterHuman confHuman = ConfCharacterHuman.getBy("modelSn",this.unitObj.modelSn);
		this.conf = ConfAI.get(confHuman.ai);
		
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
			AIBevCondDisTarObj leaf44 = new AIBevCondDisTarObj(this, this.conf.fightDis + 0.1, this.unitObj, false, 20);
			a43.addChild(leaf44);
			//把目标单位转换为坐标
			AIBevActTarObjToPos leaf48 = new AIBevActTarObjToPos(this, this.conf.fightDis);
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
	}
	

	/**
	 * 定时更主角的状态
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
