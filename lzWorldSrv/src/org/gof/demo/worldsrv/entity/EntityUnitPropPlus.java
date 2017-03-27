package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="UnitPropPlus", tableName="demo_unit_propplus")
public enum EntityUnitPropPlus {
	@Column(type=String.class, comment="基础能力", length = 512, defaults = "{}")
	base,
	@Column(type=String.class, comment="等级", length = 512, defaults = "{}")
	level,
	@Column(type=String.class, comment="Buff", length = 512, defaults = "{}")
	buff,
	@Column(type = String.class, comment = "身上所有装备", length=512,  defaults = "{}")
	itemEquip,
	@Column(type = String.class, comment="测试", length = 512, defaults = "{}")
	debug,
	;
}
