package org.gof.demo.robot;

import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralInfo;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralInfoAttIng;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralToAttIng;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.msg.Msg.SCFightAtkResult;
import org.gof.demo.worldsrv.support.Log;

import com.google.protobuf.InvalidProtocolBufferException;

public class RobotGeneralManager {
	private RobotGame game;
	public RobotGeneralManager(RobotGame game) {
		this.game = game;
	}
	
	public boolean msgHandle(String[] strArray, int size) {
		switch (strArray[0]) {
		case "generalInfo": {
			generalInfo(strArray, size);
		}
		break;
		case "generalInfoAttIng": {
			generalInfoAttIng(strArray, size);
		}
		break;
		case "generalToAttIng": {
			generalToAttIng(strArray, size);
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
			case MsgIds.SCGeneralInfo: {
				SCGeneralInfo msg = SCGeneralInfo.parseFrom(msgbuf);
				Log.coreMsg.info("{}", msg);
			}
			break;
			case MsgIds.SCGeneralInfoAttIng: {
				SCGeneralInfoAttIng msg = SCGeneralInfoAttIng.parseFrom(msgbuf);
				Log.coreMsg.info("{}", msg);
			}
			break;
			case MsgIds.SCGeneralToAttIng: {
				SCGeneralToAttIng msg = SCGeneralToAttIng.parseFrom(msgbuf);
				Log.coreMsg.info("{}", msg);
			}
			break;
			default: {
			}
		}
	
	}
	private void generalInfo(String[] strArray, int size) {
		Msg.CSGeneralInfo.Builder sendMsg = Msg.CSGeneralInfo.newBuilder();
		sendMsg.setId(Utils.longValue(strArray[1]));
		game.sendMsgToServer(game.human.getChannel(), sendMsg);
	}
	private void generalInfoAttIng(String[] strArray, int size) {
		Msg.CSGeneralInfoAttIng.Builder sendMsg = Msg.CSGeneralInfoAttIng.newBuilder();
		game.sendMsgToServer(game.human.getChannel(), sendMsg);
	}
	private void generalToAttIng(String[] strArray, int size) {
//		Msg.CSGeneralToAttIng.Builder sendMsg = Msg.CSGeneralToAttIng.newBuilder();
//		sendMsg.setId(Utils.longValue(strArray[1]));
//		sendMsg.setIndex(Utils.intValue(strArray[2]));
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
	}
}
