package org.gof.core;

import java.io.IOException;

import org.gof.core.interfaces.ISerilizable;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

/**
 * 当输入流与输出流转换时，可以用本对象作为中间类型。
 */
public class Chunk implements ISerilizable {
	public byte[] buffer;
	public int offset;
	public int length;
	
	public Chunk() {
	
	}
	
	public Chunk(Builder msg) {
		this(msg.build());
	}
	
	public Chunk(Message msg) {
		this(msg.toByteArray());
	}
	
	public Chunk(byte[] buf) {
		buffer = buf;
		offset = 0;
		length = buf.length;
	}
	
	public Chunk(byte[] buf, int off, int len) {
		buffer = buf;
		offset = off;
		length = len;
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(this.length);
		stream.writeBytes(buffer, offset, length);
	}

	@Override
	public void readFrom(InputStream stream) throws IOException {
		this.length = stream.read();
		this.offset = 0;
		this.buffer = stream.read();
	}
}
