package org.gof.demo.worldsrv.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.HumanObjectServiceProxy;
import org.gof.demo.worldsrv.config.ConfGolbal;
import org.gof.demo.worldsrv.config.ConfMail;
import org.gof.demo.worldsrv.entity.Mail;
import org.gof.demo.worldsrv.general.GeneralPlusManager;
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import org.gof.demo.worldsrv.human.HumanGlobalServiceProxy;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.item.ItemBagManager;
import org.gof.demo.worldsrv.item.ItemVO;
import org.gof.demo.worldsrv.msg.Msg.DItem;
import org.gof.demo.worldsrv.msg.Msg.DMail;
import org.gof.demo.worldsrv.msg.Msg.DVirtual;
import org.gof.demo.worldsrv.msg.Msg.SCAddMail;
import org.gof.demo.worldsrv.msg.Msg.SCCheckMailRemoveable;
import org.gof.demo.worldsrv.msg.Msg.SCDeleteMail;
import org.gof.demo.worldsrv.msg.Msg.SCPickupAllMail;
import org.gof.demo.worldsrv.msg.Msg.SCPickupMail;
import org.gof.demo.worldsrv.pocketLine.PocketLineKey;
import org.gof.demo.worldsrv.pocketLine.PocketLineServiceProxy;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.AttachmentKey;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;


public class MailManager extends ManagerBase {
	

	public static final long SYS_SENDER = 1;      //系统发送
//	public static final long MAX_MAIL_NUM = 100;						//最大邮件数量
	
	public static final long MAIL_EXPIRE_WITH_ATTCHMENT = 5*24*3600*1000;   //带有附件奖励的邮件有效期
	public static final long MAIL_EXPIRE_WITHOUT_ATTCHMENT = 3*24*3600*1000;   //无附件奖励的邮件有效期
	
	
	/**
	 * 获取实例
	 * @return
	 */
	public static MailManager inst() {
		return inst(MailManager.class);
	}
	
	/**
	 * 初始化玩家所有的邮件
	 * @param humanObj
	 */
	public void initMailList(HumanObject humanObj) {
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findBy(true, Mail.tableName, Mail.K.humanId, humanObj.getHumanId());
		prx.listenResult(this::_result_loadMailFirst,  "humanObj", humanObj);
	}
	
	public void _result_loadMailFirst(Param results, Param context) {
		//玩家
		HumanObject humanObj = context.get("humanObj");
		
		//结果
		List<Record> records = results.get();
		List<Mail> mails = new ArrayList<Mail>();
		
		for (Record re : records) {
			Mail mail = new Mail(re);	
			if(Port.getTime() > mail.getEtime()){  //过期则删除
				mail.remove();
			}else{
			    mails.add(mail);
			}
		}
		//排序
		Collections.sort(mails, (m1, m2) -> (int)(m1.getRtime() - m2.getRtime()));
		
		//往Human身上设置mail信息
		humanObj.mailList = mails;

		//构建返回信息
//		SCMailList.Builder msg = SCMailList.newBuilder();
//		for(Mail m : mails){
//			msg.addMail(this.builtDmailMsg(m));
//		}
//		humanObj.sendMsg(msg);
	}
	
	/**
	 * 给一封邮件设置已读
	 * @param humanObj
	 * @param mailId
	 */
	public void readMail(HumanObject humanObj, long mailId) {
		for (Mail mail : humanObj.mailList) {
			if(mail.getId() == mailId){
				mail.setRead(true);
				break;
			}
		}
	}

    /**
     * 游戏内发送邮件给玩家
     * @param humanId     接收者
     * @param sender      发送者
     * @param mailConfId  邮件配置ID
     */
	public void sendSysMail(long humanId,long sender,int mailConfId){
		//根据邮件ID获取邮件配置
		ConfMail conf = ConfMail.get(mailConfId);	
		//发送邮件
		sendMail(humanId,sender,conf.title,conf.content,conf.type,conf.oid,conf.num);
	}
	
