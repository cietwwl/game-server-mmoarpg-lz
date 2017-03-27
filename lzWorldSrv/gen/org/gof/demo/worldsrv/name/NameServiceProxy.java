package org.gof.demo.worldsrv.name;
                    
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

@GofGenFile
public final class NameServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_ADD_STRING = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_CHANGE_STRING_STRING = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_RANDOMNAME = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_REPEAT_STRING = 4;
	}
	private static final String SERV_ID = "name";
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private NameServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		NameService serv = (NameService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_ADD_STRING: {
				return (GofFunction1<String>)serv::add;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_CHANGE_STRING_STRING: {
				return (GofFunction2<String, String>)serv::change;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_RANDOMNAME: {
				return (GofFunction0)serv::randomName;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_REPEAT_STRING: {
				return (GofFunction1<String>)serv::repeat;
			}
			default: break;
		}
		return null;
	}
	
	/**
	 * 获取实例
	 * 大多数情况下可用此函数获取
	 * @param localPort
	 * @return
	 */
	public static NameServiceProxy newInstance() {
		String portId = Distr.getPortId(SERV_ID);
		if(portId == null) {
			LogCore.remote.error("通过servId未能找到查找上级Port: servId={}", SERV_ID);
			return null;
		}
		
		String nodeId = Distr.getNodeId(portId);
		if(nodeId == null) {
			LogCore.remote.error("通过portId未能找到查找上级Node: portId={}", portId);
			return null;
		}
		
		return createInstance(nodeId, portId, SERV_ID);
	}
	
	
	/**
	 * 创建实例
	 * @param localPort
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static NameServiceProxy createInstance(String node, String port, Object id) {
		NameServiceProxy inst = new NameServiceProxy();
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
	
	public void add(String name) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_ADD_STRING, new Object[]{ name });
	}
	
	public void change(String oldName, String newName) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_CHANGE_STRING_STRING, new Object[]{ oldName, newName });
	}
	
	public void randomName() {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_RANDOMNAME, new Object[]{  });
	}
	
	public void repeat(String newName) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_NAME_NAMESERVICE_REPEAT_STRING, new Object[]{ newName });
	}
}
