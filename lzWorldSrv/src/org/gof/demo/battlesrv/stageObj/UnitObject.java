package org.gof.demo.battlesrv.stageObj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.TickTimer;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.arpg.HumanCompeteAI;
import org.gof.demo.battlesrv.buff.BuffManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillExcute;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.skill.SkillTempInfo;
import org.gof.demo.battlesrv.support.PropCalcCommon;
import org.gof.demo.battlesrv.support.Running;
import org.gof.demo.battlesrv.support.UnitObjectStateKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.MonsterObject;
import org.gof.demo.worldsrv.config.ConfCharacterModel;
import org.gof.demo.worldsrv.config.ConfSkill;
import org.gof.demo.worldsrv.entity.Buff;
import org.gof.demo.worldsrv.entity.EntityUnitPropPlus;
import org.gof.demo.worldsrv.entity.Unit;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.SCSkillInterrupt;
import org.gof.demo.worldsrv.msg.Msg.SCStageMoveStop;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectDisappear;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectInfoChange;
import org.gof.demo.worldsrv.msg.Msg.SCUnitobjStatusChange;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

/**
 * 角色基类 包含移动、战斗等
 */
public abstract class UnitObject extends WorldObject {
	public UnitDataPersistance dataPers = new UnitDataPersistance();
	
	public AI ai; //AI模块
	public ConfCharacterModel confModel = null; //模型相关配置
	public Running running = new Running(this); // 玩家移动信息
	
	/** 技能 */
	public SkillTempInfo skillTempInfo = new SkillTempInfo(); 					// 释放的临时技能信息
	public final Map<Integer, SkillCommon> skills = new HashMap<>();	// 所有的技能
	public List<Integer> skillOrder = new ArrayList<Integer>();					// 技能释放顺序
	public SkillCommon defaultSkill;															// 默认技能，为主动技能的第一个
	
	public boolean canMove = true; 	// 是否可移动
	public boolean canCastSkill = true; // 是否可以施放技能
	public boolean canAttack = true;	// 是否可以攻击
	public boolean castSkilling = false; // 是否正在释放技能
	
	//Buff更新Timer
	public TickTimer timerBuffPulse = null;	
	
	// unitObj的状态
	public Map<UnitObjectStateKey, TickTimer> state = new HashMap<>();
	public TickTimer timerStatePulse = null;

	//保存出生的位置
	public Vector2D posBegin = new Vector2D();			//出生位置
	
	//从属关系
	public HumanObject parentObject = null;
	public long teamBundleID = -1; //这是识别小队的时候敌我双方的标志
	
	public Map<Long, UnitObject> beAttacked = new HashMap<>(); //正在被攻击

	public Map<Long, Integer> aggroList = new HashMap<>();    //仇恨值列表
	
	public int order; //站位顺序
	public int index = 0; //当前波数的位置索引
	public int matrixIndex; //阵型站位索引
	public int entryIndex; //战斗群 按order的排序
	public int profession; //职业
	
	public UnitObject(StageObject stageObj) {
		super(stageObj);
	}
	

	@Override
	public void startup() {
		//一些初始化工作
		posNow.x = posBegin.x;
		posNow.y = posBegin.y;
		
		profession = getUnit().getProfession();
	}
	

	public boolean isHumanObj() {
		return this instanceof HumanObject;
	}

	public boolean isMonsterObj() {
		return this instanceof MonsterObject;
	}
	
	public boolean isGeneralObj() {
		return this instanceof GeneralObject;
	}
	
	public long getHumanId() {
		if(isHumanObj()) {
			return id;
		} else if(isGeneralObj()) {
			return parentObject.id;
		}
		
		return -1;
	}
	
	public HumanObject getHumanObj() {
		if(isHumanObj()) {
			return (HumanObject)this;
		} else if(isGeneralObj() && parentObject != null && parentObject.isHumanObj()) {
			return (HumanObject)parentObject;
		}
		
		return null;
	}
	
	@Override
	public void pulse(int deltaTime) {
		if(timerBuffPulse == null) {
			timerBuffPulse = new TickTimer();
			timerBuffPulse.start(getTime(), BuffManager.INTERVAL_PULSE);
		}
		if(timerStatePulse == null) {
			timerStatePulse = new TickTimer();
			timerStatePulse.start(getTime(), 100);
		}
		super.pulse(deltaTime);
		
		// 单元移动了
		pulseMove(timeCurr);

		//周期到了，则更新身上的buff
		if(timerBuffPulse.isPeriod(timeCurr)) {
			BuffManager.inst().pulse(this, timeCurr);
		}
		
		// 施放可以施放的有前摇的技能
		updateSkill(timeCurr);

		// 更新各种状态
		updateState(timeCurr);

	}
	
