package org.gof.core;

import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class RemoteNode {
	public static final long INTERVAL_PING = 3000;		//连接检测时间间隔 3秒
	public static final long INTERVAL_LOST = 8000;		//连接丢失时间间隔 8秒
	
	//日志远程访问
	private final Logger logRemote = LogCore.remote;
		
	private final String remoteId;				//远程Node名称
	private final String remoteAddr;			//远程Node地址
	private final Node localNode;				//本地Node名称
	
	private final ZContext zmqContext;			//ZMQ上下文
	private final ZMQ.Socket zmqPush;			//ZMQ连接
	
	private long lastRecvPingTime = 0;			//最后一次收到连接检查时间
	
	private boolean connected;					//是否连接上
	
	/**
	 * 构造函数
	 * @param localNode
	 * @param remoteName
	 * @param remoteAddr
	 */
	public RemoteNode(Node localNode, String remoteName, String remoteAddr) {
		this.localNode = localNode;
		this.remoteId = remoteName;
		this.remoteAddr = remoteAddr;
		
		this.zmqContext = new ZContext();
		this.zmqContext.setIoThreads(1);
		
		this.zmqPush = zmqContext.createSocket(ZMQ.PUSH);
		this.zmqPush.setLinger(3000);
		this.zmqPush.setSndHWM(10000);		//default 1000 messages
		this.zmqPush.setReconnectIVL(2000);
		this.zmqPush.setReconnectIVLMax(5000);
		this.zmqPush.connect(remoteAddr);
	}
	
	/**
	 * 心跳操作
	 */
	public void pulse() {
		//当前时间
		long timeCurr = localNode.getTimeCurrent();
		
		//到达间隔时间后 进行连接检测
		ping();
		
		//活跃状态下 长时间没收到心跳检测 那么就认为连接已丢失
		if(isActive() && (timeCurr - lastRecvPingTime) > INTERVAL_LOST) {
			connected = false;
			logRemote.error("失去与远程Node的连接：name={}, addr={}", remoteId, remoteAddr);
		}
	}
	
	/**
	 * 进行连接测试
	 */
	public void ping() {
		//创建并发送测试请求
		Call call = new Call();
		call.type = Call.TYPE_PING;
		call.fromNodeId = localNode.getId();
		call.to.nodeId = remoteId;
		call.methodParam = new Object[] { localNode.getId(), localNode.getAddr()};
		
		sendCall(call);
	}
	
	/**
	 * 处理连接测试请求
	 */
	public void pingHandle() {
		//非活跃的情况下收到连接测试
		if(!isActive()) {
			logRemote.info("远程Node激活：id={}, addr={}", remoteId, remoteAddr);
		}
		
		//设置最后收到连接测试的时间
		lastRecvPingTime = localNode.getTimeCurrent();

		//设置为已连接状态
		if(!connected) {
			connected = true;
		}
	}
	
	/**
	 * 是否为活跃状态
	 * @return
	 */
	public boolean isActive() {
		return connected && lastRecvPingTime > 0;
	}
	
	/**
	 * 关闭
	 */
	public void close() {
		zmqPush.close();
		zmqContext.destroy();
	}
	
	/**
	 * 发送调用请求
	 * @param call
	 */
	public void sendCall(Call call) {
		//输出流
		OutputStream out = null;
		try {
			//创建输出流并写入
			out = new OutputStream();
			out.write(call);
			
			//发送消息
			sendCall(out.getBuffer(), out.getLength());
		} finally {
			//关闭回收
			out.close();
		}
	}
	
	/**
	 * 发送调用请求
	 * zmq内部不是线程安全的，必须做同步发送。
	 * @param call
	 */
	public synchronized void sendCall(byte[] buf, int size) {
		zmqPush.send(buf, 0, size, 0);
	}
}
