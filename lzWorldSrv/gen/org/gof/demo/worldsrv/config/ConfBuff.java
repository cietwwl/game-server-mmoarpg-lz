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
 * Buff|Buff 
 * Buff.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfBuff {
	public final int sn;			//Buff sn
	public final String name;			//名称
	public final String effectBuff;			//持续特效Act
	public final String effectOnce;			//单次特效Act
	public final String explain;			//文字说明
	public final int type;			//类型(可用于覆盖)
	public final String dispelSn;			//被驱散组SN
	public final int multiple;			//叠加参数，0不叠加，1时间叠加
	public final int priority;			//优先级
	public final int msgShowType;			//传播范围，0：不给前端发消息；1：只给自己发；2：给所有能看见的玩家发
	public final boolean isReserveDied;			//死后是否保留
	public final boolean isReserveOffLine;			//下线是否保留
	public final boolean isPeriod;			//是否周期触发
	public final int timePeriod;			//周期触发时间
	public final int timeExistOnline;			// 在线作用时间（ms），Buff在玩家在线时的作用时间，为0则表示buff的移除不受该时间的影响
	public final int timeExist;			// 最大存在时间ms，就是指该buff最多存在的时间，无论玩家是否在线，为0则表示buff的移除不受该时间的影响
	public final int timeDelay;			// 延迟时间，毫秒(ms)
	public final boolean feedback;			//反馈，如果为真那么把加到别人身上的buff。数值取负加到自己身上
	public final String[] propName;			//atkSpeed以1000
	public final float[] propValue;			//移动速度基础值：500（5m/s）；填加减值攻击速度基础值：1000，填加减值；其他为10000是100%
	public final float[] levelParam;			//升级参数， 技能等级 * 参数 +到前面的数值
	public final String defProp;			//自己的属性影响
	public final float defWeight;			//自己属性的权重比列 如攻击是100 这里填2属性就是 前面的value + 100 / 2   写-2就是减
	public final String fireProp;			//释放者的属性
	public final float fireWeight;			//释放者属性的权重比列 如攻击是100 这里填2属性就是 前面的value + 100 / 2   写-2就是减

	public ConfBuff(int sn, String name, String effectBuff, String effectOnce, String explain, int type, String dispelSn, int multiple, int priority, int msgShowType, boolean isReserveDied, boolean isReserveOffLine, boolean isPeriod, int timePeriod, int timeExistOnline, int timeExist, int timeDelay, boolean feedback, String[] propName, float[] propValue, float[] levelParam, String defProp, float defWeight, String fireProp, float fireWeight) {
			this.sn = sn;		
			this.name = name;		
			this.effectBuff = effectBuff;		
			this.effectOnce = effectOnce;		
			this.explain = explain;		
			this.type = type;		
			this.dispelSn = dispelSn;		
			this.multiple = multiple;		
			this.priority = priority;		
			this.msgShowType = msgShowType;		
			this.isReserveDied = isReserveDied;		
			this.isReserveOffLine = isReserveOffLine;		
			this.isPeriod = isPeriod;		
			this.timePeriod = timePeriod;		
			this.timeExistOnline = timeExistOnline;		
			this.timeExist = timeExist;		
			this.timeDelay = timeDelay;		
			this.feedback = feedback;		
			this.propName = propName;		
			this.propValue = propValue;		
			this.levelParam = levelParam;		
			this.defProp = defProp;		
			this.defWeight = defWeight;		
			this.fireProp = fireProp;		
			this.fireWeight = fireWeight;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfBuff> findAll() {
		return DATA.getList();
	}

	/**
	 * 通过SN获取数据
	 * @param sn
	 * @return
	 */
	public static ConfBuff get(Integer sn) {
		return DATA.getMap().get(sn);
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ConfBuff getBy(Object...params) {
		List<ConfBuff> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfBuff> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfBuff> utilBase(Object...params) {
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
		List<ConfBuff> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfBuff c : DATA.getList()) {
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
		Collections.sort(result, new Comparator<ConfBuff>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfBuff a, ConfBuff b) {
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
		public static final String sn = "sn";	//Buff sn
		public static final String name = "name";	//名称
		public static final String effectBuff = "effectBuff";	//持续特效Act
		public static final String effectOnce = "effectOnce";	//单次特效Act
		public static final String explain = "explain";	//文字说明
		public static final String type = "type";	//类型(可用于覆盖)
		public static final String dispelSn = "dispelSn";	//被驱散组SN
		public static final String multiple = "multiple";	//叠加参数，0不叠加，1时间叠加
		public static final String priority = "priority";	//优先级
		public static final String msgShowType = "msgShowType";	//传播范围，0：不给前端发消息；1：只给自己发；2：给所有能看见的玩家发
		public static final String isReserveDied = "isReserveDied";	//死后是否保留
		public static final String isReserveOffLine = "isReserveOffLine";	//下线是否保留
		public static final String isPeriod = "isPeriod";	//是否周期触发
		public static final String timePeriod = "timePeriod";	//周期触发时间
		public static final String timeExistOnline = "timeExistOnline";	// 在线作用时间（ms），Buff在玩家在线时的作用时间，为0则表示buff的移除不受该时间的影响
		public static final String timeExist = "timeExist";	// 最大存在时间ms，就是指该buff最多存在的时间，无论玩家是否在线，为0则表示buff的移除不受该时间的影响
		public static final String timeDelay = "timeDelay";	// 延迟时间，毫秒(ms)
		public static final String feedback = "feedback";	//反馈，如果为真那么把加到别人身上的buff。数值取负加到自己身上
		public static final String propName = "propName";	//atkSpeed以1000
		public static final String propValue = "propValue";	//移动速度基础值：500（5m/s）；填加减值攻击速度基础值：1000，填加减值；其他为10000是100%
		public static final String levelParam = "levelParam";	//升级参数， 技能等级 * 参数 +到前面的数值
		public static final String defProp = "defProp";	//自己的属性影响
		public static final String defWeight = "defWeight";	//自己属性的权重比列 如攻击是100 这里填2属性就是 前面的value + 100 / 2   写-2就是减
		public static final String fireProp = "fireProp";	//释放者的属性
		public static final String fireWeight = "fireWeight";	//释放者属性的权重比列 如攻击是100 这里填2属性就是 前面的value + 100 / 2   写-2就是减
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static volatile Map<Integer, ConfBuff> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfBuff> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfBuff> getMap() {
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
			Map<Integer, ConfBuff> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfBuff object = new ConfBuff(conf.getIntValue("sn"), conf.getString("name"), conf.getString("effectBuff"), conf.getString("effectOnce"), 
				conf.getString("explain"), conf.getIntValue("type"), conf.getString("dispelSn"), conf.getIntValue("multiple"), 
				conf.getIntValue("priority"), conf.getIntValue("msgShowType"), conf.getBooleanValue("isReserveDied"), conf.getBooleanValue("isReserveOffLine"), 
				conf.getBooleanValue("isPeriod"), conf.getIntValue("timePeriod"), conf.getIntValue("timeExistOnline"), conf.getIntValue("timeExist"), 
				conf.getIntValue("timeDelay"), conf.getBooleanValue("feedback"), parseStringArray(conf.getString("propName")), parseFloatArray(conf.getString("propValue")), 
				parseFloatArray(conf.getString("levelParam")), conf.getString("defProp"), conf.getFloatValue("defWeight"), conf.getString("fireProp"), 
				conf.getFloatValue("fireWeight"));
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
			String baseBath = ConfBuff.class.getResource("").getPath();
			File file = new File(baseBath + "json/ConfBuff.json");
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