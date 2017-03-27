package org.gof.core.connsrv;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.gof.core.CallPoint;
import org.gof.core.Chunk;
import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.Service;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.scheduler.ScheduleTask;
import org.gof.core.support.CallSeamMethodKey;
import org.gof.core.support.ConnectionStatus;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;
import org.gof.core.support.Sys;
import org.gof.core.support.SysException;
import org.gof.core.support.TickTimer;
import org.gof.core.support.Time;
import org.gof.core.support.Utils;
import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;

@DistrClass(
	importClass = {Chunk.class, ConnectionStatus.class}
)
public class Connection extends Service {
	protected static int connNum = 0; //连接数
	protected long m_id = 0;
	protected ConnectionStatus m_status = new ConnectionStatus();
	protected boolean m_closed = false;
	protected boolean m_delayCloseRequested = false;
	protected boolean m_waitResponse = false;
	protected TickTimer m_waitTimer = new TickTimer();
	protected TickTimer m_closeTimer = new TickTimer();
	
	protected final Channel channel;
	protected final LinkedBlockingQueue<byte[]> datas = new LinkedBlockingQueue<>();
	
	private final Logger log = LogCore.conn;
	
	/* 连接验证 */
	public static long CONN_CHECK_TIMES = 3;		//连接检查超时次数
	public TickTimer connCheckTickTimer = new TickTimer(5 * Time.SEC);	//连接检查计时器
	public int connCheckIncreaseTimes = 0;				//累计连续未收到返回的次数
	
	//消息缓存队列
	ConnectionBuf conBuf = new ConnectionBuf();
	
	
	static {
		//Window下认为是开发环境 避免Debug造成的超时断开连接 这里加大检查次数
		if(Sys.isWin()) {
			CONN_CHECK_TIMES = Long.MAX_VALUE;
		}
	}
	
	/**
	 * FIXME 获取连接ID Entty 4.1恢复channel.id()函数前 先用这个暂代
	 * @param channel
	 * @return
	 */
	public static int getId(Channel channel) {
		return channel.hashCode();
	}
	
	public void putDate(byte[] data) {
		try {
			datas.put(data);
		} catch (InterruptedException e) {
			throw new SysException(e);
		}
	}
	
	public Connection(Channel channel, Port port) {
//		m_port = ConnStartup.CONN_NODE.getRandomPort();
		super(port);
		m_id = getId(channel);
		this.channel = channel;
	}
	
	@DistrMethod
	public void updateStatus(String node, String port, long stage) {
		m_status.stageNodeId = node;
		m_status.stagePortId = port;
	}
	
	@DistrMethod
	public void updateStatus(ConnectionStatus status) {
		this.handleContinue(status);
	}
	
	@DistrMethod
	public void setStatus(int status) {
		m_status.status = status;
		this.handleContinue(m_status);
	}
	
	@DistrMethod
	public void close() {
		//延迟100毫秒再真正断开 有些消息可能还没传递完毕
		scheduleOnce(new ScheduleTask() {
			@Override
			public void execute() {
				channel.close();
			}
		}, 100);
	}
	
	@DistrMethod
	public void sendMsg(int msgId, Chunk msgbuf) {
		conBuf.addMsg(msgId, msgbuf);
		
		if(!channel.isActive()) return;
		if(!channel.isWritable()) return;

		//构造头文件数据
		ByteBuf head = channel.alloc().buffer(8);
		head.writeInt(msgbuf.length + 8);
		head.writeInt(msgId);
		
		//写入头数据
		channel.write(head);
		//Chunk类型的msgbuf肯定是protobuf直接生成的 所以buffer属性中不会有多余数据 才能这么用
		//其余地方Chunk类不建议直接使用内部的buffer
		ChannelFuture f = channel.write(msgbuf.buffer);
		f.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				conBuf.removeMsg();
//				log.info("conBuf msgId {} conBuf.idBufList.size() {} conBuf.size() {} msgbuf.length {}", 
//						msgId, conBuf.idBufList.size(), conBuf.size(), msgbuf.length);
			}
		});
		channel.flush();
		
		//记录日志
