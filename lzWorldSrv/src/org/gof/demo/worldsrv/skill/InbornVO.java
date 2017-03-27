package org.gof.demo.worldsrv.skill;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.worldsrv.msg.Msg.DInborn;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class InbornVO {
	
	public int sn;		//SN
	public int pos;		//从1开始，如果是0说明木有装配
	public int level;		//级别
	public boolean canUpgrade;		//是否能升级
	
	/**
	 * 构造方法，所有函数都有
	 * @param sn
	 * @param pos
	 * @param level
	 * @param canUpgrade
	 */
	public InbornVO(int sn, int pos, int level, boolean canUpgrade) {
		this.sn = sn;
		this.pos = pos;
		this.level = level;
		this.canUpgrade = canUpgrade;
	}
	
	/**
	 * 构造方法，传入字符串
	 * @param json
	 */
	public InbornVO(String json) {
		new InbornVO(Utils.toJSONObject(json));
	}
	
	/**
	 * 构造方法，传入JSONObject
	 * @param jo
	 */
	public InbornVO(JSONObject jo) {
		this.sn = jo.getIntValue("sn");
		this.pos = jo.getIntValue("pos");
		this.level = jo.getIntValue("level");
	}
	
	/**
	 * 把List构建成String 用于存储
	 * @param list
	 * @return
	 */
	public static String listToString(List<InbornVO> list) {
		JSONArray ja = new JSONArray();
		for (InbornVO inbornVO : list) {
			JSONObject jo = new JSONObject();
			jo.put("sn", inbornVO.sn);
			jo.put("pos", inbornVO.pos);
			jo.put("level", inbornVO.level);
			ja.add(jo);
		}
		
		return ja.toJSONString();
	} 
	
	/**
	 * 根据SN获取InbornVO
	 * @param inborns
	 * @param sn
	 * @return
	 */
	public static InbornVO getBySn(String inborns, int sn) {
		JSONArray ja = Utils.toJSONArray(inborns);
		for (int i = 0; i < ja.size(); i++) {
			InbornVO vo = new InbornVO(ja.getJSONObject(i));
			
			if(vo.sn == sn) return vo;
		}
		
		return null;
	}
	
	/**
	 * 根据JSON获取List
	 * @param inborns
	 * @return
	 */
	public static List<InbornVO> getList(String inborns) {
		List<InbornVO> res = new ArrayList<InbornVO>();
		
		JSONArray ja = Utils.toJSONArray(inborns);
		for (int i = 0; i < ja.size(); i++) {
			InbornVO vo = new InbornVO(ja.getJSONObject(i));
			res.add(vo);
		}
		
		return res;
	}
	
	/**
	 * 装备或者添加一个天赋
	 * @param sn
	 * @param level
	 * @return
	 */
	public static void addInborn(int sn, int pos, int level, UnitObject unitObj) {
		List<InbornVO> list = InbornVO.getList(unitObj.getInborns());
		
		//判断当前天赋是否已经学习
		boolean has = false;
		for (InbornVO vo : list) {
			if(vo.sn == sn){
				has = true;
				vo.level = level;
				vo.pos = pos;
				break;
			}
		}
		
		//如果没找到就加一个
		if(!has) {
			list.add(new InbornVO(sn, pos, level, false));
		}
		
		//记录到玩家身上
		String newJSON = InbornVO.listToString(list);
		unitObj.getUnit().setInborn(newJSON);
	}
	
	/**
	 * 把单个VO对象转换为DInborn格式
	 * @return
	 */
	public static DInborn voToMsg(InbornVO vo) {
		DInborn.Builder dInborn = DInborn.newBuilder();
		dInborn.setInbornSn(vo.sn);
		dInborn.setPos(vo.pos);
		dInborn.setInbronLevel(vo.level);

		return dInborn.build();
	}
	
	/**
	 * 根据JSON返回DInborn
	 * @param json
	 * @return
	 */
	public static List<DInborn> getMsgList(UnitObject unitObj){
		List<DInborn> res = new ArrayList<DInborn>();
		
		JSONArray ja = Utils.toJSONArray(unitObj.getInborns());
		for (int i = 0; i < ja.size(); i++) {
			InbornVO vo = new InbornVO(ja.getJSONObject(i));
			
			DInborn.Builder dInborn = DInborn.newBuilder();
			dInborn.setInbornSn(vo.sn);
			dInborn.setPos(vo.pos);
			dInborn.setInbronLevel(vo.level);
			res.add(dInborn.build());
		}
		
		return res;
	}
}
