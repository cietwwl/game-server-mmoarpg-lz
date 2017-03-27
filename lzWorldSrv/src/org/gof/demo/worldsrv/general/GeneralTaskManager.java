package org.gof.demo.worldsrv.general;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfGeneralTask;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DGenTaskFightTimes;
import org.gof.demo.worldsrv.msg.Msg.DGeneralTaskReward;
import org.gof.demo.worldsrv.msg.Msg.SCGenTaskFightTimes;
import org.gof.demo.worldsrv.msg.Msg.SCEnterGeneralTask;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralTaskFightResult;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralTaskReward;
import org.gof.demo.worldsrv.produce.ProduceManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONObject;


public class GeneralTaskManager extends ManagerBase{

	
	public static int CAN_FIGHT_TIMES_DAY = 5;   //伙伴副本每个类型每天可挑战次数
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static GeneralTaskManager inst() {
		return inst(GeneralTaskManager.class);
	}
	
	/**
	 * 每日五点重置伙伴副本挑战次数
	 * @param param
	 */
	@Listener(EventKey.HUMAN_RESET_FIVE)
	public void resetGenFightTimes(Param param){
		HumanObject humanObj = param.get("humanObj");
		
		//重置
		resetGenFightTimes(humanObj);
		
		//发送消息
		onSCGenTaskFightTimes(humanObj);
	}
	
	
	/**
	 * 重置伙伴副本挑战次数
	 * @param humanObj
	 */
	public void resetGenFightTimes(HumanObject humanObj){
		Map<Integer,Integer> genTaskFightTimes = new HashMap<>();
		//获取配置
		Collection<ConfGeneralTask> cgtAll = ConfGeneralTask.findAll();
		
		JSONObject obj =  Utils.toJSONObject(humanObj.getHuman().getGenTaskFightTimes());
		for(ConfGeneralTask cgt : cgtAll){
			String key = String.valueOf(cgt.type);
			if(!obj.containsKey(key)){
				obj.put(key, 0);
			}
			
			if(!genTaskFightTimes.containsKey(cgt.type)){
				genTaskFightTimes.put(cgt.type, 0);
			}
		}
		
		humanObj.getHuman().setGenTaskFightTimes(Utils.toJSONString(obj));
		
		humanObj.genTaskFightTimes = genTaskFightTimes;
	}
	
	/**
	 * 返回信息
	 * @param humanObj
	 */
	public void onSCGenTaskFightTimes(HumanObject humanObj){
		SCGenTaskFightTimes.Builder msg = SCGenTaskFightTimes.newBuilder();
		for(Entry<Integer,Integer> info : humanObj.genTaskFightTimes.entrySet()){
			DGenTaskFightTimes.Builder mmsg = DGenTaskFightTimes.newBuilder();		
			mmsg.setType(info.getKey());
			mmsg.setRemainingTimes(CAN_FIGHT_TIMES_DAY > info.getValue()?CAN_FIGHT_TIMES_DAY - info.getValue() : 0);
			
			msg.addInfo(mmsg);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 更新挑战次数
	 * @param humanObj
	 * @param type
	 */
	public void updateFightTimes(HumanObject humanObj, int type){
		JSONObject obj =  Utils.toJSONObject(humanObj.getHuman().getGenTaskFightTimes());
		
		String key = String.valueOf(type);	
		int times = obj.containsKey(key)?obj.getIntValue(key):0;
		
		obj.put(key,times+1);
		
		//存入数据
		humanObj.getHuman().setGenTaskFightTimes(Utils.toJSONString(obj));	
		
		humanObj.genTaskFightTimes.put(type, times+1);
		
		//return result
		onSCGenTaskFightTimes(humanObj);
	}
	
	/**
	 * 进入伙伴副本
	 * @param humanObject
	 */
	public void enterTask(HumanObject humanObj, int taskSn){
		//配置
		ConfGeneralTask cgt = ConfGeneralTask.get(taskSn);
		if(cgt == null){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("伙伴副本配置不存在"));
			return;
		}
		
		//判断是否开放
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_WEEK); 
		if(!Arrays.asList(cgt.openDay).contains(day - 1)){
			taskEnterFailResult(humanObj, "未开放");
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("未开放"));
			return;
		}
		
