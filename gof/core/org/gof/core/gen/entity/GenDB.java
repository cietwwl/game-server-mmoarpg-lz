package org.gof.core.gen.entity;

import static org.gof.core.gen.entity.Column.COMMENT;
import static org.gof.core.gen.entity.Column.DEFAULTS;
import static org.gof.core.gen.entity.Column.INDEX;
import static org.gof.core.gen.entity.Column.LENGTH;
import static org.gof.core.gen.entity.Column.NAME;
import static org.gof.core.gen.entity.Column.NULLABLE;
import static org.gof.core.gen.entity.Column.TYPE;
import static org.gof.core.gen.entity.Column.TYPE_DEFUALT_DOUBLE;
import static org.gof.core.gen.entity.Column.TYPE_DEFUALT_INT;
import static org.gof.core.gen.entity.Column.TYPE_DEFUALT_LONG;
import static org.gof.core.gen.entity.Column.TYPE_DEFUALT_STRING;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.gof.core.gen.GenBase;
import org.gof.core.gen.GenUtils;
import org.gof.core.support.Config;

public class GenDB {
	private String sourceDir;				// 配置源文件夹
	
	public GenDB(String source) {
		this.sourceDir = source;
	}

	/**
	 * 获取不同类型默认长度
	 * @param clazz
	 * @param obj
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public int getDefaultLength(String type) throws Exception {
		int length = 0;
		if(type == null) {
			throw new RuntimeException("不能识别的类型:" + type);
		}
		
		// 根据不同类型返回不同默认长度
		if("int".equals(type)) {
			length = TYPE_DEFUALT_INT;;
		} else if("long".equals(type)) {
			length = TYPE_DEFUALT_LONG;
		} else if("String".equals(type)) {
			length = TYPE_DEFUALT_STRING;
		} else if("double".equals(type)) {
			length = TYPE_DEFUALT_DOUBLE;
		}
		
		return length;
	}
	 
	public String getSqlType(String type) {
		String result = null;
		if("int".equals(type)) {
			result = "integer";
		} else if("long".equals(type)) {
			result = "bigint";
		} else if("String".equals(type)) {
			result = "varchar";
		} else if("boolean".equals(type)) {
			result = "tinyint";
		} else if("double".equals(type)) {
			result = "double";
		} else if("float".equals(type)) {
			result = "float";
		} 
		
		return result;
	}
	
	/**
	 * 从clazz中获取其中的所有枚举实例
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Enum<?>> getAllEnumInfo(Class<?> clazz) throws Exception {
		ArrayList<Enum<?>> result = new ArrayList<Enum<?>>();
		
		// 获得所有枚举字段成员（id, account, name, profession...）
		Enum<?>[] enums = (Enum[]) clazz.getEnumConstants();
		for(Enum<?> en : enums) {
			// 排除有@Transient注解的字段成员
			if(en.getClass().getField(en.name()).isAnnotationPresent(Transient.class)) {
				continue;
			}
			result.add(en);
		}
		
		// 如果clazz中的@EntityConfig注解设置superEntity了, 则也创建superEntity相应的字段
		Class<?> superEntity = (Class<?>) GenUtils.getPropFromEntityConfig(clazz, "superEntity");
//		if(!superEntity.equals(Object.class)) {
		while(!superEntity.equals(Object.class)) {
			Enum<?>[] superEnums = (Enum<?>[]) superEntity.getEnumConstants();
			if(superEnums != null) {
				for(Enum<?> s : superEnums) {
					// 排除有@Transient注解的字段成员
					if(s.getClass().getField(s.name()).isAnnotationPresent(Transient.class)) {
						continue;
					}
					result.add(s);
				}
			}
			superEntity = (Class<?>) GenUtils.getPropFromEntityConfig(superEntity, "superEntity");
		}
		
		return result;
	}
	
	/**
	 * 获取配置类中的所有字段名
	 * @param clazz
	 * @return
	 * @throws Exception 
	 */
	public List<String> getColumns(Class<?> clazz) throws Exception {
		List<String> result = new ArrayList<String>();
		
		// 获得所有枚举字段成员（id, account, name, profession...）
		ArrayList<Enum<?>> allEnum = getAllEnumInfo(clazz);
		
		// 遍历获取字段名
		result.add("id");	// id是默认添加的 
		for (Enum<?> e : allEnum) {
			result.add(((Enum<?>)e).name().toLowerCase());
		}
		
		return result;
	}
	
