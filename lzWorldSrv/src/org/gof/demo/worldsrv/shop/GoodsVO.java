package org.gof.demo.worldsrv.shop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.msg.Msg.DGoods;
import org.gof.demo.worldsrv.msg.Msg.DShop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public class GoodsVO  implements ISerilizable {
	public int sn;		
	public int status;					//购买状态  0：未购买   1：已购买
	
	public GoodsVO(JSONObject jo) {
		this.sn = jo.getIntValue("sn");
		this.status = jo.getIntValue("status");
	}
	
	public GoodsVO() {
	}
	
	/**
	 * 把Json转换为List
	 * @param json
	 * @return
	 */
	public static List<GoodsVO> jsonToList(String json) {
		List<GoodsVO> result = new ArrayList<GoodsVO>();
		
		JSONArray ja = JSON.parseArray(json);
		for (int i = 0; i < ja.size(); i++) {
			GoodsVO vo = new GoodsVO(ja.getJSONObject(i));
			result.add(vo);
		}
		
		return result;
	}
	
	/**
	 * 将List转换为Json
	 * @param list
	 * @return
	 */
	public static String listToJson(List<GoodsVO> list){
		JSONArray ja = new JSONArray();
		if (list != null) {
			for (GoodsVO vo : list) {
				JSONObject jo = new JSONObject();
				jo.put("sn", vo.sn);
				jo.put("status", vo.status);
				
				ja.add(jo);
			}
		}
		return ja.toJSONString();
	}
	
	/**
	 * 由VO拼装出简版DQuest消息
	 * @return
	 */
	public DGoods createMsg() {
		DGoods.Builder msg = DGoods.newBuilder();
		msg.setSn(sn);
		msg.setStatus(status);
		return msg.build();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("sn", sn)
					.append("status", status)
					.toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(status);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		status = in.read();
	}
}
