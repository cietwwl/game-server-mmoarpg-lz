package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.core.support.Time;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;

/**
 * AI：等一个随机秒数 改变tick的频率
 * 
 * @author GaoZhangCheng
 */
public class AIBevActChangeTick extends AIBevLeaf {

	double delay = 0;
	public AIBevActChangeTick(AI ai) {
		this.ai = ai;
	}
	public AIBevActChangeTick(AI ai, double delay) {
		this.ai = ai;
		this.delay = delay;
	}
	@Override
	public boolean execute(Param param) {
//		ai.timerPulse.setTimeNext(ai.unitObj.getTime() + (int)((ai.unitObj.nextDouble() * (ai.PULSE_TIME / 3 + delay))* Time.SEC));
		ai.timerPulse.setTimeNext(ai.unitObj.getTime() +(int)((5 + ai.unitObj.nextDouble() * (6 + delay))* Time.SEC));
		return true;
	}
}
