package org.gof.demo.worldsrv.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gof.core.Port;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.Utils;
import org.gof.core.support.observer.Listener;
import org.gof.demo.battlesrv.support.PropCalc;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfEquipBaseData;
import org.gof.demo.worldsrv.config.ConfGemBase;
import org.gof.demo.worldsrv.config.ConfItemData;
import org.gof.demo.worldsrv.entity.EntityItem;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.general.GeneralFragManager;
import org.gof.demo.worldsrv.human.HumanManager;
import org.gof.demo.worldsrv.inform.Inform;
import org.gof.demo.worldsrv.msg.Msg.DItem;
import org.gof.demo.worldsrv.msg.Msg.SCBagExpand;
import org.gof.demo.worldsrv.msg.Msg.SCBagUpdate;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.GetSetGrowthList;
import org.gof.demo.worldsrv.support.HumanException;
import org.gof.demo.worldsrv.support.I18n;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.ReasonResult;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;
import org.gof.demo.worldsrv.support.enumKey.ItemPackTypeKey;
import org.gof.demo.worldsrv.support.enumKey.ItemReduceTypeKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyAddLogKey;
import org.gof.demo.worldsrv.support.enumKey.MoneyReduceLogKey;
import org.gof.demo.worldsrv.support.enumKey.ProduceMoneyKey;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

import com.alibaba.fastjson.JSONObject;

/**
 * 背包逻辑。
 * 
 * @author zhangbo
 *
 */
public class ItemBagManager extends ManagerBase {

	public static final int BIND_NO = 0; // 不绑定
	public static final int BIND_YES = 1; // 绑定

	public static ItemBagManager inst() {
		return inst(ItemBagManager.class);
	}

	/**
	 * 获取玩家物品容器。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @return 玩家物品容器。
	 */
	ItemPack __getPack(HumanObject humanObj) {
		return humanObj.dataPers.items;

	}

	/**
	 * 添加多个物品，发送提示，发送变化消息。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param itemBagVOs
	 *            入包物品VO集合
	 * @return 背包变化。
	 * @see ItemVO
	 */
	public ItemChange add(HumanObject humanObj, List<ItemVO> itemBagVOs) {
		// 物品变化信息
		ItemChange itemChangeVO = new ItemChange();
		// 遍历添加
		for (ItemVO itemBagVO : itemBagVOs) {
			ConfItemData cd = ConfItemData.get(itemBagVO.sn);
			//伙伴碎片特殊处理
			if(cd.bType == ItemTypeKey.伙伴碎片){
				GeneralFragManager.inst().fragAdd(humanObj, itemBagVO.sn, itemBagVO.num);
			}else{
				ItemChange change = add(humanObj, itemBagVO.sn, itemBagVO.num, itemBagVO.bind, "sendMsg", false);
				itemChangeVO.merge(change);
			}
		}
		// 发送变化消息
		sendChangeMsg(humanObj.getHumanObj(), itemChangeVO);

		return itemChangeVO;
	}

	/**
	 * 增加物品 - 为了避免因为本函数报错，造成业务逻辑的BUG（发放物品到一半出错，造成刷物品BUG）， 所以对于错误情况会尽量的忽略，只记录日志。
	 * 
	 * @param humanObject  玩家{@code HumanObject}对象
	 * @param sn  物品SN
	 * @param num  数量
	 * @param bind  0 不绑定，1是绑定
	 * @param params 物品额外参数，没有不填
	 */
	public ItemChange add(HumanObject humanObj, int sn, int num, int bind, Object... params) {
		// 背包变化
		ItemChange itemChangeVO = new ItemChange();

		itemChangeVO.newly = true;

		// 解析参数
		Param param = new Param(params);
		// 默认值-发送数据变化
		boolean sendMsg = Utils.getParamValue(param, "sendMsg", true);
		// 默认值-空的扩展属性
		String p = Utils.getParamValue(param, "param", null);

		// 增加物品数量
		int count = num;

		// 物品配置
		ConfItemData conf = ConfItemData.get(sn);

		// 增加物品数量 0 的异常
		if (count <= 0)
			return itemChangeVO;
		// 物品配置错误
		if (conf == null) {
			Log.item.error(I18n.get("common.tip.configWrong") + ", sn = " + sn);
			return itemChangeVO;
		}
		
		//伙伴碎片特殊处理
		if(conf.bType == ItemTypeKey.伙伴碎片){
			GeneralFragManager.inst().fragAdd(humanObj, sn, num);
			return itemChangeVO;
		}

		// 玩家背包
		ItemPack pack = __getPack(humanObj);

		// 如果是可叠加物品 需要看下是否能与已有物品叠加
		if (conf.foldCount > 1) {
			// 所有可能叠加的物品
			Collection<Item> is = pack.findBySnBind(sn, bind);
			for (Item i : is) {
				// 可叠加数量
				int numol = conf.foldCount - i.getNum();
				// 不可继续叠加的忽略
				if (numol <= 0)
					continue;

				// 确认最终可叠加数量
				numol = Math.min(numol, count);
				// 剩余需要添加的数量
				count -= numol;

				// 叠加
				i.setNum(i.getNum() + numol);
				// 记录修改列表
				itemChangeVO.markNumChanged(i);

				// 堆完退出
				if (count <= 0)
					break;
			}
		}

		// 创建物品
		List<Item> items = create(humanObj.id, sn, count, bind);
		for (Item i : items) {
			int bagType = ItemPack.selectBagType(i.getSn());
			// T设置POS
			i.setPosition(pack.getFirstFreePosition(bagType));
			i.setPackType(bagType);
			// 设置扩展属性
			if (p != null) {
				i.setParam(p);
			}
			// 持久化
			i.persist();
			// 加入背包
			pack.addToBag(i);
			// 记录添加列表
			itemChangeVO.markAdded(i);
		}

		// 发送变动后的物品信息
		if (sendMsg) {
			sendChangeMsg(humanObj.getHumanObj(), itemChangeVO);
		}

		// 发送物品变动事件
		itemChangeVO.fireChangeEvent(humanObj.getHumanObj());

		// 自动使用物品
		if (__isAutoUse(sn)) {
			for (Item i : items) {
				ReasonResult rr = itemUseByPos(humanObj.getHumanObj(), i.getPackType(), i.getPosition(), i.getNum(), null);
				if (!rr.success) {
					Inform.user(humanObj.id, Inform.提示错误, rr.reason);
				}
			}
		}
		return itemChangeVO;
	}

