package org.gof.core.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gof.core.Chunk;
import org.gof.core.Record;
import org.gof.core.interfaces.IRecord;
import org.gof.core.support.BufferPool;
import org.gof.core.support.log.LogCore;

public class FlushingRecords {
	//数据 <ID, <刷新版本号, 实际数据>>
	private Map<Long, LinkedHashMap<Long, Record>> records = new HashMap<>();	
	
	/**
	 * 新增
	 * @param id
	 * @param record
	 */
	public void put(long id, long version, Record record) {
		LinkedHashMap<Long, Record> rs = records.get(id);
		if(rs == null) {
			rs = new LinkedHashMap<>();
			records.put(id, rs);
		}
		
		//增加数据
		rs.put(version, record);
	}
	
	/**
	 * 清除
	 * @param id
	 * @return
	 */
	public Record remove(long id, long version) {
		Map<Long, Record> rs = records.get(id);
		if(rs == null) return null;
		
		//删除
		Record result = rs.remove(version);
		
		//清理空包
		if(rs.isEmpty()) {
			records.remove(id);
		}
		
		return result;
	}
	
	/**
	 * 获取
	 * @param id
	 * @return
	 */
	public Record get(long id, long version) {
		Map<Long, Record> rs = records.get(id);
		if(rs == null) return null;
		
		//返回值
		return rs.get(version);
	}
	
	/**
	 * 对给定的结果进行未入库数据修正
	 * @param record
	 */
	public void pathUpdate(IRecord record, long finishedVer) {
		if(record == null) return;
		
		//主键
		long id = record.get("id");
		//未入库修改
		LinkedHashMap<Long, Record> paths = records.get(id);
		if(paths == null) return;
		
		for(Iterator<Entry<Long, Record>> iter = paths.entrySet().iterator(); iter.hasNext();) {
			Entry<Long, Record> e = iter.next();
			long v = e.getKey();
			Record r = e.getValue();
			
			//过期的版本号 忽略+删除
			//版本号的返回应该是顺序的 如果还有更早的数据 证明那个版本的更新失败了
			if(v < finishedVer) {
				LogCore.db.error("恢复数据时发现了旧数据，忽略并删除：recordVer={}, finishVer={}, record={}", v, finishedVer, r);
				
				iter.remove();
				return;
			}
			
			//恢复数据
			Chunk path = r.pathUpdateGen();
			record.patchUpdate(path);
			//回收缓冲buff
			BufferPool.deallocate(path.buffer);
		}
	}
	
	/**
	 * 获取缓存的ID列表
	 * @param servId
	 * @return
	 */
	public Set<Long> getIds() {
		return records.keySet();
	}
}
