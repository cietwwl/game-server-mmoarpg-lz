package org.gof.core.support.idAllot;

import java.util.LinkedList;

import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.gen.callback.DistrCallback;
import org.gof.core.support.Param;
import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;

/**
 * 可分配ID池
 * 此类并非线程安全的
 */
@SuppressWarnings("deprecation")
public abstract class IdAllotPoolBase {	
	private int 剩余警戒值;		//当ID池数量小于警戒值之后 就立即申请的ID
	private int 每次申请数量;
	private long 运营商标识;
	private long 游戏区标识;
	
	//可分配ID池
	private final LinkedList<Long> ids = new LinkedList<>();
	//日志
	private Logger log = LogCore.core;
	//是否在ID分配申请中
	private boolean applying = false;
	
	//所属Port
	private Port port;
	
	/**
	 * 构造函数
	 * 立即申请一些ID备用
	 */
	public IdAllotPoolBase(Port port, int platformDigit, int serverDigit) {
		this(port, platformDigit, serverDigit, 10000, 5000);
	}
	
	/**
	 * 构造函数
	 * 立即申请一些ID备用
	 */
	public IdAllotPoolBase(Port port, int platformDigit, int serverDigit, int applyNum, int warnNum) {
		//记录所属Port
		this.port = port;
		this.运营商标识 = platformDigit * (long) Math.pow(10, 17);
		this.游戏区标识 = serverDigit * (long) Math.pow(10, 13);
		this.每次申请数量 = applyNum;
		this.剩余警戒值 = warnNum;
		
		//初始化ID分配池
		port.addQueue(new PortPulseQueue() {
			public void execute(Port port) {
				//Port启动时先同步方式申请一次
				applySyn();
			}
		});
	}
	
	/**
	 * 获取可使用ID
	 * @return
	 */
	public long applyId() {
		//没有可分配的ID了
		if(ids.isEmpty()) {
			//记录日志
			log.error("[ID分配]出现了问题：可分配ID池空了，"
					+ "这种情况下会出现线程阻塞等待的情况：申请数量={}, 警戒值={}, port={}", 
					每次申请数量, 剩余警戒值, port, new Throwable());
			
			//正常来说 当ID池数量小于警戒值之后就会自动申请ID
			//此处无ID可分配也许是申请结果还木有返回
			//单木有办法 此处阻塞线程进行同步申请
			applySyn();	
		}
		
		//小于警戒值 申请新ID
		if(ids.size() < 剩余警戒值 && !applying) {
			applyAyn();
		}
		
		//获取可分配的ID
		long id = ids.pop();
		//加上运营商和服务器标识
		long result = 运营商标识 + 游戏区标识 + id;
		
		return result;
	}
	
	/**
	 * 同步申请可分配ID
	 */
	private void applySyn() {
		//申请可分配ID
		IdAllotServiceProxy prx = IdAllotServiceProxy.newInstance();
		prx.apply(每次申请数量);
		Param results = prx.waitForResult();
		
		//分配到的ID范围
		long begin = results.get("begin");
		long end = results.get("end");
		
		//加入可分配ID池中
		putIds(begin, end);
		
		//日志
		if(log.isInfoEnabled()) {
			log.info("[ID分配]同步方式向服务端申请可分配ID范围：begin={}, end={}, portId={}。", begin, end, port.getId());
		}
	}
	
	/**
	 * 异步申请可分配ID
	 */
	private void applyAyn() {
		//设置申请状态
		applying = true;
				
		IdAllotServiceProxy prx = IdAllotServiceProxy.newInstance();
		prx.apply(每次申请数量);
		prx.listenResult(this::_result_applyId);	
		
		//日志
		if(log.isInfoEnabled()) {
			log.info("[ID分配]向服务端申请新的可分配ID：portId={}", port.getId());
		}
	}
	
	/**
	 * 处理申请ID请求的返回值
	 * @param timeout
	 * @param results
	 * @param context
	 * @throws InterruptedException 
	 */
	@DistrCallback
	public void _result_applyId(Param results, Param context) {
		//设置申请状态
		applying = false;
		
		//分配到的ID范围
		long begin = results.get("begin");
		long end = results.get("end");
		
		//加入可分配ID池中
		putIds(begin, end);
		
		//日志
		if(log.isInfoEnabled()) {
			log.info("[ID分配]服务端返回可分配ID范围：begin={}, end={}, portId={}。", begin, end, port.getId());
		}
	}
	
	/**
	 * 增加可分配ID
	 * @param start
	 * @param end
	 */
	private void putIds(long start, long end) {
		//加入可分配ID池中
		for(long i = start; i <= end; i++) {
			ids.add(i);
		}
	}
}
