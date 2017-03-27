package org.gof.demo.worldsrv.character;


import java.util.List;

import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.arpg.AIRpg;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.worldsrv.config.ConfCharacterModel;
import org.gof.demo.worldsrv.config.ConfCharacterMonster;
import org.gof.demo.worldsrv.config.ConfPropCalc;
import org.gof.demo.worldsrv.config.ConfPropFactor;
import org.gof.demo.worldsrv.entity.Monster;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.DStageMonster;
import org.gof.demo.worldsrv.msg.Msg.DStageObject;
import org.gof.demo.worldsrv.msg.Msg.EWorldObjectType;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectAppear;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;


/**
 * 怪物
 */
public class MonsterObject extends CharacterObject {
	
	public ConfCharacterMonster conf;			//基本配置
	public int stageObjectSn;	//在副本内的SN
	
	public MonsterObject(StageObject stageObj, int stageObjectSn, String sn, boolean active, int index, int level) {
		super(stageObj);
		
		this.id = Port.applyId();
		this.sn = sn;
		this.stageObjectSn = stageObjectSn;
		this.conf = ConfCharacterMonster.get(sn);
		if(this.conf == null) {
			//TODO 在玩家4级之前，箭塔有可能就是空的，不能报错。
			return;
		}
		this.modelSn = this.conf.modelSn;
		this.confModel = ConfCharacterModel.get(this.conf.modelSn);
		this.name = conf.name;
		this.index = index;
		this.order = conf.profession * 1000 + 500;  //这个数字要比 武将的大
		//设置monster的配置信息
		if(conf == null) {
			Log.monster.error("地图{}上怪物{}配置不存在", stageObj.sn, sn);
			return;
		}
		
		//初始化数据
		dataPers.unit = new Monster();
		dataPers.unit.setId(id);
		this.getUnit().setHpCur(100);
		this.getUnit().setHpMax(100);
		this.getUnit().setMpCur(100);
		this.getUnit().setMpMax(100);
		this.getUnit().setModelSn(this.modelSn);
		this.getUnit().setSn(this.sn);
		this.getUnit().setProfession(conf.profession);
		
		//获得 propBase propFactor
		ConfPropCalc propBase = ConfPropCalc.get(conf.level);
		this.getUnit().setLevel(conf.level);
		if(level > 0) {
			propBase = ConfPropCalc.get(level);
			this.getUnit().setLevel(level);
		}
		ConfPropFactor propFactor = ConfPropFactor.get(conf.propFactor);
		PropCalc basePropCalc = new PropCalc(Utils.toJOSNString(propBase.propName, propBase.propValue));
		basePropCalc.mul(propFactor.propName, propFactor.propValue);
		dataPers.unitPropPlus.setBase(basePropCalc.toJSONStr());
		
		this.getUnit().setSkill(SkillManager.inst().firstInitSkills(conf.skillGroupSn));
		this.getUnit().setSkillGroupSn(conf.skillGroupSn);
		
		//初始化技能
		SkillManager.inst().initSkill(this);
		
		UnitManager.inst().propCalc(this, true);
	}
	
	@Override
	public void startup() {
		super.startup();
		this.stageEnter(stageObj);
	}
	
	@Override
	public AI getAI() {
		return ai;
	}
	@Override
	public DStageObject.Builder createMsg() {
		//monster特有信息
		DStageMonster.Builder dMonster = DStageMonster.newBuilder();
		dMonster.addAllPosEnd(running.getRunPathMsg());
		dMonster.setLevel(getUnit().getLevel());
		dMonster.setHpCur(getUnit().getHpCur());
		dMonster.setHpMax(getUnit().getHpMax());
		dMonster.setCollisionRadius((int)(confModel.collisionRadius));
		dMonster.setSpeed(getUnit().getSpeed());
		dMonster.setTeamBundleID(teamBundleID);
		dMonster.setIndex(index);
		dMonster.setCanAttack(canAttack);
		dMonster.setCanCastSkill(canCastSkill);
		dMonster.setCanMove(canMove);
		dMonster.setPropJson(getPropPlus().toJSONStr());
		dMonster.setCombat(getUnit().getCombat());
		dMonster.setSn(getUnit().getSn());
		
		List<DSkill> skills = SkillManager.inst().getSkills(this);
		dMonster.addAllSkill(skills);
		
		//共同信息
		DStageObject.Builder dObj = DStageObject.newBuilder();
		dObj.setObjId(id);
		dObj.setType(EWorldObjectType.MONSTER);
		dObj.setPos(posNow.toMsg());
		dObj.setDir(dirNow.toMsg());
		dObj.setName(name);
		dObj.setModelSn(modelSn);
		dObj.setMonster(dMonster);
		
		return dObj;
	}
	
	public SCStageObjectAppear.Builder createMsgBorn() {
		SCStageObjectAppear.Builder msgBorn = SCStageObjectAppear.newBuilder();
		msgBorn.setObjAppear(createMsg());
		msgBorn.setType(2);
		
		return msgBorn;
	}
		
	/**
	 * 怪物出现
	 */
	@Override
	public void stageShow() {
		// 已在地图中的 忽略
		if(inWorld) {
			Log.stageCommon.warn("使活动单元进入地图时发现inWorld状态为true：data={}", this);
			return;
		}

		// 设置状态为在地图中
		inWorld = true;

		// 日志
		if(Log.stageCommon.isInfoEnabled()) {
			Log.stageCommon.info("地图单位进入地图: stageId={}, objId={}, objName={}", stageObj.id, id, name);
		}

		// 通知其他玩家 有地图单元进入视野
		StageManager.inst().sendMsgToArea(createMsgBorn(), stageObj, posNow);
		
		//加入怪物AI
		if(this.ai == null) {
			this.ai = new AIRpg(this, this.conf.ai);
			//抛出怪物出生事件
			Event.fire(EventKey.MONSTER_BORN, "monsterObj", this);
		}
		
	}
	
	
	/**
	 * 怪物死亡，根据怪物配置来确定怪物是否要删除
	 */
	@Override
	public void die(UnitObject killer, Param params) {
		Event.fireEx(EventKey.MONSTER_BE_KILLED_BEFORE, stageObj.sn, "killer", killer, "dead", this);
		super.die(killer, params);
		stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "worldObj", this) {
			public void execute(Port port) {
//				StageObject stageObj = param.get("stageObj");
//				WorldObject worldObj = param.get("worldObj");
				
//				stageObj._delWorldObj(worldObj);
			}
		});
		Event.fireEx(EventKey.MONSTER_BE_KILLED, stageObj.sn, "killer", killer, "dead", this);
		
		//如果是副本把自己加入死亡队列
		if(stageObj.conf.type.equals(StageMapTypeKey.rep.getContent())){
			stageObj.monsterDies.add(this.stageObjectSn);
			
			//广播死亡，用于场景触发器：怪物死亡
			Event.fire(EventKey.SCENE_TRIGGER_03, "stageObj", stageObj, "monsterObj", this);
			
			//广播死亡，用于场景触发器：敌对目标剩余数量
			Event.fire(EventKey.SCENE_TRIGGER_05, "stageObj", stageObj, "monsterObj", this);
		}
	}
	
	@Override
	public void pulse(int deltaTime) {
		super.pulse(deltaTime);
		
		if(ai != null) ai.pulse(timeCurr);
	}

}
