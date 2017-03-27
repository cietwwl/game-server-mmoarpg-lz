package org.gof.demo.worldsrv.monster;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.support.Param;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.battlesrv.skill.SkillManager;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.MonsterObject;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.support.enumKey.StageMapTypeKey;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class MonsterManager extends org.gof.core.support.ManagerBase {
	
	/**
	 * 获取实例
	 * @return
	 */
	public static MonsterManager inst() {
		return inst(MonsterManager.class);
	}
	
	/**
	 * 生成怪物
	 * @param stageObj
	 * @param sn		怪物配置SN
	 * @param num	数量	(配置)
	 * @param active	是否激活  false 站着不动，不能被攻击和攻击
	 * @param index	当前怪物的坐标索引
	 * @return
	 */
	public List<MonsterObject> create(StageObject stageObj, int stegeObjectSn, String sn, int num, boolean active, int index, int level) {
		List<MonsterObject> result = new ArrayList<>();
		if(num <= 0 ) {
			return result;
		}
		for(int i = 0; i < num ; i++) {
			MonsterObject monsterObj = new MonsterObject(stageObj, i, sn, active, index, level);
			result.add(monsterObj);
		}
		return result;
	}
	
	/**
	 * 生成怪物
	 * @param stageObj
	 * @param sn		怪物配置SN
	* @param active	是否激活  false 站着不动，不能被攻击和攻击
	 * @param index	当前怪物的坐标索引
	 * @return
	 */
	public MonsterObject create(StageObject stageObj, int stegeObjectSn, String sn, boolean active, int index, int level) {
		MonsterObject monsterObj = new MonsterObject(stageObj, stegeObjectSn, sn, active, index, level);
			
		return monsterObj;
	}
	
	@Listener(value=EventKey.HUMAN_STAGE_ENTER_BEFORE)
	public void onHumanStageEnterBefore(Param param) {
		HumanObject humanObj = param.get("humanObj");
		if(humanObj.stageObj.conf.type.equals(StageMapTypeKey.rep.getContent())) {
			for (UnitObject unitObj : humanObj.stageObj.getMonsterObjs().values()) {
				
				if(unitObj == null) continue;
				
				//所有的武将添加光环
				SkillParam skillParam = new SkillParam();
				skillParam.tarUo = unitObj;
				skillParam.tarPos = unitObj.posNow;
				for (SkillCommon skill : unitObj.skills.values()) {
					if(skill.confSkill.enterScene) {
						SkillManager.inst().shakeOrCastSkill(unitObj, skill.confSkill.sn, skillParam);
					}
				}
			}
		}
	}

}
