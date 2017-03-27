package org.gof.demo.worldsrv.item;

import java.util.List;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.msg.Msg.SCBagExpand;
import org.gof.demo.worldsrv.produce.ProduceManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.enumKey.ItemPackTypeKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class ItemUseManager extends ManagerBase {

	public static ItemUseManager inst() {
		return inst(ItemUseManager.class);
	}

	/**
	 * 使用固定礼包。
	 * <p>
	 * zhangbo注：固定礼包其实可以用随机礼包配出来，以后就不要用它了。
	 * 
	 * @param param
	 */
	@Listener(value = EventKey.ITEM_USE, subInt = ItemTypeKey.IT_道具_固定礼包)
	public void useFixedGiftBag(Param param) {

		HumanObject humanObj = param.get("humanObject");
		Item item = param.get("itemBag");
		ConfItemData confItem = param.get("confItem");
		int num = param.getInt("num");

		// 这么配的 ->[钥匙sn或0, 产品sn, num, 产品sn, num ...]
		int[] value = confItem.value;
		int keySn = value[0];
		if (keySn > 0) { // 需要钥匙
			ReasonResult canRemove = ItemBagManager.inst().canRemove(humanObj, keySn, num);
			if (!canRemove.success) {
				return; // 钥匙不足
			}
		}
		// 构成物品
		int len = (value.length - 1) / 2;
		int[] _types = new int[len];
		int[] _nums = new int[len];
		for (int i = 0; i < len; i++) {
			int sn = value[2 * i + 1];
			int snNum = value[2 * (i + 1)] * num;
			_types[i] = sn;
			_nums[i] = snNum;
		}
		List<ProduceVo> produceItem = ProduceManager.inst().produceItem(_types, _nums);
		
		// 判断背包容量
		if (!ProduceManager.inst().canGiveProduceItem(humanObj, produceItem).success) {
			return;
		}
		// 减钥匙
		if (keySn > 0) {
			ItemBagManager.inst().remove(humanObj, keySn, num);
		}
		// 加物品
		ProduceManager.inst().giveProduceItem(humanObj, produceItem, MoneyAddLogKey.礼包);
		item.setNum(item.getNum() - num);
	}

	/**
	 * 使用随机礼包。
	 * 
	 * @param param
	 */
	@Listener(value = EventKey.ITEM_USE, subInt = ItemTypeKey.IT_道具_随机礼包)
	public void useRandomGiftBag(Param param) {

		HumanObject humanObj = param.get("humanObject");
		Item item = param.get("itemBag");
		ConfItemData confItem = param.get("confItem");
		int num = param.getInt("num");

		// 这么配的 ->[钥匙sn或0, 掉落组sn]
		int[] value = confItem.value;
		int keySn = value[0];
		int produceSn = value[1];
		
		if (keySn > 0) { // 需要钥匙
			ReasonResult canRemove = ItemBagManager.inst().canRemove(humanObj, keySn, num);
			if (!canRemove.success) {
				return; // 钥匙不足
			}
		}
		
		// 构成物品
		List<ProduceVo> produceItem;
		// 注：一个宝箱产出物品只随机一次，计入item.param，没有的时候需要随机产出一次
		String produceVoJson = item.getParam();
		if (produceVoJson.isEmpty()) {
			// 这里是随机
			produceItem = ProduceManager.inst().produceItem(humanObj, produceSn);
			produceVoJson = ProduceManager.inst().produceToJson(produceItem);
			item.setParam(produceVoJson);
		} else {
			// 直接用
			produceItem = ProduceManager.inst().jsonToProduceList(produceVoJson);
		}
		// 判断背包容量
		if (!ProduceManager.inst().canGiveProduceItem(humanObj, produceItem).success) {
			return;
		}
		// 减钥匙
		if (keySn > 0) {
			ItemBagManager.inst().remove(humanObj, keySn, num);
		}
		// 加物品
		ProduceManager.inst().giveProduceItem(humanObj, produceItem, MoneyAddLogKey.礼包);
		item.setNum(item.getNum() - num);
	}

	/**
	 * 使用后增加虚拟品。
	 * <p>
	 * zhangbo注：这个也可以用随机礼包配出来，礼包支持虚拟品。
	 * 
	 * @param param
	 */
	@Listener(value = EventKey.ITEM_USE, subInt = {ItemTypeKey.IT_道具_加虚拟品, ItemTypeKey.IT_道具_立即增加虚拟品})
	public void useVirtualItem(Param param) {

		HumanObject humanObj = param.get("humanObject");
		Item item = param.get("itemBag");
		ConfItemData confItem = param.get("confItem");
		int num = param.getInt("num");
		// 这么配的：
		// [sn, num, sn, num ...]
		int[] value = confItem.value;
		for (int i = 0; i < value.length; i += 2) {
			int sn = value[i];
			if(ProduceMoneyKey.getKeyByType(sn) != null) {
				HumanManager.inst().produceMoneyAdd(humanObj, sn, value[i + 1] * num, MoneyAddLogKey.礼包);
			}
		}

		item.setNum(item.getNum() - num);
	}

	/**
	 * 增加背包格。
	 * 
	 * @param param
	 */
	@Listener(value = EventKey.ITEM_USE, subInt = ItemTypeKey.IT_道具_增加背包格子)
	public void useExpandItem(Param param) {

		HumanObject humanObj = param.get("humanObject");
		Human human = humanObj.getHuman();
		Item item = param.get("itemBag");
		int num = param.getInt("num");
		int bagType = param.getInt("target");

		// 取已扩数量
		int oldCap = 0;
		if (bagType == ItemPackTypeKey.Bag1.packType) {
			oldCap = human.getBag1Cap();
		}
		if (bagType == ItemPackTypeKey.Bag2.packType) {
			oldCap = human.getBag2Cap();
		}
		// 设置新容量
		// 一个物品解锁一个格，但是不能超过极限容量
		int maxCap = ConfGlobalUtils.getValue(ConfGlobalKey.背包最大容量);
		int newCap = Math.min(maxCap, oldCap + num);
		if (bagType == ItemPackTypeKey.Bag1.packType) {
			human.setBag1Cap(newCap);
		}
		if (bagType == ItemPackTypeKey.Bag2.packType) {
			human.setBag2Cap(newCap);
		}
		item.setNum(item.getNum() - num);
		
		// 告知客户端
		SCBagExpand.Builder msg = SCBagExpand.newBuilder();
		msg.setCode(0);
		msg.setToIndex(newCap - 1);
		humanObj.sendMsg(msg);
	}
}
