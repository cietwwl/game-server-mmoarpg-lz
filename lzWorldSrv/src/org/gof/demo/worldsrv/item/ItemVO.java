package org.gof.demo.worldsrv.item;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.msg.Msg.DItem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

/**
 * 为了减少toJSON存入数据库的字符串长度，toJSON和从JSON还原都按JSONArray来处理，游戏上线后不能删除旧的字段
 */
public class ItemVO implements ISerilizable {
	public int sn;		
	public int num;			//数量
	public int bind;			//绑定状态
	
	public ItemVO() {
		
	}
	
	public ItemVO(String json) {
		JSONArray ja = JSON.parseArray(json);
		this.sn = ja.getIntValue(0);
		this.num = ja.getIntValue(1);
		this.bind = ja.getIntValue(2);
	}
	
	public ItemVO(int sn, int num) {
		this.sn = sn;
		this.num = num;
		this.bind = ItemBagManager.BIND_NO;
	}
	
	
	public ItemVO(int sn, int num, int bind) {
		this.sn = sn;
		this.num = num;
		this.bind = bind;
	}
	
	public ItemVO(long id, int sn, int num, int bind) {
		this.sn = sn;
		this.num = num;
		this.bind = bind;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("sn", sn)
					.append("num", num)
					.append("bind", bind)
					.toString();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(sn);
		out.write(num);
		out.write(bind);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		sn = in.read();
		num = in.read();
		bind = in.read();
	}
	
	public JSON toJSON() {
		JSONArray ja = new JSONArray();
		ja.add(sn);
		ja.add(num);
		ja.add(bind);
		
		return ja;
	}
	
	/**
	 * 由ItemVO拼装出简版item消息
	 * @param itemSn
	 * @param num
	 * @return
	 */
	public DItem createMsg() {
		DItem.Builder msg = DItem.newBuilder();
		msg.setCode(sn);
		msg.setNum(num);
		msg.setBind(bind);
		
		return msg.build();
	}
}
