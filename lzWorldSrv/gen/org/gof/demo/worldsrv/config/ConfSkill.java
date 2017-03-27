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
 * 技能|Skill 
 * Skill.xlsx
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ConfSkill {
	public final int sn;			//技能SN
	public final int snChange;			//技能可以跟换的SN
	public final boolean active;			//主动被动
	public final int type;			//0--普通技能， 1--绝招，2--杀招，3--普通攻击
	public final int targetType;			//目标类型
	public final int targetType2;			//技能触发点类型，ID同左边，仅在触发点与目标不同时填写（例如，需要在自己身上释放一个攻击单体目标的Dot）,填写此栏位后，右边（clickType)栏位必须为2
	public final int clickType;			//鼠标点选操作类型(1:角色， 2：坐标,3:自己
	public final boolean hit;			//技能是否必中
	public final int atkType;			//攻击类型1是魔法2是物理
	public final boolean canInterrupt;			//可以打断别人
	public final boolean canInterrupted;			//可以被打断
	public final boolean isCharge;			//技能是否是蓄力技能
	public final int[] nextChainSn;			//连击的后续技能 如拍拍熊的5连击 在第1个技能写后面4个
	public final int nextChainID;			//连续技下一招的ID
	public final float range;			//实际射程（m)
	public final float rangeMin;			//最小射程m
	public final float rangeMax;			//最大射程m
	public final int blackTime;			//黑屏时间
	public final int[] shakeFront;			//前摇时间
	public final int[] casting;			//激发时间
	public final int[] shakeBack;			//后摇时间
	public final int[] coolTime;			//能冷时间
	public final int[] comboTime;			//连击时间
	public final int combo;			//连击次数 为0 或者不填写表示此技能无连击
	public final int rageAttack;			//攻击获得的怒气
	public final int rageKill;			//杀死对方获得的怒气
	public final int rageBeAttack;			//被攻击获得的怒气
	public final boolean enterScene;			//开场技能
	public final boolean canMove;			//可移动
	public final int backDis;			//后退距离cm单位
	public final int[] effects;			//效果总汇
	public final int combat;			//战斗力
	public final int baseCost;			//技能升级基础花费
	public final int plusCost;			//每级增加花费
	public final int activeType;			//激活类型
	public final int position;			//技能在前端的位置
	public final int levelCut;			//与武将级别差值
	public final String[] relGeneralSn;			//羁绊技能激活需要的伙伴SN
	public final int levelLimit;			//等级限制

	public ConfSkill(int sn, int snChange, boolean active, int type, int targetType, int targetType2, int clickType, boolean hit, int atkType, boolean canInterrupt, boolean canInterrupted, boolean isCharge, int[] nextChainSn, int nextChainID, float range, float rangeMin, float rangeMax, int blackTime, int[] shakeFront, int[] casting, int[] shakeBack, int[] coolTime, int[] comboTime, int combo, int rageAttack, int rageKill, int rageBeAttack, boolean enterScene, boolean canMove, int backDis, int[] effects, int combat, int baseCost, int plusCost, int activeType, int position, int levelCut, String[] relGeneralSn, int levelLimit) {
			this.sn = sn;		
			this.snChange = snChange;		
			this.active = active;		
			this.type = type;		
			this.targetType = targetType;		
			this.targetType2 = targetType2;		
			this.clickType = clickType;		
			this.hit = hit;		
			this.atkType = atkType;		
			this.canInterrupt = canInterrupt;		
			this.canInterrupted = canInterrupted;		
			this.isCharge = isCharge;		
			this.nextChainSn = nextChainSn;		
			this.nextChainID = nextChainID;		
			this.range = range;		
			this.rangeMin = rangeMin;		
			this.rangeMax = rangeMax;		
			this.blackTime = blackTime;		
			this.shakeFront = shakeFront;		
			this.casting = casting;		
			this.shakeBack = shakeBack;		
			this.coolTime = coolTime;		
			this.comboTime = comboTime;		
			this.combo = combo;		
			this.rageAttack = rageAttack;		
			this.rageKill = rageKill;		
			this.rageBeAttack = rageBeAttack;		
			this.enterScene = enterScene;		
			this.canMove = canMove;		
			this.backDis = backDis;		
			this.effects = effects;		
			this.combat = combat;		
			this.baseCost = baseCost;		
			this.plusCost = plusCost;		
			this.activeType = activeType;		
			this.position = position;		
			this.levelCut = levelCut;		
			this.relGeneralSn = relGeneralSn;		
			this.levelLimit = levelLimit;		
	}

	public static void reLoad() {
		DATA._init();
	}
	
	/**
	 * 获取全部数据
	 * @return
	 */
	public static Collection<ConfSkill> findAll() {
		return DATA.getList();
	}

	/**
	 * 通过SN获取数据
	 * @param sn
	 * @return
	 */
	public static ConfSkill get(Integer sn) {
		return DATA.getMap().get(sn);
	}

	/**
	 * 通过属性获取单条数据
	 * @param params
	 * @return
	 */
	public static ConfSkill getBy(Object...params) {
		List<ConfSkill> list = utilBase(params);
		
		if(list.isEmpty()) return null;
		else return list.get(0);
	}
	
	/**
	 * 通过属性获取数据集合
	 * @param params
	 * @return
	 */
	public static List<ConfSkill> findBy(Object...params) {
		return utilBase(params);
	}
	
	/**
	 * 通过属性获取数据集合 支持排序
	 * @param params
	 * @return
	 */
	public static List<ConfSkill> utilBase(Object...params) {
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
		List<ConfSkill> result = new ArrayList<>();
		
		try {
			//通过条件获取结果
			for(ConfSkill c : DATA.getList()) {
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
		Collections.sort(result, new Comparator<ConfSkill>() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public int compare(ConfSkill a, ConfSkill b) {
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
		public static final String sn = "sn";	//技能SN
		public static final String snChange = "snChange";	//技能可以跟换的SN
		public static final String active = "active";	//主动被动
		public static final String type = "type";	//0--普通技能， 1--绝招，2--杀招，3--普通攻击
		public static final String targetType = "targetType";	//目标类型
		public static final String targetType2 = "targetType2";	//技能触发点类型，ID同左边，仅在触发点与目标不同时填写（例如，需要在自己身上释放一个攻击单体目标的Dot）,填写此栏位后，右边（clickType)栏位必须为2
		public static final String clickType = "clickType";	//鼠标点选操作类型(1:角色， 2：坐标,3:自己
		public static final String hit = "hit";	//技能是否必中
		public static final String atkType = "atkType";	//攻击类型1是魔法2是物理
		public static final String canInterrupt = "canInterrupt";	//可以打断别人
		public static final String canInterrupted = "canInterrupted";	//可以被打断
		public static final String isCharge = "isCharge";	//技能是否是蓄力技能
		public static final String nextChainSn = "nextChainSn";	//连击的后续技能 如拍拍熊的5连击 在第1个技能写后面4个
		public static final String nextChainID = "nextChainID";	//连续技下一招的ID
		public static final String range = "range";	//实际射程（m)
		public static final String rangeMin = "rangeMin";	//最小射程m
		public static final String rangeMax = "rangeMax";	//最大射程m
		public static final String blackTime = "blackTime";	//黑屏时间
		public static final String shakeFront = "shakeFront";	//前摇时间
		public static final String casting = "casting";	//激发时间
		public static final String shakeBack = "shakeBack";	//后摇时间
		public static final String coolTime = "coolTime";	//能冷时间
		public static final String comboTime = "comboTime";	//连击时间
		public static final String combo = "combo";	//连击次数 为0 或者不填写表示此技能无连击
		public static final String rageAttack = "rageAttack";	//攻击获得的怒气
		public static final String rageKill = "rageKill";	//杀死对方获得的怒气
		public static final String rageBeAttack = "rageBeAttack";	//被攻击获得的怒气
		public static final String enterScene = "enterScene";	//开场技能
		public static final String canMove = "canMove";	//可移动
		public static final String backDis = "backDis";	//后退距离cm单位
		public static final String effects = "effects";	//效果总汇
		public static final String combat = "combat";	//战斗力
		public static final String baseCost = "baseCost";	//技能升级基础花费
		public static final String plusCost = "plusCost";	//每级增加花费
		public static final String activeType = "activeType";	//激活类型
		public static final String position = "position";	//技能在前端的位置
		public static final String levelCut = "levelCut";	//与武将级别差值
		public static final String relGeneralSn = "relGeneralSn";	//羁绊技能激活需要的伙伴SN
		public static final String levelLimit = "levelLimit";	//等级限制
	}

	/**
	 * 数据集
	 * 单独提出来也是为了做数据延迟初始化
	 * 避免启动遍历类时，触发了static静态块
	 */
	@SuppressWarnings({"unused"})
	private static final class DATA {
		//全部数据
		private static volatile Map<Integer, ConfSkill> _map;
		
		/**
		 * 获取数据的值集合
		 * @return
		 */
		public static Collection<ConfSkill> getList() {
			return getMap().values();
		}
		
		/**
		 * 获取Map类型数据集合
		 * @return
		 */
		public static Map<Integer, ConfSkill> getMap() {
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
			Map<Integer, ConfSkill> dataMap = new HashMap<>();
			
			//JSON数据
			String confJSON = _readConfFile();
			if(StringUtils.isBlank(confJSON)) return;
			
			//填充实体数据
			JSONArray confs = (JSONArray)JSONArray.parse(confJSON);
			for(int i = 0 ; i < confs.size() ; i++){
				JSONObject conf = confs.getJSONObject(i);
				ConfSkill object = new ConfSkill(conf.getIntValue("sn"), conf.getIntValue("snChange"), conf.getBooleanValue("active"), conf.getIntValue("type"), 
				conf.getIntValue("targetType"), conf.getIntValue("targetType2"), conf.getIntValue("clickType"), conf.getBooleanValue("hit"), 
				conf.getIntValue("atkType"), conf.getBooleanValue("canInterrupt"), conf.getBooleanValue("canInterrupted"), conf.getBooleanValue("isCharge"), 
				parseIntArray(conf.getString("nextChainSn")), conf.getIntValue("nextChainID"), conf.getFloatValue("range"), conf.getFloatValue("rangeMin"), 
				conf.getFloatValue("rangeMax"), conf.getIntValue("blackTime"), parseIntArray(conf.getString("shakeFront")), parseIntArray(conf.getString("casting")), 
				parseIntArray(conf.getString("shakeBack")), parseIntArray(conf.getString("coolTime")), parseIntArray(conf.getString("comboTime")), conf.getIntValue("combo"), 
				conf.getIntValue("rageAttack"), conf.getIntValue("rageKill"), conf.getIntValue("rageBeAttack"), conf.getBooleanValue("enterScene"), 
				conf.getBooleanValue("canMove"), conf.getIntValue("backDis"), parseIntArray(conf.getString("effects")), conf.getIntValue("combat"), 
				conf.getIntValue("baseCost"), conf.getIntValue("plusCost"), conf.getIntValue("activeType"), conf.getIntValue("position"), 
				conf.getIntValue("levelCut"), parseStringArray(conf.getString("relGeneralSn")), conf.getIntValue("levelLimit"));
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
			String baseBath = ConfSkill.class.getResource("").getPath();
			File file = new File(baseBath + "json/ConfSkill.json");
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