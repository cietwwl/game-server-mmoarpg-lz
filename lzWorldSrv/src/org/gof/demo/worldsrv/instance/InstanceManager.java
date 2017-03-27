package org.gof.demo.worldsrv.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.MonsterObject;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.config.ConfRep;
import org.gof.demo.worldsrv.config.ConfRepChapter;
import org.gof.demo.worldsrv.config.ConfRepLottery;
import org.gof.demo.worldsrv.config.ConfRepQuest;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.InstanceChapter;
import org.gof.demo.worldsrv.entity.InstanceRank;
import org.gof.demo.worldsrv.general.GeneralManager;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DChapterInfo;
import org.gof.demo.worldsrv.msg.Msg.DInstance;
import org.gof.demo.worldsrv.msg.Msg.SCAllChapter;
import org.gof.demo.worldsrv.msg.Msg.SCBoxAward;
import org.gof.demo.worldsrv.msg.Msg.SCDieStage;
import org.gof.demo.worldsrv.msg.Msg.SCInstanceAuto;
import org.gof.demo.worldsrv.msg.Msg.SCInstanceEnd;
import org.gof.demo.worldsrv.msg.Msg.SCInstanceLottery;
import org.gof.demo.worldsrv.msg.Msg.SCUpdateChapter;
import org.gof.demo.worldsrv.produce.ProduceManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.quest.QuestStatusKey;
import org.gof.demo.worldsrv.quest.QuestVO;
import org.gof.demo.worldsrv.rank.RankGlobalServiceProxy;
import org.gof.demo.worldsrv.rank.RankKey;
import org.gof.demo.worldsrv.stage.StageGlobalServiceProxy;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class InstanceManager extends ManagerBase {
	
	/** 章节宝箱领取*/
	public static final int CHAPTER_BOX_未领取 = 1;					//未领取
	public static final int CHAPTER_BOX_已领取 = 2;					//已领取
	/** 任务完成情况*/
	public static final int INSTANCE_STAR_未完成 = 1;				//未完成
	public static final int INSTANCE_STAR_已完成 = 2;				//已完成
	
	/**o
	 * 获取实例
	 * @return
	 */
	public static InstanceManager inst() {
		return inst(InstanceManager.class);
	}
	
	/**
	 * 创建副本
	 * @param humanObj
	 * @param confRep
	 */
	public void create(HumanObject humanObj, ConfRep confRep) {
		//判断是否满足副本进入条件
		if(!canEnterRep(humanObj, confRep, 1)) {
			return;
		}
		int mapSn = confRep.mapSn;
		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		proxy.createInstance(mapSn, confRep.sn);
		proxy.listenResult(this::_result_create, "humanObj", humanObj, "mapSn", mapSn);
	}
	/**
	 * 副本创建完毕回调
	 * @param timeout
	 * @param results
	 * @param context
	 */
	public void _result_create(Param results, Param context) {
		//创建完毕，用户切换地图
		long stageTargetId = results.get("stageId");
		HumanObject humanObj = context.get("humanObj");
		int mapSn = context.get("mapSn");
		
		Vector2D posAppear = StageManager.inst().getBirthPosFromMapSn(mapSn);
		StageManager.inst().switchTo(humanObj, stageTargetId, posAppear);
	}
	
	/**
	 * 离开副本 自动回到副本进入前的主地图
	 * @param humanObj
	 */
	public void leave(HumanObject humanObj) {
//		//切换地图
		StageObejctInstance stageObj = (StageObejctInstance)humanObj.stageObj;
		if(stageObj == null) {
			return;
		}
		
		StageManager.inst().quitToCommon(humanObj);
	}
	
	/**
	 * 是否可以进入副本
	 * @param humanObj
	 * @param confRep
	 * @param num
	 */
	public boolean canEnterRep(HumanObject humanObj, ConfRep confRep, int num) {
		if(humanObj == null || confRep == null) {
			Inform.user(humanObj.id, Inform.提示错误, "null");
			return false;
		}
		
		Human human = humanObj.getHuman();
		
		//判断是否在common地图中
		if(!humanObj.stageObj.conf.type.equals(StageMapTypeKey.common.getContent())) {
			Inform.user(humanObj.id, Inform.提示错误, "地图错误");
			Log.instance.info("{}进入副本错误{}, {}!", humanObj.id, humanObj.stageObj.conf.type, StageMapTypeKey.common.getContent());
			return false;
		}
		
		//获得当前的章节配置
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(confRep.chapterID);
		if(chapter == null) {
			Inform.user(humanObj.id, Inform.提示错误, "章节错误");
			Log.instance.info("{}进入章节错误{}!", humanObj.id, chapter);
			return false;
		}
		
		// 判断这个副本当前是否可以攻击		
		// 如果不是第一章节的第一个副本，需要检查上一个副本是否通过
		if (confRep.chapterID != 1 && confRep.preRepID == 0 && !isPassAll(humanObj, confRep.chapterID-1)) {
			Inform.user(humanObj.id, Inform.提示操作, "上一章节没全部通过");
			return false;
		}
		
		JSONObject starJson = Utils.str2JSONObject(chapter.getRepStarJson());
		// 如果以前打过 star>0 或者等级达到并且上一个副本得到星星
		if (confRep.preRepID != 0) {
			int preStars = getStarNum(humanObj, confRep.preRepID, starJson);
			if(preStars == 0) {
				Inform.user(humanObj.id, Inform.提示操作, "上一副本没有通过");
				return false;
			} 
		}
		
		//判断活力因素
		if(human.getActValue() < confRep.cost * num) {
			Inform.user(humanObj.id, Inform.提示操作, "体力不足");
			return false;
		}
		
		//判断等级因素
		if(human.getLevel() < confRep.level) {
			Inform.user(humanObj.id, Inform.提示操作, "等级不够");
			return false;
		}
		
		//判断副本攻击次数
		JSONObject attNumJson = Utils.str2JSONObject(chapter.getRepAttNumJson());
		// 总数 - 已经攻击的次数 - 要攻击次数
		int times = confRep.attNum - getRepAttCount(attNumJson, confRep.sn) - num;
		if(times < 0) {
			Inform.user(humanObj.id, Inform.提示操作, "剩余次数不足");
			return false;
		}
		return true;
	}
	
	/**
	 * 从json中获得 对应 副本的攻击次数
	 * @param ja human.getRepProgress() 
	 * @param repSn
	 * @return
	 */
	private int getRepAttCount(JSONObject ja, int repSn) {
		int result = 0;
		for (String key : ja.keySet()) {
			if(repSn == Utils.intValue(key)) {
				result += ja.getIntValue(key);
			}
		}
		return result;
	}
	
	/**
	 * 是否章节的每一个副本都有星星
	 * @param humanObj
	 */
	private boolean isPassAll(HumanObject humanObj, int chapterID) {
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(chapterID);
		JSONObject repStarJson = Utils.str2JSONObject(chapter.getRepStarJson());
		int count = 0;
		for (String key : repStarJson.keySet()) {
			JSONArray arr = repStarJson.getJSONArray(key);
			for (int i = 0; i < 3; i++) {
				if (arr.getIntValue(i) == INSTANCE_STAR_已完成) {
					count++;
					break;
				}
			}
		}
		
		if (count == 10) {
			return true;
		}
		return false;
	}
	
//	/**
//	 * 玩家自己打副本结束调用的接口
//	 * @param humanObj
//	 */
//	public void repEnd(HumanObject humanObj) {
//		repEnd(humanObj, 1, -1);
//	}
	
	/**
	 * 副本结束给副本奖励相关
	 * @param humanObj
	 */
	public void repEnd(HumanObject humanObj) {
		//完成副本战斗
		SCInstanceEnd.Builder endMsg = SCInstanceEnd.newBuilder();
		Human human = humanObj.getHuman();
		if(!(humanObj.stageObj instanceof StageObejctInstance)) {
			Inform.user(humanObj.id, Inform.提示错误, "玩家不在副本中");
			return;
		}
		StageObejctInstance stageObjIns = (StageObejctInstance)(humanObj.stageObj);
		ConfRep confRep = stageObjIns.repCfg;
		
		int starsNew = 0;
		//修改副本完成进度
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(confRep.chapterID);
		JSONObject starJson = Utils.str2JSONObject(chapter.getRepStarJson());
		int starsOld = getStarNum(humanObj, confRep.sn, starJson);
		int[] stars = new int[3];           //副本星星 1-未完成， 2-完成
		// 通过完成的任务计算星星
		for (int i = 0; i < stageObjIns.vo.length; i++) {
			if (stageObjIns.vo[i].status == QuestStatusKey.QUEST_STATUS_已完成) {
				starsNew++;
				// 返回时候要用
				stars[i] = INSTANCE_STAR_已完成;
			} else {
				stars[i] = INSTANCE_STAR_未完成;
			}
		}
		boolean updateStar = false;
		// 根据任务完成条件给星星
		if(starsOld < starsNew) {
			int delta = starsNew - starsOld;
			updateStar = true;
			// 加入排行榜
			RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
			InstanceRank rank = new InstanceRank();
			rank.setId(humanObj.id);
			rank.setHumanId(humanObj.id);
			rank.setCharacterSn(humanObj.getHuman().getSn());
			rank.setHumanLevel(human.getLevel());
			rank.setHumanName(human.getName());
			rank.setRankTime(Port.getTime());
			// 原星星数
			int totalStars = getAllStar(humanObj);
			rank.setStars(totalStars+delta);
			
			proxy.addNew(RankKey.InstanceRank, rank);
		}
		if (updateStar) {
			starJson.put(String.valueOf(confRep.sn), Utils.toJSONString(stars));
		}
		
		chapter.setRepStarJson(Utils.toJSONString(starJson));
		for (int star : stars) {
			endMsg.addStars(star);
		}
		
		// 抽牌的产出物
		ConfRepLottery confLottery = ConfRepLottery.get(stageObjIns.repCfg.lottery);
		stageObjIns.lotteryProduce = ProduceManager.inst().produceItem(humanObj, confLottery.produce);
		for (ProduceVo vo : stageObjIns.lotteryProduce) {
			endMsg.addLotteryProduces(vo.toDProduce());
		}
		
		int times = 1;
		//监听人物属性变化
		HumanInfoChange.listen(humanObj);
			
		//修改副本的次数
		JSONObject attNumJson = Utils.str2JSONObject(chapter.getRepAttNumJson());
		times = attNumJson.getIntValue(String.valueOf(stageObjIns.repSn));
		times++;
		attNumJson.put(String.valueOf(stageObjIns.repSn), times);
		chapter.setRepAttNumJson(Utils.toJSONString(attNumJson));
		
		//扣除活力 
		HumanManager.inst().produceMoneyReduce(humanObj, ProduceMoneyKey.actValue, confRep.cost, MoneyReduceLogKey.剧情副本);
		// 加金币，经验
		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.coin, confRep.income, MoneyAddLogKey.副本);
		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.expCur, confRep.humanExp, MoneyAddLogKey.副本);
		//给东西
			
		//资源包的奖励
		//发放奖励 使用物品产出系统
		List<ProduceVo> itemProduce = humanObj.stageObj.itemProduce;
		//实际给物品
		ProduceManager.inst().giveProduceItem(humanObj, itemProduce, MoneyAddLogKey.副本);
		for (ProduceVo produceVo : itemProduce) {
			endMsg.addProduces(produceVo.toDProduce());
		}
		
		StageManager.inst().stageStarEndCheck(humanObj.stageObj);
		
		
		int star = StageManager.inst().caculateStageStar(humanObj.stageObj);
		
		GeneralManager.inst().recoverHp(humanObj);
		endMsg.setInstSn(confRep.sn);
		endMsg.setHumanExp(confRep.humanExp);
		endMsg.setGeneralExp(confRep.generalExp);
		endMsg.setIncome(confRep.income);
		humanObj.sendMsg(endMsg);
		// 更新当前章节
		updateChapter(humanObj, confRep.chapterID);
		// 是否更新下一章节
		if (isPassAll(humanObj, confRep.chapterID) && humanObj.getHuman().getLevel() >= ConfRepChapter.get(confRep.chapterID+1).openLevel) {
			updateChapter(humanObj, confRep.chapterID+1);
		}
		Event.fire(EventKey.INSTANCE_PASS, "humanObj", humanObj, "times", times, "repSn", stageObjIns.repSn, "repType", confRep.repType, "numAtt", 1);
	}
	
	/**
	 * 获得单个副本信息
	 * @param humanObj
	 */
	public DInstance.Builder getInstanceInfo(HumanObject humanObj, int chapterID, int instID) {
		DInstance.Builder dIns = DInstance.newBuilder();
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(chapterID);
		dIns.setInstSn(instID);
		JSONObject attNumJson = Utils.str2JSONObject(chapter.getRepAttNumJson());
		dIns.setAttNum(attNumJson.getIntValue(String.valueOf(instID)));
		JSONObject resetJson = Utils.str2JSONObject(chapter.getRepResetJson());
		dIns.setReset(resetJson.getIntValue(String.valueOf(instID)));
		JSONObject starJson = Utils.str2JSONObject(chapter.getRepStarJson());
		JSONArray arr = starJson.getJSONArray(String.valueOf(instID));
		
		for (int i = 0; i < arr.size(); i++) {
			dIns.addStars(arr.getIntValue(i));
		}
		return dIns;
	}
	
	/**
	 * 获得当前章节的副本全部信息
	 * @param humanObj
	 */
	public DChapterInfo.Builder getChapterInfo(HumanObject humanObj, int chapterID) {
		DChapterInfo.Builder dChapter = DChapterInfo.newBuilder();
		//获得当前的章节配置
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(chapterID);
		
		//获得章节相关副本
		int[] repSns = ConfRepChapter.get(chapterID).repSns;
		
		//遍历所有
		for (int repSn : repSns) {
			if (repSn == 0) {
				continue;
			}
			DInstance.Builder dIns = getInstanceInfo(humanObj, chapterID, repSn);
			dChapter.addInstance(dIns);
		}
		dChapter.setChapterId(chapterID);
		JSONArray arr = Utils.toJSONArray(chapter.getChapterBoxJson());
		for (int i = 0; i < 3; i++) {
			dChapter.addBoxState(arr.getIntValue(i));
		}
		return dChapter;
	}
	
	/**
	 * 获得已经开启的全部章节
	 * @param humanObj
	 */
	public void getAllChapter(HumanObject humanObj) {
		SCAllChapter.Builder msg = SCAllChapter.newBuilder();
		if (humanObj.dataPers.instanceChapters.size() == 0) {
			List<ConfRepChapter> confList = new ArrayList<ConfRepChapter>();
			confList.addAll(ConfRepChapter.findAll());
			for (ConfRepChapter conf : confList) {
//				if (humanObj.getHuman().getLevel() >= conf.openLevel) {
					newInst(humanObj, conf.sn);
//				}
			}
		}
		for (int chapterID : humanObj.dataPers.instanceChapters.keySet()) {
			DChapterInfo.Builder dChapter = getChapterInfo(humanObj, chapterID);
			msg.addInfos(dChapter);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 更新某一章节
	 * @param humanObj
	 */
	public void updateChapter(HumanObject humanObj, int chapterID) {
		SCUpdateChapter.Builder msg = SCUpdateChapter.newBuilder();
		DChapterInfo.Builder dChapter = getChapterInfo(humanObj, chapterID);
		msg.setInfo(dChapter);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 副本扫荡
	 * @param humanObj
	 * @param instSn
	 * @param num
	 */
	public void instanceAuto(HumanObject humanObj, int instSn, int num){
		//获得副本配置
		ConfRep confRep = ConfRep.get(instSn);
		//判断能不能打
		if(!canEnterRep(humanObj, confRep, num)) {
			return;
		}
		
		SCInstanceAuto.Builder endMsg = SCInstanceAuto.newBuilder();
		int times = 1;
		//监听人物属性变化
		HumanInfoChange.listen(humanObj);
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(confRep.chapterID);
			
		//修改副本的次数
		JSONObject attNumJson = Utils.str2JSONObject(chapter.getRepAttNumJson());
		times = attNumJson.getIntValue(String.valueOf(instSn));
		times += num;
		attNumJson.put(String.valueOf(instSn), times);
		chapter.setRepAttNumJson(Utils.toJSONString(attNumJson));
		
		//扣除活力 
		HumanManager.inst().produceMoneyReduce(humanObj, ProduceMoneyKey.actValue, confRep.cost * num, MoneyReduceLogKey.剧情副本);
		// 加金币，经验
		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.coin, confRep.income, MoneyAddLogKey.副本);
		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.expCur, confRep.humanExp, MoneyAddLogKey.副本);
		//给东西
		for(int i = 0 ; i < num ; i++) {
			List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
			//资源包的奖励
			//发放奖励 使用物品产出系统
			itemProduce = ProduceManager.inst().produceItem(humanObj, confRep.produceAuto);
			//副本固定物品奖励
			itemProduce.addAll(ProduceManager.inst().produceItem(confRep.produceType, confRep.produceNum));
			
			//实际给物品
			ProduceManager.inst().giveProduceItem(humanObj, itemProduce, MoneyAddLogKey.副本);
			
			for (ProduceVo produceVo : itemProduce) {
				endMsg.addProduces(produceVo.toDProduce());
			}
		}
		
		GeneralManager.inst().recoverHp(humanObj);
		endMsg.setHumanExp(confRep.humanExp);
		endMsg.setIncome(confRep.income);
		humanObj.sendMsg(endMsg);
		// 更新当前章节
		updateChapter(humanObj, confRep.chapterID);
		// 是否更新下一章节
		Event.fire(EventKey.INSTANCE_PASS, "humanObj", humanObj, "times", times, "repSn", instSn, "repType", confRep.repType, "numAtt", num);
	}
	
	/**
	 * 副本开宝箱
	 * @param humanObj
	 * @param chapter
	 * @param index
	 */
	public void instanceBoxAward(HumanObject humanObj, int chapterID, int index){
		//判断章节配置 
		ConfRepChapter confChap = ConfRepChapter.get(chapterID);
		if(confChap == null) {
			Inform.user(humanObj.id, Inform.提示错误, "null");
			return;
		}
		
		//判断index 是否超过索引
		if(confChap.chapterStar == null || index > confChap.chapterStar.length ) {
			Inform.user(humanObj.id, Inform.提示操作, "箱子错误");
			return;
		}
		
		//获得当前的章节配置
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(chapterID);
		if(chapter == null) {
			Inform.user(humanObj.id, Inform.提示错误, "章节错误");
			return;
		}
		
		//获得星星的数量
		int star = getChapterStar(humanObj, chapterID);
		//获得星星的数量
		//判断是否已经领取过了
		int[] chapterBox = Utils.arrayStrToInt(chapter.getChapterBoxJson());
		if(chapterBox[index] == CHAPTER_BOX_已领取) {
			Inform.user(humanObj.id, Inform.提示操作, "箱子已经领取过");
			return;
		}
		
		//判断星星够不够
		if(star < confChap.chapterStar[index]) {
			Inform.user(humanObj.id, Inform.提示操作, "要求的星星不足");
			return;
		}
		
		//获得物品
		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
		itemProduce = ProduceManager.inst().produceItem(humanObj, confChap.chapterStarProduce[index]);
	
		//判断是否可以给
		ReasonResult rr = ProduceManager.inst().canGiveProduceItem(humanObj, itemProduce);
		if(!rr.success) {
			Inform.user(humanObj.id, Inform.提示操作, rr.reason);
			return;
		}
		
		//实际给物品
		ProduceManager.inst().giveProduceItem(humanObj, itemProduce, MoneyAddLogKey.副本宝箱);
		
		//设置 数据库记录
		chapterBox[index] = CHAPTER_BOX_已领取;
		String chapterBoxJson = Utils.toJSONString(chapterBox);
		chapter.setChapterBoxJson(chapterBoxJson);
		
		//发送消息
		SCBoxAward.Builder msg = SCBoxAward.newBuilder();
		msg.setCode(1);
		msg.setChapterId(chapterID);
		msg.setIndex(index);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 获得玩家一章星星数量
	 * @param humanObj
	 */
	public int getChapterStar(HumanObject humanObj, int chapterID) {
		//获得星星的数量
		int star = 0;
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(chapterID);
		
		JSONObject starJson = Utils.str2JSONObject(chapter.getRepStarJson());
		for (String key : starJson.keySet()) {
			JSONArray arr = starJson.getJSONArray(key);
			for (int i = 0; i < 3; i++) {
				if (arr.getIntValue(i) == INSTANCE_STAR_已完成) {
					star++;
				}
			}
		}
		return star;
	}
	
	/**
	 * 获得总星星数量
	 * @param humanObj
	 */
	public int getAllStar(HumanObject humanObj) {
		//获得星星的数量
		int star = 0;
		for(InstanceChapter chapter : humanObj.dataPers.instanceChapters.values()) {
			star += getChapterStar(humanObj, chapter.getChapterSn());
		}
		return star;
	}

	/**
	 * 获得一个副本的星数
	 * @param humanObj
	 * @param confItem
	 * @return
	 */
	public int getStarNum(HumanObject humanObj, int repSn, JSONObject starJson) {
		int stars = 0;
		JSONArray arr = starJson.getJSONArray(String.valueOf(repSn));
		for (int i = 0; i < 3; i++) {
			if (arr.getIntValue(i) == INSTANCE_STAR_已完成) {
				stars++;
			}
		}
		return stars;
	}
	
	/**
	 * 创建一个 chapter 到HumanObject 以及数据库
	 * @param humanObj
	 * @param chapterID
	 * @return
	 */
	private InstanceChapter newInst(HumanObject humanObj, int chapterID) {
		InstanceChapter chapter = new InstanceChapter();
		chapter.setId(Port.applyId());
		chapter.setHumanId(humanObj.id);
		chapter.setChapterSn(chapterID);
		int[] chapterBox = {CHAPTER_BOX_未领取, CHAPTER_BOX_未领取, CHAPTER_BOX_未领取};
		chapter.setChapterBoxJson(Utils.toJSONString(chapterBox));
		// 初始化数据结构
		Map<String, List<Integer>> repStar = new HashMap<>();
		Map<String, Integer> repAttNum = new HashMap<>();
		Map<String, Integer> repReset = new HashMap<>();
		List<Integer> stars = new ArrayList<Integer>(){{
			add(INSTANCE_STAR_未完成);
			add(INSTANCE_STAR_未完成);
			add(INSTANCE_STAR_未完成);
		}};
		
		ConfRepChapter confChapter = ConfRepChapter.get(chapterID);
		for (int repSn : confChapter.repSns) {
			// 客户端说普通本后跟一个精英本，没有则填0
			if (repSn == 0) {
				continue;
			}
			repStar.put(String.valueOf(repSn), stars);
			ConfRep confRep = ConfRep.get(repSn);
			if (confRep.repType == 1) {
				// 普通本没有攻击次数和重置次数
				repAttNum.put(String.valueOf(repSn), -1);
				repReset.put(String.valueOf(repSn), -1);
			} else {
				repAttNum.put(String.valueOf(repSn), 0);
				repReset.put(String.valueOf(repSn), 0);
			}
		}
		chapter.setRepStarJson(Utils.toJSONString(repStar));
		chapter.setRepAttNumJson(Utils.toJSONString(repAttNum));
		chapter.setRepResetJson(Utils.toJSONString(repReset));
		chapter.persist();
		humanObj.dataPers.instanceChapters.put(chapterID, chapter);
		return chapter;
	}
	
	@Listener(EventKey.HUMAN_STAGE_ENTER)
	public void onHumanStageEnter(Param params) {
		HumanObject humanObj = params.get("humanObj");

		if(!(humanObj.stageObj instanceof StageObejctInstance)) {
			return;
		} 
		
		StageObejctInstance stageObj = (StageObejctInstance)humanObj.stageObj;
		
		//出战的武将 排位 以及 显示出来
		GeneralManager.inst().showAll(humanObj);
				
		if(!stageObj.start) {
			stageObj.start = true;
			
			//判断是否是首次
			ConfRep confRep = stageObj.repCfg;
			InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(confRep.chapterID);
			JSONObject repStarJson = Utils.str2JSONObject(chapter.getRepStarJson());
			int star = getStarNum(humanObj, confRep.sn, repStarJson);
			if(star<0 && stageObj.repCfg.produceFirst.length > 0) {
				//资源包的奖励
				//发放奖励 使用物品产出系统
				stageObj.itemProduce = ProduceManager.inst().produceItem(humanObj, stageObj.repCfg.produceFirst);
			} else {
				stageObj.itemProduce = ProduceManager.inst().produceItem(humanObj, stageObj.repCfg.produce);
			}
			//副本固定物品奖励
			stageObj.itemProduce.addAll(ProduceManager.inst().produceItem(stageObj.repCfg.produceType, stageObj.repCfg.produceNum));
			
		}
	}
	
	@Listener(EventKey.HUMAN_RESET_ZERO)
	public void onHumanResetZero(Param param) {
		HumanObject humanObj = param.get("humanObj");
		
		//清除每日添加的星星 清除每日攻击次数
		for (InstanceChapter chapter : humanObj.dataPers.instanceChapters.values()) {
			ConfRepChapter confChapter = ConfRepChapter.get(chapter.getChapterSn());
			JSONObject attNumJson = Utils.str2JSONObject(chapter.getRepAttNumJson());
			JSONObject resetJson = Utils.str2JSONObject(chapter.getRepResetJson());
			for (int repSn : confChapter.repSns) {
				// 客户端说普通本后跟一个精英本，没有则填0
				if (repSn == 0) {
					continue;
				}
				ConfRep confRep = ConfRep.get(repSn);
				if (confRep.repType == 1) {
					// 普通本没有攻击次数和重置次数
					attNumJson.put(String.valueOf(repSn), -1);
					resetJson.put(String.valueOf(repSn), -1);
				} else {
					attNumJson.put(String.valueOf(repSn), 0);
					resetJson.put(String.valueOf(repSn), 0);
				}
			}
			chapter.setRepAttNumJson(Utils.toJSONString(attNumJson));
			chapter.setRepResetJson(Utils.toJSONString(resetJson));
//			chapter.persist();
		}
		
	}
	
	/**
	 * 抽奖
	 * @param humanObj
	 */
	public void instanceLottery(HumanObject humanObj) {
		SCInstanceLottery.Builder msg = SCInstanceLottery.newBuilder();
		StageObejctInstance stageObj = (StageObejctInstance)humanObj.stageObj;
		// 判断抽奖花费够不够
		ConfRep confRep = stageObj.repCfg;
		ConfRepLottery confRepLottery = ConfRepLottery.get(confRep.lottery);
		int type = confRepLottery.costType[stageObj.lotteryTimes];
		int num = confRepLottery.costNum[stageObj.lotteryTimes];
		ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, type, num);
		if (!result.success) {
			return;
		}
		HumanManager.inst().produceMoneyReduce(humanObj, type, num, MoneyReduceLogKey.抽奖);
		List<ProduceVo> voList = stageObj.lotteryProduce;
		if (voList.size() > 0) {
			ProduceVo vo = voList.remove(voList.size()-1);
			msg.setProduce(vo.toDProduce());
			humanObj.sendMsg(msg);
			//实际给物品
			voList.clear();
			voList.add(vo);
			ProduceManager.inst().giveProduceItem(humanObj, voList, MoneyAddLogKey.副本);
		}
		stageObj.lotteryTimes++;
	}
	
	/**
	 * 死亡触发
	 * @param param
	 */
	@Listener(EventKey.HUMAN_BE_KILLED)
	public void _listener_HUMAN_BE_KILLED(Param param) {
		HumanObject humanObj = param.get("dead");
		int stageSn = param.get();
		
		String type = ConfMap.get(stageSn).type;
		if (type.equals(StageMapTypeKey.rep)) {
			SCDieStage.Builder msg = SCDieStage.newBuilder();
			msg.setStageType(type);
			humanObj.sendMsg(msg);
		}
	}
	
	/**
	 * 任务类型1：血量判断类型（击杀BOSS）事件监听
	 * @param param
	 */
	@Listener(EventKey.MONSTER_BE_KILLED)
	public void _listener_MONSTER_BE_KILLED(Param param) {
		UnitObject unitObj = param.get("killer");
		MonsterObject monsterObj = param.get("dead");
		
		HumanObject humanObj = null;
		
		//判断类型是主角还是武将
		if(unitObj instanceof HumanObject)
			humanObj = (HumanObject) unitObj;
		if(unitObj instanceof GeneralObject)
			humanObj =  ((GeneralObject) unitObj).getHumanObj();
		else 
			return;
		
		StageObejctInstance stageObj = (StageObejctInstance)humanObj.stageObj;
		for(QuestVO vo : stageObj.vo) {
			if (vo.type != 1) continue;
			
			if (Integer.parseInt(monsterObj.sn) == ConfRepQuest.get(vo.sn).target[1]) {
				vo.status = QuestStatusKey.QUEST_STATUS_已完成;
				vo.nowProgress = 1;
			} else {
				vo.status = QuestStatusKey.QUEST_STATUS_进行中;
			}
		}
	}
}
