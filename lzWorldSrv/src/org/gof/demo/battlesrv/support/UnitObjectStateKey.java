package org.gof.demo.battlesrv.support;

/**
 * unitObj的状态
 *
 */
public enum UnitObjectStateKey {
	skillback(0),					//技能击退
	stun(1),							//眩晕
	immobilize(2),				//冻结
	silence(3),						//沉默
	skill_shake(4),				//施法前摇
	skill_sheep(5),				//变羊
	skill_hypnosis(6),			//催眠
	cast_skilling(7),			//正在释放技能
	charm(8),						//魅惑
	cast_locked(9),				//技能锁定，释放时候不能移动
	;
	
	private int type;
	
	private UnitObjectStateKey(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
