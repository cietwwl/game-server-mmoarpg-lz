package org.gof.demo.worldsrv.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.Chunk;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.character.MonsterObject;

public class StageCell {
	public int i;		//行
	public int j;		//列
	private long stageId;		//所属地图
	private Map<Long, WorldObject> worldObjects = new HashMap<Long,WorldObject>();			//该cell内所有地图单位
	private Map<Long, HumanObject> humans = new HashMap<>();											//该cell内所有玩家
	private Map<Long, MonsterObject> monsters = new HashMap<>();
	private Map<Long, GeneralObject> generals = new HashMap<>();
	//消息发送机制
//	public List<Builder> msgList = new ArrayList<Builder>();
	
	List<Integer> idList = new ArrayList<Integer>();
	List<Chunk> chunkList = new ArrayList<Chunk>();
	
	public StageCell(long stageId, int i, int j) {
		this.stageId = stageId;
		this.i = i;
		this.j = j;
	}
	
	public void cleanup() {
		
	}
	
	public void sendMsg() {
		if(idList.size() <= 0) {
			return;
		}
		
		for(HumanObject ho : getHumans().values()) {
//			if(ho.isInWorld()) {
				ho.sendMsg(idList, chunkList);
//			}
		}
		
		idList.clear();
		chunkList.clear();
	}
	
	/**
	 * 是否属于同一张地图的Cell
	 * @return
	 */
	public boolean isInSameStage(StageCell cell) {
		return this.stageId == cell.stageId;
	}
	
	/**
	 * 添加地图单元
	 * @param obj
	 */
	public void addWorldObject(WorldObject obj) {
		worldObjects.put(obj.id,obj);
		
		//记录玩家
		if(obj instanceof HumanObject) {
			humans.put(obj.id, (HumanObject)obj);
		}
		
		//记录怪物
		if(obj instanceof MonsterObject) {
			monsters.put(obj.id, (MonsterObject)obj);
		}
		
		//武将
		if(obj instanceof GeneralObject) {
			generals.put(obj.id, (GeneralObject)obj);
		}
		
	}
	
	/**
	 * 删除地图单元
	 * @param obj
	 */
	public void delWorldObject(WorldObject obj) {
		worldObjects.remove(obj.id);
		
		//删除玩家
		if(obj instanceof HumanObject) {
			this.humans.remove(obj.id);
		}
		
		//删除玩家
		if(obj instanceof MonsterObject) {
			this.monsters.remove(obj.id);
		}
		
		//删除武将
		if(obj instanceof GeneralObject) {
			this.generals.remove(obj.id);
		}
		
	}
	
	/**
	 * 判断两Cell是否为同一个
	 * @param cell
	 * @return
	 */
	public boolean equals(StageCell cell) {
		if(this.i == cell.i && this.j == cell.j) return true;
		return false;
	}
	
	//根据id获取对象
	public WorldObject getWorldObject(long id) {
		return worldObjects.get(id);
	}
	public HumanObject getHuman(long id) {
		return humans.get(id);
	}
	public MonsterObject getMonster(long id) {
		return monsters.get(id);
	}

	public Map<Long, WorldObject> getWorldObjects() {
		return worldObjects;
	}

	public Map<Long, HumanObject> getHumans() {
		return humans;
	}
	
	public Map<Long, HumanObject> getHumanObjects() {
		return humans;
	}
}
