package org.gof.demo.worldsrv.scene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.MonsterObject;
import org.gof.demo.worldsrv.config.ConfScene;
import org.gof.demo.worldsrv.config.ConfSceneEvent;
import org.gof.demo.worldsrv.config.ConfScenePlot;
import org.gof.demo.worldsrv.config.ConfSceneTrigger;
import org.gof.demo.worldsrv.instance.InstanceManager;
import org.gof.demo.worldsrv.instance.StageObejctInstance;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.msg.Msg.DSceneEvent;
import org.gof.demo.worldsrv.msg.Msg.DScenePlot;
import org.gof.demo.worldsrv.msg.Msg.DSceneTrigger;
import org.gof.demo.worldsrv.msg.Msg.SCSceneEventStart;
import org.gof.demo.worldsrv.msg.Msg.SCSceneInit;
import org.gof.demo.worldsrv.msg.Msg.SCScenePlotChange;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;
import org.gof.demo.worldsrv.tower.StageObjectTower;

/**
 * 场景触发器
 *
 */
public class SceneManager extends ManagerBase {

	/**o
	 * 获取实例
	 * 
	 * @return
	 */
	public static SceneManager inst() {
		return inst(SceneManager.class);
	}
	
	/**
	 * 副本初始化，构建场景事件，触发器，事件数据结构
	 * @param stageOjbect
	 */
	public void repSceneInit(StageObject stageOjbect) {
		ConfScene confScence = ConfScene.get(stageOjbect.conf.sceneID);
		
		//构建所有的场景
		List<ScenePlot> plotList = new ArrayList<>();
		for (int i = 0; i < confScence.plotIDs.length; i++) {
			//初始化ScenePlot
			ConfScenePlot confPlot = ConfScenePlot.get(confScence.plotIDs[i]);
			ScenePlot plot = new ScenePlot(confScence.plotIDs[i], confPlot, stageOjbect);
			
			//初始化Trigger
			for (int j = 0; j < confPlot.triggerIDs.length; j++) {
				ConfSceneTrigger confTrigger = ConfSceneTrigger.get(confPlot.triggerIDs[j]);
				SceneTrigger trigger = new SceneTrigger(confPlot.triggerIDs[j], confTrigger, plot);
				plot.triggers.put(trigger.id, trigger);
				
				//如果触发器是移动，那么就加入到场景中
//				if(trigger.type == SceneConstant.SCENE_TRIGGER_01){
//					stageOjbect.plotMoveTrigger.put(trigger.id, trigger);
//				}
				
				//加入所有的触发器
				stageOjbect.plotTriggerAll.put(trigger.id, trigger);
			}
			
			//初始化Event
			for (int k = 0; k < confPlot.eventIDs.length; k++) {
				ConfSceneEvent confEvent = ConfSceneEvent.get(confPlot.eventIDs[k]);
				SceneEvent event = new SceneEvent(confPlot.eventIDs[k], confEvent, plot);
				plot.events.put(event.id, event);
			}
			
			//加入自身到场景里
			plotList.add(plot);
			stageOjbect.plotMap.put(plot.id, plot);
		}
		
		//构建前置场景关系
		for (ScenePlot sp : plotList) {
			//如果没有前置场景
			if(sp.conf.frontPlotIDs.length == 0)
				continue;
			
			//挨个注入前置场景对象
			for (String fsp : sp.conf.frontPlotIDs) {
				for (ScenePlot spr : plotList) {
					if(spr.sn.equals(fsp))
						sp.prePlots.put(spr.id, spr);
				}
			}
		}
		
		//初始化正在进行中的场景，第一次舒适化，没有前置剧情的，都默认为开始
		String plotTriggerCurr = "";
		for (ScenePlot sp : plotList) {
			if(sp.conf.frontPlotIDs.length == 0){
				sp.status = ScenePlot.SCENT_PLOT_STATUS_DOING;
				for (SceneTrigger trigger : sp.triggers.values()) {
					plotTriggerCurr += trigger.id + ",";
				}
			}
		}
		
		//设置进入场景对象中
		stageOjbect.plotTriggerCurr = plotTriggerCurr;
	}
	
