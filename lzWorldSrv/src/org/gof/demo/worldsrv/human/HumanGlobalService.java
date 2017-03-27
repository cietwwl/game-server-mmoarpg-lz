package org.gof.demo.worldsrv.human;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.gof.core.CallPoint;
import org.gof.core.Port;
import org.gof.core.connsrv.ConnectionProxy;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.scheduler.ScheduleMethod;
import org.gof.core.support.TickTimer;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.HumanObjectServiceProxy;
import org.gof.demo.worldsrv.common.DataResetService;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.SCHumanKick;
import org.gof.demo.worldsrv.msg.Msg.SCInformMsg;
//import org.gof.demo.worldsrv.msg.Msg.SCInformMsgAll;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.HumanException;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.enumKey.HumanScopeKey;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.Message;

@DistrClass(
	servId = D.SERV_HUMAN_GLOBAL,
	importClass = {HumanGlobalInfo.class ,  Map.class, HumanScopeKey.class, List.class, Message.class} 
)
public class HumanGlobalService extends GameServiceBase {
//	Map<HumanGlobalInfo, SCInformMsgAll.Builder> msgMap = new HashMap<HumanGlobalInfo, SCInformMsgAll.Builder>();
	private TickTimer msgPulseTimer = new TickTimer(500); //控制广播发送频率
	
	private TickTimer scheduleTimer = new TickTimer(10000); //调度的处理
	private Map<Integer, Long> shceduleMap = new LinkedHashMap<Integer, Long>();
	private int COUNT100Per10SEC = 500;
	//玩家状态信息
	private Map<Long, HumanGlobalInfo> datas = new HashMap<>();
		
	
	@Override
	public void pulseOverride() {
		
		//延迟处理统一发送
		if(msgPulseTimer.isPeriod(Port.getTime())) {
//			sendInfomAll();
		}
		
		if(scheduleTimer.isPeriod(Port.getTime())) {
			onSchdule();
		}
	}

	
	
	public HumanGlobalService(GamePort port) {
		super(port);
	}
	
	@Override
	protected void init() {
		
	}
	
	/**
	 * 获取玩家的全局信息
	 * @param humanId
	 */
	@DistrMethod
	public void getInfo(long humanId) {
		port.returns(datas.get(humanId));
		if(datas.get(humanId)== null)return;
	}
	

	/**
	 * 获取多个玩家的全局信息
	 * @param humanIds
	 */
	@DistrMethod
	public void getInfos(List<Long> humanIds) {
		List<HumanGlobalInfo> infos = getHumanInfos(humanIds);
		port.returns(infos);
	}
	
	/**
	 * 获取多个队伍的玩家的全局信息
	 * @param humanId
	 */
	@DistrMethod
	public void getInfosByMap(Map<Object, List<Long>>humanIdsMap) {
		Map<Object, List<HumanGlobalInfo>> result = new LinkedHashMap<Object, List<HumanGlobalInfo>>();
	    for(Map.Entry<Object, List<Long>> entry:humanIdsMap.entrySet()){
	        Object key = entry.getKey();
	        List<Long> ids = entry.getValue();
	        List<HumanGlobalInfo> infos = getHumanInfos(ids);
	        result.put(key, infos);
	    }
		port.returns(result);
	}
	private List<HumanGlobalInfo> getHumanInfos(List<Long> ids){
		List<HumanGlobalInfo> infos = new ArrayList<HumanGlobalInfo>();
		for(Long hId : ids){
			if(hId == null){
				continue;
			}
			if(datas.containsKey(hId)){
				infos.add(datas.get(hId));
			}
		}
		return infos;
	}
	/**
	 * @param humanIds
	 */
	@DistrMethod
	public void getInfo(List<Long> humansIds) {
		List<HumanGlobalInfo> humansInfo = new ArrayList<HumanGlobalInfo>() ;
		//优化
		for(Long id : humansIds){
			HumanGlobalInfo tmp = datas.get(id);
			if(tmp != null)
				humansInfo.add(tmp);
		}
		port.returns(humansInfo);
	}
	
	
	/**
	 * 注册玩家全局信息
	 * @param status
	 */
	@DistrMethod
	public void register(HumanGlobalInfo status) {
		datas.put(status.id, status);
	}
	