	/**
	 * 发送邮件给玩家
	 * @param humanId
	 * @param sender
	 * @param title
	 * @param content
	 * @param type
	 * @param oid
	 * @param num
	 */
	public void sendMail(long humanId, long sender, String title, String content, int[] type, int[] oid,int[] num){
		//检查配置
		if(num.length != type.length ||num.length != oid.length || type.length != oid.length){
			throw new SysException("Mail.xlxs 附件配置错误！");
		}
		
		//持久化邮件
		Mail mail = new Mail();
		mail.setId(Port.applyId());
		mail.setTitle(title);
		mail.setContent(content);
		mail.setRtime(Port.getTime());
		mail.setRead(false);
		mail.setHumanId(humanId);
		mail.setSender(sender);
		
		if(num.length > 0 && type.length > 0 && oid.length >0){			
		    mail.setEtime(mail.getRtime() + MAIL_EXPIRE_WITH_ATTCHMENT);
		    
		    mail.setItem(itemToJSON(type, oid, num));
		}else{
			mail.setEtime(mail.getRtime() + MAIL_EXPIRE_WITHOUT_ATTCHMENT);
		}
		
		mail.persist();
		
		//通知给玩家
		HumanGlobalServiceProxy prx = HumanGlobalServiceProxy.newInstance();
		prx.getInfo(humanId);
		prx.listenResult(this::_result_remindHumanTo, "mail", mail, "humanId", humanId);		
	}
	
	public void _result_remindHumanTo(Param results, Param context) {
		long humanId = context.get("humanId");
		Mail mail = context.get("mail");
		HumanGlobalInfo humanToInfo = results.get();
		
		//若玩家不在线，则离线接收
		if(humanToInfo == null) {
			
			//邮件提醒一条待办
			PocketLineServiceProxy pocketPxy = PocketLineServiceProxy.newInstance();
			pocketPxy.add(humanId, PocketLineKey.MAIL_REMIND, null);
		} else{
			
			//接收者接收邮件
			HumanObjectServiceProxy humanPrx = HumanObjectServiceProxy.newInstance(humanToInfo.nodeId, humanToInfo.portId, humanId);
			humanPrx.mailAccept(mail);
		}
	}	
	
	/**
	 * 将附件转化成JSON
	 * @param type
	 * @param oid
	 * @param num
	 * @return
	 */
	public static String itemToJSON(int[] type, int[] oid, int[] num){
		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>(); 
		for(int i=0;i<type.length;i++){
			String mainKey = String.valueOf(type[i]);
			if(map.containsKey(mainKey)){
				map.get(mainKey).put(String.valueOf(oid[i]), num[i]);
			}else{
				Map<String, Integer> subMap = new HashMap<String, Integer>(); 
				subMap.put(String.valueOf(oid[i]), num[i]);				
			    map.put(mainKey, subMap);
			}
		}
		return Utils.toJSONString(map);
	}
	
	/**
	 * JSON转换成item MAP
	 * @param item
	 * @return
	 */
	public static Map<Integer, Map<Integer, Integer>> JSONtoItemMap(String item){
		//JSON转换成java对象
		Map<Integer, Map<Integer, Integer>> rMap = new HashMap<Integer, Map<Integer, Integer>>();
		
		Object cObj = Utils.toJSONObject(item);
		@SuppressWarnings("unchecked")
		Map<String, Map<String, Integer>> map = (Map<String, Map<String, Integer>>)cObj; 
		
		//key类型变换
		for(String key : map.keySet()){
			Map<Integer, Integer> subRMap = new HashMap<Integer, Integer>();
			Map<String, Integer> subMap = map.get(key);
			for(Entry<String, Integer> e : subMap.entrySet()){
				subRMap.put(Integer.valueOf(e.getKey()), e.getValue());
			}
			rMap.put(Integer.valueOf(key), subRMap);
		}		
		return rMap;
	}
	
