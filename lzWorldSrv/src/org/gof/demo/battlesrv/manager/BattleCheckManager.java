package org.gof.demo.battlesrv.manager;

import java.util.List;

import org.gof.core.support.ManagerBase;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.msg.Msg.DBattleMsg;

public class BattleCheckManager extends ManagerBase {
	/**
	 * 获取实例
	 * @return
	 */
	public static BattleCheckManager inst() {
		return inst(BattleCheckManager.class);
	}
	
	public void checkBattleResult(HumanObject humanObj, DBattleMsg msg) {
		List<Long> genId = msg.getGenIdList();
		List<Vector2D>genPos = Vector2D.parseFrom(msg.getGenPosList());
		for (int i = 0; i < genId.size(); i++) {
			humanObj.slavesAttingMap.get(genId.get(i)).posNow = genPos.get(i);
		}
	}
}
