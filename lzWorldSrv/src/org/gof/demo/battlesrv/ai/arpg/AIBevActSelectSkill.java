package org.gof.demo.battlesrv.ai.arpg;

import org.gof.core.support.Param;
import org.gof.demo.battlesrv.ai.AI;
import org.gof.demo.battlesrv.ai.bevTree.AIBevLeaf;
import org.gof.demo.battlesrv.stageObj.UnitObject;

/**
 * AI：释放技能前的技能选择
 * 
 * @author GaoZhangCheng
 */
public class AIBevActSelectSkill extends AIBevLeaf {

	public static final String HP = "hp";
	public static final String HPPER = "hpPer";
	
	public AIBevActSelectSkill(AI ai) {
		this.ai = ai;
	}

	@Override
	public boolean execute(Param param) {
		
		// 不在了，就返回
		UnitObject uniObj = ai.unitObj;
	
		if(!uniObj.isInWorld() || uniObj.isDie()){
			return false;
		}
				
		//获取技能的触发
		String[] trigger = ai.conf.trigger;
		int skillSn =-1;
		
		//如果触发为空，做非空处理
		if(trigger == null || trigger.length == 0){
			trigger = new String[]{};
		}
		
		//挨个遍历要选择的技能
		for (int i = 0; i < trigger.length; i++) {
			String tri = trigger[i];
			switch (tri) {
			
			// 具体血量
			case HP:	
				if(uniObj.getUnit().getHpCur() <= ai.conf.triggerValue[i]){
					skillSn = ai.conf.triggerSkill[i];
				}
				break;
				
			// 血量百分比
			case HPPER:	
				int hpLe = uniObj.getUnit().getHpMax() * ai.conf.triggerValue[i] / 10000;
				if(uniObj.getUnit().getHpCur() <= hpLe){
					skillSn = ai.conf.triggerSkill[i];
				}
				break;
								
			default:
				break;
			}
		}
		
		//如果没有选择到触发技能条件，那么循环普通技能
		if(skillSn == -1){
			int index = ai.skillIndex % ai.conf.attack.length;
			skillSn = ai.conf.attack[index];
		}
		
		ai.curSkill = uniObj.skills.get(skillSn);
		
		return true;
	}

}
