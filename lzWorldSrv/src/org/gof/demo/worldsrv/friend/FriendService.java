package org.gof.demo.worldsrv.friend;

import java.util.List;

import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.entity.Friend;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.support.D;

import com.alibaba.fastjson.JSONArray;

/**
 * 好友
 */
@DistrClass(
	servId = D.SERV_FRIEND,
	importClass = {Human.class, List.class, HumanObject.class, Friend.class}
)
public class FriendService extends GameServiceBase {
	
	public FriendService(GamePort port) {
		super(port);
	}

	@Override
	protected void init() {
		
	}

	
	/**
	 * 添加好友
	 */
	@DistrMethod
	public void addFriend(String friendListStr, List<Long> humanIds) {
		JSONArray friendList = Utils.toJSONArray(friendListStr);
		for (long humanId : humanIds) {
			friendList.add(humanId);
		}
		port.returns(friendList.toJSONString());
	}
	
	/**
	 * 拒绝好友申请
	 */
	@DistrMethod
	public void refuseFriend(String applyListStr, long humanId) {
		JSONArray applyList = Utils.toJSONArray(applyListStr);
		applyList.remove(humanId);
		port.returns(applyList.toJSONString());
	}
	
	/**
	 * 删除好友
	 */
	@DistrMethod
	public void removeFriend(String friendListStr, long humanId) {
		JSONArray friendList = Utils.toJSONArray(friendListStr);
		friendList.remove(humanId);
		port.returns(friendList.toJSONString());
	}
	
	/**
	 * 拉黑
	 */
	@DistrMethod
	public void toBlackList(String blackListStr, long humanId) {
		JSONArray blackList = Utils.toJSONArray(blackListStr);
		blackList.add(humanId);
		port.returns(blackList.toJSONString());
	}
	
	/**
	 * 从黑名单删除
	 */
	@DistrMethod
	public void removeBlackList(String blackListStr, long humanId) {
		JSONArray blackList = Utils.toJSONArray(blackListStr);
		blackList.remove(humanId);
		port.returns(blackList.toJSONString());
	}
}
