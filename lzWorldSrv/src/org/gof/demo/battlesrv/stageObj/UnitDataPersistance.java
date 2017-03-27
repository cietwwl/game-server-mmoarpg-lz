package org.gof.demo.battlesrv.stageObj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.entity.Activity;
import org.gof.demo.worldsrv.entity.Buff;
import org.gof.demo.worldsrv.entity.Friend;
import org.gof.demo.worldsrv.entity.InstanceChapter;
import org.gof.demo.worldsrv.entity.Part;
import org.gof.demo.worldsrv.entity.PocketLine;
import org.gof.demo.worldsrv.entity.Tower;
import org.gof.demo.worldsrv.entity.Unit;
import org.gof.demo.worldsrv.item.ItemPack;

public class UnitDataPersistance implements ISerilizable {
	public Unit unit;						//基本信息
	public ItemPack items;				//物品信息
	
	public final Map<Integer, Part> parts;		// 装备位，部位号为key
	
	public final Map<Integer, InstanceChapter> instanceChapters;	//副本章节

	public final Map<Integer, Buff> buffs;			//玩家身上的buff集合，type，Buff映射
	
	public UnitPropPlusMap unitPropPlus;			//玩家属性加成
	
	public Activity activity;                      //活动
	
	public Tower tower;                            //远征（爬塔）
	
	public Friend friend;                            //好友
	
	public List<PocketLine> pocketLine;	           // 待办事项
	
	/**
	 * 构造函数
	 */
	public UnitDataPersistance() {
		items = new ItemPack();
		parts = new HashMap<>();
		buffs = new HashMap<>();
		instanceChapters = new HashMap<>();
		unitPropPlus = new UnitPropPlusMap();	
		pocketLine = new ArrayList<>();
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(unit);
		out.write(items);
		out.write(unitPropPlus);
		out.write(parts);
		out.write(buffs);
		out.write(instanceChapters);
		out.write(activity);
		out.write(tower);
		out.write(friend);
		out.write(pocketLine);
	}
	
	@Override
	public void readFrom(InputStream in) throws IOException {
		unit = in.read();
		items = in.read();
		unitPropPlus = in.read();
		parts.clear();
		parts.putAll(in.<Map<Integer, Part>>read());
		buffs.clear();
		buffs.putAll(in.<Map<Integer, Buff>>read());
		instanceChapters.clear();
		instanceChapters.putAll(in.<Map<Integer, InstanceChapter>>read());
		activity = in.read();
		tower = in.read();
		friend = in.read();
		pocketLine = in.read();
	}
}