	/**
	 * 根据状态重新设置场景剧情
	 * @param stageObject
	 */
	public void resetPlots(StageObject stageObject) {
		String finish = "";
		String curr = "";
		for (ScenePlot sp : stageObject.plotMap.values()) {
			if(sp.isFinish()) {
				finish += sp.id + ",";
			}
			if(sp.status == ScenePlot.SCENT_PLOT_STATUS_DOING){
				for (SceneTrigger trigger : sp.triggers.values()) {
					curr += trigger.id + ",";
				}
			}
		}
		stageObject.plotFinish = finish;
		stageObject.plotTriggerCurr = curr;
		
		//判断哪些剧情没有触发器，可以直接开始事件
		for (ScenePlot sp : stageObject.plotMap.values()) { 
			//如果不是进行中，跳过
			if(sp.status != ScenePlot.SCENT_PLOT_STATUS_DOING){
				continue;
			}
				
			//如果可以开始事件，那么循环触发事件
			if(sp.canDoEvent()){
				doEvent(stageObject, sp);
			}
			
		}
	}
	
	/**
	 * 剧情事件完成
	 */
	public void finishEvent(long eventId, long poltId, StageObject stageObj) {
		//如果剧情不存在，返回
		ScenePlot plot = stageObj.plotMap.get(poltId);
		if(plot == null){
			return;
		}
		
		//如果当前剧情不是进行中，返回
		if(plot.status != ScenePlot.SCENT_PLOT_STATUS_DOING) {
			return;
		}
		
		//设置剧情完成状态
		SceneEvent event = plot.events.get(eventId);
		event.status = SceneEvent.SCENE_EVENT_STATUS_FINISH;
		
		//激活触发
		Event.fire(EventKey.SCENE_TRIGGER_08, "stageObj", stageObj, "event", event);
		
		//如果没完成剧情
		if(!plot.isFinish()){
			//推送单一剧情内容
			sendScenePlotOne(plot, stageObj);
			
			return;
		}
		
		//判断前置剧情是否完成，只处理没有开始的
		for (ScenePlot p : stageObj.plotMap.values()) {
			//只处理没有开始的
			if(p.status == ScenePlot.SCENT_PLOT_STATUS_NONE){
				 p.canStart();
			}
		}
		
		//如果有发生状态改变的，重置剧情
		resetPlots(stageObj);
		
		//给玩家推送构建的新剧情
		sendScenePlot(stageObj, null);
		
	}
	
	/**
	 * 给前端发送
	 * @param stageObj
	 */
	public void sendScenePlot(StageObject stageObj, HumanObject humanObj) {
		SCSceneInit.Builder msg = SCSceneInit.newBuilder();
		
		// 循环构建每个场景
		for (ScenePlot p : stageObj.plotMap.values()) {
			DScenePlot.Builder dPlot = DScenePlot.newBuilder();
			
			//构建基础信息
			dPlot.setId(p.id);
			dPlot.setSn(p.sn);
			dPlot.setStatus(p.status);
			
			//构建所有的触发器
			for (SceneTrigger t : p.triggers.values()) {
				DSceneTrigger.Builder dTrigger = DSceneTrigger.newBuilder();
				dTrigger.setId(t.id);
				dTrigger.setSn(t.sn);
				dTrigger.setStatus(t.status);
				
				//加入到场景里
				dPlot.addTriggers(dTrigger);
			}
			
			//构建所有的事件
			for (SceneEvent e : p.events.values()) {
				DSceneEvent.Builder dEvent = DSceneEvent.newBuilder();
				dEvent.setId(e.id);
				dEvent.setSn(e.sn);
				dEvent.setStatus(e.status);
				
				//加入到场景里
				dPlot.addEvents(dEvent);
			}
			
			//加入到住MSG里
			msg.addPlots(dPlot);
		}
		
		//给该地图内所有人发
		//发送 
		if(humanObj == null){
			for (HumanObject ho : stageObj.getHumanObjs().values()) {
				ho.sendMsg(msg);
			}
		} else {
			humanObj.sendMsg(msg);
		}	
	}
	
