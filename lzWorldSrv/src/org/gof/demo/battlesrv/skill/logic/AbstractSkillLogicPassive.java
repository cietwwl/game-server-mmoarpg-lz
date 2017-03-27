package org.gof.demo.battlesrv.skill.logic;

import org.gof.demo.battlesrv.skill.SkillEventKey;
import org.gof.demo.battlesrv.skill.SkillParam;
import org.gof.demo.battlesrv.skill.SkillParamVO;
import org.gof.demo.battlesrv.stageObj.UnitObject;



/**
 * 被动技能效果
 * @author new
 *
 */
public abstract class AbstractSkillLogicPassive extends AbstractSkillLogic{

	public abstract boolean canTrigger(SkillEventKey key, boolean isAtker);
	
	/**
	 * 被动技能
	 * @param skillParamVO
	 */
	public abstract void trigger(SkillEventKey key, UnitObject tarUnit,  SkillParam position, SkillParamVO skillParamVO);
	
}
