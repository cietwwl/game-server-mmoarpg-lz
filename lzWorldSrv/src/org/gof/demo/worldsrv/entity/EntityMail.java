package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;


@Entity(entityName="Mail", tableName="demo_mail")
public enum EntityMail {
	@Column(type=long.class, comment="接收者", index=true)
	humanId,
	@Column(type=long.class, comment="发送者")
	sender,
	@Column(type=String.class, comment="标题")
	title,	
	@Column(type=String.class, comment="内容")
	content,	
	@Column(type=long.class, comment="接收时间戳")
	rtime,
	@Column(type=long.class, comment="有效期截止时间戳")
	etime,	
	@Column(type=boolean.class, comment="是否已读")
	read,	
	@Column(type=String.class, comment="附件信息(JSON)", defaults = "{}")
	item,	
	;
}