package org.gof.demo.worldsrv.pocketLine;

import org.gof.core.Port;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.gen.proxy.DistrMethod;
import org.gof.demo.worldsrv.common.GamePort;
import org.gof.demo.worldsrv.common.GameServiceBase;
import org.gof.demo.worldsrv.entity.PocketLine;
import org.gof.demo.worldsrv.support.D;

@DistrClass(
	servId = D.SERV_POCKETLINE
)
public class PocketLineService extends GameServiceBase {

	public PocketLineService(GamePort port) {
		super(port);
	}

	/**
	 * 添加一个待办
	 * @param humanId
	 * @param pocketTypeKey
	 * @param param
	 */
	@DistrMethod
	public void add(long humanId, PocketLineKey pocketTypeKey, String param) {

		createPocket(humanId, pocketTypeKey, param);
	}

	@Override
	protected void init() {
		
	}
	
	/**
	 * 创建待办实体
	 * @param humanId
	 * @param pocketTypeKey
	 * @param param
	 * @return
	 */
	private PocketLine createPocket(long humanId, PocketLineKey pocketTypeKey, String param) {
		PocketLine pocketList = new PocketLine();
		
		pocketList.setId(Port.applyId());
		pocketList.setHumanId(humanId);
		pocketList.setModuleName(pocketTypeKey.name());
		pocketList.setParam(param);
		
		pocketList.persist();
		
		return pocketList;
	}
	
}
