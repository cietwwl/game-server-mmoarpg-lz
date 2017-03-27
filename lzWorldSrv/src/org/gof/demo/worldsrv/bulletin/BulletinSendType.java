package org.gof.demo.worldsrv.bulletin;
/**
 * 目前走马灯触发方式有两种
 * GM，和系统
 */
public enum BulletinSendType {
	
	GM((byte)0),	//GM
	SYS((byte)1),	//系统添加的公告
	;
	
	private final byte type;//活动类型
	
	private BulletinSendType(byte type) {
		this.type = type;
	}

	public byte getType() {
		return type;
	}
}
