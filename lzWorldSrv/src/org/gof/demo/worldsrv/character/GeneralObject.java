package org.gof.demo.worldsrv.character;

import java.io.IOException;
import java.util.List;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.arpg.AIGen;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.worldsrv.config.ConfCharacterGeneral;
import org.gof.demo.worldsrv.config.ConfCharacterModel;
import org.gof.demo.worldsrv.entity.General;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.DStageGeneral;
import org.gof.demo.worldsrv.msg.Msg.DStageObject;
import org.gof.demo.worldsrv.msg.Msg.DVector3;
import org.gof.demo.worldsrv.msg.Msg.EWorldObjectType;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectAppear;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class GeneralObject extends CharacterObject {
	public ConfCharacterGeneral conf; //武将配置
	public GeneralObject() {
		super(null);
	}
	
	public GeneralObject(HumanObject unit, General gen) {
		super(null);
		this.id = gen.getId();
		this.sn = gen.getSn();
		this.name = gen.getName();
		this.teamBundleID = unit.id;
		this.modelSn = gen.getModelSn();
		this.dataPers.unit = gen;
		this.parentObject = unit;
		this.confModel = ConfCharacterModel.get(this.modelSn);
	}
	
	public GeneralObject(StageObject stageObj, ConfCharacterGeneral conf) {
		super(stageObj);
		this.id = Port.applyId();
		this.sn = String.valueOf(conf.sn);
		this.modelSn = conf.modelSn;
		this.confModel = ConfCharacterModel.get(conf.modelSn);
		this.name = conf.name;
		
		//初始化数据
		dataPers.unit = new General();
		dataPers.unit.setId(id);
		this.getUnit().setHpCur(100);
		this.getUnit().setHpMax(100);
		this.getUnit().setMpCur(100);
		this.getUnit().setMpMax(100);
		this.getUnit().setModelSn(this.modelSn);
		this.getUnit().setSn(this.sn);
		this.getUnit().setProfession(conf.profession);
		this.getUnit().setName(conf.name);
		this.getUnit().setLevel(conf.level);
		dataPers.unitPropPlus.setBase(Utils.toJOSNString(conf.properties, conf.value));
		this.getUnit().setSkill(SkillManager.inst().firstInitSkills(conf.skillGroupSn));
		this.getUnit().setSkillGroupSn(conf.skillGroupSn);
		
		//初始化技能
		SkillManager.inst().initSkill(this);
		
//		UnitManager.inst().propCalc(this, true);

	}
	
	@Override
	public void startup() {
		super.startup();
		
		//进入场景
		if(this.parentObject != null) {
			this.stageEnter(this.parentObject.stageObj);
		} else if(stageObj != null) {
			this.stageEnter(stageObj);
		}
		
	}
	
	public General getGeneral() {
		return (General)dataPers.unit;
	}
	
	@Override
	public AI getAI() {
		return ai;
	}
	
	@Override
	public DStageObject.Builder createMsg() {
		General general = getGeneral();
		
		//移动中的目标路径
		List<DVector3> runPath = running.getRunPathMsg();
		
		//玩家信息单元
		DStageGeneral.Builder h = DStageGeneral.newBuilder();
//		DStageHuman.Builder h = DStageHuman.newBuilder();
		h.addAllPosEnd(runPath);
		h.setLevel(general.getLevel());
		h.setHpCur(general.getHpCur());
		h.setHpMax(general.getHpMax());
		h.setMpCur(general.getMpCur());
		h.setMpMax(general.getMpMax());
		h.setSex(general.getSex());
		h.setSpeed(general.getSpeed());
		h.setProfession(general.getProfession());
		h.setPvpMode(general.getPvpMode());
		h.setInFighting(general.isInFighting());
		h.setTeamBundleID(teamBundleID);
		h.setCanAttack(canAttack);
		h.setCanCastSkill(canCastSkill);
		h.setCanMove(canMove);
		h.setPropJson(getPropPlus().toJSONStr());
		h.setSn(general.getSn());
		
		List<DSkill> skills = SkillManager.inst().getSkills(this);
		h.addAllSkill(skills);
		
		DStageObject.Builder objInfo = DStageObject.newBuilder();
		objInfo.setObjId(id);
		objInfo.setType(EWorldObjectType.GENERAL);
		objInfo.setName(name);
		objInfo.setModelSn(general.getModelSn());
		objInfo.setPos(posNow.toMsg());
		objInfo.setDir(dirNow.toMsg());
		objInfo.setGeneral(h);
		
		return objInfo;
	}
	
	public SCStageObjectAppear.Builder createMsgBorn() {
		SCStageObjectAppear.Builder msgBorn = SCStageObjectAppear.newBuilder();
		msgBorn.setObjAppear(createMsg());
		msgBorn.setType(2);
		
		return msgBorn;
	}
		
	/**
	 * 武将 出现
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
//			Log.stageCommon.info("地图单位进入地图: stageId={}, objId={}, objName={}", stageObj.id, id, name);
		}
		
		// 通知其他玩家 有地图单元进入视野
		StageManager.inst().sendMsgToArea(createMsgBorn(), stageObj, posNow);
		
		//加入AI
		if(this.ai == null) {
			this.conf = ConfCharacterGeneral.get(sn);
			this.confModel = ConfCharacterModel.get(modelSn);
			
			this.ai = new AIGen(this, this.conf.ai);
			//抛出武将出生事件
			Event.fire(EventKey.GENERAL_BORN, "generalObj", this);
		}
	
	}
	
	/**
	 * 死亡，根据怪物配置来确定怪物是否要删除
	 */
	@Override
	public void die(UnitObject killer, Param params) {
		super.die(killer, params);
		stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "worldObj", this) {
			public void execute(Port port) {
				StageObject stageObj = param.get("stageObj");
				WorldObject worldObj = param.get("worldObj");
				
				stageObj._delWorldObj(worldObj);
			}
		});
		
		
	}
	
	@Override
	public void pulse(int deltaTime) {
		super.pulse(deltaTime);
		
		if(ai != null) ai.pulse(timeCurr);
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		
		out.write(dataPers);
		
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		
		dataPers = in.read();
	}

}
