package org.gof.demo.worldsrv.character;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.CallPoint;
import org.gof.core.Chunk;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.Port;
import org.gof.core.connsrv.ConnectionProxy;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.support.Param;
import org.gof.core.support.TickTimer;
import org.gof.core.support.Time;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfCharacterRobot;
import org.gof.demo.worldsrv.config.ConfPropCalc;
import org.gof.demo.worldsrv.config.ConfPropFactor;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Mail;
import org.gof.demo.worldsrv.entity.TowerMirror;
import org.gof.demo.worldsrv.friend.FriendObject;
import org.gof.demo.worldsrv.human.HumanGlobalServiceProxy;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.msg.Msg.DSkill;
import org.gof.demo.worldsrv.msg.Msg.DStageHuman;
import org.gof.demo.worldsrv.msg.Msg.DStageObject;
import org.gof.demo.worldsrv.msg.Msg.DVector3;
import org.gof.demo.worldsrv.msg.Msg.EWorldObjectType;
import org.gof.demo.worldsrv.msg.Msg.SCMsgFill;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * 角色
 */
@DistrClass
public class HumanObject extends CharacterObject {

	public static int CLOSE_DELAY = 10;
	public CallPoint connPoint = new CallPoint();	//连接点信息
	public int loadingNum = 0;								//正在加载玩家数据时的计数器 当等于0时代表加载完毕
	public long loadingPID = 0;								//正在加载玩家数据时的请求ID

	//人物持久化信息
	protected TickTimer m_closeTimer = new TickTimer(); //延迟关闭定时器
	protected TickTimer msgFillTimer = new TickTimer(); //如果CS消息没有返回那么补充SC消失

	//客户端地图状态已准备完毕
	public boolean isClientStageReady;		
	//正在切换地图中
	public boolean isStageSwitching = false;	
	//玩家登陆状态判断 临时属性 0=无状态 1=登陆中 2=今日首次登陆中 
	public int loginStageState;	
	//记录玩家的属性变化，并在本次心跳结束后发送变化至客户端
	private HumanInfoChange humanInfoChange;
	//是否本心跳监控玩家属性变化
	private boolean isHumanInfoListen;
	//在线时间改变计时器	
	public TickTimer onlineTickTimer = new TickTimer(Time.MIN);			
	
	//*   切换场景需要携带的的信息，需要手动写入到 writeTo和readFrom两个方法中   *//
	/*怪物或者人物所带的小弟*/
	public Map<Long, CharacterObject> slaves = new HashMap<>();								//全体的小弟，包含上阵和不上阵的
	public Map<Long, CharacterObject> slavesAttingMap = new HashMap<>();				//上阵的Map		
	public List<CharacterObject> salvesAttingList = new ArrayList<CharacterObject>();		//上阵的武将的排序
	/*好友*/
	public Map<Long, FriendObject> friendList = new HashMap<>();         //好友列表
	public Map<Long, FriendObject> blackList = new HashMap<>();          //黑名单列表
	public Map<Long, FriendObject> applyList = new HashMap<>();          //申请列表
	public FriendObject friendObj;                                       //当别人添加自己时，添加的是这个对象
	/* 聊天 */
	public long informLastSayTime;								//最后一次发言时间
	/* 邮件 */
	public List<Mail> mailList = new ArrayList<Mail>();		//玩家邮件
	/* 级别技能SN：Level */
	public Map<Integer, Integer> relSkills = new HashMap<Integer, Integer>();
	/*伙伴碎片信息*/
	public Map<Integer,Integer> fragInfo = new HashMap<>();  		  //伙伴碎片数量MAP
	public Map<Integer,Integer> rareFragInfo = new HashMap<>();   //可以兑换稀有伙伴的碎片
	/*伙伴副本挑战次数     {副本类型：当日挑战次数}*/
	public Map<Integer,Integer> genTaskFightTimes = new HashMap<>();
	/*伙伴副本挑战时间记录*/
	public long genTaskFightTime = 0;
	/* 组队ID 不可用*/
	public long teamId;
	/* 爬塔信息*/
	public Map<Integer, List<TowerMirror>> towerInfo = new HashMap<>();
	
