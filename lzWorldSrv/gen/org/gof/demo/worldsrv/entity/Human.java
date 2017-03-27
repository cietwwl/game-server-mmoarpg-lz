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
public final class Human extends Unit {
	public static final String tableName = "demo_human";

	/**
	 * 属性关键字
	 */
	public static final class K extends SuperK {
		public static final String id = "id";	//id
		public static final String serverId = "serverId";	//服务器编号
		public static final String account = "account";	//账号
		public static final String timeSecOnline = "timeSecOnline";	//在线时间
		public static final String timeLogin = "timeLogin";	//最后一次登录时间
		public static final String timeLogout = "timeLogout";	//最后一次登出时间
		public static final String timeCreate = "timeCreate";	//角色创建时间
		public static final String sessionKey = "sessionKey";	//角色SessionKey
		public static final String gameOptions = "gameOptions";	//游戏设置
		public static final String fightAutoOption = "fightAutoOption";	//玩家挂机设置
		public static final String fightGeneralJSON = "fightGeneralJSON";	//不同玩法的阵容[{t,r},{}...]
		public static final String vipLevel = "vipLevel";	//VIP等级
		public static final String gold = "gold";	//元宝
		public static final String coin = "coin";	//铜币
		public static final String actValue = "actValue";	//活力
		public static final String competMoney = "competMoney";	//演武币
		public static final String stageHistory = "stageHistory";	//地图位置信息，{{id, sn ,x ,y ,common},{}}
		public static final String actValueMax = "actValueMax";	//活力最大值
		public static final String minghunIndex = "minghunIndex";	//现在命魂的层次
		public static final String shopJson = "shopJson";	//商店相关信息
		public static final String allGeneral = "allGeneral";	//玩家招募过的所有武将
		public static final String bag1Cap = "bag1Cap";	//主背包容量
		public static final String bag2Cap = "bag2Cap";	//次背包容量
		public static final String propUseLimitCounts = "propUseLimitCounts";	//物品每日使用限制次数
		public static final String trCounts = "trCounts";	//抽奖次数记录
		public static final String freeTrTime = "freeTrTime";	//免费抽奖时间记录
		public static final String tr3ItemSn = "tr3ItemSn";	//高级宝藏三个物件SN
		public static final String questIdCompletedJSON = "questIdCompletedJSON";	//已完成任务
		public static final String questNormalJSON = "questNormalJSON";	//成长任务
		public static final String questDailyJSON = "questDailyJSON";	//日常任务
		public static final String questInstDailyJSON = "questInstDailyJSON";	//副本任务
		public static final String questInstCount = "questInstCount";	//副本任务刷新剩余次数
		public static final String fragInfo = "fragInfo";	//伙伴碎片  {碎片sn：数量}
		public static final String genTaskFightTimes = "genTaskFightTimes";	//伙伴副本挑战次数{副本类型：当日挑战次数}
		public static final String liveness = "liveness";	//每日活跃度
		public static final String livenessAwardsRsvd = "livenessAwardsRsvd";	//已经领取的每日活跃度奖励
		public static final String competeCount = "competeCount";	//竞技场剩余次数
		public static final String competeRefreshCount = "competeRefreshCount";	//竞技场刷新次数
		public static final String competeLastTime = "competeLastTime";	//上次竞技时间
	}

	@Override
	public String getTableName() {
		return tableName;
	}
	
	public Human() {
		super();
		setFightGeneralJSON("[]");
		setVipLevel(0);
		setShopJson("[]");
		setTr3ItemSn("[]");
		setQuestIdCompletedJSON("[]");
		setQuestNormalJSON("[]");
		setQuestDailyJSON("[]");
		setQuestInstDailyJSON("[]");
		setFragInfo("{}");
		setGenTaskFightTimes("{}");
		setLiveness("[]");
		setLivenessAwardsRsvd("[]");
	}

