package org.gof.demo.seam.account;
                    
import java.util.List;  
import org.gof.core.Port;
import org.gof.core.CallPoint;
import org.gof.core.Service;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;
import org.gof.core.support.log.LogCore;
import org.gof.core.gen.proxy.ProxyBase;
import org.gof.core.support.function.*;
//import org.gof.demo.EnumCall;
import org.gof.core.gen.GofGenFile;
import org.gof.core.support.ConnectionStatus;

@GofGenFile
public final class AccountServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_CONNCHECK_LONG = 1;
		public static final int ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_CONNCLOSED_LONG = 2;
		public static final int ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_MSGHANDLER_LONG_CONNECTIONSTATUS_BYTES = 3;
	}
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private AccountServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		AccountService serv = (AccountService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_CONNCHECK_LONG: {
				return (GofFunction1<Long>)serv::connCheck;
			}
			case EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_CONNCLOSED_LONG: {
				return (GofFunction1<Long>)serv::connClosed;
			}
			case EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_MSGHANDLER_LONG_CONNECTIONSTATUS_BYTES: {
				return (GofFunction3<Long, ConnectionStatus, byte[]>)serv::msgHandler;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static AccountServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static AccountServiceProxy newInstance(String node, String port, Object id) {
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
	private static AccountServiceProxy createInstance(String node, String port, Object id) {
		AccountServiceProxy inst = new AccountServiceProxy();
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
	
	public void connCheck(long connId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_CONNCHECK_LONG, new Object[]{ connId });
	}
	
	public void connClosed(long connId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_CONNCLOSED_LONG, new Object[]{ connId });
	}
	
	public void msgHandler(long connId, ConnectionStatus status, byte... msgbuf) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_SEAM_ACCOUNT_ACCOUNTSERVICE_MSGHANDLER_LONG_CONNECTIONSTATUS_BYTES, new Object[]{ connId, status, msgbuf });
	}
}
