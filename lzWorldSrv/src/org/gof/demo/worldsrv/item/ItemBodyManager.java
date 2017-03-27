package org.gof.demo.worldsrv.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;




import org.apache.commons.collections.iterators.ReverseListIterator;
import org.apache.commons.collections.map.MultiValueMap;
import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.RandomUtils;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.stageObj.UnitPropPlusMap;
import org.gof.demo.battlesrv.support.PropCalcCommon;
import org.gof.demo.battlesrv.support.PropKey;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfEquipBaseData;
import org.gof.demo.worldsrv.config.ConfEquipGroup;
import org.gof.demo.worldsrv.config.ConfGemBase;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.config.ConfPartChongxingCost;
import org.gof.demo.worldsrv.config.ConfPartChongxingEffect;
import org.gof.demo.worldsrv.config.ConfPartChongxingMore;
import org.gof.demo.worldsrv.config.ConfPartQianghuaCost;
import org.gof.demo.worldsrv.config.ConfPartQianghuaEffect;
import org.gof.demo.worldsrv.config.ConfPartsData;
import org.gof.demo.worldsrv.config.ConfVip;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.entity.Part;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DParts;
import org.gof.demo.worldsrv.msg.Msg.SCEquipDown;
import org.gof.demo.worldsrv.msg.Msg.SCEquipReclaim;
import org.gof.demo.worldsrv.msg.Msg.SCEquipUp;
import org.gof.demo.worldsrv.msg.Msg.SCGemComposite;
import org.gof.demo.worldsrv.msg.Msg.SCGemCompositeAllTop;
import org.gof.demo.worldsrv.msg.Msg.SCGemCompositeTop;
import org.gof.demo.worldsrv.msg.Msg.SCPartChongxing;
import org.gof.demo.worldsrv.msg.Msg.SCPartQianghua;
import org.gof.demo.worldsrv.msg.Msg.SCSetGem;
import org.gof.demo.worldsrv.msg.Msg.SCStageObjectInfoChange;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.GetSetGrowthList;
import org.gof.demo.worldsrv.support.HumanInfoChange;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.enumKey.ItemPackTypeKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;




import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;

/**
 * 装备的物品玩法逻辑。
 *
 * @author zhangbo
 *
 */
public class ItemBodyManager extends ManagerBase {

	private static final int SEX_CARELESS = 3; // TODO 表示不分性别
	private static final int PROFESSION_CARELESS = 15; // TODO 表示不分职业

	public static ItemBodyManager inst() {
		return inst(ItemBodyManager.class);
	}

	/**
	 * 取玩家的某个装备位。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param partSn
	 *            装备位SN
	 * @return 玩家的装备位。
	 */
	Part __getPart(HumanObject humanObj, int partSn) {
		return humanObj.dataPers.parts.get(partSn);
	}

	/**
	 * 取玩家的某个装备位上的装备。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param part
	 *            装备位
	 * @return 装备位上的物品。
	 */
	Item __getEquipOnPart(HumanObject humanObj, Part part) {
		long equipId = part.getEquipId();
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		return pack.getFromBody(equipId);
	}

	/**
	 * 把背包物品上装备位。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param position
	 *            背包中的位置
	 * @return 结果与原因。
	 */
	public ReasonResult equipUp(HumanObject humanObj, int position) {
		Human human = humanObj.getHuman();
		// 玩家背包
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);

		// 从背包取物品
		Item toEquip = pack.get(position, ItemPackTypeKey.Bag1.packType);// TODO
																			// 注意一下：装备全在背包1？
		if (toEquip == null) {
			return new ReasonResult(false, I18n.get("item.common.noItem"));
		}

		// 获得装备基础信息
		ConfItemData confItem = ConfItemData.get(toEquip.getSn());

		// 不是装备
		if (ItemTypeKey.装备 != confItem.bType) {
			return new ReasonResult(false, I18n.get("item.equip.isNotEquip"));
		}

		// 等级不够
		if (human.getLevel() < confItem.level) {
			return new ReasonResult(false,
					I18n.get("item.common.levelNotEnough"));
		}

		// 玩家性别
		if (human.getSex() != confItem.sex && confItem.sex != SEX_CARELESS) {
			return new ReasonResult(false, I18n.get("item.common.cantPutOn"));
		}

		// 职业和装备不符， 15代表所有职业
		if (confItem.career != human.getProfession()
				&& confItem.career != PROFESSION_CARELESS) {
			return new ReasonResult(false, I18n.get("item.common.cantPutOn"));
		}

		// 装备对应哪个装备位
		ConfEquipBaseData confEquip = ConfEquipBaseData.get(confItem.sn);
		int partSn = confEquip.partSn;

		// 和原装备位上的装备交换位置，如果有则脱下，更新背包，没有就直接上
		Part part = __getPart(humanObj, partSn);
		Item equiped = __getEquipOnPart(humanObj, part);
		ItemChange itemChange = __swapFromBodyToBag(toEquip, equiped, part,
				pack);
		// 重算单个装备位属性
		__updatePartProp(humanObj, part);
		// 装备位被激活
		part.setEnabled(true);

		// 监听用户属性变化
		HumanInfoChange.listen(humanObj);

		// 所有装备属性重新影响人物属性
		__updateBodyProp(humanObj);

		// 触发身上装备变化事件
		Event.fire(EventKey.BODY_ITEM_CHANGE, "humanObject", humanObj,
				"publish", true);

		// 回穿戴成功消息
		SCEquipUp.Builder builder = SCEquipUp.newBuilder();
		builder.setIndex(part.getSn());
		builder.setBagIndex(position);
		humanObj.sendMsg(builder);

