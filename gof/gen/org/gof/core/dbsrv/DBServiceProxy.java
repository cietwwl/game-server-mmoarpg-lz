package org.gof.core.dbsrv;
                    
import java.util.List;  
import org.gof.core.Port;
import org.gof.core.CallPoint;
import org.gof.core.Service;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;
import org.gof.core.support.log.LogCore;
import org.gof.core.gen.proxy.ProxyBase;
import org.gof.core.support.function.*;
//import org.gof.core.EnumCall;
import org.gof.core.gen.GofGenFile;
import org.gof.core.Chunk;
import org.gof.core.Record;
import java.util.List;

@GofGenFile
public final class DBServiceProxy extends ProxyBase {
	public final class EnumCall{
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_COUNTBY_BOOLEAN_STRING_OBJECTS = 1;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_COUNTBYQUERY_BOOLEAN_STRING_STRING_OBJECTS = 2;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_DELETE_STRING_LONG = 3;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_EXECUTE_BOOLEAN_BOOLEAN_STRING_OBJECTS = 4;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_FINDALL_STRING = 5;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBY_BOOLEAN_INT_INT_STRING_OBJECTS = 6;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBY_BOOLEAN_STRING_OBJECTS = 7;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBYQUERY_BOOLEAN_STRING_STRING_OBJECTS = 8;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBYQUERY_BOOLEAN_STRING_LIST_STRING_OBJECTS = 9;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_FINDFIELDSET = 10;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_FLUSH = 11;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_FLUSH_STRING = 12;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_GET_STRING_LONG = 13;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_GET_STRING_LIST = 14;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_GETBY_BOOLEAN_STRING_OBJECTS = 15;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_GETBYQUERY_BOOLEAN_STRING_STRING_OBJECTS = 16;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_GETBYQUERY_BOOLEAN_STRING_LIST_STRING_OBJECTS = 17;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_INSERT_RECORD = 18;
		public static final int ORG_GOF_CORE_DBSRV_DBSERVICE_UPDATE_STRING_LONG_CHUNK_BOOLEAN = 19;
	}
	private static final String SERV_ID = "dbCommon";
	
	private CallPoint remote;
	private Port localPort;
	
	/**
	 * 私有构造函数
	 * 防止实例被私自创建 必须通过newInstance函数
	 */
	private DBServiceProxy() {}
	
	/**
	 * 获取函数指针
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object getMethodFunction(Service service, int methodKey) {
		DBService serv = (DBService)service;
		switch (methodKey) {
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_COUNTBY_BOOLEAN_STRING_OBJECTS: {
				return (GofFunction3<Boolean, String, Object[]>)serv::countBy;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_COUNTBYQUERY_BOOLEAN_STRING_STRING_OBJECTS: {
				return (GofFunction4<Boolean, String, String, Object[]>)serv::countByQuery;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_DELETE_STRING_LONG: {
				return (GofFunction2<String, Long>)serv::delete;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_EXECUTE_BOOLEAN_BOOLEAN_STRING_OBJECTS: {
				return (GofFunction4<Boolean, Boolean, String, Object[]>)serv::execute;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDALL_STRING: {
				return (GofFunction1<String>)serv::findAll;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBY_BOOLEAN_INT_INT_STRING_OBJECTS: {
				return (GofFunction5<Boolean, Integer, Integer, String, Object[]>)serv::findBy;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBY_BOOLEAN_STRING_OBJECTS: {
				return (GofFunction3<Boolean, String, Object[]>)serv::findBy;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBYQUERY_BOOLEAN_STRING_STRING_OBJECTS: {
				return (GofFunction4<Boolean, String, String, Object[]>)serv::findByQuery;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBYQUERY_BOOLEAN_STRING_LIST_STRING_OBJECTS: {
				return (GofFunction5<Boolean, String, List, String, Object[]>)serv::findByQuery;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDFIELDSET: {
				return (GofFunction0)serv::findFieldSet;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FLUSH: {
				return (GofFunction0)serv::flush;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FLUSH_STRING: {
				return (GofFunction1<String>)serv::flush;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GET_STRING_LONG: {
				return (GofFunction2<String, Long>)serv::get;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GET_STRING_LIST: {
				return (GofFunction2<String, List>)serv::get;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GETBY_BOOLEAN_STRING_OBJECTS: {
				return (GofFunction3<Boolean, String, Object[]>)serv::getBy;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GETBYQUERY_BOOLEAN_STRING_STRING_OBJECTS: {
				return (GofFunction4<Boolean, String, String, Object[]>)serv::getByQuery;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GETBYQUERY_BOOLEAN_STRING_LIST_STRING_OBJECTS: {
				return (GofFunction5<Boolean, String, List, String, Object[]>)serv::getByQuery;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_INSERT_RECORD: {
				GofFunction1<Record> f = (record) -> { try { serv.insert(record); } catch(Exception e) { throw new org.gof.core.support.SysException(e); } };
				return f;
			}
			case EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_UPDATE_STRING_LONG_CHUNK_BOOLEAN: {
				return (GofFunction4<String, Long, Chunk, Boolean>)serv::update;
			}
			default: break;
		}
		return null;
	}
	
	/**
	 * 获取实例
	 * 大多数情况下可用此函数获取
	 * @param localPort
	 * @return
	 */
	public static DBServiceProxy newInstance() {
		String portId = Distr.getPortId(SERV_ID);
		if(portId == null) {
			LogCore.remote.error("通过servId未能找到查找上级Port: servId={}", SERV_ID);
			return null;
		}
		
		String nodeId = Distr.getNodeId(portId);
		if(nodeId == null) {
			LogCore.remote.error("通过portId未能找到查找上级Node: portId={}", portId);
			return null;
		}
		
		return createInstance(nodeId, portId, SERV_ID);
	}
	
	
	/**
	 * 创建实例
	 * @param localPort
	 * @param node
	 * @param port
	 * @param id
	 * @return
	 */
	private static DBServiceProxy createInstance(String node, String port, Object id) {
		DBServiceProxy inst = new DBServiceProxy();
		inst.localPort = Port.getCurrent();
		inst.remote = new CallPoint(node, port, id);
		
		return inst;
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	/**
	 * 监听返回值
	 * @param obj
	 * @param methodName
	 * @param context
	 */
	public void listenResult(GofFunction2<Param, Param> method, Param context) {
		localPort.listenResult(method, context);
	}
	
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Object...context) {
		listenResult(method, new Param(context));
	}
	
