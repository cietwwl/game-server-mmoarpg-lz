package org.gof.demo.worldsrv.support;

import org.gof.demo.worldsrv.config.ConfGolbal;
import org.gof.demo.worldsrv.support.enumKey.ConfGlobalKey;

public class ConfGlobalUtils {

	private ConfGlobalUtils() { }

	public static String getStrValue(ConfGlobalKey key) {
		return ConfGolbal.get(key.SN).strValue;
	}

	public static int getValue(ConfGlobalKey key) {
		return ConfGolbal.get(key.SN).value;
	}

}
