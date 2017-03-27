package org.gof.demo.worldsrv.inform;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import org.gof.demo.worldsrv.human.HumanGlobalServiceProxy;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.SCInformMsg;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.enumKey.HumanScopeKey;

public class InformManager extends ManagerBase {
	
	/**
	 * 获取实例
	 * @return
	 */
	public static InformManager inst() {
		return inst(InformManager.class);
	}
	
	/**
	 * 聊天/发言
	 * @param humanObj
	 * @param channel
	 * @param content
	 */
	public void chat(HumanObject humanObj, int channel, String content) {
		Human human = humanObj.getHuman();
		long humanId = human.getId();		//发言的玩家的ID

		//发言冷却
		if(!isCooldown(humanObj)) {
			Inform.user(humanObj.id, channel, I18n.get("common.inform.frequentlySay"));
			return;
		}
		
		if(isChannel(Inform.地图, channel)){				
			Inform.stage(humanObj.stageObj.id, channel, content, humanId);
//		} else if(isChannel(Inform.私聊, channel)) {		
//			Inform.user(receiveHumanId, channel, content, humanId);
		} else if(isChannel(Inform.世界, channel)) {
			Inform.all(channel, content, humanId);						
		} else if(isChannel(Inform.喇叭, channel)) {
			Inform.all(channel, content, humanId);	
		} else if(isChannel(Inform.征召, channel)) {
			Inform.all(channel, content, humanId);	
		}
		
		//更新发言时间
		humanObj.informLastSayTime = Port.getTime();
	}
	
	/**
	 * 验证发言是否冷却
	 * @param humanObject
	 * @return
	 */
	private boolean isCooldown(HumanObject humanObject) {
		if(humanObject.informLastSayTime == 0) return true;
		//发言间隔
		int interval = 100;
		
		return (humanObject.informLastSayTime + interval) < Port.getTime();
	}

	/**
	 * 发送消息
	 * @param key
	 * @param sendHumanId 	发送人的Id
	 * @param keyVal	接受对象的Id
	 * @param channel	频道
	 * @param content	发送内容
	 */
	public void sendMsg(HumanScopeKey key, long keyVal, Long sendHumanId, int channel, String content) {
		List<Long> list = new ArrayList<>();
		
		if(keyVal != 0) list.add(keyVal);
		
		sendMsg(key, list, sendHumanId, channel, content);
	}
	
	/**
	 * 发送消息 同时给多个目标发送
	 * @param paramKey	参数类型，即：在 地图/联盟/国家/组队 中查找 keyTypes 中的数据
	 * @param keyVals	地图/联盟/国家/组队 Id
	 * @param channel	频道
	 * @param content	内容
	 */
	public void sendMsg(HumanScopeKey key, List<Long> keyVals, Long sendHumanId, int channel, String content) {
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.sendInform(key, keyVals, channel, content, sendHumanId);
	}
	
	/**
	 * 创建返回客户端的推送消息
	 * @param channel
	 * @param content
	 * @return
	 */
	public SCInformMsg createMsg(int channel, String content, HumanGlobalInfo sender, List<HumanGlobalInfo> receiveHumanInfos) {
		Msg.SCInformMsg.Builder msg = Msg.SCInformMsg.newBuilder();
		msg.setChannel(channel);
		msg.setContent(content);
		
		if(sender != null){
			msg.setSendHumanId(sender.id);
			msg.setSendHumanName(sender.name);
		}
		
		//私聊特殊处理 "[张三]对 你 说，你 对[李四]说 " 所以要设置接受者信息
//		if(channel == Inform.私聊){
//			HumanGlobalInfo hsi = receiveHumanInfos.get(0);
//			msg.setReceiveHumanId(hsi.id);
//			msg.setReceiveHumanName(hsi.name);
//		}
		
		return msg.build();
	}
	
	/**
	 * 频道判断
	 * @param channelNum
	 * @param channel
	 * @return
	 */
	boolean isChannel(int channelNum, int channel) {
		return (channelNum & channel) == channelNum;
	}
}