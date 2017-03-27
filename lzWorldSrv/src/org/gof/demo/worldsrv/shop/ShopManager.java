package org.gof.demo.worldsrv.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfGoods;
import org.gof.demo.worldsrv.config.ConfShop;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.msg.Msg.SCBuyShopGoods;
import org.gof.demo.worldsrv.msg.Msg.SCBuyShopResult;
import org.gof.demo.worldsrv.msg.Msg.SCOpenTempShop;
import org.gof.demo.worldsrv.msg.Msg.SCRefreshShopResult;
import org.gof.demo.worldsrv.msg.Msg.SCResultShopTags;
import org.gof.demo.worldsrv.msg.Msg.SCShopResult;
import org.gof.demo.worldsrv.produce.ProduceManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.observer.EventKey;



public class ShopManager extends ManagerBase{
	
	public static final int SHOP_未解锁 = -1;
	public static final int SHOP_临时解锁 = -2;
	public static final int SHOP_永久解锁 = -3;
	public static final int SHOP_永久开启 = -4;
	
	public static final int GOODS_未购买 = 0;
	public static final int GOODS_已购买 = 1;
	
	public static final int 临时解锁条件_完成普通副本 = 1;
	public static final int 临时解锁条件_完成精英副本 = 2;
	public static final int 临时解锁条件_完成抽奖 = 3;
	
	/**o
	 * 获取实例
	 * 
	 * @return
	 */
	public static ShopManager inst() {
		return inst(ShopManager.class);
	}
	
	/**
	 * 初始化商店
	 * 
	 * @param humanObj
	 */
	public void initShop(Human human) {
		List<ConfShop> shopList = new ArrayList<ConfShop>();
		shopList.addAll(ConfShop.findAll());
		List<ShopVO> voList = new ArrayList<ShopVO>();
		for (ConfShop confShop : shopList) {
			ShopVO shopVO = new ShopVO();
			shopVO.sn = confShop.sn;
			shopVO.status = SHOP_未解锁;
			shopVO.openCount = confShop.openCount;
			voList.add(shopVO);
		}
		human.setShopJson(ShopVO.listToJson(voList));
	}
	
