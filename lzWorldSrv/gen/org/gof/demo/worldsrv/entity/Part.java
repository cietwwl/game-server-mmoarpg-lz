package org.gof.demo.worldsrv.entity;

import org.apache.commons.lang3.exception.ExceptionUtils;

import org.gof.core.db.DBConsts;
import org.gof.core.Chunk;
import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.support.BufferPool;
import org.gof.core.support.SysException;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.support.log.LogCore;
import org.gof.core.entity.EntityBase;
import org.gof.core.gen.GofGenFile;

@GofGenFile
public final class Part extends EntityBase {
	public static final String tableName = "demo_part";

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String id = "id";	//id
		public static final String sn = "sn";	//装备位编号SN
		public static final String humanId = "humanId";	//玩家ID
		public static final String qianghuaLv = "qianghuaLv";	//强化等级
		public static final String chongxingLv = "chongxingLv";	//冲星等级
		public static final String propJson = "propJson";	//总属性
		public static final String equipId = "equipId";	//装备ID
		public static final String gemsIds = "gemsIds";	//宝石数组ID
		public static final String enabled = "enabled";	//是否激活
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public Part() {
		super();
	}

	public Part(Record record) {
		super(record);
	}

	
	/**
	 * 新增数据
	 */
	@Override
	public void persist() {
		//状态错误
		if(record.getStatus() != DBConsts.RECORD_STATUS_NEW) {
			LogCore.db.error("只有新增包能调用persist函数，请确认状态：data={}, stackTrace={}", this, ExceptionUtils.getStackTrace(new Throwable()));
			return;
		}
		
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.insert(record);
		
		//重置状态
		record.resetStatus();
	}
	/**
	 * 提交SetNoChange 没有入库的进入 数据库写缓存队列
	 * 这里不是立即入库的
	 */
	public void commitNoChange() {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//提交属性
		record.commitToUpdate();

		//更新后的数据状态
		int statusNew = record.getStatus();
		
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	/**
	 * 同步修改数据至DB服务器
	 * 默认不立即持久化到数据库
	 */
	@Override
	public void update() {
		update(false);
	}
	
	/**
	 * 同步修改数据至DB服务器
	 * @param sync 是否立即同持久化到数据库
	 */
	@Override
	public void update(boolean sync) {
		//新增包不能直接调用update函数 请先调用persist
		if(record.getStatus() == DBConsts.RECORD_STATUS_NEW) {
			throw new SysException("新增包不能直接调用update函数，请先调用persist：data={}", this);
		}
		
		//升级包
		Chunk path = record.pathUpdateGen();
		if(path == null || path.length == 0) return;

		//将升级包同步至DB服务器
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.update(getTableName(), getId(), path, sync);
		
		//回收缓冲包
		BufferPool.deallocate(path.buffer);
		
		//重置状态
		record.resetStatus();
	}

	/**
	 * 删除数据
	 */
	@Override
	public void remove() {
		DBServiceProxy prx = DBServiceProxy.newInstance();
		prx.delete(getTableName(), getId());
	}

	/**
	 * id
	 */
	public long getId() {
		return record.get("id");
	}

	public void setId(final long id) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("id", id);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangeId(final long id) {
		//更新属性
		record.setNoUpdate("id", id);
	}
	/**
	 * 装备位编号SN
	 */
	public int getSn() {
		return record.get("sn");
	}

	public void setSn(final int sn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("sn", sn);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangeSn(final int sn) {
		//更新属性
		record.setNoUpdate("sn", sn);
	}
	/**
	 * 玩家ID
	 */
	public long getHumanId() {
		return record.get("humanId");
	}

	public void setHumanId(final long humanId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("humanId", humanId);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangeHumanId(final long humanId) {
		//更新属性
		record.setNoUpdate("humanId", humanId);
	}
	/**
	 * 强化等级
	 */
	public int getQianghuaLv() {
		return record.get("qianghuaLv");
	}

	public void setQianghuaLv(final int qianghuaLv) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("qianghuaLv", qianghuaLv);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangeQianghuaLv(final int qianghuaLv) {
		//更新属性
		record.setNoUpdate("qianghuaLv", qianghuaLv);
	}
	/**
	 * 冲星等级
	 */
	public String getChongxingLv() {
		return record.get("chongxingLv");
	}

	public void setChongxingLv(final String chongxingLv) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("chongxingLv", chongxingLv);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangeChongxingLv(final String chongxingLv) {
		//更新属性
		record.setNoUpdate("chongxingLv", chongxingLv);
	}
	/**
	 * 总属性
	 */
	public String getPropJson() {
		return record.get("propJson");
	}

	public void setPropJson(final String propJson) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("propJson", propJson);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangePropJson(final String propJson) {
		//更新属性
		record.setNoUpdate("propJson", propJson);
	}
	/**
	 * 装备ID
	 */
	public long getEquipId() {
		return record.get("equipId");
	}

	public void setEquipId(final long equipId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("equipId", equipId);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangeEquipId(final long equipId) {
		//更新属性
		record.setNoUpdate("equipId", equipId);
	}
	/**
	 * 宝石数组ID
	 */
	public String getGemsIds() {
		return record.get("gemsIds");
	}

	public void setGemsIds(final String gemsIds) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("gemsIds", gemsIds);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangeGemsIds(final String gemsIds) {
		//更新属性
		record.setNoUpdate("gemsIds", gemsIds);
	}
	/**
	 * 是否激活
	 */
	public boolean isEnabled() {
		return record.<Integer>get("enabled") == 1;
	}

	public void setEnabled(boolean enabled) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("enabled", enabled ? 1 : 0);

		//更新后的数据状态
		int statusNew = record.getStatus();
		//1.如果更新前是普通状态 and 更新后是修改状态，那么就记录这条数据，用来稍后自动提交。
		//2.哪怕之前是修改状态，只要数据是刚创建或串行化过来的新对象，则也会记录修改，因为有些时候会串行化过来一个修改状态下的数据。
		if((statusOld == DBConsts.RECORD_STATUS_NONE && statusNew == DBConsts.RECORD_STATUS_MODIFIED) ||
		   (statusOld == DBConsts.RECORD_STATUS_MODIFIED && record.isNewness())) {
			//记录修改的数据 用来稍后自动提交
			Port.getCurrent().addEntityModify(this);
			//如果是刚创建或串行化过来的新对象 取消这个标示
			if(record.isNewness()) {
				record.setNewness(false);
			}
		}
	}
	
	/**
	* 修改VO 不入库不建议使用
	*/
	@Deprecated
	public void setNoChangeEnabled(final boolean enabled) {
		//更新属性
		record.setNoUpdate("enabled", enabled ? 1 : 0);
	}

}