package org.gof.demo.battlesrv.ai;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.support.TickTimer;
import org.gof.demo.battlesrv.ai.bevTree.AIBevNodeControl;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfAI;

public abstract class AI {
	public boolean DEBUG = false;
	public long PULSE_TIME = 135;
	public boolean active = false; //AI是否激活
	public long activeDelayTime = 0; //激活延迟时间
	public long activeMoveTime = 0;	//距离检测时间 
	
	public ConfAI conf;
	public UnitObject unitObj = null; // 所属单元
	public TickTimer timerPulse = new TickTimer(); // 刷新状态时间
	public long timeCreate; // ai创建时间
	public long timeInFight; // 最后战斗时间
	public List<Integer> skillList = new ArrayList<>(); // 条件技能队列
	
	
	public UnitObject humanObj;			// 主角
	public UnitObject targetObj; 			// 攻击目标
	public double targetObjDistance; 	// 攻击目标距离
	public UnitObject targetMoveObj; 	// 移动目标
	public Vector2D tarMovePos;  			// 现在正在移动的位置
	public Vector2D tarMoveLastPos; 	// 上次移动的时候的位置
	public Vector2D tarLastPos; 				// 上次移动的目标
	public AIMoveDirFromKey moveDirFrom = AIMoveDirFromKey.FromLeft; //默认遇到阻挡从左向右移动
	public AIBehaviorKey behavior;	
	
	public SkillCommon curSkill; //当前可以释放的技能
	
	protected AIBevNodeControl aiBevRoot;
	//************************************************************************************
	public int skillIndex = 0;
	public boolean skillOpenEnd = false; //开场技能体系
	public int skillOpenIndex = 0; //开场技能序号
    public float combatRange() {
    	SkillCommon tempSkill = curSkill;
    	if(tempSkill == null) {
    		tempSkill = this.unitObj.defaultSkill;
    	}
    		
    	if(tempSkill == null) {
    		return 99999;
    	}
    	return tempSkill.confSkill.range + unitObj.entryIndex * 0.5f;
    }
    public boolean bAutoCombat = true;  //是否自动释放大招
    public boolean canCastSpecialSkill = false;  //现在是否可以释放大招
	//************************************************************************************
	abstract public void pulse(long curr);
	
	/**
	 * 判断AI是否是激活状态
	 * @param curr 
	 * @return
	 */
	public boolean isActive(long curr) {
        if (unitObj.confModel.sn.equals("xuchu"))
        {
//        	Log.temp.info("isActive {} {} {}", active, activeDelayTime, unitObj.geDeltaTime());
        }
		if(active) {
			if(activeDelayTime<= 0) {
				return true;
			} else {
				activeDelayTime -= unitObj.geDeltaTime();
				if(activeDelayTime <= 0) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
	
	public void setActive(boolean boo) {
		this.active = boo;
	}
	
	/**
	 * 设置AI自动激活时间
	 * @param delay
	 */
	public void setActiveDelay(long delay) {
		this.active = true;
		activeDelayTime = delay;
	}
	
}
