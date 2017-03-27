package org.gof.demo.battlesrv.dot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gof.core.support.Param;
import org.gof.core.support.TickTimer;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.TemporaryObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfDot;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.msg.Msg.DHpChgOnce;
import org.gof.demo.worldsrv.msg.Msg.DHpChgTar;
import org.gof.demo.worldsrv.msg.Msg.DStageDot;
import org.gof.demo.worldsrv.msg.Msg.DStageObject;
import org.gof.demo.worldsrv.msg.Msg.EWorldObjectType;
import org.gof.demo.worldsrv.msg.Msg.SCFightDotHpChg;
import org.gof.demo.worldsrv.msg.Msg.SCFightHpChg;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;

/**
 * 持续伤害dot
 */
public class DotObject extends TemporaryObject {
	
	private UnitObject unitObjFire;		//释放dot的unitObj
	private WorldObject worldObjectCreate;	//生成bullet的对象， 可能是玩家 可能是武将 可能是 dot 可能是bullet
	private UnitObject unitObjTar;		//初始作用目标
	private double dotTimePct = 1;			//dot消失百分比
	private ConfDot confDot;
	public int skillSn;
	public List<Long> trrigerList = new ArrayList<>();	//触发事件序列
	private long timeEnd;				//结束时间
	private TickTimer updateTimer;
	private TickTimer pulseTimer;
	public SCFightHpChg.Builder hpChangeMsg = null;				//技能伤害血量包
	private Param param;
	private int intervalElapse = 0;
	
	private long delayPulse;
	
	public DotObject(StageObject stage, ConfDot confDot, int skillSn, UnitObject unitObjFire, WorldObject worldObjectCreate, UnitObject unitObjTar, Vector2D vec, double dotPct) {
		super(stage);
		long curr = stage.getTime();
		this.unitObjFire = unitObjFire;
		this.fireObj = unitObjFire;
//		Log.fight.info("DotObject :{} {}", unitObjFire.id, unitObjFire.name);
		this.worldObjectCreate = worldObjectCreate;
		this.unitObjTar = unitObjTar;
		this.dotTimePct = dotPct;
		this.confDot = confDot;
		this.skillSn = skillSn;
		this.timeEnd = curr + (int)(confDot.totalTime * dotTimePct)  + confDot.timeDelay;
		this.posNow.set(vec);
		this.delayPulse = confDot.timeDelay;
//		Log.fight.info("doDot :{} {}", confDot.totalTime, dotTimePct);
//		Log.fight.info("DotObject :{} {} {} {}", id, curr, stage.stopLastTime, this.timeEnd - curr);
		for(int i = 0 ; i < confDot.interval.length ; i++) {
			//查找效果配置表
			//设置差值 1000 3000 5000 => 1000 2000 2000
			int temp = confDot.interval[i];
			trrigerList.add(temp - intervalElapse + this.delayPulse);
			intervalElapse = temp;
		}
		
		if(trrigerList.size() > 0) {
			this.updateTimer = new TickTimer(curr, trrigerList.remove(0));
		} else {
			updateTimer.start(Integer.MAX_VALUE);
		}
		
	}
	
	/**
	 * 初始化，如果给释放者加buff
	 */
	public void init() {
		pulseTimer = new TickTimer(stageObj.getTime(), 50, true);
		param = new Param("dot", this, "hpLostKey", HpLostKey.DOT);
	}
	
	/**
	 * 将本次造成掉血消息构造进用户造成掉血信息
	 * @param humanObject
	 * @param id
	 * @param dhpChgOnce
	 */
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
	public void pulse(int deltaTime) {
		super.pulse(deltaTime);
		if(timeCurr > timeEnd) {
			//去除清理功能
			this.stageLeave();
//			Log.fight.info("stageLeave :{} {}", id, this.timeEnd - curr);
			return ;
		}
//		Log.fight.info("pulse :{} {} {} {} {}", id, curr, stageObj.stopLastTime, this.timeEnd - curr, updateTimer.getTimeLeft(curr));
		if(!updateTimer.isPeriod(timeCurr)) {
			
		}  else {
			//如果触发的list 没有了 那么直接让updateTimer 为不可到达时间
			if(trrigerList.size() > 0) {
				updateTimer.start(getTime(), trrigerList.remove(0));
			} else {
				updateTimer.start(Integer.MAX_VALUE);
			}
			
			//dot作用
			doDot();
			
			//循环本地图玩家,广播血包
			if(hpChangeMsg != null) {
				SCFightDotHpChg.Builder msg = SCFightDotHpChg.newBuilder();
				msg.setDotSn(confDot.sn);
				msg.setHpChg(hpChangeMsg);
				msg.setCastId(unitObjFire.id);
				StageManager.inst().sendMsgToArea(msg, stageObj, posNow);
				hpChangeMsg = null;
			}
		}
		
		
		
		if(!pulseTimer.isPeriod(timeCurr)) {
			return;
		} 
		
		//如果是跟随人移动的dot，重置dot位置
		if(confDot.moveType == 1) {
			if(confDot.targetSelf) {
				if(unitObjFire == null || unitObjFire.isDie()) {
					this.stageLeave();
					return ;
				}
				
				this.posNow.set(unitObjFire.posNow);
			} else {
				if(unitObjTar == null || unitObjFire.isDie()) {
					this.stageLeave();
					return ;
				}
				
				this.posNow.set(unitObjTar.posNow);
			}
		} 
		//如果可以打断， 那么释放者没了 或者不能释放技能了就消失
		if(confDot.interrupt) {
			if(unitObjFire == null || unitObjFire.isDie() || !unitObjFire.canCastSkill) {
//				Log.fight.info("stageLeaveinterrupt :{} {}", id, this.timeEnd - timeCurr);
				this.stageLeave();
			}
		}
	}
		
	/**
	 * dot作用
	 */
	public void doDot() {
		List<UnitObject> tarsList = new ArrayList<UnitObject>();
		Set<UnitObject> tarsSet = new HashSet<>();
		//获得对应的 技能逻辑
		if(confDot.skillLogic != null && confDot.skillLogic.length > 0 ) {
			for (int i : confDot.skillLogic) {
//				Log.fight.info("doDot :{}", id);
//				Log.fight.info("doDot :{} {}", id, this.timeEnd - StageManager.inst().getTime(this));
				tarsList = SkillManager.inst().executeSkillLogic(i, unitObjFire, this, unitObjTar, posNow, param);
//				Log.fight.info("doDot tarsList :{}", tarsList.size());
				for (UnitObject unit : tarsList) {
					tarsSet.add(unit);
				}
			}
		}
	}

	@Override
	public Msg.DStageObject.Builder createMsg() {
		//玩家信息单元
		DStageDot.Builder dotInfo = DStageDot.newBuilder();
		dotInfo.setDotSn(this.confDot.sn);
		dotInfo.setPos(this.posNow.toMsg());
		dotInfo.setScopeType(0);
		dotInfo.setScopeParam1(0);
		dotInfo.setScopeParam2(360);
		dotInfo.setCreateId(worldObjectCreate == null ? null : worldObjectCreate.id);
		
		DStageObject.Builder objInfo = DStageObject.newBuilder();
		objInfo.setObjId(id);
		objInfo.setType(EWorldObjectType.DOT);
		objInfo.setName(name);
		objInfo.setModelSn(modelSn);
		objInfo.setPos(this.posNow.toMsg());
		objInfo.setDot(dotInfo);
		
		return objInfo;
	}
}
