package org.gof.demo.battlesrv.skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.manager.FightManager;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.logic.AbstractSkillLogic;
import org.gof.demo.battlesrv.skill.logic.SkillLogicManager;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.battlesrv.support.UnitObjectStateKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfInborn;
import org.gof.demo.worldsrv.config.ConfParam;
import org.gof.demo.worldsrv.config.ConfSkill;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.DHpChgOnce;
import org.gof.demo.worldsrv.msg.Msg.DHpChgTar;
import org.gof.demo.worldsrv.msg.Msg.SCFightHpChg;
import org.gof.demo.worldsrv.msg.Msg.SCFightSkill;
import org.gof.demo.worldsrv.msg.Msg.SCFightSkill.Builder;
import org.gof.demo.worldsrv.skill.InbornVO;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class SkillCommon {
	
	public SkillVO confSkill;										//技能配置
	public UnitObject unitObj;										//释放者
	public int skillLevel;											//技能等级
	public List<AbstractSkillLogic> logics = new ArrayList<>();	//技能效果
	public Map<String, Integer> temp = new HashMap<>();			//临时信息
	public int comboCount;											//连击次数
	public SCFightSkill.Builder scFightSkill = null;				//技能广播包
	public SCFightHpChg.Builder hpChangeMsg = null;				//技能伤害血量包
	public Vector2D dVec = null;								//人物技能攻击向前追击
	public double skillShakePct = 1;							//放技能的时候前摇进行的百分比
	public int mutilMagic = 0; //多重施法次数
	public int rageSkillValue = 1000;					//可以释放怒气技能的 怒气大小
	
	
	public SkillCommon(UnitObject unitObj, ConfSkill confSkill, int skillLevel) {
		this.unitObj = unitObj;
		this.confSkill = new SkillVO(confSkill);
		this.skillLevel = skillLevel;
		
		rageSkillValue = Utils.intValue(ConfParam.getBy(ConfParam.K.sn, "rageMax").value);
		//初始化技能效果
		try {
			
			for(int effSn : confSkill.effects) {
				//查找效果配置表 //skillLevel
				ConfSkillEffect confSkillEffect = ConfSkillEffect.getBy("level", 1, "effectSn", effSn);
				
				//效果表中没有找到
				if(confSkillEffect == null) continue;
				
				//初始化技能效果
				logics.add(SkillLogicManager.inst().initLogic(this, confSkillEffect.sn));
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 是否可以施放技能
	 * @return
	 */
	public ReasonResult canCast(SkillParam param) {
		//开场技能不能被释放
		if(confSkill.enterScene) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.stateCanNotCast"));
		}
		//处于不能释放技能的状态
		if(!unitObj.canCastSkill && confSkill.type == 0) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.stateCanNotCast"));
		}
		//处于不能普攻的状态
		if(!unitObj.canAttack && confSkill.type == 3) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.stateCanNotCast"));
		}
		//技能释放中，不能打断
		if(unitObj.state.containsKey(UnitObjectStateKey.cast_skilling)){
			return new ReasonResult(false, "技能释放中，不能打断");
		}
		//该技能是被动技能，无法施放
		if(!confSkill.active) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.skillNotActive"));
		}
		//施法者死亡
		if(unitObj.isDie()) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.die"));
		}
		//目标不能死亡
		if(param.tarUo != null && param.tarUo.isDie()) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.tarDie"));
		}
		
		//状态受限
		ReasonResult tmp = isLimitNormal();
		if(!tmp.success) {
			return tmp;
		}
		
		//冷却中
		tmp = isCoolingNormal(param);
		if(!tmp.success) {
			return tmp;
		}
		
		//目标不合法
		tmp = isLegal(param);
		if(!tmp.success) {
			return tmp;
		}
			
		//不再范围内
		tmp = isScope(param);
		if(!tmp.success) {
			return tmp;
		}

		return new ReasonResult(true, "");
	}
	
	
	/**
	 * 施放技能,第一阶段
	 * @param position
	 */
	public void castFirst(SkillParam position) {
		//根据atkSpeed 计算前摇时间
		int shakeFrontTime = calcSharkTime(SkillSharkTypeKey.SKILL_SHARK_FRONT);
		if(shakeFrontTime > 0) {
			//有前摇技能，就是有延迟效果的
			//前摇技能，玩家停止移动,魂将释放则不限制移动
			if(unitObj.running.isRunning()) {
				unitObj.stop();
			}
			
			//计算前摇结束时间
			SkillExcute skillExcute = new SkillExcute(confSkill.sn, position, unitObj.getTime(), shakeFrontTime);
			unitObj.skillTempInfo.skillToExcute = skillExcute;
			
			//进入前摇状态
			unitObj.toState(UnitObjectStateKey.skill_shake, shakeFrontTime);
			
			//如果是黑屏状态那么直接处理 只有是武将才会黑屏
//			if(confSkill.blackTime > 0 && (unitObj.isGeneralObj() || unitObj.isHumanObj())) { 
			if(confSkill.blackTime > 0 && unitObj.getHumanObj() != null) { 
				unitObj.stageObj.stopStageObjTime(unitObj, confSkill.blackTime);
			}
		} else {
			//怪物释放技能，进入1s的发呆时间（播放释放技能的需要时间）
			if(unitObj.isMonsterObj()) {
				unitObj.toState(UnitObjectStateKey.skill_shake, 1000);
			}
		}
		
		//设置技能总体释放状态：前摇+激发+后摇
		int allTime = calcSharkTime(SkillSharkTypeKey.SKILL_SHARK_FRONT) + 
							 calcSharkTime(SkillSharkTypeKey.SKILL_SHARK_CASTING) + 
							 calcSharkTime(SkillSharkTypeKey.SKILL_SHARK_BACK);
		unitObj.toState(UnitObjectStateKey.cast_skilling, allTime);
		//如果技能锁定，那么不能移动
		if(!this.confSkill.canMove){
			unitObj.toState(UnitObjectStateKey.cast_locked, allTime);
		}
		
		//放技能的时候停止移动,魂将放技能的时候不管
		if(unitObj.running.isRunning()) {
			unitObj.stop();
		}
		
		SkillParamVO vo1 = new SkillParamVO();
		Event.fire(EventKey.SKILL_PASSIVE_CHECK, 
							"unitAtk", unitObj,
							"unitDef", null, 
							"SkillParam", position,
							"skillEventKey", SkillEventKey.EVENT_ON_SKILL_BEFORE, 
							"vo", vo1);
		
		//按效果顺序依次施放
		doLogics(position, 0);
		
		//技能发送事件
		fireAttackedEvent(0);
		
		//广播技能包
		broadSkillMsgFirst(position);

		//造成伤害，判断是否有吸血被动
		int hpLost = getTemp("hurt");
		
		//判断扣血后是否有被动技能触发(吸血)
		SkillParamVO vo = new SkillParamVO();
		Event.fire(EventKey.SKILL_PASSIVE_CHECK, 
							"unitAtk", unitObj,
							"unitDef", null, 
							"SkillParam", position,
							"skillEventKey", SkillEventKey.EVENT_ON_SKILL_END, 
							"vo", vo);
		
		if(FightManager.inst().isSuck(unitObj)) {
			double bloodSuck = vo.bloodSuckPct * FightManager.inst().bloodSuck(unitObj, hpLost);
			if(bloodSuck > 0) {
				//吸血
				UnitManager.inst().addHp(unitObj, HpLostKey.SKILL, (int)bloodSuck, unitObj);
			}
		}
		
		//放入技能冷却中
		addCoolDown();
		
		
		//如果技能只有一个阶段清空本次技能缓存数据，否则第二阶段清空
		if(shakeFrontTime == 0) {
			temp.clear();
		}
		
		//发送战斗单元攻击事件
		Event.fire(EventKey.UNIT_ATTACK, "unitObj", unitObj);
		// 发送战斗单元动作事件
		Event.fire(EventKey.UNIT_ACT, "unitObj", unitObj);
		if(unitObj.isHumanObj()) {
			// 发送玩家攻击事件
			Event.fire(EventKey.HUMAN_ATTACK, "humanObj", unitObj);
			// 发送玩家动作事件
			Event.fire(EventKey.HUMAN_ACT, "humanObj", unitObj);
		} else if(unitObj.isMonsterObj()) {
			// 发送怪物攻击事件
			Event.fire(EventKey.MONSTER_ATTACK, "monsterObj", unitObj);
			// 发送怪物动作事件
			Event.fire(EventKey.MONSTER_ACT, "monsterObj", unitObj);
		}
		
	}
	
	/**
	 * 释放技能，第二阶段
	 * @param position
	 */
	public void castSecond(SkillParam position) {
		//按效果顺序依次施放
		doLogics(position, 1);

		//技能发送事件
		fireAttackedEvent(1);
		
		//广播技能包
		broadSKillMsgSecond(position);
		
		//造成伤害，判断是否有吸血被动
		int hpLost = getTemp("hurt");
		
		//判断扣血后是否有被动技能触发(吸血)
		SkillParamVO vo = new SkillParamVO();
		Event.fire(EventKey.SKILL_PASSIVE_CHECK, 
							"unitAtk", unitObj,
							"unitDef", null, 
							"SkillParam", position,
							"skillEventKey", SkillEventKey.EVENT_ON_SKILL_END, 
							"vo", vo);
		
		if(FightManager.inst().isSuck(unitObj)) {
			double bloodSuck = vo.bloodSuckPct * FightManager.inst().bloodSuck(unitObj, hpLost);
			if(bloodSuck > 0) {
				//吸血
				UnitManager.inst().addHp(unitObj, HpLostKey.SKILL, (int)bloodSuck, unitObj);
			}
		}
		
		//清空本次技能缓存数据
		temp.clear();
	}
	
	/**
	 * 受限制的状态是否正常
	 * @return
	 */
	public ReasonResult isLimitNormal() {
		return new ReasonResult(true, "");
	}
	
	/**
	 * 记录冷却
	 */
	public void addCoolDown() {
		//放入技能冷却中
		SkillTempInfo skillInfo = unitObj.skillTempInfo;
		
		//没有冷却
		int sharkCoolTime = this.calcSharkTime(SkillSharkTypeKey.SKILL_SHARK_COOLDOWN);
		if(sharkCoolTime <= 200) return ;
		
		int realCoolTime =  sharkCoolTime * 1;
		
		if(realCoolTime < 200) {
			realCoolTime = 200;
		}
		//加入技能冷却
		skillInfo.cooldown.put(confSkill.sn, unitObj.getTime() + realCoolTime);
	}
	
	/**
	 * 冷却状态是否正常
	 * @return
	 */
	public ReasonResult isCoolingNormal(SkillParam param) {
		SkillTempInfo skillInfo = unitObj.skillTempInfo;
		
		//技能处于常规冷却
		if(skillInfo.cooldown.containsKey(confSkill.sn)) {
			if(skillInfo.cooldown.get(confSkill.sn) > unitObj.getTime()) {
				return new ReasonResult(false, I18n.get("skill.skillCommon.inCooling"));
			}
		}
		
		return new ReasonResult(true, "");
	}
	
	/** 鼠标点选类型 */
	public static int CLICK_TAR = 1;		//目标是人或者怪
	public static int CLICK_VECTOR = 2;	//目标是坐标
	public static int CLICK_SELF = 3;		//目标是自己
	
	/**
	 * 是否合法目标
	 * @param position
	 * @return
	 */
	public ReasonResult isLegal(SkillParam position) {
		//有目标型
		if(confSkill.clickType == CLICK_TAR && position.tarUo == null) {
			return new ReasonResult(false, I18n.get("fight.isLegal.tarIllegal"));
		}
		
		//坐标型
		if(confSkill.clickType == CLICK_VECTOR && position.tarPos == null) {
			return new ReasonResult(false, I18n.get("fight.isLegal.tarIllegal"));
		}
		
		//如果不是可对自己释放的技能不能攻击自己
		if(confSkill.clickType != CLICK_SELF && unitObj.isHumanObj() && position.tarUo == unitObj) {
			return new ReasonResult(false, I18n.get("skill.checkRight.tarNotSelf"));
		}
		
		//按效果顺序依次施放
		for(AbstractSkillLogic logic : logics) {
			ReasonResult rr = logic.isLegal(position);
			if(!rr.success) {
				return rr;
			}
		}
		
		
		return new ReasonResult(true, "");
	}
	
	public ReasonResult isScope(SkillParam pos) {
		
		float distanceError = 0.5f;
		//目标是自己
		if(confSkill.clickType == CLICK_SELF) {
			return new ReasonResult(true, "");
		}
		
		//自己位置
		Vector2D ownPos = unitObj.posNow;
		
		//目标距离
		double distance = 0;

		//目标是人或者怪物
		if(confSkill.clickType == CLICK_TAR) {
			distance = ownPos.distance(pos.tarUo.posNow);
			
			//如果目标为怪物，则减去目标的碰撞半径
			distance -= (pos.tarUo).confModel.collisionRadius;
		} else {
			//目标是坐标
			distance = ownPos.distance(pos.tarPos);
		}
		
		//如果施放技能方为怪物，则减去攻方的碰撞半径
		distance -= (unitObj).confModel.collisionRadius;
		distance = Math.max(0, distance);
		
		//距离小于最小射程
		if(confSkill.rangeMin - distanceError >0 && distance < confSkill.rangeMin - distanceError) {
			return new ReasonResult(false, I18n.get("skill.skillCommon.scopeNear"));
		}
		
		//距离大于最大射程
		if(distance > confSkill.rangeMax + distanceError) {
			return new ReasonResult(false, I18n.get("skill.skillCommon.scopeFar"));
		}
		
		return new ReasonResult(true, "");
	}
	
	/**
	 * 顺序使用技能效果
	 * @param position
	 */
	private void doLogics(SkillParam position, int period) {
		// 初始化技能消息
		if (scFightSkill == null) {
			scFightSkill = SCFightSkill.newBuilder();
		}
		
		for(AbstractSkillLogic logic : logics) {
			//两段式：不是当前阶段
			if(logic.conf.period != period) continue;
			
			//判断是否是当前连击次数的逻辑效果
			boolean flag = false;
			for(int count : logic.conf.comboCount) {
				if(count == comboCount) {
					flag = true;
					break;
				}
			}
			if(!flag) continue;
			
			//判断是否应该触发该技能效果
			double rand = unitObj.nextDouble();
			if(rand > logic.conf.triggerPct) continue ;
			
			//触发
//			if(!logic.canDoSkillEffect(position).success) continue;
			logic.doSkillEffect(position);
		}
	}
	
	public Builder createFightMsg(SkillParam position) {
		//构建技能广播包
		if(scFightSkill == null) {
			scFightSkill = Msg.SCFightSkill.newBuilder();
		}
		
		//释放技能者的id
		scFightSkill.setCastId(unitObj.id);
		//技能id
		scFightSkill.setSkillId(confSkill.sn);
		//技能目标
		if(position.tarUo != null) {
			scFightSkill.setTarId(position.tarUo.id);
		}
		if(position.tarPos != null) {
			scFightSkill.setPos(position.tarPos.toMsg());
		}
		//设置攻击类型是人物攻击
		scFightSkill.setAtkerType(1);
		//将技能造成的掉血信息加入技能包中
		if(hpChangeMsg != null) {
			scFightSkill.setHpChg(hpChangeMsg);
		}
		
		//加入人物攻击追击位移
		if(dVec != null) {
			scFightSkill.setAttPos(dVec.toMsg());
		}
		
		//加入技能前摇百分比
		scFightSkill.setSkillShakePct(skillShakePct);
		
		scFightSkill.setMutilMagic(mutilMagic);
		
		//设置连击次数
		scFightSkill.setComboCount(comboCount);
				
		return scFightSkill;
	}
	
	
	/**
	 * 广播技能：第一阶段
	 * @param position
	 */
	protected void broadSkillMsgFirst(SkillParam position) {
		Builder msg = createFightMsg(position);
		msg.setPeriod(1);
		msg.setAtkerType(1);
		msg.setFinal(position.finalAtk);
		
		//循环本地图玩家,发送技能包
		StageManager.inst().sendMsgToArea(msg, unitObj.stageObj, unitObj.posNow);
		
		//发送完消息后，清空
		scFightSkill = null;
		hpChangeMsg = null;
		dVec = null;
		skillShakePct = 1;
	}
	
	/**
	 * 广播技能消息：第二阶段
	 * @param position
	 */
	protected void broadSKillMsgSecond(SkillParam position) {
		Builder msg = createFightMsg(position);
		msg.setPeriod(2);
		msg.setAtkerType(1);
		msg.setFinal(position.finalAtk);
		
		//循环本地图玩家,发送技能包
		StageManager.inst().sendMsgToArea(msg, unitObj.stageObj, unitObj.posNow);
		
		//发送完消息后，清空
		scFightSkill = null;
		hpChangeMsg = null;
		dVec = null;
		mutilMagic = 0;
	}
	
	/**
	 * 发送技能命中单位被攻击事件
	 */
	private void fireAttackedEvent(int period) {
		//派发释放技能事件，处理怒气
		Event.fire(EventKey.UNIT_DO_SKILL, "attacker", unitObj, "skillSn", confSkill.sn, "period", period);
		
		//场景触发器：技能派发
		Event.fire(EventKey.SCENE_TRIGGER_04, "stageObj", unitObj.stageObj, "unitObj", unitObj, "skillSn", confSkill.sn);
	}
	
	/**
	 * 添加技能的缓存
	 * @param key
	 * @param value
	 */
	public void addTemp(String key, int value) {
		if(temp.containsKey(key)) {
			temp.put(key, temp.get(key) + value);
		} else {
			temp.put(key, value);
		}
	}
	
	/**
	 * 获取技能的缓存量
	 * @param key
	 * @return
	 */
	public int getTemp(String key) {
		if(temp.containsKey(key)) {
			return temp.get(key);
		} else {
			return 0;
		}
	}
	
	/**
	 * 将本次造成掉血消息构造进用户造成掉血信息
	 * @param humanObject
	 * @param id
	 * @param dhpChgOnce
	 */
	public void addSkillHpChg(long id, DHpChgOnce.Builder dhpChgOnce) {
		if(hpChangeMsg == null) {
			hpChangeMsg = SCFightHpChg.newBuilder();
		}
		
		boolean flag = false;
		DHpChgTar.Builder dhpTar = DHpChgTar.newBuilder(); 
		dhpTar.setId(id);
		for(DHpChgTar.Builder tmp : hpChangeMsg.getDhpChgTarBuilderList()) {
			if(tmp.getId() != id) continue;
			dhpTar = tmp;
			flag = true;
		}
		
		dhpTar.addDhpChgOnce(dhpChgOnce);
		
		if(!flag) {
			hpChangeMsg.addDhpChgTar(dhpTar);
		}
	}
	
	private boolean canCombo(UnitObject unitObj, SkillVO confSkill, SkillParam param) {
		
		//非连击技能
		if(confSkill.combo <= 0) return false;
		//上次释放的技能不是该技能
		if(unitObj.skillTempInfo.lastSkillSn != confSkill.sn) return false;
		
		//施法间隔大于连击时间 
		int comboTime = calcSharkTime(SkillSharkTypeKey.SKILL_SHARK_COMBO);
		if(unitObj.getTime() - unitObj.skillTempInfo.lastSkillTime > comboTime) return false;
		
		//超过最大连击次数
		if(comboCount >= confSkill.combo - 1 ) return false;
		
		return true;
	}
	
	/**
	 * 计算连击数
	 * @param finalAtk		主动请求连击最后一击
	 */
	public void comboCal(SkillParam param) {
		//判断是否连击
		long now = unitObj.getTime();
		if(canCombo(unitObj, confSkill, param)) {
			//主动请求连击最后一击的，直接将连击次数设最大值
			if(param.finalAtk) {
				comboCount = confSkill.combo - 1;
			} else {
				//连击数+1
				comboCount += 1;
			}
		} else {
			comboCount = 0;
		}
		
		//设置最近一次释放的技能sn和时间
		unitObj.skillTempInfo.lastSkillSn = confSkill.sn;
		unitObj.skillTempInfo.lastSkillTime = now;
		
	}
	
	/**
	 * 根据不同的阶段类型，获取响应的时间
	 */
	public int calcSharkTime(SkillSharkTypeKey type) {
		return calcSharkTime(type, this.confSkill);
	}
	
	/**
	 * 根据不同的阶段类型，获取响应的时间
	 */
	public int calcSharkTime(SkillSharkTypeKey type, SkillVO confSkill) {
		int time = 0;			//返回时间
		int subKey = 0;		//数组下标
		
		//如果是连击技能，就获取当前连击次数
		if(confSkill.combo > 0) {
			subKey = comboCount;
		}
		
		switch (type) {
		// 前摇
		case SKILL_SHARK_FRONT:	
			time = confSkill.shakeFront[subKey];
			break;
			
		// 激发
		case SKILL_SHARK_CASTING:
			time = confSkill.casting[subKey];
			break;
			
		// 后摇
		case SKILL_SHARK_BACK:
			time = confSkill.shakeBack[subKey];
			break;
			
		// 冷却
		case SKILL_SHARK_COOLDOWN:
			time = confSkill.coolTime[subKey];
			break;
			
		// 连击
		case SKILL_SHARK_COMBO:
			time = confSkill.comboTime[subKey];
			break;

		default:
			break;
		}
		
		return time;
	}
	
	/**
	 * 直接取消当前技能
	 */
	public void cancelCurrSkill(){
		unitObj.cancelCurrSkill();
	}
	/**
	 * 天赋更改技能属性
	 * @param 
	 */
	public void inbornToSkill(InbornVO invo){
		ConfInborn conf = ConfInborn.get(invo.sn);
		if(conf.type == 3 ) {
			inbornToEffect(invo);
			return ;
		}
		if(conf.type != 2 ) return;
		//对可累加的参数进行处理
		for(int i = 0 ; i < conf.param.length ; i++){
			if(conf.param[i].equals("range")){    //改变技能范围
				this.confSkill.range += conf.baseValue[i]+conf.growValue[i]*invo.level;
				
			}else if(conf.param[i].equals("coolTime")){   //改变技能效果的冷却时间
				for(int j = 0; j < this.confSkill.coolTime.length; j++)
				this.confSkill.coolTime[j] += (int)(conf.baseValue[i]+conf.growValue[i]*invo.level);
			}
		}
		//对直接赋值的参数进行处理
		
		for(int i = 0; i < conf.param1.length; i++){
			if(conf.param1[i].equals("atkType")){
				int atkType = Integer.parseInt(conf.value1[i]);
				this.confSkill.atkType = atkType;
			}else if(conf.param1[i].equals("canInterrupt")){
				if(conf.value1[i].equals("true")){
					this.confSkill.canInterrupt = true;
				}else{
					this.confSkill.canInterrupt = false;
				}
				
			}else if(conf.param1[i].equals("canInterrupted")){
				if(conf.value1[i] .equals("true")){
					this.confSkill.canInterrupted = true;
				}else{
					this.confSkill.canInterrupted = false;
				}
				
			}else if(conf.param1[i].equals("range")){    //改变技能范围
				int range = Integer.parseInt(conf.value1[i]);
				this.confSkill.range = range;
				
			}else if(conf.param1[i].equals("coolTime")){   //改变技能效果的冷却时间

			}else if(conf.param1[i].equals("effects")){
				int effectSn = Integer.parseInt(conf.value1[i]);
                ConfSkillEffect confSkillEffect = ConfSkillEffect.getBy("level", 1, "effectSn", effectSn);
				//效果表中没有找到
				if(confSkillEffect == null) continue;
				//初始化技能效果
				logics.add(SkillLogicManager.inst().initLogic(this, confSkillEffect.sn));
			}
		}
	}
	/**
	 * 天赋改变该技能中某个技能效果
	 * @param invo
	 */
	public void inbornToEffect(InbornVO invo){
		ConfInborn conf = ConfInborn.get(invo.sn);
		for(AbstractSkillLogic logic : logics) {
			if(logic.conf.sn != conf.effectSn)continue;
			
			for(int i = 0; i < conf.param1.length; i++){
				if(conf.param1[i].equals("scopeType")){
					int scopeType = Integer.parseInt(conf.value1[i]);
					logic.conf.scopeType = scopeType;
				}else if(conf.param1[i].equals("scopeParam1")){
					float scopeParam1 = Float.parseFloat(conf.value1[i]);
					logic.conf.scopeParam1 = scopeParam1;
				}else if(conf.param1[i].equals("scopeParam2")){
					int scopeParam2 = Integer.parseInt(conf.value1[i]);
					logic.conf.scopeParam2 = scopeParam2;
				}else if(conf.param1[i].equals("scopeParam3")){
					int scopeParam3 = Integer.parseInt(conf.value1[i]);
					logic.conf.scopeParam3 = scopeParam3;
				}else if(conf.param1[i].equals("targetNum")){
					int targetNum = Integer.parseInt(conf.value1[i]);
					logic.conf.targetNum = targetNum;
				}else if(conf.param1[i].equals("effectDis")){
					float effectDis = Float.parseFloat(conf.value1[i]);
					logic.conf.effectDis = effectDis;
				}else if(conf.param1[i].equals("logicSn")){
					int logicSn = Integer.parseInt(conf.value1[i]);
					logic.conf.logicSn = logicSn;
				}else if(conf.param1[i].equals("param1")){
					logic.conf.param1 = conf.value1[i];
				}else if(conf.param1[i].equals("param2")){
					logic.conf.param2 = conf.value1[i];
				}else if(conf.param1[i].equals("param3")){
					logic.conf.param3 = conf.value1[i];
				}else if(conf.param1[i].equals("param4")){
					logic.conf.param4 = conf.value1[i];
				}else if(conf.param1[i].equals("param5")){
					logic.conf.param5 = conf.value1[i];
				}else if(conf.param1[i].equals("param6")){
					logic.conf.param6 = conf.value1[i];
				}else if(conf.param1[i].equals("param7")){
					logic.conf.param7 = conf.value1[i];
				}else if(conf.param1[i].equals("param8")){
					logic.conf.param8 = conf.value1[i];
				}
		    }
			
			
			
			for(int i = 0 ; i < conf.param.length ; i++){
				if(conf.param[i].equals("targetNum")){    //范围搜索的目标个数
					logic.conf.targetNum += (int)(conf.baseValue[i]+conf.growValue[i]*invo.level);
					
				}else{  
				}
			}
				
		}
	}
}
