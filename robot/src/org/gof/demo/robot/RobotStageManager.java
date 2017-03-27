//package org.gof.demo.robot;
//
//import org.gof.core.support.Utils;
//import org.gof.demo.battlesrv.support.Vector3D;
//import org.gof.demo.worldsrv.msg.Msg;
//import org.gof.demo.worldsrv.msg.Msg.SCInstanceAll;
//import org.gof.demo.worldsrv.msg.MsgIds;
//import org.gof.demo.worldsrv.support.Log;
//
//import com.google.protobuf.InvalidProtocolBufferException;
//
//public class RobotStageManager {
//
//	private RobotGame game;
//
//	public RobotStageManager(RobotGame game) {
//		this.game = game;
//	}
//
//	public boolean msgHandle(String[] strArray, int size) {
//		switch (strArray[0]) {
//		case "stageSwitch": {
//			stageSwich(strArray, size);
//		}
//			break;
//		case "stageEnter": {
//			stageEnter(strArray, size);
//		}
//			break;
//		case "stageMove": {
//			stageMove(strArray, size);
//		}
//			break;
//		case "StageMoveStop": {
//			stageMoveStop(strArray, size);
//		} break;
//		case "instanceEnter": {
//			instanceEnter(strArray, size);
//		} break;
//		case "instanceLeave": {
//			instanceLeave(strArray, size);
//		} break;
//		case "instanceAll": {
//			instanceAll(strArray, size);
//		} break;
//		case "instanceNumAdd": {
//			instanceNumAdd(strArray, size);
//		} break;
//		case "instanceAuto": {
//			instanceAuto(strArray, size);
//		} break;
//		
//		case "LoadClass": {
//			loadClass(strArray, size);
//		} break;
//		case "test": {
//			testAgain(strArray, size);
//		}
//			break;
//		default: {
//			return false;
//		}
//		}
//		return true;
//	}
//
//	public static void msgReceive(int msgId, byte[] msgbuf) throws InvalidProtocolBufferException {
//		switch (msgId) {
//			case MsgIds.SCInstanceAll: {
//				SCInstanceAll msg = SCInstanceAll.parseFrom(msgbuf);
//				Log.coreMsg.info("{}", msg);
//			}break;
//			
//			default: {
//			}
//		}
//	
//	}
//	private void stageSwich(String[] strArray, int size) {
//		Msg.CSStageSwitch.Builder sendMsg = Msg.CSStageSwitch.newBuilder();
//		sendMsg.setAreaSwitchKey(strArray[1]);
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	private void stageEnter(String[] strArray, int size) {
//		Msg.CSStageEnter.Builder sendMsg = Msg.CSStageEnter.newBuilder();
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	private void stageMove(String[] strArray, int size) {
//		Msg.CSStageMove.Builder sendMsg = Msg.CSStageMove.newBuilder();
//		float x = Utils.floatValue(strArray[1]);
//		float y = Utils.floatValue(strArray[2]);
//		if(x == 0 || y == 0) {
//			Log.temp.info("坐标输入错误");
//		}
//		
//		sendMsg.addPosEnd(new Vector3D(x, 27 ,y).toMsg());
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	
//	private void stageMoveStop(String[] strArray, int size) {
//		Msg.CSStageMoveStop.Builder sendMsg = Msg.CSStageMoveStop.newBuilder();
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	private void instanceEnter(String[] strArray, int size) {
//		Msg.CSInstanceEnter.Builder sendMsg = Msg.CSInstanceEnter.newBuilder();
//		sendMsg.setInstSn(1011);
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	private void instanceLeave(String[] strArray, int size) {
//		Msg.CSInstanceLeave.Builder sendMsg = Msg.CSInstanceLeave.newBuilder();
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	private void instanceAll(String[] strArray, int size) {
//		Msg.CSInstanceAll.Builder sendMsg = Msg.CSInstanceAll.newBuilder();
//		sendMsg.setChapterId(1);
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	private void instanceNumAdd(String[] strArray, int size) {
//		Msg.CSInstanceNumAdd.Builder sendMsg = Msg.CSInstanceNumAdd.newBuilder();
//		sendMsg.setInstItemSn(10101);
//		sendMsg.setNumAdd(3);
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	private void instanceAuto(String[] strArray, int size) {
//		Msg.CSInstanceAuto.Builder sendMsg = Msg.CSInstanceAuto.newBuilder();
//		sendMsg.setInstSn(1010101);
//		sendMsg.setNum(1);
//		game.sendMsgToServer(game.human.getChannel(), sendMsg);
//	}
//	
//	private void loadClass(String[] strArray, int size) {
//		Msg.CSTest.Builder test = Msg.CSTest.newBuilder();
//		test.setCscode(998800);
//		test.setCsstring("org.gof.demo.worldsrv.test.TestManager");
//		game.sendMsgToServer(game.human.getChannel(),  test);
//		
//		test.setCsstring("org.gof.demo.worldsrv.stage.StageManager");
//		game.sendMsgToServer(game.human.getChannel(),  test);
//	}
//	
//	private void testAgain(String[] strArray, int size) {
//		//增加测试代码，服务器端会自动返回这个接口，从这里发送信息，插入一段测试逻辑
//		Msg.CSTest.Builder msgTest = Msg.CSTest.newBuilder();
//		msgTest.setCscode(110011);
//		msgTest.setCsstring("this is a CSTest!");
//		game.sendMsgToServer(game.human.getChannel(),  msgTest);
//	}
//	
//}
