package org.gof.demo.worldsrv.activity;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfQuest;
import org.gof.demo.worldsrv.config.ConfSignIn;
import org.gof.demo.worldsrv.entity.Activity;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.msg.Msg.DSignIn;
import org.gof.demo.worldsrv.msg.Msg.SCSignIn;
import org.gof.demo.worldsrv.quest.QuestNormalManager;
import org.gof.demo.worldsrv.quest.QuestTypeKey;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONObject;


public class ActivityManager extends ManagerBase {
	
	/**签到领取*/
	public static final int SIGNIN_未领取 = 0;					//未领取
	public static final int SIGNIN_基础领取 = 1;					//基础领取
	public static final int SIGNIN_VIP领取 = 2;					//VIP领取
	
	/**
	 * 获取实例
	 * @return
	 */
	public static ActivityManager inst() {
		return inst(ActivityManager.class);
	}
	
	/**
	 * 首次登陆初始化
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_FIRST_LOGIN)
	public void initActivity(Param param) {
		HumanObject humanObj = param.get("humanObj");
		if (humanObj.dataPers.activity == null) {
			Activity activity = new Activity();
			activity.setId(Port.applyId());
			activity.setHumanId(humanObj.getHumanId());
			Map<String, Integer> signInMap = new HashMap<>();
			signInMap.put("count", 0);
			signInMap.put("time", 0);
			signInMap.put("state", SIGNIN_未领取);
			activity.setSignInJson(Utils.toJSONString(signInMap));
			Map<String, Integer> lineLoginMap = new HashMap<>();
			lineLoginMap.put("days", 0);
			lineLoginMap.put("time", 0);
			activity.setLineLoginJson(Utils.toJSONString(lineLoginMap));
			Map<String, Integer> addLoginMap = new HashMap<>();
			addLoginMap.put("days", 0);
			addLoginMap.put("time", 0);
			activity.setAddLoginJson(Utils.toJSONString(addLoginMap));
			activity.persist();
			humanObj.dataPers.activity = activity;
		}
	}
	
	/**
	 * 首次登陆重置状态
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_RESET_ZERO)
	public void resetSignInState(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Activity activity = humanObj.dataPers.activity;
		String signIn = activity.getSignInJson();
		JSONObject signInJson = Utils.str2JSONObject(signIn);
		signInJson.put("state", SIGNIN_未领取);
		activity.setSignInJson(Utils.toJSONString(signInJson));
	}
	
	/**
	 * 检查累计登陆
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_RESET_ZERO)
	public void updateLineLoginDays(Param param) {
		HumanObject humanObj = param.get("humanObj");
		long now = System.currentTimeMillis();
		Activity activity = humanObj.dataPers.activity;
		JSONObject lineLoginJson = Utils.str2JSONObject(activity.getLineLoginJson());
		
		// 如果等于7天需要清零
		int days = lineLoginJson.getIntValue("days");
		int time = lineLoginJson.getIntValue("time");
		if (days >= 7) {
			lineLoginJson.put("days", 0);
			activity.setAddLoginJson(Utils.toJSONString(lineLoginJson));
			QuestNormalManager.inst().removeCompletedByType(humanObj, QuestTypeKey.QUEST_TYPE_10);
		}
		
		if (!Utils.isSameDay(time, now)) {
			Event.fire(EventKey.LOGIN_AWARD, "humanObject", humanObj, "days", days++);
			lineLoginJson.put("time", now);
			lineLoginJson.put("days", days++);
			activity.setLineLoginJson(Utils.toJSONString(lineLoginJson));
		}
	}
	
	
	/**
	 * 检查连续登陆
	 * @param humanObj
	 * @param msg
	 */
	@Listener(EventKey.HUMAN_RESET_ZERO)
	public void updateAddLoginDays(Param param) {
		HumanObject humanObj = param.get("humanObj");
		long now = System.currentTimeMillis();
		Activity activity = humanObj.dataPers.activity;
		JSONObject addLoginJson = Utils.str2JSONObject(activity.getAddLoginJson());
		
		// 如果等于7天需要清零
		int time = addLoginJson.getIntValue("time");
		int days = addLoginJson.getIntValue("days");
		if (days >= 7  || Utils.getDaysBetween(time, now) > 1) {
			addLoginJson.put("days", 0);
			activity.setAddLoginJson(Utils.toJSONString(addLoginJson));
			// 删除已完成的ID
			QuestNormalManager.inst().removeCompletedByType(humanObj, QuestTypeKey.QUEST_TYPE_11);
		}
		
		
		if (!Utils.isSameDay(time, now)) {
			Event.fire(EventKey.LOGIN_AWARD, "humanObject", humanObj, "days", days++);
			addLoginJson.put("time", now);
			addLoginJson.put("days", days++);
			activity.setAddLoginJson(Utils.toJSONString(addLoginJson));
		}
	}
	
