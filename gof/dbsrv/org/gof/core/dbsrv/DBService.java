package org.gof.core.dbsrv;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.gof.core.CallReturn;
import org.gof.core.Chunk;
import org.gof.core.Port;
import org.gof.core.PortPulseQueue;
import org.gof.core.Record;
import org.gof.core.Service;
import org.gof.core.db.CachedRecords;
import org.gof.core.db.CachedTables;
import org.gof.core.db.DBConnection;
import org.gof.core.db.DBConsts;
import org.gof.core.db.Field;
import org.gof.core.db.FieldSet;
import org.gof.core.db.FlushingRecords;
import org.gof.core.db.FlushingTable;
import org.gof.core.db.OrderBy;
import org.gof.core.gen.callback.DistrCallback;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.core.interfaces.IRecord;
import org.gof.core.support.Config;
import org.gof.core.support.Distr;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.TickTimer;
import org.gof.core.support.Time;
import org.gof.core.support.Utils;
import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;

/**
 * 对外提供数据库操作接口类
 * 新增(Insert)删除(Remove)操作会立即同步到数据库
 * 更新(Update)会有写缓存 延迟同步至数据库
 * 
 * !查询缓存已暂时屏蔽
 * !getBy/findBy系列函数，效率较低除了系统启动和玩家登陆外尽量避免调用这些接口，靠业务级别缓存来解决。
 *  查询前要求传入的flush参数一定要注意，如果传入不对可能会造成查询结果错误或性能问题。
 *  如果查询参数仅仅涉及到不会改变的属性(创建/删除操作会立即同步)，那么flush可以为false，其余的都为true。
 */
@DistrClass(
	servId = Distr.SERV_DB_COMMON,
	importClass = {Chunk.class, Record.class, List.class}
)
public class DBService extends Service {
	//字段长度（用来预估缓冲大小）
	private static final int COL_DEF_LEN = 12;
	
	//数量查询KEY
	private static final String SQL_KEY_COUNT = "count(1)";
	//全部参数KEY
	private static final String SQL_KEY_ALL = "*";
	//写缓存创建时间
	public static final String CACHE_WRITE_CREATE_TIME_KEY = "_cache_write_create_time_";
	
	//数据库连接
	private final DBConnection dbConn;
	//数据读取缓存
	//private final CachedTables cachedRead = new CachedTables();
	//数据写入缓存
	private final CachedTables cachedWrite = new CachedTables();
	//写入缓存刷新中的临时缓存
	private final FlushingTable flushing = new FlushingTable();
	
	//常用SQL语句缓存
	private final Map<String, String> SQL_INSERT = new HashMap<>();		//新增SQL语句缓存<表名, SQL>
	private final Map<String, String> SQL_DELETE = new HashMap<>();		//删除SQL语句缓存<表名, SQL>
	private final Map<String, String> SQL_SELECT_ALL = new HashMap<>();	//查询全部数据SQL语句缓存<表名, SQL>
	
	//缓冲同步间隔
	private final TickTimer flushTimer = new TickTimer(Time.SEC);
	//立即开始一次缓存刷新
	private boolean flushNowOnce = false;
	
	//异步刷新版本号
	private long flushVerNow = 0;
	//异步刷新已完成版本号
	private long flushVerFinish = 0;
	
	//日志
	private final Logger log = LogCore.db;
	
	public DBService(DBPort port) {
		super(port);
		
		dbConn = new DBConnection(Config.DB_URL, Config.DB_USER, Config.DB_PWD);
		
		initFieldSet();
	}
	
	/**
	 * 通过升级包更新数据
	 * @param tableName
	 * @param id
	 * @param patch
	 * @param sync
	 */
	@DistrMethod
	public void update(String tableName, long id, Chunk patch, boolean sync) {
		/* 写缓存 */
		CachedRecords write = cachedWrite.getOrCreate(tableName);
		Record w = write.get(id);
		//初始化
		if(w == null) {
			w = Record.newInstance(tableName);
			w.setStatus(DBConsts.RECORD_STATUS_MODIFIED);
			w.set("id", id);
			//记录写缓存创建时间
			w.set(CACHE_WRITE_CREATE_TIME_KEY, port.getTimeCurrent());
			
			write.put(id, w);
		}
		
		//使修改生效
		w.patchUpdate(patch);
		
		//立即持久化至数据库
		if(sync || Config.DB_CACHED_SYNC_SEC == 0) {
			syncRecord(w);
		}
		
		/* 读缓存 */
//		CachedRecords read = cachedRead.getOrCreate(tableName);
//		Record r = read.get(id);
//
//		//有缓存 修改当前读缓存
//		if(r != null) {
//			//使修改生效
//			r.patchUpdate(patch);
//		}
	}
	
	/**
	 * 新增一条数据
	 * @param tableName
	 * @param chunk
	 * @param cache
	 * @throws SQLException
	 * @throws IOException 
	 */
	@DistrMethod
	public void insert(Record record) throws SQLException, IOException {
		//立即持久化
		syncRecord(record);
	}
	
