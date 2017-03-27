package org.gof.demo.worldsrv.general;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfCharacterGeneral;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.msg.Msg.DFragInfo;
import org.gof.demo.worldsrv.msg.Msg.SCFragInfo;
import org.gof.demo.worldsrv.msg.Msg.SCOneFragInfo;
import org.gof.demo.worldsrv.msg.Msg.SCSellFrag;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;

import com.alibaba.fastjson.JSONObject;

public class GeneralFragManager extends ManagerBase{

	/**
	 * 获取实例
	 * @return
	 */
	public static GeneralFragManager inst() {
		return inst(GeneralFragManager.class);
	}
	
	
	/**
	 * 初始化伙伴碎片信息
	 * @param humanObj
	 */
	public void initFragInfo(HumanObject humanObj){
		Map<Integer,Integer> fragInfo = new HashMap<>();  //伙伴碎片数量MAP
		Map<Integer,Integer> rareFragInfo = new HashMap<>();   //可以兑换稀有伙伴的碎片
		
		//伙伴碎片信息
		JSONObject obj =  Utils.toJSONObject(humanObj.getHuman().getFragInfo());
		
		//统计星满伙伴的碎片
		for(Entry<Long, CharacterObject> gen : humanObj.slaves.entrySet()){
			GeneralObject generalObj = (GeneralObject)gen.getValue();			
			if(generalObj.getGeneral().getStar() >= GeneralPlusManager.STAR_LEVEL_MAX){
				ConfCharacterGeneral conf = ConfCharacterGeneral.get(generalObj.getGeneral().getSn());
				String key = String.valueOf(conf.fragSn);
				if(obj.containsKey(key)){	
					int n = obj.getIntValue(key);
					if(n <= 0){
						continue;
					}
				    rareFragInfo.put(conf.fragSn, n);
				}
			}
		}
		
		for(String key : obj.keySet()){
			int m = obj.getIntValue(key);
			if(m <= 0){
				continue;
			}
			fragInfo.put(Integer.valueOf(key), obj.getIntValue(key));
		}
		
		//加入humanObj
		humanObj.fragInfo = fragInfo;
		humanObj.rareFragInfo = rareFragInfo;
	}

	
	/**
	 * +碎片
	 */
	public void fragAdd(HumanObject humanObj, int fragSn, int num){
		int hasNum = humanObj.fragInfo.containsKey(fragSn)?humanObj.fragInfo.get(fragSn):0;
		int result = hasNum + num;
		
		updateFrag(humanObj, fragSn, result);
		
		//update humanObj
		if(humanObj.rareFragInfo.containsKey(fragSn)){
			humanObj.rareFragInfo.put(fragSn, result);
		}
   
		humanObj.fragInfo.put(fragSn, result);
		
		//碎片信息推送
		onSCFragInfo(humanObj);	
	}
	
	/**
	 * 更新碎片(操作JSON）
	 */
	private void updateFrag(HumanObject humanObj, int fragSn, int num){	
		//伙伴碎片信息
		JSONObject obj =  Utils.toJSONObject(humanObj.getHuman().getFragInfo());
		
		String key = String.valueOf(fragSn);	
		obj.put(key,num);
		
		//存入数据
		humanObj.getHuman().setFragInfo(Utils.toJSONString(obj));	
	}
	
	/**
	 * 批量删除伙伴碎片 (操作JSON）
	 * @param humanObj
	 * @param frags
	 */
	private void removeFrags(HumanObject humanObj, List<Integer> frags){
		//伙伴碎片信息
		JSONObject obj =  Utils.toJSONObject(humanObj.getHuman().getFragInfo());
		
		for(int sn : frags){
			String key = String.valueOf(sn);
			if(obj.containsKey(key)){
			    obj.remove(key);
			}
		}
		
		//存入数据
		humanObj.getHuman().setFragInfo(Utils.toJSONString(obj));
	}
	
	/**
	 * 是否可减 (兑换伙伴或升星级)
	 * @param humanObj
	 * @param fragSn
	 * @param num
	 * @return
	 */
	public boolean canFragReduce(HumanObject humanObj, int fragSn, int num){
		if(!humanObj.fragInfo.containsKey(fragSn)){
			return false;
		}

		if( humanObj.fragInfo.get(fragSn) < num){
			return false;
		}
		
		return true;
	}
	
	/**
	 * -碎片(兑换伙伴或升星级)
	 * @param humanObj
	 * @param fragSn
	 * @param num
	 * @return
	 */
	public void fragReduce(HumanObject humanObj, int fragSn, int num){
		int result = humanObj.fragInfo.get(fragSn) - num;
		
		updateFrag(humanObj, fragSn, result);
		
		if(result > 0){
			humanObj.fragInfo.put(fragSn, result);
			
			if(humanObj.rareFragInfo.containsKey(fragSn)){
				humanObj.rareFragInfo.put(fragSn, result);
			}
		}
		else{
			humanObj.fragInfo.remove(fragSn);
			
			if(humanObj.rareFragInfo.containsKey(fragSn)){
				humanObj.rareFragInfo.remove(fragSn);
			}
		}
		
		//返回信息
		onSCFragInfo(humanObj);
	}
	
