package org.gof.demo.worldsrv.competition;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Time;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.entity.CompetitionHuman;
import org.gof.demo.worldsrv.entity.CompetitionLog;
import org.gof.demo.worldsrv.entity.CompetitionMirror;
import org.gof.demo.worldsrv.entity.General;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Unit;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DBattleLog;
import org.gof.demo.worldsrv.msg.Msg.DHumanInfo;
import org.gof.demo.worldsrv.msg.Msg.SCCompetitionBattleLog;
import org.gof.demo.worldsrv.msg.Msg.SCCompetitionEnd;
import org.gof.demo.worldsrv.msg.Msg.SCCompetitionInfo;
import org.gof.demo.worldsrv.msg.Msg.SCDieStage;
import org.gof.demo.worldsrv.msg.Msg.SCRefreshCount;
import org.gof.demo.worldsrv.msg.Msg.SCResetCDTime;
import org.gof.demo.worldsrv.stage.StageGlobalServiceProxy;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.support.observer.EventKey;
import org.gof.demo.worldsrv.team.TeamGlobalServiceProxy;

public class CompetitionManager extends ManagerBase {
	public static final int comptitionMapSn = 110029;
	/**
	 * 获取实例
	 * @return
	 */
	public static CompetitionManager inst() {
		return inst(CompetitionManager.class);
	}
	
	/**
	 * 创建竞技场
	 * @param humanObj
	 */
	public void create(HumanObject humanObj, long defenderId) {
		Human human = humanObj.getHuman();
		//自己打自己
		if (humanObj.id == defenderId) {
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("competition.create.error.human.self"));
			return;
		}
		//冷却时间未到
		int coolTime = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场CD时间间隔);
		if (Port.getTime() - human.getCompeteLastTime() <= coolTime*Time.SEC) {
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("competition.create.error.cooltime"));
			return;
		}
		//挑战次数不足
		if (human.getCompeteCount() <= 0) {
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("competition.create.error.count.notenough"));
			return;
		}
		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		proxy.createCompetition(humanObj,comptitionMapSn, defenderId);
		proxy.listenResult(this::_result_create, "humanObj", humanObj, "mapSn", comptitionMapSn);
	}
	
	/**
	 * 竞技场创建完毕回调
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
	 * 离开竞技场 自动回到关卡进入前的主地图
	 * @param humanObj
	 */
	public void leave(HumanObject humanObj) {
//		//切换地图
		StageObjectCompetition stageObj = (StageObjectCompetition)humanObj.stageObj;
		if(stageObj == null) {
			return;
		}
		
		StageManager.inst().quitToCommon(humanObj);
	}
	
	/**
	 * 结算
	 * @param humanObj
	 */
	public void competEnd(HumanObject humanObj,boolean isWin) {
		Human human = humanObj.getHuman();
		human.setCompeteLastTime(System.currentTimeMillis());
		human.setCompeteCount(human.getCompeteCount()-1);
		
		// 更新属性和伙伴信息S
		String propStr = humanObj.getPropPlus().toJSONStr();
		
		List<CharacterObject> salvesAttingList = humanObj.salvesAttingList;
		List<CompetitionMirror> mirrorList = new ArrayList<>();
		if (mirrorList.size() > 0) {
			for (CharacterObject obj : salvesAttingList) {
				mirrorList.add(unitToMirror(obj, humanObj.id, humanObj.sn, obj.getUnit().getAttingIndex()));
			}
		}
		
		CompetitionServiceProxy proxy = CompetitionServiceProxy.newInstance();
		proxy.update(humanObj.id, propStr, mirrorList);
		proxy.listenResult(this::_result_competEnd, "isWin", isWin, "humanObj", humanObj);
	}
	
	public void _result_competEnd(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		boolean isWin = context.get("isWin");
		StageObjectCompetition stageObj = (StageObjectCompetition)(humanObj.stageObj);
		//交换排名
		CompetitionServiceProxy proxy = CompetitionServiceProxy.newInstance();
		proxy.swapRank(humanObj.getHumanId(), stageObj.defenderId, isWin);
		proxy.listenResult(this::_result_competEnd2, "isWin", isWin, "humanObj", humanObj);
	}
	
	public void _result_competEnd2(Param results, Param context) {
		boolean isWin = context.get("isWin");
		HumanObject humanObj = context.get("humanObj");
		Human human = humanObj.getHuman();
		long coin = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场金币奖励);
		long expCur = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场钻石奖励);
		long competMoney = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场演武币奖励);
		// 此处有公式
		int ratio = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场奖励系数);
		coin = coin*(1+human.getLevel()/100*ratio);
		expCur = expCur*(1+human.getLevel()/100*ratio);
		competMoney = competMoney*(1+human.getLevel()/100*ratio);
		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.coin, coin, MoneyAddLogKey.离线竞技);
		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.expCur, expCur, MoneyAddLogKey.离线竞技);
