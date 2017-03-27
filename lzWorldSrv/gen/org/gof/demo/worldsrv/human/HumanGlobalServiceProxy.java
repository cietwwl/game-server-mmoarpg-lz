package org.gof.demo.worldsrv.human;
                    
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
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import java.util.Map;
import org.gof.demo.worldsrv.support.enumKey.HumanScopeKey;
import java.util.List;
import com.google.protobuf.Message;

@GofGenFile
public final class HumanGlobalServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_CHANGECONNPOINT_LONG_CALLPOINT = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_CANCEL_LONG = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFO_LIST = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFO_LONG = 4;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFOS_LIST = 5;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFOSBYMAP_MAP = 6;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETRECOMMENDUSERS_LONG_STRING_STRING_INT = 7;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_ISLOGINED_STRING = 8;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_KICK_LONG_STRING = 9;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_REGISTER_HUMANGLOBALINFO = 10;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDINFORM_HUMANSCOPEKEY_LIST_INT_STRING_LONG = 11;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDMSG_LONG_MESSAGE = 12;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDMSGTOALL_LIST_MESSAGE = 13;
		public static final int ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_STAGEIDMODIFY_LONG_LONG_STRING_STRING_STRING = 14;
	}
	private static final String SERV_ID = "humanGlobal";
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private HumanGlobalServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		HumanGlobalService serv = (HumanGlobalService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_CHANGECONNPOINT_LONG_CALLPOINT: {
				return (GofFunction2<Long, CallPoint>)serv::ChangeConnPoint;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_CANCEL_LONG: {
				return (GofFunction1<Long>)serv::cancel;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFO_LIST: {
				return (GofFunction1<List>)serv::getInfo;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFO_LONG: {
				return (GofFunction1<Long>)serv::getInfo;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFOS_LIST: {
				return (GofFunction1<List>)serv::getInfos;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFOSBYMAP_MAP: {
				return (GofFunction1<Map>)serv::getInfosByMap;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETRECOMMENDUSERS_LONG_STRING_STRING_INT: {
				return (GofFunction4<Long, String, String, Integer>)serv::getRecommendUsers;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_ISLOGINED_STRING: {
				return (GofFunction1<String>)serv::isLogined;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_KICK_LONG_STRING: {
				return (GofFunction2<Long, String>)serv::kick;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_REGISTER_HUMANGLOBALINFO: {
				return (GofFunction1<HumanGlobalInfo>)serv::register;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDINFORM_HUMANSCOPEKEY_LIST_INT_STRING_LONG: {
				return (GofFunction5<HumanScopeKey, List, Integer, String, Long>)serv::sendInform;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDMSG_LONG_MESSAGE: {
				return (GofFunction2<Long, Message>)serv::sendMsg;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDMSGTOALL_LIST_MESSAGE: {
				return (GofFunction2<List, Message>)serv::sendMsgToAll;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_STAGEIDMODIFY_LONG_LONG_STRING_STRING_STRING: {
				return (GofFunction5<Long, Long, String, String, String>)serv::stageIdModify;
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
	public static HumanGlobalServiceProxy newInstance() {
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
	private static HumanGlobalServiceProxy createInstance(String node, String port, Object id) {
		HumanGlobalServiceProxy inst = new HumanGlobalServiceProxy();
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
	
	public void ChangeConnPoint(Long id, CallPoint point) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_CHANGECONNPOINT_LONG_CALLPOINT, new Object[]{ id, point });
	}
	
	public void cancel(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_CANCEL_LONG, new Object[]{ humanId });
	}
	
	public void getInfo(List humansIds) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFO_LIST, new Object[]{ humansIds });
	}
	
	public void getInfo(long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFO_LONG, new Object[]{ humanId });
	}
	
	public void getInfos(List humanIds) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFOS_LIST, new Object[]{ humanIds });
	}
	
	public void getInfosByMap(Map humanIdsMap) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETINFOSBYMAP_MAP, new Object[]{ humanIdsMap });
	}
	
	public void getRecommendUsers(long humanId, String friendListStr, String blackListStr, int combat) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_GETRECOMMENDUSERS_LONG_STRING_STRING_INT, new Object[]{ humanId, friendListStr, blackListStr, combat });
	}
	
	public void isLogined(String account) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_ISLOGINED_STRING, new Object[]{ account });
	}
	
	public void kick(long id, String reason) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_KICK_LONG_STRING, new Object[]{ id, reason });
	}
	
	public void register(HumanGlobalInfo status) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_REGISTER_HUMANGLOBALINFO, new Object[]{ status });
	}
	
	public void sendInform(HumanScopeKey type, List keys, int channel, String content, Long sendHumanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDINFORM_HUMANSCOPEKEY_LIST_INT_STRING_LONG, new Object[]{ type, keys, channel, content, sendHumanId });
	}
	
	public void sendMsg(long humanId, Message msg) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDMSG_LONG_MESSAGE, new Object[]{ humanId, msg });
	}
	
	public void sendMsgToAll(List excludeIds, Message msg) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_SENDMSGTOALL_LIST_MESSAGE, new Object[]{ excludeIds, msg });
	}
	
	public void stageIdModify(long humanId, long stageIdNew, String stageName, String nodeId, String portId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_HUMAN_HUMANGLOBALSERVICE_STAGEIDMODIFY_LONG_LONG_STRING_STRING_STRING, new Object[]{ humanId, stageIdNew, stageName, nodeId, portId });
	}
}
