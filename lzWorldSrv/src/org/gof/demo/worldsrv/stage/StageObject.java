package org.gof.demo.worldsrv.stage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gof.core.CallPoint;
import org.gof.core.Port;
import org.gof.core.support.Distr;
import org.gof.core.support.TickTimer;
import org.gof.demo.battlesrv.stageObj.StageObjectBattle;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.HumanObjectService;
import org.gof.demo.worldsrv.character.MonsterObject;
import org.gof.demo.worldsrv.config.ConfStageScore;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.scene.SceneManager;
import org.gof.demo.worldsrv.scene.ScenePlot;
import org.gof.demo.worldsrv.scene.SceneTrigger;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;

public class StageObject extends StageObjectBattle{

	public double cellWidth = 32;				//单元格宽
	public double cellHeight = 32;				//单元格高
	int w;								//单元格总宽
	int h;								//单元格总高

	public long LineId;			//地图分线的原始ID //如果是分线地图那么就是 SN 如果不是分线地图就是ID
	public int lineNum = 1;		//地图分线号
	public String name;				//地图名称
	private StagePort port;			//所属Port
	private StageCell [][] cells;		//该地图内所有地图块
	
	//场景事件触发结构
	public Map<Long, ScenePlot> plotMap = new HashMap<>();			//所有的数据结构
	public String plotTriggerCurr = "";			//当前的进行中的剧情触发器
	public String plotFinish = "";			//已经完成的
	public Map<Long, SceneTrigger> plotMoveTrigger = new HashMap<>();	//移动的trigger，用于优化处理，移动比较频繁
	public Map<Long, SceneTrigger> plotTriggerAll = new HashMap<>();			//所有的触发器
	
	//奖励缓存
	public List<ProduceVo> itemProduce = new ArrayList<ProduceVo>();

	private Map<Long, HumanObject> humanObjs = new HashMap<>();			//该地图内所有人
	private Map<Long, MonsterObject> monsterObjs = new HashMap<>();		//该地图内所有怪物 
	private Map<Long, GeneralObject> generalObjs = new HashMap<>();		//该地图内所有武将
	
	public List<StageStarVo> stageScoreList = new ArrayList<>();			//副本评分相关
	
	//该地图内，被干死的怪
	public Set<Integer> monsterDies = new HashSet<>();	
	
	//马上要生成怪的列表，生完后清空
	public int[] monsterToBirth;

	private TickTimer msgPulseTimer; //控制广播发送频率

	public StageObject(StagePort port, long stageId, int stageSn) {
		super(stageId, stageSn);

		this.port = port;

		if(conf.humanMaxNum > 0) {
			this.LineId = stageSn;
		} else {
			this.LineId = stageId;
		}
		this.name = conf.name;
		this.msgPulseTimer = new TickTimer(conf.msgPulseTime);

		//如果是副本，就初始化场景事件
		if(conf.type.equals(StageMapTypeKey.rep.getContent())) {
			SceneManager.inst().repSceneInit(this);
		}
		
		ConfStageScore confStageScore = ConfStageScore.get(stageSn);
		if(confStageScore!=null){
			stageScoreList = StageManager.inst().initStageScore(confStageScore);
		}
	}

	/**
	 * 建立对应的service,stageObject的子类需要自己的
	 * service的时候，可以覆盖此方法
	 */
	public StageObjectService createService(Port port) {
		//设置服务接口
		return new StageObjectService(this, port);
	}

	/**
	 * 地图初始化
	 */
	public void startup() {
		//创建服务此stageobject的service
		StageObjectService serv = createService(port);
		serv.startupLocal();

		//设置服务接口
		port.addService(serv);

		//将地图进行全局注册
		StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
		prx.infoRegister(id, sn, name, Distr.getNodeId(port.getId()), port.getId());

		//将地图进行单元格分割
		w = (int)(Math.ceil(width / cellWidth));
		h = (int)(Math.ceil(height / cellHeight ));
		cells = new StageCell[h][w];
		for(int j = 0; j < w; j++) {
			for(int i = 0; i < h; i++) {
				StageCell cell = new StageCell(sn, i, j);
				cells[i][j] = cell;
			}
		}

//		createMosnter();
	}

	/**
	 * 销毁地图
	 */
	public void destory() {
		//删除服务接口
		port.delServiceBySafe(id);

		//将地图从全局信息中删除
		StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
		prx.infoCancel(id);
	}


	public int getRepSn() {
		return -1;
	}

	@Override
	public void pulse() {
		super.pulse();
		//执行各单位本次心跳中的事务
		long curr = Port.getTime();

		if(msgPulseTimer.isPeriod(curr)) {
			//心跳所有stageCell中的消息
			for(int i = 0 ; i < h ; i++) {
				for(int j = 0 ; j < w ; j++) {
					cells[i][j].sendMsg();
				}
			}
		}
	}