		// 回背包变化消息
		itemChange.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);

		return new ReasonResult(true);
	}

	/**
	 * 将来自背包中的物品{@code toEquip}，和来自装备位的物品{@code equiped}交换容器。
	 * <p>
	 * 自动判断为{@code null}的情况，返回背包变化的状态。
	 * <p>
	 * 要注意：<br>
	 * 这个里面不判断背包容量问题；<br>
	 * 物品上哪个装备位依赖{@code toEquip.position}赋的字段值。
	 * 
	 * @param toEquip
	 *            准备上装备位的物品
	 * @param equiped
	 *            准备从装备位上替下的物品
	 * @param part
	 *            装备位
	 * @param pack
	 *            背包
	 * @return 背包的变化。
	 */
	private ItemChange __swapFromBodyToBag(Item toEquip, Item equiped,
			Part part, ItemPack pack) {
		ItemChange itemChange = new ItemChange();
		// 若有物品要上装备位，先从包里移走。
		if (toEquip != null) {
			int bagType = ItemPack.selectBagType(toEquip.getSn());
			pack.remove(toEquip.getPosition(), bagType);
			// 背包标记更新
			itemChange.markDeleting(toEquip);
		}
		// 若有物品要下装备位，就从装备位移走。
		if (equiped != null) {
			pack.removeBody(equiped.getId());
		}
		// 若有物品要下装备位，此物品放到背包。
		if (equiped != null) {
			int bagType = ItemPack.selectBagType(equiped.getSn());
			// 手动设置位置
			equiped.setPackType(bagType);
			int toBagPosition = pack.getFirstFreePosition(bagType); // 注意一下背包容量问题
			equiped.setPosition(toBagPosition);
			pack.addToBag(equiped);
			// 背包标记更新
			itemChange.markAdded(equiped);
		}
		// 若有物品要上装备位，上之。
		if (toEquip != null) {
			// 手动设置装备位
			int partSn = part.getSn();
			toEquip.setPosition(partSn);
			toEquip.setPackType(ItemPackTypeKey.Parts.packType);
			pack.toBody(toEquip);
		}
		part.setEquipId(toEquip != null ? toEquip.getId() : 0);
		return itemChange;
	}

	/**
	 * 脱装备。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param partSn
	 *            装备位SN
	 * @return 结果与原因。
	 */
	public ReasonResult equipDown(HumanObject humanObj, int partSn) {

		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		Part part = __getPart(humanObj, partSn);
		Item equiped = __getEquipOnPart(humanObj, part);
		if (equiped == null) {
			return new ReasonResult(false, I18n.get("item.common.noItem"));
		}
		// 判断进包位置是否在容量上限之内
		int packType = ItemPack.selectBagType(equiped.getSn());
		int posFree = pack.getFirstFreePosition(packType);
		int posLimit = ItemBagManager.inst().getNumMax(humanObj, packType);
		if (posLimit < posFree) {
			return new ReasonResult(false, I18n.get("item.error.numNotEnough"));
		}

		// 脱下，更新背包
		ItemChange itemChange = __swapFromBodyToBag(null, equiped, part, pack);
		// 重算单个装备位属性
		__updatePartProp(humanObj, part);
		// 装备位取消激活
		part.setEnabled(false);

		// 监听用户属性变化
		HumanInfoChange.listen(humanObj);

		// 所有装备属性重新影响人物属性
		__updateBodyProp(humanObj);

		// 触发身上装备变化事件
		Event.fire(EventKey.BODY_ITEM_CHANGE, "humanObject", humanObj,
				"publish", true);

		// 回穿戴成功消息
		SCEquipDown.Builder success = SCEquipDown.newBuilder();
		success.setCode(0);
		humanObj.sendMsg(success);

		// 回背包变化消息
		itemChange.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return new ReasonResult(true);
	}

	/**
	 * 处理变更装备后的事件。
	 * 
	 * @param param
	 */
	@Listener(value = EventKey.BODY_ITEM_CHANGE)
	public void onBodyItemChange(Param param) {

		HumanObject humanObj = param.get("humanObject");
		boolean publish = param.getBoolean("publish");

		// 给附近玩家发送变化信息
		if (publish) {
			SCStageObjectInfoChange.Builder msgPutOn = SCStageObjectInfoChange
					.newBuilder();
			msgPutOn.setObj(humanObj.createMsg());
			StageManager.inst().sendMsgToArea(msgPutOn, humanObj.stageObj,
					humanObj.posNow);
		}
	}

	/**
	 * 重算玩家身上装备属性对玩家属性的影响。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 */
	private void __updateBodyProp(HumanObject humanObj) {
		// 装备的所有属性，包括强化、镶嵌等等，算出来后都加到人物属性上
		UnitPropPlusMap pp = humanObj.dataPers.unitPropPlus;
		String partsProp = __calcCurrentPartsProp(humanObj);
		pp.setItemEquip(partsProp);

		// 重新计算战斗力
		UnitManager.inst().propCalc(humanObj);
	}

	/**
	 * 计算当前装备位提供的属性值。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @return 属性值Json
	 */
	private String __calcCurrentPartsProp(HumanObject humanObj) {
		PropCalcCommon propCalc = new PropCalcCommon();
		Map<Integer, Part> parts = humanObj.dataPers.parts;

		// 逐个部位找装备，激活装备位上的属性
		for (Part part : parts.values()) {
			// 上了装备才生效
			if (part.isEnabled()) {
				// 加属性值
				String partProp = part.getPropJson();
				propCalc.plus(partProp);
			}
		}

		// 额外一：找寻冲星附加属性
		Collection<ConfPartChongxingMore> chongxingEx = ConfPartChongxingMore
				.findAll();
		for (ConfPartChongxingMore conf : chongxingEx) {
			boolean accomplished = false;
			for (int phaseSn : conf.phase) {
				Part part = parts.get(phaseSn);
				// 上了装备才生效
				if (part.isEnabled()) {
					int cxStar = getChongxingLv(part, phaseSn);
					// 是否达到星要求
					if (conf.star <= cxStar) {
						accomplished = true;
						continue;
					}
				}
				// 不满足条件就直接跳出
				accomplished = false;
				break;
			}
			// 满足条件情况下加属性
			if (accomplished) {
				for (int i = 0; i < conf.attrType.length; i++) {
					propCalc.plus(conf.attrType[i],
							Double.valueOf(conf.effetValue[i]));
				}
			}
		}
		// 额外二：找寻套装属性
		Map<Integer, Integer> hasTaozhuang = new HashMap<>();
		for (Part part : parts.values()) {
			// 上了装备才生效
			if (part.isEnabled()) {
				Item equip = __getEquipOnPart(humanObj, part);
				ConfEquipBaseData equipConf = ConfEquipBaseData.get(equip
						.getSn());
				if (equipConf.groupSn != 0) {
					hasTaozhuang.merge(equipConf.groupSn, 1, Integer::sum);
				}
			}
		}
		// 按套装件数的不同分别激活套装属性
		for (Map.Entry<Integer, Integer> taozhuang : hasTaozhuang.entrySet()) {
			int taozhuangSn = taozhuang.getKey();
			int has = taozhuang.getValue();
			ConfEquipGroup groupConf = ConfEquipGroup.get(taozhuangSn);
			int len1 = groupConf.attrType.length;
			for (int i = 0; i < groupConf.pEffect.length; i += (1 + len1)) {
				int need = groupConf.pEffect[i];
				if (has >= need) {
					for (int j = 0; j < len1; j++) {
						propCalc.plus(groupConf.attrType[j],
								Double.valueOf(groupConf.pEffect[i + 1 + j]));
					}
				}
			}
		}
		return propCalc.toJSONStr();
	}

	/**
	 * 构建玩家的装备信息消息。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @return 消息Builder
	 */
	public List<DParts> getEquipInfo(HumanObject humanObj) {
		List<DParts> result = new ArrayList<>();

		Collection<Part> parts = humanObj.dataPers.parts.values();
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		for (Part part : parts) {
			DParts.Builder dr = DParts.newBuilder();

			dr.setSn(part.getSn());

			long equipId = part.getEquipId();
			if (equipId > 0) {
				Item equip = pack.getFromBody(equipId);
				dr.setEquipSn(equip.getSn());
			} else {
				dr.setEquipSn(0);
			}

			int qianghuaLv = part.getQianghuaLv();
			dr.setQhLv(qianghuaLv);

			List<Integer> cxlist = new GetSetGrowthList<Integer>();
			Collection<ConfPartChongxingCost> allCxCost = ConfPartChongxingCost
					.findAll();
			for (ConfPartChongxingCost cxCost : allCxCost) {
				int chongxingLv = getChongxingLv(part, cxCost.sn);
				cxlist.set(cxCost.sn, chongxingLv);
			}
			for (int i = 1; i < cxlist.size(); i++) {
				Integer cxLv = cxlist.get(i);
				dr.addCxLvs(cxLv == null ? 0 : cxLv.intValue());
			}

			for (int slot = 0; slot < 3; slot++) {
				Item gem = __findGemOnSlot(pack, part, slot);
				if (gem == null) {
					dr.addGemSns(0);
				} else {
					dr.addGemSns(gem.getSn());
				}
			}
			result.add(dr.build());
		}
		return result;
	}

	/**
	 * 物品分解。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param positions
	 *            选定装备在背包内位置。
	 * @return 结果与原因。
	 */
	public ReasonResult equipReclaim(HumanObject humanObj,
			List<Integer> positions) {
		// 玩家背包
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);

		// 从背包取物品
		List<Item> toDelete = new ArrayList<>();
		for (Integer pos : positions) {
			Item equip = pack.get(pos, ItemPackTypeKey.Bag1.packType);// TODO
																		// 注意一下：装备全在背包1？
			if (equip == null) {
				return new ReasonResult(false, I18n.get("item.common.noItem"));
			}
			ConfItemData confItem = ConfItemData.get(equip.getSn());
			if (confItem.bType != ItemTypeKey.装备) {
				return new ReasonResult(false, "你选的不是装备");
			}
			toDelete.add(equip);
		}
		// 看这些物品能分解成什么物品
		Map<Integer, Integer> bySn = new HashMap<>();
		for (Item equip : toDelete) {
			ConfEquipBaseData confEqBase = ConfEquipBaseData.get(equip.getSn());
			if (confEqBase.reclaimSn > 0) {
				bySn.merge(confEqBase.reclaimSn, confEqBase.reclaimNum, Integer::sum);
			}
		}
		List<ItemVO> toGen = new ArrayList<>();
		bySn.forEach((sn, num) -> toGen.add(new ItemVO(sn, num)));

		// 删材料
		ItemChange itemChange = new ItemChange();
		for (Item e : toDelete) {
			pack.remove(e.getPosition(), e.getPackType());
			e.remove();
			// 记录被删除的物品
			itemChange.markDeleting(e);
		}
		// 告知一次客户端物品减少
		itemChange.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);

		// 生成新物品
		// TODO 生成的注定比删掉的少？背包够了？
		ItemBagManager.inst().add(humanObj, toGen);

		// 通知成功
		SCEquipReclaim.Builder success = SCEquipReclaim.newBuilder();
		success.setCode(0);
		humanObj.sendMsg(success);
		Inform.user(humanObj.id, Inform.提示操作, "分解成功");
		return new ReasonResult(true);

	}

	/**
	 * 处理强化。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param partSn
	 *            装备位
	 * @return 结果与原因。
	 */
	public ReasonResult partQianghua(HumanObject humanObj, int partSn) {

		Part part = ItemBodyManager.inst().__getPart(humanObj, partSn);

		int qianghuaLv = part.getQianghuaLv();

		ConfPartQianghuaCost conf = ConfPartQianghuaCost.get(qianghuaLv + 1);
		// 判断等级上限
		int confTimes = ConfGlobalUtils.getValue(ConfGlobalKey.强化倍数);
		if (qianghuaLv == humanObj.getHuman().getLevel() * confTimes) {
			return new ReasonResult(false, I18n.get("part.qianghua.maxLevel"));
		}
		// 判断消耗
		int needMoney = conf.money;
		ReasonResult canProduceReduce = HumanManager.inst().canProduceReduce(
				humanObj, conf.moneyType, needMoney);
		if (!canProduceReduce.success) {
			return canProduceReduce;
		}
		ReasonResult canRemove = ItemBagManager.inst().canRemove(humanObj,
				conf.stoneSn, conf.stones);
		if (!canRemove.success) {
			return canRemove;
		}
		// 处理消耗
		HumanManager.inst().produceMoneyReduce(humanObj, conf.moneyType,
				needMoney, MoneyReduceLogKey.装备位强化);
		ItemBagManager.inst().remove(humanObj, conf.stoneSn, conf.stones);
		// 处理升级
		part.setQianghuaLv(qianghuaLv + 1);
		// 重算单个装备位属性
		__updatePartProp(humanObj, part);
		// 监听用户属性变化
		HumanInfoChange.listen(humanObj);
		// 所有装备属性重新影响人物属性
		__updateBodyProp(humanObj);

		// 如果生效则发出装备变更事件
		Event.fire(EventKey.BODY_ITEM_CHANGE, "humanObject", humanObj,
				"publish", true);
		//发布任务强化装备事件
		Event.fire(EventKey.BODY_PART_QIANGHUA, "humanObj", humanObj);
		
		// 回消息
		SCPartQianghua.Builder mb = SCPartQianghua.newBuilder();
		mb.setCode(0);
		humanObj.sendMsg(mb);

		return new ReasonResult(true);
	}

	/**
	 * 重算某个装备位的属性，算属性的时候需要用到职业。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param part
	 *            装备位
	 */
	private void __updatePartProp(HumanObject humanObj, Part part) {

		// 0.准备配置
		ConfPartsData confPart = ConfPartsData.get(part.getSn());
		// 1.统计所有玩法的加成值，按固定值和百分比分类
		Map<PropKey, Float> byFix = new HashMap<>();
		Map<PropKey, Float> byPct = new HashMap<>();
		Map<String, Float> unknown = new HashMap<>();

		// 1.1 强化的加成放入对应的Map
		int qhLv = part.getQianghuaLv();
		int professionIndex = humanObj.getHuman().getProfession();
		ConfPartQianghuaEffect confQh = ConfPartQianghuaEffect
				.get(part.getSn());
		int attrLen = confPart.attrType.length;
		for (int j = 0; j < attrLen; j++) {
			PropKey propKey = PropKey.getEnumByType(confPart.attrType[j]);
			int propValue = confQh.effetValue[j + professionIndex * attrLen]
					* qhLv;
			byFix.merge(propKey, (float) propValue, Float::sum);
		}
		// 1.2 冲星的加成放入对应的Map
		String cxLvStr = part.getChongxingLv();
		JSONObject cxLv = Utils.toJSONObject(cxLvStr);
		cxLv.forEach((phase, starJson) -> {
			Integer star = TypeUtils.castToInt(starJson);
			if (star != null && star > 0) {
				ConfPartChongxingEffect confCx = ConfPartChongxingEffect.getBy(
						ConfPartChongxingEffect.K.phase,
						Integer.valueOf(phase), ConfPartChongxingEffect.K.star,
						star);
				for (int j = 0; j < confPart.attrType.length; j++) {
					PropKey propKey = PropKey
							.getEnumByType(confPart.attrType[j]);
					int propValue = confCx.effetValue;
					byPct.merge(propKey, (float) propValue, Float::sum);
				}
			}
		});
		// 1.3 镶嵌宝石的加成放入对应的Map
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		String gems = part.getGemsIds();
		JSONObject gemIds = Utils.toJSONObject(gems);
		gemIds.forEach((pos, gemIdJson) -> {
			Long gemId = TypeUtils.castToLong(gemIdJson);
			if (gemId != null && gemId > 0) {
				Item gem = pack.getFromBody(gemId);
				ConfGemBase confGem = ConfGemBase.get(gem.getSn());
				for (int j = 0; j < confGem.attrType.length; j++) {
					String propName = confGem.attrType[j];
					PropKey propKey = PropKey.getEnumByType(propName);
					int propValue = confGem.attrValue[j];
					if (propKey != null) {
						byFix.merge(propKey, (float) propValue, Float::sum);
					} else {
						unknown.merge(propName, (float) propValue, Float::sum);
					}
				}
			}
		});
		// 2.统计完毕开始计算
		PropCalcCommon propCalc = new PropCalcCommon();
		// 2.1 基础值
		if (part.getEquipId() > 0) {
			Item equipOnPart = ItemBodyManager.inst().__getEquipOnPart(
					humanObj, part);
			propCalc.plus(equipOnPart.getPropJSON());
		}
		// 2.2 基础值乘以百分比
		byPct.forEach((propKey, attrValue) -> {
			String name = propKey.name();
			if (propCalc.getInt(name) > 0) {
				propCalc.mul(name, 1 + 0.0001f * attrValue);
			}
		});
		// 2.3 再加固定值
		byFix.forEach((propKey, attrValue) -> {
			String name = propKey.name();
			if (propCalc.getInt(name) > 0) {
				propCalc.plus(name, attrValue.doubleValue());
			}
		});

		// 2.4 融合中间属性
		unknown.forEach((propName, attrValue) -> {
			propCalc.plus(propName, attrValue.doubleValue());
		});
		// 3.赋值到装备位
		part.setPropJson(propCalc.toJSONStr());
	}

	/**
	 * 处理冲星。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param partSn
	 *            装备位
	 * @param phase
	 *            阶段
	 * @param addPct
	 *            手动附加成功率（5的倍数）
	 * @return 结果与原因。
	 */
	public ReasonResult partChongxing(HumanObject humanObj, int partSn,
			int phase, int addPct) {

		Part part = ItemBodyManager.inst().__getPart(humanObj, partSn);

		int cxLv = getChongxingLv(part, phase);

		// 判断等级上限
		ConfPartChongxingCost confCx = ConfPartChongxingCost.get(phase);
		int maxStar = ConfGlobalUtils.getValue(ConfGlobalKey.冲星最大几星);
		if (maxStar == cxLv) {
			return new ReasonResult(false, I18n.get("part.chongxing.maxStars"));
		}
		// 判断阶段开启
		if (humanObj.getHuman().getLevel() < confCx.needLevel) {
			return new ReasonResult(false,
					I18n.get("part.chongxing.wrongLevel"));
		}
		// 判断附加成功率是否合理
		if (addPct < 0 || addPct > 100 || addPct % 5 != 0) {
			return new ReasonResult(false, "part.chongxing.wrongCostItemNum");
		}
		// 判断消耗
		int needMoney = confCx.money[cxLv];

		ReasonResult canProduceReduce = HumanManager.inst().canProduceReduce(
				humanObj, confCx.moneyType, needMoney);
		if (!canProduceReduce.success) {
			return canProduceReduce;
		}
		int stoneNum = (int) Math.ceil(addPct * confCx.stones[cxLv]);   // 向上取整
		// VIP判断
		ConfVip confVip = ConfVip.get(humanObj.getHuman().getVipLevel());
		if (confVip != null) {
			int ratio = confVip.chongXingEffect;
			stoneNum = (int) Math.floor(stoneNum-stoneNum*ratio/10);   // 向下取整
		}
		
		ReasonResult canRemove = ItemBagManager.inst().canRemove(humanObj,
				confCx.stoneSn, stoneNum);
		if (!canRemove.success) {
			return canRemove;
		}
		// 处理消耗
		HumanManager.inst().produceMoneyReduce(humanObj, confCx.moneyType,
				needMoney, MoneyReduceLogKey.装备位冲星);
		ItemBagManager.inst().remove(humanObj, confCx.stoneSn, stoneNum);
		// 随机是否成功
		int prob = Integer.min(addPct, confCx.maxAddPct[cxLv]);
		int ranInt = RandomUtils.nextInt(100);
		boolean randSuccess = prob > ranInt;
		int newLv = cxLv;
		if (randSuccess) {
			// 若成功处理升级
			newLv = cxLv + 1;
		} else {
			// 若失败处理掉级
			ConfPartChongxingEffect confCxEffect = ConfPartChongxingEffect
					.getBy(ConfPartChongxingEffect.K.phase,
							Integer.valueOf(phase),
							ConfPartChongxingEffect.K.star, cxLv + 1);
			Map<Integer, Integer> weightMap = new HashMap<>();
			for (int i = 0; i < confCxEffect.failEffect.length; i += 2) {
				weightMap.put(confCxEffect.failEffect[i],
						confCxEffect.failEffect[i + 1]);
			}
			int failEffect = ItemRandomTools.randomWithJSONObject(weightMap);
			newLv = cxLv + failEffect; // failEffect配负数
		}
		String cxStr = part.getChongxingLv();
		JSONObject json = Utils.toJSONObject(cxStr);
		json.put(String.valueOf(phase), newLv);
		part.setChongxingLv(Utils.toJSONString(json));

		// 重算单个装备位属性
		__updatePartProp(humanObj, part);
		// 监听用户属性变化
		HumanInfoChange.listen(humanObj);
		// 所有装备属性重新影响人物属性
		__updateBodyProp(humanObj);

		// 如果生效则发出装备变更事件
		Event.fire(EventKey.BODY_ITEM_CHANGE, "humanObject", humanObj,
				"publish", true);
		//发布任务冲星装备位事件
		Event.fire(EventKey.BODY_PART_CHONGXING, "humanObj", humanObj);
		
		// 回消息
		SCPartChongxing.Builder mb = SCPartChongxing.newBuilder();
		mb.setCode(0);
		mb.setGrade(newLv);
		humanObj.sendMsg(mb);

		return new ReasonResult(true);
	}

	/**
	 * 取某阶段冲星等级。
	 * 
	 * @param part
	 *            装备位
	 * @param phaseSn
	 *            阶段
	 * @return 阶段冲星等级。
	 */
	public int getChongxingLv(Part part, int phaseSn) {
		String cxLvStr = part.getChongxingLv();
		JSONObject cxLv = Utils.toJSONObject(cxLvStr);
		return cxLv.getIntValue(String.valueOf(phaseSn));
	}

	/**
	 * 处理镶嵌指定宝石。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param partSn
	 *            装备位
	 * @param bagIndex
	 *            宝石在背包的位置
	 * @return 结果与原因。
	 */
	public ReasonResult partGemUp(HumanObject humanObj, int partSn, int bagIndex) {

		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		// 找出选择的宝石
		Item toGem = pack.get(bagIndex, ItemPackTypeKey.Bag1.packType);
		// 判断是否有宝石
		if (toGem == null) {
			return new ReasonResult(false, I18n.get("item.ruby.insert.noRuby"));
		}
		// 判断是否是宝石
		ConfItemData confItem = ConfItemData.get(toGem.getSn());
		if (ItemTypeKey.宝石 != confItem.bType) {
			return new ReasonResult(false, I18n.get("item.ruby.insert.notRuby"));
		}
		// 判断宝石使用等级
		int humanLv = humanObj.getHuman().getLevel();
		if (confItem.level > humanLv) {
			return new ReasonResult(false, "等级不够不能使用此宝石");
		}
		// 匹配对应的槽
		Item gemed = null;
		Part part = ItemBodyManager.inst().__getPart(humanObj, partSn);
		ConfPartsData confPart = ConfPartsData.get(partSn);
		int[] confSlotTypes = confPart.gemTypeSn;
		int slot = -1;
		for (int i = 0; i < confSlotTypes.length; i++) {
			// 找槽时要判断开启
			if (confPart.gemNeedLv[i] <= humanLv) {
				int confSlotType = confSlotTypes[i];
				if (confSlotType == confItem.sType) {
					slot = i;
					// 优先找空槽
					gemed = __findGemOnSlot(pack, part, slot);
					if (gemed == null)
						break;
				}
			}
		}
		// 如果没找到对应合适的槽就错了
		if (slot == -1) {
			return new ReasonResult(false,
					I18n.get("item.ruby.insert.wrongType"));
		}
		// 处理交换
		ItemChange itemChange = __swapGemsFromBodyToBag(toGem, gemed, part,
				pack, slot);
		// 重算单个装备位属性
		__updatePartProp(humanObj, part);
		// 监听用户属性变化
		HumanInfoChange.listen(humanObj);
		// 所有装备属性重新影响人物属性
		__updateBodyProp(humanObj);

		// 触发身上装备变化事件
		Event.fire(EventKey.BODY_ITEM_CHANGE, "humanObject", humanObj,
				"publish", true);
		//发布任务宝石镶嵌事件
		Event.fire(EventKey.BODY_GEM_CHANGE, "humanObj", humanObj);
		
		// 回穿戴成功消息
		SCSetGem.Builder success = SCSetGem.newBuilder();
		success.setCode(0);
		success.addPosInfo(slot);
		success.addPosInfo(toGem.getSn());
		humanObj.sendMsg(success);

		// 回背包变化消息
		itemChange.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return new ReasonResult(true);
	}

	/**
	 * 找部位某槽上的宝石。
	 * 
	 * @param pack
	 *            背包
	 * @param part
	 *            部位
	 * @param slot
	 *            槽
	 * @return 部位某槽上的宝石或{@code null}。
	 */
	private Item __findGemOnSlot(ItemPack pack, Part part, int slot) {
		String gems = part.getGemsIds();
		JSONObject gemIds = Utils.toJSONObject(gems);
		long gemId = gemIds.getLongValue(String.valueOf(slot));
		if (gemId > 0) {
			return pack.getFromBody(gemId);
		}
		return null;
	}

	/**
	 * 交换背包中和宝石位的宝石。
	 * <p>
	 * 注意：不判断背包容量
	 * 
	 * @param toGem
	 *            要上位的宝石
	 * @param gemed
	 *            要下位的宝石
	 * @param part
	 *            装备位
	 * @param pack
	 *            背包
	 * @param slot
	 *            宝石槽下标
	 * @return 背包物品变化情况。
	 */
	private ItemChange __swapGemsFromBodyToBag(Item toGem, Item gemed,
			Part part, ItemPack pack, int slot) {
		ItemChange itemChange = new ItemChange();
		// 若有物品要上装备位，先从包里移走。
		if (toGem != null) {
			int bagType = ItemPack.selectBagType(toGem.getSn());
			pack.remove(toGem.getPosition(), bagType);
			// 背包标记更新
			itemChange.markDeleting(toGem);
		}
		// 若有物品要下装备位，就从装备位移走。
		if (gemed != null) {
			pack.removeBody(gemed.getId());
		}
		// 若有物品要下装备位，此物品放到背包。
		if (gemed != null) {
			int bagType = ItemPack.selectBagType(gemed.getSn());
			// 手动设置位置
			gemed.setPackType(bagType);
			int toBagPosition = pack.getFirstFreePosition(bagType);
			gemed.setPosition(toBagPosition);
			pack.addToBag(gemed);
			// 背包标记更新
			itemChange.markAdded(gemed);
		}
		// 若有物品要上装备位，上之。
		if (toGem != null) {
			// 手动设置装备位
			int partSn = part.getSn();
			toGem.setPosition(partSn);
			toGem.setPackType(ItemPackTypeKey.Parts.packType);

			pack.toBody(toGem);
		}

		String gemsIds = part.getGemsIds();
		JSONObject gemIdsJson = Utils.toJSONObject(gemsIds);
		gemIdsJson.put(String.valueOf(slot), toGem != null ? toGem.getId() : 0);
		part.setGemsIds(gemIdsJson.toJSONString());
		return itemChange;
	}

	/**
	 * 一键填满三个宝石槽。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param partSn
	 *            装备位SN
	 * @return 结果与原因。
	 */
	public ReasonResult partGemUpAll(HumanObject humanObj, int partSn) {
		// 判断宝石位开启
		ConfPartsData confPart = ConfPartsData.get(partSn);
		boolean[] usableSlots = new boolean[3];
		int humanLv = humanObj.getHuman().getLevel();
		boolean theresAvailable = false;
		for (int i = 0; i < 3; i++) {
			usableSlots[i] = confPart.gemNeedLv[i] <= humanLv;
			theresAvailable = true;
		}
		if (!theresAvailable) {
			return new ReasonResult(false, "一个能用的槽都没有");
		}
		// 准备好要用的对象
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		Part part = ItemBodyManager.inst().__getPart(humanObj, partSn);
		String gemInfos = part.getGemsIds();
		JSONObject gemIds = Utils.toJSONObject(gemInfos);
		int[] confSlotTypes = confPart.gemTypeSn;
		boolean shouldSwap = false;
		List<Integer> posInfo = new ArrayList<>();
		ItemChange itemChange = new ItemChange();
		// 挨个按照槽类型找宝石
		for (int slot = 0; slot < confSlotTypes.length; slot++) {
			if (!usableSlots[slot]) {
				continue;
			}
			int confSlotType = confSlotTypes[slot];

			// 从包里找对应种类宝石
			Item bestGemInBag = __selectBestGem(pack, confSlotType);

			if (bestGemInBag != null) {
				// 找槽上已有的宝石
				Item gemed = null;
				long gemId = gemIds.getLongValue(String.valueOf(slot));
				if (gemId > 0) {
					gemed = pack.getFromBody(gemId);
				}
				// 再比较一次好坏
				boolean isBetter = __isGemBetter(bestGemInBag, gemed);
				// 如果确实更好
				if (isBetter) {
					// 交换位置
					itemChange.merge(__swapGemsFromBodyToBag(bestGemInBag,
							gemed, part, pack, slot));
					// 记录交换
					posInfo.add(slot);
					posInfo.add(bestGemInBag == null ? 0 : bestGemInBag.getSn());
					shouldSwap = true;
				}
			}
		}

		// 如果交换过
		if (shouldSwap) {
			// 重算单个装备位属性
			__updatePartProp(humanObj, part);
			// 监听用户属性变化
			HumanInfoChange.listen(humanObj);
			// 所有装备属性重新影响人物属性
			__updateBodyProp(humanObj);

			// 触发身上装备变化事件
			Event.fire(EventKey.BODY_ITEM_CHANGE, "humanObject", humanObj,
					"publish", true);

			// 回穿戴成功消息
			SCSetGem.Builder success = SCSetGem.newBuilder();
			success.setCode(0);
			success.addAllPosInfo(posInfo);
			humanObj.sendMsg(success);

			// 回背包变化消息
			itemChange.fireChangeEvent(humanObj);
			ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		} else {
			// 回穿戴成功消息
			SCSetGem.Builder success = SCSetGem.newBuilder();
			success.setCode(0);
			humanObj.sendMsg(success);
		}

		return new ReasonResult(true);
	}

	/**
	 * 判断背包中的某宝石是否比装备位上的好。
	 * 
	 * @param bestGemInBag
	 *            背包中的宝石
	 * @param gemed
	 *            装备位上的宝石
	 * @return <code>true</code>是，<code>false</code> 否。
	 */
	private boolean __isGemBetter(Item bestGemInBag, Item gemed) {
		// 如果包里没找出，那就别让他交换了
		if (bestGemInBag == null) {
			return false;
		} else {
			if (gemed == null) {
				return true;
			} else {
				return ConfGemBase.get(bestGemInBag.getSn()).combat > ConfGemBase
						.get(gemed.getSn()).combat; // 战斗力判断
			}
		}
	}

	/**
	 * 从背包中找最好的宝石。
	 * 
	 * @param pack
	 *            背包
	 * @param gemType
	 *            宝石类型
	 * @return 最好的宝石。
	 */
	private Item __selectBestGem(ItemPack pack, int gemType) {
		Item result = null;
		// TODO 这里是假定宝石都是在主背包里的，按说背包类型找物品应该有个对应法则
		Bag<Item> __gemBag = pack
				.__getProperBag(ItemPackTypeKey.Bag1.packType);
		for (Item item : __gemBag.getList()) {
			if (item != null) {
				ConfItemData confItem = ConfItemData.get(item.getSn());
				if (confItem.bType == ItemTypeKey.宝石
						&& confItem.sType == gemType) {
					// 找到了，和前一个比较好坏
					if (result == null) {
						result = item;
					} else if (__isGemBetter(result, item)) {
						result = item;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 卸下一个指定宝石。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param partSn
	 *            装备位Sn
	 * @param slot
	 *            宝石槽下标
	 * @return 结果与原因。
	 */
	public ReasonResult partGemDown(HumanObject humanObj, int partSn, int slot) {
		// 判断宝石位开启
		ConfPartsData confPart = ConfPartsData.get(partSn);
		int humanLv = humanObj.getHuman().getLevel();
		if (confPart.gemNeedLv[slot] > humanLv) {
			return new ReasonResult(false,
					I18n.get("item.ruby.insert.wrongLevel"));
		}
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		Part part = ItemBodyManager.inst().__getPart(humanObj, partSn);
		// 找出选择的宝石
		Item gemed = __findGemOnSlot(pack, part, slot);
		// 判断是否有宝石
		if (gemed == null) {
			return new ReasonResult(false, I18n.get("item.ruby.remove.noRuby"));
		}
		// 判断背包容量
		int packType = ItemPack.selectBagType(gemed.getSn());
		int posFree = pack.getFirstFreePosition(packType);
		int posLimit = ItemBagManager.inst().getNumMax(humanObj, packType);
		if (posLimit < posFree) {
			return new ReasonResult(false, I18n.get("item.error.numNotEnough"));
		}
		// 处理交换
		ItemChange itemChange = __swapGemsFromBodyToBag(null, gemed, part,
				pack, slot);
		// 重算单个装备位属性
		__updatePartProp(humanObj, part);
		// 监听用户属性变化
		HumanInfoChange.listen(humanObj);
		// 所有装备属性重新影响人物属性
		__updateBodyProp(humanObj);

		// 触发身上装备变化事件
		Event.fire(EventKey.BODY_ITEM_CHANGE, "humanObject", humanObj,
				"publish", true);

		// 回脱成功消息
		SCSetGem.Builder success = SCSetGem.newBuilder();
		success.setCode(0);
		success.addPosInfo(slot);
		success.addPosInfo(0);
		humanObj.sendMsg(success);

		// 回背包变化消息
		itemChange.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return new ReasonResult(true);
	}

	/**
	 * 一键卸下某装备位的全部宝石。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param partSn
	 *            装备位Sn
	 * @return 结果与原因。
	 */
	public ReasonResult partGemDownAll(HumanObject humanObj, int partSn) {
		// 判断宝石位开启
		ConfPartsData confPart = ConfPartsData.get(partSn);
		int humanLv = humanObj.getHuman().getLevel();
		boolean canDown = false;
		for (int slotNeedLv : confPart.gemNeedLv) {
			if (slotNeedLv <= humanLv) {
				canDown = true;
				break;
			}
		}
		if (!canDown) {
			return new ReasonResult(false,
					I18n.get("item.ruby.insert.wrongLevel"));
		}
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		Part part = ItemBodyManager.inst().__getPart(humanObj, partSn);

		// 找出此部位的所有宝石
		Map<Integer, Item> gemedList = new HashMap<>();
		List<ItemVO> itemVOs = new ArrayList<>();
		String gems = part.getGemsIds();
		JSONObject gemIds = Utils.toJSONObject(gems);
		gemIds.forEach((slotKey, gemIdJson) -> {
			Long gemId = TypeUtils.castToLong(gemIdJson);
			if (gemId != null && gemId > 0) {
				Item gemed = pack.getFromBody(gemId);
				gemedList.put(TypeUtils.castToInt(slotKey), gemed);

				itemVOs.add(new ItemVO(gemed.getSn(), gemed.getNum()));
			}
		});
		// 判断是否有宝石
		if (gemedList.isEmpty()) {
			return new ReasonResult(false, I18n.get("item.ruby.remove.noRuby"));
		}
		// 判断背包容量
		ReasonResult canAdd = ItemBagManager.inst().canAdd(humanObj, itemVOs);
		if (!canAdd.success) {
			return new ReasonResult(false, I18n.get("item.error.numNotEnough"));
		}
		// 处理交换
		ItemChange itemChange = new ItemChange();
		gemedList.forEach((slot, gemed) -> {
			itemChange.merge(__swapGemsFromBodyToBag(null, gemed, part, pack,
					slot));
		});

		// 重算单个装备位属性
		__updatePartProp(humanObj, part);
		// 监听用户属性变化
		HumanInfoChange.listen(humanObj);
		// 所有装备属性重新影响人物属性
		__updateBodyProp(humanObj);

		// 触发身上装备变化事件
		Event.fire(EventKey.BODY_ITEM_CHANGE, "humanObject", humanObj,
				"publish", true);

		// 回脱成功消息
		SCSetGem.Builder success = SCSetGem.newBuilder();
		success.setCode(0);
		success.addPosInfo(0);
		success.addPosInfo(0);
		success.addPosInfo(1);
		success.addPosInfo(0);
		success.addPosInfo(2);
		success.addPosInfo(0);
		humanObj.sendMsg(success);

		// 回背包变化消息
		itemChange.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		return new ReasonResult(true);
	}

	/**
	 * 选定宝石合成一级。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param position
	 *            选定宝石在背包的位置。
	 * @return 结果与原因。
	 */
	public ReasonResult gemComposite(HumanObject humanObj, int position) {
		int bagType = ItemPackTypeKey.Bag1.packType; // XXX 注意一下：宝石全在背包1
		// 查找背包物品
		ItemPack bag = ItemBagManager.inst().__getPack(humanObj);
		Item item = bag.get(position, bagType);
		if (item == null) {
			return new ReasonResult(false, I18n.get("item.common.noItem"));
		}

		// 查找配置
		ConfItemData confItem = ConfItemData.get(item.getSn());
		if (confItem == null) {
			return new ReasonResult(false, I18n.get("common.tip.configWrong"));
		}

		// 判断是否是宝石
		if (confItem.bType != ItemTypeKey.宝石) {
			return new ReasonResult(false, I18n.get("item.error.canNotMerge"));
		}
		// 判断是否可以合成
		ConfGemBase confGem = ConfGemBase.get(item.getSn());
		if (confGem.next[0] == 0) {
			return new ReasonResult(false, I18n.get("item.error.canNotMerge"));
		}

		// 找材料，判断数量
		List<Item> materialGems = __solveGemComposite(item, bag);
		if (materialGems == null) {
			return new ReasonResult(false, I18n.get("item.common.notEnough"));
		}
		// 删材料
		ItemChange itemChange = new ItemChange();
		for (Item m : materialGems) {
			bag.remove(m.getPosition(), m.getPackType());
			m.remove();
			// 记录被删除的物品
			itemChange.markDeleting(m);
		}
		// 告知一次客户端物品减少
		itemChange.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		// 背包加新物品（肯定容量够、内部第二次告知背包更新）
		ItemBagManager.inst().add(humanObj, confGem.next[0], 1, item.getBind());

		// 告知客户端
		SCGemComposite.Builder success = SCGemComposite.newBuilder();
		success.setCode(0);
		humanObj.sendMsg(success);

		return new ReasonResult(true);
	}

	/**
	 * 给{@link #gemComposite(HumanObject, int)}找材料。
	 * 
	 * @param gem
	 *            选定宝石
	 * @param pack
	 *            背包
	 * @return 若材料足够，返回以选定宝石为首的一系列被消耗材料；若不足，返回{@code null}。
	 */
	private List<Item> __solveGemComposite(Item gem, ItemPack pack) {
		int sn = gem.getSn();
		ConfGemBase conf = ConfGemBase.get(sn);
		// 无合成功能
		if (conf.next[0] == 0)
			return null;
		// 有合成功能时判断材料数量
		int need = conf.next[1];
		List<Item> sameGems = pack.findBySnBind(sn, gem.getBind());
		// 不足返回null
		if (sameGems.size() < need)
			return null;
		List<Item> result = new ArrayList<>();
		// 指定的那个宝石第一个就给他删除掉
		result.add(gem);
		need--;
		// 再随便找点其他的同类宝石凑数
		for (int i = 0, sz = sameGems.size(); need > 0 && i < sz; i++) {
			Item gemA = sameGems.get(i);
			if (gem != gemA) {
				result.add(gemA);
				need--;
			}
		}
		return result;
	}

	/**
	 * 选定宝石合成到尽量大的级。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param position
	 *            选定宝石在背包的位置。
	 * @return 结果与原因。
	 */
	public ReasonResult gemCompositeTop(HumanObject humanObj, int position) {
		int bagType = ItemPackTypeKey.Bag1.packType; // XXX 注意一下：宝石全在背包1
		// 查找背包物品
		ItemPack bag = ItemBagManager.inst().__getPack(humanObj);
		Item item = bag.get(position, bagType);
		if (item == null) {
			return new ReasonResult(false, I18n.get("item.common.noItem"));
		}

		// 查找配置
		ConfItemData confItem = ConfItemData.get(item.getSn());
		if (confItem == null) {
			return new ReasonResult(false, I18n.get("common.tip.configWrong"));
		}

		// 判断是否是宝石
		if (confItem.bType != ItemTypeKey.宝石) {
			return new ReasonResult(false, I18n.get("item.error.canNotMerge"));
		}
		// 判断是否可以合成
		ConfGemBase confGem = ConfGemBase.get(item.getSn());
		if (confGem.next[0] == 0) {
			return new ReasonResult(false, I18n.get("item.error.canNotMerge"));
		}

		// 找材料，判断数量，找材料时能得知最终晋级后的物品sn
		Param mResult = __solveGemCompositeTop(item, bag);
		if (mResult == null) {
			return new ReasonResult(false, I18n.get("item.common.notEnough"));
		}
		// 删材料
		ItemChange itemChange = new ItemChange();
		List<Item> materials = mResult.get("toRemove");
		for (Item m : materials) {
			bag.remove(m.getPosition(), m.getPackType());
			m.remove();
			// 记录被删除的物品
			itemChange.markDeleting(m);
		}
		// 告知一次客户端物品减少
		itemChange.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChange);
		// 背包加新物品（肯定容量够、内部第二次告知背包更新）
		int toSn = mResult.getInt("toSn");
		ItemBagManager.inst().add(humanObj, toSn, 1, item.getBind());

		// 告知客户端
		SCGemCompositeTop.Builder success = SCGemCompositeTop.newBuilder();
		success.setCode(0);
		humanObj.sendMsg(success);

		return new ReasonResult(true);
	}

	/**
	 * 给{@link #gemCompositeTop(HumanObject, int)}找材料。
	 * <p>
	 * 在返回值中取扣除的材料：<br>
	 * <code>
	 * 	List&lt;Item&gt; toRemove = param.get(&quot;toRemove&quot;);
	 * </code><br>
	 * 
	 * 在返回值中取生成新宝石SN：<br>
	 * <code>
	 * 	int toSn = param.get(&quot;toSn&quot;);
	 * </code>
	 * 
	 * @param gem
	 *            选定宝石
	 * @param pack
	 *            背包
	 * @return 若材料足够，返回解决方案（生成物SN和以选定宝石为首的一系列被消耗材料）；若不足，返回{@code null}。
	 */
	@SuppressWarnings("unchecked")
	private Param __solveGemCompositeTop(Item gem, ItemPack pack) {
		// 找出有合成潜力的宝石
		List<Item> gems = __findCompositableGems(pack, gem.getPackType());
		// 筛选出合成链中的宝石
		__filterUnrelatives(gem, gems);
		// 想出全部合成至最高级的办法
		List<Item> toRemove = null;
		Integer toSn = null;
		{
			// 这个MultiMap既放实体Item，又放虚拟出的ItemVO，为了计数方便，每个ItemVO对象的num都是1
			MultiValueMap bySn = MultiValueMap.decorate(
					new HashMap<Integer, Object>(), ArrayList::new); // 用ArrayList实现MultiMap，保证ItemVO在所有Item之后。

			// 宝石按SN分类灌入MultiMap
			gems.forEach(item -> bySn.put(item.getSn(), item));

			// 一只循环bySn，直到所有sn的都不足以生成新东西
			while (true) {
				boolean needCheck = false;
				Set<Integer> keys = new HashSet<>(bySn.keySet());
				for (Integer sn : keys) {
					ConfGemBase confGem = ConfGemBase.get(sn);
					if (confGem.next[0] == 0) {
						// 这个SN是顶级的，不用管它了
						continue;
					} else {
						// 非顶级的判断是否足够升级
						int nextSn = confGem.next[0];
						int oneNeed = confGem.next[1];
						int has = bySn.size(sn);
						if (oneNeed <= has) {
							// 算算可以生成多少个
							int canProduce = has / oneNeed;
							// 反向迭代器，保证优先删除FakeGem
							@SuppressWarnings("rawtypes")
							ReverseListIterator iterator = new ReverseListIterator(
									(List) bySn.getCollection(sn));
							// 可生成的东西加到bySn的对应Key的末尾，与此同时材料从bySn中删除
							for (int i = canProduce; i > 0; i--) {
								FakeGem fakeGem = new FakeGem(nextSn);
								for (int j = oneNeed; j > 0; j--) {
									Object object = iterator.next();
									iterator.remove();
									fakeGem.materials.add(object);
								}
								bySn.put(nextSn, fakeGem);
							}
							// 生成新东西的时候，就需要再循环判断一次了
							needCheck = true;
						}
					}
				}
				// 循环一遍bySn没有可生成的新东西了，就跳出
				if (!needCheck) {
					break;
				}
			}
			// 找出使用gem为材料的那一个
			for (Object _entry : bySn.entrySet()) {
				Map.Entry<Integer, List<Object>> entry = (Map.Entry<Integer, List<Object>>) _entry;
				Integer sn = entry.getKey();
				List<Object> objlist = entry.getValue();
				for (Object obj : objlist) {
					if ((obj instanceof FakeGem)) {
						FakeGem fkGem = (FakeGem) obj;
						if (fkGem.isMaterialContains(gem)) {
							toSn = sn;
							toRemove = fkGem.getRemoves();
						}
					}
				}
			}
		}
		// 未能合成上次sn，返回null
		if (toSn == null) {
			return null;
		} else {
			return new Param("toSn", toSn, "toRemove", toRemove);
		}
	}

	/**
	 * 伪物品，表示中间生成的物品，便于写算法。
	 * 
	 * @author zhangbo
	 *
	 */
	private static class FakeGem {
		@SuppressWarnings("unused")
		int sn;
		List<Object> materials;

		public FakeGem(int sn) {
			this.sn = sn;
			materials = new ArrayList<>();
		}

		/**
		 * 判断这个伪宝石的合成材料是否包含{@code gem}。
		 * 
		 * @param gem
		 *            指定宝石
		 * @return 这个伪宝石的合成材料是否包含{@code gem}。
		 */
		boolean isMaterialContains(Item gem) {
			for (Object material : materials) {
				if (material instanceof FakeGem) {
					FakeGem fakeGem = (FakeGem) material;
					boolean contains = fakeGem.isMaterialContains(gem);
					if (contains)
						return true;
				}
				if (material == gem) {
					return true;
				}
			}
			return false;
		}

		/**
		 * 递归得到这个伪物品的合成材料有哪些真物品。
		 * 
		 * @return 合成材料有哪些真物品。
		 */
		List<Item> getRemoves() {
			List<Item> result = new ArrayList<>();
			for (Object material : materials) {
				if (material instanceof Item) {
					result.add((Item) material);
				}
				if (material instanceof FakeGem) {
					FakeGem fakeGem = (FakeGem) material;
					result.addAll(fakeGem.getRemoves());
				}
			}
			return result;
		}
	}

	/**
	 * 从一堆宝石{@code gems}中过滤掉和{@code gem}合成关系无关的。
	 * 
	 * @param gem
	 *            合成宝石
	 * @param gems
	 *            所有宝石
	 */
	private void __filterUnrelatives(Item gem, List<Item> gems) {
		int exampleSn = gem.getSn();
		z: for (Iterator<Item> iterator = gems.iterator(); iterator.hasNext();) {
			Item item = iterator.next();
			// 如果这个item和gem是一类，保留
			int checkSn = item.getSn();
			if (checkSn == exampleSn) {
				continue;
			}
			// 如果这个item可以合成到gem，保留
			int crtSn = checkSn;
			do {
				ConfGemBase conf1 = ConfGemBase.get(crtSn);
				if (conf1 == null) {
					break;
				}
				crtSn = conf1.next[0];
				if (conf1.next[0] == exampleSn) {
					continue z;
				}
			} while (true);
			// 如果gem可以合成到item，保留
			crtSn = exampleSn;
			do {
				ConfGemBase conf1 = ConfGemBase.get(crtSn);
				if (conf1 == null) {
					break;
				}
				crtSn = conf1.next[0];
				if (conf1.next[0] == checkSn) {
					continue z;
				}
			} while (true);
			// 否则是无关的宝石，过滤掉
			iterator.remove();
		}
	}

	/**
	 * 背包里所有的宝石尽量合成到高级。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @return 结果与原因。
	 */
	public ReasonResult gemCompositeAllTop(HumanObject humanObj) {
		int bagType = ItemPackTypeKey.Bag1.packType; // XXX 注意一下：宝石全在背包1
		// 查找背包可合成的宝石
		ItemPack pack = ItemBagManager.inst().__getPack(humanObj);
		List<Item> gems = __findCompositableGems(pack, bagType);
		if (gems.isEmpty()) {
			return new ReasonResult(false, "没有能合成的宝石");
		}

		// 分析gems列表，找材料，算出能生成哪些新宝石，要扣掉哪些旧宝石
		Param mResult = __solveGemCompositeAllTop(gems);
		List<Item> toRemove = mResult.get("toRemove");
		if (toRemove.isEmpty()) {
			return new ReasonResult(false, I18n.get("item.common.notEnough"));
		}
		// 删材料
		ItemChange itemChangeVO = new ItemChange();
		for (Item material : toRemove) {
			pack.remove(material.getPosition(), material.getPackType());
			material.remove();
			// 记录被删除的物品
			itemChangeVO.markDeleting(material);
		}
		// 告知一次客户端物品减少
		itemChangeVO.fireChangeEvent(humanObj);
		ItemBagManager.inst().sendChangeMsg(humanObj, itemChangeVO);
		// 背包加新物品（肯定容量够、内部第二次告知背包更新）
		List<ItemVO> toGen = mResult.get("toGen");
		if (!toGen.isEmpty())
			ItemBagManager.inst().add(humanObj, toGen);

		// 告知客户端
		SCGemCompositeAllTop.Builder success = SCGemCompositeAllTop
				.newBuilder();
		success.setCode(0);
		humanObj.sendMsg(success);

		return new ReasonResult(true);
	}

	/**
	 * 给{@link #gemCompositeAllTop(HumanObject)}找有合成潜力的宝石，即未到最顶级的。
	 * 
	 * @param pack
	 *            背包
	 * @param bagType
	 *            子包类型
	 * @return 有合成潜力的宝石列表。
	 */
	private List<Item> __findCompositableGems(ItemPack pack, int bagType) {
		List<Item> result = new ArrayList<>();
		Bag<Item> bag = pack.__getProperBag(bagType);
		for (Item item : bag.getList()) {
			// 非空
			if (item == null)
				continue;
			ConfItemData confItemData = ConfItemData.get(item.getSn());
			// 是宝石
			if (confItemData.bType == ItemTypeKey.宝石) {
				ConfGemBase confGemBase = ConfGemBase.get(item.getSn());
				// 未顶级
				if (confGemBase.next[0] != 0) {
					result.add(item);
				}
			}
		}
		return result;
	}

	/**
	 * 给{@link #gemCompositeAllTop(HumanObject)}找解决方案，包括如何扣材料和如何生成新宝石。
	 * <p>
	 * 在返回值中取扣除的材料：<br>
	 * <code>
	 * 	List&lt;Item&gt; toRemove = param.get(&quot;toRemove&quot;);
	 * </code><br>
	 * 
	 * 在返回值中取如何生成新宝石：<br>
	 * <code>
	 * 	List&lt;ItemVO&gt; toGen = param.get(&quot;toGen&quot;);
	 * </code>
	 * 
	 * @param gems
	 *            有合成潜力的宝石
	 * @return 如何扣材料和如何生成新宝石。
	 */
	@SuppressWarnings("unchecked")
	private Param __solveGemCompositeAllTop(List<Item> gems) {

		// 这个MultiMap既放实体Item，又放虚拟出的ItemVO，为了计数方便，每个ItemVO对象的num都是1
		MultiValueMap bySn = MultiValueMap.decorate(
				new HashMap<Integer, Object>(), ArrayList::new); // 用ArrayList实现MultiMap，保证ItemVO在所有Item之后。

		// 宝石按SN分类灌入MultiMap
		gems.forEach(item -> bySn.put(item.getSn(), item));

		List<Item> toRemove = new ArrayList<>();
		// 一只循环bySn，直到所有sn的都不足以生成新东西
		while (true) {
			boolean needCheck = false;
			Set<Integer> keys = new HashSet<>(bySn.keySet());
			for (Integer sn : keys) {
				ConfGemBase confGem = ConfGemBase.get(sn);
				if (confGem.next[0] == 0) {
					// 这个SN是顶级的，不用管它了
					continue;
				} else {
					// 非顶级的判断是否足够升级
					int nextSn = confGem.next[0];
					int oneNeed = confGem.next[1];
					int has = bySn.size(sn);
					if (oneNeed <= has) {
						// 算算可以生成多少个
						int canProduce = has / oneNeed;
						// 反向迭代器，保证优先删除ItemVO
						@SuppressWarnings("rawtypes")
						ReverseListIterator iterator = new ReverseListIterator(
								(List) bySn.getCollection(sn));
						// 记录要删的材料，删除了的材料从列表中删除
						for (int i = canProduce * oneNeed; i > 0; i--) {
							Object object = iterator.next();
							iterator.remove();
							if (object instanceof Item) {
								Item item = (Item) object;
								toRemove.add(item);
							}
						}
						// 可生成的东西加到bySn的对应Key的末尾
						for (int i = canProduce; i > 0; i--) {
							bySn.put(nextSn, new ItemVO(nextSn, 1,
									ItemBagManager.BIND_NO));
						}
						// 生成新东西的时候，就需要再循环判断一次了
						needCheck = true;
					}
				}
			}
			// 循环一遍bySn没有可生成的新东西了，就跳出
			if (!needCheck) {
				break;
			}
		}
		// bySn中找ItemVO，这些就是toGen的东西
		List<ItemVO> toGen = new ArrayList<>();
		bySn.forEach((snKey, snValue) -> {
			Integer sn = (Integer) snKey;
			List<Object> objlist = (List<Object>) snValue;
			int voNum = 0;
			for (Object obj : objlist) {
				if ((obj instanceof ItemVO))
					voNum++;
			}
			if (voNum > 0) {
				toGen.add(new ItemVO(sn, voNum, ItemBagManager.BIND_NO));
			}
		});
		Param result = new Param("toRemove", toRemove, "toGen", toGen);
		return result;
	}

	/**
	 * 玩家建号的时候，生成各个装备位。
	 * 
	 * @param param
	 */
	@Listener(EventKey.HUMAN_CREATE)
	public void createParts(Param param) {
		Human human = param.get("human");
		long humanId = human.getId();
		Collection<ConfPartsData> parts = ConfPartsData.findAll();
		// 循环所有配置的装备位，生成一条装备位数据
		for (ConfPartsData confPartsData : parts) {
			int sn = confPartsData.sn;
			// 赋初始值
			Part part = new Part();
			part.setId(Port.applyId());
			part.setHumanId(humanId);
			part.setSn(sn);
			part.setGemsIds("{}");
			part.setChongxingLv("{}");
			part.setPropJson("{}");
			// 存数据库
			part.persist();
		}
	}
}
