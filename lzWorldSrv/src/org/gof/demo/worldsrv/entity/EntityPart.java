package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

/**
 * 装备位的定义。
 * 
 * @author zhangbo
 *
 */
@Entity(entityName = "Part", tableName="demo_part")
public enum EntityPart {
	@Column(type = int.class, comment = "装备位编号SN")
	sn,
	@Column(type = long.class, comment = "玩家ID", index = true)
	humanId,
	@Column(type = int.class, comment = "强化等级")
	qianghuaLv,
	@Column(type = String.class, comment = "冲星等级")
	chongxingLv,
	@Column(type = String.class, comment = "总属性")
	propJson,
	@Column(type = long.class, comment = "装备ID")
	equipId,
	@Column(type = String.class, comment = "宝石数组ID")
	gemsIds,
	@Column(type = boolean.class, comment = "是否激活")
	enabled;
}
