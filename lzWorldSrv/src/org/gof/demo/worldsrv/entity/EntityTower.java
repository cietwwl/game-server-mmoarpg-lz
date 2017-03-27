package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="Tower", tableName="demo_tower")
public enum EntityTower {
	@Column(type=long.class, comment="所属用户", index=true)
	humanId,
	@Column(type=int.class, comment="今日剩余刷新次数")
	refreshCount,
	@Column(type=int.class, comment="重置BUFF次数")
	resetBuffCount,
	@Column(type=int.class, comment="当前BUFF")
	buffSn,
	@Column(type=int.class, comment="关卡进度")
	curLayer,
	@Column(type=int.class, comment="领宝箱进度")
	curBox,
	@Column(type=String.class, comment="每个关卡的对手信息", defaults = "{}", length=4096)
	layerEnemyInfoJson,
	@Column(type=String.class, comment="每个关卡的对手信息", defaults = "[]", length=2048)
	layerJson,
	@Column(type=String.class, comment="关卡对应的神秘奖励", defaults = "{}", length=2048)
	awardJson,
	;
}
