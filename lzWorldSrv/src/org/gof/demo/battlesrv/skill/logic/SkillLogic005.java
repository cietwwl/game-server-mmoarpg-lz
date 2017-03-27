package org.gof.demo.battlesrv.skill.logic;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.HpLostKey;
import org.gof.demo.battlesrv.support.UnitObjectStateKey;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.msg.Msg.DBackPos;
import org.gof.demo.worldsrv.support.Log;

/**
 * 许褚的抓人咋人的技能
 * @author rattler
 *
 */
public class SkillLogic005 extends AbstractSkillLogicActive{
	public int skillLogicArr[];			//logicSn	
	private Param param;
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		
		skillLogicArr = Utils.arrayStrToInt(conf.param1);
		param = new Param("skill", skill, "hpLostKey", HpLostKey.SKILL);
	}
	
	@Override 
	public void doSkillEffect(SkillParam position) {
		UnitObject uo = skill.unitObj;
		if(uo == null) {
			return;
		}
		
		
		UnitObject OriUnitObj = position.tarUo;
		
		if(conf.targetSelf) {
//			vec = skill.unitObj.posNow;
			OriUnitObj = skill.unitObj;
		}
		
		List<UnitObject> result = new ArrayList<>();
		Vector2D targetPos = OriUnitObj.posNow;
		
		//获得对方离自己最远的单位
		//获得所有teamBundleID 是目标的东西
		for(UnitObject uo1 : OriUnitObj.stageObj.getUnitObjs().values()) {
			if(uo1.teamBundleID == OriUnitObj.teamBundleID && !uo1.isDie()) {
				result.add(uo1);
			}
		}
		
		//找到离自己最远的
//		double dis = -1;
//		double tempDis;
//		for (UnitObject uo : result) {
//			tempDis = skill.unitObj.posNow.distance(uo.posNow);
//			if(tempDis > dis) {
//				dis = tempDis;
//				resultObj = uo;
//			}
//		}
//		
//		if(resultObj == null) {
//			return;
//		}
//		
//		//获得要移动的位置
//		targetPos = resultObj.posNow;
		if(!uo.isDie()) {
			Vector2D backPos = Vector2D.lookAtDis(skill.unitObj.posNow, OriUnitObj.posNow, OriUnitObj.posNow, 3.0);
			targetPos = SkillManager.inst().getDefBackPos(true, OriUnitObj, backPos, 2.5);
			Log.fight.info("targetPos {} {}", targetPos.x, targetPos.y);
			OriUnitObj.posNow = targetPos;
		}
		
		//发送消息
		DBackPos.Builder backPosMsg = DBackPos.newBuilder();
		backPosMsg.setId(OriUnitObj.id);
		backPosMsg.setPos(targetPos.toMsg());
		backPosMsg.setType(3);
		skill.scFightSkill.addBackPos(backPosMsg);
		//更新怪物的位置
		OriUnitObj.posNow = targetPos;
		//更新状态
		OriUnitObj.toState(UnitObjectStateKey.silence, 1000);
		
		
		//在移动的位置调用伤害
		Long useId = uo.id;
		
		if(useId != null) {
			if(skillLogicArr != null && skillLogicArr.length > 0 ) {
				for (int i : skillLogicArr) {
					SkillManager.inst().executeSkillLogic(i, uo, uo, OriUnitObj, targetPos, param);
				}
			}
		}
		
	}

	@Override
	public void doSkillEffectToTar(UnitObject unitDef) {
		
	}
}
