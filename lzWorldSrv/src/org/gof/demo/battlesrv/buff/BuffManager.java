package org.gof.demo.battlesrv.buff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.manager.FightManager;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.UnitPropPlusMap;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.battlesrv.support.PropCalcCommon;
import org.gof.demo.battlesrv.support.PropExtKey;
import org.gof.demo.battlesrv.support.PropKey;
import org.gof.demo.battlesrv.support.UnitObjectStateKey;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfBuff;
import org.gof.demo.worldsrv.entity.Buff;
import org.gof.demo.worldsrv.msg.Msg.DBuff;
import org.gof.demo.worldsrv.msg.Msg.DBuffProp;
import org.gof.demo.worldsrv.msg.Msg.SCBuffAdd;
import org.gof.demo.worldsrv.msg.Msg.SCBuffDispel;
import org.gof.demo.worldsrv.msg.Msg.SCBuffUpdate;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.Message.Builder;

public class BuffManager extends ManagerBase {
	
	public static long INTERVAL_PULSE = 100;
	private static long lastPusle = 0;
	
	//走通用逻辑，可以在作用时累加，失效时直接减去的字段集合
	private List<String> propNormal;
		
	//不能走通用逻辑，不能累加的各种特殊字段集合
	private List<String> propSpecial;
	
	public BuffManager() {
		propNormal = PropExtKey.toList();
		propNormal.addAll(PropKey.toList());
		
		propSpecial = Utils.ofList(	PropExtKey.hpCur.name(),							//当前血量
												PropExtKey.hpMaxPct.name(),				//最大血量
												PropExtKey.hpCurPct.name(),						//当前血量系数
												PropExtKey.mpCur.name(),							//当前魔量
												PropExtKey.mpCurPct.name(),						//当前魔量系数
												PropExtKey.hpLoss.name(),							//伤害值
												PropExtKey.hpLossPct.name(),	
//												PropKey.rage.name(),	
												UnitObjectStateKey.stun.name(), 					//眩晕
												UnitObjectStateKey.immobilize.name(), 		//定身
												UnitObjectStateKey.silence.name(),			//沉默
												UnitObjectStateKey.skill_hypnosis.name(),	//催眠
												UnitObjectStateKey.skill_sheep.name(),	//变羊
												"skill_charm",				//魅惑
												"skill_immuneMag",			//魔法免疫
												"skill_immunePhy",			//物理免疫
												"skill_noDead",				//不死状态
												"skill_magShield",			//魔法盾
												"skill_phyShield",			//物理盾
												"skill_allShield",			//免伤盾
												"skill_god",				//无敌状态
												"skill_immuneControl",		//免疫控制
												"skill_rageAttack", 		//释放技能获得怒气增加
												"skill_rageKill"			//杀人回复怒气增加
												);				
	}
	
	/**
	 * 获取实例
	 * @return
	 */
	public static BuffManager inst() {
		return inst(BuffManager.class);
	}
	
