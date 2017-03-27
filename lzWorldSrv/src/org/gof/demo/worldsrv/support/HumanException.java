package org.gof.demo.worldsrv.support;

import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.inform.Inform;


/**
 * 当程序出现可预知错误，并希望用户看到出错信息的时候抛出此异常
 */
public class HumanException extends RuntimeException {
	private static final long serialVersionUID = 1;
	
	public HumanException(long humanId, String msg, Object...params) {
		super(Utils.createStr(msg, params));
		
		Inform.user(humanId, Inform.提示错误, Utils.createStr(msg, params));
	}
	
	public HumanException(Throwable e, long humanId, String msg, Object...params) {
		super(Utils.createStr(msg, params), e);

		Inform.user(humanId, Inform.提示错误, Utils.createStr(msg, params));
	}
}