package org.gof.demo.worldsrv.quest;
/**
 * 任务状态
 */
public interface QuestStatusKey {
	public static final int QUEST_STATUS_可接 = 0;			//任务状态：可接
	public static final int QUEST_STATUS_进行中 = 1;			//任务状态：进行中
	public static final int QUEST_STATUS_已完成 = 2;			//任务状态：已完成
	public static final int QUEST_STATUS_已领取奖励 = 3;		//任务状态：已领取奖励
	public static final int QUEST_STATUS_已过期 = 4;			//任务状态：已过期
}
