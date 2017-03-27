package org.gof.demo.worldsrv.support.enumKey;


public enum ItemPackTypeKey {
	Parts(0),		// 装备位
	Bag1(1),		// 主背包
	Bag2(2),		// 第二背包
	;
	
	private ItemPackTypeKey(int packType) {
		this.packType = packType;
	}

	public final int packType;

	public static ItemPackTypeKey getType(int packType) {
		for(ItemPackTypeKey k : values()) {
			if(k.packType != packType) continue;
			return k;
		}
		
		return null;
	}
}