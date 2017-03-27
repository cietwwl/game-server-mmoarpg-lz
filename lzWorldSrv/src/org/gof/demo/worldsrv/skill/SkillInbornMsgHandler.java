package org.gof.demo.worldsrv.skill;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.msg.Msg.CSAddRelSkill;
import org.gof.demo.worldsrv.msg.Msg.CSChangeInborn;
import org.gof.demo.worldsrv.msg.Msg.CSDelRelSkill;
import org.gof.demo.worldsrv.msg.Msg.CSInbornLevelup;
import org.gof.demo.worldsrv.msg.Msg.CSSkillInit;
import org.gof.demo.worldsrv.msg.Msg.CSSkillLevelup;

/**
 * 技能，天赋，羁绊技能升级相关接口
 * 
 * @author GaoZhangCheng
 */
public class SkillInbornMsgHandler {
	
	/**
	 * 初始化技能主界面
	 * @param param
	 */
	@MsgReceiver(CSSkillInit.class)
	public void onCSSkillInit(MsgParam param) {
		SkillInbornManager.inst().skillInbornInit(param.getHumanObject());
	}
	
	/**
	 * 技能升级
	 * @param param
	 */
	@MsgReceiver(CSSkillLevelup.class)
	public void onCSSkillLevelup(MsgParam param) {
		CSSkillLevelup msg = param.getMsg();
		
		SkillInbornManager.inst().skillLevelup(param.getHumanObject(), msg.getSkillSn());
	}
	
	/**
	 * 天赋升级
	 * @param param
	 */
	@MsgReceiver(CSInbornLevelup.class)
	public void onCSInbornLevelup(MsgParam param) {
		CSInbornLevelup msg = param.getMsg();
		
		SkillInbornManager.inst().inbornLevelup(param.getHumanObject(), msg.getInbornSn());
	}
	
	/**
	 * 更换天赋
	 * @param param
	 */
	@MsgReceiver(CSChangeInborn.class)
	public void onCSChangeInborn(MsgParam param) {
		CSChangeInborn msg = param.getMsg();
		
		SkillInbornManager.inst().inbornChange(param.getHumanObject(), msg.getPos(), msg.getInbornSn());
	}
	
	/**
	 * 装备羁绊技能
	 * @param param
	 */
	@MsgReceiver(CSAddRelSkill.class)
	public void onCSAddRelSkill(MsgParam param) {
		CSAddRelSkill msg = param.getMsg();
		
		SkillInbornManager.inst().setRelSkill(param.getHumanObject(), msg.getRelSkillSn(), msg.getPos());
	}
	
	/**
	 *卸下某个羁绊技能
	 * @param param
	 */
	@MsgReceiver(CSDelRelSkill.class)
	public void onCSDelRelSkill(MsgParam param) {
		CSDelRelSkill msg = param.getMsg();
		
		SkillInbornManager.inst().delRelSkill(param.getHumanObject(), msg.getRelSkillSn(), msg.getPos());
	}
}