	/**
	 * 在线接收邮件后续操作
	 * @param humanObj
	 */
	public void addNew(HumanObject humanObj, Mail mail) {
		//放到玩家身上第一个
		humanObj.mailList.add(0,mail);
	
		//给前端发消息
		SCAddMail.Builder msg = SCAddMail.newBuilder();
		msg.setMail(this.builtDmailMsg(mail));
		humanObj.sendMsg(msg);
		
		//如果超过100封做删除处理
		int numTotal = humanObj.mailList.size();
		if(numTotal > ConfGlobalUtils.getValue(ConfGlobalKey.邮箱存储上限)) {
			
			//遍历后面的
			List<Mail> toRemove = new ArrayList<Mail>();
			for (int i = 99; i < numTotal; i++) {
				toRemove.add(humanObj.mailList.get(i));
				humanObj.mailList.remove(i);
			}
			
			//删除
			List<Long> mailIds = new ArrayList<>();
			for (Mail delMail : toRemove) {
				mailIds.add(delMail.getId());
				delMail.remove();
			}
			
			//返回删除信息
			SCDeleteMail.Builder delMsg = SCDeleteMail.newBuilder();
			for(int i = 0; i < mailIds.size(); i++){
				delMsg.addIds(mailIds.get(i));
			}			
			humanObj.sendMsg(delMsg);	
		}
	}	


	/**
	 * 获取邮件中的附件（货币及物品）
	 * @param humanObj
	 * @param mailId
	 */
	public void pickupMailAttachment(HumanObject humanObj, long mailId) {
		Mail mail = null;
		
		//查找当前的邮件
		for (Mail m : humanObj.mailList) {
			if(m.getId() == mailId){
				mail = m;
				break;
			}
		}
		
		//没找到
		if(mail == null){
			pickupMailResult(humanObj, 1, "该邮件不存在!");
			Inform.user(humanObj.getHumanId(), Inform.提示操作,"该邮件不存在!");
			return;
		}

		//该邮件无附属货币或物品
		if(mail.getItem().equals("{}")){
			pickupMailResult(humanObj, 2, "该邮件无附件!");
			Inform.user(humanObj.getHumanId(), Inform.提示操作,"该邮件无附件!");
			return;
		}
		
		//过期验证
		if(Port.getTime() > mail.getEtime()){
			//设置邮件状态 直接删除
			humanObj.mailList.remove(mail);
			mail.remove();	
			
			pickupMailResult(humanObj, 3, "该邮件已过期!");
			Inform.user(humanObj.getHumanId(), Inform.提示操作,"该邮件已过期!");
			return;			
		}

		//JSON转换成java对象
		Map<Integer, Map<Integer, Integer>> map = JSONtoItemMap(mail.getItem());
		
		//判断物品是否可以添加到背包里   若可以则直接加入背包
		Map<Integer, Integer> iMap = map.get(AttachmentKey.item.getType());
		if(iMap != null){
			boolean itemAddState = itemAdd(humanObj, iMap);
			if(!itemAddState){
				pickupMailResult(humanObj, 4, "背包已满!");
				Inform.user(humanObj.getHumanId(), Inform.提示操作,"背包已满!");			
				return;
			}
		}

        //获取货币附件		
		Map<Integer, Integer> cMap = map.get(AttachmentKey.money.getType());	
		moneyAdd(humanObj,cMap);
		
		//设置邮件状态 直接删除
		humanObj.mailList.remove(mail);
		mail.remove();	
		
		//提示成功提取
		pickupMailResult(humanObj, 0, "提取成功!");
		Inform.user(humanObj.getHumanId(), Inform.提示操作,"附件提取成功!");		
	}
	
	/**
	 * 提取邮件提示
	 * @param humanObj
	 * @param code
	 * @param reason
	 */
	public void pickupMailResult(HumanObject humanObj, int code, String reason){
    	SCPickupMail.Builder msg = SCPickupMail.newBuilder();	
        msg.setCode(code);
        if(!reason.equals("")){
            msg.setReason(reason);
        }
		humanObj.sendMsg(msg);			
	}