	/**
	 * 获取签到信息
	 * @param humanObj
	 * @param msg
	 */
	public DSignIn.Builder getSignInInfo(HumanObject humanObj) {
		Activity activity = humanObj.dataPers.activity;
		String signIn = activity.getSignInJson();
		JSONObject signInJson = Utils.str2JSONObject(signIn);
		DSignIn.Builder dSignIn = DSignIn.newBuilder();
		String date = new SimpleDateFormat("yyyyMM").format(System.currentTimeMillis());
		dSignIn.setMonth(Integer.valueOf(date));
		// 这里是登录天数！！而不是签到次数！！！
		if (Utils.isSameDay(signInJson.getIntValue("time"), System.currentTimeMillis())) {
			dSignIn.setDays(signInJson.getIntValue("count")-1);
		} else {
			dSignIn.setDays(signInJson.getIntValue("count"));
		}
		
		dSignIn.setState(signInJson.getIntValue("state"));
		return dSignIn;
	}
	
	/**
	 * 签到
	 * @param humanObj
	 * @param msg
	 */
	public void signIn(HumanObject humanObj) {
		Activity activity = humanObj.dataPers.activity;
		String signIn = activity.getSignInJson();
		JSONObject signInJson = Utils.str2JSONObject(signIn);
		int signInTime = signInJson.getIntValue("time");
		int state = signInJson.getIntValue("state");
		int count = signInJson.getIntValue("count");
		long now = System.currentTimeMillis();
		// 如果不是同一天或者是同一天只领取了基础奖励
		if ((!Utils.isSameDay(signInTime, now) && now > signInTime) || 
				(Utils.isSameDay(signInTime, now) && (state == SIGNIN_基础领取))) {
			signInJson.put("time", now);
			if (state == SIGNIN_未领取) {
				signInJson.put("count", ++count);
				signInJson.put("state", SIGNIN_基础领取);
			} else if (state == SIGNIN_基础领取) {
				signInJson.put("state", SIGNIN_VIP领取);
			}
			activity.setSignInJson(Utils.toJSONString(signInJson));
			// 增加奖励
			String date = new SimpleDateFormat("yyyyMM").format(now);
			ConfSignIn confSignIn = ConfSignIn.get(Integer.valueOf(date));
			int[] awards = confSignIn.award;
			// 如果当前vip等级大于指定vip等级可以领取双倍奖励
			int itemSn = awards[count*3];
			int num = awards[count*3+1];
			int vip = awards[count*3+2];
			if (vip != -1 && humanObj.getHuman().getVipLevel() >= vip) {
				num *= 2;
			}
			// 如果背包空间不足
			ReasonResult result = ItemBagManager.inst().canAdd(humanObj, itemSn, num, 0);
			if (!result.success) {
				Log.game.info("{}添加奖励背包不足!",humanObj.name);
				Inform.user(humanObj.id, Inform.提示操作, I18n.get("item.error.numNotEnough"));
				return;
			}
			// 进背包
			ItemBagManager.inst().add(humanObj, itemSn, num, 0);
			SCSignIn.Builder msg = SCSignIn.newBuilder();
			msg.setState(signInJson.getIntValue("state"));
			humanObj.sendMsg(msg);
		} else {
			Log.game.info("{}签到不符合要求!",humanObj.name);
			return;
		}
	}
}
