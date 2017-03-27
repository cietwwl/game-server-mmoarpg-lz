package org.gof.demo.battlesrv.ai.bevTree;

import org.gof.core.support.Param;

/**
 * 选择节点
 * 
 * @author GaoZhangCheng
 */
public class AIBevSelectorNode extends AIBevNodeControl{

	@Override
	public boolean execute(Param param) {
        boolean result = false;
        for (AIBevLeaf iterable_element : child) {
        	if(iterable_element.executeLogic(param)) {
        		return true;
        	}
		}
        return result;
	}
	

}
