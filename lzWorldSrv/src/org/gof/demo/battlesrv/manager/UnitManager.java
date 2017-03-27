package org.gof.demo.battlesrv.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.bullet.BulletObject;
import org.gof.demo.battlesrv.dot.DotObject;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.battlesrv.support.PropCalcCommon;
import org.gof.demo.battlesrv.support.PropKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfCharacterGrow;
import org.gof.demo.worldsrv.config.ConfCharacterProperty;
import org.gof.demo.worldsrv.config.ConfSkill;
import org.gof.demo.worldsrv.entity.Unit;
import org.gof.demo.worldsrv.msg.Msg.DBackPos;
import org.gof.demo.worldsrv.msg.Msg.DHpChgOnce;
import org.gof.demo.worldsrv.msg.Msg.DHpChgTar;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.SCFightHpChg;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class UnitManager extends ManagerBase {
	
	public double PERCENTDIV = 10000D;
	/**
	 * 获取实例
	 * @return
	 */
	public static UnitManager inst() {
		return inst(UnitManager.class);
	}
	
	/**
	 * 加血
	 * @param unitObjToAdd
	 * @param hpAdd
	 * @param unitObjFire		加血引起者
	 */
	public void addHp(UnitObject unitObjToAdd, HpLostKey hpLostKey, int hpAdd, UnitObject unitObjFire) {
		if(hpAdd <= 0) return ;
		
		//当前剩余血量
		setHpCur(unitObjToAdd, Math.min(getHpMax(unitObjToAdd), getHpCur(unitObjToAdd) + hpAdd));
				
		//将耗血信息耗血的对象血包中
		DHpChgOnce.Builder dhpChgOnce = hpChg(unitObjToAdd, hpLostKey.name(), unitObjFire, hpAdd, 0, false);
		SCFightHpChg.Builder msg = SCFightHpChg.newBuilder();
		
		DHpChgTar.Builder dhpTar = DHpChgTar.newBuilder();
		dhpTar.setId(unitObjToAdd.id);
		dhpTar.addDhpChgOnce(dhpChgOnce);
		
		msg.addDhpChgTar(dhpTar);
		StageManager.inst().sendMsgToArea(msg, unitObjToAdd.stageObj, unitObjToAdd.posNow);
	}
	
	public void sendRageChange(UnitObject unitObjToAdd, HpLostKey hpLostKey, UnitObject unitObjFire) {
		//将耗血信息耗血的对象血包中
		DHpChgOnce.Builder dhpChgOnce = hpChg(unitObjToAdd, hpLostKey.name(), unitObjFire, 0, 0, false);
		SCFightHpChg.Builder msg = SCFightHpChg.newBuilder();
		
		DHpChgTar.Builder dhpTar = DHpChgTar.newBuilder();
		dhpTar.setId(unitObjToAdd.id);
		dhpTar.addDhpChgOnce(dhpChgOnce);
		
		msg.addDhpChgTar(dhpTar);
		StageManager.inst().sendMsgToArea(msg, unitObjToAdd.stageObj, unitObjToAdd.posNow);
	}
	
	/**
	 * 扣血
	 * @param unitObj
	 * @param hpLoss
	 * @param unitObjFire 攻击方
	 */
	public void reduceHp(UnitObject unitObj, int hpLoss, UnitObject unitObjFire, Param param) {
		if(unitObj == null || unitObj.stageObj == null) {
			return;
		}
		
		if(unitObj.isDie()) return ;
		
		//扣血小于0
		if(hpLoss < 0) return ;
		if(param == null) param = new Param();
		
		int skillSn = SkillManager.inst().getSkillSnFromParam(param);
		ConfSkill confskill = ConfSkill.get(skillSn);
		
		HpLostKey hpLostKey = Utils.getParamValue(param, "hpLostKey", HpLostKey.SKILL);
		String hpType = hpLostKey.name();
		
		//是否命中
		boolean isHit = Utils.getParamValue(param, "isHit", true);
		
		//是否暴击
		boolean isCrit = Utils.getParamValue(param, "isCrit", false);
		
		int damegeNum = Utils.getParamValue(param, "damegeNum", 0);
		float damegeAdd = Utils.getParamValue(param, "damegeAdd", 0f);
		float hurtAdd = Utils.getParamValue(param, "hurtAdd", 0f);
		Vector2D dBackVec = Utils.getParamValue(param, "dBackVec", null);
		float backDis = Utils.getParamValue(param, "backDis", 0f);
		
		boolean ishurtAdd = hurtAdd == 0f ? false : true;
		
		boolean isdamegeAdd = damegeAdd == 0f ? false : true;
		
		//正常攻击为0，暴击为1， 闪避为2， 格挡为3  单体连携技 1 << 5; 连击连携技 1 << 6
		int effect = 0;
		if(!isHit) effect = 2;
		if(isCrit) effect = 1;
		if(ishurtAdd) effect = effect | 1 << 5;
		if(isdamegeAdd) effect = effect | 1 << 6;
		
		/** 一、当前血量减血 */
		int realHp = 0;
		//减血
		if(unitObj.skillTempInfo.noDead) {
			realHp = Math.max(1, getHpCur(unitObj) - hpLoss);
		} else {
			realHp = Math.max(0, getHpCur(unitObj) - hpLoss);
		}
		
		setHpCur(unitObj, realHp);
		
		if(isHit){
			//抛出扣血事件
			int stageSn = unitObj.stageObj.sn;
			Event.fireEx(EventKey.UNIT_HPLOSS, stageSn, "unitObj", unitObj, "unitObjFire", unitObjFire);
			if(unitObj.isHumanObj()) {
				Event.fireEx(EventKey.HUMAN_HPLOSS, stageSn, "humanObj", unitObj);
			} else if(unitObj.isMonsterObj()) {
				if(unitObjFire == null) {
					Event.fireEx(EventKey.MONSTER_HPLOSS_BY_NO_HUMAN, stageSn, "monsterObj", unitObj, "hpLost", hpLoss);
				} else {
					Event.fireEx(EventKey.MONSTER_HPLOSS, stageSn, "monsterObj", unitObj, "atker", unitObjFire, "hpLost", hpLoss);
				}
			}
		}
		if(getHpCur(unitObj) <= 0){
			//派发垂死事件，看看有无复活可能
			Event.fire(EventKey.SKILL_PASSIVE_CHECK, 
					"unitAtk", unitObj,
					"unitDef", null, 
					"SkillParam", new SkillParam(),
					"skillEventKey", SkillEventKey.EVENT_ON_UNIT_DYING, 
					"vo", new SkillParamVO());
			
			if(getHpCur(unitObj) <= 0){
				//死亡
				unitObj.die(unitObjFire, new Param("skillSn", skillSn));
			}
			
		}
		float hpPct = hpLoss / (float)unitObj.getUnit().getHpMax();
		
		//将耗血信息加入到需要接受到掉血广播的人中
		DHpChgOnce.Builder dhpChgOnce = hpChg(unitObj, hpType, unitObjFire, hpLoss, effect, true);
		if(confskill != null && backDis != 0) {
			//如果攻击防守的整容一样 那么防守不后退
			if(unitObjFire.teamBundleID == unitObj.teamBundleID) {
				backDis = 0;
			}
			DBackPos.Builder backPosMsg = SkillManager.inst().setDBackPosMons(false, unitObjFire, unitObj, unitObj.posNow, backDis);
			if(backPosMsg != null) {
				dhpChgOnce.setBackPos(backPosMsg);
			}
			if(dBackVec != null) {
				dhpChgOnce.setAttPos(dBackVec.toMsg());
			}
		}
		if(isdamegeAdd) {
			dhpChgOnce.setDamegeNum(damegeNum);
			dhpChgOnce.setDamegeAdd(damegeAdd);
		}
		if(ishurtAdd) {
			dhpChgOnce.setHurtAdd(hurtAdd);
//			Log.temp.info("dhpChgOnce {} {}",unitObj.name, unitObjFire.name);
		}
		
		//技能耗血,将掉血包加到技能包里返回
		if(param.containsKey("skill")) {
			SkillCommon skill = param.get("skill");
			
			skill.addSkillHpChg(unitObj.id, dhpChgOnce);
			
		} else if(param.containsKey("dot")) {
			DotObject dotObj = param.get("dot");
			
			dotObj.addSkillHpChg(unitObj.id, dhpChgOnce);
			
		} else if(param.containsKey("bullet")) {
			BulletObject bulletObj = param.get("bullet");
			
			bulletObj.addSkillHpChg(unitObj.id, dhpChgOnce);
		} else {
			SCFightHpChg.Builder msg = SCFightHpChg.newBuilder();
			
			DHpChgTar.Builder dhpTar = DHpChgTar.newBuilder();
			dhpTar.setId(unitObj.id);
			dhpTar.addDhpChgOnce(dhpChgOnce);
			msg.setSkillSn(skillSn);
			msg.addDhpChgTar(dhpTar);
			StageManager.inst().sendMsgToArea(msg, unitObj.stageObj, unitObj.posNow);
		}
		
		//10%血打断
		if(hpPct >= 0.1) {
			unitObj.interruptCurrSkill(unitObjFire);
		}
				
	}
	
	public DHpChgOnce.Builder hpChg(UnitObject unitObj, String hpType, UnitObject unitObjFire, int hpChange, int effect, boolean hpLost) {
		long attackerId = (unitObjFire == null ? 0 : unitObjFire.id);
		int atkRage = 0;
		int defRage = 0;
	
		Unit unit = unitObj.getUnit();
		DHpChgOnce.Builder dhpChgOnce = DHpChgOnce.newBuilder();
		dhpChgOnce.setHpChange(hpChange);
		dhpChgOnce.setHpType(hpType);
		dhpChgOnce.setHpCur(unit.getHpCur());
		dhpChgOnce.setHpMax(unit.getHpMax());
		dhpChgOnce.setEffect(effect);
		dhpChgOnce.setHpLost(hpLost);
		dhpChgOnce.setAttackerId(attackerId);
		dhpChgOnce.setAtkRage(atkRage);
		dhpChgOnce.setDefRage(defRage);
		dhpChgOnce.setHpType(hpType);
		return dhpChgOnce;
	}
	
	/**
	 * 获取unit当前血量
	 * @param unitObj
	 * @return
	 */
	private int getHpCur(UnitObject unitObj) {
		return unitObj.getUnit().getHpCur();
	}
	
	/**
	 * 设置unit当前血量
	 * @param unitObj
	 * @param hpCur
	 */
	private void setHpCur(UnitObject unitObj, int hpCur) {
		unitObj.getUnit().setHpCur(Math.max(0, hpCur));
	}
	
	/**
	 * 获取unit当前最大血量
	 * @param unitObj
	 * @return
	 */
	private int getHpMax(UnitObject unitObj) {
		return unitObj.getUnit().getHpMax();
	}
	
	@Listener(value=EventKey.UNIT_BE_ATTACKED)
	public void onUnitBeAttacked(Param param) {
		UnitObject unitDef = param.get("unitDef");
		UnitObject unitAtk = param.get("unitAtk");
		int skillSn = param.get("skillSn");
		ConfSkill confSkill = ConfSkill.get(skillSn);
		if(confSkill == null) {
			return;
		}
		
		//技能打断
		SkillManager.inst().interruptCurrSkill(skillSn, unitAtk, unitDef);
		
	}
	
	@Listener(value=EventKey.UNIT_BE_KILLED)
	public void onUnitBeKilled(Param param) {
		UnitObject unitObj = param.get("killer");
		int skillSn = param.get("skillSn");
		ConfSkill confSkill = ConfSkill.get(skillSn);
		if(confSkill == null) {
			return;
		}
	}
	
	public void propCalc(UnitObject unitObj) {
		propCalc(unitObj, false);
	}
	
	/**
	 * 计算当前战斗单位属性
	 * @param unitObj
	 */
	public void propCalc(UnitObject unitObj, boolean init) {
		Unit unit = unitObj.getUnit();

		// 各模块加成属性
		PropCalcCommon propPlus = unitObj.getPropPlus();
		
		// 记录属性计算前的血/魔
		int hpMaxOld = unit.getHpMax();
		int mpMaxOld = unit.getMpMax();

		// 计算人物属性
		int hpMaxPct = propPlus.getInt(PropKey.hpMaxPct);		//生命值上限
		int hpMax = (int) (propPlus.getInt(PropKey.hpMax) * (1.0D + hpMaxPct / PERCENTDIV));
		int mpMaxPct = propPlus.getInt(PropKey.mpMaxPct);		//法力值上限
		int mpMax = (int) (propPlus.getInt(PropKey.mpMax) * (1.0D + mpMaxPct / PERCENTDIV));
		int atkPhyPct = propPlus.getInt(PropKey.atkPhyPct);		//物理攻击
		int atkPhy = (int) (propPlus.getInt(PropKey.atkPhy) * (1.0D + atkPhyPct / PERCENTDIV));
		int defPhyPct = propPlus.getInt(PropKey.defPhyPct);		//物理防御
		int defPhy = (int) (propPlus.getInt(PropKey.defPhy) * (1.0D + defPhyPct / PERCENTDIV));
		int atkMagPct = propPlus.getInt(PropKey.atkMagPct);		//魔法攻击
		int atkMag = (int) (propPlus.getInt(PropKey.atkMag) * (1.0D + atkMagPct / PERCENTDIV));
		int defMagPct = propPlus.getInt(PropKey.defMagPct);		//魔法防御
		int defMag = (int) (propPlus.getInt(PropKey.defMag) * (1.0D + defMagPct / PERCENTDIV));
		int hitPct = propPlus.getInt(PropKey.hitPct);						//命中
		int hit = (int) (propPlus.getInt(PropKey.hit) * (1.0D + hitPct / PERCENTDIV));
		int dodgePct = propPlus.getInt(PropKey.dodgePct);			//闪避
		int dodge = (int) (propPlus.getInt(PropKey.dodge) * (1.0D + dodgePct / PERCENTDIV));
		int critPct = propPlus.getInt(PropKey.critPct);					//暴击
		int crit = (int) (propPlus.getInt(PropKey.crit) * (1.0D + critPct / PERCENTDIV));
		int critAddPct = propPlus.getInt(PropKey.critAddPct);		//暴击加成
		int critAdd = (int) (propPlus.getInt(PropKey.critAdd) * (1.0D + critAddPct / PERCENTDIV));
		int toughPct = propPlus.getInt(PropKey.toughPct);			//坚韧
		int tough = (int) (propPlus.getInt(PropKey.tough) * (1.0D + toughPct / PERCENTDIV));
		int elem1AtkPct = propPlus.getInt(PropKey.elem1AtkPct);//元素1伤害
		int elem1Atk = (int) (propPlus.getInt(PropKey.elem1Atk) * (1.0D + elem1AtkPct / PERCENTDIV));
		int elem1DefPct = propPlus.getInt(PropKey.elem1DefPct);//元素1防御
		int elem1Def = (int) (propPlus.getInt(PropKey.elem1Def) * (1.0D + elem1DefPct / PERCENTDIV));
		int elem2AtkPct = propPlus.getInt(PropKey.elem2AtkPct);//元素2伤害
		int elem2Atk = (int) (propPlus.getInt(PropKey.elem2Atk) * (1.0D + elem2AtkPct / PERCENTDIV));
		int elem2DefPct = propPlus.getInt(PropKey.elem2DefPct);//元素2防御
		int elem2Def = (int) (propPlus.getInt(PropKey.elem2Def) * (1.0D + elem2DefPct / PERCENTDIV));
		int elem3AtkPct = propPlus.getInt(PropKey.elem3AtkPct);//元素3伤害
		int elem3Atk = (int) (propPlus.getInt(PropKey.elem3Atk) * (1.0D + elem3AtkPct / PERCENTDIV));
		int elem3DefPct = propPlus.getInt(PropKey.elem3DefPct);//元素3防御
		int elem3Def = (int) (propPlus.getInt(PropKey.elem3Def) * (1.0D + elem3DefPct / PERCENTDIV));
		int elem4AtkPct = propPlus.getInt(PropKey.elem4AtkPct);//元素4伤害
		int elem4Atk = (int) (propPlus.getInt(PropKey.elem4Atk) * (1.0D + elem4AtkPct / PERCENTDIV));
		int elem4DefPct = propPlus.getInt(PropKey.elem4DefPct);//元素4防御
		int elem4Def = (int) (propPlus.getInt(PropKey.elem4Def) * (1.0D + elem4DefPct / PERCENTDIV));
		int suckPct = propPlus.getInt(PropKey.suckPct);					//吸血概率
		int suck = (int) (propPlus.getInt(PropKey.suck) * (1.0D + suckPct / PERCENTDIV));
		int suckRatioPct = propPlus.getInt(PropKey.suckRatioPct);	//吸血比例
		int suckRatio = (int) (propPlus.getInt(PropKey.suckRatio) * (1.0D + suckRatioPct / PERCENTDIV));
		int avoidAtkPct = propPlus.getInt(PropKey.avoidAtkPct);		//免伤比例
		int avoidAtk = (int) (propPlus.getInt(PropKey.avoidAtk) * (1.0D + avoidAtkPct / PERCENTDIV));
		int skillHealthPct = propPlus.getInt(PropKey.skillHealthPct);//治疗量
		int skillHealth = (int) (propPlus.getInt(PropKey.skillHealth) * (1.0D + skillHealthPct / PERCENTDIV));
		int speedPct = propPlus.getInt(PropKey.speedPct);				//移动速度
		int speed = (int) (propPlus.getInt(PropKey.speed) * (1.0D + speedPct / PERCENTDIV));
		speed =  speed < 0 ? 0 : speed;
		int hpRecovPct = propPlus.getInt(PropKey.hpRecovPct);		//生命恢复比例
		int hpRecov = (int) (propPlus.getInt(PropKey.hpRecov) * (1.0D + hpRecovPct / PERCENTDIV));
		int mpRecovPct = propPlus.getInt(PropKey.mpRecovPct);	//法力恢复比例
		int mpRecov = (int) (propPlus.getInt(PropKey.mpRecov) * (1.0D + mpRecovPct / PERCENTDIV));
		int mpReducePct = propPlus.getInt(PropKey.mpReducePct);//能耗降低
		int mpReduce = (int) (propPlus.getInt(PropKey.mpReduce) * (1.0D + mpReducePct / PERCENTDIV));
		
		//设置新的血量
		unit.setHpMax(hpMax);
		unit.setHpMaxPct(hpMaxPct);
		unit.setMpMax(mpMax);
		unit.setMpMaxPct(mpMaxPct);
		
		//重新计算先用的红和蓝
		double hpNew = unit.getHpCur() * (1.0 * unit.getHpMax() / hpMaxOld);
		double mpNew = unit.getMpCur() * (1.0 * unit.getMpMax() / mpMaxOld);
		unit.setHpCur((int) hpNew);
		unit.setMpCur((int) mpNew);
		
		// 设置上新的属性
		unit.setAtkPhy(atkPhy);
		unit.setAtkMagPct(atkMagPct);
		unit.setDefPhy(defPhy);
		unit.setDefPhyPct(defPhyPct);
		unit.setAtkMag(atkMag);
		unit.setAtkMagPct(atkMagPct);
		unit.setDefMag(defMag);
		unit.setDefMagPct(defMagPct);
		unit.setHit(hit);
		unit.setHitPct(hitPct);
		unit.setDodge(dodge);
		unit.setDodgePct(dodgePct);
		unit.setCrit(crit);
		unit.setCritPct(critPct);
		unit.setCritAdd(critAdd);
		unit.setCritAddPct(critAddPct);
		unit.setTough(tough);
		unit.setToughPct(toughPct);
		unit.setElem1Atk(elem1Atk);
		unit.setElem1AtkPct(elem1AtkPct);
		unit.setElem1Def(elem1Def);
		unit.setElem1DefPct(elem1DefPct);
		unit.setElem2Atk(elem2Atk);
		unit.setElem2AtkPct(elem2AtkPct);
		unit.setElem2Def(elem2Def);
		unit.setElem2DefPct(elem2DefPct);
		unit.setElem3Atk(elem3Atk);
		unit.setElem3AtkPct(elem3AtkPct);
		unit.setElem3Def(elem3Def);
		unit.setElem3DefPct(elem3DefPct);
		unit.setElem4Atk(elem4Atk);
		unit.setElem4AtkPct(elem4AtkPct);
		unit.setElem4Def(elem4Def);
		unit.setElem4DefPct(elem4DefPct);
		unit.setSuck(suck);
		unit.setSuckPct(suckPct);
		unit.setSuckRatio(suckRatio);
		unit.setSuckRatioPct(suckRatioPct);
		unit.setAvoidAtk(avoidAtk);
		unit.setAvoidAtkPct(avoidAtkPct);
		unit.setSkillHealth(skillHealth);
		unit.setSkillHealthPct(skillHealthPct);
		unit.setSpeed(speed);
		unit.setSpeedPct(speedPct);
		unit.setHpRecov(hpRecov);
		unit.setHpRecovPct(hpRecovPct);
		unit.setMpRecov(mpRecov);
		unit.setMpRecovPct(mpReducePct);
		unit.setMpReduce(mpReduce);
		unit.setMpReducePct(mpReducePct);

		// 计算战斗力
		int combat = getCombat(unitObj, unit);
		unit.setCombat(combat);

	}

	/**
	 * 计算人物属性
	 * 
	 * @param unit
	 * @param propKey  属性 仅仅适用于二级属性，非抗性
	 */
	private int propSecCalc(Unit unit, PropKey propKey) {

		// 计算人物二级属性
		for (ConfCharacterProperty p : ConfCharacterProperty.findAll()) {
			if (!propKey.name().equals(p.type))
				continue;

			Map<String, Double> calcRadio = new HashMap<>();
			if (StringUtils.isNotBlank(p.conversionType1)) {
				calcRadio.put(p.conversionType1, p.conversionRatio1);
			}
			if (StringUtils.isNotBlank(p.conversionType2)) {
				calcRadio.put(p.conversionType2, p.conversionRatio2);
			}
			if (StringUtils.isNotBlank(p.conversionType3)) {
				calcRadio.put(p.conversionType3, p.conversionRatio3);
			}
			if (StringUtils.isNotBlank(p.conversionType4)) {
				calcRadio.put(p.conversionType4, p.conversionRatio4);
			}

			double value = 0;
			try {
				for (String prop : calcRadio.keySet()) {
					int propValue = Utils.fieldRead(unit, prop);
					value += propValue * calcRadio.get(prop);
				}
			} catch (Exception e) {
				throw new SysException(e);
			}

			return (int) value;
		}

		return 0;
	}

	/**
	 * 战斗力=属性战斗力+技能效果战斗力
	 * 
	 * @param unit
	 * @return
	 */
	private int getCombat(UnitObject unitObj, Unit unit) {
		//如果或者当前unit的Human
		HumanObject humanObj = unitObj.getHumanObj();
		
		int combatAll = 0;
		
		//如果是单个武将
		if(unitObj.isGeneralObj()) {
			//技能战力
			int combatSkill = calcCombatSkill(unitObj);
			int combatProp = calcCombatProp(unitObj);
			combatAll = combatSkill + combatProp;
			
			//如果是human，就是所有的武将
		} else if (unitObj.isHumanObj()) {
			HumanInfoChange.listen(humanObj);
			
			for (UnitObject uo : humanObj.salvesAttingList) {
				if(uo != null) {
					
					//技能战力
					int combatSkill = calcCombatSkill(uo);
					int combatProp = calcCombatProp(uo);
					
					combatAll += (combatSkill + combatProp);
				}
			}
			humanObj.getHuman().setCombat(combatAll);
		}
		
		return combatAll;
	}
	
	/**
	 * 计算单个武将的属性战斗力
	 * 
	 * 属性战斗力=（
	 * 						物理攻击 + 法术攻击 + 物理防御 + 法术防御
	 *					+ 0.4 * 物理攻击 * （物理暴击/6000 + 物理韧性/（物理韧性+1000））*（1+暴击伤害百分比）
	 *					+ 0.4 * 法术攻击 * （法术暴击/6000 + 法术韧性/（法术韧性+1000））*（1+暴击伤害百分比）
	 *					+ （（怒气回复 + 初始怒气 + 吸怒气值）/1000 + 1 + 能量消耗降低）* 法术攻击
	 *					+ 	0.04 * （最大生命 + 生命回复） *（1 + 技能治疗效果）* （1 + 吸血等级/（吸血等级 + 200）） * 
	 *					   （
	 *							+ 1/（1 - min（闪避值/（闪避值 + 1000），0.8）） 	
	 *							+ 1/（1 - min（命中百分比，0.8））
	 *							+ 1/（1 - min（格挡百分比，0.8））/2
	 *							+ 1/（1 - min（穿透百分比，0.8））/2
	 *							+ 1/（1 - min（物理免伤，0.8））
	 *							+ 1/（1 - min（物理免伤抵抗，0.8））
	 *							+ 1/（1 - min（法术免伤，0.8））
	 *							+ 1/（1 - min（法术免伤抵抗，0.8））
	 *							-  6
	 *					   	）
	 *					+ 最终伤害 + 最终免伤 + 物理防御减 + 法术防御减
	 *					）* 0.6 
	 * @param unitObj
	 * @return
	 */
	private int calcCombatProp(UnitObject unitObj) {
		Unit u = unitObj.getUnit();
		double combatProp =u.getAtkPhy() + u.getAtkMag();
		
		return (int)combatProp;
	}
	
	/**
	 * 计算单个武将的技能战斗力
	 * 
	 * 技能效果战斗力=技能1+技能2+技能3+技能4、、、（配置）
	 * @param unitObj
	 * @return
	 */
	private int calcCombatSkill(UnitObject unitObj) {
		int combatSkill = 0;
		
		List<DSkill> dskillList =SkillManager.inst().getSkills(unitObj);
		for (DSkill dSkill : dskillList) {
			ConfSkill conf = ConfSkill.get(dSkill.getSkillSn());
			
			if(conf == null) continue;
			combatSkill += (conf.combat * dSkill.getSkillLevel()) ;
		}
		
		return combatSkill;
	}
	
	/**
	 * 获取指定职业 指定等级下的玩家属性
	 * 
	 * @param profession
	 * @param level
	 * @return
	 */
	public PropCalc getLevelProp(int profession, int level) {
		// 配置信息
		List<ConfCharacterGrow> confs = ConfCharacterGrow.findBy("profession", profession);

		// 重新计算基础属性
		PropCalc prop = new PropCalc();
		for (ConfCharacterGrow conf : confs) {
			Double value = conf.base * ( 1.0D * (level-1) * conf.factor / 100 ) + (level-1) * conf.plus ;
			prop.plus(PropKey.valueOf(conf.prop), value.intValue());
		}

		return prop;
	}
	
	
	public int getPropKeyValue(PropKey key, Unit unit) {
		int result = 0;
		switch (key) {
			case hpMax:  result = unit.getHpMax(); 						break;
			case hpMaxPct: result = unit.getHpMaxPct(); 				break;
			case mpMax:  result = unit.getMpMax();						break;
			case mpMaxPct: result = unit.getMpMaxPct(); 			break;
			case atkPhy: result = unit.getAtkPhy(); 						break;
			case atkPhyPct:  result = unit.getAtkPhyPct(); 				break;
			case defPhy: result = unit.getDefPhy(); 						break;
			case defPhyPct:  result = unit.getDefPhyPct();				break;
			case atkMag: result = unit.getAtkMag();						break;
			case atkMagPct: result = unit.getAtkMagPct(); 			break;
			case defMag:  result = unit.getDefMag(); 						break;
			case defMagPct: result = unit.getDefMagPct(); 			break;
			case hit:  result = unit.getHit();										break;
			case hitPct: result = unit.getHitPct(); 							break;
			case dodge: result = unit.getDodge(); 							break;
			case dodgePct:  result = unit.getDodgePct(); 				break;
			case crit: result = unit.getCrit(); 									break;
			case critPct:  result = unit.getCritPct();							break;
			case critAdd: result = unit.getCritAdd(); 						break;
			case critAddPct: result = unit.getCritAddPct(); 				break;
			case tough:  result = unit.getTough(); 							break;
			case toughPct: result = unit.getToughPct(); 					break;
			case elem1Atk:  result = unit.getElem1Atk();				break;
			case elem1AtkPct: result = unit.getElem1AtkPct(); 		break;
			case elem1Def: result = unit.getElem1Def(); 				break;
			case elem1DefPct:  result = unit.getElem1DefPct(); 		break;
			case elem2Atk:  result = unit.getElem2Atk();				break;
			case elem2AtkPct: result = unit.getElem2AtkPct(); 		break;
			case elem2Def: result = unit.getElem2Def(); 				break;
			case elem2DefPct:  result = unit.getElem2DefPct(); 		break;
			case elem3Atk:  result = unit.getElem3Atk();				break;
			case elem3AtkPct: result = unit.getElem3AtkPct(); 		break;
			case elem3Def: result = unit.getElem3Def(); 				break;
			case elem3DefPct:  result = unit.getElem3DefPct(); 		break;
			case elem4Atk:  result = unit.getElem4Atk();				break;
			case elem4AtkPct: result = unit.getElem4AtkPct(); 		break;
			case elem4Def: result = unit.getElem4Def(); 				break;
			case elem4DefPct:  result = unit.getElem4DefPct(); 		break;
			case suck: result = unit.getSuck(); 								break;
			case suckPct:  result = unit.getSuckPct();						break;
			case suckRatio: result = unit.getSuckRatio(); 				break;
			case suckRatioPct: result = unit.getSuckRatioPct(); 		break;
			case avoidAtk:  result = unit.getAvoidAtk(); 					break;
			case avoidAtkPct: result = unit.getAvoidAtkPct(); 		break;
			case skillHealth:  result = unit.getSkillHealth();				break;
			case skillHealthPct: result = unit.getSkillHealthPct(); 	break;
			case speed: result = unit.getSpeed(); 							break;
			case speedPct:  result = unit.getSpeedPct(); 				break;
			case hpRecov: result = unit.getHpRecov(); 					break;
			case hpRecovPct:  result = unit.getHpRecovPct();			break;
			case mpRecov: result = unit.getMpRecov(); 					break;
			case mpRecovPct: result = unit.getMpRecovPct(); 		break;
			case mpReduce:  result = unit.getMpReduce(); 			break;
			case mpReducePct: result = unit.getMpReducePct(); 	break;
			
			default: break;
		}
		return result;
	}
	
	public void setPropKeyValue(PropKey key, int value,Unit unit) {
		switch (key) {
			case hpMax:  unit.setHpMax(value); 						break;
			case hpMaxPct: unit.setHpMaxPct(value); 			break;
			case mpMax:  unit.setMpMax(value);					break;
			case mpMaxPct: unit.setMpMaxPct(value); 			break;
			case atkPhy: unit.setAtkPhy(value); 						break;
			case atkPhyPct:  unit.setAtkPhyPct(value); 			break;
			case defPhy: unit.setDefPhy(value); 						break;
			case defPhyPct:  unit.setDefPhyPct(value);			break;
			case atkMag: unit.setAtkMag(value);					break;
			case atkMagPct: unit.setAtkMagPct(value); 			break;
			case defMag:  unit.setDefMag(value); 					break;
			case defMagPct: unit.setDefMagPct(value); 			break;
			case hit:  unit.setHit(value);									break;
			case hitPct: unit.setHitPct(value); 							break;
			case dodge: unit.setDodge(value); 						break;
			case dodgePct:  unit.setDodgePct(value); 			break;
			case crit: unit.setCrit(value); 									break;
			case critPct:  unit.setCritPct(value);						break;
			case critAdd: unit.setCritAdd(value); 					break;
			case critAddPct: unit.setCritAddPct(value); 			break;
			case tough:  unit.setTough(value); 						break;
			case toughPct: unit.setToughPct(value); 				break;
			case elem1Atk:  unit.setElem1Atk(value);				break;
			case elem1AtkPct: unit.setElem1AtkPct(value); 	break;
			case elem1Def: unit.setElem1Def(value); 				break;
			case elem1DefPct:  unit.setElem1DefPct(value); 	break;
			case elem2Atk:  unit.setElem2Atk(value);				break;
			case elem2AtkPct: unit.setElem2AtkPct(value); 	break;
			case elem2Def: unit.setElem2Def(value); 				break;
			case elem2DefPct:  unit.setElem2DefPct(value); 	break;
			case elem3Atk:  unit.setElem3Atk(value);				break;
			case elem3AtkPct: unit.setElem3AtkPct(value); 	break;
			case elem3Def: unit.setElem3Def(value); 				break;
			case elem3DefPct:  unit.setElem3DefPct(value); 	break;
			case elem4Atk:  unit.setElem4Atk(value);				break;
			case elem4AtkPct: unit.setElem4AtkPct(value); 	break;
			case elem4Def: unit.setElem4Def(value); 				break;
			case elem4DefPct:  unit.setElem4DefPct(value); 	break;
			case suck: unit.setSuck(value); 								break;
			case suckPct:  unit.setSuckPct(value);					break;
			case suckRatio: unit.setSuckRatio(value); 				break;
			case suckRatioPct: unit.setSuckRatioPct(value); 	break;
			case avoidAtk:  unit.setAvoidAtk(value); 				break;
			case avoidAtkPct: unit.setAvoidAtkPct(value); 		break;
			case skillHealth:  unit.setSkillHealth(value);			break;
			case skillHealthPct: unit.setSkillHealthPct(value); 	break;
			case speed: unit.setSpeed(value); 							break;
			case speedPct:  unit.setSpeedPct(value); 				break;
			case hpRecov: unit.setHpRecov(value); 					break;
			case hpRecovPct:  unit.setHpRecovPct(value);		break;
			case mpRecov: unit.setMpRecov(value); 				break;
			case mpRecovPct: unit.setMpRecovPct(value); 		break;
			case mpReduce:  unit.setMpReduce(value); 			break;
			case mpReducePct: unit.setMpReducePct(value); break;
			
			default: break;
		}
	}
}