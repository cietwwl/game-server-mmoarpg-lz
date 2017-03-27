package org.gof.demo.battlesrv.ai;

import java.util.List;

import org.gof.demo.battlesrv.msgHandler.FightMsgHandler;
import org.gof.demo.battlesrv.msgHandler.StageBattleMsgHandler;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSFightAtk;
import org.gof.demo.worldsrv.msg.Msg.CSSkillInterrupt;
import org.gof.demo.worldsrv.msg.Msg.CSStageMove;
import org.gof.demo.worldsrv.msg.Msg.CSStageMoveStop;

public class AISendMsg {
	public static void sendCSStageMove(UnitObject unitObj, long sendId, Vector3D posBegin, List<Vector3D> posTo, Vector3D dir) {
		if(!unitObj.isHumanObj()) {
			return;
		}
		
		CSStageMove.Builder msg = CSStageMove.newBuilder();
		msg.setObjId(sendId);
		msg.setPosBegin(posBegin.toMsg());
		for (int i = 0; i < posTo.size(); i++) {
			msg.addPosEnd(posTo.get(i).toMsg());
		}
		msg.setDir(dir.toMsg());
		
		MsgParam param = new MsgParam(msg.build());
		param.setHumanObject((HumanObject)unitObj);
		StageBattleMsgHandler handler = new StageBattleMsgHandler();
		handler.onCSStageMove(param);
	}
	
	public static void sendCSStageMoveStop(UnitObject unitObj, long sendId, Vector2D posEnd) {
		if(!unitObj.isHumanObj()) {
			return;
		}
		
		CSStageMoveStop.Builder msg = CSStageMoveStop.newBuilder();
		msg.setSendId(sendId);
		msg.setPosEnd(posEnd.toMsg());
		
		MsgParam param = new MsgParam(msg.build());
		param.setHumanObject((HumanObject)unitObj);
		StageBattleMsgHandler handler = new StageBattleMsgHandler();
		handler.onCSStageMoveStop(param);
	}
	
	public static void sendSkillCast(AI ai, long tarId, Vector2D tarPos) {
		sendCSFightAtk(ai.unitObj, ai.unitObj.id, ai.curSkill.confSkill.sn, tarId, tarPos);
	}
	public static void sendCSFightAtk(UnitObject unitObj, long sendId, int skillSn, long tarId, Vector2D tarPos) {
		if(!unitObj.isHumanObj()) {
			return;
		}
		
		CSFightAtk.Builder msg = CSFightAtk.newBuilder();
		msg.setCasterId(sendId);
		msg.setSkillId(skillSn);
		if(tarId > 0) {
			msg.setTarId(tarId);
		}
		if(tarPos != null) {
			msg.setPos(tarPos.toMsg());
		}
		
		MsgParam param = new MsgParam(msg.build());
		param.setHumanObject((HumanObject)unitObj);
		FightMsgHandler handler = new FightMsgHandler();
		handler.onCSFightAtk(param);
	}
	
	public static void sendCSSkillInterrupt(UnitObject unitObj, long sendId) {
		if(!unitObj.isHumanObj()) {
			return;
		}
		
		CSSkillInterrupt.Builder msg = CSSkillInterrupt.newBuilder();
		msg.setUnitObjID(sendId);
		
		MsgParam param = new MsgParam(msg.build());
		param.setHumanObject((HumanObject)unitObj);
		FightMsgHandler handler = new FightMsgHandler();
		handler.onCSSkillInterrupt(param);
	}
}
