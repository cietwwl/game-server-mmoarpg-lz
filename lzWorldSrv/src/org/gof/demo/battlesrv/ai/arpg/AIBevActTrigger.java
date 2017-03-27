package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.core.support.Time;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;

public class AIBevActTrigger extends AIBevLeaf {
	private long time = 0;
	private long period = 0;
	private boolean executing = false;
	public AIBevActTrigger(AI ai) {
		this.ai = ai;
		this.time = (int)((5 + ai.unitObj.nextDouble() * 6)* Time.SEC);
	}
	
	public AIBevActTrigger(AI ai, long time) {
		this.ai = ai;
		this.time = time;
	}
	public boolean execute(Param param) {
		if(executing) {
			if(ai.unitObj.getTime() > this.period) { 
				executing = false;
				return true;
			}
		}
		
		if(!executing) {
			this.period = ai.unitObj.getTime() + this.time;
			executing = true;
		} 
		
		return false;
	}
}
