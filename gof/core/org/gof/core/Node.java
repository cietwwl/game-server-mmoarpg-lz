package org.gof.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.interfaces.IThreadCase;
import org.gof.core.support.BufferPool;
import org.gof.core.support.CallSeamMethodKey;
import org.gof.core.support.Distr;
import org.gof.core.support.RandomUtils;
import org.gof.core.support.SeamServiceBase;
import org.gof.core.support.ThreadHandler;
import org.gof.core.support.TickTimer;
import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public final class Node implements IThreadCase {
	//日志远程访问
	private final Logger logRemote = LogCore.remote;
	private final ThreadHandler thread;				//线程管理类
	
	private final String id;							//Node名称
	private final String addr;							//Node地址
	private final ConcurrentMap<String, Port> ports = new ConcurrentHashMap<>();				//下属Port
	private final ConcurrentMap<String, RemoteNode> remoteNodes = new ConcurrentHashMap<>();	//连接的远程Node

	private final ZContext zmqContext;					//ZMQ上下文
	private final ZMQ.Socket zmqPull;					//ZMQ连接

	private long timeCurrent;							//当前时间戳
	private volatile boolean start;					//是否开启
	private final TickTimer remoteNodePulseTimer = new TickTimer(RemoteNode.INTERVAL_PING);	//远程Node调用定时器
	
	/**
	 * 构造函数
	 * @param id
	 * @param addr
	 */
	public Node(String id, String addr) {
		this.id = id;
		this.addr = addr;

		this.zmqContext = new ZContext();
		this.zmqContext.setIoThreads(1);
		
		this.zmqPull = zmqContext.createSocket(ZMQ.PULL);
		this.zmqPull.setLinger(3000);
		this.zmqPull.bind(addr);

		this.start = true;
		
		thread = new ThreadHandler(this);
	}

	/**
	 * 开始运行
	 */
	public void startup() {
		thread.setName(toString());
		thread.startup();
	}
	
	/**
	 * 暂停当前node，暂停node下的所有的Port
	 * 此方法只有在ClassLoader的时候才可以调用！！
	 * @throws InterruptedException 
	 */
	@Deprecated
	public void pause() {
		if (!start) return;

		//清理下属port
		for(Port p : ports.values()) {
			p.pause();
		}
	}
	
	/**
	 * 恢复当前node，暂恢复node下的所有的Port
	 * 此方法只有在ClassLoader的时候才可以调用！！
	 * @throws InterruptedException 
	 */
	@Deprecated
	public void resume() {
		if (!start) return;

		//清理下属port
		for(Port p : ports.values()) {
			p.resume();
		}
	}
	
	/**
	 * 关闭
	 */
	public void stop() {
		if (!start) return;

		//设置为关闭状态
		start = false;

		//清理下属port
		for(Port p : ports.values()) {
			p.stop();
		}
		
		//清理远程Node
		for(RemoteNode r : remoteNodes.values()) {
			r.close();
		}

		//关闭ZMQ
		zmqPull.close();
		zmqContext.destroy();
	}
	
	/**
	 * 心跳操作
	 */
	public void pulse() {
		if (!start) return;
		
		//当前时间
		timeCurrent = System.currentTimeMillis();
		
		//接受其他Node发送过来的Call调用
		pulseCallPuller();
		//调用远程Node的心跳操作
		pulseRemoteNodes();
	}
	
	/**
	 * 接受其他Node发送过来的Call请求
	 */
	private void pulseCallPuller() {
		while (true) {
			//可重用buff
			byte[] buf = null;
			try {
				//申请buff
				buf = BufferPool.allocate();
				
				//接受到的字节流长度
				//zmq是基于块传输的 所以不用考虑流切割的问题
				int recvLen = zmqPull.recv(buf, 0, buf.length, ZMQ.DONTWAIT);
				//如果长度<=0 代表没有接到数据 本心跳接收任务结束
				if (recvLen <= 0) {
					break;
				}
				
				//处理Call请求
				callHandle(buf, recvLen);
			} catch(Exception e) {
				//吞掉并打印异常
				LogCore.core.error("", e);
			} finally {
				//回收buff
				BufferPool.deallocate(buf);
			}
		}
	}

	/**
	 * 调用远程Node的心跳操作
	 */
	private void pulseRemoteNodes() {
		//检查时间间隔
		if (!remoteNodePulseTimer.isPeriod(timeCurrent)) {
			return;
		}
		
		//遍历远程Node
		for(RemoteNode r : remoteNodes.values()) {
			r.pulse();
		}
	}

	/**
	 * 发送请求
	 * @param nodeId
	 * @param buffer
	 * @param bufferLength
	 */
	public void sendCall(String nodeId, byte[] buffer, int bufferLength) {
		//同一Node下 无需走传输协议 内部直接接收即可
		if (id.equals(nodeId)) {
			callHandle(buffer, bufferLength);
		} else {	//其余的需要通过远程Node来发送请求值目标Node
			RemoteNode node = remoteNodes.get(nodeId);
			if(node != null) {
				node.sendCall(buffer, bufferLength);
			} else {
				logRemote.error("发送Call请求时，发现未知远程节点: nodeId={}", nodeId);
			}
		}
	}

	/**
	 * 发送请求
	 * @param nodeId
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
			sendCall(call.to.nodeId, out.getBuffer(), out.getLength());
		} finally {
			//关闭回收
			out.close();
		}
	}

	/**
	 * 返回一个随机的下属port
	 * 连接服务器随机分配玩家时会用到
	 * @param max
	 * @return
	 */
	public Port getRandomPort() {
		int index = RandomUtils.nextInt(ports.size());
		return getPort(Integer.toString(index));
	}

	/**
	 * 通过名称来获取Port
	 * @param portId
	 * @return
	 */
	public Port getPort(String portId) {
		return ports.get(portId);
	}

	/**
	 * 添加Port
	 * @param port
	 */
	public void addPort(Port port) {
		ports.put(port.getId(), port);
	}

	/**
	 * 删除Port
	 * @param port
	 */
	public void delPort(Port port) {
		ports.remove(port.getId());
	}

	/**
	 * 添加远程Node
	 * @param name
	 * @param addr
	 */
	public RemoteNode addRemoteNode(String name, String addr) {
		//创建远程Node并与本Node相连
		RemoteNode remote = new RemoteNode(this, name, addr);
		remoteNodes.put(name, remote);
		
		//发送心跳检测
		remote.ping();
		
		return remote;
	}

	/**
	 * 删除远程Node
	 * @param name
	 */
	public void delRemoteNode(String name) {
		RemoteNode node = remoteNodes.remove(name);
		if (node != null) {
			node.close();
		}
	}
	
	/**
	 * 远程Node是否已连接
	 * @param remoteNodeName
	 * @return
	 */
	public boolean isRemoteNodeConnected(String remoteNodeName) {
		RemoteNode n = remoteNodes.get(remoteNodeName);
		if (n == null) return false;
		
		return n.isActive();
	}

	/**
	 * 处理接收到的Call请求
	 * @param buf
	 * @param len
	 */
	private void callHandle(byte[] buf, int len) {
		//转化为输出流
		InputStream input = new InputStream(buf, 0, len);
		//是否已读取到末尾
		while(!input.isAtEnd()) {
			//先读取一个Call请求
			Call call = input.read();

			//根据请求类型来分别处理
			switch (call.type) {
				//PRC远程调用请求
				case Call.TYPE_RPC: {
					//日志记录
//					if(logRemote.isDebugEnabled()) {
//						logRemote.debug("接收到RPC请求：call={}", call);
//					}
					
					//请求分发
					Port port = ports.get(call.to.portId);
					if (port != null) {
						port.addCall(call);
					} else {
						if(logRemote.isInfoEnabled()) {
							logRemote.info("接收到RPC请求后，未能找到合适的接收者：call={}", call);
						}
					}
				}
				break;
				
				//PRC远程调用请求的返回值
				case Call.TYPE_RPC_RETURN: {
					//日志记录
//					if(logRemote.isDebugEnabled()) {
//						logRemote.debug("接收到RPC返回结果：call={}", call);
//					}
					
					//请求分发
					Port port = ports.get(call.to.portId);
					if (port != null) {
						port.addCallResult(call);
					} else {
						if(logRemote.isInfoEnabled()) {
							logRemote.info("接收到RPC返回值后，未能找到合适的接收者：call={}", call);
						}
					}
				}
				break;
				
				//系统整合调用
				case Call.TYPE_MIX: {
					//日志记录
//					if(logRemote.isDebugEnabled()) {
//						logRemote.debug("接收到MSG消息：call={}", call);
//					}
					
					//请求分发
					Port port = ports.get(Distr.PORT_DEFAULT);
					if (port != null) {
						//整合函数类型
						CallSeamMethodKey methodKey = call.param.get("method");
						
						SeamServiceBase serv = port.getService(Distr.SERV_SEAM);
						serv.handler(methodKey, call);
					} else {
						if(logRemote.isInfoEnabled()) {
							logRemote.info("接收到MSG消息，未能找到合适的接收者：call={}", call);
						}
					}
				}
				break;
				
				//连接检测
				case Call.TYPE_PING: {
					//根据请求者的名称来获取远程Node
					RemoteNode node = remoteNodes.get(call.fromNodeId);
					
					//第一次收到连接检测 反向增加一个对方的远程Node
					if(node == null) {
						String name = (String) call.methodParam[0];
						String addr = (String) call.methodParam[1];
						
						node = addRemoteNode(name, addr);
					}
					
					//处理连接检测请求
					node.pingHandle();
				}
				break;
			}
		}
	}
	
	public String getId() {
		return id;
	}

	public String getAddr() {
		return addr;
	}

	public long getTimeCurrent() {
		return timeCurrent;
	}

	@Override
	public void caseRunOnce() {
		pulse();
	}
	
	@Override
	public void caseStart() {
		
	}

	@Override
	public void caseStop() {
		
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", getId())
				.append("addr", getAddr())
				.toString();
	}
}