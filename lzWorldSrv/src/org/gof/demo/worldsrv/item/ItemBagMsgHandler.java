package org.gof.demo.worldsrv.item;

import java.util.List;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.CSArrangeBag;
import org.gof.demo.worldsrv.msg.Msg.CSBagExpand;
import org.gof.demo.worldsrv.msg.Msg.CSItemBachSell;
import org.gof.demo.worldsrv.msg.Msg.CSUseItem;
import org.gof.demo.worldsrv.support.ReasonResult;

public class ItemBagMsgHandler {

	@MsgReceiver(CSUseItem.class)
	public void onCSUseItem(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSUseItem msg = param.getMsg();
		int packType = msg.getType();
		int position = msg.getIndex();
		int num = msg.getNum();

		ReasonResult rr = ItemBagManager.inst().itemUseByPos(humanObj, packType, position, num, packType); // TODO 注意：最后一个参数，只扩包使用，目前只用了一个背包，暂时共用这个packType
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
		}
	}

	@MsgReceiver(CSItemBachSell.class)
	public void onCSItemBachSell(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSItemBachSell msg = param.getMsg();
		int bagKey = msg.getType();
		List<Integer> positions = msg.getPolistList();

		ReasonResult rr = ItemBagManager.inst().sell(humanObj, bagKey, positions);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
		}
	}

	@MsgReceiver(CSArrangeBag.class)
	public void onCSArrangeBag(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSArrangeBag msg = param.getMsg();
		int bagKey = msg.getType();
		ItemBagManager.inst().arrange(humanObj, bagKey);
	}
	
	@MsgReceiver(CSBagExpand.class)
	public void onCSBagExpand(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSBagExpand msg = param.getMsg();
		int bagKey = msg.getType();
		int toIndex = msg.getToIndex();
		ReasonResult rr = ItemBagManager.inst().expand(humanObj, bagKey, toIndex);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
		}
	}
}
