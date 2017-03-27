package org.gof.demo.worldsrv.name;

import org.apache.commons.lang3.StringUtils;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfParam;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.SCChangeNameRandomResult;
import org.gof.demo.worldsrv.msg.Msg.SCChangeNameResult;
import org.gof.demo.worldsrv.support.NameFix;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class NameManager extends ManagerBase {

	/**o
	 * 获取实例
	 * 
	 * @return
	 */
	public static NameManager inst() {
		return inst(NameManager.class);
	}

	/**
	 * 改名字， 任务里改名字不花费，其余的要花费
	 * 
	 * @param humanObj
	 * @param name
	 * @param cost
	 */
	public void changeName(HumanObject humanObj, String name) {
		boolean cost = false;
		
		//名字不能为空
		if(StringUtils.isEmpty(name)){
			Inform.user(humanObj.id, Inform.提示操作, "名字不能为空");
			return;
		}
		
		if(name.length() > 7) {
			Inform.user(humanObj.id, Inform.提示操作, "名字不要超过七个字~");
			return;
		}
		
		//花钱
		int costGold = 0;
		if(cost) {
			ConfParam conf = ConfParam.get("changeName");
			costGold = Integer.parseInt(conf.value);
			ReasonResult result = HumanManager.inst().canProduceReduce(humanObj, ProduceMoneyKey.gold, costGold);
			if(!result.success){
				Inform.user(humanObj.id, Inform.提示操作, Utils.createStr("元宝不够{}", costGold) );
				return;
			}
		}
		
		//检查名字是否有屏蔽字，如果有屏蔽字返回
		String fix = NameFix.shield(name);
		if(!StringUtils.isEmpty(fix)) {
			Inform.user(humanObj.id, Inform.提示操作, "臣妾做不到啊%>_<%名字有屏蔽字创建不了啊" );
			
			//返回消息
			SCChangeNameResult.Builder msg = SCChangeNameResult.newBuilder();
			msg.setResult(false);
			msg.setShield(fix);
			humanObj.sendMsg(msg);
			return;
		}
		
		//判断名字是否重复
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.repeat(name);
		prx.listenResult(this::_result_getHumanNameRepeat,"humanObj", humanObj, "name", name, "costGold", costGold);
	}
	public void _result_getHumanNameRepeat(Param results, Param context){
		// 上下文环境
		HumanObject humanObj = context.get("humanObj");
		String name = context.getString("name");
		int costGold = context.getInt("costGold");
				
		// 查询结果
		if(results.getBoolean("repeat")){
			Inform.user(humanObj.id, Inform.提示操作, "名字重复");
			return;
		}
				
		//修改名字
		Human human = humanObj.getHuman();
		String oldName = human.getName();
		human.setName(name);
		
		//同步名字服务
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.change(oldName, name);
		
		//扣钱
		if(costGold > 0){
			HumanManager.inst().produceMoneyReduce(humanObj, ProduceMoneyKey.gold, costGold, MoneyReduceLogKey.改名);
		}
		
		//派发事件
		Event.fire(EventKey.HUMAN_CHANGE_NAME, "humanObj", humanObj);
				
		//返回消息
		SCChangeNameResult.Builder msg = SCChangeNameResult.newBuilder();
		msg.setResult(true);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 随机获取名字
	 * @return
	 */
	public void randomName(HumanObject humanObj) {
		
		NameServiceProxy prx = NameServiceProxy.newInstance();
		prx.randomName();
		prx.listenResult(this::_result_randomNameRepeat,"humanObj", humanObj);
	}
	public void _result_randomNameRepeat(Param results, Param context) {
		// 上下文环境
		HumanObject humanObj = context.get("humanObj");
		
		SCChangeNameRandomResult.Builder msg = SCChangeNameRandomResult.newBuilder();
		msg.setName(results.get("randomName"));
		humanObj.sendMsg(msg);
	}
}