package org.gof.demo.worldsrv.stage;

import org.gof.core.Chunk;
import org.gof.core.Port;
import org.gof.core.Service;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.ConnectionStatus;
import org.gof.core.support.Distr;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.competition.StageObjectCompetition;
import org.gof.demo.worldsrv.instance.StageObejctInstance;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.tower.StageObjectTower;

@DistrClass(
	importClass = {HumanObject.class}
)
public class StageService extends Service {
	
	
	public StageService(StagePort port) {
		super(port);
	}
	
	@Override
	public Object getId() {
		return D.SERV_STAGE_DEFAULT;
	}
	
	/**
	 * 创建普通地图
	 * @param stageSn
	 * @param lineNum
	 */
	@DistrMethod
	public void createStageCommon(int stageSn, int lineNum) {
		long stageID = Port.applyId();
		
		StageObject stage = new StageObject((StagePort)port, stageID, stageSn);
		stage.startup();
		stage.lineNum = lineNum;
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.id, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建副本地图
	 * @param stageSn
	 */
	@DistrMethod
	public void createStageInstance(int stageSn, int repSn) {
		long stageID = Port.applyId();
		
		StageObject stage = new StageObejctInstance((StagePort)port, stageID, stageSn, repSn);
		stage.startup();
		
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.id, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建爬塔地图
	 * @param stageSn
	 */
	@DistrMethod
	public void createStageTower(int stageSn, int towerLayer) {
		long stageID = Port.applyId();
		
		StageObject stage = new StageObjectTower((StagePort)port, stageID, stageSn, towerLayer);
		stage.startup();
		
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.id, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 创建竞技场地图
	 * @param stageSn
	 */
	@DistrMethod
	public void createStageCompetition(HumanObject humanObj,int stageSn, long defenderId) {
		long stageID = Port.applyId();
		
		StageObject stage = new StageObjectCompetition(humanObj,(StagePort)port, stageID, stageSn, defenderId);
		stage.startup();
		
		String portId = stage.getPort().getId();
		port.returns("stageId", stage.id, "nodeId", Distr.getNodeId(portId), "portId", portId);
	}
	
	/**
	 * 销毁地图
	 * @param gameId
	 */
	@DistrMethod
	public void destroy(long gameId) {
		StageObject stage = ((StagePort)port).getStageObject(gameId);
		if(stage != null) {
			stage.destory();
		}
	}

	
}
