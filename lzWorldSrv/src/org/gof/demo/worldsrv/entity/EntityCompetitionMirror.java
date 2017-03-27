package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="CompetitionMirror", tableName="demo_competition_mirror")
public enum EntityCompetitionMirror {
	@Column(type=long.class, comment="humanID", index=true)
	humanID,
	@Column(type=int.class, comment="当前等级")
	level,
	@Column(type=String.class, comment="姓名", index=true)
	name,
	@Column(type=String.class, comment="sn")
	sn,
	@Column(type=String.class, comment="modelSn")
	modelSn,
	@Column(type=int.class, comment="profession")
	profession,
	@Column(type=int.class, comment="性别")
	sex,
	@Column(type=int.class, comment="战斗力")
	combat,
	@Column(type=String.class, comment="技能", defaults="{}")
	skill,
	@Column(type=int.class, comment="技能组SN")
	skillGroupSn,
	@Column(type=String.class, comment="所有能力", length = 512, defaults = "{}")
	base,
	@Column(type = long.class, comment="玩家同步时间", index=true) 
	enterTime,
	@Column(type=int.class, comment="伙伴星级")
	star,
	@Column(type=int.class, comment="伙伴品质")
	quality,	
	@Column(type=String.class, comment="伙伴装备信息",defaults="[]")
	equip,	
	@Column(type = boolean.class, comment="是否是玩家，否则是伙伴", index=true) 
	human,
	@Column(type=int.class, comment="伙伴站的位置")
	attendIndex,
}