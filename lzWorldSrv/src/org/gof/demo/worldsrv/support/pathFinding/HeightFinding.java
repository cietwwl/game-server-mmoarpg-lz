package org.gof.demo.worldsrv.support.pathFinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.demo.battlesrv.support.Vector2D;
import org.gof.demo.battlesrv.support.Vector3D;
import org.gof.demo.worldsrv.config.ConfMap;

/**
 * 根据配置文件，查找坐标信息
 */
public class HeightFinding {
	
	// 每个场景所有的坐标高度点的信息
	private static Map<String, Map<Integer, Float>> HEIGHT_INFO = new HashMap<String, Map<Integer, Float>>();
	
	// 每个场景所有的宽度信息，用于计算高度信息的索引值
	private static Map<String, Integer> HEIGHT_WIDTH = new HashMap<String, Integer>();
	
	// 高度描述文件的路径
	private static final String HEIGHT_INFO_DIR = PathFinding.class.getResource("/").getPath() + "META-INF/stageConfig/heightInfo/";
	
	// 高度描述文件的后缀
	private static final String INFO_SUFFIX = ".txt";
	
	private static final String SPLIT_SUFFIX = ",";
	
	// 高度描述文件需要忽略的行数
	private static final int INGORE_LINE = 1;
	
	static {
		init();
	}
	
	/**
	 * 启动时候加载数据文件到内存中
	 */
	private static void init() {
		// 遍历height文件所在目录
		File dir = new File(HEIGHT_INFO_DIR);
		if(!dir.isDirectory())	return;
		
		// 挨个文件遍历，放入到Map中
		BufferedReader br = null;
		File[] files = dir.listFiles();
		for(File f : files) {
			// 过滤不是匹配类型的文件
			String fileName = f.getName();
			if(!fileName.endsWith(INFO_SUFFIX)) continue;
			
			//获取Key，用asset作为Key
			String asset = fileName.replace(INFO_SUFFIX, "");
			
			//读取文件流
			try {
				br = new BufferedReader(new FileReader(f));
				
				String[] minBoundInfo = br.readLine().split(SPLIT_SUFFIX);
				//场景可能产生的坐标
				int minx = Utils.intValue(minBoundInfo[0]);
				int miny = Utils.intValue(minBoundInfo[2]);
				//暂时不用的空行，忽略掉
				for (int i = 0; i < INGORE_LINE; i++) {
					br.readLine();
				}
				
				//这一行，是地图的大小
				String[] info = br.readLine().split(SPLIT_SUFFIX);
				if(info.length != 2 )
					throw new SysException(Utils.createStr("场景高度描述文件错误：宽高信息缺失，文件:{}，行数:{}。",fileName, INGORE_LINE+1 ));
				//实际有的坐标
				int cosX = Utils.intValue(info[0]);
				int cosY = Utils.intValue(info[1]);
				//进行了坐标修正处理
				int cosXOffset = (int)(Utils.floatValue(info[0]) + minx);
//				int cosYOffset = (int)(Utils.floatValue(info[1]) + miny);
				//存放一下每个场景的宽度
				HEIGHT_WIDTH.put(asset, cosXOffset);
				
				// 读取所有的坐标的高度
				String content= "";
				String t;
				while ((t = br.readLine()) != null ){
					content += t;
				}
				String[] height = content.split(SPLIT_SUFFIX);
				if(cosX * cosY != height.length)
					throw new SysException(Utils.createStr("场景高度描述文件错误：坐标数量不匹配。文件:{}，坐标总数:{}，高度总数:{}。", fileName, cosX*cosY, height.length));
				
				// 把高度坐标放入每个点里
				Map<Integer, Float> point = new HashMap<>();
				for (int i = 0; i < cosX; i++) {
					for (int j = 0; j < cosY; j++) {
						point.put((j + miny) * cosXOffset + (i + minx), Utils.floatValue(height[j * cosX + i]));
					}
				}
				
				HEIGHT_INFO.put(asset, point);
				
			} catch (IOException e) {
				throw new SysException(Utils.createStr("场景高度描述文件错误：读取文件错误。文件:{}。", fileName));
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					throw new SysException(e);
				}
			}
		}
	}
	
	/**
	 * 查找2D坐标所在的位置的3D坐标，保护高度信息
	 * @param stageSn
	 * @param pos
	 * @return
	 */
	public static Vector3D posHeight(int stageSn, Vector2D pos){
		String asset = ConfMap.get(stageSn).asset;
		Float height = HEIGHT_INFO.get(asset).get((int)pos.y * HEIGHT_WIDTH.get(asset) + (int)pos.x);
		Vector3D result = new Vector3D();
		result.x = pos.x;
		result.y = pos.y;
		if(height == null) 
			height = -99999f;
		result.z = height;
		return result;
	}
	
	/**
	 * 查找每个3D坐标是否有高度值，并且记录
	 * @param stageSn
	 * @param pos
	 * @return
	 */
	public static boolean posHeight(int stageSn, Vector3D pos){
		String asset = ConfMap.get(stageSn).asset;
		Float height = HEIGHT_INFO.get(asset).get((int)pos.y * HEIGHT_WIDTH.get(asset) + (int)pos.x);
		pos.z = height;
		if(pos.z != 0) {
			return true;
		}
		return false;
	}
	
}
