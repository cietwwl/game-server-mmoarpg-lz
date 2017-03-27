package org.gof.demo.worldsrv.treasure;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.CSGatcha;
import org.gof.demo.worldsrv.msg.Msg.CSGatcha10;
import org.gof.demo.worldsrv.support.ReasonResult;

public class TreasureMsgHandler {

	@MsgReceiver(CSGatcha.class)
	public void onCSGatcha(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGatcha msg = param.getMsg();
		int tType = msg.getTType();

		ReasonResult rr = TreasureManager.inst().gatcha(humanObj, tType);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
		}
	}

	@MsgReceiver(CSGatcha10.class)
	public void onCSGatcha10(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGatcha10 msg = param.getMsg();
		int tType = msg.getTType();

		ReasonResult rr = TreasureManager.inst().gatcha10(humanObj, tType);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
		}
	}
}
