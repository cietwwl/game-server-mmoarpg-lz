package org.gof.demo.seam.main;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gof.core.Node;
import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.connsrv.main.ConnStartup;
import org.gof.core.support.Distr;
import org.gof.core.support.log.LogCore;
import org.gof.core.support.observer.MsgSender;
import org.gof.demo.CommonSerializer;
import org.gof.demo.ListenerInit;
import org.gof.demo.MsgReceiverInit;
import org.gof.demo.MsgSerializer;
import org.gof.demo.seam.DefaultPort;
import org.gof.demo.seam.SeamService;
import org.gof.demo.seam.account.AccountService;
import org.gof.demo.worldsrv.InitFieldSet;
import org.gof.demo.worldsrv.stage.StageGlobalService;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class WorldStartup {
	public static void main(String[] args) {
		//设置个默认值 便于系统调试
		if(args.length == 0) {
			args = new String[] {"0"};
		}
		
		System.setProperty("logFileName", "world" + args[0]);
		
		//验证参数数量
		if(args.length < 1) {
			System.out.println("useage: serverNum");
			return ;
		}
		
		//初始化基本环境
		//MsgSender.init();
		//Event.init();
		MsgReceiverInit.init(MsgSender.instance);
		ListenerInit.init(Event.instance);
		MsgSerializer.init();
		CommonSerializer.init();
		
		//创建Node
		int worldNum = Integer.valueOf(args[0]);
		String nodeId = D.NODE_WORLD_PREFIX + worldNum;
		String nodeAddr = Distr.getNodeAddr(nodeId);
		Node node = new Node(nodeId, nodeAddr);
		
		/* 1 设置远程Node */
		//1.1 连接服务器
		ConnStartup.startup(node);
//		for(int i = 0; i < Distr.PORT_STARTUP_NUM_CONN; i++) {
//			String pid= Distr.NODE_CONNECT_PREFIX + i;
//			node.addRemoteNode(pid, Distr.getNodeAddr(pid));
//		}
		
		//1.2 数据服务器
		node.addRemoteNode(Distr.NODE_DB, Distr.getNodeAddr(Distr.NODE_DB));

		//等待连接DB服务器
		Log.game.info("等待连接数据服务器...");
		while(!node.isRemoteNodeConnected(Distr.NODE_DB)) {
			try {
				Thread.sleep(10);
				node.pulse();
			} catch (InterruptedException e) {
				LogCore.core.error(ExceptionUtils.getStackTrace(e));
			}
		}
		
		//等待1秒 在继续初始化
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			LogCore.core.error(ExceptionUtils.getStackTrace(e1));
		}
		
		//1.3 游戏服务器
		for(int i = 0; i < D.NODE_WORLD_STARTUP_NUM; i++) {
			//不用连接自己
			if(i == worldNum) continue;
			//远程nodeId
			String nid= D.NODE_WORLD_PREFIX + i;
			//连接远程
			node.addRemoteNode(nid, Distr.getNodeAddr(nid));
		}
		
		//1.4 平台服务器
		node.addRemoteNode(D.NODE_PLATFORM, Distr.getNodeAddr(D.NODE_PLATFORM));
		
		/* 2 加载系统数据 */
		//2.1 创建个临时Port
		DefaultPort portDef = new DefaultPort(Distr.PORT_DEFAULT);
		portDef.startup(node);
		
		//2.2 加载系统数据
		final InitFieldSet servInitData = new InitFieldSet();
		portDef.addQueue(new PortPulseQueue() {
			@Override
			public void execute(Port port) {
				servInitData.init();
			}
		});
		
		//等待加载完成
		while(!servInitData.isCompleted()) {
			try {
				Thread.sleep(10);
				node.pulse();
			} catch (InterruptedException e) {
				LogCore.core.error(ExceptionUtils.getStackTrace(e));
			}
		}
		
		//2.3整合服务
		SeamService seamServ = new SeamService(portDef);
		seamServ.startup();
		portDef.addService(seamServ);
		
		/* 3 启动系统默认服务 */
		//只在默认Node上启动
		if(nodeId.equals(Distr.NODE_DEFAULT)) {
			//登陆服务
			AccountService gateServ = new AccountService(portDef);
			gateServ.startup();
			portDef.addService(gateServ);
			
			//初始化地图全局服务 必须在初始化地图前就开启本服务 所以就先放这了
			StageGlobalService stageGlobalServ = new StageGlobalService(portDef);
			stageGlobalServ.startup();
			portDef.addService(stageGlobalServ);
		}
		
		//发布服务器初始化开始事件
		Event.fire(EventKey.GAME_STARTUP_BEFORE, "node", node);

		//Node正式启动
		node.startup();
		
		//发布服务器初始化结束事件
		Event.fire(EventKey.GAME_STARTUP_FINISH, "node", node);
		
		//启动日志信息
		Log.game.info("====================");
		Log.game.info(nodeId + " started.");
		Log.game.info("Listen:" + nodeAddr);
		Log.game.info("====================");
		
		//系统关闭时进行清理
		Runtime.getRuntime().addShutdownHook(new Thread() { 
			public void run() { 
				
			} 
		});
	}
}
