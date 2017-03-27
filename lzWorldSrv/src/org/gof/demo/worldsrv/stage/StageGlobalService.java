package org.gof.demo.worldsrv.stage;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.CallPoint;
import org.gof.core.Chunk;
import org.gof.core.Port;
import org.gof.core.Service;
import org.gof.core.connsrv.ConnectionProxy;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.HumanObjectServiceProxy;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanGlobalServiceProxy;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.item.ItemPack;
import org.gof.demo.worldsrv.msg.Msg.SCStageSwitch;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.google.protobuf.Message;

@DistrClass(
		servId = D.SERV_STAGE_GLOBAL,
		importClass = {HumanObject.class, List.class, Message.class, Vector2D.class}
)
public class StageGlobalService extends Service {
		
	//地图信息集合
	private final Map<Long, StageGlobalInfo> infos = new HashMap<Long,StageGlobalInfo>();
	
	//分线系统 key是主地图的 sn。 value 是对应的所有分线地图
	private final Map<Integer, Map<Long, StageGlobalInfo>> stageMulLines = new HashMap<Integer, Map<Long, StageGlobalInfo>>();
	
	public StageGlobalService(Port port) {
		super(port);
	}
	
	@Override
	public Object getId() {
		return D.SERV_STAGE_GLOBAL;
	}
	
	/**
	 * 注册地图信息
	 * @param stageId
	 * @param stageSn
	 */
	@DistrMethod
	public void infoRegister(long stageId, int stageSn, String stageName, String nodeId, String portId) {
		//创建信息对象并缓存
		StageGlobalInfo info= new StageGlobalInfo(stageId, stageSn, stageName, nodeId, portId);
		infos.put(stageId, info);
		
		//添加分线信息
		ConfMap conf = ConfMap.get(info.sn);
		if(conf.humanMaxNum > 0) {
			Map<Long, StageGlobalInfo> mulLines = stageMulLines.get(stageSn);
			 if(mulLines == null) {
				 mulLines = new LinkedHashMap<Long, StageGlobalInfo>();
				 stageMulLines.put(info.sn, mulLines);
			 } 
			 info.lineNum = mulLines.size();
			 mulLines.put(info.id, info);
			 
		}
		
		//记录创建日志
		if(Log.stageCommon.isInfoEnabled()) {
			Log.stageCommon.info("创建地图：stageId={}, stageSn={}", stageId, stageSn);
		}
		
		Event.fireEx(EventKey.STAGE_REGISTER, stageSn, 
				"stageSn", stageSn, 
				"nodeId", nodeId, 
				"portId", portId, 
				"stageId", stageId);
		
	}
	
	/**
	 * 注销地图信息
	 * @param stageId
	 */
	@DistrMethod
	public void infoCancel(long stageId) {
		StageGlobalInfo information = infos.remove(stageId);
		
		if(information == null) {
			Log.stageCommon.warn("销毁地图时发现地图已不存在：stageId={}", stageId);
			return;
		}
		
		//添加分线信息
		ConfMap conf = ConfMap.get(information.sn);
		if(conf.humanMaxNum > 0) {
			Map<Long, StageGlobalInfo> mulLines = stageMulLines.get(information.sn);
			 if(mulLines != null) {
				 mulLines.remove(information);
			 } 
		}
		
		Event.fireEx(EventKey.STAGE_CANCEL, information.sn, 
				"stageSn", information.sn, 
				"nodeId", port.getCallFromNodeId(), 
				"portId", port.getCallFromPortId(), 
				"stageId", stageId);
	}
	
	@DistrMethod
	public void destroy(long stageId) {
		StageGlobalInfo info = infos.get(stageId);
		if(info == null) {
			return;
		}
		
		StageServiceProxy prx = StageServiceProxy.newInstance(info.nodeId, info.portId, D.SERV_STAGE_DEFAULT);
		prx.destroy(stageId);
	}
	
