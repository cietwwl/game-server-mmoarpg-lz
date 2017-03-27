package org.gof.core.dbsrv;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gof.core.CallReturn;
import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.RecordTransient;
import org.gof.core.Service;
import org.gof.core.db.DBConnection;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.support.Config;
import org.gof.core.support.Distr;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;

/**
 * 处理一些消耗较大的操作
 * 不对外提供服务 仅供DBService调用
 */
@DistrClass(
	servId = Distr.SERV_DB_LARGE,
	importClass = {CallReturn.class}
)
public class DBLargeService extends Service {
	private final DBConnection dbConn;			//数据库连接
	private final Logger log = LogCore.db;		//日志
	
	/**
	 * 构造函数
	 * @param port
	 */
	public DBLargeService(Port port) {
		super(port);
		dbConn = new DBConnection(Config.DB_URL, Config.DB_USER, Config.DB_PWD);
	}

	/**
	 * 配合DBService进行Find查询并返回给调用者
	 */
	@DistrMethod
	public void findAndReturn(CallReturn callReturn, boolean single, boolean fullQuery, String sql, Object...params) {	
		//读取数据
		try(PreparedStatement ps = dbConn.prepareStatement(sql)) {
			//设置?占位符参数
			int num = 1;
			for(Object val : params) {
				//属性值 如果是List则判定为in语句
				if(val instanceof Collection) {
					Collection<?> vals = (Collection<?>) val;
					//设置in参数
					for(Object v : vals) {
						ps.setObject(num, v);
						num++;
					}
				} else {		//默认为相等
					ps.setObject(num, val);
					num++;
				}
			}
			
			//查询结果 进行拼装
			List<Object> results = new ArrayList<>();
			try(ResultSet rs = ps.executeQuery()) {
				ResultSetMetaData meta = rs.getMetaData();
				//表名 因为是单表，所以第一个字段所属表就是表名
				String tableName = meta.getTableName(1);

				//如果是全表查询 那就可以返回了
				if(fullQuery) {
					//封装结果
					while(rs.next()) {
						//创建数据集合
						Record data = new Record(tableName, rs);
						//加入返回值
						results.add(data);
					}
				
				//如果是某些字段的查询 那么需要设置下返回数据
				} else {
					while(rs.next()) {
						//创建数据集合
						RecordTransient r = new RecordTransient(tableName, rs);
						//加入返回值
						results.add(r);
					}
				}
			}
			
			//返回值
			Object result = null;
			if(single && !results.isEmpty()) {
				result = results.get(0);
			} else if(single && results.isEmpty()) {
				result = null;
			} else {
				result = results;
			}
			
			//返回请求
			if(callReturn == null) {
				port.returns(result);
			} else {
				port.returns(callReturn, result);
			}
		} catch (Exception e) {
			throw new SysException(e, "执行SQL={}, 参数={}", sql, Utils.ofList(params));
		}
	}
	
	/**
	 * 配合DBService进行Count查询并返回给调用者
	 */
	@DistrMethod
	public void countAndReturn(CallReturn callReturn, String sql, Object...params) {	
		//读取数据
		try(PreparedStatement ps = dbConn.prepareStatement(sql)) {
			//设置?占位符参数
			int num = 1;
			for(Object val : params) {
				//属性值 如果是List则判定为in语句
				if(val instanceof Collection) {
					Collection<?> vals = (Collection<?>) val;
					//设置in参数
					for(Object v : vals) {
						ps.setObject(num, v);
						num++;
					}
				} else {		//默认为相等
					ps.setObject(num, val);
					num++;
				}
			}
			
			//查询结果
			int result = 0;
			try(ResultSet rs = ps.executeQuery()) {
				rs.next();
				result = (int) rs.getLong(1);
			}
			
			//返回请求
			port.returns(callReturn, result);
		} catch (Exception e) {
			throw new SysException(e, "执行SQL={}, 参数={}", sql, Utils.ofList(params));
		}
	}
	
	/**
	 * 执行SQL语句，支持?占位符
	 * 一般用来执行update或insert语句
	 * @param sql
	 * @param params
	 */
	@DistrMethod
	public void executeUpdate(String sql, Object...params) {
		executeUpdate(false, sql, params);
	}
	
	/**
	 * 执行SQL语句，支持?占位符
	 * 一般用来执行update或insert语句
	 * @param sql
	 * @param needResult 是否需要返回值通知调用者
	 * @param params
	 */
	@DistrMethod
	public void executeUpdate(boolean needResult, String sql, Object...params) {
		if(log.isDebugEnabled()) {
			log.debug("执行SQL={}，参数={}", sql, Utils.ofList(params));
		}
		
		try(PreparedStatement ps = dbConn.prepareStatement(sql)) {
			for(int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
			
			//执行
			int result = ps.executeUpdate();
			
			//返回执行完毕通知
			if(needResult) {
				port.returns("success", true, "count",  result);
			}
		} catch (Exception e) {
			//错误情况下也进行通知
			port.returns("success", false, "count",  0);
			throw new SysException(e, sql);
		}
	}

	@Override
	public Object getId() {
		return Distr.SERV_DB_LARGE;
	}
}