	/**
	 * 为human添加货币
	 * @param humanObj
	 * @param cMap
	 */
	public void moneyAdd(HumanObject humanObj, Map<Integer, Integer> cMap){
		if(cMap == null){
			return;
		}
		for (Entry<Integer, Integer> e : cMap.entrySet()) {
			ProduceMoneyKey cKey = ProduceMoneyKey.getEnumByType(e.getKey());
			//伙伴经验特殊处理
			if(cKey == ProduceMoneyKey.genExpCur){
				GeneralPlusManager.inst().addAttingGeneralExp(humanObj, e.getValue());		
			}else{
			    HumanManager.inst().produceMoneyAdd(humanObj,cKey,e.getValue(),MoneyAddLogKey.获取邮件附件);
			}
		}		
	}
	
	/**
	 * 提取邮件物品并入包
	 * @param humanObj
	 * @param subMap
	 * @return
	 */
	public boolean itemAdd(HumanObject humanObj, Map<Integer, Integer> subMap){
		List<ItemVO> itemVOs = new ArrayList<ItemVO>();
		for(Entry<Integer, Integer> e : subMap.entrySet()){
			ItemVO itemVO = new ItemVO(e.getKey(),e.getValue());
			itemVOs.add(itemVO);
		}
		
		ReasonResult canAdd = ItemBagManager.inst().canAdd(humanObj, itemVOs);
		if(!canAdd.success) {
			return false;
		}
		
		//添加到玩家的背包里
		ItemBagManager.inst().add(humanObj, itemVOs);
		
		return true;
	}
	
	/**
	 * 获取所有邮件中的附件
	 * @param humanObj
	 */
	public void pickupAllMailAttachment(HumanObject humanObj) {
		//玩家邮件列表
	    List<Mail> mails = humanObj.mailList;
	    
	    List<Long> canRemoveMailIds = new ArrayList<>();           //取附件后可以被删除的邮件id
	    Map<Integer,Integer> cMap = new HashMap<Integer,Integer>();   //money类附件统计
	    Map<Integer,Integer> iMap = new HashMap<Integer,Integer>();   //item类附件统计
		
		for (Mail mail : mails) {
			if(mail.getItem().equals("{}")){
				continue;
			}
			
			Map<Integer, Map<Integer, Integer>> map = JSONtoItemMap(mail.getItem());			
			for(int key : map.keySet()){
				
				//分类处理
				AttachmentKey attchKey = AttachmentKey.getEnumByType(key);
				switch(attchKey){
					case money:
						Map<Integer, Integer> subCMap = map.get(key);
						for(Entry<Integer, Integer> e : subCMap.entrySet()){
							int subKey = e.getKey();
							if(cMap.containsKey(subKey)){
								int value = cMap.get(subKey);
								cMap.put(subKey, value + e.getValue());
							}else{
								cMap.put(subKey, e.getValue());
							}
						}	
						break;
					case item:
						Map<Integer, Integer> subIMap = map.get(key);
						for(Entry<Integer, Integer> e : subIMap.entrySet()){
							int subKey = e.getKey();
							if(iMap.containsKey(subKey)){
								int value = iMap.get(subKey);
								iMap.put(subKey, value + e.getValue());
							}else{
								iMap.put(subKey, e.getValue());
							}
						}
						break;
					default:
						break;
				}
			}
			//加入可删除列表
			canRemoveMailIds.add(mail.getId());
		}

		//判断物品是否可以添加到背包里   若可以则直接加入背包
		boolean itemAddState = itemAdd(humanObj, iMap);			
		if(!itemAddState){
			pickupAllMailResult(humanObj, 4, "背包已满!");
			Inform.user(humanObj.getHumanId(), Inform.提示操作,"背包已满!");
			return;
		}
		
        //获取货币附件		
		moneyAdd(humanObj,cMap);	
    	
		//删除已提取附件的邮件
    	Iterator<Mail> iter = mails.iterator();  
    	while(iter.hasNext()){  
    		Mail mail = iter.next();  
    	    if(canRemoveMailIds.contains(mail.getId())){  
    	    	iter.remove();  
    	    	mail.remove();
    	    }  
    	} 
    	
    	//返回删除信息
    	deleteMailIdsInfo(humanObj,canRemoveMailIds);
    	
		//提示成功提取
    	pickupAllMailResult(humanObj, 0, "提取成功!");
	}
	