	@DistrMethod //
	public void login(long humanId, CallPoint connPoint, List<List<?>> lastStageIds, int firstStory) {
		Long pid = port.createReturnAsync();
		ConfMap conf = ConfMap.get((Integer)(lastStageIds.get(0).get(1)));
		
		//这是剧情，跳过
		if(firstStory == -1) {	
			
		} else {
			
			StageGlobalInfo information = null;
			
			//根据玩家记录最后下线点的坐标，来取出生地图
			for (List<?> list : lastStageIds) {
				information = this.infos.get(list.get(0));
				conf = ConfMap.get((Integer)(list.get(1)));
				if(conf.type.equals(StageMapTypeKey.common.getContent()) && information != null) {
					break;
				}
			}
			
			//如果为空，就取默认出生地图
			if(information == null) {
				information = this.infos.get(HumanManager.stageInitSn);  //1是SN
			}

			StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
			proxy.applyStageBySn(information.id);
			proxy.listenResult(this::_result_login,  "pid", pid, "humanId", humanId, "connPoint", connPoint);
		}
	}
	public void _result_login(boolean timeout, Param results, Param context) {
		Long pid = context.get("pid");
		if(timeout) {
			port.returnsAsync(pid, "code", -2000);
			return;
		}
		
		long humanId = context.get("humanId");
		CallPoint connPoint = context.get("connPoint");
		long stageId = results.get("stageId");
		
		StageGlobalInfo information = this.infos.get(stageId);
		StageObjectServiceProxy prx = StageObjectServiceProxy.newInstance(information.nodeId, information.portId, information.id);
		prx.login(humanId, connPoint, information.id, new Vector2D(-1f, -1f));	
		prx.listenResult(this::_result_login1, "pid", pid);
	}
	public void _result_login1(boolean timeout, Param results, Param context) {
		Long pid = context.get("pid");
		if(timeout) {
			port.returnsAsync(pid, "code", -2000);
			return;
		}
		
		port.returnsAsync(pid, results.toArray());
	}

	/**
	 * 地图玩家数量增加
	 * @param stageId
	 */
	@DistrMethod
	public void stageHumanNumAdd(long stageId) {
		stageHumanNumChange(stageId, true);
	}
	
	/**
	 * 地图玩家数量减少
	 * @param stageId
	 */
	@DistrMethod
	public void stageHumanNumReduce(long stageId) {
		stageHumanNumChange(stageId, false);
	}
	
	@DistrMethod
	public void applyStageBySn(long stageId) {
		long result = -1;

		StageGlobalInfo info = infos.get(stageId);
		if(info == null) {
			result = -1;
		}
		
		//地图配置信息
		ConfMap conf = ConfMap.get(info.sn);
		Map<Long, StageGlobalInfo> mulLines = null;
		//判断是否是分线地图
		if(conf.humanMaxNum == 0) {
			result = stageId;
		} else {
			mulLines = stageMulLines.get(info.sn);
			 if(mulLines == null) {
				 result = -1;
			 } else {
				 for (StageGlobalInfo stageGlobalInfo : mulLines.values()) {
					if(stageGlobalInfo.humanNum < conf.humanMaxNum) {
						result = stageGlobalInfo.id;
						break;
					}
				}
			 }
		}
		
		//如果需要分线 并且是一般地图
		if(result < 0 && conf.type.equals(StageMapTypeKey.common.getContent())) {
			//创建新地图
			//获得地图分线数量
			int lineNum = 1;
			if(mulLines != null) {
				lineNum = mulLines.size() + 1;
			} 
			 
			Long pid = port.createReturnAsync();
			StageServiceProxy proxy = StageServiceProxy.newInstance(Distr.getNodeId(conf.portId), conf.portId, D.SERV_STAGE_DEFAULT);
			proxy.createStageCommon(conf.sn, lineNum);
			proxy.listenResult(this::_result_applyStageBySn, "pid", pid, "sn", conf.sn);
		} else {
			port.returns("stageId", result);
		}
		
		if(mulLines != null) {
			//处理删除多余分线的问题
			int mulEnterCount = 0;	//可以进入的分线数量
			StageGlobalInfo emptyInfo = null;
			for (StageGlobalInfo stageGlobalInfo : mulLines.values()) {
				if(stageGlobalInfo.humanNum < conf.humanMaxNum) {
					if(conf.humanMaxNum <= 0) {
						emptyInfo = stageGlobalInfo;
					}
					mulEnterCount += conf.humanMaxNum - stageGlobalInfo.humanNum;
					//如果可进入的数值比较多，并且后面的地图是空的
					if(mulEnterCount - conf.humanMaxNum > 100) {
						StageServiceProxy proxy = StageServiceProxy.newInstance(emptyInfo.nodeId, emptyInfo.portId, D.SERV_STAGE_DEFAULT);
						proxy.destroy(emptyInfo.id);
					}
				}
			}
		}
	}
	
