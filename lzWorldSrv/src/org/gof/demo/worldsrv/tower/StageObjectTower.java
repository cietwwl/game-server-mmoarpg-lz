package org.gof.demo.worldsrv.tower;

import org.gof.core.support.Time;
import org.gof.demo.battlesrv.stageObj.StageRandomUtils;
import org.gof.demo.worldsrv.config.ConfTower;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.stage.StagePort;

public class StageObjectTower extends StageObject{
	public boolean start = false;
	public long createdAt;				//创建
	public boolean destroy;			//是否正在摧毁
	public boolean isPass;			//是否已经通过
	public ConfTower confTower = null;
	public int layer; //实际的层 也是 confTower 的sn
	
	public static long DESTROY_TIME = 10 * Time.SEC;
	
	public StageObjectTower(StagePort port, long stageId, int stageSn, int layer) {
		super(port, stageId, stageSn);
		
		this.layer = layer;
		confTower = ConfTower.get(layer);
		createdAt = this.getTime();
		
		//副本就一个大格子
		this.cellWidth = this.width;
		this.cellHeight = this.height;
		
		this.randUtils = new StageRandomUtils(100);
	}
	
//	public void refreshLayer(int layer) {
//		ConfTower confTower = ConfTower.get(layer);
//		this.confTower = confTower;
//		this.layer = layer;
//	}
	@Override
	public void pulse() {
		if(!start) {
//			return;
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
		if(confTower == null) {
			return;
		}
		//生成怪物
		StageManager.inst().createObjByConf(this);
	}
	
	@Override
	public int getRepSn() {
		return layer;
	}
	
	@Override
	public void destory() {
		if(this.destroy) return;
		//删除副本地图
		super.destory();
		this.destroy = true;
	}
}
