package org.gof.demo.worldsrv.entity;

import org.gof.core.db.DBConsts;
import org.gof.core.Port;
import org.gof.core.Record;
import org.gof.core.entity.EntityBase;
import org.gof.core.gen.GofGenFile;

@GofGenFile
public abstract class Unit extends EntityBase {
	public Unit() {
		super();
		setCombat(1000);
		setAttingIndex(-1);
		setSkill("[]");
		setInborn("[]");
	}

	public Unit(Record record) {
		super(record);
	}
	
	/**
	 * 属性关键字
	 */
	public static class SuperK {
		public static final String id = "id";	//id
		public static final String hpMax = "hpMax";	//生命值上限
		public static final String hpMaxPct = "hpMaxPct";	//生命值上限百分比
		public static final String mpMax = "mpMax";	//法力值上限
		public static final String mpMaxPct = "mpMaxPct";	//法力值上限百分比
		public static final String atkPhy = "atkPhy";	//阳属性攻击(物理攻击)
		public static final String atkPhyPct = "atkPhyPct";	//阳属性攻击(物理攻击)百分比
		public static final String defPhy = "defPhy";	//阳属性防御(物理防御)
		public static final String defPhyPct = "defPhyPct";	//阳属性防御(物理防御)百分比
		public static final String atkMag = "atkMag";	//阴属性攻击(魔法攻击)
		public static final String atkMagPct = "atkMagPct";	//阴属性攻击(魔法攻击)百分比
		public static final String defMag = "defMag";	//阴属性防御(魔法防御)
		public static final String defMagPct = "defMagPct";	//阴属性防御(魔法防御)百分比
		public static final String hit = "hit";	//命中
		public static final String hitPct = "hitPct";	//命中百分比
		public static final String dodge = "dodge";	//闪避
		public static final String dodgePct = "dodgePct";	//闪避百分比
		public static final String crit = "crit";	//暴击概率(暴击)
		public static final String critPct = "critPct";	//暴击概率(暴击)百分比
		public static final String critAdd = "critAdd";	//暴击比例(暴击加成)
		public static final String critAddPct = "critAddPct";	//暴击比例(暴击加成)百分比
		public static final String tough = "tough";	//坚韧
		public static final String toughPct = "toughPct";	//坚韧百分比
		public static final String elem1Atk = "elem1Atk";	//元素1伤害
		public static final String elem1AtkPct = "elem1AtkPct";	//元素1伤害百分比
		public static final String elem1Def = "elem1Def";	//元素1防御
		public static final String elem1DefPct = "elem1DefPct";	//元素1防御百分比
		public static final String elem2Atk = "elem2Atk";	//元素2伤害
		public static final String elem2AtkPct = "elem2AtkPct";	//元素2伤害百分比
		public static final String elem2Def = "elem2Def";	//元素2防御
		public static final String elem2DefPct = "elem2DefPct";	//元素2防御百分比
		public static final String elem3Atk = "elem3Atk";	//元素3伤害
		public static final String elem3AtkPct = "elem3AtkPct";	//元素3伤害百分比
		public static final String elem3Def = "elem3Def";	//元素3防御
		public static final String elem3DefPct = "elem3DefPct";	//元素3防御百分比
		public static final String elem4Atk = "elem4Atk";	//元素4伤害
		public static final String elem4AtkPct = "elem4AtkPct";	//元素4伤害百分比
		public static final String elem4Def = "elem4Def";	//元素4防御
		public static final String elem4DefPct = "elem4DefPct";	//元素4防御百分比
		public static final String suck = "suck";	//吸血概率
		public static final String suckPct = "suckPct";	//吸血概率百分比
		public static final String suckRatio = "suckRatio";	//吸血比率
		public static final String suckRatioPct = "suckRatioPct";	//吸血比率百分比
		public static final String avoidAtk = "avoidAtk";	//免伤比例
		public static final String avoidAtkPct = "avoidAtkPct";	//免伤比例百分比
		public static final String skillHealth = "skillHealth";	//治疗效果
		public static final String skillHealthPct = "skillHealthPct";	//治疗效果百分比
		public static final String speed = "speed";	//移动速度
		public static final String speedPct = "speedPct";	//移动速度百分比
		public static final String hpRecov = "hpRecov";	//生命恢复比例
		public static final String hpRecovPct = "hpRecovPct";	//生命恢复比例百分比
		public static final String mpRecov = "mpRecov";	//魔法恢复比例
		public static final String mpRecovPct = "mpRecovPct";	//魔法恢复比例百分比
		public static final String mpReduce = "mpReduce";	//能耗降低(魔法使用降低)
		public static final String mpReducePct = "mpReducePct";	//能耗降低(魔法使用降低)百分比
		public static final String aggro = "aggro";	//仇恨值
		public static final String level = "level";	//当前等级
		public static final String hpCur = "hpCur";	//当前生命
		public static final String mpCur = "mpCur";	//当前法力
		public static final String combat = "combat";	//战斗力
		public static final String pvpMode = "pvpMode";	//PVP模式
		public static final String pvpModeOriginal = "pvpModeOriginal";	//PVP还原模式
		public static final String pvpDiWoId = "pvpDiWoId";	//敌我模式下的队伍id,仅敌我模式下有效
		public static final String pvpModeTimeToHePing = "pvpModeTimeToHePing";	//上次更改PVP模式为和平模式的时间
		public static final String sn = "sn";	//配置表SN
		public static final String name = "name";	//姓名
		public static final String modelSn = "modelSn";	//模型sn
		public static final String profession = "profession";	//职业(1战士,2刺客,3咒术师)
		public static final String sex = "sex";	//性别
		public static final String expCur = "expCur";	//当前经验
		public static final String expUpgrade = "expUpgrade";	//本级升到下一级所需经验
		public static final String inFighting = "inFighting";	//是否在战斗状态(可以不持久化)
		public static final String fightStateEndTime = "fightStateEndTime";	//战斗状态截止时间(可以不持久化)
		public static final String attingIndex = "attingIndex";	//unit对应的阵型位置
		public static final String skill = "skill";	//技能
		public static final String skillGroupSn = "skillGroupSn";	//技能组
		public static final String inborn = "inborn";	//天赋
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
	 * 生命值上限
	 */
	public int getHpMax() {
		return record.get("hpMax");
	}

