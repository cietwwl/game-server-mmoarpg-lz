package org.gof.demo.worldsrv.friend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.Record;
import org.gof.core.RecordTransient;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.HumanObjectServiceProxy;
import org.gof.demo.worldsrv.entity.Friend;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.PocketLine;
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import org.gof.demo.worldsrv.human.HumanGlobalServiceProxy;
import org.gof.demo.worldsrv.msg.Msg.SCAcceptFriend;
import org.gof.demo.worldsrv.msg.Msg.SCFriendList;
import org.gof.demo.worldsrv.msg.Msg.SCRecommendFriend;
import org.gof.demo.worldsrv.msg.Msg.SCRefuseFriend;
import org.gof.demo.worldsrv.msg.Msg.SCRemoveBlackList;
import org.gof.demo.worldsrv.msg.Msg.SCRemoveFriend;
import org.gof.demo.worldsrv.msg.Msg.SCRequestFriend;
import org.gof.demo.worldsrv.msg.Msg.SCSearchFriend;
import org.gof.demo.worldsrv.msg.Msg.SCToBlackList;
import org.gof.demo.worldsrv.pocketLine.Pocket;
import org.gof.demo.worldsrv.pocketLine.PocketLineEventSubKey;
import org.gof.demo.worldsrv.pocketLine.PocketLineKey;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class FriendManager extends ManagerBase {

	/**
	 * 获取实例
	 * @return
	 */
	public static FriendManager inst() {
		return inst(FriendManager.class);
	}
	
	/**
	 * 临时的假数据
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_FIRST_LOGIN)
	public void onAddTempData(Param param) {
		HumanObject humanObj = param.get("humanObj");
		if (humanObj.dataPers.friend == null) {
			Friend f = new Friend();
			f.setId(humanObj.getHumanId());
			f.setHumanId(humanObj.getHumanId());
			f.persist();
			humanObj.dataPers.friend = f;
		}
		Friend friend = humanObj.dataPers.friend;
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
		friendList.add(100010000000000218L);
		friendList.add(100010000000360001L);
		friend.setFriendList(friendList.toJSONString());
		JSONArray blackList = Utils.toJSONArray(friend.getBlackList());
		blackList.add(100010000000270063L);
		blackList.add(100010000000270001L);
		blackList.add(100010000000090001L);
		friend.setBlackList(blackList.toJSONString());
		JSONArray applyList = Utils.toJSONArray(friend.getApplyList());
		applyList.add(100010000003420156L);
		applyList.add(100010000000000063L);
		applyList.add(100010000000000125L);
		friend.setApplyList(applyList.toJSONString());
	}
	
	/**
	 * 初始化各个列表
	 * @param humanObj
	 */
	public void initFriend(HumanObject humanObj) {
		initFriendList(humanObj);
		initBlackList(humanObj);
		initApplyList(humanObj);
		FriendObject friendObj = new FriendObject();
		friendObj.id = humanObj.id;
		friendObj.modelSn = humanObj.getHuman().getSn();
		friendObj.name = humanObj.name;
		friendObj.level = humanObj.getHuman().getLevel();
		friendObj.combat = humanObj.getHuman().getCombat();
		friendObj.online = true;
		humanObj.friendObj = friendObj;
	}
	
	/**
	 * 初始化好友列表
	 * @param humanObj
	 */
	public void initFriendList(HumanObject humanObj) {
		Friend friend = humanObj.dataPers.friend;
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
		String fids = "";
		for (Object humanId : friendList) {
			if ("".equals(fids)) {
				fids = String.valueOf(humanId);
			} else {
				fids = fids + "," + String.valueOf(humanId);
			}
		}
		//要查询的列
		List<String> colList = new ArrayList<>();
		colList.add(Human.K.id);
		colList.add(Human.K.sn);
		colList.add(Human.K.level);
		colList.add(Human.K.name);
		colList.add(Human.K.combat);
		
		String whereSql = Utils.createStr(" where id in ({})", fids);
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findByQuery(false, Human.tableName, colList, whereSql);
		prx.listenResult(this::_result_initFriendList, "humanObj", humanObj);
	}
	
	public void _result_initFriendList(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<RecordTransient> list = results.get();
		List<Long> fids = new ArrayList<>();
		for(RecordTransient record : list){
			FriendObject friendObj = new FriendObject(record);
			humanObj.friendList.put(friendObj.id, friendObj);
			fids.add(friendObj.id);
		}
		// 判断是否在线
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(fids);
		prx.listenResult(this::_result_initFriendList2, "humanObj", humanObj);
	}
	
	public void _result_initFriendList2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		// 在线的好友
		List<HumanGlobalInfo> humansInfo = results.get();
		for (HumanGlobalInfo info : humansInfo) {
			FriendObject friendObj = humanObj.friendList.get(info.id);
			friendObj.online = true;
		}
	}
	
	/**
	 * 初始化黑名单
	 * @param humanObj
	 */
	public void initBlackList(HumanObject humanObj) {
		Friend friend = humanObj.dataPers.friend;
		JSONArray blackList = Utils.toJSONArray(friend.getBlackList());
		String fids = "";
		for (Object humanId : blackList) {
			if ("".equals(fids)) {
				fids = String.valueOf(humanId);
			} else {
				fids = fids + "," + String.valueOf(humanId);
			}
		}
		//要查询的列
		List<String> colList = new ArrayList<>();
		colList.add(Human.K.id);
		colList.add(Human.K.sn);
		colList.add(Human.K.level);
		colList.add(Human.K.name);
		colList.add(Human.K.combat);
		
		String whereSql = Utils.createStr(" where id in ({})", fids);
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findByQuery(false, Human.tableName, colList, whereSql);
		prx.listenResult(this::_result_initBlackList, "humanObj", humanObj);
	}
	
	public void _result_initBlackList(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<RecordTransient> list = results.get();
		for(RecordTransient record : list){
			FriendObject friendObj = new FriendObject(record);
			humanObj.blackList.put(friendObj.id, friendObj);
		}
	}
	
	/**
	 * 初始化申请列表
	 * @param humanObj
	 */
	public void initApplyList(HumanObject humanObj) {
		Friend friend = humanObj.dataPers.friend;
		JSONArray applyList = Utils.toJSONArray(friend.getApplyList());
		String fids = "";
		for (Object humanId : applyList) {
			if ("".equals(fids)) {
				fids = String.valueOf(humanId);
			} else {
				fids = fids + "," + String.valueOf(humanId);
			}
		}
		//要查询的列
		List<String> colList = new ArrayList<>();
		colList.add(Human.K.id);
		colList.add(Human.K.sn);
		colList.add(Human.K.level);
		colList.add(Human.K.name);
		colList.add(Human.K.combat);
		
		String whereSql = Utils.createStr(" where id in ({})", fids);
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findByQuery(false, Human.tableName, colList, whereSql);
		prx.listenResult(this::_result_initApplyList, "humanObj", humanObj);
	}
	
	public void _result_initApplyList(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<RecordTransient> list = results.get();
		for(RecordTransient record : list){
			FriendObject friendObj = new FriendObject(record);
			humanObj.applyList.put(friendObj.id, friendObj);
		}
	}
	
	/**
	 * 返回所有列表(打开返回)
	 * @param humanObj
	 */
	public void getAllList(HumanObject humanObj) {
		SCFriendList.Builder msg = SCFriendList.newBuilder();
		// 好友列表
		for (FriendObject obj : humanObj.friendList.values()) {
			msg.addFriendList(obj.createMsg());
		}
		// 申请人列表
		for (FriendObject obj : humanObj.applyList.values()) {
			msg.addApplyList(obj.createMsg());
		}
		// 黑名单列表
		for (FriendObject obj : humanObj.blackList.values()) {
			msg.addBlackList(obj.createMsg());
		}
		humanObj.sendMsg(msg);
	}
