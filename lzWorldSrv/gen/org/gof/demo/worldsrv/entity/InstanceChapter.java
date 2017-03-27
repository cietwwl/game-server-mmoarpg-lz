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
public final class InstanceChapter extends EntityBase {
	public static final String tableName = "demo_instance";

	/**
	 * 属性关键字
	 */
	public static final class K {
		public static final String id = "id";	//id
		public static final String humanId = "humanId";	//所属用户
		public static final String chapterSn = "chapterSn";	//章节SN
		public static final String chapterBoxJson = "chapterBoxJson";	//每个章节对应三个宝箱的领取状态
		public static final String repStarJson = "repStarJson";	//章节副本对应完成进度（星星）repID:[1,1,1]
		public static final String repAttNumJson = "repAttNumJson";	//章节副本对应完成次数存放的是repID:attNum
		public static final String repResetJson = "repResetJson";	//章节副本对应花钱增加次数repID:resetNum
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public InstanceChapter() {
		super();
		setChapterBoxJson("{}");
		setRepStarJson("{}");
		setRepAttNumJson("{}");
		setRepResetJson("{}");
	}

	public InstanceChapter(Record record) {
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
	 * 所属用户
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
	 * 章节SN
	 */
	public int getChapterSn() {
		return record.get("chapterSn");
	}

	public void setChapterSn(final int chapterSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("chapterSn", chapterSn);

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
	public void setNoChangeChapterSn(final int chapterSn) {
		//更新属性
		record.setNoUpdate("chapterSn", chapterSn);
	}
	/**
	 * 每个章节对应三个宝箱的领取状态
	 */
	public String getChapterBoxJson() {
		return record.get("chapterBoxJson");
	}

	public void setChapterBoxJson(final String chapterBoxJson) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("chapterBoxJson", chapterBoxJson);

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
	public void setNoChangeChapterBoxJson(final String chapterBoxJson) {
		//更新属性
		record.setNoUpdate("chapterBoxJson", chapterBoxJson);
	}
	/**
	 * 章节副本对应完成进度（星星）repID:[1,1,1]
	 */
	public String getRepStarJson() {
		return record.get("repStarJson");
	}

	public void setRepStarJson(final String repStarJson) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("repStarJson", repStarJson);

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
	public void setNoChangeRepStarJson(final String repStarJson) {
		//更新属性
		record.setNoUpdate("repStarJson", repStarJson);
	}
	/**
	 * 章节副本对应完成次数存放的是repID:attNum
	 */
	public String getRepAttNumJson() {
		return record.get("repAttNumJson");
	}

	public void setRepAttNumJson(final String repAttNumJson) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("repAttNumJson", repAttNumJson);

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
	public void setNoChangeRepAttNumJson(final String repAttNumJson) {
		//更新属性
		record.setNoUpdate("repAttNumJson", repAttNumJson);
	}
	/**
	 * 章节副本对应花钱增加次数repID:resetNum
	 */
	public String getRepResetJson() {
		return record.get("repResetJson");
	}

	public void setRepResetJson(final String repResetJson) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("repResetJson", repResetJson);

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
	public void setNoChangeRepResetJson(final String repResetJson) {
		//更新属性
		record.setNoUpdate("repResetJson", repResetJson);
	}

}