	/**
	 * 创建入包物品，考虑叠加数，返回的是多个物品的集合。
	 * <p>
	 * 不执行存库。
	 * 
	 * @param humanId
	 *            玩家ID
	 * @param sn
	 *            物品SN
	 * @param num
	 *            数量
	 * @param bind
	 *            0不绑定1绑定
	 * @return 入包物品集合。
	 */
	public final List<Item> create(long humanId, int sn, int num, int bind) {
		// 配置信息
		ConfItemData confItem = ConfItemData.get(sn);
		// 叠加最大数量 不能小于1
		int overlap = confItem.foldCount;
		if (overlap < 1)
			overlap = 1;

		// 创建的物品
		List<Item> items = new GetSetGrowthList<>();

		// 剩余需要创建的物品数量
		int numRest = num;
		while (numRest > 0) {
			// 当前创建物品的数量
			int numItem = Math.min(numRest, overlap);
			// 剩余需要创建的物品数量
			numRest -= numItem;

			// 创建物品
			Item item = __createBase(humanId, confItem, bind);
			item.setNum(numItem);

			items.add(item);
		}

		return items;
	}

	/**
	 * 创建物品基本数据，不包括数量，不包括位置。
	 * 
	 * @param humanId
	 *            玩家Id
	 * @param conf
	 *            物品配置
	 * @param bind
	 *            0非绑定1绑定
	 * @return 只有基本数据信息的物品。
	 */
	private Item __createBase(long humanId, ConfItemData conf, int bind) {
		// 物品基本信息
		Item item = new Item();
		item.setSn(conf.sn);
		item.setId(Port.applyId());
		item.setBind(bind);
		item.setSn(conf.sn);
		item.setHumanId(humanId);
		item.setIsNew(true);

		// 这个数字create函数中设置
		// item.setNum(num);
		// 以下字段在加入背包时设置
		// item.setPosition(position);
		// item.setPackType(packType);

		// 剩下字段调用__initByType方法实现
		__initByType(item, conf);

		// 抛出物品创建事件
		Event.fire(EventKey.ITEM_INIT, "item", item);

		return item;
	}

	/**
	 * 根据配置的物品类型，给物品赋属性值。
	 * 
	 * @param item
	 *            初生的物品
	 * @param conf
	 *            物品配置
	 */
	private void __initByType(Item item, ConfItemData conf) {
		int bType = conf.bType;
		int sn = item.getSn();
		switch (bType) {
		case ItemTypeKey.装备:
			// 装备坯子基础属性
			PropCalc propCalc = new PropCalc();
			ConfEquipBaseData equipData = ConfEquipBaseData.get(sn);
			if (equipData != null) {
				String[] type = equipData.baseAttrType;
				int[] val = equipData.baseAttrValue;
				propCalc.plus(Utils.toJOSNString(type, val));
				item.setPropJSON(propCalc.toJSONStr());
				
				item.setCombat(equipData.combat);
			}
			
			break;
		case ItemTypeKey.宝石:
			// 注：宝石有属性的概念，但是我没有用这个propJSON字段记录，原因如下
			// 首先，宝石的属性可以从属性表读出
			// 更重要的是，宝石配的属性可以让他配百分比，这个百分比如果往propJSON里面记的话，每变一次装备就要重新赋值一次，吃力不讨好。
			// 这点不同于装备基础值，装备基础值只可能是固定值。
			item.setPropJSON("{}");
			ConfGemBase gemData = ConfGemBase.get(sn);
			item.setCombat(gemData.combat);
			break;
		case ItemTypeKey.道具:
			if (conf.sType == ItemTypeKey.IT_道具_随机礼包) {
				String giftInfo = ""; // 以后根据掉落组形成产出物品json
				item.setParam(giftInfo);
			}
			break;
		default:
			item.setPropJSON("{}");
		}
	}

	/**
	 * 发送物品变化消息给玩家。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param itemChange
	 *            背包的变化
	 */
	public void sendChangeMsg(HumanObject humanObj, ItemChange itemChange) {

		SCBagUpdate.Builder updateMsg = itemChange.getUpdateMsg();
		if (updateMsg == null) {
			return;
		}
		humanObj.sendMsg(updateMsg);
	}

	/**
	 * 判断是否可加入背包。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param sn
	 *            物品SN
	 * @param num
	 *            数量
	 * @param bind
	 *            0非绑定1绑定
	 * @return 结果与原因。
	 */
	public ReasonResult canAdd(HumanObject humanObj, int sn, int num, int bind) {
		ItemVO vo = new ItemVO(sn, num, bind);

		return canAdd(humanObj, Utils.ofList(vo));
	}

