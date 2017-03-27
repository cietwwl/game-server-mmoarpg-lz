package com.pwrd.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextArea;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class ExcelToPhone extends ExcelTo
{
  public ExcelToPhone(String pathConfFile, String pathExportFile, String pathFreemarker, JTextArea showMsg, String entityPackageName)
  {
    super(pathConfFile, pathExportFile, pathFreemarker, showMsg, "c", 
      entityPackageName);
  }

  public void cleanGeneratedFile()
  {
    showMsgFront("√清理已生成的<手机端>数据\n");
    try {
      cleanFileBySuffix(".cpp", this.pathExportFile);
      cleanFileBySuffix(".h", this.pathExportFile);
      cleanFileBySuffix(".json", this.pathExportFile + "/json/");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void startGenProcess(String fileName, String sheetName)
  {
    try
    {
      fileName = "Conf" + fileName;
      genJSONFile(this.pathExportFile + "/json/", fileName, this.resultsClient);
      genPhoneConfEntity(fileName, sheetName);
    } catch (Exception e) {
      super.cleanContainer();
      e.printStackTrace();
    }
  }

  private void genPhoneConfEntity(String entityName, String entityNameCN)
    throws Exception
  {
    if ((entityNameCN == null) || (entityNameCN.equals(""))) {
      throw new Exception("执行中断!非法Sheet名称  <" + entityName + ">");
    }
    Configuration cfg = new Configuration();

    cfg.setDirectoryForTemplateLoading(new File(this.pathFreemarkerTmpl));

    cfg.setObjectWrapper(new DefaultObjectWrapper());
    cfg.setEncoding(Locale.getDefault(), "UTF-8");

    Template tempCPP = cfg.getTemplate("ExcelToCPP.ftl");
    Template tempH = cfg.getTemplate("ExcelToH.ftl");

    Map<String, Object> root = new HashMap<String, Object>();
    root.put("className", entityName);
    root.put("idType", convertTypeToCPlus(this.idType));

    Set<Map<String, String>> properties = new HashSet<Map<String, String>>();
    root.put("properties", properties);
    root.put("excelFileName", this.excelFileName);

    if (this.clientInfos.isEmpty()) return;

    for (Map.Entry<String, Map<String, String>> entity : this.clientInfos.entrySet()) {
      for (Map.Entry<String, String> en : entity.getValue().entrySet())
      {
        if (((String)en.getKey()).equals("type")) {
          en.setValue(convertTypeToCPlus((String)en.getValue()));
        }
      }

      properties.add(entity.getValue());
    }
    writeFile(this.pathExportFile, entityName, root, tempCPP, ".cpp", "GBK");
    writeFile(this.pathExportFile, entityName, root, tempH, ".h", "GBK");
  }

  private String convertTypeToCPlus(String type)
  {
    if (type.equals("boolean"))
      return "bool";
    if ((type.equals("long")) || (type.equals("Integer"))) {
      return "int";
    }

    return type;
  }

  protected void finalWork()
  {
    try
    {
      super.showMsg("\n\n开始组合已生成的文件", true, false);
      processToOneFile();
      super.showMsg("\n文件组合结束，可以拷贝数据文件", true, false);
    } catch (Exception e) {
      e.printStackTrace();
      super.showMsg("合成文件时发生错误!", true, false);
    }
  }

  private void processToOneFile()
    throws Exception
  {
    File file = new File(this.pathExportFile);
    File[] files = file.listFiles();
    List<String> cppList = new ArrayList<String>();
    List<String> hList = new ArrayList<String>();
    for (File f : files) {
      if (f.getName().contains(".cpp"))
        cppList.add(readFile(f));
      if (f.getName().contains(".h")) {
        hList.add(readFile(f));
      }
    }
    cleanFileBySuffix(".cpp", this.pathExportFile);
    cleanFileBySuffix(".h", this.pathExportFile);

    genFinalFile(cppList, ".cpp");
    genFinalFile(hList, ".h");
  }

  private String readFile(File file)
  {
    String fileContent = "";
    BufferedReader reader = null;
    try {
      String tempString = "";
      reader = new BufferedReader(new FileReader(file));
      while ((tempString = reader.readLine()) != null) {
        fileContent = fileContent + tempString + "\n";
      }
      reader.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

    return fileContent;
  }

  private void genFinalFile(List<String> list, String type)
    throws Exception
  {
    Configuration cfg = new Configuration();

    cfg.setDirectoryForTemplateLoading(new File(this.pathFreemarkerTmpl));

    cfg.setObjectWrapper(new DefaultObjectWrapper());
    cfg.setEncoding(Locale.getDefault(), "UTF-8");

    Template temp = null;
    if (type.equals(".cpp"))
      temp = cfg.getTemplate("ExcelToCPPFile.ftl");
    else {
      temp = cfg.getTemplate("ExcelToHFile.ftl");
    }
    Map<String, Object> root = new HashMap<String, Object>();
    root.put("fileContent", list);

    writeFile(this.pathExportFile, "JsonData", root, temp, type, "GBK");
  }
}