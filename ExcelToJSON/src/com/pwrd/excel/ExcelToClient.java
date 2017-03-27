package com.pwrd.excel;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextArea;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class ExcelToClient extends ExcelTo
{
  private String pathASFile;

  public ExcelToClient(String pathConfFile, String pathExportFile, String pathFreemarker, JTextArea showMsg, String entityPackageName)
  {
    super(pathConfFile, pathExportFile, pathFreemarker, showMsg, "c", 
      entityPackageName);
  }

  public void cleanGeneratedFile()
  {
    super.showMsgFront("√清理已生成的<客户端>数据\n");
    this.pathASFile = (this.pathExportFile + "file/");
    try
    {
      cleanFileBySuffix(".json", this.pathExportFile);
      cleanFileBySuffix(".as", this.pathASFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void startGenProcess(String fileName, String sheetName)
  {
    try
    {
      genASFile(fileName, sheetName);
      fileName = "Conf" + fileName;

      genJSONFile(this.pathExportFile, fileName, this.resultsClient);
    }
    catch (Exception e)
    {
      cleanContainer();
      e.printStackTrace();
    }
  }

  private void genASFile(String entityName, String entityNameCN)
    throws Exception
  {
    Configuration cfg = new Configuration();

    cfg.setDirectoryForTemplateLoading(new File(this.pathFreemarkerTmpl));

    cfg.setObjectWrapper(new DefaultObjectWrapper());
    cfg.setEncoding(Locale.getDefault(), "UTF-8");

    Template temp = cfg.getTemplate("ExcelToAS.ftl");
    Map<String, Object> root = new HashMap<String, Object>();
    root.put("entityName", entityName);
    root.put("excelFileName", this.excelFileName);
    root.put("entityNameCN", entityNameCN);

    Set<Map<String, String>> properties = new HashSet<Map<String, String>>();
    root.put("properties", properties);

    if (this.clientInfos.isEmpty()) return;

    for (Map.Entry<String, Map<String, String>> entity : this.clientInfos.entrySet())
    {
      properties.add(entity.getValue());
    }

    super.writeFile(this.pathASFile, entityName + "VO", root, temp, ".as", "UTF-8");
  }

  protected void finalWork()
  {
  }
}