package org.gof.demo.worldsrv.common;

import org.gof.core.gen.proxy.DistrClass;
import org.gof.demo.worldsrv.support.D;

@DistrClass(
	servId = D.SERV_DATA_RESET
)
public class DataResetService extends GameServiceBase {
	//每日零时重置
	public static final String CRON_DAY_ZERO = "1 0 0 * * ?";
	//每日五时重置
	public static final String CRON_DAY_FIVE = "1 0 5 * * ?";
	
	//每日零时重置
	public static final String CRON_DAY_ZERO_10 = "1 0 0 * * ?";
	//每日五时重置
	public static final String CRON_DAY_FIVE_10 = "1 0 5 * * ?";
	
	//每日21时重置
	public static final String CRON_DAY_21ST = "1 0 21 * * ?";
	//每周零时重置
	public static final String CRON_WEEK_ZERO = "1 0 0 ? * MON";
	//每周五时重置
	public static final String CRON_WEEK_FIVE = "1 0 5 ? * MON";
	
	//每日12时重置
	public static final String CRON_DAY_12ST = "1 0 12 * * ?";
	//每日18时重置
	public static final String CRON_DAY_18ST = "1 0 18 * * ?";
	

	public DataResetService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		
	}
}
