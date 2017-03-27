package org.gof.demo.worldsrv.quest;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.MonsterObject;
import org.gof.demo.worldsrv.config.ConfQuestInstDaily;
import org.gof.demo.worldsrv.config.ConfQuestInstGroup;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemVO;
import org.gof.demo.worldsrv.mail.MailManager;
import org.gof.demo.worldsrv.msg.Msg.SCQuestInstDailyCount;
import org.gof.demo.worldsrv.msg.Msg.SCQuestInstDailyInfo;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

/**
 * 每日日常·副本日常任务
 */
public class QuestInstDailyManager extends ManagerBase {
	private static final int QUEST_INST_DAILY_NUM = 3;
	private static final SecureRandom RAND = new SecureRandom();
	/**
	 * 获取实例
	 */
	public static QuestInstDailyManager inst() {
		return inst(QuestInstDailyManager.class);
	}
	
	/**
	 * 每日首次登陆  
	 * 刷新进入副本次数
	 * 初始化角色身上的每日副本任务
	 * @param humanObj
	 */
	@Listener({EventKey.HUMAN_RESET_ZERO, EventKey.HUMAN_FIRST_LOGIN})
	public void initQuestInstDailyCount(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		human.setQuestInstCount(ConfGlobalUtils.getValue(ConfGlobalKey.每日进副本任务次数));
		initQuestInstDailyAll(humanObj.getHuman());
	}

	/**
	 * 初始化3个副本任务
	 * @param human
	 */
	public void initQuestInstDailyAll(Human human) {
		List<QuestVO> voList = new ArrayList<QuestVO>();
		List<ConfQuestInstGroup> canAcceptGroup = new ArrayList<ConfQuestInstGroup>();
		//所有的任务组
		Collection<ConfQuestInstGroup> allConfList = ConfQuestInstGroup.findAll();
		//构建可接任务组
		for (ConfQuestInstGroup conf : allConfList) {
		if (checkCanAccept(human, conf)) {
			canAcceptGroup.add(conf);
			}
		}
		// 从任务组里随机3组，每组包含三个星级
		Collections.shuffle(canAcceptGroup);
		try {
				List<ConfQuestInstGroup> confList = canAcceptGroup.subList(0, QUEST_INST_DAILY_NUM);
				for (ConfQuestInstGroup groupConf : confList) {
				// 每组中选一个
				int length = groupConf.questIds.length;
				int index = RAND.nextInt(length);
				int questId = groupConf.questIds[index];
				// 初始化任务
				ConfQuestInstDaily conf = ConfQuestInstDaily.get(questId);
				QuestVO vo = initQuestInstDaily(human, conf);
				if(vo != null){
					voList.add(vo);
				}
			}
		} 
		catch (Exception e) {
			Log.quest.info("日常任务初始化失败: {},{}:{}",human.getId(),human.getName(),e.toString());
		}
		human.setQuestInstDailyJSON(QuestVO.listToJson(voList));
	}
		
	/**
	 * 检查能否接取新任务
	 * @param human
	 */
	public boolean checkCanAccept(Human human, ConfQuestInstGroup conf) {
        // 任务等级是否达到条件
		if (conf.level[0] > human.getLevel() || human.getLevel() > conf.level[1]) {
			return false;
		}
		return true;
 }
 
	/**
	 * 玩家接新任务
	 * @param human
	 */
	public QuestVO initQuestInstDaily(Human human, ConfQuestInstDaily quest) {
		if(quest == null){
			return null;
		}
		QuestVO vo = new QuestVO();
		vo.sn = quest.sn;
		vo.status = QuestStatusKey.QUEST_STATUS_进行中;
		vo.type = quest.type;
		vo.nowProgress = 0;
		
		switch (quest.type) {
		case QuestTypeKey.QUEST_INST_TYPE_1://任务类型1：血量判断类型（击杀BOSS）
			vo.targetProgress = quest.target[0];
			return vo;
			
		case QuestTypeKey.QUEST_INST_TYPE_2://任务类型2：全部击杀判断（击杀副本中的所有怪物）
			vo.targetProgress = quest.target.length;
		case QuestTypeKey.QUEST_INST_TYPE_3://任务类型3：触发指定某些机关（触发副本中所有机关）
			vo.targetProgress = quest.target.length;
			return vo;
			
		case QuestTypeKey.QUEST_INST_TYPE_4://任务类型4：生存
		case QuestTypeKey.QUEST_INST_TYPE_5://任务类型5：指定位置
		case QuestTypeKey.QUEST_INST_TYPE_6://任务类型6：NPC生存
		case QuestTypeKey.QUEST_INST_TYPE_7://任务类型7：怪物逃跑
			vo.targetProgress = 1;
			return vo;
		default:
			return null;
		}
	}
	
