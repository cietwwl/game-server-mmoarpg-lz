package org.gof.demo.worldsrv.support.pathFinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gof.core.Port;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.worldsrv.config.ConfMap;
import org.gof.demo.worldsrv.support.D;
import org.gof.demo.worldsrv.support.Log;

/**
 * 寻路核心类
 *
 */
public class PathFinding {
	// nav文件所在目录及其后缀
	private static String navDir = PathFinding.class.getResource("/").getPath() + "META-INF/stageConfig/mesh/";
	private static String navSuffix = ".bytes";
	
	/**
	 * 加载寻路用到的C++动态库recast.dll
	 * 初始化加载所有数据
	 */
	static {
		//TODO 别忘了关 注释了下面1行
		System.loadLibrary("recast");
//		checkFilesExist();
		init();
	}
	
	/**
	 * 检查地图的xml和nav是否都存在
	 */
	private static void checkFilesExist() {
		Collection<ConfMap> confList = ConfMap.findAll();
		List<ConfMap> xmlLost = new ArrayList<>();
		List<ConfMap> navLost = new ArrayList<>();
		for(ConfMap conf : confList) {
			int sn = conf.sn;
			//判断xml文件和nav文件是否都存在，如果不存在则直接抛出异常
			File navFile = new File(navDir + sn + navSuffix);
			if(!navFile.exists()) {
				navLost.add(conf);
			}
		}
		
		if(!navLost.isEmpty()) {
			System.out.println("+++++navLost+++++以下地图缺失nav文件+++++navLost+++++");
			for(ConfMap conf : navLost) {
				System.out.println(conf.name + " " + conf.sn);
			}
		}
		if(!xmlLost.isEmpty() || !navLost.isEmpty()) {
			throw new SysException(Utils.createStr("有地图的xml或者nav文件缺失，无法启动！！！！！"));
		}
	}
	
	private static int getStageSnPort(String name) {
		return (Port.getCurrent().getId() + name).hashCode();
	}
	/**
	 * 遍历nav导航网格数据所在文件夹，初始化加载所有地图导航数据
	 */
//	private static void init() {
//		// 遍历nav文件所在目录
//		File dir = new File(navDir);
//		if(!dir.isDirectory())	return;
//		
//		File[] files = dir.listFiles();
//		for(File f : files) {
//			// 过滤不是nav文件
//			String fileName = f.getName();
//			if(!fileName.endsWith(navSuffix)) continue;
//			
//			int stageSnCode = fileName.replace(navSuffix, "").hashCode();
//			
//			loadNavData(stageSnCode, getStageSnPort(fileName.replace(navSuffix, "")), f.getAbsolutePath());
//		}
//	}
	private static void init() {
		File dir = new File(navDir);
		if(!dir.isDirectory())	return;

		// 给每个stagePort都初始化寻路相关数据
		for(int i = 0; i < D.PORT_STAGE_STARTUP_NUM; ++i) {
			//拼PortId
			String portId = D.PORT_STAGE_PREFIX + i;
			
			// 遍历nav文件所在目录
			File[] files = dir.listFiles();
			for(File f : files) {
				// 过滤不是nav文件
				String fileName = f.getName();
				if(!fileName.endsWith(navSuffix)) continue;
				
				int stageSnCode = fileName.replace(navSuffix, "").hashCode();
				int stageSnPortCode = (portId + fileName.replace(navSuffix, "")).hashCode();
				boolean result = loadNavData(stageSnCode, stageSnPortCode, f.getAbsolutePath());
				if(!result) {
					SysException e = new SysException(Utils.createStr("加载nav数据出错，stageInfo={}", portId + fileName.replace(navSuffix, "")));
					throw e;
				}
			}
		}
	}
	/**
	 * 初始化并加载nav数据
	 * @param navPath
	 */
	private static native boolean loadNavData(int stageSn, int stageSnPort, String navPath);
	
	/**
	 * 根据起点终点坐标找到路径
	 * @param startPos
	 * @param endPos
	 * @return
	 */
	public static List<Vector3D> findPaths(int stageSn, Vector3D startPos, Vector3D endPos) {
		return findPaths(stageSn, startPos, endPos, PathFindingFlagKey.init);
	}
	
