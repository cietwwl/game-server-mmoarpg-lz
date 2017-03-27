package org.gof.demo.worldsrv.human;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.msg.Msg.CSChangeModel;
import org.gof.demo.worldsrv.msg.Msg.CSHumanInfo;
import org.gof.demo.worldsrv.msg.Msg.SCHumanInfo;

public class HumanMsgHandler {
	
	
	@MsgReceiver(CSHumanInfo.class)
	public void _result_onCSHumanInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		Human human = humanObj.getHuman();
		
		//武将收集最多数量
		int genCount = 0;
		String[] generalRequired = human.getAllGeneral().split(",");
		if(generalRequired!= null && generalRequired.length > 0) {
			genCount = generalRequired.length;
		}
		SCHumanInfo.Builder msg = SCHumanInfo.newBuilder();
		msg.setCombat(human.getCombat());
		msg.setGeneralColCount(genCount);
		humanObj.sendMsg(msg);
	}
	
	@MsgReceiver(CSChangeModel.class)
	public void onCSChangeModel(MsgParam param) {
		CSChangeModel msg = param.getMsg();
		
		HumanManager.inst().changeModel(param.getHumanObject(), msg.getModel());
	}
	
}
