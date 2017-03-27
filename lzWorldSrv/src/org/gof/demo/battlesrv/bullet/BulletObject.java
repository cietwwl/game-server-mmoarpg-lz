package org.gof.demo.battlesrv.bullet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gof.core.support.Param;
import org.gof.core.support.TickTimer;
import org.gof.core.support.Time;
import org.gof.demo.battlesrv.manager.StageBattleManager;
import org.gof.demo.battlesrv.skill.SkillEffectVO;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.TemporaryObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfBullet;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.DHpChgOnce;
import org.gof.demo.worldsrv.msg.Msg.DHpChgTar;
import org.gof.demo.worldsrv.msg.Msg.DStageBullet;
import org.gof.demo.worldsrv.msg.Msg.DStageObject;
import org.gof.demo.worldsrv.msg.Msg.EWorldObjectType;
import org.gof.demo.worldsrv.msg.Msg.SCFightBulletHpChg;
import org.gof.demo.worldsrv.msg.Msg.SCFightBulletMove;
import org.gof.demo.worldsrv.msg.Msg.SCFightHpChg;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;

/**
 * BulletObject 对象处理逻辑
 * 
 * @author GaoZhangCheng
 */
public class BulletObject extends TemporaryObject {
	
	private UnitObject unitObjFire;		//释放bullet的unitObject 
	private WorldObject worldObjectCreate;	//生成bullet的对象， 可能是玩家 可能是武将 可能是 dot 可能是bullet
	private UnitObject unitObjTar;		//初始作用目标
	private ConfBullet confBullet;
	public int skillSn;				//bullet 所属技能ID
	private Vector2D posTar;			//目标位置
	private long timeEnd;				//结束时间
	private long delayFly;				//延迟飞行
	private TickTimer updateTimer = new TickTimer();
	private int trrigerCount = 0;
	//当type==2的时候 反弹触发的人
	private Map<UnitObject, Integer> trrigerUnit = new HashMap<UnitObject, Integer>();
	private Map<Long, UnitObject> doBulletMap = new HashMap<Long, UnitObject>();//子弹实际影响过的人
	public SCFightHpChg.Builder hpChangeMsg = null;				//技能伤害血量包
	private Param param;
	private long runTimeBegin;									//移动开始时间
	private final Vector2D runPosBegin = new Vector2D();			//移动开始坐标
	private final Vector2D runPosEnd = new Vector2D();				//移动结束坐标
	private double speed;				//移动速度
	//为了便于运算的暂存值
	public double runTempSin;			//起始至目标的Sin值
	public double runTempCos;			//起始至目标的Cos值
	
	public BulletObject(StageObject stage, int skillSn, ConfBullet confBullet, UnitObject unitObjFire, WorldObject worldObjectCreate, UnitObject unitObjTar, Vector2D vec) {
		super(stage);
		long curr = stage.getTime();
		this.confBullet = confBullet;
		this.skillSn = skillSn;
		this.unitObjFire = unitObjFire;
		this.fireObj = unitObjFire;
		this.worldObjectCreate = worldObjectCreate;
		this.unitObjTar = unitObjTar;
		this.confBullet = confBullet;
		this.timeEnd = curr + confBullet.totalTime + confBullet.delayFly;
		this.delayFly = curr + confBullet.delayFly;
		this.posNow.set(worldObjectCreate.posNow);   //子弹是从创建者开始的
		this.posTar = vec;
		this.speed = confBullet.speed / 100;
		
	}
	
