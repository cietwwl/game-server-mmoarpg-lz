package org.gof.demo.worldsrv.tower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.RecordTransient;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.competition.CompetitionHumanObj;
import org.gof.demo.worldsrv.competition.CompetitionServiceProxy;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.config.ConfTower;
import org.gof.demo.worldsrv.entity.CompetitionMirror;
import org.gof.demo.worldsrv.entity.General;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Tower;
import org.gof.demo.worldsrv.entity.TowerMirror;
import org.gof.demo.worldsrv.friend.FriendObject;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DGeneralEquipment;
import org.gof.demo.worldsrv.msg.Msg.DGeneralInfo;
import org.gof.demo.worldsrv.msg.Msg.DLayerInfo;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.SCAllLayer;
import org.gof.demo.worldsrv.msg.Msg.SCDieStage;
import org.gof.demo.worldsrv.msg.Msg.SCLayerAward;
import org.gof.demo.worldsrv.msg.Msg.SCLayerEnd;
import org.gof.demo.worldsrv.msg.Msg.SCLayerRefreshBuff;
import org.gof.demo.worldsrv.produce.ProduceManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.stage.StageGlobalServiceProxy;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TowerManager extends ManagerBase {

	/**关卡完成*/
	public static final int LAYER_未完成 = 0;					//未完成
	public static final int LAYER_已完成 = 1;					//已完成
	/**宝箱领取*/
	public static final int BOX_未领取 = 0;					    //未领取
	public static final int BOX_已领取 = 1;					    //已领取
	
	public static TowerManager inst() {
		return inst(TowerManager.class);
	}
	
	/**
	 * 到时刷新
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_RESET_ZERO)
	public void refreshTower(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Tower tower = humanObj.dataPers.tower;
		if (tower != null) {
			tower.setResetBuffCount(ConfGlobalUtils.getValue(ConfGlobalKey.爬塔刷新buff次数));
			tower.setRefreshCount(ConfGlobalUtils.getValue(ConfGlobalKey.爬塔每日重置次数));
		}
	}
	
	/**
	 * 初始化爬塔
	 * @param humanObj
	 */
	public void initTower(HumanObject humanObj) {
		Tower tower = humanObj.dataPers.tower;
		if (tower == null) return;
		JSONArray layerJson = Utils.toJSONArray(tower.getLayerJson());
		
		String humanIds = "";
		for (Object humanId : layerJson) {
			if ("".equals(humanIds)) {
				humanIds = String.valueOf(humanId);
			} else {
				humanIds = humanIds + "," + String.valueOf(humanId);
			}
		}
		
		String whereSql = Utils.createStr(" where id in ({})", humanIds);
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findBy(false, TowerMirror.tableName, whereSql);
		prx.listenResult(this::_result_initTower, "humanObj", humanObj);
	}
	
	public void _result_initTower(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<TowerMirror> mirrList = new ArrayList<>();
		for (int i = 1; i <= 15; i++) {
			humanObj.towerInfo.put(i, mirrList);
		}
		List<Record> list = results.get();
		for(Record record : list){
			TowerMirror mirror = new TowerMirror(record);
			List<TowerMirror> mList = humanObj.towerInfo.get(mirror.getLayer());
			mList.add(mirror);
			humanObj.towerInfo.put(mirror.getLayer(), mList);
		}
		System.out.println(humanObj.towerInfo.size());
	}

	/**
	 * 竞技场数据转换成爬塔数据
	 * @param humanObj
	 */
	private TowerMirror __competMirrToTowerMirr(CompetitionMirror cMirr) {
		TowerMirror tMirr = new TowerMirror();
		tMirr.setId(Port.applyId());
		tMirr.setHumanID(cMirr.getHumanID());
		tMirr.setLevel(cMirr.getLevel());
		tMirr.setName(cMirr.getName());
		tMirr.setSn(cMirr.getSn());
		tMirr.setModelSn(cMirr.getModelSn());
		tMirr.setProfession(cMirr.getProfession());
		tMirr.setSex(cMirr.getSex());
		tMirr.setSkill(cMirr.getSkill());
		tMirr.setSkillGroupSn(cMirr.getSkillGroupSn());
		tMirr.setBase(cMirr.getBase());
		tMirr.setEnterTime(cMirr.getEnterTime());
		tMirr.setStar(cMirr.getStar());
		tMirr.setQuality(cMirr.getQuality());
		tMirr.setEquip(cMirr.getEquip());
		tMirr.setHuman(cMirr.isHuman());
		tMirr.setAttendIndex(cMirr.getAttendIndex());
		tMirr.persist();
		return tMirr;
	}
	
	/**
	 * 获得关卡数据
	 * @param humanObj
	 */
	public void getTowerInfo(HumanObject humanObj, Tower tower, boolean persist) {
		// 通过自己的排名在竞技排行榜找对手
		CompetitionServiceProxy prx = CompetitionServiceProxy.newInstance();
		prx.getTowerInfo(humanObj.id);
		prx.listenResult(this::_result_getTowerInfo, "humanObj", humanObj, "tower", tower, "persist", persist);
	}
	
	public void _result_getTowerInfo(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		Tower tower = context.get("tower");
		boolean persist = context.get("persist");
		List<CompetitionHumanObj> humans = results.get("list");
//		Human human = humanObj.getHuman();
//		Tower tower = humanObj.dataPers.tower;
//		if (tower == null) {
//			tower = new Tower();
//		}
		tower.setId(humanObj.getHumanId());
		tower.setHumanId(humanObj.getHumanId());
		tower.setCurLayer(1);
		tower.setCurBox(1);
		tower.setBuffSn(0);
		List<Long> layerInfo = new ArrayList<>();
		int layer = 1;
		for (CompetitionHumanObj obj : humans) {
			List<TowerMirror> mirrList = new ArrayList<>();
//			List<Long> ids = new ArrayList<>();
			CompetitionMirror humanMirror = obj.humanMirror;
			List<CompetitionMirror> gensMirror = obj.gensMirror;
			mirrList.add(__competMirrToTowerMirr(humanMirror));
//			ids.add(humanMirror.getId());
			for (CompetitionMirror mirr : gensMirror) {
//				ids.add(mirr.getId());
				mirrList.add(__competMirrToTowerMirr(mirr));
			}
			// 加入内存
			humanObj.towerInfo.put(layer, mirrList);
			layerInfo.add(humanMirror.getHumanID());
			layer++;
		}
		tower.setLayerJson(Utils.toJSONString(layerInfo));
		
		Map<String, String> awardInfo = new HashMap<>();
		for (int i = 1; i <= 15; i++) {
			ConfTower confTower = ConfTower.get(i);
			List<ProduceVo> awards = ProduceManager.inst().produceItem(humanObj, confTower.randProduce);
			for (ProduceVo vo : awards) {
				Map<String, Integer> voInfo = new HashMap<>();
				voInfo.put(String.valueOf(vo.sn), vo.num);
				String infoStr = (Utils.toJSONString(voInfo));
				awardInfo.put(String.valueOf(i), infoStr);
			}
		}
		tower.setAwardJson(Utils.toJSONString(awardInfo));
		tower.setResetBuffCount(ConfGlobalUtils.getValue(ConfGlobalKey.爬塔刷新buff次数));
		tower.setRefreshCount(ConfGlobalUtils.getValue(ConfGlobalKey.爬塔每日重置次数));
		if (persist) tower.persist();
		humanObj.dataPers.tower = tower;
		updateAllLayer(humanObj);
	}
	
	/**
	 * 创建关卡
	 * @param humanObj
	 * @param confRep
	 */
	public void create(HumanObject humanObj, ConfTower confTower) {
		int mapSn = confTower.mapSn;
		
		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		proxy.createTower(mapSn, confTower.sn);
		proxy.listenResult(this::_result_create, "humanObj", humanObj, "mapSn", mapSn);
	}
	
	/**
	 * 离开关卡 自动回到关卡进入前的主地图
	 * @param humanObj
	 */
	public void leave(HumanObject humanObj) {
//		//切换地图
		StageObjectTower stageObj = (StageObjectTower)humanObj.stageObj;
		if(stageObj == null) {
			return;
		}
		
		StageManager.inst().quitToCommon(humanObj);
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
	 * 关卡结算
	 * @param humanObj
	 */
	public void layerEnd(HumanObject humanObj) {
		//完成副本战斗
		SCLayerEnd.Builder endMsg = SCLayerEnd.newBuilder();
		StageObjectTower stageObjTower = (StageObjectTower)humanObj.stageObj;
		Tower tower = humanObj.dataPers.tower;
		ConfTower confTower = stageObjTower.confTower;
		// 加经验
		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.expCur, confTower.exp, MoneyAddLogKey.爬塔);
		//获得随机BUFF
		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
		itemProduce = ProduceManager.inst().produceItem(humanObj, confTower.buff);
		ProduceVo vo = itemProduce.get(0);
		int buffSn = vo.sn;
		tower.setBuffSn(buffSn);
		// 保存血量
		
		endMsg.setLayerSn(confTower.sn);
		humanObj.sendMsg(endMsg);
		tower.setCurLayer(tower.getCurLayer()+1);
		updateAllLayer(humanObj);
	}
		
	/**
	 * 更新所有关卡
	 * @param humanObj
	 */
	public void updateAllLayer(HumanObject humanObj) {
		Tower tower = humanObj.dataPers.tower;
		if (tower == null) return;
		SCAllLayer.Builder endMsg = SCAllLayer.newBuilder();
		JSONObject awardJson = Utils.str2JSONObject(tower.getAwardJson());
		endMsg.setCurLayer(tower.getCurLayer());
		endMsg.setRefreshCount(tower.getRefreshCount());
		endMsg.setResetBuffCount(tower.getResetBuffCount());
		endMsg.setBuffSn(tower.getBuffSn());
		for (int i = 1; i <= 15; i++) {
			DLayerInfo.Builder dLayer = DLayerInfo.newBuilder();
			JSONObject awardObj = Utils.str2JSONObject(String.valueOf(awardJson.get(String.valueOf(i))));
			for (String key : awardObj.keySet()) {
				int itemSn = Integer.valueOf(key);
				int num = awardObj.getIntValue(key);
				ProduceVo vo = new ProduceVo(itemSn, num);
				dLayer.addProduce(vo.toDProduce());
			}
			dLayer.setLayerSn(i);
			if (tower.getCurLayer() > i) {
				dLayer.setLayerState(LAYER_已完成);
			} else {
				dLayer.setLayerState(LAYER_未完成);
			}
			if (tower.getCurBox() > i) {
				dLayer.setBoxState(BOX_已领取);
			} else {
				dLayer.setBoxState(BOX_未领取);
			}
			List<TowerMirror> mirrList = humanObj.towerInfo.get(i);
			for (TowerMirror mirr : mirrList) {
				if (mirr.isHuman()) {
					dLayer.setHumanSn(mirr.getSn());
					dLayer.setName(mirr.getName());
					dLayer.setCombat(mirr.getCombat());
					dLayer.setLevel(mirr.getLevel());
				} else {
//					DGeneralInfo.Builder dGeneral = DGeneralInfo.newBuilder();
//					dGeneral.setId(mirr.getId());
//					dGeneral.setStar(mirr.getStar());
//					dGeneral.setQuality(mirr.getQuality());
//					dGeneral.setCombat(mirr.getCombat());
//					dGeneral.setAttingPos(mirr.getAttendIndex());
//					// 构建伙伴技能
//					DSkill.Builder dSkill = DSkill.newBuilder();
//					JSONArray skillArr = Utils.toJSONArray(mirr.getSkill());
//					for (int j = 0; j < skillArr.size(); j++) {
//						JSONObject skillJson = skillArr.getJSONObject(j);
//						dSkill.setSkillSn(skillJson.getIntValue("sn"));
//						dSkill.setPosition(skillJson.getIntValue("pos"));
//						dSkill.setSkillLevel(skillJson.getIntValue("level"));
//					}
//					dGeneral.addSkill(dSkill);
//					// 构建伙伴装备(暂时空缺)
//					//DGeneralEquipment.Builder dEquip = DGeneralEquipment.newBuilder();
//					dLayer.addGeneral(dGeneral);
				}
				
			}
			endMsg.addInfos(dLayer);
		}
		System.out.println(endMsg.getAllFields());
		humanObj.sendMsg(endMsg);
	}
	
	/**
	 * 领宝箱（基础奖励+神秘奖励）
	 * @param humanObj
	 */
	public void award(HumanObject humanObj, int layerSn) {
		Tower tower = humanObj.dataPers.tower;
		SCLayerAward.Builder msg = SCLayerAward.newBuilder();
		List<ProduceVo> voList = new ArrayList<ProduceVo>();
		JSONObject awardJson = Utils.str2JSONObject(tower.getAwardJson());
		JSONObject awardObj = Utils.str2JSONObject(String.valueOf(awardJson.get(String.valueOf(layerSn))));
		for (String key : awardObj.keySet()) {
			int itemSn = Integer.valueOf(key);
			int num = awardObj.getIntValue(key);
			ProduceVo vo = new ProduceVo(itemSn, num);
			voList.add(vo);
		}
		//获得物品
		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
		itemProduce = ProduceManager.inst().produceItem(humanObj, ConfTower.get(layerSn).fixProduce);
		voList.addAll(itemProduce);
		
		//判断是否可以给
		ReasonResult rr = ProduceManager.inst().canGiveProduceItem(humanObj, voList);
		if(!rr.success) {
			Inform.user(humanObj.id, Inform.提示操作, rr.reason);
			return;
		}
		//实际给物品
		ProduceManager.inst().giveProduceItem(humanObj, voList, MoneyAddLogKey.远征宝箱);
		
		tower.setCurBox(tower.getCurBox()+1);
		//发送消息
		msg.setCode(1);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 刷新当前BUFF
	 * @param humanObj
	 */
	public void refreshBuff(HumanObject humanObj) {
		SCLayerRefreshBuff.Builder msg = SCLayerRefreshBuff.newBuilder();
		Tower tower = humanObj.dataPers.tower;
		int count = tower.getResetBuffCount();
		if (count > 0) {
			ConfTower confTower = ConfTower.get(tower.getCurLayer());
			//获得随机BUFF
			List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
			itemProduce = ProduceManager.inst().produceItem(humanObj, confTower.buff);
			ProduceVo vo = itemProduce.get(0);
			int buffSn = vo.sn;
			tower.setBuffSn(buffSn);
			msg.setBuffSn(buffSn);
			humanObj.sendMsg(msg);
			tower.setResetBuffCount(--count);
			int cost = ConfGlobalUtils.getValue(ConfGlobalKey.爬塔刷新buff消耗);
			ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, ProduceMoneyKey.gold, cost);
			if (!result.success) {
				return;
			}
			HumanManager.inst().produceMoneyReduce(humanObj, ProduceMoneyKey.gold, cost, MoneyReduceLogKey.远征BUFF刷新);
		}
	}
	
	/**
	 * 刷新所有关卡
	 * @param humanObj
	 */
	public void refreshTower(HumanObject humanObj) {
		Tower tower = humanObj.dataPers.tower;
		int count = tower.getRefreshCount();
		if (count > 0) {
			Iterator<Entry<Integer, List<TowerMirror>>> iter = humanObj.towerInfo.entrySet().iterator();
			while(iter.hasNext()){ 
				Entry<Integer, List<TowerMirror>> entry = iter.next();
				List<TowerMirror> list = entry.getValue();
				Iterator<TowerMirror> iter2 = list.iterator();
				// 删除数据库
				while(iter2.hasNext()){
					TowerMirror mirr = iter2.next();
					mirr.remove();
				}
				// 删除内存
				iter.remove();
			}
			getTowerInfo(humanObj, tower, false);
			updateAllLayer(humanObj);
			tower.setRefreshCount(tower.getResetBuffCount()-1);
		}
	}
	
	/**
	 * 开启爬塔
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_UPGRADE)
	public void onHumanLevelOpenTower(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		Tower tower = humanObj.dataPers.tower;
		if (tower == null && human.getLevel() >= ConfGlobalUtils.getValue(ConfGlobalKey.爬塔开启等级)) {
			tower = new Tower();
			getTowerInfo(humanObj, tower, true);
//			tower.persist();
		}
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
		if (type.equals(StageMapTypeKey.tower)) {
			SCDieStage.Builder msg = SCDieStage.newBuilder();
			msg.setStageType(type);
			humanObj.sendMsg(msg);
		}
	}
}