//		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.competMoney, competMoney, MoneyAddLogKey.离线竞技);
		
		CompetitionHuman attacker = results.get("attacker");
		CompetitionHuman defender = results.get("defender");
		boolean isSwap = results.get("isSwap");
		
		int oldRank = 0;
		int newRank = 0;
		if (isWin) {
			newRank = attacker.getRank();
			if (isSwap) {
				oldRank = defender.getRank();
			} else {
				oldRank = attacker.getRank();
			}
		} else {
			oldRank = attacker.getRank();
			if (isSwap) {
				newRank = defender.getRank();
			} else {
				newRank = attacker.getRank();
			}
			
		}
		//发送消息
		SCCompetitionEnd.Builder msg = SCCompetitionEnd.newBuilder();
		msg.setIsWin(isWin);
		msg.setOldRank(oldRank);
		msg.setNewRank(newRank);
		msg.setCoin(coin);
		msg.setExp(expCur);
		msg.setCompetMoney(10);
		humanObj.sendMsg(msg);
	}
	/**
	 * 打开战斗记录
	 * @param humanObj
	 */
	public void openCompetitionLog(HumanObject humanObj) {
		// 查询战斗记录
		String whereSql = "WHERE attackerId = ? or defenderId = ? ORDER BY createdTime desc limit 20";

		DBServiceProxy pxy = DBServiceProxy.newInstance();
		pxy.findByQuery(false, CompetitionLog.tableName, whereSql, humanObj.id, humanObj.id);
		pxy.listenResult(this::_result_openCompetitionLog, "humanObj", humanObj);
	}
	
	public void _result_openCompetitionLog(Param results, Param context) {
		List<Record> logs = results.get();
		HumanObject humanObj = context.get("humanObj");
		Human human = humanObj.getHuman();
		
		SCCompetitionBattleLog.Builder msg = SCCompetitionBattleLog.newBuilder();
		
		//构建最近战斗记录
		for(Record r : logs) {
			CompetitionLog competLog = new CompetitionLog(r);
			
			DBattleLog.Builder dLog = DBattleLog.newBuilder();
			boolean isActive = competLog.getAttackerId() == human.getId();
			dLog.setTime((System.currentTimeMillis() - competLog.getCreatedTime())/Time.SEC);		// 返回现在距离历史挑战时间之差
			// 主动攻击显示防守玩家信息，被动防守显示主动攻击玩家信息, 输赢也是
			dLog.setName(isActive ? competLog.getDefenderName() : competLog.getAttackerName());
			dLog.setIsWin(isActive ? competLog.isWin() : !competLog.isWin());
			dLog.setLevel(isActive ? competLog.getDefenderLevel() : competLog.getAttackerLevel());
			dLog.setIsActive(isActive);
			dLog.setRank(isActive ? competLog.getAttackerRank() : competLog.getDefenderRank());
			dLog.setRankChange(isActive ? competLog.getDefenderRank() - competLog.getAttackerRank() : competLog.getAttackerRank() - competLog.getDefenderRank());
			dLog.setHumanSn(isActive ? competLog.getAttackerModelSn() : competLog.getDefenderModelSn());
			msg.addLogs(dLog);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 换一批
	 * @param humanObj
	 */
	public void competitionInfo(HumanObject humanObj) {
		CompetitionServiceProxy proxy = CompetitionServiceProxy.newInstance();
		proxy.getCompeteInfo(humanObj.getHumanId());
		proxy.listenResult(this::_result_competitionInfo, "humanObj", humanObj);
	}
	
	public void _result_competitionInfo(Param results, Param context) {
		SCCompetitionInfo.Builder msg = SCCompetitionInfo.newBuilder();
		HumanObject humanObj = context.get("humanObj");
		List<CompetitionHuman> humans = results.get("humans");
		Human human = humanObj.getHuman();
		for (CompetitionHuman competHuman : humans) {
			DHumanInfo.Builder info = DHumanInfo.newBuilder();
			info.setHumanId(competHuman.getHumanId());
			info.setHumanSn(competHuman.getCharacterSn());
			info.setName(competHuman.getHumanName());
			info.setLevel(competHuman.getHumanLevel());
			info.setCombat(competHuman.getCombat());
			info.setRank(competHuman.getRank());
			msg.addInfos(info);
		}
		msg.setCount(human.getCompeteCount());
		int coolTime = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场CD时间间隔);
		int deltaTime = 0;
		int goneTime = (int) ((System.currentTimeMillis() - human.getCompeteLastTime())/Time.SEC);
		if (coolTime - goneTime > 0) {
			deltaTime = coolTime - goneTime;
		}
		// 这里要自测
		msg.setTimeCool(deltaTime);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 重置进入竞技场CD时间
	 * @param humanObj
	 */
	public void resetCDTime(HumanObject humanObj) {
		SCResetCDTime.Builder msg = SCResetCDTime.newBuilder();
		Human human = humanObj.getHuman();
		int cost = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场重置CD消耗);
		// 扣钱
		ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, ProduceMoneyKey.gold, cost);
		if (!result.success) {
			msg.setResultCode(0);
			return;
		}
		HumanManager.inst().produceMoneyReduce(humanObj, ProduceMoneyKey.gold, cost, MoneyReduceLogKey.离线竞技场清除冷却CD);
		int coolTime = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场CD时间间隔);
		human.setCompeteLastTime(human.getCompeteLastTime()-coolTime*Time.SEC);
		msg.setResultCode(1);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 重置竞技场挑战次数
	 * @param humanObj
	 */
	public void refreshCount(HumanObject humanObj) {
		SCRefreshCount.Builder msg = SCRefreshCount.newBuilder();
		Human human = humanObj.getHuman();
		int refreshCount = human.getCompeteRefreshCount();
		if (refreshCount <= 0) {
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("competition.refresh.error.num.max"));
			return;
		}
		int cost = ConfGlobalUtils.getValue(ConfGlobalKey.竞技场刷新消耗);
		// 扣钱
		ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, ProduceMoneyKey.gold, cost);
		if (!result.success) {
			msg.setResultCode(0);
			return;
		}
		HumanManager.inst().produceMoneyReduce(humanObj, ProduceMoneyKey.gold, cost, MoneyReduceLogKey.离线竞技场挑战次数增加);
		
		human.setCompeteRefreshCount(refreshCount-1);
		human.setCompeteCount(ConfGlobalUtils.getValue(ConfGlobalKey.竞技场每日进入次数));
		msg.setResultCode(1);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 重置刷新次数和进入次数
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_RESET_ZERO)
	public void refreshCompet(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		human.setCompeteRefreshCount(ConfGlobalUtils.getValue(ConfGlobalKey.竞技场每日刷新次数));
		human.setCompeteCount(ConfGlobalUtils.getValue(ConfGlobalKey.竞技场每日进入次数));
	}
	
	/**
	 * 登陆
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_FIRST_LOGIN)
	public void onCompetHumanLogin(Param param) {
		HumanObject humanObj = param.get("humanObj");
		
		Human human = humanObj.getHuman();
		if (human.getLevel() >= ConfGlobalUtils.getValue(ConfGlobalKey.竞技场开启等级)) {
			List<CharacterObject> salvesAttingList = humanObj.salvesAttingList;
			List<CompetitionMirror> mirrorList = new ArrayList<>();
			if (mirrorList.size() > 0) {
				for (CharacterObject obj : salvesAttingList) {
					mirrorList.add(unitToMirror(obj, humanObj.id, humanObj.sn, obj.getUnit().getAttingIndex()));
				}
			}
			
			CompetitionServiceProxy proxy = CompetitionServiceProxy.newInstance();
			proxy.addNew(human, unitToMirror(humanObj, humanObj.id, humanObj.sn, -1), mirrorList);
		}
	}
	
	/**
	 * 更新玩家升级
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_UPGRADE)
	public void onUpdateCompetHumanLevel(Param param) {
		HumanObject humanObj = param.get("humanObj");
		CompetitionServiceProxy proxy = CompetitionServiceProxy.newInstance();
		proxy.updateLevel(humanObj);
	}
	
	/**
	 * 生成镜像
	 * @param humanObj
	 * @param msg
	 */
	public CompetitionMirror unitToMirror(UnitObject unitObj, long humanId, String humanSn, int attendIndex) {
		Unit unit = unitObj.getUnit();
		CompetitionMirror mirror = new CompetitionMirror();
		mirror.setId(unit.getId());
		mirror.setHumanID(humanId);
		mirror.setEnterTime(Port.getTime());
		mirror.setLevel(unit.getLevel());
		mirror.setName(unit.getName());
		mirror.setSn(humanSn);
		mirror.setModelSn(unit.getModelSn());
		mirror.setProfession(unit.getProfession());
		mirror.setSex(unit.getSex());
		if (unitObj.isHumanObj()) {
			mirror.setHuman(true);
		} else {
			mirror.setHuman(false);
			mirror.setStar(((General)unit).getStar());
			mirror.setQuality(((General)unit).getQuality());
			mirror.setEquip(((General)unit).getEquip());
			mirror.setAttendIndex(attendIndex);
		}
		mirror.setSkill(unit.getSkill());
		mirror.setSkillGroupSn(unit.getSkillGroupSn());
		mirror.setBase(unitObj.getPropPlus().toJSONStr());
		mirror.persist();
		return mirror;
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
		if (type.equals(StageMapTypeKey.competition)) {
			SCDieStage.Builder msg = SCDieStage.newBuilder();
			msg.setStageType(type);
			humanObj.sendMsg(msg);
		}
	}
}