	@DistrMethod
	public void ChangeConnPoint(Long id, CallPoint point) {
		HumanGlobalInfo info = datas.get(id);
		if(info != null) {
			info.connPoint = point;
		}
	}
	
	/**
	 * 玩家是否已登录
	 * @param account
	 */
	@DistrMethod
	public void isLogined(String account) {
		boolean logined = false;
		long humanId = 0;
		HumanGlobalInfo humanInfo = null;
		for(HumanGlobalInfo h : datas.values()) {
			if(StringUtils.equals(account, h.account)) {
				logined = true;
				humanId = h.id;
				humanInfo = h;
				break;
			}
		}
		
		port.returns("logined", logined, "humanId", humanId, "humanInfo", humanInfo);
	}
	
	/**
	 * 踢出玩家
	 * @param id
	 * @param reason
	 */
	@DistrMethod
	public void kick(long id, String reason) {
		kickHuman(id, reason);
	}
	
	
	/**
	 * 踢人下线
	 * @param connPoint
	 * @param msg
	 */
	private void kickHuman(long humanId, String reason) {
		//发送消息 通知玩家被踢
		SCHumanKick.Builder msg = SCHumanKick.newBuilder();
		msg.setReason(reason);
		sendMsg(humanId, msg.build());
		
		//玩家连接信息
		HumanGlobalInfo info = datas.get(humanId);
		
		//断开连接
		ConnectionProxy prx = ConnectionProxy.newInstance(info.connPoint);
		prx.close();
		
//		//直接清除玩家的一些信息 因为如果玩家连接
		HumanObjectServiceProxy prxSource = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, humanId);
		prxSource.kickClosed();
	}
	
	/**
	 * 发送消息至玩家
	 * @param builder
	 */
	@DistrMethod
	public void sendMsg(long humanId, Message msg) {
		//玩家连接信息
		HumanGlobalInfo info = datas.get(humanId);
		
		if(info == null) return ;
		HumanGlobalManager.inst().sendMsg(info.connPoint, msg);
	}
	
	/**
	 * 发送消息至全服玩家，有要排除的则设置excludeIds
	 * @param excludeIds
	 * @param msg
	 */
	@DistrMethod
	public void sendMsgToAll(List<Long> excludeIds, Message msg) {
		// 给所有玩家发送消息
		for(HumanGlobalInfo info : datas.values()) {
			// 排除不需要发送的玩家id
			if(excludeIds.contains(info.id)) {
				HumanGlobalManager.inst().sendMsg(info.connPoint, msg);
			}
		}
	}
	
	/**
	 * 清楚玩家全局信息
	 * @param status
	 */
	@DistrMethod
	public void cancel(long humanId) {
		datas.remove(humanId);
	}
	
	@DistrMethod
	public void stageIdModify(long humanId, long stageIdNew, String stageName, String nodeId, String portId) {
		HumanGlobalInfo info = datas.get(humanId);
		
		//特殊处理
		if(info == null) {
			Log.human.error("修改玩家全局地图信息时出错，玩家数据不存在："
							+ "humanId={}, stageIdNew={}, stageName={}, nodeId={}, portId={}",
							humanId, stageIdNew, stageName, nodeId, portId);
			return;
		}
		
		//更新地图ID
		info.stageId = stageIdNew;
		info.stageName = stageName;
		info.nodeId = nodeId;
		info.portId = portId;
	}
