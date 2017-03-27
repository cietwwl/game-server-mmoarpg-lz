package org.gof.demo.battlesrv.ai.bevTree;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.support.EnumBevNodeType;

/**
 * 叶子节点类型
 * 
 * @author GaoZhangCheng
 */
public abstract class AIBevLeaf {
    protected String name;
    //the layer node in tree , the root node is 0 layer.
    protected int layerIndex = 0;
    protected EnumBevNodeType nodeType;
    
    protected boolean nonLogic = false;
    protected AI ai;
    abstract public boolean execute(Param param);
    
    public boolean executeLogic(Param param) {
    	boolean result = execute(param);
    	if(nonLogic) {
			result = !result;
		}
    	return result;
    }
    
}
