package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="Unit", isSuper=true)
public enum EntityUnit {
	
	/* 战斗属性 */
	@Column(type=int.class, comment="生命值上限")
	hpMax,
	@Column(type=int.class, comment="生命值上限百分比")
	hpMaxPct,
	@Column(type=int.class, comment="法力值上限")
	mpMax,
	@Column(type=int.class, comment="法力值上限百分比")
	mpMaxPct,
	@Column(type=int.class, comment="阳属性攻击(物理攻击)")
	atkPhy,
	@Column(type=int.class, comment="阳属性攻击(物理攻击)百分比")
	atkPhyPct,
	@Column(type=int.class, comment="阳属性防御(物理防御)")
	defPhy,
	@Column(type=int.class, comment="阳属性防御(物理防御)百分比")
	defPhyPct,
	@Column(type=int.class, comment="阴属性攻击(魔法攻击)")
	atkMag,
	@Column(type=int.class, comment="阴属性攻击(魔法攻击)百分比")
	atkMagPct,
	@Column(type=int.class, comment="阴属性防御(魔法防御)")
	defMag,
	@Column(type=int.class, comment="阴属性防御(魔法防御)百分比")
	defMagPct,
	@Column(type=int.class, comment="命中")
	hit,
	@Column(type=int.class, comment="命中百分比")
	hitPct,
	@Column(type=int.class, comment="闪避")
	dodge,
	@Column(type=int.class, comment="闪避百分比")
	dodgePct	,
	@Column(type=int.class, comment="暴击概率(暴击)")
	crit,
	@Column(type=int.class, comment="暴击概率(暴击)百分比")
	critPct,
	@Column(type=int.class, comment="暴击比例(暴击加成)")
	critAdd,
	@Column(type=int.class, comment="暴击比例(暴击加成)百分比")
	critAddPct,
	@Column(type=int.class, comment="坚韧")
	tough,
	@Column(type=int.class, comment="坚韧百分比")
	toughPct,
	@Column(type=int.class, comment="元素1伤害")
	elem1Atk	,
	@Column(type=int.class, comment="元素1伤害百分比")
	elem1AtkPct,
	@Column(type=int.class, comment="元素1防御")
	elem1Def	,
	@Column(type=int.class, comment="元素1防御百分比")
	elem1DefPct,
	@Column(type=int.class, comment="元素2伤害")
	elem2Atk	,
	@Column(type=int.class, comment="元素2伤害百分比")
	elem2AtkPct,
	@Column(type=int.class, comment="元素2防御")
	elem2Def	,
	@Column(type=int.class, comment="元素2防御百分比")
	elem2DefPct,
	@Column(type=int.class, comment="元素3伤害")
	elem3Atk	,
	@Column(type=int.class, comment="元素3伤害百分比")
	elem3AtkPct,
	@Column(type=int.class, comment="元素3防御")
	elem3Def	,
	@Column(type=int.class, comment="元素3防御百分比")
	elem3DefPct,
	@Column(type=int.class, comment="元素4伤害")
	elem4Atk,
	@Column(type=int.class, comment="元素4伤害百分比")
	elem4AtkPct,
	@Column(type=int.class, comment="元素4防御")
	elem4Def	,
	@Column(type=int.class, comment="元素4防御百分比")
	elem4DefPct,
	@Column(type=int.class, comment="吸血概率")
	suck,
	@Column(type=int.class, comment="吸血概率百分比")
	suckPct,
	@Column(type=int.class, comment="吸血比率")
	suckRatio	,
	@Column(type=int.class, comment="吸血比率百分比")
	suckRatioPct,
	@Column(type=int.class, comment="免伤比例")
	avoidAtk,
	@Column(type=int.class, comment="免伤比例百分比")
	avoidAtkPct,
	@Column(type=int.class, comment="治疗效果")
	skillHealth,
	@Column(type=int.class, comment="治疗效果百分比")
	skillHealthPct,
	@Column(type=int.class, comment="移动速度")
	speed,
	@Column(type=int.class, comment="移动速度百分比")
	speedPct,
	@Column(type=int.class, comment="生命恢复比例")
	hpRecov,
	@Column(type=int.class, comment="生命恢复比例百分比")
	hpRecovPct,
	@Column(type=int.class, comment="魔法恢复比例")
	mpRecov,
	@Column(type=int.class, comment="魔法恢复比例百分比")
	mpRecovPct,
	@Column(type=int.class, comment="能耗降低(魔法使用降低)")
	mpReduce,
	@Column(type=int.class, comment="能耗降低(魔法使用降低)百分比")
	mpReducePct,
	
	/* 仇恨值 */
	@Column(type=int.class, comment="仇恨值")
	aggro,
	
	/* 玩家属性 */
	@Column(type=int.class, comment="当前等级")
	level,
	@Column(type=int.class, comment="当前生命")
	hpCur,
	@Column(type=int.class, comment="当前法力")
	mpCur,
	@Column(type=int.class, comment="战斗力", defaults="1000")
	combat,
	@Column(type=String.class, comment="PVP模式")
	pvpMode,
	@Column(type=String.class, comment="PVP还原模式")
	pvpModeOriginal,
	@Column(type=int.class, comment="敌我模式下的队伍id,仅敌我模式下有效")
	pvpDiWoId,
	@Column(type=long.class, comment="上次更改PVP模式为和平模式的时间")
	pvpModeTimeToHePing,
	
	@Column(type=String.class, comment="配置表SN", defaults="")
	sn,
	@Column(type=String.class, comment="姓名", index=true)
	name,
	@Column(type=String.class, comment="模型sn")
	modelSn,
	@Column(type=int.class, comment="职业(1战士,2刺客,3咒术师)")
	profession,
	@Column(type=int.class, comment="性别")
	sex,
	@Column(type=long.class, comment="当前经验")
	expCur,
	@Column(type=long.class, comment="本级升到下一级所需经验")
	expUpgrade,
	@Column(type=boolean.class, comment="是否在战斗状态(可以不持久化)")
	inFighting,
	@Column(type=long.class, comment="战斗状态截止时间(可以不持久化)")
	fightStateEndTime,
	@Column(type=int.class, comment="unit对应的阵型位置", defaults="-1")
	attingIndex,
	
	/* 技能 */
	@Column(type=String.class, comment="技能", defaults="[]")
	skill,
	@Column(type=int.class, comment="技能组")
	skillGroupSn,
	@Column(type=String.class, comment="天赋", defaults="[]")
	inborn,
	

	;
}