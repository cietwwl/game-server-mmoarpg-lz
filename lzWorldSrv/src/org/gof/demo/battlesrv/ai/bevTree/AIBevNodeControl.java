package org.gof.demo.battlesrv.ai.bevTree;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制执行节点
 * 
 * @author GaoZhangCheng
 */
public abstract class AIBevNodeControl extends AIBevLeaf{

	protected List<AIBevLeaf> child = new ArrayList<AIBevLeaf>();

	public void addChild(AIBevLeaf leaf) {
		child.add(leaf);
	}
	public void clear() {
		child.clear();
	}
}