	/**
	 * 初始化方法
	 * @param stage
	 */
	public void init(StageObject stage) {
		runTimeBegin = stage.getTime() + confBullet.delayFly;
		runPosBegin.set(posNow);
		if(confBullet.trrigerType == 1 || confBullet.trrigerType == 4) {
			//位置直接触发
			runPosEnd.set(posTar);
		} else if(confBullet.trrigerType == 2) {
			//目标触发
			runPosEnd.set(unitObjTar.posNow);
			addTrrigerUnit(unitObjTar);
		} else if(confBullet.trrigerType == 3) {
			//一直飞下去
			runPosEnd.set(posTar);
		}
		
		//为了便于运算的暂存值
		initTempValue();
		
		//计算3type 的实际飞行路径
		if(confBullet.trrigerType == 3) {
			//一直飞下去
			//移动距离
			double posMoveMax = (confBullet.totalTime * speed) / Time.SEC;
			//移动距离对应的横纵偏移量
			double diffX = runTempCos * posMoveMax;
			double diffY = runTempSin * posMoveMax;
			
			//实际移动距离		
			double trueX = runPosBegin.x + diffX;
			double trueY = runPosBegin.y + diffY;
			runPosEnd.x = trueX;
			runPosEnd.y = trueY;
		}
		
		//重置移动的timer
		updateTimer.start(stage.getTime(), 100);
		
		param = new Param("bullet", this, "hpLostKey", HpLostKey.BULLET);
	}
	
	@Override
	public void pulse(int deltaTime) {
		super.pulse(deltaTime);
		if(timeCurr > timeEnd) {
			//去除清理功能
			leave();
			return ;
		}
		
		if(timeCurr < delayFly) {
			return;
		}
		
		
		if(!updateTimer.isPeriod(timeCurr)) {
			return ;
		} 
		
		if(confBullet.trrigerType == 2) {
			//弹弹球状态 跟着人跑 指向目标
			runPosEnd.set(unitObjTar.posNow);
			initTempValue();
		}
		
		//经过时间
		long timeDiff = timeCurr - runTimeBegin;
		//移动距离
		double posMoveMax = (timeDiff * speed) / Time.SEC;
		//移动距离对应的横纵偏移量
		double diffX = runTempCos * posMoveMax;
		double diffY = runTempSin * posMoveMax;
		
		//实际移动距离		
		double trueX = runPosBegin.x + diffX;
		double trueY = runPosBegin.y + diffY;
		
		//验证一下 不能超过目标点
		//理论上不会出现这种情况 检查一下放心
		if(runPosEnd.x >= runPosBegin.x && runPosEnd.x < trueX) trueX = runPosEnd.x;
		else if(runPosEnd.x < runPosBegin.x && runPosEnd.x > trueX) trueX = runPosEnd.x;
		if(runPosEnd.y >= runPosBegin.y && runPosEnd.y < trueY) trueY = runPosEnd.y;
		else if(runPosEnd.y < runPosBegin.y && runPosEnd.y > trueY) trueY = runPosEnd.y;
		
		this.posNow.x = trueX;
		this.posNow.y = trueY;
		
//		Log.fight.info("bullet pursue:{} {} {} {} to {} unit {} {}", confBullet.trrigerType, posMoveMax,this.posNow, runPosBegin, runPosEnd, unitObjFire.id, unitObjTar.id);
		Vector2D trueVector = new Vector2D(trueX, trueY);
		boolean isReach = runPosEnd.equals(trueVector);
		//只有触发类型不是1 2是一直飞下去的并且持续触发
		if(confBullet.trrigerType == 1 || confBullet.trrigerType == 2) {
			if(!isReach) return;
		} 
		
//		Log.fight.info("doBulletdoBullet");
		doBullet();
		
		//发送伤害消息
		sendBulletHpMsg();
		
		searchNext(isReach, timeCurr);
	}

	/**
	 * 发送伤害消息
	 */
	public void sendBulletHpMsg() {
		//循环本地图玩家,广播血包
		if(hpChangeMsg != null) {
			SCFightBulletHpChg.Builder msg = SCFightBulletHpChg.newBuilder();
			msg.setBulletID(id);
			msg.setHpChg(hpChangeMsg);
			msg.setCastId(unitObjFire.id);
			StageManager.inst().sendMsgToArea(msg, stageObj, posNow);
			hpChangeMsg = null;
		}
	}
	