	/**
	 * 获取所有约束信息
	 * @param clazz
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getFieldInfo(Class<?> clazz, Object obj) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		
		// 获取所有约束信息
		String name = ((Enum<?>)obj).name();
		String typeName = ((Class<?>) GenUtils.getFieldInfo(obj, "type")).getSimpleName();
		String type = getSqlType(typeName);
		
		int length = (Integer) GenUtils.getFieldInfo(obj, LENGTH);
		boolean index = (Boolean) GenUtils.getFieldInfo(obj, INDEX);
		String nullable = (Boolean) GenUtils.getFieldInfo(obj, NULLABLE) == true ? "NULL" : "NOT NULL";
		String comment = (String) GenUtils.getFieldInfo(obj, COMMENT);
		
		//默认值
		String def = (String) GenUtils.getFieldInfo(obj, DEFAULTS);
		if("boolean".equals(typeName) && !StringUtils.isEmpty(def)) {
			def = Boolean.parseBoolean(def) ? "1" : "0";
		}
		String defaults = def.equals("") ? "" : " DEFAULT '" + def + "'";
		
		// 如果长度为0，即没设长度，则提取默认值 
		if(length == 0) {			
			length = getDefaultLength(typeName);
		}
		
		result.put("name", name);
		result.put(TYPE, type);
		result.put(LENGTH, length);
		result.put(INDEX, index);
		result.put(NULLABLE, nullable);
		result.put(DEFAULTS, defaults);
		result.put(COMMENT, comment);
		
		return result;
	}
	
	/**
	 * 获取表中所有字段信息
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getTableInfo(Class<?> clazz) throws Exception {
		List<Map<String, Object>> tableInfo = new ArrayList<Map<String, Object>>();
		
		// 获得所有枚举字段成员（id, account, name, profession...）
		ArrayList<Enum<?>> allEnum = getAllEnumInfo(clazz);
		
		// 遍历字段成员获取信息
		for (Enum<?> e : allEnum) {
			// 获取字段约束信息
			Map<String, Object> field = getFieldInfo(clazz, e);
			tableInfo.add(field);
		}
		
		return tableInfo;
	}
	
	/**
	 * 获取某个字段的约束信息
	 * @param clazz
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getOneFieldInfo(Class<?> clazz, String name) throws Exception {
		Map<String, Object> fieldInfo = new HashMap<String, Object>();
		
		// 获得所有枚举字段成员（id, account, name, profession...）
		ArrayList<Enum<?>> allEnum = getAllEnumInfo(clazz);
		
		// 遍历所有字段
		for (Enum<?> e : allEnum) {
			// 如果不是想要的字段信息, 则跳过
			if(!((Enum<?>)e).name().equalsIgnoreCase(name)) {
				continue;
			}
			// 获取字段约束信息
			fieldInfo = getFieldInfo(clazz, e);
		}
		
		return fieldInfo;
	}
	
	/**
	 * 获取配置表中需要创建索引的字段
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public List<String> getIndexField(Class<?> clazz) throws Exception {
		List<String> result = new ArrayList<String>();
		
		// 找出class中所有需要创建索引的字段
		Object[] fields = clazz.getEnumConstants();
		for(Object f : fields){
			boolean index = (Boolean) GenUtils.getFieldInfo(f, INDEX);
			if(index) result.add(f.toString());
		}
		
		// 这里还有superEntity里面的索引需要创建！！！
		// 如果clazz中的@EntityConfig注解设置superEntity了, 则也记录superEntity的索引
		Class<?> superEntity = (Class<?>) GenUtils.getPropFromEntityConfig(clazz, "superEntity");
		if(!superEntity.equals(Object.class)) {
			Enum<?>[] superEnums = (Enum<?>[]) superEntity.getEnumConstants();
			if(superEnums != null) {
				for(Enum<?> s : superEnums) {
					boolean index = (Boolean) GenUtils.getFieldInfo(s, INDEX);
					if(index) result.add(s.toString());
				}
			}
		}
		
		return result;
	}
	
	/**
	 * 获取配置表中所有要创建的索引名
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public List<String> getIndexInfo(Class<?> clazz) throws Exception {
		List<String> result = new ArrayList<String>();
		
		// 获取表名，生成索引名（table_column）
		String tableName = (String) GenUtils.getPropFromEntityConfig(clazz, "tableName");
		List<String> fields = getIndexField(clazz);
		for(String f : fields) {
			result.add(tableName + "_" + f);
		}
		
		return result;
	}
	
	/**
	 * 在表上创建索引
	 * @param conn
	 * @param tableName
	 * @param clazz 
	 * @param columns
	 * @throws SQLException
	 */
	public void checkCreateIndex(Connection conn, String tableName, Class<?> clazz) throws Exception {
		// 反射获取配置中待创建索引
		List<String> indexConfs = getIndexInfo(clazz);
		
		// 表中加索引的列信息
		List<String> indexTables = new ArrayList<String>();
		DatabaseMetaData dbMeta = conn.getMetaData();
		String schema = null;
		
		// 获取表中索引信息
		ResultSet indexs = dbMeta.getIndexInfo(null, schema, tableName, false, true);
		while(indexs.next()) {
			indexTables.add(indexs.getString("INDEX_NAME"));
		}
		indexs.close();
		
		// 若数据表索引包含配置类中全部索引，则不用建索引，直接返回
		if(indexTables.containsAll(indexConfs)) {
			return ;
		}
		
		// 找出配置中有，数据表中没有的索引
		List<String> indexDifs = new ArrayList<String>();
		for(String i : indexConfs) {
			if(!indexTables.contains(i)) {
				indexDifs.add(i);
			}
		}
		
		// 创建索引
		Statement st = conn.createStatement();
		for(String index : indexDifs) {
			String column = index.replaceAll(tableName + "_", "");
			String indexSql = "CREATE INDEX " + index + " ON " + tableName +"(" + column + ")";
			System.out.println("建索引: " + indexSql);
			st.executeUpdate(indexSql);
		}
		st.close();
	}
	
