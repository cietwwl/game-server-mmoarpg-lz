package org.gof.demo.worldsrv.treasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfTreasureData;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.item.ItemRandomTools;
import org.gof.demo.worldsrv.msg.Msg.DTreasure;
import org.gof.demo.worldsrv.msg.Msg.SCGatcha;
import org.gof.demo.worldsrv.produce.ProduceManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.shop.ShopManager;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TreasureManager extends ManagerBase {

	public static final int TREASURE_宝藏类型数 = 3;

	public static final int TREASURE_TYPE_初级宝藏 = 1;
	public static final int TREASURE_TYPE_中级宝藏 = 2;
	public static final int TREASURE_TYPE_高级宝藏 = 3;

	public static final int TREASURE_高级宝藏展示个数 = 6;
	public static final int TREASURE_高级宝藏每日热点个数 = 3;

	public static TreasureManager inst() {
		return inst(TreasureManager.class);
	}

	/**
	 * 做一次抽奖。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param tType
	 *            宝藏类型
	 * @return 结果与原因。
	 */
	public ReasonResult gatcha(HumanObject humanObj, int tType) {

		// 判断宝藏类型是否正确
		if (tType != TREASURE_TYPE_初级宝藏 && tType != TREASURE_TYPE_中级宝藏
				&& tType != TREASURE_TYPE_高级宝藏) {
			return new ReasonResult(false, "宝藏单抽失败，错误的宝藏类型" + tType);
		}
		// TODO 判断VIP，高级宝藏只有VIP能抽

		// 取宝藏配置
		ConfTreasureData conf = ConfTreasureData.get(tType);
		int virType = conf.virType;
		int virCount = conf.virCount;

		// 生成临时宝藏逻辑对象
		Treasure treasure = Treasure.treasure(humanObj, tType);

		// 判断宝藏下一抽是否免费
		boolean isFree = treasure.isNextFree();
		// 免费时候不花钱
		if (isFree) {
			virCount = 0;
		} else {
			// 否则判断钱是否够
			ReasonResult canProduceReduce = HumanManager.inst()
					.canProduceReduce(humanObj, virType, virCount);
			if (!canProduceReduce.success) {
				return canProduceReduce;
			}
		}

		// 看这次随出什么物品了
		List<ProduceVo> ag = treasure.nextGatcha();
		
		// 给一个宝藏购买的物品
		ag.add(new ProduceVo(conf.itemCode, 1));

		// 判断背包容量
		ReasonResult canAdd = ProduceManager.inst().canGiveProduceItem(humanObj, ag);
		if (!canAdd.success) {
			return canAdd;
		}

		// 非免费时要扣钱
		if (!isFree) {
			HumanManager.inst().produceMoneyReduce(humanObj, virType, virCount,
					MoneyReduceLogKey.抽奖);
		}
		// 入包
		ProduceManager.inst().giveProduceItem(humanObj, ag, MoneyAddLogKey.宝藏抽奖);

		// 记录抽奖
		if (isFree) {
			treasure.noteFreeGatcha();
		}
		treasure.noteGatcha();

		// 告知客户端结果
		// 买的那个物品不需要告诉他们，删掉
		ag.remove(ag.size() - 1);
		
		SCGatcha.Builder builder = SCGatcha.newBuilder();
		builder.setTreasure(treasure.getMsg());
		// 发送高级宝藏时候，为了表现出“一个高级+5个垃圾的效果”，特殊处理一下
		__setBuilderItems(tType, ag, builder);
		humanObj.sendMsg(builder);
		// 派发事件
		Event.fire(EventKey.LOTTERY_SELECT, "count", 1, "humanObj", humanObj);
		Event.fire(EventKey.OPEN_TEMP_SHOP, "humanObj", humanObj, "openCondition", ShopManager.临时解锁条件_完成抽奖);
		Log.treasure.info(humanObj.getHuman().getName() + "\t宝藏单抽成功\t"
				+ builder.getItemAndNumList());

		return new ReasonResult(true);
	}

	/**
	 * 单抽的个数赋值。
	 * 
	 * @param tType
	 *            宝藏ID
	 * @param ag
	 *            奖励
	 * @param builder
	 */
	private void __setBuilderItems(int tType, List<ProduceVo> items,
			SCGatcha.Builder builder) {
		Map<Integer, Integer> map = new HashMap<>();
		// 生成物品按SN压缩数量
		items.forEach(itemVO -> {
			map.merge(itemVO.sn, itemVO.num, Integer::sum);
		});
		// 放入builder
		map.forEach((key, value) -> {
			builder.addItemAndNum(key);
			builder.addItemAndNum(value);
		});
		// 当是高级宝藏时，保证它展示六个物品图标
		if (tType == TREASURE_TYPE_高级宝藏) {

			int itemAndNumCount = builder.getItemAndNumCount();
			if (itemAndNumCount > 0 && itemAndNumCount < TREASURE_高级宝藏展示个数 * 2) {
				List<Integer> hotItemsList = builder.getTreasure()
						.getHotItemsList();
				// 做法就是把叠加的分出几个凑成六个
				int transPos = 0;
				while (itemAndNumCount < TREASURE_高级宝藏展示个数 * 2
						&& transPos < itemAndNumCount) {
					int itemCode = builder.getItemAndNum(transPos);
					int itemNum = builder.getItemAndNum(transPos + 1);
					if (hotItemsList.contains(itemCode)) {
						transPos = transPos + 2;
					} else {
						if (itemNum > 1) {
							builder.setItemAndNum(transPos + 1, itemNum - 1);
							builder.addItemAndNum(itemCode);
							builder.addItemAndNum(1);
							itemAndNumCount += 2;
						} else {
							transPos = transPos + 2;
						}
					}
				}
			}
		}
	}

	/**
	 * 做一次10连抽。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param tType
	 *            宝藏类型
	 * @return 结果与原因。
	 */
	public ReasonResult gatcha10(HumanObject humanObj, int tType) {
		// 只能是初级、中级宝藏
		if (tType != TREASURE_TYPE_初级宝藏 && tType != TREASURE_TYPE_中级宝藏) {
			return new ReasonResult(false, "宝藏10连抽失败，错误的宝藏类型" + tType);
		}
		// 取配置
		ConfTreasureData conf = ConfTreasureData.get(tType);
		int virType = conf.virType;
		int multiCount = ConfGlobalUtils.getValue(ConfGlobalKey.连抽次数);
		// 连抽有个折扣，得算一下到底花多少钱
		float batchSale = 1.0f * ConfGlobalUtils.getValue(ConfGlobalKey.连抽折扣) / 10000;
		int batchCost = (int) Math.ceil(batchSale * conf.virCount * multiCount);
		// 判断钱够不够
		ReasonResult canProduceReduce = HumanManager.inst().canProduceReduce(
				humanObj, virType, batchCost);
		if (!canProduceReduce.success) {
			return canProduceReduce;
		}

		// 记录要生成的物品
		List<ProduceVo> toGive = new ArrayList<>();

		// 生成宝藏对象
		Treasure treasure = Treasure.treasure(humanObj, tType);
		// 随机连抽物品
		List<List<ProduceVo>> aglist = treasure.nextMultiGatcha(multiCount);
		// 随出的物品加入入包列表
		aglist.forEach(toGive::addAll);
		// 再给若干个购买物品
		toGive.add(new ProduceVo(conf.itemCode, multiCount));

		// 判断背包容量
		ReasonResult canAdd = ProduceManager.inst().canGiveProduceItem(humanObj, toGive);
		if (!canAdd.success) {
			return canAdd;
		}
		// 扣钱
		HumanManager.inst().produceMoneyReduce(humanObj, virType, batchCost,
				MoneyReduceLogKey.抽奖);
		// 加物品
		ProduceManager.inst().giveProduceItem(humanObj, toGive, MoneyAddLogKey.宝藏抽奖);

		// 记录抽奖
		treasure.noteMultiGatcha(multiCount);
		
		// 告知客户端
		SCGatcha.Builder builder = SCGatcha.newBuilder();
		Map<Integer, Integer> map = new HashMap<>();
		for (List<ProduceVo> items : aglist) {
			for (ProduceVo item : items) {
				map.merge(item.sn, item.num, Integer::sum);
			}
			map.forEach((key, value) -> {
				builder.addItemAndNum(key);
				builder.addItemAndNum(value);
			});
			map.clear();
		}
		builder.setTreasure(treasure.getMsg());

		humanObj.sendMsg(builder);
		// 派发事件
		Event.fire(EventKey.LOTTERY_SELECT, "count", multiCount, "humanObj",
				humanObj);

		Log.treasure.info(humanObj.getHuman().getName() + "\t宝藏10连抽成功\t"
				+ builder.getItemAndNumList());

		return new ReasonResult(true);
	}

	/**
	 * 刷新每日抽奖信息。
	 * 
	 * @param param
	 */
	@Listener(value = EventKey.HUMAN_RESET_ZERO)
	public void resetTreasureDaily(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();

		// 初级宝藏每日免费次数重置
		String trCounts = human.getTrCounts();
		JSONObject json = Utils.toJSONObject(trCounts);
		json.remove(TreasureManager.TREASURE_TYPE_初级宝藏 + "f");

		// 初级宝藏免费抽奖时间重置
		String freeTrTime = human.getFreeTrTime();
		JSONObject json2 = Utils.toJSONObject(freeTrTime);
		json2.remove(TreasureManager.TREASURE_TYPE_初级宝藏);
		human.setFreeTrTime(json2.toJSONString());

		// 高级宝藏热点碎片重置
		List<Integer> sns = __updateUltimateProduceSn();
		String tr3ItemSn = Utils.toJSONString(sns);
		human.setTr3ItemSn(tr3ItemSn);
	}

	private List<Integer> __updateUltimateProduceSn() {

		int[] randomcode = ConfTreasureData.get(TREASURE_TYPE_高级宝藏).normalGroup;
		// 按理说这个randomcode的长度绝对大于3，但是防止死循环，做个特殊处理
		if (randomcode.length <= 3) {
			Set<Integer> answer = new HashSet<>();
			for (int produceSn : randomcode) {
				answer.add(produceSn);
			}
			return new ArrayList<>(answer);
		} else {
			// 不重复随机
			Map<Integer, Integer> randMap = new HashMap<>();
			for (int produceSn : randomcode) {
				randMap.put(produceSn, 1);
			}
			Set<Integer> answer = new HashSet<>();
			while (answer.size() < TREASURE_高级宝藏每日热点个数) {
				int randomSn = ItemRandomTools.randomWithJSONObject(randMap);
				if (!answer.contains(randomSn)) {
					answer.add(randomSn);
					randMap.remove(randomSn);
				}
			}
			return new ArrayList<>(answer);
		}
	}

	/**
	 * 得到玩家高级宝藏热点物品SN。
	 * 
	 * @param humanObj
	 * @return 物品SN。
	 */
	public Integer[] getUltimateHotSn(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		// 这里记录了今日的三个热点随机组
		List<Integer> produceSns = getUltimateProduceSns(human);
		Set<Integer> answer = new HashSet<>();
		for (Integer produceSn : produceSns) {
			answer.addAll(ProduceManager.inst().getPossibleSns(humanObj,
					produceSn));
		}
		return answer.toArray(new Integer[answer.size()]);
	}

	/**
	 * 得到玩家今日高级宝藏随机组SN，没有会生成。
	 * 
	 * @param human
	 * @return 玩家今日高级宝藏随机组SN。
	 */
	public List<Integer> getUltimateProduceSns(Human human) {
		String sns = human.getTr3ItemSn();
		List<Integer> produceSns;
		if (sns.isEmpty() || sns.equals("[]")) {
			// 如果没有，手动生成一次
			produceSns = __updateUltimateProduceSn();
			String tr3ItemSn = Utils.toJSONString(produceSns);
			human.setTr3ItemSn(tr3ItemSn);
		} else {
			// 有则直接转出来用
			JSONArray json = Utils.toJSONArray(sns);
			produceSns = new ArrayList<>();
			for (int i = 0, sz = json.size(); i < sz; i++) {
				produceSns.add(json.getInteger(i));
			}
		}
		return produceSns;
	}

	/**
	 * 获取初始化消息。
	 * 
	 * @param humanObj
	 * @return 初始化消息。
	 */
	public List<DTreasure> getTreasureMsg(HumanObject humanObj) {
		List<DTreasure> l = new ArrayList<>();
		l.add(Treasure.treasure(humanObj, TREASURE_TYPE_初级宝藏).getMsg().build());
		l.add(Treasure.treasure(humanObj, TREASURE_TYPE_中级宝藏).getMsg().build());
		l.add(Treasure.treasure(humanObj, TREASURE_TYPE_高级宝藏).getMsg().build());
		return l;
	}
}
