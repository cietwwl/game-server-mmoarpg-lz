package org.gof.demo.worldsrv.scene;

import org.gof.core.Port;
import org.gof.demo.worldsrv.config.ConfSceneEvent;


/**
 * 场景数据结构：事件
 * 
 * @author GaoZhangCheng
 */
public class SceneEvent {
	//常量
	public static final int SCENE_EVENT_STATUS_NONE = 0;		//没完成
	public static final int SCENE_EVENT_STATUS_FINISH = 1;		//完成
		
	public long id;					//唯一ID
	public String sn;				//配置SN
	public int type;					//事件类型
	public int status;				//状态
	public ConfSceneEvent conf;	//配置
	
	public ScenePlot plot;		//场景剧情
	
	/**
	 * 构造方法
	 */
	public SceneEvent(String sn, ConfSceneEvent conf, ScenePlot plot) {
		this.id = Port.applyId();
		this.sn = sn;
		this.conf = conf;
		this.plot = plot;
		this.type = conf.eventType;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
}
