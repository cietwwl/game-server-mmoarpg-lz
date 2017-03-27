package org.gof.demo.battlesrv.skill;

import org.gof.demo.worldsrv.config.ConfSkill;


/**
 * 技能的包装类，处理其他系统对技能基本属性的修改
 * 
 * 
 */
public class SkillVO {
	public final int sn;			//技能SN
	public final int snChange;			//技能可以跟换的SN
	public final boolean active;			//主动被动
	public final int type;			//0--普通技能， 1--绝招，2--杀招，3--普通攻击
	public final int targetType;			//目标类型
	public final int targetType2;			//技能触发点类型，ID同左边，仅在触发点与目标不同时填写（例如，需要在自己身上释放一个攻击单体目标的Dot）,填写此栏位后，右边（clickType)栏位必须为2
	public final int clickType;			//鼠标点选操作类型(1:角色， 2：坐标,3:自己
	public final boolean hit;			//技能是否必中
	public int atkType;			//攻击类型1是魔法2是物理
	public boolean canInterrupt;			//可以打断别人
	public boolean canInterrupted;			//可以被打断
	public final boolean isCharge;			//技能是否是蓄力技能
	public final int[] nextChainSn;			//连击的后续技能 如拍拍熊的5连击 在第1个技能写后面4个
	public final int nextChainID;			//连续技下一招的ID
	public float range;			//实际射程（m)
	public final float rangeMin;			//最小射程m
	public final float rangeMax;			//最大射程m
	public final int blackTime;			//黑屏时间
	public final int[] shakeFront;			//前摇时间
	public final int[] casting;			//激发时间
	public final int[] shakeBack;			//后摇时间
	public int[] coolTime;			//能冷时间
	public final int[] comboTime;			//连击时间
	public final int combo;			//连击次数 为0 或者不填写表示此技能无连击
	public final int rageAttack;			//攻击获得的怒气
	public final int rageKill;			//杀死对方获得的怒气
	public final int rageBeAttack;			//被攻击获得的怒气
	public final boolean enterScene;			//开场技能
	public final boolean canMove;			//可移动
	public final int backDis;			//后退距离cm单位
	public final int[] effects;			//效果总汇
	public final int combat;			//战斗力
	public final int baseCost;			//技能升级基础花费
	public final int plusCost;			//每级增加花费
	public final int activeType;			//激活类型
	public final int position;			//技能在前端的位置
	public final int levelCut;			//与武将级别差值
	public final String[] relGeneralSn;			//羁绊技能激活需要的伙伴SN
	public final int levelLimit;			//等级限制
	
	public SkillVO(ConfSkill conf){
		
		this.sn = conf.sn;		
		this.snChange = conf.snChange;		
		this.active = conf.active;		
		this.type = conf.type;		
		this.targetType = conf.targetType;		
		this.targetType2 = conf.targetType2;		
		this.clickType = conf.clickType;		
		this.hit = conf.hit;		
		this.atkType = conf.atkType;		
		this.canInterrupt = conf.canInterrupt;		
		this.canInterrupted = conf.canInterrupted;		
		this.isCharge = conf.isCharge;		
		this.nextChainSn = conf.nextChainSn;		
		this.nextChainID = conf.nextChainID;		
		this.range = conf.range;		
		this.rangeMin = conf.rangeMin;		
		this.rangeMax = conf.rangeMax;		
		this.blackTime = conf.blackTime;		
		this.shakeFront = conf.shakeFront;		
		this.casting = conf.casting;		
		this.shakeBack = conf.shakeBack;		
		//this.coolTime = conf.coolTime;
		this.coolTime = new int[conf.coolTime.length];
		for(int i = 0 ;i<conf.coolTime.length ;i++){
			this.coolTime[i]=conf.coolTime[i];
		}
		this.comboTime = conf.comboTime;		
		this.combo = conf.combo;		
		this.rageAttack = conf.rageAttack;		
		this.rageKill = conf.rageKill;		
		this.rageBeAttack = conf.rageBeAttack;		
		this.enterScene = conf.enterScene;		
		this.canMove = conf.canMove;		
		this.backDis = conf.backDis;		
		this.effects = conf.effects;		
		this.combat = conf.combat;		
		this.baseCost = conf.baseCost;		
		this.plusCost = conf.plusCost;		
		this.activeType = conf.activeType;		
		this.position = conf.position;		
		this.levelCut = conf.levelCut;		
		this.relGeneralSn = conf.relGeneralSn;		
		this.levelLimit = conf.levelLimit;
		
	}
}
