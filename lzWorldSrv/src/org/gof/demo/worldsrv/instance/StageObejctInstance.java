package org.gof.demo.worldsrv.instance;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.support.Time;
import org.gof.demo.battlesrv.stageObj.StageRandomUtils;
import org.gof.demo.worldsrv.config.ConfRep;
import org.gof.demo.worldsrv.config.ConfRepQuest;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.quest.QuestStatusKey;
import org.gof.demo.worldsrv.quest.QuestVO;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.stage.StagePort;

public class StageObejctInstance extends StageObject{
	
	
	public boolean start = false;
	public long createdAt;				//创建
	public boolean destroy;			//是否正在摧毁
	public boolean isPass;			//是否已经通过
	public ConfRep repCfg = null;
	public int repSn;
	public QuestVO[] vo = new QuestVO[3];
	//抽奖奖励缓存
	public List<ProduceVo> lotteryProduce = new ArrayList<ProduceVo>();
	public int lotteryTimes = 0;
	
	public static long DESTROY_TIME = 10 * Time.SEC;
	
	public StageObejctInstance(StagePort port, long stageId, int stageSn, int repSn) {
		super(port, stageId, stageSn);
		
		this.repSn = repSn;
		repCfg = ConfRep.get(repSn);
		createdAt = this.getTime();
		// 如果有任务， 就初始化副本中的任务
		if (repCfg.repTask.length == 3) {
			for (int i = 0; i < 3; i++) {
				ConfRepQuest confRepQuest = ConfRepQuest.get(repCfg.repTask[i]);
				vo[i] = new QuestVO();
				vo[i].sn = confRepQuest.sn;
				vo[i].type = confRepQuest.type;
				vo[i].nowProgress = 0;
				vo[i].targetProgress = confRepQuest.target[0];
				vo[i].status = QuestStatusKey.QUEST_STATUS_进行中;
			}
		}
		//副本就一个大格子
		this.cellWidth = this.width;
		this.cellHeight = this.height;
		
		this.randUtils = new StageRandomUtils(100);
	}
	
	@Override
	public void pulse() {
		if(!start) {
			return;
		}
		
		super.pulse();
		
		long curr = this.getTime();
		
		//如果一定时间内副本没人 删除
		if(curr - this.createdAt > DESTROY_TIME) {
			//没人了直接删除地图
			if(this.getHumanObjs().isEmpty()) {
				this.destory();
			} 
		}
	}
	
	@Override
	public void createMosnter() {
		if(repCfg == null) {
			return;
		}
		//生成怪物
		StageManager.inst().createObjByConf(this);
	}
	
	@Override
	public int getRepSn() {
		return repSn;
	}
	
	@Override
	public void destory() {
		if(this.destroy) return;
		//删除副本地图
		super.destory();
		this.destroy = true;
	}
}