	public void listenResult(GofFunction3<Boolean, Param, Param> method, Param context) {
		localPort.listenResult(method, context);
	}
	
	
	/**
	 * 等待返回值
	 */
	public Param waitForResult() {
		return localPort.waitForResult();
	}
	
	public void countBy(boolean flush, String tableName, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_COUNTBY_BOOLEAN_STRING_OBJECTS, new Object[]{ flush, tableName, params });
	}
	
	public void countByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_COUNTBYQUERY_BOOLEAN_STRING_STRING_OBJECTS, new Object[]{ flush, tableName, whereAndOther, params });
	}
	
	public void delete(String tableName, long id) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_DELETE_STRING_LONG, new Object[]{ tableName, id });
	}
	
	public void execute(boolean needResult, boolean flushAll, String sql, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_EXECUTE_BOOLEAN_BOOLEAN_STRING_OBJECTS, new Object[]{ needResult, flushAll, sql, params });
	}
	
	public void findAll(String tableName) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDALL_STRING, new Object[]{ tableName });
	}
	
	public void findBy(boolean flush, int firstResult, int maxResults, String tableName, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBY_BOOLEAN_INT_INT_STRING_OBJECTS, new Object[]{ flush, firstResult, maxResults, tableName, params });
	}
	
	public void findBy(boolean flush, String tableName, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBY_BOOLEAN_STRING_OBJECTS, new Object[]{ flush, tableName, params });
	}
	
	public void findByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBYQUERY_BOOLEAN_STRING_STRING_OBJECTS, new Object[]{ flush, tableName, whereAndOther, params });
	}
	
	public void findByQuery(boolean flush, String tableName, List columns, String whereAndOther, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDBYQUERY_BOOLEAN_STRING_LIST_STRING_OBJECTS, new Object[]{ flush, tableName, columns, whereAndOther, params });
	}
	
	public void findFieldSet() {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FINDFIELDSET, new Object[]{  });
	}
	
	public void flush() {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FLUSH, new Object[]{  });
	}
	
	public void flush(String tableName) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_FLUSH_STRING, new Object[]{ tableName });
	}
	
	public void get(String tableName, long id) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GET_STRING_LONG, new Object[]{ tableName, id });
	}
	
	public void get(String tableName, List ids) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GET_STRING_LIST, new Object[]{ tableName, ids });
	}
	
	public void getBy(boolean flush, String tableName, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GETBY_BOOLEAN_STRING_OBJECTS, new Object[]{ flush, tableName, params });
	}
	
	public void getByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GETBYQUERY_BOOLEAN_STRING_STRING_OBJECTS, new Object[]{ flush, tableName, whereAndOther, params });
	}
	
	public void getByQuery(boolean flush, String tableName, List columns, String whereAndOther, Object... params) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_GETBYQUERY_BOOLEAN_STRING_LIST_STRING_OBJECTS, new Object[]{ flush, tableName, columns, whereAndOther, params });
	}
	
	public void insert(Record record) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_INSERT_RECORD, new Object[]{ record });
	}
	
	public void update(String tableName, long id, Chunk patch, boolean sync) {
		localPort.call(remote, EnumCall.ORG_GOF_CORE_DBSRV_DBSERVICE_UPDATE_STRING_LONG_CHUNK_BOOLEAN, new Object[]{ tableName, id, patch, sync });
	}
}
