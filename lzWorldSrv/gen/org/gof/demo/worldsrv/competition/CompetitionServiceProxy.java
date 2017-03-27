package org.gof.demo.worldsrv.competition;
                    
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
import org.gof.demo.worldsrv.entity.Human;
import java.util.List;
import org.gof.demo.worldsrv.entity.CompetitionHuman;
import org.gof.demo.worldsrv.entity.CompetitionMirror;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.entity.Human;

@GofGenFile
public final class CompetitionServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_ADDNEW_HUMAN_COMPETITIONMIRROR_LIST = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETCOMPETRANKNUM = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETCOMPETEINFO_LONG = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETHUMANOBJINFO_LONG = 4;
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETHUMANRANKINFO_LONG = 5;
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETTOWERINFO_LONG = 6;
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_SWAPRANK_LONG_LONG_BOOLEAN = 7;
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_UPDATE_LONG_STRING_LIST = 8;
		public static final int ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_UPDATELEVEL_HUMANOBJECT = 9;
	}
	private static final String SERV_ID = "competition";
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private CompetitionServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		CompetitionService serv = (CompetitionService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_ADDNEW_HUMAN_COMPETITIONMIRROR_LIST: {
				return (GofFunction3<Human, CompetitionMirror, List>)serv::addNew;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETCOMPETRANKNUM: {
				return (GofFunction0)serv::getCompetRankNum;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETCOMPETEINFO_LONG: {
				return (GofFunction1<Long>)serv::getCompeteInfo;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETHUMANOBJINFO_LONG: {
				return (GofFunction1<Long>)serv::getHumanObjInfo;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETHUMANRANKINFO_LONG: {
				return (GofFunction1<Long>)serv::getHumanRankInfo;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETTOWERINFO_LONG: {
				return (GofFunction1<Long>)serv::getTowerInfo;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_SWAPRANK_LONG_LONG_BOOLEAN: {
				return (GofFunction3<Long, Long, Boolean>)serv::swapRank;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_UPDATE_LONG_STRING_LIST: {
				return (GofFunction3<Long, String, List>)serv::update;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_UPDATELEVEL_HUMANOBJECT: {
				return (GofFunction1<HumanObject>)serv::updateLevel;
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
	public static CompetitionServiceProxy newInstance() {
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
	private static CompetitionServiceProxy createInstance(String node, String port, Object id) {
		CompetitionServiceProxy inst = new CompetitionServiceProxy();
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
	
	public void addNew(Human human, CompetitionMirror humanMirror, List mirrorList) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_ADDNEW_HUMAN_COMPETITIONMIRROR_LIST, new Object[]{ human, humanMirror, mirrorList });
	}
	
	public void getCompetRankNum() {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETCOMPETRANKNUM, new Object[]{  });
	}
	
	public void getCompeteInfo(long attackerId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETCOMPETEINFO_LONG, new Object[]{ attackerId });
	}
	
	public void getHumanObjInfo(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETHUMANOBJINFO_LONG, new Object[]{ humanId });
	}
	
	public void getHumanRankInfo(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETHUMANRANKINFO_LONG, new Object[]{ humanId });
	}
	
	public void getTowerInfo(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_GETTOWERINFO_LONG, new Object[]{ humanId });
	}
	
	public void swapRank(long attackerId, long defenderId, boolean win) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_SWAPRANK_LONG_LONG_BOOLEAN, new Object[]{ attackerId, defenderId, win });
	}
	
	public void update(long humanId, String prop, List mirrorList) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_UPDATE_LONG_STRING_LIST, new Object[]{ humanId, prop, mirrorList });
	}
	
	public void updateLevel(HumanObject humanObj) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_COMPETITION_COMPETITIONSERVICE_UPDATELEVEL_HUMANOBJECT, new Object[]{ humanObj });
	}
}
