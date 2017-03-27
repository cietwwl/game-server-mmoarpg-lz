package org.gof.demo.worldsrv.support;

import java.io.IOException;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;

/**
 * 需要错误原因的返回结果
 */
public class ReasonResult implements ISerilizable {
	public boolean success;
	public String reason = "";
	
	public ReasonResult() {
		
	}
	
	public ReasonResult(boolean success) {
		super();
		this.success = success;
	}
	
	public ReasonResult(boolean success, String reason) {
		super();
		this.success = success;
		this.reason = reason;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(success);
		out.write(reason);
	}
	
	@Override
	public void readFrom(InputStream in) throws IOException {
		success = in.read();
		reason = in.read();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("success", success).append("reason", reason).toString();
	}
}
