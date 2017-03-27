package org.gof.demo.seam.id;

import org.gof.core.Port;
import org.gof.core.support.idAllot.IdAllotPoolBase;
import org.gof.demo.worldsrv.support.C;

/**
 * 可分配ID池
 * 此类并非线程安全的
 */
public class IdAllotPool extends IdAllotPoolBase {

	public IdAllotPool(Port port) {
		super(port, C.GAME_PLATFORM_ID, C.GAME_SERVER_ID);
	}
	
	public IdAllotPool(Port port, int applyNum, int warnNum) {
		super(port, C.GAME_PLATFORM_ID, C.GAME_SERVER_ID, applyNum, warnNum);
	}
}
