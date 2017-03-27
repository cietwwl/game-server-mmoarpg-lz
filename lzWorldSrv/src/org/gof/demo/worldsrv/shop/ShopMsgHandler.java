package org.gof.demo.worldsrv.shop;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSBuyShop;
import org.gof.demo.worldsrv.msg.Msg.CSBuyShopGoods;
import org.gof.demo.worldsrv.msg.Msg.CSOpenShop;
import org.gof.demo.worldsrv.msg.Msg.CSRefreshShop;
import org.gof.demo.worldsrv.msg.Msg.CSRequestShopTags;

public class ShopMsgHandler {

	/**
	 * 请求商店标签
	 * 
	 * @param param
	 */
	@MsgReceiver(CSRequestShopTags.class)
	public void onCSRequestTags(MsgParam param) {
		HumanObject humanObject = param.getHumanObject();
		ShopManager.inst().tagsInfo(humanObject);
	}
	
	/**
	 * 打开商店
	 * 
	 * @param param
	 */
	@MsgReceiver(CSOpenShop.class)
	public void onCSOpenShop(MsgParam param) {
		HumanObject humanObject = param.getHumanObject();
		CSOpenShop msg = param.getMsg();
		int shopSn = msg.getShopSn();
		ShopManager.inst().openShop(humanObject, shopSn);
	}
	
	/**
	 * 购买商品
	 * 
	 * @param param
	 */
	@MsgReceiver(CSBuyShopGoods.class)
	public void onCSBuy(MsgParam param) {
		HumanObject humanObject = param.getHumanObject();
		CSBuyShopGoods msg = param.getMsg();
		int shopSn = msg.getShopSn();
		int index = msg.getIndex();
		
		ShopManager.inst().buyGoods(humanObject, shopSn, index);
	}
	
	/**
	 * 刷新商店
	 * 
	 * @param param
	 */
	@MsgReceiver(CSRefreshShop.class)
	public void onRefreshShop(MsgParam param) {
		HumanObject humanObject = param.getHumanObject();
		CSRefreshShop msg = param.getMsg();
		int shopSn = msg.getShopSn();
		
		ShopManager.inst().refreshShop(humanObject, shopSn);
	}
	
	/**
	 * 购买商店
	 * 
	 * @param param
	 */
	@MsgReceiver(CSBuyShop.class)
	public void onBuyShop(MsgParam param) {
		HumanObject humanObject = param.getHumanObject();
		CSBuyShop msg = param.getMsg();
		int shopSn = msg.getShopSn();
		
		ShopManager.inst().buyShop(humanObject, shopSn);
	}
}
