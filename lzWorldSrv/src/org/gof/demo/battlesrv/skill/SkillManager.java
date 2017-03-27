package org.gof.demo.battlesrv.skill;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.buff.BuffManager;
import org.gof.demo.battlesrv.bullet.BulletManager;
import org.gof.demo.battlesrv.bullet.BulletObject;
import org.gof.demo.battlesrv.dot.DotManager;
import org.gof.demo.battlesrv.dot.DotObject;
import org.gof.demo.battlesrv.manager.FightManager;
import org.gof.demo.battlesrv.manager.StageBattleManager;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.logic.AbstractSkillLogic;
import org.gof.demo.battlesrv.skill.logic.AbstractSkillLogicActive;
import org.gof.demo.battlesrv.skill.logic.AbstractSkillLogicPassive;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.battlesrv.support.EnumSkillTypeKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfInborn;
import org.gof.demo.worldsrv.config.ConfSkill;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.config.ConfSkillGroup;
import org.gof.demo.worldsrv.entity.Buff;
import org.gof.demo.worldsrv.entity.Unit;
import org.gof.demo.worldsrv.msg.Msg.DBackPos;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.skill.InbornVO;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class SkillManager extends ManagerBase {
	
	/**
	 * 获取实例
	 * @return
	 */
	public static SkillManager inst() {
		return inst(SkillManager.class);
	}
	
	
	/**
	 * 登录到地图时，初始化技能并且发送技能列表
	 * @param params
	 */
	@Listener(EventKey.HUMAN_STAGE_ENTER)
	public void onHumanLoginStage(Param params) {
		HumanObject humanObj = params.get("humanObj");
		
		//初始化玩家技能
		initSkill(humanObj);
		initInborn(humanObj);
		for(UnitObject uo : humanObj.slaves.values()) {
			if(uo != null && uo.isGeneralObj()) {
				SkillManager.inst().initSkill(uo);
			}
		}
		
	}
	
	/**
	 * 初始化技能
	 * @param unitObj
	 * @return
	 */
	public void initSkill(UnitObject unitObj) {
		//初始化主角技能
		initSkillRole(unitObj);
	}
	
	/**
	 * 初始化天赋
	 * @param unitObj
	 */
	public void initInborn(UnitObject unitObj){
		//获取玩家所有天赋
		List<InbornVO> list = InbornVO.getList(unitObj.getInborns());
		for(InbornVO ib:list){
			ConfInborn conf = ConfInborn.get(ib.sn);
			if(conf.type == 1 )continue;
			SkillCommon skill = unitObj.skills.get(conf.skillSn);
			if(skill !=null ){
				skill.inbornToSkill(ib);
			}
		}
		
	}
	/**
	 * 初始化主角技能
	 * @param unitObj
	 */
	public void initSkillRole(UnitObject unitObj) {
		
		//清空技能
		Map<Integer, SkillCommon> skills = unitObj.skills;
		skills.clear();
		unitObj.skillOrder.clear();
		
		//获取主角技能
		JSONArray ja = Utils.toJSONArray(unitObj.getSkills());
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			int sn = jo.getIntValue("sn");
			int level = jo.getIntValue("level");
			ConfSkill confSkill = ConfSkill.get(Integer.valueOf(sn));
			if(confSkill == null) {
				Log.fight.info("技能配置里面没有此配置，{} : {}",unitObj.name, sn);
				continue;
			}
			unitObj.skillOrder.add(sn);
			
			//初始化正常技能
			SkillCommon skill = new SkillCommon(unitObj, confSkill, level);
			skills.put(new Integer(sn), skill);
		}
		
		int skillGroupSn = unitObj.dataPers.unit.getSkillGroupSn();
		//默认技能
		unitObj.defaultSkill = skills.get(skillGroupSn);
	}
	
	/**
	 * 角色初始化时，初始化技能组里所有技能的字符串
	 * @param skillGroupSn
	 * @return
	 */
	public String firstInitSkills(int skillGroupSn) {
		ConfSkillGroup confSkillGroup = ConfSkillGroup.get(skillGroupSn);
		int[] skillAll = new int[confSkillGroup.activeSkills.length + confSkillGroup.passiveSkills.length];
		int[] skillLevel =  new int[skillAll.length];
		int[] pos = new int[skillAll.length];
		
		//主动技能
		int i = 0;
		for (int actSkill : confSkillGroup.activeSkills) {
			skillAll[i] = actSkill;
			if(actSkill == 0) continue;
			
			skillLevel[i] = 1;
			pos[i] = ConfSkill.get(actSkill).position;
			i++;
		}
		//被动技能
		for (int pasSkill : confSkillGroup.passiveSkills) {
			skillAll[i] = pasSkill;
			if(pasSkill == 0) continue;
			
			skillLevel[i] = 1;
			pos[i] = ConfSkill.get(pasSkill).position;
			i++;
		}
		
		JSONArray ja = new JSONArray();
		for (int j = 0; j < skillAll.length; j++) {
			JSONObject jo = new JSONObject();
			jo.put("sn", skillAll[j]);
			jo.put("level", skillLevel[j]);
			jo.put("pos", pos[j]);
			ja.add(jo);
		}
		
		return ja.toJSONString();
	}
	
	/**
	 * 单一释放技能接口，简单处理，根据配置释放技能
	 * @param unitObj
	 * @param skillSn
	 */
	public void doSimpleSkill(UnitObject attackUnitObj, int skillSn) {
		ConfSkill conf = ConfSkill.get(skillSn);
		if(conf == null) return ;
		
		SkillParam skillParam = new SkillParam();
		//必须是有目标的技能
		if( conf.clickType  == SkillCommon.CLICK_TAR) {
			UnitObject defUnitObj = null;
			
			//如果没有AI
			if(attackUnitObj.ai == null) {
				// 从仇恨值列表里取出仇恨值最高的
				int aggro = 0;
				for (Entry<Long, Integer> entry : attackUnitObj.aggroList.entrySet()) {
					//如果仇恨值小，跳过
					if(entry.getValue() <= aggro) continue;
					
					//取出对象
					UnitObject tempObj = attackUnitObj.stageObj.getUnitObj(entry.getKey());
					//如果死了，跳过
					if(!tempObj.isInWorld() || tempObj.isDie()) continue;
					//如果一伙跳过
					if(tempObj.teamBundleID != attackUnitObj.teamBundleID) continue;
					
					if(entry.getValue() > aggro){
						defUnitObj = tempObj;
					}
				}
				
				//如果没有仇恨值，那么就从攻击者随机选出来一个
				for (UnitObject tempObj : attackUnitObj.beAttacked.values()) {
					// 如果死了，跳过
					if(!tempObj.isInWorld() || tempObj.isDie()) continue;
					// 如果一伙跳过
					if(tempObj.teamBundleID != attackUnitObj.teamBundleID) continue;
					
					//找到就跳过
					defUnitObj = tempObj;
					break;
				}
				
				//TODO 如果没有，就找一个离自己最近的
				
			} else {//如果有AI，就选取ai的目标
				defUnitObj = attackUnitObj.ai.targetObj;
			}
			
			//目标不存在
			if(defUnitObj == null) {
				return ;
			}
			
			//如果有目标，设定目标
			skillParam.tarUo = defUnitObj;
			skillParam.tarPos = defUnitObj.posNow;
		}
		
		//奔着朝向发技能
		if(conf.clickType == SkillCommon.CLICK_VECTOR) {
			
			skillParam.tarPos = attackUnitObj.posNow;
		}
		
		//技能配置的点选目标是自己
		if(conf.clickType == SkillCommon.CLICK_SELF) {
			skillParam.tarUo = attackUnitObj;
			skillParam.tarPos = attackUnitObj.posNow;
		}
		
		//是否是连击或连点技能最终伤害
		skillParam.finalAtk = false;
		
		//释放技能
		SkillManager.inst().shakeOrCastSkill(attackUnitObj, skillSn, skillParam);
	}
	
	/**
	 * 有前摇技能加入施放队列，无前摇技能直接施放
	 * @param unitObj
	 * @param skillSn
	 * @param param
	 * @param atkerType		1玩家或者怪物本身 2魂将
	 * @param finalAtk		连点或者连击技能最终技
	 */
	public ReasonResult shakeOrCastSkill(UnitObject unitObj, int skillSn, SkillParam param) {
		//判断技能是否能释放
		SkillCommon skill = null;
		
		skill = unitObj.skills.get(skillSn);
		//技能不存在
		if(skill == null) {
			Log.fight.error("技能不存在，攻击者name={} 攻击者id={},技能id={} skillSize={}", unitObj.name, unitObj.id, skillSn,  unitObj.skills.size());
			
			return new ReasonResult(false, I18n.get("fight.canCastSkill.notExist", skillSn));
		}
			
		if(skill.confSkill.snChange > 0) {
			SkillCommon skillChange = unitObj.skills.get(skill.confSkill.snChange);
			if(skillChange != null) {
				skill = skillChange;
			}
		}
		
		
		//施放大招的优先级高于普通攻击，因此施放大招时先取消其他技能施放
		if(skill.confSkill.type == EnumSkillTypeKey.绝招.getType() || skill.confSkill.type == EnumSkillTypeKey.杀招.getType()){
			skill.cancelCurrSkill();
		}
		
		//判断是否能施放技能
		ReasonResult result = skill.canCast(param);
		if(!result.success) {
			return result;
		} 
		
		//计算连击
		skill.comboCal(param);
				
		//检查是否在前摇状态
		SkillExcute curSkillExcute = unitObj.skillTempInfo.skillToExcute;
		if(curSkillExcute != null) {
			return new ReasonResult(false, I18n.get("fight.canCastSkill.stateInShake"));
		}
		
		//发攻击前事件
		if(unitObj.isHumanObj()) {
			Event.fire(EventKey.HUMAN_ACTS_BEFORE, "humanObj", (HumanObject)unitObj);
		}
		
		//施放技能
		skill.castFirst(param);
		
		return new ReasonResult(true);
	}
	
	/**
	 * 获取技能列表
	 * @param human
	 * @return
	 */
	public List<DSkill> getSkills(UnitObject unitObj) {
		List<DSkill> result = new ArrayList<>();
		
		JSONArray ja = Utils.toJSONArray(unitObj.getSkills());
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			
			DSkill.Builder dSkill = DSkill.newBuilder();
			dSkill.setSkillSn(jo.getIntValue("sn"));
			dSkill.setSkillLevel(jo.getIntValue("level"));
			dSkill.setPosition(jo.getIntValue("pos"));
			
			result.add(dSkill.build());
		}
		
		return result;
	}
	
	/**
	 * 给某一个技能加1级
	 * @param uniObj
	 * @param sn
	 */
	public void addSkill(UnitObject uniObj, int sn) {
		JSONObject jo = null;
		//遍历ＪＳＯＮ，找到要升级的技能
		JSONArray ja = Utils.toJSONArray(uniObj.getSkills());
		for (int i = 0; i < ja.size(); i++) {
			JSONObject j = ja.getJSONObject(i);
			if(j.getInteger("sn") == sn){
				jo = j;
				break;
			}
		}
		
		//找到了要升级的技能
		if(jo!=null) {
			int level = jo.getIntValue("level");
			jo.put("level", level+1);
		} else {
			jo =  new JSONObject();
			jo.put("sn", sn);
			jo.put("level", 1);
			ConfSkill conf = ConfSkill.get(sn);
			jo.put("pos", conf.position);
			ja.add(jo);
		}
		
		uniObj.getUnit().setSkill(Utils.toJSONString(ja));
		// 派发事件
		Event.fire(EventKey.SKILL_UPGRADE, "uniObj", uniObj, "sn", sn);
	}
	
	/**
	 *  删除一个技能
	 * @param uniObj
	 * @param sn
	 */
	public void removeSkill(UnitObject uniObj, int sn) {
		JSONArray ja = Utils.toJSONArray(uniObj.getSkills());
		
		JSONObject target = null;
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if(jo.getInteger("sn") == sn){
				target = jo;
				break;
			}
		}
		
		//没有就返回
		if(target == null) {
			return;
		}
		
		//重新设置技能
		ja.remove(target);
		uniObj.getUnit().setSkill(Utils.toJSONString(ja));
	}
	
	/**
	 * 添加某一个技能
	 * @param uniObj
	 * @param sn
	 */
	public void addSkill(UnitObject uniObj, int sn, int level, int position) {
		//遍历ＪＳＯＮ，找到要升级的技能
		JSONArray ja = Utils.toJSONArray(uniObj.getSkills());
		boolean find = false;
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if(jo.getInteger("sn") == sn){
				find = true;
				jo.put("level", level);
				break;
			}
		}
		
		//如果没找到就新加一个
		if(!find){
			JSONObject jo =  new JSONObject();
			jo.put("sn", sn);
			jo.put("level", level);
			jo.put("pos", position);
			ja.add(jo);
		}
		
		uniObj.getUnit().setSkill(Utils.toJSONString(ja));
	}
	
	/**
	 * 添加一堆技能，都是一样的级别
	 * @param uniObj
	 * @param sn
	 */
	public void addSkill(UnitObject uniObj, int[] sn, int level, int[] pos) {
		for (int i = 0; i < sn.length; i++) {
			if(sn[i] == 0) continue;
			addSkill(uniObj, sn[i], level, pos[i]);
		}
	}
	
	/**
	 * 添加一堆技能
	 * @param uniObj
	 * @param sn
	 */
	public void addSkill(UnitObject uniObj, int[] sn, int[] level, int[] pos) {
		for (int i = 0; i < sn.length; i++) {
			if(sn[i] == 0) continue;
			addSkill(uniObj, sn[i], level[i], pos[i]);
		}
	}
	
	/**
	 * 被动技能检查
	 * @param param
	 */
	@Listener(EventKey.SKILL_PASSIVE_CHECK)
	public void passiveSkillCheck(Param param) {
		UnitObject unitAtk = param.get("unitAtk");
		UnitObject unitDef = param.get("unitDef");
		SkillEventKey key = param.get("skillEventKey");
		SkillParam position = param.get("SkillParam");
		SkillParamVO vo = param.get("vo");
		
		//攻击方检查被动
		if(unitAtk != null) {
			doPassiveSkill(unitAtk, unitDef, position, key, true, vo);
		}
		
		//防御方检查被动
		if(unitDef != null) {
			doPassiveSkill(unitDef, unitAtk, position, key, false, vo);
		}
	}
	
	/**
	 * 检查触发被动技能
	 * @param self
	 * @param tar
	 * @param key
	 * @param isAtker
	 * @param isPartner
	 * @param skillParamVO
	 */
	private void doPassiveSkill(UnitObject self, UnitObject tar, SkillParam position, SkillEventKey key,  boolean isAtker,SkillParamVO skillParamVO) {
		List<SkillCommon> skills = new ArrayList<>();
		
		//获取角色所有技能
		skills.addAll(self.skills.values());
		
		//判断被动技能是否触发
		for(SkillCommon skill : skills) {
//			主动技能略过
			if(skill.confSkill.active) continue;
			
			//被动技能cd中
			if(!skill.isCoolingNormal(null).success) {
				continue;
			}
			
			//标记被动初始没有触发过
			boolean flag = false;
			
			//遍历被动技能的所有技能效果，依次判断触发
			for(AbstractSkillLogic logic : skill.logics) {
				//主动效果略过
				if(logic instanceof AbstractSkillLogicActive) {
					continue;
				}
				
				AbstractSkillLogicPassive logicPassive = (AbstractSkillLogicPassive)logic;
				
				//获取随机概率
				double rand = self.nextDouble();
				
				//没有触发
				if(rand > logicPassive.conf.triggerPct) continue;
				
				//效果触发
				if(logicPassive.canTrigger(key, isAtker)) {
					logicPassive.trigger(key, tar, position, skillParamVO);
					flag = true;
				}
			}
			
			//被动技能放入技能冷却中
			if(flag) {
				skill.addCoolDown();
			}
		}
	}
	
	
	/**
	 * 用于计算伤害
	 * @param unitObjAtk 攻击者
	 * @param unitObjDef 防守者
	 * @param skillSn  技能SN
	 * @param lostHpPct 技能伤害系数
	 * @param addDamage 技能额外伤害
	 * @return
	 */
	public int calcHurt(UnitObject unitObjAtk, UnitObject unitObjDef, int skillSn, double lostHpPct, int addDamage, Param param) {
		int hpLost = 0;
		
		//判断计算伤害前是否有被动技能触发（攻击忽视对方防御）
		SkillParamVO vo = new SkillParamVO();
		if(param.containsKey("skill")) {
			Event.fire(EventKey.SKILL_PASSIVE_CHECK, 
					"unitAtk", unitObjAtk, 
					"unitDef", unitObjDef, 
					"skillEventKey", SkillEventKey.EVENT_ON_BEFORE_CALC_HURT, 
					"vo", vo);
		}
		
		//获得攻击类型
		ConfSkill confSkill = ConfSkill.get(skillSn);
		int atkType = 1;
		if(confSkill != null) {
			atkType = confSkill.atkType;
		}
		//普通伤害
		hpLost += FightManager.inst().calcBaseHurt(unitObjAtk, unitObjDef, atkType);
		hpLost = (int)(hpLost * lostHpPct + addDamage);
		
		//伤害后处理
		if(unitObjDef.skillTempInfo.godMod) {
			hpLost = 0;
		} else if(atkType == 1 && unitObjDef.skillTempInfo.immuneMag) {
			hpLost = 0;
		} else if(atkType == 2 && unitObjDef.skillTempInfo.immunePhy) {
			hpLost = 0;
		}
		
		hpLost = unitObjDef.skillTempInfo.getHplostSheild(unitObjDef, atkType, hpLost);
		
		
		return hpLost;
	}
	
	/**
	 * 指定目标伤害的处理
	 * @param unitObjAtk
	 * @param unitObjDef
	 * @param param 在技能中 new Param("skill", skill, "hpLostKey", HpLostKey.SKILL) 
	 *  ("hpLostKey", HpLostKey.DOT, "dot", this) 
	 *  ("hpLostKey", HpLostKey.BULLET, "bullet", this)
	 * @param addDamage 技能额外伤害
	 * @param hit 是否必中
	 * @return
	 */
	public int doSkillEffectToTar(UnitObject unitObjAtk, UnitObject unitObjDef, Param param ,int addDamage, double lostHpPct) {
		Unit ua = unitObjAtk.getUnit();
		
		int skillSn = getSkillSnFromParam(param);
		int hpLost = calcHurt(unitObjAtk, unitObjDef, skillSn, lostHpPct, addDamage, param);
		ConfSkill confSkill = ConfSkill.get(skillSn);
		int atkType = 1;
		if(confSkill != null) {
			atkType = confSkill.atkType;
		}
		
		//发送攻击时间
		Event.fire(EventKey.UNIT_BE_ATTACKED, "unitAtk", unitObjAtk, "unitDef", unitObjDef, "skillSn", skillSn);
		
		//判断扣血前攻击方是否有被动技能触发（攻击伤害加成或受攻击伤害减免）
		SkillParamVO vo = new SkillParamVO();
		if(param.containsKey("skill")) {
			Event.fire(EventKey.SKILL_PASSIVE_CHECK, 
					"unitAtk", unitObjAtk, 
					"unitDef", unitObjDef, 
					"skillEventKey", SkillEventKey.EVENT_ON_BEFORE_HPLOST, 
					"vo", vo);
		}
		
		if(unitObjAtk != null) {
			
			boolean isCrit = FightManager.inst().isCrit(unitObjAtk, unitObjDef, atkType);
			
			//计算暴击伤害
			if(isCrit) {
				Double critHert = hpLost + hpLost * ua.getCritAdd() / 10000.0D;
				hpLost = critHert.intValue();
			} 
			//扣血
			param.put("isCrit", isCrit);
			UnitManager.inst().reduceHp(unitObjDef, hpLost, unitObjAtk, param);
		} 	
		
		//技能中记录本次伤害
		if(hpLost > 0) {
			//判断扣血后是否有被动技能触发
			if(param.containsKey("skill")) {
				Event.fire(EventKey.SKILL_PASSIVE_CHECK, "unitAtk", unitObjAtk, 
						"unitDef", unitObjDef, 
						"skillEventKey", SkillEventKey.EVENT_ON_AFT_HPLOST, 
						"vo", new SkillParamVO());
			}
		}
		
		return hpLost;
	}
	/**
	 * 获取作用目标集合
	 * 
	 * @return
	 */
	public List<UnitObject> getTars(SkillEffectVO conf, UnitObject unitObjAtk, UnitObject tarUo, Vector2D tarPos) {
		List<UnitObject> tars = new ArrayList<>();
		int count = conf.targetNum;
		if(conf.logicSn != 1) {
			count += unitObjAtk.skillTempInfo.mutilMagic; 
		}
		// 单体取目标
		if (conf.scopeType == 1) {
			if (conf.targetSelf) {
				tars.add(unitObjAtk);
			} else {
				tars.add(tarUo);
			}
		} else if (conf.scopeType == 2) {
			// 目标周围圆形取坐标 圆形内所有的unitObj
			List<UnitObject> unitAll = StageBattleManager.inst().getUnitObjsInCircle(unitObjAtk.stageObj, tarPos, conf.scopeParam1);
			tars.addAll(unitAll);
		} else if (conf.scopeType == 3) {
			// 自身朝向扇形取坐标 扇形中的所有unitObj

			// 如果目标点跟自身点重合，做若干修正
			Vector2D vec = tarPos;
			if (tarPos == unitObjAtk.posNow) {
				vec = new Vector2D(vec.x, vec.y + 0.001);
			}

			List<UnitObject> unitAll = StageBattleManager.inst().getUnitObjsInSector(unitObjAtk.stageObj, unitObjAtk.posNow, vec, conf.scopeParam1, conf.scopeParam2, true);
			tars.addAll(unitAll);
		} else if (conf.scopeType == 4) {
			// 自身朝向后扇形

			// 如果目标点跟自身点重合，做若干修正
			Vector2D vec = tarPos;
			if (tarPos == unitObjAtk.posNow) {
				vec = new Vector2D(vec.x, vec.y + 0.001);
			}

			List<UnitObject> unitAll = StageBattleManager.inst() .getUnitObjsInSector(unitObjAtk.stageObj, unitObjAtk.posNow, vec, conf.scopeParam1, conf.scopeParam2, false);
			tars.addAll(unitAll);
		} else if (conf.scopeType == 5) {
			// 自身朝向矩形
			// 如果目标点跟自身点重合，做若干修正
			Vector2D vec = tarPos;
			if (tarPos == unitObjAtk.posNow) {
				vec = new Vector2D(vec.x, vec.y + 0.001);
			}

			List<UnitObject> unitAll = StageBattleManager.inst() .getUnitObjsInRectangle(unitObjAtk.stageObj, unitObjAtk.posNow, vec, conf.scopeParam1, conf.scopeParam2);
			tars.addAll(unitAll);
		} else if (conf.scopeType == 6) {
			//全地图
			for(UnitObject unitObj : unitObjAtk.stageObj.getUnitObjs().values()) {
				tars.add(unitObj);
			}
		} else if (conf.scopeType == 7 || conf.scopeType == 8) {
			//如果当前目标不在范围内就换个 type2 == 2的
			if(tarUo == null || tarUo.isDie() || tarUo.posNow.distance(tarPos) > conf.scopeParam1 || tarUo.teamBundleID == unitObjAtk.teamBundleID) {
				// 目标周围圆形取坐标 圆形内所有的unitObj
				List<UnitObject> unitAll = StageBattleManager.inst().getUnitObjsInCircle(unitObjAtk.stageObj, tarPos, conf.scopeParam1);
				tars.addAll(unitAll);
			} else {
				tars.add(tarUo);
			}
		} else if (conf.scopeType == 9) {
			// 目标周围圆形取坐标 圆形内所有的unitObj
			for(int i = 0 ; i < count ; i++) {
				UnitObject hpLeastObj = StageBattleManager.inst().getUnitObjsInCircleHpLeast(unitObjAtk.stageObj, tars, tarPos, conf.scopeParam1);
				if(hpLeastObj != null) {
					tars.add(hpLeastObj);
				}
			}
		} else if (conf.scopeType == 10) {
			// 目标周围圆形取坐标 圆形内所有的unitObj
			 List<UnitObject> unitAll = StageBattleManager.inst().getUnitObjsInCircleProfession(unitObjAtk.stageObj, conf.scopeParam2, tarPos, conf.scopeParam1);
			 tars.addAll(unitAll);
		}
		
		
		// 删除inworld = false的unitObj
		Iterator<UnitObject> iter = tars.iterator();
		while (iter.hasNext()) {
			UnitObject unitObjTmp = iter.next();
			
			if(unitObjTmp == null) {
				continue;
			}
			//强制删除humanobject
//			if (unitObjTmp.isHumanObj()) {
//				iter.remove();
//				continue;
//			}
			
			// inworld=false，或者是技能释放者都得排除
			//如果是攻击敌方的技能，如果去除自己的人
			//如果攻击方是自己的，那么去除对方的人
			boolean bExcludeTeamBundle = conf.excludeTeamBundle;
			if (
					(!unitObjTmp.isInWorld() && conf.scopeType != 1)
//					|| (!conf.targetSelf && unitObjTmp.id == unitObjAtk.id)
					|| (!bExcludeTeamBundle && unitObjTmp.teamBundleID != unitObjAtk.teamBundleID)
					|| (bExcludeTeamBundle && unitObjTmp.teamBundleID == unitObjAtk.teamBundleID)
					) {
				iter.remove();
				continue;
			} 

			if (unitObjTmp.isDie() && conf.scopeType != 1) {
				iter.remove();
				continue;
			}

		}

		//如果从远处向近处搜索 并且数量大于1 那么从一堆中间找个最远的
		if(tars.size() > 1 && conf.scopeType == 8) {
			tars = StageBattleManager.inst().getUnitObjFarest(tars, unitObjAtk, conf.excludeTeamBundle);
		}
		
		// 并且数量大于1 那么从一堆中间找个最近的
		if(tars.size() > 1 && conf.scopeType == 7) {
			tars = StageBattleManager.inst().getUnitObjNearest(tars, unitObjAtk, conf.excludeTeamBundle);
		}
		
		// 取指定最大个数目标,如果是群伤技能的话
		if (conf.scopeType != 1 && count < tars.size() && count != 0) {
			int sizeMinus = tars.size() - count;
			for (int i = 0; i < sizeMinus; i++) {
				tars.remove(unitObjAtk.nextInt(tars.size()));
			}
		}

		unitObjAtk.skillTempInfo.mutilMagic = 0;
		return tars;
	}
	
	public void executeSkillLogic(ConfSkillEffect conf,  UnitObject unitObjFire, WorldObject worldObjectCreate, UnitObject unitDef, Param paramConst) {
		int skillSn = getSkillSnFromParam(paramConst);
		Param param = new Param(paramConst);
		//伤害系统
		if(conf.logicSn == 1) {
			double pctCommon = Utils.doubleValue(conf.param1);
			int addCommon = Utils.intValue(conf.param2);
			//等级加成
			SkillCommon skill = unitObjFire.skills.get(skillSn);
			int add = skill.skillLevel * conf.levelParam;
			addCommon += add;
			int[] buffSn = Utils.arrayStrToInt(conf.param3);
			double buffPct = Utils.doubleValue(conf.param4);
//			param = new Param("skill", skill, "hpLostKey", HpLostKey.SKILL);
			double buffPctTemp = 0;
			if(buffSn != null && buffSn.length > 0) {
				for (Buff buff : unitDef.dataPers.buffs.values()) {
					for(int i = 0 ; i < buffSn.length ; i++) {
						if(buff.getSn() == buffSn[i]) {
							buffPctTemp = buffPct;
							break;
						}
					}
				}
			}
			
			SkillManager.inst().doSkillEffectToTar(unitObjFire, unitDef,  param, addCommon, pctCommon + buffPctTemp);
//			Log.fight.info("skil001 : {} {}", unitObjFire.name, conf.sn);
//			Log.fight.info("doBulle hpLost {}", hpLost);
		} else if(conf.logicSn == 2) {
			//dot
			Vector2D vec = unitDef.posNow;
			UnitObject unitObj = unitDef;
			
			if(conf.targetSelf) {
				vec = unitObjFire.posNow;
				unitObj = unitObjFire;
			}
			int dotSn = Utils.intValue(conf.param1);
			DotManager.inst().create(unitObjFire.stageObj, dotSn, skillSn, unitObjFire, worldObjectCreate, unitObj, vec, 1);
		} else if(conf.logicSn == 3) {
			//buff
			UnitObject uo = unitObjFire;
			Long useId = uo.id;
			
			if(useId != null) {
				int buffSn = Utils.intValue(conf.param1);
				int buffProp = Utils.intValue(conf.param2);
				if(buffProp == 0) {
					buffProp = 100;
				}
				if(buffProp >= unitObjFire.nextInt(100)) {
					BuffManager.inst().add(unitDef, useId, buffSn, skillSn, conf.targetFriend);
				}
			}
		} else if(conf.logicSn == 4) {
			//bullet
			Vector2D vec = unitDef.posNow;
			UnitObject unitObj = unitDef;
			
			if(conf.targetSelf) {
				vec = unitObjFire.posNow;
				unitObj = unitObjFire;
			}
			int bulletSn = Utils.intValue(conf.param1);
			BulletManager.inst().create(unitObjFire.stageObj, bulletSn, skillSn, unitObjFire, worldObjectCreate, unitObj, vec);
		}
	}
	
	/**
	 * @param sn
	 * @param skillSn 这个为0 表示不是有技能直接触发 比如DOT 生出来的， bullet生出来的
	 * @param unitObjFire
	 * @param unitObjTar
	 * @param tarVec
	 * @param param
	 * @return
	 */
	public List<UnitObject> executeSkillLogic(int sn, UnitObject unitObjFire, WorldObject worldObjectCreate, UnitObject unitObjTar, Vector2D tarVec, Param param) {
		List<UnitObject> tars = new ArrayList<UnitObject>();
		ConfSkillEffect conf = ConfSkillEffect.getBy("level", 1, "effectSn", sn);
		if(conf == null) {
			return tars;
		}
		
		tars = SkillManager.inst().getTars(new SkillEffectVO(conf), unitObjFire, unitObjTar, tarVec);
		//获得人物位移
		float backDis = conf.effectDis;
		if(tars.size() > 0 && conf.attackMove) {
			Vector2D dBackVec = SkillManager.inst().getDBackPosFire(unitObjFire, tarVec, backDis);
			if(dBackVec != null) {
				param.put("dBackVec", dBackVec);
				param.put("backDis", backDis);
			}
		}
		
		//产生的bullet，并且没有目标的话，那么忽略目标直接打
		if(conf.logicSn == 4 && tars.isEmpty()){
			int skillSn = getSkillSnFromParam(param);
			int bulletSn = Utils.intValue(conf.param1);
			BulletManager.inst().create(unitObjFire.stageObj, bulletSn, skillSn, unitObjFire, worldObjectCreate, null, tarVec);
			
		} else {
			for (UnitObject unitDef : tars) {
				executeSkillLogic(conf, unitObjFire, worldObjectCreate, unitDef, param);
			}
		}
		
		
		return tars;
	}
	
	public int getSkillSnFromParam(Param param) {
		int skillSn = 0;
		if(param.containsKey("skill")) {
			SkillCommon skill = param.get("skill");
			skillSn = skill.confSkill.sn;
			
		} else if(param.containsKey("dot")) {
			DotObject dotObj = param.get("dot");
			skillSn = dotObj.skillSn;
			
		} else if(param.containsKey("bullet")) {
			BulletObject bulletObj = param.get("bullet");
			skillSn = bulletObj.skillSn;
		}
		return skillSn;
	}
		
	/**
	 * 打断技能
	 */
	public void interruptCurrSkill(int sn, UnitObject uintObjAtt, UnitObject uintObjDef){
		ConfSkill confSkill = ConfSkill.get(sn);
		//如果当前技能可以打断
		if(confSkill == null || !confSkill.canInterrupt) {
			return;
		}
		
		//判断对方的缓冲技能是否可以被打断
		uintObjDef.interruptCurrSkill(uintObjAtt);
		
	}
	
	public boolean isSpeciaCasting(WorldObject obj) {
		if(obj == null) {
			return false;
		}
		UnitObject atkObj = null;
		if(obj instanceof UnitObject) {
			atkObj = (UnitObject)obj;
		} else {
			return false;
		}
		//判断上次释放的技能是不是绝招 或者 杀招
		ConfSkill confSkill = ConfSkill.get(atkObj.skillTempInfo.lastSkillSn);
		if(confSkill == null) {
			return false;
		}
		if(confSkill.type == EnumSkillTypeKey.绝招.getType() || confSkill.type == EnumSkillTypeKey.杀招.getType()){
			//判断是否正在释放技能中
			if(atkObj.castSkilling) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isChargeCasting(WorldObject obj) {
		UnitObject atkObj = null;
		if(obj instanceof UnitObject) {
			atkObj = (UnitObject)obj;
		} else {
			return false;
		}
		ConfSkill confSkill = ConfSkill.get(atkObj.skillTempInfo.lastSkillSn);
		if(confSkill == null) {
			return false;
		}
		if(confSkill.isCharge) {
			return true;
		}
		return false;
	}
	
	/**
	 *
	 * @param unitObject
	 * @param skillSn
	 * @return
	 */
	/**
	 *  计算被技能攻击导致的后退
	 * @param canMove	是否能移动
	 * @param unitFire		攻击目标
	 * @param unitDef		受攻击目标
	 * @param targetPos	目标点
	 * @param backDis		主角技能移动距离
	 * @return
	 */
	public DBackPos.Builder setDBackPosMons(boolean canMove, UnitObject unitFire, UnitObject unitDef, Vector2D targetPos, float backDis) {
		
		if(backDis == 0) {
			return null;
		}
		
		if(!canMove) {
			return null;
		}
		
		//如果是人，就直接返回
		if(unitDef.isHumanObj()){
//			return null;
		}
		
		unitDef.stop();
		
		
		Vector2D backPos = getDefBackPos(canMove, unitDef, targetPos, backDis);
		
		if(backPos == null) {
			return null;
		}
		//发送消息
		DBackPos.Builder backPosMsg = DBackPos.newBuilder();
		backPosMsg.setId(unitDef.id);
		backPosMsg.setPos(backPos.toMsg());
		backPosMsg.setType(2);
		unitDef.posNow = backPos;
		
		return backPosMsg;
	}
	
	/**
	 * 只计算位移，用冲量计算
	 * @param unitFire
	 * @param tars
	 * @param conf
	 * @return
	 */
	public float backDis(UnitObject unitFire, List<UnitObject> tars, ConfSkillEffect conf) {
		float massTotal = 0;
//		for (UnitObject unitObject : tars) {
//			massTotal += unitObject.confModel.mass;
//		}
		
		massTotal += unitFire.confModel.mass;
		if(massTotal == 0) {
			return 0;
		}
		float dis = 0;
		//计算位移
		dis = conf.impulse / massTotal;
		return dis;
	}
	
	/**
	 * 攻击者位移
	 * @param unitFire
	 * @param unitDef
	 * @param backDis
	 * @return
	 */
	public Vector2D getDBackPosFire(UnitObject unitFire, Vector2D targetPos, float backDis) {
		if(backDis == 0) {
			return null;
		}
		
		Vector2D backPos = getFireBackPos(true, unitFire, targetPos, backDis);
		if(backPos == null) {
			return null;
		}
		unitFire.posNow = backPos;
		return backPos;
	}
	
	/**
	 * 计算攻击位移
	* @param canMove		是否能移动
	 * @param unitFire		攻击者
	 * @param unitDef		被攻击者
	 * @param backDis		技能攻击移动距离
	 * @return
	 */
	public Vector2D getFireBackPos(boolean canMove , UnitObject unitFire, Vector2D targetPos, double backDis) {
		
		Vector2D backPos = null;
		Vector2D start = unitFire.posNow;
		Vector2D end = targetPos;
		
		backPos = Vector2D.lookAtDis(start, end, unitFire.posNow, backDis);
		backPos = StageManager.getRaycastDis(unitFire.stageObj.sn, unitFire.posNow, backPos, unitFire.stageObj.pathFindingFlag);
		
		return backPos;
	}
	
	public Vector2D getDefBackPos(boolean canMove , UnitObject unitDef, Vector2D targetPos, double backDis) {
		
		Vector2D backPos = null;
		Vector2D start = unitDef.posNow;
		Vector2D end = targetPos;
		
		if(start.distance(end) < 0.1){
			return end;
		}
		
		backPos = Vector2D.lookAtDis(start, end, unitDef.posNow, backDis);
		backPos = StageManager.getRaycastDis(unitDef.stageObj.sn, unitDef.posNow, backPos, unitDef.stageObj.pathFindingFlag);
		
		return backPos;
	}
	
}
