package org.gof.core.support;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * 通过给定的包名，来得到包下的全部CLASS类。
 */
public class PackageClass {
	private static String[] PATHS;		//类根目录地址
	//忽略的类 有些类Class.forName()时会报错...
	private static List<String> ignoreClass = new ArrayList<>();
	
	static {
		//初始化类根目录地址
		initPaths();
	}
	
	/**
	 * 获取包目录下的全部CLASS
	 * @param packageNames
	 * @return
	 */
	public static Set<Class<?>> find() {
		return find("");
	}
	
	/**
	 * 获取包目录下的全部CLASS
	 * @param packageNames
	 * @return
	 */
	public static Set<Class<?>> find(Collection<String> packageNames) {
		Set<Class<?>> result = new HashSet<Class<?>>();
		
		for(String p : packageNames) {
			result.addAll(find(p));
		}
		
		return result;
	}
	
	/**
	 * 获取包目录下的全部CLASS
	 * @param packageName
	 * @return
	 */
	public static Set<Class<?>> find(String packageName) {
		try {
			//返回值
			Set<Class<?>> result = new HashSet<Class<?>>();
			
			//从指定的目录中查找目录和以.class结尾的文件
			List<File> files = findFiles(packageName);
			
			//遍历此目录所有的文件
			for (File file : files) {
				String fileName = file.getName();
				
				//如果是目录 递归遍历
				if(file.isDirectory()) {
					String pack;
					if(StringUtils.isEmpty(packageName)) pack = fileName;
					else pack = packageName + "." + fileName;
					Set<Class<?>> r = find(pack);
					
					//记录返回值
					result.addAll(r);
				}
				
				//如果是文件 直接进行记录
				if (file.isFile()) {
					//得到类名 比如将User.class变为User
					String classSimpleName = fileName.substring(0, fileName.lastIndexOf('.'));
					//得到类全名 比如org.gof.entity.User
					String className = packageName + "." + classSimpleName;

					//忽略类
					if(ignoreClass.contains(className)) continue;
					
					//得到类class信息
					Class<?> clazz = Class.forName(className);
					
					//记录返回值
					result.add(clazz);
				}
			}
			
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 将从配置文件中读取的形如org.gof转成org/gof路径格式
	 * @param packageName
	 * @return
	 */
	public static String packageToPath(String packageName) {
		return packageName.replaceAll("\\.", "/");
	}
	
	/**
	 * 忽略的类 有些类Class.forName()时会报错或有其他问题
	 * @param ignoreClass
	 */
	public static void ignoreClass(List<Class<?>> ignoreClass) {
		PackageClass.ignoreClass.clear();
		for(Class<?> c : ignoreClass) {
			PackageClass.ignoreClass.add(c.getName());
		}
	}
	
	/**
	 * 获取符合包限制的文件
	 * @return
	 */
	private static List<File> findFiles(String packageName) {
		List<File> result = new ArrayList<>();
		for(String path : PATHS) {
			//包对应的文件目录
			File dir = new File(path, packageToPath(packageName));
			
			//从指定的目录中查找目录和以.class结尾的文件
			File[] files = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory()) return true;
					return pathname.getName().matches(".*\\.class$");
				}
			});
			if(files == null) files = new File[0];
			
			//记录本地址下符合的类文件
			for(File f : files) {
				result.add(f);
			}
		}
		
		return result;
	}
	
	/**
	 * 初始化类根目录地址
	 * @return
	 */
	private static void initPaths() {
		try {
			String pathStr = System.getProperty("java.class.path");
			if(Sys.isWin()) {
				PATHS = pathStr.split(";");
			} else {
				PATHS = pathStr.split(":");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
	}
}