package org.gof.demo.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.gof.core.support.RandomUtils;
import org.gof.core.support.SysException;
import org.gof.core.support.TickTimer;
import org.gof.core.support.Time;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.robot.Human;
import org.gof.demo.robot.RobotFigheManager;
import org.gof.demo.robot.RobotGame;
import org.gof.demo.robot.RobotGeneralManager;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.SCLoginResult;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.support.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public class RobotMsgHandler extends ChannelInboundHandlerAdapter {
	
	public RobotGame game = new RobotGame();
	
	private void saveChannel(Channel channel) {
		if(game.human.getChannel() == null || !game.human.getChannel().equals(channel)) {
			game.human.setChannel(channel);
		}
	}
	
	public void channelRead(ChannelHandlerContext ctx, Object buffer) throws InvalidProtocolBufferException, InterruptedException {
		//消息
		byte[] buf = (byte[]) buffer;
		
		int len = Utils.bytesToInt(buf, 0);
		int msgId = Utils.bytesToInt(buf, 4);
		byte[] msgbuf = new byte[len - 8];
		System.arraycopy(buf, 8, msgbuf, 0, len - 8);
		
		saveChannel(ctx.channel());
		
		System.out.println("The Rotot has received Msg："+msgId);
		switch(msgId) {
			case MsgIds.SCLoginResult: {
				SCLoginResult msg = SCLoginResult.parseFrom(msgbuf);
				if(msg.getResultCode() < 0) {
					//Log.temp.info("登陆返回信息={}", msg.getResultReason());
					return;
				}
				
				Msg.CSQueryCharacters.Builder querymsg = Msg.CSQueryCharacters.newBuilder();
				sendMsgToServer(ctx.channel(), querymsg);	
			}
			break;
			
			case MsgIds.SCQueryCharactersResult: {
					Msg.SCQueryCharactersResult msg = Msg.SCQueryCharactersResult.parseFrom(msgbuf);
					//玩家角色已创建 进行登录
					int count = msg.getCharactersCount();
					if(count > 0) {
						Msg.CSCharacterLogin.Builder sendmsg = Msg.CSCharacterLogin.newBuilder();
						sendmsg.setHumanId(msg.getCharacters(0).getId());
						sendMsgToServer(ctx.channel(), sendmsg);
					} else {	//玩家角色未创建 进行创建
						Msg.CSCharacterCreate.Builder msgSend = Msg.CSCharacterCreate.newBuilder();
						msgSend.setName("apple" + RandomUtils.nextInt(999));
						msgSend.setProfession(1);
						msgSend.setSex(1);
						msgSend.setGenSn("500001");
						
						sendMsgToServer(ctx.channel(), msgSend);
					}
			}
			break;
			
			case MsgIds.SCCharacterCreateResult: {
				Msg.SCCharacterCreateResult msg = Msg.SCCharacterCreateResult.parseFrom(msgbuf);
				
				int code = msg.getResultCode();
				
				//创建失败
				if(code < 0) {
					throw new SysException("创建角色失败。");
				} else {	//创建成功 登录
					Msg.CSCharacterLogin.Builder sendmsg = Msg.CSCharacterLogin.newBuilder();
					sendmsg.setHumanId(msg.getHumanId());
					sendMsgToServer(ctx.channel(), sendmsg);
				}
			}
			break;
			
			case MsgIds.SCCharacterLoginResult: {
				Msg.SCCharacterLoginResult msg = Msg.SCCharacterLoginResult.parseFrom(msgbuf);
				
				//进入游戏
				beginGame();
				//Log.coreMsg.info("登录游戏结果返回编号：{}", msg.getResultCode());
			}
			break;
			
			
			
			case MsgIds.SCInitData: {
				long time = System.currentTimeMillis();
				Msg.SCInitData msg = Msg.SCInitData.parseFrom(msgbuf);
				int a = 0;
				for(int  i = 0 ; i < 1000 ; i++) {
					a++;
					msg = Msg.SCInitData.parseFrom(msgbuf);
				}
				
				//Log.coreMsg.info("服务器返回玩家的初始属性:{} {} {}",a, msg, System.currentTimeMillis() - time);
				
				setHumanMsg(msg);
				
				//进入地图
				Msg.CSStageEnter.Builder sendmsg = Msg.CSStageEnter.newBuilder();
				sendMsgToServer(ctx.channel(), sendmsg);
				
				if(msg.getStage().getInstanceSn() == 1){
					group1();
				}
						
			}
			break;
			
			
			case MsgIds.SCAccountReconnectResult: {
				Msg.SCAccountReconnectResult msg = Msg.SCAccountReconnectResult.parseFrom(msgbuf);
				
				//Log.coreMsg.info("SCAccountReconnectResult={}", msg);
				beginGame();
//				Msg.CSStageEnter.Builder sendmsg = Msg.CSStageEnter.newBuilder();
//				sendMsgToServer(ctx.channel(), sendmsg);
			}
			break;
			
			case MsgIds.SCStageEnterResult: {
				Msg.SCStageEnterResult msg = Msg.SCStageEnterResult.parseFrom(msgbuf);
				//Log.coreMsg.info("SCStageEnterResult={}", msg);
			}
			break;
			
			//---------------------------------------------------------------------------------------
			
			case MsgIds.SCStageSwitch: {
				Msg.SCStageSwitch msg = Msg.SCStageSwitch.parseFrom(msgbuf);
				
				//Log.coreMsg.info("SCStageSwitch={}", msg);
				
				Msg.CSStageEnter.Builder sendmsg = Msg.CSStageEnter.newBuilder();
				sendMsgToServer(ctx.channel(), sendmsg);
			}
			break;
			case MsgIds.SCStageObjectAppear: {
				Msg.SCStageObjectAppear msg = Msg.SCStageObjectAppear.parseFrom(msgbuf);
				
				//Log.coreMsg.info("SCStageObjectAppear = {}", msg);
				
				
//						Msg.CSStageSwitch.Builder sendMsg = Msg.CSStageSwitch.newBuilder();
//						sendMsg.setAreaSwitchKey("10001");
//						sendMsgToServer(e.getChannel(), sendMsg);
			}
			break;
			case MsgIds.SCHumanKick : {
				//Log.temp.debug("收到踢人信息!!!!!!");
			}
			break;
			
//			case MsgIds.SCStageMove: {
//				Msg.SCStageMove msg = Msg.SCStageMove.parseFrom(msgbuf);
//				
//				//Log.coreMsg.info("移动ING:{}", msg);
//				
//			}
//			break;
			case MsgIds.SCHumanInfoChange: {
			Msg.SCHumanInfoChange msg = Msg.SCHumanInfoChange.parseFrom(msgbuf);
			
			//Log.coreMsg.info("SCHumanInfoChange:{}", msg);
			
		}
		break;
		}
		RobotFigheManager.msgReceive(msgId, msgbuf);
		RobotGeneralManager.msgReceive(msgId, msgbuf);
//		RobotStageManager.msgReceive(msgId, msgbuf);
	}
	
	/**
	 * 定时等待
	 * @throws InterruptedException 
	 */
	
	public boolean waitTime(long intval) {
		TickTimer timer = new TickTimer();
		timer.start(intval, true);
		while(!timer.isPeriod(System.currentTimeMillis())){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				
			}
		}
		timer.stop();
		return true;
	}
	/**
	 * 第一组，进城里溜达
	 */
	public void group1(){
		Log.coreMsg.info("开始执行group1");
		
		//等待1分钟
		waitTime( 60 * Time.SEC );

		//离开战斗
//		game.stageMgr.msgHandle(new String[]{"instanceLeave"}, 1);
	}
	
	/**
	 * 第二组，接任务开始打
	 */
	public void group2() {
		
	}
	
	private void sendMsgToServer(Channel channel, com.google.protobuf.Message.Builder builder) {
		Message msg = builder.build();
		int msgId = MsgIds.getIdByClass(msg.getClass());

		byte[] buf = msg.toByteArray();
		byte[] header = new byte[8];
		Utils.intToBytes(header, 0, buf.length + 8);
		Utils.intToBytes(header, 4, msgId);
		
//		channel.write(header);
//		channel.writeAndFlush(buf);
		
		channel.write(header);
		channel.write(buf);
		channel.flush();
		//Log.coreMsg.info("发送消息至服务器：id={}, name={}", msgId, MsgIds.getNameById(msgId));
	}
	
	/**
	 * 新建立一个channel
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		Msg.CSLogin.Builder sendmsg = Msg.CSLogin.newBuilder();
		String name = "cao" + System.currentTimeMillis();
		System.out.println(name);
		sendmsg.setAccount(name);// qq16
		
		sendmsg.setPassword("1");
		
//		Msg.CSAccountReconnect.Builder sendmsg = Msg.CSAccountReconnect.newBuilder();
//		sendmsg.setName("1");// qq16
//		sendmsg.setSessionKey(1);
		sendMsgToServer(ctx.channel(), sendmsg);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		
		//Log.temp.info("连接已断开");
	}
	
	private void setHumanMsg(Msg.SCInitData msg) {
		Human human = game.human;
		human.id = msg.getHuman().getId();
		human.name = msg.getHuman().getName();
		human.level = msg.getHuman().getLevel();
		human.sex = msg.getHuman().getSex();
		human.profession = msg.getHuman().getProfession();
		human.stageId = msg.getStage().getId();
		human.stageSn = msg.getStage().getSn();
		
		human.posNow = new Vector2D(msg.getStage().getPosNow());
		human.speed = msg.getHuman().getProp().getSpeed();
	}
	
	private void beginGame() {
		game.setGameRunning(true);
	}
}
