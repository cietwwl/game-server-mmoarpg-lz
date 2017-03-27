package org.gof.demo.worldsrv.common;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.gof.core.Node;
import org.gof.core.gen.proxy.DistrClass;
import org.gof.core.support.Distr;
import org.gof.core.support.ManagerBase;
import org.gof.core.support.PackageClass;
import org.gof.core.support.Param;
import org.gof.core.support.SysException;
import org.gof.core.support.observer.Listener;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.observer.EventKey;

public class GameServiceManager extends ManagerBase {
	/**
	 * 游戏启动时 开启本服务
	 * @param params
	 * @throws Exception 
	 */
	@Listener(EventKey.GAME_STARTUP_BEFORE)
	public void onGameStartupBefore(Param params) {
		try{
		Node node = params.get("node");
		
		for(int i = 0; i < D.PORT_GAME_STARTUP_NUM; i++) {
			//拼PortId
			String portId = D.PORT_GAME_PREFIX + i;
			
			//验证启动Node
			String nodeId = Distr.getNodeId(portId);
			if(!node.getId().equals(nodeId)) {
				continue;
			}
			
			//启动服务
			GamePort portGlobal = new GamePort(portId);
			portGlobal.startup(node);
			
			//初始化下属服务
			initService(portGlobal);
		}
		}catch(Exception e){
			throw new SysException(e);
		}
	}
	
	/**
	 * 初始化下属服务
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	private void initService(GamePort port) throws Exception {
		// 获取源文件夹下的所有类
		Set<Class<?>> clazzSet = PackageClass.find();

		// 遍历所有类，取出ServiceCommonBase的子类，并进行初始化
		for(Class<?> clazz : clazzSet) {
			//只处理GameServiceBase的子类
			if(!GameServiceBase.class.isAssignableFrom(clazz)) {
				continue;
			}
			
			//必须有@DistrClass注解
			if(!clazz.isAnnotationPresent(DistrClass.class)) {
				continue;
			}
			
			//根据注解信息查看是否为本Port启动
			DistrClass anno = clazz.getAnnotation(DistrClass.class);
			String portId = Distr.getPortId(anno.servId());
			if(!port.getId().equals(portId)) {
				continue;
			}
			
			//进行初始化
			GameServiceBase serv = (GameServiceBase) clazz.getConstructor(GamePort.class).newInstance(port);
			serv.startupLocal();
		}
	}
}
