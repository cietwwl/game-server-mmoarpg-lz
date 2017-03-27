package org.gof.demo.worldsrv.shop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.msg.Msg.DQuest;
import org.gof.demo.worldsrv.msg.Msg.DShop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 商店的VO类，主要用于Json之间互转。
 */
public class ShopVO implements ISerilizable {
	public int sn;		
	public int refreshCount;			//付费刷新次数
	public long refreshTime;			//上次刷新时间
	public int status;					//解锁状态  -1：未解锁   -2：临时解锁   -3：永久解锁   -4：永久开启
	public long openTime;				//临时解锁时间
	public int openCount;				//临时解锁次数
	public List<GoodsVO> shelfs = new ArrayList<GoodsVO>();		//商品列表
	
	public ShopVO(JSONObject jo) {
		this.sn = jo.getIntValue("sn");
		this.refreshCount = jo.getIntValue("refreshCount");
		this.refreshTime = jo.getLongValue("refreshTime");
		this.status = jo.getIntValue("status");
		this.openTime = jo.getLongValue("openTime");
		this.openCount = jo.getIntValue("openCount");
		JSONArray goodsJson = JSON.parseArray(jo.getString("shelfs"));
		shelfs = GoodsVO.jsonToList(goodsJson.toJSONString());
	}
	
	public ShopVO() {
	}
	
	/**
	 * 把Json转换为List
	 * @param json
	 * @return
	 */
	public static List<ShopVO> jsonToList(String json) {
		List<ShopVO> result = new ArrayList<ShopVO>();
		
		JSONArray ja = JSON.parseArray(json);
		for (int i = 0; i < ja.size(); i++) {
			ShopVO vo = new ShopVO(ja.getJSONObject(i));
			result.add(vo);
		}
		
		return result;
	}
	
	/**
	 * 将List转换为Json
	 * @param list
	 * @return
	 */
	public static String listToJson(List<ShopVO> list){
		JSONArray ja = new JSONArray();
		
		for (ShopVO vo : list) {
			JSONObject jo = new JSONObject();
			jo.put("sn", vo.sn);
			jo.put("refreshCount", vo.refreshCount);
			jo.put("refreshTime", vo.refreshTime);
			jo.put("status", vo.status);
			jo.put("openTime", vo.openTime);
			jo.put("openCount", vo.openCount);
			jo.put("shelfs", GoodsVO.listToJson(vo.shelfs));

			ja.add(jo);
		}
		
		return ja.toJSONString();
	}
	
	/**
	 * 由VO拼装出简版DQuest消息
	 * @return
	 */
	public DShop createMsg() {
		DShop.Builder msg = DShop.newBuilder();
		msg.setSn(sn);
		msg.setRefreshCount(refreshCount);
		for (GoodsVO vo : shelfs) {
			msg.addShelfs(vo.createMsg());
		}
		return msg.build();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("sn", sn)
					.append("refreshCount", refreshCount)
					.append("refreshTime", refreshTime)
					.append("status", status)
					.append("openTime", openTime)
					.append("openCount", openCount)
					.append("shelfs", shelfs)
					.toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(refreshCount);
		out.write(refreshTime);
		out.write(status);
		out.write(openTime);
		out.write(openCount);
		out.write(shelfs);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		refreshCount = in.read();
		refreshTime = in.read();
		status = in.read();
		openTime = in.read();
		openCount = in.read();
		shelfs = in.read();
	}
}
