package org.gof.demo.battlesrv.bullet;

import org.gof.core.support.ManagerBase;
import org.gof.demo.battlesrv.stageObj.UnitObject;
import org.gof.demo.battlesrv.stageObj.WorldObject;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.worldsrv.config.ConfBullet;
import org.gof.demo.worldsrv.stage.StageObject;

/**
 * Bullet对象，调用入口
 * 
 * @author GaoZhangCheng
 */
public class BulletManager extends ManagerBase {
	
	/**
	 * 获取实例
	 * @return
	 */
	public static BulletManager inst() {
		return inst(BulletManager.class);
	}


	/**
	 * 产生一个子弹
	 * @param stageObj
	 * @param sn
	 * @param skillSn
	 * @param unitObjFire
	 * @param unitObjTar
	 * @param vector
	 */
	public void create(StageObject stageObj, int sn, int skillSn, UnitObject unitObjFire, WorldObject worldObjectCreate, UnitObject unitObjTar, Vector2D vector) {
		ConfBullet confBullet = ConfBullet.get(sn);
		
		BulletObject bulletObj = new BulletObject(stageObj, skillSn, confBullet, unitObjFire, worldObjectCreate, unitObjTar, vector);
		bulletObj.init(stageObj);
		
		bulletObj.stageEnter(stageObj);
	}

}
