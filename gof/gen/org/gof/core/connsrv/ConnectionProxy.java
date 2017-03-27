package org.gof.core.connsrv;
                    
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
import org.gof.core.Chunk;
import org.gof.core.support.ConnectionStatus;

@GofGenFile
public final class ConnectionProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_CLOSE = 1;
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_GETMSGBUF = 2;
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_INITMSGBUF_CALLPOINT = 3;
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSG_LIST_LIST = 4;
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSG_INT_CHUNK = 5;
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSGBUF = 6;
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_SETSTATUS_INT = 7;
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_UPDATESTATUS_CONNECTIONSTATUS = 8;
		public static final int ORG_GOF_CORE_CONNSRV_CONNECTION_UPDATESTATUS_STRING_STRING_LONG = 9;
	}
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private ConnectionProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		Connection serv = (Connection)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_CLOSE: {
				return (GofFunction0)serv::close;
			}
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_GETMSGBUF: {
				return (GofFunction0)serv::getMsgBuf;
			}
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_INITMSGBUF_CALLPOINT: {
				return (GofFunction1<CallPoint>)serv::initMsgBuf;
			}
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSG_LIST_LIST: {
				return (GofFunction2<List, List>)serv::sendMsg;
			}
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSG_INT_CHUNK: {
				return (GofFunction2<Integer, Chunk>)serv::sendMsg;
			}
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSGBUF: {
				return (GofFunction0)serv::sendMsgBuf;
			}
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_SETSTATUS_INT: {
				return (GofFunction1<Integer>)serv::setStatus;
			}
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_UPDATESTATUS_CONNECTIONSTATUS: {
				return (GofFunction1<ConnectionStatus>)serv::updateStatus;
			}
			case EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_UPDATESTATUS_STRING_STRING_LONG: {
				return (GofFunction3<String, String, Long>)serv::updateStatus;
			}
			default: break;
		}
		return null;
	}
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ConnectionProxy newInstance(CallPoint targetPoint) {
		return createInstance(targetPoint.nodeId, targetPoint.portId, targetPoint.servId);
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ConnectionProxy newInstance(String node, String port, Object id) {
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
	private static ConnectionProxy createInstance(String node, String port, Object id) {
		ConnectionProxy inst = new ConnectionProxy();
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
	
	public void close() {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_CLOSE, new Object[]{  });
	}
	
	public void getMsgBuf() {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_GETMSGBUF, new Object[]{  });
	}
	
	public void initMsgBuf(CallPoint connPoint) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_INITMSGBUF_CALLPOINT, new Object[]{ connPoint });
	}
	
	public void sendMsg(List idList, List chunkList) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSG_LIST_LIST, new Object[]{ idList, chunkList });
	}
	
	public void sendMsg(int msgId, Chunk msgbuf) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSG_INT_CHUNK, new Object[]{ msgId, msgbuf });
	}
	
	public void sendMsgBuf() {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_SENDMSGBUF, new Object[]{  });
	}
	
	public void setStatus(int status) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_SETSTATUS_INT, new Object[]{ status });
	}
	
	public void updateStatus(ConnectionStatus status) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_UPDATESTATUS_CONNECTIONSTATUS, new Object[]{ status });
	}
	
	public void updateStatus(String node, String port, long stage) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_CONNSRV_CONNECTION_UPDATESTATUS_STRING_STRING_LONG, new Object[]{ node, port, stage });
	}
}
