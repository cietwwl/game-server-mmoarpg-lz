package org.gof.demo.worldsrv.name;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gof.core.RecordTransient;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.NameFix;

/**
 * 离线竞技场全局服务
 */
@DistrClass(
	servId = D.SERV_NAME,
	importClass = {}
)
public class NameService extends GameServiceBase {
	//所有的名字
	private static Set<String> NAME_SET = new HashSet<String>();
	private static final int COUNTER_PER_FIND = 1000;

	public NameService(GamePort port) {
		super(port); 
	}
	
	/**
	 * 在服务器启动时候，将所有的名字加载到内存里
	 */
	@Override
	protected void init() {
		DBServiceProxy prx = DBServiceProxy.newInstance();
		List<RecordTransient> records;
		//获得数量
		prx.countBy(false, Human.tableName);
		Param param = prx.waitForResult();
		int count = param.get();
		
		
		Log.temp.info("star to init NameService: count {}", count);
		long time = System.currentTimeMillis();
		int loopCount = count / 1000;
		//分页查询
		for(int i = 0 ; i <= loopCount ; i++) {
			// where条件 
			String whereSql = Utils.createStr(" limit {}, {}", i * COUNTER_PER_FIND, COUNTER_PER_FIND);
			// 查询的列
			List<String> colums = new ArrayList<String>();
			colums.add("name");
			prx.findByQuery(false, Human.tableName, colums, whereSql);
			param = prx.waitForResult();
			records = param.get();
			//加载离线竞技数据
			for(RecordTransient r : records) {
				NAME_SET.add(r.get("name"));
			}
		}
		
		Log.temp.info("finish to init NameService: cost {}", System.currentTimeMillis() - time);
	}
	
	/**
	 * 添加一个新的名字
	 * @param name
	 */
	@DistrMethod
	public void add(String name) {
		NAME_SET.add(name);
	}
	
	/**
	 * 更改名字
	 * @param oldName
	 * @param newName
	 */
	@DistrMethod
	public void change(String oldName, String newName) {
		NAME_SET.remove(oldName);
		NAME_SET.add(newName);
	}
	
	/**
	 * 是否重复
	 * @param oldName
	 * @param newName
	 */
	@DistrMethod
	public void repeat(String newName) {
		port.returns("repeat",NAME_SET.contains(newName));
	}
	
	/**
	 * 随机获得一个名字
	 * @return
	 */
	@DistrMethod
	public void randomName() {
		while(true) {
			String randomName = NameFix.randomName();
			if(!NAME_SET.contains(randomName)){
				port.returns("randomName",randomName);
				break;
			}
		}
	}
}
