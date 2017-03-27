package org.gof.demo.worldsrv.team;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.CSTeamApply;
import org.gof.demo.worldsrv.msg.Msg.CSTeamApplyConfirm;
import org.gof.demo.worldsrv.msg.Msg.CSTeamInvite;
import org.gof.demo.worldsrv.msg.Msg.CSTeamInviteConfirm;
import org.gof.demo.worldsrv.msg.Msg.CSTeamLeave;
import org.gof.demo.worldsrv.msg.Msg.CSTeamMine;
import org.gof.demo.worldsrv.msg.Msg.CSTeamRepCall;
import org.gof.demo.worldsrv.msg.Msg.CSTeamRepCreate;
import org.gof.demo.worldsrv.msg.Msg.CSTeamRepKick;
import org.gof.demo.worldsrv.msg.Msg.CSTeamRepLeave;
import org.gof.demo.worldsrv.msg.Msg.CSTeamRepLeaveQueue;
import org.gof.demo.worldsrv.msg.Msg.CSTeamReqCloseUI;
import org.gof.demo.worldsrv.msg.Msg.CSTeamReqJoin;
import org.gof.demo.worldsrv.msg.Msg.CSTeamReqLaunch;
import org.gof.demo.worldsrv.msg.Msg.CSTeamReqOpenUI;

/**
 * 组队副本
 */
public class TeamRepMsgHandler {
	
	@MsgReceiver(CSTeamReqOpenUI.class)
	public void OpenUI(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamReqOpenUI msg = param.getMsg();
		int reqId = msg.getReqId();
		TeamManager.inst().OpenUI(humanObj, reqId);
	}
	@MsgReceiver(CSTeamReqCloseUI.class)
	public void CloseUI(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamReqCloseUI msg = param.getMsg();
		int reqId = msg.getReqId();
		TeamManager.inst().CloseUI(humanObj.id, reqId);
	}
	@MsgReceiver(CSTeamReqJoin.class)
	public void join(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamReqJoin msg = param.getMsg();
		int reqId = msg.getReqId();
		long teamId = msg.getTeamId();
		TeamManager.inst().join(humanObj, reqId, teamId);
	}
	@MsgReceiver(CSTeamReqLaunch.class)
	public void launch(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamReqLaunch msg = param.getMsg();
		int reqId = msg.getReqId();
		TeamManager.inst().launch(humanObj, reqId);
	}
	@MsgReceiver(CSTeamRepCreate.class)
	public void create(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamRepCreate msg = param.getMsg();
		int reqId = msg.getReqId();
		TeamManager.inst().create(humanObj, reqId);
	}
	
	/* 3321 召唤队友*/
	@MsgReceiver(CSTeamRepCall.class)
	public void call(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamRepCall msg = param.getMsg();
		int reqId = msg.getReqId();
		TeamManager.inst().call(humanObj, reqId);//
	}
	
	/* 3311 踢掉队员 */
	@MsgReceiver(CSTeamRepKick.class)
	public void kick(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamRepKick msg = param.getMsg();
		long memberId = msg.getMemberId();
        TeamManager.inst().kick(humanObj, memberId);
	}
	
	/* 普通队伍·邀请对方和自己组队，自己为队长 */
	@MsgReceiver(CSTeamInvite.class)
	public void invite(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamInvite msg = param.getMsg();
		long humanId = msg.getMemberId();
		TeamManager.inst().invite(humanObj, humanId);
	}
	
	/* 接受对方的组队邀请，对方为队长， 结果可能为拒绝也可能为同意 */
	@MsgReceiver(CSTeamInviteConfirm.class)
	public void inviteConfirm(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamInviteConfirm msg = param.getMsg();
		long humanId = msg.getMemberId();
		int result = msg.getResult();//1：同意
		if(result == 0){
			Inform.user(humanId, Inform.提示操作, "对方拒绝了你的邀请！");
			return;
		}
		TeamManager.inst().inviteConfirm(humanObj, humanId);
	}
	
	/* 普通组队·申请加入到对方的队伍 */
	@MsgReceiver(CSTeamApply.class)
	public void apply(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamApply msg = param.getMsg();
		long humanId = msg.getMemberId();
		TeamManager.inst().apply(humanObj, humanId);
	}
	
	/* (队长)接受对方的申请，允许对方加入到己方的队伍  可能是同意也可能是拒绝*/
	@MsgReceiver(CSTeamApplyConfirm.class)
	public void applyConfirm(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamApplyConfirm msg = param.getMsg();
		long humanId = msg.getMemberId();
		int result = msg.getResult();//1：同意
		if(result == 0){
			Inform.user(humanId, Inform.提示操作, "对方拒绝了你的申请！");
			return;
		}
		TeamManager.inst().applyConfirm(humanObj, humanId);
	}
	
	/* 普通组队的离开，目前与副本队伍的离开不做区分 */
	@MsgReceiver(CSTeamLeave.class)
	public void leave(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		TeamManager.inst().leaveReq(humanObj);
	}
	
	/* 3309 队员离开 */
	@MsgReceiver(CSTeamRepLeave.class)
	public void leaveReq(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		TeamManager.inst().leaveReq(humanObj);
	}
	
	/* 3317  队长离开副本队伍序列 */;
	@MsgReceiver(CSTeamRepLeaveQueue.class)
	public void leaveQueue(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTeamRepLeaveQueue msg = param.getMsg();
		int reqId = msg.getReqId();
		TeamManager.inst().leaveQueue(humanObj, reqId);
	}
	
	/* 3341 请求更新自己的队伍信息[主面板] */
	@MsgReceiver(CSTeamMine.class)
	public void getMyTeam(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		TeamManager.inst().getMyTeam(humanObj);
	}
}
