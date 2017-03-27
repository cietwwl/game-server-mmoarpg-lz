package org.gof.demo.worldsrv.shop;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSBuyMallGoods;
import org.gof.demo.worldsrv.msg.Msg.CSOpenMall;

public class MallMsgHandler {

	/**
	 * 打开商城
	 * 
	 * @param param
	 */
	@MsgReceiver(CSOpenMall.class)
	public void onCSOpenMall(MsgParam param) {
		HumanObject humanObject = param.getHumanObject();
		CSOpenMall msg = param.getMsg();
		int mallSn = msg.getMallSn();
		MallManager.inst().openMall(humanObject, mallSn);
	}
	
	/**
	 * 购买商品
	 * 
	 * @param param
	 */
	@MsgReceiver(CSBuyMallGoods.class)
	public void onCSBuy(MsgParam param) {
		HumanObject humanObject = param.getHumanObject();
		CSBuyMallGoods msg = param.getMsg();
		int mallSn = msg.getMallSn();
		int index = msg.getIndex();
		int num = msg.getNum();
		
		MallManager.inst().buyGoods(humanObject, mallSn, index, num);
	}
}
