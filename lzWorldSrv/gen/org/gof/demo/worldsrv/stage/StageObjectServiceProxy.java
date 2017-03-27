package org.gof.demo.worldsrv.stage;
                    
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
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.battlesrv.support.Vector2D;
import java.util.Map;
import com.google.protobuf.Message;

@GofGenFile
public final class StageObjectServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEOBJECTSERVICE_LOGIN_LONG_CALLPOINT_LONG_VECTOR2D = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEOBJECTSERVICE_REGISTER_HUMANOBJECT = 2;
	}
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private StageObjectServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		StageObjectService serv = (StageObjectService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEOBJECTSERVICE_LOGIN_LONG_CALLPOINT_LONG_VECTOR2D: {
				return (GofFunction4<Long, CallPoint, Long, Vector2D>)serv::login;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEOBJECTSERVICE_REGISTER_HUMANOBJECT: {
				return (GofFunction1<HumanObject>)serv::register;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static StageObjectServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static StageObjectServiceProxy newInstance(String node, String port, Object id) {
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
	private static StageObjectServiceProxy createInstance(String node, String port, Object id) {
		StageObjectServiceProxy inst = new StageObjectServiceProxy();
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
	
	public void login(long humanId, CallPoint connPoint, long stageId, Vector2D stagePos) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEOBJECTSERVICE_LOGIN_LONG_CALLPOINT_LONG_VECTOR2D, new Object[]{ humanId, connPoint, stageId, stagePos });
	}
	
	public void register(HumanObject humanObj) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEOBJECTSERVICE_REGISTER_HUMANOBJECT, new Object[]{ humanObj });
	}
}