	/**
	 * 判断多个物品是否可加入背包。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param itemVOs
	 *            入包物品VO
	 * @return 结果与原因。
	 * @see ItemVO
	 */
	public ReasonResult canAdd(HumanObject humanObj, List<ItemVO> itemVOs) {
		// 批量判断时需要按需求分包
		List<ItemVO> itemToBag1 = new GetSetGrowthList<>();
		List<ItemVO> itemToBag2 = new GetSetGrowthList<>();
		for (ItemVO item : itemVOs) {
			ConfItemData cd = ConfItemData.get(item.sn);
			//伙伴碎片特殊处理
			if(cd.bType == ItemTypeKey.伙伴碎片){
				continue;
			}
			
			if (ItemPack.selectBagType(item.sn) == ItemPackTypeKey.Bag1.packType) {
				itemToBag1.add(item);
			} else {
				itemToBag2.add(item);
			}
		}
		// 需要加入背包1的
		ReasonResult bag1Result = __canAdd(humanObj, itemToBag1,
				ItemPackTypeKey.Bag1.packType);
		if (!bag1Result.success) {
			return bag1Result;
		}
		// 需要加入背包2的
		ReasonResult bag2Result = __canAdd(humanObj, itemToBag2,
				ItemPackTypeKey.Bag2.packType);
		return bag2Result;
	}

	/**
	 * 判断是否可添加到某个子背包。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param itemVOs
	 *            入包物品VO
	 * @param bagType
	 *            背包类型
	 * @return 结果与原因。
	 * @see ItemVO
	 */
	private ReasonResult __canAdd(HumanObject humanObj, List<ItemVO> itemVOs,
			int bagType) {
		// 当前剩余格子的数量
		int numRest = getNumRest(humanObj, bagType);
		// 新增物品需要使用的格子数
		int numNeed = 0;

		// 遍历要添加的内容 获取添加此种物品需要的格子数
		for (ItemVO i : itemVOs) {

			// 物品配置
			ConfItemData conf = ConfItemData.get(i.sn);

			// 数量错误
			if (i.num <= 0) {
				return new ReasonResult(false,
						I18n.get("common.tip.illegalNum") + ", num = " + i.num);
			}

			// 物品配置错误
			if (conf == null) {
				return new ReasonResult(false,
						I18n.get("common.tip.configWrong") + ", sn = " + i.sn);
			}

			// 非叠加物品 有多少个就需要多少个格子
			int need = 0;
			if (conf.foldCount <= 1) {
				need = i.num;

				// 可叠加物品 部分新增需求可以与之前的物品叠加
			} else {
				// 查看已有同类(SN+绑定)物品，可以用于叠加的数量
				int rest = 0;

				// 玩家背包
				ItemPack pack = __getPack(humanObj);

				for (Item item : pack.findBySnBind(i.sn, i.bind)) {
					rest += conf.foldCount - item.getNum();
				}

				// 如果要添加的数量大于可叠加的数量 那么就还需要占用新格子
				if (i.num > rest) {
					need = (i.num - rest) / conf.foldCount + 1;
				}
			}

			// 累加需要的格子数
			numNeed += need;

			// 不用都叠加完毕 只要过程中已经超过可用格子数了 就可以直接返回了
			if (numNeed > numRest) {
				return new ReasonResult(false,
						I18n.get("item.error.numNotEnough"));
			}
		}

		return new ReasonResult(true);
	}

	/**
	 * 获取某背包总容量。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param bagType
	 *            背包类型
	 * @return 容器容量
	 */
	public int getNumMax(HumanObject humanObj, int bagType) {
		if (bagType == ItemPackTypeKey.Bag1.packType)
			return humanObj.getHuman().getBag1Cap();
		else
			return humanObj.getHuman().getBag2Cap();
	}

	/**
	 * 获取某背包剩余可用格的数目。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param bagType
	 *            背包类型
	 * @return 剩余可用格数目。
	 */
	public int getNumRest(HumanObject humanObj, int bagType) {
		// 取对应容器
		ItemPack pack = __getPack(humanObj);
		Bag<Item> bag = pack.__getProperBag(bagType);
		int sz = bag.size();
		// 剩余格子 = 背包格子总数 - 使用的格子数量
		int used = sz - pack.findNullsInBag(bagType).size();
		int totol = getNumMax(humanObj, bagType);
		return totol - used;
	}

	/**
	 * 能否删除背包物品，对于业务上经常出现的“仅使用非绑物品”接口进行支持。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param onlyNoBind
	 *            是否只删除不绑定物品
	 * @param sn
	 *            物品SN
	 * @param num
	 *            数量
	 * @return 结果与原因。
	 */
	public ReasonResult canRemove(HumanObject humanObj, boolean onlyNoBind,
			int sn, int num) {
		ItemReduceTypeKey type = onlyNoBind ? ItemReduceTypeKey.ONLY_BIND_NO
				: ItemReduceTypeKey.FIRST_BIND_YES;

		return canRemove(humanObj, sn, num, type);
	}

	/**
	 * 能否删除背包物品，优先使用绑定物品。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param sn
	 *            物品SN
	 * @param num
	 *            数量
	 * @return 结果与原因。
	 */
	public ReasonResult canRemove(HumanObject humanObj, int sn, int num) {
		return canRemove(humanObj, sn, num, ItemReduceTypeKey.FIRST_BIND_YES);
	}

	/**
	 * 能否删除背包多个物品，优先使用绑定物品。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param itemVo
	 *            物品SN与数量集合
	 * @return 结果与原因。
	 */
	public ReasonResult canRemove(HumanObject humanObj,
			Collection<ItemVO> itemVo) {
		for (ItemVO itemVO2 : itemVo) {
			ReasonResult ans = canRemove(humanObj, itemVO2.sn, itemVO2.num);
			if (!ans.success) {
				return ans;
			}
		}
		return new ReasonResult(true);
	}