//	private void sendInfomAll() {
//		for (Map.Entry<HumanGlobalInfo, SCInformMsgAll.Builder> entry : msgMap.entrySet()) {
//			HumanGlobalManager.inst().sendMsg(entry.getKey().connPoint, entry.getValue().build());
//		}
//		msgMap.clear();
//	}

	@DistrMethod
	public void sendInform(HumanScopeKey type, List<Long> keys, int channel, String content, Long sendHumanId) {
		//获取符合条件的玩家
		List<HumanGlobalInfo> receiver = getInfoByScope(type, keys);
		
		//发送人
		HumanGlobalInfo sender = null;
		if(sendHumanId != null) {
			sender = datas.get(sendHumanId);
		}
		
		/* 拼聊天消息 */
		SCInformMsg.Builder msgBuild = SCInformMsg.newBuilder();
		msgBuild.setChannel(channel);
		msgBuild.setContent(content);
		//发送人
		if(sender != null){
			msgBuild.setSendHumanId(sender.id);
			msgBuild.setSendHumanName(sender.name);
			//msgBuild.setSendHumanVIPLevel(sender.vipSn);
		}
		
		//私聊特殊处理 "[张三]对 你 说，你 对[李四]说 " 所以要设置接受者信息
		//私聊肯定是1v1的
//		if(channel == Inform.私聊 && !receiver.isEmpty()) {
//			HumanGlobalInfo r = receiver.get(0);
//			msgBuild.setReceiveHumanId(r.id);
//			msgBuild.setReceiveHumanName(r.name);
//		}
		
		//最终消息
//		SCInformMsg msg = msgBuild.build();
		
		//循环发送消息
		for(HumanGlobalInfo r : receiver) {
//			SCInformMsgAll.Builder msg = msgMap.get(r);
//			if(msg == null) {
//				msg = SCInformMsgAll.newBuilder();
//			}
//			msg.addInformMsg(msgBuild);
//			msgMap.put(r, msg);
			HumanGlobalManager.inst().sendMsg(r.connPoint, msgBuild.build());
		}

		//目标玩家不在线 提示发送者
		if(channel == Inform.私聊 && sender != null){
			//目标玩家不在线
			if(receiver.isEmpty()) throw new HumanException(sender.id, I18n.get("common.inform.targetOffLine"));
		}
	}
	
	private List<HumanGlobalInfo> getInfoByScope(HumanScopeKey scope, List<Long> keys){
		List<HumanGlobalInfo> list = new ArrayList<>();
				
		//所有玩家
		if(scope.equals(HumanScopeKey.ALL)){
			list.addAll(datas.values());
			return list;	
		}
		
		//其余情况需要单独判断
		for(HumanGlobalInfo i : datas.values()){
			switch (scope) {
				case HUMAN: {	//单个玩家
					if(keys.contains(i.id)) list.add(i);
				}
				break;
				case STAGE: {	//地图上的玩家
					if(keys.contains(i.stageId)) list.add(i);
				}
				break;
//				case COUNTRY: {	//国家
//					if(keys.contains((long)i.countryId)) list.add(i);
//				}
//				break;
				case UNION: {	//联盟
					if(keys.contains(i.unionId)) list.add(i);
				}
				break;
				case TEAM: {	//组队
//					if(keys.contains(i.teamId)) list.add(i);
				}
				break;
				default:
					break;
			}
		}
		
		return list;
	}
	
	/**
	 * 根据条件筛选在线玩家
	 * @param humanId
	 * @param friendListStr 
	 * @param blackListStr 
	 * @param combat
	 */
	@DistrMethod
	public void getRecommendUsers(long humanId, String friendListStr ,String blackListStr, int combat) {
		List<HumanGlobalInfo> recommendList = new ArrayList<HumanGlobalInfo>();
		JSONArray friendList = Utils.toJSONArray(friendListStr);
		JSONArray blackList = Utils.toJSONArray(blackListStr);
		//先剔除在好友名单和黑名单的
		List<HumanGlobalInfo> users = new ArrayList<HumanGlobalInfo>();
		for (HumanGlobalInfo info : datas.values()) {
			//如果是自己，则不推荐
			if (humanId == info.id) continue;
			
			boolean flag = false ;//标志位
			//判断是否在好友名单
			for (int i = 0; i < friendList.size(); i++) {
				if (humanId == friendList.getLongValue(i)) {
					flag = true;
					break;
				}
			}
			if (flag) continue;
			//判断是否在黑名单
			for (int i = 0; i < blackList.size(); i++) {
				if (humanId == blackList.getLongValue(i)) {
					flag = true;
					break;
				}
			}
			if (flag) continue;
			users.add(info);//满足三个条件，则将玩家信息加入列表返回
		}
		
		float ratio = 0.3f;
		List<HumanGlobalInfo> tmp = new ArrayList<HumanGlobalInfo>();
		for (HumanGlobalInfo info : users) {
			//判断战斗力是否在30%内
			if (Math.abs(combat-info.combat)/1 > ratio) continue;
			tmp.add(info);
			//最多10个
			if(tmp.size() == 5) break;
		}
		if (tmp.size() == 0) {
			Collections.shuffle(users);//将在线玩家随机打乱
			if (users.size() > 5) {
				for (int i = 0; i < 5; i++) {
					tmp.add(users.get(i));
				}
			} else {
				tmp = users;
			}
		}
		
		//返回推荐玩家
		 for(int i=0;i< tmp.size();i++){
			 recommendList.add(tmp.get(i));
		 }
		port.returns("recommendList", recommendList);
	}
	
	@ScheduleMethod(DataResetService.CRON_DAY_ZERO)
	public void schdDayZeroReset() {
		long timeNow = Port.getTime();
		shceduleMap.put(EventKey.HUMAN_RESET_ZERO, timeNow);
	}
	
	@ScheduleMethod(DataResetService.CRON_DAY_FIVE)
	public void schdDayFiveReset() {
		long timeNow = Port.getTime();
		shceduleMap.put(EventKey.HUMAN_RESET_FIVE, timeNow);
	}
	
	@ScheduleMethod(DataResetService.CRON_DAY_12ST)
	public void schdDay12STReset() {
		long timeNow = Port.getTime();
		shceduleMap.put(EventKey.HUMAN_RESET_12ST, timeNow);
	}
	
	@ScheduleMethod(DataResetService.CRON_DAY_18ST)
	public void schdDay18STReset() {
		long timeNow = Port.getTime();
		shceduleMap.put(EventKey.HUMAN_RESET_18ST, timeNow);
	}
	
	@ScheduleMethod(DataResetService.CRON_DAY_21ST)
	public void schdDay21STReset() {
		long timeNow = Port.getTime();
		shceduleMap.put(EventKey.HUMAN_RESET_21ST, timeNow);
	}
	
	/**
	 * 每6分钟一次，用户体力恢复
	 */
	@ScheduleMethod("0 0/6 * * * ?") 
	public void schdEvery6Min() {
		long timeNow = Port.getTime();
		shceduleMap.put(EventKey.HUMAN_RESET_6MIN, timeNow);
	}
	
	public void onSchdule() {
		if(shceduleMap.size() <= 0) {
			return;
		}
		
		int i = 0;
		for (HumanGlobalInfo obj : datas.values()) {
			boolean sendSchd = false;
			for (Entry<Integer, Long> entry: shceduleMap.entrySet()) {
				if(obj.timeLogin < entry.getValue()) {
					//发送到玩家
					obj.timeLogin = entry.getValue();
					HumanObjectServiceProxy proxy = HumanObjectServiceProxy.newInstance(obj.nodeId, obj.portId, obj.id);
					proxy.onSchedule(entry.getKey(), obj.timeLogin);
					sendSchd = true;
				}
			}
			if(sendSchd) {
				i++;
				//超过数量返回
				if(i > COUNT100Per10SEC) {
					return;
				}
			}
			
		}
		if(i == 0) {
			shceduleMap.clear();
		}
	}
	
}