package org.gof.demo.battlesrv.skill;

import org.gof.demo.worldsrv.config.ConfSkillEffect;

/**
 * 技能效果的包装类，处理其他系统对技能效果基本属性的修改
 * 
 * @author GaoZhangCheng
 */
public class SkillEffectVO {

	public final int sn;					//sn
	public final int effectSn;			//effectSn
	public final int level;				//等级
	public final int period;				//两段式的哪段（0,1）填0的代表发招立刻后端发送给前端的逻辑，伤害基本都在这；填1的表示等待施法时间之后再发送，一般用于buff和dot
	public final int[] comboCount;			//三连击，连点对应的第几次连击（0,1,2…）
	public final double triggerPct;			//触发几率
	public int scopeType;			//目标范围类型
	public float scopeParam1;			//范围参数1(半径/宽度（矩形）)
	public int scopeParam2;			//范围参数2（角度/高度（矩形）） 如果是10选择1职业1 2职业2 4职业3 3职业12 5职业13 6职业23 7职业123
	public int scopeParam3;			//
	public int targetNum;			//范围搜索的目标个数
	public final int targetFriend;			//技能友好度，=0为中性技能，>0为正面技能，<0为 负面技能
	public final boolean targetSelf;			//目标是否有自己 单体对自己的技能 targetSelf 为true ,如果为false那么忽略
	public final boolean excludeTeamBundle;			//是否排除自己武将小弟 为true删除自己的全部人， 为false 删除敌人的全部人
	public final boolean attackMove;			//是否攻击追击
	public float effectDis;			//移动距离
	public final float impulse;			//技能冲量
	public final int levelParam;			//升级参数，如果伤害 那么是技能等级*参数， 如果buff 为概率， 大于等于对方等级的发生的概率
	public int logicSn;			//调用的逻辑库ID
	public String param1;			//参数1
	public String param2;			//参数2 对于Buff 来说是产生概率
	public String param3;			//参数3
	public String param4;			//参数4
	public String param5;			//参数5
	public String param6;			//参数6
	public String param7;			//参数7
	public String param8;			//参数8
	
	public SkillEffectVO(ConfSkillEffect conf){
		this.sn = conf.sn;		
		this.effectSn = conf.effectSn;		
		this.level = conf.level;		
		this.period = conf.period;		
		this.comboCount = conf.comboCount;		
		this.triggerPct = conf.triggerPct;		
		this.scopeType = conf.scopeType;		
		this.scopeParam1 = conf.scopeParam1;		
		this.scopeParam2 = conf.scopeParam2;		
		this.scopeParam3 = conf.scopeParam3;		
		this.targetNum = conf.targetNum;		
		this.targetFriend = conf.targetFriend;		
		this.targetSelf = conf.targetSelf;		
		this.excludeTeamBundle = conf.excludeTeamBundle;		
		this.attackMove = conf.attackMove;		
		this.effectDis = conf.effectDis;		
		this.impulse = conf.impulse;		
		this.levelParam = conf.levelParam;		
		this.logicSn = conf.logicSn;		
		this.param1 = conf.param1;		
		this.param2 = conf.param2;		
		this.param3 = conf.param3;		
		this.param4 = conf.param4;		
		this.param5 = conf.param5;		
		this.param6 = conf.param6;		
		this.param7 = conf.param7;		
		this.param8 = conf.param8;		
	}
}
