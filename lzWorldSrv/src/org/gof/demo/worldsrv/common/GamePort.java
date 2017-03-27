package org.gof.demo.worldsrv.common;

import org.gof.core.Port;
import org.gof.core.Service;
import org.gof.core.support.SysException;
import org.gof.core.support.idAllot.IdAllotPoolBase;
import org.gof.demo.seam.id.IdAllotPool;

public class GamePort extends Port {

	public GamePort(String name) {
		super(name);
	}

	/**
	 * 覆写父类函数
	 * 增加防ID重复的判断机制
	 */
	@Override
	public void addService(Service service) {
		//先检查一下此ID之前是否已经添加过 避免由于ID冲突造成隐藏的BUG
		Service serv = getService(service.getId());
		if(serv != null) {
			throw new SysException("PortCommon添加下属服务时发送重复的号码：ID={}", service.getId());
		}
		
		super.addService(service);
	}

	@Override
	protected IdAllotPoolBase initIdAllotPool() {
		return new IdAllotPool(this);
	}
}
