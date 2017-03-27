package org.gof.demo.worldsrv.friend;
                    
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
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.entity.Friend;

@GofGenFile
public final class FriendServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_ADDFRIEND_STRING_LIST = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REFUSEFRIEND_STRING_LONG = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REMOVEBLACKLIST_STRING_LONG = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REMOVEFRIEND_STRING_LONG = 4;
		public static final int ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_TOBLACKLIST_STRING_LONG = 5;
	}
	private static final String SERV_ID = "friend";
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private FriendServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		FriendService serv = (FriendService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_ADDFRIEND_STRING_LIST: {
				return (GofFunction2<String, List>)serv::addFriend;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REFUSEFRIEND_STRING_LONG: {
				return (GofFunction2<String, Long>)serv::refuseFriend;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REMOVEBLACKLIST_STRING_LONG: {
				return (GofFunction2<String, Long>)serv::removeBlackList;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REMOVEFRIEND_STRING_LONG: {
				return (GofFunction2<String, Long>)serv::removeFriend;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_TOBLACKLIST_STRING_LONG: {
				return (GofFunction2<String, Long>)serv::toBlackList;
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
	public static FriendServiceProxy newInstance() {
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
	private static FriendServiceProxy createInstance(String node, String port, Object id) {
		FriendServiceProxy inst = new FriendServiceProxy();
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
	
	public void addFriend(String friendListStr, List humanIds) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_ADDFRIEND_STRING_LIST, new Object[]{ friendListStr, humanIds });
	}
	
	public void refuseFriend(String applyListStr, long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REFUSEFRIEND_STRING_LONG, new Object[]{ applyListStr, humanId });
	}
	
	public void removeBlackList(String blackListStr, long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REMOVEBLACKLIST_STRING_LONG, new Object[]{ blackListStr, humanId });
	}
	
	public void removeFriend(String friendListStr, long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_REMOVEFRIEND_STRING_LONG, new Object[]{ friendListStr, humanId });
	}
	
	public void toBlackList(String blackListStr, long humanId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_FRIEND_FRIENDSERVICE_TOBLACKLIST_STRING_LONG, new Object[]{ blackListStr, humanId });
	}
}
