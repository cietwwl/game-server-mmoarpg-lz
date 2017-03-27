package org.gof.core.support.idAllot;

import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.Service;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.dbsrv.entity.IdAllot;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;

/**
 * ID分配服务基类
 */
@DistrClass(
	servId = Distr.SERV_ID_ALLOT
)
public class IdAllotService extends Service {
	//数据库中记录最大ID数的主键
	private static final long DB_ID = 1;
	
	private IdAllot data;		//ID分配记录
	
	public IdAllotService(Port port) {
		super(port);
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		//用同步获取数据库初始ID
		DBServiceProxy db = DBServiceProxy.newInstance();
		db.get(IdAllot.tableName, DB_ID);
		Param param = db.waitForResult();
		Record r = param.get();
		
		//首次 初始化
		if(r == null) {
			data = new IdAllot();
			data.setId(DB_ID);
			data.setValue(0);
			data.persist();
		} else {	//恢复之前记录值
			data = new IdAllot(r);
		}
	}

	/**
	 * 申请ID
	 */
	@DistrMethod
	public void apply(int num) {
		//分配数量
		long idMin = data.getValue() + 1;
		long idMax = data.getValue() + num;
		
		//记录分配
		data.setValue(idMax);
		//不延迟更新 立即同步到数据库
		data.update(true);

		//返回值
		port.returns("begin", idMin, "end", idMax);
	}
	
	@Override
	public Object getId() {
		return Distr.SERV_ID_ALLOT;
	}
}
