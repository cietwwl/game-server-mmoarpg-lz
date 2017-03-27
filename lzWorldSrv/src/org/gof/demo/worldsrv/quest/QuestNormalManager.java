package org.gof.demo.worldsrv.quest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfGemBase;
import org.gof.demo.worldsrv.config.ConfPartsData;
import org.gof.demo.worldsrv.config.ConfQuest;
import org.gof.demo.worldsrv.entity.Activity;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.entity.Part;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemBodyManager;
import org.gof.demo.worldsrv.item.ItemPack;
import org.gof.demo.worldsrv.item.ItemVO;
import org.gof.demo.worldsrv.mail.MailManager;
import org.gof.demo.worldsrv.msg.Msg.SCCommitQuestNormalResult;
import org.gof.demo.worldsrv.msg.Msg.SCQuestInfo;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class QuestNormalManager extends ManagerBase {
	
	private static final int SLOT_NUM = 3;					//每个装备位的槽数
	/**
	 * 获取实例
	 * @return
	 */
	public static QuestNormalManager inst() {
		return inst(QuestNormalManager.class);
	}
	
	/**
	 * 初始化主线任务、支线任务
	 * @param human
	 */
	public void initQuestNormal(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		List<ConfQuest> confList = new ArrayList<ConfQuest>();
		confList.addAll(ConfQuest.findAll());
		
		List<QuestVO> oldVoList = QuestVO.jsonToList(human.getQuestNormalJSON());
		
		List<QuestVO> newVoList = new ArrayList<QuestVO>();

		
		//循环构建新的QuestVO
		for (ConfQuest conf : confList) {
			if (checkCanAccept(human, conf)) {
				newVoList.add(initQuest(humanObj, conf));
			}
		}
		
		oldVoList.addAll(newVoList);
		human.setQuestNormalJSON(QuestVO.listToJson(oldVoList));
		sendMsg(humanObj, oldVoList);
	}
	
	/**
	 * 检查能否接取新任务
	 * @param human
	 */
	public boolean checkCanAccept(Human human, ConfQuest conf) {
		int pre = conf.preSn;
		JSONArray questIds = Utils.toJSONArray(human.getQuestIdCompletedJSON());
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		
		// 任务组
		if (conf.group == QuestTypeKey.QUEST_日常) {
			return false;
		}
		if (conf.group == QuestTypeKey.QUEST_活动) {
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
		// 要接任务是否已完成
		if (questIds.contains(conf.sn)) {
			return false;
		}
		// 前置任务是否已完成
		if(!questIds.contains(pre) && pre != 0){
			return false;
		}	
		// 是否已接
		for (QuestVO vo : voList) {
			if (vo.sn == conf.sn) 
				return false;
		}
		// 是否达到开启时间
		if (conf.beginTime != 0 && conf.endTime != 0) {
			int hour = Utils.getHourOfTime(Port.getTime());
			if(hour < conf.beginTime || hour > conf.endTime){
				return false;
			}
		}
		return true;
	}
	private boolean isQusestEnd(HumanObject humanObj, int sn){
		QuestVO vo = initQuest(humanObj, ConfQuest.get(sn));
		if(vo == null){
			return false;
		}
		return vo.status == QuestStatusKey.QUEST_STATUS_已完成;
	}
	/**
	 * 玩家接新任务
	 * @param human
	 */
	public QuestVO initQuest(HumanObject humanObj, ConfQuest quest) {
		QuestVO vo = null;
		switch (quest.type) {
		case QuestTypeKey.QUEST_TYPE_1:
			vo = initQuest_Type1(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_2:
			vo = initQuest_Type2(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_3:
			vo = initQuest_Type3(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_4:
			vo = initQuest_Type4(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_5:
			vo = initQuest_Type5(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_6:
			vo = initQuest_Type6(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_7:
			vo = initQuest_Type7(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_8:
			vo = initQuest_Type8(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_9:
			vo = initQuest_Type9(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_10:
			vo = initQuest_Type10(humanObj, quest);
			break;
		case QuestTypeKey.QUEST_TYPE_11:
			vo = initQuest_Type11(humanObj, quest);
			break;
		default:
			vo = new QuestVO();
			break;
		}
		return vo;
	}
	
	/**
	 * 初始化任务类型1：提升玩家等级
	 */
	private QuestVO initQuest_Type1(HumanObject humanObj, ConfQuest quest){
		Human human = humanObj.getHuman();
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = human.getLevel();
		
		//如果级别达到，直接完成
		if(human.getLevel() >= quest.target[0]) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}

	/**
	 * 初始化任务类型2：指定数量的伙伴等级达到N级
	 */
	private QuestVO initQuest_Type2(HumanObject humanObj, ConfQuest quest){
		//Human human = humanObj.getHuman();
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		int count = 0;
		for (CharacterObject co : humanObj.slaves.values()) {
			GeneralObject genObj = (GeneralObject)co;
			if (genObj.getGeneral().getLevel() >= quest.target[1]) {
				count++;
			}
		}
		vo.nowProgress = count;
		//如果级别达到，直接完成
		if(vo.nowProgress >= vo.targetProgress) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	
	/**
	 * 初始化任务类型3：指定数量的伙伴品质达到N级
	 */
	private QuestVO initQuest_Type3(HumanObject humanObj, ConfQuest quest){
		//Human human = humanObj.getHuman();
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		int count = 0;
		for (CharacterObject co : humanObj.slaves.values()) {
			GeneralObject genObj = (GeneralObject)co;
			if (genObj.getGeneral().getQuality() >= quest.target[1]) {
				count++;
			}
		}
		vo.nowProgress = count;
		//如果级别达到，直接完成
		if(vo.nowProgress >= vo.targetProgress) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	
	/**
	 * 初始化任务类型4：伙伴数量
	 */
	private QuestVO initQuest_Type4(HumanObject humanObj, ConfQuest quest){
		//Human human = humanObj.getHuman();
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = humanObj.slaves.size();
		
		//如果级别达到，直接完成
		if(vo.nowProgress >= vo.targetProgress) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	
	/**
	 * 初始化任务类型5：指定个数的技能等级升到N级
	 */
	private QuestVO initQuest_Type5(HumanObject humanObj, ConfQuest quest){
		//Human human = humanObj.getHuman();
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		int count = 0;
		for (SkillCommon skill : humanObj.skills.values()) {
			if (skill.skillLevel >= quest.target[1]) {
				count++;
			}
		}
		vo.nowProgress = count;
		//如果级别达到，直接完成
		if(vo.nowProgress >= vo.targetProgress) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	
	/**
	 * 初始化任务类型6：M个装备位强化到N级
	 * 强化装备位指定位置和等级达成，target[0]为数目，target[1]为指定的等级
	 */
	private QuestVO initQuest_Type6(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		int targetLevel = quest.target[1];//需要达成的强化等级
		Map<Integer, Part> parts = humanObj.dataPers.parts;
		for (Part p : parts.values()) {
			int qhlv = p.getQianghuaLv();
			if(qhlv >= targetLevel){
				vo.nowProgress ++;
			}
		}
		if(vo.nowProgress >= vo.targetProgress ) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	/**
	 * 初始化任务类型7：M个装备冲星到N级
	 * target[0]为数目，target[1]为等级段，target[2]为指定的等级
	 */
	private QuestVO initQuest_Type7(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		
		int targetLevel = quest.target[2];//需要达成的冲星等级
		int targetPhase = quest.target[1];
		Map<Integer, Part> parts = humanObj.dataPers.parts;
		for (Part p : parts.values()) {
			int cxlv = ItemBodyManager.inst().getChongxingLv(p, targetPhase);
			if(cxlv >= targetLevel){
				vo.nowProgress ++;
			}
		}
		if(vo.nowProgress >= vo.targetProgress ) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	/**
	 * 初始化任务类型8：已开启槽位所有的宝石达到N级。如果接到任务的时候三个槽位，需要达到4级，那么接到任务后，如果还有新的
	 * 槽位开启，则只要一共有三个槽位镶嵌就算完成任务
	 * ①判断宝石位是否开启，记录开启数目
	 * ②判断是否完成
	 */
	private QuestVO initQuest_Type8(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.targetProgress = 0;
		vo.nowProgress = 0;
		int targetLevel = quest.target[0];//需要达成的宝石等级
		
		//判断宝石位是否开启
		int humanLv = humanObj.getHuman().getLevel();
		
		for(ConfPartsData cf : ConfPartsData.findAll()){
			for (Integer k : cf.gemNeedLv ) {
				if(k <= humanLv){
					vo.targetProgress ++;
				}
			}
		}
		
		ItemPack pack = humanObj.dataPers.items;
		Map<Integer, Part> parts = humanObj.dataPers.parts;
		for (Part part : parts.values()) {
			String gems = part.getGemsIds();
			JSONObject gemIds = Utils.toJSONObject(gems);
			for(int i = 0; i< SLOT_NUM; i++){
				long gemId = gemIds.getLongValue(String.valueOf(i));
				if (gemId > 0) {
					 Item it = pack.getFromBody(gemId);
					 if(ConfGemBase.get(it.getSn()).level >= targetLevel){
						 vo.nowProgress ++;
					 }
				}
			}
		}
		
		if(vo.nowProgress >= vo.targetProgress ) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	/**
	 * 初始化任务类型9：使用指定道具N次
	 */
	private QuestVO initQuest_Type9(HumanObject humanObj, ConfQuest quest){
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
	 * 初始化任务类型10：累计登陆奖励
	 */
	private QuestVO initQuest_Type10(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		Activity activity = humanObj.dataPers.activity;
		JSONObject addLoginJson = Utils.str2JSONObject(activity.getAddLoginJson());
		vo.nowProgress = addLoginJson.getIntValue("days");
		
		//如果级别达到，直接完成
		if(vo.nowProgress >= quest.target[0]) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	
	/**
	 * 初始化任务类型11：连续登陆奖励
	 */
	private QuestVO initQuest_Type11(HumanObject humanObj, ConfQuest quest){
		/* 设置任务信息 */
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.targetProgress = quest.target[0];
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		Activity activity = humanObj.dataPers.activity;
		JSONObject lineLoginJson = Utils.str2JSONObject(activity.getLineLoginJson());
		vo.nowProgress = lineLoginJson.getIntValue("days");
		
		//如果级别达到，直接完成
		if(vo.nowProgress >= quest.target[0]) {
			vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		} else {
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		}
		return vo;
	}
	
	/**
	 * 从已完成任务列表中删除某一组的任务
	 */
	public void removeCompletedByGroup(HumanObject humanObj, int group){
		Human human = humanObj.getHuman();
		JSONArray questIds = Utils.toJSONArray(human.getQuestIdCompletedJSON());
		Iterator<Object> iter = questIds.iterator();
		while(iter.hasNext()) {
			int questId = (int) iter.next();
			if (ConfQuest.get(questId).group == group) {
				iter.remove();
			}
		}
		human.setQuestIdCompletedJSON(Utils.toJSONString(questIds));
	}
	
	/**
	 * 从已完成任务列表中删除某一类型的任务
	 */
	public void removeCompletedByType(HumanObject humanObj, int type){
		Human human = humanObj.getHuman();
		JSONArray questIds = Utils.toJSONArray(human.getQuestIdCompletedJSON());
		Iterator<Object> iter = questIds.iterator();
		while(iter.hasNext()) {
			int questId = (int) iter.next();
			if (ConfQuest.get(questId).type == type) {
				iter.remove();
			}
		}
		human.setQuestIdCompletedJSON(Utils.toJSONString(questIds));
	}
	
	/**
	 * 系统自动提交任务,有对任务的voList删除操作
	 * @param humanObj
	 * @param questId,即配置表里的sn，在表里是唯一的，对于每个玩家也是唯一的
	 * @return 是否有可以自动提交的任务且提交成功
	 * @date 2015年5月6日 下午6:41:15
	 */
	private boolean autoCommitQuest(HumanObject humanObj, List<QuestVO> voList){
		Human human = humanObj.getHuman();
		boolean autoCommited = false;

		Iterator<QuestVO> iter = voList.iterator();
		while(iter.hasNext()) {
			QuestVO vo = iter.next();
			ConfQuest confQuest = ConfQuest.get(vo.sn);
			if(confQuest == null){
				continue;
			}
			int questId = vo.sn;
			//如果不是自动提交类型，则不提交
			if(confQuest.commitType != 1){
				continue;
			}
			//判断任务状态
//			if(vo.status == QUEST_STATUS_已完成||vo.status == QUEST_STATUS_已领取奖励){
//				continue;
//			}
			// 要提交的任务是否已过期
			if (confQuest.endTime != 0 && Utils.getHourOfTime(Port.getTime()) > confQuest.endTime) {
				return false;
			}
			//判断是否可以提交
			if(!isQusestEnd(humanObj, questId)){
				continue;
			}
			// 增加奖励失败
			if(!sendAward(humanObj, questId)){
				return false;
			}
			// 删除任务增加到已完成列表
			JSONArray questIds = Utils.toJSONArray(human.getQuestIdCompletedJSON());
			questIds.add(questId);
			human.setQuestIdCompletedJSON(questIds.toJSONString());//?
			iter.remove();
			humanObj.getHuman().setQuestNormalJSON(QuestVO.listToJson(voList));
			//发送任务完成消息，客户端会提示任务完成
			SCCommitQuestNormalResult.Builder msg = SCCommitQuestNormalResult.newBuilder();
			msg.setCode(questId);
			humanObj.sendMsg(msg);
			autoCommited = true;
		}
		return autoCommited;
	}
	/**
	 * 提交任务
	 * @param humanObj
	 */
	public void commitQuest(HumanObject humanObj, int questId) {
		Human human = humanObj.getHuman();
		
		// 要提交的任务是否已过期
		ConfQuest confQuest = ConfQuest.get(questId);
		if (confQuest.endTime != 0 && Utils.getHourOfTime(Port.getTime()) > confQuest.endTime) {
			Log.quest.info("任务已过期不能提交: {}",questId);
			return;
		}
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
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
				if (ConfQuest.get(questId).group != QuestTypeKey.QUEST_活动) {
					JSONArray questIds = Utils.toJSONArray(human.getQuestIdCompletedJSON());
					questIds.add(questId);
					human.setQuestIdCompletedJSON(questIds.toJSONString());
					iter.remove();
					//发送任务完成消息
					SCCommitQuestNormalResult.Builder msg = SCCommitQuestNormalResult.newBuilder();
					msg.setCode(questId);
					humanObj.sendMsg(msg);
				} else {
					vo.status = QuestStatusKey.QUEST_STATUS_已领取奖励;
				}
			}
		}
		// 加载新任务
		if (confQuest.nextSn != 0) {
			ConfQuest confNextQuest = ConfQuest.get(confQuest.nextSn);
			QuestVO vo = initQuest(humanObj, confNextQuest);
			voList.add(vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
		sendMsg(humanObj, voList);
	}
	/**
	 * 发送奖励，背包满了则发送邮件
	 * @param humanObj
	 * @param questId
	 */
	private boolean sendAward(HumanObject humanObj, int questId){
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
			//背包满了发邮件
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("quest.awards.bymail"));
			sendAwardsByMail(humanObj, confQuest);
			Log.shop.info("{}完成任务{}背包空间不足!",humanObj.name, confQuest.name);
		}
		// 进背包
		ItemBagManager.inst().add(humanObj, itemVOs);
		return true;
	}

	private void sendAwardsByMail(HumanObject humanObj, ConfQuest confQuest) {
		String title = I18n.get("quest.mail.title");
		String content = I18n.get("quest.mail.content",confQuest.name);
		int[] awards = confQuest.award;
		int awardsNum = awards.length / 2;
		int [] type = new int[awardsNum];
		int [] oid = new int[awardsNum];
		int [] num = new int[awardsNum];
		for (int i = 0; i < awardsNum; i++) {
			type[i] = QuestTypeKey.QUEST_MAIL_AWARD_TYPE_1;
			oid[i] = awards[i*2];
			num[i] = awards[i*2 + 1];
		}
		MailManager.inst().sendMail(humanObj.getHumanId(), MailManager.SYS_SENDER, title, content, type, oid, num);
	}
	/**
	 * 增加奖励
	 * @param questId
	 * @param humanObj
	 * @return
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
			Log.shop.info("{}添加奖励背包不足!",humanObj.name);
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
	public  void openQuest(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 检查有没有可接任务（主要是定时刷出来的任务）
		initQuestNormal(humanObj);
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		// 检查有没有可删的任务（主要是限时任务到时间的）
		Iterator<QuestVO> iter = voList.iterator();
		while(iter.hasNext()) {
			QuestVO vo = iter.next();
			ConfQuest conf = ConfQuest.get(vo.sn);
			if(conf == null){
				iter.remove();
				Log.quest.info("{}删除任务励{}!",humanObj.name, vo.sn);
			}
			if (conf.endTime != 0 && Utils.getHourOfTime(Port.getTime()) > conf.endTime) {
				iter.remove();
			}
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
		System.out.println("成长任务："+voList.toString());
		//推送前端消息体
		sendMsg(humanObj, voList);
//		
//		GlobalZoneObjectServiceProxy prx = GlobalZoneObjectServiceProxy.newInstance();
//		prx.enter(humanObj);
	}
	
	/**
	 * 更新任务列表
	 * 每次更新任务列表，判断是否是达到任务条件自动完成的类型
	 * @param param
	 */
	@Listener(EventKey.UPDATE_QUEST)
	public void _listener_UPDATE_QUEST(Param param) {
		//本次更新有自动完成提交的任务
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		
		List<ConfQuest> confList = new ArrayList<ConfQuest>();
		confList.addAll(ConfQuest.findAll());
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		//循环构建新的QuestVO
		voList = doAutoCommit(humanObj, voList); 
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
		sendMsg(humanObj, voList);
	}
	/**
	 * 自动提交所有的可以自动提交的, 并将自动完成的从列表中删除。
	 * 如果更新到新的任务，则自动放到角色升上
	 * @param humanObj
	 * @param voList	not null
	 * @return 任务列表
	 * @date 2015年5月6日 下午5:51:56
	 */
	private List<QuestVO> doAutoCommit(HumanObject humanObj, List<QuestVO> voList) {
		boolean autoCommited = autoCommitQuest(humanObj, voList);
		//如果有自动提交的，则查看配置的任务重有无新的任务可以添加
		if(autoCommited){//递归
			//循环构建新的QuestVO
			for (ConfQuest conf : ConfQuest.findAll()) {
				if (checkCanAccept(humanObj.getHuman(), conf)) {
					voList.add(initQuest(humanObj, conf));
					humanObj.getHuman().setQuestNormalJSON(QuestVO.listToJson(voList));
				}
			}
			return doAutoCommit(humanObj, voList);
		}
		return voList;
	}

	/**
	 * 用户升级事件监听
	 * @param param
	 */
	@Listener(EventKey.HUMAN_UPGRADE)
	public void _listener_HUMAN_UPGRADE(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_1) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.nowProgress = human.getLevel();
			if (human.getLevel() >= quest.target[0]) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}

	/**
	 * 伙伴等级提升
	 * @param param
	 */
	@Listener(EventKey.GENERAL_UPGRADE)
	public void _listener_GENERAL_UPGRADE(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_2) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			int count = 0;
			for (CharacterObject co : humanObject.slaves.values()) {
				GeneralObject genObj = (GeneralObject)co;
				if (genObj.getGeneral().getLevel() >= quest.target[1]) {
					count++;
				}
			}
			vo.nowProgress = count;
			//如果级别达到，直接完成
			if(vo.nowProgress >= vo.targetProgress) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	/**
	 * 伙伴品质升级
	 * @param param
	 */
	@Listener(EventKey.GENERAL_QUILITY_LEVELUP)
	public void _listener_GENERAL_QUILITY_LEVELUP(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_3) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			int count = 0;
			for (CharacterObject co : humanObject.slaves.values()) {
				GeneralObject genObj = (GeneralObject)co;
				if (genObj.getGeneral().getQuality() >= quest.target[1]) {
					count++;
				}
			}
			vo.nowProgress = count;
			//如果级别达到，直接完成
			if(vo.nowProgress >= vo.targetProgress) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 伙伴数量
	 * @param param
	 */
	@Listener(EventKey.GENERAL_CREATE)
	public void _listener_GENERAL_CREATE(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_4) continue;
			//ConfQuest quest = ConfQuest.get(vo.sn);
			
			vo.nowProgress = humanObject.slaves.size();
			//如果级别达到，直接完成
			if(vo.nowProgress >= vo.targetProgress) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 指定个数的技能等级升到N级
	 * @param param
	 */
	@Listener(EventKey.SKILL_UPGRADE)
	public void _listener_SKILL_UPGRADE(Param param) {
		UnitObject uniObj=param.get("uniObj");
		HumanObject humanObject = uniObj.getHumanObj();
		
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_5) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			int count = 0;
			for (SkillCommon skill : humanObject.skills.values()) {
				if (skill.skillLevel >= quest.target[1]) {
					count++;
				}
			}
			vo.nowProgress = count;
			//如果级别达到，直接完成
			if(vo.nowProgress >= vo.targetProgress) {
				vo.status =QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	/**
	 * 	 * 初始化任务类型7：M个装备冲星到N级
	 * target[0]为数目，target[1]为等级段，target[2]为指定的等级
	 */
	@Listener(EventKey.BODY_PART_QIANGHUA)
	public void _listener_BODY_PART_QIANGHUA(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if(vo.type != QuestTypeKey.QUEST_TYPE_6) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			vo.targetProgress = quest.target[0];
			vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			vo.type = quest.type;
			
			int targetLevel = quest.target[1];//需要达成的强化等级
			Map<Integer, Part> parts = humanObject.dataPers.parts;
			for (Part p : parts.values()) {
				int qhlv = p.getQianghuaLv();
				if(qhlv >= targetLevel){
					vo.nowProgress ++;
				}
			}
			if(vo.nowProgress >= vo.targetProgress ) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	/**
	 * 装备冲星，判断“指个数装备位类型强化到N级”
	 */
	@Listener(EventKey.BODY_PART_CHONGXING)
	public void _listener_BODY_PART_CHONGXING(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if(vo.type != QuestTypeKey.QUEST_TYPE_7) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			int targetLevel = quest.target[2];//需要达成的冲星等级
			int targetPhase = quest.target[1];
			
			Map<Integer, Part> parts = humanObject.dataPers.parts;
			for (Part p : parts.values()) {
				int cxlv = ItemBodyManager.inst().getChongxingLv(p, targetPhase);
				if(cxlv >= targetLevel){
					vo.nowProgress ++;
				}
			}
			if(vo.nowProgress >= vo.targetProgress ) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	/**
	 * 装备冲星，判断“指个数装备位类型强化到N级”
	 */
	@Listener({EventKey.BODY_GEM_CHANGE, EventKey.BODY_GEM_COMPOSITE})
	public void _listener_BODY_GEM_CHANGE(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if(vo.type != QuestTypeKey.QUEST_TYPE_8) continue;
			ConfQuest quest = ConfQuest.get(vo.sn);
			int targetLevel = quest.target[0];// 钻石等级
			
			ItemPack pack = humanObject.dataPers.items;
			Map<Integer, Part> parts = humanObject.dataPers.parts;
			for (Part part : parts.values()) {
				String gems = part.getGemsIds();
				JSONObject gemIds = Utils.toJSONObject(gems);
				for(int i = 0; i< SLOT_NUM; i++){
					long gemId = gemIds.getLongValue(String.valueOf(i));
					if (gemId > 0) {
						 Item it = pack.getFromBody(gemId);
						 if(ConfGemBase.get(it.getSn()).level >= targetLevel){
							 vo.nowProgress ++;
						 }
					}
				}
			}
			if(vo.nowProgress >= vo.targetProgress ) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObject, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	/**
	 * 使用某个道具N次
	 * @param param
	 * @date 2015年5月8日 下午5:59:33
	 */
	@Listener(EventKey.ITEM_BE_USED_SUCCESS)
	public void _listener_ITEM_USE(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_9) continue;
			
			ConfQuest quest = ConfQuest.get(vo.sn);
			Integer sn = param.get("sn");
			if(sn != quest.target[0])continue;
			vo.nowProgress += 1;
			if (vo.nowProgress >= quest.target[1]) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObj, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 累计登陆完成
	 * @param param
	 */
	@Listener(EventKey.LOGIN_AWARD)
	public void _listener_ADD_LOGIN_AWARD(Param param) {
		HumanObject humanObj = param.get("humanObject");
		Human human = humanObj.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_10) continue;
			
			ConfQuest quest = ConfQuest.get(vo.sn);
			Activity activity = humanObj.dataPers.activity;
			JSONObject addLoginJson = Utils.str2JSONObject(activity.getAddLoginJson());
			vo.nowProgress = addLoginJson.getIntValue("days");
			if (vo.nowProgress >= quest.target[0]) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObj, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
	
	/**
	 * 连续登陆完成
	 * @param param
	 */
	@Listener(EventKey.LOGIN_AWARD)
	public void _listener_LINE_LOGIN_AWARD(Param param) {
		HumanObject humanObj = param.get("humanObject");
		Human human = humanObj.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestNormalJSON());
		for(QuestVO vo : voList) {
			if (vo.type != QuestTypeKey.QUEST_TYPE_11) continue;
			
			ConfQuest quest = ConfQuest.get(vo.sn);
			Activity activity = humanObj.dataPers.activity;
			JSONObject lineLoginJson = Utils.str2JSONObject(activity.getLineLoginJson());
			vo.nowProgress = lineLoginJson.getIntValue("days");
			if (vo.nowProgress >= quest.target[0]) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
			sendMsg(humanObj, vo);
		}
		human.setQuestNormalJSON(QuestVO.listToJson(voList));
	}
}