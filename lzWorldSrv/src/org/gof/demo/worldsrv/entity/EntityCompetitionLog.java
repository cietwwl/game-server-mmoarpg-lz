package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="CompetitionLog", tableName="demo_competition_log")
public enum EntityCompetitionLog {
	@Column(type=long.class, comment="挑战者id", index = true)
	attackerId,
	@Column(type=String.class, comment="挑战者姓名", index = true)
	attackerName,
	@Column(type=int.class, comment="挑战者等级")
	attackerLevel,
	@Column(type=String.class, comment="挑战者模型SN", index = true)
	attackerModelSn,
	@Column(type=long.class, comment="被挑战者id", index = true)
	defenderId,
	@Column(type=String.class, comment="被挑战者姓名", index = true)
	defenderName,
	@Column(type=int.class, comment="被挑战者等级")
	defenderLevel,
	@Column(type=String.class, comment="被挑战者模型SN", index = true)
	defenderModelSn,
	@Column(type=boolean.class, comment="是否获胜")
	win,
	@Column(type=int.class, comment="玩家当前新的排名, 如果排名不变则为0，否则为当前新的排名")
	attackerRank,
	@Column(type=int.class, comment="被挑战玩家当前新的排名, 如果排名不变则为0，否则为当前新的排名")
	defenderRank,
	@Column(type=long.class, comment="比赛时间")
	createdTime,
	;
}