	/**
	 * 根据起点，终点，掩码寻路，返回Vector2路径列表
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	public static List<Vector3D> findPaths(int stageSn, Vector3D startPos, Vector3D endPos, int flag) {
		// 转换坐标为float[]
		float[] start = startPos.toDetourFloat3();
		float[] end = endPos.toDetourFloat3();
		
		// 寻路结果
		String asset = ConfMap.get(stageSn).asset;
		int stageSnCode = getStageSnPort(asset);
		float[] paths = findPath(stageSnCode, start, end, flag);
		int len = paths.length;
		
		// 转成需要的Vector2
		List<Vector3D> result = new ArrayList<>();
		for(int i=0; i<len; i+=3) {
			Vector3D point = new Vector3D(paths[i], paths[i+1] ,paths[i+2]).toServFromDetour();
			
			result.add(point);
		}
		
		return result;
	}
	
	/**
	 * 根据起点，终点，掩码寻路
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	private static native float[] findPath(int stageSn, float[] startPos, float[] endPos, int flag);
	
	/**
	 * 判断坐标是否在阻挡区域内
	 * @param pos
	 * @return
	 */
	public static boolean isPosInBlock(int stageSn, Vector3D pos){
		String asset = ConfMap.get(stageSn).asset;
		int stageSnCode = getStageSnPort(asset);
		return isPosInBlock(stageSnCode, new float[]{(float)pos.y, (float)pos.z, (float)pos.x});
		
	}
	
	/**
	 * 判断坐标是否在阻挡区域内
	 * @param pos
	 * @return
	 */
	private static native boolean isPosInBlock(int stageSn, float[] pos);
	
	/**
	 * 检测起点到终点是否有阻挡，有则返回阻挡坐标，无则返回终点坐标
	 * @param startPos
	 * @param endPos
	 * @return
	 */
	public static Vector3D raycast(int stageSn, Vector3D startPos, Vector3D endPos) {
		return raycast(stageSn, startPos, endPos, PathFindingFlagKey.init);
	}
	
	/**
	 * 检测起点到终点是否有阻挡，有则返回阻挡坐标，无则返回终点坐标
	 * FIXME 待删除 由于导航网格bug，所以需要把坐标延长1000米，然后修正 等代码修整后注释这段代码
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	public static Vector3D raycast(int stageSn, Vector3D startPos, Vector3D endPos, int flag) {
		
		Vector2D new2D = new Vector2D();
		Vector3D new3D = new Vector3D(endPos.x, endPos.y, endPos.z);
		new2D = new3D.sub(startPos).toVector2D().normalize().mul(9999);
		
		 //转换坐标为float[]
		float[] start = startPos.toDetourFloat3();
		float[] end = endPos.toDetourFloat3();
	
		end[0] = (float) new2D.y;
		end[2] = (float) new2D.x;
		
		float[] endFix = new float[3];
		endFix[0] = end[0] - start[0] ;
		
		// 检测结果
		String asset = ConfMap.get(stageSn).asset;
		int stageSnCode = getStageSnPort(asset);
		float[] paths = raycast(stageSnCode, start, end, flag);
		
		// 转成需要的Vector2
		Vector3D result = new Vector3D(paths[0], paths[1], paths[2]).toServFromDetour();
//		Log.temp.info("raycast {} {} {} {}",end[0], end[1], end[2], result );
		
		if( result.distanceFar(startPos) > endPos.distanceFar(startPos) ) {
			return endPos;
		}
		
		return result;
		
	}
	
	/**
	 * 检测起点到终点是否有阻挡，有则返回阻挡坐标，无则返回终点坐标
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	private static native float[] raycast(int stageSn, float[] startPos, float[] endPos, int flag);
	
	/**
	 * 判断两个点是否能到达，判断标准为recase找到的终点与给定终点距离小于0.1
	 * @param stageSn
	 * @param startPos
	 * @param endPos
	 * @param flag
	 * @return
	 */
	public static boolean canReach(int stageSn, Vector3D startPos, Vector3D endPos, int flag) {
		List<Vector3D> paths = findPaths(stageSn, startPos, endPos, flag);
		if(paths.isEmpty()) return false;
		Vector3D pathEnd = paths.get(paths.size() - 1);
		if(pathEnd.distance(endPos) <= 0.1) return true;
		
		return false;
	}
	
	/**
	 * 获得坐标对应的高度
	 * @param pos
	 * @return
	 */
	private static native float[] posHeight(int stageSn, float[] pos);
	
	public static Vector3D posHeight(int stageSn, Vector2D pos){
		String asset = ConfMap.get(stageSn).asset;
		int stageSnCode = getStageSnPort(asset);
		float[] temp = posHeight(stageSnCode, new float[]{(float)pos.y, 0, (float)pos.x});
		Vector3D result = new Vector3D();
		result.x = pos.x;
		result.y = pos.y;
		result.z = temp[1];
		
		return result;
	}
	
	public static boolean posHeight(int stageSn, Vector3D pos){
		String asset = ConfMap.get(stageSn).asset;
		int stageSnCode = asset.hashCode();
		float[] temp = posHeight(stageSnCode, new float[]{(float)pos.y, 0, (float)pos.x});
		pos.z = temp[1];
		if(pos.z != 0) {
			return true;
		}
		return false;
	}
	
}
