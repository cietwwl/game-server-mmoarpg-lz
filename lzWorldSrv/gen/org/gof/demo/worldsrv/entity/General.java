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
import org.gof.demo.worldsrv.entity.Unit;
import org.gof.core.gen.GofGenFile;

@GofGenFile
public final class General extends Unit {
	public static final String tableName = "demo_general";

	/**
	 * 属性关键字
	 */
	public static final class K extends SuperK {
		public static final String id = "id";	//id
		public static final String humanID = "humanID";	//所属的ID
		public static final String star = "star";	//伙伴星级
		public static final String quality = "quality";	//伙伴品质
		public static final String equip = "equip";	//伙伴装备信息
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public General() {
		super();
		setEquip("[]");
	}

	public General(Record record) {
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
	 * 所属的ID
	 */
	public long getHumanID() {
		return record.get("humanID");
	}

	public void setHumanID(final long humanID) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("humanID", humanID);

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
	public void setNoChangeHumanID(final long humanID) {
		//更新属性
		record.setNoUpdate("humanID", humanID);
	}
	/**
	 * 伙伴星级
	 */
	public int getStar() {
		return record.get("star");
	}

	public void setStar(final int star) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("star", star);

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
	public void setNoChangeStar(final int star) {
		//更新属性
		record.setNoUpdate("star", star);
	}
	/**
	 * 伙伴品质
	 */
	public int getQuality() {
		return record.get("quality");
	}

	public void setQuality(final int quality) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("quality", quality);

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
	public void setNoChangeQuality(final int quality) {
		//更新属性
		record.setNoUpdate("quality", quality);
	}
	/**
	 * 伙伴装备信息
	 */
	public String getEquip() {
		return record.get("equip");
	}

	public void setEquip(final String equip) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("equip", equip);

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
	public void setNoChangeEquip(final String equip) {
		//更新属性
		record.setNoUpdate("equip", equip);
	}

}