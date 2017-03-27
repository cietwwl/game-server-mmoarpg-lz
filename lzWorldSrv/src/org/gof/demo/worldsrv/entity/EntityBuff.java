package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="Buff", tableName="demo_buff")
public enum EntityBuff {
	@Column(type=int.class, comment="BuffSn", index=true)
	sn,
	@Column(type=int.class, comment="类型")
	type,
	@Column(type=boolean.class, comment="是否作用过")
	affected,
	@Column(type=long.class, comment="下次更新时间")
	timePulse,
	@Column(type=long.class, comment="在线存在时间（在线累加）")
	timeExistOnline,
	@Column(type=long.class, comment="结束时间戳")
	timeEnd,
	@Column(type=long.class, comment="Buff作用对象Id")
	idAffect,
	@Column(type=long.class, comment="施放者Id")
	idFire,
	@Column(type=String.class, comment="该buff初始对属性的影响", defaults = "{}")
	propPlusDefaultJSON,
	@Column(type=String.class, comment="该buff对属性的影响", defaults = "{}")
	propPlusJSON,
	;

}