	/**
	 * 能否删除背包物品。
	 * 
	 * @param humanObject
	 *            玩家{@code HumanObject}对象
	 * @param sn
	 *            物品SN
	 * @param num
	 *            数量
	 * @param typeKey
	 *            删除规则 仅绑定 仅非绑 优先绑定
	 * @return 结果与原因。
	 * @see ItemReduceTypeKey
	 */
	public ReasonResult canRemove(HumanObject humanObj, int sn, int num,
			ItemReduceTypeKey typeKey) {

		ItemPack pack = __getPack(humanObj);

		// 1 检查数量是否足够
		// 1.1 获取符合删除规则的物品数量
		int count = 0;
		switch (typeKey) {
		// 只移除绑定
		case ONLY_BIND_YES: {
			List<Item> items = pack.findBySnBind(sn, 1);
			for (Item item : items)
				count += item.getNum();
		}
			break;

		// 只移除非绑
		case ONLY_BIND_NO: {
			List<Item> items = pack.findBySnBind(sn, 0);
			for (Item item : items)
				count += item.getNum();
		}
			break;

		// 优先移除绑定 取出全部
		default: {
			List<Item> items = pack.findBySnBind(sn, 0);
			items.addAll(pack.findBySnBind(sn, 1));
			for (Item item : items)
				count += item.getNum();
		}
		}
		// 1.2 检查数量
		if (count < num) {
			return new ReasonResult(false, I18n.get("item.error.itemNotEnough"));
		}

		return new ReasonResult(true);
	}

	/**
	 * 针对单个物品的数量，判断能否删除。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param item
	 *            物品
	 * @param num
	 *            数量
	 * @return 结果与原因。
	 */
	public ReasonResult __canRemove(HumanObject humanObj, Item item, int num) {
		// 检查数量
		if (item.getNum() < num) {
			return new ReasonResult(false, I18n.get("item.error.itemNotEnough"));
		}

		return new ReasonResult(true);
	}

	/**
	 * 删除背包物品，对于业务上经常出现的“仅使用非绑物品”接口进行支持。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param onlyNoBind
	 *            是否只删除不绑定物品
	 * @param sn
	 *            物品SN
	 * @param num
	 *            数量
	 * @param params
	 *            额外参数，没有可不写
	 * @return 背包物品改变。
	 */
	public ItemChange remove(HumanObject humanObj, boolean onlyNoBind, int sn,
			int num, Object... params) {
		ItemReduceTypeKey type = onlyNoBind ? ItemReduceTypeKey.ONLY_BIND_NO
				: ItemReduceTypeKey.FIRST_BIND_YES;

		return remove(humanObj, sn, num, type, params);
	}

	/**
	 * 删除物品
	 * 
	 * @param humanObj
	 * @param sn
	 * @param num
	 * @param params
	 * @return
	 */
	public ItemChange remove(HumanObject humanObj, int sn, int num,
			Object... params) {
		return remove(humanObj, sn, num, ItemReduceTypeKey.FIRST_BIND_YES,
				params);
	}

	/**
	 * 删除多个物品
	 * 
	 * @param humanObj
	 * @param itemVO
	 * @param params
	 * @return
	 */
	public ItemChange remove(HumanObject humanObj, Collection<ItemVO> itemVO,
			Object... params) {
		ItemChange ans = new ItemChange();
		for (ItemVO vo : itemVO) {
			ans.merge(remove(humanObj, vo.sn, vo.num,
					ItemReduceTypeKey.FIRST_BIND_YES, params));
		}
		return ans;
	}

	/**
	 * 删除物品
	 * 
	 * @param humanObj
	 * @param sn
	 * @param num
	 * @param typeKey
	 * @param params
	 * @return
	 */
	public ItemChange remove(HumanObject humanObj, int sn, int num,
			ItemReduceTypeKey typeKey, Object... params) {
		// 验证
		ReasonResult result = canRemove(humanObj, sn, num, typeKey);
		if (!result.success) {
			throw new HumanException(humanObj.id, result.reason);
		}

		// 解析参数
		Param param = new Param(params);
		// 默认值-发送物品变动消息
		boolean sendMsg = Utils.getParamValue(param, "sendMsg", true);

		// 背包
		ItemPack pack = __getPack(humanObj);

		// 物品修改信息
		ItemChange itemChangeVO = new ItemChange();

		// 取出可以删除的物品
		List<Item> items;
		switch (typeKey) {
		// 只移除绑定
		case ONLY_BIND_YES: {
			items = pack.findBySnBind(sn, 1);
		}
			break;

		// 只移除非绑
		case ONLY_BIND_NO: {
			items = pack.findBySnBind(sn, 0);
		}
			break;

		// 优先移除绑定 取出全部
		default: {
			items = pack.findBySnBind(sn, 1);
			items.addAll(pack.findBySnBind(sn, 0));
		}
		}

		// 进行排序
		// 1 优先删除绑定的 2 优先删除叠加数量少的
		Collections.sort(items, (Item a, Item b) -> {
			if (a.getBind() == b.getBind()) {
				return a.getNum() - b.getNum();
			} else {
				return b.getBind() - a.getBind();
			}
		});

		// 进行删除
		int numNeed = num; // 需要删除的数量
		for (Item i : items) {
			// 当前物品可删除数量
			int n = Math.min(i.getNum(), numNeed);
			// 剩余需要删除的数量
			numNeed = numNeed - n;

			// 减少数量
			i.setNum(i.getNum() - n);

			// 物品需要被删除
			if (i.getNum() == 0) {
				i.remove();

				// 记录被删除的物品
				itemChangeVO.markDeleting(i);

				// 物品仍然有剩余 那么说明已经不用在继续删除了
			} else {
				// 记录被修改的物品
				itemChangeVO.markNumChanged(i);
				break;
			}
		}

		// 发送物品变动信息
		if (sendMsg) {
			sendChangeMsg(humanObj.getHumanObj(), itemChangeVO);
		}

		// 发送物品变动事件
		itemChangeVO.fireChangeEvent(humanObj.getHumanObj());

		return itemChangeVO;
	}

