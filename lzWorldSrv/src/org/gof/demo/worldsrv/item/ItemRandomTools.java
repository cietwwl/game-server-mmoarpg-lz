package org.gof.demo.worldsrv.item;

import java.util.List;
import java.util.Map;

import org.gof.core.support.RandomUtils;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.produce.ProduceWeightVo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

public class ItemRandomTools {
	/**
	 * 从randMap中随机出一个key值，其中value是概率权值
	 * @param json
	 * @return
	 */
	public static int randomWithJSONObject(Map<Integer, Integer>randMap) {
		
		//计算总权值
		int total = 0;
		for(int value : randMap.values()) {
			total += value;
		}
		
		//随机出总权值内的随机数
		int rand = RandomUtils.nextInt(total);
		
		//随机出最终的key
		int res = 0;
		for(Integer key : randMap.keySet()) {
			if(rand < randMap.get(key)) {
				res = key;
				break;
			}
			
			rand -= randMap.get(key);
		}
		
		return res;
	}
	
	public static ProduceVo randomWithJSONObject(List<ProduceWeightVo>randMap) {
		
		//计算总权值
		int total = 0;
		for(ProduceWeightVo value : randMap) {
			total += value.weight;
		}
		
		//随机出总权值内的随机数
		int rand = RandomUtils.nextInt(total);
		
		//随机出最终的key
		ProduceVo res = null;
		for(ProduceWeightVo vo : randMap) {
			if(rand < vo.weight) {
				res = new ProduceVo(vo.sn, vo.num);
				break;
			}
			
			rand -= vo.weight;
		}
		
		return res;
	}
	
	/**
	 * 从数组中随机出一个位置，其中每个元素都是该index位置的概率
	 * 格式为[100,100,100,80]
	 * @param json
	 * @return 随机抽中的数组的index
	 */
	public static int randomWithArray(String json) {
		JSONArray ja = JSON.parseArray(json);
		
		//计算总权值
		int total = 0;
		for(Object value : ja) {
			total += Utils.intValue(value.toString());
		}
		
		//随机出总权值内的随机数
		int rand = RandomUtils.nextInt(total);
		
		//随机出最终的索引
		int index = -1;
		for(int i = 0; i < ja.size(); i++) {
			if(rand <= ja.getIntValue(i)) {
				index = i;
				break;
			}
			
			rand -= ja.getIntValue(i);
		}
		
		return index;
	}
}
