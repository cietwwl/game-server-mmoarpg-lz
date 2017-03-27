package org.gof.demo.worldsrv.rank;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSInstanceRank;
import org.gof.demo.worldsrv.msg.Msg.CSLevelRank;

public class RankMsgHandler {
	
	/**
	 * 打开副本排行榜
	 * @param param
	 */
	@MsgReceiver(CSInstanceRank.class)
	public void onCSInstanceRank(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		RankManager.inst().instanceRank(humanObj);
		
	}
	
	/**
	 * 打开等级排行榜
	 * @param param
	 */
	@MsgReceiver(CSLevelRank.class)
	public void onLevelRank(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		RankManager.inst().levelRank(humanObj);
		
	}
}
