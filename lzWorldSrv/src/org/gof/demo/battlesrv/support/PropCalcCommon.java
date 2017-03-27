package org.gof.demo.battlesrv.support;

import org.gof.core.support.Utils;


/**
 * 通用属性累加，不仅适用于prop，而且适用于propExt
 */
public class PropCalcCommon extends PropCalcBase<String, Double> {
	public PropCalcCommon() {
	}
	
	public PropCalcCommon(String json) {
		super(json);
	}
	
	public int getInt(Enum<?> e) {
		return getInt(e.name());
	}


	public double getDouble(Enum<?> e) {
		return getDouble(e.name());
	}
	
	@Override
	protected String toKey(String key) {
		return key;
	}

	@Override
	protected Double toValue(Object value) {
		return Utils.doubleValue(value.toString());
	}

	@Override
	protected boolean canDiscard(Object value) {
		double temp = toValue(value);
		if(temp < 1e-06 && temp > -1e-06) {
			return true;
		}
		return false;
	}
	
}
