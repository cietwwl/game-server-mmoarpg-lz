package org.gof.demo.worldsrv.stage;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 地图全局信息
 * 记录Node,Port等信息，地图间通信等情况下会用到。
 */
public class StageGlobalInfo {
	public final long id;				//地图真实ID
	public final int sn;				//地图配置SN
	public final String name;			//地图名称
	public final String nodeId;		//Node名称
	public final String portId;		//Port名称
	public int lineNum = 1;		//分线的编号
	public int humanNum = 0;			//地图中玩家个数
	
	/**
	 * 构造函数
	 * @param id
	 * @param sn
	 * @param name
	 * @param nodeId
	 * @param portId
	 */
	public StageGlobalInfo(long id, int sn, String name, String nodeId, String portId) {
		super();
		this.id = id;
		this.sn = sn;
		this.name = name;
		this.nodeId = nodeId;
		this.portId = portId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
						.append("id", id)
						.append("sn", sn)
						.append("name", name)
						.append("nodeId", nodeId)
						.append("portId", portId)
						.append("lineNum", lineNum)
						.append("humanNum", humanNum)
						.toString();
	}
}
