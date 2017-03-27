package org.gof.demo.worldsrv.inform;

import java.util.List;

import org.gof.demo.worldsrv.support.enumKey.HumanScopeKey;

public class Inform {
	/** 频道位置示意  试着把下面的注释想象成一个屏幕!
	__________________________________________________
	|
	|---------------通告滚动(从右向左滚动)
	|---------------传闻上飘(从下向上淡出)
	|
	|-------------------切地图
	|-------------------提示弹窗
	|
	|
	|喇叭---------------------------提示操作/提示错误
	|---------------------提示功能
	|世界/地图/阵营/联盟/组队/私聊
	|
	|
	--------------------------------------------------
	*/
	


	public final static int 系统 = 1;			//系统
	public final static int 喇叭 = 2;
	public final static int 队伍 = 3;
	public final static int 私聊 = 4;			//私聊
	public final static int 联盟 = 5;			//联盟
	public final static int 世界 = 6;			//世界频道
	public final static int 征召 = 7;			//世界频道
	public final static int 地图 = 8;			//地图(当前)频道
	public final static int 通告滚动 = 128;			
	public final static int 提示操作 = 256;			

//	public final static int 提示操作 = 512;		//黄色提示
	public final static int 提示错误 = 1024;		//红色提示
	public final static int 提示弹窗 = 2048;
	
	/**
	 * 发送消息
	 * @param channel		频道位置
	 * @param scopeKey		发送范围
	 * @param targetKey		目标(可能是地图 Id、队伍Id、军团Id、阵营Id、玩家Id)
	 * @param content		内容
	 * @param sendHumanId	发送人
	 */
	public static void send(int channel, HumanScopeKey scopeKey, Long targetKey, String content, Long sendHumanId) {
		InformManager.inst().sendMsg(scopeKey, targetKey, sendHumanId, channel, content);
	}
	
	/**
	 * 发送消息
	 * @param channel		频道位置
	 * @param scopeKey		发送范围
	 * @param targetKey		目标(可能是地图 Id、队伍Id、军团Id、阵营Id、玩家Id)
	 * @param content		内容
	 */
	public static void send(int channel, HumanScopeKey scopeKey, Long targetKey, String content) {
		send(channel, scopeKey, targetKey, content, null);
	}
	
	/**
	 * 向指定Id的玩家发送信息
	 * @param receiveHumanId 接收人Id
	 * @param sendHumanId	 发送人Id 
	 * @param channel	频道号
	 * @param content	内容
	 */
	public static void user(Long receiveHumanId, int channel, String content, Long sendHumanId){
		InformManager.inst().sendMsg(HumanScopeKey.HUMAN, receiveHumanId, sendHumanId, channel, content);
	}
	
	/**
	 * 向指定Id的玩家发送信息
	 * @param receiveHumanId	接收人Id
	 * @param channel			频道
	 * @param content			内容
	 */
	public static void user(Long receiveHumanId, int channel, String content){
		user(receiveHumanId, channel, content, null);
	}
	
	/**
	 * 向指定Id的玩家集合发送信息
	 * @param receiveHumanIds	接收人的Id集合
	 * @param channel			频道号
	 * @param content			发送内容
	 * @param sendHumanId		发送人Id 
	 */
	public static void users(List<Long> receiveHumanIds, int channel, String content, Long sendHumanId){
		InformManager.inst().sendMsg(HumanScopeKey.HUMAN, receiveHumanIds, sendHumanId, channel, content);
	}
	
	/**
	 * 向指定Id的玩家集合发送信息
	 * @param receiveHumanIds	接收人的Id集合
	 * @param channel			频道号
	 * @param content			发送内容
	 */
	public static void users(List<Long> receiveHumanIds, int channel, String content){
		users(receiveHumanIds, channel, content, null);
	}
	
	/**
	 * 向指定地图上的玩家发送信息
	 * @param sendHumanId	发送人Id
	 * @param stageId		地图Id
	 * @param channel		频道号
	 * @param content		内容
	 */
	public static void stage(Long stageId, int channel, String content, Long sendHumanId){
		InformManager.inst().sendMsg(HumanScopeKey.STAGE, stageId, sendHumanId, channel, content);
	}
	
	/**
	 * 向指定地图上的玩家发送信息
	 * @param stageId		地图Id
	 * @param channel		频道号
	 * @param content		内容
	 */
	public static void stage(Long stageId, int channel, String content){
		stage(stageId, channel, content, null);
	}
	
	/**
	 * 向指定阵营的玩家发送信息
	 * @param sendHumanId	发送人Id
	 * @param countryId		阵营Id
	 * @param channel		频道号
	 * @param content		内容
	 */
	public static void country(Long countryId, int channel, String content, Long sendHumanId) {
		InformManager.inst().sendMsg(HumanScopeKey.COUNTRY, countryId, sendHumanId, channel, content);
	}
	
	/**
	 * 向指定阵营的玩家发送信息
	 * @param countryId
	 * @param channel
	 * @param content
	 */
	public static void country(Long countryId, int channel, String content) {
		country(countryId, channel, content, null);
	}
	
	/**
	 * 向指定军团发送消息
	 * @param unionId		军团Id
	 * @param channel		频道号
	 * @param content		内容
	 * @param sendHumanId	发送者Id
	 */
	public static void union(Long unionId, int channel, String content, Long sendHumanId) {
		InformManager.inst().sendMsg(HumanScopeKey.UNION, unionId, sendHumanId, channel, content);
	}
	
	/**
	 * 向指定军团发送消息
	 * @param unionId
	 * @param channel
	 * @param content
	 */
	public static void union(Long unionId, int channel, String content) {
		union(unionId, channel, content, null);
	}
	
	/**
	 * 向指定队伍发送消息
	 * @param termId		队伍Id
	 * @param channel		频道号
	 * @param content		内容
	 * @param sendHumanId	发送者Id
	 */
	public static void team(Long teamId, int channel, String content, Long sendHumanId) {
		InformManager.inst().sendMsg(HumanScopeKey.TEAM, teamId, sendHumanId, channel, content);
	}
	
	/**
	 * 向指定队伍发送消息
	 * @param termId
	 * @param channel
	 * @param content
	 */
	public static void team(Long teamId, int channel, String content) {
		team(teamId, channel, content, null);
	}
	
	/**
	 * 向所有人发送消息
	 * @param channel		频道号
	 * @param content		内容
	 * @param sendHumanId	发送者Id
	 */
	public static void all(int channel, String content, Long sendHumanId) {
		InformManager.inst().sendMsg(HumanScopeKey.ALL, 0, sendHumanId, channel, content);
	}
	
	/**
	 * 向所有人发送消息
	 * @param channel
	 * @param content
	 */
	public static void all(int channel, String content) {
		all(channel, content, null);
	}
}
