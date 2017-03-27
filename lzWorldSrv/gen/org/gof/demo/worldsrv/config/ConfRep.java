package org.gof.demo.worldsrv.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.gof.core.db.OrderBy;
import org.gof.core.support.ConfigJSON;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;

/**
 * 副本配置|Rep 
 * Rep.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfRep {
	public final int sn;			//SN
	public final int repType;			//副本类型
	public final int preRepID;			//上一个副本
	public final int[] produceFirst;			//首次掉落包
	public final int[] produce;			//掉落包
	public final int[] produceAuto;			//扫荡掉落包
	public final int[] produceType;			//固定掉落类型
	public final int[] produceNum;			//固定掉落数值
	public final int mapSn;			//地图编号
	public final int chapterID;			//所属章节
	public final int level;			//开启等级
	public final int cost;			//消耗体力
	public final int income;			//金币收益
	public final int humanExp;			//人物获得经验
	public final int generalExp;			//伙伴获得经验
	public final int attNum;			//每天攻击次数
	public final int[] repTask;			//副本任务
	public final int resetCost;			//每天重置花费(钻石)
	public final int limitTime;			//挑战时间
	public final int lottery;			//抽奖sn
	public final int chapter;			//哇嘎嘎
	public final int belongItemSn;			//对对对

	public ConfRep(int sn, int repType, int preRepID, int[] produceFirst, int[] produce, int[] produceAuto, int[] produceType, int[] produceNum, int mapSn, int chapterID, int level, int cost, int income, int humanExp, int generalExp, int attNum, int[] repTask, int resetCost, int limitTime, int lottery, int chapter, int belongItemSn) {
			this.sn = sn;		
			this.repType = repType;		
			this.preRepID = preRepID;		
			this.produceFirst = produceFirst;		
			this.produce = produce;		
			this.produceAuto = produceAuto;		
			this.produceType = produceType;		
			this.produceNum = produceNum;		
			this.mapSn = mapSn;		
			this.chapterID = chapterID;		
			this.level = level;		
			this.cost = cost;		
			this.income = income;		
			this.humanExp = humanExp;		
			this.generalExp = generalExp;		
			this.attNum = attNum;		
			this.repTask = repTask;		
			this.resetCost = resetCost;		
			this.limitTime = limitTime;		
			this.lottery = lottery;		
			this.chapter = chapter;		
			this.belongItemSn = belongItemSn;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfRep> findAll() {
		return DATA.getList();
	}

	/**
	 * 通过SN获取数据
	 * @param sn
	 * @return
	 */
	public static ConfRep get(Integer sn) {
		return DATA.getMap().get(sn);
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ConfRep getBy(Object...params) {
		List<ConfRep> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfRep> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfRep> utilBase(Object...params) {
		List<Object> settings = Utils.ofList(params);
		
		//查询参数
		final Map<String, Object> paramsFilter = new LinkedHashMap<>();		//过滤条件
		final Map<String, OrderBy> paramsOrder = new LinkedHashMap<>();		//排序规则
				
		//参数数量
		int len = settings.size();
		
		//参数必须成对出现
		if(len % 2 != 0) {
			throw new SysException("查询参数必须成对出现:query={}", settings);
		}
		
		//处理成对参数
		for(int i = 0; i < len; i += 2) {
			String key = (String)settings.get(i);
			Object val = settings.get(i + 1);
			
			//参数 排序规则
			if(val instanceof OrderBy) {
				paramsOrder.put(key, (OrderBy) val);
			} else {	//参数 过滤条件
				paramsFilter.put(key, val);
			}
		}
		
		//返回结果
		List<ConfRep> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfRep c : DATA.getList()) {
				//本行数据是否符合过滤条件
				boolean bingo = true;
				
				//判断过滤条件
				for(Entry<String, Object> p : paramsFilter.entrySet()) {
					Field field = c.getClass().getField(p.getKey());
					
					//实际结果
					Object valTrue = field.get(c);
					//期望结果
					Object valWish = p.getValue();
					
					//有不符合过滤条件的
					if(!valWish.equals(valTrue)) {
						bingo = false;
						break;
					}
				}
				
				//记录符合结果
				if(bingo) {
					result.add(c);
				}
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
		
		//对结果进行排序
		Collections.sort(result, new Comparator<ConfRep>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfRep a, ConfRep b) {
				try {
					for(Entry<String, OrderBy> e : paramsOrder.entrySet()) {
						//两方字段
						Field fa = a.getClass().getField(e.getKey());
						Field fb = b.getClass().getField(e.getKey());
						//两方字段值
						Comparable va = (Comparable) fa.get(a);
						Comparable vb = (Comparable) fb.get(b);
						
						//值排序结果
						int compareResult = va.compareTo(vb);
						
						//相等时 根据下一个值进行排序
						if(va.compareTo(vb) == 0) continue;
						
						//配置排序规则
						OrderBy order = e.getValue();
						if(order == OrderBy.ASC) return compareResult;		//正序
						else return -1 * compareResult;					//倒序
					}
				} catch (Exception e) {
					throw new SysException(e);
				}

				return 0;
			}
		});
		
		return result;
	}

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String sn = "sn";	//SN
		public static final String repType = "repType";	//副本类型
		public static final String preRepID = "preRepID";	//上一个副本
		public static final String produceFirst = "produceFirst";	//首次掉落包
		public static final String produce = "produce";	//掉落包
		public static final String produceAuto = "produceAuto";	//扫荡掉落包
		public static final String produceType = "produceType";	//固定掉落类型
		public static final String produceNum = "produceNum";	//固定掉落数值
		public static final String mapSn = "mapSn";	//地图编号
		public static final String chapterID = "chapterID";	//所属章节
		public static final String level = "level";	//开启等级
		public static final String cost = "cost";	//消耗体力
		public static final String income = "income";	//金币收益
		public static final String humanExp = "humanExp";	//人物获得经验
		public static final String generalExp = "generalExp";	//伙伴获得经验
		public static final String attNum = "attNum";	//每天攻击次数
		public static final String repTask = "repTask";	//副本任务
		public static final String resetCost = "resetCost";	//每天重置花费(钻石)
		public static final String limitTime = "limitTime";	//挑战时间
		public static final String lottery = "lottery";	//抽奖sn
		public static final String chapter = "chapter";	//哇嘎嘎
		public static final String belongItemSn = "belongItemSn";	//对对对
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static volatile Map<Integer, ConfRep> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfRep> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfRep> getMap() {
			//延迟初始化
			if(_map == null) {
				synchronized (DATA.class) {
					if(_map == null) {
						_init();
					}
				}
			}
			
			return _map;
		}


		/**
		 * 初始化数据
		 */
		private static void _init() {
			Map<Integer, ConfRep> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfRep object = new ConfRep(conf.getIntValue("sn"), conf.getIntValue("repType"), conf.getIntValue("preRepID"), parseIntArray(conf.getString("produceFirst")), 
				parseIntArray(conf.getString("produce")), parseIntArray(conf.getString("produceAuto")), parseIntArray(conf.getString("produceType")), parseIntArray(conf.getString("produceNum")), 
				conf.getIntValue("mapSn"), conf.getIntValue("chapterID"), conf.getIntValue("level"), conf.getIntValue("cost"), 
				conf.getIntValue("income"), conf.getIntValue("humanExp"), conf.getIntValue("generalExp"), conf.getIntValue("attNum"), 
				parseIntArray(conf.getString("repTask")), conf.getIntValue("resetCost"), conf.getIntValue("limitTime"), conf.getIntValue("lottery"), 
				conf.getIntValue("chapter"), conf.getIntValue("belongItemSn"));
				dataMap.put(conf.getInteger("sn"), object);
			}

			//保存数据
			_map = Collections.unmodifiableMap(dataMap);
		}
		
		
		
		public static double[] parseDoubleArray(String value) {
			if(value == null) value = "";
			if(StringUtils.isEmpty(value)){
				return new double[0];
			}
			String[] elems = value.split(",");
			if(elems.length > 0) {
				double []temp = new double[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.doubleValue(elems[i]);
				}
				return temp;
			}
			return null;
	  }
	  
		public static float[] parseFloatArray(String value) {
			if(value == null) value = "";
			
			String[] elems = value.split(",");
			if(StringUtils.isEmpty(value)){
				return new float[0];
			}
			if(elems.length > 0) {
				float []temp = new float[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.floatValue(elems[i]);
				}
				return temp;
			}
			return null;
		}
		
		public static int[] parseIntArray(String value) {
			if(value == null) value = "";
			if(StringUtils.isEmpty(value)){
				return new int[0];
			}
			String[] elems = value.split(",");
			if(elems.length > 0) {
				int []temp = new int[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.intValue(elems[i]);
				}
				return temp;
			}
			return null;
		}
	
		public static String[] parseStringArray(String value) {
			if(value == null) value = "";
			if(StringUtils.isEmpty(value)){
				return new String[0];
			}
			String[] elems = value.split(",");
			if(elems.length > 0) {
				String []temp = new String[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = elems[i];
				}
				return temp;
			}
			return null;
		}
		
		public static long[] parseLongArray(String value) {
			if(value == null) value = "";
			if(StringUtils.isEmpty(value)){
				return new long[0];
			}
			String[] elems = value.split(",");
			if(elems.length > 0) {
				long []temp = new long[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.longValue(elems[i]);
				}
				return temp;
			}
			return null;
		}
	 public static boolean[] parseBoolArray(String value) {
			if(value == null) value = "";
			if(StringUtils.isEmpty(value)){
				return new boolean[0];
			}
			String[] elems = value.split(",");
			if(elems.length > 0) {
				boolean []temp = new boolean[elems.length] ;
				for(int i = 0 ; i < elems.length ; i++) {
					temp[i] = Utils.booleanValue(elems[i]);
				}
				return temp;
			}
			return null;
	 }
	
	
	
		/**
		 * 读取游戏配置
		 */
		private static String _readConfFile() {
			String baseBath = ConfRep.class.getResource("").getPath();
			File file = new File(baseBath + "json/ConfRep.json");
			//文件不存在 忽略
			if(!file.exists()) return null;

			String result = "";

			try ( BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
			    String tempString = "";
			    while ((tempString = reader.readLine()) != null) {
				result += tempString;
			    }
			} catch (IOException e) {
			    throw new RuntimeException(e);
			} 

			return result;
		}
	}
    
}