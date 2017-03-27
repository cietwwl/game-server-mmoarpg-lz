package com.pwrd.excel;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JTextArea;

import com.pwrd.excel.ExcelTo;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class ExcelToServer extends ExcelTo
{
  public ExcelToServer(String pathConfFile, String pathExportFile, String pathFreemarker, JTextArea showMsg, String entityPackageName)
  {
    super(pathConfFile, pathExportFile, pathFreemarker, showMsg, "s", 
      entityPackageName);
  }

  public void cleanGeneratedFile()
  {
    super.showMsgFront("√清理已生成的<服务器端>数据\n");
    try {
      super.cleanFileBySuffix(".json", getJsonPath());

      super.cleanFileBySuffix(".java", getEntityPath());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void startGenProcess(String fileName, String sheetName)
  {
    if ((this.entityPackageName == null) || (this.entityPackageName.equals(""))) {
      super.showMsgFront("☢ java包名没有设置 \n");
      return;
    }
    try {
      fileName = "Conf" + fileName;
      genJSONFile(getJsonPath(), fileName, this.resultsServer);

      genJavaConfEntity(fileName, sheetName);
    }
    catch (Exception e) {
      cleanContainer();
      e.printStackTrace();
    }
  }

  private void genJavaConfEntity(String entityName, String entityNameCN)
    throws Exception
  {
    if ((entityNameCN == null) || (entityNameCN.equals(""))) {
      super.showMsgFront("执行中断!非法Sheet名称  <" + entityName + ">");
      return;
    }

    super.checkFieldNameRepeat(this.entityInfos);

    Configuration cfg = new Configuration();

    cfg.setDirectoryForTemplateLoading(new File(this.pathFreemarkerTmpl));

    cfg.setObjectWrapper(new DefaultObjectWrapper());
    cfg.setEncoding(Locale.getDefault(), "UTF-8");

    Template temp = cfg.getTemplate("ExcelToJava.ftl");

    Map<String, Object> root = new HashMap<String, Object>();
    root.put("entityName", entityName);
    root.put("excelFileName", this.excelFileName);
    root.put("packageName", this.entityPackageName);
    root.put("entityNameCN", entityNameCN);
    root.put("idType", this.idType);

    Set<Object> properties = new LinkedHashSet<Object>();
    root.put("properties", properties);

    String paramMethod = "";
    String paramInit = "";

    if (this.entityInfos.isEmpty()) return;

    int records = 0;

    for (Entry<String, Map<String, String>> entity : this.entityInfos.entrySet())
    {
      properties.add(entity.getValue());

      paramMethod = paramMethod + (String)(entity.getValue()).get("type") + " " + (String)(entity.getValue()).get("name");

      String type = (String)(entity.getValue()).get("type");
      if (type.equalsIgnoreCase("int"))
        paramInit = paramInit + "conf.getIntValue(\"" + (String)(entity.getValue()).get("name") + "\")";
      else if (type.equalsIgnoreCase("boolean"))
        paramInit = paramInit + "conf.getBooleanValue(\"" + (String)(entity.getValue()).get("name") + "\")";
      else if (type.equalsIgnoreCase("double"))
        paramInit = paramInit + "conf.getDoubleValue(\"" + (String)(entity.getValue()).get("name") + "\")";
      else if (type.equalsIgnoreCase("long"))
        paramInit = paramInit + "conf.getLongValue(\"" + (String)(entity.getValue()).get("name") + "\")";
      else if (type.equalsIgnoreCase("float"))
        paramInit = paramInit + "conf.getFloatValue(\"" + (String)(entity.getValue()).get("name") + "\")";
      else if (type.equalsIgnoreCase("float[]"))
        paramInit = paramInit + "parseFloatArray(conf.getString(\"" + (String)(entity.getValue()).get("name") + "\"))";
      else if (type.equalsIgnoreCase("double[]"))
        paramInit = paramInit + "parseDoubleArray(conf.getString(\"" + (String)(entity.getValue()).get("name") + "\"))";
      else if (type.equalsIgnoreCase("String[]"))
        paramInit = paramInit + "parseStringArray(conf.getString(\"" + (String)(entity.getValue()).get("name") + "\"))";
      else if (type.equalsIgnoreCase("int[]"))
        paramInit = paramInit + "parseIntArray(conf.getString(\"" + (String)(entity.getValue()).get("name") + "\"))";
      else if (type.equalsIgnoreCase("boolean[]"))
        paramInit = paramInit + "parseBoolArray(conf.getString(\"" + (String)(entity.getValue()).get("name") + "\"))";
      else if (type.equalsIgnoreCase("long[]"))
        paramInit = paramInit + "parseLongArray(conf.getString(\"" + (String)(entity.getValue()).get("name") + "\"))";
      else {
        paramInit = paramInit + "conf.getString(\"" + (String)(entity.getValue()).get("name") + "\")";
      }

      records++;
      if (records != this.entityInfos.size()) {
        paramMethod = paramMethod + ", ";
        paramInit = paramInit + ", ";
      }

      if ((records % 4 == 0) && (records != this.entityInfos.size())) {
        paramInit = paramInit + "\n\t\t\t\t";
      }

    }

    root.put("paramMethod", paramMethod);
    root.put("paramInit", paramInit);

    writeFile(getEntityPath(), entityName, root, temp, ".java", "UTF-8");
  }

  private String getEntityPath() {
    String newPath = "";
    if (this.pathExportFile.contains("web"))
      newPath = this.pathExportFile.replace("web/", "");
    else
      newPath = this.pathExportFile.replace("phone/", "");
    return newPath;
  }

  private String getJsonPath() {
    String newPath = this.pathExportFile + "/json/";

    return newPath;
  }

  protected void finalWork()
  {
  }
}