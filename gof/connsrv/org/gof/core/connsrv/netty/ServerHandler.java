package org.gof.core.connsrv.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.gof.core.Port;
import org.gof.core.connsrv.Connection;
import org.gof.core.connsrv.main.ConnStartup;
import org.gof.core.support.Config;
import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	private final Logger log = LogCore.conn;

	//当前的全部连接
	public static final ConcurrentLinkedQueue<Connection> conns = new ConcurrentLinkedQueue<>();
	
	//当前连接信息
	private Connection conn;
	private RC4 _rc4 = null;
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		try {
			//记录数据
			byte[] buff = (byte[])msg;
			if(Config.CONN_ENCRYPT && _rc4 != null){
				_rc4.crypt(buff, 4, -1);
			}
			conn.putDate(buff);
		} catch (Exception et) {
			log.error("", et);
		}
    }
	
	/**
	 * 建立新连接
	 * @throws Exception 
	 */
	@Override  
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		Channel channel = ctx.channel();

		//日志
//		if(log.isDebugEnabled()) {
			InetSocketAddress addrLocal = (InetSocketAddress)channel.localAddress();
			log.info("新建一个连接：connId={}, port={}, addr={}", Connection.getId(channel), addrLocal.getPort(), channel.remoteAddress());
//		}
		
		Port port = ConnStartup.CONN_NODE.getPort("0");
		conn = new Connection(channel, port);
		conn.startup();
		
		//加入连接列表
		conns.add(conn);
		
		if(Config.CONN_ENCRYPT){
			byte[] key = RC4.getRandomKey();
			_rc4 = new RC4(key);
			conn.sendBytesWithLength(key);
		}
    }
	
	@Override  
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		
		//日志
		if(log.isDebugEnabled()) {
			log.debug("连接关闭：connId={}, status={}", conn.getId(), conn == null ? "" : conn.getStatusString());
		}
		
		//关闭玩家连接
		if(conn != null) {
			conn.closeDelay();
			
			//清理连接列表
			conns.remove(conn);
		}
    }
	
	/**
	 * 有异常发生
	 * @throws Exception 
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		//输出错误日志
		log.error("连接发生异常：connId={}", Connection.getId(ctx.channel()), cause);
	}
}
