package org.gof.demo.worldsrv.quest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfQuest;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemVO;
import org.gof.demo.worldsrv.msg.Msg.SCCommitQuestNormalResult;
import org.gof.demo.worldsrv.msg.Msg.SCQuestInfo;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;

/**
 * 每日任务
 *
 */
public class QuestDailyManager extends ManagerBase {

	/**o
	 * 获取实例
	 * 
	 * @return
	 */
	public static QuestDailyManager inst() {
		return inst(QuestDailyManager.class);
	}
	

	/**
	 * 每日首次登陆 刷新每日任务
	 * @param humanObj
	 */
	@Listener({EventKey.HUMAN_RESET_ZERO, EventKey.HUMAN_FIRST_LOGIN})
	public void resetQuestDailyFirst(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		
		List<ConfQuest> confList = new ArrayList<ConfQuest>();
		confList.addAll(ConfQuest.findAll());
		
		// 删除完成的日常任务
		QuestNormalManager.inst().removeCompletedByGroup(humanObj, QuestTypeKey.QUEST_日常);
//		JSONArray questIds = Utils.toJSONArray(human.getQuestIdCompletedJSON());
//		for (ConfQuest conf : confList) {
//			if (questIds.contains(conf.sn)) {
//				questIds.remove(conf.sn);
//			}
//		}
//		human.setQuestIdCompletedJSON(questIds.toJSONString());
		
		initQuestDaily(humanObj);
	}

