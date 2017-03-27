package org.gof.core.gen.execute;

import java.sql.Connection;

import org.gof.core.gen.GenBase;
import org.gof.core.gen.GenClean;
import org.gof.core.gen.entity.GenDB;
import org.gof.core.gen.entity.GenEntity;
import org.gof.core.gen.observer.GenObServerInit;
import org.gof.core.gen.proxy.GenProxy;
import org.gof.core.gen.serializer.GenSerializer;
import org.gof.core.interfaces.ISerilizable;
//import org.gof.core.gen.callback.GenCallback;
import org.gof.core.support.Config;
import org.gof.core.support.observer.Listener;
import org.gof.core.support.observer.MsgReceiver;

import com.google.protobuf.GeneratedMessage;



public class Gen {
	public static void main(String[] args) throws Exception {
		if(args.length < 2) {
			System.out.println("usage: package, targetDir!");
		}
		
		//设置log4j2配置文件所需的环境变量，作用是gen的时候
		//不会报配置没找到的错误，同时有gen.log的日志记录
		System.setProperty("logFileName", "gen");
		
		String packageName = args[0];
		String target = args[1];
		//第三个参数 指定 user.dir
		if(args.length >= 3 && !"".equals(args[2])) {
			target = args[2] + target;
			GenBase.userDir = args[2];
			//第四个参数 指定文件生成
			if(args.length > 3)
				GenBase.pluginDesFileName = args[3];
		} else {
			target = System.getProperty("user.dir") + target;
		}
		
		GenBase genEntity = new GenEntity(packageName, target);
		//GenBase genCallback = new GenCallback(packageName, target);
		GenBase genProxy = new GenProxy(packageName, target);
		GenBase genMsgReceiverInit = new GenObServerInit(packageName, target, "MsgReceiverInit", "ObServerInit.ftl", MsgReceiver.class);
		GenBase genListenerInit = new GenObServerInit(packageName, target, "ListenerInit", "ObServerInit.ftl", Listener.class);

		GenBase genCommonSerializer = new GenSerializer(packageName, target, "CommonSerializer","CommonSerializer.ftl", ISerilizable.class, 0);
		GenBase genMsgSerializer = new GenSerializer(packageName, target, "MsgSerializer","MsgSerializer.ftl", GeneratedMessage.class, 1);
		
		//-------------------------检查能否生成---------------------------------------------//
		// 判断不能生成Entity
		if(!genEntity.canGen) {
			System.err.println("不能生成Entity，请检查重试。。");
			return;
		}
		
		// 判断不能生成Callback
		//if(!genCallback.canGen) {
		//	System.err.println("不能生成Callback，请检查重试。。");
		//	return ;
		//}
		
		// 判断不能生成Proxy
		if(!genProxy.canGen) {
			System.err.println("不能生成Proxy，请检查重试。。");
			return ;
		}
		
		if(!genMsgReceiverInit.canGen){
			System.err.println("不能生成MsgReceiverInit，请检查重试。。");
			return ;
		}
		// ------------------------删除文件后生成--------------------------------------//
		GenClean clean = new GenClean(packageName, target);
		clean.clean();
		
		// ------------------------开始生成实体类、Callback、Proxy----------------------//
		//
		genMsgReceiverInit.genFiles();
		genListenerInit.genFiles();

		genCommonSerializer.genFiles();
		genMsgSerializer.genFiles();
		
		// 测试生成实体类
		genEntity.genFiles();
		
		// 测试生成Callback类
		//genCallback.genFiles();
		
		// 测试生成Proxy类
		genProxy.genFiles();
		

		
		// 测试自动建表
		GenDB genDB = new GenDB(packageName);
		Connection conn = genDB.getDBConnection("com.mysql.jdbc.Driver", Config.DB_URL, Config.DB_USER, Config.DB_PWD);
		genDB.genDB(conn);
		
		//生成完后 插件指定的文件置空
		GenBase.pluginDesFileName = null;
		
		//强制结束
		System.exit(0);
	}
}