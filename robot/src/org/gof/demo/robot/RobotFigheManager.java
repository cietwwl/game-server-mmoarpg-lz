package org.gof.demo.robot;

import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.SCFightAtkResult;
import org.gof.demo.worldsrv.msg.MsgIds;

import com.google.protobuf.InvalidProtocolBufferException;

public class RobotFigheManager {
	private RobotGame game;
	public RobotFigheManager(RobotGame game) {
		this.game = game;
	}
	
	public boolean msgHandle(String[] strArray, int size) {
		switch (strArray[0]) {
		case "fightattack": {
			fightAttack(strArray, size);
		}
		break;
		default: {
			return false;
		}
		}
		return true;
	}
	
	public static void msgReceive(int msgId, byte[] msgbuf) throws InvalidProtocolBufferException {
		switch (msgId) {
			case MsgIds.SCFightAtkResult: {
				SCFightAtkResult msg = SCFightAtkResult.parseFrom(msgbuf);
				if(msg.getResultCode() >= 0) {
//					Log.coreMsg.info("技能发放成功");
				} else {
//					Log.coreMsg.info("技能发放失败");
				}
			}
			break;
			case MsgIds.SCFightSkill: {
//				SCFightSkill msg = SCFightSkill.parseFrom(msgbuf);
//				Log.coreMsg.info("战斗结果:{}", msg);
			}
			break;
			default: {
			}
		}
	
	}
	private void fightAttack(String[] strArray, int size) {
		Msg.CSFightAtk.Builder sendMsg = Msg.CSFightAtk.newBuilder();
		sendMsg.setCasterId(game.human.id);
		sendMsg.setSkillId(111000);
		sendMsg.setTarId(Utils.longValue(strArray[2]));
		sendMsg.setAtkerType(1);
		game.sendMsgToServer(game.human.getChannel(), sendMsg);
	}
}
