package org.gof.demo.worldsrv.friend;

import java.io.IOException;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.RecordTransient;
import org.gof.core.interfaces.ISerilizable;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.human.HumanGlobalInfo;
import org.gof.demo.worldsrv.msg.Msg.DFriendInfo;

public class FriendObject implements ISerilizable {
	
	public long  id;              		//用户Id
	public String modelSn ;             //头像
	public int level ;           		//当前等级
	public String name ;                //名称
	public int combat ;                 //战斗力
	public boolean online ;             //是否在线
	
	public FriendObject() {}
	
	public FriendObject(RecordTransient record) {
		id = record.get(Human.K.id);
		modelSn = record.get(Human.K.sn);
		level = record.get(Human.K.level);
		name = record.get(Human.K.name);
		combat = record.get(Human.K.combat);
	}
	
	public FriendObject(HumanGlobalInfo info) {
		id = info.id;
		modelSn = info.modelSn;
		level = info.level;
		name = info.name;
		combat = info.combat;
	}
	
	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(id);
		out.write(modelSn);
		out.write(level);
		out.write(name);
		out.write(combat);
		out.write(online);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		id = in.read();
		modelSn = in.read();
		level = in.read();
		name = in.read();
		combat = in.read();
		online = in.read();
	}

	/**
	 * DFriendInfo消息
	 * @return
	 */
	public DFriendInfo createMsg() {
		DFriendInfo.Builder msg = DFriendInfo.newBuilder();
		msg.setHumanId(id);
		msg.setHumanSn(modelSn);
		msg.setLevel(level);
		msg.setName(name);
		msg.setCombat(combat);
		msg.setOnline(online);
		return msg.build();
	}
	
}
