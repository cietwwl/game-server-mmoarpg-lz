package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="PocketLine", tableName="demo_pocket_line")
public enum EntityPocketLine {
	@Column(type=long.class, comment="玩家Id", index=true)
	humanId,
	@Column(type=String.class, comment="模块名称")
	moduleName,
	@Column(type=String.class, comment="待办事项", length=255)
	param,
}