	public void _result_applyStageBySn(boolean timeout, Param results, Param context) {
		long stageId = results.get("stageId");
		String nodeId = results.get("nodeId");
		String portId = results.get("portId");
		
		Long pid = context.get("pid");
		if(timeout) {
			port.returnsAsync(pid, "code", -1000, "reason", "等待返回请求超时");
			return ;
		}
		
		int sn = context.get("sn");
		ConfMap conf = ConfMap.get(sn);
		infoRegister(stageId, sn, conf.name, nodeId, portId);
		
		port.returnsAsync(pid, "stageId", stageId);		
	}
	/**
	 * 地图玩家数量变动
	 * @param stageId
	 * @param add
	 */
	private void stageHumanNumChange(long stageId, boolean add) {
		StageGlobalInfo info = this.infos.get(stageId);
		
		if(info == null) return ;
		
		//地图人数变动
		if(add) {
			info.humanNum = Math.max(0, info.humanNum + 1);
		} else {
			info.humanNum = Math.max(0, info.humanNum - 1);
		}
	}

	@DistrMethod
	public void switchToStage(HumanObject humanObj, long stageTargetId, Vector2D posAppear) {
		Long pid = port.createReturnAsync();
		StageGlobalServiceProxy proxy = StageGlobalServiceProxy.newInstance();
		proxy.applyStageBySn(stageTargetId);
		proxy.listenResult(this::_result_switchToStage1,  "pid", pid, "humanObj", humanObj, "posAppear", posAppear);
	}
	
	public void _result_switchToStage1(Param results, Param context) {
		long stageTargetId = results.get("stageId");
		HumanObject humanObj = context.get("humanObj");
		Vector2D posAppear = context.get("posAppear");
		
		Human human = humanObj.getHuman();
		
		//根据配置重设出生点
		StageGlobalInfo temp = this.infos.get(stageTargetId);
		//常规地图位置
		ConfMap conf = ConfMap.get(temp.sn);

		//玩家上一张地图坐标
		Vector2D posOld = new Vector2D(humanObj.posNow.x, humanObj.posNow.y);
		
		//玩家新地图的坐标
		humanObj.posNow = posAppear;
		
		/* 离开原地图 */
		//从原地图清离,原地图玩家数-1
		StageGlobalInfo infoSource = this.infos.get(humanObj.getStageNowId());	
		if(infoSource != null) {
			HumanObjectServiceProxy prxSource = HumanObjectServiceProxy.newInstance(infoSource.nodeId, infoSource.portId, humanObj.id);
			prxSource.leave();
			stageHumanNumReduce(humanObj.getStageNowId());
		}

		/* 业务逻辑部分 */
		//更新玩家地图路径信息
//		if(conf.humanMaxNum > 0) {
//			HumanManager.inst().recordStage(humanObj, temp.sn, temp.sn, conf.type, posOld);
//		} else {
			HumanManager.inst().recordStage(humanObj, stageTargetId, temp.sn, conf.type, posOld);
//		}
		
		
		/* 进入新地图 */
		StageGlobalInfo infoTarget = this.infos.get(stageTargetId);
		
		//更新玩家全局信息
		HumanGlobalServiceProxy prxHumanStatus = HumanGlobalServiceProxy.newInstance();
		prxHumanStatus.stageIdModify(human.getId(), infoTarget.id, infoTarget.name, infoTarget.nodeId, infoTarget.portId);
		
		//注册玩家至新地图，新地图玩家数+1(业务逻辑只能写在此处前，防止玩家数据提前串行化，到时修改失效)
		StageObjectServiceProxy prxTarget = StageObjectServiceProxy.newInstance(infoTarget.nodeId, infoTarget.portId, infoTarget.id);
		prxTarget.register(humanObj);
		prxTarget.listenResult(this::_result_switchToStage, "infoTarget", infoTarget, "humanObj", humanObj);
		stageHumanNumChange(stageTargetId, true);
	}
	public void _result_switchToStage(boolean timeout, Param results, Param context) {
		StageGlobalInfo infoTarget = context.get("infoTarget");
		HumanObject humanObj = context.get("humanObj");
		Vector2D posNow = results.get("posNow");
		int repSn = results.get("repSn");
		Log.temp.info("_result_switchToStage: {}", posNow);
		/* 消息处理 */
		//返回地图切换消息
		SCStageSwitch.Builder msgSwitch = SCStageSwitch.newBuilder();
		msgSwitch.setStageId(infoTarget.id);
		msgSwitch.setStageSn(infoTarget.sn);
		msgSwitch.setRepSn(repSn);
		msgSwitch.setPos(posNow.toMsg());
		msgSwitch.setLineNum(infoTarget.lineNum);
		SCStageSwitch msg = msgSwitch.build();
		
		//玩家连接
		CallPoint connPoint = humanObj.connPoint;
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint.nodeId, connPoint.portId, connPoint.servId);
		prx.sendMsg(MsgIds.getIdByClass(msg.getClass()), new Chunk(msg));
		
