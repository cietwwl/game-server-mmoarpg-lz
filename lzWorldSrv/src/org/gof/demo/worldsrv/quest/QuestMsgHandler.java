package org.gof.demo.worldsrv.quest;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSCommitQuestInstDaily;
import org.gof.demo.worldsrv.msg.Msg.CSCommitQuestNormal;
import org.gof.demo.worldsrv.msg.Msg.CSCommitQuestDaily;
import org.gof.demo.worldsrv.msg.Msg.CSOpenQuest;
import org.gof.demo.worldsrv.msg.Msg.CSOpenQuestDaily;
import org.gof.demo.worldsrv.msg.Msg.CSOpenQuestInstDaily;

/**
 * 	任务
 */
public class QuestMsgHandler {
	
	/**
	 * 提交任务
	 * @param param
	 */
	@MsgReceiver(CSCommitQuestNormal.class)
	public void commitQuestNormal(MsgParam param){
		
		HumanObject humanObj = param.getHumanObject();
		CSCommitQuestNormal msg = param.getMsg();
		int code = msg.getCode();
		QuestNormalManager.inst().commitQuest(humanObj, code);
	}
	
	/**
	 * 打开成长任务
	 * @param param
	 */
	@MsgReceiver(CSOpenQuest.class)
	public void openQuest(MsgParam param) {
		
		QuestNormalManager.inst().openQuest(param.getHumanObject());
	}
	
	/**
	 * 打开界面初始化每日任务
	 * @param param
	 */
	@MsgReceiver(CSOpenQuestDaily.class)
	public void openQuestDaily(MsgParam param) {
		
		QuestDailyManager.inst().openQuestDaily(param.getHumanObject());
	}
	
	/**
	 * 提交当前的每日任务
	 * @param param
	 */
	@MsgReceiver(CSCommitQuestDaily.class)
	public void commitQuestDaily(MsgParam param) {
		CSCommitQuestDaily msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		
		QuestDailyManager.inst().commitQuestDaily(humanObj, msg.getCode());
	}
	
	/**
	 * 每日日常
	 * 打开界面初始化每日任务
	 * @param param
	 */
	@MsgReceiver(CSOpenQuestInstDaily.class)
	public void openQuestInstDaily(MsgParam param) {
		
		QuestInstDailyManager.inst().openQuestInstDaily(param.getHumanObject());
	}
	
	/**
	 * 每日日常
	 * 提交当前的每日任务
	 * @param param
	 */
	@MsgReceiver(CSCommitQuestInstDaily.class)
	public void commitQuestInstDaily(MsgParam param) {
		CSCommitQuestInstDaily msg = param.getMsg();
		HumanObject humanObj = param.getHumanObject();
		
		QuestInstDailyManager.inst().commitQuestInstDaily(humanObj, msg.getCode());
	}
}
