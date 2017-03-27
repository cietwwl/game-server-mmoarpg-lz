package org.gof.demo.worldsrv.dailyliveness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.msg.Msg.DLiveness;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 每日活跃度的VO类，主要用于json之间互转。
 */
public class LivenessVO implements ISerilizable {
	public int sn;		
	public int type;					//类型
	public int currNum;					//当前完成次数
	public int status;					//状态
	public LivenessVO(JSONObject jo) {
		this.sn = jo.getIntValue("sn");
		this.type = jo.getIntValue("tp");
		this.currNum = jo.getIntValue("currNum");
		this.status = jo.getIntValue("st");
	}
	
	public LivenessVO() {
	}
	
	/**
	 * 把json转换为List
	 * @param json
	 * @return
	 */
	public static List<LivenessVO> jsonToList(String json) {
		List<LivenessVO> result = new ArrayList<LivenessVO>();
		if(json == null || "".equals(json) || "{}".equals(json)){
			return result;
		}
		JSONArray ja = JSON.parseArray(json);
		for (int i = 0; i < ja.size(); i++) {
			LivenessVO vo = new LivenessVO(ja.getJSONObject(i));
			result.add(vo);
		}
		
		return result;
	}
	
	/**
	 * 将List转换为json
	 * @param questDailyList
	 * @return
	 */
	public static String listToJson(List<LivenessVO> questList){
		JSONArray ja = new JSONArray();
		
		for (LivenessVO vo : questList) {
			JSONObject jo = new JSONObject();
			jo.put("sn", vo.sn);
			jo.put("tp", vo.type);
			jo.put("currNum", vo.currNum);
			jo.put("st", vo.status);
			ja.add(jo);
		}
		return ja.toJSONString();
	}
	
	/**
	 * 由VO拼装出简版DQuest消息
	 * @return
	 */
	public DLiveness createMsg() {
		DLiveness.Builder msg = DLiveness.newBuilder();
		msg.setSn(sn);
		msg.setTp(type);
		msg.setCurrNum(currNum);
		msg.setStatus(status);
		return msg.build();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("sn", sn)
					.append("tp",type)
					.append("currNum", currNum)
					.append("st", status)
					.toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(type);
		out.write(currNum);
		out.write(status);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		type = in.read();
		currNum = in.read();
		status = in.read();
	}
}
