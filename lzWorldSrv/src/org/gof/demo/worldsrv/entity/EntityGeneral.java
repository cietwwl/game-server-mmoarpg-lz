package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="General", tableName="demo_general", superEntity=EntityUnit.class)
public enum EntityGeneral {
	@Column(type=long.class, comment="所属的ID", index=true)
	humanID,
	@Column(type=int.class, comment="伙伴星级")
	star,
	@Column(type=int.class, comment="伙伴品质")
	quality,	
	
	@Column(type=String.class, comment="伙伴装备信息",defaults="[]")
	equip,		
	;
}
