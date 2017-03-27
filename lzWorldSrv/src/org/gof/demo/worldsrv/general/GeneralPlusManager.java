package org.gof.demo.worldsrv.general;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.battlesrv.support.PropCalcCommon;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfCharacterGeneral;
import org.gof.demo.worldsrv.config.ConfGeneralEquipQualityUp;
import org.gof.demo.worldsrv.config.ConfGeneralGrow;
import org.gof.demo.worldsrv.config.ConfGeneralLvUp;
import org.gof.demo.worldsrv.config.ConfGeneralQualityUp;
import org.gof.demo.worldsrv.config.ConfGeneralStarUp;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.entity.General;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemTypeKey;
import org.gof.demo.worldsrv.item.ItemVO;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralEquipUp;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralExpAdd;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralInfo;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralQualityUp;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralRecruitResult;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralStarUp;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;



public class GeneralPlusManager extends ManagerBase {
	
	public static final int GEN_LEVEL_MAX = 100;			            //伙伴最高级别
	public static final int STAR_LEVEL_MAX = 5;						//伙伴升星最高级别
	public static final int QUALITY_LV_MAX = 15;                    //伙伴品质最高级别
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static GeneralPlusManager inst() {
		return inst(GeneralPlusManager.class);
	}
	
	
	/**
	 * 创建伙伴
	 * @param humanObj
	 * @param sn
	 */
	public void recriuit(HumanObject humanObj, String sn){
		//判断玩家是否拥有这个伙伴
		for(Entry<Long, CharacterObject> entry : humanObj.slaves.entrySet()) {
			GeneralObject generalObj = (GeneralObject)entry.getValue();
			if(generalObj.getGeneral().getSn().equals(sn)) {
				Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("您已经有这个伙伴了"));
				return;
			}
		}	
		
		//获取伙伴配置
		ConfCharacterGeneral cg = ConfCharacterGeneral.get(sn);
		if(cg == null) {
			Inform.user(humanObj.id, Inform.提示操作, "武将配置不存在！");
			return;
		}
		
