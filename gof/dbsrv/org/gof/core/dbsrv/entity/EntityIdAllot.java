package org.gof.core.dbsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="IdAllot", tableName="core_id_allot")
public enum EntityIdAllot {
	@Column(type=long.class, comment="当前已分配的ID最大值", index=true)
	value,
	;
}