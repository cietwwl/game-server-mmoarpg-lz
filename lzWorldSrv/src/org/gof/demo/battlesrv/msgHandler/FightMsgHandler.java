package org.gof.demo.battlesrv.msgHandler;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.battlesrv.manager.FightManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfSkill;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.CSFightAtk;
import org.gof.demo.worldsrv.msg.Msg.CSFightRevive;
import org.gof.demo.worldsrv.msg.Msg.CSSkillAddGeneral;
import org.gof.demo.worldsrv.msg.Msg.CSSkillInterrupt;
import org.gof.demo.worldsrv.msg.Msg.CSSkillRemoveGeneral;
import org.gof.demo.worldsrv.msg.Msg.SCFightAtkResult;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.test.TestManager;

public class FightMsgHandler {
	
	/**
	 * 请求施放技能
	 * @param param
	 */
	@MsgReceiver(CSFightAtk.class)
	public void onCSFightAtk(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSFightAtk msg = param.getMsg();
		long sendId = msg.getCasterId();
		
		if(humanObj.stageObj == null) {
			return;
		}
		
		if(humanObj.stageObj.conf.type.equals(StageMapTypeKey.common.getContent())) {
//			Inform.user(humanObj.id, Inform.提示操作, "主地图中不能战斗");
//			return;
		}
		
		//获得指定角色
		UnitObject unitObject = humanObj.getUnitControl(sendId);
		if(unitObject == null) {
			unitObject = humanObj.stageObj.getUnitObj(sendId);
			if(unitObject == null) {
				Log.common.info("CSFightAtk unitObj = null");
				return;
			}
		}
		
		//技能sn
		int skillSn = msg.getSkillId();
		
		ConfSkill conf = ConfSkill.get(skillSn);
		if(conf == null) return ;
		
		SkillParam skillParam = new SkillParam();
		if(msg.hasTarId() && conf.clickType  == SkillCommon.CLICK_TAR) {
			long tarId = msg.getTarId();
			
			UnitObject unitObj = unitObject.stageObj.getUnitObj(tarId);
			
			//目标不存在
			if(unitObj == null) {
				return ;
			}
			skillParam.tarUo = unitObj;
			skillParam.tarPos = unitObj.posNow;
		}
		
		if(msg.hasPos() && conf.clickType == SkillCommon.CLICK_VECTOR) {
			skillParam.tarPos = new Vector2D(msg.getPos());
			if(msg.hasTarId()) {
				long tarId = msg.getTarId();
				UnitObject unitObj = unitObject.stageObj.getUnitObj(tarId);
				skillParam.tarUo = unitObj;
			}
		}
		
		//技能配置的点选目标是自己
		if(conf.clickType == SkillCommon.CLICK_SELF) {
			skillParam.tarUo = unitObject;
			skillParam.tarPos = unitObject.posNow;
		}
		
		//是否是连击或连点技能最终伤害
		skillParam.finalAtk = msg.hasFinal() ? msg.getFinal() : false;
		
		ReasonResult result = SkillManager.inst().shakeOrCastSkill(unitObject, skillSn, skillParam);
		if(!result.success) {
			long time = unitObject.skillTempInfo.lastSkillTime - unitObject.getTime();
			if(TestManager.DEBUG) {
				Inform.user(humanObj.id, Inform.提示错误, result.reason + String.valueOf(skillSn) + String.valueOf(time) + String.valueOf(unitObject.skillTempInfo.lastSkillSn));
			}
			Log.fight.info("shakeOrCastSkill Error: {} {} {} {} {} ", result.reason, unitObject.name, skillSn, time, unitObject.skillTempInfo.lastSkillSn);
			SCFightAtkResult.Builder sendMsg = SCFightAtkResult.newBuilder();
			sendMsg.setResultCode(-1);
			sendMsg.setSendId(sendId);
			humanObj.sendMsg(sendMsg);
		}
	}
	
	/**
	 * 人物请求复活
	 * @param param
	 */
	@MsgReceiver(CSFightRevive.class)
	public void onCSFightRevive(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSFightRevive msg = param.getMsg();
		
		//type: 1回城复活，2原地复活
		int type = 2;
		if(msg.hasType()) {
			type = msg.getType();
		}
		
		if(type != 1 && type != 2) {
			Log.fight.info(I18n.get("common.tip.paramWrong"));
			return ;
		}
		
		ReasonResult result = HumanManager.inst().revive(humanObj, type, false);
		if(!result.success) {
			Log.fight.info(result.reason);
			return ;
		}
		
		if(type == 2) {
			Log.fight.info( I18n.get("fight.revive.success1"));
		} else {
			Log.fight.info( I18n.get("fight.revive.success2"));
		}
	}
	
	
	/**
	 * 发送技能打断
	 * @param param
	 */
	@MsgReceiver(CSSkillInterrupt.class)
	public void onCSSkillInterrupt(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSkillInterrupt msg = param.getMsg();
		long sendId = msg.getUnitObjID();
		
		if(humanObj.stageObj == null) {
			return;
		}
		
		//获得指定角色
		UnitObject unitObject = humanObj.getUnitControl(sendId);
		if(unitObject == null) {
			unitObject = humanObj.stageObj.getMonsterObj(sendId);
			if(unitObject == null) {
				Log.common.info("onCSStageMove unitObj = null");
				return;
			}
		}
		
		unitObject.interruptCurrSkill(unitObject);
		
		
	}
	
	/**
	 * 战斗剧情添加武将接口
	 * @param param
	 */
	@MsgReceiver(CSSkillAddGeneral.class)
	public void onCSSkillAddGeneral(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSkillAddGeneral msg = param.getMsg();
		String sn = msg.getGeneralSn();
		Vector2D vec = new Vector2D(msg.getPos());
		
		FightManager.inst().onCSSkillAddGeneral(humanObj, sn, vec);
	}
	
	/**
	 * 战斗剧情删除武将
	 * @param param
	 */
	@MsgReceiver(CSSkillRemoveGeneral.class)
	public void onCSSkillRemoveGeneral(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSSkillRemoveGeneral msg = param.getMsg();
		long id = msg.getUnitObjID();
		
		FightManager.inst().onCSSkillRemoveGeneral(humanObj, id);
	}
	
}