	/**
	 * 为unitObj添加一个buff
	 * @param unitObj	目标
	 * @param idFire		Buff施放者Id
	 * @param buffSn	Buffsn
	 */
	public void add(UnitObject unitObj, long idFire, int buffSn, int skillSn, int friend) {
		UnitObject unitFire = unitObj.stageObj.getUnitObjs().get(idFire);
		
		if(friend < 0) {
			Event.fire(EventKey.UNIT_BE_ATTACKED, "unitAtk", unitFire, "unitDef", unitObj, "skillSn", skillSn);
			//如果有 免疫所有负面状态 返回
			if(unitObj.skillTempInfo.immuneControl) { 
				return;
			}
		}
		
		ConfBuff conf = ConfBuff.get(buffSn);
		if(conf == null) {
			Log.buff.info("buff add null {} {}", buffSn, skillSn);
		}
		String defProp = conf.defProp;
		String fireProp = conf.fireProp;
		float weight = 1;
		if(!StringUtils.isEmpty(defProp) && PropKey.contains(defProp)) {
			if(conf.defWeight != 0) {
				weight += UnitManager.inst().getPropKeyValue(PropKey.getEnumByType(defProp), unitObj.getUnit()) / conf.defWeight;
			}
		}
		if(!StringUtils.isEmpty(fireProp) && PropKey.contains(fireProp)) {
			if(conf.fireWeight != 0) {
				weight += UnitManager.inst().getPropKeyValue(PropKey.getEnumByType(fireProp), unitFire.getUnit()) / conf.fireWeight;
			}
		}
		
		//技能等级加成
		float[] propValueArr = null;
		if(conf.propValue != null && conf.propValue.length > 0) {
			propValueArr = new float[conf.propValue.length];
			for (int i = 0; i < conf.propValue.length; i++) {
				propValueArr[i] = conf.propValue[i];
			}
		}
		
		SkillCommon skillCom = unitFire.skills.get(skillSn);
		if(skillCom != null) {
			int skillLevel = skillCom.skillLevel;
			if(conf.levelParam != null && conf.levelParam.length > 0) {
				for (int i = 0; i < conf.levelParam.length; i++) {
					propValueArr[i] += conf.levelParam[i] * skillLevel;
				}
			}
		}
		Map<String, Float> propMap = Utils.toMapWeight(conf.propName, propValueArr, weight);
		add(unitObj, idFire, buffSn, Utils.toJSONString(propMap));
		
		//反馈效果
		if(conf.feedback) {
			Map<String, Float> propMapNag = new HashMap<String, Float>();
			for (String key : propMap.keySet()) {
				propMapNag.put(key, -propMap.get(key));
			}
			add(unitFire, idFire, buffSn, Utils.toJSONString(propMapNag));
		}
	}
	
	/**
	 * 添加一个动态buff，动态是指buff的属性效果值不是在配置表里固定的
	 * @param unitObj
	 * @param idFire
	 * @param buffSn
	 * @param propPlusJSON
	 */
	public void add(UnitObject unitObj, long idFire, int buffSn, String propPlusJSON) {
		ConfBuff conf = ConfBuff.get(buffSn);
		if(conf == null) {
			Log.temp.error("Buff配置不存在sn={},{}", buffSn, new SysException("Buff配置不存在sn=" + buffSn));
			return;
		}
		
		//根据互斥优先级，判断该单位身上是否需要添加该Buff		
		//查看身上是否有同类Buff
		Buff buffInSameType = unitObj.getBuffs().get(conf.type);
		if(buffInSameType != null) {
			//不可叠加
			if(conf.multiple == 0) {
				//根据优先级是否需要把旧的去掉
				if(conf.priority < ConfBuff.get(buffInSameType.getSn()).priority) {
					//新Buff优先级低，什么都不需要做
					return;
				} else {
					//移除旧的低优先级Buff
					remove(unitObj, buffInSameType);
				}
			} else if (conf.multiple == 1) {
				//时间叠加
				buffInSameType.setTimeEnd(buffInSameType.getTimeEnd() + conf.timeExist);
				
				//buff时间更新了
				SCBuffUpdate.Builder buffUpdate = SCBuffUpdate.newBuilder();
				buffUpdate.setObjId(unitObj.id);
				buffUpdate.setBuff(createMsgBuff(buffInSameType));
				sendMsg(unitObj, buffUpdate, conf);
				
				return;
			}  else if (conf.multiple == 2) {
				//属性叠加
			}
		}
		
		//添加新Buff
		long curr = unitObj.getTime();
		Buff buff = new Buff();
		buff.setId(Port.applyId());
		buff.setType(conf.type);
		buff.setSn(buffSn);
		buff.setIdAffect(unitObj.id);
		buff.setIdFire(idFire);
		buff.setPropPlusDefaultJSON(propPlusJSON);
		//时间
		buff.setTimeEnd(curr + conf.timeExist);
		buff.setTimePulse(curr + conf.timeDelay);
		lastPusle = curr;
		
		//如果是同一类的Buff，则上下次更新时间保留
		if(buffInSameType != null) {
			buff.setTimePulse(buffInSameType.getTimePulse());
		}
		
		unitObj.getBuffs().put(buff.getType(), buff);
		
		//如果是人身上的buff，且该buff在人下线后需要保留，则持久化
		if(unitObj.isHumanObj() && conf.isReserveOffLine) {
			buff.persist();
		}
		
		//根据Buff的广播类型发送消息，增加新的Buff
		SCBuffAdd.Builder buffAdd = SCBuffAdd.newBuilder();
		buffAdd.setBuff(createMsgBuff(buff));
		buffAdd.setObjId(unitObj.id);
		//给前端发送消息
		sendMsg(unitObj, buffAdd, conf);
		
		//buff直接先作用一次
		if(unitObj instanceof HumanObject) {
			HumanObject humanObj= (HumanObject)unitObj;
			//监听玩家自身属性变化
			HumanInfoChange.listen(humanObj);
		}
		pulseBuff(unitObj, buff, true);
		
	}
	
