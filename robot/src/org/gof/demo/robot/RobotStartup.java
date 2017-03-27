package org.gof.demo.robot;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

import org.gof.core.connsrv.netty.Decoder;
import org.gof.core.connsrv.netty.Encoder;
import org.gof.core.support.Utils;
import org.gof.demo.netty.RobotMsgHandler;

public class RobotStartup {

	public enum UnitObjectStateKey {
		skillback,					//技能击退
		stun,								//眩晕
		immobilize,					//冻结
		silence,							//沉默	
		skill_shake,					//施法前摇
		skill_charm,				//魅惑
		skill_hypnosis,				//催眠
		;
	}
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		System.setProperty("logFileName", "robot");
		
		RobotMsgHandler robot = new RobotMsgHandler();
		Bootstrap b = new Bootstrap();
		b.group(new NioEventLoopGroup());
		b.channel(NioSocketChannel.class);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast(new Decoder(), new Encoder(), robot);
			}
		});

		ChannelFuture f = b.connect(new InetSocketAddress(10188)).sync(); // (5)

		f.channel().closeFuture().sync();
	}
	
	public static double[] parseDoubleArray(String value) {
		String[] elems = value.split(",");
		if(elems.length > 0) {
			double []temp = new double[elems.length] ;
			for(int i = 0 ; i < elems.length ; i++) {
				temp[i] = Utils.doubleValue(elems[i]);
			}
			return temp;
		}
		return null;
  }
  
	public static float[] parseFloatArray(String value) {
		String[] elems = value.split(",");
		if(elems.length > 0) {
			float []temp = new float[elems.length] ;
			for(int i = 0 ; i < elems.length ; i++) {
				temp[i] = Utils.floatValue(elems[i]);
			}
			return temp;
		}
		return null;
	}
	
	public static int[] parseIntArray(String value) {
		String[] elems = value.split(",");
		if(elems.length > 0) {
			int []temp = new int[elems.length] ;
			for(int i = 0 ; i < elems.length ; i++) {
				temp[i] = Utils.intValue(elems[i]);
			}
			return temp;
		}
		return null;
	}

	public static String[] parseStringArray(String value) {
		String[] elems = value.split(",");
		if(elems.length > 0) {
			String []temp = new String[elems.length] ;
			for(int i = 0 ; i < elems.length ; i++) {
				temp[i] = elems[i];
			}
			return temp;
		}
		return null;
	}
	public static long[] parseLongArray(String value) {
		String[] elems = value.split(",");
		if(elems.length > 0) {
			long []temp = new long[elems.length] ;
			for(int i = 0 ; i < elems.length ; i++) {
				temp[i] = Utils.longValue(elems[i]);
			}
			return temp;
		}
		return null;
	}
	public static boolean[] parseBoolArray(String value) {
		String[] elems = value.split(",");
		if(elems.length > 0) {
			boolean []temp = new boolean[elems.length] ;
			for(int i = 0 ; i < elems.length ; i++) {
				temp[i] = Utils.booleanValue(elems[i]);
			}
			return temp;
		}
		return null;
	}
}