	/**
	 * 创建并复制一个目标对象的副本。
	 * 
	 * @param srcItem
	 *            源物品对象
	 * @return 新物品对象。
	 */
	public Item clone(Item itemSrc) {
		// 创建对象
		Item itemDes = new Item();
		itemDes.setSn(itemSrc.getSn());
		// 复制属性
		for (EntityItem item : EntityItem.values()) {
			Object value = Utils.fieldRead(itemSrc, item.name());

			Utils.fieldWrite(itemDes, item.name(), value);
		}

		return itemDes;
	}

	/**
	 * 整理背包并发消息给玩家。
	 * <p>
	 * 整理背包步骤：
	 * <ul>
	 * <li>压缩
	 * <ul>
	 * <li>合并可叠加的相同物品</li>
	 * <li>删除叠加后数量为0的物品</li>
	 * </ul>
	 * </li>
	 * <li>排序</li>
	 * </ul>
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param bagType
	 *            背包类型。
	 */
	public void arrange(HumanObject humanObj, int bagType) {
		ItemChange itemChange = new ItemChange();
		ItemPack pack = __getPack(humanObj);

		Bag<Item> whichBag = pack.__getProperBag(bagType);

		// 算法围绕这个__itemGroups操作
		Map<String, List<Item>> __itemGroups = new HashMap<>();

		// 原物品列表按SN和bind分类到__itemGroups
		for (Item itemA : whichBag.getList()) {
			if (itemA != null) {
				String key = itemA.getSn() + "_" + itemA.getBind();
				if (!__itemGroups.containsKey(key)) {
					__itemGroups.put(key, new GetSetGrowthList<>());
				}
				List<Item> group = __itemGroups.get(key);
				group.add(itemA);
			}
		}
		// __itemGroups中逐个value压缩物品数量
		for (Entry<String, List<Item>> groupWithKey : __itemGroups.entrySet()) {

			// 通过代表性物品判断是否可叠加
			List<Item> group = groupWithKey.getValue();
			int sn = group.get(0).getSn();
			ConfItemData conf = ConfItemData.get(sn);
			int cellLimit = conf.foldCount;

			// 当可叠加时压缩数量
			if (cellLimit > 1) {
				for (int i = 0; i < group.size(); i++) {
					Item __destItem = group.get(i);
					for (int j = i + 1; j < group.size(); j++) {
						Item __srcItem = group.get(j);

						// 物品数量要往前堆叠（可减少排序次数）
						if (__destItem.getNum() < cellLimit) {

							// 计算需要往前转移多少个
							int trans = Math.min(
									cellLimit - __destItem.getNum(),
									__srcItem.getNum());
							if (trans > 0) {
								__destItem.setNum(__destItem.getNum() + trans);
								__srcItem.setNum(__srcItem.getNum() - trans);

								// 记录改变
								itemChange.markNumChanged(__destItem);

								if (__srcItem.getNum() > 0) {
									itemChange.markNumChanged(__srcItem);
								} else {
									itemChange.markDeleting(__srcItem);
									// 如果有格子数量为0了，还要数据库删除这格东西
									__srcItem.remove();
								}
							}
						}
					}
				}
			}
		}
		// 重新生成背包集合__newDatas
		Bag<Item> __newDatas = new Bag<>();

		ArrayList<Entry<String, List<Item>>> tmpEntrys = new ArrayList<>(
				__itemGroups.entrySet());
		// 先按物品排序
		tmpEntrys.sort(this::__compareGroup);
		// 再分别取每个key下物品列表，拼接新背包
		tmpEntrys.forEach((groupWithKey -> {
			List<Item> _itemGroup = groupWithKey.getValue();
			_itemGroup.forEach((item) -> {
				if (item.getNum() > 0) {
					__newDatas.add(item);
					// 审查位置的改变
					item.setPosition(__newDatas.size() - 1);
				}
			});
		}));

		// 新集合替换旧的
		pack.__setProperBag(bagType, __newDatas);
		// 想办法告知客户端怎么交换能达到这个效果，目前还没想到更有效的算法，等高人指点
		__getSwaps(whichBag, itemChange);

		itemChange.fireChangeEvent(humanObj);

		// 发布整理背包事件
		Event.fire(EventKey.ITEM_BAG_ARRANGE, "humanObject", humanObj);
		// 告知客户端
		sendChangeMsg(humanObj, itemChange);
	}

	/**
	 * 计算旧背包怎么样才能变换成新背包。
	 * 
	 * @param bag
	 *            旧背包
	 * @param itemChange
	 *            记录怎么交换
	 */
	private void __getSwaps(Bag<Item> bag, ItemChange itemChange) {
		List<Object[]> elems = new GetSetGrowthList<>();
		for (int i = 0, sz = bag.size(); i < sz; i++) {
			Item item = bag.get(i);
			if (item != null && item.getNum() > 0) {
				elems.add(new Object[] { i, item });
			} else {
				elems.add(null);
			}
		}
		for (int i = 0, sz = elems.size(); i < sz; i++) {
			Object[] crt = elems.get(i);
			while (crt != null) {
				int crtPos = (int) crt[0];
				Item crtItem = (Item) crt[1];
				if (crtPos != crtItem.getPosition()) {

					itemChange.markPosSwapped(crtItem, crtPos);

					Object[] next = elems.get(crtItem.getPosition());
					Collections.swap(elems, crtPos, crtItem.getPosition());
					crt[0] = crtItem.getPosition();
					if (next != null) {
						next[0] = crtPos;
					}
					crt = next;
				} else {
					break;
				}
			}
		}
	}