	/**
	 * 一键提取返回信息
	 * @param humanObj
	 * @param code
	 * @param reason
	 */
	public void pickupAllMailResult(HumanObject humanObj, int code, String reason){
		SCPickupAllMail.Builder msg = SCPickupAllMail.newBuilder();	
        msg.setCode(code);
        if(!reason.equals("")){
            msg.setReason(reason);
        }
		humanObj.sendMsg(msg);			
	}	
	
	/**
	 * 被删除邮件的ID列表信息
	 * @param humanObj
	 * @param ids
	 */
	public void deleteMailIdsInfo(HumanObject humanObj,List<Long> ids){
    	SCDeleteMail.Builder msg = SCDeleteMail.newBuilder();
		for(int i = 0; i < ids.size(); i++){
			msg.addIds(ids.get(i));
		}		
		humanObj.sendMsg(msg);			
	}
	
	/**
	 * 清理邮件通知
	 * @param humanObj
	 */
	public void clearMail(HumanObject humanObj){
		List<Long> ids = clearExpire(humanObj);
		deleteMailIdsInfo(humanObj,ids);
	}
	
	/**
	 * 清理过期邮件
	 * @param humanObj
	 * @return
	 */
	private List<Long> clearExpire(HumanObject humanObj){
		List<Mail> list = humanObj.mailList;
		List<Long> mailIds = new ArrayList<>();

    	Iterator<Mail> iter = list.iterator();  
    	while(iter.hasNext()){  
    		Mail mail = iter.next();  
    	    if(Port.getTime() > mail.getEtime()){  
    	    	mailIds.add(mail.getId());
    	    	iter.remove();  
    	    	mail.remove();
    	    }  
    	} 
    	
		return mailIds;
	}	
	
	/**
	 * 验证邮件是否有效
	 * @param humanObj
	 * @param mailId
	 */
	public void checkMailRemoveable(HumanObject humanObj, long mailId){
		Mail mail = null;
		
		//查找当前的邮件
		for (Mail m : humanObj.mailList) {
			if(m.getId() == mailId){
				mail = m;
				break;
			}
		}	
		
		//没找到
		if(mail == null){
			Inform.user(humanObj.getHumanId(), Inform.提示操作,"该邮件不存在!");
			return;
		}
		
		//删除邮件操作
		long state = Port.getTime() > mail.getEtime()?0:mail.getEtime();
		if(state <= 0){
			humanObj.mailList.remove(mail);
			mail.remove();				
		}
		
		SCCheckMailRemoveable.Builder msg = SCCheckMailRemoveable.newBuilder();
		msg.setSpanTime(state);
		humanObj.sendMsg(msg);
	}
	
	/**
	 * 构建Dmail信息
	 * @param mail
	 * @return
	 */
	public DMail.Builder builtDmailMsg (Mail mail) {
		DMail.Builder msg = DMail.newBuilder();
		
		msg.setId(mail.getId());
		msg.setSender(mail.getSender());
		msg.setTitle(mail.getTitle());
		msg.setContent(mail.getContent());

		msg.setRead(mail.isRead() ? 1 : 0);
		msg.setSendTime(mail.getRtime());
		msg.setSpanTime(mail.getEtime() - Port.getTime());
		
		//附件
		//JSON转换成java对象
		if(!mail.getItem().equals("{}")){
			Map<Integer, Map<Integer, Integer>> map = JSONtoItemMap(mail.getItem());
			
			Map<Integer, Integer> cMap = map.get(AttachmentKey.money.getType());
			if(cMap != null){
				DVirtual.Builder dv = DVirtual.newBuilder();
				for (Entry<Integer, Integer> e : cMap.entrySet()) {
					dv.setType(e.getKey());
					dv.setNum(e.getValue());			
					msg.addVirtuals(dv);
				}
			}
			
			//附件物品
			Map<Integer, Integer> iMap = map.get(AttachmentKey.item.getType());
			if(iMap != null){
				DItem.Builder di = DItem.newBuilder();
				for (Entry<Integer, Integer> e : iMap.entrySet()) {
					di.setCode(e.getKey());
					di.setNum(e.getValue());
					msg.addItems(di);
				}
			}
		}
						
		return msg;
	}	
	
}
