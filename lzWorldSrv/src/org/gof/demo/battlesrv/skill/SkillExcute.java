package org.gof.demo.battlesrv.skill;

import org.gof.core.support.TickTimer;


public class SkillExcute {
	public int sn;					//sn
	public SkillParam tarPos;		//目标位置
	public TickTimer tickTimer;		//可以执行的时间
	
	public SkillExcute(int sn, SkillParam tarPos, long curr, long tickTimer) {
		super();
		this.sn = sn;
		this.tarPos = tarPos;
		this.tickTimer = new TickTimer(curr, tickTimer);
	}
}
