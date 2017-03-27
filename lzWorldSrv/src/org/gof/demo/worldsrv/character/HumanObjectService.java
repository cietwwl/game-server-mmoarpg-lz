package org.gof.demo.worldsrv.character;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.CallPoint;
import org.gof.core.Port;
import org.gof.core.RecordTransient;
import org.gof.core.Service;
import org.gof.core.connsrv.ConnectionProxy;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.MsgHandler;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.seam.msg.HumanExtendMsgHandler;
import org.gof.demo.worldsrv.entity.Friend;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Mail;
import org.gof.demo.worldsrv.friend.FriendManager;
import org.gof.demo.worldsrv.friend.FriendObject;
import org.gof.demo.worldsrv.friend.FriendServiceProxy;
import org.gof.demo.worldsrv.general.GeneralManager;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.mail.MailManager;
import org.gof.demo.worldsrv.stage.StagePort;

import com.alibaba.fastjson.JSONArray;

@DistrClass(
	importClass = {Mail.class, Friend.class, FriendObject.class}
)
public class HumanObjectService extends Service {
	private HumanExtendMsgHandler msgHandler = MsgHandler.getInstance(HumanExtendMsgHandler.class);
	
	//对应的玩家对象
	private final HumanObject humanObj;
	
	/**
	 * 构造函数
	 * @param humanObj
	 */
	public HumanObjectService(HumanObject humanObj, Port port) {
		super(port);
		this.humanObj = humanObj;
	}
	
	/**
	 * 获取所属Port
	 * @return
	 */
	public StagePort getPort() {
		return humanObj.getPort();
	}
	
	/**
	 * 离开地图
	 * @param humanId
	 */
	@DistrMethod
	public void leave() {
		humanObj.stageLeave();
		GeneralManager.inst().hideAll(humanObj);
	}
	
	@DistrMethod
	public void ChangeConnPoint(CallPoint point) {
		humanObj.connPoint = point;
		humanObj.clearCloseStatus();
	}
	
	/**
	 * 接受并转发通信消息
	 * @param humanId
	 * @param chunk
	 */
	@DistrMethod
	public void msgHandler(long connId, byte[] chunk) {
		//忽略错误连接ID的请求
		long humanConnId = (long) humanObj.connPoint.servId;
		if(humanConnId != connId) {
			//将发送错误连接的请求连接关了
			CallPoint connPoint = new CallPoint();
			connPoint.nodeId = port.getCallFromNodeId();
			connPoint.portId = port.getCallFromPortId();
			connPoint.servId = connId;
			
			ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
			prx.close();
			return;
		}
		
		msgHandler.handle(chunk, "humanObj", humanObj);
	}
	
	/**
	 * 连接关闭
	 * @param humanId
	 */
	@DistrMethod
	public void connClosed(long connId) {
		//忽略错误连接ID的请求
		long humanConnId = (long) humanObj.connPoint.servId;
//		Log.temp.info("connClosed {} {} {}", humanConnId, connId, Port.getTime());
		if(humanConnId != connId) {
			return;
		}
		
		humanObj.connDelayCloseClear();
	}
	
	@DistrMethod
	public void kickClosed() {
		//直接T人
		humanObj.connCloseClear();
	}
	
	/**
	 * 连接存活验证
	 * @param humanId
	 */
	@DistrMethod
	public void connCheck(long connId) {
		port.returns(true);
	}
	
	/**
	 * 调度事件的处理 ， 来自于 humanGlobalSerivce
	 * @param key
	 */
	@DistrMethod
	public void onSchedule(int key, long timeLast) {
		HumanManager.inst().onScheduleEvent(humanObj, key, timeLast);
	}

	/**
	 * 接收邮件
	 */
	@DistrMethod
	public void mailAccept(Mail mail) {
		MailManager.inst().addNew(humanObj, mail);
	}	
	
	@DistrMethod
	public void getHuman() {
		getPort().returns(humanObj.getHuman());
	}
	
	@Override
	public Object getId() {
		return humanObj.id;
	}
	
	public HumanObject getHumanObj() {
		return humanObj;
	}
	
	/**
	 * 添加好友申请
	 * @param acceptHumanObj(好友申请者)
	 */
	@DistrMethod
	public void addFriendApply(FriendObject friendObj) {
		FriendManager.inst().addRequestFriend(humanObj, friendObj);
		// 更新
		humanObj.applyList.put(friendObj.id, friendObj);
	}
	
	/**
	 * 添加好友
	 * @param acceptHumanObj(主动添加者)
	 */
	@DistrMethod
	public void addFriend(FriendObject friendObj) {
		FriendManager.inst().addFriend(humanObj, friendObj);
		// 更新
		humanObj.friendList.put(friendObj.id, friendObj);
	}
	
	/**
	 * 删除好友
	 * @param removerId(主动删除者)
	 */
	@DistrMethod
	public void removeFriend(long removerId) {
		FriendManager.inst().removeFriend2(humanObj, removerId);
		// 更新
		humanObj.friendList.remove(removerId);
	}
	
	/**
	 * 通知好友上下线
	 * @param humanId(上下线者)
	 */
	@DistrMethod
	public void updateFriendOnline(long humanId, boolean online) {
		// 更新
		FriendObject friendObj = humanObj.friendList.get(humanId);
		friendObj.online = online;
	}
}
