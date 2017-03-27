package org.gof.demo.worldsrv.test;

import java.util.Map.Entry;

import org.gof.core.Port;
import org.gof.core.support.Time;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.MsgReceiver;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.battlesrv.support.PropKey;
import org.gof.demo.seam.msg.MsgParam;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfCharacterGeneral;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.entity.Activity;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.InstanceChapter;
import org.gof.demo.worldsrv.entity.InstanceRank;
import org.gof.demo.worldsrv.general.GeneralManager;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.instance.InstanceManager;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemPack;
import org.gof.demo.worldsrv.mail.MailManager;
import org.gof.demo.worldsrv.msg.Msg.CSInformToAll;
import org.gof.demo.worldsrv.msg.Msg.CSTestAddItem;
import org.gof.demo.worldsrv.msg.Msg.CSTestAddMoney;
import org.gof.demo.worldsrv.msg.Msg.CSTestGiveGeneral;
import org.gof.demo.worldsrv.msg.Msg.CSTestInstanceStar;
import org.gof.demo.worldsrv.msg.Msg.CSTestSendSysMail;
import org.gof.demo.worldsrv.msg.Msg.CSTestUpdateTime;
import org.gof.demo.worldsrv.msg.Msg.CSTestVIP;
import org.gof.demo.worldsrv.msg.Msg.CSTestWhoIsMyDad;
import org.gof.demo.worldsrv.msg.Msg.SCGeneralInfo;
import org.gof.demo.worldsrv.rank.RankGlobalServiceProxy;
import org.gof.demo.worldsrv.rank.RankKey;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TestMsgHandler {
	
	/**
	 * 添加货币
	 * @param param
	 */
	@MsgReceiver(CSTestAddMoney.class)
	public void onCSTestAddMoney(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTestAddMoney msg = param.getMsg();
		
		HumanManager.inst().produceMoneyAdd(humanObj, msg.getSn(), msg.getNum(), MoneyAddLogKey.测试接口);
	}
	
	/**
	 * 添加物品
	 * @param param
	 */
	@MsgReceiver(CSTestAddItem.class)
	public void onCSTestAddItem(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTestAddItem msg = param.getMsg();
//		if (msg.getSn() == -1) {
//			ItemBagManager.inst().add(humanObj, 900001, 99, 0);
//			ItemBagManager.inst().add(humanObj, 330102, 99, 0);
//			for (int i=339001; i<=339020;i++) {
//				ItemBagManager.inst().add(humanObj, i, 99, 0);
//			}
//			return;
//		}
		ItemPack bag = humanObj.dataPers.items;
		ItemBagManager.inst().add(humanObj, msg.getSn(), msg.getNum(), ConfItemData.get(msg.getSn()).bind);
	}

	/**
	 * 添加武将
	 * @param param
	 */
	@MsgReceiver(CSTestGiveGeneral.class)
	public void onCSTestGiveGeneral(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSTestGiveGeneral msg = param.getMsg();
		String sn = msg.getSn();
		ConfCharacterGeneral conf = ConfCharacterGeneral.get(sn);
		if(conf == null) {
			Inform.user(humanObj.id, Inform.提示操作, "武将不存在！");
			return;
		}

		//判断玩家是否拥有这个伙伴
		for(Entry<Long, CharacterObject> entry : humanObj.slaves.entrySet()) {
			GeneralObject generalObj = (GeneralObject)entry.getValue();
			if(generalObj.getGeneral().getSn().equals(sn)) {
				Inform.user(humanObj.getHumanId(), Inform.提示操作, Utils.createStr("您已经有这个伙伴了"));
				return;
			}
		}
		
		GeneralObject genObj = GeneralManager.inst().createAddToHuman(humanObj, Port.applyId(), conf.sn);
		
		long genID = genObj.id;
		
		SCGeneralInfo.Builder msgSend = SCGeneralInfo.newBuilder();
		//获得武将的具体信息
		CharacterObject unitObj = humanObj.slaves.get(genID);
		if(unitObj != null) {
			msgSend.setUnit(GeneralManager.inst().createDGeneralInfo(unitObj));
		}
		
		humanObj.sendMsg(msgSend);
	}
	
	@MsgReceiver(CSTestWhoIsMyDad.class)
	public void onCSTestWhoIsMyDad(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		
		//设置debug属性
		PropCalc propCalc = new PropCalc();
		propCalc.plus(PropKey.atkPhy, 500000);
		propCalc.plus(PropKey.defPhy, 500000);
		propCalc.plus(PropKey.hpMax, 500000);
		
		//获得所有武将
		for (UnitObject uo : humanObj.salvesAttingList) {
			if(uo == null) continue;
			uo.dataPers.unitPropPlus.setDebug(propCalc.toJSONStr());
			UnitManager.inst().propCalc(uo);
		}
		
	}

	/**
	 * 获得所有武将
	 * @param param
	 */
	@MsgReceiver(org.gof.demo.worldsrv.msg.Msg.CSTestGiveAllGeneral.class)
	public void onCSTestGiveAllGeneral(MsgParam param){
	
		HumanObject humanObj=param.getHumanObject();
		for(ConfCharacterGeneral confGeneral:ConfCharacterGeneral.findAll()){
			
			if(confGeneral.canRecriuit)         //是否可招募
			addGeneral(humanObj,confGeneral);
		}	
	}
	
	private void addGeneral(HumanObject humanObj,ConfCharacterGeneral confGeneral){	
		//判断玩家是否拥有这个武将
		for(Entry<Long, CharacterObject> entry : humanObj.slaves.entrySet()) {
			GeneralObject generalObj = (GeneralObject)entry.getValue();
			if(generalObj.getGeneral().getSn().equals(confGeneral.sn)) {
				return;
			}
		}
		
		GeneralObject genObj = GeneralManager.inst().createAddToHuman(humanObj, Port.applyId(), confGeneral.sn);	
		long genID = genObj.id;
		SCGeneralInfo.Builder msgSend = SCGeneralInfo.newBuilder();
		//获得武将的具体信息
		CharacterObject unitObj = humanObj.slaves.get(genID);
		if(unitObj != null) {
			msgSend.setUnit(GeneralManager.inst().createDGeneralInfo(unitObj));
		}
		humanObj.sendMsg(msgSend);	
	}
	
	//发邮件
	@MsgReceiver(CSTestSendSysMail.class)
	public void  onCSTestSendSysMail(MsgParam param){
		HumanObject humanObj = param.getHumanObject();
		CSTestSendSysMail msg = param.getMsg();
		MailManager.inst().sendSysMail(humanObj.getHumanId(), MailManager.SYS_SENDER, msg.getMailSn());
	}
	
	/**
	 * 修改VIP等级
	 * @param param
	 */
	@MsgReceiver(CSTestVIP.class)
	public void onCSTestVIP(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		Human human = humanObj.getHuman();
		CSTestVIP msg = param.getMsg();
		int vip = msg.getVip();
		human.setVipLevel(vip);
		//监听玩家自身属性变化
		HumanInfoChange.listen(humanObj);
	}
	
	/**
	 * 修改副本星星
	 * @param param
	 */
	@MsgReceiver(CSTestInstanceStar.class)
	public void onCSInstanceStar(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		Human human = humanObj.getHuman();
		CSTestInstanceStar msg = param.getMsg();
		int chapterID = msg.getChapterID();
		int instID = msg.getInstID();
		int stars = msg.getStars();
		//修改副本完成进度
		InstanceChapter chapter = humanObj.dataPers.instanceChapters.get(chapterID);
		JSONObject repStarJson = Utils.str2JSONObject(chapter.getRepStarJson());
		JSONArray arr = repStarJson.getJSONArray(String.valueOf(instID));
		for (int i = 0; i < stars; i++) {
			arr.set(i, InstanceManager.INSTANCE_STAR_已完成);
		}
		
		repStarJson.put(String.valueOf(instID), arr);
		chapter.setRepStarJson(Utils.toJSONString(repStarJson));
		InstanceManager.inst().updateChapter(humanObj, chapterID);
		
		// 加入排行榜
		RankGlobalServiceProxy proxy = RankGlobalServiceProxy.newInstance();
		InstanceRank rank = new InstanceRank();
		rank.setId(humanObj.id);
		rank.setHumanId(humanObj.id);
		rank.setCharacterSn(humanObj.getHuman().getSn());
		rank.setHumanLevel(human.getLevel());
		rank.setHumanName(human.getName());
		rank.setRankTime(Port.getTime());
		// 原星星数
		int totalStars = InstanceManager.inst().getAllStar(humanObj);
		rank.setStars(totalStars);
		
		proxy.addNew(RankKey.InstanceRank, rank);
	}
	
	/**
	 * 修改签到、登陆奖励时间
	 * @param param
	 */
	@MsgReceiver(CSTestUpdateTime.class)
	public void onCSUpdateTime(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		Activity activity = humanObj.dataPers.activity;
		String signIn = activity.getSignInJson();
		JSONObject signInJson = Utils.str2JSONObject(signIn);
		signInJson.put("time", signInJson.getIntValue("time")-Time.DAY);
		activity.setSignInJson(Utils.toJSONString(signInJson));
		
		String addLogin = activity.getAddLoginJson();
		JSONObject addLoginJson = Utils.str2JSONObject(addLogin);
		addLoginJson.put("time", addLoginJson.getIntValue("time")-Time.DAY);
		activity.setAddLoginJson(Utils.toJSONString(addLoginJson));
		
		String lineLogin = activity.getSignInJson();
		JSONObject lineLoginJson = Utils.str2JSONObject(lineLogin);
		lineLoginJson.put("time", lineLoginJson.getIntValue("time")-Time.DAY);
		activity.setLineLoginJson(Utils.toJSONString(lineLoginJson));
		
//		System.out.println("签到日期修改为："+DateUtil.formatDate(player.getSignIn().getSignInTime()));
//		System.out.println("开服登陆日期修改为："+DateUtil.formatDate(player.getConLogin().getTime()));
//		System.out.println("累计登陆签到日期修改为："+DateUtil.formatDate(player.getAccLogin().getTime()));
	}
	
	/**
	 * 发系统消息
	 * @param param
	 */
	@MsgReceiver(CSInformToAll.class)
	public void onCSToAll(MsgParam param) {
		HumanObject humanObj = param.getHumanObject();
		CSInformToAll msg = param.getMsg();
		String content = msg.getContent();
		Inform.all(Inform.系统, content, humanObj.id);
	}
}