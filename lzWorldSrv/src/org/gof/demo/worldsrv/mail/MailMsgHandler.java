package org.gof.demo.worldsrv.mail;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.msg.Msg.CSCheckMailRemoveable;
import org.gof.demo.worldsrv.msg.Msg.CSClearMail;
import org.gof.demo.worldsrv.msg.Msg.CSMailList;
import org.gof.demo.worldsrv.msg.Msg.CSPickupAllMail;
import org.gof.demo.worldsrv.msg.Msg.CSPickupMail;
import org.gof.demo.worldsrv.msg.Msg.CSReadMail;

public class MailMsgHandler {


	/**
	 * 玩家打开邮件列表
	 * @param param
	 */
	@MsgReceiver(CSMailList.class)
	public void onCSMailList(MsgParam param) {
		MailManager.inst().initMailList(param.getHumanObject());
	}
	
	/**
	 * 将邮件设置为已读
	 * @param param
	 */
	@MsgReceiver(CSReadMail.class)
	public void onCSReadMail(MsgParam param) {
		CSReadMail msg = param.getMsg();
		
		MailManager.inst().readMail(param.getHumanObject(), msg.getId());
	}
	
	/**
	 * 领取邮件里的物品
	 * @param param
	 */
	@MsgReceiver(CSPickupMail.class)
	public void onCSPickupMail(MsgParam param) {
		CSPickupMail msg = param.getMsg();
		
		MailManager.inst().pickupMailAttachment(param.getHumanObject(), msg.getId());
	}
	
	/**
	 * 领取所有邮件里的物品
	 * @param param
	 */
	@MsgReceiver(CSPickupAllMail.class)
	public void onCSPickupAllMail(MsgParam param) {
		MailManager.inst().pickupAllMailAttachment(param.getHumanObject());
	}	

	/**
	 * 清理邮件
	 * @param param
	 */
	@MsgReceiver(CSClearMail.class)
	public void onCSClearMail(MsgParam param) {
		MailManager.inst().clearMail(param.getHumanObject());
	}	
	
	/**
	 * 验证邮件是否有效
	 * @param param
	 */
	@MsgReceiver(CSCheckMailRemoveable.class)
	public void onCSCheckMailRemoveable(MsgParam param) {
		CSCheckMailRemoveable msg = param.getMsg();
		
		MailManager.inst().checkMailRemoveable(param.getHumanObject(), msg.getId());
	}	
	
}
