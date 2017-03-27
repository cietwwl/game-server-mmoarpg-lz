package ${packageName};

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pwrd.core.support.ConfigJSON;

/**
 * ${entityNameCN}
 * @author System
 * 此类是系统自动生成类 不要直接修改，修改后也会被覆盖
 */
@ConfigJSON
public class ${entityName} {
	
	private static final Map<${idType}, ${entityName}> datas = new HashMap<>();
	private static final List<${entityName}> values = new ArrayList<>();

	public static ${entityName} getDataBySN(${idType} SN) {
		return datas.get(SN);
	}
	
	public static List<${entityName}> getValues() {
		return values;
	}
	
	<#-- 字段 -->
	<#list properties as prop>
	public final ${prop.type} ${prop.name};				//${prop.note}
    </#list>

	<#-- 构造方法 -->
	public ${entityName}(${paramMethod}) {
	<#list properties as prop>
		this.${prop.name} = ${prop.name};			
    </#list>
	}
	
	<#-- 初始化 -->
	public static void init() {
		String confJson = ${entityName}.readConfFile();
		
		JSONArray confs = (JSONArray)JSONArray.parse(confJson);
		for(int i = 0 ; i < confs.size() ; i++){
			JSONObject conf = confs.getJSONObject(i);
			${entityName} object = new ${entityName}(${paramInit});
			values.add(object);
			datas.put(conf.get${idType}("sn"), object);
		}
	}
	
	<#-- 读取游戏配置 -->
	private static String readConfFile() {
		String baseBath = ${entityName}.class.getResource("").getPath();
        File file = new File(baseBath + "json/${entityName}.json");
       
        String result = "";
        
        try ( BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))){
            String tempString = "";
            <#-- 一次读一行 -->
            while ((tempString = reader.readLine()) != null) {
            	result += tempString;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } 
        
        return result;
    }
    
}