//	---------------------------------------申请和添加申请---------------------------------------
	/**
	 * 好友申请
	 * @param humanObj
	 * @param toHumanId
	 */
	public void requestFriend(HumanObject humanObj, long toHumanId) {
		SCRequestFriend.Builder msg = SCRequestFriend.newBuilder();
		Friend friend = humanObj.dataPers.friend;
		// 是否等于自己
		if (toHumanId == humanObj.getHumanId()) {
			Log.friend.info("申请人不能是自己:{}",toHumanId);
			msg.setResult(false);
			humanObj.sendMsg(msg);
			return;
		}
		// 申请人的好友上限是否已满
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
		if (friendList.size() >= ConfGlobalUtils.getValue(ConfGlobalKey.好友列表上限)) {
			Log.friend.info("申请人好友已满，申请人:{}，被申请人:{}", humanObj.getHumanId(), toHumanId);
			msg.setResult(false);
			humanObj.sendMsg(msg);
			return;
		}
		// 是否在好友列表中
		for (Object hid : friendList) {
			if (toHumanId == Long.valueOf(hid.toString())) {
				Log.friend.info("好友中已存在，申请人:{}，被申请人:{}", humanObj.getHumanId(), toHumanId);
				msg.setResult(false);
				humanObj.sendMsg(msg);
				return;
			} 
		}
		// 是否在黑名单列表中
		JSONArray blackList = Utils.toJSONArray(friend.getBlackList());
		for (Object bid : blackList) {
			if (toHumanId == Long.valueOf(bid.toString())) {
				Log.friend.info("黑名单中已存在，申请人:{}，被申请人:{}", humanObj.getHumanId(), toHumanId);
				msg.setResult(false);
				humanObj.sendMsg(msg);
				return;
			} 
		}
		// 是否在申请列表中
		JSONArray applyList = Utils.toJSONArray(friend.getApplyList());
		for (Object aid : applyList) {
			if (toHumanId == Long.valueOf(aid.toString())) {
				Log.friend.info("申请名单中已存在，申请人:{}，被申请人:{}", humanObj.getHumanId(), toHumanId);
				msg.setResult(false);
				humanObj.sendMsg(msg);
				return;
			} 
		}
		msg.setHumanId(toHumanId);
		msg.setResult(true);
		humanObj.sendMsg(msg);
		
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(toHumanId);
		prx.listenResult(this::_result_requestFriend, "humanObj", humanObj, "toHumanId", toHumanId);
	}
	
	public void _result_requestFriend(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		HumanGlobalInfo toHumanInfo = results.get();
		Long  toHumanId = context.get("toHumanId");
		
		//代办事项
		if(toHumanInfo == null) {
			Map<String, Long> paramMap = new HashMap<String, Long>();
			paramMap.put("applyId", humanObj.getHumanId());
			Pocket.add(toHumanId, PocketLineKey.ADD_FRIEND_APPLY, Utils.toJSONString(paramMap));
		}else{
			//如果在线更新申请列表
			HumanObjectServiceProxy humanPrx = HumanObjectServiceProxy.newInstance(toHumanInfo.nodeId, toHumanInfo.portId, toHumanInfo.id);
			humanPrx.addFriendApply(humanObj.friendObj);
		}
	}
	
	/**
	 * 添加好友申请
	 * @param humanObj
	 * @param toHumanId
	 */
	public void addRequestFriend(HumanObject humanObj, FriendObject friendObj) {
		Friend friend = humanObj.dataPers.friend;
		JSONArray applyList = Utils.toJSONArray(friend.getApplyList());
		applyList.add(friendObj.id);
		String applyListStr = applyList.toJSONString();
		friend.setApplyList(applyListStr);
	}
	
	/**
	 * 通知离线的人 添加好友申请
	 * @param param
	 */
	@Listener(value = EventKey.POCKET_LINE_HANDLE_ONE, 
			subStr = PocketLineEventSubKey.HUMAN_POCKET_LINE_HANDLE_ADD_FRIEND_APPLY)
	public void onAcceptApply(Param param) {
		HumanObject humanObj = param.get("humanObj");
		PocketLine pocket = param.get("pocketLine");
		//待办参数
		Map<String, Long> resMap = JSON.parseObject(pocket.getParam(), Map.class);
		//申请人ID
		Long applyId = resMap.get("applyId");
		Friend friend = humanObj.dataPers.friend;
		JSONArray applyList = Utils.toJSONArray(friend.getApplyList());
		applyList.add(applyId);
		String applyListStr = applyList.toJSONString();
		friend.setApplyList(applyListStr);
	}