	/**
	 * 比较两个物品组。
	 * 
	 * @param _a
	 *            前一个物品组
	 * @param _b
	 *            后一个物品组
	 * @return 正数表示前一个大，负数表示后一个大。
	 */
	private int __compareGroup(Entry<String, List<Item>> _a,
			Entry<String, List<Item>> _b) {
		Item itemA = _a.getValue().get(0);
		Item itemB = _b.getValue().get(0);
		int snA = itemA.getSn();
		int snB = itemB.getSn();

		if (snA == snB) {

			// 同物品非绑定的在前
			return itemA.getBind() == ItemBagManager.BIND_YES ? -1 : 1;
		} else {

			// 不同物品首先比较排序字段
			ConfItemData confA = ConfItemData.get(snA);
			ConfItemData confB = ConfItemData.get(snB);
			if (confA.sequence != confB.sequence) {

				// 小的在前
				return confA.sequence - confB.sequence;
			} else {

				// 排序字段相同，sn大的在前
				return snB - snA;
			}
		}
	}

	/**
	 * 是否是直接使用的物品。
	 * 
	 * @param sn
	 *            物品SN
	 * @return 是否是直接使用的物品。
	 */
	private boolean __isAutoUse(int sn) {
		ConfItemData conf = ConfItemData.get(sn);
		// TODO 这个判断以后可能还需完善
		if (conf != null
				&& conf.bType == ItemTypeKey.道具 && conf.sType == ItemTypeKey.IT_道具_立即增加虚拟品)
			return true;
		return false;
	}

	/**
	 * 是否是可卖的。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param item
	 *            物品
	 * @return 结果与原因。
	 */
	private ReasonResult __canSell(HumanObject humanObj, Item item) {
		// 物品不存在
		if (item == null)
			return new ReasonResult(false, I18n.get("item.common.noItem"));

		ConfItemData confItem = ConfItemData.get(item.getSn());
		// 配置错误
		if (confItem == null) {
			return new ReasonResult(false, I18n.get("common.tip.configWrong"));
		}
		// 配置中不能卖
		if (confItem.price <= 0) {
			Log.item.warn("物品出售失败，配置为不能出售，SN:{}", item.getSn());
			return new ReasonResult(false, I18n.get("item.common.cantSell"));
		}
		return new ReasonResult(true);
	}

	/**
	 * 使用背包内某种物品若干个，优先使用{@code example}。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param example
	 *            被使用的物品样本
	 * @param num
	 *            数量
	 * @param target
	 *            作用目标
	 * @return 结果与原因。
	 */
	public ReasonResult itemUseByExample(HumanObject humanObj, Item example,
			int num, Object target) {
		// 找出所有此类物品
		int sn = example.getSn();
		ItemPack pack = __getPack(humanObj);
		int countBySn = pack.countBySn(sn);
		if (num > 0 && countBySn < num) {
			return new ReasonResult(false, I18n.get("item.common.notEnough"));
		}
		// 找出此sn物品
		List<Item> bySn = pack.findBySnBind(sn, BIND_NO);
		// 把样品物品移到开头，优先使用样品
		for (int j = 0, sz = bySn.size(); j < sz; j++) {
			Item item = bySn.get(j);
			if (item == example) {
				Collections.swap(bySn, 0, j);
			}
		}
		// 逐个物品使用，直到用够数量
		int leftNum = num;
		for (Item snItem : bySn) {
			int canUse = snItem.getNum();
			int shouldUse = Math.min(canUse, leftNum);
			if (shouldUse > 0) {

				itemUseByPos(humanObj, snItem.getPackType(),
						snItem.getPosition(), shouldUse, target);

				leftNum -= shouldUse;
			}
			if (leftNum == 0) {
				break;
			}
		}
		return new ReasonResult(true);
	}

	/**
	 * 使用背包内某种物品若干个，不指定位置。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param sn
	 *            背包类型
	 * @param num
	 *            数量
	 * @param target
	 *            作用目标
	 * @return 结果与原因。
	 */
	public ReasonResult itemUseBySn(HumanObject humanObj, int sn, int num,
			Object target) {
		// 找出所有此类物品
		ItemPack pack = __getPack(humanObj);
		int countBySn = pack.countBySn(sn);
		if (num > 0 && countBySn < num) {
			return new ReasonResult(false, I18n.get("item.common.notEnough"));
		}
		// 找出此sn物品，逐个物品使用，直到用够数量
		int leftNum = num;
		List<Item> bySn = pack.findBySnBind(sn, BIND_NO);
		for (Item snItem : bySn) {
			int canUse = snItem.getNum();
			int shouldUse = Math.min(canUse, leftNum);
			if (shouldUse > 0) {

				itemUseByPos(humanObj, snItem.getPackType(),
						snItem.getPosition(), shouldUse, target);

				leftNum -= shouldUse;
			}
			if (leftNum == 0) {
				break;
			}
		}
		return new ReasonResult(true);
	}

