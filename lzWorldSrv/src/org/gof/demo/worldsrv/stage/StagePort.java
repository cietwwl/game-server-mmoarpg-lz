package org.gof.demo.worldsrv.stage;

import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.support.idAllot.IdAllotPoolBase;
import org.gof.demo.seam.id.IdAllotPool;


public class StagePort extends Port {
	
	public StagePort(String name) {
		super(name);
	}
	
	static {
		init();
	}
	
	/**
	 * 初始化地图
	 */
	public static void init(){
			
	}
	
	/**
	 * 在Port中安全创建普通地图
	 * @param stageSn
	 */
	public void createCommonSafe(int stageSn) {
		this.addQueue(new PortPulseQueue(stageSn) {
			@Override
			public void execute(Port port) {
				StageManager.inst().createCommon(param.getInt());
			}
		});
	}
	
	/**
	 * 获取地图对象
	 * @param id
	 * @return
	 */
	public StageObject getStageObject(long id) {
		StageObjectService serv = getService(id);
		if(serv == null) return null;
		
		return serv.getStageObj();
	}

	@Override
	protected IdAllotPoolBase initIdAllotPool() {
		return new IdAllotPool(this);
	}
	
}
