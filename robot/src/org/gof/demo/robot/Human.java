package org.gof.demo.robot;

import io.netty.channel.Channel;

import org.gof.demo.robot.vo.UnitBase;

public class Human extends UnitBase{
	
	private Channel channel = null;
	
	public int level;
	public int sex;
	public int hpCur;
	public int mpCur;
	public int profession;
	
	public double speed;
	public long stageId;
	public int stageSn;
	
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}
