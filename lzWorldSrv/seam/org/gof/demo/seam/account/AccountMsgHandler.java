package org.gof.demo.seam.account;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.gof.core.CallPoint;
import org.gof.core.Chunk;
import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.connsrv.ConnectionProxy;
import org.gof.core.support.ConnectionStatus;
import org.gof.core.support.Param;
import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParamAccount;
import org.gof.demo.worldsrv.bulletin.BulletinManager;
import org.gof.demo.worldsrv.common.HumanCreateApplyServiceProxy;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.msg.Msg.CSAccountRandomName;
import org.gof.demo.worldsrv.msg.Msg.CSAccountReconnect;
import org.gof.demo.worldsrv.msg.Msg.CSBulletinOpenUI;
import org.gof.demo.worldsrv.msg.Msg.CSCharacterCreate;
import org.gof.demo.worldsrv.msg.Msg.CSCharacterLogin;
import org.gof.demo.worldsrv.msg.Msg.CSLogin;
import org.gof.demo.worldsrv.msg.Msg.CSQueryCharacters;
import org.gof.demo.worldsrv.msg.Msg.SCAccountReconnectResult;
import org.gof.demo.worldsrv.msg.Msg.SCCharacterCreateResult;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.support.C;

import com.google.protobuf.GeneratedMessage;

public class AccountMsgHandler {
	
	//下属监听消息
	public static final Set<Class<? extends GeneratedMessage>> methods = new HashSet<>();

	static {
		//寻找本类监听的消息
		Method[] mths = AccountMsgHandler.class.getMethods();
		for(Method m : mths) {
			//不是监听函数的忽略
			if(!m.isAnnotationPresent(MsgReceiver.class)) {
				continue;
			}
			
			//记录
			MsgReceiver ann = m.getAnnotation(MsgReceiver.class);
			methods.add(ann.value()[0]);
		}
	}
	
	/**
	 * 玩家登陆请求
	 * @param param
	 */
	@MsgReceiver(CSLogin.class)
	public void onCSLogin(MsgParamAccount param) {
		
		CSLogin msg = param.getMsg();
		CallPoint connPoint = param.getConnPoint();
		ConnectionStatus connStatus = param.getConnStatus();

		//登录
		AccountManager.inst().login(connPoint, connStatus, msg.getAccount(), msg.getPassword(), msg.getToken());
	}
	
	/**
	 * 角色查询请求
	 * @param param
	 */
	@MsgReceiver(CSQueryCharacters.class)
	public void onCSQueryCharacters(MsgParamAccount param) {
		
		Long connId = param.getConnId();
		AccountService serv = param.getService();
		AccountObject obj = serv.datas.get(connId);
		
		AccountManager.inst().queryCharacters(obj, obj.status.account);
	}
	
	/**
	 * 角色创建请求
	 * @param param
	 */
	@MsgReceiver(CSCharacterCreate.class)
	public void onCSCharacterCreate(MsgParamAccount param) {
		AccountService serv = param.getService();
		long connId = param.getConnId();
		CSCharacterCreate msg = param.getMsg();
		AccountObject obj = serv.datas.get(connId);
		
		//先查询验证是否能创建角色
		HumanCreateApplyServiceProxy prx = HumanCreateApplyServiceProxy.newInstance();
		prx.apply(C.GAME_SERVER_ID, obj.status.account, msg.getName().toLowerCase());
		prx.listenResult(this::_result_onCSCharacterCreate, "gateObj", obj, "msg", msg);
	}
	
	/**
	 * 角色创建请求 查询角色信息 是否可以被创建
	 * @param results
	 * @param context
	 */
	public void _result_onCSCharacterCreate(Param results, Param context) {
		//返回值
		boolean succeed = results.get("result");
		String reason = results.get("reason");
		
		//上下文
		AccountObject obj = context.get("gateObj");
		CSCharacterCreate msg = context.get("msg");

		//验证是否可以创建角色
		if(!succeed) {
			//创建失败消息
			SCCharacterCreateResult.Builder msgSender = SCCharacterCreateResult.newBuilder(); 
			msgSender.setResultCode(-1);
			msgSender.setResultReason(reason);
			
			//返回消息
			ConnectionProxy prx = ConnectionProxy.newInstance(obj.connPoint);
			prx.sendMsg(MsgIds.SCCharacterCreateResult, new Chunk(msgSender));
			
			return;
		}
		
		//验证通过 正式创建角色
		Human human = HumanManager.inst().create(Port.applyId(), C.GAME_SERVER_ID, obj.status.account, msg.getName().toLowerCase(), msg.getProfession(), msg.getSex(), msg.getGenSn());
		
		//创建成功消息
		SCCharacterCreateResult.Builder msgSender = SCCharacterCreateResult.newBuilder(); 
		msgSender.setResultCode(0);
		msgSender.setHumanId(human.getId());
		
		//发送消息
		ConnectionProxy prx = ConnectionProxy.newInstance(obj.connPoint);
		prx.sendMsg(MsgIds.SCCharacterCreateResult, new Chunk(msgSender));
	}
	
