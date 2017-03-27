package org.gof.demo.worldsrv.treasure;

import java.util.List;

import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfTreasureData;
import org.gof.demo.worldsrv.msg.Msg.DTreasure;
import org.gof.demo.worldsrv.produce.ProduceVo;

/**
 * {@code TreasureData}对应的逻辑功能封装。
 * 
 * @author zhangbo
 *
 */
public abstract class Treasure {

	protected ConfTreasureData trConf;

	protected HumanObject humanObj;

	/**
	 * 简单工厂方法。
	 * 
	 * @param humanObj
	 *            玩家{@code HumanObject}对象
	 * @param tType
	 *            宝藏类型
	 * @return 类型对应的宝藏实例。
	 */
	public static Treasure treasure(HumanObject humanObj, int tType) {
		if (tType == TreasureManager.TREASURE_TYPE_初级宝藏)
			return new PremierTreasure(humanObj);
		if (tType == TreasureManager.TREASURE_TYPE_中级宝藏)
			return new GreatTreasure(humanObj);
		if (tType == TreasureManager.TREASURE_TYPE_高级宝藏)
			return new UltimateTreasure(humanObj);
		return null;
	}

	protected Treasure(HumanObject humanObj, ConfTreasureData trData) {
		this.humanObj = humanObj;
		this.trConf = trData;
	}

	/**
	 * 进行一次单抽。
	 * 
	 * @return 得到的东西。
	 */
	abstract public List<ProduceVo> nextGatcha();

	/**
	 * 进行一次连抽。
	 * 
	 * @return 得到的东西。
	 */
	abstract public List<List<ProduceVo>> nextMultiGatcha(int n);

	public boolean isNextFree() {
		return false;
	}

	public boolean isNextHiden() {
		return false;
	}

	public boolean[] isNextNHiden(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("n:" + n);
		}
		boolean[] nextNHiden = new boolean[n];
		if (nextNHiden != null) {
			int[] toHidenShift = trConf.groupShift;
			int gatchaCount = getGatchaCount();
			cycleAlgorithm(nextNHiden, toHidenShift, gatchaCount);
		}
		return nextNHiden;
	}

	/**
	 * 根据周期和已进行的次数，算出之后若干次的关键点，填入{@code dest}。
	 * 
	 * @param dest
	 *            输出参数，关键点为{@code true}
	 * @param toHidenShift
	 *            周期（先从左到右依次取值，之后一直取最后一个值）
	 * @param gatchaCount
	 *            已进行次数
	 */
	private void cycleAlgorithm(boolean[] dest, final int[] toHidenShift,
			final int gatchaCount) {
		int section = 0;
		int nextPos = toHidenShift[section];
		int steps = toHidenShift[section];
		if (gatchaCount > 0) {
			int tempCount = gatchaCount;
			while (true) {
				if (gatchaCount > nextPos) {
					if (toHidenShift.length - section > 1) {
						tempCount -= steps;
						section++;
						steps = toHidenShift[section];
						nextPos += steps;
					} else {
						nextPos += tempCount / steps * steps;
					}
					continue;
				}
				if (gatchaCount == nextPos) {
					if (toHidenShift.length - section > 1) {
						section++;
						steps = toHidenShift[section];
					}
					nextPos += steps;
					break;
				}
				break;
			}
		}
		// assert hidenpos > gatchaCount : "算法错";
		for (int i = 0; i < dest.length; i++) {
			int nGatcha = i + gatchaCount + 1;
			if (nGatcha == nextPos) {
				dest[i] = true;
				if (toHidenShift.length - section > 1) {
					section++;
				}
				steps = toHidenShift[section];
				nextPos += steps;
			} else {
				dest[i] = false;
			}
		}
	}

	public boolean isNextHasBonus() {
		return false;
	}

	public boolean[] isNextNHasBonus(int n) {
		if (n < 0) {
			return null;
		} else {
			return new boolean[n];
		}
	}

	/**
	 * 按类型取上次<em>免费</em>抽奖时刻。
	 * 
	 * @return 类型上次抽奖时刻。
	 */
	abstract public long getFreeGatchaTime();

	/**
	 * 取抽奖次数。
	 * 
	 * @return 抽奖次数。
	 */
	abstract public int getGatchaCount();

	/**
	 * 记录免费抽奖次数。
	 * 
	 * @param trType
	 *            宝藏类型
	 */
	abstract public void noteFreeGatcha();

	/**
	 * 记录付费连抽次数。
	 * 
	 * @param trType
	 *            宝藏类型
	 * @param n
	 *            次数
	 */
	abstract void noteMultiGatcha(int n);

	/**
	 * 记录付费抽奖次数。
	 * 
	 * @param trType
	 *            宝藏类型
	 */
	public void noteGatcha() {
		noteMultiGatcha(1);
	}

	public ConfTreasureData getConf() {
		return this.trConf;
	}

	public int getSn() {
		return this.trConf.sn;
	}

	public abstract DTreasure.Builder getMsg();
}