	/**
	 * 通过sn获取unitObj身上的Buff
	 * @param unitObj
	 * @param type
	 * @return
	 */
	public Buff getBySn(UnitObject unitObj, int sn) {
		for(Buff buff : unitObj.getBuffs().values()) {
			if(buff.getSn() == sn) {
				return buff;
			}
		}
		return null;
	}
	
	/**
	 * 移除该单位身上的所有Buff
	 * @param unitObj
	 */
	public void removeAll(UnitObject unitObj) {
		List<Buff> removeList = new ArrayList<>();
		for(Buff buff : unitObj.getBuffs().values()) {
			removeList.add(buff);
		}
		//删除Buff
		for(Buff buff : removeList) {
			remove(unitObj, buff);
		}
	}
	
	/**
	 * 移除buff效果
	 * @param unitObj
	 * @param buff
	 */
	public void remove(UnitObject unitObj, Buff buff) {
		PropCalcCommon propDefault = new PropCalcCommon(buff.getPropPlusDefaultJSON());
		//特殊字段，直接增加某些值的，比如说加血，加魔，时间到了不会减去
		for(String key : propSpecial) {
			double value = propDefault.getDouble(key);
			if(value == 0) continue;
			removeSpecial(unitObj, key);
		}
		
		//先把buff的影响修正
		PropCalcCommon propMinus = new PropCalcCommon(buff.getPropPlusJSON());
		
		PropCalcCommon prop;
		boolean needSendMsg = false;
		for(String key : propNormal) {
			double value = propMinus.getDouble(key);
			if(value == 0.0) continue;
			
			if(key.equals(PropKey.speed.name())) {
				needSendMsg = true;
				break;
			}
		}

		
		UnitPropPlusMap pp = unitObj.dataPers.unitPropPlus;
		prop = new PropCalcCommon(pp.getBuff());
		prop.minus(propMinus);
		pp.setBuff(prop.toJSONStr());
		//重新计算玩家属性
		UnitManager.inst().propCalc(unitObj);
		
		ConfBuff confBuff = ConfBuff.get(buff.getSn());
		//发送广播
		if(needSendMsg) {
//			Log.buff.info(" buff remove");
			sendMsg(unitObj, unitObj.createMsgUpdate(), confBuff);
		}

		//从单元身上移走
		unitObj.getBuffs().remove(buff.getType());
		buff.remove();
		
		//群发消息，Buff消失
		SCBuffDispel.Builder buffDispel = SCBuffDispel.newBuilder();
		buffDispel.setObjId(unitObj.id);
		buffDispel.setSn(buff.getSn());
		sendMsg(unitObj, buffDispel, confBuff);
		
	}
	