	/**
	 * 给前端发送
	 * @param stageObj
	 */
	public void sendScenePlotOne(ScenePlot plot, StageObject stageObj) {
		SCScenePlotChange.Builder msg = SCScenePlotChange.newBuilder();
		
		DScenePlot.Builder dPlot = DScenePlot.newBuilder();
		//构建基础信息
		dPlot.setId(plot.id);
		dPlot.setSn(plot.sn);
		dPlot.setStatus(plot.status);
			
		//构建所有的触发器
		for (SceneTrigger t : plot.triggers.values()) {
			DSceneTrigger.Builder dTrigger = DSceneTrigger.newBuilder();
			dTrigger.setId(t.id);
			dTrigger.setSn(t.sn);
			dTrigger.setStatus(t.status);
			
			//加入到场景里
			dPlot.addTriggers(dTrigger);
		}
		
		//构建所有的事件
		for (SceneEvent e : plot.events.values()) {
			DSceneEvent.Builder dEvent = DSceneEvent.newBuilder();
			dEvent.setId(e.id);
			dEvent.setSn(e.sn);
			dEvent.setStatus(e.status);
			
			//加入到场景里
			dPlot.addEvents(dEvent);
		}
			
		//加入到住MSG里
		msg.setPlot(dPlot);
		
		
		//给该地图内所有人发
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			humanObj.sendMsg(msg);
		}
	}
	
	/**
	 * 前端主动触发触发器
	 * @param stageObj
	 * @param plotId
	 * @param triggerId
	 */
	public void clientDoTrigger(StageObject stageObj, long plotId, long triggerId){
		ScenePlot plot = stageObj.plotMap.get(plotId);
		if(plot == null) return;
		
		SceneTrigger trigger = stageObj.plotTriggerAll.get(triggerId);
		if(trigger == null) return;
		
		if(trigger.type == SceneConstant.SCENE_TRIGGER_01){
			Event.fire(EventKey.SCENE_TRIGGER_01, "stageObj", stageObj, "trigger", trigger, "client", true);
		}
	}
	
	/**
	 * 监听器， 移动
	 * @param param
	 */
	@Listener(EventKey.SCENE_TRIGGER_01)
	public void _listener_SCENE_TRIGGER_01(Param param) {
		StageObject stageObj = param.get("stageObj");
		SceneTrigger trigger = param.get("trigger");
		Boolean client = param.get("client");
		
		//如果没有移动事件，返回
		if(trigger == null)
			return;
		
		//如果是前端触发
		if(!client)
			return;
		
		//如果完成，返回
		if(trigger.status == SceneTrigger.SCENE_TRIGGER_STATUS_FINISH)
			return;
		
		//如果类型不对也返回
		if(trigger.type != SceneConstant.SCENE_TRIGGER_01)
			return;
			
		//激活事件
		trigger.status = SceneTrigger.SCENE_TRIGGER_STATUS_FINISH;
		doEvent(stageObj, trigger.plot);
		
	}
	
	/**
	 * 监听器， 怪物剩余血量
	 * @param param
	 */
	@Listener(EventKey.SCENE_TRIGGER_02)
	public void _listener_SCENE_TRIGGER_02(Param param) {
		StageObject stageObj = param.get("stageObj");
		MonsterObject monsterObj = param.get("monsterObj");
		
		//如果没有没有当前的触发器
		if(stageObj.plotTriggerCurr.length() < 10)
			return;
		
		//循环判断剧情
		boolean done = false;
		SceneTrigger actTrigger = null;
		String[] plotCurrArr = stageObj.plotTriggerCurr.split(",");
		for (String s : plotCurrArr) {
			if(s.isEmpty()) continue;
			
			//拿到触发器
			long triggerId = Long.parseLong(s);
			SceneTrigger trigger = stageObj.plotTriggerAll.get(triggerId);
			
			//如果剧情不存在，跳过
			if(trigger == null){
				continue;
			}
			
			//如果类型不对，跳过
			if(trigger.type != SceneConstant.SCENE_TRIGGER_02)
				continue;
			
			//如果完成，跳过
			if(trigger.status == SceneTrigger.SCENE_TRIGGER_STATUS_FINISH)
				continue;
			
			//怪物类型不对，返回
			if(trigger.conf.nParam1 != monsterObj.stageObjectSn)
				continue;
			
			//如果死了，肯定满足条件
			if(monsterObj.isDie()){
				done = true;
				actTrigger = trigger;
				break;
			}
			
			//如果血量是数值
			if(trigger.conf.nParam2 == 1 && monsterObj.getUnit().getHpCur() <= trigger.conf.nParam3){
				done = true;
				actTrigger = trigger;
				break;
			}
			
			//如果血量是百分比
			if(trigger.conf.nParam2 == 2) {
				//剩余血量占比
				float value = 1.0F * monsterObj.getUnit().getHpCur() / monsterObj.getUnit().getHpMax();
				if(value <= trigger.conf.fParam1){
					done = true;
					actTrigger = trigger;
					break;
				}
			}
		}
		
		//激活事件
		if(done){
			actTrigger.status = SceneTrigger.SCENE_TRIGGER_STATUS_FINISH;
			doEvent(stageObj, actTrigger.plot);
		}
	}
	
	/**
	 * 监听器， 怪物死了
	 * @param param
	 */
	@Listener(EventKey.SCENE_TRIGGER_03)
	public void _listener_SCENE_TRIGGER_03(Param param) {
		StageObject stageObj = param.get("stageObj");
		
		//如果没有没有当前的触发器
		if(stageObj.plotTriggerCurr.length() < 10)
			return;
		
		//循环判断剧情
		boolean done = false;
		SceneTrigger actTrigger = null;
		String[] plotCurrArr = stageObj.plotTriggerCurr.split(",");
		for (String s : plotCurrArr) {
			if(s.isEmpty()) continue;
			
			//拿到触发器
			long triggerId = Long.parseLong(s);
			SceneTrigger trigger = stageObj.plotTriggerAll.get(triggerId);
			
			//如果剧情不存在，跳过
			if(trigger == null){
				continue;
			}
			
			//如果完成，跳过
			if(trigger.status == SceneTrigger.SCENE_TRIGGER_STATUS_FINISH)
				continue;
			
			//如果类型不对，跳过
			if(trigger.type != SceneConstant.SCENE_TRIGGER_03)
				continue;
			
			//判断怪物都死没死
			Set<Integer> allMid = new HashSet<>();
			for (int mid : trigger.conf.arrnParam1) {
				allMid.add(mid);
			}
			if(stageObj.monsterDies.containsAll(allMid)){
				actTrigger = trigger;
				done = true;
				break;
			}
		}
		
		//激活事件
		if(done){
			actTrigger.status = SceneTrigger.SCENE_TRIGGER_STATUS_FINISH;
			doEvent(stageObj, actTrigger.plot);
		}
	}
	
	/**
	 * 监听器，施放技能触发
	 * @param param
	 */
	@Listener(EventKey.SCENE_TRIGGER_04)
	public void _listener_SCENE_TRIGGER_04(Param param) {
		StageObject stageObj = param.get("stageObj");
		UnitObject unitObj = param.get("unitObj");
		int skillSn = param.get("skillSn");
		
		//如果没有没有当前的触发器
		if(stageObj.plotTriggerCurr.length() < 10)
			return;
		
		//循环判断剧情触发器
		boolean done = false;
		SceneTrigger actTrigger = null;
		String[] plotCurrArr = stageObj.plotTriggerCurr.split(",");
		for (String s : plotCurrArr) {
			if(s.isEmpty()) continue;
			
			//拿到触发器
			long triggerId = Long.parseLong(s);
			SceneTrigger trigger = stageObj.plotTriggerAll.get(triggerId);
			
			//如果剧情不存在，跳过
			if(trigger == null){
				continue;
			}
			
			//如果完成，跳过
			if(trigger.status == SceneTrigger.SCENE_TRIGGER_STATUS_FINISH)
				continue;
			
			//如果类型不对，跳过
			if(trigger.type != SceneConstant.SCENE_TRIGGER_04)
				continue;
			
			//如果有释放者
			if(trigger.conf.nParam1 == 0){
				if(trigger.conf.nParam2 == skillSn){
					actTrigger = trigger;
					done = true;
					break;
				}
			} else {
				if((trigger.conf.nParam1+"").equals(unitObj.sn) && trigger.conf.nParam2 == skillSn){
					actTrigger = trigger;
					done = true;
					break;
				}
			}
			
		}
		
		//激活事件
		if(done){
			actTrigger.status = SceneTrigger.SCENE_TRIGGER_STATUS_FINISH;
			doEvent(stageObj, actTrigger.plot);
		}
	}
	
	/**
	 * 监听器，敌对目标剩余数量
	 * @param param
	 */
	@Listener(EventKey.SCENE_TRIGGER_05)
	public void _listener_SCENE_TRIGGER_05(Param param) {
		StageObject stageObj = param.get("stageObj");
		
		//如果没有没有当前的剧情触发器
		if(stageObj.plotTriggerCurr.length() < 10)
			return;
		
		//循环判断剧情触发器
		boolean done = false;
		SceneTrigger actTrigger = null;
		String[] plotCurrArr = stageObj.plotTriggerCurr.split(",");
		for (String s : plotCurrArr) {
			if(s.isEmpty()) continue;
			
			//拿到触发器
			long triggerId = Long.parseLong(s);
			SceneTrigger trigger = stageObj.plotTriggerAll.get(triggerId);
			
			//如果剧情不存在，跳过
			if(trigger == null){
				continue;
			}
			
			//如果类型不对，跳过
			if(trigger.type != SceneConstant.SCENE_TRIGGER_05)
				continue;
			
			//如果完成，跳过
			if(trigger.status == SceneTrigger.SCENE_TRIGGER_STATUS_FINISH)
				continue;
			
			//如果有生还者
			int lost = 0;
			for (MonsterObject mo : stageObj.getMonsterObjs().values()) {
				if(mo.isDie()) continue;
				lost ++;
			}
			
			//判断是否小于要求
			System.out.println("LOST_____________________" + lost + ":" + trigger.conf.nParam1);
			if(lost > trigger.conf.nParam1){
				return;
			}
			
			actTrigger = trigger;
			done = true;
			break;
		}
		
		//激活事件
		if(done){
			actTrigger.status = SceneTrigger.SCENE_TRIGGER_STATUS_FINISH;
			doEvent(stageObj, actTrigger.plot);
		}
	}
	
	/**
	 * 监听器，进入副本
	 * @param param
	 */
	@Listener(EventKey.SCENE_TRIGGER_07)
	public void _listener_SCENE_TRIGGER_07(Param param) {
		StageObject stageObj = param.get("stageObj");
		
		//如果没有没有当前的剧情触发器
		if(stageObj.plotTriggerCurr.length() < 10)
			return;
		
		//循环判断剧情触发器
		boolean done = false;
		SceneTrigger actTrigger = null;
		String[] plotCurrArr = stageObj.plotTriggerCurr.split(",");
		for (String s : plotCurrArr) {
			if(s.isEmpty()) continue;
			
			//拿到触发器
			long triggerId = Long.parseLong(s);
			SceneTrigger trigger = stageObj.plotTriggerAll.get(triggerId);
			
			//如果剧情不存在，跳过
			if(trigger == null){
				continue;
			}
			
			//如果完成，跳过
			if(trigger.status == SceneTrigger.SCENE_TRIGGER_STATUS_FINISH)
				continue;
			
			//如果类型不对，跳过
			if(trigger.type != SceneConstant.SCENE_TRIGGER_07)
				continue;
			
			//如果有直接触发
			done = true;
			actTrigger = trigger;
			break;
		}
		
		//激活事件
		if(done){
			actTrigger.status = SceneTrigger.SCENE_TRIGGER_STATUS_FINISH;
			doEvent(stageObj, actTrigger.plot);
		}
	}
	
	/**
	 * 监听器，完成某个事件
	 * @param param
	 */
	@Listener(EventKey.SCENE_TRIGGER_08)
	public void _listener_SCENE_TRIGGER_08(Param param) {
		StageObject stageObj = param.get("stageObj");
		SceneEvent event = param.get("event");
		
		//如果没有没有当前的剧情触发器
		if(stageObj.plotTriggerCurr.length() < 10)
			return;
		
		//循环判断剧情触发器
		boolean done = false;
		SceneTrigger actTrigger = null;
		String[] plotCurrArr = stageObj.plotTriggerCurr.split(",");
		for (String s : plotCurrArr) {
			if(s.isEmpty()) continue;
			
			//拿到触发器
			long triggerId = Long.parseLong(s);
			SceneTrigger trigger = stageObj.plotTriggerAll.get(triggerId);
			
			//如果剧情不存在，跳过
			if(trigger == null){
				continue;
			}
			
			//如果完成，跳过
			if(trigger.status == SceneTrigger.SCENE_TRIGGER_STATUS_FINISH)
				continue;
			
			//如果类型不对，跳过
			if(trigger.type != SceneConstant.SCENE_TRIGGER_08)
				continue;
			
			//如果有直接触发
			if(trigger.conf.strParam1.contains(event.sn)){
				done = true;
				actTrigger = trigger;
				break;
			}
		}
		
		//激活事件
		if(done){
			actTrigger.status = SceneTrigger.SCENE_TRIGGER_STATUS_FINISH;
			doEvent(stageObj, actTrigger.plot);
		}
	}
	
	/**
	 * 满足触发条件，激活所有的事件
	 * 
	 * @param stageObj
	 * @param HumanObj
	 * @param events
	 */
	public void doEvent(StageObject stageObj, ScenePlot plot) {
		
		//判断剧情状态，如果不是进行中，直接返回
		if(plot.status != ScenePlot.SCENT_PLOT_STATUS_DOING){
			return;
		}
		
		// 挨个判断plot内的trigger是否都满足，如果满足激活事件
		for (SceneTrigger trigger : plot.triggers.values()) {
			if(trigger.status == SceneTrigger.SCENE_TRIGGER_STATUS_NONE){
				return;
			}
		}
		
		//挨个触发事件
		for (SceneEvent e : plot.events.values()) {
			
			//给一个默认的Key值
			int eventKey = 0;
			switch (e.type) {
			case SceneConstant.SCENE_EVENT_01:
				eventKey = EventKey.SCENE_EVENT_01;
				break;
			case SceneConstant.SCENE_EVENT_02:
				eventKey = EventKey.SCENE_EVENT_02;
				break;
			case SceneConstant.SCENE_EVENT_03:
				eventKey = EventKey.SCENE_EVENT_03;
				break;
			case SceneConstant.SCENE_EVENT_04:
				eventKey = EventKey.SCENE_EVENT_04;
				break;
			case SceneConstant.SCENE_EVENT_05:
				eventKey = EventKey.SCENE_EVENT_05;
				break;
			case SceneConstant.SCENE_EVENT_06:
				eventKey = EventKey.SCENE_EVENT_06;
				break;
			case SceneConstant.SCENE_EVENT_07:
				eventKey = EventKey.SCENE_EVENT_07;
				break;
			case SceneConstant.SCENE_EVENT_08:
				eventKey = EventKey.SCENE_EVENT_08;
				break;
			case SceneConstant.SCENE_EVENT_09:
				eventKey = EventKey.SCENE_EVENT_09;
				break;
			case SceneConstant.SCENE_EVENT_10:
				eventKey = EventKey.SCENE_EVENT_10;
				break;
			case SceneConstant.SCENE_EVENT_11:
				eventKey = EventKey.SCENE_EVENT_11;
				break;

				//如果没有匹配，直接返回
			default:
				return;
			}
			
			//触发事件激活
			Event.fire(eventKey, "stageObj", stageObj, "plot", plot, "eventId", e.id);
			
		}
	}
	
	/**
	 * 触发情景对话
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_01)
	public void _listener_SCENE_EVENT_01(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_01)
			return;
		
		//处理情景对话，推送给前端，由前端处理
		SCSceneEventStart.Builder msg = SCSceneEventStart.newBuilder();
		msg.setPoltId(plot.id);
		msg.setEventId(eventId);
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			humanObj.sendMsg(msg);
		}
		
		//完成事件
		//finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * QTE操作
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_02)
	public void _listener_SCENE_EVENT_02(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_02)
			return;
		
		//播放多媒体，推送给前端，由前端处理
		SCSceneEventStart.Builder msg = SCSceneEventStart.newBuilder();
		msg.setPoltId(plot.id);
		msg.setEventId(eventId);
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			humanObj.sendMsg(msg);
		}
		
		//完成事件
		//finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 播放多媒体
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_03)
	public void _listener_SCENE_EVENT_03(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_03)
			return;
		
		//播放多媒体，推送给前端，由前端处理
		SCSceneEventStart.Builder msg = SCSceneEventStart.newBuilder();
		msg.setPoltId(plot.id);
		msg.setEventId(eventId);
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			humanObj.sendMsg(msg);
		}
		
		//完成事件
		//finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 改变镜头
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_04)
	public void _listener_SCENE_EVENT_04(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_04)
			return;
		
		//改变镜头，推送给前端，由前端处理
		SCSceneEventStart.Builder msg = SCSceneEventStart.newBuilder();
		msg.setPoltId(plot.id);
		msg.setEventId(eventId);
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			humanObj.sendMsg(msg);
		}
		
		//完成事件
		//finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 刷怪
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_05)
	public void _listener_SCENE_EVENT_05(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_05)
			return;
		
		//刷怪
		stageObj.monsterToBirth = sceneEvent.conf.arrnParam1;
		stageObj.createMosnter();
		
		//完成事件
		finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 技能
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_06)
	public void _listener_SCENE_EVENT_06(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_06)
			return;
		
		/* 处理技能 */
		//如果是0，那么就是人来释放
		if(sceneEvent.conf.nParam1 == 0){
			for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
				SkillManager.inst().doSimpleSkill(humanObj, sceneEvent.conf.nParam2);
			}
		} else {
			for (UnitObject unitObj : stageObj.getUnitObjs().values()) {
				
				//如果不是要求的施法者，发技能
				if(unitObj.sn.equals(sceneEvent.conf.nParam1+"")) continue;
				
				//如果死了，跳过
				if(!unitObj.isInWorld() || unitObj.isDie()) continue;
				
				//发技能
				SkillManager.inst().doSimpleSkill(unitObj, sceneEvent.conf.nParam2);
				break;
			}
		}
		
		//完成事件
		finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 机关
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_07)
	public void _listener_SCENE_EVENT_07(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_07)
			return;
		
		//机关，推送给前端，由前端处理
		SCSceneEventStart.Builder msg = SCSceneEventStart.newBuilder();
		msg.setPoltId(plot.id);
		msg.setEventId(eventId);
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			humanObj.sendMsg(msg);
		}
		
		//完成事件
		//finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 玩家移动
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_08)
	public void _listener_SCENE_EVENT_08(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_08)
			return;
		
		//移动，推送给前端，由前端处理
		SCSceneEventStart.Builder msg = SCSceneEventStart.newBuilder();
		msg.setPoltId(plot.id);
		msg.setEventId(eventId);
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			humanObj.sendMsg(msg);
		}
		
		//完成事件
		//finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 处理道具
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_09)
	public void _listener_SCENE_EVENT_09(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_09)
			return;
		
		//处理道具
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			
			//大于0是获得道具，小于0是删除道具
			if(sceneEvent.conf.nParam2 > 0) {
				ItemBagManager.inst().add(humanObj, sceneEvent.conf.nParam1, sceneEvent.conf.nParam2, 0);
			} else {
				ItemBagManager.inst().remove(humanObj, sceneEvent.conf.nParam1, sceneEvent.conf.nParam2, 0);
			}
			
		}
		
		//完成事件
		finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 处理警告/通知
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_10)
	public void _listener_SCENE_EVENT_10(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_10)
			return;
		
		//处理警告/通知 由前端来处理
		SCSceneEventStart.Builder msg = SCSceneEventStart.newBuilder();
		msg.setPoltId(plot.id);
		msg.setEventId(eventId);
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			humanObj.sendMsg(msg);
		}
		
		//完成事件
		//finishEvent(eventId, plot.id, stageObj);
	}
	
	/**
	 * 结算结果，推送结算
	 * @param param
	 */
	@Listener(EventKey.SCENE_EVENT_11)
	public void _listener_SCENE_EVENT_11(Param param) {
		StageObject stageObj = param.get("stageObj");
		ScenePlot plot = param.get("plot");
		long eventId = param.get("eventId");
		
		//提取当前执行的事件
		SceneEvent sceneEvent = plot.events.get(eventId);
		//如果不存在，直接返回
		if(sceneEvent == null) 
			return;
		//如果类型不对返回
		if(sceneEvent.type != SceneConstant.SCENE_EVENT_11)
			return;
		
		//处理结算 TODO 后续有什么玩家，都要加进来
		for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
			//如果是副本，弹出副本结算
			if(stageObj instanceof StageObejctInstance){
				InstanceManager.inst().repEnd(humanObj);
				
				
			} else if (stageObj instanceof StageObjectTower){
				//TowerManager.inst().layerEnd(humanObj);
			} 
		}
		
		//完成事件
		finishEvent(eventId, plot.id, stageObj);
	}

}