		//是否可以招募
		if(!cg.canRecriuit){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("该伙伴不可招募"));
			return;
		}

		//判断对应碎片是否足够
		boolean check = cg.isRare?GeneralFragManager.inst().canRareFragReduce(humanObj, cg.needFragNum):
			                           GeneralFragManager.inst().canFragReduce(humanObj, cg.fragSn, cg.needFragNum);	
		if(!check){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("碎片不足"));
			return;
		}	

		long coin = humanObj.getHuman().getCoin();
		long needCoin = cg.needCoin;
		//银两是否足够
		if(coin < needCoin){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("银两不足"));
			return;
		}

		//生成伙伴 
		GeneralManager.inst().createAddToHuman(humanObj, Port.applyId(), sn);
		
		//扣除银两
		ProduceMoneyKey cKey = ProduceMoneyKey.coin;
		HumanManager.inst().produceMoneyReduce(humanObj, cKey, needCoin, MoneyReduceLogKey.伙伴招募);
		
		//扣除对应碎片
		if(cg.isRare){
			GeneralFragManager.inst().rareFragReduce(humanObj, cg.needFragNum);
		}else{
			GeneralFragManager.inst().fragReduce(humanObj, cg.fragSn, cg.needFragNum);
		}	
		
		//发送招募成功通知
		SCGeneralRecruitResult.Builder msg = SCGeneralRecruitResult.newBuilder();
		msg.setSn(sn);
		msg.setResult(true);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 玩家消费伙伴经验丹获得伙伴经验
	 * @param humanObj
	 * @param genID
	 * @param exp
	 */
	public void genExpAddWithProp(HumanObject humanObj, long genID, int propSn, int PropNum){
		//获得伙伴的具体信息
		GeneralObject GenObj = (GeneralObject) humanObj.slaves.get(genID);
		if(GenObj == null) {
			Inform.user(humanObj.id, Inform.提示操作, "武将不存在！");
			return;
		}
		
		General gen = GenObj.getGeneral();
		
		//伙伴等级不能高于角色等级
		if(gen.getLevel() >= humanObj.getHuman().getLevel()){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("伙伴等级不能高于角色等级"));
			return;
		}
		
		//经验丹道具配置
		ConfItemData cd = ConfItemData.get(propSn);
		if (cd.bType != ItemTypeKey.道具 || cd.sType != ItemTypeKey.IT_道具_伙伴经验丹) {
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("该道具物品不是伙伴经验丹"));
			return;			
		}

		//满级了
		if(gen.getLevel() >= GEN_LEVEL_MAX){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("伙伴已满级"));
			return;
		}
		
		//判断经验丹是否足够
		ReasonResult rr = ItemBagManager.inst().canRemove(humanObj, propSn, PropNum);
		if(!rr.success){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("经验丹不足"));
			return;
		}	
		
		//经验值
		long exp = cd.value[0] * PropNum;
		//加经验逻辑
		generalExpAdd(humanObj, GenObj, exp);		
		
		//扣除经验丹
		ItemBagManager.inst().remove(humanObj, propSn, PropNum);
		
		//result
		SCGeneralExpAdd.Builder msg = SCGeneralExpAdd.newBuilder();
		msg.setResult(true);
		msg.setExp((int)exp);
		humanObj.sendMsg(msg);
		
		//加载事件
		Event.fire(EventKey.PROP_ADD_EXP, "generalObj", GenObj, "humanObj", humanObj);
	}
	
	/**
	 * 玩家打副本、获得奖励等   方式为伙伴获得经验
	 * @param humanObj
	 * @param genID
	 * @param exp
	 */
	public void genExpAddDoTask(HumanObject humanObj, long genID, long exp){
		//获得伙伴的具体信息
		GeneralObject GenObj = (GeneralObject) humanObj.slaves.get(genID);
		General gen = GenObj.getGeneral();
		
		//满级了
		if(gen.getLevel() >= GEN_LEVEL_MAX){
			return;
		}
		
		//伙伴等级不能高于角色等级
		if(gen.getLevel() >= humanObj.getHuman().getLevel()){
			return;
		}
		
		//加经验逻辑
		generalExpAdd(humanObj, GenObj, exp);
	}

	/**
	 * 给伙伴加经验值
	 * @param humanObj
	 * @param genID
	 */
	private void generalExpAdd(HumanObject humanObj,GeneralObject GenObj, long exp){
		//获得伙伴的具体信息
		General gen = GenObj.getGeneral();
		
		//加经验
		gen.setExpCur(gen.getExpCur() + exp);
		
		boolean levelUp = false;
		while(gen.getExpCur() >= gen.getExpUpgrade()){
			
			gen.setExpCur(gen.getExpCur() - gen.getExpUpgrade());
			
			gen.setLevel(gen.getLevel() + 1);
			
			ConfGeneralLvUp cglu = ConfGeneralLvUp.get(gen.getLevel());
			if(cglu == null){
				continue;
			}
			
			gen.setExpUpgrade(cglu.exp);
			
			levelUp = true;
		}
		
		if(levelUp){
			//计算属性加成
			calcGeneralLvUpProp(GenObj);
		}
		
		//给前端发送属性变化
		SCGeneralInfo.Builder msgSend = SCGeneralInfo.newBuilder();
		msgSend.setUnit(GeneralManager.inst().createDGeneralInfo(GenObj));
		humanObj.sendMsg(msgSend);
		
		//加载事件
		Event.fire(EventKey.GENERAL_UPGRADE, "generalObj", GenObj, "humanObj", humanObj);
	}
	
	/**
	 * 伙伴星级升级
	 * @param humanObj
	 * @param genID
	 */
	public void generalStarUp(HumanObject humanObj, long genID){
		//获得伙伴的具体信息
		GeneralObject GenObj = (GeneralObject) humanObj.slaves.get(genID);
		if(GenObj == null) {
			Inform.user(humanObj.id, Inform.提示操作, "武将不存在！");
			return;
		}
		
		General gen = GenObj.getGeneral();
		
		//伙伴配置
		ConfCharacterGeneral cg = ConfCharacterGeneral.get(gen.getSn());
		if(cg.isRare){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("该伙伴不可以升星级"));
			return;
		}
		
		//伙伴升星级配置
		ConfGeneralStarUp cgsu = ConfGeneralStarUp.getBy(ConfGeneralStarUp.K.genSn, gen.getSn(), ConfGeneralStarUp.K.starSn, gen.getStar());
		if(cgsu == null){
			Inform.user(humanObj.id, Inform.提示操作, "伙伴星级升级配置不存在！");
			return;
		}
		
		ConfGeneralStarUp cgsuNext = ConfGeneralStarUp.getBy(ConfGeneralStarUp.K.genSn, gen.getSn(), ConfGeneralStarUp.K.starSn, gen.getStar() + 1);
		//满星判断
		if(gen.getStar() >= STAR_LEVEL_MAX || cgsuNext == null){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("该伙伴已达满星"));
			return;
		}
		
		long coin = humanObj.getHuman().getCoin();
		long needCoin = cgsu.needCoin;
		//银两是否足够
		if(coin < needCoin){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("银两不足"));
			return;
		}
	
		//判断对应碎片是否足够
		boolean check = GeneralFragManager.inst().canFragReduce(humanObj, cg.fragSn, cgsu.needFragNum);	
		if(!check){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("碎片不足"));
			return;
		}	
		
		//伙伴星级升级
		gen.setStar(gen.getStar() + 1);
		
		//加技能
		if(cgsuNext.skillSn != 0){
		    SkillManager.inst().addSkill(GenObj, cgsuNext.skillSn, 1, 0);
		}
		
		//计算属性加成
		calcGeneralStarUpProp(GenObj);
		
		//扣除对应碎片
		GeneralFragManager.inst().fragReduce(humanObj, cg.fragSn, cgsu.needFragNum);
		
		if(gen.getStar() >= STAR_LEVEL_MAX){
			GeneralFragManager.inst().copyToRareFrag(humanObj, cg.fragSn);
		}
			
		//扣除银两
		ProduceMoneyKey cKey = ProduceMoneyKey.coin;
		HumanManager.inst().produceMoneyReduce(humanObj, cKey, needCoin, MoneyReduceLogKey.伙伴升星);
		
		//给前端发送属性变化
		SCGeneralInfo.Builder msgSend = SCGeneralInfo.newBuilder();
		msgSend.setUnit(GeneralManager.inst().createDGeneralInfo(GenObj));
		humanObj.sendMsg(msgSend);
		
		//结果
		SCGeneralStarUp.Builder msg = SCGeneralStarUp.newBuilder();
		msg.setResult(true);
		msg.setSn(gen.getSn());
		msg.setStar(gen.getStar());
		humanObj.sendMsg(msg);
		
		//加载事件
		Event.fire(EventKey.GENERAL_STAR_LEVELUP, "generalObj", GenObj, "humanObj", humanObj);
	}
	
	/**
	 * 伙伴品质升级
	 * @param humanObj
	 * @param genID
	 */
	public void generalQualityUp(HumanObject humanObj, long genID){
		//获得伙伴的具体信息
		GeneralObject GenObj = (GeneralObject) humanObj.slaves.get(genID);
		if(GenObj == null) {
			Inform.user(humanObj.id, Inform.提示操作, "武将不存在！");
			return;
		}
		
		General gen = GenObj.getGeneral();	
		
		//配置
		ConfGeneralQualityUp cgqu = ConfGeneralQualityUp.getBy(ConfGeneralQualityUp.K.genSn, gen.getSn(), ConfGeneralQualityUp.K.qualitySn, gen.getQuality());
		if(cgqu == null){
			Inform.user(humanObj.id, Inform.提示操作, "伙伴品质升级配置不存在！");
			return;
		}
		
		//检查装备品质
		GeneralEquipment ge = new GeneralEquipment(gen.getEquip());
		if(!ge.checkEquipQuality(gen.getQuality() + 1)){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("装备级别不足"));
			return;
		}
		
		//伙伴等级判断
		if(gen.getLevel() < cgqu.needLevel){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("伙伴等级不足"));
			return;			
		}

		ConfGeneralQualityUp cgquNext = ConfGeneralQualityUp.getBy(ConfGeneralQualityUp.K.genSn, gen.getSn(), ConfGeneralQualityUp.K.qualitySn, gen.getQuality() + 1);
		
		//品质升满判断
		if(gen.getQuality() >= QUALITY_LV_MAX || cgquNext == null){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("已达最高品质"));
			return;
		}
		
		long coin = humanObj.getHuman().getCoin();
		//银两是否足够
		if(coin < cgqu.needCoin){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("银两不足"));
			return;
		}
	
		//伙伴品质升级
		gen.setQuality(gen.getQuality() + 1);	
		
		//加技能
		if(cgquNext.skillSn != 0){
		    SkillManager.inst().addSkill(GenObj, cgquNext.skillSn, 1, 0);
		}
		
		//升级技能
		if(cgquNext.canUpSkillSn.length > 0){
			for(int skillSn : cgquNext.canUpSkillSn){
				SkillManager.inst().addSkill(GenObj, skillSn);
			}
		}

		//计算属性加成
		calcGeneralQulityUpProp(GenObj);
		
		//扣除银两
		ProduceMoneyKey cKey = ProduceMoneyKey.coin;
		HumanManager.inst().produceMoneyReduce(humanObj, cKey, cgqu.needCoin, MoneyReduceLogKey.伙伴升品质);		
		
		
		//加载事件
		Event.fire(EventKey.GENERAL_QUILITY_LEVELUP, "generalObj", GenObj, "humanObj", humanObj);
		
		//给前端发送属性变化
		SCGeneralInfo.Builder msgSend = SCGeneralInfo.newBuilder();
		msgSend.setUnit(GeneralManager.inst().createDGeneralInfo(GenObj));
		humanObj.sendMsg(msgSend);	
		
		//结果
		SCGeneralQualityUp.Builder msg = SCGeneralQualityUp.newBuilder();
		msg.setResult(true);
		msg.setSn("1");
		humanObj.sendMsg(msg);

	}
	
	/**
	 * 伙伴装备品质升级
	 * @param humanObj
	 * @param equipSn
	 * @param genID
	 */
	public void generalEquipQualityUp(HumanObject humanObj, long genID, int equipSn){
		//获得伙伴的具体信息
		GeneralObject GenObj = (GeneralObject) humanObj.slaves.get(genID);
		if(GenObj == null) {
			Inform.user(humanObj.id, Inform.提示操作, "武将不存在！");
			return;
		}
		
		//检查配置
		ConfGeneralEquipQualityUp cgeu = ConfGeneralEquipQualityUp.get(equipSn);
		if(cgeu == null){
			Inform.user(humanObj.id, Inform.提示操作, "装备品质升级配置不存在！");
			return;
		}
		
		General gen = GenObj.getGeneral();		
		
		GeneralEquipment ge = new GeneralEquipment(gen.getEquip());
		int eql = ge.getEquipQualityLv(equipSn);
		//品质升满判断
		if(eql >= QUALITY_LV_MAX || cgeu.nextEquipSn == 0){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("已达最高品质"));
			return;
		}		
		
		long coin = humanObj.getHuman().getCoin();
		//银两是否足够
		if(coin < cgeu.needCoin){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("银两不足"));
			return;
		}
		
		//升级需要的道具
		int[] needItems = cgeu.needItems;
		int[] needNum = cgeu.needNum;
		
		//需要的道具数量判断
		List<ItemVO> itemvos = new ArrayList<>();
		for(int i=0; i<needItems.length; i++){
			itemvos.add(new ItemVO(needItems[i], needNum[i]));
		}
		ReasonResult rr = ItemBagManager.inst().canRemove(humanObj, itemvos);
		if(!rr.success){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("道具数量不足"));
			return;
		}			
				
		//装备品质升级
		ge.repalceEquipQualityLv(equipSn, cgeu.nextEquipSn);		
		gen.setEquip(ge.equipInfoJson());
		
		//扣除道具
		ItemBagManager.inst().remove(humanObj, itemvos);
		
		//扣除银两
		ProduceMoneyKey cKey = ProduceMoneyKey.coin;
		HumanManager.inst().produceMoneyReduce(humanObj, cKey, cgeu.needCoin, MoneyReduceLogKey.伙伴装备升品质);	
		
		//给前端发送属性变化
		SCGeneralInfo.Builder msgSend = SCGeneralInfo.newBuilder();
		msgSend.setUnit(GeneralManager.inst().createDGeneralInfo(GenObj));
		humanObj.sendMsg(msgSend);	
		
		//result
		SCGeneralEquipUp.Builder msg = SCGeneralEquipUp.newBuilder();
		msg.setResult(true);
		humanObj.sendMsg(msg);	
	}
	
	/**
	 * 伙伴升级加成计算
	 * @param generalObj
	 */
	public void calcGeneralLvUpProp(GeneralObject generalObj){
		//获取配置
		ConfGeneralGrow confGrow = ConfGeneralGrow.get(generalObj.getGeneral().getSn());
		if(confGrow == null){
			Inform.user(generalObj.getHumanId(), Inform.提示操作, Utils.createStr("伙伴属性成长配置不存在"));
			return;
		}
		
		if(confGrow.properties.length != confGrow.value.length){
			Inform.user(generalObj.getHumanId(), Inform.提示操作, Utils.createStr("GeneralGrow配置错误"));
			return;	
		}
		
		//计算级别加成
		PropCalcCommon prop = new PropCalcCommon();
		
		int level = generalObj.getGeneral().getLevel();
		for (int i = 0; i < level; i++) {			
			prop.plus(Utils.toJOSNString(confGrow.properties, confGrow.value));
		}
		
		//加入到伙伴中
		generalObj.dataPers.unitPropPlus.setLevel(prop.toJSONStr());		
	}
	
	/**
	 * 伙伴生星级属性加成计算
	 * @param generalObj
	 */
	public void calcGeneralStarUpProp(GeneralObject generalObj){
		//获取配置
		List<ConfGeneralStarUp> starList = new ArrayList<ConfGeneralStarUp>();
		starList.addAll(ConfGeneralStarUp.findBy(ConfGeneralStarUp.K.genSn, generalObj.getGeneral().getSn()));
		
		//计算属性加成
		PropCalc prop = new PropCalc();
		int statLv = generalObj.getGeneral().getStar();
		
		for(ConfGeneralStarUp conf : starList){
			if(conf.starSn > statLv){
				continue;
			}
			
			if(conf.properties.length != conf.value.length){
				Inform.user(generalObj.getHumanId(), Inform.提示操作, Utils.createStr("伙伴升星级配置错误"));
				return;	
			}
			
			prop.plus(Utils.toJOSNString(conf.properties, conf.value));
		}
		
		//加入到伙伴中
		generalObj.dataPers.unitPropPlus.setLevel(prop.toJSONStr());			
	}
	
	/**
	 * 伙伴升级品质加成计算
	 * @param generalObj
	 */
	public void calcGeneralQulityUpProp(GeneralObject generalObj){
		//获取配置
		List<ConfGeneralQualityUp> quList = new ArrayList<ConfGeneralQualityUp>();
		quList.addAll(ConfGeneralQualityUp.findBy(ConfGeneralQualityUp.K.genSn, generalObj.getGeneral().getSn()));
		
		//计算属性加成
		PropCalc prop = new PropCalc();
		int qulityLv = generalObj.getGeneral().getQuality();
		
		for(ConfGeneralQualityUp conf : quList){
			if(conf.qualitySn > qulityLv){
				continue;
			}
			
			if(conf.properties.length != conf.value.length){
				Inform.user(generalObj.getHumanId(), Inform.提示操作, Utils.createStr("伙伴升品质配置错误"));
				return;	
			}
			
			prop.plus(Utils.toJOSNString(conf.properties, conf.value));
		}
		
		//加入到伙伴中
		generalObj.dataPers.unitPropPlus.setLevel(prop.toJSONStr());				
	}
	
	/**
	 * 给上阵伙伴加经验
	 * @param humanObj
	 * @param num
	 */
	public void addAttingGeneralExp(HumanObject humanObj,int num){
		for(long genId : humanObj.slavesAttingMap.keySet()){
		    genExpAddDoTask(humanObj, genId, num);
		}
	}
	
	//-------------------------------------------old code below----------------------------------------------
	