	/**
	 * 地图单元移动
	 * 
	 * @param timeCurr
	 *            当前时间
	 */
	public void pulseMove(long timeCurr) {
		if (!isInWorld())
			return;
		if (!running.isRunning())
			return;
		// 单元移动了
		running._pulse(timeCurr);
		
	}
	
	/**
	 * 待施放技能队列中取可以施放的技能，放技能
	 */
	public void updateSkill(long curr) {
		SkillExcute skillToExcute = skillTempInfo.skillToExcute;

		// 如果没有前摇技能，则不管
		if (skillToExcute == null)
			return;

		// 前摇时间没有结束
		if (!skillToExcute.tickTimer.isOnce(curr))
			return;

		skillTempInfo.skillToExcute = null;

		SkillCommon skill = null;

		skill = skills.get(skillToExcute.sn);

		// 技能不存在
		if (skill == null) {
			throw new SysException("技能不存在，攻击者id={},技能Sn={}", id, skillToExcute.sn);
		}

		this.removeState(UnitObjectStateKey.skill_shake);
				
		skill.castSecond(skillToExcute.tarPos);
		
	}

	/**
	 * 强制单元停止
	 * 
	 */
	public void stop() {
		if (!running.isRunning())
			return;

		//强制同步移动
		this.running._pulse(this.getTime(), true);
		
		// 停止移动
		running._stop();

		// 发送消息
		SCStageMoveStop.Builder msgStop = SCStageMoveStop.newBuilder();
		msgStop.setObjId(id);
		msgStop.setPosEnd(posNow.toMsg());
		StageManager.inst().sendMsgToArea(msgStop, stageObj, posNow);
	}
	
	/**
	 * 地图单元移动
	 * 
	 * @param posFrom
	 * @param posTo
	 */
	public void move(Vector3D posFrom, List<Vector3D> posTo, Vector3D dir, boolean fromClient) {
		if (!isInWorld())
			return;
		if (!canMove)
			return;
		
		
		// 移动过于频繁，忽略这次消息
		//if (fromClient && !running.isTimeExpired()) {
			//return;
		//}
//		if(posFrom.z == 0) {
//			posFrom.z = 0;
//		}
		// 修正起点
//		if (isHumanObj()) {
//			posFrom.set(running.correctPosFrom(posFrom));
//		}

		
		// 修正所有点，如果连续两个点相同，则移出后一个
		Vector3D pos = new Vector3D();
		pos.set(posFrom);
		Iterator<Vector3D> it = posTo.iterator();
		while (it.hasNext()) {
			Vector3D posNext = it.next();
			if (pos.distance(posNext) < 0.01) {
				it.remove();
				continue;
			}
			pos.set(posNext);
		}

		// 目标点为空
		if (posTo.isEmpty())
			return;

		if (this.isHumanObj()) {
			Event.fire(EventKey.HUMAN_MOVE_START_BEFORE, "humanObj", this);
			Event.fire(EventKey.HUMAN_ACTS_BEFORE, "humanObj", this);
		}
		
		// 移动
		running._move(posFrom, posTo, 1.0D * getUnit().getSpeed() / 100);



		// 发送消息给前端
		Msg.SCStageMove.Builder move = Msg.SCStageMove.newBuilder();
		move.setObjId(id);
		move.setPosBegin(posFrom.toMsg());
		move.addAllPosEnd(running.getRunPathMsg());
		move.setDir(dir.toMsg());
		StageManager.inst().sendMsgToArea(move, stageObj, posNow);

		
		// 抛出开始移动的事件
		Event.fire(EventKey.UNIT_MOVE_START, "unitObj", this);
		Event.fire(EventKey.UNIT_ACT, "unitObj", this);
		if (this.isHumanObj()) {
			Event.fire(EventKey.HUMAN_MOVE_START, "humanObj", this);
			Event.fire(EventKey.HUMAN_ACT, "humanObj", this);
		} else {
			Event.fire(EventKey.MONSTER_MOVE_START, "monsterObj", this);
			Event.fire(EventKey.MONSTER_ACT, "monsterObj", this);
		}

		// 记录日志
		if (this.isHumanObj()) {
			if (Log.stageMove.isInfoEnabled()) {
//				Log.stageMove.info("角色({} {})开始移动，起始位置{}，接下来的目标为{}。",
//						this.name, this.id,posFrom.getPosStr(),
//						running.getRunPathMsg());
			}
		} else {
			if (Log.stageMove.isInfoEnabled()) {
//				Log.stageMove.info("地图单元({})开始移动，起始位置{}，接下来的目标为{}。", this.name,
//						posFrom.getPosStr(), running.getRunPathMsg());
			}
		}

	}


