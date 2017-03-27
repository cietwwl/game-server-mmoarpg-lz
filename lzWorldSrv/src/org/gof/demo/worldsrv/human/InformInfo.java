package org.gof.demo.worldsrv.human;

import java.util.List;

import org.gof.demo.worldsrv.support.enumKey.HumanScopeKey;

public class InformInfo {
	HumanScopeKey type;
	List<Long> keys;
	int channel;
	String content;
	Long sendHumanId;
	public InformInfo(HumanScopeKey type, List<Long> keys, int channel, String content, Long sendHumanId){
		this.type = type;
		this.keys = keys;
		this.channel = channel;
		this.content = content;
		this.sendHumanId = sendHumanId;
	}
	
}
