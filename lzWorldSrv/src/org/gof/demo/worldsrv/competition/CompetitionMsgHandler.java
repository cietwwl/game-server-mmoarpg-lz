package org.gof.demo.worldsrv.competition;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSCompetitionBattleLog;
import org.gof.demo.worldsrv.msg.Msg.CSCompetitionEnd;
import org.gof.demo.worldsrv.msg.Msg.CSCompetitionEnter;
import org.gof.demo.worldsrv.msg.Msg.CSCompetitionLeave;
import org.gof.demo.worldsrv.msg.Msg.CSGetSeflRankPage;
import org.gof.demo.worldsrv.msg.Msg.CSOpenRank;
import org.gof.demo.worldsrv.msg.Msg.CSRefreshCount;
import org.gof.demo.worldsrv.msg.Msg.CSRefreshEnemy;
import org.gof.demo.worldsrv.msg.Msg.CSResetCDTime;
import org.gof.demo.worldsrv.rank.RankManager;

public class CompetitionMsgHandler {

	// 进入竞技场
	@MsgReceiver(CSCompetitionEnter.class)
	public void onCompetitionEnter(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSCompetitionEnter msg = param.getMsg();
		long defenderId = msg.getHumanId(); 
		//判断是否满足副本进入条件
//		boolean canRep = TowerManager.inst().canEnterRep(humanObj, confRep);
//		if(!canRep) {
//			Inform.user(humanObj.id, Inform.提示错误, I18n.get("instance.enter.error.notInCommon"));
//			return;
//		}

		//建立关卡
		CompetitionManager.inst().create(humanObj, defenderId);
	}
	
	// 关卡结束
	@MsgReceiver(CSCompetitionEnd.class)
	public void onCSLayerEnd(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompetitionManager.inst().competEnd(humanObj,true);
	}
	
	// 离开关卡
	@MsgReceiver(CSCompetitionLeave.class)
	public void onCSCompetitionLeave(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompetitionManager.inst().leave(humanObj);
	}	
	
	// 打开战斗记录
	@MsgReceiver(CSCompetitionBattleLog.class)
	public void onCSCompetitionBattleLog(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompetitionManager.inst().openCompetitionLog(humanObj);
	}
	
	// 打开竞技场界面(换一批)
	@MsgReceiver(CSRefreshEnemy.class)
	public void onCSRefreshEnemy(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompetitionManager.inst().competitionInfo(humanObj);
	}
	
	// 重置进入竞技场CD时间
	@MsgReceiver(CSResetCDTime.class)
	public void onCSResetCDTime(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompetitionManager.inst().resetCDTime(humanObj);
	}
	
	// 重置竞技场挑战次数
	@MsgReceiver(CSRefreshCount.class)
	public void onCSRefreshCount(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CompetitionManager.inst().refreshCount(humanObj);
	}
	
	// 打开排行榜
	@MsgReceiver(CSOpenRank.class)
	public void onCSOpenRank(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSOpenRank msg = param.getMsg();
		RankManager.inst().competitionRank(humanObj, msg.getPage());
	}
	
	// 打开排行榜
	@MsgReceiver(CSGetSeflRankPage.class)
	public void onCSGetSeflRankPage(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		RankManager.inst().getSelfRankPage(humanObj);
	}
}
