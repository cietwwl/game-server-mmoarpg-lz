package org.gof.core.dbsrv.main;

import org.gof.core.Node;
import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.dbsrv.DBLargeService;
import org.gof.core.dbsrv.DBPort;
import org.gof.core.dbsrv.DBService;
import org.gof.core.support.Distr;
import org.gof.core.support.idAllot.IdAllotService;
import org.gof.core.support.log.LogCore;

public class DBStartup {

	public static void main(String[] args) {
		//设置日志文件名称
		System.setProperty("logFileName", "db");
		
		//创建Node
		Node node = new Node(Distr.NODE_DB, Distr.getNodeAddr(Distr.NODE_DB));
		
		//启动普通Port
		DBPort portDB = new DBPort(Distr.PORT_DB_COMMON);
		portDB.startup(node);
		
		final DBService dbServ = new DBService(portDB);
		dbServ.startup();
		portDB.addService(dbServ);
		
		//启动高消耗Port
		DBPort portDBLarge = new DBPort(Distr.PORT_DB_LARGE);
		portDBLarge.startup(node);
		
		DBLargeService dbLargeServ = new DBLargeService(portDBLarge);
		dbLargeServ.startup();
		portDBLarge.addService(dbLargeServ);
		
		//启动ID分配服务
		DBPort portIdAllot = new DBPort(Distr.PORT_ID_ALLOT);
		portIdAllot.startup(node);
		portIdAllot.addQueue(new PortPulseQueue() {
			@Override
			public void execute(Port port) {
				IdAllotService serv = new IdAllotService(port);
				serv.startup();
				serv.init();
				port.addService(serv);
			}
		});

		//启动Node
		node.startup();
		
		//启动日志信息
		LogCore.core.info("================================================");
		LogCore.core.info("pwdbsrv started.");
		LogCore.core.info("Listen:" + Distr.getNodeAddr(Distr.NODE_DB));
		LogCore.core.info("================================================");
		
		//系统关闭时进行清理
		Runtime.getRuntime().addShutdownHook(new Thread() { 
			public void run() {
				//通知立即刷新缓存服务器
				dbServ.flushNow();
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} 
		});
	}
}