	public void removeSpecial(UnitObject unitObj, String prop) {
		switch(prop) {
		case "stun":
			unitObj.removeState(UnitObjectStateKey.stun);
			break;
		//定身
		case "immobilize":
			unitObj.removeState(UnitObjectStateKey.immobilize);
			break;
		//沉默
		case "silence":
			unitObj.removeState(UnitObjectStateKey.silence);
			break;
		//催眠
		case "skill_hypnosis":
			unitObj.removeState(UnitObjectStateKey.skill_hypnosis);
			break;
		//变羊
		case "skill_sheep":
			unitObj.removeState(UnitObjectStateKey.skill_sheep);
			break;
		//魅惑
		case "skill_charm":
			unitObj.removeState(UnitObjectStateKey.charm);
			break;
		//无敌
		case "skill_god":
			unitObj.skillTempInfo.godMod = false;
			break;
		//魔法免疫
		case "skill_immuneMag":
			unitObj.skillTempInfo.immuneMag = false;
			break;
		//不死
		case "skill_noDead":
			unitObj.skillTempInfo.noDead = false;
			break;
		//物理免疫
		case "skill_immunePhy":
			unitObj.skillTempInfo.immunePhy = false;
			break;
		//魔法盾
		case "skill_magShield":
			unitObj.skillTempInfo.magShield = 0;
			break;
		//物理盾
		case "skill_phyShield":
			unitObj.skillTempInfo.phyShield = 0;
			break;
		//免伤盾
		case "skill_allShield":
			unitObj.skillTempInfo.allShield = 0;
			break;
		//免疫所有负面buff
		case "immuneControl":
			unitObj.skillTempInfo.immuneControl = false;
			break;
		case "skill_rageAttack":
			unitObj.skillTempInfo.rageAttack = 0;
			break;
		case "skill_rageKill":
			unitObj.skillTempInfo.rageKill = 0;
			break;
		default:
			break;
	}
	}
	
	/**
	 * 生成包含Buff基本信息的消息
	 * @param buff
	 * @return
	 */
	public DBuff createMsgBuff(Buff buff) {
		ConfBuff conf = ConfBuff.get(buff.getSn());
		DBuff.Builder dBuff = DBuff.newBuilder();
		dBuff.setSn(buff.getSn());
		dBuff.setTimeLeft((int)((conf.timeExistOnline - buff.getTimeExistOnline())));
				
		//增加每个BUFF的具体处理内容
		JSONObject data = JSON.parseObject(buff.getPropPlusDefaultJSON());
		for (Entry<String, Object> entry : data.entrySet()) {
			DBuffProp.Builder buffProp = DBuffProp.newBuilder();
			buffProp.setBuffKey(entry.getKey());
			buffProp.setBuffValue(Double.valueOf(entry.getValue().toString()));
			dBuff.addBuffProp(buffProp);
		}
//		for(int i = 0 ; i < conf.propName.length ; i++) {
//			DBuffProp.Builder buffProp = DBuffProp.newBuilder();
//			buffProp.setBuffKey(conf.propName[i]);
//			buffProp.setBuffValue(conf.propValue[i]);
//			dBuff.addBuffProp(buffProp);
//		}
		return dBuff.build();
	}
	
	/**
	 * 单独更新一个buff
	 * @param unitObj
	 * @param buff
	 */
	public void pulseBuff(UnitObject unitObj, Buff buff, boolean immediate) {
		ConfBuff conf = ConfBuff.get(buff.getSn());
		
		long curr = unitObj.getTime();
		if(!immediate) {
			buff.setTimeExistOnline(buff.getTimeExistOnline() + (curr - lastPusle));
		}
		
		lastPusle = unitObj.getTime();
		//不是周期性Buff，且已经作用过了
		if(!conf.isPeriod && buff.isAffected()) return;
		
		//如果buff下一次生效时间还没到
		if(curr < buff.getTimePulse()) return;
		
		//生效
		affect(unitObj, buff, conf);
		
		//下次生效
		buff.setTimePulse(curr + conf.timePeriod);
		
	}
	
