package org.gof.demo.worldsrv.test;

import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.Log;

/**
 * TestService 测试的Service类
 * @author Change
 *
 */
@DistrClass(
		servId = D.SERV_TEST
)
public class TestService extends GameServiceBase {	
	
	/**
	 * 初始化数据
	 * @return
	 */
	protected void init() {
		
	}
	
	public TestService(GamePort port) {
		super(port);
	}

	@DistrMethod
	public void csTestService(int msgId) {
		TestManager.inst().testManager();
		
		Log.game.info("TestService has received CSTest, CODE is [{}]", msgId);
		TestManager.inst().reLoadConf();
		port.returns("AAA");
	}	
}
