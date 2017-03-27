package org.gof.core;

import org.gof.core.support.Param;

/**
 * 异步接收返回值
 * 配合port.waitForResult()使用
 */
public class CallResultSync extends CallResultBase {
	private Param results;
	
	/**
	 * 构造函数
	 * @param id
	 */
	public CallResultSync(long id, long timeoutDelay) {
		super(id, timeoutDelay);
	}
	
	/**
	 * 处理返回值
	 * @param call
	 */
	@Override
	public void onResult(Call call) {
		results = call.returns;
	}
	
	/**
	 * 返回值超时 进行后续处理
	 */
	@Override
	public void onTimeout() {
		
	}
	
	/**
	 * 获取数据返回值
	 * @return
	 */
	public Param getResults() {
		return results;
	}
	
	/**
	 * 请求处理完成
	 * @return
	 */
	public boolean isCompleted() {
		return results != null;
	}
}

