package org.gof.demo.worldsrv.pocketLine;



public class Pocket {

	/**
	 * 添加一个待办事项
	 * @param dbId
	 * @param humanId
	 * @param pocketTypeKey
	 * @param param
	 */
	public static void add(long humanId, PocketLineKey pocketLineKey, String param) {
		PocketLineServiceProxy pxy = PocketLineServiceProxy.newInstance();
		pxy.add(humanId, pocketLineKey, param);
	}
}
