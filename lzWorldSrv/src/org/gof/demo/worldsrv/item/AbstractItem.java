package org.gof.demo.worldsrv.item;

import org.gof.core.Record;
import org.gof.demo.worldsrv.entity.ItemBase;
import org.gof.demo.worldsrv.msg.Msg.DItem;

/**
 * 用于给被继承的实体加方法。
 * 
 * @author zhangbo
 *
 */
public abstract class AbstractItem extends ItemBase {

	public AbstractItem() {
		super();
	}

	public AbstractItem(Record record) {
		super(record);
	}

	/**
	 * 创建消息
	 * 
	 * @return 消息Builder
	 */
	public DItem.Builder createMsg() {
		DItem.Builder dItem = DItem.newBuilder();
		int sn = getSn();
		dItem.setCode(sn);
		dItem.setNum(getNum());
		dItem.setPos(getPosition());
		dItem.setBind(getBind());
		boolean isNew = isIsNew();
		if (isNew) {
			setIsNew(false);
		}
		dItem.setIsNew(isNew);
		return dItem;
	}
}