package org.gof.demo.seam;

import org.gof.core.Port;
import org.gof.core.support.idAllot.IdAllotPoolBase;
import org.gof.demo.seam.id.IdAllotPool;

public class DefaultPort extends Port  {
	public DefaultPort(String name) {
		super(name);
	}
	
	@Override
	protected IdAllotPoolBase initIdAllotPool() {
		return new IdAllotPool(this);
	}
}