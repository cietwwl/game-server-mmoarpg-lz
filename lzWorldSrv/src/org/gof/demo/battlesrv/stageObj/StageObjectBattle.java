package org.gof.demo.battlesrv.stageObj;

import java.util.HashMap;
import java.util.Map;

import org.gof.core.Port;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.pathFinding.PathFindingFlagKey;

public class StageObjectBattle {
	public double width;					//地图的宽	
	public double height;				//地图的高
	
	public int sn;				//地图配置SN
	public long id;				//地图实际ID
	public ConfMap conf;					//所属配置
	
	private long createTime;//副本创建时间
	private long currTime; //这个场景当前的时间， 当进行瞬间战斗计算的时候 这个时间会很神奇
	private long currLastTime; //这个场景当前的时间， 当进行瞬间战斗计算的时候 这个时间会很神奇
	private int deltaTime; //每帧时间差

	private long stopTimeStart = -1; //场景暂停的时间
	private long stopTimeEnd = -1; //场景暂停结束的时间
	public long stopTotalTime = 0; //场景暂停的总时间
	private Map<Long, UnitObject> targetSpecialObjs = new HashMap<Long, UnitObject>();
	public boolean stopAll = false;
	
	private Map<Long, WorldObject> worldObjs = new HashMap<Long, WorldObject>();//该地图内所有单位
	private Map<Long, UnitObject> unitObjs = new HashMap<Long, UnitObject>();//该地图内所有攻击单位
	
	//本地图允许的寻路掩码
	public int pathFindingFlag = PathFindingFlagKey.init;
	public StageRandomUtils randUtils = null;  //固定随机数系统
	
    public StageObjectBattle(long stageId, int stageSn)
    {
    	this.conf = ConfMap.get(stageSn);

        this.sn = stageSn;
        this.id = stageId;
        this.width = conf.width;
        this.height = conf.height;
        
        createTime = Port.getTime();
		currTime = Port.getTime();
		currLastTime = currTime;
		
		this.randUtils = new StageRandomUtils(100);
    }
    
	public void stopStageObjTime(UnitObject unitObj, long time) {
		//如果以前已经暂停了 那么现在当前时间等于暂停时间
		if(this.stopTimeEnd > 0) {
			time += this.currTime - this.stopTimeStart;
		} else {
			if(targetSpecialObjs.size() > 0)
				targetSpecialObjs.clear();
		}
		if(unitObj != null) {
			targetSpecialObjs.put(unitObj.id, unitObj);
		}

		this.stopTimeStart = this.currTime;
		this.stopTimeEnd = this.currTime + time;
	}
	public void recoverStageObjTime() {
		targetSpecialObjs.clear();
		this.stopTimeStart = -1;
		this.stopTimeEnd = -1;
	}

	private void pulseStop(long curr) {
		if(isStopping()) {
			stopTotalTime += this.deltaTime;
			if(this.stopTimeEnd <= curr) {
				recoverStageObjTime();
			}
		}
	}

	public boolean isStopping() {
		return (this.stopTimeEnd >= 0);
	}
	
	public void pulse() {
		//执行各单位本次心跳中的事务
		long curr = Port.getTime();
		this.currTime = curr;
		this.deltaTime += (int)(currTime - currLastTime);
		this.currLastTime = this.currTime;
		if(this.deltaTime >= 33) {
			pulseStop(this.currTime);

			pulseWorldObjs(this.deltaTime);	
//			Log.stageCommon.info("stageobj pulse delta {}", this.deltaTime);
			this.deltaTime = 0;
		}
	}
	
	public void pulse(int deltaTime) {
		//执行各单位本次心跳中的事务
        this.currLastTime = this.currTime;
        long curr = Port.getTime();
        if (deltaTime > 0)
        {
            curr = currLastTime + deltaTime;
        }
        this.currTime = curr;
        this.deltaTime = (int)(currTime - currLastTime);
        //D.log("stageObj pulse {0}", this.deltaTime);
        pulseStop(this.currTime);

        pulseWorldObjs(this.deltaTime);
	}
	
	
	private void pulseWorldObjs(int deltaTime) {

		if(stopAll) {
			return;
		}
		for(WorldObject wo : worldObjs.values()) {
			try {
				if(wo == null) {
					if(Log.stageCommon.isInfoEnabled()) {
						Log.stageCommon.info("执行地图心跳操作时发现下属WorldObj为空 worldObjs={}", worldObjs);
					}

					continue;
				}
				if(targetSpecialObjs.containsKey(wo.id) || (wo.fireObj != null && targetSpecialObjs.containsKey(wo.fireObj.id))) {
					//如果正在放大招。 并且是在队列中的那么播放
					if(SkillManager.inst().isChargeCasting(wo)) {
						wo.pulse(0);
					} else {
						wo.pulse(deltaTime);
					}
				} else if(isStopping()) {
					wo.pulse(0);
				} else {
					wo.pulse(deltaTime);
				}
			} catch (Exception e) {
				//特意吞掉异常 避免由于某个对象出错 造成全部操作都无法执行的问题
				//				Log.stageCommon.error(ExceptionUtils.getStackTrace(e));
				e.printStackTrace();
			}
		}

	}
	public boolean worldObjInSpecial(WorldObject wo) {
		if(SkillManager.inst().isSpeciaCasting(wo)) {
			return true;
		}
		if(wo.fireObj != null && SkillManager.inst().isSpeciaCasting(wo)) {
			return true;
		}
		return false;
	}

	public UnitObject getUnitObj(long unitId) {
		return unitObjs.get(unitId);
	}


	public Map<Long, UnitObject> getUnitObjs() {
		return unitObjs;
	}

	public WorldObject getWorldObj(long objId) {
		return worldObjs.get(objId);
	}
	
	public Map<Long, WorldObject> getWorldObjs() {
		return worldObjs;
	}
	
	
	public void _addWorldObj(WorldObject obj) {
		worldObjs.put(obj.id, obj);
		if(obj instanceof UnitObject) {
			unitObjs.put(obj.id, (UnitObject)obj);
		}
	}

	public void _delWorldObj(WorldObject obj) {
		worldObjs.remove(obj.id);
		if(obj instanceof UnitObject) {
			unitObjs.remove(obj.id);
		}
	}
	/**
	 * 返回当前时间
	 * @return
	 */
	public long getTime() {
		return currTime;
	}
	
	/**
	 * 返回副本创建时间
	 * @return
	 */
	public long getCreateTime() {
		return createTime;
	}

}
