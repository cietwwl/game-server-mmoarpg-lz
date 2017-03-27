package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.core.support.TickTimer;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.ai.bevTree.AIBevNodeControl;

/**
 * AI：添加延时处理
 * 
 * @author GaoZhangCheng
 */
public class AIBevDelayNode extends AIBevNodeControl{

	private TickTimer timeAIBevDelay = new TickTimer();
	private boolean executing = false;
	private long time = 0;
	public AIBevDelayNode(long time) {
		this.time = time;
	}
	public boolean execute(Param param) {
		boolean result = true;
		if(executing) {
			long curr = ai.unitObj.getTime();
			
			//如果时间到了 执行所有延迟逻辑
			if(timeAIBevDelay.isPeriod(curr)) {
			  for (AIBevLeaf iterable_element : child) {
		        	iterable_element.execute(param);
				}
			  executing = false;
			}
	        
		} else {
			if(ai == null) return false;
			
			timeAIBevDelay.start(ai.unitObj.getTime(), time);
			executing = true;
		}
		
		return result;
	}

}
