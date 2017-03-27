package org.gof.demo.worldsrv.entity;

import org.gof.core.gen.entity.Column;
import org.gof.core.gen.entity.Entity;


@Entity(entityName="Friend", tableName="demo_friend")
public enum EntityFriend {
	
	@Column(type = long.class, comment = "玩家id" , index=true)
	humanId,
	@Column(type = String.class, comment = "好友列表" , defaults = "[]", length=2048)
	friendList,
	@Column(type = String.class, comment = "黑名单列表" , defaults = "[]", length=2048)
	blackList,
	@Column(type = String.class, comment = "申请人列表" , defaults = "[]", length=2048)
	applyList,
}
