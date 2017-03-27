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
 * 武将配置|CharacterGeneral 
 * CharacterGeneral.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfCharacterGeneral {
	public final String sn;			//武将SN
	public final String name;			//武将姓名
	public final int sex;			//性别
	public final int profession;			//职业
	public final String modelSn;			//模型sn
	public final int ai;			//AI
	public final int skillGroupSn;			//技能组SN
	public final int[] equipmnet;			//伙伴装备
	public final int fragSn;			//对应碎片sn
	public final int needFragNum;			//招募需要碎片的数量
	public final int needCoin;			//招募需要的银两
	public final boolean canRecriuit;			//是否可被招募
	public final boolean isRare;			//是否为稀有伙伴
	public final int country;			//武将国家
	public final int initStar;			//初始星级
	public final int initQuality;			//初始品质
	public final int talent;			//资质
	public final int level;			//初始等级
	public final int evoLevel;			//进阶数
	public final int type;			//伙伴类型
	public final int stype;			//特殊类型
	public final String[] properties;			//属性类别
	public final float[] value;			//属性值
	public final int[] relation;			//羁绊
	public final int order;			//站位顺序
	public final int growLevelSn;			//升级成长

	public ConfCharacterGeneral(String sn, String name, int sex, int profession, String modelSn, int ai, int skillGroupSn, int[] equipmnet, int fragSn, int needFragNum, int needCoin, boolean canRecriuit, boolean isRare, int country, int initStar, int initQuality, int talent, int level, int evoLevel, int type, int stype, String[] properties, float[] value, int[] relation, int order, int growLevelSn) {
			this.sn = sn;		
			this.name = name;		
			this.sex = sex;		
			this.profession = profession;		
			this.modelSn = modelSn;		
			this.ai = ai;		
			this.skillGroupSn = skillGroupSn;		
			this.equipmnet = equipmnet;		
			this.fragSn = fragSn;		
			this.needFragNum = needFragNum;		
			this.needCoin = needCoin;		
			this.canRecriuit = canRecriuit;		
			this.isRare = isRare;		
			this.country = country;		
			this.initStar = initStar;		
			this.initQuality = initQuality;		
			this.talent = talent;		
			this.level = level;		
			this.evoLevel = evoLevel;		
			this.type = type;		
			this.stype = stype;		
			this.properties = properties;		
			this.value = value;		
			this.relation = relation;		
			this.order = order;		
			this.growLevelSn = growLevelSn;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfCharacterGeneral> findAll() {
		return DATA.getList();
	}

	/**
	 * 通过SN获取数据
	 * @param sn
	 * @return
	 */
	public static ConfCharacterGeneral get(String sn) {
		return DATA.getMap().get(sn);
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ConfCharacterGeneral getBy(Object...params) {
		List<ConfCharacterGeneral> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfCharacterGeneral> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfCharacterGeneral> utilBase(Object...params) {
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
		List<ConfCharacterGeneral> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfCharacterGeneral c : DATA.getList()) {
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
		Collections.sort(result, new Comparator<ConfCharacterGeneral>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfCharacterGeneral a, ConfCharacterGeneral b) {
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
		public static final String sn = "sn";	//武将SN
		public static final String name = "name";	//武将姓名
		public static final String sex = "sex";	//性别
		public static final String profession = "profession";	//职业
		public static final String modelSn = "modelSn";	//模型sn
		public static final String ai = "ai";	//AI
		public static final String skillGroupSn = "skillGroupSn";	//技能组SN
		public static final String equipmnet = "equipmnet";	//伙伴装备
		public static final String fragSn = "fragSn";	//对应碎片sn
		public static final String needFragNum = "needFragNum";	//招募需要碎片的数量
		public static final String needCoin = "needCoin";	//招募需要的银两
		public static final String canRecriuit = "canRecriuit";	//是否可被招募
		public static final String isRare = "isRare";	//是否为稀有伙伴
		public static final String country = "country";	//武将国家
		public static final String initStar = "initStar";	//初始星级
		public static final String initQuality = "initQuality";	//初始品质
		public static final String talent = "talent";	//资质
		public static final String level = "level";	//初始等级
		public static final String evoLevel = "evoLevel";	//进阶数
		public static final String type = "type";	//伙伴类型
		public static final String stype = "stype";	//特殊类型
		public static final String properties = "properties";	//属性类别
		public static final String value = "value";	//属性值
		public static final String relation = "relation";	//羁绊
		public static final String order = "order";	//站位顺序
		public static final String growLevelSn = "growLevelSn";	//升级成长
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static volatile Map<String, ConfCharacterGeneral> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfCharacterGeneral> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<String, ConfCharacterGeneral> getMap() {
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
			Map<String, ConfCharacterGeneral> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfCharacterGeneral object = new ConfCharacterGeneral(conf.getString("sn"), conf.getString("name"), conf.getIntValue("sex"), conf.getIntValue("profession"), 
				conf.getString("modelSn"), conf.getIntValue("ai"), conf.getIntValue("skillGroupSn"), parseIntArray(conf.getString("equipmnet")), 
				conf.getIntValue("fragSn"), conf.getIntValue("needFragNum"), conf.getIntValue("needCoin"), conf.getBooleanValue("canRecriuit"), 
				conf.getBooleanValue("isRare"), conf.getIntValue("country"), conf.getIntValue("initStar"), conf.getIntValue("initQuality"), 
				conf.getIntValue("talent"), conf.getIntValue("level"), conf.getIntValue("evoLevel"), conf.getIntValue("type"), 
				conf.getIntValue("stype"), parseStringArray(conf.getString("properties")), parseFloatArray(conf.getString("value")), parseIntArray(conf.getString("relation")), 
				conf.getIntValue("order"), conf.getIntValue("growLevelSn"));
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
			String baseBath = ConfCharacterGeneral.class.getResource("").getPath();
			File file = new File(baseBath + "json/ConfCharacterGeneral.json");
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