//	---------------------------------------添加好友相关---------------------------------------
	/**
	 * 添加好友
	 * @param humanObj
	 * @param toHumanId
	 */
	public void acceptFriend(HumanObject humanObj, List<Long> list) {
		ArrayList<Long> humanIds = new ArrayList<>(list);
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.get(Friend.tableName, humanIds);
		prx.listenResult(this::_result_acceptCheck ,"humanObj", humanObj , "humanIds" , humanIds);
	}
//	
	public void _result_acceptCheck(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		SCAcceptFriend.Builder msg = SCAcceptFriend.newBuilder();
		List<Long> humanIds = context.get("humanIds");
		List<Record> records = results.get();
		List<Long> removedIds = new ArrayList<>();
		for (Record r : records) {
			Friend friend = new Friend(r);
			// 申请人的好友上限是否已满
			JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
			if (friendList.size() >= ConfGlobalUtils.getValue(ConfGlobalKey.好友列表上限)) {
				humanIds.remove(friend.getHumanId());
				removedIds.add(friend.getHumanId());
			}
		}
		// 申请人的好友数量已满
		for (long humanId : removedIds) {
			Log.friend.info("申请人的好友数量已满:{}", humanId);
			msg.setResult(false);
			humanObj.sendMsg(msg);
		}
		Friend friend = humanObj.dataPers.friend;
		for (long humanId : humanIds) {
			// 是否等于自己
			if (humanId == humanObj.getHumanId()) {
				Log.friend.info("添加的好友不能是自己:{}", humanId);
				msg.setResult(false);
				humanObj.sendMsg(msg);
				humanIds.remove(humanId);
				continue;
			}
			// 接受方的好友上限是否已满
			JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
			if (friendList.size() >= ConfGlobalUtils.getValue(ConfGlobalKey.好友列表上限)) {
				Log.friend.info("接受方好友已满，接受方:{}，被接受方:{}", humanObj.getHumanId(), humanId);
				msg.setResult(false);
				humanObj.sendMsg(msg);
				humanIds.remove(humanId);
				continue;
			}
			// 是否在好友列表中
			for (Object hid : friendList) {
				if (humanId == Long.valueOf(hid.toString())) {
					Log.friend.info("好友中已存在，接受方:{}，被接受方:{}", humanObj.getHumanId(), humanId);
					msg.setResult(false);
					humanObj.sendMsg(msg);
					humanIds.remove(humanId);
					continue;
				} 
			}
			// 是否在黑名单列表中
			JSONArray blackList = Utils.toJSONArray(friend.getBlackList());
			for (Object bid : blackList) {
				if (humanId == Long.valueOf(bid.toString())) {
					Log.friend.info("黑名单中已存在，接受方:{}，被接受方:{}", humanObj.getHumanId(), humanId);
					msg.setResult(false);
					humanObj.sendMsg(msg);
					humanIds.remove(humanId);
					continue;
				} 
			}
		}
		
		FriendServiceProxy prx = FriendServiceProxy.newInstance();
		prx.addFriend(friend.getFriendList(), humanIds);
		prx.listenResult(this::_result_addFriend, "humanObj", humanObj, "humanIds", humanIds);
	}
	
	public void _result_addFriend(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<Long> humanIds = context.get("humanIds");
		String friendListStr = results.get();
		Friend friend = humanObj.dataPers.friend;
		friend.setFriendList(friendListStr);
		
		String fids = "";
		for (Object humanId : humanIds) {
			if ("".equals(fids)) {
				fids = String.valueOf(humanId);
			} else {
				fids = fids + "," + String.valueOf(humanId);
			}
		}
		
		//要查询的列
		List<String> colList = new ArrayList<>();
		colList.add(Human.K.id);
		colList.add(Human.K.sn);
		colList.add(Human.K.level);
		colList.add(Human.K.name);
		colList.add(Human.K.combat);
		
		String whereSql = Utils.createStr(" where id in ({})", fids);
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findByQuery(false, Human.tableName, colList, whereSql);
		prx.listenResult(this::_result_addFriend2, "humanObj", humanObj, "humanIds", humanIds);
	}
	
	public void _result_addFriend2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<Long> humanIds = context.get("humanIds");
		SCAcceptFriend.Builder msg = SCAcceptFriend.newBuilder();
		Friend friend = humanObj.dataPers.friend;
		JSONArray applyList = Utils.toJSONArray(friend.getApplyList());
		List<RecordTransient> list = results.get();
		// 好友加入内存
		for(RecordTransient record : list){
			FriendObject friendObj = new FriendObject(record);
			humanObj.friendList.put(friendObj.id, friendObj);
			msg.addHumanIds(friendObj.id);
			// 申请移除内存
			humanObj.applyList.remove(friendObj.id);
			if (applyList.contains(friendObj.id)) applyList.remove(friendObj.id);
			friend.setApplyList(applyList.toJSONString());
		}
		msg.setResult(true);
		humanObj.sendMsg(msg);
		
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanIds);
		prx.listenResult(this::_result_addFriend3, "humanObj", humanObj, "humanIds", humanIds);
	}
	
	public void _result_addFriend3(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		// 在线的
		List<HumanGlobalInfo> humansInfo = results.get();
		// 全部的
		List<Long> humanIds = context.get("humanIds");
		// 离线的
		for (HumanGlobalInfo info : humansInfo) {
			if (humanIds.contains(info.id)) {
				humanIds.remove(info.id);
			}
		}
		
		//代办事项
		for (long humanId : humanIds) {
			Map<String, Long> paramMap = new HashMap<String, Long>();
			paramMap.put("toHumanId", humanObj.getHumanId());
			Pocket.add(humanId, PocketLineKey.ADD_FRIEND, Utils.toJSONString(paramMap));
		}
		for (HumanGlobalInfo info : humansInfo) {
			//如果在线更新申请列表
			HumanObjectServiceProxy humanPrx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
			humanPrx.addFriend(humanObj.friendObj);
		}
	}
	
	/**
	 * 添加好友(对方调用)
	 * @param humanObj
	 * @param toHumanId
	 */
	public void addFriend(HumanObject humanObj, FriendObject friendObj) {
		Friend friend = humanObj.dataPers.friend;
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
		friendList.add(friendObj.id);
		String friendListStr = friendList.toJSONString();
		friend.setFriendList(friendListStr);
	}
	
	/**
	 * 通知离线的人 添加好友
	 * @param param
	 */
	@Listener(value = EventKey.POCKET_LINE_HANDLE_ONE, 
			subStr = PocketLineEventSubKey.HUMAN_POCKET_LINE_HANDLE_ADD_FRIEND)
	public void onAddFriend(Param param) {
		HumanObject humanObj = param.get("humanObj");
		PocketLine pocket = param.get("pocketLine");
		//待办参数
		Map<String, Long> resMap = JSON.parseObject(pocket.getParam(), Map.class);
		//已经添加你好友的人的ID
		Long toHumanId = resMap.get("toHumanId");
		Friend friend = humanObj.dataPers.friend;
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
		friendList.add(toHumanId);
		String friendListStr = friendList.toJSONString();
		friend.setFriendList(friendListStr);
	}
