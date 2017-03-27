package org.gof.demo.worldsrv.character;
                    
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
import org.gof.demo.worldsrv.entity.Mail;
import org.gof.demo.worldsrv.entity.Friend;
import org.gof.demo.worldsrv.friend.FriendObject;

@GofGenFile
public final class HumanObjectServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CHANGECONNPOINT_CALLPOINT = 1;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ADDFRIEND_FRIENDOBJECT = 2;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ADDFRIENDAPPLY_FRIENDOBJECT = 3;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CONNCHECK_LONG = 4;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CONNCLOSED_LONG = 5;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_GETHUMAN = 6;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_KICKCLOSED = 7;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_LEAVE = 8;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_MAILACCEPT_MAIL = 9;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_MSGHANDLER_LONG_BYTES = 10;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ONSCHEDULE_INT_LONG = 11;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_REMOVEFRIEND_LONG = 12;
		public static final int ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_UPDATEFRIENDONLINE_LONG_BOOLEAN = 13;
	}
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private HumanObjectServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		HumanObjectService serv = (HumanObjectService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CHANGECONNPOINT_CALLPOINT: {
				return (GofFunction1<CallPoint>)serv::ChangeConnPoint;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ADDFRIEND_FRIENDOBJECT: {
				return (GofFunction1<FriendObject>)serv::addFriend;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ADDFRIENDAPPLY_FRIENDOBJECT: {
				return (GofFunction1<FriendObject>)serv::addFriendApply;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CONNCHECK_LONG: {
				return (GofFunction1<Long>)serv::connCheck;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CONNCLOSED_LONG: {
				return (GofFunction1<Long>)serv::connClosed;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_GETHUMAN: {
				return (GofFunction0)serv::getHuman;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_KICKCLOSED: {
				return (GofFunction0)serv::kickClosed;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_LEAVE: {
				return (GofFunction0)serv::leave;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_MAILACCEPT_MAIL: {
				return (GofFunction1<Mail>)serv::mailAccept;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_MSGHANDLER_LONG_BYTES: {
				return (GofFunction2<Long, byte[]>)serv::msgHandler;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ONSCHEDULE_INT_LONG: {
				return (GofFunction2<Integer, Long>)serv::onSchedule;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_REMOVEFRIEND_LONG: {
				return (GofFunction1<Long>)serv::removeFriend;
			}
			case EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_UPDATEFRIENDONLINE_LONG_BOOLEAN: {
				return (GofFunction2<Long, Boolean>)serv::updateFriendOnline;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static HumanObjectServiceProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static HumanObjectServiceProxy newInstance(String node, String port, Object id) {
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
	private static HumanObjectServiceProxy createInstance(String node, String port, Object id) {
		HumanObjectServiceProxy inst = new HumanObjectServiceProxy();
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
	
	public void ChangeConnPoint(CallPoint point) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CHANGECONNPOINT_CALLPOINT, new Object[]{ point });
	}
	
	public void addFriend(FriendObject friendObj) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ADDFRIEND_FRIENDOBJECT, new Object[]{ friendObj });
	}
	
	public void addFriendApply(FriendObject friendObj) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ADDFRIENDAPPLY_FRIENDOBJECT, new Object[]{ friendObj });
	}
	
	public void connCheck(long connId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CONNCHECK_LONG, new Object[]{ connId });
	}
	
	public void connClosed(long connId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_CONNCLOSED_LONG, new Object[]{ connId });
	}
	
	public void getHuman() {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_GETHUMAN, new Object[]{  });
	}
	
	public void kickClosed() {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_KICKCLOSED, new Object[]{  });
	}
	
	public void leave() {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_LEAVE, new Object[]{  });
	}
	
	public void mailAccept(Mail mail) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_MAILACCEPT_MAIL, new Object[]{ mail });
	}
	
	public void msgHandler(long connId, byte... chunk) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_MSGHANDLER_LONG_BYTES, new Object[]{ connId, chunk });
	}
	
	public void onSchedule(int key, long timeLast) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_ONSCHEDULE_INT_LONG, new Object[]{ key, timeLast });
	}
	
	public void removeFriend(long removerId) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_REMOVEFRIEND_LONG, new Object[]{ removerId });
	}
	
	public void updateFriendOnline(long humanId, boolean online) {
		localPort.call(remote, EnumCall.ORG_GOF_DEMO_WORLDSRV_CHARACTER_HUMANOBJECTSERVICE_UPDATEFRIENDONLINE_LONG_BOOLEAN, new Object[]{ humanId, online });
	}
}