	/**
	 * 角色进入游戏请求
	 * @param param
	 */
	@MsgReceiver(CSCharacterLogin.class)
	public void onCSCharacterLogin(MsgParamAccount param) {
		AccountService serv = param.getService();
		long connId = param.getConnId();
		CSCharacterLogin msg = param.getMsg();
		
		AccountObject obj = serv.datas.get(connId);
		
		//登陆游戏
		AccountManager.inst().characterLogin(obj, msg.getHumanId(), false);
	}
	
	/**
	 * 断线重连
	 * @param param
	 */
	@MsgReceiver(CSAccountReconnect.class)
	public void onCSAccountReconnect(MsgParamAccount param) {
		CSAccountReconnect msg = param.getMsg();
		
//		//查询要恢复的数据
//		DBServiceProxy db = DBServiceProxy.newInstance();
//		db.getBy(false, Human.tableName, Human.K.sessionKey, msg.getSessionKey());
//		db.listenResult(this, AccountMsgHandlerCallback._result_onCSAccountReconnect, "msgParam", param);
		
		CallPoint connPoint = param.getConnPoint();
		AccountManager.inst().reconnect(msg.getName(), connPoint);
	}
	
	/**
	 * 断线重连 返回值处理
	 * @param results
	 * @param context
	 */
	public void _result_onCSAccountReconnect(Param results, Param context) {		
		MsgParamAccount msgParam = context.get("msgParam");
		
		//参数
		Long connId = msgParam.getConnId();
		AccountService serv = msgParam.getService();
		ConnectionStatus status = msgParam.getConnStatus();
		CallPoint connPoint = msgParam.getConnPoint();
		
		//返回结果
		Record r = results.get();
		
		//可以恢复？
		boolean recover = true;
				
		//无法通过SessionKey恢复
		if(r == null) recover = false;
		
		//恢复玩家数据
		Human human = new Human(r);
		
		//在尝试看下有没有登陆中的同account数据
		if(recover) {	
			//检查下如果此account正在登陆中 那么就断线重连失败
			//如果玩家已登陆到游戏中，那么sessionKey会改变，所以不用考虑已在游戏中的玩家
			AccountObject accOld = AccountManager.inst().getByAccount(serv, human.getAccount());
			
			if(accOld != null) recover = false;
		}
		
		//返回恢复状态
		SCAccountReconnectResult.Builder sendMsg = SCAccountReconnectResult.newBuilder();
		
		//如果找不到恢复数据 则恢复失败 否则就当做成功
		if(recover) sendMsg.setResultCode(0);
		else sendMsg.setResultCode(-1);
		
		//发送恢复状态消息
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
		prx.sendMsg(MsgIds.SCAccountReconnectResult, new Chunk(sendMsg));
		
		//如果不可恢复 那么流程到此结束
		if(!recover) {
			return;
		}

		//取出会用到的恢复数据
		long humanId = human.getId();
		String account = human.getAccount();
		
		//重建账户数据
		AccountObject obj = new AccountObject(connId, serv, status, connPoint);
		obj.humanId = humanId;
		obj.status.humanId = humanId;
		obj.status.account = account;
		obj.status.status = ConnectionStatus.STATUS_GATE;
		
		serv.datas.put(obj.getId(), obj);
		
		//玩家登陆
		AccountManager.inst().characterLogin(obj, obj.humanId, true);
	}
	
	/**
	 * 请求一个随机名字
	 * @param param
	 */
	@MsgReceiver(CSAccountRandomName.class)
	public void onCSAccountRandomName(MsgParamAccount param) {
		
		CallPoint connPoint = param.getConnPoint();

		//获取随机名字
		AccountManager.inst().randomName(connPoint);
	}
	
	/**
	 * 没选角色前打开公告面板
	 */
	@MsgReceiver(CSBulletinOpenUI.class)
	public void bulletinOpenUI(MsgParamAccount param){
		CallPoint connPoint = param.getConnPoint();
		BulletinManager.inst().openUI(connPoint);
	}
}
