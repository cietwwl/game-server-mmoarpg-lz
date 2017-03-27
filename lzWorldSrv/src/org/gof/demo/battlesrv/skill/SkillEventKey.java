package org.gof.demo.battlesrv.skill;

/**
 * 被动技能触发点枚举
 * @author new
 *
 */
public enum SkillEventKey {
	EVENT_ON_SKILL_BEFORE,			//技能释放前只会有1次
	EVENT_ON_BEFORE_CALC_HURT,		//计算伤害前
	EVENT_ON_BEFORE_HPLOST,			//扣血前
	EVENT_ON_AFT_HPLOST,			//扣血后
	EVENT_ON_TAR_EFFECT_END,		//技能施放对目标效果结束后（攻防双方都有数据）
	EVENT_ON_SKILL_END,				//技能释放结束后(此事件只有攻击方数据)
	EVENT_ON_SKILL_INTERRUPT,		//技能被打断的时候
	EVENT_ON_UNIT_DYING,			//单位即将死亡时(主要用于重生效果。此事件只有单位自己的数据)	
}