	/**
	 * 如果返回的 tars.size > 0 那么就返回true 表示激活了
	 * @return
	 */
	public boolean doBullet() {
		boolean result = false;
		List<UnitObject> tarsList = new ArrayList<UnitObject>();
		Set<UnitObject> tarsSet = new HashSet<>();
		
		//获得对应的 技能逻辑
		if(confBullet.skillLogic != null && confBullet.skillLogic.length > 0 ) {
			for (int i : confBullet.skillLogic) {
				//如果1 2 是到达触发
				if(confBullet.trrigerType == 1 || confBullet.trrigerType == 2) {
					tarsList = SkillManager.inst().executeSkillLogic(i, unitObjFire, this, unitObjTar, posNow, param);
				} else {
					//3 4 是一直在触发并且只对唯一的人触发一次
					ConfSkillEffect conf = ConfSkillEffect.getBy("level", 1, "effectSn", i);
					if(conf == null) {
						return false;
					}
//					Log.fight.info("doBulletdoBullet {} {}", confBullet.sn, conf.effectSn);
					
					//获得可以影响的
					tarsList = SkillManager.inst().getTars(new SkillEffectVO(conf), unitObjFire, unitObjTar, posNow);
					for (UnitObject unitDef : tarsList) {
						if(!doBulletMap.containsKey(unitDef.id)){
							SkillManager.inst().executeSkillLogic(conf, unitObjFire, worldObjectCreate, unitDef, param);
						}
					}
				}
				
				for (UnitObject unit : tarsList) {
					tarsSet.add(unit);
				}
			}
		}
		if(tarsSet.size() > 0) {
			result = true;
		}
		for (UnitObject tar : tarsSet) {
			doBulletMap.put(tar.id, tar);
			
		}
		
			
		
		return result;
	}
	
	/**
	 * 寻找下一个目标点
	 */
	public void searchNext(boolean isReach, long curr) {
		UnitObject originalUnitObj = null;
		if(confBullet.trrigerType == 1) {
			//位置到了直接离开
			leave();
			return;
		} else if(confBullet.trrigerType == 2) {
			//目标到了再找找还有没有
			trrigerCount++;
			if(trrigerCount >= confBullet.trrigerNum) {
				leave();
				return;
			}
			runPosBegin.set(runPosEnd);
			originalUnitObj = unitObjTar;
			unitObjTar = searchNextUnit();
			if(unitObjTar == null) {
				leave();
				return;
			}
			
			runTimeBegin = curr;
			runPosBegin.set(posNow);
			runPosEnd.set(unitObjTar.posNow);
			addTrrigerUnit(unitObjTar);
			
			//为了便于运算的暂存值
			initTempValue();
			
			if(!originalUnitObj.equals(unitObjTar)) {
				//发送变线消息到前端
				SCFightBulletMove.Builder msg = SCFightBulletMove.newBuilder();
				msg.setBulletID(id);
				msg.setBulletSN(confBullet.sn);
				msg.setUnitOriginId(originalUnitObj == null ? 0 : originalUnitObj.id);
				msg.setUnitTarId(unitObjTar == null ? 0 : unitObjTar.id);
				StageManager.inst().sendMsgToArea(msg, stageObj, posNow);
			}
		} else if(confBullet.trrigerType == 3) {
			//如果触发数量满足就退出
			if(confBullet.trrigerNum > 0 && doBulletMap.size() >= confBullet.trrigerNum) {
//				Log.fight.info("trrigerType {} {}", confBullet.trrigerNum, doBulletMap.size());
				leave();
				return;
			}
		}  else if(confBullet.trrigerType == 4 && isReach) {
			//位置到了直接离开
			leave();
			return;
		} 
		
	}
	
	private void addTrrigerUnit(UnitObject unitObj) {
		Integer count = trrigerUnit.get(unitObj);
		if(count == null) {
			trrigerUnit.put(unitObj, 1);
		} else {
			trrigerUnit.put(unitObj, count + 1);
		}
	}
	
