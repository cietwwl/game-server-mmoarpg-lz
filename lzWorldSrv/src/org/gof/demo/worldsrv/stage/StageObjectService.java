package org.gof.demo.worldsrv.stage;

import java.util.Map;

import org.gof.core.CallPoint;
import org.gof.core.Port;
import org.gof.core.Service;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.HumanObject;

import com.google.protobuf.Message;

@DistrClass(
	importClass = {HumanObject.class, Vector2D.class, Map.class, Message.class}
)
public class StageObjectService extends Service {
	protected final StageObject stageObj;
	
	/**
	 * 初始化数据
	 * @return
	 */
	protected void init() {
		
	}
	
	/**
	 * 启动服务
	 */
	public void startupLocal() {
		this.startup();
		init();
	}
	
	/**
	 * 构造函数
	 * @param stageObj
	 */
	public StageObjectService(StageObject stageObj, Port port) {
		super(port);
		this.stageObj = stageObj;
	}
	

	@Override
	public Object getId() {
		return stageObj.id;
	}

	@Override
	public void pulseOverride() {
		stageObj.pulse();
	}

	public StageObject getStageObj() {
		return stageObj;
	}
	
	@DistrMethod
	public void login(long humanId, CallPoint connPoint, long stageId, Vector2D stagePos) {
		stageObj.login(humanId, connPoint, stageId, stagePos);
	}
	
	@DistrMethod
	public void register(HumanObject humanObj) {
		//将玩家注入进地图
		humanObj.stageRegister(stageObj);
		int repSn = stageObj.getRepSn(); 
//		Vector2D vec = new Vector2D(stageObj.conf.birth[0], stageObj.conf.birth[2]);
		Port.getCurrent().returns("posNow", humanObj.posNow, "repSn", repSn);
	}
	
}
