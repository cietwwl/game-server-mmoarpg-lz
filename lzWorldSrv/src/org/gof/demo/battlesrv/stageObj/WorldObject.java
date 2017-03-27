package org.gof.demo.battlesrv.stageObj;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.msg.Msg.DStageObject;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectAppear;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectDisappear;
import org.gof.demo.worldsrv.stage.StageCell;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.support.Log;

/**
 * 地图单元基类
 */
public abstract class WorldObject implements ISerilizable {
	public StageObject stageObj;			//所属地图
	public StageCell stageCell;				//所属地图格
	
	public long id;									//ID
	public String name = "";					//name
	public String modelSn = "";							//模型Sn
	public String sn;	//配置表sn
	
	protected boolean inWorld = false;				//是否在地图上显示 本属性不应该被Distr同步
	
	public Vector2D posNow = new Vector2D();	//坐标
	public Vector2D dirNow = new Vector2D();	//方向
	
	public abstract DStageObject.Builder createMsg();
	
	public WorldObject fireObj; //制造这个对象的源头
	private long timeCreate; //产生时候的时间
	protected long timeCurr; //当前的时候  使用timeCrete +  pulse 的时间
	protected int deltaTime;
	
	public WorldObject(StageObject stageObj) {
		this.stageObj = stageObj;
	}
	
	public WorldObject() {
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(name);
		out.write(modelSn);
		out.write(sn);
		out.write(posNow);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		name = in.read();
		modelSn = in.read();
		sn = in.read();
		posNow = in.read();
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("name", name).toString();
	}
	
	public boolean isInWorld() {
		return inWorld;
	}
	
	public void pulse(int deltaTime) {
		timeCurr += deltaTime;
		this.deltaTime = deltaTime;
	}
	
	public long getTime() {
		return timeCurr;
	}
	
	public long geDeltaTime() {
		return deltaTime;
	}
	
	public void startup() { }

	public void stageRegister(StageObject stageObj) {
		
		//在地图中添加活动单元
		stageObj._addWorldObj(this);
		
		this.timeCreate = stageObj.getTime();
		this.timeCurr = this.timeCreate;
	}
	
	/**
	 * 在地图显示
	 */
	public void stageShow() {
		//已在地图中的 忽略
		if(inWorld) {
			Log.stageCommon.warn("使活动单元进入地图时发现inWorld状态为true：data={}", this);
			return;
		}
		
		//设置状态为在地图中
		inWorld = true;

		//日志
		if(Log.stageCommon.isInfoEnabled()) {
			Log.stageCommon.info("地图单位进入地图: stageId={}, objId={}, objName={}", stageObj.id, id, name);
		}
		
		//通知其他玩家 有地图单元进入视野
		StageManager.inst().sendMsgToArea(createMsgAppear(1), stageObj, posNow);
	}
	
	/**
	 * 在地图显示： 复活
	 */
	public void stageShowRevive() {
		//已在地图中的 忽略
		if(inWorld) {
			Log.stageCommon.warn("使活动单元进入地图时发现inWorld状态为true：data={}", this);
			return;
		}
		
		//设置状态为在地图中
		inWorld = true;

		//日志
		if(Log.stageCommon.isInfoEnabled()) {
			Log.stageCommon.info("地图单位进入地图: stageId={}, objId={}, objName={}", stageObj.id, id, name);
		}
		
		//通知其他玩家 有地图单元进入视野
		StageManager.inst().sendMsgToArea(createMsgAppear(2), stageObj, posNow);
	}
	
	/**
	 * 进入地图
	 * @param stageObj
	 */
	public void stageEnter(StageObject stageObj) {
		stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "worldObj", this) {
			@Override
			public void execute(Port port) {
				WorldObject worldObj = param.get("worldObj");
				StageObject stageObj = param.get("stageObj");
				
				//加入地图并显示
				worldObj.stageRegister(stageObj);
				worldObj.stageShow();
			}
		});
	}
	
	public final void stageLeave() {
		//设置状态
		inWorld = false;
		
		//将具体删除操作排入队列 在心跳的最后在进行删除
		//因为本心跳中可能还有后续操作需要本对象的实例
		if(stageObj == null) {
			return;
		}
		stageObj.getPort().addQueue(new PortPulseQueue("stageObj", stageObj, "worldObj", this) {
			public void execute(Port port) {
				StageObject stageObj = param.get("stageObj");
				WorldObject worldObj = param.get("worldObj");
				
				stageObj._delWorldObj(worldObj);
				
				//发送消息 通知客户端
				//通知其他玩家 有地图单元离开视野
				StageManager.inst().sendMsgToArea(worldObj.createMsgDisappear(), stageObj, worldObj.posNow);
			}
		});
	}
	
	/**
	 * 从地图隐藏
	 */
	public void stageHide() {
		//设置状态
		inWorld = false;
		
		//通知其他玩家 有地图单元离开视野
		StageManager.inst().sendMsgToArea(createMsgDisappear(), stageObj, posNow);
	}
	
	/**
	 * 创建地图单元进入视野消息，表示自己出现在了别人视野
	 * 1 是正常出现 2是复活
	 */
	public SCStageObjectAppear.Builder createMsgAppear(int type) {
		SCStageObjectAppear.Builder msgAppear = SCStageObjectAppear.newBuilder();
		msgAppear.setObjAppear(createMsg());
		msgAppear.setType(type);
		
		return msgAppear;
	}
	
	/**
	 * 创建地图单元离开视野消息
	 * @return
	 */
	public SCStageObjectDisappear.Builder createMsgDisappear() {
		SCStageObjectDisappear.Builder msgDisappear = SCStageObjectDisappear.newBuilder();
		msgDisappear.setObjId(id);
		msgDisappear.setType(1);
		
		return msgDisappear;
	}
	
}
