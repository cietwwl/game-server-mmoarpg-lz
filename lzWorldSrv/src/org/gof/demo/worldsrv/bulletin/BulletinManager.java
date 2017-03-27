package org.gof.demo.worldsrv.bulletin;

import java.text.MessageFormat;
import java.util.Collection;

import org.gof.core.CallPoint;
import org.gof.core.Chunk;
import org.gof.core.connsrv.ConnectionProxy;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfBulletin;
import org.gof.demo.worldsrv.config.ConfBulletinRoll;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DBulletinItem;
import org.gof.demo.worldsrv.msg.Msg.SCBulletinOpenUI;
import org.gof.demo.worldsrv.msg.MsgIds;
import org.gof.demo.worldsrv.support.observer.EventKey;
/**
 * 公告 和 走马灯
 */
public class BulletinManager extends ManagerBase {
	

	public static final int ROLL_TYPE_1 = 1;	//当玩家获得指定品质的装备
	public static final int ROLL_TYPE_2 = 2;	//当玩家获得指定成就
	public static final int ROLL_TYPE_3 = 3;	//当玩家集齐指定套装
	public static final int ROLL_TYPE_4 = 4;	//当玩家将指定套装属性系统全部激活
	public static final int ROLL_TYPE_5 = 5;	//当玩家竞技场达到一定连胜
	public static final int ROLL_TYPE_6 = 6;	//当玩家竞技场达到一定名次
	
	/**
	 * 获取实例
	 * @return
	 */
	public static BulletinManager inst() {
		return inst(BulletinManager.class);
	}
	/**
	 * 普通公告，客户端主动请求
	 * @param connPoint
	 */
	public void openUI(CallPoint connPoint) {
		SCBulletinOpenUI.Builder msg = SCBulletinOpenUI.newBuilder();
		Collection<ConfBulletin> cfs = ConfBulletin.findAll();
		for(ConfBulletin cf : cfs){
			DBulletinItem.Builder it = DBulletinItem.newBuilder();
			it.setId(cf.sn);
			it.setStatus(cf.bulletinStatusType);
			it.setType(cf.bulletinType);
			it.setContent(cf.content);
			it.setName(cf.name);
			msg.addItems(it.build());
		}
		ConnectionProxy prx = ConnectionProxy.newInstance(connPoint);
		prx.sendMsg(MsgIds.SCBulletinOpenUI, new Chunk(msg));
	}
	/**
	 * 对某个人发送走马灯
	 */
	public static void sendRollInform(long humanId, String content){
		Inform.user(humanId, Inform.通告滚动, content);
	}
	/**
	 * 全服走马灯
	 */
	public static void sendRollInformAll(String content){
		Inform.all(Inform.通告滚动, content);
	}
	/* **************************
	 * 玩家达到各种条件触发的走马灯滚动   *
	 * **************************/
	/** 当玩家获得指定品质的装备  */
	@Listener(EventKey.ITEM_CHANGE_ADD)
	public void _listener_ROLL_TYPE_1(Param param) {
		HumanObject humanObj = null; 
		
		CharacterObject obj = param.get("humanObj");
		if(obj instanceof HumanObject){
			humanObj = (HumanObject) obj;
		}else{
			return;
		}
		ConfBulletinRoll cf = ConfBulletinRoll.getBy("rollType",ROLL_TYPE_1);
		if(cf == null){
			return;
		}
		Item item = param.get("item");
		if(item.getSn() == cf.target[0]){
			String itemName = ConfItemData.get(item.getSn()).name;
			String content = MessageFormat.format(cf.content, humanObj.name, itemName);
			Inform.user(humanObj.id, Inform.通告滚动, content);
		}
	}
	
	
	/** 当玩家获得指定成就  */
//	@Listener(EventKey.)
	public void _listener_ROLL_TYPE_2(Param param) {
	}
	/** 当玩家集齐指定套装 */
//	@Listener(EventKey.)
	public void _listener_ROLL_TYPE_3(Param param) {
	}
	/** 当玩家将指定套装属性系统全部激活 */
//	@Listener(EventKey.)
	public void _listener_ROLL_TYPE_4(Param param) {
	}
	/** 当玩家竞技场达到一定连胜 */
	@Listener(EventKey.COMPETE_OFFLINE_PASS)
	public void _listener_ROLL_TYPE_5(Param param) {
	}
	/** 当玩家竞技场达到一定名次 */
	@Listener(EventKey.INSTANCE_PASS)
	public void _listener_ROLL_TYPE_6(Param param) {
	}
}