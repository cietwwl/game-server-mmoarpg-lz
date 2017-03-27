package org.gof.demo.worldsrv.dailyliveness;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSOpenLivenessUI;
import org.gof.demo.worldsrv.msg.Msg.CSRsvLivenessAwards;

/**
 * 	每日活跃度
 */
public class LivenessMsgHandler {
	
	/**
	 * 领取活跃度奖励
	 * @param param
	 */
	@MsgReceiver(CSRsvLivenessAwards.class)
	public void rsvLivenessAwards(MsgParam param){
		
		HumanObject humanObj = param.getHumanObject();
		CSRsvLivenessAwards msg = param.getMsg();
		int code = msg.getCode();
		LivenessManager.inst().rsvLivenessAwards(humanObj, code);
	}
	
	/**
	 * 打开每日活跃度UI
	 * @param param
	 */
	@MsgReceiver(CSOpenLivenessUI.class)
	public void openLivenessUI(MsgParam param) {
		LivenessManager.inst().openLivenessUI(param.getHumanObject());
	}
}