	/**
	 * 建表操作
	 * @param conn
	 * @param tableName
	 * @param clazz
	 * @throws Exception
	 */
	public void createTable(Connection conn, String tableName, Class<?> clazz) throws Exception {
		// 拼成SQL语句
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE `").append(tableName).append("`");	// 建表
		sql.append("(");
		sql.append("`id` bigint(20) NOT NULL,");						// 创建默认主键
		
		// 获取并遍历配置表字段
		List<Map<String, Object>> tableInfo = getTableInfo(clazz);
		for(Map<String, Object> t : tableInfo) {
			sql.append("`").append(t.get("name")).append("` ");		// 字段名
			sql.append(t.get(TYPE));								// 类型
			//这里区分一下，创建表的时候double，float，decimal需要指定两个参数(可以默认不指定)，这里区分下
			if(t.get(TYPE).equals("double") || "float".equals(t.get(TYPE)) || "decimal".equals(t.get(TYPE))) {
				sql.append(" ");										// 长度
			} else {
				sql.append("(").append(t.get(LENGTH)).append(") ");		// 长度
			}
			
			sql.append(t.get(NULLABLE));							// 是否为空
			sql.append(t.get(DEFAULTS));							// 默认值
			sql.append(" COMMENT '").append(t.get(COMMENT)).append("'");		//注释
			sql.append(",");
		}
		sql.append("PRIMARY KEY (`id`)");							// 设置主键
		sql.append(")");
		System.out.println("\n建表: " + sql);
		
		// 执行建表操作
		Statement st = conn.createStatement();
		st.executeUpdate(sql.toString());
		st.close();
		
		// 建索引
		checkCreateIndex(conn, tableName, clazz);
	}
	
