package org.gof.demo.worldsrv.support.enumKey;

public enum AttachmentKey {

	//邮件附件类型枚举
	
	money(0),  //货币类型
	item(1);   //物品类型
		
	private AttachmentKey(int type){
		this.type = type;
	} 
	
	private int type;
	
	public int getType() {
		return type;
	}
	
	public static AttachmentKey getEnumByType(int type) {
		for(AttachmentKey k : values()) {
			if(k.type == type)
				return k;
		}
		return null;
	}	
	
}