	/**
	 * 增加奖励
	 * @param questId
	 * @param humanObj
	 * @return
	 */
	public void addAward(int questId, HumanObject humanObj) {
		ConfQuestInstDaily confQuest = ConfQuestInstDaily.get(questId);
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
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("quest.awards.bymail"));
			sendAwardsByMail(humanObj, confQuest);
			Log.quest.info("{}副本任务完成获得奖励失败，背包空间不足!发往邮箱",humanObj.name);
			return;
		}
		// 进背包
		ItemBagManager.inst().add(humanObj, itemVOs);
	}
	private void sendAwardsByMail(HumanObject humanObj, ConfQuestInstDaily confQuest) {
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
	 * 向前端发送任务信息
	 * @param humanObj
	 * @param human
	 */
	public void sendMsg(HumanObject humanObj, List<QuestVO> voList) {
		SCQuestInstDailyInfo.Builder msg = SCQuestInstDailyInfo.newBuilder();
		//推送前端消息体
		for (QuestVO vo : voList) {
			msg.addQuest(vo.createMsg());
		}
		humanObj.sendMsg(msg);
	}
	/**	 * 每次打开界面初始化
	 * @param humanObj
	 */
	public  void openQuestInstDaily(HumanObject humanObj) {
		Human human = humanObj.getHuman();
 		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestInstDailyJSON());
		if(voList.size() < QUEST_INST_DAILY_NUM){
			initQuestInstDailyAll(humanObj.getHuman());
			voList = QuestVO.jsonToList(human.getQuestInstDailyJSON());
		}
		human.setQuestInstDailyJSON(QuestVO.listToJson(voList));
		
		SCQuestInstDailyCount.Builder msg = SCQuestInstDailyCount.newBuilder();
		msg.setCount(humanObj.getHuman().getQuestInstCount());
		humanObj.sendMsg(msg);
		
		//推送前端消息体
		sendMsg(humanObj, voList);
	}
	
	/**
	 * 提交每日任务
	 * @param sn
	 * @param humanObj
	 */
	public void commitQuestInstDaily(HumanObject humanObj, int questId) {
		Human human = humanObj.getHuman();
		
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestInstDailyJSON());
		Iterator<QuestVO> iter = voList.iterator();
		while(iter.hasNext()) {
			QuestVO vo = iter.next();
			if (vo.sn == questId) {
				if (vo.status != QuestStatusKey.QUEST_STATUS_已完成) {
					Log.quest.info("任务状态不正确: {}",vo.status);
					return;
				}
				// 增加奖励
				addAward(questId, humanObj);
			}
		}
		voList.clear();
		// 刷3个新任务
		initQuestInstDailyAll(human);
		human.setQuestInstCount(human.getQuestInstCount()-1);
		human.setQuestInstDailyJSON(QuestVO.listToJson(voList));
		SCQuestInstDailyCount.Builder msg = SCQuestInstDailyCount.newBuilder();
		msg.setCount(human.getQuestInstCount());
		humanObj.sendMsg(msg);
		//推送前端消息体
		sendMsg(humanObj, voList);
	}
	
	/**
	 * 任务类型1：血量判断类型（击杀BOSS）事件监听
	 * 任务类型2：全部击杀判断（击杀副本中的所有怪物）
	 * 触发对①和②任务的判断
	 * 判断是否所在地图的某个怪物被杀，是否是指定副本的多少怪物被击杀
	 */
	@Listener(EventKey.MONSTER_BE_KILLED)
	public void _listener_MONSTER_BE_KILLED(Param param) {
		UnitObject unitObj = param.get("killer");
		MonsterObject monsterObj = param.get("dead");
		
		HumanObject humanObj = null;
		
		//判断被击杀者的类型
		if(unitObj instanceof HumanObject)
			humanObj = (HumanObject) unitObj;
		if(unitObj instanceof GeneralObject)
			humanObj =  ((GeneralObject) unitObj).getHumanObj();
		else 
			return;
		
		Human human = humanObj.getHuman();
		int reqId = Integer.parseInt(monsterObj.sn);
		List<QuestVO> voList = QuestVO.jsonToList(human.getQuestInstDailyJSON());
		try {
			for(QuestVO vo : voList) {
				ConfQuestInstDaily quest = ConfQuestInstDaily.get(vo.sn);
				if(quest == null)continue;
				if(quest.get(vo.sn).repId != reqId) continue;
				switch (vo.type) {
				case QuestTypeKey.QUEST_INST_TYPE_1:
					if (Integer.parseInt(monsterObj.sn) == quest.target[0]) {
						setQuestFinish(humanObj, vo);
					} else {
						vo.status = QuestStatusKey.QUEST_STATUS_进行中;
					}	
					break;
				case QuestTypeKey.QUEST_INST_TYPE_2:
					vo.nowProgress ++;
					if(vo.nowProgress >= vo.targetProgress){
						setQuestFinish(humanObj, vo);
					} else {
						vo.status = QuestStatusKey.QUEST_STATUS_进行中;
					}	
					break;
				default:
					continue;
				}
			}
		} catch (Exception e) {
			Log.quest.info("每日日常·血量判断类型（击杀BOSS）事件监听err: {}:{}",human.getName(),e.toString());
		}
		human.setQuestInstDailyJSON(QuestVO.listToJson(voList));
	}
	/**
	 * 任务类型4：生存
	 * @param param
	 * @date 2015年5月13日 下午6:55:50
	 */
	@Listener(EventKey.INSTANCE_PASS)
	public void _listener_INSTANCE_PASS(Param param) {
	}

	private void setQuestFinish(HumanObject humanObject, QuestVO vo) {
		vo.status = QuestStatusKey.QUEST_STATUS_已完成;
		vo.nowProgress = vo.targetProgress;
		generateNewQuests(humanObject);
	}

	private void generateNewQuests(HumanObject humanObject) {
		Human human = humanObject.getHuman();
		int count = human.getQuestInstCount();
		if(count > 0){
			count --;
			human.setQuestInstCount(count);
		}
		initQuestInstDailyAll(human);
	}
}