	public void setHpMax(final int hpMax) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("hpMax", hpMax);

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
	public void setNoChangeHpMax(final int hpMax) {
		//更新属性
		record.setNoUpdate("hpMax", hpMax);
	}
	/**
	 * 生命值上限百分比
	 */
	public int getHpMaxPct() {
		return record.get("hpMaxPct");
	}

	public void setHpMaxPct(final int hpMaxPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("hpMaxPct", hpMaxPct);

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
	public void setNoChangeHpMaxPct(final int hpMaxPct) {
		//更新属性
		record.setNoUpdate("hpMaxPct", hpMaxPct);
	}
	/**
	 * 法力值上限
	 */
	public int getMpMax() {
		return record.get("mpMax");
	}

	public void setMpMax(final int mpMax) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("mpMax", mpMax);

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
	public void setNoChangeMpMax(final int mpMax) {
		//更新属性
		record.setNoUpdate("mpMax", mpMax);
	}
	/**
	 * 法力值上限百分比
	 */
	public int getMpMaxPct() {
		return record.get("mpMaxPct");
	}

	public void setMpMaxPct(final int mpMaxPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("mpMaxPct", mpMaxPct);

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
	public void setNoChangeMpMaxPct(final int mpMaxPct) {
		//更新属性
		record.setNoUpdate("mpMaxPct", mpMaxPct);
	}
	/**
	 * 阳属性攻击(物理攻击)
	 */
	public int getAtkPhy() {
		return record.get("atkPhy");
	}

	public void setAtkPhy(final int atkPhy) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("atkPhy", atkPhy);

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
	public void setNoChangeAtkPhy(final int atkPhy) {
		//更新属性
		record.setNoUpdate("atkPhy", atkPhy);
	}
	/**
	 * 阳属性攻击(物理攻击)百分比
	 */
	public int getAtkPhyPct() {
		return record.get("atkPhyPct");
	}

	public void setAtkPhyPct(final int atkPhyPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("atkPhyPct", atkPhyPct);

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
	public void setNoChangeAtkPhyPct(final int atkPhyPct) {
		//更新属性
		record.setNoUpdate("atkPhyPct", atkPhyPct);
	}
	/**
	 * 阳属性防御(物理防御)
	 */
	public int getDefPhy() {
		return record.get("defPhy");
	}

	public void setDefPhy(final int defPhy) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("defPhy", defPhy);

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
	public void setNoChangeDefPhy(final int defPhy) {
		//更新属性
		record.setNoUpdate("defPhy", defPhy);
	}
	/**
	 * 阳属性防御(物理防御)百分比
	 */
	public int getDefPhyPct() {
		return record.get("defPhyPct");
	}

	public void setDefPhyPct(final int defPhyPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("defPhyPct", defPhyPct);

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
	public void setNoChangeDefPhyPct(final int defPhyPct) {
		//更新属性
		record.setNoUpdate("defPhyPct", defPhyPct);
	}
	/**
	 * 阴属性攻击(魔法攻击)
	 */
	public int getAtkMag() {
		return record.get("atkMag");
	}

	public void setAtkMag(final int atkMag) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("atkMag", atkMag);

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
	public void setNoChangeAtkMag(final int atkMag) {
		//更新属性
		record.setNoUpdate("atkMag", atkMag);
	}
	/**
	 * 阴属性攻击(魔法攻击)百分比
	 */
	public int getAtkMagPct() {
		return record.get("atkMagPct");
	}

	public void setAtkMagPct(final int atkMagPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("atkMagPct", atkMagPct);

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
	public void setNoChangeAtkMagPct(final int atkMagPct) {
		//更新属性
		record.setNoUpdate("atkMagPct", atkMagPct);
	}
	/**
	 * 阴属性防御(魔法防御)
	 */
	public int getDefMag() {
		return record.get("defMag");
	}

	public void setDefMag(final int defMag) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("defMag", defMag);

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
	public void setNoChangeDefMag(final int defMag) {
		//更新属性
		record.setNoUpdate("defMag", defMag);
	}
	/**
	 * 阴属性防御(魔法防御)百分比
	 */
	public int getDefMagPct() {
		return record.get("defMagPct");
	}

	public void setDefMagPct(final int defMagPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("defMagPct", defMagPct);

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
	public void setNoChangeDefMagPct(final int defMagPct) {
		//更新属性
		record.setNoUpdate("defMagPct", defMagPct);
	}
	/**
	 * 命中
	 */
	public int getHit() {
		return record.get("hit");
	}

	public void setHit(final int hit) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("hit", hit);

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
	public void setNoChangeHit(final int hit) {
		//更新属性
		record.setNoUpdate("hit", hit);
	}
	/**
	 * 命中百分比
	 */
	public int getHitPct() {
		return record.get("hitPct");
	}

	public void setHitPct(final int hitPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("hitPct", hitPct);

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
	public void setNoChangeHitPct(final int hitPct) {
		//更新属性
		record.setNoUpdate("hitPct", hitPct);
	}
	/**
	 * 闪避
	 */
	public int getDodge() {
		return record.get("dodge");
	}

	public void setDodge(final int dodge) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("dodge", dodge);

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
	public void setNoChangeDodge(final int dodge) {
		//更新属性
		record.setNoUpdate("dodge", dodge);
	}
	/**
	 * 闪避百分比
	 */
	public int getDodgePct() {
		return record.get("dodgePct");
	}

	public void setDodgePct(final int dodgePct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("dodgePct", dodgePct);

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
	public void setNoChangeDodgePct(final int dodgePct) {
		//更新属性
		record.setNoUpdate("dodgePct", dodgePct);
	}
	/**
	 * 暴击概率(暴击)
	 */
	public int getCrit() {
		return record.get("crit");
	}

	public void setCrit(final int crit) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("crit", crit);

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
	public void setNoChangeCrit(final int crit) {
		//更新属性
		record.setNoUpdate("crit", crit);
	}
	/**
	 * 暴击概率(暴击)百分比
	 */
	public int getCritPct() {
		return record.get("critPct");
	}

	public void setCritPct(final int critPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("critPct", critPct);

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
	public void setNoChangeCritPct(final int critPct) {
		//更新属性
		record.setNoUpdate("critPct", critPct);
	}
	/**
	 * 暴击比例(暴击加成)
	 */
	public int getCritAdd() {
		return record.get("critAdd");
	}

	public void setCritAdd(final int critAdd) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("critAdd", critAdd);

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
	public void setNoChangeCritAdd(final int critAdd) {
		//更新属性
		record.setNoUpdate("critAdd", critAdd);
	}
	/**
	 * 暴击比例(暴击加成)百分比
	 */
	public int getCritAddPct() {
		return record.get("critAddPct");
	}

	public void setCritAddPct(final int critAddPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("critAddPct", critAddPct);

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
	public void setNoChangeCritAddPct(final int critAddPct) {
		//更新属性
		record.setNoUpdate("critAddPct", critAddPct);
	}
	/**
	 * 坚韧
	 */
	public int getTough() {
		return record.get("tough");
	}

	public void setTough(final int tough) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("tough", tough);

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
	public void setNoChangeTough(final int tough) {
		//更新属性
		record.setNoUpdate("tough", tough);
	}
	/**
	 * 坚韧百分比
	 */
	public int getToughPct() {
		return record.get("toughPct");
	}

	public void setToughPct(final int toughPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("toughPct", toughPct);

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
	public void setNoChangeToughPct(final int toughPct) {
		//更新属性
		record.setNoUpdate("toughPct", toughPct);
	}
	/**
	 * 元素1伤害
	 */
	public int getElem1Atk() {
		return record.get("elem1Atk");
	}

	public void setElem1Atk(final int elem1Atk) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem1Atk", elem1Atk);

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
	public void setNoChangeElem1Atk(final int elem1Atk) {
		//更新属性
		record.setNoUpdate("elem1Atk", elem1Atk);
	}
	/**
	 * 元素1伤害百分比
	 */
	public int getElem1AtkPct() {
		return record.get("elem1AtkPct");
	}

	public void setElem1AtkPct(final int elem1AtkPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem1AtkPct", elem1AtkPct);

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
	public void setNoChangeElem1AtkPct(final int elem1AtkPct) {
		//更新属性
		record.setNoUpdate("elem1AtkPct", elem1AtkPct);
	}
	/**
	 * 元素1防御
	 */
	public int getElem1Def() {
		return record.get("elem1Def");
	}

	public void setElem1Def(final int elem1Def) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem1Def", elem1Def);

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
	public void setNoChangeElem1Def(final int elem1Def) {
		//更新属性
		record.setNoUpdate("elem1Def", elem1Def);
	}
	/**
	 * 元素1防御百分比
	 */
	public int getElem1DefPct() {
		return record.get("elem1DefPct");
	}

	public void setElem1DefPct(final int elem1DefPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem1DefPct", elem1DefPct);

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
	public void setNoChangeElem1DefPct(final int elem1DefPct) {
		//更新属性
		record.setNoUpdate("elem1DefPct", elem1DefPct);
	}
	/**
	 * 元素2伤害
	 */
	public int getElem2Atk() {
		return record.get("elem2Atk");
	}

	public void setElem2Atk(final int elem2Atk) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem2Atk", elem2Atk);

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
	public void setNoChangeElem2Atk(final int elem2Atk) {
		//更新属性
		record.setNoUpdate("elem2Atk", elem2Atk);
	}
	/**
	 * 元素2伤害百分比
	 */
	public int getElem2AtkPct() {
		return record.get("elem2AtkPct");
	}

	public void setElem2AtkPct(final int elem2AtkPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem2AtkPct", elem2AtkPct);

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
	public void setNoChangeElem2AtkPct(final int elem2AtkPct) {
		//更新属性
		record.setNoUpdate("elem2AtkPct", elem2AtkPct);
	}
	/**
	 * 元素2防御
	 */
	public int getElem2Def() {
		return record.get("elem2Def");
	}

	public void setElem2Def(final int elem2Def) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem2Def", elem2Def);

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
	public void setNoChangeElem2Def(final int elem2Def) {
		//更新属性
		record.setNoUpdate("elem2Def", elem2Def);
	}
	/**
	 * 元素2防御百分比
	 */
	public int getElem2DefPct() {
		return record.get("elem2DefPct");
	}

	public void setElem2DefPct(final int elem2DefPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem2DefPct", elem2DefPct);

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
	public void setNoChangeElem2DefPct(final int elem2DefPct) {
		//更新属性
		record.setNoUpdate("elem2DefPct", elem2DefPct);
	}
	/**
	 * 元素3伤害
	 */
	public int getElem3Atk() {
		return record.get("elem3Atk");
	}

	public void setElem3Atk(final int elem3Atk) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem3Atk", elem3Atk);

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
	public void setNoChangeElem3Atk(final int elem3Atk) {
		//更新属性
		record.setNoUpdate("elem3Atk", elem3Atk);
	}
	/**
	 * 元素3伤害百分比
	 */
	public int getElem3AtkPct() {
		return record.get("elem3AtkPct");
	}

	public void setElem3AtkPct(final int elem3AtkPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem3AtkPct", elem3AtkPct);

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
	public void setNoChangeElem3AtkPct(final int elem3AtkPct) {
		//更新属性
		record.setNoUpdate("elem3AtkPct", elem3AtkPct);
	}
	/**
	 * 元素3防御
	 */
	public int getElem3Def() {
		return record.get("elem3Def");
	}

	public void setElem3Def(final int elem3Def) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem3Def", elem3Def);

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
	public void setNoChangeElem3Def(final int elem3Def) {
		//更新属性
		record.setNoUpdate("elem3Def", elem3Def);
	}
	/**
	 * 元素3防御百分比
	 */
	public int getElem3DefPct() {
		return record.get("elem3DefPct");
	}

	public void setElem3DefPct(final int elem3DefPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem3DefPct", elem3DefPct);

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
	public void setNoChangeElem3DefPct(final int elem3DefPct) {
		//更新属性
		record.setNoUpdate("elem3DefPct", elem3DefPct);
	}
	/**
	 * 元素4伤害
	 */
	public int getElem4Atk() {
		return record.get("elem4Atk");
	}

	public void setElem4Atk(final int elem4Atk) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem4Atk", elem4Atk);

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
	public void setNoChangeElem4Atk(final int elem4Atk) {
		//更新属性
		record.setNoUpdate("elem4Atk", elem4Atk);
	}
	/**
	 * 元素4伤害百分比
	 */
	public int getElem4AtkPct() {
		return record.get("elem4AtkPct");
	}

	public void setElem4AtkPct(final int elem4AtkPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem4AtkPct", elem4AtkPct);

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
	public void setNoChangeElem4AtkPct(final int elem4AtkPct) {
		//更新属性
		record.setNoUpdate("elem4AtkPct", elem4AtkPct);
	}
	/**
	 * 元素4防御
	 */
	public int getElem4Def() {
		return record.get("elem4Def");
	}

	public void setElem4Def(final int elem4Def) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem4Def", elem4Def);

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
	public void setNoChangeElem4Def(final int elem4Def) {
		//更新属性
		record.setNoUpdate("elem4Def", elem4Def);
	}
	/**
	 * 元素4防御百分比
	 */
	public int getElem4DefPct() {
		return record.get("elem4DefPct");
	}

	public void setElem4DefPct(final int elem4DefPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("elem4DefPct", elem4DefPct);

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
	public void setNoChangeElem4DefPct(final int elem4DefPct) {
		//更新属性
		record.setNoUpdate("elem4DefPct", elem4DefPct);
	}
	/**
	 * 吸血概率
	 */
	public int getSuck() {
		return record.get("suck");
	}

	public void setSuck(final int suck) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("suck", suck);

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
	public void setNoChangeSuck(final int suck) {
		//更新属性
		record.setNoUpdate("suck", suck);
	}
	/**
	 * 吸血概率百分比
	 */
	public int getSuckPct() {
		return record.get("suckPct");
	}

	public void setSuckPct(final int suckPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("suckPct", suckPct);

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
	public void setNoChangeSuckPct(final int suckPct) {
		//更新属性
		record.setNoUpdate("suckPct", suckPct);
	}
	/**
	 * 吸血比率
	 */
	public int getSuckRatio() {
		return record.get("suckRatio");
	}

	public void setSuckRatio(final int suckRatio) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("suckRatio", suckRatio);

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
	public void setNoChangeSuckRatio(final int suckRatio) {
		//更新属性
		record.setNoUpdate("suckRatio", suckRatio);
	}
	/**
	 * 吸血比率百分比
	 */
	public int getSuckRatioPct() {
		return record.get("suckRatioPct");
	}

	public void setSuckRatioPct(final int suckRatioPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("suckRatioPct", suckRatioPct);

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
	public void setNoChangeSuckRatioPct(final int suckRatioPct) {
		//更新属性
		record.setNoUpdate("suckRatioPct", suckRatioPct);
	}
	/**
	 * 免伤比例
	 */
	public int getAvoidAtk() {
		return record.get("avoidAtk");
	}

	public void setAvoidAtk(final int avoidAtk) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("avoidAtk", avoidAtk);

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
	public void setNoChangeAvoidAtk(final int avoidAtk) {
		//更新属性
		record.setNoUpdate("avoidAtk", avoidAtk);
	}
	/**
	 * 免伤比例百分比
	 */
	public int getAvoidAtkPct() {
		return record.get("avoidAtkPct");
	}

	public void setAvoidAtkPct(final int avoidAtkPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("avoidAtkPct", avoidAtkPct);

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
	public void setNoChangeAvoidAtkPct(final int avoidAtkPct) {
		//更新属性
		record.setNoUpdate("avoidAtkPct", avoidAtkPct);
	}
	/**
	 * 治疗效果
	 */
	public int getSkillHealth() {
		return record.get("skillHealth");
	}

	public void setSkillHealth(final int skillHealth) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("skillHealth", skillHealth);

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
	public void setNoChangeSkillHealth(final int skillHealth) {
		//更新属性
		record.setNoUpdate("skillHealth", skillHealth);
	}
	/**
	 * 治疗效果百分比
	 */
	public int getSkillHealthPct() {
		return record.get("skillHealthPct");
	}

	public void setSkillHealthPct(final int skillHealthPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("skillHealthPct", skillHealthPct);

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
	public void setNoChangeSkillHealthPct(final int skillHealthPct) {
		//更新属性
		record.setNoUpdate("skillHealthPct", skillHealthPct);
	}
	/**
	 * 移动速度
	 */
	public int getSpeed() {
		return record.get("speed");
	}

	public void setSpeed(final int speed) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("speed", speed);

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
	public void setNoChangeSpeed(final int speed) {
		//更新属性
		record.setNoUpdate("speed", speed);
	}
	/**
	 * 移动速度百分比
	 */
	public int getSpeedPct() {
		return record.get("speedPct");
	}

	public void setSpeedPct(final int speedPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("speedPct", speedPct);

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
	public void setNoChangeSpeedPct(final int speedPct) {
		//更新属性
		record.setNoUpdate("speedPct", speedPct);
	}
	/**
	 * 生命恢复比例
	 */
	public int getHpRecov() {
		return record.get("hpRecov");
	}

	public void setHpRecov(final int hpRecov) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("hpRecov", hpRecov);

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
	public void setNoChangeHpRecov(final int hpRecov) {
		//更新属性
		record.setNoUpdate("hpRecov", hpRecov);
	}
	/**
	 * 生命恢复比例百分比
	 */
	public int getHpRecovPct() {
		return record.get("hpRecovPct");
	}

	public void setHpRecovPct(final int hpRecovPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("hpRecovPct", hpRecovPct);

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
	public void setNoChangeHpRecovPct(final int hpRecovPct) {
		//更新属性
		record.setNoUpdate("hpRecovPct", hpRecovPct);
	}
	/**
	 * 魔法恢复比例
	 */
	public int getMpRecov() {
		return record.get("mpRecov");
	}

	public void setMpRecov(final int mpRecov) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("mpRecov", mpRecov);

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
	public void setNoChangeMpRecov(final int mpRecov) {
		//更新属性
		record.setNoUpdate("mpRecov", mpRecov);
	}
	/**
	 * 魔法恢复比例百分比
	 */
	public int getMpRecovPct() {
		return record.get("mpRecovPct");
	}

	public void setMpRecovPct(final int mpRecovPct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("mpRecovPct", mpRecovPct);

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
	public void setNoChangeMpRecovPct(final int mpRecovPct) {
		//更新属性
		record.setNoUpdate("mpRecovPct", mpRecovPct);
	}
	/**
	 * 能耗降低(魔法使用降低)
	 */
	public int getMpReduce() {
		return record.get("mpReduce");
	}

	public void setMpReduce(final int mpReduce) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("mpReduce", mpReduce);

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
	public void setNoChangeMpReduce(final int mpReduce) {
		//更新属性
		record.setNoUpdate("mpReduce", mpReduce);
	}
	/**
	 * 能耗降低(魔法使用降低)百分比
	 */
	public int getMpReducePct() {
		return record.get("mpReducePct");
	}

	public void setMpReducePct(final int mpReducePct) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("mpReducePct", mpReducePct);

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
	public void setNoChangeMpReducePct(final int mpReducePct) {
		//更新属性
		record.setNoUpdate("mpReducePct", mpReducePct);
	}
	/**
	 * 仇恨值
	 */
	public int getAggro() {
		return record.get("aggro");
	}

	public void setAggro(final int aggro) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("aggro", aggro);

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
	public void setNoChangeAggro(final int aggro) {
		//更新属性
		record.setNoUpdate("aggro", aggro);
	}
	/**
	 * 当前等级
	 */
	public int getLevel() {
		return record.get("level");
	}

	public void setLevel(final int level) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("level", level);

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
	public void setNoChangeLevel(final int level) {
		//更新属性
		record.setNoUpdate("level", level);
	}
	/**
	 * 当前生命
	 */
	public int getHpCur() {
		return record.get("hpCur");
	}

	public void setHpCur(final int hpCur) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("hpCur", hpCur);

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
	public void setNoChangeHpCur(final int hpCur) {
		//更新属性
		record.setNoUpdate("hpCur", hpCur);
	}
	/**
	 * 当前法力
	 */
	public int getMpCur() {
		return record.get("mpCur");
	}

	public void setMpCur(final int mpCur) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("mpCur", mpCur);

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
	public void setNoChangeMpCur(final int mpCur) {
		//更新属性
		record.setNoUpdate("mpCur", mpCur);
	}
	/**
	 * 战斗力
	 */
	public int getCombat() {
		return record.get("combat");
	}

	public void setCombat(final int combat) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("combat", combat);

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
	public void setNoChangeCombat(final int combat) {
		//更新属性
		record.setNoUpdate("combat", combat);
	}
	/**
	 * PVP模式
	 */
	public String getPvpMode() {
		return record.get("pvpMode");
	}

	public void setPvpMode(final String pvpMode) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("pvpMode", pvpMode);

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
	public void setNoChangePvpMode(final String pvpMode) {
		//更新属性
		record.setNoUpdate("pvpMode", pvpMode);
	}
	/**
	 * PVP还原模式
	 */
	public String getPvpModeOriginal() {
		return record.get("pvpModeOriginal");
	}

	public void setPvpModeOriginal(final String pvpModeOriginal) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("pvpModeOriginal", pvpModeOriginal);

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
	public void setNoChangePvpModeOriginal(final String pvpModeOriginal) {
		//更新属性
		record.setNoUpdate("pvpModeOriginal", pvpModeOriginal);
	}
	/**
	 * 敌我模式下的队伍id,仅敌我模式下有效
	 */
	public int getPvpDiWoId() {
		return record.get("pvpDiWoId");
	}

	public void setPvpDiWoId(final int pvpDiWoId) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("pvpDiWoId", pvpDiWoId);

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
	public void setNoChangePvpDiWoId(final int pvpDiWoId) {
		//更新属性
		record.setNoUpdate("pvpDiWoId", pvpDiWoId);
	}
	/**
	 * 上次更改PVP模式为和平模式的时间
	 */
	public long getPvpModeTimeToHePing() {
		return record.get("pvpModeTimeToHePing");
	}

	public void setPvpModeTimeToHePing(final long pvpModeTimeToHePing) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("pvpModeTimeToHePing", pvpModeTimeToHePing);

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
	public void setNoChangePvpModeTimeToHePing(final long pvpModeTimeToHePing) {
		//更新属性
		record.setNoUpdate("pvpModeTimeToHePing", pvpModeTimeToHePing);
	}
	/**
	 * 配置表SN
	 */
	public String getSn() {
		return record.get("sn");
	}

	public void setSn(final String sn) {
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
	public void setNoChangeSn(final String sn) {
		//更新属性
		record.setNoUpdate("sn", sn);
	}
	/**
	 * 姓名
	 */
	public String getName() {
		return record.get("name");
	}

	public void setName(final String name) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("name", name);

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
	public void setNoChangeName(final String name) {
		//更新属性
		record.setNoUpdate("name", name);
	}
	/**
	 * 模型sn
	 */
	public String getModelSn() {
		return record.get("modelSn");
	}

	public void setModelSn(final String modelSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("modelSn", modelSn);

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
	public void setNoChangeModelSn(final String modelSn) {
		//更新属性
		record.setNoUpdate("modelSn", modelSn);
	}
	/**
	 * 职业(1战士,2刺客,3咒术师)
	 */
	public int getProfession() {
		return record.get("profession");
	}

	public void setProfession(final int profession) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("profession", profession);

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
	public void setNoChangeProfession(final int profession) {
		//更新属性
		record.setNoUpdate("profession", profession);
	}
	/**
	 * 性别
	 */
	public int getSex() {
		return record.get("sex");
	}

	public void setSex(final int sex) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("sex", sex);

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
	public void setNoChangeSex(final int sex) {
		//更新属性
		record.setNoUpdate("sex", sex);
	}
	/**
	 * 当前经验
	 */
	public long getExpCur() {
		return record.get("expCur");
	}

	public void setExpCur(final long expCur) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("expCur", expCur);

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
	public void setNoChangeExpCur(final long expCur) {
		//更新属性
		record.setNoUpdate("expCur", expCur);
	}
	/**
	 * 本级升到下一级所需经验
	 */
	public long getExpUpgrade() {
		return record.get("expUpgrade");
	}

	public void setExpUpgrade(final long expUpgrade) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("expUpgrade", expUpgrade);

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
	public void setNoChangeExpUpgrade(final long expUpgrade) {
		//更新属性
		record.setNoUpdate("expUpgrade", expUpgrade);
	}
	/**
	 * 是否在战斗状态(可以不持久化)
	 */
	public boolean isInFighting() {
		return record.<Integer>get("inFighting") == 1;
	}

	public void setInFighting(boolean inFighting) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("inFighting", inFighting ? 1 : 0);

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
	public void setNoChangeInFighting(final boolean inFighting) {
		//更新属性
		record.setNoUpdate("inFighting", inFighting ? 1 : 0);
	}
	/**
	 * 战斗状态截止时间(可以不持久化)
	 */
	public long getFightStateEndTime() {
		return record.get("fightStateEndTime");
	}

	public void setFightStateEndTime(final long fightStateEndTime) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("fightStateEndTime", fightStateEndTime);

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
	public void setNoChangeFightStateEndTime(final long fightStateEndTime) {
		//更新属性
		record.setNoUpdate("fightStateEndTime", fightStateEndTime);
	}
	/**
	 * unit对应的阵型位置
	 */
	public int getAttingIndex() {
		return record.get("attingIndex");
	}

	public void setAttingIndex(final int attingIndex) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("attingIndex", attingIndex);

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
	public void setNoChangeAttingIndex(final int attingIndex) {
		//更新属性
		record.setNoUpdate("attingIndex", attingIndex);
	}
	/**
	 * 技能
	 */
	public String getSkill() {
		return record.get("skill");
	}

	public void setSkill(final String skill) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("skill", skill);

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
	public void setNoChangeSkill(final String skill) {
		//更新属性
		record.setNoUpdate("skill", skill);
	}
	/**
	 * 技能组
	 */
	public int getSkillGroupSn() {
		return record.get("skillGroupSn");
	}

	public void setSkillGroupSn(final int skillGroupSn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("skillGroupSn", skillGroupSn);

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
	public void setNoChangeSkillGroupSn(final int skillGroupSn) {
		//更新属性
		record.setNoUpdate("skillGroupSn", skillGroupSn);
	}
	/**
	 * 天赋
	 */
	public String getInborn() {
		return record.get("inborn");
	}

	public void setInborn(final String inborn) {
		//更新前的数据状态
		int statusOld = record.getStatus();
		
		//更新属性
		record.set("inborn", inborn);

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
	public void setNoChangeInborn(final String inborn) {
		//更新属性
		record.setNoUpdate("inborn", inborn);
	}

}