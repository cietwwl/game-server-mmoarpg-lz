package org.gof.core;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.interfaces.ISerilizable;

public class CallReturn implements ISerilizable {
	public long id;
	public String nodeId;
	public String portId;
	
	public CallReturn() { }

	public CallReturn(long id, String nodeId, String portId) {
		this.id = id;
		this.nodeId = nodeId;
		this.portId = portId;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(id);
		stream.write(nodeId);
		stream.write(portId);
	}
	
	@Override
	public void readFrom(InputStream stream) throws IOException {
		id = stream.read();
		nodeId = stream.read();
		portId = stream.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
					.append("id", id)
					.append("nodeId", nodeId)
					.append("portId", portId)
					.toString();
	}
}
