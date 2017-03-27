package org.gof.core;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.interfaces.ISerilizable;
import org.gof.core.support.Param;

public class Call implements ISerilizable {
	//请求类型
	public static final int TYPE_RPC = 1000;			//远程调用
	public static final int TYPE_RPC_RETURN = 2000;	//远程调用返回
	public static final int TYPE_MIX = 3000;			//整合专用
	public static final int TYPE_PING = 4000;			//心跳检测
	
	public long id;							//请求ID
	public int type;							//请求类型 1000 2000 3000 4000
	public String fromNodeId;					//发送方NodeId
	public String fromPortId;					//发送方PortId(Call请求听过port发送，简化业务没精确到service)
	public CallPoint to = new CallPoint();		//接收方
	public int methodKey;					//调用函数名称
	public Object[] methodParam;				//调用函数参数
	public Param returns = new Param();			//返回值
	public Param param = new Param();			//扩展参数
	
	/**
	 * 创建CallReturn
	 * @return
	 */
	public CallReturn createCallReturn() {
		return new CallReturn(id, fromNodeId, fromPortId);
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(type);
		stream.write(fromNodeId);
		stream.write(fromPortId);
		stream.write(to);
		stream.write(id);
		stream.write(methodKey);
		stream.write(methodParam);
		stream.write(returns);
		stream.write(param);
	}
	
	@Override
	public void readFrom(InputStream stream) throws IOException {
		type = stream.read();
		fromNodeId = stream.read();
		fromPortId = stream.read();
		to = stream.read();
		id = stream.read();
		methodKey = stream.read();
		methodParam = stream.read();
		returns = stream.read();
		param = stream.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("type", type)
					.append("fromNodeId", fromNodeId)
					.append("fromPortId", fromPortId)
					.append("to", to)
					.append("callId", id)
					.append("methodKey", methodKey)
					.append("methodParameters", methodParam)
					.append("returnValues", returns)
					.toString();
	}
}
