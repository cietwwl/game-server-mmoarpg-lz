package org.gof.demo.battlesrv.skill.logic;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.SysException;
import org.gof.demo.battlesrv.skill.SkillCommon;
import org.gof.demo.worldsrv.config.ConfSkillEffect;

public class SkillLogicManager extends ManagerBase {
	
//			1, SkillLogic001.class ,		//直接伤害
//			2, SkillLogic002.class ,		//dot
//			3, SkillLogic003.class, 		//buff
//			4, SkillLogic004.class, 	 	//bullet
//			5, SkillLogic005.class,			//将目标扔到最远的单位，并产生伤害， 如果targetSelf是true，那么是扔自己到地方最远单位
//			6, SkillLogic006.class,			//添加怪物的属性， 修改怪物的技能 只限于怪物
//			100, SkillLogic100.class,		//(被动)被攻击方释放各种dot buff bullet
//			101, SkillLogic101.class, 		//(被动)用于掉血连续触发buf  dot bullet 比如 5% 10% 15%等  80,60,40,20
//			102, SkillLogic102.class, 		//攻击方吸血(被动)
//			103, SkillLogic103.class,		//攻击概率触发技能 连击 攻击者(被动)
//			104, SkillLogic104.class, 		//被攻击概率触发技能 反击 防御者(被动)
//			105, SkillLogic105.class, 		//死亡后释放技能(被动)
//			106, SkillLogic106.class 		//(被动)打断也能释放技能 见大法师的推波。 
//			107, SkillLogic107.class 		//(被动)多重施法
	
	public static SkillLogicManager inst() {
		return inst(SkillLogicManager.class);
	}

	/**
	 * 初始化技能效果参数
	 * @param sn	技能效果id
	 * @return
	 */
	public AbstractSkillLogic initLogic(SkillCommon skillCommon, int sn) {
		ConfSkillEffect confSkillEffect = ConfSkillEffect.get(sn);
		
		if(confSkillEffect == null) {
			throw new SysException("技能效果没找到，效果id={}", sn);
		}
		
		//初始化技能效果
		try {
			AbstractSkillLogic skillLogic = instance(confSkillEffect.logicSn);
			skillLogic.init(skillCommon, confSkillEffect);
			return skillLogic;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	public AbstractSkillLogic instance(int logicSn) {
		AbstractSkillLogic skillLogic = null;
		switch (logicSn) {
		case 1: {
			skillLogic = new SkillLogic001();
		}break;
		case 2: {
			skillLogic = new SkillLogic002();
		}break;
		case 3: {
			skillLogic = new SkillLogic003();
		}break;
		case 4: {
			skillLogic = new SkillLogic004();
		}break;
		case 5: {
			skillLogic = new SkillLogic005();
		}break;
		case 6: {
			skillLogic = new SkillLogic006();
		}break;
		case 7: {
			skillLogic = new SkillLogic007();
		}break;
		case 100: {
			skillLogic = new SkillLogic100();
		}break;
		case 101: {
			skillLogic = new SkillLogic101();
		}break;
		case 102: {
			skillLogic = new SkillLogic102();
		}break;
		case 103: {
			skillLogic = new SkillLogic103();
		}break;
		case 104: {
			skillLogic = new SkillLogic104();
		}break;
		case 105: {
			skillLogic = new SkillLogic105();
		}break;
		case 106: {
			skillLogic = new SkillLogic106();
		}break;
		case 107: {
			skillLogic = new SkillLogic107();
		}break;
		case 109: {
			skillLogic = new SkillLogic109();
		}break;
		default:
			break;
		}
		return skillLogic;
	}
}
