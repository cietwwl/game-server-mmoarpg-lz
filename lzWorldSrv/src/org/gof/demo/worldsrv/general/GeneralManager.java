package org.gof.demo.worldsrv.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.gof.core.Record;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.manager.StageBattleManager;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.stageObj.UnitDataPersistance;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.UnitPropPlusMap;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfCharacterGeneral;
import org.gof.demo.worldsrv.config.ConfGeneralLvUp;
import org.gof.demo.worldsrv.config.ConfGeneralQualityUp;
import org.gof.demo.worldsrv.config.ConfGeneralStarUp;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.config.ConfSkill;
import org.gof.demo.worldsrv.entity.Buff;
import org.gof.demo.worldsrv.entity.General;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.UnitPropPlus;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DGeneralEquipment;
import org.gof.demo.worldsrv.msg.Msg.DGeneralInfo;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralRemove;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralToAttIng;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.LoadHumanDataUtils;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GeneralManager extends ManagerBase {
	
	
	public static int ATTING_GENERAL_MAX_NUM = 5;    //出站伙伴上限
	
	/**
	 * 获取实例
	 * @return
	 */
	public static GeneralManager inst() {
		return inst(GeneralManager.class);
	}
	
	/**
	 * 生成武将并直接装入human
	 * @param humanObject
	 * @param id
	 * @param sn
	 * @return
	 */
	public GeneralObject createAddToHuman(HumanObject humanObj, long id, String sn) {
		UnitDataPersistance dataPers = GeneralManager.inst().create(humanObj.getHuman(), id, sn, -1);
		GeneralObject generalObj = GeneralManager.inst().addToUnit(humanObj, (General)dataPers.unit);
		
		//属性加成
		GeneralPlusManager.inst().calcGeneralLvUpProp(generalObj);
		GeneralPlusManager.inst().calcGeneralStarUpProp(generalObj);
		GeneralPlusManager.inst().calcGeneralQulityUpProp(generalObj);
		
		generalObj.dataPers = dataPers;
		
		//派发生成事件
		Event.fire(EventKey.GENERAL_CREATE, "generalObj", generalObj, "humanObj", humanObj);
		
		//初始化技能
//		SkillManager.inst().initSkill(generalObj);
		
		//计算战斗力 
		UnitManager.inst().propCalc(generalObj);
		
		return generalObj;
	}
	
	/**
	 * 将武将加入到小弟的范围中
	 * @param unit
	 * @param gen
	 * @return
	 */
	public GeneralObject addToUnit(HumanObject unit, General gen) {
		GeneralObject obj = new GeneralObject(unit, gen);
		unit.slaves.put(gen.getId(), obj);
		if(gen.getAttingIndex() > -1) {
			addBattle(unit, obj.id, gen.getAttingIndex());
		}
		
		return obj;
	}
	
	/**
	 * 武将初始化
	 * @param humanID
	 * @param id
	 * @param name
	 * @param profession
	 * @param sex
	 * @param modelSn
	 * @return
	 */
	public UnitDataPersistance create(Human human, long id, String sn, int atkIndex) {
//		ConfCharacterGeneral conf = ConfCharacterGeneral.get(sn);
		//获取武将配置
		ConfCharacterGeneral conf = ConfCharacterGeneral.get(sn);
		
		UnitDataPersistance dataPers = new UnitDataPersistance();
		General gen = new General();
		gen.setHumanID(human.getId());
		gen.setId(id);
		if(conf != null) {
			gen.setName(conf.name);  // former:  conf.name + String.valueOf(conf.name)
		}
		gen.setProfession(conf.profession);  //conf.profession
		gen.setSex(conf.sex);
		
		gen.setLevel(conf.level);
		gen.setExpCur(0);
		ConfGeneralLvUp cglu = ConfGeneralLvUp.get(gen.getLevel());
		gen.setExpUpgrade(cglu.exp);
		
		gen.setSn(sn);
		gen.setModelSn(conf.modelSn);
		
		gen.setAttingIndex(atkIndex);
		// cur与max要相等 并且不能为零
		gen.setHpCur(1);
		gen.setHpMax(1);
		gen.setMpCur(1);
		gen.setMpMax(1);
		
		//首次初始化技能
		gen.setSkill(SkillManager.inst().firstInitSkills(conf.skillGroupSn));
		gen.setSkillGroupSn(conf.skillGroupSn);
//		gen.setSkill(this.addInitSkill(sn, conf.initQuality, conf.initStar));
		
		//初始化装备      品质     星级
		gen.setEquip(new GeneralEquipment(conf.equipmnet).equipInfoJson());
		gen.setQuality(conf.initQuality);
		gen.setStar(conf.initStar);
		
		//给玩家设置当前的武将，加入到拥有的武将列表中, 用于武将羁绊
		HumanManager.inst().addGeneralHistory(human, sn);
		
		// 持久化
		gen.persist();
		
		//属性加成
		UnitPropPlus pp = new UnitPropPlus();
		pp.setId(gen.getId());

		//基础属性
		PropCalc basePropCalc = new PropCalc(Utils.toJOSNString(conf.properties, conf.value));
		pp.setBase(basePropCalc.toJSONStr());
		
		pp.persist();
		
		dataPers.unit = gen;
		
		UnitPropPlusMap unitProp = new UnitPropPlusMap();
		unitProp.unitPropPlus = pp;
		unitProp.init(pp);
		
		dataPers.unitPropPlus = unitProp;
		
		return dataPers;
	}
	
	/**
	 *初始技能
	 * @param sn
	 * @param qualityLv
	 * @return
	 */
	private String addInitSkill(String sn,int qualityLv, int starLv){
		//获取配置
		ConfGeneralQualityUp cgqu = ConfGeneralQualityUp.getBy(ConfGeneralQualityUp.K.genSn, sn, ConfGeneralQualityUp.K.qualitySn, qualityLv);
		
		JSONArray obj =  Utils.toJSONArray("[]");	
		
		if(cgqu != null){
			if(cgqu.skillSn != 0){
			    //构建数据
				ConfSkill cs = ConfSkill.get(cgqu.skillSn);
				
				JSONObject oqu =  new JSONObject();
				oqu.put("sn", cgqu.skillSn);
				oqu.put("level", 1);
				oqu.put("pos", cs!=null?cs.position:0);
				
				obj.add(oqu);
			}
		}
		
		ConfGeneralStarUp cgsu = ConfGeneralStarUp.getBy(ConfGeneralStarUp.K.genSn, sn, ConfGeneralStarUp.K.starSn,starLv);

		if(cgsu != null){
			if(cgsu.skillSn != 0){
			    //构建数据
				ConfSkill cs = ConfSkill.get(cgsu.skillSn);
				
				JSONObject oqs =  new JSONObject();
				oqs.put("sn", cgsu.skillSn);
				oqs.put("level", 1);
				oqs.put("pos", cs!=null?cs.position:0);
				
				obj.add(oqs);
			}
		}
		
		return  Utils.toJSONString(obj);
	}
	
	
	/**
	 * 伙伴出战
	 * @param humanObj
	 * @param genIds
	 */
	public void generalToAtting(HumanObject humanObj, List<Long> genIds, int type){
		
		//如果大于5直接抛出
		if(humanObj.slavesAttingMap.size() >= ATTING_GENERAL_MAX_NUM) {
			Inform.user(humanObj.getHumanId(), Inform.提示操作, "出战武将数量错误");
			return;
		}
		
		//添加对应的武将
		for (int i = 0; i < genIds.size(); i++) {
			
			if(humanObj.slavesAttingMap.size() > ATTING_GENERAL_MAX_NUM) {
				Inform.user(humanObj.getHumanId(), Inform.提示操作, "出战武将数量错误");
				return;
			}
			CharacterObject unitObj = humanObj.slaves.get(genIds.get(i));
			GeneralManager.inst().addBattle(unitObj.parentObject, unitObj.id, humanObj.slavesAttingMap.size() + i);
		}
		
		//需要设置到数据库里
		GeneralManager.inst().setFightGeneral(humanObj, type, genIds);
		
		//构建返回消息
		SCGeneralToAttIng.Builder msgSend = SCGeneralToAttIng.newBuilder();
		msgSend.addAllUnits(genIds);
		humanObj.sendMsg(msgSend);
		
		Event.fireEx(EventKey.HUMAN_STAGE_ENTER, humanObj.stageObj.sn, "humanObj", humanObj);
	}

	
	public void login(HumanObject humanObject) {
		
		//计算全部战斗力
		propCalcAll(humanObject);
		
		//改在进入场景的时候计算
//		for(UnitObject uo : humanObject.slaves.values()) {
//			if(uo.isGeneralObj()) {
//				SkillManager.inst().initSkill(uo);
//			}
//		}
	}
	
	/**
	 * 根据玩法类型，获得出战武将的List
	 * 
	 * @param type
	 * @return
	 */
	public List<Long> getFightGeneral(HumanObject humanObj, int type){
		String fightGeneralJSON = humanObj.getHuman().getFightGeneralJSON();
		List<Long> result = new ArrayList<Long>();
		
		//如果是默认值，就直接返回空的
		if("[]".equals(fightGeneralJSON)) {
			return result;
		}
		
		//挨个遍历JSON
		JSONArray ja = Utils.toJSONArray(fightGeneralJSON);
		String r = null;
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			if(jo.getInteger("t") != type){
				continue;
			}
			
			//找到了就记录
			r= jo.getString("r");
		}
		
		//如果还没有记录当前玩法阵容，就返回一个空的
		if(StringUtils.isEmpty(r)) {
			return result;
		}
		
		String[] generalStrArr = r.split("\\|");
		for (int j = 0; j < generalStrArr.length; j++) {
			if(StringUtils.isEmpty(generalStrArr[j])){
				continue;
			}
			
			result.add(Long.parseLong(generalStrArr[j]));
		}
		
		return result;
	}
	
	/**
	 * 根据玩法类型，获得出战武将的List
	 * 
	 * @param type
	 * @return
	 */
	public void setFightGeneral(HumanObject humanObj, int type, List<Long> untis){
		String fightGeneralJSON = humanObj.getHuman().getFightGeneralJSON();
		
		//如果为空，直接返回
		if(untis.isEmpty()){
			return ;
		}
		
		//拼接字符串
		String r = "";
		for (Long l : untis) {
			r+=l + "|";
		}
		r = r.substring(0, r.length() - 1);
		
		//挨个遍历JSON
		boolean find = false;
		JSONArray ja = Utils.toJSONArray(fightGeneralJSON);
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			String temp = null;
			if(jo.getInteger("t") == type){
				find = true;
				temp = jo.getString("r");
				//如果没变化，直接返回
				if(temp.equals(r)){
					return ;
				} else {
					jo.put("r", r);
					break;
				}
			}
		}
		
		//如果没有找到，就插入新记录
		if(!find) {
			JSONObject jo = new JSONObject();
			jo.put("t", type);
			jo.put("r", r);
			ja.add(jo);
		}
		
		//设置新的值
		humanObj.getHuman().setFightGeneralJSON(ja.toJSONString());
		
	}
	
	/**
	 * 武将出战
	 * @param parentUnit
	 * @param id
	 * @param index
	 */
	public void addBattle(HumanObject parentUnit, long id, int index) {
		CharacterObject unit = parentUnit.slaves.get(id);
		
		//让原来的武将下来 
//		CharacterObject unitOld = parentUnit.salvesAttingList.get(index);
//		if(unitOld != null) {
//			parentUnit.slavesAttingMap.remove(unitOld.id);
//			unitOld.getUnit().setAttingIndex(-1);
//		}
		
		//移数据结构
		parentUnit.slavesAttingMap.put(id, unit);
		parentUnit.salvesAttingList.set(index, unit);
		unit.getUnit().setAttingIndex(index);
		
		return;
	}

	
	/**
	 * 所有武将出战
	 * @param unit
	 */
	public void addBattleAll(HumanObject unit) {
		addBattleAll(unit, false);	
	}
	
	/**
	 * 这unit 下面的所有可以出战的武将出战
	 * @param unit
	 * @param recoverHP
	 */
	public void addBattleAll(HumanObject unit, boolean recoverHP) {
		unit.slavesAttingMap.clear();
		
		//设置玩家出战
		for(UnitObject uo : unit.slaves.values()) {
			uo.parentObject = unit;
			if(uo.getUnit().getAttingIndex() > -1) {
				addBattle(unit, uo.id, uo.getUnit().getAttingIndex());
				//恢复HP
				if(recoverHP) {
					uo.getUnit().setHpCur(uo.getUnit().getHpMax());
				}
			}
			
			//初始化技能
			SkillManager.inst().initSkill(uo);
		}
		
	}
	
	public void recoverHp(HumanObject humanObj) {
		//重置武将的血
		for (UnitObject uo : humanObj.slaves.values()) {
			if(uo.isGeneralObj() && uo.getUnit().getAttingIndex() > -1){
				uo.dataPers.unit.setHpCur(uo.dataPers.unit.getHpMax());
			}
		}
	}
	
	/**
	 * 判断这个单位是否出战
	 * @param unit
	 * @return
	 */
	public boolean haveAtting(HumanObject unit) {
		for(UnitObject uo : unit.slaves.values()) {
			if(uo.getUnit().getAttingIndex() > -1) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 删除武将
	 * @param humanObj
	 * @param id
	 * @return
	 */
	public void removeGeneral(HumanObject humanObj, long generalId) {
		//先从内存里删除
		humanObj.slaves.remove(generalId);
		
		//从数据库里删除
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.delete(General.tableName, generalId);
		
		HumanInfoChange.listen(humanObj);
		UnitManager.inst().propCalc(humanObj);
	}
	
	/**
	 * 删除出战的武将 包括数据库数据。 一般用于第1次进入游戏的剧情
	 * @param humanObj
	 * @param generalId
	 */
	public void removeGeneralBattleAll(HumanObject humanObj) {
		for(UnitObject uo : humanObj.slavesAttingMap.values()) {
			humanObj.salvesAttingList.set(uo.getUnit().getAttingIndex(), null);
			uo.getUnit().remove();
			//先从内存里删除
			humanObj.slaves.remove(uo.id);
			SCGeneralRemove.Builder msg = SCGeneralRemove.newBuilder();
			msg.setId(uo.id);
			humanObj.sendMsg(msg);
		}
		humanObj.slavesAttingMap.clear();
		
		HumanInfoChange.listen(humanObj);
		UnitManager.inst().propCalc(humanObj);
		
	}
	
	/**
	 * 武将下场，从战斗阵型中移除
	 * @param parentUnit
	 * @param id
	 */
	public void removeBattle(HumanObject parentUnit, long id) {
		UnitObject unit = parentUnit.slaves.get(id);
		//移数据结构
		parentUnit.slavesAttingMap.remove(id);
		parentUnit.salvesAttingList.set(unit.getUnit().getAttingIndex(), null);
		unit.getUnit().setAttingIndex(-1);
		
	}
	
	/**
	 * 对武将进行排序， unitObject 实现了 comParator接口
	 * @param list
	 */
	private void sortUnitObject(List<CharacterObject> list ){
		if(list.size() <=0 )
			return;
		Comparator<CharacterObject> comp = list.get(0);
		Collections.sort(list, comp);
	}
	
	/**
	 * 所有武将显示 并根据对面的怪物出身配置 生成对应的武将站位
	 * @param parentUnit
	 */
	public void showAll(HumanObject parentUnit) {
		
		//获得武将的具体位置
		showPosiont(parentUnit);
	}
	
	public void hideAll(HumanObject parentUnit) {
		for(UnitObject unit : parentUnit.slavesAttingMap.values()) {
			//进入场景
			unit.stageLeave();
		}
	}
	
	/**
	 * 武将出生
	 * @param parentUnit
	 */
	public List<CharacterObject> showPosiont(HumanObject parentUnit) {
		List<CharacterObject> unitList = new ArrayList<CharacterObject>();
//		unitList.addAll(parentUnit.salvesAttingList);
		for (CharacterObject characterObject : parentUnit.salvesAttingList) {
			if(characterObject == null) {
				continue;
			}
			GeneralObject genObj = (GeneralObject)characterObject;
//			genObj = new GeneralObject(parentUnit, genObj.getGeneral());
			unitList.add(genObj);
		}
//		sortUnitObject(unitList);
		//获得武将的具体位置
		//获得第0个点的 开始和目标
		
		for (UnitObject uniObj : unitList) {
			
			if(uniObj != null) {
				uniObj.posBegin = StageBattleManager.inst().randomPosInCircle(parentUnit.posNow, 0, 3);
				uniObj.startup();
			}
		}
		
		return unitList;
	}
	
	public void removeBattleAll(HumanObject unit) {
		for(UnitObject uo : unit.slavesAttingMap.values()) {
			unit.salvesAttingList.set(uo.getUnit().getAttingIndex(), null);
			uo.getUnit().setAttingIndex(-1);
			uo.stageLeave();
		}
		unit.slavesAttingMap.clear();
	}
	
	/**
	 * 重新计算所有单位的数值
	 * @param humanObject
	 */
	public void propCalcAll(HumanObject humanObject) {
		for(UnitObject uo : humanObject.slaves.values()) {
			if(uo.isGeneralObj()) {
				UnitManager.inst().propCalc(uo);
			}
		}
	}
	
	/**重新计算出战单位的数值
	 * @param humanObject
	 */
	public void propCalcAtting(HumanObject humanObject) {
		//所有出战武将计算属性
		for (UnitObject uo : humanObject.salvesAttingList) {
			if(uo != null) {
				UnitManager.inst().propCalc(uo);
			}
		}
	}
	
	/**
	 * 构建武将基础信息的数据体
	 * @param unit
	 * @return
	 */
	public DGeneralInfo.Builder createDGeneralInfo(CharacterObject unit) {
		DGeneralInfo.Builder msg = DGeneralInfo.newBuilder();

		if(unit.isGeneralObj()) {
			msg.setId(unit.id);
			msg.setUnit(unit.createDUnit());
			
			//设置出战位置
			msg.setAttingPos(unit.getUnit().getAttingIndex());
			
			//装备信息
			GeneralObject generalObj = (GeneralObject)unit;
			List<DGeneralEquipment> equs = this.genEquipmentinfo(generalObj.getGeneral().getEquip());
			msg.addAllEquipment(equs);
			
			msg.setStar(generalObj.getGeneral().getStar());
			msg.setQuality(generalObj.getGeneral().getQuality());
			
			//设置武将技能
			List<DSkill> skills = SkillManager.inst().getSkills(unit);
			msg.addAllSkill(skills);
		}
		return msg;
	}
	
	/**
	 * 伙伴装备信息
	 * @param equipInfo
	 * @return
	 */
	private List<DGeneralEquipment> genEquipmentinfo(String equipInfo){
		List<DGeneralEquipment> list = new ArrayList<DGeneralEquipment>();
		
		JSONArray obj = Utils.toJSONArray(equipInfo);
		for(int i=0; i < obj.size(); i++){
		    DGeneralEquipment.Builder de = DGeneralEquipment.newBuilder();
		    
			int equipSn = obj.getIntValue(i);
			ConfItemData cid = ConfItemData.get(equipSn);
		    de.setSn(equipSn);
		    de.setQuality(cid.quality);
		    
		    list.add(de.build());
		}
		
		return list;
	}
	
	
	//----------------------------------------------------------------------------
	@Listener(value=EventKey.HUMAN_STAGE_ENTER)
	public void onHumanStageEnter(Param param) {
//		HumanObject humanObj = param.get("humanObj");
//		for (UnitObject unitObj : humanObj.slaves.values()) {
//			if(unitObj.isGeneralObj()) {
//				GeneralObject genObj = (GeneralObject)unitObj;
//				genObj.cofGen = ConfCharacterGeneral.get(genObj.modelSn);
//				//设置monster的配置信息
//				if(genObj.cofGen == null) {
//					Log.monster.error("地图{}上武将{}配置不存在", genObj.stageObj.sn, genObj.modelSn);
//					return;
//				}
//				genObj.confModel = ConfCharacterModel.get(genObj.cofGen.modelSn);
//			}
//		}
	}
	@Listener(value=EventKey.HUMAN_STAGE_ENTER_BEFORE)
	public void onHumanStageEnterBefore(Param param) {
		HumanObject humanObj = param.get("humanObj");
		
		//修改index 为-1的怪物的teambundle
		for (UnitObject unitObject : humanObj.stageObj.getUnitObjs().values()) {
			if(unitObject.isDie()) {
				continue;
			}
			if(!unitObject.isMonsterObj()) {
				continue;
			}
 			if(unitObject.index < 0) {
				unitObject.teamBundleID = humanObj.teamBundleID;
			}
		}
		
		GeneralManager.inst().addBattleAll(humanObj, true);
		
		//判断地图是否可以进武将
		if(humanObj.stageObj.conf.generalShow == 1) {
			//出战的武将启动光环
			for (UnitObject unitObj : humanObj.salvesAttingList) {
				//所有的武将怒气改为0 
				if(unitObj == null) continue;
				
				//所有的武将添加光环
				SkillParam skillParam = new SkillParam();
				skillParam.tarUo = unitObj;
				skillParam.tarPos = unitObj.posNow;
				for (SkillCommon skill : unitObj.skills.values()) {
					if(skill.confSkill.enterScene) {
						SkillManager.inst().shakeOrCastSkill(unitObj, skill.confSkill.sn, skillParam);
					}
				}
			}
		}
		
	}
	
	@Listener(value=EventKey.HUMAN_LOGOUT)
	public void onHumanLogout(Param param) {
		HumanObject humanObj = param.get("humanObj");
		hideAll(humanObj);
	}
	
	@Listener(value=EventKey.HUMAN_DATA_LOAD_BEGIN)
	public void loadGeneralData(Param param) {
		//找到对应的所有general列表
		HumanObject humanObj = param.get();

		long humanId = humanObj.id;
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findBy(true, General.tableName, General.K.humanID, humanId);
		prx.listenResult(this::_result_loadGeneralData,  "humanObj", humanObj);
		
		//一次加载事件开始
		Event.fire(EventKey.HUMAN_DATA_LOAD_BEGIN_ONE, humanObj);
		
	}
	
	public void _result_loadGeneralData(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");
		//结果
		List<Record> records = results.get();
		for (Record record : records) {
			General gen = new General(record);
			GeneralObject obj = addToUnit(humanObj, gen);
			LoadHumanDataUtils.loadGeneral(humanObj , obj, "unitPropPlus", null, UnitPropPlus.class, "id", gen.getId());
			LoadHumanDataUtils.loadGeneral(humanObj , obj, "buffs", "type", Buff.class, "idAffect", gen.getId());
		}
		
		//一次加载事件结束
		Event.fire(EventKey.HUMAN_DATA_LOAD_FINISH_ONE, humanObj);
	}
}
