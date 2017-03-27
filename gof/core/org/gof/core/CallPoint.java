package org.gof.core;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.interfaces.ISerilizable;

/**
 * 远程调用结点信息
 */
public class CallPoint implements ISerilizable {
	public String nodeId;
	public String portId;
	public Object servId;
	
	/**
	 * 构造函数
	 */
	public CallPoint() {

	}
	
	/**
	 * 构造函数
	 * @param nodeId
	 * @param portId
	 * @param servId
	 */
	public CallPoint(String nodeId, String portId, Object servId) {
		this.nodeId = nodeId;
		this.portId = portId;
		this.servId = servId;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(nodeId);
		stream.write(portId);
		stream.write(servId);
	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		nodeId = stream.read();
		portId = stream.read();
		servId = stream.read();
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("nodeId", nodeId)
					.append("portId", portId)
					.append("servId", servId)
					.toString();
	}
}
