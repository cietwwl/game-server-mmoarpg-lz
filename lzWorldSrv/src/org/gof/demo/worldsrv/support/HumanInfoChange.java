package org.gof.demo.worldsrv.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.support.PropKey;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.msg.Msg.DHuman;
import org.gof.demo.worldsrv.msg.Msg.DProp;
import org.gof.demo.worldsrv.msg.Msg.SCHumanInfoChange;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;

public class HumanInfoChange {
	//自己按需要添加，但是一定要注意不要弄错了，添加的字段必须是DHuman消息上同名属性
	//human上的属性
	private final static List<String> fieldsHuman = Utils.ofList(
																"level",						//等级
																"hpCur",					//当前生命
																"mpCur",					//当前法力
																"combat",					//当前战斗力
																"actValueMax",			    //活力MAX
																"vipLevel"			        //VIP
															);

	//humanObj上字段字段
	private final static List<String> fieldsHumanObj = Utils.ofList(
																	"canMove",				//能否移动
																	"canCastSkill",			//能否施放技能
																	"canAttack"				//能否动作
																	); 
	//玩家对比数据 较旧的部分
	private Map<String, Object> humanInfoOld;
	
	/**
	 * 构造函数
	 * @param humanObjOld
	 */
	public HumanInfoChange(HumanObject humanObjOld) {
		this.humanInfoOld = getHumanInfo(humanObjOld);
	}
	
	/**
	 * 增加监听信息
	 * @param humanObjOld
	 */
	public static void listen(HumanObject humanObjOld) {
		humanObjOld.humanInfoChangeListen();
	}
	
	/**
	 * humanInfoChange监听一次的结果
	 * @param humanObjNew
	 */
	public void resultForListen(HumanObject humanObjNew) {
		Map<String, Object> humanInfoNew = getHumanInfo(humanObjNew);
		Map<String, Object> humanInfoChange = getHumanInfoChange(humanInfoNew);
		
		//如果有改变 就需要向客户端发送改变消息
		if(!humanInfoChange.isEmpty()) {
			SCHumanInfoChange msg = createMsg(humanInfoChange, humanObjNew);
			humanObjNew.sendMsg(msg);
			
		}
		
		//在本次将最新的humanInfo保存下来，下一次检测就可以直接用不用再重新获取
		humanInfoOld = humanInfoNew;
	}

	/**
	 * 获取人物身上所有需要的信息
	 * @param humanObj
	 * @return
	 */
	private Map<String, Object> getHumanInfo(HumanObject humanObj) {
		Map<String, Object> result = new HashMap<>();
		
		//组织所有需要的字段的值
		/*
		 * human相关字段
		 */
		List<String> fieldsHumanAll = new ArrayList<>();
		fieldsHumanAll.addAll(fieldsHuman);
		fieldsHumanAll.addAll(ProduceMoneyKey.toList());
		fieldsHumanAll.addAll(PropKey.toList());
		
		Human human = humanObj.getHuman();
		for(String key : fieldsHumanAll) {
			Object value = Utils.fieldRead(human, key);
			if(value == null) continue;
			result.put(key, value);
		}
		
		//其他特殊字段，不能在Human身上直接取到的，需要每次都计算的
		/*
		 * humanObj上的字段
		 */
		for(String key : fieldsHumanObj) {
			Object value = Utils.fieldRead(humanObj, key);
			if(value == null) continue;
			result.put(key, value);
		}
		
		
		return result;
	}
	
	/**
	 * 获取人物属性变化，返回变化字段的最终值
	 * @param human
	 * @return
	 */
	private Map<String, Object> getHumanInfoChange(Map<String, Object> humanInfoNew) {
		Map<String, Object> result = new HashMap<>();
		for(Entry<String, Object> entry : humanInfoOld.entrySet()) {
			String key = entry.getKey();
			
			Object valueOld = entry.getValue();
			Object valueNew = humanInfoNew.get(key);
			
			//如果两个字段的字符串值相同，那么不管是String还是int还是double还是long，那值应该都相同，这个值就不用管
			if(valueOld.equals(valueNew)) continue;
			
			result.put(key, valueNew);
		}
		
		return result;
	}
	
	/**
	 * 构建Human Info 变化信息，如果没有变化，那就返回null
	 * @param humanInfoChange
	 * @return
	 */
	private SCHumanInfoChange createMsg(Map<String, Object> humanInfoChange, HumanObject humanObjNew) {
		//没有改动 返回null
		if(humanInfoChange.isEmpty()) return null;
		
		//返回全部
		humanInfoChange = getHumanInfo(humanObjNew);
		
		//返回消息
		SCHumanInfoChange.Builder msgResult = SCHumanInfoChange.newBuilder();
		DHuman.Builder dHuman = DHuman.newBuilder();
		
		//prop属性
		Map<String, Object> prop = new HashMap<>();
		for(Entry<String, Object> entry : humanInfoChange.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if(PropKey.contains(key)) {
				prop.put(key, value);
				
			} else {
				Utils.fieldWrite(dHuman, key, value);
			}
		}
		
		dHuman.setProp(createMsgDProp(prop));
		msgResult.setHuman(dHuman);
		
		return msgResult.build();
	}
	
	/**
	 * 构造属性消息包
	 * @param prop
	 * @return
	 */
	private DProp createMsgDProp(Map<String, Object> prop) {
		DProp.Builder dProp = DProp.newBuilder();
		for(Entry<String, Object> entry : prop.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			Utils.fieldWrite(dProp, key, value);
		}
		return dProp.build();
	}
}