	/**
	 * 使用某背包某位置处的物品若干个。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象。
	 * @param bagType
	 *            背包类型
	 * @param position
	 *            背包位置下标
	 * @param num
	 *            数量
	 * @param target
	 *            作用目标
	 * @return 结果与原因。
	 */
	public ReasonResult itemUseByPos(HumanObject humanObj, int bagType,
			int position, int num, Object target) {
		ItemPack bag = __getPack(humanObj);

		// 查找背包物品
		Item item = bag.get(position, bagType);
		if (item == null) {
			return new ReasonResult(false, I18n.get("item.common.noItem"));
		}

		// 查找配置
		ConfItemData confItem = ConfItemData.get(item.getSn());
		if (confItem == null) {
			return new ReasonResult(false, I18n.get("common.tip.configWrong"));
		}

		// 判断次数限制
		ReasonResult canUseItem = __canUseItem(humanObj, confItem, num);
		if (!canUseItem.success) {
			return canUseItem;
		}
		// 数量不足
		if (num < 0 || item.getNum() < num) {
			return new ReasonResult(false, I18n.get("item.common.notEnough"));
		}

		int numBef = item.getNum();
		int type = confItem.sType;
		// 发布使用物品事件
		Event.fireEx(EventKey.ITEM_USE, type, "humanObject", humanObj,
				"itemBag", item, "confItem", confItem, "num", num, "target", target);

		if (item.getNum() - numBef != 0) {

			// 设置物品CD （目前没有CD）

			// 设置物品使用次数
			__addUseTime(humanObj, confItem, numBef);

			// 发布物品使用成功事件
			Event.fire(EventKey.ITEM_BE_USED_SUCCESS, "humanObj", humanObj,
					"sn", item.getSn());

			// 记录物品改变
			ItemChange itemChangeVO = new ItemChange();
			// 物品需要被删除
			if (item.getNum() == 0) {
				item.remove();

				// 记录被删除的物品
				itemChangeVO.markDeleting(item);

				// 物品仍然有剩余 那么说明已经不用在继续删除了
			} else {
				// 记录被修改的物品
				itemChangeVO.markNumChanged(item);
			}

			itemChangeVO.fireChangeEvent(humanObj);

			// 告知客户端
			sendChangeMsg(humanObj, itemChangeVO);
		}

		return new ReasonResult(true);
	}

	/**
	 * 判断物品可否使用。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param confItem
	 *            物品配置
	 * @param num
	 *            使用个数
	 * @return 结果与原因。
	 */
	private ReasonResult __canUseItem(HumanObject humanObj,
			ConfItemData confItem, int num) {
		if (0 == confItem.useable) {
			return new ReasonResult(false, "此物不能使用");
		}
		if (confItem.dayLimit > 0) {
			String propUseLimitCountsStr = humanObj.getHuman()
					.getPropUseLimitCounts();
			JSONObject countsJson = Utils.toJSONObject(propUseLimitCountsStr);
			int c = countsJson.getIntValue(String.valueOf(confItem.sn));
			if (c + num > confItem.dayLimit) {
				return new ReasonResult(false, "此种物品今天不能再使用");
			}
		}
		return new ReasonResult(true);
	}

	/**
	 * 增加物品使用次数。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param confItem
	 *            物品配置
	 * @param num
	 *            使用次数
	 */
	private void __addUseTime(HumanObject humanObj, ConfItemData confItem,
			int num) {
		if (confItem.dayLimit > 0) {
			Human human = humanObj.getHuman();
			String propUseLimitCountsStr = human.getPropUseLimitCounts();
			JSONObject countsJson = Utils.toJSONObject(propUseLimitCountsStr);
			String key = String.valueOf(confItem.sn);
			int c = countsJson.getIntValue(key);
			c += num;
			countsJson.put(key, c);
			human.setPropUseLimitCounts(countsJson.toJSONString());
		}
	}

	/**
	 * 出售某背包某些位置的物品。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param bagType
	 *            背包类型
	 * @param positions
	 *            位置下标
	 * @return 结果与原因。
	 */
	public ReasonResult sell(HumanObject humanObj, int bagType,
			List<Integer> positions) {
		// 玩家背包
		ItemPack bag = __getPack(humanObj);
		// 卖多少钱
		int price = 0;
		// 找出物品
		List<Item> itemList = new GetSetGrowthList<>();
		for (Integer pos : positions) {
			Item item = bag.get(pos, bagType);
			if (item == null) {
				return new ReasonResult(false, I18n.get("item.common.noItem"));
			}
			// 能否出售
			ReasonResult result = __canSell(humanObj, item);
			if (!result.success) {
				Log.item.error("出售物品时错误,原因：{},物品Id:{},角色Id:{}", result.reason,
						item.getSn(), humanObj.id);
				return result;
			}
			price += ConfItemData.get(item.getSn()).price * item.getNum();

			itemList.add(item);
		}
		// 扔掉物品
		ItemChange itemChangeVO = new ItemChange();
		for (Item item : itemList) {
			bag.remove(item.getPosition(), item.getPackType());
			item.remove();
			// 记录被删除的物品
			itemChangeVO.markDeleting(item);
		}
		itemChangeVO.fireChangeEvent(humanObj);

		// 发布出售物品事件
		Event.fire(EventKey.ITEM_BAG_SELL, "humanObject", humanObj, "itemList",
				itemList);

		// 加钱
		HumanManager.inst().produceMoneyAdd(humanObj, ProduceMoneyKey.coin,
				price, MoneyAddLogKey.出售物品);
		// 告知客户端
		sendChangeMsg(humanObj, itemChangeVO);
		return new ReasonResult(true);
	}