	public Human(Record record) {
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
	 * 服务器编号
	 */
	public int getServerId() {
		return record.get("serverId");
	}

	public void setServerId(final int serverId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("serverId", serverId);

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
	public void setNoChangeServerId(final int serverId) {
		//更新属性
		record.setNoUpdate("serverId", serverId);
	}
	/**
	 * 账号
	 */
	public String getAccount() {
		return record.get("account");
	}

	public void setAccount(final String account) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("account", account);

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
	public void setNoChangeAccount(final String account) {
		//更新属性
		record.setNoUpdate("account", account);
	}
	/**
	 * 在线时间
	 */
	public int getTimeSecOnline() {
		return record.get("timeSecOnline");
	}

	public void setTimeSecOnline(final int timeSecOnline) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("timeSecOnline", timeSecOnline);

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
	public void setNoChangeTimeSecOnline(final int timeSecOnline) {
		//更新属性
		record.setNoUpdate("timeSecOnline", timeSecOnline);
	}
	/**
	 * 最后一次登录时间
	 */
	public long getTimeLogin() {
		return record.get("timeLogin");
	}

	public void setTimeLogin(final long timeLogin) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("timeLogin", timeLogin);

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
	public void setNoChangeTimeLogin(final long timeLogin) {
		//更新属性
		record.setNoUpdate("timeLogin", timeLogin);
	}
	/**
	 * 最后一次登出时间
	 */
	public long getTimeLogout() {
		return record.get("timeLogout");
	}

	public void setTimeLogout(final long timeLogout) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("timeLogout", timeLogout);

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
	public void setNoChangeTimeLogout(final long timeLogout) {
		//更新属性
		record.setNoUpdate("timeLogout", timeLogout);
	}
	/**
	 * 角色创建时间
	 */
	public long getTimeCreate() {
		return record.get("timeCreate");
	}

	public void setTimeCreate(final long timeCreate) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("timeCreate", timeCreate);

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
	public void setNoChangeTimeCreate(final long timeCreate) {
		//更新属性
		record.setNoUpdate("timeCreate", timeCreate);
	}
	/**
	 * 角色SessionKey
	 */
	public long getSessionKey() {
		return record.get("sessionKey");
	}

	public void setSessionKey(final long sessionKey) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("sessionKey", sessionKey);

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
	public void setNoChangeSessionKey(final long sessionKey) {
		//更新属性
		record.setNoUpdate("sessionKey", sessionKey);
	}
	/**
	 * 游戏设置
	 */
	public String getGameOptions() {
		return record.get("gameOptions");
	}

	public void setGameOptions(final String gameOptions) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("gameOptions", gameOptions);

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
	public void setNoChangeGameOptions(final String gameOptions) {
		//更新属性
		record.setNoUpdate("gameOptions", gameOptions);
	}
	/**
	 * 玩家挂机设置
	 */
	public String getFightAutoOption() {
		return record.get("fightAutoOption");
	}

	public void setFightAutoOption(final String fightAutoOption) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("fightAutoOption", fightAutoOption);

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
	public void setNoChangeFightAutoOption(final String fightAutoOption) {
		//更新属性
		record.setNoUpdate("fightAutoOption", fightAutoOption);
	}
	/**
	 * 不同玩法的阵容[{t,r},{}...]
	 */
	public String getFightGeneralJSON() {
		return record.get("fightGeneralJSON");
	}

	public void setFightGeneralJSON(final String fightGeneralJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("fightGeneralJSON", fightGeneralJSON);

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
	public void setNoChangeFightGeneralJSON(final String fightGeneralJSON) {
		//更新属性
		record.setNoUpdate("fightGeneralJSON", fightGeneralJSON);
	}
	/**
	 * VIP等级
	 */
	public int getVipLevel() {
		return record.get("vipLevel");
	}

	public void setVipLevel(final int vipLevel) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("vipLevel", vipLevel);

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
	public void setNoChangeVipLevel(final int vipLevel) {
		//更新属性
		record.setNoUpdate("vipLevel", vipLevel);
	}
	/**
	 * 元宝
	 */
	public long getGold() {
		return record.get("gold");
	}

	public void setGold(final long gold) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("gold", gold);

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
	public void setNoChangeGold(final long gold) {
		//更新属性
		record.setNoUpdate("gold", gold);
	}
	/**
	 * 铜币
	 */
	public long getCoin() {
		return record.get("coin");
	}

	public void setCoin(final long coin) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("coin", coin);

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
	public void setNoChangeCoin(final long coin) {
		//更新属性
		record.setNoUpdate("coin", coin);
	}
	/**
	 * 活力
	 */
	public long getActValue() {
		return record.get("actValue");
	}

	public void setActValue(final long actValue) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("actValue", actValue);

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
	public void setNoChangeActValue(final long actValue) {
		//更新属性
		record.setNoUpdate("actValue", actValue);
	}
	/**
	 * 演武币
	 */
	public long getCompetMoney() {
		return record.get("competMoney");
	}

	public void setCompetMoney(final long competMoney) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("competMoney", competMoney);

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
	public void setNoChangeCompetMoney(final long competMoney) {
		//更新属性
		record.setNoUpdate("competMoney", competMoney);
	}
	/**
	 * 地图位置信息，{{id, sn ,x ,y ,common},{}}
	 */
	public String getStageHistory() {
		return record.get("stageHistory");
	}

	public void setStageHistory(final String stageHistory) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("stageHistory", stageHistory);

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
	public void setNoChangeStageHistory(final String stageHistory) {
		//更新属性
		record.setNoUpdate("stageHistory", stageHistory);
	}
	/**
	 * 活力最大值
	 */
	public long getActValueMax() {
		return record.get("actValueMax");
	}

	public void setActValueMax(final long actValueMax) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("actValueMax", actValueMax);

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
	public void setNoChangeActValueMax(final long actValueMax) {
		//更新属性
		record.setNoUpdate("actValueMax", actValueMax);
	}
	/**
	 * 现在命魂的层次
	 */
	public int getMinghunIndex() {
		return record.get("minghunIndex");
	}

	public void setMinghunIndex(final int minghunIndex) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("minghunIndex", minghunIndex);

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
	public void setNoChangeMinghunIndex(final int minghunIndex) {
		//更新属性
		record.setNoUpdate("minghunIndex", minghunIndex);
	}
	/**
	 * 商店相关信息
	 */
	public String getShopJson() {
		return record.get("shopJson");
	}

	public void setShopJson(final String shopJson) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("shopJson", shopJson);

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
	public void setNoChangeShopJson(final String shopJson) {
		//更新属性
		record.setNoUpdate("shopJson", shopJson);
	}
	/**
	 * 玩家招募过的所有武将
	 */
	public String getAllGeneral() {
		return record.get("allGeneral");
	}

	public void setAllGeneral(final String allGeneral) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("allGeneral", allGeneral);

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
	public void setNoChangeAllGeneral(final String allGeneral) {
		//更新属性
		record.setNoUpdate("allGeneral", allGeneral);
	}
	/**
	 * 主背包容量
	 */
	public int getBag1Cap() {
		return record.get("bag1Cap");
	}

	public void setBag1Cap(final int bag1Cap) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("bag1Cap", bag1Cap);

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
	public void setNoChangeBag1Cap(final int bag1Cap) {
		//更新属性
		record.setNoUpdate("bag1Cap", bag1Cap);
	}
	/**
	 * 次背包容量
	 */
	public int getBag2Cap() {
		return record.get("bag2Cap");
	}

	public void setBag2Cap(final int bag2Cap) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("bag2Cap", bag2Cap);

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
	public void setNoChangeBag2Cap(final int bag2Cap) {
		//更新属性
		record.setNoUpdate("bag2Cap", bag2Cap);
	}
	/**
	 * 物品每日使用限制次数
	 */
	public String getPropUseLimitCounts() {
		return record.get("propUseLimitCounts");
	}

	public void setPropUseLimitCounts(final String propUseLimitCounts) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("propUseLimitCounts", propUseLimitCounts);

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
	public void setNoChangePropUseLimitCounts(final String propUseLimitCounts) {
		//更新属性
		record.setNoUpdate("propUseLimitCounts", propUseLimitCounts);
	}
	/**
	 * 抽奖次数记录
	 */
	public String getTrCounts() {
		return record.get("trCounts");
	}

	public void setTrCounts(final String trCounts) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("trCounts", trCounts);

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
	public void setNoChangeTrCounts(final String trCounts) {
		//更新属性
		record.setNoUpdate("trCounts", trCounts);
	}
	/**
	 * 免费抽奖时间记录
	 */
	public String getFreeTrTime() {
		return record.get("freeTrTime");
	}

	public void setFreeTrTime(final String freeTrTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("freeTrTime", freeTrTime);

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
	public void setNoChangeFreeTrTime(final String freeTrTime) {
		//更新属性
		record.setNoUpdate("freeTrTime", freeTrTime);
	}
	/**
	 * 高级宝藏三个物件SN
	 */
	public String getTr3ItemSn() {
		return record.get("tr3ItemSn");
	}

	public void setTr3ItemSn(final String tr3ItemSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("tr3ItemSn", tr3ItemSn);

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
	public void setNoChangeTr3ItemSn(final String tr3ItemSn) {
		//更新属性
		record.setNoUpdate("tr3ItemSn", tr3ItemSn);
	}
	/**
	 * 已完成任务
	 */
	public String getQuestIdCompletedJSON() {
		return record.get("questIdCompletedJSON");
	}

	public void setQuestIdCompletedJSON(final String questIdCompletedJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("questIdCompletedJSON", questIdCompletedJSON);

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
	public void setNoChangeQuestIdCompletedJSON(final String questIdCompletedJSON) {
		//更新属性
		record.setNoUpdate("questIdCompletedJSON", questIdCompletedJSON);
	}
	/**
	 * 成长任务
	 */
	public String getQuestNormalJSON() {
		return record.get("questNormalJSON");
	}

	public void setQuestNormalJSON(final String questNormalJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("questNormalJSON", questNormalJSON);

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
	public void setNoChangeQuestNormalJSON(final String questNormalJSON) {
		//更新属性
		record.setNoUpdate("questNormalJSON", questNormalJSON);
	}
	/**
	 * 日常任务
	 */
	public String getQuestDailyJSON() {
		return record.get("questDailyJSON");
	}

	public void setQuestDailyJSON(final String questDailyJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("questDailyJSON", questDailyJSON);

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
	public void setNoChangeQuestDailyJSON(final String questDailyJSON) {
		//更新属性
		record.setNoUpdate("questDailyJSON", questDailyJSON);
	}
	/**
	 * 副本任务
	 */
	public String getQuestInstDailyJSON() {
		return record.get("questInstDailyJSON");
	}

	public void setQuestInstDailyJSON(final String questInstDailyJSON) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("questInstDailyJSON", questInstDailyJSON);

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
	public void setNoChangeQuestInstDailyJSON(final String questInstDailyJSON) {
		//更新属性
		record.setNoUpdate("questInstDailyJSON", questInstDailyJSON);
	}
	/**
	 * 副本任务刷新剩余次数
	 */
	public int getQuestInstCount() {
		return record.get("questInstCount");
	}

	public void setQuestInstCount(final int questInstCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("questInstCount", questInstCount);

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
	public void setNoChangeQuestInstCount(final int questInstCount) {
		//更新属性
		record.setNoUpdate("questInstCount", questInstCount);
	}
	/**
	 * 伙伴碎片  {碎片sn：数量}
	 */
	public String getFragInfo() {
		return record.get("fragInfo");
	}

	public void setFragInfo(final String fragInfo) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("fragInfo", fragInfo);

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
	public void setNoChangeFragInfo(final String fragInfo) {
		//更新属性
		record.setNoUpdate("fragInfo", fragInfo);
	}
	/**
	 * 伙伴副本挑战次数{副本类型：当日挑战次数}
	 */
	public String getGenTaskFightTimes() {
		return record.get("genTaskFightTimes");
	}

	public void setGenTaskFightTimes(final String genTaskFightTimes) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("genTaskFightTimes", genTaskFightTimes);

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
	public void setNoChangeGenTaskFightTimes(final String genTaskFightTimes) {
		//更新属性
		record.setNoUpdate("genTaskFightTimes", genTaskFightTimes);
	}
	/**
	 * 每日活跃度
	 */
	public String getLiveness() {
		return record.get("liveness");
	}

	public void setLiveness(final String liveness) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("liveness", liveness);

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
	public void setNoChangeLiveness(final String liveness) {
		//更新属性
		record.setNoUpdate("liveness", liveness);
	}
	/**
	 * 已经领取的每日活跃度奖励
	 */
	public String getLivenessAwardsRsvd() {
		return record.get("livenessAwardsRsvd");
	}

	public void setLivenessAwardsRsvd(final String livenessAwardsRsvd) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("livenessAwardsRsvd", livenessAwardsRsvd);

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
	public void setNoChangeLivenessAwardsRsvd(final String livenessAwardsRsvd) {
		//更新属性
		record.setNoUpdate("livenessAwardsRsvd", livenessAwardsRsvd);
	}
	/**
	 * 竞技场剩余次数
	 */
	public int getCompeteCount() {
		return record.get("competeCount");
	}

	public void setCompeteCount(final int competeCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("competeCount", competeCount);

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
	public void setNoChangeCompeteCount(final int competeCount) {
		//更新属性
		record.setNoUpdate("competeCount", competeCount);
	}
	/**
	 * 竞技场刷新次数
	 */
	public int getCompeteRefreshCount() {
		return record.get("competeRefreshCount");
	}

	public void setCompeteRefreshCount(final int competeRefreshCount) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("competeRefreshCount", competeRefreshCount);

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
	public void setNoChangeCompeteRefreshCount(final int competeRefreshCount) {
		//更新属性
		record.setNoUpdate("competeRefreshCount", competeRefreshCount);
	}
	/**
	 * 上次竞技时间
	 */
	public long getCompeteLastTime() {
		return record.get("competeLastTime");
	}

	public void setCompeteLastTime(final long competeLastTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("competeLastTime", competeLastTime);

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
	public void setNoChangeCompeteLastTime(final long competeLastTime) {
		//更新属性
		record.setNoUpdate("competeLastTime", competeLastTime);
	}

}