//		if (log.isDebugEnabled() && msgId != 1212) {
//			log.debug("发送消息至客户端：account={}, connId={}, msgId={}", m_status.account, m_id, msgId);
//		}
	}
	
	public void sendBytesWithLength(byte[] buffer){
		if(!channel.isOpen()) return;
		ByteBuf head = channel.alloc().buffer(4);
		head.writeInt(buffer.length + 4);
		channel.write(head);
		channel.write(buffer);
		channel.flush();
	}
	
	@DistrMethod
	public void sendMsg(List<Integer> idList, List<Chunk> chunkList) {
		if(!channel.isOpen()) return;
		
		for(int i = 0 ; i < idList.size() ; i++) {
			int msgId = idList.get(i);
			Chunk msgbuf = chunkList.get(i);
			//构造头文件数据
			ByteBuf head = channel.alloc().buffer(8);
			head.writeInt(msgbuf.length + 8);
			head.writeInt(msgId);
			
			//写入头数据
			channel.write(head);
			//Chunk类型的msgbuf肯定是protobuf直接生成的 所以buffer属性中不会有多余数据 才能这么用
			//其余地方Chunk类不建议直接使用内部的buffer
			channel.write(msgbuf.buffer);
		}
		channel.flush();
		
	}
	/**
	 * 初始化消息缓存 只有在掉线重连的时候才用的上
	 * @param connPoint
	 */
	@DistrMethod
	public void initMsgBuf(CallPoint connPoint) {
		ConnectionProxy connPrx = ConnectionProxy.newInstance(connPoint);
//		connPrx.getMsgBuf();
		connPrx.listenResult(this::_result_InitMsgBuf);
	}
	public void _result_InitMsgBuf(Param results, Param context) {
		ConnectionBuf conBuf = results.get();
		this.conBuf = conBuf;
	}
	
	@DistrMethod
	public void getMsgBuf() {
		port.returns(conBuf);
	}
	
	/**
	 * 发送消息缓存的数据 只有在消息重新连接的时候才用的上
	 * @param connPoint
	 */
	@DistrMethod
	public void sendMsgBuf() {
		//获得对应connect的conBuf信息
		conBuf.sendMsg(channel);
	}
	/**
	 * 延时关闭
	 * 2秒后生效
	 */
	public void closeDelay() {
		if(!this.m_closed && !m_delayCloseRequested) {
			m_delayCloseRequested = true;
			m_closeTimer.start(300);
		}
	}
	
	@Override
	public Object getId() {
		return m_id;
	}
	
	public void startup() {
		super.startup();
		port.addQueue(new PortPulseQueue(this) {
			
			@Override
			public void execute(Port port) {
				ConnPort portConn = (ConnPort)port;
				portConn.openConnection(param.<Connection>get());
			}
		});
	}

	@Override
	public void pulseOverride() {
		//如果延时关闭已经到时间 那么就进行关闭
		if(m_delayCloseRequested && m_closeTimer.isOnce(port.getTimeCurrent())) {
			//清理延时关闭状态 避免再次触发
			m_delayCloseRequested = false;
			
			//进行关闭处理
			handleClose();
		}
		
		//如果计时器超时 那么就进行关闭连接操作
		if(m_waitTimer.isStarted() && m_waitTimer.isOnce(port.getTimeCurrent())) {
			//日志
			log.warn("登陆阶段超时，主动关闭连接：id={}", m_id);
			
			//关闭连接
			channel.close();
		}
		
		//连接验证
		connCheck();
	}

	/**
	 * 连接关闭
	 * 同时其他服务器进行信息清理
	 */
	public void handleClose() {
		m_closed = true;
		
		//日志
//		if(log.isDebugEnabled()) {
//			log.debug("进行连接关闭的清理工作：connId={}, status={}", m_id, m_status.status);
//		}

		switch(m_status.status) {
			case ConnectionStatus.STATUS_LOGIN: {
//				if(log.isInfoEnabled()) {
//					log.info("关闭验证阶段的连接：connId={}", m_id);
//				}
			}
			break;
			case ConnectionStatus.STATUS_GATE: {
				CallPoint toPoint = new CallPoint(Distr.NODE_DEFAULT, Distr.PORT_DEFAULT, Distr.SERV_GATE);
				port.callSeam(toPoint, new Object[] {m_id}, "method", CallSeamMethodKey.ACCOUNT_LOST);
			}
			break;
			case ConnectionStatus.STATUS_PLAYING: {
				CallPoint toPoint = new CallPoint(m_status.stageNodeId, m_status.stagePortId, m_status.humanId);
				port.callSeam(toPoint, new Object[] {m_id}, "method", CallSeamMethodKey.WORLD_LOST);
			}
			break;
			case ConnectionStatus.STATUS_LOSTED: {
//				if(log.isInfoEnabled()) {
//					log.info("关闭断开阶段的连接：connId={}", m_id);
//				}
			}
			break;
		}
		
		port.addQueue(new PortPulseQueue(this) {
			@Override
			public void execute(Port port) {
				ConnPort portConn = (ConnPort)port;
				portConn.closeConnection(param.<Connection>get());
			}
		});
	}
	
	protected void handleContinue(ConnectionStatus status) {
		
		if(status.status == ConnectionStatus.STATUS_LOSTED) {
			this.close();
		} else {
			m_status = status;
		}
		m_waitResponse = false;
		m_waitTimer.stop();
	}

	public void handleInput() {		
		//启动定时器
		if(!datas.isEmpty()) {
			
		}
		
		while(!datas.isEmpty()) {
			try {
				handleIncoming(datas.poll());
			} catch (Exception e) {
				// 不做任何处理 仅仅抛出异常
				// 避免因为一个任务的出错 造成后续的任务无法继续执行 需要等到下一个心跳
				log.error("", e);
			}
		}
	}
	
	/**
	 * 消息接受
	 * @param msgbuf
	 */
	private void handleIncoming(byte[] msgbuf) {
		int mid = Utils.bytesToInt(msgbuf, 4);
		
		//记录日志
		if(log.isDebugEnabled() && mid != 1211 && mid != 1212) {
			log.debug("收到客户端消息：account={}, connId={}, msgId={}", m_status.account, m_id, mid);
		}
		
		//关闭或关闭中的连接 不在接收新的客户端请求
		if(m_closed || m_delayCloseRequested) {
			if(log.isDebugEnabled() && mid != 1211 && mid != 1212) {
				log.debug("连接关闭中，忽略收到的客户端消息：id={}", mid);
			}
		}

		switch(m_status.status) {
			case ConnectionStatus.STATUS_LOGIN:
			case ConnectionStatus.STATUS_GATE: {
				CallPoint toPoint = new CallPoint(Distr.NODE_DEFAULT, Distr.PORT_DEFAULT, Distr.SERV_GATE);
				port.callSeam(toPoint, new Object[] {m_id, m_status, msgbuf}, "method", CallSeamMethodKey.ACCOUNT_MSG);
				
				m_waitResponse = true;
				m_waitTimer.start(150 * Time.SEC);
				
				//记录日志
//				if(log.isDebugEnabled()) {
//					log.debug("将客户端消息转发给账号服务：account={}, connId={}, msgId={}", m_status.account, m_id, mid);
//				}
			}
			break;
			case ConnectionStatus.STATUS_PLAYING: {
				CallPoint toPoint = new CallPoint(m_status.stageNodeId, m_status.stagePortId, m_status.humanId);
				port.callSeam(toPoint, new Object[] {m_id, msgbuf}, "method", CallSeamMethodKey.WORLD_MSG);
				
				//记录日志
//				if(log.isDebugEnabled()) {
//					log.debug("将客户端消息转发给游戏服务：account={}, connId={}, msgId={}", m_status.account, m_id, mid);
//				}
			}
			break;
			case ConnectionStatus.STATUS_LOSTED: {
				if(log.isDebugEnabled()) {
					log.debug("接到客户端信息，但是玩家已是断线状态，忽略此消息。");
				}
			}
			break;
			default: {
				log.warn("转发消息时发现错误的玩家状态：{}", m_status.status);
			}
		}
	}
	
	/**
	 * 连接验证
	 */
	private void connCheck() {
		//验证间隔
		if(!connCheckTickTimer.isPeriod(Port.getTime())) {
			return;
		}
		
		//避免由于Debug断点等情况 造成瞬间发送多个检查请求
		connCheckTickTimer.reStart();
		
		//清理掉超时的连接
		if(connCheckIncreaseTimes >= CONN_CHECK_TIMES) {
			//清理
			channel.close();
			
			//日志
			LogCore.conn.warn("清理错误的连接：conn={}, status={}, account={}, humanId={}", 
									m_id, m_status.status, m_status.account, m_status.humanId);
			return;
		}
		
		//根据状态进行验证
		switch(m_status.status) {
			case ConnectionStatus.STATUS_LOGIN:
			case ConnectionStatus.STATUS_GATE: {
				CallPoint toPoint = new CallPoint(Distr.NODE_DEFAULT, Distr.PORT_DEFAULT, Distr.SERV_GATE);
				port.callSeam(toPoint, new Object[] {m_id}, "method", CallSeamMethodKey.ACCOUNT_CHECK);
				port.listenResult(this::_result_pulseConnCheck);
			}
			break;
			case ConnectionStatus.STATUS_PLAYING: {
				CallPoint toPoint = new CallPoint(m_status.stageNodeId, m_status.stagePortId, m_status.humanId);
				port.callSeam(toPoint, new Object[] {m_id}, "method", CallSeamMethodKey.WORLD_CHECK);
				port.listenResult(this::_result_pulseConnCheck);
			}
		}
		
		//累加连接检查次数
		connCheckIncreaseTimes++;
	}
	
	public void _result_pulseConnCheck(Param results, Param context) {
		boolean has = results.get();
		//没找到这个连接
		if(!has) return;
		
		//收到过就清空累计次数
		connCheckIncreaseTimes = 0;
	}
	
	/**
	 * 获取连接状态字符串信息
	 * @return
	 */
	public String getStatusString() {
		return m_status.toString();
	}
}
