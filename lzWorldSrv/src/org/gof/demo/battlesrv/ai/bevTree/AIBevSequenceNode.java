package org.gof.demo.battlesrv.ai.bevTree;

import org.gof.core.support.Param;

/**
 * 顺序节点
 * 
 * @author GaoZhangCheng
 */
public class AIBevSequenceNode extends AIBevNodeControl{

	@Override
	public boolean execute(Param param) {
        boolean result = true;
        for (AIBevLeaf iterable_element : child) {
        	if(!iterable_element.executeLogic(param)) {
        		return false;
        	}
		}
        return result;
	}

}
