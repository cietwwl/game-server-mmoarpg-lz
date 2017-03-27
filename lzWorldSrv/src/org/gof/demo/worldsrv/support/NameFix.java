package org.gof.demo.worldsrv.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gof.core.support.RandomUtils;
import org.gof.core.support.SysException;
import org.gof.core.support.Utils;
import org.gof.demo.worldsrv.support.pathFinding.PathFinding;

/**
 * 名字处理相关类，包含起名和屏蔽字库
 */
public class NameFix {
	
	// 文件的路径
	private static final String NAMEFIX_DIR = PathFinding.class.getResource("/").getPath() + "META-INF/nameFix/";
	
	// 屏蔽字文件名
	private static final String SHIELD_FILE_NAME = "shield.txt";
	// 屏蔽字库
	private static Set<String> SHIELD_SET = new HashSet<String>();
	
	// FirstName
	private static final String FIRST_NAME_FILE_NAME = "firstName.txt";
	// FirstName库
	private static Map<Integer, String> FIRST_NAME_MAP = new HashMap<Integer, String>();
	
	// FamilyName
	private static final String FAMILY_NAME_FILE_NAME = "familyName.txt";
	// FamilyName库
	private static Map<Integer, String> FAMILY_NAME_MAP = new HashMap<Integer, String>();
	
	static {
		initShield();
		initName();
	}
	
	/**
	 * 启动时加载屏蔽字库
	 */
	private static void initShield() {
		// 读取文件
		File file = new File(NAMEFIX_DIR + SHIELD_FILE_NAME);
		
		//文件为空直接返回
		if(!file.exists())	return;
		
		// 挨个文件遍历，放入到Set中
		BufferedReader br = null;
		
		try {
			//读取文件流
			br = new BufferedReader(new FileReader(file));
			
			String t;
			while((t = br.readLine()) != null ) {
				SHIELD_SET.add(t);
			}
				
		} catch (IOException e) {
			throw new SysException(Utils.createStr("读取文件错误。文件:{}。", SHIELD_FILE_NAME));
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				throw new SysException(e);
			}
		}
	}
	
	/**
	 * 启动时加载屏蔽字库
	 */
	private static void initName() {
		// 读取文件
		File fileFirstName = new File(NAMEFIX_DIR + FIRST_NAME_FILE_NAME);
		File fileFamilyName = new File(NAMEFIX_DIR + FAMILY_NAME_FILE_NAME);
		
		//文件为空直接返回
		if(!fileFirstName.exists() || !fileFamilyName.exists())	return;
		
		// 挨个文件遍历，放入到Set中
		BufferedReader br = null;
		BufferedReader br2 = null;
		
		try {
			String t;
			
			//读取文件流：名字
			br = new BufferedReader(new FileReader(fileFirstName));
			int i = 0;
			while((t = br.readLine()) != null ) {
				FIRST_NAME_MAP.put(i, t);
				i++;
			}
			
			//读取文件流：姓
			br2 = new BufferedReader(new FileReader(fileFamilyName));
			i = 0;
			while((t = br2.readLine()) != null ) {
				FAMILY_NAME_MAP.put(i, t);
				i++;
			}
				
		} catch (IOException e) {
			throw new SysException("读取名字文件错误");
		} finally {
			try {
				br.close();
				br2.close();
			} catch (IOException e) {
				throw new SysException(e);
			}
		}
	}
	
	/**
	 * 随机获取一个名字
	 */
	public static String randomName() {
		//如果为空说明有异常
		if(FIRST_NAME_MAP.isEmpty() || FAMILY_NAME_MAP.isEmpty()) {
			throw new SysException("读取名字文件错误");
		}
		
		//随机起一个姓
		int familyRandom = RandomUtils.nextInt(FAMILY_NAME_MAP.size());
		String familyName = FAMILY_NAME_MAP.get(familyRandom);
		
		//随机起一个名字
		int firstRandom = RandomUtils.nextInt(FIRST_NAME_MAP.size());
		String fistName = FIRST_NAME_MAP.get(firstRandom);
		
		return familyName + fistName;
	}
	
	/**
	 * 判断是否有屏蔽字
	 * 
	 * @param s 
	 * @return 如果为空就说明没问题
	 */
	public static String shield(String str) {
		String result = null;
		
		for (String shieldStr : SHIELD_SET) {
			if(str.contains(shieldStr)) {
				result = shieldStr;
				break;
			}
		}
				
		return result;
	}
	
}
