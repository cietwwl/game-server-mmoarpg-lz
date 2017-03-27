package org.gof.demo.worldsrv.friend;

import java.util.List;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSAcceptFriend;
import org.gof.demo.worldsrv.msg.Msg.CSFriendList;
import org.gof.demo.worldsrv.msg.Msg.CSRecommendFriend;
import org.gof.demo.worldsrv.msg.Msg.CSRefuseFriend;
import org.gof.demo.worldsrv.msg.Msg.CSRemoveBlackList;
import org.gof.demo.worldsrv.msg.Msg.CSRemoveFriend;
import org.gof.demo.worldsrv.msg.Msg.CSRequestFriend;
import org.gof.demo.worldsrv.msg.Msg.CSSearchFriend;
import org.gof.demo.worldsrv.msg.Msg.CSToBlackList;

public class FriendMsgHandler {

	// 请求几个列表列表
	@MsgReceiver(CSFriendList.class)
	public void onCSFriendList(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		FriendManager.inst().getAllList(humanObj);
	}
	
	// 推荐好友
	@MsgReceiver(CSRecommendFriend.class)
	public void onCSRecommendFriend(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		FriendManager.inst().recommendUsers(humanObj);
	}
	
	// 好友申请
	@MsgReceiver(CSRequestFriend.class)
	public void onCSRequestFriend(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRequestFriend msg = param.getMsg();
		long toHumanId = msg.getHumanId();
		FriendManager.inst().requestFriend(humanObj, toHumanId);
	}
	
	// 接受好友
	@MsgReceiver(CSAcceptFriend.class)
	public void onCSAcceptFriend(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSAcceptFriend msg = param.getMsg();
		List<Long> humanIds = msg.getHumanIdsList();
		FriendManager.inst().acceptFriend(humanObj, humanIds);
	}
	
	// 拒绝好友请求
	@MsgReceiver(CSRefuseFriend.class)
	public void onCSRefuseFriend(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRefuseFriend msg = param.getMsg();
		long humanId = msg.getHumanId();
		FriendManager.inst().refuseFriend(humanObj, humanId);
	}
	
	// 搜索好友
	@MsgReceiver(CSSearchFriend.class)
	public void onCSSearchFriend(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSearchFriend msg = param.getMsg();
		long humanId = msg.getHumanId();
		FriendManager.inst().searchFriend(humanObj, humanId);
	}

	// 删除好友
	@MsgReceiver(CSRemoveFriend.class)
	public void onCSRemoveFriend(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRemoveFriend msg = param.getMsg();
		long humanId = msg.getHumanId();
		FriendManager.inst().removeFriend(humanObj, humanId, true);
	}
	
	// 拉黑好友
	@MsgReceiver(CSToBlackList.class)
	public void onCSToBlackList(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSToBlackList msg = param.getMsg();
		long humanId = msg.getHumanId();
		FriendManager.inst().toBlackList(humanObj, humanId);
	}
	
	// 删除黑名单玩家
	@MsgReceiver(CSRemoveBlackList.class)
	public void onCSRemoveBlackList(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSRemoveBlackList msg = param.getMsg();
		long humanId = msg.getHumanId();
		FriendManager.inst().removeBlackList(humanObj, humanId);
	}
}
