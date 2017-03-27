package org.gof.demo.worldsrv.general;

import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.config.ConfItemData;

import com.alibaba.fastjson.JSONArray;


public class GeneralEquipment {
	
	public JSONArray equipInfo;   //伙伴装备信息 
	
	
	public GeneralEquipment(String info){
		this.equipInfo = Utils.toJSONArray(info);
	}
	
	public GeneralEquipment(int[] equipIds){
		this.equipInfo = Utils.toJSONArray("[]");
		for(int key : equipIds){
			equipInfo.add(key);  
		}
	}
	
	
	public JSONArray getEequipInfo(){
		return this.equipInfo;
	}
	
	/**
	 * 获取伙伴装备品质
	 * @param equipSn
	 * @return
	 */
	public int getEquipQualityLv(int equipSn){
		ConfItemData cid = ConfItemData.get(equipSn);
		return cid.quality;
	}
	
	/**
	 * 修改伙伴装备品质
	 * @param equipSn
	 * @param level
	 */
	public void repalceEquipQualityLv(int beforeEquipSn, int repEquipSn){
        int index = equipInfo.indexOf(beforeEquipSn);
        if(index >= 0){
        	equipInfo.remove(index);
        	equipInfo.add(index, repEquipSn);
        }else{
        	if(!equipInfo.contains(repEquipSn)){
                equipInfo.add(repEquipSn);
        	}
        }
	}
	
	/**
	 * 检查装备品质级别
	 * @param qulityLimit
	 * @return
	 */
	public boolean checkEquipQuality(int qulityLimit){
		for(int i=0; i < equipInfo.size(); i++){
			int equipSn = equipInfo.getIntValue(i);
			ConfItemData cid = ConfItemData.get(equipSn);
			if(cid.quality < qulityLimit){
				return false;
			}
		}
		return true;
	}

	/**
	 * 对象转换成JSON
	 * @return
	 */
	public String equipInfoJson(){
		return Utils.toJSONString(equipInfo);
	}
		
}
