package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="Activity", tableName="demo_activity")
public enum EntityActivity {
	@Column(type=long.class, comment="所属用户", index=true)
	humanId,
	@Column(type=String.class, comment="签到相关{count:count,time:time,state:state}", defaults = "{}")
	signInJson,
	@Column(type=String.class, comment="连续登陆相关{count:count,time:time}", defaults = "{}")
	lineLoginJson,
	@Column(type=String.class, comment="累计登陆相关{count:count,time:time}", defaults = "{}")
	addLoginJson,
	;
}
