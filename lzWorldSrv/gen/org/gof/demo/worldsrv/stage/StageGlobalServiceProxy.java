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
import java.util.List;
import com.google.protobuf.Message;
import org.gof.demo.battlesrv.support.Vector2D;

@GofGenFile
public final class StageGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_APPLYSTAGEBYSN_LONG = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATECOMPETITION_HUMANOBJECT_INT_LONG = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATEINSTANCE_INT_INT = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATETOWER_INT_INT = 4;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_DESTROY_LONG = 5;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_INFOCANCEL_LONG = 6;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_INFOREGISTER_LONG_INT_STRING_STRING_STRING = 7;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_LOGIN_LONG_CALLPOINT_LIST_INT = 8;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_QUITTOCOMMON_HUMANOBJECT_INT_OBJECTS = 9;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_STAGEHUMANNUMADD_LONG = 10;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_STAGEHUMANNUMREDUCE_LONG = 11;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_SWITCHTOSTAGE_HUMANOBJECT_LONG_VECTOR2D = 12;
	}
	private static final String SERV_ID = "stageGlobal";
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private StageGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		StageGlobalService serv = (StageGlobalService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_APPLYSTAGEBYSN_LONG: {
				return (GofFunction1<Long>)serv::applyStageBySn;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATECOMPETITION_HUMANOBJECT_INT_LONG: {
				return (GofFunction3<HumanObject, Integer, Long>)serv::createCompetition;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATEINSTANCE_INT_INT: {
				return (GofFunction2<Integer, Integer>)serv::createInstance;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATETOWER_INT_INT: {
				return (GofFunction2<Integer, Integer>)serv::createTower;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_DESTROY_LONG: {
				return (GofFunction1<Long>)serv::destroy;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_INFOCANCEL_LONG: {
				return (GofFunction1<Long>)serv::infoCancel;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_INFOREGISTER_LONG_INT_STRING_STRING_STRING: {
				return (GofFunction5<Long, Integer, String, String, String>)serv::infoRegister;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_LOGIN_LONG_CALLPOINT_LIST_INT: {
				return (GofFunction4<Long, CallPoint, List, Integer>)serv::login;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_QUITTOCOMMON_HUMANOBJECT_INT_OBJECTS: {
				return (GofFunction3<HumanObject, Integer, Object[]>)serv::quitToCommon;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_STAGEHUMANNUMADD_LONG: {
				return (GofFunction1<Long>)serv::stageHumanNumAdd;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_STAGEHUMANNUMREDUCE_LONG: {
				return (GofFunction1<Long>)serv::stageHumanNumReduce;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_SWITCHTOSTAGE_HUMANOBJECT_LONG_VECTOR2D: {
				return (GofFunction3<HumanObject, Long, Vector2D>)serv::switchToStage;
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
	public static StageGlobalServiceProxy newInstance() {
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
	private static StageGlobalServiceProxy createInstance(String node, String port, Object id) {
		StageGlobalServiceProxy inst = new StageGlobalServiceProxy();
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
	
	public void applyStageBySn(long stageId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_APPLYSTAGEBYSN_LONG, new Object[]{ stageId });
	}
	
	public void createCompetition(HumanObject humanObj, int stageSn, long defenderId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATECOMPETITION_HUMANOBJECT_INT_LONG, new Object[]{ humanObj, stageSn, defenderId });
	}
	
	public void createInstance(int stageSn, int instanceSn) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATEINSTANCE_INT_INT, new Object[]{ stageSn, instanceSn });
	}
	
	public void createTower(int stageSn, int layerSn) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_CREATETOWER_INT_INT, new Object[]{ stageSn, layerSn });
	}
	
	public void destroy(long stageId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_DESTROY_LONG, new Object[]{ stageId });
	}
	
	public void infoCancel(long stageId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_INFOCANCEL_LONG, new Object[]{ stageId });
	}
	
	public void infoRegister(long stageId, int stageSn, String stageName, String nodeId, String portId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_INFOREGISTER_LONG_INT_STRING_STRING_STRING, new Object[]{ stageId, stageSn, stageName, nodeId, portId });
	}
	
	public void login(long humanId, CallPoint connPoint, List lastStageIds, int firstStory) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_LOGIN_LONG_CALLPOINT_LIST_INT, new Object[]{ humanId, connPoint, lastStageIds, firstStory });
	}
	
	public void quitToCommon(HumanObject humanObj, int nowSn, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_QUITTOCOMMON_HUMANOBJECT_INT_OBJECTS, new Object[]{ humanObj, nowSn, params });
	}
	
	public void stageHumanNumAdd(long stageId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_STAGEHUMANNUMADD_LONG, new Object[]{ stageId });
	}
	
	public void stageHumanNumReduce(long stageId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_STAGEHUMANNUMREDUCE_LONG, new Object[]{ stageId });
	}
	
	public void switchToStage(HumanObject humanObj, long stageTargetId, Vector2D posAppear) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGEGLOBALSERVICE_SWITCHTOSTAGE_HUMANOBJECT_LONG_VECTOR2D, new Object[]{ humanObj, stageTargetId, posAppear });
	}
}
