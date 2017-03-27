package org.gof.demo.worldsrv.general;

import java.util.List;
import java.util.Map.Entry;

import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.CSEnterGeneralTask;
import org.gof.demo.worldsrv.msg.Msg.CSFragInfo;
import org.gof.demo.worldsrv.msg.Msg.CSGenTaskFightTimes;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralEquipUp;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralExpAdd;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralInfo;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralInfoAttIng;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralList;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralQualityUp;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralRecruit;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralStarUp;
import org.gof.demo.worldsrv.msg.Msg.CSGeneralToAttIng;
import org.gof.demo.worldsrv.msg.Msg.CSOneFragInfo;
import org.gof.demo.worldsrv.msg.Msg.CSSellFrag;
import org.gof.demo.worldsrv.msg.Msg.DGeneralInfo;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralInfo;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralInfoAttIng;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralList;

public class GeneralMsgHandler {

	/**
	 * 获取已经招募的伙伴信息
	 * @param param
	 */
	@MsgReceiver(CSGeneralList.class)
	public void onCSGeneralList(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		
		SCGeneralList.Builder msg = SCGeneralList.newBuilder();
		
		for(Entry<Long, CharacterObject> gen : humanObj.slaves.entrySet()){
			DGeneralInfo.Builder msgSend = GeneralManager.inst().createDGeneralInfo(gen.getValue());		
			msg.addUnits(msgSend);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 武将招募
	 * @param param
	 */
	@MsgReceiver(CSGeneralRecruit.class)
	public void onCSGeneralRecruit(MsgParam param) {
		CSGeneralRecruit msg = param.getMsg();
		
		GeneralPlusManager.inst().recriuit(param.getHumanObject(), msg.getSn());
	}
	
	/**
	 * 获取武将信息
	 * @param param
	 */
	@MsgReceiver(CSGeneralInfo.class)
	public void onCSGeneralInfo(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGeneralInfo msg = param.getMsg();
		
		long genID = msg.getId();
		
		SCGeneralInfo.Builder msgSend = SCGeneralInfo.newBuilder();
		//获得武将的具体信息
		CharacterObject unitObj = humanObj.slaves.get(genID);
		if(unitObj != null) {
			msgSend.setUnit(GeneralManager.inst().createDGeneralInfo(unitObj));
			humanObj.sendMsg(msgSend);
		}		
	}

	
	/**
	 * 伙伴消费经验丹加经验
	 * @param param
	 */
	@MsgReceiver(CSGeneralExpAdd.class)
	public void onCSGeneralExpAdd(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSGeneralExpAdd msg = param.getMsg();
		
		long genID = msg.getId();	
		int propSn = msg.getPropSn();
		int propNum = msg.getPropNum();
		
		GeneralPlusManager.inst().genExpAddWithProp(humanObj, genID, propSn, propNum);
	}
	
	/**
	 * 伙伴升星级
	 * @param param
	 */
	@MsgReceiver(CSGeneralStarUp.class)
	public void onCSGeneralStarUp(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSGeneralStarUp msg = param.getMsg();
		
		long genID = msg.getId();		
		GeneralPlusManager.inst().generalStarUp(humanObj, genID);
	}
	
	/**
	 * 伙伴升品质
	 * @param param
	 */
	@MsgReceiver(CSGeneralQualityUp.class)
	public void onCSGeneralQualityUp(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSGeneralQualityUp msg = param.getMsg();
		
		long genID = msg.getId();		
		GeneralPlusManager.inst().generalQualityUp(humanObj, genID);		
	}
	
	/**
	 * 伙伴装备升品质
	 * @param param
	 */
	@MsgReceiver(CSGeneralEquipUp.class)
	public void onCSGeneralEquipUp(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSGeneralEquipUp msg = param.getMsg();
		
		long genID = msg.getId();		
		int equipId = msg.getEquipSn();
		GeneralPlusManager.inst().generalEquipQualityUp(humanObj, genID, equipId);		
	}
	
	/**
	 * 伙伴碎片信息
	 */
	@MsgReceiver(CSFragInfo.class)
	public void onCSFragInfo(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		
		if(humanObj.fragInfo.isEmpty()){
			GeneralFragManager.inst().initFragInfo(humanObj);
		}
		GeneralFragManager.inst().onSCFragInfo(humanObj);
	}

	/**
	 * one 伙伴碎片信息
	 */
	@MsgReceiver(CSOneFragInfo.class)
	public void onCSOneFragInfo(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSOneFragInfo msg = param.getMsg();
		
		if(humanObj.fragInfo.isEmpty()){
			GeneralFragManager.inst().initFragInfo(humanObj);
		}
		GeneralFragManager.inst().onSCOneFragInfo(humanObj, msg.getFragSn());
	}
	
	/**
	 * 出售碎片
	 * @param param
	 */
	@MsgReceiver(CSSellFrag.class)
	public void onCSSellFrag(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSSellFrag msg = param.getMsg();
		
		List<Integer> frags = msg.getFragSnList();
		
		GeneralFragManager.inst().sellFrag(humanObj, frags);
	}
	
	/**
	 * 获得伙伴副本当日剩余挑战次数信息
	 * @param param
	 */
	@MsgReceiver(CSGenTaskFightTimes.class)
	public void onCSGenTaskFightTimes(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		
		if(humanObj.genTaskFightTimes.isEmpty()){
			GeneralTaskManager.inst().resetGenFightTimes(humanObj);
		}
		
		GeneralTaskManager.inst().onSCGenTaskFightTimes(humanObj);
	}
	
	/**
	 * 进入伙伴副本
	 * @param param
	 */
	@MsgReceiver(CSEnterGeneralTask.class)
	public void onCSEnterGeneralTask(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		
		CSEnterGeneralTask msg = param.getMsg();
		int taskSn = msg.getSn();
		
		GeneralTaskManager.inst().enterTask(humanObj, taskSn);
	}
	
	
	/**
	 * 伙伴上阵
	 * @param param
	 */
	@MsgReceiver(CSGeneralToAttIng.class)
	public void onCSGeneralToAttIng(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGeneralToAttIng msg = param.getMsg();

		List<Long> unitIds = msg.getUnitsList();
		
		GeneralManager.inst().generalToAtting(humanObj, unitIds, 100);
	}
	
	
	
	/**
	 * 查询出战武将信息
	 * @param param
	 */
	@MsgReceiver(CSGeneralInfoAttIng.class)
	public void onCSGeneralInfoAttIng(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSGeneralInfoAttIng msg = param.getMsg();
		int type = msg.getType();
		
		//取出武将列表，如果为空，就取副本的，副本类型是1
		List<Long> generalList = GeneralManager.inst().getFightGeneral(humanObj, type);
		if(generalList.isEmpty()){
			generalList = GeneralManager.inst().getFightGeneral(humanObj, 1);
		}
		
		//发送消息
		SCGeneralInfoAttIng.Builder msgSend = SCGeneralInfoAttIng.newBuilder();
		msgSend.setType(type);
		msgSend.addAllUnits(generalList);
		humanObj.sendMsg(msgSend);		
	}
	
	
}
