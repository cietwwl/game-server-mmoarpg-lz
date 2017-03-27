package org.gof.demo.worldsrv.support;

import java.util.Properties;

import org.gof.core.support.Utils;

/**
 * 系统参数
 * 本类都是简单的参数配置，如果需要判断系统环境，
 * 比如是手游还是页游，则使用S.java判断
 */
public class C {
	//配置文件名称
	private static final String CONFIG_NAME =  "gofConfig.properties";
	
	public static final int GAME_PLATFORM_ID;				//运营平台ID 最大91
	public static final int GAME_SERVER_ID;				//游戏区ID 最大9999
	public static final String GAME_I18N_KEY;				//游戏语言
	
	public static final boolean DEBUG_ENABLE;				//启动DEBUG
	public static final int CLIENT_VERSION;				//客户端版本号 临时方案
	
	static {
		Properties prop = Utils.readProperties(CONFIG_NAME);
		
		GAME_PLATFORM_ID = Integer.valueOf(prop.getProperty("game.platform.id"));
		GAME_SERVER_ID = Integer.valueOf(prop.getProperty("game.server.id"));
		GAME_I18N_KEY = prop.getProperty("game.i18n.key");
		
		DEBUG_ENABLE = Boolean.valueOf(prop.getProperty("debug.enable"));
		CLIENT_VERSION = Integer.valueOf(prop.getProperty("client.version"));
	}
}