	/**
	 * 更新表操作
	 * @param con
	 * @param tableName
	 * @param clazz
	 * @throws Exception
	 */
	public void updateTable(Connection con, String tableName, Class<?> clazz) throws Exception {	
		//获取表中列信息
		DatabaseMetaData dBMetaData = con.getMetaData();
		ResultSet colSet = dBMetaData .getColumns(null, "%", tableName, "%");
		
		//表中已有的列名
		List<String> colTables = new ArrayList<String>();
		while(colSet.next()) {
			colTables.add(colSet.getString("COLUMN_NAME").toLowerCase());
		}
		colSet.close();
		
		//配置中的列名
		List<String> colConfs = getColumns(clazz);
		
		// 如果数据表中列名包含配置表中全部列名, 则检查创建索引，不用更新表，直接返回
		if(colTables.containsAll(colConfs)){
			checkCreateIndex(con, tableName, clazz);
			return;
		}
		
		// 找出两表列名不同
		List<String> colDifs = new ArrayList<String>();
		for(String col : colConfs) {
			if(!colTables.contains(col)) {
				colDifs.add(col);
			}
		}
		
		// 取得配置中的表字段信息, 拼成SQL语句
		StringBuffer sql = new StringBuffer();
		sql.append("ALTER TABLE `").append(tableName).append("` ");		// 更新表
		for(int i = 0; i < colDifs.size(); i++) {
			String col = colDifs.get(i);
			Map<String, Object> field = getOneFieldInfo(clazz, col);
			
			if(i > 0) sql.append(", ");
			
			sql.append("ADD `").append(field.get(NAME)).append("` ");				// 增加列名
			sql.append(field.get(TYPE));								// 类型
			//这里区分一下，创建表的时候double，float，decimal需要指定两个参数(可以默认不指定)，这里区分下
			if(field.get(TYPE).equals("double") || "float".equals(field.get(TYPE)) || "decimal".equals(field.get(TYPE))) {
				sql.append(" ");										// 长度
			} else {
				sql.append("(").append(field.get(LENGTH)).append(") ");		// 长度
			}
			sql.append(field.get(NULLABLE));							// 是否为空
			sql.append(field.get(DEFAULTS));							// 默认值
		}
		
		System.out.println("\n更新表: " + sql.toString());
		
		// 更新表操作
		Statement st = con.createStatement();
		st.executeUpdate(sql.toString());
		st.close();
		
		// 建索引
		checkCreateIndex(con, tableName, clazz);
	}
	
	// TODO 数据库连接方面需要改进
	public Connection getDBConnection(String driver, String urlDB, String user, String pwd) throws Exception {
		// 连接MYSQL数据库
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(urlDB, user, pwd);
		
		return conn;
	}
	
	/**
	 * 根据配置源文件夹检查建数据表
	 */
	public void genDB(Connection conn) {
		try {
			// 获取源文件夹下的所有类
			Set<Class<?>> sources = GenBase.getSources(sourceDir);
			
			// 遍历所有类，取出有注解的生成实体类
			for(Class<?> clazz : sources) {
				// 过滤没有EntityConfig注解的类, 并建表
				if(clazz.isAnnotationPresent(Entity.class)) {
					// 获取@EntityConfig上的isSuper
					boolean isSuper = (boolean) GenUtils.getPropFromEntityConfig(clazz, "isSuper");
					
					// 若isSuper为true则不生成表，也不检查表
					if(isSuper) 	continue;

					checkAndCreat(clazz, conn);
				}
			}
			
			// 关闭连接
			conn.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 检查并建表或更新表结构
	 * @param clazz
	 * @throws Exception
	 */
	public void checkAndCreat(Class<?> clazz, Connection conn) throws Exception {
		
		// 获取表的信息
		String catalog = null;
		String schema = "%";
		String tableName = ((String) GenUtils.getPropFromEntityConfig(clazz, "tableName")).toLowerCase();
		String[] types = new String[] { "TABLE" };
		DatabaseMetaData dBMetaData = conn.getMetaData();
		
		// 从databaseMetaData获取表信息
		ResultSet tableSet = dBMetaData.getTables(catalog, schema, tableName, types);
		
		// 如果表不存在, 则建表
		if (!tableSet.next()) {
			createTable(conn, tableName, clazz);
		} else {	//表存在, 则更新表
			updateTable(conn, tableName, clazz);
		}
		
		// 关闭数据库连接
		tableSet.close();
	}
	
	/**
	 * 给部署服务器时调用新建数据库
	 * @param args 一个参数要搜索的源文件夹
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String sourceDir = args[0];
		
		//设置log4j2配置文件所需的环境变量，作用是gen的时候
		//不会报配置没找到的错误，同时有gen.log的日志记录
		System.setProperty("logFileName", "genDB");
		
		GenDB genDB = new GenDB(sourceDir);
		Connection conn = genDB.getDBConnection("com.mysql.jdbc.Driver", Config.DB_URL, Config.DB_USER, Config.DB_PWD);
		genDB.genDB(conn);
		
		System.out.println("执行完毕，如果没有输出则说明无需建表或者无需更新表结构。");
		
		//正常退出
		System.exit(0);
	}

}
