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

@GofGenFile
public final class StageServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGECOMMON_INT_INT = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGECOMPETITION_HUMANOBJECT_INT_LONG = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGEINSTANCE_INT_INT = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGETOWER_INT_INT = 4;
		public static final int ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_DESTROY_LONG = 5;
	}
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private StageServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		StageService serv = (StageService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGECOMMON_INT_INT: {
				return (GofFunction2<Integer, Integer>)serv::createStageCommon;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGECOMPETITION_HUMANOBJECT_INT_LONG: {
				return (GofFunction3<HumanObject, Integer, Long>)serv::createStageCompetition;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGEINSTANCE_INT_INT: {
				return (GofFunction2<Integer, Integer>)serv::createStageInstance;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGETOWER_INT_INT: {
				return (GofFunction2<Integer, Integer>)serv::createStageTower;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_DESTROY_LONG: {
				return (GofFunction1<Long>)serv::destroy;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static StageServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static StageServiceProxy newInstance(String node, String port, Object id) {
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
	private static StageServiceProxy createInstance(String node, String port, Object id) {
		StageServiceProxy inst = new StageServiceProxy();
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
	
	public void createStageCommon(int stageSn, int lineNum) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGECOMMON_INT_INT, new Object[]{ stageSn, lineNum });
	}
	
	public void createStageCompetition(HumanObject humanObj, int stageSn, long defenderId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGECOMPETITION_HUMANOBJECT_INT_LONG, new Object[]{ humanObj, stageSn, defenderId });
	}
	
	public void createStageInstance(int stageSn, int repSn) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGEINSTANCE_INT_INT, new Object[]{ stageSn, repSn });
	}
	
	public void createStageTower(int stageSn, int towerLayer) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_CREATESTAGETOWER_INT_INT, new Object[]{ stageSn, towerLayer });
	}
	
	public void destroy(long gameId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_STAGE_STAGESERVICE_DESTROY_LONG, new Object[]{ gameId });
	}
}
