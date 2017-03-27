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
 * 中级宝箱
 */
public class GreatTreasure extends Treasure {

	public GreatTreasure(HumanObject humanObj) {
		super(humanObj, ConfTreasureData
				.get(TreasureManager.TREASURE_TYPE_中级宝藏));
	}

	@Override
	public boolean isNextFree() {
		return getFreeCd() == 0;
	}

	@Override
	public boolean isNextHiden() {
		return getToHiden() == 1;
	}

	@Override
	public boolean isNextHasBonus() {
		int bonusStep = trConf.hasBonus;
		if (bonusStep == 0) {
			return false;
		} else {
			return bonusStep - getBonusProc() == 1;
		}
	}

	@Override
	public boolean[] isNextNHasBonus(int n) {
		boolean[] nextNHiden = new boolean[n];
		if (nextNHiden != null) {
			int gatchaCount = getGatchaCount();
			int bonusCyc = this.trConf.hasBonus;
			for (int i = 0; i < n; i++) {
				int nGatcha = gatchaCount + 1 + i;
				if (nGatcha % bonusCyc == 0) {
					nextNHiden[i] = true;
				}
			}
		}
		return nextNHiden;
	}

	@Override
	public DTreasure.Builder getMsg() {
		DTreasure.Builder builder = DTreasure.newBuilder();
		builder.setType(getSn());
		builder.setBonusProc(getBonusProc());
		builder.setDailyFree(0);
		builder.setCd(getFreeCd());
		builder.setToHiden(getToHiden());
		int multiCount = ConfGlobalUtils.getValue(ConfGlobalKey.连抽次数);
		float batchSale = 1.0f * ConfGlobalUtils.getValue(ConfGlobalKey.连抽折扣) / 10000;
		int batchCost = (int) Math.ceil(batchSale * trConf.virCount
				* multiCount);
		builder.setBatchSale(batchSale * 10);
		builder.setBatchCost(batchCost);
		Log.treasure.debug("中级宝藏赠送进度={}，免费cd={}", builder.getBonusProc(),
				builder.getCd());
		return builder;
	}

	private int getBonusProc() {
		int hasBonus = trConf.hasBonus;
		if (hasBonus == 0) {
			return 0;
		} else {
			int gatchaCount = getGatchaCount();
			return gatchaCount % hasBonus;
		}
	}

	private int getToHiden() {
		int[] toHidenShift = trConf.groupShift;
		int gatchaCount = getGatchaCount();
		int section = 0;
		while (gatchaCount > toHidenShift[section]) {
			gatchaCount = gatchaCount - toHidenShift[section];
			if (section + 1 < toHidenShift.length) {
				section++;
			} else {
				break;
			}
		}
		int toHiden = toHidenShift[section];
		if (toHiden == 0) {
			return 0;
		}
		toHiden = toHiden - gatchaCount % toHiden;
		return toHiden;
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
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_中级宝藏);
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
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_中级宝藏) + "f";
		int c = json.getIntValue(key);
		c++;
		json.put(key, c);
		human.setTrCounts(json.toJSONString());

		long now = Port.getTime();
		String trTimesStr = human.getFreeTrTime();
		JSONObject json2 = Utils.toJSONObject(trTimesStr);
		String key2 = String.valueOf(TreasureManager.TREASURE_TYPE_中级宝藏);
		json2.put(key2, now);
		human.setFreeTrTime(json2.toJSONString());
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
		if (isNextHasBonus()) {
			List<ProduceVo> aglist = ProduceManager.inst().produceItem2(
					humanObj, dropId);
			int dropId2 = trConf.bonusGroup;
			List<ProduceVo> ag = ProduceManager.inst().produceItem2(humanObj,
					dropId2);
			aglist.addAll(ag);
			return aglist;
		} else {
			return ProduceManager.inst().produceItem2(humanObj, dropId);
		}
	}

	@Override
	public List<List<ProduceVo>> nextMultiGatcha(int n) {
		List<List<ProduceVo>> aglist = new ArrayList<>();
		boolean[] nextNHiden = isNextNHiden(n);
		boolean[] nextNHasBonus = isNextNHasBonus(n);
		for (int i = 0; i < n; i++) {
			if (nextNHiden[i]) {
				List<ProduceVo> ag = ProduceManager.inst().produceItem2(humanObj,
						trConf.hidenGroup);
				aglist.add(ag);
			} else {
				List<ProduceVo> ag = ProduceManager.inst().produceItem2(humanObj,
						trConf.normalGroup[0]);
				aglist.add(ag);
			}
			if (nextNHasBonus[i]) {
				List<ProduceVo> ag = ProduceManager.inst().produceItem2(humanObj,
						trConf.bonusGroup);
				aglist.add(ag);
			}
		}
		return aglist;
	}

	@Override
	public long getFreeGatchaTime() {
		Human human = humanObj.getHuman();
		String timeStr = human.getFreeTrTime();
		JSONObject json = Utils.toJSONObject(timeStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_中级宝藏);
		return json.getLongValue(key);
	}

	@Override
	public int getGatchaCount() {
		Human human = humanObj.getHuman();
		String trCountsStr = human.getTrCounts();
		JSONObject json = Utils.toJSONObject(trCountsStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_中级宝藏);
		return json.getIntValue(key);
	}
}