	/**
	 * 删除
	 * @param tableName
	 * @param id
	 */
	@DistrMethod
	public void delete(String tableName, long id) {
		//模拟一个record 进行删除
		Record record = Record.newInstance(tableName);
		record.setStatus(DBConsts.RECORD_STATUS_DELETED);
		record.set("id", id);
		
		//删除数据
		syncRecord(record);
		
		//删除读缓存
//		cachedRead.removeRecord(tableName, id);
		//删除写缓存
		cachedWrite.removeRecord(tableName, id);
	}
	
	@DistrMethod
	public void findFieldSet() {
		port.returns(FieldSet.CACHE);
	}
	
	/**
	 * 获取符合条件的数据数量
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void countBy(boolean flush, String tableName, Object... params) {
		utilCount(flush, tableName, params);
	}
	
	/**
	 * 获取查询的单体数据
	 * 如果有多条符合则返回第一条
	 * 当查询出的结果在缓存中已存在时，返回缓存中的数据。
	 * 当需要缓存结果时，如果缓存已存在，则不更新缓存。
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void countByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		utilSqlCount(flush, tableName, whereAndOther, params);
	}
	
	/**
	 * 根据主键获取数据
	 * 本操作不会造成写缓存的刷新
	 * @param tableName		表名
	 * @param id			主键
	 */
	@DistrMethod
	public void get(String tableName, long id) {
		//TODO 查询缓存 暂时取消
//		//读缓存
//		CachedRecords cached = cachedRead.getOrCreate(tableName);
//		Record record = cached.get(id);
//		
//		//命中查询缓存 直接返回
//		if(record != null) {
//			port.returns(record);
//			return;
//		}
		
		/* 未命中就取数据库 */
		Param params = utilBaseCreateSql(0, 1, tableName, "id", id);
		String sql = params.get("sql");
		Object[] pa = params.get("params");
		
		//累加返回结果
		List<Record> rs = utilGet(tableName, sql, pa);
		
		//返回结果
		Object result;
		if(rs.isEmpty()) {
			result = null;
		} else {
			result = rs.get(0);
		}
		
		//返回请求
		port.returns(result);
	}
	
	/**
	 * 根据主键获取数据
	 * 本操作不会造成写缓存的刷新
	 * @param tableName		表名
	 * @param id			主键
	 */
	@DistrMethod
	public void get(String tableName, List<Long> ids) {
		//最终返回值
		List<Record> results = new ArrayList<>();
		
		//TODO 查询缓存 暂时取消 missIds == ids
		List<Long> missIds = ids;
		
//		//读缓存
//		CachedRecords cached = cachedRead.getOrCreate(tableName);
//		
//		/* 先在读缓存中找结果，并记录未命中的ID */
//		List<Long> missIds = new ArrayList<>();		
//		for(Long id : ids) {
//			Record data = cached.get(id);
//			//未命中
//			if(data == null) {
//				missIds.add(id);
//			} else {
//				results.add(data);
//			}
//		}
//		
//		//全部命中查询缓存 直接返回
//		if(missIds.isEmpty()) {
//			port.returns(results);
//			return;
//		}
		
		/* 未命中的取数据库 */
		Param params = utilBaseCreateSql(0, missIds.size(), tableName, "id", missIds);
		String sql = params.get("sql");
		Object[] pa = params.get("params");
		
		//累加返回结果
		List<Record> rs = utilGet(tableName, sql, pa);
		results.addAll(rs);
		
		//返回请求
		port.returns(results);
	}
	
	/**
	 * 获取符合条件的单体数据
	 * 如果有多条符合则返回第一条
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	@SuppressWarnings("unchecked")
	public void getBy(boolean flush, String tableName, Object...params) {
		//如果仅仅是根据ID查询 那么优化下
		if(params.length == 2 && "id".equals(params[0])) {
			Object param1 = params[1];
			if(param1 instanceof List) {
				get(tableName, (List<Long>)param1);
			} else {
				get(tableName, (long)param1);
			}
		//正常查询
		} else {
			utilGetBy(flush, tableName, params);
		}
	}
	
	/**
	 * 获取查询的单体数据
	 * 如果有多条符合则返回第一条
	 * 当查询出的结果在缓存中已存在时，返回缓存中的数据。
	 * 当需要缓存结果时，如果缓存已存在，则不更新缓存。
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void getByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		getByQuery(flush, tableName, Utils.ofList("*"), whereAndOther, params);
	}
	
	/**
	 * 获取查询的单体数据
	 * 如果有多条符合则返回第一条
	 * 当查询出的结果在缓存中已存在时，返回缓存中的数据。
	 * 当需要缓存结果时，如果缓存已存在，则不更新缓存。
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void getByQuery(boolean flush, String tableName, List<String> columns, String whereAndOther, Object... params) {
		utilSqlBase(flush, true, tableName, columns, whereAndOther, params);
	}
	
	/**
	 * 获取全部数据集合
	 * 支持排序
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void findAll(String tableName) {
		findBy(false, tableName);
	}
	
	/**
	 * 获取符合条件的数据集合
	 * 支持排序
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void findBy(boolean flush, String tableName, Object... params) {
		findBy(flush, 0, Integer.MAX_VALUE, tableName, params);
	}
	
	/**
	 * 获取符合条件的数据集合
	 * 支持分页，支持排序
	 * @param tableName
	 * @param firstResult
	 * @param maxResults
	 * @param params
	 */
	@DistrMethod
	public void findBy(boolean flush, int firstResult, int maxResults, String tableName, Object... params) {
		utilFindBy(flush, firstResult, maxResults, tableName, params);
	}
	
