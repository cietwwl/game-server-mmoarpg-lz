package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

/**
 * 物品属性定义。
 * 
 * @author zhangbo
 *
 */
@Entity(entityName = "ItemBase", isSuper=true)
public enum EntityItemBase {
	@Column(type = int.class, comment = "SN")
	sn,
	@Column(type = long.class, comment = "玩家ID", index = true)
	humanId,
	@Column(type = int.class, comment = "数量")
	num,
	@Column(type = int.class, comment = "绑定状态")
	bind,
	@Column(type = int.class, comment = "物品在容器中的位置 装备位从1开始 背包从0开始")
	position,
	@Column(type = int.class, comment = "所在容器")
	packType,
	@Column(type = String.class, comment = "装备总属性")
	propJSON,
	@Column(type = int.class, comment = "装备战斗力")
	combat,
	@Column(type = boolean.class, comment = "是否是新的")
	isNew,
	@Column(type = String.class, comment = "扩展参数")
	param;	
}
