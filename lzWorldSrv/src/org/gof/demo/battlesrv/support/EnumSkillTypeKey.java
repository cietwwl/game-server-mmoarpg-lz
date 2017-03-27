package org.gof.demo.battlesrv.support;

public enum EnumSkillTypeKey {
	普通技能(0),	//一般技能
	绝招(1),	//手动释放的大招
	杀招(2),	//跳过技能努气 前摇体系的 不用怒气的大招
	普通攻击(3),  //一般攻击
	;
	
	private int type;
	
	private EnumSkillTypeKey(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
