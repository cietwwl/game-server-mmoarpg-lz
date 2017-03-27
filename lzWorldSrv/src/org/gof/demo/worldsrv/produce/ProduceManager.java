package org.gof.demo.worldsrv.produce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.RandomUtils;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.config.ConfProduce;
import org.gof.demo.worldsrv.config.ConfProduceWeight;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.general.GeneralPlusManager;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemRandomTools;
import org.gof.demo.worldsrv.item.ItemVO;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;

import com.alibaba.fastjson.JSONObject;

public class ProduceManager extends ManagerBase {

	public static int RANDOMTOTAL = 10000; //随机使用的最大数量

	public static int ALL_PROFESSION = 15; // zhangbo：职业随机时，填15的表示全职业（15这个数和物品配置中的职业统一）
	
	public static ProduceManager inst() {
		return inst(ProduceManager.class);
	}
	
	/**
	 * 从 produce 配置中获得物品
	 * @param humanObj
	 * @param sn
	 * @return
	 */
	public List<ProduceVo> produceItem(HumanObject humanObj, int sn) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		List<ConfProduce> confPros = ConfProduce.findBy(ConfProduce.K.produceSn, sn);
		Human human = humanObj.getHuman();
		ConfProduce confReal = null;
		
		//根据等级判断获得正确的配置
		for (ConfProduce confProduce : confPros) {
			// 等级筛选
			if(confProduce.levelMin <= human.getLevel() && human.getLevel() <= confProduce.levelMax) {
				// zhangbo：加了职业筛选
				if (confProduce.profession == ALL_PROFESSION // 全职业
						|| confProduce.profession == human.getProfession()) { // 匹配职业
					confReal = confProduce;
					break;
				}
			}
		}
		
		if(confReal == null) {
			return result;
		}
		
		//获得概率出现的东西
		if(confReal.itemList != null && confReal.itemList.length > 0) {
			int length = confReal.itemList.length;
			int rand = 0;
			for(int i = 0 ; i < length ; i++) {
			    rand = RandomUtils.nextInt(RANDOMTOTAL);
				if(rand < confReal.itemListProp[i]) {
					result.add(new ProduceVo(confReal.itemList[i], confReal.itemListNum[i]));
				}
					
			}
		}
		
		//获得概率权重出现的东西
		if(confReal.itemPackage != null && confReal.itemPackage.length > 0 && confReal.itemPackageMaxNum > 0 && confReal.itemPackageMinNum >= 0) {
			//获得实际的包个数
			int num = confReal.itemPackageMinNum;
			if(confReal.itemPackageMaxNum > confReal.itemPackageMinNum) {
				num = RandomUtils.nextInt(confReal.itemPackageMaxNum - confReal.itemPackageMinNum + 1);
				num += confReal.itemPackageMinNum;
			} 
			
			for(int i = 0 ; i < num ; i++) {
				//通过权重判断从
				List<ProduceWeightVo> weightList = new ArrayList<ProduceWeightVo>();
					int length = confReal.itemPackage.length;
					//便利所有的包 把所有的可能物品添加到map中
					for(int j = 0 ; j < length ; j++) {
						//从 配置中取得具体weight 包的内容
						ConfProduceWeight confWeight = ConfProduceWeight.get(confReal.itemPackage[j]);
						if(confWeight.itemList != null && confWeight.itemList.length > 0) {
							int weightLength = confWeight.itemList.length;
							for(int k = 0 ; k < weightLength ; k++) {
								weightList.add(new ProduceWeightVo(confWeight.itemList[k],
										confWeight.itemListNum[k], confWeight.itemListWeight[k]));
							}
						}
					}
				//开始随机
				ProduceVo item = ItemRandomTools.randomWithJSONObject(weightList);
				if(item != null) {
					result.add(item);
				}
				
			}
		}
		