	/**
	 * 获取所有子背包物品信息。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象。
	 * @return 对个子背包的物品信息。
	 */
	public List<List<DItem>> getBagInfo(HumanObject humanObj) {
		List<DItem> bag1Result = new GetSetGrowthList<>();
		ItemPack bag = __getPack(humanObj);
		// 背包1的东西
		for (Item item : bag.bag1.getList()) {
			if (item != null) {
				bag1Result.add(item.createMsg().build());
			}
		}
		// 背包2的东西
		List<DItem> bag2Result = new GetSetGrowthList<>();
		for (Item item : bag.bag2.getList()) {
			if (item != null) {
				bag2Result.add(item.createMsg().build());
			}
		}
		// 合并到一个List返回
		List<List<DItem>> result = new GetSetGrowthList<>();
		result.add(bag1Result);
		result.add(bag2Result);
		return result;
	}

	/**
	 * 建号阶段，给玩家一些初始物品。
	 * 
	 * @param param
	 */
	@Listener(EventKey.HUMAN_CREATE)
	public void giveInitItems(Param param) {
		Human human = param.get("human");
		// 创建物品
		List<Item> items = create(human.getId(), 31000040, 3, 0); // TODO
																	// 这是3个测试装备
		List<Item> moneys = create(human.getId(), 350009, 165, 0); // 给几个测试钱袋
		items.addAll(moneys);
		items.addAll(create(human.getId(), 350015, 108, 0)); // 另一种测试钱袋
		items.addAll(create(human.getId(), 350001, 290, 0)); // 测试冲星石
		items.addAll(create(human.getId(), 350002, 99, 0)); // 测试强化石
		items.addAll(create(human.getId(), 31010001, 5, 0)); // 测试宝石
		items.addAll(create(human.getId(), 350047, 32, 0)); // 测试扩包道具
		items.addAll(create(human.getId(), 360001, 12, 0)); // 福利大礼包

		Map<Integer, Integer> nextPosByBagType = new HashMap<>();
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			int bagType = ItemPack.selectBagType(item.getSn());
			int position = nextPosByBagType.getOrDefault(bagType, 0);
			//
			item.setPosition(position);
			item.setPackType(bagType);

			nextPosByBagType.put(bagType, position + 1);
			// 持久化
			item.persist();
		}
		// 存入库后就行了，不需要做其他的
	}

	/**
	 * 扩充某子背包下标到{@code toIndex}。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param bagType
	 *            背包类型
	 * @param toIndex
	 *            扩到的下标
	 * @return 结果与原因。
	 */
	public ReasonResult expand(HumanObject humanObj, int bagType, int toIndex) {
		ReasonResult result;
		Human human = humanObj.getHuman();
		// 取已扩数量
		int oldCap = 0;
		if (bagType == ItemPackTypeKey.Bag1.packType) {
			oldCap = human.getBag1Cap();
		}
		if (bagType == ItemPackTypeKey.Bag2.packType) {
			oldCap = human.getBag2Cap();
		}
		// 判断下标是否合理
		int newCap = toIndex + 1;
		int expand = newCap - oldCap;
		if (newCap > ConfGlobalUtils.getValue(ConfGlobalKey.背包最大容量)
				|| expand <= 0) {
			return new ReasonResult(false, "不能扩容至" + newCap);
		}
		// 优先使用扩包道具
		int itemSn = ConfGlobalUtils.getValue(ConfGlobalKey.扩包道具SN);
		ItemPack pack = __getPack(humanObj);
		int itemCanExpand = Math.min(expand, pack.countBySn(itemSn));

		// 无道具花钱扩包
		int moneyExpand = expand - itemCanExpand;
		int moneyType = ConfGlobalUtils.getValue(ConfGlobalKey.扩包花钱类型);
		int price = ConfGlobalUtils.getValue(ConfGlobalKey.扩包一格需要的钱数)
				* moneyExpand;
		// 判断钱
		if (price > 0) {
			result = HumanManager.inst().canProduceReduce(humanObj, moneyType,
				price);
			if (!result.success) {
				return result;
			}
		}
		// 使用物品
		if (itemCanExpand > 0) {
			itemUseBySn(humanObj, itemSn, itemCanExpand, bagType);
		}
		// 减钱
		if (price > 0) {
			HumanManager.inst().produceMoneyReduce(humanObj, moneyType, price,
					MoneyReduceLogKey.扩包);
		}
		// 重设背包上限
		if (bagType == ItemPackTypeKey.Bag1.packType) {
			human.setBag1Cap(newCap);
		}
		if (bagType == ItemPackTypeKey.Bag2.packType) {
			human.setBag2Cap(newCap);
		}

		// 发布扩包事件
		Event.fire(EventKey.ITEM_BAG_EXPAND, "humanObject", humanObj,
				"toIndex", toIndex);

		SCBagExpand.Builder msg = SCBagExpand.newBuilder();
		msg.setCode(0);
		msg.setToIndex(toIndex);
		humanObj.sendMsg(msg);

		return new ReasonResult(true);
	}

	// /**
	// * 获得物品颜色 临时方法
	// *
	// * @param item
	// * @return
	// */
	// public String colorGetTmpTool(ConfItemData item) {
	// String color = "";
	// switch (item.quality) {
	// case 1:
	// color = "#FBFFFFFF";
	// break;
	// case 2:
	// color = "#FB00FF00";
	// break;
	// case 3:
	// color = "#FB0000FF";
	// break;
	// case 4:
	// color = "#FB800080";
	// break;
	// case 5:
	// color = "#FBFFA500";
	// break;
	// default:
	// throw new SysException("未找到对应的颜色");
	// }
	//
	// return color + item.name + "#";
	// }

	/**
	 * 初始化限制次数
	 * 
	 * @param human
	 */
	@Listener(value = EventKey.HUMAN_RESET_ZERO)
	public void resetDayLimit(Param param) {
		HumanObject humanObj = param.get("humanObj");
		Human human = humanObj.getHuman();

		human.setPropUseLimitCounts("");
		Log.item.info("初始化道具使用限制次数");
	}
}