	/**
	 * 是否可减   (兑换稀有伙伴 或 出售处理)
	 * @param humanObj
	 * @param num
	 * @return
	 */
	public boolean canRareFragReduce(HumanObject humanObj, int num){
		int rareNum = this.getRareFragNum(humanObj);
		if(rareNum < num){
			return false;
		}
		
		return true;
	}
	
	/**
	 * -碎片（兑换稀有伙伴 或 出售处理）
	 * @param humanObj
	 * @param num
	 * @return
	 */
	public void rareFragReduce(HumanObject humanObj, int num){	
		int reduceNum = num;
		Set<Entry<Integer, Integer>> rareFrag = humanObj.rareFragInfo.entrySet();
		
		for(Entry<Integer,Integer> info : rareFrag){
			if(info.getValue() > reduceNum){
				int result = info.getValue() - reduceNum;
				
				humanObj.rareFragInfo.put(info.getKey(), result);
				humanObj.fragInfo.put(info.getKey(), result);
				
				updateFrag(humanObj, info.getKey(), result);
				
				break;
			}else{
				humanObj.rareFragInfo.remove(info.getKey());
				humanObj.fragInfo.remove(info.getKey());
				
				updateFrag(humanObj, info.getKey(), 0);
				
				reduceNum -= info.getValue();
			}
		}	
		
		//返回信息
		onSCFragInfo(humanObj);
	}
	
	/**
	 * 碎片出售
	 * @param humanObj
	 * @param frags
	 */
	public void sellFrag(HumanObject humanObj, List<Integer> frags){
		int coin = 0;
		
		for(int sn : frags){
			if(humanObj.fragInfo.containsKey(sn)){
				ConfItemData cgf = ConfItemData.get(sn);
				if(cgf == null){
					continue;
				}
				coin += cgf.price * humanObj.fragInfo.get(sn);
				
				humanObj.fragInfo.remove(sn);
				
				if(humanObj.rareFragInfo.containsKey(sn)){
					humanObj.rareFragInfo.remove(sn);
				}
			}
		}
		
		//修改JSON数据
		this.removeFrags(humanObj, frags);
		
		//返回信息
		onSCFragInfo(humanObj);
		
		if(coin > 0){
			ProduceMoneyKey cKey = ProduceMoneyKey.coin;
			HumanManager.inst().produceMoneyAdd(humanObj,cKey,coin,MoneyAddLogKey.出售伙伴碎片);
		}
		
		SCSellFrag.Builder msg = SCSellFrag.newBuilder();
		msg.setResult(true);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 添加碎片到    rareFragInfo
	 * @param humanObj
	 * @param fragSn
	 */
	public void copyToRareFrag(HumanObject humanObj, int fragSn){
		if(!humanObj.fragInfo.containsKey(fragSn)){
			return;
		}		
		int num = humanObj.fragInfo.get(fragSn);
		if(!humanObj.rareFragInfo.containsKey(fragSn)){
			humanObj.rareFragInfo.put(fragSn, num);
		}
		
		//碎片信息推送
		onSCFragInfo(humanObj);	
	}
	
	/**
	 * 可用于兑换稀有伙伴的碎片数量
	 * @param humanObj
	 * @return
	 */
	public int getRareFragNum(HumanObject humanObj){
		return humanObj.rareFragInfo.values().stream().mapToInt(n -> n).sum();
	}
	
	/**
	 * 伙伴碎片信息
	 * @param humanObj
	 */
	public void onSCFragInfo(HumanObject humanObj){
		SCFragInfo.Builder msg = SCFragInfo.newBuilder();
		msg.setRareNum(this.getRareFragNum(humanObj));
		
		for(Entry<Integer,Integer> info : humanObj.fragInfo.entrySet()){
			DFragInfo.Builder msg1 = DFragInfo.newBuilder();
			msg1.setSn(info.getKey());
			msg1.setNum(info.getValue());
			
			msg.addInfo(msg1);
		}
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 单个伙伴碎片
	 * @param humanObj
	 */
	public void onSCOneFragInfo(HumanObject humanObj, int fragSn){
		SCOneFragInfo.Builder msg = SCOneFragInfo.newBuilder();
		
		msg.setSn(fragSn);
		if(fragSn == 0){
			msg.setNum(this.getRareFragNum(humanObj));
		}else{
			msg.setNum(humanObj.fragInfo.containsKey(fragSn)?humanObj.fragInfo.get(fragSn):0);
		}
		humanObj.sendMsg(msg);
	}
	
	
}