	/**
	 * 构造函数
	 */
	public HumanObject() {
		super(null);
		for(int i = 0; i < 5 ; i++) {
			salvesAttingList.add(null);
		}
	}
	
	// 主角
	public HumanObject(StageObject stageObj, ConfCharacterRobot conf) {
		super(stageObj);
		this.id = Port.applyId();
		this.sn = String.valueOf(conf.sn);
		this.modelSn = conf.modelSn;
		this.name = conf.name;
//		this.index = index;
//		this.order = confRobot.profession * 1000 + 500;  //这个数字要比 武将的大
		
		//初始化数据
		dataPers.unit = new Human();
		dataPers.unit.setId(id);
		this.getUnit().setName(conf.name);
		this.getUnit().setHpCur(100);
		this.getUnit().setHpMax(100);
		this.getUnit().setMpCur(100);
		this.getUnit().setMpMax(100);
		this.getUnit().setModelSn(this.modelSn);
		this.getUnit().setSn(this.sn);
		this.getUnit().setProfession(conf.profession);
		
		//获得 propBase propFactor
		ConfPropCalc propBase = ConfPropCalc.get(conf.level);
		this.getUnit().setLevel(conf.level);
		if(conf.level > 0) {
			propBase = ConfPropCalc.get(conf.level);
			this.getUnit().setLevel(conf.level);
		}
		ConfPropFactor propFactor = ConfPropFactor.get(conf.propFactor);
		PropCalc basePropCalc = new PropCalc(Utils.toJOSNString(propBase.propName, propBase.propValue));
		basePropCalc.mul(propFactor.propName, propFactor.propValue);
		dataPers.unitPropPlus.setBase(basePropCalc.toJSONStr());
		this.getUnit().setSkill(SkillManager.inst().firstInitSkills(conf.skillGroupSn));
		this.getUnit().setSkillGroupSn(conf.skillGroupSn);
		
		//初始化技能
		SkillManager.inst().initSkill(this);
		
//		UnitManager.inst().propCalc(this, true);
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		super.writeTo(out);
		
		out.write(dataPers);
		out.write(connPoint);
		out.write(informLastSayTime);
		out.write(slaves);
		out.write(slavesAttingMap);
		out.write(salvesAttingList);
		out.write(relSkills);
		out.write(mailList);
		out.write(fragInfo);
		out.write(rareFragInfo);
		out.write(genTaskFightTimes);
		out.write(genTaskFightTime);
		out.write(teamId);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		super.readFrom(in);
		
		dataPers = in.read();
		connPoint = in.read();
		informLastSayTime = in.read();
		slaves = in.read();
		slavesAttingMap = in.read();
		salvesAttingList = in.read();
		relSkills = in.read();
		mailList = in.read();
		fragInfo = in.read();
		rareFragInfo = in.read();
		genTaskFightTimes = in.read();
		genTaskFightTime = in.read();
		teamId = in.read();
	}
	
	@Override
	public void die(UnitObject killer, Param params) {
		super.die(killer, params);
		
		//发送人物死亡事件
		Event.fireEx(EventKey.HUMAN_BE_KILLED, stageObj.sn, "killer", killer, "dead", this);
		
		
	}
	
	public Human getHuman() {
		return (Human)dataPers.unit;
	}
	
	@Override
	public void pulse(int deltaTime) {
		//先执行通用操作
		super.pulse(deltaTime);
		
		if(ai != null) ai.pulse(timeCurr);
		
		if(m_closeTimer.isOnce(timeCurr)) {
			connCloseClear();
		}
		
		if(msgFillTimer.isOnce(timeCurr)) {
			sendScFillMsg();
		}
		
		//客户端是否已完成玩家的加载并登陆到地图前 不做任何操作
		if(!isClientStageReady) {
			return;
		}
//		Log.temp.info("我是一个可爱的小蛇：{}", name);
		
		
		//累加玩家在线时间
		HumanManager.inst().onlineTimePlus(this);
		
		//发送玩家本身属性更新信息至客户端
		if(isHumanInfoListen) {
			isHumanInfoListen = false;
			humanInfoChange.resultForListen(this);
		}
		
	}
	
