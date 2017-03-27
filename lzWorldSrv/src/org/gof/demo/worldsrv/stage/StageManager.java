package org.gof.demo.worldsrv.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gof.core.Chunk;
import org.gof.core.Node;
import org.gof.core.Port;
import org.gof.core.scheduler.ScheduleTask;
import org.gof.core.support.Distr;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.MonsterObject;
import org.gof.demo.worldsrv.config.ConfCharacterMonster;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.config.ConfScene;
import org.gof.demo.worldsrv.config.ConfSceneCharacter;
import org.gof.demo.worldsrv.config.ConfStageScore;
import org.gof.demo.worldsrv.monster.MonsterManager;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.SCStageEnterResult;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectAppear;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectDisappear;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.MathUtils;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.observer.EventKey;
import org.gof.demo.worldsrv.support.pathFinding.HeightFinding;
import org.gof.demo.worldsrv.support.pathFinding.PathFinding;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;


public class StageManager extends ManagerBase {
	
	/**
	 * 获取实例
	 * @return
	 */
	public static StageManager inst() {
		return inst(StageManager.class);
	}
	
	/**
	 * 游戏启动时 创建主地图
	 * @param params
	 * @throws Exception 
	 */
	@Listener(EventKey.GAME_STARTUP_BEFORE)
	public void onGameStartupBefore(Param params) {
		try{
		Node node = params.get("node");
		
		//初始化地图
		for(int i = 0; i < D.PORT_STAGE_STARTUP_NUM; ++i) {
			//拼PortId
			String portId = D.PORT_STAGE_PREFIX + i;
			
			//验证启动Node
			String nodeId = Distr.getNodeId(portId);
			if(!node.getId().equals(nodeId)) {
				continue;
			}
			
			//创建地图Port
			StagePort portStage = new StagePort(portId);
			portStage.startup(node);
			
			//默认服务
			StageService stageServ = new StageService(portStage);
			stageServ.startup();
			portStage.addService(stageServ);
			
			//加载下属主地图
			List<ConfMap> stages = ConfMap.findBy(ConfMap.K.portId, portId, ConfMap.K.type, "common");
			for(ConfMap s : stages) {
				portStage.createCommonSafe(s.sn);
			}
			
		}
		}catch(Exception e){
			throw new SysException(e);
		}
	}
	
	/**
	 * 创建普通地图
	 * @param stageSn
	 */
	public StageObject createCommon(int stageSn) {
		StagePort port = (StagePort)Port.getCurrent();

		StageObject stage = new StageObject(port, stageSn, stageSn);
		stage.startup();
		
		return stage;
	}
	
	/**
	 * 摧毁地图
	 * @param gameId
	 */
	public void destory(long gameId) {
		StagePort port = (StagePort)Port.getCurrent();
		StageObject stage = port.getStageObject(gameId);
		if(stage != null) {
			stage.destory();
		}
	}
	
	
	/**
	 * 发送消息至玩家
	 */
	public void sendMsgToHumans(Builder builder, Collection<HumanObject> humans) {
		for(HumanObject human : humans) {
			human.sendMsg(builder);
		}
	}
	
	/**
	 * 发送消息至地图中的全部玩家
	 */
	public void sendMsgToStage(Builder builder, StageObject stageObj) {
		sendMsgToHumans(builder, stageObj.getHumanObjs().values());
	}

	/**
	 * 发送消息至九宫格中的全部玩家
	 */
	public void sendMsgToArea9grid(Builder builder, StageObject stageObj, Vector2D pos) {
		sendMsgToHumans(builder, getHumanObjsInArea(stageObj, pos));
	}
	
	public void sendMsgToArea(Builder builder, StageObject stageObj, Vector2D pos) {
//		sendMsgToHumans(builder, getHumanObjsInArea(stageObj, pos));
		//获取九宫格
		StageCell cell = stageObj.getCell(pos);
		if(cell == null) return;
		List<StageCell> cells = getCellsInArea(stageObj, cell);
		
		Message msg = builder.build();
		Chunk ch = new Chunk(msg);
		int id = MsgIds.getIdByClass(msg.getClass());
		for(StageCell c : cells) {
			c.idList.add(id);
			c.chunkList.add(ch);
		}
	}
	
