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
 * 触发条件|SceneTrigger 
 * SceneTrigger.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfSceneTrigger {
	public final String sn;			//触发条件ID
	public final int triggerType;			//触发条件类型
	public final int nParam1;			//整型参数1
	public final int nParam2;			//整型参数2
	public final int nParam3;			//整型参数3
	public final int[] arrnParam1;			//整型数组参数1
	public final int[] arrnParam2;			//整型数组参数2
	public final int[] arrnParam3;			//整型数组参数3
	public final float fParam1;			//浮点参数1
	public final float fParam2;			//浮点参数2
	public final float fParam3;			//浮点参数3
	public final float[] arrfParam1;			//浮点数组参数1
	public final float[] arrfParam2;			//浮点数组参数2
	public final float[] arrfParam3;			//浮点数组参数3
	public final String strParam1;			//字符串参数1

	public ConfSceneTrigger(String sn, int triggerType, int nParam1, int nParam2, int nParam3, int[] arrnParam1, int[] arrnParam2, int[] arrnParam3, float fParam1, float fParam2, float fParam3, float[] arrfParam1, float[] arrfParam2, float[] arrfParam3, String strParam1) {
			this.sn = sn;		
			this.triggerType = triggerType;		
			this.nParam1 = nParam1;		
			this.nParam2 = nParam2;		
			this.nParam3 = nParam3;		
			this.arrnParam1 = arrnParam1;		
			this.arrnParam2 = arrnParam2;		
			this.arrnParam3 = arrnParam3;		
			this.fParam1 = fParam1;		
			this.fParam2 = fParam2;		
			this.fParam3 = fParam3;		
			this.arrfParam1 = arrfParam1;		
			this.arrfParam2 = arrfParam2;		
			this.arrfParam3 = arrfParam3;		
			this.strParam1 = strParam1;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfSceneTrigger> findAll() {
		return DATA.getList();
	}

	/**
	 * 通过SN获取数据
	 * @param sn
	 * @return
	 */
	public static ConfSceneTrigger get(String sn) {
		return DATA.getMap().get(sn);
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ConfSceneTrigger getBy(Object...params) {
		List<ConfSceneTrigger> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfSceneTrigger> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfSceneTrigger> utilBase(Object...params) {
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
		List<ConfSceneTrigger> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfSceneTrigger c : DATA.getList()) {
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
		Collections.sort(result, new Comparator<ConfSceneTrigger>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfSceneTrigger a, ConfSceneTrigger b) {
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
		public static final String sn = "sn";	//触发条件ID
		public static final String triggerType = "triggerType";	//触发条件类型
		public static final String nParam1 = "nParam1";	//整型参数1
		public static final String nParam2 = "nParam2";	//整型参数2
		public static final String nParam3 = "nParam3";	//整型参数3
		public static final String arrnParam1 = "arrnParam1";	//整型数组参数1
		public static final String arrnParam2 = "arrnParam2";	//整型数组参数2
		public static final String arrnParam3 = "arrnParam3";	//整型数组参数3
		public static final String fParam1 = "fParam1";	//浮点参数1
		public static final String fParam2 = "fParam2";	//浮点参数2
		public static final String fParam3 = "fParam3";	//浮点参数3
		public static final String arrfParam1 = "arrfParam1";	//浮点数组参数1
		public static final String arrfParam2 = "arrfParam2";	//浮点数组参数2
		public static final String arrfParam3 = "arrfParam3";	//浮点数组参数3
		public static final String strParam1 = "strParam1";	//字符串参数1
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static volatile Map<String, ConfSceneTrigger> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfSceneTrigger> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<String, ConfSceneTrigger> getMap() {
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
			Map<String, ConfSceneTrigger> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfSceneTrigger object = new ConfSceneTrigger(conf.getString("sn"), conf.getIntValue("triggerType"), conf.getIntValue("nParam1"), conf.getIntValue("nParam2"), 
				conf.getIntValue("nParam3"), parseIntArray(conf.getString("arrnParam1")), parseIntArray(conf.getString("arrnParam2")), parseIntArray(conf.getString("arrnParam3")), 
				conf.getFloatValue("fParam1"), conf.getFloatValue("fParam2"), conf.getFloatValue("fParam3"), parseFloatArray(conf.getString("arrfParam1")), 
				parseFloatArray(conf.getString("arrfParam2")), parseFloatArray(conf.getString("arrfParam3")), conf.getString("strParam1"));
				dataMap.put(conf.getString("sn"), object);
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
			String baseBath = ConfSceneTrigger.class.getResource("").getPath();
			File file = new File(baseBath + "json/ConfSceneTrigger.json");
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