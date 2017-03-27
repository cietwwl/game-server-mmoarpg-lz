package org.gof.demo.worldsrv.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.core.support.SysException;
import org.gof.demo.worldsrv.entity.Item;
import org.gof.demo.worldsrv.support.GetSetGrowthList;
import org.gof.demo.worldsrv.support.enumKey.ItemPackTypeKey;

/**
 * 物品容器基类。
 * <p>
 * 游戏中物品对象所在的容器，如背包、装备位、宝石槽等。
 * 
 * @author zhangbo
 *
 */
public class ItemPack implements ISerilizable {
	/** 主背包中的物品 */
	public Bag<Item> bag1;
	/** 次背包中的物品 */
	protected Bag<Item> bag2;
	/** 装备位、宝石槽中的物品，按Id索引 */
	protected Map<Long, Item> onParts;

	/**
	 * 框架调用构造器。
	 */
	public ItemPack() {
		init();
	}

	/**
	 * 初始化存储容器。
	 */
	public void init() {
		this.bag1 = new Bag<>();
		this.bag2 = new Bag<>();
		this.onParts = new HashMap<>();
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(bag1);
		out.write(bag2);
		out.write(onParts);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		bag1 = in.read();
		bag2 = in.read();
		onParts = in.read();
	}

	/**
	 * 构造有物品的容器，供旧玩家加载数据库数据时使用。
	 * 
	 * @param items
	 *            数据库中存的玩家物品
	 */
	public ItemPack(Collection<Item> items) {
		init();

		for (Item item : items) {
			// 取物品容器类型
			int packType = item.getPackType();
			// 如果是身上的装备，放入Map，否则按类型分背包存入对应List
			if (packType == ItemPackTypeKey.Parts.packType) {
				this.onParts.put(item.getId(), item);
			} else {
				// 得到存入List的位置
				int pos = item.getPosition();
				// 取对应的List放入
				Bag<Item> bag = __getProperBag(packType);
				bag.set(pos, item);
			}
		}
	}

	/**
	 * 通过物品SN取可存入的背包类型。
	 * 
	 * @param sn
	 *            物品SN
	 * @return 背包类型。
	 */
	public static int selectBagType(int sn) { // TODO
		return ItemPackTypeKey.Bag1.packType;
	}

	/**
	 * 根据背包类型取对应的存储{@code List}。
	 * 
	 * @param bagType
	 *            背包类型
	 * @return 背包对应的{@code List}。
	 * @throws SysException
	 *             如果{@code bagType}不是正确的背包子类型。
	 */
	Bag<Item> __getProperBag(int bagType) {
		Bag<Item> bag;
		ItemPackTypeKey packKey = ItemPackTypeKey.getType(bagType);
		switch (packKey) {
		case Bag1:
			bag = bag1;
			break;
		case Bag2:
			bag = bag2;
			break;
		default:
			throw new SysException("物品背包类型不对，请检查参数 " + bagType);
		}
		return bag;
	}

	/**
	 * 替换某个子背包的存储列表。
	 * 
	 * @param bagType
	 *            背包类型
	 * @param newBag
	 *            新背包存储列表
	 * @throws SysException
	 *             如果{@code bagType}不是正确的背包子类型。
	 */
	void __setProperBag(int bagType, Bag<Item> newBag) {
		ItemPackTypeKey packKey = ItemPackTypeKey.getType(bagType);
		switch (packKey) {
		case Bag1:
			bag1 = newBag;
			break;
		case Bag2:
			bag2 = newBag;
			break;
		default:
			throw new SysException("物品背包类型不对，请检查参数 " + bagType);
		}
	}

	/**
	 * 添加物品到背包。
	 * <p>
	 * 注：存入背包时依赖物品SN和位置{@code position}，需要提前设置好。
	 * 
	 * @param item
	 *            入包物品
	 */
	public void addToBag(Item item) {
		int packType = selectBagType(item.getSn());
		Bag<Item> bag = __getProperBag(packType);
		int position = item.getPosition();
		bag.set(position, item);
	}

	/**
	 * 删除在{@code position}位置处的背包物品，此位置被置为{@code null}，不影响其他物品的位置。
	 * 
	 * @param position
	 *            位置下标
	 * @param bagType
	 *            背包类型
	 * @return 对应背包{@code position}下标处的物品。
	 */
	public Item remove(int position, int bagType) {
		Bag<Item> bag = __getProperBag(bagType);
		return bag.set(position, null);
	}

	/**
	 * 通过位置获取背包物品。
	 * 
	 * @param position
	 *            位置下标
	 * @param bagType
	 * @return 对应背包{@code position}下标处的物品。
	 */
	public Item get(int position, int bagType) {
		Bag<Item> bag = __getProperBag(bagType);
		Item item = bag.get(position);
		return item;
	}

	/**
	 * 获取对应背包中最小空闲位置。
	 * 
	 * @param bagType
	 *            背包类型
	 * 
	 * @return 对应背包中最小空闲位置下标。
	 */
	public int getFirstFreePosition(int bagType) {
		Bag<Item> bag = __getProperBag(bagType);
		int i = 0;
		for (int sz = bag.size(); i < sz; i++) {
			Item item = bag.get(i);
			if (item == null) {
				break;
			}
		}
		return i;
	}

	/**
	 * 找出物品ID为{@code sn}，绑定状态为{@code bind}的所有背包物品。
	 * 
	 * @param sn
	 *            物品ID
	 * @param bind
	 *            {@code 0}不绑定，{@code 1}是绑定
	 * @return 所有符合条件的物品。
	 */
	public List<Item> findBySnBind(int sn, int bind) {
		List<Item> result = new ArrayList<>();
		for (Item item : bag1.getList()) {
			if (item != null && item.getSn() == sn && item.getBind() == bind) {
				result.add(item);
			}
		}
		for (Item item : bag2.getList()) {
			if (item != null && item.getSn() == sn && item.getBind() == bind) {
				result.add(item);
			}
		}
		return result;
	}

	/**
	 * 计数物品ID为{@code sn}的所有背包物品（绑定和非绑定都算）。
	 * 
	 * @param sn
	 * @return
	 */
	public int countBySn(int sn) {
		int result = 0;
		for (Item item : bag1.getList()) {
			if (item != null && item.getSn() == sn) {
				result += item.getNum();
			}
		}
		for (Item item : bag2.getList()) {
			if (item != null && item.getSn() == sn) {
				result += item.getNum();
			}
		}
		return result;
	}

	/**
	 * 在背包中找出填{@code null}的空格。
	 * 
	 * @param packType
	 * @return 所有空格下标，返回值是从小到大的。
	 */
	public List<Integer> findNullsInBag(int packType) {
		List<Integer> result = new ArrayList<>();
		Bag<Item> bag = __getProperBag(packType);
		for (int i = 0; i < bag.size(); i++) {
			if (bag.get(i) == null) {
				result.add(i);
			}
		}
		return result;
	}

	/**
	 * 按物品ID取身上的物品。
	 * 
	 * @param id
	 *            物品Id
	 * @return 对应Id的物品。
	 */
	public Item getFromBody(long id) {
		return this.onParts.get(id);
	}

	/**
	 * 将某个物品放到装备位上。
	 * 
	 * @param item
	 *            物品
	 */
	public void toBody(Item item) {
		this.onParts.put(item.getId(), item);
	}

	/**
	 * 从身上取下某Id的物品。
	 * 
	 * @param id
	 *            物品Id
	 * @return 取下的物品。
	 */
	public Item removeBody(long id) {
		Item item = this.onParts.remove(id);
		return item;
	}
}
