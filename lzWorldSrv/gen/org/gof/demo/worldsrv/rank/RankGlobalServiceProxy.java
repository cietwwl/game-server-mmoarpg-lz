package org.gof.demo.worldsrv.rank;
                    
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
import org.gof.core.entity.EntityBase;
import java.util.List;
import org.gof.demo.worldsrv.entity.CompetitionHuman;

@GofGenFile
public final class RankGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_ADD_COMPETITIONHUMAN = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_ADDNEW_RANKKEY_ENTITYBASE = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_DELETECOMPETRANKDATA = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETCOMPETRANKINDEX_LONG = 4;
		public static final int ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETINSTANCERANKINDEX_LONG = 5;
		public static final int ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETLEVELRANKINDEX_LONG = 6;
		public static final int ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETRANKLIST_RANKKEY_INT_INT = 7;
		public static final int ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_UPDATEINSTANCERANKLEVEL_LONG_INT = 8;
	}
	private static final String SERV_ID = "rank";
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private RankGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		RankGlobalService serv = (RankGlobalService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_ADD_COMPETITIONHUMAN: {
				return (GofFunction1<CompetitionHuman>)serv::add;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_ADDNEW_RANKKEY_ENTITYBASE: {
				return (GofFunction2<RankKey, EntityBase>)serv::addNew;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_DELETECOMPETRANKDATA: {
				return (GofFunction0)serv::deleteCompetRankData;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETCOMPETRANKINDEX_LONG: {
				return (GofFunction1<Long>)serv::getCompetRankIndex;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETINSTANCERANKINDEX_LONG: {
				return (GofFunction1<Long>)serv::getInstanceRankIndex;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETLEVELRANKINDEX_LONG: {
				return (GofFunction1<Long>)serv::getLevelRankIndex;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETRANKLIST_RANKKEY_INT_INT: {
				return (GofFunction3<RankKey, Integer, Integer>)serv::getRankList;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_UPDATEINSTANCERANKLEVEL_LONG_INT: {
				return (GofFunction2<Long, Integer>)serv::updateInstanceRankLevel;
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
	public static RankGlobalServiceProxy newInstance() {
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
	private static RankGlobalServiceProxy createInstance(String node, String port, Object id) {
		RankGlobalServiceProxy inst = new RankGlobalServiceProxy();
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
	
	public void add(CompetitionHuman human) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_ADD_COMPETITIONHUMAN, new Object[]{ human });
	}
	
	public void addNew(RankKey key, EntityBase entityAdd) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_ADDNEW_RANKKEY_ENTITYBASE, new Object[]{ key, entityAdd });
	}
	
	public void deleteCompetRankData() {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_DELETECOMPETRANKDATA, new Object[]{  });
	}
	
	public void getCompetRankIndex(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETCOMPETRANKINDEX_LONG, new Object[]{ humanId });
	}
	
	public void getInstanceRankIndex(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETINSTANCERANKINDEX_LONG, new Object[]{ humanId });
	}
	
	public void getLevelRankIndex(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETLEVELRANKINDEX_LONG, new Object[]{ humanId });
	}
	
	public void getRankList(RankKey key, int fromIndex, int toIndex) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_GETRANKLIST_RANKKEY_INT_INT, new Object[]{ key, fromIndex, toIndex });
	}
	
	public void updateInstanceRankLevel(long humanId, int level) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_RANK_RANKGLOBALSERVICE_UPDATEINSTANCERANKLEVEL_LONG_INT, new Object[]{ humanId, level });
	}
}