//	
//	/**
//	 * 根据伙伴，计算级别数值
//	 * @param generalObj
//	 */
//	public void calcGeneralLevelProp(GeneralObject generalObj) {
//		
//		//获取模板
//		ConfCharacterGeneral confGen = ConfCharacterGeneral.get(generalObj.getGeneral().getSn());
//		ConfGeneralGrow confGrow = ConfGeneralGrow.getBy(ConfGeneralGrow.K.sn, confGen.growLevelSn);
//		
//		//取出级别星级加成，计算干扰值
//		float ficter = 1.0F;
//		float[] value = confGrow.value;
//		
//		//计算级别加成
//		PropCalcCommon prop = new PropCalcCommon();
//		int level = generalObj.getGeneral().getLevel();
//		for (int i = 0; i < level; i++) {
//			//重新计算级别加成
//			float[] newValue = new float[value.length];
//			for (int j = 0; j < value.length; j++) {
//				newValue[j] = value[j] * ficter;
//			}
//			
//			prop.plus(Utils.toJOSNString(confGrow.properties, newValue));
//		}
//		
//		//加入到伙伴中
//		generalObj.dataPers.unitPropPlus.setLevel(prop.toJSONStr());
//	}
	
	/**
	 * 用户升级事件监听
	 * @param param
	 */
	@Listener(EventKey.HUMAN_UPGRADE)
	public void onHumanUpgrade(Param param) {
//		HumanObject humanObj = param.get("humanObj");
//		int level = humanObj.getHuman().getLevel();
//		
//		//挨个遍历伙伴，设置成长值
//		for (Entry<Long, CharacterObject> e : humanObj.slaves.entrySet()) {
//			GeneralObject go = (GeneralObject)e.getValue();
//			go.getGeneral().setLevel(level);
//			
//			//根据级别拼装属性
//			calcGeneralLevelProp(go);
//			
//			//重新计算伙伴属性
//			UnitManager.inst().propCalc(go);
//			
//			//给前端发送属性变化
//			SCGeneralInfo.Builder msgSend = SCGeneralInfo.newBuilder();
//			msgSend.setUnit(GeneralManager.inst().createDGeneralInfo(go));
//			humanObj.sendMsg(msgSend);
//		}
	}
	
	/**
	 * 监听伙伴创建时间，处理级别属性
	 * @param param
	 */
	@Listener(EventKey.GENERAL_CREATE)
	public void onGeneralCreate(Param param) {
//		GeneralObject generalObj = param.get("generalObj");
//		calcGeneralLevelProp(generalObj);
	}
}