	/**
	 * 更新unitObj身上的buff状态
	 * @param unitObj
	 */
	public void pulse(UnitObject unitObj, long curr) {
		Collection<Buff> buffs = unitObj.getBuffs().values();
		if(buffs.size() > 0 && unitObj instanceof HumanObject) {
			HumanObject humanObj= (HumanObject)unitObj;
			//监听玩家自身属性变化
			HumanInfoChange.listen(humanObj);
		}
		//在这里遍历buff
		List<Buff> removeList = new ArrayList<>();
		for(Buff buff : buffs) {
			ConfBuff conf = ConfBuff.get(buff.getSn());
			//生效过的Buff，如果作用时间或者存在时间到了，则删除
			//最大存在时间如果不配置，为0，则表示这个buff除非累计时间到了，不然可以一直存在
			//最大存在时间如果配置为0，则表示这个buff的在线作用时间不用管，除非最大时间到了
			//如果两个都为0，那么这个buff就一直存在着
			if(buff.isAffected() && ((conf.timeExistOnline > 0 && buff.getTimeExistOnline() >= conf.timeExistOnline) || (conf.timeExist > 0 && buff.getTimeEnd() < curr))) {
				removeList.add(buff);
				continue;
			}
			//如果两个时间都为0，就是配错了
			if(buff.isAffected() && conf.timeExist <= 0 && conf.timeExistOnline <= 0) {
				removeList.add(buff);
				continue;
			}
			
			//该buff具体的更新操作
			pulseBuff(unitObj, buff, false);
		}
		
		//移除需要删除的buff 
		for(Buff buff : removeList) {
			remove(unitObj, buff);
		}
	}
	
