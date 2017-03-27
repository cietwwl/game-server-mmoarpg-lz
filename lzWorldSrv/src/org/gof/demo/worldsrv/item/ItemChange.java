package org.gof.demo.worldsrv.item;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.gof.demo.battlesrv.stageObj.CharacterObject;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.msg.Msg.DBagCellAdd;
import org.gof.demo.worldsrv.msg.Msg.DBagCellMove;
import org.gof.demo.worldsrv.msg.Msg.DBagCellNumChange;
import org.gof.demo.worldsrv.msg.Msg.DBagUpdate;
import org.gof.demo.worldsrv.msg.Msg.DItem;
import org.gof.demo.worldsrv.msg.Msg.SCBagUpdate;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class ItemChange {

	/** 物品数量发生变化 */
	public static final byte NUM_CHANGE = 1;
	/** 增加物品 */
	public static final byte ADD = 2;
	/** 移动物品 */
	public static final byte MOVE = 3;

	// 添加列表
	private Set<Item> adds = new HashSet<>();
	// 修改列表
	private Set<Item> mods = new HashSet<>();
	// 删除列表
	private Set<Item> dels = new HashSet<>();
	// 是否是新获得的物品
	public boolean newly = false;

	// 未推给客户端的变动操作
	private Queue<DBagUpdate> modifyQueue = new LinkedList<>();

	/**
	 * 合并修改
	 * 
	 * @param change
	 */
	public void merge(ItemChange change) {
		// 合并添加列表
		adds.addAll(change.adds);

		// 合并修改列表
		mods.addAll(change.mods);

		// 合并删除列表
		dels.addAll(change.dels);

		newly = change.newly;

		modifyQueue.addAll(change.modifyQueue);
	}

	/**
	 * 发送事件
	 * 
	 * @param humanObj
	 */
	public void fireChangeEvent(CharacterObject humanObj) {
		// 发送 物品增加 事件
		for (Item i : adds) {
			fireAddEvent(humanObj, i);
		}

		// 发送 物品修改 事件
		for (Item i : mods) {
			// 可堆叠的物品新增物品时发送增加事件
			if (newly) {
				fireAddEvent(humanObj, i);
				continue;
			}

			// 修改事件
			fireModEvent(humanObj, i);
		}

		// 发送 物品删除 事件
		for (Item i : dels) {
			fireDelEvent(humanObj, i);
		}
	}

	/**
	 * 增加事件
	 * 
	 * @param humanObj
	 * @param item
	 */
	private void fireAddEvent(CharacterObject humanObj, Item item) {
		fireChangeEvent(humanObj, item);
		Event.fireEx(EventKey.ITEM_CHANGE_ADD, ConfItemData.get(item.getSn()).bType, "humanObj", humanObj, "item", item);
	}

	/**
	 * 删除事件
	 * 
	 * @param humanObj
	 * @param item
	 */
	private void fireDelEvent(CharacterObject humanObj, Item item) {
		fireChangeEvent(humanObj, item);
		Event.fireEx(EventKey.ITEM_CHANGE_DEL, ConfItemData.get(item.getSn()).bType, "humanObj", humanObj, "item", item);
	}

	/**
	 * 修改事件
	 * 
	 * @param humanObj
	 * @param item
	 */
	private void fireModEvent(CharacterObject humanObj, Item item) {
		fireChangeEvent(humanObj, item);
		Event.fireEx(EventKey.ITEM_CHANGE_MOD, ConfItemData.get(item.getSn()).bType, "humanObj", humanObj, "item", item);
	}

	/**
	 * 物品变动事件
	 * 
	 * @param humanObj
	 * @param item
	 */
	private void fireChangeEvent(CharacterObject humanObj, Item item) {
		Event.fireEx(EventKey.ITEM_CHANGE,
				ConfItemData.get(item.getSn()).bType, "humanObj", humanObj,
				"item", item);
	}

	/**
	 * 标记一个物品<em>将要</em>被删除。
	 * 
	 * @param item
	 *            即将删除的物品
	 */
	public void markDeleting(Item item) {
		this.dels.add(item);

		DBagUpdate.Builder b = DBagUpdate.newBuilder();
		b.setPType(item.getPackType());
		b.setType(NUM_CHANGE);
		DBagCellNumChange.Builder bb = DBagCellNumChange.newBuilder();
		bb.setIndex(item.getPosition());
		bb.setNum(0);
		b.setNumChanger(bb);
		this.modifyQueue.add(b.build());
	}

	/**
	 * 标记一个新物品<em>已</em>加入。
	 * 
	 * @param item
	 *            已加入的物品
	 */
	public void markAdded(Item item) {
		this.adds.add(item);

		DBagUpdate.Builder b = DBagUpdate.newBuilder();
		b.setPType(item.getPackType());
		b.setType(ADD);
		DBagCellAdd.Builder bb = DBagCellAdd.newBuilder();
		bb.setIndex(item.getPosition());
		bb.setItem(item.createMsg());
		b.setAdder(bb);
		this.modifyQueue.add(b.build());
	}

	/**
	 * 标记一个物品数量<em>已</em>改变。
	 * 
	 * @param item
	 *            已被改变数量的物品
	 */
	public void markNumChanged(Item item) {
		this.mods.add(item);

		DBagUpdate.Builder b = DBagUpdate.newBuilder();
		b.setPType(item.getPackType());
		b.setType(NUM_CHANGE);
		DBagCellNumChange.Builder bb = DBagCellNumChange.newBuilder();
		bb.setIndex(item.getPosition());
		bb.setNum(item.getNum());
		b.setNumChanger(bb);
		this.modifyQueue.add(b.build());
	}

	/**
	 * 标记一个物品位置<em>已</em>改变。
	 * 
	 * @param item 已被改变位置的物品
	 * @param oldPosition 改变之前的位置
	 */
	public void markPosSwapped(Item item, int oldPosition) {
		DBagUpdate.Builder b = DBagUpdate.newBuilder();
		b.setPType(item.getPackType());
		b.setType(MOVE);
		DBagCellMove.Builder bb = DBagCellMove.newBuilder();
		bb.setTIndex(oldPosition);
		bb.setSIndex(item.getPosition());
		b.setMove(bb);
		this.modifyQueue.add(b.build());
	}

	/**
	 * 得到更新数据。
	 * 
	 * @return 更新数据。
	 */
	public SCBagUpdate.Builder getUpdateMsg() {
		if (modifyQueue.isEmpty())
			return null;
		SCBagUpdate.Builder builder = SCBagUpdate.newBuilder();
		DBagUpdate update = modifyQueue.poll();
		while (update != null) {
			builder.addUpdates(update);
			__debugDBagUpdate(update);
			update = modifyQueue.poll();
		}
		Log.item.info("*******背包更新完毕********");
		return builder;
	}

	/**
	 * 抽出的debug方法，当{@link #getUpdateMsg()}时展示背包操作。
	 * 
	 * @param update
	 */
	private void __debugDBagUpdate(DBagUpdate update) {
		if (Log.item.isInfoEnabled()) {
			StringBuilder log = new StringBuilder();
			switch (update.getType()) {
			case NUM_CHANGE:
				log.append("数量变化：");
				int index = update.getNumChanger().getIndex();
				int num = update.getNumChanger().getNum();
				log.append("index[" + index + "]->" + num);
				break;
			case ADD:
				log.append("增加：");
				index = update.getAdder().getIndex();
				DItem item = update.getAdder().getItem();
				int code = item.getCode();
				num = item.getNum();
				log.append("index[" + index + "]->" + code + "x" + num);
				break;
			case MOVE:
				log.append("交换位置：");
				DBagCellMove move = update.getMove();
				int sindex = move.getSIndex();
				int tindex = move.getTIndex();
				log.append("index[" + sindex + "]<->index[" + tindex + "]");
				break;
			}
			Log.item.info(log.toString());
		}
	}
}
