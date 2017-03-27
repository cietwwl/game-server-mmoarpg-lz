package org.gof.demo.robot;

import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.gof.core.interfaces.IThreadCase;
import org.gof.core.support.ThreadHandler;
import org.gof.core.support.Utils;
import org.gof.demo.robot.vo.Monster;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.support.Log;

import com.google.protobuf.Message;

public class RobotGame implements IThreadCase{
	
	private final ThreadHandler thread;				//线程管理类
	private  Scanner scan = new Scanner(System.in);
	
	public Human human;
	public List<Monster> monster = new ArrayList<Monster>();
	public boolean gameRunning = false; 
	
//	public RobotStageManager stageMgr;
	public RobotFigheManager fightMgr;
	public RobotGeneralManager generalMgr;
	
	public RobotGame() {
		super();
		
		human = new Human();
		
		thread = new ThreadHandler(this);
		startup();
		
//		stageMgr = new RobotStageManager(this);
		fightMgr = new RobotFigheManager(this);
		generalMgr = new RobotGeneralManager(this);
	}

	public void init() {
		monster.clear();
	}
	@Override
	public void caseStart() {
		
	}

	@Override
	public void caseStop() {
		
	}

	@Override
	public void caseRunOnce() {
		pulse();
	}

	public void startup() {
		thread.startup();
	}
	
	public void pulse() {
		if(!gameRunning) {
			return;
		}
		String msg = scan.nextLine();
//		System.out.println(msg);
		
		String [] strArr = msg.split(" ");
		if(strArr.length == 0) {
			return;
		}
		
//		stageMgr.msgHandle(strArr, strArr.length);
		fightMgr.msgHandle(strArr, strArr.length);
		generalMgr.msgHandle(strArr, strArr.length);
	}
	
	
	public void setGameRunning(boolean gameRunning) {
		this.gameRunning = gameRunning;
	}
	
	public void sendMsgToServer(Channel channel, com.google.protobuf.Message.Builder builder) {
		Message msg = builder.build();
		int msgId = MsgIds.getIdByClass(msg.getClass());

		byte[] buf = msg.toByteArray();
		byte[] header = new byte[8];
		Utils.intToBytes(header, 0, buf.length + 8);
		Utils.intToBytes(header, 4, msgId);
		
		channel.write(header);
		channel.write(buf);
		channel.flush();
		Log.coreMsg.info("发送消息至服务器：id={}, name={}", msgId, MsgIds.getNameById(msgId));
	}
	
}
