package org.gof.demo.worldsrv.quest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.msg.Msg.DQuest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 任务的VO类，主要用于Json之间互转。
 */
public class QuestVO implements ISerilizable {
	public int sn;		
	public int targetProgress;			//目标进度
	public int nowProgress;				//当前进度
	public int status;					//状态
	public int type;					//任务类型
	
	public QuestVO(JSONObject jo) {
		this.sn = jo.getIntValue("sn");
		this.targetProgress = jo.getIntValue("tp");
		this.nowProgress = jo.getIntValue("np");
		this.status = jo.getIntValue("st");
		this.type = jo.getIntValue("t");
	}
	
	public QuestVO() {
	}
	
	/**
	 * 把Json转换为List
	 * @param json
	 * @return
	 */
	public static List<QuestVO> jsonToList(String json) {
		List<QuestVO> result = new ArrayList<QuestVO>();
		
		JSONArray ja = JSON.parseArray(json);
		for (int i = 0; i < ja.size(); i++) {
			QuestVO vo = new QuestVO(ja.getJSONObject(i));
			result.add(vo);
		}
		
		return result;
	}
	
	/**
	 * 将List转换为Json
	 * @param questDailyList
	 * @return
	 */
	public static String listToJson(List<QuestVO> questList){
		JSONArray ja = new JSONArray();
		
		for (QuestVO vo : questList) {
			JSONObject jo = new JSONObject();
			jo.put("sn", vo.sn);
			jo.put("tp", vo.targetProgress);
			jo.put("np", vo.nowProgress);
			jo.put("st", vo.status);
			jo.put("t", vo.type);
			
			ja.add(jo);
		}
		
		return ja.toJSONString();
	}
	
	/**
	 * 由VO拼装出简版DQuest消息
	 * @return
	 */
	public DQuest createMsg() {
		DQuest.Builder msg = DQuest.newBuilder();
		msg.setSn(sn);
		msg.setTargetProgress(targetProgress);
		msg.setNowProgress(nowProgress);
		msg.setStatus(status);
		return msg.build();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("sn", sn)
					.append("targetProgress", targetProgress)
					.append("nowProgress", nowProgress)
					.append("status", status)
					.append("type", type)
					.toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(targetProgress);
		out.write(nowProgress);
		out.write(status);
		out.write(type);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		targetProgress = in.read();
		nowProgress = in.read();
		status = in.read();
		type = in.read();
	}
}