	/**
	 * 初始化日常任务
	 * @param human
	 */
	public void initQuestDaily(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		List<ConfQuest> confList = new ArrayList<ConfQuest>();
		confList.addAll(ConfQuest.findAll());
		
		List<QuestVO> voList = new ArrayList<QuestVO>();
		//循环构建新的QuestVO
		for (ConfQuest conf : confList) {
			if (checkCanAccept(human, conf)) {
				voList.add(initQuest(humanObj, conf));
			}
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
		
		//推送前端消息体
		sendMsg(humanObj, voList);
	}
	
	/**
	 * 检查能否接取新任务
	 * @param human
	 */
	public boolean checkCanAccept(Human human, ConfQuest conf) {
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		JSONArray questIds = Utils.toJSONArray(human.getQuestIdCompletedJSON());
		// 任务组
		if (conf.group != QuestTypeKey.QUEST_日常) {
			return false;
		}
		// VIP等级是否达到条件
		if (conf.vipLevel > human.getVipLevel()) {
			return false;
		}
		// 任务等级是否达到条件
		if (conf.level > human.getLevel()) {
			return false;
		}
		// 是否达到开启时间
		if (conf.beginTime != 0 && conf.endTime != 0) {
			int hour = Utils.getHourOfTime(Port.getTime());
			if(hour < conf.beginTime || hour > conf.endTime){
				return false;
			}
		}
		// 是否已接
		for (QuestVO vo : voList) {
			if (vo.sn == conf.sn) 
				return false;
		}
		// 要接任务是否已完成
		if (questIds.contains(conf.sn)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 玩家接新任务
	 * @param human
	 */
	public QuestVO initQuest(HumanObject humanObj, ConfQuest quest) {
		QuestVO vo = null;
		switch (quest.type) {
		case QuestTypeKey.QUEST_TYPE_101:
			vo = initQuestDaily_Type1(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_102:
			vo = initQuestDaily_Type2(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_103:
			vo = initQuestDaily_Type3(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_104:
			vo = initQuestDaily_Type4(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_105:
			vo = initQuestDaily_Type5(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_106:
			vo = initQuestDaily_Type6(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_107:
			vo = initQuestDaily_Type7(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_9:
			vo = initQuestDaily_Type9(humanObj,	quest);
		default:
			vo = new QuestVO();
			break;
		}
		return vo;
	}
	
	/**
	 * 初始化任务类型1：强化任意装备N次
	 */
	private QuestVO initQuestDaily_Type1(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		return vo;
	}
	
	/**
	 * 初始化任务类型2：冲星装备位N次
	 */
	private QuestVO initQuestDaily_Type2(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		return vo;
	}
	
	/**
	 * 初始化任务类型3：镶嵌宝石N次
	 */
	private QuestVO initQuestDaily_Type3(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		return vo;
	}
	
	/**
	 * 初始化任务类型4：使用伙伴经验丹
	 */
	private QuestVO initQuestDaily_Type4(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		return vo;
	}
	
	/**
	 * 初始化任务类型5：金币抽奖
	 */
	private QuestVO initQuestDaily_Type5(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		return vo;
	}
	
	/**
	 * 初始化任务类型6：商店购买
	 */
	private QuestVO initQuestDaily_Type6(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		return vo;
	}
	
	/**
	 * 初始化任务类型7：升级角色技能N次
	 */
	private QuestVO initQuestDaily_Type7(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		return vo;
	}

	/**
	 * 初始化任务类型9：使用指定道具N次
	 */
	private QuestVO initQuestDaily_Type9(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		vo.targetProgress = quest.target[1];//使用道具的次数
		return vo;
	}
	
	/**
	 * 发送任务奖励，如果背包满则发送失败
	 * @param questId
	 * @param humanObj
	 * @date 2015年5月8日 上午10:58:21
	 */
	public boolean addAward(int questId, HumanObject humanObj) {
		ConfQuest confQuest = ConfQuest.get(questId);
		List<ItemVO> itemVOs = new ArrayList<ItemVO>();
		int[] awards = confQuest.award;
		for (int i = 0; i < awards.length; i  +=2) {
			int itemId = awards[i];
			int num = awards[i+1];
			ItemVO itemVO = new ItemVO(itemId, num);
			itemVOs.add(itemVO);
		}
		// 如果背包空间不足
		ReasonResult result = ItemBagManager.inst().canAdd(humanObj, itemVOs);
		if (!result.success) {
			Log.quest.info("{}完成日常任务背包空间不足!",humanObj.name);
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("item.error.numNotEnough"));
			return false;
		}
		// 进背包
		ItemBagManager.inst().add(humanObj, itemVOs);
		return true;
	}

	/**
	 * 向前端发送一个任务信息
	 * @param humanObj
	 * @param human
	 */
	public void sendMsg(HumanObject humanObj, QuestVO vo) {
		SCQuestInfo.Builder msg = SCQuestInfo.newBuilder();
		//推送前端消息体
		msg.addQuest(vo.createMsg());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 向前端发送全部任务信息
	 * @param humanObj
	 * @param human
	 */
	public void sendMsg(HumanObject humanObj, List<QuestVO> voList) {
		SCQuestInfo.Builder msg = SCQuestInfo.newBuilder();
		//推送前端消息体
		for (QuestVO vo : voList) {
			msg.addQuest(vo.createMsg());
		}
		humanObj.sendMsg(msg);
	}
	/**
	 * 每次打开界面初始化
	 * @param humanObj
	 */
	public  void openQuestDaily(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		// 检查有没有可接任务（主要是定时刷出来的任务）
		List<ConfQuest> confList = new ArrayList<ConfQuest>();
		confList.addAll(ConfQuest.findAll());
		for (ConfQuest conf : confList) {
			if (checkCanAccept(human, conf)) {
				voList.add(initQuest(humanObj, conf));
			}
		}
		
		// 检查有没有可删的任务（主要是限时任务到时间的）
		Iterator<QuestVO> iter = voList.iterator();
		while(iter.hasNext()) {
			QuestVO vo = iter.next();
			ConfQuest conf = ConfQuest.get(vo.sn);
			if (conf.endTime != 0 && Utils.getHourOfTime(Port.getTime()) > conf.endTime) {
				iter.remove();
			}
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
		System.out.println("日常任务："+voList.toString());
		//推送前端消息体
		sendMsg(humanObj, voList);
	}
	
	/**
	 * 提交每日任务
	 * @param sn
	 * @param humanObj
	 */
	public void commitQuestDaily(HumanObject humanObj, int questId) {
		Human human = humanObj.getHuman();
		// 要提交的任务是否已过期
		ConfQuest conf = ConfQuest.get(questId);
		if (conf.endTime != 0 && Utils.getHourOfTime(Port.getTime()) > conf.endTime) {
			Log.quest.info("任务已过期不能提交: {}",questId);
			return;
		}
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		Iterator<QuestVO> iter = voList.iterator();
		while(iter.hasNext()) {
			QuestVO vo = iter.next();
			if (vo.sn == questId) {
				if (vo.status != QuestStatusKey.QUEST_STATUS_已完成) {
					Log.quest.info("任务状态不正确: {}",vo.status);
					return;
				}
				// 增加奖励
				if(!addAward(questId, humanObj)){
					return;
				}
				// 删除任务增加到已完成列表
				JSONArray questIds = Utils.toJSONArray(human.getQuestIdCompletedJSON());
				questIds.add(questId);
				human.setQuestIdCompletedJSON(questIds.toJSONString());
				// 删除任务
				iter.remove();
				//发送任务完成消息
				SCCommitQuestNormalResult.Builder msg = SCCommitQuestNormalResult.newBuilder();
				msg.setCode(questId);
				humanObj.sendMsg(msg);
			}
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
		//推送前端消息体
		sendMsg(humanObj, voList);
	}
	
	/**
	 * 更新任务列表
	 * @param param
	 */
	@Listener(EventKey.UPDATE_QUEST)
	public void _listener_UPDATE_QUEST(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		
		List<ConfQuest> confList = new ArrayList<ConfQuest>();
		confList.addAll(ConfQuest.findAll());
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		//循环构建新的QuestVO
		for (ConfQuest conf : confList) {
			if (checkCanAccept(human, conf)) {
				voList.add(initQuest(humanObj, conf));
				Event.fire(EventKey.UPDATE_LIVENESS, "humanObj", humanObj);
			}
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
		sendMsg(humanObj, voList);
	}
	
	/**
	 * 强化装备事件监听
	 * @param param
	 */
	@Listener(EventKey.BODY_PART_QIANGHUA)
	public void _listener_BODY_PART_QIANGHUA(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_101) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[0]) {
				setFinish(humanObject, vo);
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
	}

	/**
	 * 冲星装备位事件监听
	 * @param param
	 */
	@Listener(EventKey.BODY_PART_CHONGXING)
	public void _listener_BODY_PART_CHONGXING(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_102) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[0]) {
				setFinish(humanObject, vo);
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 宝石镶嵌事件监听
	 * @param param
	 */
	@Listener(EventKey.BODY_GEM_CHANGE)
	public void _listener_BODY_GEM_CHANGE(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_103) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[0]) {
				setFinish(humanObject, vo);
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 使用伙伴经验丹
	 * @param param
	 */
	@Listener(EventKey.PROP_ADD_EXP)
	public void _listener_PROP_ADD_EXP(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_104) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[0]) {
				setFinish(humanObject, vo);
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 宝藏，抽卡
	 * @param param
	 */
	@Listener(EventKey.LOTTERY_SELECT)
	public void _listener_LOTTERY_SELECT(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_105) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[0]) {
				setFinish(humanObject, vo);
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 商店购买
	 * @param param
	 */
	@Listener(EventKey.BUG_GOODS)
	public void _listener_BUG_GOODS(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_106) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[0]) {
				setFinish(humanObject, vo);
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 升级角色技能N次
	 * @param param
	 */
	@Listener(EventKey.SKILL_UPGRADE)
	public void _listener_SKILL_UPGRADE(Param param) {
		UnitObject uniObj=param.get("uniObj");
		HumanObject humanObj = uniObj.getHumanObj();
		Human human = humanObj.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_107) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[0]) {
				setFinish(humanObj, vo);
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObj, vo);
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
	}
	/**
	 * 使用某个道具N次
	 * @date 2015年5月8日 下午5:00:32
	 */
	@Listener(EventKey.ITEM_USE)
	public void _listener_ITEM_USE(Param param) {
		HumanObject humanObj = param.get("humanObject");
		Human human = humanObj.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestDailyJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_9) continue;
			
			ConfQuest quest = ConfQuest.get(vo.sn);
			Integer sn = param.get("sn");
			if(sn != quest.target[0])continue;
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[1]) {
				setFinish(humanObj, vo);
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObj, vo);
		}
		human.setQuestDailyJSON(QuestVO.listToJson(voList));
	}


	private void setFinish(HumanObject humanObj, QuestVO vo) {
		vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		//加载事件
		Event.fire(EventKey.DAILY_QUEST_PASS, "humanObj", humanObj);
	}
}