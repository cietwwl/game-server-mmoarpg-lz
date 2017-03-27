package org.gof.core.support;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;

/**
 * 玩家连接状态
 */
public class ConnectionStatus implements ISerilizable {
	//状态代码
	public static final int STATUS_LOGIN = 0;			//登陆中
	public static final int STATUS_GATE = 1;			//选择角色
	public static final int STATUS_PLAYING = 2;		//游戏中
	public static final int STATUS_LOSTED = 3;		//已断开连接
	
	public int status = STATUS_LOGIN;					//默认状态为登陆中
	public long humanId;								//玩家ID
	public String account = "";							//玩家账号
	public String stageNodeId = "";						//所在地图Node
	public String stagePortId = "";						//所在地图Port
	//public long stageId;								//所在地图ID
	
	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(status);
		stream.write(humanId);
		stream.write(account);
		stream.write(stageNodeId);
		stream.write(stagePortId);
	}
	
	@Override
	public void readFrom(InputStream stream) throws IOException {
		this.status = stream.read();
		this.humanId = stream.read();
		this.account = stream.read();
		this.stageNodeId = stream.read();
		this.stagePortId = stream.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("humanId", humanId)
					.append("account", account)
					.append("status", status)
					.append("stageNodeId", stageNodeId)
					.append("stagePortId", stagePortId)
					.toString();
	}
}
