package org.gof.demo.battlesrv.stageObj;

import java.util.Comparator;

import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.manager.UnitManager;
import org.gof.demo.battlesrv.support.PropKey;
import org.gof.demo.worldsrv.msg.Msg.DProp;
import org.gof.demo.worldsrv.msg.Msg.DUnit;
import org.gof.demo.worldsrv.stage.StageCell;
import org.gof.demo.worldsrv.stage.StageManager;
import org.gof.demo.worldsrv.stage.StageObject;
import org.gof.demo.worldsrv.stage.StagePort;

/**
 * 处理玩家 武将 怪物的公共属性 方法。 但是不包括战斗， 使得战斗可以独立
 * @author rattler
 *
 */
public abstract class CharacterObject extends UnitObject  implements Comparator<CharacterObject> {
	public CharacterObject(StageObject stageObj) {
		super(stageObj);
	}
	
	public StagePort getPort() {
		return stageObj.getPort();
	}
	
	@Override
	public void pulseMove(long timeCurr) {
		super.pulseMove(timeCurr);
		
		StageCell cellBegin = stageCell;
		StageCell cellEnd = stageObj.getCell(posNow);
		stageCell = cellEnd;
		// 判断玩家有没有跨地图格了
		if (cellBegin != null && !cellEnd.equals(cellBegin)) { // 跨地图格了
			StageManager.inst().cellChanged(cellBegin, cellEnd, this);
		}
	}
	
	public DUnit.Builder createDUnit() {
		// 玩家基本属性
		DProp.Builder dProp = DProp.newBuilder();
		for (PropKey propKey : PropKey.values()) {
			Object v = UnitManager.inst().getPropKeyValue(propKey, getUnit());
			Utils.fieldWrite(dProp, propKey.name(), v);
		}
		

		// 基本信息
		DUnit.Builder dUnit = DUnit.newBuilder();
		dUnit.setProp(dProp);
		dUnit.setId(id);
		dUnit.setName(getUnit().getName());
		dUnit.setLevel(getUnit().getLevel());
		dUnit.setSex(getUnit().getSex());
		dUnit.setHpCur(getUnit().getHpCur());
		dUnit.setHpMax(getUnit().getHpMax());
		dUnit.setMpCur(getUnit().getMpCur());
		dUnit.setMpMax(getUnit().getMpMax());
		dUnit.setProfession(getUnit().getProfession());
		dUnit.setTeamBundleID(teamBundleID);
		dUnit.setModelSn(getUnit().getModelSn());
		dUnit.setSn(getUnit().getSn());
		dUnit.setExpCur(getUnit().getExpCur());
		dUnit.setExpUpgrade(getUnit().getExpUpgrade());
		
		return dUnit;
	}
	@Override
	public int compare(CharacterObject u1, CharacterObject u2) {
		if(u2 == null || u1 == null)
			return 0;
		if(u1.profession < u2.profession)
			return -1;
		else if(u1.profession > u2.profession)
			return 1;
		else
			return u1.order - u2.order;
	}
}
