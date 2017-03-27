package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="CompetitionHuman", tableName="demo_competition_human")
public enum EntityCompetitionHuman {
	@Column(type=long.class, comment="玩家ID")
	humanId,
	@Column(type=String.class, comment="角色模型SN")
	characterSn,
	@Column(type=String.class, comment="玩家姓名")
	humanName,
	@Column(type=int.class, comment="玩家等级")
	humanLevel,
	@Column(type=int.class, comment="战斗力")
	combat,
	@Column(type=long.class, comment="更新排名时间")
	rankTime,
	@Column(type=int.class, comment="玩家当前排名")
	rank,
	@Column(type=int.class, comment="每日结算时的排名")
	rankDaily,
	@Column(type=long.class, comment="发奖的时间")
	awardTime,
	@Column(type=boolean.class, comment="是否是机器人")
	isRobot,
	;
}
