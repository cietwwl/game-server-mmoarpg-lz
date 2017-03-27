package org.gof.demo.worldsrv.item;

import java.util.List;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.CSEquipDown;
import org.gof.demo.worldsrv.msg.Msg.CSEquipReclaim;
import org.gof.demo.worldsrv.msg.Msg.CSEquipUp;
import org.gof.demo.worldsrv.msg.Msg.CSGemComposite;
import org.gof.demo.worldsrv.msg.Msg.CSGemCompositeAllTop;
import org.gof.demo.worldsrv.msg.Msg.CSGemCompositeTop;
import org.gof.demo.worldsrv.msg.Msg.CSGemDown;
import org.gof.demo.worldsrv.msg.Msg.CSGemDownAll;
import org.gof.demo.worldsrv.msg.Msg.CSGemUp;
import org.gof.demo.worldsrv.msg.Msg.CSGemUpAll;
import org.gof.demo.worldsrv.msg.Msg.CSPartChongxing;
import org.gof.demo.worldsrv.msg.Msg.CSPartQianghua;
import org.gof.demo.worldsrv.msg.Msg.SCEquipDown;
import org.gof.demo.worldsrv.msg.Msg.SCEquipReclaim;
import org.gof.demo.worldsrv.msg.Msg.SCGemComposite;
import org.gof.demo.worldsrv.msg.Msg.SCGemCompositeAllTop;
import org.gof.demo.worldsrv.msg.Msg.SCGemCompositeTop;
import org.gof.demo.worldsrv.msg.Msg.SCPartChongxing;
import org.gof.demo.worldsrv.msg.Msg.SCPartQianghua;
import org.gof.demo.worldsrv.msg.Msg.SCSetGem;
import org.gof.demo.worldsrv.support.ReasonResult;

public class ItemBodyMsgHandler {

	@MsgReceiver(CSEquipUp.class)
	public void onCSEquipUp(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSEquipUp msg = param.getMsg();
		int position = msg.getIndex();

		ReasonResult rr = ItemBodyManager.inst().equipUp(humanObj, position);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
		}
	}

	@MsgReceiver(CSEquipDown.class)
	public void onCSEquipDown(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSEquipDown msg = param.getMsg();
		int position = msg.getIndex();

		ReasonResult rr = ItemBodyManager.inst().equipDown(humanObj, position);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCEquipDown.Builder fail = SCEquipDown.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSEquipReclaim.class)
	public void onCSEquipReclaim(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSEquipReclaim msg = param.getMsg();
		List<Integer> positions = msg.getIndexListList();

		ReasonResult rr = ItemBodyManager.inst().equipReclaim(humanObj, positions);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示操作, rr.reason);
			SCEquipReclaim.Builder fail = SCEquipReclaim.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSPartQianghua.class)
	public void onCSPartQianghua(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSPartQianghua msg = param.getMsg();
		int part = msg.getPart();

		ReasonResult rr = ItemBodyManager.inst().partQianghua(humanObj, part);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCPartQianghua.Builder fail = SCPartQianghua.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSPartChongxing.class)
	public void onCSPartChongxing(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSPartChongxing msg = param.getMsg();
		int part = msg.getPart();
		int phase = msg.getPhase();
		int seekPos = msg.getSeekPos();
		ReasonResult rr = ItemBodyManager.inst().partChongxing(humanObj, part, phase, seekPos);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCPartChongxing.Builder fail = SCPartChongxing.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSGemUp.class)
	public void onCSGemUp(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGemUp msg = param.getMsg();
		int bagIndex = msg.getBagIndex();
		int partSn = msg.getPart();
		ReasonResult rr = ItemBodyManager.inst().partGemUp(humanObj, partSn, bagIndex);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCSetGem.Builder fail = SCSetGem.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSGemUpAll.class)
	public void onCSGemUpAll(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGemUpAll msg = param.getMsg();
		int partSn = msg.getPart();
		ReasonResult rr = ItemBodyManager.inst().partGemUpAll(humanObj, partSn);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCSetGem.Builder fail = SCSetGem.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSGemDown.class)
	public void onCSGemDown(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGemDown msg = param.getMsg();
		int partSn = msg.getPart();
		int slot = msg.getSlot();
		ReasonResult rr = ItemBodyManager.inst().partGemDown(humanObj, partSn,
				slot);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCSetGem.Builder fail = SCSetGem.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSGemDownAll.class)
	public void onCSGemDownAll(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGemDownAll msg = param.getMsg();
		int partSn = msg.getPart();
		ReasonResult rr = ItemBodyManager.inst().partGemDownAll(humanObj,
				partSn);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCSetGem.Builder fail = SCSetGem.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSGemComposite.class)
	public void onCSGemComposite(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGemComposite msg = param.getMsg();
		int bagIndex = msg.getBagIndex();
		ReasonResult rr = ItemBodyManager.inst().gemComposite(humanObj,
				bagIndex);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCGemComposite.Builder fail = SCGemComposite.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSGemCompositeTop.class)
	public void onCSGemCompositeTop(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGemCompositeTop msg = param.getMsg();
		int bagIndex = msg.getBagIndex();
		ReasonResult rr = ItemBodyManager.inst().gemCompositeTop(humanObj,
				bagIndex);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示错误, rr.reason);
			SCGemCompositeTop.Builder fail = SCGemCompositeTop.newBuilder();
			fail.setCode(1);
			humanObj.sendMsg(fail);
		}
	}

	@MsgReceiver(CSGemCompositeAllTop.class)
	public void onCSGemCompositeAllTop(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		ReasonResult rr = ItemBodyManager.inst().gemCompositeAllTop(humanObj);
		if (!rr.success) {
			Inform.user(humanObj.id, Inform.提示操作, rr.reason);
//			SCGemCompositeAllTop.Builder fail = SCGemCompositeAllTop.newBuilder();
//			fail.setCode(1);
//			fail.setReason(rr.reason);
//			humanObj.sendMsg(fail);
		}
	}
}