	@Override
	public void startup() {
		super.startup();
		this.stageEnter(stageObj);
	}
	
	/**
	 * 获取玩家信息，玩家进入地图时能够被其他玩家看到的基础信息
	 */
	@Override
	public DStageObject.Builder createMsg() {
		Human human = getHuman();
		
		//移动中的目标路径
		List<DVector3> runPath = running.getRunPathMsg();
		
		//玩家信息单元
		DStageHuman.Builder h = DStageHuman.newBuilder();
		h.addAllPosEnd(runPath);
		h.setLevel(human.getLevel());
		h.setHpCur(human.getHpCur());
		h.setHpMax(human.getHpMax());
		h.setMpCur(human.getMpCur());
		h.setMpMax(human.getMpMax());
		h.setSex(human.getSex());
		h.setSpeed(human.getSpeed());
		h.setProfession(human.getProfession());
		h.setPvpMode(human.getPvpMode());
		h.setSn(human.getSn());
		h.setInFighting(human.isInFighting());
		h.setTeamBundleID(teamBundleID);
		h.setCanAttack(canAttack);
		h.setCanCastSkill(canCastSkill);
		h.setCanMove(canMove);
		h.setPropJson(getPropPlus().toJSONStr());
		
		List<DSkill> skills = SkillManager.inst().getSkills(this);
		h.addAllSkill(skills);
		
		DStageObject.Builder objInfo = DStageObject.newBuilder();
		objInfo.setObjId(id);
		objInfo.setType(EWorldObjectType.HUMAN);
		objInfo.setName(name);
		
		objInfo.setModelSn(human.getModelSn());
		objInfo.setPos(posNow.toMsg());
		objInfo.setHuman(h);
		
		return objInfo;
	}
	
	
	
	/**
	 * 发送消息至玩家
	 * @param builder
	 */
	public void sendMsg(Builder builder) {
		if(builder == null) return ;
		
		sendMsg(builder.build());
	}
	
	public void sendMsg(List<Builder> builders) {
		if(builders == null || builders.size() <= 0) return ;
		
		List<Integer> idList = new ArrayList<Integer>();
		List<Chunk> chunkList = new ArrayList<Chunk>();
		for (Builder builder : builders) {
			Message msg = builder.build();
			idList.add(MsgIds.getIdByClass(msg.getClass()));
			chunkList.add(new Chunk(msg));
		}
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(idList, chunkList);
		stopMsgTimer();
	}
	
	public void sendMsg(List<Integer> idList, List<Chunk> chunkList) {
		if(idList == null || chunkList == null || idList.size() <= 0) return ;
		
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(idList, chunkList);
		stopMsgTimer();
	}
	
	/**
	 * 发送消息至玩家
	 * @param builder
	 */
	public void sendMsg(Message msg) {
		if(msg == null) return ;
		
		//玩家连接信息
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(MsgIds.getIdByClass(msg.getClass()), new Chunk(msg));
		stopMsgTimer();
	}
	
	
	/**
	 * 将玩家注册到地图中 暂不显示
	 * @param stageObj
	 */
	public void stageRegister(StageObject stageObj) {
		//断线重连等情况下，会出现注册玩家前，可能会残留之前的数据
		//这里调用数据的原坐标，能避免坐标差距，造成客户端人物位置改变。
		HumanObject humanObjOld = stageObj.getHumanObj(id);
		if(humanObjOld != null) {
			posNow.set(humanObjOld.posNow);
		}
		
		//调用父类实现
		super.stageRegister(stageObj);
		
	}
	
	public void connDelayCloseClear() {
		//处理延迟关于 启动关闭后XX 秒才会彻底清除玩家数据
		m_closeTimer.start(CLOSE_DELAY * Time.SEC);
	}
	/**
	 * 玩家下线时进行清理
	 * @param humanObj
	 */
	public void connCloseClear() {
		
		
//		//发布退出事件
		Event.fireEx(EventKey.HUMAN_LOGOUT, stageObj.sn, "humanObj", this);

		//清理
		HumanGlobalServiceProxy hgsprx = HumanGlobalServiceProxy.newInstance();
		hgsprx.cancel(id);
	}
	
