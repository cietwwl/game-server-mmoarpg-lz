package org.gof.demo.worldsrv.treasure;

import java.util.ArrayList;
import java.util.List;

import org.gof.core.Port;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfTreasureData;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.msg.Msg.DTreasure;
import org.gof.demo.worldsrv.produce.ProduceManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.support.ConfGlobalUtils;
import org.gof.demo.worldsrv.support.Log;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;

import com.alibaba.fastjson.JSONObject;
/**
 * 初级宝箱
 */
public class PremierTreasure extends Treasure {

	public PremierTreasure(HumanObject humanObj) {
		super(humanObj, ConfTreasureData
				.get(TreasureManager.TREASURE_TYPE_初级宝藏));
	}

	@Override
	public boolean isNextFree() {
		return hasFreeCount() && getFreeCd() == 0;
	}

	@Override
	public boolean isNextHiden() {
		return getToHiden() == 1;
	}

	@Override
	public DTreasure.Builder getMsg() {
		DTreasure.Builder builder = DTreasure.newBuilder();
		builder.setType(getSn());
		builder.setBonusProc(0);
		builder.setDailyFree(getDailyFree());
		if (hasFreeCount()) {
			builder.setCd(getFreeCd());
		} else {
			builder.setCd(0);
		}
		builder.setToHiden(getToHiden());
		int multiCount = ConfGlobalUtils.getValue(ConfGlobalKey.连抽次数);
		float batchSale = 1.0f * ConfGlobalUtils.getValue(ConfGlobalKey.连抽折扣) / 10000;
		int batchCost = (int) Math.ceil(batchSale * trConf.virCount
				* multiCount);
		builder.setBatchSale(batchSale * 10);
		builder.setBatchCost(batchCost);
		Log.treasure.debug("初级宝藏免费cd={}，免费次数已用={}", builder.getCd(),
				builder.getDailyFree());
		return builder;
	}

	private int getToHiden() {
		int toHidenShift = trConf.groupShift[0];
		if (toHidenShift == 0) {
			return 0;
		}
		int gatchaCount = getGatchaCount();
		int toHiden = toHidenShift - gatchaCount % toHidenShift;
		return toHiden;
	}

	private boolean hasFreeCount() {
		return getDailyFree() < trConf.freeLimit;
	}

	private long getFreeCd() {
		long now = Port.getTime();
		return Math.max(getFreeGatchaTime() + trConf.cd * 1000 * 60 - now, 0);
	}

	@Override
	void noteMultiGatcha(int n) {
		Human human = humanObj.getHuman();
		String trCountsStr = human.getTrCounts();
		JSONObject json = Utils.toJSONObject(trCountsStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_初级宝藏);
		int c = json.getIntValue(key);
		c += n;
		json.put(key, c);
		human.setTrCounts(json.toJSONString());
	}

	@Override
	public void noteFreeGatcha() {
		Human human = humanObj.getHuman();
		String trCountsStr = human.getTrCounts();
		JSONObject json = Utils.toJSONObject(trCountsStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_初级宝藏) + "f";
		int c = json.getIntValue(key);
		c++;
		json.put(key, c);
		human.setTrCounts(json.toJSONString());

		long now = Port.getTime();
		String trTimesStr = human.getFreeTrTime();
		JSONObject json2 = Utils.toJSONObject(trTimesStr);
		String key2 = String.valueOf(TreasureManager.TREASURE_TYPE_初级宝藏);
		json2.put(key2, now);
		human.setFreeTrTime(json2.toJSONString());
	}

	/**
	 * 初级宝藏今日抽奖次数。
	 * 
	 * @return 初级宝藏今日抽奖次数。
	 */
	private int getDailyFree() {
		Human human = humanObj.getHuman();
		String trCountsStr = human.getTrCounts();
		JSONObject json = Utils.toJSONObject(trCountsStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_初级宝藏) + "f";
		return json.getIntValue(key);
	}

	@Override
	public List<ProduceVo> nextGatcha() {
		int dropId = 0;
		if (isNextHiden()) {
			dropId = trConf.hidenGroup;
		} else {
			if (isNextFree()) {
				dropId = trConf.bonusGroup;
			} else {
				dropId = trConf.normalGroup[0];
			}
		}
		List<ProduceVo> ag = ProduceManager.inst()
				.produceItem2(humanObj, dropId);
		return ag;
	}

	@Override
	public List<List<ProduceVo>> nextMultiGatcha(int n) {
		List<List<ProduceVo>> aglist = new ArrayList<>();
		boolean[] nextNHiden = isNextNHiden(n);
		for (int i = 0; i < n; i++) {
			int dropId = nextNHiden[i] ? trConf.hidenGroup
					: trConf.normalGroup[0];
			List<ProduceVo> ag = ProduceManager.inst().produceItem2(humanObj,
					dropId);
			aglist.add(ag);
		}
		return aglist;
	}

	@Override
	public long getFreeGatchaTime() {
		Human human = humanObj.getHuman();
		String timeStr = human.getFreeTrTime();
		JSONObject json = Utils.toJSONObject(timeStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_初级宝藏);
		return json.getLongValue(key);
	}

	@Override
	public int getGatchaCount() {
		Human human = humanObj.getHuman();
		String trCountsStr = human.getTrCounts();
		JSONObject json = Utils.toJSONObject(trCountsStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_初级宝藏);
		return json.getIntValue(key);
	}
}