		//更新连接服务器的玩家信息
		prx.updateStatus(infoTarget.nodeId, infoTarget.portId, infoTarget.id);
	}
	
	@DistrMethod
	public void createInstance(int stageSn, int instanceSn) {
		Long pid = port.createReturnAsync();
		
		//地图配置信息
		ConfMap conf = ConfMap.get((int)stageSn);
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(conf.portId), conf.portId, D.SERV_STAGE_DEFAULT);
		prx.createStageInstance(stageSn, instanceSn);
		prx.listenResult(this::_result_createInstance, "pid", pid);
	}
	
	public void _result_createInstance(boolean timeout, Param results, Param context) {
		Long pid = context.get("pid");
		if(timeout) {
			port.returnsAsync(pid, "code", -1000, "reason", "等待返回请求超时");
			return ;
		}
		
		port.returnsAsync(pid, results.toArray());		
	}
	
	@DistrMethod
	public void createTower(int stageSn, int layerSn) {
		Long pid = port.createReturnAsync();
		
		//地图配置信息
		ConfMap conf = ConfMap.get((int)stageSn);
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(conf.portId), conf.portId, D.SERV_STAGE_DEFAULT);
		prx.createStageTower(stageSn, layerSn);
		prx.listenResult(this::_result_createTower, "pid", pid);
	}
	
	public void _result_createTower(boolean timeout, Param results, Param context) {
		Long pid = context.get("pid");
		if(timeout) {
			port.returnsAsync(pid, "code", -1000, "reason", "等待返回请求超时");
			return ;
		}
		
		port.returnsAsync(pid, results.toArray());		
	}
	
	@DistrMethod
	public void createCompetition(HumanObject humanObj,int stageSn, long defenderId) {
		Long pid = port.createReturnAsync();
		
		//地图配置信息
		ConfMap conf = ConfMap.get((int)stageSn);
		StageServiceProxy prx = StageServiceProxy.newInstance(Distr.getNodeId(conf.portId), conf.portId, D.SERV_STAGE_DEFAULT);
		prx.createStageCompetition(humanObj,stageSn, defenderId);
		prx.listenResult(this::_result_createCompetition, "pid", pid);
	}
	
	public void _result_createCompetition(boolean timeout, Param results, Param context) {
		Long pid = context.get("pid");
		if(timeout) {
			port.returnsAsync(pid, "code", -1000, "reason", "等待返回请求超时");
			return ;
		}
		
		port.returnsAsync(pid, results.toArray());		
	}
	
	/**
	 * 退出副本 活动 等地图到进入前的主地图
	 * @param humanObj
	 * @param params
	 */
	@DistrMethod
	public void quitToCommon(HumanObject humanObj, int nowSn, Object...params) {
		
		Param param = new Param(params);
		//是否需要返回
		boolean callBack = Utils.getParamValue(param, "callBack", false);
		
		int mapSn = HumanManager.inst().stageHistoryCommonSn(humanObj);
		ConfMap conf = ConfMap.get(nowSn);
		if(mapSn > 0 && conf!=null && conf.type.equals(StageMapTypeKey.rep.getContent())) {
			Vector2D posAppear = HumanManager.inst().stageHistoryCommon(humanObj);
			switchToStage(humanObj, mapSn, posAppear);
		} else {
			Log.stageCommon.info("StageInstanceManager.leave error {}", mapSn);
		}
		
		
		if(callBack) {
			port.returns();
		}
	}
	
}
