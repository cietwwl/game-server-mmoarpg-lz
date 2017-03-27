package org.gof.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.gof.core.support.SysException;
import org.gof.core.support.log.LogCore;
import org.slf4j.Logger;

/**
 * 数据库连接对象
 * 由于架构单线程无并发的特点 不使用连接池 保持一个连接即可
 */
public class DBConnection {
	private Logger log = LogCore.db;
	
	private String dbUrl;			//数据库连接URL
	private String dbUser;			//用户名
	private String dbPwd;			//密码
	
	/**
	 * 构造函数
	 * @param url
	 * @param user
	 * @param pwd
	 */
	public DBConnection(String url, String user, String pwd) {
		try {
			this.dbUrl = url;
			this.dbUser = user;
			this.dbPwd = pwd;
			
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 获取PrepareStatement对象
	 * @param sql
	 * @return
	 */
	public PreparedStatement prepareStatement(String sql) {
		try {
			return getConnection().prepareStatement(sql);
		} catch (SQLException e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 获取Statement对象
	 * @return
	 */
	public Statement createStatement() {
		try {
			return getConnection().createStatement();
		} catch (SQLException e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 关闭
	 */
	public void close() {
		try {
			if(CONN != null) {
				CONN.close();
			}
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	
	/**
	 * 获取数据库连接
	 * @return
	 */
	private Connection getConnection() {
		try {
			if(CONN == null || !CONN.isValid(0)) {
				CONN = DriverManager.getConnection(dbUrl, dbUser, dbPwd);
				//记录日志
				if(log.isInfoEnabled()) {
					log.info("创建新的数据库连接。");
				}
			}
			
			return CONN;
		} catch (Exception e) {
			throw new SysException(e);
		}
	}
	private Connection CONN = null;			//保持连接 不要直接使用这个属性来获取连接
}
