package org.gof.demo.battlesrv.stageObj;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gof.core.InputStream;
import org.gof.core.OutputStream;
import org.gof.core.interfaces.ISerilizable;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.support.PropCalcCommon;
import org.gof.demo.worldsrv.entity.EntityUnitPropPlus;
import org.gof.demo.worldsrv.entity.UnitPropPlus;

public class UnitPropPlusMap implements ISerilizable{
	public Map<String, Map<String, Double>>dataMap = new HashMap<String, Map<String, Double>>();
	public UnitPropPlus unitPropPlus = new UnitPropPlus();
	private PropCalcCommon propCalc = null;
	
	/**
	 * 初始化 在读取数据库以后调用
	 */
	public void init(UnitPropPlus unitPropPlus) {
		this.unitPropPlus = unitPropPlus;
		for(EntityUnitPropPlus k : EntityUnitPropPlus.values()) {
			String pp = Utils.fieldRead(unitPropPlus, k.name());
			dataMap.put(k.name(), toMap(pp));
		}
	}
	
	public Map<String, Double> toMap(String pp) {
		propCalc = new PropCalcCommon();
		propCalc.removeAll();
		propCalc.plus(pp);
		return propCalc.getDatas();
	}
	
	
	public String getBase() {
		return this.unitPropPlus.getBase();
	}

	public void setBase(String base) {
		this.unitPropPlus.setBase(base);
		dataMap.put(UnitPropPlus.K.base, toMap(base));
	}

	public String getLevel() {
		return this.unitPropPlus.getLevel();
	}

	public void setLevel(String level) {
		this.unitPropPlus.setLevel(level);
		dataMap.put(UnitPropPlus.K.level, toMap(level));
	}

	public String getBuff() {
		return this.unitPropPlus.getBuff();
	}

	public void setBuff(String buff) {
		this.unitPropPlus.setBuff(buff);
		dataMap.put(UnitPropPlus.K.buff, toMap(buff));
	}

	public String getItemEquip() {
		return this.unitPropPlus.getItemEquip();
	}

	public void setItemEquip(String itemEquip) {
		this.unitPropPlus.setItemEquip(itemEquip);
		dataMap.put(UnitPropPlus.K.itemEquip, toMap(itemEquip));
	}

	public String getDebug() {
		return this.unitPropPlus.getDebug();
	}

	public void setDebug(String debug) {
		this.unitPropPlus.setDebug(debug);
		dataMap.put(UnitPropPlus.K.debug, toMap(debug));
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		out.write(dataMap);
		out.write(unitPropPlus);
	}

	@Override
	public void readFrom(InputStream in) throws IOException {
		dataMap = in.read();
		unitPropPlus = in.read();
	}
	
	
}