	public void clearCloseStatus() {
		m_closeTimer.stop();
	}
	
	/**
	 * 启动消息补偿定时器。 如果服务器没有回SC消息 自己补一个
	 */
	public void startMsgTimer() {
		msgFillTimer.start(200);
	}
	
	public void stopMsgTimer() {
		msgFillTimer.stop();
//		Log.temp.info("stopMsgTimer");
	}
	
	public void sendScFillMsg() {
		SCMsgFill.Builder msg = SCMsgFill.newBuilder();
		sendMsg(msg);
//		Log.temp.info("sendScFillMsg");
	}
	/**
	 * 获取玩家当前stageId
	 * @param human
	 * @return
	 */
	public long getStageNowId() {
		return getStageLastIds().get(0);
	}
	
	/**
	 * 返回玩家之前经历的地图路径的id集合，历史靠近的地图在list的index较小的位置
	 * @param human
	 * @return
	 */
	public List<Long> getStageLastIds() {
		List<Long> res = new ArrayList<>();
		JSONArray ja = JSON.parseArray(getHuman().getStageHistory());
		
		for(Object obj : ja) {
			JSONArray jaTemp = JSON.parseArray(obj.toString());
			res.add(jaTemp.getLongValue(0));
		}
		
		return res;
	}
	
	/**
	 * 获取玩家在地图历史中某张地图的坐标
	 * @param humanObj
	 * @param stageId
	 * @return
	 */
	public Vector2D getStagePos(long stageId) {
		Vector2D vector = new Vector2D(-1, -1);
		Human human = getHuman();
		
		JSONArray ja = JSON.parseArray(human.getStageHistory());
		for(Object obj : ja) {
			JSONArray jaTemp = JSON.parseArray(obj.toString());
			if(stageId == jaTemp.getLongValue(0)) {
				vector.x = jaTemp.getDoubleValue(2);
				vector.y = jaTemp.getDoubleValue(3);
			}
		}
		
		return vector;
	}
	
	/**
	 * 设置当前地图历史路径的x，y坐标
	 * @param x
	 * @param y
	 */
	public void setStagePos(int x, int y) {
		Human human = getHuman();
		
		JSONArray ja = JSON.parseArray(human.getStageHistory());
		JSONArray jaTemp = JSON.parseArray(ja.getString(0));
		jaTemp.set(2, x);
		jaTemp.set(3, y);
		
		ja.set(0, jaTemp);
		
		human.setStageHistory(ja.toJSONString());
	}
	
	/**
	 * 监听玩家的属性变化，并在本次心跳结束后发送变化至客户端
	 */
	public void humanInfoChangeListen() {
		//本次心跳已经添加过监听 忽略新的监听请求
		if(isHumanInfoListen) return ;
		isHumanInfoListen = true;
		
		if(humanInfoChange == null) {
			humanInfoChange = new HumanInfoChange(this);
		}
	}
	
	public CharacterObject getCharacterFromSlave(Long id) {
		if(id == this.id) {
			return this;
		}
		UnitObject unitObj = slaves.get(id);
		if(unitObj instanceof CharacterObject) {
			return (CharacterObject)unitObj;
		} 
			
		return null;
	}
	
	/**
	 * 通过ID活着的自己可以控制的对象， 如果是玩家自己就返回自己，如果是武将那么就返回武将
	 * @param id
	 * @return
	 */
	public UnitObject getUnitControl(long id) {
		if(this.id == id) {
			return this;
		} else {
			return slavesAttingMap.get(id);
		}
	}
	
	/**
	 * 根据武将的SN获取武将的对象
	 * @param generalSn
	 * @return
	 */
	public GeneralObject getGeneralObjectBySn(String generalSn) {
		for (CharacterObject co : this.slaves.values()) {
			GeneralObject genObj = (GeneralObject)co;
			if(genObj.sn.equals(generalSn)){
				return genObj;
			}
		}
		
		return null;
	}
	
	@Override
	public AI getAI() {
		return ai;
	}
	
}
