package org.gof.demo.worldsrv.dailyliveness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfDailyLiveness;
import org.gof.demo.worldsrv.config.ConfDailyLivenessAwards;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemVO;
import org.gof.demo.worldsrv.msg.Msg.SCLivenessInfo;
import org.gof.demo.worldsrv.msg.Msg.SCRsvLivenessAwardsResult;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONArray;
/**
 *  打开每日活跃度面板刷新，每次完成相应的活动，触发更新
 */
public class LivenessManager extends ManagerBase {
	private static final String JSON_EMPT_STR = "[]";
	private static final int LIVENESS_STATUS_TIME_LIMIT = 3;	//未到开放时间
	private static final int LIVENESS_STATUS_PASS = 2;	//已完成
	private static final int LIVENESS_STATUS_RUN = 1;	//进行中
	private static final int LIVENESS_STATUS_INIT = 0;	//未完成
	
	private static final int LIVENESS_AWARD_RSVD = 1;	//奖励已领
	private static final int LIVENESS_AWARD_NOT_RSV = 0;	//奖励未领
	
	/**
	 * 获取实例
	 * @return
	 */
	public static LivenessManager inst() {
		return inst(LivenessManager.class);
	}
	
	/**
	 * 初始化
	 */
	public void initAllLiveness(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		/* 活跃度配置 */
		List<ConfDailyLiveness> cfList = new ArrayList<ConfDailyLiveness>();
		cfList.addAll(ConfDailyLiveness.findAll());
		/* 角色身上的活跃度信息 */
		String voJsn = human.getLiveness();
		List<LivenessVO> oldVoList = LivenessVO.jsonToList(voJsn);
		List<LivenessVO> newVOList = new ArrayList<LivenessVO>();
		/* 构建新的LiveVOList */
		for (ConfDailyLiveness cf : cfList) {
			LivenessVO vo = initLiveness(humanObj, cf);
			if(vo != null){
				newVOList.add(vo);
			}
		}
		oldVoList.addAll(newVOList);
		human.setLiveness(LivenessVO.listToJson(oldVoList));
	}
	
	private LivenessVO initLiveness(HumanObject humanObj, ConfDailyLiveness cf) {
		switch (cf.type) {
		case LivenessTypeKey.DAILY_QUEST_TYPE:
			return initDailyQuestLiveness(humanObj, cf);
		case LivenessTypeKey.MALL:
			return initMAllLiveness(humanObj, cf);
		default:
			return null;
		}
		
	}
	
	private LivenessVO initMAllLiveness(HumanObject humanObj,
			ConfDailyLiveness cf) {
		Human human = humanObj.getHuman();
		List<LivenessVO> voList = LivenessVO.jsonToList(human.getLiveness());
		// 检查是否活跃度活动已在角色身上
		Iterator<LivenessVO> iter = voList.iterator();
		while(iter.hasNext()) {
			LivenessVO vo = iter.next();
			if (cf.sn == vo.sn) {
				return null;
			}
		}
		LivenessVO vo = new LivenessVO();
		vo.sn = cf.sn;
		vo.type = cf.type;
		vo.status = LIVENESS_STATUS_INIT;
		return vo;
	}

	private LivenessVO initDailyQuestLiveness(HumanObject humanObj,
			ConfDailyLiveness cf) {
		Human human = humanObj.getHuman();
		List<LivenessVO> voList = LivenessVO.jsonToList(human.getLiveness());
		// 检查是否活跃度活动已在角色身上
		Iterator<LivenessVO> iter = voList.iterator();
		while(iter.hasNext()) {
			LivenessVO vo = iter.next();
			if (cf.sn == vo.sn) {
				return null;
			}
		}
		LivenessVO vo = new LivenessVO();
		vo.sn = cf.sn;
		vo.type = cf.type;
		vo.status = LIVENESS_STATUS_INIT;
		return vo;
	}