	public void die(UnitObject killer, Param params) {
		Unit unit = getUnit();
		unit.setHpCur(0);
		
		// 设置状态
		inWorld = false;

		// 停止移动
		stop();

		Event.fireEx(EventKey.UNIT_BE_KILLED, stageObj.sn, "killer", killer, "dead", this , "skillSn", params.get("skillSn"));
		
		Param param = new Param(params);

		long killerId = 0;
		String killerName = "";
		int skillSn = Utils.getParamValue(param, "skillSn", 0);
		if (killer != null) {
			killerId = killer.id;
			killerName = killer.name;
		}

		// 通知其他玩家 有地图单元离开视野
		SCStageObjectDisappear.Builder msg = createMsgDie();
		msg.setKillerId(killerId);
		msg.setKillerName(killerName);
		msg.setSkillSn(skillSn);

		StageManager.inst().sendMsgToArea(msg, stageObj, posNow);
		
	}


	/**
	 * 让unitObj进入某种状态，time为持续时间
	 * 
	 * @param stateKey
	 * @param time
	 */
	public void toState(UnitObjectStateKey stateKey, long time) {
//		Log.temp.info("toState :{} {}", stateKey.toString(), time);
		long curr = getTime();
		if (state.get(stateKey) != null) {
			TickTimer timer = state.get(stateKey);
			long timeLeft = timer.getTimeLeft(curr);
			timer.start(getTime(), Math.max(time, timeLeft));
		} else {
			TickTimer timer = new TickTimer();
			timer.start(getTime(), time);
			state.put(stateKey, timer);
		}
		
		updateState(curr, true);
	}

	public void removeState(UnitObjectStateKey stateKey) {
		if(state.containsKey(stateKey)) {
			this.state.remove(stateKey);
		}
		//强制根据状态刷新信息
		updateState(getTime(), true);
	}
	/**
	 * 更新unitObj的状态
	 */
	public void updateState(long curr){
		updateState(curr, false);
	}
	
	public void extendCastSkillingTime() {
		if(state.containsKey(UnitObjectStateKey.stun) || 
				state.containsKey(UnitObjectStateKey.skill_sheep) ||
				state.containsKey(UnitObjectStateKey.skill_hypnosis)) {
			
			if(state.containsKey(UnitObjectStateKey.cast_skilling)) {
				state.get(UnitObjectStateKey.cast_skilling).extendTimeNext(this.deltaTime);
			}
		}
			 
	}
	//force 是否强制更加状态刷新unit属性
	public void updateState(long curr, boolean force) {
		if (!force && !timerStatePulse.isPeriod(curr))
			return;
		// 如果死亡，则把所有限制状态移出
		if (isDie()) {
			state.clear();
		}

		List<UnitObjectStateKey> removeList = new ArrayList<>();
		boolean canMove = true;
		boolean canCastSkill = true;
		boolean canAttack = true;
		boolean castSkilling = false;
		
		extendCastSkillingTime();
		
		for (Entry<UnitObjectStateKey, TickTimer> entry : state.entrySet()) {
			UnitObjectStateKey state = entry.getKey();
			TickTimer timer = entry.getValue();
			// 根据不同的状态进行不同的处理，同时如果时间到了则解除状态
			switch (state) {
			case cast_skilling:	//正在释放技能
				if (timer.isOnce(curr)) {
					removeList.add(state);
				} else {
					castSkilling = true;
					canCastSkill = false;
				}
				break;
			case skillback:		//技能击退
				if (timer.isOnce(curr)) {
					removeList.add(state);
				} else {
					canMove = false;
					canCastSkill = false;
					canAttack = false;
				}
				break;
			case stun:		//眩晕
				if (timer.isOnce(curr)) {
					removeList.add(state);
				} else {
					canMove = false;
					canCastSkill = false;
					canAttack = false;
				}
				break;
			case immobilize:		//冻结
				if (timer.isOnce(curr)) {
					removeList.add(state);
				} else {
					canMove = false;
				}
				break;
			case silence:		//沉默
				if (timer.isOnce(curr)) {
					removeList.add(state);
				} else {
					canCastSkill = false;
				}
				break;
			case skill_shake:		//施法前摇
				if (timer.isOnce(curr)) {
//					Log.temp.info("skill_shake start:{}", Port.getTime());
					removeList.add(state);
				} else {
//					canMove = false;
					canCastSkill = false;
					canAttack = false;
				}
				break;
			case skill_hypnosis:		//催眠
				if (timer.isOnce(curr)) {
					removeList.add(state);
				} else {
					canMove = false;
					canCastSkill = false;
					canAttack = false;
				}
				break;
			case skill_sheep:			//变羊
				if (timer.isOnce(curr)) {
					removeList.add(state);
				} else {
					canMove = false;
					canCastSkill = false;
					canAttack = false;
				}
				break;	
			case cast_locked:			//主动
				if (timer.isOnce(curr)) {
					removeList.add(state);
				} else {
					canMove = false;
				}
				break;	
			default:
				break;
			}
		}
		
		// 如果不能移动，直接停下来先
		if (!canMove) {
			stop();
		}
		
		boolean sendMsg =false;
		// 发送状态变化消息
		if (!isDie() && (this.canMove != canMove | this.canCastSkill != canCastSkill | this.canAttack != canAttack)) {
			if (this.isHumanObj()) {
				HumanInfoChange.listen((HumanObject) this);
			} 
			sendMsg = true;
		}

		this.canMove = canMove;
		this.canCastSkill = canCastSkill;
		this.canAttack = canAttack;
		this.castSkilling = castSkilling;
		// 移走记录
		for (UnitObjectStateKey state : removeList) {
			this.state.remove(state);
		}
		
		if(sendMsg) {
			SCUnitobjStatusChange.Builder StatChange = SCUnitobjStatusChange.newBuilder();
			long type = 0;
			for (UnitObjectStateKey stat : this.state.keySet()) {
				type = type | (1 << stat.getType());
			}
			StatChange.setType(type);
			StatChange.setId(id);
			StatChange.setTeamBundleID(teamBundleID);
			StatChange.setCanMove(this.canMove);
			StatChange.setCanCastSkill(this.canCastSkill);
			StatChange.setCanAttack(this.canAttack);
			StageManager.inst().sendMsgToArea(StatChange, stageObj, posNow);
		}
	}
	
