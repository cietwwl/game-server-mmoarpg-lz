package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;

@Entity(entityName="InstanceChapter", tableName="demo_instance")
public enum EntityInstanceChapter {
	@Column(type=long.class, comment="所属用户", index=true)
	humanId,
	@Column(type=int.class, comment="章节SN")
	chapterSn,
	@Column(type=String.class, comment="每个章节对应三个宝箱的领取状态", defaults = "{}")
	chapterBoxJson,
	@Column(type=String.class, comment="章节副本对应完成进度（星星）repID:[1,1,1]", defaults = "{}")
	repStarJson,
	@Column(type=String.class, comment="章节副本对应完成次数存放的是repID:attNum", defaults = "{}")
	repAttNumJson,
	@Column(type=String.class, comment="章节副本对应花钱增加次数repID:resetNum", defaults = "{}")
	repResetJson,
	;
}