	/**
	 * 领取活跃度奖励
	 * @param humanObj
	 */
	public void rsvLivenessAwards(HumanObject humanObj, int sn) {
		Human human = humanObj.getHuman();
		ConfDailyLivenessAwards cf = ConfDailyLivenessAwards.get(sn);
		if(cf == null){
			Log.liveness.info("{}领取活跃度奖励{}参数错误!",humanObj.name, sn);
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("item.error.numerror"));//todo
		}
		JSONArray awardsRsvd = Utils.toJSONArray(human.getLivenessAwardsRsvd());
		if(awardsRsvd.contains(sn)){
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("item.error.rsvd"));//todo
			return;
		}
		int num = getLiveness(humanObj);
		int tarNum = cf.tarLiveness;
		if(num < tarNum){
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("item.error.numerror"));//todo
			return;
		}
		//奖励发送成功则修改领取记录,记录的都是SN
		if(addAward(humanObj, sn)){
			awardsRsvd.add(sn);
			human.setLivenessAwardsRsvd(awardsRsvd.toJSONString());
			SCRsvLivenessAwardsResult.Builder msg = SCRsvLivenessAwardsResult.newBuilder();
			msg.setCode(sn);
			humanObj.sendMsg(msg);
		}
		
	}
	private int getLiveness(HumanObject humanObj){
		Human human = humanObj.getHuman();
		int num = 0;
		List<LivenessVO> list = LivenessVO.jsonToList(human.getLiveness());
		for (LivenessVO vo : list) {
			ConfDailyLiveness cf = ConfDailyLiveness.get(vo.sn);
			if(cf == null)continue;
			num += cf.liveness;
		}
		return num;
	}
	/**
	 * 发送奖励，背包满了不发送
	 * @param humanObj
	 * @param livenessId
	 */
	private boolean addAward(HumanObject humanObj, int sn){
		ConfDailyLivenessAwards cf = ConfDailyLivenessAwards.get(sn);
		if(cf == null){
			return false;
		}
		List<ItemVO> itemVOs = new ArrayList<ItemVO>();
		int[] awards = cf.awardIds;
		int[] nums = cf.awardNums;
		for (int i = 0; i < awards.length; i++) {
			int itemId = awards[i];
			int num = nums[i];
			ItemVO itemVO = new ItemVO(itemId, num);
			itemVOs.add(itemVO);
		}
		// 如果背包空间不足
		ReasonResult result = ItemBagManager.inst().canAdd(humanObj, itemVOs);
		if (!result.success) {
			Log.liveness.info("{}领取活跃度奖励{}背包空间不足!",humanObj.name, sn);
			Inform.user(humanObj.id, Inform.提示操作, I18n.get("item.error.numNotEnough"));
			return false;
		}
		// 进背包
		ItemBagManager.inst().add(humanObj, itemVOs);
		return true;
	}

	/**
	 * 向前端发送一个活跃度活动信息
	 * @param humanObj
	 * @param human
	 */
	public void sendMsg(HumanObject humanObj, LivenessVO vo) {
		SCLivenessInfo.Builder msg = SCLivenessInfo.newBuilder();
		//推送前端消息体
		msg.addLiveness(vo.createMsg());
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 向前端发送全部活跃度信息
	 * @param humanObj
	 * @param human
	 */
	public void sendMsg(HumanObject humanObj, List<LivenessVO> voList, List<Integer> awards) {
		SCLivenessInfo.Builder msg = SCLivenessInfo.newBuilder();
		//推送前端消息体
		for (LivenessVO vo : voList) {
			msg.addLiveness(vo.createMsg());
		}
		msg.addAllAwardsRsvd(awards);
		humanObj.sendMsg(msg);
	}

	/**
	 * 每次打开界面初始化
	 * @param humanObj
	 */
	public  void openLivenessUI(HumanObject humanObj) {
		Human human = humanObj.getHuman();
		/* 活跃度配置 */
		List<ConfDailyLiveness> cfList = new ArrayList<ConfDailyLiveness>();
		cfList.addAll(ConfDailyLiveness.findAll());
		/* 角色身上的活跃度信息 */
		String voJsn = human.getLiveness();
		List<LivenessVO> oldVoList = LivenessVO.jsonToList(voJsn);
		List<LivenessVO> newVOList = new ArrayList<LivenessVO>();
		/* 构建新的LiveVOList */
		for (ConfDailyLiveness cf : cfList) {
			LivenessVO vo = initLiveness(humanObj, cf);
			if(vo != null){
				newVOList.add(vo);
			}
		}
		oldVoList.addAll(newVOList);
		human.setLiveness(LivenessVO.listToJson(oldVoList));
		/* 活跃度奖励 */
		String rsvJsn = human.getLivenessAwardsRsvd();
		JSONArray awardsRsvd = Utils.toJSONArray(rsvJsn);
		List<Integer> as = new ArrayList<Integer>();
		for(ConfDailyLivenessAwards cf	: ConfDailyLivenessAwards.findAll()){
			as.add(cf.sn);
		} 
		/* 整理客户端	*/
		Collections.sort(as);
		for(int i = 0; i < as.size(); i++){
			if(awardsRsvd.contains(as.get(i))){
				as.set(i, LIVENESS_AWARD_RSVD);//l
				continue;
			}
			as.set(i, LIVENESS_AWARD_NOT_RSV);//0
		}
		sendMsg(humanObj, oldVoList, as);	
	}
	@Listener(EventKey.DAILY_QUEST_PASS)
	public void _listener_DAILY_QUEST_PASS(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		
		List<LivenessVO> voList = LivenessVO.jsonToList(human.getLiveness());
		for(LivenessVO vo : voList) {
			if (vo.type != LivenessTypeKey.DAILY_QUEST_TYPE) continue;
			if (vo.status == LIVENESS_STATUS_PASS)continue;
			ConfDailyLiveness cf = ConfDailyLiveness.get(vo.sn);
			vo.currNum ++;
			if (vo.currNum >= cf.tarNum) {
				vo.status = LIVENESS_STATUS_PASS;
			} else {
				vo.status = LIVENESS_STATUS_RUN;
			}
			sendMsg(humanObject, vo);
		}
		human.setLiveness(LivenessVO.listToJson(voList));
	}
	@Listener(EventKey.MALL_BUG_GOODS_BY_GOLD)
	public void _listener_MALL_BUG_GOODS_BY_GOLD(Param param) {
		HumanObject humanObject = param.get("humanObj");
		Human human = humanObject.getHuman();
		Integer num = param.get("num");
		List<LivenessVO> voList = LivenessVO.jsonToList(human.getLiveness());
		for(LivenessVO vo : voList) {
			if (vo.type != LivenessTypeKey.MALL) continue;
			if (vo.status == LIVENESS_STATUS_PASS)continue;
			ConfDailyLiveness cf = ConfDailyLiveness.get(vo.sn);
			vo.currNum += num;
			if (vo.currNum >= cf.tarNum) {
				vo.status = LIVENESS_STATUS_PASS;
				vo.currNum = cf.tarNum;
			} else {
				vo.status = LIVENESS_STATUS_RUN;
			}
			sendMsg(humanObject, vo);
		}
		human.setLiveness(LivenessVO.listToJson(voList));
	}
	/* 更新身上的活跃度 */
	@Listener({EventKey.UPDATE_LIVENESS})
	public void updateLiveness(Param param){
		HumanObject humanObj = param.get("humanObj");
		initAllLiveness(humanObj);
	}
	/* 定时清空操作 */
	@Listener({EventKey.HUMAN_RESET_ZERO})
	public void resetLiveness(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();
		human.setLiveness(JSON_EMPT_STR);
		human.setLivenessAwardsRsvd(JSON_EMPT_STR);
	}
}