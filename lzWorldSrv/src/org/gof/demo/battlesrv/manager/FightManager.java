package org.gof.demo.battlesrv.manager;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.RandomUtils;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.MonsterObject;
import org.gof.demo.worldsrv.config.ConfParam;
import org.gof.demo.worldsrv.config.ConfSkillCirtParam;
import org.gof.demo.worldsrv.entity.Unit;


public class FightManager extends ManagerBase {
	/**
	 * 获取实例
	 * @return
	 */
	public static FightManager inst() {
		return inst(FightManager.class);
	}
	
	public static float PERINT = 10000;
	
	/**计算伤害公式
	 * @param unitObjAtk
	 * @param unitObjDef
	 * @param atkType 攻击类型 1是魔法攻击
	 * @return
	 */
	public int calcBaseHurt(UnitObject unitObjAtk, UnitObject unitObjDef, int atkType) {
		Unit ua = unitObjAtk.getUnit();
		Unit ud = unitObjDef.getUnit();
		
		int result = 0;
		
		//计算魔法攻击伤害
		if(atkType == 1) {
			result = ua.getAtkMag()  - ud.getDefMag() ;
		} else {
			result = ua.getAtkPhy()  - ud.getDefMag() ;
		}
		
		if(result < 1)
			return 1;
		else 
			return result;
	}
	
	/**
	 * 计算命中公式
	 * 
	 * @param uoAtk
	 * @param uoDef
	 * @return
	 */
	public boolean isHit(UnitObject uoAtk, UnitObject uoDef) {
		Unit ua = uoAtk.getUnit();
		Unit ud = uoDef.getUnit();
		//命中值修正值=己方总命中-目标总闪避
		double hitFix = ua.getHit()  - ud.getDodge();
		//最终命中值=额定命中值+命中修正值
		double hitVal = 9000D + hitFix;
		//判断最终命中值是否大于5：如果是，最终命中值=最终命中值；如果不是，最终命中值=5
		double hit = 500D;
		if(hitVal > hit) hit = hitVal;
			
		//如果：Random（1，10000）小于等于最终命中值，则为技能命中。
		int hitRoll = RandomUtils.nextInt(10000);
		if(hitRoll > hit) 
			return false;
		else 
			return true;
		
	}
	
	/**
	 * 是否是暴击
	 * @param uoAtk
	 * @param uoDef
	 * @param atkType
	 * @return
	 */
	public boolean isCrit(UnitObject uoAtk, UnitObject uoDef, int atkType) {
		Unit ua = uoAtk.getUnit();
		Unit ud = uoDef.getUnit();
		//暴击值修正值=己方总暴击-目标总韧性
		double critFix = ua.getCrit() - ud.getTough() ;
		//最终暴击值=额定暴击值+暴击修正值
		double critValue = 500D + critFix;
		//判断最终暴击值是否大于5：若是，最终暴击值=最终暴击值；否则，最终暴击值=额定暴击值。
		double crit = 0D;
		if(critValue > 500D)	crit = critValue;
		else crit = 500D;
		
		//如果：Random（1，10000）小于等于最终暴击值，则为技能暴击。
		int critRoll = RandomUtils.nextInt(10000);
		if(critRoll > crit)
			return false;
		else 
			return true;
		
	}
	
	/**
	 * 判断是否吸血
	 * @param uoAtk
	 * @return
	 */
	public boolean isSuck(UnitObject uoAtk) {
		Unit ua = uoAtk.getUnit();
		
		double suckValue = ua.getSuck();
		int suckRoll = RandomUtils.nextInt(10000);
		if(suckRoll < suckValue)
			return true;
		else 
			return false;
	}
	
	/**
	 * 计算吸血量
	 * @param uoAtk
	 * @param atkType
	 * @return
	 */
	public double bloodSuck(UnitObject uoAtk, int hoLost) {
		Unit ua = uoAtk.getUnit();
		
		double suckRatio =hoLost * ua.getSuckRatio() / 10000D;
		
		return suckRatio;
	
	}
	
	/**
	 * 属性覆盖
	 * @param unitObj
	 */
	public void recoverProp(UnitObject unitObj) {

	}
	
	/**
	 * 战斗剧情添加武将接口
	 * @param humanObj
	 * @param genSn
	 */
	public void onCSSkillAddGeneral(HumanObject humanObj, String genSn, Vector2D posBirth) {
		
		int index = 0;
		MonsterObject monsterObj = new MonsterObject(humanObj.stageObj, 0, genSn, true, index, 0);
		monsterObj.posBegin = posBirth;
		
		//添加到出战阵容
		humanObj.slavesAttingMap.put(monsterObj.id, monsterObj);
		
		//出现
		monsterObj.teamBundleID = humanObj.teamBundleID;
		monsterObj.parentObject = humanObj;
		monsterObj.startup();
	}
	
	/**战斗剧情删除武将
	 * @param humanObj
	 * @param id
	 */
	public void onCSSkillRemoveGeneral(HumanObject humanObj, long id) {
		UnitObject unitObj = humanObj.stageObj.getUnitObj(id);
		unitObj.stageLeave();
	}
	
	public int getHitParam() {
		int value = Utils.intValue(ConfParam.get("hitParam").value);
		return value;
	}
	
	public int getDodgeParam() {
		int value = Utils.intValue(ConfParam.get("dodgeParam").value);
		return value;
	}
	
	public int getCritMagParam(int level) {
		int value = ConfSkillCirtParam.get(level).critMag;
		return value;
	}
	
	public int getCritPhyParam(int level) {
		int value = ConfSkillCirtParam.get(level).critPhy;
		return value;
	}
	
	public int getToughParam() {
		int value = Utils.intValue(ConfParam.get("toughParam").value);
		return value;
	}
	
}