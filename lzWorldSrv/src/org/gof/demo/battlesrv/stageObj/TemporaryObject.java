package org.gof.demo.battlesrv.stageObj;

import org.gof.core.Port;
import org.gof.demo.worldsrv.msg.Msg;
import org.gof.demo.worldsrv.stage.StageObject;

/**
 * 地图场景临时单元
 * 支持到时后自动移出场景
 */
public abstract class TemporaryObject extends WorldObject {
	public TemporaryObject(StageObject stageObj) {
		super(stageObj);
		this.id = Port.applyId();
	}

	@Override
	public abstract Msg.DStageObject.Builder createMsg();
}