	public StagePort getPort() {
		return port;
	}
	
	public void createMosnter() {
		StageManager.inst().createObjByConf(this);
	}

	public void login(long humanId, CallPoint connPoint, long stageId, Vector2D stagePos) {
		//玩家数据
		HumanObject humanObj = new HumanObject();
		humanObj.id = humanId;
		humanObj.connPoint = connPoint;
		humanObj.stageObj = this;
		humanObj.posNow = stagePos;
		humanObj.teamBundleID = humanId;

		//玩家登录游戏
		HumanManager.inst().loadData(humanObj);
	}

	/**
	 * 获取玩家对象
	 * @param humanId
	 * @return
	 */
	public HumanObject getHumanObj(long humanId) {
		return humanObjs.get(humanId);
	}

	/**
	 * 获取怪物对象
	 * @param humanId
	 * @return
	 */
	public MonsterObject getMonsterObj(long monsterId) {
		return monsterObjs.get(monsterId);
	}
	public GeneralObject getGeneralObj(long genId) {
		return generalObjs.get(genId);
	}

	public Map<Long, HumanObject> getHumanObjs() {
		return humanObjs;
	}

	public Map<Long, MonsterObject> getMonsterObjs() {
		return monsterObjs;
	}

	public Map<Long, GeneralObject> getGeneralObjs() {
		return generalObjs;
	}
	
	/**
	 * 添加地图单元
	 * 不要直接调用本函数 请使用WorldObject.stageRegister()
	 * @param obj
	 */
	@Override
	public void _addWorldObj(WorldObject obj) {
		super._addWorldObj(obj);
		/* 设置地图与活动单元的关系 */
		obj.stageObj = this;

		//记录玩家
		if(obj instanceof HumanObject) {
			HumanObject ho = (HumanObject) obj;
			humanObjs.put(obj.id, ho);
			//设置访问玩家服务接口
			HumanObjectService humanObjServ = new HumanObjectService(ho, port);
			humanObjServ.startup();
			port.addService(humanObjServ);
		}

		//记录怪物
		if(obj instanceof MonsterObject) {
			monsterObjs.put(obj.id, (MonsterObject)obj);
		}

		if(obj instanceof GeneralObject) {
			generalObjs.put(obj.id, (GeneralObject)obj);
		}
		
		/* 设置地图格与活动单元的关系 */
		StageCell cell = getCell(obj.posNow);
		cell.addWorldObject(obj);
		obj.stageCell = getCell(obj.posNow);

		//日志
		if(Log.stageCommon.isDebugEnabled()) {
			Log.stageCommon.debug("将活动单元注册至地图中：stage={}, obj={}", this, obj);
		}
	}

	/**
	 * 删除地图单元
	 * 不要擅自调用这个接口 移除地图单元建议调用WorldObject.stageLeave()接口
	 * @param obj
	 */
	@Override
	public void _delWorldObj(WorldObject obj) {
		super._delWorldObj(obj);
		/* 解除地图与活动单元的关系 */
		obj.stageObj = null;

		//删除玩家
		if(obj instanceof HumanObject) {
			humanObjs.remove(obj.id);

			//删除访问玩家服务接口
			//只有当接口中的HumanObject与要删除的是同一内存对象时才进行删除
			//否则由于清理的延迟操作，在切换地图等情况下会造成：
			//标记为清理 -> 注册至地图 -> 进行真实清理（造成了service丢失的情况）
			HumanObjectService serv = port.getService(obj.id);
			if(serv != null && serv.getHumanObj() == obj) {
				port.delService(obj.id);
			}
		}

		//删除怪物
		if(obj instanceof MonsterObject) {
			monsterObjs.remove(obj.id);
		}

		/* 解除地图格与活动单元的关系 */
		if(obj.stageCell != null) {
			obj.stageCell.delWorldObject(obj);
			obj.stageCell = null;
		}

		//日志
		if(Log.stageCommon.isDebugEnabled()) {
			Log.stageCommon.debug("将活动单元从地图中删除：stage={}, obj={}", this, obj);
		}
		
	}

	public StageCell getCell(Vector2D pos) {
		int j = (int)Math.floor(pos.x / cellWidth);
		int i = (int)Math.floor(pos.y / cellHeight);
		StageCell cell = getCell(i, j);

		//发现错误坐标
		if(cell == null) {
			Log.stageCommon.error("无法通过坐标获取到对应的地图Cell：stageSn={}, vector={}, starckTrace={}", sn, pos, ExceptionUtils.getStackTrace(new Throwable()));
		}

		return cell;
	}

	public StageCell getCell(int i, int j) {
		if(i < 0 || j < 0 || i >= cells.length || j >= cells[0].length) {
			return null;
		}

		return cells[i][j];
	}

}
