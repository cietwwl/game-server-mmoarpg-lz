package org.gof.core.connsrv;
                    
import java.util.List;  
import org.gof.core.Port;
import org.gof.core.CallPoint;
import org.gof.core.Service;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;
import org.gof.core.support.log.LogCore;
import org.gof.core.gen.proxy.ProxyBase;
import org.gof.core.support.function.*;
//import org.gof.core.EnumCall;
import org.gof.core.gen.GofGenFile;

@GofGenFile
public final class ConnServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_CORE_CONNSRV_CONNSERVICE_CHECK_LONG = 1;
	}
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private ConnServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		ConnService serv = (ConnService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNSERVICE_CHECK_LONG: {
				return (GofFunction1<Long>)serv::check;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ConnServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ConnServiceProxy newInstance(String node, String port, Object id) {
		return createInstance(node, port, id);
	}
	
	/**
	 * 创建实例
	 * @param localPort
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static ConnServiceProxy createInstance(String node, String port, Object id) {
		ConnServiceProxy inst = new ConnServiceProxy();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Param context) {
		localPort.listenResult(method, context);
	}
	
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Param context) {
		localPort.listenResult(method, context);
	}
	
	
	/**
	 * 等待返回值
	 */
	public Param waitForResult() {
		return localPort.waitForResult();
	}
	
	public void check(long connId) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNSERVICE_CHECK_LONG, new Object[]{ connId });
	}
}