	private UnitObject searchNextUnit() {
		UnitObject unitObj = null;
		if(confBullet.trrigerType == 2) {
			List<UnitObject> unitAll = StageBattleManager.inst().getUnitObjsInCircle(stageObj, posNow, confBullet.trrigerParam1);
			//找一个在list 中离posNow 最近的 。并且不在trrigerUnit中
			double dis = 9999;
			double tempDis;
			for (UnitObject unitObject : unitAll) {
				if(unitObject.isDie()) {
					continue;
				}
				if(unitObject == unitObjTar) {
					continue;
				}
				tempDis = posNow.distance(unitObject.posNow);
				if(tempDis < dis/* && trrigerUnit.get(unitObject) == null*/) {
					 //删除 不包含自己的时候的自己 不包含敌人时候的敌人
					if((confBullet.excludeTeamBundle && unitObject.teamBundleID == unitObjFire.teamBundleID)
					|| (!confBullet.excludeTeamBundle && unitObject.teamBundleID != unitObjFire.teamBundleID)) {
						continue;
					}
					dis = tempDis;
					unitObj = unitObject;
				}
			}
		}
		
		return unitObj;
	}
	public void addSkillHpChg(long id, DHpChgOnce.Builder dhpChgOnce) {
		if(hpChangeMsg == null) {
			hpChangeMsg = SCFightHpChg.newBuilder();
			hpChangeMsg.setSkillSn(skillSn);
		}
		
		boolean flag = false;
		DHpChgTar.Builder dhpTar = DHpChgTar.newBuilder(); 
		dhpTar.setId(id);
		for(DHpChgTar.Builder tmp : hpChangeMsg.getDhpChgTarBuilderList()) {
			if(tmp.getId() != id) continue;
			dhpTar = tmp;
			flag = true;
		}
		
		dhpTar.addDhpChgOnce(dhpChgOnce);
		
		if(!flag) {
			hpChangeMsg.addDhpChgTar(dhpTar);
		}
		
		
	}
	@Override
	public Msg.DStageObject.Builder createMsg() {
		DStageObject.Builder objInfo = DStageObject.newBuilder();
		
		//玩家信息单元
		DStageBullet.Builder bulletInfo = DStageBullet.newBuilder();
		bulletInfo.setBulletSn(confBullet.sn);
		bulletInfo.setSkillSn(skillSn);
		bulletInfo.setPos(runPosBegin.toMsg());
		bulletInfo.setPosTar(runPosEnd.toMsg());
		bulletInfo.setUnitTarID(unitObjTar == null ? 0 : unitObjTar.id);
		bulletInfo.setCreateId(worldObjectCreate == null ? 0 : worldObjectCreate.id);
		bulletInfo.setFireId(unitObjFire == null ? 0 : unitObjFire.id);
		
		objInfo.setObjId(id);
		objInfo.setType(EWorldObjectType.BULLET);
		objInfo.setName(name);
		objInfo.setModelSn(modelSn);
		objInfo.setPos(this.posNow.toMsg());
		objInfo.setBullet(bulletInfo);
		
//		Log.common.info("createMsg {}", confBullet.sn);
		return objInfo;
	}
	public void leave() {
		runPosEnd.x = runPosBegin.x;
		runPosEnd.y = runPosBegin.y;
		this.stageLeave();
	}
	/**
	 * 为了便于运算的暂存值
	 */
	private void initTempValue() {
		if(runPosEnd.equals(runPosBegin)) {
			return;
		}
		
		//起始至目标横纵偏移量
		double diffX = runPosEnd.x - runPosBegin.x;
		double diffY = runPosEnd.y - runPosBegin.y;
		
		//实际距离
		double diffTrue = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
		if(diffTrue == 0) {
			runTempSin = 0;
			runTempCos = 0;
			return;
		}
		
		//起始至目标的Sin,Cos值
		runTempSin = diffY / diffTrue;
		runTempCos = diffX / diffTrue;
	}

}
