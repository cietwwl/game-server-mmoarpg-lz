package org.gof.demo.battlesrv.support;

import org.gof.core.support.Utils;

/**
 * 属性计算，只供PropKey中枚举的字段使用
 */
public class PropCalc extends PropCalcBase<PropKey, Integer> {
	public PropCalc() {
		
	}
	
	public PropCalc(String json) {
		super(json);
	}
	
	@Override
	protected PropKey toKey(String key) {
		return PropKey.valueOf(key);
	}
	
	@Override
	protected Integer toValue(Object value) {
		return Utils.intValue(value.toString());
	}

	@Override
	protected boolean canDiscard(Object value) {
		if(toValue(value) == 0) {
			return true;
		}
		return false;
	}
}
