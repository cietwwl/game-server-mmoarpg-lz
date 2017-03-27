package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="LevelRank", tableName="demo_level_rank")
public enum EntityLevelRank {
	@Column(type=long.class, comment="玩家ID")
	humanId,
	@Column(type=String.class, comment="角色模型SN")
	characterSn,
	@Column(type=String.class, comment="玩家姓名")
	humanName,
	@Column(type=int.class, comment="玩家等级")
	humanLevel,
	@Column(type=long.class, comment="更新排名时间")
	rankTime,
	@Column(type=int.class, comment="玩家排名")
	rank,
	;
}