		//判断玩家等级
		if(humanObj.getHuman().getLevel() < cgt.needLv){
			taskEnterFailResult(humanObj, "等级不足");
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("等级不足"));
			return;
		}
		
		//上阵伙伴check
		for(Entry<Long, CharacterObject> gen : humanObj.slavesAttingMap.entrySet()){
			GeneralObject generalObj = (GeneralObject) gen.getValue();
			if(!Arrays.asList(cgt.genSn).contains(generalObj.getGeneral().getSn())){
				taskEnterFailResult(humanObj, "上阵伙伴不符合要求");
				Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("上阵伙伴不符合要求"));
				return;
			}
		}
		
		//判断挑战次数
		if(humanObj.genTaskFightTimes.get(cgt.type) >= CAN_FIGHT_TIMES_DAY){
			taskEnterFailResult(humanObj, "挑战次数已用完");
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("挑战次数已用完"));
			return;
		}
		
		//判断体力
		if(humanObj.getHuman().getActValue() < cgt.conCost){
			taskEnterFailResult(humanObj, "体力不足");
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("体力不足"));
			return;
		}
		
		//检查背包空位
		List<ProduceVo> itemProduce = ProduceManager.inst().simpleGainByMaxProb(humanObj, cgt.produceSn);
		ReasonResult result = ProduceManager.inst().canGiveProduceItem(humanObj, itemProduce);
		if(!result.success){
			taskEnterFailResult(humanObj, "背包剩余位置不足");
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("背包剩余位置不足"));
			return;
		}
		
		//TODO   切换场景   进入副本
		
		
		//记录挑战时间
		humanObj.genTaskFightTime = Port.getTime();
		
		//return result
		SCEnterGeneralTask.Builder msg = SCEnterGeneralTask.newBuilder();
		msg.setResult(true);
		msg.setEnterTime(Port.getTime());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 进入副本失败
	 * @param humanObj
	 * @param reason
	 */
	public void taskEnterFailResult(HumanObject humanObj,String reason){
		SCEnterGeneralTask.Builder msg = SCEnterGeneralTask.newBuilder();
		msg.setResult(false);
		msg.setReason(reason);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * TODO 伙伴副本战斗结果监听
	 * @param param
	 */
	public void genTaskfightResult(Param param) {
		HumanObject humanObj = param.get("humanObj");   //用户对象
		int taskSn = param.get("genTaskSn");   //伙伴副本SN
		
		//胜利发布奖励    更新挑战次数
		boolean win = true;
		if(win){
			victoryHandle(humanObj, taskSn);
		}else{
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("挑战失败"));
			fightResult(humanObj, win, "挑战失败");
		}		
	}
	
	/**
	 * 挑战结果
	 * @param humanObj
	 * @param result
	 * @param reason
	 */
	public void fightResult(HumanObject humanObj, boolean result, String reason){
		SCGeneralTaskFightResult.Builder msg = SCGeneralTaskFightResult.newBuilder();
		msg.setResult(result);
		if(!"".equals(reason)){
			msg.setReason(reason);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 挑战胜利加奖励     更新挑战次数
	 * @param humanObj
	 * @param taskSn
	 */
	public void victoryHandle(HumanObject humanObj, int taskSn){
		//配置
		ConfGeneralTask cgt = ConfGeneralTask.get(taskSn);
		
		long start = humanObj.genTaskFightTime;  //from humanObj
		if(Port.getTime() - start > cgt.timeLimit*1000){
			Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("战斗超出时限"));
			fightResult(humanObj, false, "战斗超出时限");
			return;
		}
		
		//更新挑战次数    if win: +
		updateFightTimes(humanObj, cgt.type);
				
		//RETURN FIGHT RESULT
		fightResult(humanObj, true, "");
		
		//给玩家发奖励
		List<ProduceVo> produce = ProduceManager.inst().getAndGiveSimpleGain(humanObj, cgt.produceSn);
		
		//返回奖励
		SCGeneralTaskReward.Builder msg = SCGeneralTaskReward.newBuilder();
		for(ProduceVo pv : produce){
			DGeneralTaskReward.Builder subMsg = DGeneralTaskReward.newBuilder();
			subMsg.setSn(pv.sn);
			subMsg.setNum(pv.num);
			
			msg.addInfo(subMsg);
		}
		humanObj.sendMsg(msg);
	}
	
}