	/**
	 * 获取某张地图中以某个地图格为中心的九宫格
	 * @param stageObjId
	 * @param cellId
	 * @return
	 */
	public List<StageCell> getCellsInArea(StageObject stageObj, StageCell cell) {
		List<StageCell> result = new ArrayList<>();
		
		int i = cell.i;
		int j = cell.j;
		
		int [] is = {i - 1, i, i + 1};
		int [] js = {j - 1, j, j + 1};
		for(int y : js) {
			for(int x : is) {
				StageCell temp = stageObj.getCell(x, y);
				if(temp == null) continue;
				result.add(temp);
			}
		}
		return result;
	}
	
	/**
	 * 获得stageCell中的玩家
	 * @param stageObj
	 * @param pos
	 * @return
	 */
	public List<HumanObject> getHumanObjsInArea(StageObject stageObj, Vector2D pos) {
		List<HumanObject> result = new ArrayList<>();
		if(stageObj == null) {
			return result;
		}
		//获取九宫格
		StageCell cell = stageObj.getCell(pos);
		List<StageCell> cells = getCellsInArea(stageObj, cell);
		
		//在九宫格中获取玩家
		for(StageCell c : cells) {
			for(HumanObject ho : c.getHumans().values()) {
				if(ho.isInWorld()) {
					result.add(ho);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 地图单元跨地图格，不只是人，还可能是怪物等
	 * @param cellBegin
	 * @param cellEnd
	 * @param objId
	 */
	public void cellChanged(StageCell cellBegin, StageCell cellEnd, WorldObject obj) {
		//不是同一张地图内
		if(!cellBegin.isInSameStage(cellEnd)) {
			throw new SysException("严重错误！地图单元跨不同地图的地图格");
		}
		//没跨格
		if(cellBegin.equals(cellEnd)) return;
		
		StageObject stageObj = obj.stageObj;
		if(Log.stageMove.isDebugEnabled()) {
			Log.stageMove.debug("地图单元跨地图格子了，objId={}，cellBegin={}，cellEnd={}", obj.id, cellBegin.i + "," + cellBegin.j, cellEnd.i + "," + cellEnd.j);
		}
		//把obj从旧的区域移走，加入到新的区域
		cellBegin.delWorldObject(obj);
		cellEnd.addWorldObject(obj);
		
		//通知旧的区域有地图单位离开
		List<StageCell> cellsLeave = getCellsChangedLeave(stageObj, cellBegin, cellEnd);
		SCStageObjectDisappear.Builder msgObjDisappear = obj.createMsgDisappear();
		for(StageCell cell : cellsLeave) {
			for(HumanObject humanObj : cell.getHumanObjects().values()) {
				if(humanObj.id == obj.id) continue;
				//发送消息
				humanObj.sendMsg(msgObjDisappear);
				if(Log.stageMove.isInfoEnabled()) {
					Log.stageMove.info("移动单元{}从{}的视野中消失", obj.id, humanObj.name);
				}
			}
		}
		
		//通知新的区域有物体进入
		SCStageObjectAppear.Builder msgObjAppear = obj.createMsgAppear(1);
		
		List<StageCell> cellsNew = this.getCellChangedEnter(stageObj, cellBegin, cellEnd);
		for(StageCell cell : cellsNew) {
			for(HumanObject humanObj : cell.getHumanObjects().values()) {
				if(humanObj.id == obj.id) continue;
				//发送消息
				humanObj.sendMsg(msgObjAppear);
				if(Log.stageMove.isInfoEnabled()) {
					Log.stageMove.info("移动单元{}名字为{}出现在{}的视野中", obj.id, humanObj.name);
				}
			}
		}
		
		//给玩家发送周围信息
		if(obj instanceof HumanObject) {
			HumanObject humanObj = (HumanObject)obj;
			//离开格子中的东西
			for(StageCell cell : cellsLeave) {
				for(WorldObject o : cell.getWorldObjects().values()) {
					humanObj.sendMsg(o.createMsgDisappear());
				}
			}
			//新增格子中的东西
			for(StageCell cell : cellsNew) {
				for(WorldObject wo : cell.getWorldObjects().values()) {
					if(wo.id == obj.id) continue;
					if(!wo.isInWorld()) continue;
					humanObj.sendMsg(wo.createMsgAppear(1));
				}
			}
		}
		
		
		
	}
	
	/**
	 * 获取进入新区域后，新九宫格对比旧九宫格的增加区域。
	 * @param stageObjId
	 * @param cellBegin
	 * @param cellEnd
	 * @return
	 */
	private List<StageCell> getCellChangedEnter(StageObject stageObj, StageCell cellBegin, StageCell cellEnd) {
		//新旧区域
		List<StageCell> begin = getCellsInArea(stageObj, cellBegin);
		List<StageCell> end = getCellsInArea(stageObj, cellEnd);

		//取出新增部分
		List<StageCell> result = new ArrayList<>();
		for(StageCell c : end) {
			if(begin.contains(c))
				continue;

			result.add(c);
		}

		return result;
	}
	
	/**
	 * 获取进入新区域后，新九宫格对比旧九宫格减少的区域。
	 * @param stageObjId
	 * @param cellBegin
	 * @param cellEnd
	 * @return
	 */
	private List<StageCell> getCellsChangedLeave(StageObject stageObj, StageCell cellBegin, StageCell cellEnd) {
		//新旧区域
		List<StageCell> begin = getCellsInArea(stageObj, cellBegin);
		List<StageCell> end = getCellsInArea(stageObj, cellEnd);
		
		//取出减少部分
		List<StageCell> result = new ArrayList<>();
		for(StageCell c : begin) {
			if(end.contains(c)) continue;
			
			result.add(c);
		}
		
		return result;
	}
	
	
	/**
	  * 通过坐标来获取区九宫格域内所有单元
	 * @param stageObj
	 * @param pos
	 * @return
	 */
	public List<WorldObject> getWorldObjsInArea(StageObject stageObj, Vector2D pos) {
		List<WorldObject> result = new ArrayList<>();
		//获取九宫格
		StageCell cell = stageObj.getCell(pos);
		List<StageCell> cells = this.getCellsInArea(stageObj, cell);
		
		//在九宫格中获取地图单元
		for(StageCell c : cells) {
			for(WorldObject wo : c.getWorldObjects().values()) {
				result.add(wo);
			}
		}
		
		return result;
	}
	
	
	/**
	 * 将玩家拉到当前地图上的某个位置
	 * @param humanObj
	 * @param vector
	 */
	public void pullTo(HumanObject humanObj, Vector2D vector) {
		//原地点发送消失消息
		humanObj.stageHide();
		
		humanObj.posNow = vector;
		
		//通知前端刷新到当前点
		Msg.SCStagePullTo.Builder msgPull = Msg.SCStagePullTo.newBuilder();
		msgPull.setPos(humanObj.posNow.toMsg());
		humanObj.sendMsg(msgPull.build());
		
		SCStageEnterResult.Builder msgER = SCStageEnterResult.newBuilder();
		for(WorldObject o : getWorldObjsInArea(humanObj.stageObj, humanObj.posNow)) {
			if(!o.isInWorld()) continue;
			if(o.equals(humanObj)) continue;

			msgER.addObj(o.createMsg());
		}
		humanObj.sendMsg(msgER);
		
		//从现在的地点出现
		humanObj.stageShow();
	}
	
	/**
	 * 切换地图,尽量用此处方法，这里会加延迟切换并且改变玩家状态
	 * @param humanObj
	 * @param stageTargetId
	 * @param posAppear						//切换地图后的出现位置，为null的话出现在默认位置
	 */
	public void switchTo(final HumanObject humanObj, final long stageTargetId, final Vector2D posAppear) {
		//正在切地图状态中
		if(humanObj.isStageSwitching) return ;
		
		Port port = Port.getCurrent();
		
		//切换地图加200ms延迟，并且改变用户状态设置为切换地图中
		port.getService(D.SERV_STAGE_DEFAULT).scheduleOnce(new ScheduleTask() {
			@Override
			public void execute() {
				//切换地图
				StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
				prx.switchToStage(humanObj, stageTargetId, posAppear);
				
				//设置玩家状态为正在切换地图中
				humanObj.isStageSwitching = true;
			}
		}, 200);
		
	}
	
	/**
	 * 退出副本 活动 等地图到进入前的主地图
	 * @param humanObj
	 * @param params 可以不用填
	 */
	public void quitToCommon(final HumanObject humanObj, final Object...params) {
		//正在切地图状态中
		if(humanObj.isStageSwitching) return ;
		long id = humanObj.id;
		
		Param param = new Param(params);
		long delay = Utils.getParamValue(param, "delay", 200L);
		
		Port port = Port.getCurrent();
		
		//切换地图加200ms延迟，并且改变用户状态设置为切换地图中
		port.getService(D.SERV_STAGE_DEFAULT).scheduleOnce(new ScheduleTask() {
			@Override
			public void execute() {
				
				// 如果已经退出，就返回
				if(humanObj == null || humanObj.stageObj == null) {
					Log.stageCommon.info("Human already exit {}", id);
					return;
				}
				
				//切换地图
				StageGlobalServiceProxy prx = StageGlobalServiceProxy.newInstance();
				prx.quitToCommon(humanObj, humanObj.stageObj.conf.sn, params);
				
				//设置玩家状态为正在切换地图中
				humanObj.isStageSwitching = true;
			}
		}, delay);
	}
	
	/**
	 * 生成地图点配置的地图单元
	 */
	public void createObjByConf(StageObject stageObj) {
		ConfMap confMap = stageObj.conf;
		ConfScene confScene = ConfScene.get(confMap.sceneID);
		
		//如果没有就返回啊~
		if(stageObj.monsterToBirth == null || stageObj.monsterToBirth.length == 0) return;
		
		// 挨个目标点种怪
		for (int sn : stageObj.monsterToBirth) {
			
			//获取怪物配置
			ConfSceneCharacter confSceneCharacter = ConfSceneCharacter.get(sn);
			ConfCharacterMonster confMonster = ConfCharacterMonster.get(confSceneCharacter.monsterSn);
			//生成怪物逻辑
			MonsterObject monster = MonsterManager.inst().create(stageObj, sn, confMonster.sn, true, 0, confMonster.level);
			
			//坐标
			Vector2D posBegin = new Vector2D(confSceneCharacter.position[0], confSceneCharacter.position[2]);
			//朝向
			Vector2D dirNow = new Vector2D(confSceneCharacter.rotation[0], confSceneCharacter.rotation[2]);
			
			//怪物属性
			monster.posBegin = posBegin;
			monster.dirNow = dirNow;
			monster.teamBundleID = confScene.sn;
			monster.startup();
		}
		
		//生成完就清空
		stageObj.monsterToBirth = null;
	}
	
	/**
	 * 生成地图点配置的地图单元
	 */
	public void createObjByConf(StageObject stageObj, List<UnitObject> objs) {
		//坐标
		Vector2D posBegin = new Vector2D(ConfGlobalUtils.getValue(ConfGlobalKey.镜像位置X坐标), ConfGlobalUtils.getValue(ConfGlobalKey.镜像位置Y坐标));
		//朝向
		Vector2D dirNow = new Vector2D(ConfGlobalUtils.getValue(ConfGlobalKey.镜像朝向X坐标), ConfGlobalUtils.getValue(ConfGlobalKey.镜像朝向Y坐标));
		
		for (UnitObject unitObj : objs) {
			unitObj.posBegin = posBegin;
			unitObj.dirNow = dirNow;
			unitObj.startup();
		}
	}
	
	/**
	 * 全角度碰撞 判断obj 在地图中可能会发生所有碰撞角度
	 * @param obj
	 * @param dir
	 * @param angleArr
	 * @return
	 */
	public boolean stageCollsionDetectAll(UnitObject obj, Vector2D dir, List<Double> angleArr) {
		boolean result = false;
		if(angleArr != null) {
			angleArr.clear();
		}
		for (UnitObject unitObj : obj.stageObj.getUnitObjs().values()) {
			if(!unitObj.isDie() && unitObj.isInWorld()) {
				if(unitObj.id == obj.id) {
					continue;
				}
				
				if(MathUtils.collisionDetectAll(obj.posNow, dir, unitObj.posNow, obj.confModel.collisionRadius + unitObj.confModel.collisionRadius, angleArr)) {
					result = true;
				}
			}
		}
		return result;
	}
	
	/**
	 * 全角度碰撞 判断obj 是否产生碰撞
	 * @param obj
	 * @param dir
	 * @param angleArr
	 * @return
	 */
	public boolean stageCollsionDetect(UnitObject obj, Vector2D dir) {
		for (UnitObject unitObj : obj.stageObj.getUnitObjs().values()) {
			if(!unitObj.isDie() && unitObj.isInWorld()) {
				if(unitObj.id == obj.id) {
					continue;
				}
				if(MathUtils.collisionDetect(obj.posNow, dir, unitObj.posNow, obj.confModel.collisionRadius + unitObj.confModel.collisionRadius, null)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 获得坐标的Z
	 * @param stageSn
	 * @param vec
	 * @return
	 */
	public static boolean getHeight(int stageSn, Vector3D vec) {
//		boolean result = PathFinding.posHeight(stageSn, vec);
		boolean result = HeightFinding.posHeight(stageSn, vec);
		return result;
	}
	/**
	 * 获得坐标的Z
	 * @param stageSn
	 * @param vec
	 * @return
	 */
	public static double getHeightY(int stageSn, Vector2D vec) {
//		Vector3D result = PathFinding.posHeight(stageSn, vec);
		Vector3D result = HeightFinding.posHeight(stageSn, vec);
		return result.y;
	}
	/**
	 * 获得坐标的Z
	 * @param stageSn
	 * @param vec
	 * @return
	 */
	public static Vector3D getHeight(int stageSn, Vector2D pos){
		Vector3D result = HeightFinding.posHeight(stageSn, pos);
		if(result.z == 0) {
			result = PathFinding.posHeight(stageSn, pos);
		}
		return result;
	}
	/**
	 * 如果下一个目标点不能直接到达，那么取起点和终点直线上从起点能到达的最远的点
	 * @param stageSn
	 * @param vec
	 * @return
	 */
	public static Vector2D getRaycastDis(int stageSn, Vector2D posBegin, Vector2D posEnd, int flag) { 
		Vector3D endReal = PathFinding.raycast(stageSn, getHeight(stageSn,posBegin), getHeight(stageSn,posEnd), flag);
		return endReal.toVector2D();
	}
	
	/**
	 * 获取场景中剩余的怪物数量
	 * @param stageObj
	 * @return
	 */
	public int getLastMonsterCount(StageObject stageObj){
		int count = 0;
		for (MonsterObject mo : stageObj.getMonsterObjs().values()) {
			if(mo.isDie()) continue;
			count ++;
		}
		return count;
	}
	
	
	public Vector2D getBirthPosFromMapSn(int mapSn) {
		//获得 mapConf
		ConfMap conf = ConfMap.get(mapSn);
		//获得 mapPos
		ConfScene confScene = ConfScene.get(conf.sceneID);
		Vector2D posAppear = new Vector2D(confScene.bornPoint[0], confScene.bornPoint[2]);
		return posAppear;
	}
	
	/**
	 * 初始化场景评分相关
	 * @param stageScore
	 */
	public List<StageStarVo> initStageScore(ConfStageScore stageScore){
		List<StageStarVo> list = new ArrayList<>();
		int len = stageScore.type.length;
		for(int i=0; i<len; i++){
			StageStarVo vo = new StageStarVo();
			vo.sn = stageScore.sn;
			vo.status = new int[3][];
			vo.type = stageScore.type;
			
			vo.targetSn = new String[3][];
			vo.targetSn[0] = stageScore.conditions0;
			vo.targetSn[1] = stageScore.conditions1;
			vo.targetSn[2] = stageScore.conditions2;
			
			vo.targetProgress = new int[3][];
			vo.targetProgress[0] = stageScore.conditionValue0;
			vo.targetProgress[1] = stageScore.conditionValue1;
			vo.targetProgress[2] = stageScore.conditionValue2;
			
			vo.nowProgress = new int[3][];
			list.add(vo);
		}
		return list;
	}
	
	
	/**
	 * 使用了某个技能
	 * @param param
	 */
	@Listener(EventKey.UNIT_DO_SKILL)
	public void _listener_UNIT_DO_SKILL(Param param) {
		UnitObject attacker = param.get("attacker");
		String skillSn = param.get("skillSn");
		if(attacker instanceof HumanObject){
			StageObject stageObj = attacker.stageObj;
			
			for(StageStarVo stageScore : stageObj.stageScoreList){
				int len = stageScore.type.length;
				for(int i=0; i<len; i++){
					if(stageScore.type[i] == StageScoreTypeKey.TYPE_不使用指定技能){
						stageScore.checkTarget(skillSn, i, true,true);
					}
				}
			}
		}
		
	}
	
	/**
	 * 战斗单元死亡
	 * @param param
	 */
	@Listener(EventKey.UNIT_BE_KILLED)
	public void _listener_UNIT_BE_KILLED(Param param) {
		UnitObject unitObjDead = param.get("dead");
		if(unitObjDead instanceof MonsterObject){//针对怪物
			StageObject stageObj = unitObjDead.stageObj;
			List<StageStarVo> stageScores = stageObj.stageScoreList;
			for(StageStarVo stageScore : stageScores){
				int len = stageScore.type.length;
				for(int i=0; i<len; i++){
					if(stageScore.type[i] == StageScoreTypeKey.TYPE_击杀指定怪物一定数量){
						stageScore.checkTarget(unitObjDead.sn, i, true,false);
						
					}else if(stageScore.type[i] == StageScoreTypeKey.TYPE_累计击杀一定数量怪物){
						stageScore.checkTarget(unitObjDead.sn, i, false,false);
						
					}
				}
			}
		}
	}
	
	/**
	 * 结束时检测的条件
	 */
	public void stageStarEndCheck(StageObject stageObj){
		for(StageStarVo stageStar : stageObj.stageScoreList){
			int len = stageStar.type.length;
			for(int i=0; i<len; i++){
				if(stageStar.type[i] == StageScoreTypeKey.TYPE_击杀所有怪物){
					//击杀所有怪物
					int count = getLastMonsterCount(stageObj);
					if(count ==0){
						stageStar.finishTarget(i);
					}
				}else if(stageStar.type[i] == StageScoreTypeKey.TYPE_指定伙伴上阵){
					for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
						for (UnitObject uo : humanObj.salvesAttingList) {
							stageStar.checkTarget(uo.sn, i, true,false);
						}
					}
				}else if(stageStar.type[i] == StageScoreTypeKey.TYPE_指定时间通关){
					long timeDiff = (stageObj.getTime() - stageObj.getCreateTime())/1000;
					stageStar.nowProgress[i][0] = (int)timeDiff;
					if(stageStar.nowProgress[i][0] <= stageStar.targetProgress[i][0]){
						stageStar.status[i][0] = StageScoreStatusKey.STATUS_FINISHED;
					}
					
				}else if(stageStar.type[i] == StageScoreTypeKey.TYPE_主角血量不低于){
					for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
						double percent = humanObj.getHuman().getHpCur() / humanObj.getHuman().getHpMax() * 100;
						if( percent >= stageStar.targetProgress[i][0]){
							stageStar.finishTarget(i);
						}
					}
				}else if(stageStar.type[i] == StageScoreTypeKey.TYPE_全员血量不低于){
					int currHp = 0;
					int maxHp = 0;
					for (HumanObject humanObj : stageObj.getHumanObjs().values()) {
						currHp += humanObj.getHuman().getHpCur();
						maxHp  += humanObj.getHuman().getHpMax();
						for(CharacterObject general : humanObj.salvesAttingList){
							currHp += general.dataPers.unit.getHpCur();
							maxHp += general.dataPers.unit.getHpMax();
						}
					}
					
					double percent =  currHp/ maxHp * 100;
					if( percent >= stageStar.targetProgress[i][0]){
						stageStar.finishTarget(i);
					}
				}else if(stageStar.type[i] == StageScoreTypeKey.TYPE_获得指定道具指定数量){
					for(ProduceVo product : stageObj.itemProduce){
						stageStar.checkTarget(String.valueOf(product.sn), i, product.num);
					}
				}
			}
			
		}
	}
	
	/**
	 * 计算副本星级
	 * @return
	 */
	public int caculateStageStar(StageObject stageObj){
		int score= 0;
		for(StageStarVo stageStar : stageObj.stageScoreList){
			int len = stageStar.status.length;
			for(int i=0; i<len; i++){
				
				boolean finished = true;
				int[] status = stageStar.status[i];
				for(int k=0; k<status.length ; k++){
					if(status[k] == StageScoreStatusKey.STATUS_INIT || status[k] == StageScoreStatusKey.STATUS_FAILED){
						finished = false;
						break;
					}
				}
				if(finished){
					score++;
				}
			}
		}
		return score;
	}
	
}