		return result;
		
	}

	/**
	 * 从 produce 配置中获得物品
	 * <p>
	 * 同{@link #produceItem(HumanObject, int)}比，在权重掉落的实现上，加了“不重复”随机的代码。
	 * <p>
	 * “不重复”随机：掉出一样来后，尽可能掉别的东西，来源于段绪写的代码，都随出一遍后，可以重复掉。
	 * <p>
	 * 但我一直有个疑问，这种“不重复”的处理，绝对改变了物品的权重，所以我只是在第一轮中进行不重复，之后按权重来。
	 * 
	 * @param humanObj
	 * @param sn
	 * @return
	 * @author zhangbo
	 */
	public List<ProduceVo> produceItem2(HumanObject humanObj, int sn) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		List<ConfProduce> confPros = ConfProduce.findBy(ConfProduce.K.produceSn, sn);
		Human human = humanObj.getHuman();
		ConfProduce confReal = null;
		
		//根据等级判断获得正确的配置
		for (ConfProduce confProduce : confPros) {
			// 等级筛选
			if(confProduce.levelMin <= human.getLevel() && human.getLevel() <= confProduce.levelMax) {
				// zhangbo：加了职业筛选
				if (confProduce.profession == ALL_PROFESSION // 全职业
						|| confProduce.profession == human.getProfession()) { // 匹配职业
					confReal = confProduce;
					break;
				}
			}
		}
		
		if(confReal == null) {
			return result;
		}
		
		//获得概率出现的东西
		if(confReal.itemList != null && confReal.itemList.length > 0) {
			int length = confReal.itemList.length;
			int rand = 0;
			for(int i = 0 ; i < length ; i++) {
			    rand = RandomUtils.nextInt(RANDOMTOTAL);
				if(rand < confReal.itemListProp[i]) {
					result.add(new ProduceVo(confReal.itemList[i], confReal.itemListNum[i]));
				}
					
			}
		}
		
		//获得概率权重出现的东西
		if(confReal.itemPackage != null && confReal.itemPackage.length > 0 && confReal.itemPackageMaxNum > 0 && confReal.itemPackageMinNum >= 0) {
			//获得实际的包个数
			int num = confReal.itemPackageMinNum;
			if(confReal.itemPackageMaxNum > confReal.itemPackageMinNum) {
				num = RandomUtils.nextInt(confReal.itemPackageMaxNum - confReal.itemPackageMinNum + 1);
				num += confReal.itemPackageMinNum;
			} 
		
			//权重计算
			List<ProduceWeightVo> weightList = new ArrayList<>();
			int length = confReal.itemPackage.length;
			//便利所有的包 把所有的可能物品添加到map中
			for(int j = 0 ; j < length ; j++) {
				//从 配置中取得具体weight 包的内容
				ConfProduceWeight confWeight = ConfProduceWeight.get(confReal.itemPackage[j]);
				if(confWeight.itemList != null && confWeight.itemList.length > 0) {
					int weightLength = confWeight.itemList.length;
					for(int k = 0 ; k < weightLength ; k++) {
						weightList.add(new ProduceWeightVo(confWeight.itemList[k],
								confWeight.itemListNum[k], confWeight.itemListWeight[k]));
					}
				}
			}
			
			List<ProduceWeightVo> weightListCopy = new ArrayList<>(weightList);
			int weightListSize = weightList.size();
			for (int i = 0; i < num; i++) {
				
				// zhangbo：这里第一轮做个不重复随机处理，随完一轮后用权重
				if (confReal.itemPackageRept && i < weightListSize) {
					// 开始不重复随机
					ProduceVo item = ItemRandomTools
							.randomWithJSONObject(weightListCopy);
					if (item != null) {
						result.add(item);
						// 从随机列表删掉这一个东西
						weightListCopy.removeIf(produceWeightVo -> produceWeightVo.sn == item.sn);
					}
				} else {
					// 开始随机
					ProduceVo item = ItemRandomTools
							.randomWithJSONObject(weightList);
					if (item != null) {
						result.add(item);
					}
				}
			}
		}
		
		return result;
		
	}
	
	/**
	 * 从类型数组 以及 数量数组拼装item
	 * @param type
	 * @param num
	 * @return
	 */
	public List<ProduceVo> produceItem(int[] type, int[] num) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if(type != null && type.length > 0) {
			int length = type.length;
			for(int j = 0 ; j < length ; j++) {
				result.add(new ProduceVo(type[j], num[j]));
			}
		}
		return result;
	}
	
	/**
	 * 从 produce 配置中获得物品
	 * @param humanObj
	 * @param sns
	 */
	public List<ProduceVo> produceItem(HumanObject humanObj, int[] sns) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if(sns != null && sns.length > 0) {
			for (int sn : sns) {
				result.addAll(produceItem(humanObj, sn));
			}
		}
		return result;
	}
	
	/**
	 * 实际给物品
	 * @param humanObj
	 * @param itemProduce
	 * @param log
	 */
	public void giveProduceItem(HumanObject humanObj, List<ProduceVo> itemProduce, MoneyAddLogKey log) {
		//封装itemVo list
		List<ItemVO> itemList = new ArrayList<ItemVO>();
		
		for (ProduceVo produceVo : itemProduce) {
			//添加货币
			if(!produceVo.isItem) {
				//伙伴经验特殊处理
				if(produceVo.sn == ProduceMoneyKey.genExpCur.getType()){
					GeneralPlusManager.inst().addAttingGeneralExp(humanObj, produceVo.num);					
				}else{
				    HumanManager.inst().produceMoneyAdd(humanObj, produceVo.sn, produceVo.num, log);
				}
			}
			itemList = ProduceVo.addItemVoList(itemList, produceVo);
		}
		
		//添加物品
		ItemBagManager.inst().add(humanObj, itemList);
		
	}
	
	/**
	 * 判断能不能添加
	 * @param humanObj
	 * @param itemProduce
	 */
	public ReasonResult canGiveProduceItem(HumanObject humanObj, List<ProduceVo> itemProduce) {
		List<ItemVO> itemList = new ArrayList<ItemVO>();
		for (ProduceVo produceVo : itemProduce) {
			itemList = ProduceVo.addItemVoList(itemList, produceVo);
		}
		return ItemBagManager.inst().canAdd(humanObj, itemList);
	}
	
	public void addProduceMoneyKey(HumanObject humanObj) {
		
	}
	
	/**
	 * 直接通过物品包SN 给物品 并返回给的物品
	 * @param humanObj
	 * @param sn
	 * @param log
	 * @return
	 */
	public List<ProduceVo> getAndGiveProduce(HumanObject humanObj, int sn, MoneyAddLogKey log) {
		List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();
		itemProduce = ProduceManager.inst().produceItem(humanObj, sn);
		ProduceManager.inst().giveProduceItem(humanObj, itemProduce, log);
		return itemProduce;
	}
	
	/**
	 * 直接通过物品包SN 给物品 并返回给的物品
	 * @param humanObj
	 * @param sn
	 * @param log
	 * @return
	 */
	public List<ProduceVo> getAndGiveProduce(HumanObject humanObj, int[] sns, MoneyAddLogKey log) {
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		if(sns != null && sns.length > 0) {
			for (int sn : sns) {
				result.addAll(getAndGiveProduce(humanObj, sn, log));
			}
		}
		return result;
	}
	
	/**
	 * 合并物品 主要用于扫荡这样的模块 物品一堆一堆的 。 需要合并
	 * @param itemProduce
	 * @return
	 */
	public List<ProduceVo> mergeProduce(List<ProduceVo> itemProduce) {
		List<ProduceVo> itemProduceItem = new ArrayList<ProduceVo>();
		//物品合并
		Map<Integer, ProduceVo> itemMerge = new HashMap<Integer, ProduceVo>();
		for (ProduceVo produceVo : itemProduce) {
			if(itemMerge.containsKey(produceVo.sn)) {
				ProduceVo temp = itemMerge.get(produceVo.sn);
				temp.num += produceVo.num;
			} else {
				itemMerge.put(produceVo.sn, produceVo);
			}
		}
		for (ProduceVo produceVo : itemMerge.values()) {
			itemProduceItem.add(produceVo);
		}
		return itemProduceItem;
	}
	
	/**
	 * 将produceVo转化为Jon
	 * @param list
	 * @return
	 */
	public String produceToJson(List<ProduceVo> list) {
		Map<String, Integer> map = new HashMap<String, Integer>(); 
		for (ProduceVo produceVo : list) {
			map.put(String.valueOf(produceVo.sn), produceVo.num);
		}
		
		return Utils.toJSONString(map);
	}
	
	/**
	 * 把Json转化为List, Json里面是个Map<物品ID， 物品数量>
	 * @param json
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public List<ProduceVo> jsonToProduceList(String json) {
		//判断非空
		if(StringUtils.isEmpty(json)) 
			return new ArrayList<ProduceVo>();
		
		//提取Map
		Map<Integer, Integer> map= JSONObject.parseObject(json, Map.class);
		List<ProduceVo> itemList = new ArrayList<ProduceVo>();
		for (Entry<Integer, Integer> e : map.entrySet()) {
			ProduceVo vo = new ProduceVo(Integer.parseInt(e.getKey()+""), e.getValue());
			itemList.add(vo);
		}
		
		return itemList;
	}
	
	/**
	 * 给玩家发送获得物品提示
	 * @param humanObj
	 * @param produceList
	 */
	public void sendInfo (HumanObject humanObj, List<ProduceVo> produceList) {
		
		//提示信息
		String s = "您获得了：";
		for (ProduceVo pvo : produceList) {
			ConfItemData item = ConfItemData.get(pvo.sn); 
			s +=  Utils.createStr("{}*{};", item.name, pvo.num);
		}
		Inform.user(humanObj.id, Inform.提示操作, s);
	}
	
	/**
	 * 得到可能开出的东西SN。
	 * 
	 * @param humanObj
	 * @param produceSn
	 * @return
	 */
	public Set<Integer> getPossibleSns(HumanObject humanObj, int produceSn) {
		Set<Integer> sns = new HashSet<>();

		List<ConfProduce> confPros = ConfProduce.findBy(ConfProduce.K.produceSn, produceSn);
		Human human = humanObj.getHuman();
		ConfProduce confReal = null;
		
		//根据等级判断获得正确的配置
		for (ConfProduce confProduce : confPros) {
			// 等级筛选
			if(confProduce.levelMin <= human.getLevel() && human.getLevel() <= confProduce.levelMax) {
				// 职业筛选
				if (confProduce.profession == ALL_PROFESSION // 全职业
						|| confProduce.profession == human.getProfession()) { // 匹配职业
					confReal = confProduce;
					break;
				}
			}
		}
		
		if(confReal == null) {
			return null;
		}
		
		//获得概率出现的东西
		if(confReal.itemList != null && confReal.itemList.length > 0) {
			int length = confReal.itemList.length;
			for(int i = 0 ; i < length ; i++) {
				int num = confReal.itemListNum[i];
				if (num > 0) {
					sns.add(confReal.itemList[i]);
				}
			}
		}
		
		//获得概率权重出现的东西
		if(confReal.itemPackage != null && confReal.itemPackage.length > 0 && confReal.itemPackageMaxNum > 0 && confReal.itemPackageMinNum >= 0) {
			int length = confReal.itemPackage.length;
			//便利所有的包 把所有的可能物品添加到 sns 中
			for(int j = 0 ; j < length ; j++) {
				//从 配置中取得具体weight 包的内容
				ConfProduceWeight confWeight = ConfProduceWeight.get(confReal.itemPackage[j]);
				if(confWeight.itemList != null && confWeight.itemList.length > 0) {
					int weightLength = confWeight.itemList.length;
					for(int k = 0 ; k < weightLength ; k++) {
						sns.add(confWeight.itemList[k]);
					}
				}
			}
				
		}
		return sns;
		
	}
	
	
	/**
	 * 按最大几率获得物品列表  （简单随机   伙伴副本奖励用）
	 * @param sn
	 * @return
	 */
	public List<ProduceVo> simpleGainByMaxProb(HumanObject humanObj, int sn){
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		List<ConfProduce> confPros = ConfProduce.findBy(ConfProduce.K.produceSn, sn);
		Human human = humanObj.getHuman();
		ConfProduce confReal = null;
		
		//根据等级判断获得正确的配置
		for (ConfProduce confProduce : confPros) {
			// 等级筛选
			if(confProduce.levelMin <= human.getLevel() && human.getLevel() <= confProduce.levelMax) {
				// zhangbo：加了职业筛选
				if (confProduce.profession == ALL_PROFESSION // 全职业
						|| confProduce.profession == human.getProfession()) { // 匹配职业
					confReal = confProduce;
					break;
				}
			}
		}
		
		if(confReal == null) {
			return result;
		}
		
		if(confReal.itemList != null && confReal.itemList.length > 0) {
			int length = confReal.itemList.length;
			for(int i = 0 ; i < length ; i++) {
				result.add(new ProduceVo(confReal.itemList[i], confReal.itemListNum[i]));				
			}
		}
		return result;
	}

	/**
	 * 通过物品包SN 给物品 并返回给的物品    （简单随机   伙伴副本奖励用）
	 * @param humanObj
	 * @param sn
	 * @return
	 */
	public List<ProduceVo> getAndGiveSimpleGain(HumanObject humanObj, int sn){
		List<ProduceVo> result = new ArrayList<ProduceVo>();
		List<ConfProduce> confPros = ConfProduce.findBy(ConfProduce.K.produceSn, sn);
		Human human = humanObj.getHuman();
		ConfProduce confReal = null;
		
		//根据等级判断获得正确的配置
		for (ConfProduce confProduce : confPros) {
			// 等级筛选
			if(confProduce.levelMin <= human.getLevel() && human.getLevel() <= confProduce.levelMax) {
				// zhangbo：加了职业筛选
				if (confProduce.profession == ALL_PROFESSION // 全职业
						|| confProduce.profession == human.getProfession()) { // 匹配职业
					confReal = confProduce;
					break;
				}
			}
		}
		
		if(confReal == null) {
			return result;
		}
		
		//获得概率出现的东西
		if(confReal.itemList != null && confReal.itemList.length > 0) {
			int length = confReal.itemList.length;
			int rand = 0;
			for(int i = 0 ; i < length ; i++) {
			    rand = RandomUtils.nextInt(RANDOMTOTAL);
				if(rand < confReal.itemListProp[i]) {
					result.add(new ProduceVo(confReal.itemList[i], confReal.itemListNum[i]));
				}
					
			}
		}
		
		ProduceManager.inst().giveProduceItem(humanObj, result, MoneyAddLogKey.任务);
		return result;		
	}

	
}