	//取消当前正在施放的技能
	public void cancelCurrSkill(){
		//取消当前技能
		skillTempInfo.skillToExcute = null;
		
		removeState(UnitObjectStateKey.skill_shake);
	}
	
	/**
	 * 技能打断
	 */
	public void interruptCurrSkill(UnitObject uintObjInterrupt){
		if(skillTempInfo.skillToExcute == null){
			return;
		}
		
		ConfSkill confTemp = ConfSkill.get(skillTempInfo.skillToExcute.sn);
		if(confTemp != null && confTemp.canInterrupted) {
			//计算前摇百分比
			SkillParamVO vo = new SkillParamVO();
			
			Event.fire(EventKey.SKILL_PASSIVE_CHECK, 
					"unitAtk", uintObjInterrupt, 
					"unitDef", this, 
					"skillEventKey", SkillEventKey.EVENT_ON_SKILL_INTERRUPT, 
					"vo", vo);
			
			skillTempInfo.skillToExcute = null;
			
			SCSkillInterrupt.Builder msg = SCSkillInterrupt.newBuilder();
			msg.setSkillSn(confTemp.sn);
			msg.setUnitObjID(id);
			StageManager.inst().sendMsgToArea(msg, stageObj, posNow);
		}
	}
	
	public boolean isDie() {
		return getUnit().getHpCur() <= 0;
	}
	
	public AI getAI() {
		return null;
	}
	
	public Unit getUnit() {
		return dataPers.unit;
	}
	
	public Map<Integer, Buff> getBuffs() {
		return dataPers.buffs;
	}

	public PropCalcCommon getPropPlus() {
		UnitPropPlusMap humanPropPlus = dataPers.unitPropPlus;
		
		PropCalcCommon data = new PropCalcCommon();
		//遍历加成属性来累加数据
		for(EntityUnitPropPlus k : EntityUnitPropPlus.values()) {
			data.plus(humanPropPlus.dataMap.get(k.name()));
		}
		
		return data;
	}

	public String getSkills() {
		return dataPers.unit.getSkill();
	}
	
	public String getInborns() {
		return dataPers.unit.getInborn();
	}
	
   public double nextDouble() {
       if(this.stageObj == null) {
    	   Log.temp.info("Error nextDouble stageObj == null");
    	   return 0;
       }
       return this.stageObj.randUtils.nextDouble();
    }

    public int nextInt(int range) {
        if(this.stageObj == null) {
     	   Log.temp.info("Error nextInt stageObj == null");
     	   return 0;
        }
        return this.stageObj.randUtils.nextInt(range);
    }
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		out.write(teamBundleID);
		out.write(order);
		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		teamBundleID = in.read();
		order = in.read();
	}
	
	
	public SCStageObjectInfoChange.Builder createMsgUpdate() {
		SCStageObjectInfoChange.Builder infoChange = SCStageObjectInfoChange.newBuilder();
		infoChange.setObj(createMsg());
		
		return infoChange;
	}
	
	public SCStageObjectDisappear.Builder createMsgDie() {
		SCStageObjectDisappear.Builder msgObjDisappear = SCStageObjectDisappear
				.newBuilder();
		msgObjDisappear.setObjId(id);
		msgObjDisappear.setType(2);

		return msgObjDisappear;
	}
	
	public void setCompeteAI(UnitObject targetObj){
		this.ai = new HumanCompeteAI(this, targetObj);
	}

	
}