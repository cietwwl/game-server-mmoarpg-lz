package org.gof.demo.worldsrv.common;

import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.Service;
import org.gof.core.gen.proxy.DistrClass;

public abstract class GameServiceBase extends Service {
	private String serviceId = "";					//服务ID号

	/**
	 * 初始化数据
	 * @return
	 */
	protected abstract void init();
	
	/**
	 * 构造函数
	 * @param port
	 */
	public GameServiceBase(GamePort port) {
		super(port);
	}
	
	/**
	 * 启动服务
	 */
	public void startupLocal() {
		port.addQueue(new PortPulseQueue(this) {
			@Override
			public void execute(Port port) {
				Service serv = param.get();
				serv.startup();
				
				init();
				
				port.addService(param.<GameServiceBase>get());
			}
		});
	}
	
	@Override
	public final Object getId() {
		//初始化服务ID
		if("".equals(serviceId)) {
			DistrClass conf = getClass().getAnnotation(DistrClass.class);
			serviceId = conf.servId();
		}
		
		return serviceId;
	}
}
