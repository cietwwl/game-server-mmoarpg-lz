package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.worldsrv.config.ConfCharacterMonster;
import org.gof.demo.worldsrv.config.ConfPropCalc;
import org.gof.demo.worldsrv.config.ConfPropFactor;
import org.gof.demo.worldsrv.config.ConfSkillEffect;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.Log;

/**
 * 变身技能：添加怪物的属性， 修改怪物的技能 只限于怪物
 * @author rattler
 *
 */
public class SkillLogic006 extends AbstractSkillLogicActive{
	public String monsSN;			//怪物sn
	@Override
	public void init(SkillCommon skillCommon, ConfSkillEffect conf) {
		super.init(skillCommon, conf);
		monsSN = (conf.param1);
		
	}
	
	@Override
	 public void doSkillEffectToTar(UnitObject unitDef) {
		if(!unitDef.isMonsterObj()) {
			return;
		}
		
		ConfCharacterMonster conf = ConfCharacterMonster.get(monsSN); 
		if(conf == null) {
			Log.fight.info("SkillLogic006 monSn error: {}", monsSN);
			return;
		}
		
		UnitObject monObj = unitDef;
		
		ConfPropCalc propBase = ConfPropCalc.get(conf.level);
		ConfPropFactor propFactor = ConfPropFactor.get(conf.propFactor);
		PropCalc basePropCalc = new PropCalc(Utils.toJOSNString(propBase.propName, propBase.propValue));
		basePropCalc.mul(propFactor.propName, propFactor.propValue);
		
		unitDef.dataPers.unitPropPlus.setBase(basePropCalc.toJSONStr());
		
        //清理技能
        monObj.getUnit().setSkill("{}");
        
		//初始化技能
		monObj.getUnit().setSkill(SkillManager.inst().firstInitSkills(conf.skillGroupSn));
		
		//初始化技能
		SkillManager.inst().initSkill(monObj);
		
		UnitManager.inst().propCalc(monObj);
		
		monObj.modelSn = conf.modelSn;
		monObj.sn = monsSN;
		monObj.name = conf.name;
		
		//发送怪物更新消息
		StageManager.inst().sendMsgToArea(monObj.createMsgUpdate(), unitDef.stageObj, unitDef.posNow);
	}

}
