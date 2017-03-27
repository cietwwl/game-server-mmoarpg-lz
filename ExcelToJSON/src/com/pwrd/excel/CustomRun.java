package com.pwrd.excel;

import java.io.File;

import javax.swing.JTextArea;

public class CustomRun extends Thread{

	public String runType;
	public JTextArea showMsg;
	public File[] files;
	public String pathConfFile;			//配置路径
	public String pathExportFile;		//导出文件路径
	public String pathFreemarker;		//freemarker末班路径
	public String entityPackageName;	//java包名
	
	/**
	 * 按照 runType 类型生成相应的数据 
	 * @param runType
	 * @param showMsg
	 * @param files
	 * @param pathConfFile
	 * @param pathExportFile
	 * @param pathFreemarker
	 * @param entityPackageName
	 */
	public CustomRun(String runType,JTextArea showMsg, File[] files, String pathConfFile, String pathExportFile,
			String pathFreemarker, String entityPackageName) {
		this.runType = runType;
		this.showMsg = showMsg;
		this.files = files;
		this.pathConfFile = pathConfFile;
		this.pathExportFile = pathExportFile;
		this.pathFreemarker = pathFreemarker;
		this.entityPackageName = entityPackageName;
	}
	
	@Override
	public void run() {

		//客户端
		if(runType.equals("client")) {
			showMsg.append("生成客户端文件\n");
			ExcelTo et = new ExcelToClient(pathConfFile, pathExportFile,
					pathFreemarker, showMsg, entityPackageName);
			et.cleanGeneratedFile();
			et.processExcel(files);
		}
		//服务器端
		if(runType.equals("server")) {
			showMsg.append("\n\n生成服务器文件\n");
			ExcelTo et = new ExcelToServer(pathConfFile, pathExportFile,
					pathFreemarker, showMsg, entityPackageName);
			et.cleanGeneratedFile();
			et.processExcel(files);
			
		}
		//手机端
		if(runType.equals("phone")) {
			showMsg.append("\n\n生成手机端文件\n");
			ExcelTo et = new ExcelToPhone(pathConfFile, pathExportFile,
					pathFreemarker, showMsg, entityPackageName);
			et.cleanGeneratedFile();
			et.processExcel(files);
		}
	}
}
