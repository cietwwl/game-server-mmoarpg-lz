package org.gof.demo.worldsrv.shop;

import org.gof.core.support.ManagerBase;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfGoods;
import org.gof.demo.worldsrv.config.ConfMall;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.msg.Msg.SCBuyMallGoods;
import org.gof.demo.worldsrv.msg.Msg.SCMallResult;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;


public class MallManager extends ManagerBase{
	
	/**o
	 * 获取实例
	 * 
	 * @return
	 */
	public static MallManager inst() {
		return inst(MallManager.class);
	}
	
	/**
	 * 打开商城
	 * 
	 * @param humanObj
	 * @param shopType
	 */
	public void openMall(HumanObject humanObj, int shopSn) {
		ConfMall confMall = ConfMall.get(shopSn);
		int[] shelfs = confMall.shelfs;
		SCMallResult.Builder msg = SCMallResult.newBuilder();
		for (int i = 0; i < shelfs.length; i++) {
			msg.addItemList(i);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 购买商品
	 * 
	 * @param humanObj
	 * @param shopId
	 * @param goodsIndex
	 * @param num
	 * @param goodsId
	 */
	public void buyGoods(HumanObject humanObj, int shopSn, int index, int num) {
		// 获得商品ID
		ConfMall confMall = ConfMall.get(shopSn);
		int[] shelfs = confMall.shelfs;
		int goodsId = shelfs[index];
		ConfGoods confGoods = ConfGoods.get(goodsId);
		SCBuyMallGoods.Builder msg = SCBuyMallGoods.newBuilder();
		// 如果背包空间不足
		ReasonResult result = ItemBagManager.inst().canAdd(humanObj, confGoods.itemId, num, 0);
		if (!result.success) {
			Log.shop.info("{}购买商品{}背包空间不足!",humanObj.name);
			msg.setCode(1);
			return;
		}
		// 扣货币
		int sum = confGoods.price * num;
		if (confGoods.sale > 0) {
			sum = sum * confGoods.sale / 10;
		}
		result = HumanManager.inst().canProduceReduce(humanObj, confGoods.costType, sum);
		if (!result.success) {
			Log.shop.info("货币不足，{}购买{}，当前货币{}，需要货币{}",humanObj.name, goodsId, humanObj.getHuman().getCoin());
			msg.setCode(1);
			return;
		}
		HumanManager.inst().produceMoneyReduce(humanObj, confGoods.costType, sum, MoneyReduceLogKey.商城);
		// 进背包
		ItemBagManager.inst().add(humanObj, confGoods.itemId, num, 0);
		msg.setCode(0);
		humanObj.sendMsg(msg);
		
		//加载事件
		Event.fire(EventKey.BUG_GOODS, "humanObj", humanObj);
		//如果是钻石购买
		if(ProduceMoneyKey.gold.getType() == confGoods.costType){
			Event.fire(EventKey.MALL_BUG_GOODS_BY_GOLD, "humanObj", humanObj, "num", sum);
		}
	}
}