	/**
	 * 人身上的Buff作用一次
	 * 要统计属性，还有一些作用一次的，特殊处理：hpCur,mpCur,coin,exp,conk(昏迷),freeze(定身)
	 * @param human
	 * @param positive
	 */
	public void affect(UnitObject unitObj, Buff buff, ConfBuff confBuff) {
		PropCalcCommon propDefault = new PropCalcCommon(buff.getPropPlusDefaultJSON());
		//特殊字段，直接增加某些值的，比如说加血，加魔，时间到了不会减去
		for(String key : propSpecial) {
			double value = propDefault.getDouble(key);
			if(value == 0) continue;
			
			UnitObject unitFire = unitObj.stageObj.getUnitObjs().get(buff.getIdFire());
			affectPropSpecial(unitObj, unitFire, key, value);
		}
		
		boolean needSendMsg = false;
		//普通字段，作用时加上，失效时要减掉的字段
		//组织需要加成的属性
		PropCalcCommon propPlus = new PropCalcCommon();
		for(String key : propNormal) {
			double value = propDefault.getDouble(key);
			if(value == 0.0) continue;
			
			if(key.equals(PropKey.speed.name())) {
				needSendMsg = true;
			}
			propPlus.plus(key, value);
		}
		
		PropCalcCommon prop;
		UnitPropPlusMap pp = unitObj.dataPers.unitPropPlus;
		prop = new PropCalcCommon(pp.getBuff());
		prop.plus(propPlus);
		pp.setBuff(prop.toJSONStr());
		//重新计算玩家属性
		UnitManager.inst().propCalc(unitObj);
		//发送广播
		if(needSendMsg) {
//			Log.buff.info(" buff affect");
			sendMsg(unitObj, unitObj.createMsgUpdate(), confBuff);
		}

		//重新计算并保存buff的加成效果
		PropCalcCommon propBuff = new PropCalcCommon(buff.getPropPlusJSON());
		propBuff.plus(propPlus);
		buff.setPropPlusJSON(propBuff.toJSONStr());
		
		if(!buff.isAffected()) {
			buff.setAffected(true);
		}
	}
	
	
	/**
	 * buff作用时比较特殊的处理逻辑，包括：hpCur,hpCurPct,mpCur,mpCurPct,damageGold,damagePoison,damageCurse,stun(昏迷),immobilize(定身),silence(沉默)
	 * @param human
	 * @param prop
	 * @param value
	 */
	public void affectPropSpecial(UnitObject unitObj, UnitObject unitFire, String prop, double value) {
		  if (unitObj == null){
              return;
          }
		//unitFire可能为null!!!!!!!
		int valueInt = (int)value;
		switch(prop) {
			
			//血量hpCur
			case "hpCur":
				if(valueInt < 0) {
					UnitManager.inst().reduceHp(unitObj, Math.abs(valueInt), unitFire, new Param("hpLostKey", HpLostKey.BUFF));
				} else {
					if(unitObj.getUnit().getSkillHealth() > 0) {
						valueInt = Math.abs(valueInt) + unitObj.getUnit().getSkillHealth();
					}
					UnitManager.inst().addHp(unitObj, HpLostKey.BUFF, Math.abs(valueInt), unitFire);
				}
				break;
			case "hpCurPct":
				valueInt = (int)(unitObj.getUnit().getHpCur() * value / FightManager.PERINT);
				if(valueInt < 0) {
					UnitManager.inst().reduceHp(unitObj, (int)Math.abs(valueInt), unitFire, new Param("hpLostKey", HpLostKey.BUFF));
				} else {
					if(unitObj.getUnit().getSkillHealth() > 0) {
						valueInt = Math.abs(valueInt) + unitObj.getUnit().getSkillHealth();
					}
					UnitManager.inst().addHp(unitObj, HpLostKey.BUFF, Math.abs(valueInt), unitFire);
				}
				break;
			case "hpMaxPct":
				valueInt = (int)(unitObj.getUnit().getHpMax() * value / FightManager.PERINT);
				if(valueInt < 0) {
					UnitManager.inst().reduceHp(unitObj, (int)Math.abs(valueInt), unitFire, new Param("hpLostKey", HpLostKey.BUFF));
				} else {
					if(unitObj.getUnit().getSkillHealth() > 0) {
						valueInt = Math.abs(valueInt) + unitObj.getUnit().getSkillHealth();
					}
					UnitManager.inst().addHp(unitObj, HpLostKey.BUFF, Math.abs(valueInt), unitFire);
				}
				break;
			//魔量mpCur
			case "mpCur":
				break;
			case "mpCurPct":
				break;
			//伤害值
			case "hoLoss":
				if(valueInt <= 0) break;
				UnitManager.inst().reduceHp(unitObj, valueInt, unitFire, new Param("hpLostKey", HpLostKey.BUFF));
				break;
			//伤害系数
			case "hpLossPct":
				if(value <= 0) break;
				valueInt = (int)(unitObj.getUnit().getHpMax() * value / FightManager.PERINT);
				UnitManager.inst().reduceHp(unitObj, valueInt, unitFire, new Param("hpLostKey", HpLostKey.BUFF));
				 break;
			//眩晕
			case "stun":
				unitObj.toState(UnitObjectStateKey.stun, valueInt);
				break;
			//定身
			case "immobilize":
				unitObj.toState(UnitObjectStateKey.immobilize, valueInt);
				break;
			//沉默
			case "silence":
				unitObj.toState(UnitObjectStateKey.silence, valueInt);
				break;
			//催眠
			case "skill_hypnosis":
				unitObj.toState(UnitObjectStateKey.skill_hypnosis, valueInt);
				break;
			//变羊
			case "skill_sheep":
				unitObj.toState(UnitObjectStateKey.skill_sheep, valueInt);
				break;
			//魅惑
			case "skill_charm":
				if(unitFire != null) {
				unitObj.toState(UnitObjectStateKey.charm, valueInt);
				}
				break;
			//无敌
			case "skill_god":
				unitObj.skillTempInfo.godMod = true;
				break;
			//魔法免疫
			case "skill_immuneMag":
				unitObj.skillTempInfo.immuneMag = true;
				break;
			//物理免疫
			case "skill_immunePhy":
				unitObj.skillTempInfo.immunePhy = true;
				break;
			//不死
			case "skill_noDead":
				unitObj.skillTempInfo.noDead = true;
				break;
			//魔法盾
			case "skill_magShield":
				unitObj.skillTempInfo.magShield = valueInt;
				break;
			//物理盾
			case "skill_phyShield":
				unitObj.skillTempInfo.phyShield = valueInt;
				break;
			//免伤盾
			case "skill_allShield":
				unitObj.skillTempInfo.allShield = valueInt;
				break;
				//免疫所有负面buff
			case "immuneControl":
				unitObj.skillTempInfo.immuneControl = true;
				break;
			case "skill_rageAttack":
				unitObj.skillTempInfo.rageAttack = valueInt;
				break;
			case "skill_rageKill":
				unitObj.skillTempInfo.rageKill = valueInt;
				break;
			default:
				break;
		}
	}
	
