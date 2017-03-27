package org.gof.demo.seam.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.gof.core.CallPoint;
import org.gof.core.Chunk;
import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.connsrv.ConnectionProxy;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.ConnectionStatus;
import org.gof.core.support.Distr;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.platform.LoginServiceProxy;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.HumanObjectServiceProxy;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import org.gof.demo.worldsrv.human.HumanGlobalServiceProxy;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.SCAccountRandomName;
import org.gof.demo.worldsrv.msg.Msg.SCAccountReconnectResult;
import org.gof.demo.worldsrv.msg.Msg.SCHumanKick;
import org.gof.demo.worldsrv.msg.Msg.SCLoginResult;
import org.gof.demo.worldsrv.msg.Msg.SCMsgFill;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.name.NameServiceProxy;
import org.gof.demo.worldsrv.stage.StageGlobalServiceProxy;
import org.gof.demo.worldsrv.support.C;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class AccountManager extends ManagerBase {	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static AccountManager inst() {
		return inst(AccountManager.class);
	}
	
	/**
	 * 玩家登陆 在此处验证玩家的身份
	 * 根据当前逻辑此处只传递用户名即可 永远验证通过 以后根据业务需求再修改
	 * 如果账号之前已登录，那么就踢掉之前的玩家连接，本次登录也算失败。
	 * @param account
	 */
	public void login(CallPoint connPoint, ConnectionStatus connStatus, String account, String pass, String token) {
		
		//角色名为空，直接返回
		if(StringUtils.isEmpty(account)){
			ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
			SCLoginResult.Builder reply = SCLoginResult.newBuilder();
			reply.setResultCode(-1);
			reply.setResultReason("账号为空");
			prx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply));
			return;
		}
		
		//账号一律使用小写
		account = account.toLowerCase();
		
		//连接一个随机的验证服务
		String portLogin = D.PORT_PLATFORM_LOGIN_PREFIX + new Random().nextInt(D.NODE_WORLD_STARTUP_PLATFORM_LOGIN);
		LoginServiceProxy loginServ = LoginServiceProxy.newInstance(D.NODE_PLATFORM, portLogin, D.SERV_PLATFORM_LOGIN);
		loginServ.check(account, token);	
		loginServ.listenResult(this::_result_login, "connPoint", connPoint, "connStatus", connStatus, "account", account);
		
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
		SCMsgFill.Builder reply = SCMsgFill.newBuilder();
		prx.sendMsg(MsgIds.SCMsgFill, new Chunk(reply));
	}
	public void _result_login(Param results, Param context) {
		//登陆验证结果
		boolean result = results.getBoolean();
		if(!result) {
			CallPoint connPoint = context.get("connPoint");
			
			//返回登录失败
			ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
			SCLoginResult.Builder reply = SCLoginResult.newBuilder();
			reply.setResultCode(-1);
			reply.setResultReason(I18n.get("account.login.checkFail"));
			prx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply));
			return;
		}
		
		//先检验玩家是否已登录
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.isLogined(context.getString("account"));
		prx.listenResult(this::_result_login2, context);
	}
	public void _result_login2(Param results, Param context) {
		CallPoint connPoint = context.get("connPoint");
		String account = context.get("account");
		ConnectionStatus status = context.get("connStatus");
		
		//环境 这里应该只有account会调用此函数
		Port port = Port.getCurrent();
		AccountService serv = port.getService(Distr.SERV_GATE);

		//检查下之前是否有登陆中的玩家 有就踢了
		//这里仅踢登陆中的 后面会踢在游戏中的玩家
		AccountObject accOld = getByAccount(serv, account);
		if(accOld != null) {
			ConnectionProxy connPrx = ConnectionProxy.newInstance(accOld.connPoint);
			
			//提示另一个玩家 被踢
			SCHumanKick.Builder kickMsg = SCHumanKick.newBuilder();
			kickMsg.setReason(I18n.get("account.login.kickWithRepeat"));
			
			//发消息
			connPrx.sendMsg(MsgIds.SCHumanKick, new Chunk(kickMsg));
			
			//断开另一个玩家的连接
			connPrx.close();
		}
		
		//如果账号之前已登录 则踢出 本次登录也失败
		boolean logined = results.get("logined");
		long loginedHumanId = results.get("humanId");
		if(logined) {
			//踢出玩家
			HumanGlobalServiceProxy hgPrx = HumanGlobalServiceProxy.newInstance();
			hgPrx.kick(loginedHumanId, I18n.get("account.login.kickWithRepeat"));
			
			//消息
			SCLoginResult.Builder reply = SCLoginResult.newBuilder();
			reply.setResultCode(-1);
			reply.setResultReason(I18n.get("account.login.humanIsLogined"));
			
			//返回登录失败
			ConnectionProxy connPrx = ConnectionProxy.newInstance(connPoint);
			connPrx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply));
			
			return;
		}
		
		//获取连接代理
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
		
		//更新链接状态
		status.status = ConnectionStatus.STATUS_GATE;
		status.account = account;
		prx.updateStatus(status);
		
		//注册玩家消息
		long connId = (long) connPoint.servId;
		AccountObject obj = new AccountObject(connId, serv, status, connPoint);
		serv.datas.put(obj.getId(), obj);
		
		//发送登录请求结果
		SCLoginResult.Builder reply = SCLoginResult.newBuilder();
		reply.setResultCode(C.CLIENT_VERSION);
		prx.sendMsg(MsgIds.SCLoginResult, new Chunk(reply));
	}
	
	/**
	 * 查询玩家角色信息
	 */
	public void queryCharacters(AccountObject accObj, String account) {
		DBServiceProxy dbProxy = DBServiceProxy.newInstance();
		dbProxy.findBy(false, Human.tableName, "account", account);
		dbProxy.listenResult(this::_result_queryCharacters, "gateObj", accObj);
	}
	
	public void _result_queryCharacters(Param results, Param context) {
		AccountObject gateObj = context.get("gateObj");
		
		Msg.SCQueryCharactersResult.Builder builder = Msg.SCQueryCharactersResult.newBuilder();
		
		List<Record> records = results.get();
		for(Record r : records) {
			//玩家信息
			Human human = new Human(r);
			
			Msg.DCharacter.Builder info = Msg.DCharacter.newBuilder();
			info.setId(human.getId());
			info.setLevel(human.getLevel());
			info.setName(human.getName());
			info.setProfession(human.getProfession());
			info.setSex(human.getSex());
			info.setSn(human.getSn());
			builder.addCharacters(info);
		}
		
		ConnectionProxy prx = ConnectionProxy.newInstance(gateObj.connPoint);
		prx.sendMsg(MsgIds.SCQueryCharactersResult, new Chunk(builder));
	}
	
	/**
	 * 角色登陆
	 * @param accObj
	 * @param humanId
	 * @param reconnect false=正常登陆 true=断线重连
	 */
	public void characterLogin(AccountObject accObj, long humanId, boolean reconnect) {
		accObj.humanId = humanId;

		//获取玩家的地图ID
		DBServiceProxy db = DBServiceProxy.newInstance();
		db.getBy(false, Human.tableName, "id", humanId, "account", accObj.status.account);
		db.listenResult(this::_result_characterLogin, "accObj", accObj, "reconnect", reconnect);
	}
	
	public void _result_characterLogin(Param results, Param context) {
		Record r = results.get();
		AccountObject accObj = context.get("accObj");
		boolean reconnect = context.get("reconnect");
		
//		Log.temp.info("人物已经登录：连接ID={}, HumanID={}", accObj.getId(), accObj.humanId);
		
//		//获取并处理地图历史路径
		String stageHistory = r.get("stageHistory");
		List<List<?>> stageIdsHistory = new ArrayList<>();
		JSONArray ja = JSON.parseArray(stageHistory);
		for(Object obj : ja) {
			JSONArray jaTemp = JSON.parseArray(obj.toString());
			List<?> tmp = Utils.ofList(jaTemp.getLongValue(0), jaTemp.getIntValue(1), jaTemp.getDoubleValue(2), jaTemp.getDoubleValue(3));
			
			stageIdsHistory.add(tmp);
		}
		
		//根据地图历史路径获取可登陆地图
		StageGlobalServiceProxy prx1 = StageGlobalServiceProxy.newInstance();
		prx1.login(accObj.humanId, accObj.connPoint, stageIdsHistory, 0);
		prx1.listenResult(this::_result_characterLogin2, "accObj", accObj, "reconnect", reconnect);
	}
	public void _result_characterLogin2(Param results, Param context) {
		AccountObject accObj = context.get("accObj");
		boolean reconnect = context.get("reconnect");
		
		//连接代理
		ConnectionProxy prxConn = ConnectionProxy.newInstance(accObj.connPoint);
		
		//应答客户端
		if(!reconnect) {
			Msg.SCCharacterLoginResult.Builder msgResult = Msg.SCCharacterLoginResult.newBuilder();
			msgResult.setResultCode(0);
			prxConn.sendMsg(MsgIds.SCCharacterLoginResult, new Chunk(msgResult));
		}
		
		//更新连接状态
		String node = results.get("node");
		String port = results.get("port");
		
		ConnectionStatus status = accObj.status;
		status.humanId = accObj.humanId;
		status.stageNodeId = node;
		status.stagePortId = port;
		status.status = ConnectionStatus.STATUS_PLAYING;
		
		prxConn.updateStatus(status);
		
		//清理当前的缓存数据
		accObj.serv.datas.remove(accObj.getId());
	}
	
	/**
	 * 在登陆信息中 查找同account的数据
	 * @param serv
	 * @param account
	 * @return
	 */
	public AccountObject getByAccount(AccountService serv, String account) {
		//遍历寻找登陆中的玩家信息 看有没有同account的
		for(AccountObject o : serv.datas.values()) {
			if(account.equals(o.status.account)) {
				return o;
			}
		}
		
		return null;
	}
	
	public void reconnect(String account, CallPoint connPoint) {
		//从humanGlobal 中获得人物的连接 
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.isLogined(account);
		prx.listenResult(this::_result_reconnect, "connPoint", connPoint);
	}
	
	public void _result_reconnect(Param results, Param context) {
		CallPoint connPointNew = context.get("connPoint");
		HumanGlobalInfo humanInfo = results.get("humanInfo");
		boolean logined = results.get("logined");
		if(!logined) {
			SCAccountReconnectResult.Builder sendMsg = SCAccountReconnectResult.newBuilder();
			//不能重新连接 直接走断线
			sendMsg.setResultCode(-1);
			//发送恢复状态消息
			ConnectionProxy prx = ConnectionProxy.newInstance(connPointNew);
			prx.sendMsg(MsgIds.SCAccountReconnectResult, new Chunk(sendMsg));
			return;
		}
		
		
		//打开新的发送新的人物连接 并更新conn 的状态
		ConnectionProxy connPrxNew = ConnectionProxy.newInstance(connPointNew);
		ConnectionStatus status = new ConnectionStatus();
		status.account = humanInfo.account;
		status.humanId = humanInfo.id;
		status.stageNodeId = humanInfo.nodeId;
		status.stagePortId = humanInfo.portId;
		status.status = ConnectionStatus.STATUS_PLAYING;
		connPrxNew.updateStatus(status);
		//copy 旧的连接 缓冲过来
		connPrxNew.initMsgBuf(humanInfo.connPoint);
		
		//关闭原来的人物连接
		ConnectionProxy connPrxOld = ConnectionProxy.newInstance(humanInfo.connPoint);
		connPrxOld.close();
//		Log.temp.info("connPrxOld {} {} ", humanInfo.connPoint.servId, Port.getTime());
		
		//更新humanObject的conn
		HumanObjectServiceProxy prxHumanObj = HumanObjectServiceProxy.newInstance(humanInfo.nodeId, humanInfo.portId, humanInfo.id);
		prxHumanObj.ChangeConnPoint(connPointNew);
//		Log.temp.info("connPointNew {} {} ", connPointNew.servId, Port.getTime());
		//更新humanGlobal对应的HumanObject 的conn
		HumanGlobalServiceProxy prxHumanGol = HumanGlobalServiceProxy.newInstance();
		prxHumanGol.ChangeConnPoint(humanInfo.id, connPointNew);
		
		//发送缓冲数据
		connPrxNew.sendMsgBuf();
		//返回恢复状态
		SCAccountReconnectResult.Builder sendMsg = SCAccountReconnectResult.newBuilder();
		sendMsg.setResultCode(1);
		//发送恢复状态消息
		connPrxNew.sendMsg(MsgIds.SCAccountReconnectResult, new Chunk(sendMsg));
		
		
	}
	
	@Listener(EventKey.HUMAN_LOGIN_FINISH)
	public void onHumanLogin(Param param) {
		HumanObject humanObj = param.get("humanObj");		
		Human human = humanObj.getHuman();
		
		human.setSessionKey((long)humanObj.connPoint.servId);
		//这里做同步操作 避免登陆根据sessionKey查询时需要刷新写缓存
		human.update(true);
	}
	
	/**
	 * 随机获取名字
	 * @return
	 */
	public void randomName(CallPoint connPoint) {
		
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.randomName();
		prx.listenResult(this::_result_randomNameRepeat, "connPoint", connPoint);
	}
	public void _result_randomNameRepeat(Param results, Param context) {
		// 上下文环境
		CallPoint connPoint = context.get("connPoint");
		
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
		SCAccountRandomName.Builder reply = SCAccountRandomName.newBuilder();
		reply.setName(results.get("randomName"));
		prx.sendMsg(MsgIds.SCAccountRandomName, new Chunk(reply));
	}
	
}