//	---------------------------------------拒绝好友申请相关---------------------------------------
	/**
	 * 拒绝好友申请
	 * @param humanObj
	 * @param applyerId
	 */
	public void refuseFriend(HumanObject humanObj, long applyerId) {
		Friend friend = humanObj.dataPers.friend;
		FriendServiceProxy prx = FriendServiceProxy.newInstance();
		prx.refuseFriend(friend.getApplyList(), applyerId);
		prx.listenResult(this::_result_refuseFriend, "humanObj", humanObj, "applyerId", applyerId);
	}
	
	public void _result_refuseFriend(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		long applyerId = context.get("applyerId");
		String applyListStr = results.get();
		Friend friend = humanObj.dataPers.friend;
		friend.setApplyList(applyListStr);
		//删除内存
		if (humanObj.applyList.containsKey(applyerId)) {
			humanObj.applyList.remove(applyerId);
		}
		SCRefuseFriend.Builder msg = SCRefuseFriend.newBuilder();
		msg.setHumanId(applyerId);
		msg.setResult(true);
		humanObj.sendMsg(msg);
	}
//	---------------------------------------删除好友相关---------------------------------------
	/**
	 * 删除好友
	 * @param humanObj
	 * @param humanId
	 */
	public void removeFriend(HumanObject humanObj, long humanId, boolean flag) {
		Friend friend = humanObj.dataPers.friend;
		FriendServiceProxy proxy = FriendServiceProxy.newInstance();
		proxy.removeFriend(friend.getFriendList(), humanId);
		proxy.listenResult(this::_result_removeFriend, "humanObj", humanObj, "humanId", humanId, "flag", flag);
	}
	
	public void _result_removeFriend(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		long humanId = context.get("humanId");
		boolean flag = context.get("flag");
		String friendListStr = results.get();
		Friend friend = humanObj.dataPers.friend;
		friend.setFriendList(friendListStr);
		//删除内存
		if (humanObj.friendList.containsKey(humanId)) {
			humanObj.friendList.remove(humanId);
		}
		if (flag) {
			SCRemoveFriend.Builder msg = SCRemoveFriend.newBuilder();
			msg.setHumanId(humanId);
			msg.setResult(true);
			humanObj.sendMsg(msg);
		}
		
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_removeFriend2, "humanObj", humanObj, "humanId", humanId);
	}
	
	public void _result_removeFriend2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		HumanGlobalInfo beRemovedInfo = results.get();
		Long  beRemovedId = context.get("humanId");
		
		//代办事项
		if(beRemovedInfo == null) {
			Map<String, Long> paramMap = new HashMap<String, Long>();
			paramMap.put("removerId", humanObj.getHumanId());
			Pocket.add(beRemovedId, PocketLineKey.REMOVE_FRIEND, Utils.toJSONString(paramMap));
		}else{
			//如果在线更新申请列表
			HumanObjectServiceProxy humanPrx = HumanObjectServiceProxy.newInstance(beRemovedInfo.nodeId, beRemovedInfo.portId, beRemovedInfo.id);
			humanPrx.removeFriend(humanObj.getHumanId());
		}
	}
	
	/**
	 * 删除好友(对方调用)
	 * @param humanObj
	 * @param toHumanId
	 */
	public void removeFriend2(HumanObject humanObj, long humanId) {
		Friend friend = humanObj.dataPers.friend;
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
		friendList.remove(humanId);
		String friendListStr = friendList.toJSONString();
		friend.setFriendList(friendListStr);
		//删除内存
		if (humanObj.friendList.containsKey(humanId)) {
			humanObj.friendList.remove(humanId);
		}
		SCRemoveFriend.Builder msg = SCRemoveFriend.newBuilder();
		msg.setHumanId(humanId);
		msg.setResult(true);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 通知离线的人 删除好友
	 * @param param
	 */
	@Listener(value = EventKey.POCKET_LINE_HANDLE_ONE, 
			subStr = PocketLineEventSubKey.HUMAN_POCKET_LINE_HANDLE_REMOVE_FRIEND)
	public void onRemoveFriend(Param param) {
		HumanObject humanObj = param.get("humanObj");
		PocketLine pocket = param.get("pocketLine");
		//待办参数
		Map<String, Long> resMap = JSON.parseObject(pocket.getParam(), Map.class);
		//已经删除你的人的ID
		Long removerId = resMap.get("removerId");
		Friend friend = humanObj.dataPers.friend;
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
		friendList.remove(removerId);
		String friendListStr = friendList.toJSONString();
		friend.setFriendList(friendListStr);
	}
//	---------------------------------------黑名单相关---------------------------------------
	/**
	 * 拉黑好友或者陌生人
	 * @param humanObj
	 * @param humanId
	 */
	public void toBlackList(HumanObject humanObj, long humanId) {
		Friend friend = humanObj.dataPers.friend;
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
		// 如果是好友先解除好友关系
		if (friendList.contains(humanId)) {
			removeFriend(humanObj, humanId, false);
		}
		FriendServiceProxy proxy = FriendServiceProxy.newInstance();
		proxy.toBlackList(friend.getFriendList(), humanId);
		proxy.listenResult(this::_result_toBlackList, "humanObj", humanObj, "humanId", humanId);
	}
	
	public void _result_toBlackList(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		long humanId = context.get("humanId");
		String blackListStr = results.get();
		Friend friend = humanObj.dataPers.friend;
		friend.setBlackList(blackListStr);
		//删除内存
		if (humanObj.friendList.containsKey(humanId)) {
			humanObj.friendList.remove(humanId);
		}
		//要查询的列
		List<String> colList = new ArrayList<>();
		colList.add(Human.K.id);
		colList.add(Human.K.sn);
		colList.add(Human.K.level);
		colList.add(Human.K.name);
		colList.add(Human.K.combat);
		
		String whereSql = Utils.createStr(" where id in ({})", humanId);
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findByQuery(false, Human.tableName, colList, whereSql);
		prx.listenResult(this::_result_toBlackList2, "humanObj", humanObj, "humanId", humanId);
	}
	
	public void _result_toBlackList2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		long humanId = context.get("humanId");
		List<RecordTransient> list = results.get();
		for(RecordTransient record : list){
			FriendObject friendObj = new FriendObject(record);
			humanObj.blackList.put(friendObj.id, friendObj);
		}
		SCToBlackList.Builder msg = SCToBlackList.newBuilder();
		msg.setHumanId(humanId);
		msg.setResult(true);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 删除黑名单
	 * @param humanObj
	 * @param humanId
	 */
	public void removeBlackList(HumanObject humanObj, long humanId) {
		Friend friend = humanObj.dataPers.friend;
		FriendServiceProxy proxy = FriendServiceProxy.newInstance();
		proxy.removeBlackList(friend.getBlackList(), humanId);
		proxy.listenResult(this::_result_removeBlackList, "humanObj", humanObj, "humanId", humanId);
	}
	public void _result_removeBlackList(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		long humanId = context.get("humanId");
		String blackListStr = results.get();
		Friend friend = humanObj.dataPers.friend;
		friend.setBlackList(blackListStr);
		//删除内存
		if (humanObj.blackList.containsKey(humanId)) {
			humanObj.blackList.remove(humanId);
		}
		SCRemoveBlackList.Builder msg = SCRemoveBlackList.newBuilder();
		msg.setHumanId(humanId);
		msg.setResult(true);
		humanObj.sendMsg(msg);
	}
//	---------------------------------------查找推荐相关---------------------------------------
	/**
	 * 查询好友 精确查找
	 * @param humanObj
	 * @param reqmsg
	 */
	public void searchFriend(HumanObject humanObj , long humanId){
		//要查询的列
		List<String> listCol = new ArrayList<>();
		listCol.add(Human.K.id);
		listCol.add(Human.K.sn);
		listCol.add(Human.K.level);
		listCol.add(Human.K.name);
		listCol.add(Human.K.combat);
		String whereSql = Utils.createStr(" where id='{}'", humanId);
		
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findByQuery(false, Human.tableName, listCol, whereSql);
		prx.listenResult(this::_result_searchFriend, "humanObj", humanObj);
	}
	
	public void  _result_searchFriend(Param results, Param context){
		HumanObject humanObj = context.get("humanObj");	
		SCSearchFriend.Builder msg = SCSearchFriend.newBuilder();
		
		List<RecordTransient> list = results.get();
		for(RecordTransient record : list){
			FriendObject friendObj = new FriendObject(record);
			msg.addInfos(friendObj.createMsg());
		}
		
		humanObj.sendMsg(msg);//应答客户端
	}
	
	/**
	 * 推荐好友
	 * @param humanObj
	 */
	public void recommendUsers(HumanObject humanObj){
		Friend friend = humanObj.dataPers.friend;
		String friendListStr = friend.getFriendList();
		String blackListStr = friend.getBlackList();
		int combat = humanObj.getHuman().getCombat();
		
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getRecommendUsers(humanObj.getHumanId(), friendListStr, blackListStr, combat);
		prx.listenResult(this::_result_recommendusers, "humanObj", humanObj);
	}
	
	public void _result_recommendusers(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		List<HumanGlobalInfo> recommendList = results.get("recommendList");
		
		SCRecommendFriend.Builder msg = SCRecommendFriend.newBuilder();
		for(HumanGlobalInfo r : recommendList) {
			FriendObject friendObj = new FriendObject(r);
			msg.addInfos(friendObj.createMsg());
		}
		humanObj.sendMsg(msg);//应答客户端
	}
//	---------------------------------------玩家上下线相关---------------------------------------
	/**
	 * 玩家上线
	 * @param param
	 */
	@Listener(EventKey.HUMAN_LOGIN)
	public void onHumanLogin(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Friend friend = humanObj.dataPers.friend;
		JSONArray friendList = Utils.toJSONArray(friend.getFriendList());
  		List<Long> fids = new ArrayList<>();
  		for (Object fid : friendList) {
  			fids.add(Long.valueOf(fid.toString()));
  		}
  		
		// 在线好友
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(fids);
		prx.listenResult(this::_result_login2, "humanObj", humanObj);
	}
	
	public void _result_login2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		// 在线的
		List<HumanGlobalInfo> humansInfo = results.get();
		
		for (HumanGlobalInfo info : humansInfo) {
			//如果在线
			if (info != null) {
				HumanObjectServiceProxy humanPrx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
				humanPrx.updateFriendOnline(humanObj.id, true);
			}
		}
	}
	
	/**
	 * 玩家下线
	 * @param param
	 */
	@Listener(EventKey.HUMAN_LOGOUT)
	public void onLogout(Param param) {
  		HumanObject humanObj = param.get("humanObj");
  		List<Long> fids = new ArrayList<>();
  		fids.addAll(humanObj.friendList.keySet());
  		
		// 在线好友
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(fids);
		prx.listenResult(this::_result_logout2, "humanObj", humanObj);
	}
	
	public void _result_logout2(Param results, Param context) {
		HumanObject humanObj = context.get("humanObj");
		// 在线的
		List<HumanGlobalInfo> humansInfo = results.get();
		
		for (HumanGlobalInfo info : humansInfo) {
			//如果在线
			if (info != null) {
				HumanObjectServiceProxy humanPrx = HumanObjectServiceProxy.newInstance(info.nodeId, info.portId, info.id);
				humanPrx.updateFriendOnline(humanObj.id, false);
			}
		}
	}
}
