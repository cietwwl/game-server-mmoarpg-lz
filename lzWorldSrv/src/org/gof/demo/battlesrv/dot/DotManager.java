package org.gof.demo.battlesrv.dot;

import org.gof.core.support.ManagerBase;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfDot;
import org.gof.demo.worldsrv.stage.StageObject;

public class DotManager extends ManagerBase {
	
	/**
	 * 获取实例
	 * @return
	 */
	public static DotManager inst() {
		return inst(DotManager.class);
	}

	/**
	 * 地图上加dot
	 * @param unitObj
	 * @param sn
	 * @param vector
	 */
	public void create(StageObject stageObj, int sn, int skillSn, UnitObject unitObjFire, WorldObject worldObjectCreate, UnitObject unitObjTar, Vector2D vector, double shakePct) {
		ConfDot confDot = ConfDot.get(sn);
		
		DotObject dotObj = new DotObject(stageObj, confDot, skillSn, unitObjFire, worldObjectCreate, unitObjTar, vector, shakePct);
		dotObj.init();
		
		dotObj.stageEnter(stageObj);
	}
}
