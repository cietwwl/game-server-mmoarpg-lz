package org.gof.demo.worldsrv.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.gof.core.Record;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.entity.EntityBase;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.stageObj.UnitDataPersistance;
import org.gof.demo.battlesrv.stageObj.UnitPropPlusMap;
import org.gof.demo.worldsrv.character.GeneralObject;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.entity.UnitPropPlus;
import org.gof.demo.worldsrv.item.ItemPack;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

/**
 * 玩家登录时 加载玩家数据辅助类
 * 基于一些默认情况 没有通用性
 * 被加载类型必须是List Map ItemPack 或 EntityBase的子类
 * 如果是Map类型 key值必须是主键ID
 */
public class LoadHumanDataUtils {
	
	/**
	 * 加载玩家数据辅助方法
	 * @param humanObj 玩家对象
	 * @param propName	被加载的属性名
	 * @param entityClass 包装类型 必须是EntityBase的子类
	 * @param queryParams 查询参数
	 */
	public static void load(HumanObject humanObj, String propName, String mapKey, Class<? extends EntityBase> entityClass, Object...queryParams) {
		try {
			new LoadHumanDataUtils().loadHumanData(humanObj, propName, mapKey, entityClass, queryParams);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}

	/**
	 * 加载数据
	 * @param humanObj
	 * @param propName
	 * @param entityClass
	 * @param queryParams
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void loadHumanData(HumanObject humanObj, String propName, String mapKey, Class<? extends EntityBase> entityClass, Object...queryParams) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		//获取表名
		Field f = entityClass.getField("tableName");
		String tableName = (String) f.get(Human.class);
		
		//准备环境
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findBy(true, tableName, queryParams);
		prx.listenResult(this::_result_loadHumanData, "humanObj", humanObj, "propName", propName, "entityClass", entityClass, "mapKey", mapKey);
		
		//一次加载事件开始
		Event.fire(EventKey.HUMAN_DATA_LOAD_BEGIN_ONE, humanObj);
	}
	
	
	/**
	 * 玩家登录游戏后 加载玩家的数据返回
	 * @param timeout
	 * @param results
	 * @param context
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void _result_loadHumanData(Param results, Param context) {
		try {
			//玩家
			String propName = context.get("propName");
			Class<? extends EntityBase> entityClass = context.get("entityClass");
			HumanObject humanObj = context.get("humanObj");
			UnitDataPersistance data = humanObj.dataPers;
			String mapKey = context.get("mapKey");
			
			List<Record> records = results.get();
	
			//已知 必然是public属性 所以直接拿
			Field field = UnitDataPersistance.class.getField(propName);
			Object fieldObj = field.get(data);
			
			//存储类型 暂时只支持List Map ItemPack EntityBase的子类
			Class<?> propType = field.getType();
			
			//包装构造函数
			Constructor<? extends EntityBase> constMonthod = entityClass.getConstructor(Record.class);
	
			//根据不同的存储类型来组织数据
			if(propType.isAssignableFrom(List.class)) {
				//拼装数据
				List<Object> val = new ArrayList<>();
				for(Record r : records) {
					val.add(constMonthod.newInstance(r));
				}
				
				//设置数据
				propType.getMethod("addAll", Collection.class).invoke(fieldObj, val);
			} else if(propType.isAssignableFrom(Map.class)) {
				//拼装数据
				String key = "id";
				if(!StringUtils.isEmpty(mapKey)) {
					key = mapKey;
					Map<Integer, Object> val = new LinkedHashMap<>();
					for(Record r : records) {
						val.put(r.<Integer>get(key), constMonthod.newInstance(r));
					}
					//设置数据
					propType.getMethod("putAll", Map.class).invoke(fieldObj, val);
				} else {
					Map<Long, Object> val = new LinkedHashMap<>();
					for(Record r : records) {
						val.put(r.<Long>get(key), constMonthod.newInstance(r));
					}
					//设置数据
					propType.getMethod("putAll", Map.class).invoke(fieldObj, val);
				}
				
			} else if(propType.isAssignableFrom(UnitPropPlusMap.class)) {
				if(!records.isEmpty()) {
					UnitPropPlus pp = (UnitPropPlus)constMonthod.newInstance(records.get(0));
					propType.getMethod("init", UnitPropPlus.class).invoke(fieldObj,  pp);
//					data.unitPropPlus.init(pp);
				}
			} else if (propType.isAssignableFrom(ItemPack.class)) {
				if(!records.isEmpty()) {
					List<Item> val = new ArrayList<>();
					for(Record r : records) {
						val.add((Item) constMonthod.newInstance(r));
					}
					data.items = new ItemPack(val);
				} else {
					data.items = new ItemPack();
				}
			} else {
				//拼装数据
				if(!records.isEmpty()) {
					EntityBase propValue = constMonthod.newInstance(records.get(0));
					
					//设置数据
					Utils.fieldWrite(data, propName, propValue);
				}
			}
			
			//一次加载事件结束
			Event.fire(EventKey.HUMAN_DATA_LOAD_FINISH_ONE, humanObj);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	public static void loadGeneral(HumanObject humanObj, GeneralObject genObj, String propName, String mapKey, Class<? extends EntityBase> entityClass, Object...queryParams) {
		try {
			new LoadHumanDataUtils().loadGeneralData(humanObj, genObj,propName, mapKey, entityClass, queryParams);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	private void loadGeneralData(HumanObject humanObj, GeneralObject genObj, String propName, String mapKey, Class<? extends EntityBase> entityClass, Object...queryParams) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		//获取表名
		Field f = entityClass.getField("tableName");
		String tableName = (String) f.get(Human.class);
		
		//准备环境
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.findBy(true, tableName, queryParams);
		prx.listenResult(this::_result_loadGeneralData, "humanObj", humanObj, "generalObj", genObj, "propName", propName, "entityClass", entityClass, "mapKey", mapKey);
		
		//一次加载事件开始
		Event.fire(EventKey.HUMAN_DATA_LOAD_BEGIN_ONE, humanObj);
	}
	
	public void _result_loadGeneralData(Param results, Param context) {
		try {
			//玩家
			String propName = context.get("propName");
			Class<? extends EntityBase> entityClass = context.get("entityClass");
			HumanObject humanObj = context.get("humanObj");
			GeneralObject genObj = context.get("generalObj");
			UnitDataPersistance data = genObj.dataPers;
			String mapKey = context.get("mapKey");
			
			List<Record> records = results.get();
	
			//已知 必然是public属性 所以直接拿
			Field field = UnitDataPersistance.class.getField(propName);
			Object fieldObj = field.get(data);
			
			//存储类型 暂时只支持List Map ItemPack EntityBase的子类
			Class<?> propType = field.getType();
			
			//包装构造函数
			Constructor<? extends EntityBase> constMonthod = entityClass.getConstructor(Record.class);
	
			//根据不同的存储类型来组织数据
			if(propType.isAssignableFrom(List.class)) {
				//拼装数据
				List<Object> val = new ArrayList<>();
				for(Record r : records) {
					val.add(constMonthod.newInstance(r));
				}
				
				//设置数据
				propType.getMethod("addAll", Collection.class).invoke(fieldObj, val);
			} else if(propType.isAssignableFrom(Map.class)) {
				//拼装数据
				String key = "id";
				if(!StringUtils.isEmpty(mapKey)) {
					key = mapKey;
					Map<Integer, Object> val = new LinkedHashMap<>();
					for(Record r : records) {
						val.put(r.<Integer>get(key), constMonthod.newInstance(r));
					}
					//设置数据
					propType.getMethod("putAll", Map.class).invoke(fieldObj, val);
				} else {
					Map<Long, Object> val = new LinkedHashMap<>();
					for(Record r : records) {
						val.put(r.<Long>get(key), constMonthod.newInstance(r));
					}
					//设置数据
					propType.getMethod("putAll", Map.class).invoke(fieldObj, val);
				}
				
			} else if(propType.isAssignableFrom(UnitPropPlusMap.class)) {
				if(!records.isEmpty()) {
					UnitPropPlus pp = (UnitPropPlus)constMonthod.newInstance(records.get(0));
					propType.getMethod("init", UnitPropPlus.class).invoke(fieldObj,  pp);
				}
			} else {
				//拼装数据
				if(!records.isEmpty()) {
					EntityBase propValue = constMonthod.newInstance(records.get(0));
					
					//设置数据
					Utils.fieldWrite(data, propName, propValue);
				}
			}
			
			//一次加载事件结束
			Event.fire(EventKey.HUMAN_DATA_LOAD_FINISH_ONE, humanObj);
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
}
