package org.gof.demo.worldsrv.support;

import java.util.Properties;

import org.gof.core.support.Utils;

/**
 * 节点配置
 */
public class D {
	//配置文件名称
	private static final String CONFIG_NAME = "gofDistr.properties";
	
	//前缀
	public static final String NODE_WORLD_PREFIX = "world";			//游戏服务器Node前缀
	
	public static final String PORT_PLATFORM_LOGIN_PREFIX = "login";	//平台登陆业务Port前缀
	
	public static final String PORT_GAME_PREFIX = "game";				//游戏业务Port前缀
	public static final String PORT_STAGE_PREFIX = "stage";			//游戏地图Port前缀
	
	//NODE
	public static final String NODE_PLATFORM = "platform";				//平台NodeId
	
	//服务
	public static final String SERV_PLATFORM_LOGIN = "login";			//平台登陆验证服务ID
	
	public static final String SERV_TEST = "test";
	public static final String SERV_HUMAN_CREATE_APPLY = "humanCreateApply";	//玩家创建申请服务
	public static final String SERV_GAME_VALUE = "gameValue";			//全局数据信息
	public static final String SERV_STAGE_GLOBAL = "stageGlobal";		//全局地图信息服务
	public static final String SERV_HUMAN_GLOBAL = "humanGlobal";		//全局玩家信息服务
	public static final String SERV_DATA_RESET = "dataReset";			//每日数据重置
	public static final String SERV_STAGE_DEFAULT = "stageDefault";	//地图默认服务
	public static final String SERV_QUEST_GLOBAL = "questGlobal";		//全局任务服务
	public static final String SERV_TEAM = "team";						//队伍服务(组队)
	public static final String SERV_POCKETLINE = "pocketLine";			//待办服务
	public static final String SERV_RANK = "rank";						//排行服务
	public static final String SERV_WORLDBOSS = "worldBoss";			//世界Boss服务
	public static final String SERV_UNION = "union";					//军团服务
	public static final String SERV_UNION_LEAGUE = "unionLeague";		//兵团战
	public static final String SERV_COUNTRY = "country";				//阵营服务
	public static final String SERV_NOTICE = "notice";					//广播服务
	public static final String SERV_NAME = "name";				//名字服务
	public static final String SERV_MAIL = "mail";				//邮件服务
	
	public static final String SERV_COMPETITION = "competition";				//竞技场
	public static final String SERV_FRIEND = "friend";				//好友
	//其他配置
	public static final int NODE_WORLD_STARTUP_PLATFORM_LOGIN;			//游戏服务实例数
	public static final int NODE_WORLD_STARTUP_NUM;					//游戏服务实例数
	public static final int PORT_STAGE_STARTUP_NUM;					//地图Port服务实例数
	public static final int PORT_GAME_STARTUP_NUM;						//游戏业务Port服务实例数
	
	static {
		Properties prop = Utils.readProperties(CONFIG_NAME);    //加载
		
		NODE_WORLD_STARTUP_PLATFORM_LOGIN = Integer.valueOf(prop.getProperty("port.startup.num.platform.login"));
		NODE_WORLD_STARTUP_NUM = Integer.valueOf(prop.getProperty("node.startup.num.world"));
		PORT_STAGE_STARTUP_NUM = Integer.valueOf(prop.getProperty("port.startup.num.stage"));
		PORT_GAME_STARTUP_NUM = Integer.valueOf(prop.getProperty("port.startup.num.game"));
	}
}