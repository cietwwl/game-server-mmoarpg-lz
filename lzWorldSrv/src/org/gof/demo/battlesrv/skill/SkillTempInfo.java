package org.gof.demo.battlesrv.skill;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.battlesrv.stageObj.UnitObject;

public class SkillTempInfo implements ISerilizable {
	public Map<Integer, Long> globalCool = new HashMap<>();			//公共冷却
	public Map<Integer, Long> cooldown = new HashMap<>();				//技能冷却时间
	public SkillExcute skillToExcute = null;						 	//前摇技能信息
	public int lastSkillSn; // 上次释放的技能sn
	public long lastSkillTime;
	
	public boolean godMod = false;	//无敌
	public boolean immuneMag = false;	//魔法免疫
	public boolean immunePhy = false;	//魔法免疫
	public boolean noDead = false;  //怎么都不死
	public boolean immuneControl = false; //免疫所有负面buff
	
	public int magShield = 0;//魔法盾
	public int phyShield = 0;//物理盾
	public int allShield = 0;//全伤害盾
	
	public int rageAttack = 0; //释放技能获得怒气增加
	public int rageKill = 0;//杀人回复怒气增加
	
	public int mutilMagic = 0; //多重施法次数
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(globalCool);
		out.write(cooldown);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		globalCool = in.read();
		cooldown = in.read();
	}
	
	/**
	 * 计算在魔法盾环境下的扣血
	 * @param unitObjAtk
	 * @param hpLost
	 * @return
	 */
	public int getHplostSheild(UnitObject unitObjAtk, int atkType, int hpLost) {
		if(allShield <= 0 && magShield <= 0 && phyShield <= 0) {
			return hpLost;
		}
		
		if(allShield > hpLost) {
			hpLost = 0;
			allShield -= hpLost;
		} else {
			if(allShield > 0) {
				hpLost -= allShield;
				allShield = 0;
			}
			if(atkType == 1) {
				if(magShield > hpLost) {
					hpLost = 0;
					magShield -= hpLost;
				} else {
					hpLost -= magShield;
					magShield = 0;
				}
			} else{ // if(unitObjAtk.attType == 2) 
				if(phyShield > hpLost) {
					hpLost = 0;
					phyShield -= hpLost;
				} else {
					hpLost -= phyShield;
					phyShield = 0;
				}
			}
		}
		return hpLost;
	}
}