	/**
	 * 请求标签
	 * 
	 * @param humanObj
	 */
	public void tagsInfo(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		String shopJson = human.getShopJson();
		List<ShopVO> voList = ShopVO.jsonToList(shopJson);
		List<Integer> results = new ArrayList<Integer>();
		// 遍历所有商店状态
		for (ShopVO vo : voList) {
			ConfShop confShop = ConfShop.get(vo.sn);
			// 如果是未解锁
			if (vo.status == SHOP_未解锁) {
				if (confShop.level != 0 && human.getLevel() >= confShop.level) {
					vo.status = SHOP_永久解锁;
				}
				if (confShop.vip != 0 && human.getVipLevel() >= confShop.vip) {
					vo.status = SHOP_永久解锁;
				}
				if (vo.status == SHOP_永久解锁 && confShop.openCost == 0) {
					vo.status = SHOP_永久开启;
				}
				results.add(vo.status);
			} else if (vo.status == SHOP_临时解锁) {
				if (vo.openTime > Port.getTime()) {
					// 返回剩余时间
					results.add((int)(vo.openTime - Port.getTime()));
				} else {
					// 修改状态
					vo.status = SHOP_未解锁;
					vo.openTime = 0;
					results.add(vo.status);
				}
			} else {
				results.add(vo.status);
			}
		}
		human.setShopJson(ShopVO.listToJson(voList));
		SCResultShopTags.Builder msg = SCResultShopTags.newBuilder();
		for (int i : results) {
			msg.addTags(i);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 通过id取得shop
	 * 
	 * @param voList
	 * @param shopSn
	 */
	public ShopVO returnShop(List<ShopVO> voList, int shopSn) {
		for (ShopVO vo : voList) {
			if (vo.sn == shopSn) {
				return vo;
			}
		}
		return null;
	}
	
	/**
	 * 打开商店
	 * 
	 * @param humanObj
	 * @param shopSn
	 */
	public void openShop(HumanObject humanObj, int shopSn) {
		Human human = humanObj.getHuman();
		String shopJson = human.getShopJson();
		List<ShopVO> voList = ShopVO.jsonToList(shopJson);
		ShopVO shopVO = returnShop(voList, shopSn);
		ConfShop confShop = ConfShop.get(shopSn);
		if (canRefreshShop(shopVO, confShop)) {
			// 刷新商品
			refreshGoods(humanObj, shopVO);
			shopVO.refreshTime = Port.getTime();
			human.setShopJson(ShopVO.listToJson(voList));
		}
		SCShopResult.Builder msg = SCShopResult.newBuilder();
		msg.setShop(shopVO.createMsg());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 自然时间是否刷新商店
	 * 
	 */
	public boolean canRefreshShop(ShopVO shopVO, ConfShop confShop) {
		// 刷新时间点
		int[] refreshTime = confShop.refreshTime;
		// 上次刷新时间
		long lastRefreshTime = shopVO.refreshTime;
		int lastHour = Utils.getHourOfTime(lastRefreshTime);
		int nowHour = Utils.getHourOfTime(Port.getTime());
		// 如果跨天并且大于时间点
		if (!Utils.isSameDay(lastRefreshTime, Port.getTime())) {
			if (lastRefreshTime == 0 || nowHour >= refreshTime[0]) {
				return true;
			}
		} else {
			// 如果同天并且跨过一个刷新时间点
			for (int i = 0; i < refreshTime.length; i++) {
				if (lastHour < refreshTime[i] && nowHour >= refreshTime[i]) {
					return true;
				}
			}
		}
		return false;	
	}
	
	/**
	 * 购买商品
	 * 
	 * @param humanObj
	 * @param shopSn
	 * @param index
	 */
	public void buyGoods(HumanObject humanObj, int shopSn, int index) {
		// 获得商品ID
		Human human = humanObj.getHuman();
		String shopJson = human.getShopJson();
		List<ShopVO> voList = ShopVO.jsonToList(shopJson);
		ShopVO shopVO = returnShop(voList, shopSn);
		GoodsVO goodsVO = shopVO.shelfs.get(index);
		if (goodsVO.status == GOODS_已购买) {
			return;
		}
		
		ConfGoods confGoods = ConfGoods.get(goodsVO.sn);
		SCBuyShopGoods.Builder msg = SCBuyShopGoods.newBuilder();
		// 如果背包空间不足
		ReasonResult result = ItemBagManager.inst().canAdd(humanObj, confGoods.itemId, 1, 0);
		if (!result.success) {
			Log.shop.info("{}购买商品{}背包空间不足!",humanObj.name);
			msg.setCode(1);
			return;
		}
		// 扣货币
		int sum = confGoods.price * 1;
		if (confGoods.sale > 0) {
			sum = sum * confGoods.sale / 10;
		}
		result = HumanManager.inst().canProduceReduce(humanObj, confGoods.costType, sum);
		if (!result.success) {
			Log.shop.info("货币不足，{}购买{}，当前货币{}，需要货币{}",humanObj.name, goodsVO.sn, humanObj.getHuman().getCoin());
			msg.setCode(1);
			return;
		}
		HumanManager.inst().produceMoneyReduce(humanObj, confGoods.costType, sum, MoneyReduceLogKey.神秘商店);
		// 进背包
		ItemBagManager.inst().add(humanObj, confGoods.itemId, 1, 0);
		// 修改购买状态
		goodsVO.status = GOODS_已购买;
//		shopVO.shelfs.set(index, goodsVO);
//		voList.set(shopSn, shopVO);
		human.setShopJson(ShopVO.listToJson(voList));
		
		msg.setCode(0);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 刷新商店
	 * 
	 * @param humanObj
	 * @param shopSn
	 */
	public void refreshShop(HumanObject humanObj, int shopSn) {
		// 获得商品ID
		Human human = humanObj.getHuman();
		String shopJson = human.getShopJson();
		List<ShopVO> voList = ShopVO.jsonToList(shopJson);
		ShopVO shopVO = returnShop(voList, shopSn);
		ConfShop confShop = ConfShop.get(shopSn);
		SCRefreshShopResult.Builder msg1 = SCRefreshShopResult.newBuilder();
		// 修改次数
		if (shopVO.refreshCount <= 0) {
			msg1.setCode(2);
			System.out.println("今日刷新次数用完！");
			return;
		}
		// 扣钱
		int refreshCount = confShop.refreshCount - shopVO.refreshCount;
		int type = confShop.refreshCost[refreshCount*2];
		int num = confShop.refreshCost[refreshCount*2+1];
		ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, type, num);
		if (!result.success) {
			msg1.setCode(1);
			System.out.println("货币不足！");
			return;
		}
		HumanManager.inst().produceMoneyReduce(humanObj, type, num, MoneyReduceLogKey.神秘商店);
		// 扣刷新次数
		shopVO.refreshCount-=1;

		// 刷新商品
		refreshGoods(humanObj, shopVO);
		human.setShopJson(ShopVO.listToJson(voList));
		SCShopResult.Builder msg = SCShopResult.newBuilder();
		msg.setShop(shopVO.createMsg());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 刷新商品
	 * 
	 * @param humanObj
	 * @param shopVO
	 */
	public void refreshGoods(HumanObject humanObj, ShopVO shopVO) {
		// 清空商店
		shopVO.shelfs.clear();
		
		ConfShop confShop = ConfShop.get(shopVO.sn);
		// 获取随机组ID
		int[] produceSn = confShop.shelfs;
		for (int i = 0; i < produceSn.length; i++) {
			List<ProduceVo> goodsProduce = new ArrayList<ProduceVo>();
			goodsProduce = ProduceManager.inst().produceItem(humanObj, produceSn[i]);
			ProduceVo produceVO = goodsProduce.get(0);
			// 得到商品ID
			int goodsId = produceVO.sn;
			GoodsVO goodsVO = new GoodsVO();
			goodsVO.sn = goodsId;
			goodsVO.status = GOODS_未购买;
			shopVO.shelfs.add(goodsVO);
		}
	}
	
	/**
	 * 开启商店(解锁的商店还需要购买才能开启)
	 * 
	 * @param humanObj
	 * @param shopVO
	 */
	public void buyShop(HumanObject humanObj, int shopSn) {
		ConfShop confShop = ConfShop.get(shopSn);
		SCBuyShopResult.Builder msg = SCBuyShopResult.newBuilder();
		int cost = confShop.openCost;
		// 扣钱
		ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, ProduceMoneyKey.gold, cost);
		if (!result.success) {
			msg.setCode(1);
			System.out.println("货币不足！");
			return;
		}
		HumanManager.inst().produceMoneyReduce(humanObj, ProduceMoneyKey.gold, cost, MoneyReduceLogKey.神秘商店);
		// 保存状态
		Human human = humanObj.getHuman();
		String shopJson = human.getShopJson();
		List<ShopVO> voList = ShopVO.jsonToList(shopJson);
		ShopVO shopVO = returnShop(voList, shopSn);
		shopVO.status = SHOP_永久开启;
		human.setShopJson(ShopVO.listToJson(voList));
		msg.setShopSn(shopSn);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 每日重置刷新次数
	 */
	@Listener(value=EventKey.HUMAN_RESET_ZERO)
	public void resetRefreshCount(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		String shopJson = human.getShopJson();
		List<ShopVO> voList = ShopVO.jsonToList(shopJson);
		for (ShopVO shopVO : voList) {
			shopVO.refreshCount = ConfShop.get(shopVO.sn).refreshCount;
			shopVO.openCount = ConfShop.get(shopVO.sn).openCount;
		}
		human.setShopJson(ShopVO.listToJson(voList));
	}
	
	/**
	 * 监听临时商店开启
	 * @param param
	 */
	@Listener(EventKey.OPEN_TEMP_SHOP)
	public void _listener_OPEN_TEMP_SHOP(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		String shopJson = human.getShopJson();
		List<ShopVO> voList = ShopVO.jsonToList(shopJson);
		
		int openCondition = param.get("openCondition");
		List<ConfShop> confList = new ArrayList<ConfShop>();
		confList.addAll(ConfShop.findAll());
		// 遍历所有配置有openCondition的
		for (ConfShop confShop : confList) {
			int[] condition = confShop.openCondition;
			for (int i = 0; i < condition.length; i++) {
				if (condition[i] == openCondition) {
					ShopVO shopVO = returnShop(voList, confShop.sn);
					// 没有临时解锁的剩余次数，跳过
					if (shopVO.openCount <= 0) continue;
					// 如果已经开启，跳过
					if (shopVO.status == SHOP_临时解锁) continue;
					
					int prob = confShop.openProb;
					Random rand = new Random();
					int r = rand.nextInt(100);
					if (prob >= r) {
						// 通知客户端
						SCOpenTempShop.Builder msg = SCOpenTempShop.newBuilder();
						msg.setShopSn(confShop.sn);
						humanObj.sendMsg(msg);
						// 开启临时商店
						shopVO.status = SHOP_临时解锁;
						shopVO.openTime = Port.getTime() + confShop.openTime*1000;
						shopVO.refreshCount = confShop.refreshCount;
						shopVO.openCount -= 1;
						human.setShopJson(ShopVO.listToJson(voList));
					}
				}
			}
		}
	}
}
