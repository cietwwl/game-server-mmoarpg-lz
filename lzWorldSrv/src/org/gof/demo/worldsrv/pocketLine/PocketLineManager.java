package org.gof.demo.worldsrv.pocketLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gof.core.Record;
import org.gof.core.dbsrv.DBServiceProxy;
import org.gof.core.gen.callback.DistrCallback;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.Param;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.character.HumanObject;
import org.gof.demo.worldsrv.entity.PocketLine;
import org.gof.demo.worldsrv.support.observer.Event;
import org.gof.demo.worldsrv.support.observer.EventKey;

@SuppressWarnings("deprecation")
public class PocketLineManager extends ManagerBase{

	public static PocketLineManager inst() {
		return inst(PocketLineManager.class);
	}
	
	/**
	 * 登录时 加载待办
	 * @param param
	 */
	@Listener(EventKey.HUMAN_LOGIN)
	public void loadHumanDataPocketList(Param param) {
		HumanObject humanObject = param.get("humanObj");
		
		//
		DBServiceProxy dbProxy = DBServiceProxy.newInstance();
		dbProxy.findBy(false, PocketLine.tableName, "humanId", humanObject.id);
		
		dbProxy.listenResult(this::_result_loadHumanDataPocketList, "humanObject", humanObject);
	}
	
	/**
	 * 登录时加载待办回调
	 * @param results
	 * @param context
	 */
	@DistrCallback
	public void _result_loadHumanDataPocketList(Param results, Param context) {
		HumanObject humanObject = context.get("humanObject");
		
		Map<PocketLineKey, List<PocketLine>> pocketMap = new HashMap<>();
			
		//处理返回数据
		for(Record r : results.<List<Record>>get()) {
			PocketLine pocketLine = new PocketLine(r);
			//待办Key  先转类型 确定类型存在 
			PocketLineKey key = PocketLineKey.valueOf(pocketLine.getModuleName());
			//发送事件
			Event.fireEx(EventKey.POCKET_LINE_HANDLE_ONE, key.toString(),
					"humanObj", humanObject, "pocketLine", pocketLine);
			
			if(pocketMap.get(key) == null) {
				pocketMap.put(key, new ArrayList<PocketLine>());
			}
			//按模块分类
			pocketMap.get(key).add(pocketLine);
		}
		
		//按模块分类发送事件
		for(Map.Entry<PocketLineKey, List<PocketLine>> map : pocketMap.entrySet()) {
			//发送事件
			Event.fireEx(EventKey.POCKET_LINE_HANDLE, map.getKey(),
					"humanObj", humanObject, "pocketLines", map.getValue());
		}
		
		//发送处理待办结束
		Event.fire(EventKey.POCKET_LINE_HANDLE_END, humanObject);
	}
} 
