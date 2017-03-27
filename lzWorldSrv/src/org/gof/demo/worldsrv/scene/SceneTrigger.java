package org.gof.demo.worldsrv.scene;

import org.gof.core.Port;
import org.gof.demo.worldsrv.config.ConfSceneTrigger;


/**
 * 场景数据结构：触发器
 * 
 * @author GaoZhangCheng
 */
public class SceneTrigger {
	//常量
	public static final int SCENE_TRIGGER_STATUS_NONE = 0;		//已触发
	public static final int SCENE_TRIGGER_STATUS_FINISH = 1;		//没触发
	
	public long id;						//唯一ID
	public String sn;					//配置SN
	public int status;					//状态
	public int type;						//事件类型
	public ConfSceneTrigger conf;	//配置
	
	public ScenePlot plot;			//场景剧情
	
	/**
	 * 构造方法
	 */
	public SceneTrigger(String sn, ConfSceneTrigger conf, ScenePlot plot) {
		this.id = Port.applyId();
		this.sn = sn;
		this.conf = conf;
		this.plot = plot;
		this.type = conf.triggerType;
	}
	
}
