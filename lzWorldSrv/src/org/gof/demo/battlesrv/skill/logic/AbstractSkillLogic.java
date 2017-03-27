package org.gof.demo.battlesrv.skill.logic;

import java.util.ArrayList;
import java.util.List;

import org.gof.demo.battlesrv.manager.FightManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEffectVO;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.UnitObjectStateKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.msg.Msg.DBackPos;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public abstract class AbstractSkillLogic {
	public SkillCommon skill; // 所属技能
	public SkillEffectVO conf; // 技能效果

	/**
	 * 初始化技能效果对象
	 * 
	 * @param params
	 */
	public void init(SkillCommon skillCommon, ConfSkillEffect confSkillEffect) {
		skill = skillCommon;

		this.conf = new SkillEffectVO(confSkillEffect);
		
	}

	/**
	 * 检查参数是否有问题
	 * 
	 * @param position
	 * @return
	 */
	public ReasonResult isLegal(SkillParam position) {

		return new ReasonResult(true, "");
	}

	/**
	 * 获取作用目标集合
	 * 
	 * @return
	 */
	public List<UnitObject> getTars(SkillParam position) {
		
		List<UnitObject> tars = new ArrayList<>();

		tars = SkillManager.inst().getTars(conf, skill.unitObj, position.tarUo, position.tarPos);

		return tars;
	}

	/**
	 * 是否可以释放
	 * 
	 * @param position
	 * @return
	 */
	public ReasonResult canDoSkillEffect(SkillParam position) {
		// 施法者死亡
		if (skill.unitObj.isDie()) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.die"));
		}

		// 目标死亡1
		if (position.tarUo != null && position.tarUo.isDie()) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.tarDie"));
		}

		return new ReasonResult(true);
	}

	/**
	 * 对所有目标施放技能效果
	 * 
	 * @param unitAtk
	 * @param position
	 */
	public void doSkillEffect(SkillParam position) {
		
		// 对技能作用范围内的有效目标进行技能作用
		List<UnitObject> tars = getTars(position);
		float backDis = conf.effectDis;
		if(conf.attackMove) {
			skill.dVec = SkillManager.inst().getDBackPosFire(skill.unitObj, position.tarPos, backDis);
		}
		
		for (UnitObject unitDef : tars) {
			if(unitDef == null) continue;
			// 对单个目标进行技能作用, 先判断是否命中
			boolean isHit = FightManager.inst().isHit(skill.unitObj, unitDef);
			if(isHit) {
				doSkillEffectToTar(unitDef);
			}
			
			//如果miss，增加到队列里
			if(!isHit) {
				skill.scFightSkill.addMissId(unitDef.id);
			}

			skill.scFightSkill.addDefId(unitDef.id);
			//如果攻击防守的整容一样 那么防守不后退
			if(skill.unitObj.teamBundleID == unitDef.teamBundleID) {
				backDis = 0;
			}
			
			//如果有伤害计算位移
			if(isHit) {
				DBackPos.Builder backPosMsg = SkillManager.inst().setDBackPosMons(conf.attackMove, skill.unitObj, unitDef, position.tarPos, backDis);
				if(backPosMsg != null) {
					skill.scFightSkill.addBackPos(backPosMsg);
				}
			}
			
			//如果是被攻击者是怪物，并且命中，那么发广播开始判断是否有剧情激活
			if(isHit && unitDef.isMonsterObj()){
				
				//发触发器广播
				Event.fire(EventKey.SCENE_TRIGGER_02, "stageObj", unitDef.stageObj, "monsterObj", unitDef);
			}
			
			// 判断技能结束前是否有被动技能触发（攻击结束后触发buff或者受攻击结束后触发buff）
			SkillParamVO vo = new SkillParamVO();
			Event.fire(EventKey.SKILL_PASSIVE_CHECK, "unitAtk", skill.unitObj, "unitDef", unitDef, "skillEventKey", SkillEventKey.EVENT_ON_TAR_EFFECT_END, "vo", vo);
            
			//如果攻击的目标不是人，那么加入到仇恨值列表
			if (!unitDef.isHumanObj() ){
            	if(unitDef.aggroList.get(skill.unitObj.id) == null){
            		unitDef.aggroList.put(skill.unitObj.id, 2);
            		
            	}else{
            		int key = unitDef.aggroList.get(skill.unitObj.id);
            		unitDef.aggroList.put(skill.unitObj.id, key+1);
            	}
			}
		}
		
	}
	
	/**
	 * //计算击退 击倒 这个函数有点长
	 * @param unitDef
	 * @param tars
	 */
	public void calcBackDis(List<UnitObject> tars, SkillParam position) {
		if (conf.impulse <= 0) {
			return;
		}
		
		//计算技能产生伤害目标的整体质量---------------------------------
		List<UnitObject> downTars = new ArrayList<UnitObject>();
		List<UnitObject> backTars = new ArrayList<UnitObject>();
		double massTotal = 0;
		for (UnitObject obj : tars) {
			//如果攻击者人玩家 防守者是怪物
			if (skill.unitObj.isHumanObj()) {

				double rand = skill.unitObj.nextInt(100) / 100f;
				
				//计算会产生击倒的同学
				double prop = 1 - obj.confModel.backDownProp;
				// 判断怪物是否被击倒
				if (rand > prop) {
					downTars.add(obj);
				} else {
					//计算会产生击退的同学
					prop = prop - obj.confModel.backProp;
					// 判断怪物是否被击退
					if (rand > prop) {
						backTars.add(obj);
					}
				}
				
			}
		}
		
		if(downTars.size() == 0 && backTars.size() == 0) {
			return;
		}
		
		//根据冲量计算位移--------------------------------------
		for (UnitObject unitObject : downTars) {
			massTotal += unitObject.confModel.mass;
		}
		for (UnitObject unitObject : backTars) {
			massTotal += unitObject.confModel.mass;
		}
		
		if(massTotal == 0) {
			return;
		}
		
		double dis = 0;
		//计算位移
		dis = conf.impulse / massTotal;
		
		//计算更新所有被攻击者的位置------------------------------------
		Vector2D backPos;
		for (UnitObject unitObject : downTars) {
			backPos = Vector2D.lookAtDis(skill.unitObj.posNow, unitObject.posNow, unitObject.posNow, dis);
			backPos = StageManager.getRaycastDis(skill.unitObj.stageObj.sn, unitObject.posNow, backPos, skill.unitObj.stageObj.pathFindingFlag);
			//发送消息
			DBackPos.Builder backPosMsg = DBackPos.newBuilder();
			backPosMsg.setId(unitObject.id);
			backPosMsg.setPos(backPos.toMsg());
			backPosMsg.setType(2);
			skill.scFightSkill.addBackPos(backPosMsg);
			//更新怪物的位置
			unitObject.posNow = backPos;
			//更新状态
			unitObject.toState(UnitObjectStateKey.stun, unitObject.confModel.backDownTime);
		}
		for (UnitObject unitObject : backTars) {
			backPos = Vector2D.lookAtDis(skill.unitObj.posNow, unitObject.posNow, unitObject.posNow ,dis);
			backPos = StageManager.getRaycastDis(skill.unitObj.stageObj.sn, unitObject.posNow, backPos, skill.unitObj.stageObj.pathFindingFlag);
			//发送消息
			DBackPos.Builder backPosMsg = DBackPos.newBuilder();
			backPosMsg.setId(unitObject.id);
			backPosMsg.setPos(backPos.toMsg());
			backPosMsg.setType(2);
			skill.scFightSkill.addBackPos(backPosMsg);
			//更新怪物的位置
			unitObject.posNow = backPos;
			//更新状态
			unitObject.toState(UnitObjectStateKey.stun, unitObject.confModel.backTime);
		}
		
		//计算人物跟进 -----------------------------
		//这个技能是否需要跟进， 找出自己的目标跟进
		if(skill.unitObj.isHumanObj() && conf.attackMove) {
			
			if(position.tarUo != null) {
				//如果目标存在 那么跟进目标	
				if(skill.unitObj.posNow.distance(position.tarUo.posNow) > dis + 0.5) {
					skill.dVec = Vector2D.lookAtDis(skill.unitObj.posNow, position.tarUo.posNow, skill.unitObj.posNow, dis);
					skill.dVec = StageManager.getRaycastDis(skill.unitObj.stageObj.sn, skill.unitObj.posNow, skill.dVec, skill.unitObj.stageObj.pathFindingFlag);
					skill.unitObj.posNow = skill.dVec;
				}
			} else if(position.tarUo == null && position.tarPos != null ){
				//如果位置存在那么跟进位置
				if(skill.unitObj.posNow.distance(position.tarPos) > dis + 0.5) {
					skill.dVec = Vector2D.lookAtDis(skill.unitObj.posNow, position.tarPos, skill.unitObj.posNow, dis);
					skill.dVec = StageManager.getRaycastDis(skill.unitObj.stageObj.sn, skill.unitObj.posNow, skill.dVec, skill.unitObj.stageObj.pathFindingFlag);
					skill.unitObj.posNow = skill.dVec;
				}
			}
		}
	}
	
	
	public void calcBackDis(UnitObject obj) {
		// 判断技能技能是否需要判断冲量
		if (conf.impulse <= 0) {
			return;
		}
		//如果攻击者人玩家 防守者是怪物
		if (skill.unitObj.isHumanObj()) {

			double rand = skill.unitObj.nextInt(100) / 100f;
			double prop = 1 - obj.confModel.backDownProp;
			Vector2D backPos;
			// 判断怪物是否被击倒
			if (rand > prop) {
				//计算位置
				backPos = calcImpulse(obj);
				
				//发送消息
				DBackPos.Builder backPosMsg = DBackPos.newBuilder();
				backPosMsg.setId(obj.id);
				backPosMsg.setPos(backPos.toMsg());
				backPosMsg.setType(2);
				skill.scFightSkill.addBackPos(backPosMsg);
				//更新怪物的位置
				obj.posNow = backPos;
				//更新状态
				obj.toState(UnitObjectStateKey.stun, obj.confModel.backDownTime);
				
				return;
			}
			
			// 判断怪物是否被击退
			prop = prop - obj.confModel.backProp;
			if (rand > prop) {
				//计算位置
				backPos = calcImpulse(obj);
				
				//发送消息
				DBackPos.Builder backPosMsg = DBackPos.newBuilder();
				backPosMsg.setId(obj.id);
				backPosMsg.setPos(backPos.toMsg());
				backPosMsg.setType(2);
				skill.scFightSkill.addBackPos(backPosMsg);
				//更新怪物的位置
				obj.posNow = backPos;
				//更新状态
				obj.toState(UnitObjectStateKey.stun, obj.confModel.backTime);
				return;
			}
			
		}
	}
	
	/**
	 * 计算冲量
	 * @param obj
	 * @return
	 */
	public Vector2D calcImpulse(UnitObject obj) {
		Vector2D result;
		double dis = 0;
		
		//计算位移
		dis = conf.impulse / obj.confModel.mass;
		
		result = Vector2D.lookAtDis(skill.unitObj.posNow, obj.posNow, obj.posNow, dis);
		//计算具体的点
		return result;
	}

	/**
	 * 对单个目标作用效果
	 * 
	 * @param unitAtk
	 * @param unitDef
	 */
	public abstract void doSkillEffectToTar(UnitObject unitDef);

	/**
	 * 计算伤害,不同的逻辑库会覆盖
	 * 
	 * @return
	 */
	public int calcHurt(UnitObject unitDef) {
		return 0;
	}

	public int calcHpLost(UnitObject unitDef, boolean calcCrit) {
		int hurt = calcHurt(unitDef);


		return hurt;
	}

}
