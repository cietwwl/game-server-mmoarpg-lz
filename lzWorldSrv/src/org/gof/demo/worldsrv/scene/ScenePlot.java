package org.gof.demo.worldsrv.scene;

import java.util.HashMap;
import java.util.Map;

import org.gof.core.Port;
import org.gof.demo.worldsrv.config.ConfScenePlot;
import org.gof.demo.worldsrv.stage.StageObject;

/**
 * 场景数据结构：剧情
 * 
 * @author GaoZhangCheng
 */
public class ScenePlot {
	//常量
	public static final int  SCENT_PLOT_STATUS_NONE= 0;			//未开始
	public static final int  SCENT_PLOT_STATUS_DOING= 1;		//进行中
	public static final int  SCENT_PLOT_STATUS_FINISH= 2;		//完成
	
	public long id;									//唯一ID
	public String sn;									//配置SN
	public int status;									//状态
	public ConfScenePlot conf;					//配置
	public StageObject stageObject;			//所属的StageObject
	
	public Map<Long, ScenePlot> prePlots = new HashMap<>();		//前置剧情
	public Map<Long, SceneTrigger> triggers = new HashMap<>();		//触发器列表
	public Map<Long, SceneEvent> events = new HashMap<>();			//事件列表
	
	/**
	 * 构造方法
	 */
	public ScenePlot(String sn, ConfScenePlot conf, StageObject stageObject) {
		this.id = Port.applyId();
		this.sn = sn;
		this.conf = conf;
		this.status = SCENT_PLOT_STATUS_NONE;
		this.stageObject = stageObject;
	}
	
	/**
	 * 判断当前剧情是否完结
	 * @return
	 */
	public boolean isFinish() {
		
		//如果已经完成，直接返回
		if(this.status == ScenePlot.SCENT_PLOT_STATUS_FINISH){
			return true;
		}
		
		//如果未开始，返回
		if(this.status == ScenePlot.SCENT_PLOT_STATUS_NONE){
			return false;
		}
		
		//挨个判断条件
		boolean finish = true;
		for (SceneEvent event : events.values()) {
			if(event.status == SceneEvent.SCENE_EVENT_STATUS_NONE)
				finish = false;
		}
		
		//已经完成
		if(finish){
			status = ScenePlot.SCENT_PLOT_STATUS_FINISH;
		}
		
		return finish;
	}
	
	/**
	 * 判断当前剧情是否可以开始
	 * @return
	 */
	public boolean canStart() {
		//如果当前状态不是未开始，直接返回true
		if(status != ScenePlot.SCENT_PLOT_STATUS_NONE) 
			return true;
		
		//挨个判断前置条件是否都已经完成
		boolean canStart = true;
		for (ScenePlot p : prePlots.values()) {
			if(!p.isFinish()){
				canStart = false;
			}
		}
		
		//如果可以开始，设置状态
		if(canStart){
			status = ScenePlot.SCENT_PLOT_STATUS_DOING;
		}
		
		return canStart;
	}
	
	/**
	 * 判断当前剧情是否触发器都已经完成，如果没有或者完成，就可以开始事件
	 * 
	 * @return
	 */
	public boolean canDoEvent() {
		//如果当前状态不是进行中，直接返回true
		if(status != ScenePlot.SCENT_PLOT_STATUS_DOING) 
			return true;
		
		//挨个判断触发器状态
		boolean canDoEvent = true;
		for (SceneTrigger st : triggers.values()) {
			if(st.status == SceneTrigger.SCENE_TRIGGER_STATUS_NONE){
				canDoEvent = false;
				break;
			}
		}
		
		return canDoEvent;
	}
	
}