	/**
	 * 获取查询条件的数据集合
	 * 当查询出的结果在缓存中已存在时，返回缓存中的数据。
	 * 当需要缓存结果时，如果缓存已存在，则不更新缓存。
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void findByQuery(boolean flush, String tableName, String whereAndOther, Object... params) {
		findByQuery(flush, tableName, Utils.ofList("*"), whereAndOther, params);
	}
	
	/**
	 * 获取查询条件的数据集合
	 * 当查询出的结果在缓存中已存在时，返回缓存中的数据。
	 * 当需要缓存结果时，如果缓存已存在，则不更新缓存。
	 * @param tableName
	 * @param params
	 */
	@DistrMethod
	public void findByQuery(boolean flush, String tableName, List<String> columns, String whereAndOther, Object... params) {
		utilSqlBase(flush, false, tableName, columns, whereAndOther, params);
	}
	
	/**
	 * 执行SQL语句，支持?占位符
	 * 一般用来执行update或insert语句
	 * 
	 * @param sql
	 * @param params
	 */
	private void executeSync(String sql, Object... params) {
		try(PreparedStatement ps = dbConn.prepareStatement(sql)) {
			for(int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
			
			//执行
			int result = ps.executeUpdate();
			
			//日志
			if(log.isDebugEnabled()) {
				log.debug("执行SQL={}, 参数={}, 影响行数={}", sql, Utils.ofList(params), result);
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 会委托DBLargeService执行SQL语句，支持?占位符
	 * 除非特殊情况下，否则不建议调用本函数
	 * 由于无法判断此操作的影响返回，flush=true时会刷新全部写缓存
	 * 如果调用者可以判断影响返回，可以设置flush=false，通过预先调用flush(tableName)的方式来降低影响
	 * @param needResult 调用方是否需要收到执行完毕的消息
	 * @param flushAll 是否需要刷新全部缓存
	 * @param sql
	 * @param params
	 */
	@DistrMethod
	public void execute(boolean needResult, boolean flushAll, String sql, Object... params) {
		//刷新缓存
		if(flushAll) {
			LogCore.db.info("调用了execute函数，触发写缓存刷新操作。");
			flushDatabase(Long.MAX_VALUE);
		}
		
		//委托执行请求
		DBLargeServiceProxy prx = DBLargeServiceProxy.newInstance();
		prx.executeUpdate(needResult, sql, params);
		
		//监控返回值
		if(needResult) {
			long rid = port.createReturnAsync();
			prx.listenResult(this::_result_excute, "rid", rid);
		}
	}
	
	@DistrCallback
	public void _result_excute(Param results, Param context) {
		long rid = context.get("rid");
		Object success = results.get("success");
		Object count = results.get("count");
		
		port.returnsAsync(rid, "success", success, "count", count);
	}
	
	/**
	 * 刷新缓存至数据库
	 */
	@DistrMethod
	public void flush() {
		flushDatabase(Long.MAX_VALUE);
		
		//日志
		if(log.isInfoEnabled()) {
			log.info("远程调用刷新数据库全部写缓存。");
		}
	}
	
	/**
	 * 刷新单张表的缓存至数据库
	 * @param tableName
	 */
	@DistrMethod
	public void flush(String tableName) {
		int count = flushTable(tableName, Long.MAX_VALUE);
		
		//日志
		if(log.isInfoEnabled()) {
			log.info("远程调用刷新数据库写缓存，表名={}，数量={}", tableName, count);
		}
	}
	
	/**
	 * 刷新全部缓存至数据库
	 */
	private void flushDatabase(long flushTime) {
		//缓存间隔时间为0时 就不用定时同步了
		//因为更新时已经做了判断 会立即持久化至数据库
		if(Config.DB_CACHED_SYNC_SEC == 0) {
			return;
		}
		
		//遍历数据 找到需要刷新的缓存
		int count = 0;
		for(String tableName : cachedWrite.getTableNames()) {
			int c = flushTable(tableName, flushTime);
			//增加刷新数据量
			count += c;
		}
		
		//记录日志
		//关闭时自动调用flushNowOnce时会报错 所以忽略这种情况
		//开启日志 && 不是关闭前刷新 && 有需要持久化的数据 && (是全局刷新 || 按时间刷新时符合抽样概率)
		if(log.isInfoEnabled() && !flushNowOnce && count > 0 && (flushTime == Long.MAX_VALUE || new Random().nextInt(10) == 0)) {
			log.info("进行缓存同步，同步数据量={}。", count);
		}
	}
	
	/**
	 * 刷新单张表的缓存至数据库
	 * @param tableName 需要刷新的表名
	 * @param flushTime 刷新时间戳 小于这个时间之前的数据都需要进行持久化。
	 * @return
	 */
	private int flushTable(String tableName, long flushTime) {
		CachedRecords cached = cachedWrite.getOrCreate(tableName);
		
		//刷新数据量
		int count = 0;
		for(Record record : cached.values()) {
			//不是脏数据 忽略
			if(!record.isDirty()) continue;
			//未达到刷新时间 忽略
			if(record.<Long>get(CACHE_WRITE_CREATE_TIME_KEY) > flushTime) {
				continue;
			}
			
			port.addQueue(new PortPulseQueue(record) {
				public void execute(Port port) {
					Record r = param.get();

					//同步
					DBService simplDB = port.getService(Distr.SERV_DB_COMMON);
					simplDB.syncRecord(r);
				}
			});
			
			//记录修改数量
			++count;
		}
		
		return count;
	}
	
	/**
	 * 查询返回符合结果数量的基础函数
	 * @param tableName
	 * @param params
	 * @return
	 */
	private void utilCount(boolean flush, String tableName, Object... params) {
		List<Object> settings = Utils.ofList(params);
		
		//查询参数
		Map<String, Object> paramsFilter = new LinkedHashMap<>();		//过滤条件
		
		//处理成对参数
		int len = settings.size();
		for(int i = 0; i < len; i += 2) {
			String key = (String)settings.get(i);
			Object val = settings.get(i + 1);
			
			//参数 排序规则忽略
			if(val instanceof OrderBy) {
				//忽略
			} else {	//参数 过滤条件
				paramsFilter.put(key, val);
			}
		}

		//最终查询SQL
		String sql = Utils.createStr("select {} from `{}` {}", SQL_KEY_COUNT, tableName, utilBaseGenSqlWhere(paramsFilter));
		
		//刷新本表的缓存
		if(flush) {
			flushTable(tableName, Long.MAX_VALUE);
			
			//刷新日志
			if(log.isInfoEnabled()) {
				log.info("执行查询引起数据库刷新缓存操作，tableName={}, sql={}", tableName, sql);
			}
		}
		
		//创建异步返回
		CallReturn callReturn = port.getCall().createCallReturn();
		
		//委托查询
		DBLargeServiceProxy prx = DBLargeServiceProxy.newInstance();
		prx.countAndReturn(callReturn, sql, paramsFilter.values().toArray());
	}
	
	/**
	 * SQL结果数量查询基础函数
	 * @param sql
	 * @param whereAndOther
	 * @param params
	 * @return
	 */
	private void utilSqlCount(boolean flush, String tableName, String whereAndOther, Object...params) {
		List<Object> settings = Utils.ofList(params);
		
		//拼接最终SQL
		String sql = Utils.createStr("select {} from `{}` {}", SQL_KEY_COUNT, tableName, whereAndOther);

		//刷新本表的缓存
		if(flush) {
			flushTable(tableName, Long.MAX_VALUE);
			
			//刷新日志
			if(log.isInfoEnabled()) {
				log.info("执行查询引起数据库刷新缓存操作，tableName={}, sql={}", tableName, sql);
			}
		}
		
		//创建异步返回
		CallReturn callReturn = port.getCall().createCallReturn();
		
		//委托查询
		DBLargeServiceProxy prx = DBLargeServiceProxy.newInstance();
		prx.countAndReturn(callReturn, sql, settings.toArray());
	}
	
	/**
	 * 查询返回数据集合的基础函数
	 * @param tableName
	 * @param firstResult
	 * @param maxResults
	 * @param params
	 * @return
	 */
	private void utilFindBy(boolean flush, int firstResult, int maxResults, String tableName, Object... params) {
		utilBase(flush, false, firstResult, maxResults, tableName, params);
	}
	
	/**
	 * 查询返回单体数据的基础函数
	 * @param tableName
	 * @param servId
	 * @return
	 */
	private void utilGetBy(boolean flush, String tableName, Object... params) {
		utilBase(flush, true, 0, 1, tableName, params);
	}
	
	/**
	 * 通过ID获得返回值的工具类
	 * 只有这里会调用查询了，就不进一步抽象了...
	 * @param tableName
	 * @param servId
	 * @return
	 */
	private List<Record> utilGet(String tableName, String sql, Object... params) {
		//写缓存
		CachedRecords write = cachedWrite.getOrCreate(tableName);
		//刷新中的临时缓存
		FlushingRecords flush = flushing.getOrCreate(tableName);
		
		//日志
		if(log.isDebugEnabled()) {
			log.debug("执行SQL={}, 参数={}", sql, Utils.ofList(params));
		}
		
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
			List<Record> results = new ArrayList<>();
			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()) {
					//创建数据集合
					Record data = new Record(tableName, rs);
					
					//合并未入库修改
					flush.pathUpdate(data, flushVerFinish);
					write.pathUpdate(data);
					
					//加入返回值
					results.add(data);
				}
			}
			
			//返回值
			return results;
		} catch (Exception e) {
			throw new SysException(e, "执行SQL={}, 参数={}", sql, Utils.ofList(params));
		}
	}
	
	/**
	 * 查询信息基础函数
	 * 当查询出的结果在缓存中已存在时，返回缓存中的数据。
	 * 当需要缓存结果时，如果缓存已存在，则不更新缓存。
	 * @param flush 是否需要先刷新缓存
	 * @param single 是否返回单一结果
	 * @param tableName
	 * @param firstResult
	 * @param maxResults
	 * @param params
	 * @return
	 */
	private void utilBase(boolean flush, boolean single, int firstResult, int maxResults, String tableName, Object... params) {
		//需要执行的SQL及参数
		Param sqlParam = utilBaseCreateSql(firstResult, maxResults, tableName, params);
		String sql = sqlParam.get("sql");
		Object[] pa = sqlParam.get("params");
		
		//刷新本表的缓存
		if(flush) {
			flushTable(tableName, Long.MAX_VALUE);
			
			//刷新日志
			if(log.isInfoEnabled()) {
				log.info("执行查询引起数据库刷新缓存操作，tableName={}, sql={}", tableName, sql);
			}
		}
		
		//创建异步返回
		CallReturn callReturn = port.getCall().createCallReturn();

		//委托查询
		//如果查询前刷新了 那么large那边直接返回即可
		//如果没刷新就需要返回到这里再未入库数据同步
		DBLargeServiceProxy prx = DBLargeServiceProxy.newInstance();
		prx.findAndReturn(flush ? callReturn : null, single, true, sql, pa);
		
		//如果查询前没刷新 那么返回值需要做未入库修正
		if(!flush) {
			prx.listenResult(this::_result_utilBase, "tableName", tableName, "callReturn", callReturn);
		}
	}
	
	/**
	 * 多表查询或返回部分值时 无法缓存结果
	 * @param sql
	 * @param params
	 * @return
	 */
	private void utilSqlBase(boolean flush, boolean single, String tableName, List<String> columns, String whereAndOther, Object...params) {
		//需要执行的SQL及参数
		Param sqlParam = utilSqlBaseCreateSql(tableName, columns, whereAndOther, params);
		String sql = sqlParam.get("sql");
		Object[] pa = sqlParam.get("params");
		boolean isFullQuery = sqlParam.get("isFullQuery");
		
		//刷新本表的缓存
		if(flush) {
			flushTable(tableName, Long.MAX_VALUE);
			
			//刷新日志
			if(log.isInfoEnabled()) {
				log.info("执行查询引起数据库刷新缓存操作，tableName={}, sql={}", tableName, sql);
			}
		}
		
		//创建异步返回
		CallReturn callReturn = port.getCall().createCallReturn();
		
		//委托查询
		//如果查询前刷新了 那么large那边直接返回即可
		//如果没刷新就需要返回到这里再未入库数据同步
		DBLargeServiceProxy prx = DBLargeServiceProxy.newInstance();
		prx.findAndReturn(flush ? callReturn : null, single, isFullQuery, sql, pa);
		
		//如果查询前没刷新 那么返回值需要做未入库修正
		if(!flush) {
			prx.listenResult(this::_result_utilBase, "tableName", tableName, "callReturn", callReturn);
		}
	}
	
	@DistrCallback
	public void _result_utilBase(Param results, Param context) {
		//能来这个逻辑的 证明上面没做flush刷新缓存操作 需要做未入库数据修正
		Object result = results.get();
		String tableName = context.get("tableName");
		CallReturn callReturn = context.get("callReturn");
		
		//写缓存
		CachedRecords write = cachedWrite.getOrCreate(tableName);
		//刷新中的临时缓存
		FlushingRecords flush = flushing.getOrCreate(tableName);
		
		//未入库数据修正
		//集合数据
		if(result instanceof List) {
			@SuppressWarnings("unchecked")
			List<IRecord> rs = (List<IRecord>) result;
			for(IRecord r : rs) {
				flush.pathUpdate(r, flushVerFinish);
				write.pathUpdate(r);
			}
		//单体数据
		} else {
			IRecord r = (IRecord) result;
			flush.pathUpdate(r, flushVerFinish);
			write.pathUpdate(r);
		}
		
		//返回
		port.returns(callReturn, result);
	}
	
	/**
	 * 创建基本查询SQL语句
	 * @param firstResult
	 * @param maxResults
	 * @param tableName
	 * @param params
	 * @return
	 */
	private Param utilBaseCreateSql(int firstResult, int maxResults, String tableName, Object... params) {
		List<Object> settings = Utils.ofList(params);
		
		//查询参数
		Map<String, Object> paramsFilter = new LinkedHashMap<>();		//过滤条件
		Map<String, OrderBy> paramsOrder = new LinkedHashMap<>();		//排序规则
//		boolean paramCache = false;									//缓存数据
//		
//		//处理独立参数
//		for(Iterator<Object> iter = settings.iterator(); iter.hasNext();) {
//			Object p = iter.next();
//			//参数 开启缓存
//			if(p instanceof Cache) {
//				paramCache = (p == Cache.TRUE);
//				iter.remove();
//			}
//		}
		
		//处理成对参数
		int len = settings.size();
		for(int i = 0; i < len; i += 2) {
			String key = (String)settings.get(i);
			Object val = settings.get(i + 1);
			
			//参数 排序规则
			if(val instanceof OrderBy) {
				paramsOrder.put(key, (OrderBy) val);
			} else {	//参数 过滤条件
				paramsFilter.put(key, val);
			}
		}
		
		//根据参数拼装出参数
		String sql = utilBaseGenSqlFrom(tableName) 
				   + utilBaseGenSqlWhere(paramsFilter)
				   + utilBaseGenSqlOrderBy(paramsOrder)
				   + utilBaseGenSqlLimit(firstResult, maxResults);
		
		return new Param("sql", sql, "params", paramsFilter.values().toArray());
	}
	
	/**
	 * 多表查询或返回部分值时 无法缓存结果
	 * @param sql
	 * @param params
	 * @return
	 */
	private Param utilSqlBaseCreateSql(String tableName, List<String> columns, String whereAndOther, Object...params) {
		List<Object> settings = Utils.ofList(params);
		
		//查询参数
//		boolean paramCache = false;		//缓存数据
//		
//		//处理独立参数
//		for(Iterator<Object> iter = settings.iterator(); iter.hasNext();) {
//			Object p = iter.next();
//			//参数 开启缓存
//			if(p instanceof Cache) {
//				paramCache = (p == Cache.TRUE);
//				iter.remove();
//			}
//		}
		
		/* 字段修饰 */		
		//是否为全表查询
		boolean isFullQuery = false;
		if(columns.size() == 1 && SQL_KEY_ALL.equals(columns.get(0))) {
			isFullQuery = true;
		}
		
		//拼接属性SQL
		String columnStr;
		
		//特殊字段 单独处理
		if(isFullQuery) {
			columnStr = columns.get(0);
			
		//常规字段
		} else {
			//拼属性字符串
			int size = columns.size();
			StringBuilder sb = new StringBuilder(size * COL_DEF_LEN);
			for(int i = 0; i < size; i++) {
				String c = columns.get(i);
				
				//多个参数分割符
				if(i > 0) {
					sb.append(",");
				}

				//具体参数
				sb.append("`").append(c).append("`");
			}
			
			//如果玩家查询属性中 没有ID 就给他加上，否则之后无法根据ID使未入库数据生效了。
			if(!columns.contains("id")) {
				sb.append(", `id`");
			}
			
			//查询字段最终SQL
			columnStr = sb.toString();
		}
		
		//拼接最终SQL
		String sql = Utils.createStr("select {} from `{}` {}", columnStr, tableName, whereAndOther);
		
		return new Param("sql", sql, "params", settings.toArray(), "isFullQuery", isFullQuery);
	}
	
	/**
	 * 查询信息基础函数工具函数
	 * 用来生成from语句
	 * @param tableName
	 * @return
	 */
	private String utilBaseGenSqlFrom(String tableName) {
		//获取SQL
		String sql = SQL_SELECT_ALL.get(tableName);
		if(sql != null) return sql;
		
		//没有找到缓存SQL 开始创建
		sql = new StringBuilder(50).append("SELECT * FROM `").append(tableName).append("` t").toString();
		
		//缓存SQL
		SQL_SELECT_ALL.put(tableName, sql);
		
		//日志
		if(log.isDebugEnabled()) {
			log.debug("生成基础查询SQL语句：table={}, sql={}", tableName, sql);
		}
		
		return sql;
	}
	
	/**
	 * 查询信息基础函数工具函数
	 * 用来生成where语句
	 * @param tableName
	 * @return
	 */
	private String utilBaseGenSqlWhere(Map<String, Object> paramsFilter) {
		//SQL语句
		StringBuilder sqlWhere = new StringBuilder(34);
		//无参数 立即返回
		if(paramsFilter.isEmpty()) return sqlWhere.toString();
		
		//将参数拼装为?占位符的形式
		for(Entry<String, Object> e : paramsFilter.entrySet()) {
			String key = e.getKey();
			Object val = e.getValue();
			
			//需要增加and分割
			if(sqlWhere.length() > 0) sqlWhere.append(" AND ");
			
			//属性名
			sqlWhere.append("`").append(key).append("`");
			
			//属性值 如果是List则判定为in语句
			if(val instanceof Collection) {
				Collection<?> vals = (Collection<?>) val;
				//拼装in
				sqlWhere.append(" in ").append("(");
				for(int i = 0; i < vals.size(); i++) {
					if(i > 0) sqlWhere.append(",");
					sqlWhere.append("?");
				}
				sqlWhere.append(")");
			} else {		//默认为相等
				sqlWhere.append("=").append("?");
			}
		}
		
		//在头部插入where语句
		sqlWhere.insert(0, " WHERE ");
		
		return sqlWhere.toString();
	}
	
	/**
	 * 查询信息基础函数工具函数
	 * 用来生成orderBy语句
	 * @param tableName
	 * @return
	 */
	private String utilBaseGenSqlOrderBy(Map<String, OrderBy> params) {
		//SQL语句
		StringBuilder orderWhere = new StringBuilder(34);
		//无参数 立即返回
		if(params.isEmpty()) return orderWhere.toString();
		
		//将参数拼装
		for(Entry<String, OrderBy> e : params.entrySet()) {
			//需要增加and分割
			if(orderWhere.length() > 0) orderWhere.append(",");
			
			orderWhere.append("`").append(e.getKey()).append("`").append(" ").append(e.getValue().name());
		}
		
		//在头部插入where语句
		orderWhere.insert(0, " ORDER BY ");
		
		return orderWhere.toString();
	}
	
	/**
	 * 查询信息基础函数工具函数
	 * 用来生成limit语句
	 * @param tableName
	 * @return
	 */
	private String utilBaseGenSqlLimit(int firstResult, int maxResults) {
		//SQL语句
		StringBuilder limitWhere = new StringBuilder(34);
		
		//默认值 无需limit语句
		if(firstResult == 0 && maxResults == Integer.MAX_VALUE) {
			return limitWhere.toString();
		}
		
		//拼装SQL
		limitWhere.append(" LIMIT ").append(firstResult).append(",").append(maxResults);
		
		return limitWhere.toString();
	}
	
	/**
	 * 同步
	 * @param record
	 */
	private void syncRecord(Record record) {
		//没有改动过
		if(!record.isDirty()) {
			return;
		}

		//根据状态进行不同处理
		switch(record.getStatus()) {
			//新增
			case DBConsts.RECORD_STATUS_NEW: {
				_syncRecordInsert(record);
				break;
			}
			//修改
			case DBConsts.RECORD_STATUS_MODIFIED: {
				_syncRecordUpdate(record);
				break;
			}
			//删除
			case DBConsts.RECORD_STATUS_DELETED: {
				_syncRecordDelete(record);
				break;
			}
		}
	}
	
	/**
	 * 将record新增数据持久化到数据库
	 * @param record
	 */
	private void _syncRecordInsert(Record record) {
		//状态错误
		if(!record.isNew()) return;
		
		//执行SQL语句
		String sql = syncRecordInsertSQLGen(record);
		
		//实际参数值
		Object[] params = syncRecordInsertParamsGen(record);
		
		//执行
		executeSync(sql, params);
		
		//重置状态
		record.resetStatus();
	}
	
	/**
	 * 将record被删除数据持久化到数据库
	 * @param record
	 */
	private void _syncRecordDelete(Record record) {
		//状态错误
		if(!record.isDeleted()) return;
		
		//执行SQL语句
		String sql = SQL_DELETE.get(record.getTableName());
		
		//没有找到缓存SQL 开始创建
		if(sql == null) {
			sql = Utils.createStr("DELETE FROM `{}` WHERE `id`=?", record.getTableName());
			
			//缓存SQL
			SQL_DELETE.put(record.getTableName(), sql);
			
			//日志
			if(log.isDebugEnabled()) {
				log.debug("生成删除SQL语句：table={}, sql={}", record.getTableName(), sql);
			}
		}
		
		//执行
		executeSync(sql, record.get("id"));
	}
	
	/**
	 * 将record更新数据持久化到数据库
	 * @param record
	 */
	private void _syncRecordUpdate(Record record) {
		//状态错误
		if(!record.isModified()) return;
		
		//主键
		long id = record.get("id");
		
		//1.1 拼写SQL语句
		String sql = "UPDATE `{}` SET {} WHERE `id`={}";
		
		//1.2 SET语句部分
		Map<String, Object> sqlSetMap = syncRecordUpdateSetGen(record);
		String sqlSet = (String) sqlSetMap.get("sql");
		Object[] params = (Object[]) sqlSetMap.get("params");
		
		//1.3 最终SQL
		sql = Utils.createStr(sql, record.getTableName(), sqlSet, id);
		
		//执行
		long version = ++flushVerNow;
		DBLargeServiceProxy prx = DBLargeServiceProxy.newInstance();
		prx.executeUpdate(true, sql, params);
		prx.listenResult(this::_result_syncRecordUpdate, "version", version, "tableName", record.getTableName(), "id", id);
		
		/* 将缓存数据移入刷新中的临时缓存 */
		//清除写缓存
		CachedRecords write = cachedWrite.getOrCreate(record.getTableName());
		if(write != null) {
			write.remove(id);
		}
		
		//移入刷新临时缓存
		FlushingRecords flush = flushing.getOrCreate(record.getTableName());
		flush.put(id, version, record);
	}
	
	@DistrCallback
	public void _result_syncRecordUpdate(Param results, Param context) {
		boolean success = results.get("success");
		long version = context.get("version");
		String tableName = context.get("tableName");
		long id = context.get("id");
		
		//返回结果
		if(!success) {
			LogCore.db.error("执行更新操作失败，清理刷新缓存数据。cxt={}", context);
		}
		
		//记录已完成版本号
		if(version > flushVerFinish) {
			flushVerFinish = version;
		} else {
			LogCore.db.error("删除临时缓存是发现了过期的版本号。flushVerFinish={}, ctx={}", flushVerFinish, context);
		}
		
		//删除刷新临时缓存
		FlushingRecords flush = flushing.getOrCreate(tableName);
		flush.remove(id, version);
	}
	
	/**
	 * 将record新增数据持久化到数据库 占位语句生成
	 * @param record
	 * @return <sqlInto: [属性列], sqlValues, [占位符], params: [参数]>
	 */
	private String syncRecordInsertSQLGen(Record record) {
		//已有缓存 直接返回
		String sql = SQL_INSERT.get(record.getTableName());
		if(sql != null) return sql;
		
		//主体SQL
		sql = "INSERT INTO `{}`({}) VALUES({})";
		
		//拼写缓冲
		StringBuilder sqlInto = new StringBuilder(25 * COL_DEF_LEN);
		StringBuilder sqlValues = new StringBuilder(50);
		
		//信息定义
		FieldSet fieldSet = record.getFieldSet();
		
		//拼写SQL
		for(String name : fieldSet.getFieldNames()) {
			//连接多个时 需要加入分隔符
			if(sqlInto.length() > 0) {
				sqlInto.append(",");
				sqlValues.append(",");
			}
			
			//占位符
			sqlInto.append("`").append(name).append("`");
			sqlValues.append("?");
		}
		
		//最终SQL
		sql = Utils.createStr(sql, record.getTableName(), sqlInto, sqlValues);
		
		//缓存SQL
		SQL_INSERT.put(record.getTableName(), sql);
		
		//日志
		if(log.isDebugEnabled()) {
			log.debug("生成新增SQL语句：table={}, sql={}", record.getTableName(), sql);
		}
		
		return sql;
	}
	
	/**
	 * 将record新增数据持久化到数据库 占位语句生成
	 * @param record
	 * @return <sqlInto: [属性列], sqlValues, [占位符], params: [参数]>
	 */
	private Object[] syncRecordInsertParamsGen(Record record) {
		List<Object> paramList = new ArrayList<>();
		
		//信息定义
		FieldSet fieldSet = record.getFieldSet();
		
		//实际值
		for(String name : fieldSet.getFieldNames()) {
			paramList.add(record.get(name));
		}
		
		//参数拼装备数组 便于之后的操作
		Object[] params = new Object[paramList.size()];
		paramList.toArray(params);
		
		return params;
	}
	
	/**
	 * 将record更新数据持久化到数据库 set语句生成
	 * @param record
	 * @return {sql: sql语句, params: [参数1, 参数2]}
	 */
	private Map<String, Object> syncRecordUpdateSetGen(Record record) {
		StringBuilder sql = new StringBuilder(5 * COL_DEF_LEN);
		List<Object> paramList = new ArrayList<>();
		
		//字段设置
		FieldSet fieldSet = FieldSet.get(record.getTableName());
		
		//拼写SQL
		for(String name : record.getFieldModified()) {
			//主键的更新忽略
			if(Record.PRIMARY_KEY_NAME.equals(name)) continue;
			//记录更新创建时间的字段忽略
			if(CACHE_WRITE_CREATE_TIME_KEY.equals(name)) continue;
			
			//更新值
			Object val = record.get(name);
			
			//验证字符串类型的长度是否符合
			Field f = fieldSet.getField(name);
			if(f.entityType == DBConsts.ENTITY_TYPE_STR) {
				String v = (String) val;
				if(f.columnLen < v.length()) {
					LogCore.db.error("刷新缓存时发现了超出长度的字段，忽略此字段的持久化：strLen={}, strContext={}, field={}, record={}", v.length(), v, f, record);
					continue;
				}
			}
			
			//分隔符
			if(sql.length() > 0) sql.append(",");
			//占位符
			sql.append("`").append(name).append("`=?");
			//实际值
			paramList.add(val);
		}
		
		//参数拼装备数组 便于之后的操作
		Object[] params = new Object[paramList.size()];
		paramList.toArray(params);
		
		return Utils.ofMap("sql", sql.toString(), "params", params);
	}
	
	/**
	 * 初始化FieldSet
	 */
	private void initFieldSet() {
		try {
			ResultSet rs = dbConn.createStatement().executeQuery("select TABLE_NAME from information_schema.tables where table_schema = '" + Config.DB_SCHEMA + "'");
			
			List<String> tableNames = new ArrayList<>();
			while(rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				tableNames.add(tableName);
			}
			
			for(String n : tableNames) {
				initTable(n);
			}
			
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 初始化FieldSet
	 * @param tableName
	 * @throws SQLException
	 */
	private void initTable(String tableName) throws SQLException {
		//缓存FieldSet
		String sql = Utils.createStr("SELECT * FROM `{}` WHERE 0 = 1", tableName);
		ResultSet rs = dbConn.createStatement().executeQuery(sql);
		
		FieldSet fs = new FieldSet(rs.getMetaData());
		FieldSet.put(tableName, fs);
	}
	
	/**
	 * 立即开始一次缓存刷新
	 */
	public void flushNow() {
		flushNowOnce = true;
	}

	@Override
	public Object getId() {
		return Distr.SERV_DB_COMMON;
	}
	
	@Override
	public void pulseOverride() {
		long now = port.getTimeCurrent();
		
		//强制刷新缓存
		if(flushNowOnce) {
			flushDatabase(Long.MAX_VALUE);
			
			//还原刷新标示
			flushNowOnce = false;
		}
				
		//定时刷新缓存数据
		if(flushTimer.isPeriod(now)) {
			flushDatabase(now - Config.DB_CACHED_SYNC_SEC * Time.SEC);
		}
	}
}
