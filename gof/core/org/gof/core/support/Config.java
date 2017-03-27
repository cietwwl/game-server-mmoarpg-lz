package org.gof.core.support;

import java.util.Properties;

/**
 * 配置参数
 */
public class Config {
	//配置文件名称
	private static final String CONFIG_NAME = "gofConfig.properties";
	
	//数据服务配置
	public static final String DB_SCHEMA;		//数据库名
	public static final String DB_URL;			//数据库连接串
	public static final String DB_USER;		//数据库用户名
	public static final String DB_PWD;			//数据库密码
	
	public static final int CONN_PORT;			//连接服务器端口
	public static final boolean CONN_ENCRYPT;  //是否加密网络消息
	
	public static final int DB_CACHED_SYNC_SEC;		//缓存同步间隔(秒) 设置为0时关闭缓存
	
	public static final int EFFECT_PLUS_LAG;		//心跳处理时间间隔较长(毫秒)
	
	static {
		//获取配置
		Properties prop = Utils.readProperties(CONFIG_NAME);
		
		//数据服务配置
		DB_SCHEMA = prop.getProperty("db.schema");
		DB_URL = prop.getProperty("db.url");
		DB_USER = prop.getProperty("db.user");
		DB_PWD = prop.getProperty("db.pwd");
		
		//缓存同步间隔
		if(Sys.isWin()) {
			//windows下认为是开发环境
			DB_CACHED_SYNC_SEC = 1;
		} else {
			DB_CACHED_SYNC_SEC = Utils.intValue(prop.getProperty("db.cached.sync.sec"));
		}
		
		//连接配置 FIXME 加密配置
		CONN_PORT = Utils.intValue(prop.getProperty("conn.port"));
//		CONN_ENCRYPT = Utils.booleanValue(prop.getProperty("conn.encrypt"));
		CONN_ENCRYPT = false;
		//心跳处理时间间隔较长(毫秒)
		EFFECT_PLUS_LAG = Utils.intValue(prop.getProperty("effect.plus.lag"));
	}
}