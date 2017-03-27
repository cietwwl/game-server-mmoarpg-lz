package org.gof.core.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 节点设置
 */
public class Distr {
	//配置文件名称
	private static final String CONFIG_NAME = "gofDistr.properties";
	
	//配置前缀
	private static final String PREFIX_NODE_ADDR = "node.addr";
	private static final String PREFIX_PORT_STARTUP = "port.startwith";
	private static final String PREFIX_SERV_STARTUP = "serv.startwith";
	
	//Node地址
	private static final Map<String, String> NODE_ADDR = new HashMap<>();
	//Port所属Node
	private static final Map<String, String> PORT_STARTWITH = new HashMap<>();
	//Service所属Port
	private static final Map<String, String> SERV_STARTWITH = new HashMap<>();
	
	//默认参数
	public static final String NODE_DEFAULT = "world0";		//默认主控游戏服务Node
	public static final String PORT_DEFAULT = "port0";			//默认主控游戏服务Port
	
	//前缀
	public static final String NODE_CONNECT_PREFIX = "conn";	//连接服务器Node前缀
	
	//Node
	public static final String NODE_DB = "db";					//数据库Node名称
	
	//Port
	public static final String PORT_DB_COMMON = "dbCommon";	//数据库默认Port名称
	public static final String PORT_DB_LARGE = "dbLarge";		//数据库高耗操作Port名称
	public static final String PORT_ID_ALLOT = "idAllot";		//ID分配Port
	
	//Service
	public static final String SERV_DB_COMMON = "dbCommon";	//常规数据服务
	public static final String SERV_DB_LARGE = "dbLarge";		//高消耗数据服务
	public static final String SERV_CONN = "conn";				//连接总服务
	public static final String SERV_SEAM = "seam";				//整合服务
	public static final String SERV_GATE = "gate";				//选人服务
	public static final String SERV_ID_ALLOT = "idAllot";		//ID分配服务
	
	//其他配置
	public static final int PORT_STARTUP_NUM_CONN;				//连接服务实例数
	
	static {
		//核心配置
		Properties propPWRD = Utils.readProperties(CONFIG_NAME);
		readConfig(propPWRD);
		
		//游戏配置,简化了配置，使用gofDistr单一文件配置所有service
		//String[] fileGames = propPWRD.getProperty("game.ext.file").split(",");
		//读取配置
		//for(String fileGame : fileGames) {
		//	Properties propGame = Utils.readProperties(fileGame.trim());
		//	readConfig(propGame);
		//}
		
		//连接服务配置
		PORT_STARTUP_NUM_CONN = Integer.valueOf(propPWRD.getProperty("port.startup.num.conn"));
	}
	
	/**
	 * 获取Node连接地址
	 * @param nodeId
	 * @return
	 */
	public static String getNodeAddr(String nodeId) {
		return NODE_ADDR.get(nodeId);
	}
	
	/**
	 * 通过Port来获取父节点Node
	 * @param portId
	 * @return
	 */
	public static String getNodeId(String portId) {
		return PORT_STARTWITH.get(portId);
	}
	
	/**
	 * 通过Serv来获取父节点Port
	 * @param servId
	 * @return
	 */
	public static String getPortId(String servId) {
		return SERV_STARTWITH.get(servId);
	}
	
	/**
	 * 通过Node来获取子节点Port集合
	 * @param servId
	 * @return
	 */
	public static List<String> getPortIds(String nodeId) {
		List<String> ids = new ArrayList<>();
		for(String id : portIds()) {
			if(nodeId.equals(getNodeId(id))) {
				ids.add(id);
			}
		}
		
		return ids;
	}
	
	/**
	 * 通过Port来获取子节点Serv集合
	 * @param servId
	 * @return
	 */
	public static List<String> getServIds(String portId) {
		List<String> ids = new ArrayList<>();
		for(String id : servIds()) {
			if(portId.equals(getPortId(id))) {
				ids.add(id);
			}
		}
		
		return ids;
	}
	
	/**
	 * 所有Port的关键字
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<String> portIds() {
		return new ArrayList(PORT_STARTWITH.keySet());
	}
	
	/**
	 * 所有Serv的关键字
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<String> servIds() {
		return new ArrayList(SERV_STARTWITH.keySet());
	}
	
	/**
	 * 读取配置
	 * @param prop
	 */
	private static void readConfig(Properties prop) {
		//遍历配置
		for(Entry<Object, Object> e : prop.entrySet()) {
			//配置的值
			String pk = (String) e.getKey();
			String pv = (String) e.getValue();
			
			//分解Key为前缀和关键字
			int lastIndexPoint = pk.lastIndexOf(".");
			String prefix = pk.substring(0, lastIndexPoint);
			String key = pk.substring(lastIndexPoint + 1);

			//Node地址配置
			if(PREFIX_NODE_ADDR.equals(prefix)) {
				NODE_ADDR.put(key, pv);
			
			//Port启动配置
			} else if(PREFIX_PORT_STARTUP.equals(prefix)) {
				PORT_STARTWITH.put(key, pv);
			
			//Service启动配置
			} else if(PREFIX_SERV_STARTUP.equals(prefix)) {
				SERV_STARTWITH.put(key, pv);
			}
		}
	}
}