package org.gof.core.dbsrv;
                    
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
import org.gof.core.CallReturn;

@GofGenFile
public final class DBLargeServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_CORE_DBSRV_DBLARGESERVICE_COUNTANDRETURN_CALLRETURN_STRING_OBJECTS = 1;
		public static final int ORG_GOF_CORE_DBSRV_DBLARGESERVICE_EXECUTEUPDATE_STRING_OBJECTS = 2;
		public static final int ORG_GOF_CORE_DBSRV_DBLARGESERVICE_EXECUTEUPDATE_BOOLEAN_STRING_OBJECTS = 3;
		public static final int ORG_GOF_CORE_DBSRV_DBLARGESERVICE_FINDANDRETURN_CALLRETURN_BOOLEAN_BOOLEAN_STRING_OBJECTS = 4;
	}
	private static final String SERV_ID = "dbLarge";
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private DBLargeServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		DBLargeService serv = (DBLargeService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_CORE_DBSRV_DBLARGESERVICE_COUNTANDRETURN_CALLRETURN_STRING_OBJECTS: {
				return (GofFunction3<CallReturn, String, Object[]>)serv::countAndReturn;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBLARGESERVICE_EXECUTEUPDATE_STRING_OBJECTS: {
				return (GofFunction2<String, Object[]>)serv::executeUpdate;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBLARGESERVICE_EXECUTEUPDATE_BOOLEAN_STRING_OBJECTS: {
				return (GofFunction3<Boolean, String, Object[]>)serv::executeUpdate;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBLARGESERVICE_FINDANDRETURN_CALLRETURN_BOOLEAN_BOOLEAN_STRING_OBJECTS: {
				return (GofFunction5<CallReturn, Boolean, Boolean, String, Object[]>)serv::findAndReturn;
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
	public static DBLargeServiceProxy newInstance() {
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
	private static DBLargeServiceProxy createInstance(String node, String port, Object id) {
		DBLargeServiceProxy inst = new DBLargeServiceProxy();
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
	
	public void countAndReturn(CallReturn callReturn, String sql, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBLARGESERVICE_COUNTANDRETURN_CALLRETURN_STRING_OBJECTS, new Object[]{ callReturn, sql, params });
	}
	
	public void executeUpdate(String sql, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBLARGESERVICE_EXECUTEUPDATE_STRING_OBJECTS, new Object[]{ sql, params });
	}
	
	public void executeUpdate(boolean needResult, String sql, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBLARGESERVICE_EXECUTEUPDATE_BOOLEAN_STRING_OBJECTS, new Object[]{ needResult, sql, params });
	}
	
	public void findAndReturn(CallReturn callReturn, boolean single, boolean fullQuery, String sql, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBLARGESERVICE_FINDANDRETURN_CALLRETURN_BOOLEAN_BOOLEAN_STRING_OBJECTS, new Object[]{ callReturn, single, fullQuery, sql, params });
	}
}
