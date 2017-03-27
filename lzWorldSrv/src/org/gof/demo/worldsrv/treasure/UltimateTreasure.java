package org.gof.demo.worldsrv.treasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.config.ConfTreasureData;
import org.gof.demo.worldsrv.entity.Human;
import org.gof.demo.worldsrv.item.ItemRandomTools;
import org.gof.demo.worldsrv.msg.Msg.DTreasure;
import org.gof.demo.worldsrv.produce.ProduceManager;
import org.gof.demo.worldsrv.produce.ProduceVo;
import org.gof.demo.worldsrv.support.Log;

import com.alibaba.fastjson.JSONObject;

public class UltimateTreasure extends Treasure {

	public UltimateTreasure(HumanObject humanObj) {
		super(humanObj, ConfTreasureData
				.get(TreasureManager.TREASURE_TYPE_高级宝藏));

	}

	@Override
	public boolean[] isNextNHiden(int n) {
		if (n <= 0) {
			throw new IllegalArgumentException("n:" + n);
		}
		return new boolean[n];
	}

	@Override
	public DTreasure.Builder getMsg() {
		DTreasure.Builder builder = DTreasure.newBuilder();
		builder.setType(getSn());
		builder.setBonusProc(0);
		builder.setDailyFree(0);
		builder.setCd(0);
		builder.setToHiden(0);
		builder.setBatchSale(0);
		builder.setBatchCost(0);
		Integer[] hotSns = TreasureManager.inst().getUltimateHotSn(humanObj);
		for (Integer sn : hotSns) {
			builder.addHotItems(sn);
		}
		Log.treasure.debug("{}/t今日热点装备：{}", humanObj.getHuman().getName(),
				builder.getHotItemsList());
		return builder;
	}

	@Override
	void noteMultiGatcha(int n) {
		Human human = humanObj.getHuman();
		String trCountsStr = human.getTrCounts();
		JSONObject json = Utils.toJSONObject(trCountsStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_高级宝藏);
		int c = json.getIntValue(key);
		c += n;
		json.put(key, c);
		human.setTrCounts(json.toJSONString());
	}

	@Override
	public void noteFreeGatcha() {
		// 高级的没有Free
	}

	@Override
	public List<ProduceVo> nextGatcha() {
		Integer[] hotSn = TreasureManager.inst().getUltimateHotSn(humanObj);
		int[] normalGroup = new int[hotSn.length];
		for (int i = 0; i < hotSn.length; i++) {
			normalGroup[i] = hotSn[i];
		}
		Human human = humanObj.getHuman();
		List<Integer> hotSns = TreasureManager.inst().getUltimateProduceSns(
				human);
		Map<Integer, Integer> randMap = new HashMap<>();
		hotSns.forEach(sn -> randMap.put(sn, 1));
		int produceSn = ItemRandomTools.randomWithJSONObject(randMap);
		List<ProduceVo> aglist = ProduceManager.inst().produceItem2(humanObj,
				produceSn);
		List<ProduceVo> ag = ProduceManager.inst().produceItem2(humanObj,
				trConf.bonusGroup);
		aglist.addAll(ag);
		return aglist;
	}

	@Override
	public List<List<ProduceVo>> nextMultiGatcha(int n) {
		// 高级的没有连抽
		return new ArrayList<>();
	}

	@Override
	public long getFreeGatchaTime() {
		return 0; // 没有免费
	}

	@Override
	public int getGatchaCount() {
		Human human = humanObj.getHuman();
		String trCountsStr = human.getTrCounts();
		JSONObject json = Utils.toJSONObject(trCountsStr);
		String key = String.valueOf(TreasureManager.TREASURE_TYPE_高级宝藏);
		return json.getIntValue(key);
	}

}