	/**
	 * 根据不同情况，给前端发送消息
	 * @param unitObject
	 * @param builder
	 */
	public void sendMsg(UnitObject unitObj, Builder builder, ConfBuff confBuff) {
		switch(confBuff.msgShowType) {
		//不给前端发送消息
		case 0:
			break;
		//只给自己发 
		case 1:
			HumanObject humanObj = unitObj.getHumanObj();
			humanObj.sendMsg(builder);
			break;
		//群发广播
		case 2:
			StageManager.inst().sendMsgToArea(builder, unitObj.stageObj, unitObj.posNow);
			break;
		}
	}
	
	
	
	/**
	 * 驱散unitobj上能被dispelId驱散的buff
	 * @param unitObj
	 * @param dispelId
	 */
	public void dispel(UnitObject unitObj, String dispelSn) {
		List<Buff> dispelList = new ArrayList<>();
		//查找可以被驱散的Buff
		for(Buff buff : unitObj.getBuffs().values()) {
			ConfBuff confBuff = ConfBuff.get(buff.getSn());
			if(dispelSn.equals(confBuff.dispelSn)) {
				dispelList.add(buff);
			}
		}
		//驱散Buff
		for(Buff buff : dispelList) {
			remove(unitObj, buff);
		}
	}
	
	@Listener(EventKey.UNIT_BE_KILLED)
	public void dispelByUnitBeKilled(Param param) {
		UnitObject unitObj = param.get("dead");
		// 检测身上是否有buff在单元死亡后不保留
		List<Buff> removeList = new ArrayList<>();
		for(Buff buff : unitObj.getBuffs().values()) {
			ConfBuff confBuff = ConfBuff.get(buff.getSn());
			if(confBuff.isReserveDied)
				continue;
			removeList.add(buff);
		}
		// 移除需要移除的buff
		for(Buff buff : removeList) {
			remove(unitObj, buff);
		}
	}
	
	/**
	 * 玩家上线是重新计算buff加成效果
	 * @param param
	 */
	@Listener(EventKey.HUMAN_LOGIN)
	public void onHumanLogin(Param param) {
		HumanObject humanObj = param.get("humanObj");
		onHumanLoginBuffCal(humanObj);
		for (UnitObject unitObj : humanObj.slaves.values()) {
			onHumanLoginBuffCal(unitObj);
		}
	}
	
	private void onHumanLoginBuffCal(UnitObject unitObj) {
		UnitPropPlusMap pp = unitObj.dataPers.unitPropPlus;
		// 先清理旧的效果，再重新计算需要保存的Buff的效果
		pp.setBuff("{}");
		PropCalcCommon buffPropPlus = new PropCalcCommon();
		for(Buff buff : unitObj.getBuffs().values()) {
			buffPropPlus.plus(buff.getPropPlusJSON());
		}
		pp.setBuff(buffPropPlus.toJSONStr());
		
		UnitManager.inst().propCalc(unitObj);
	}
	
	
}
