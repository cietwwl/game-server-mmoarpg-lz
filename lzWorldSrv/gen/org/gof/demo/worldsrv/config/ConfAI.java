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
 * AI|AI 
 * AI.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfAI {
	public final int sn;			//AI编号SN
	public final boolean isAttactAuto;			//是否主动攻击
	public final boolean isCounterattackAuto;			//是否可以攻击
	public final float radiusMove;			//活动半径(如果活动半径为0，则表示这个怪不会巡逻)
	public final float radiusAlert;			//警戒半径
	public final float radiusChase;			//追击半径
	public final int timePatrol;			//巡逻周期（ms）
	public final String[] skillAll;			//所有技能JSON{sn:等级}
	public final int[] skillLevel;			//
	public final String[] trigger;			//触发条件:1.hp    血量2.hpPer 血量百分比 
	public final int[] triggerValue;			//触发值
	public final int[] triggerSkill;			//触发技能
	public final int[] attack;			//循环攻击sn,根据攻击顺序循环
	public final int timeFireSkill;			//施放技能CD（单位ms）
	public final int timeRefresh;			//死亡刷新时间（ms）如果为0，表示死亡后直接删除，不再刷新
	public final float grid9Dis;			//寻路和目标点网格的记录
	public final float grid9Radius;			//寻路目标之间的距离 至少要比前面的 grid9Dis * sin（22.5）*2 grid9Dis * 0.765 小
	public final float fightDis;			//可攻击的距离
	public final float genMaxDis;			//武将与主角的最大距离
	public final float genStaDis;			//武将的围绕半径

	public ConfAI(int sn, boolean isAttactAuto, boolean isCounterattackAuto, float radiusMove, float radiusAlert, float radiusChase, int timePatrol, String[] skillAll, int[] skillLevel, String[] trigger, int[] triggerValue, int[] triggerSkill, int[] attack, int timeFireSkill, int timeRefresh, float grid9Dis, float grid9Radius, float fightDis, float genMaxDis, float genStaDis) {
			this.sn = sn;		
			this.isAttactAuto = isAttactAuto;		
			this.isCounterattackAuto = isCounterattackAuto;		
			this.radiusMove = radiusMove;		
			this.radiusAlert = radiusAlert;		
			this.radiusChase = radiusChase;		
			this.timePatrol = timePatrol;		
			this.skillAll = skillAll;		
			this.skillLevel = skillLevel;		
			this.trigger = trigger;		
			this.triggerValue = triggerValue;		
			this.triggerSkill = triggerSkill;		
			this.attack = attack;		
			this.timeFireSkill = timeFireSkill;		
			this.timeRefresh = timeRefresh;		
			this.grid9Dis = grid9Dis;		
			this.grid9Radius = grid9Radius;		
			this.fightDis = fightDis;		
			this.genMaxDis = genMaxDis;		
			this.genStaDis = genStaDis;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfAI> findAll() {
		return DATA.getList();
	}

	/**
	 * 通过SN获取数据
	 * @param sn
	 * @return
	 */
	public static ConfAI get(Integer sn) {
		return DATA.getMap().get(sn);
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ConfAI getBy(Object...params) {
		List<ConfAI> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfAI> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfAI> utilBase(Object...params) {
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
		List<ConfAI> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfAI c : DATA.getList()) {
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
		Collections.sort(result, new Comparator<ConfAI>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfAI a, ConfAI b) {
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
		public static final String sn = "sn";	//AI编号SN
		public static final String isAttactAuto = "isAttactAuto";	//是否主动攻击
		public static final String isCounterattackAuto = "isCounterattackAuto";	//是否可以攻击
		public static final String radiusMove = "radiusMove";	//活动半径(如果活动半径为0，则表示这个怪不会巡逻)
		public static final String radiusAlert = "radiusAlert";	//警戒半径
		public static final String radiusChase = "radiusChase";	//追击半径
		public static final String timePatrol = "timePatrol";	//巡逻周期（ms）
		public static final String skillAll = "skillAll";	//所有技能JSON{sn:等级}
		public static final String skillLevel = "skillLevel";	//
		public static final String trigger = "trigger";	//触发条件:1.hp    血量2.hpPer 血量百分比 
		public static final String triggerValue = "triggerValue";	//触发值
		public static final String triggerSkill = "triggerSkill";	//触发技能
		public static final String attack = "attack";	//循环攻击sn,根据攻击顺序循环
		public static final String timeFireSkill = "timeFireSkill";	//施放技能CD（单位ms）
		public static final String timeRefresh = "timeRefresh";	//死亡刷新时间（ms）如果为0，表示死亡后直接删除，不再刷新
		public static final String grid9Dis = "grid9Dis";	//寻路和目标点网格的记录
		public static final String grid9Radius = "grid9Radius";	//寻路目标之间的距离 至少要比前面的 grid9Dis * sin（22.5）*2 grid9Dis * 0.765 小
		public static final String fightDis = "fightDis";	//可攻击的距离
		public static final String genMaxDis = "genMaxDis";	//武将与主角的最大距离
		public static final String genStaDis = "genStaDis";	//武将的围绕半径
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static volatile Map<Integer, ConfAI> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfAI> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfAI> getMap() {
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
			Map<Integer, ConfAI> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfAI object = new ConfAI(conf.getIntValue("sn"), conf.getBooleanValue("isAttactAuto"), conf.getBooleanValue("isCounterattackAuto"), conf.getFloatValue("radiusMove"), 
				conf.getFloatValue("radiusAlert"), conf.getFloatValue("radiusChase"), conf.getIntValue("timePatrol"), parseStringArray(conf.getString("skillAll")), 
				parseIntArray(conf.getString("skillLevel")), parseStringArray(conf.getString("trigger")), parseIntArray(conf.getString("triggerValue")), parseIntArray(conf.getString("triggerSkill")), 
				parseIntArray(conf.getString("attack")), conf.getIntValue("timeFireSkill"), conf.getIntValue("timeRefresh"), conf.getFloatValue("grid9Dis"), 
				conf.getFloatValue("grid9Radius"), conf.getFloatValue("fightDis"), conf.getFloatValue("genMaxDis"), conf.getFloatValue("genStaDis"));
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
			String baseBath = ConfAI.class.getResource("").getPath();
			File file = new File(baseBath + "json/ConfAI.json");
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