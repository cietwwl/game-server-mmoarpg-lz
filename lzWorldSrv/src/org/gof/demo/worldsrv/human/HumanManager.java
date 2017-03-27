package org.gof.demo.worldsrv.human;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.db.OrderBy;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.TickTimer;
import org.gof.core.support.Time;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.manager.StageBattleManager;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.UnitDataPersistance;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.battlesrv.support.PropKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.worldsrv.activity.ActivityManager;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfCharacterGrow;
import org.gof.demo.worldsrv.config.ConfCharacterHuman;
import org.gof.demo.worldsrv.config.ConfCharacterModel;
import org.gof.demo.worldsrv.config.ConfLevelExp;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.config.ConfParam;
import org.gof.demo.worldsrv.dailyliveness.LivenessManager;
import org.gof.demo.worldsrv.entity.Activity;
import org.gof.demo.worldsrv.entity.Buff;
import org.gof.demo.worldsrv.entity.Friend;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.InstanceChapter;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.entity.Mail;
import org.gof.demo.worldsrv.entity.Part;
import org.gof.demo.worldsrv.entity.PocketLine;
import org.gof.demo.worldsrv.entity.Tower;
import org.gof.demo.worldsrv.entity.UnitPropPlus;
import org.gof.demo.worldsrv.friend.FriendManager;
import org.gof.demo.worldsrv.general.GeneralFragManager;
import org.gof.demo.worldsrv.general.GeneralManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.instance.InstanceManager;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemBodyManager;
import org.gof.demo.worldsrv.item.ItemPack;
import org.gof.demo.worldsrv.mail.MailManager;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.DBag;
import org.gof.demo.worldsrv.msg.Msg.DHuman;
import org.gof.demo.worldsrv.msg.Msg.DItem;
import org.gof.demo.worldsrv.msg.Msg.DParts;
import org.gof.demo.worldsrv.msg.Msg.DProp;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.DTreasure;
import org.gof.demo.worldsrv.msg.Msg.DVector2;
import org.gof.demo.worldsrv.msg.Msg.SCChangeModel;
import org.gof.demo.worldsrv.msg.Msg.SCFightRevive;
import org.gof.demo.worldsrv.quest.QuestDailyManager;
import org.gof.demo.worldsrv.quest.QuestNormalManager;
import org.gof.demo.worldsrv.quest.QuestVO;
import org.gof.demo.worldsrv.shop.ShopManager;
import org.gof.demo.worldsrv.stage.StageGlobalServiceProxy;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.stage.StagePort;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.LoadHumanDataUtils;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;
import org.gof.demo.worldsrv.support.pathFinding.HeightFinding;
import org.gof.demo.worldsrv.tower.TowerManager;
import org.gof.demo.worldsrv.treasure.TreasureManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class HumanManager extends ManagerBase {
	// 玩家初始主地图
	public static int stageInitSn = 100001; //1
	public static int stageInitRepSn = 1; //1

	public static final int ONLINE_CHANGE_INTERVAL_SEC = 10; // 在线时间心跳间隔

	/**o
	 * 获取实例
	 * 
	 * @return
	 */
	public static HumanManager inst() {
		return inst(HumanManager.class);
	}

	/**
	 * 创建玩家
	 * 
	 * @param account
	 * @param name
	 * @param profession
	 * @param sex
	 * @return
	 */
	public Human create(long id, int serverId, String account, String name, int profession, int sex, String sn) {

		// 从配置中获得地图
		ConfCharacterHuman confHuman = ConfCharacterHuman.get(sn);
		ConfMap confMap = ConfMap.get(stageInitSn);
		Vector2D vecTemp = StageBattleManager.inst().randomPosInCircle(StageManager.inst().getBirthPosFromMapSn(stageInitSn), 0, 3);
		List<?> temp = Utils .ofList(confMap.sn, confMap.sn, vecTemp.x, vecTemp.y, confMap.type);
		List<?> stageInfo = Utils.ofList(temp);
		// 初始化玩家信息
		Human human = new Human();
		human.setId(id);
		human.setSn(confHuman.sn);
		human.setServerId(serverId);
		human.setAccount(account);
		human.setName(name);
		human.setProfession(profession);
		human.setSex(sex);
		human.setLevel(1);
		human.setVipLevel(0);
		human.setTimeCreate(System.currentTimeMillis());
		human.setTimeLogin(0L);
		human.setTimeLogout(0L);
		//给钱
		human.setCoin(10000);
		human.setGold(10000);
		human.setCompetMoney(0);
		human.setActValue(100);
		
		//背包大小
		human.setBag1Cap(ConfGlobalUtils.getValue(ConfGlobalKey.背包初始格数));
		human.setBag2Cap(ConfGlobalUtils.getValue(ConfGlobalKey.背包初始格数));

		// 设置地图信息
		human.setStageHistory(JSON.toJSONString(stageInfo));

		human.setSkill("[]");
		human.setModelSn(confHuman.modelSn);
	
		//初始化经验
		human.setExpCur(0);
		human.setExpUpgrade(ConfLevelExp.get(1).exp);
		
		//首次初始化技能
		human.setSkill(SkillManager.inst().firstInitSkills(confHuman.skillGroupSn));
		human.setSkillGroupSn(confHuman.skillGroupSn);
		
		//初始化第一个任务
		//QuestNormalManager.inst().initQuestNormal(human);
		//初始化副本日常任务
		//QuestInstDailyManager.inst().initQuestInstDailyAll(human);
		
		//初始化商店
		ShopManager.inst().initShop(human);
		
		// 持久化
		human.persist();
		
		/* 玩家属性加成 */
		UnitPropPlus pp = new UnitPropPlus();
		pp.setId(human.getId());
		
		//计算默认属性
		PropCalc basePropCalc = new PropCalc();
		List<ConfCharacterGrow> confList = ConfCharacterGrow.findBy(ConfCharacterGrow.K.profession, human.getProfession());
		for (ConfCharacterGrow conf : confList) {
			basePropCalc.plus(PropKey.valueOf(conf.prop), conf.base);
		}
		pp.setBase(basePropCalc.toJSONStr());
		
		// 等级属性加成
		PropCalc levelPropPlus = UnitManager.inst().getLevelProp(human.getProfession(), 1);
		pp.setLevel(levelPropPlus.toJSONStr());
		
		pp.persist();
		
		Event.fire(EventKey.HUMAN_CREATE, "human", human);
		
		return human;
	}


	/**
	 * 玩家登录游戏后 加载玩家的数据
	 */
	public void loadData(HumanObject humanObj) {
		// 获取当前请求编号
		long pid = Port.getCurrent().createReturnAsync();
		humanObj.loadingPID = pid;

		// 先将human主数据加载好
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.get(Human.tableName, humanObj.id);
		prx.listenResult(this::_result_loadHumanDataMain, "humanObj", humanObj);
	}
	public void _result_loadHumanDataMain(Param results, Param context) {
		// 玩家
		HumanObject humanObj = context.get("humanObj");
		UnitDataPersistance data = humanObj.dataPers;

		// 处理返回数据
		data.unit = new Human((Record) results.get());

		// 发布事件 让其他各个模块开始加载数据
		Event.fire(EventKey.HUMAN_DATA_LOAD_BEGIN, humanObj);
	}

	/**
	 * 玩家数据加载完毕后 登录游戏
	 * 
	 * @param humanObj
	 */
	private void login(HumanObject humanObj) {
		// 注册到地图
		StageObject stageObj = humanObj.stageObj;

		// 获取玩家在该地图的历史坐标
		Vector2D vector = humanObj.getStagePos(stageObj.LineId);
		Vector3D result = HeightFinding.posHeight(stageObj.sn, vector);
		// 没找到历史坐标或者是死亡状态，打回出生点
		if ((vector.x == -1 && vector.y == -1) || humanObj.isDie() || result.y <= 0) {
			// 常规地图位置
			if (humanObj.posNow.x == -1 && humanObj.posNow.y == -1) {
				Vector2D posNow = StageManager.inst().getBirthPosFromMapSn(stageObj.sn);
				humanObj.posNow = posNow;
			}
		} else {
			// 历史路径
			humanObj.posNow = vector;
		}

		// 将玩家注册进地图 暂不显示
		humanObj.stageRegister(stageObj);

		/* 初始化玩家信息 */
		Human human = humanObj.getHuman();

		// 计算人物属性
		UnitManager.inst().propCalc(humanObj);
		//武将的登陆
		GeneralManager.inst().login(humanObj);
		
		humanObj.name = human.getName();
		humanObj.modelSn = human.getModelSn();
		
		// 刷新进游戏，如果人物死亡，设为满血复活
		if (humanObj.isDie()) {
			human.setHpCur(human.getHpMax());
		}

		/* 发送玩家登录时间及修改玩家登录时间 */
		// 当前时间
		long timeNow = Port.getTime();
		
		// 上次最后登录时间
		long timeLast = human.getTimeLogin();
		
		// 
		if(timeLast == 0){
			Event.fire(EventKey.HUMAN_FIRST_LOGIN, "humanObj", humanObj);
		}

		// 设置登陆状态
		if (Utils.isSameDay(timeLast, timeNow)) {
			humanObj.loginStageState = 1;
		} else {
			humanObj.loginStageState = 2;
		} 

		//家族玩家登陆事件和代办任务事项
		onScheduleLogin(humanObj, timeNow);
		
		Event.fire(EventKey.HUMAN_LOGIN, "humanObj", humanObj, "timeLoginLast", timeLast);

		// 添加玩家全局信息
		StagePort stagePort = stageObj.getPort();
		HumanGlobalInfo hs = new HumanGlobalInfo();
		hs.id = humanObj.id;
		hs.modelSn = human.getSn();
		hs.account = human.getAccount();
		hs.name = human.getName();
		hs.nodeId = stagePort.getNodeId();
		hs.portId = stagePort.getId();
		hs.stageId = stageObj.id;
		hs.stageName = stageObj.name;
		hs.level = human.getLevel();
		hs.combat = human.getCombat();
		hs.sex = human.getSex();
		hs.profession = human.getProfession();
		hs.connPoint = humanObj.connPoint;
		hs.timeLogin = human.getTimeLogin();
		hs.sn = human.getSn();
		HumanGlobalServiceProxy prxShs = HumanGlobalServiceProxy.newInstance();
		prxShs.register(hs);

		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		// 玩家地图人数+1
		proxy.stageHumanNumAdd(humanObj.stageObj.id);
		
		stageHistoryAmend(humanObj);
		
		//重新设置玩家地图信息
		HumanManager.inst().recordStageId(humanObj, stageObj.id, stageObj.sn);
		
		//初始化伙伴碎片信息
		GeneralFragManager.inst().initFragInfo(humanObj);
		
		//初始化 mail
		MailManager.inst().initMailList(humanObj);
		
		//初始化第一个任务
		QuestNormalManager.inst().initQuestNormal(humanObj);
		//初始化日常任务
		QuestDailyManager.inst().initQuestDaily(humanObj);
		// 初始化副本信息
		InstanceManager.inst().getAllChapter(humanObj);
		// 初始化角色的每日活跃度信息，测试用
		LivenessManager.inst().initAllLiveness(humanObj);
		// 初始化爬塔信息
		TowerManager.inst().initTower(humanObj);
		// 初始化好友信息
		FriendManager.inst().initFriend(humanObj);
	}
	
	/**
	 * 重新设置玩家地图信息
	 * @param humanObj
	 * @param stageId
	 * @param stageSn
	 */
	public void recordStageId(HumanObject humanObj, long stageId, int stageSn) {
		Human human = humanObj.getHuman();
		JSONArray ja = JSON.parseArray(human.getStageHistory());
		for (int i = 0; i < ja.size(); i++) {
			JSONArray jaTemp = JSON.parseArray(ja.getString(i));
			if(jaTemp.getInteger(1) == stageSn) {
				jaTemp.set(0, stageId);
				ja.set(i, jaTemp);
				break;
			}
		}
		human.setStageHistory(JSON.toJSONString(ja));
	}

	/**
	 * 用于玩家等于时候处理没有处理的时间时间
	 * @param humanObj
	 * @param timeNow
	 */
	public void onScheduleLogin(HumanObject humanObj, long timeNow) {
		Human h = humanObj.getHuman();
		
		long timeLast = h.getTimeLogin();
		long timeZero = Utils.getTimeBeginOfToday(timeNow);
		//更新玩家登录时间
		h.setTimeLogin(timeNow);
		
		//防止出现切换地图多次更新
		if(timeLast < timeZero && timeZero <= timeNow) {
			Event.fire(EventKey.HUMAN_RESET_ZERO, "humanObj", humanObj, "timeLoginLast", timeLast);
		}
		
		//发送本日五时首次登录事件
		long timeFive = timeZero + 5 * Time.HOUR;
		if(timeLast < timeFive && timeFive <= timeNow) {
			Event.fire(EventKey.HUMAN_RESET_FIVE, "humanObj", humanObj, "timeLoginLast", timeLast);
		}
		
	}
	
	public void onScheduleEvent(HumanObject humanObj, int key, long timeNow) {
		Human h = humanObj.getHuman();
		h.setTimeLogin(timeNow);
		Event.fire(key, "humanObj", humanObj, "timeLoginLast", timeNow);
	}
	
	/**
	 * 发送登录初始化信息至客户端
	 */
	private void sendInitDataToClient(HumanObject humanObj) {
		// 玩家基础信息
		Human human = humanObj.getHuman();
		StageObject stageObj = humanObj.stageObj;
		// long curr = Port.getTime();

		// 玩家基本属性
		DProp.Builder dProp = DProp.newBuilder();
		for (PropKey propKey : PropKey.values()) {
			Object v = UnitManager.inst().getPropKeyValue(propKey, human);
			Utils.fieldWrite(dProp, propKey.name(), v);
		}

		// 基本信息
		DHuman.Builder dHuman = DHuman.newBuilder();
		dHuman.setProp(dProp);
		dHuman.setId(humanObj.id);
		dHuman.setName(human.getName());
		dHuman.setExpCur(human.getExpCur());
		dHuman.setLevel(human.getLevel());
		dHuman.setVipLevel(human.getVipLevel());
		dHuman.setSex(human.getSex());
		dHuman.setHpCur(human.getHpCur());
		dHuman.setHpMax(human.getHpMax());
		dHuman.setMpCur(human.getMpCur());
		dHuman.setMpMax(human.getMpMax());
		dHuman.setProfession(human.getProfession());
		dHuman.setTeamBundleID(humanObj.teamBundleID);
		dHuman.setActValue(human.getActValue());
		dHuman.setActValueMax(human.getActValueMax());
		dHuman.setCombat(human.getCombat());
		dHuman.setPropJson(humanObj.getPropPlus().toJSONStr());
		dHuman.setModelSn(human.getModelSn());
		dHuman.setSn(human.getSn());
		dHuman.setGold(human.getGold());
		dHuman.setCoin(human.getCoin());
		dHuman.setCompetMoney(human.getCompetMoney());
		List<Long> genlist = new ArrayList<Long>();
		for (Long genID : humanObj.slaves.keySet()) {
			genlist.add(genID);
		}
		dHuman.addAllGeneralList(genlist);

		// 地图信息
		Msg.DInitDataStage.Builder dStage = Msg.DInitDataStage.newBuilder();
		dStage.setPosNow(new Vector2D(humanObj.posNow.x, humanObj.posNow.y).toMsg());
		dStage.setId(stageObj.id);
		dStage.setSn(stageObj.sn);
		dStage.setInstanceSn(stageObj.getRepSn());
		dStage.setLineNum(stageObj.lineNum);

		List<DSkill> skills = SkillManager.inst().getSkills(humanObj);
		
		// 物品信息
		List<List<DItem>> bagInfo = ItemBagManager.inst().getBagInfo(humanObj);
		DBag.Builder dBag1 = DBag.newBuilder();
		dBag1.addAllItems(bagInfo.get(0));
		dBag1.setCapacity(humanObj.getHuman().getBag1Cap());
		DBag.Builder dBag2 = DBag.newBuilder();
		dBag2.addAllItems(bagInfo.get(1));
		dBag2.setCapacity(humanObj.getHuman().getBag2Cap());

		// 装备信息
		List<DParts> dParts = ItemBodyManager.inst().getEquipInfo(humanObj);
		dHuman.addAllParts(dParts);
		
		// 最终消息
		Msg.SCInitData.Builder msg = Msg.SCInitData.newBuilder();
		msg.setHuman(dHuman);
		msg.setStage(dStage);
		msg.addAllSkill(skills);
		msg.addDBag(dBag1);
		msg.addDBag(dBag2);

        //mail信息
		for(Mail m : humanObj.mailList){
			msg.addMails(MailManager.inst().builtDmailMsg(m));
		}
		// 宝藏数据
		List<DTreasure> dTreasure = TreasureManager.inst().getTreasureMsg(humanObj);
		msg.addAllTreasures(dTreasure);
		// 签到相关
		msg.setSignIn(ActivityManager.inst().getSignInInfo(humanObj));
		humanObj.sendMsg(msg);
	}

	/**
	 * 记录人物路径 只在切换地图的时候调用
	 * @param humanObj
	 * @param stageId
	 * @param stageSn
	 * @param stageType
	 * @param vectorOld
	 */
	public void recordStage(HumanObject humanObj, long stageId, int stageSn,
			String stageType, Vector2D vectorOld) {
		Human human = humanObj.getHuman();

		List<?> temp = Utils.ofList(stageId, stageSn, humanObj.posNow.x, humanObj.posNow.y,
				stageType);
		List<?> stageInfo = Utils.ofList(temp);

		if(stageType.equals(StageMapTypeKey.common.getContent())) {
			
			//如果地图的类型是common 那么直接全部重置
			human.setStageHistory(JSON.toJSONString(stageInfo));
		} else if(stageType.equals(StageMapTypeKey.rep.getContent())) {
			
			//如果地图的类型是副本那么把地图存在第1个位置
			JSONArray ja = JSON.parseArray(human.getStageHistory());
			
			//设置普通地图原来的坐标
			JSONArray jaTemp = JSON.parseArray(ja.getString(0));
			jaTemp.set(2, vectorOld.x);
			jaTemp.set(3, vectorOld.y);
			ja.set(0, jaTemp);
			
			ja.add(0, temp);
			human.setStageHistory(JSON.toJSONString(ja));
		}
		
		
	}
	
	/**
	 * 从地图历史信息中找到普通地图的坐标信息
	 * @param humanObj
	 * @return
	 */
	public Vector2D stageHistoryCommon(HumanObject humanObj) {
		Vector2D result = new Vector2D();
		
		Human human = humanObj.getHuman();
		JSONArray ja = JSON.parseArray(human.getStageHistory());
		Iterator<Object> iter = ja.iterator();
		//循环遍历查找地图
		while(iter.hasNext()) {
			Object next = iter.next();
			JSONArray jaNext = JSON.parseArray(next.toString());
			String stageType = jaNext.getString(4);
			
			if(stageType.equals(StageMapTypeKey.common.getContent())) {
				result.x = jaNext.getDoubleValue(2);
				result.y = jaNext.getDoubleValue(3);
				break;
			}
		}
		return result;
	}
	
	public int stageHistoryCommonSn(HumanObject humanObj) {
		int result = -1;
		
		Human human = humanObj.getHuman();
		JSONArray ja = JSON.parseArray(human.getStageHistory());
		Iterator<Object> iter = ja.iterator();
		//循环遍历查找地图
		while(iter.hasNext()) {
			Object next = iter.next();
			JSONArray jaNext = JSON.parseArray(next.toString());
			String stageType = jaNext.getString(4);
			
			if(stageType.equals(StageMapTypeKey.common.getContent())) {
				result = jaNext.getIntValue(1);
				break;
			}
		}
		return result;
	}
	
	/**
	 * 修复由于登陆的时候造成的一些地图问题
	 * @param humanObj
	 */
	public void stageHistoryAmend(HumanObject humanObj) {
//		long stageCurId = humanObj.stageObj.id;
		
		Human human = humanObj.getHuman();
		//删除所有不是common的
		JSONArray ja = JSON.parseArray(human.getStageHistory());
		Iterator<Object> iter = ja.iterator();
		//循环遍历查找上一张地图
		while(iter.hasNext()) {
			Object next = iter.next();
			JSONArray jaNext = JSON.parseArray(next.toString());
			String stageType = jaNext.getString(4);
			
			if(stageType.equals(StageMapTypeKey.common.getContent())) break;
			
			iter.remove();
		}
		human.setStageHistory(JSON.toJSONString(ja));
		
	}
	
	/**
	 * 获得正在出战的武将ID
	 * @param humanObj
	 * @return
	 */
	public List<Long> getAttGenIdList(HumanObject humanObj) {
		List<Long> result = new ArrayList<Long>();
		for (UnitObject uo : humanObj.salvesAttingList) {
			if(uo == null) {
				continue;
			}
			result.add(uo.id);
		}
		return result;
	}
	/**
	 * 复活
	 * 
	 * @param humanObj
	 * @param type  1回城复活 2原地复活
	 * @param system 系统复活，不消耗任何道具金钱
	 * @return
	 */
	public ReasonResult revive(HumanObject humanObj, int type, boolean system) {
		Human human = humanObj.getHuman();

		if (!humanObj.isDie()) {
			return new ReasonResult(false, "您当前未死亡不需要复活");
		}

//		int stageSn = humanObj.stageObj.sn;
		HumanInfoChange.listen(humanObj);

		if (type == 1) {
			Vector2D vector = humanObj.posNow;
			human.setHpCur(human.getHpMax());

			// 将玩家拉到当前地图的vector位置
			StageManager.inst().pullTo(humanObj, vector);
			
		} else {
			human.setHpCur(human.getHpMax());

			// 玩家出现
			humanObj.stageShowRevive();

			// 人物复活事件
			Event.fire(EventKey.HUMAN_REVIVE, "humanObj", humanObj);
		}
		
		//发送复活给前端
		SCFightRevive.Builder msg = SCFightRevive.newBuilder();
		msg.setHumanId(humanObj.id);
		msg.setHpCur(human.getHpCur());
		//当前坐标
		DVector2.Builder dPos = DVector2.newBuilder();
		dPos.setX((float)humanObj.posNow.x);
		dPos.setY((float)humanObj.posNow.y);
		msg.setPos(dPos);
		humanObj.sendMsg(msg);
		
		return new ReasonResult(true);
	}

	/**
	 * 玩家在线时间累积
	 * @param humanObject
	 */
	public void onlineTimePlus(HumanObject humanObject) {
		TickTimer onlineTickTime = humanObject.onlineTickTimer;
		
		if(!onlineTickTime.isPeriod(Port.getTime())) {
			return ;
		} 

		Human human = humanObject.getHuman();
		
		//本次累计增加秒数
		long secPlus = onlineTickTime.getInterval() / Time.SEC;
		
		//单位 秒
		human.setTimeSecOnline((int)(human.getTimeSecOnline() + secPlus));
		
	}
	
	//货币***********************************************************
	
	/**
	 * 所有货币添加统一接口  ProduceMoneyKey中所有类型
	 * @param humanObj
	 * @param type ProduceMoneyKey 中的type
	 * @param num
	 * @param log
	 */
	public void produceMoneyAdd(HumanObject humanObj, int type, long num ,MoneyAddLogKey log) {
		produceMoneyAdd(humanObj, ProduceMoneyKey.getEnumByType(type), num, log);
	}
	/**
	 * 所有货币添加统一接口 ProduceMoneyKey中所有类型
	 * @param humanObj
	 * @param key
	 * @param num
	 * @param log
	 */
	public void produceMoneyAdd(HumanObject humanObj, ProduceMoneyKey key, long num ,MoneyAddLogKey log) {
		//判断数量 
		if(num <= 0) {
			throw new SysException("添加数量不能小于0");
		}
		if(key == null) {
			throw new SysException("检查是否能消耗玩家货币时发现无法解析的类型;");
		}
		
		//默认日志
		if(log == null) log = MoneyAddLogKey.未设置;
		
		Human human = humanObj.getHuman();
		
		//监听属性变化
		HumanInfoChange.listen(humanObj);
		long oldNum = 0;
		long newNum = 0;
		
		switch (key.getType()) {
			case 1: {
				//铜币
				oldNum = human.getCoin();
				newNum = oldNum + num;
				human.setCoin(newNum);
			}
			break;
			case 2: { 
				//元宝
				oldNum = human.getGold();
				newNum = oldNum + num;
				human.setGold(newNum);
				//持久化
				human.update(true);
			}
			break;
			case 3: {
				oldNum = human.getExpCur();
				//经验
				expAdd(humanObj, num, log);
				newNum = human.getExpCur();
			}
			break;
			case 4: { 
				//活力
				oldNum = human.getActValue();
				newNum = oldNum + num;
				human.setActValue(newNum);
			}
			default:
				throw new SysException("消耗玩家货币时发现无法解析的类型：{}", key.getType());
		}
		Event.fireEx(EventKey.HUMAN_PRODUCE_PROP_CHANGE, key, "humanObj", humanObj, "log", log, "propOld", oldNum, "propNew", newNum);
		Event.fireEx(EventKey.HUMAN_PRODUCE_PROP_ADD, key, "humanObj", humanObj, "log", log, "propOld", oldNum, "propNew", newNum);
	}
	
	/**
	 * 所有货币统一删除接口 ProduceMoneyKey中所有类型 不包括exp exp现在不能减少
	 * @param humanObj
	 * @param type ProduceMoneyKey 中的type
	 * @param num
	 * @param log
	 */
	public void produceMoneyReduce(HumanObject humanObj, int type, long num ,MoneyReduceLogKey log) {
		produceMoneyReduce(humanObj, ProduceMoneyKey.getEnumByType(type), num, log);
	}
	/**
	 * 所有货币统一删除接口 ProduceMoneyKey中所有类型 不包括exp exp现在不能减少
	 * @param humanObj
	 * @param key
	 * @param num
	 * @param log
	 */
	public void produceMoneyReduce(HumanObject humanObj, ProduceMoneyKey key, long num ,MoneyReduceLogKey log) {
		ReasonResult rr = canProduceReduce(humanObj, key, num);
		if(!rr.success) {
			throw new SysException(rr.reason);
		}
		if(key == null) {
			throw new SysException("检查是否能消耗玩家货币时发现无法解析的类型;");
		}
		
		//默认日志
		if(log == null) log = MoneyReduceLogKey.未设置;
		
		//玩家信息
		Human human = humanObj.getHuman();
		
		//监听玩家自身属性变化
		HumanInfoChange.listen(humanObj);
		long oldNum = 0;
		long newNum = 0;
		
		switch (key.getType()) {
			case 1: {
				//铜币
				oldNum = human.getCoin();
				newNum = oldNum - num;
				human.setCoin(newNum);
			}
			break;
			case 2: { 
				//元宝
				oldNum = human.getGold();
				newNum = oldNum - num;
				human.setGold(newNum);
				//持久化
				human.update(true);
			}
			break;
			case 3: {
				//经验
			}
			break;
			case 4: {
				//活力
				oldNum = human.getActValue();
				newNum = oldNum - num;
				human.setActValue(newNum);
			}
			break;
			default:
				throw new SysException("消耗玩家货币时发现无法解析的类型：{}", key.getType());
		}
		
		Event.fireEx(EventKey.HUMAN_PRODUCE_PROP_CHANGE, key, "humanObj", humanObj, "log", log, "propOld", oldNum, "propNew", newNum);
		Event.fireEx(EventKey.HUMAN_PRODUCE_PROP_REDUCE, key, "humanObj", humanObj, "log", log, "propOld", oldNum, "propNew", newNum);
		
	}
	
	public ReasonResult canProduceReduce(HumanObject humanObj, int type, long num) {
		return canProduceReduce(humanObj, ProduceMoneyKey.getEnumByType(type), num);
	}
	public ReasonResult canProduceReduce(HumanObject humanObj, ProduceMoneyKey key, long num) {
		//判断数量 
		if(num < 0) {
			throw new SysException("添加数量不能小于0");
		}
		if(key == null) {
			throw new SysException("检查是否能消耗玩家货币时发现无法解析的类型;");
		}
		
		long nowNum = this.getProduceReduce(humanObj, key);
		if(nowNum < num) {
			return new ReasonResult(false, "货币不足！");
		}
		
		return new ReasonResult(true);
	}
	
	/**
	 * 获取某种类型货币的数量
	 * @param humanObj
	 * @param type
	 * @return
	 */
	public long getProduceReduce(HumanObject humanObj, int type) {
		return getProduceReduce(humanObj, ProduceMoneyKey.getEnumByType(type));
	}
	public long getProduceReduce(HumanObject humanObj, ProduceMoneyKey key) {
		
		if(key == null) {
			throw new SysException("无法解析的货币类型;");
		}
		
		Human human = humanObj.getHuman();
		
		switch (key.getType()) {
			case 1:  return human.getCoin();				//铜币
			case 2:  return human.getGold(); 				//元宝
			case 3:  return human.getExpCur(); 			//经验
			case 4:  return human.getActValue();		//活力
			default: throw new SysException("检查是否能消耗玩家货币时发现无法解析的类型：{}", key.getType());
		}
	}
	

	public void expAdd(HumanObject humanObj, long exp, MoneyAddLogKey log) {
		Human human = humanObj.getHuman();
		human.setExpCur(human.getExpCur() + exp);
		
		//默认日志
		if(log == null) log = MoneyAddLogKey.未设置;
				
		boolean levelUp = false;
		HumanInfoChange.listen(humanObj);
		
		//如果达到升级条件
		while (human.getExpCur() >= human.getExpUpgrade()) {
			levelUp = true;
			ConfLevelExp confNext = ConfLevelExp.get(human.getLevel() + 1);
			
			human.setExpCur(human.getExpCur() - human.getExpUpgrade());
			human.setExpUpgrade(confNext.exp);
			human.setLevel(human.getLevel() + 1);
			
			//主角升级与伙伴无关
//			for (UnitObject uo : humanObj.slaves.values()) {
//				if(uo == null) {
//					continue;
//				}
//				uo.getUnit().setLevel(human.getLevel());
//			}
			
			//发布升级事件
			Event.fire(EventKey.HUMAN_UPGRADE, "humanObj", humanObj);
			Event.fire(EventKey.UPDATE_QUEST, "humanObj", humanObj);
		}
		
		//给前端发布消息
		if(levelUp) {
			
			// 等级属性加成
			PropCalc levelPropPlus = UnitManager.inst().getLevelProp(human.getProfession(), human.getLevel());
			// 加入
			humanObj.dataPers.unitPropPlus.setLevel(levelPropPlus.toJSONStr());	
			//重新计算
			UnitManager.inst().propCalc(humanObj);
		}
	}
	
	/**
	 * 每6分钟恢复一点活力，上限不能超过级别限制
	 * @param param
	 */
	@Listener(EventKey.HUMAN_RESET_6MIN)
	public void actAddIn6Min(Param param) {
		HumanObject humanObj = param.get("humanObj");
		
		//取出基准值和级别限制最大值
		long levelActMax = humanObj.getHuman().getActValueMax();
		long levelActNow = humanObj.getHuman().getActValue();
		
		//如果最大值溢出，直接返回
		if(levelActNow >= levelActMax)
			return;
		
		//添加一点活力
		produceMoneyAdd(humanObj, ProduceMoneyKey.actValue, 1, null);
	}
	
	/**
	 * 每次登陆，处理添加活力点数逻辑
	 * @param humanObj
	 * @param point
	 */
	@SuppressWarnings("unused")
	private void loginActValueAdd(HumanObject humanObj, long point) {
		
		//如果小于1，直接返回
		if(point < 1) return;
		
		//取出基准值和级别限制最大值
		long levelActMax = humanObj.getHuman().getActValueMax();
		long levelActNow = humanObj.getHuman().getActValue();
		
		//如果最大值溢出，直接返回
		if(levelActNow >= levelActMax)
			return;
		
		//如果新添加的超过最大值，就修正一下
		if((point+levelActNow)>levelActMax){
			point = levelActMax - levelActNow;
		}
		
		//添加活力
		produceMoneyAdd(humanObj, ProduceMoneyKey.actValue, point, null);
	}

	//货币结束**************************************************************
	

	@Listener(EventKey.POCKET_LINE_HANDLE_END)
	public void readyToSendInitataToClient(Param param) {
		HumanObject humanObj = param.get();

		// 发送初始化消息给客户端
		sendInitDataToClient(humanObj);
	}

	/**
	 * 玩家登录游戏后 加载玩家的数据 对于简单的符合规则的数据 可以统一再这里加载
	 * 
	 * @param human
	 */
	@Listener(EventKey.HUMAN_DATA_LOAD_BEGIN)
	public void loadHumanData(Param param) {
		HumanObject humanObj = param.get();

		long humanId = humanObj.id;

		// 属性加成
		LoadHumanDataUtils.load(humanObj, "unitPropPlus", null, UnitPropPlus.class, "id", humanId);
		// buff
		LoadHumanDataUtils.load(humanObj , "buffs", "type", Buff.class, "idAffect", humanId);
		// 物品系列
		LoadHumanDataUtils.load(humanObj, "items", null, Item.class, "humanId", humanId, Item.K.position, OrderBy.ASC);
		// 读取副本
		LoadHumanDataUtils.load(humanObj , "instanceChapters", "chapterSn", InstanceChapter.class, "humanId", humanId);
		// 装备位信息
		LoadHumanDataUtils.load(humanObj, "parts", "sn", Part.class, "humanId", humanId);
		// 活动
		LoadHumanDataUtils.load(humanObj, "activity", null, Activity.class, "humanId", humanId);
		// 爬塔
		LoadHumanDataUtils.load(humanObj, "tower", null, Tower.class, "humanId", humanId);
		// 待办
		LoadHumanDataUtils.load(humanObj, "pocketLine", null, PocketLine.class, "humanId", humanId);
		// 好友
		LoadHumanDataUtils.load(humanObj, "friend", null, Friend.class, "humanId", humanId);

	}

	/**
	 * 玩家登录游戏后 加载玩家的数据 开始了一条新的数据加载
	 * 
	 * @param human
	 */
	@Listener(EventKey.HUMAN_DATA_LOAD_BEGIN_ONE)
	public void loadDataBeginOne(Param param) {
		HumanObject humanObj = param.get();
		humanObj.loadingNum++;
	}

	/**
	 * 玩家登录游戏后 加载玩家的数据 完成了一条新的数据加载
	 * 
	 * @param human
	 */
	@Listener(EventKey.HUMAN_DATA_LOAD_FINISH_ONE)
	public void loadDataFinishOne(Param param) {
		HumanObject humanObj = param.get();
		humanObj.loadingNum--;

		// 玩家数据全部加载完毕 可以正式进行登录了
		if (humanObj.loadingNum <= 0) {
			// 玩家数据加载完毕后 登录游戏
			login(humanObj);

			// 返回
			Port port = Port.getCurrent();
			port.returnsAsync(humanObj.loadingPID, "node", port.getNode().getId(), "port", port.getId());
		}
	}

	@Listener(EventKey.HUMAN_LOGOUT)
	public void onLogout(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		
		// 同步玩家位置
		JSONArray ja = JSON.parseArray(human.getStageHistory());
		JSONArray jaTemp = JSON.parseArray(ja.getString(0));
		ConfMap confMap = ConfMap.get(Utils.intValue(jaTemp.getString(1)));
		//第1场战斗 是直接发送的没有 地图的记录
		if(humanObj.stageObj.sn == confMap.sn) {
			jaTemp.set(2, humanObj.posNow.x);
			jaTemp.set(3, humanObj.posNow.y);
			ja.set(0, jaTemp);
			human.setStageHistory(JSON.toJSONString(ja));
		}
		
		// 更新退出时间
		human.setTimeLogout(Port.getTime());

		// 移除地图
		humanObj.stageLeave();
		
		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		// 地图玩家数-1
		proxy.stageHumanNumReduce(humanObj.stageObj.id);
	}

	/**
	 * 玩家完成加载登录到地图中时进行操作
	 * 
	 * @param params
	 */
	@Listener(EventKey.HUMAN_STAGE_ENTER)
	public void onHumanStageEnter(Param params) {
		HumanObject humanObj = params.get("humanObj");

		//添加监听器
		HumanInfoChange.listen(humanObj); 
		
		// 设置状态 代表客户端已经准备好了
		humanObj.isClientStageReady = true;
		
		if(humanObj.stageObj.conf.type.equals(StageMapTypeKey.common.getContent())) {
			humanObj.getHuman().setSpeed(Utils.intValue(ConfParam.getBy(ConfParam.K.sn, "commonSpeed").value));
		} else if(humanObj.stageObj.conf.type.equals(StageMapTypeKey.rep.getContent())) {
			humanObj.getHuman().setSpeed(Utils.intValue(ConfParam.getBy(ConfParam.K.sn, "repSpeed").value));
		}
		
		//补满血
		humanObj.getHuman().setHpCur(humanObj.getHuman().getHpMax());
		UnitManager.inst().propCalc(humanObj);
		
		humanObj.confModel = ConfCharacterModel.get(humanObj.getHuman().getModelSn());
		
	}
	
	/**
	 * 记录玩家每个招募过的武将, 用于武将羁绊
	 * @param human
	 * @param generalSn
	 */
	public void addGeneralHistory(Human human, String generalSn){
		String generalStr = generalSn+"";
		
		if(!human.getAllGeneral().contains(generalStr)) {
			if(human.getAllGeneral().isEmpty()) {
				human.setAllGeneral(generalStr);
			} else {
				human.setAllGeneral(human.getAllGeneral() + "," + generalStr);
			}
		}
	}
	
	public void changeModel(HumanObject humanObj, String modelSn) {
		boolean canChange = false;
		//判断是否有自己的武将
		for (UnitObject uo : humanObj.slaves.values()) {
			if(uo == null) {
				continue;
			}
			if(uo.confModel.sn.equals(modelSn)) {
				canChange = true;
				break;
			}
		}
		
		if(!canChange) {
			Inform.user(humanObj.id, Inform.提示操作, "没有对应的武将！");
			return;
		}
		
		humanObj.getUnit().setModelSn(modelSn);
		SCChangeModel.Builder msg = SCChangeModel.newBuilder();
		msg.setHumId(humanObj.id);
		msg.setModel(modelSn);
		StageManager.inst().sendMsgToArea(msg, humanObj.stageObj, humanObj.posNow);
	}
	
}