package org.gof.demo.worldsrv.dailyliveness;
/* 活跃度活动类型
 * 1.每日任务
 * 2.伙伴试炼
 * 3.组队副本
 * 4.世界boss
 * 5.远征
 * 6.情缘
 * 7.商城
 * 8.竞技场
 * 9.公会
 */
public interface LivenessTypeKey {
	public static final int DAILY_QUEST_TYPE = 1;	//每日任务
	public static final int GENERAL_TYPE = 2; 		//伙伴试炼
	public static final int TEAM_INSTANCE = 3; 		//组队副本
	public static final int WORLD_BOSS = 4; 		//世界BOSS
	public static final int MALL = 7; 		//商城
	public static final int ARENA = 8; 		//竞技场
	public static final int UNION = 9; 		//公会
	
}
