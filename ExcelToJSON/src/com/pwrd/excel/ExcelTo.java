package com.pwrd.excel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import freemarker.template.Template;

public abstract class ExcelTo extends Thread {
	private int flagCS = 0;
	private int flagType = 1;
	private int flagNameEN = 2;
	private int flagNameCN = 3;
	public String pathConfFile;
	public String pathExportFile;
	public String pathFreemarkerTmpl;
	public String entityPackageName;
	public JTextArea showMsg;
	public static List<String> errorFiles = new ArrayList<String>();
	public String excelFileName;
	public String idType = "String";

	List<Map<Object, Object>> resultsServer = new ArrayList<Map<Object, Object>>();
	List<Map<Object, Object>> resultsClient = new ArrayList<Map<Object, Object>>();

	Map<String, Map<String, String>> entityInfos = new LinkedHashMap<String, Map<String, String>>();
	Map<String, Map<String, String>> clientInfos = new LinkedHashMap<String, Map<String, String>>();
	List<Object> ids = new ArrayList<Object>();
	public String CSSign;
	public boolean escap = false;

	public List<String> fieldNames = new ArrayList<String>();

	int countConfFile = 0;

	public void run() {
		init();
		readExcel();
		finalWork();
	}

	protected abstract void finalWork();

	public void init() {
		this.countConfFile = 0;
		this.ids.clear();
		if ((this.CSSign == null) || ("".equals(this.CSSign))) {
			showMsgBack("☢ 没有设置文件生成类型(服务器/客户端)");
		}
		if ((this.pathConfFile == null) || ("".equals(this.pathConfFile))) {
			showMsgFront("☢ 游戏配置文件的路径没有设置");
			return;
		}
		if ((this.pathExportFile == null) || ("".equals(this.pathExportFile))) {
			showMsgFront("☢ 文件生成目录没有设置");
			return;
		}
		if ((this.pathFreemarkerTmpl == null)
				|| ("".equals(this.pathFreemarkerTmpl))) {
			showMsgFront("☢ 生成文件模板路径没有设置");
			return;
		}

		errorFiles = new ArrayList<String>();

		cleanGeneratedFile();
		this.showMsg.requestFocus();
	}

	public ExcelTo(String pathConfFile, String pathExportFile, String pathFreemarker, JTextArea showMsg, String CSSign, String entityPackageName) {
		this.pathConfFile = pathConfFile;
		this.pathExportFile = pathExportFile;
		this.pathFreemarkerTmpl = pathFreemarker;
		this.showMsg = showMsg;
		this.CSSign = CSSign;
		this.entityPackageName = entityPackageName;
	}

	public void readExcel() {
		File f = new File(this.pathConfFile);
		if (!f.isDirectory()) {
			showMsgFront("☢ 游戏配置路径设置错误:" + this.pathConfFile);
			return;
		}

		File[] files = f.listFiles();

		processExcel(files);
	}

	public void processExcel(File[] files) {
		showMsgFront("发现文件 " + files.length + " 个 \n");

		for (int i = 0; i < files.length; i++) {
			if (this.escap)
				break;
			String filePathName = files[i].getPath();

			filePathName = processPath(filePathName);

			if ((!filePathName.endsWith(".xls")) && (!filePathName.endsWith(".xlsx")) && (!filePathName.endsWith("xlsm"))) {
				showMsgBack("☢非法配置文件，忽略处理! " + filePathName + " \n");
			} else if (filePathName.indexOf("~$") != -1) {
				showMsgBack("☢临时文件，忽略处理! " + filePathName + " \n");
			} else {
				this.excelFileName = files[i].getName();
				showMsg("开始处理: <" + filePathName + ">\n", true, true);

				this.countConfFile += 1;

				processWorkBook(filePathName);
			}
		}
		showMsgFront("√处理完毕,处理配置文件 " + this.countConfFile + " 个");
	}

	public void processWorkBook(String excelFile) {
		try {
			XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream( new File(excelFile)));

			Map<String, List<String>> map = processSameSheet(workbook);
			if (map.isEmpty()) {
				showMsgBack("表中没有任何有效数据, 忽略处理!");
				return;
			}

			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String fileName = (String) entry.getKey();
				String sheetName = "";

				for (String str : entry.getValue()) {
					XSSFSheet sheet = workbook.getSheet(str);
					sheetName = sheetName + sheet.getSheetName() + " ";

					processSheet(sheet, excelFile);
				}

				startGenProcess(fileName, sheetName);
				cleanContainer();
			}

		} catch (Exception e) {
			e.printStackTrace();
			showMsgFront("☢处理<" + excelFile + ">时异常，原因: " + e.getMessage() + "    ->右键打开错误文件\n");
			addErrorFile(excelFile);

			cleanContainer();
		}
	}

	private void processSheet(XSSFSheet sheet, String excelFile) throws Exception {
		this.fieldNames.clear();

		int totalCount = sheet.getLastRowNum();

		if (totalCount == 0) {
			throw new Exception("☢sheet:" + sheet.getSheetName() + ", 没有数据,请检查\n");
		}

		int totalColume = sheet.getRow(0).getLastCellNum();

		for (int row = this.flagNameCN; row <= totalCount; row++) {
			if (this.escap)
				break;
			Map<Object, Object> serverMap = new LinkedHashMap<Object, Object>();

			Map<Object, Object> clientMap = new LinkedHashMap<Object, Object>();

			for (int colume = 0; colume < totalColume; colume++) {
				if (this.escap)
					break;
				Map<String, String> infoMap = new LinkedHashMap<String, String>();
				try {
					XSSFCell cellSOrC = sheet.getRow(this.flagCS).getCell(colume);
					if ((cellSOrC != null) && (cellSOrC.getCellType() != 3)) {
						String serverOrClient = sheet.getRow(this.flagCS).getCell(colume).getStringCellValue().toLowerCase();

						if ((row <= this.flagNameCN) && (serverOrClient != null) && (!serverOrClient.equals(""))) {
							saveHeadInfo(sheet, infoMap, colume, serverOrClient);
						} else {
							XSSFCell cell = sheet.getRow(row).getCell(colume);
							String fieldNameEN;
							String dateType;
							try {
								dateType = sheet.getRow(this.flagType) .getCell(colume).getStringCellValue();
								fieldNameEN = sheet.getRow(this.flagNameEN).getCell(colume).getStringCellValue();
							} catch (Exception e) {
								
								throw new Exception("表头信息定义错误!");
							}
							if (colume == 0) {
								checkSnAndSaveType(cell, dateType);

								fieldNameEN = toSaveCase(fieldNameEN.toUpperCase());
							}

							Object value = processCellAndGetDate(sheet, cell, dateType);

							if ((serverOrClient.contains("c")) && (this.CSSign.equalsIgnoreCase("c"))) {
								convertDataByTypeAndSetIntoMap(clientMap, fieldNameEN, dateType, value);
							}

							if ((serverOrClient.contains("s")) && (this.CSSign.equalsIgnoreCase("s"))) {
								convertDataByTypeAndSetIntoMap(serverMap, fieldNameEN, dateType, value);
							}
						}
					}
				} catch (Exception e) {
					showMsgFront("☢sheet:" + sheet.getSheetName() + ", " + (row + 1) + " 行, " + convertNumToLetter(colume) + " 列, 数据异常,可能是：" + e.getMessage() + "   ->右键打开错误文件\n");
					addErrorFile(excelFile);
					e.printStackTrace();
				}
			}

			if (!serverMap.isEmpty())
				this.resultsServer.add(serverMap);
			if (!clientMap.isEmpty())
				this.resultsClient.add(clientMap);
		}
	}

	private Object processCellAndGetDate(XSSFSheet sheet, XSSFCell cell, String dateType)
			throws Exception {
		if ((cell == null) || (cell.getCellType() == 3)) {
			cell = setDefaultValue(sheet, dateType);
		}

		switch (cell.getCellType()) {
		case 0:
			return formatSciNot(cell.getNumericCellValue());
		case 1:
			String value = cell.getStringCellValue();

			if (dateType.equalsIgnoreCase("json")) {
				checkJSONFormat(value);
			}
			return value;
		case 4:
			if ((!dateType.equals("boolean")) && (!dateType.equals("String")))
				throw new Exception("☢数据类型不应当是Boolean类型");

			return Boolean.valueOf(cell.getBooleanCellValue());
		case 2:
			return checkDataForFormula(cell, dateType);
		case 3:
		}
		throw new Exception("☢未知数据类型！");
	}

	public XSSFCell setDefaultValue(XSSFSheet sheet, String dateType) {
		XSSFCell cell = sheet.createRow(16383).createCell(16383);
		if ((dateType.equalsIgnoreCase("int"))
				|| (dateType.equalsIgnoreCase("long"))
				|| (dateType.equalsIgnoreCase("float")))
			cell.setCellValue(0.0D);
		else if (dateType.equalsIgnoreCase("double"))
			cell.setCellValue(0.0D);
		else if (dateType.equalsIgnoreCase("boolean"))
			cell.setCellValue(false);
		else if (dateType.equalsIgnoreCase("json"))
			cell.setCellValue("{}");
		else {
			cell.setCellValue("");
		}

		return cell;
	}

	public void checkSnAndSaveType(XSSFCell cell, String dateType) throws Exception {
		if ((cell == null) || (cell.getCellType() == 3)) {
			throw new Exception("ID列不能为空!");
		}

		String sn = null;

		if (cell.getCellType() == 0) {
			sn = String.valueOf(cell.getNumericCellValue());
		} else if (cell.getCellType() == 2)
			sn = cell.getCellFormula();
		else {
			sn = cell.getStringCellValue();
		}

		if (this.ids.contains(sn))
			throw new Exception("ID中有重复的值: " + sn);
		if ("".equals(sn))
			throw new Exception("ID不能为空字符串！");
		this.ids.add(sn);

		this.idType = (dateType.equalsIgnoreCase("int") ? "Integer" : dateType);
	}

	public void saveHeadInfo(XSSFSheet sheet, Map<String, String> infoMap, int colume, String clientOrServer) throws Exception {
		String dateType = sheet.getRow(this.flagType).getCell(colume).getStringCellValue();

		infoMap.put("type", dateType.equalsIgnoreCase("json") ? "String" : dateType);

		String nameEn = sheet.getRow(this.flagNameEN).getCell(colume).getStringCellValue();

		if (colume == 0) {
			nameEn = toSaveCase(nameEn.toUpperCase());
		}
		infoMap.put("name", nameEn);

		String note = sheet.getRow(this.flagNameCN).getCell(colume).getStringCellValue();

		note = note.replaceAll("\n", "");
		infoMap.put("note", note);
		if (clientOrServer.contains("s")) {
			this.entityInfos.put(nameEn, infoMap);
		}

		if (clientOrServer.contains("c")) {
			this.clientInfos.put(nameEn, infoMap);
		}

		if (this.fieldNames.contains(nameEn.toLowerCase())) {
			throw new Exception("有重复定义字段 " + nameEn);
		}

		this.fieldNames.add(nameEn.toLowerCase());
	}

	public abstract void cleanGeneratedFile();

	public void showMsgFront(String content) {
		showMsg(content, true, false);
	}

	public void showMsgBack(String content) {
		showMsg(content, false, true);
	}

	public void showMsg(String content, boolean isShowFront, boolean isShowBack) {
		if ((this.showMsg != null) && (isShowFront)) {
			this.showMsg.append(content);

			scrollScreen();
		}

		if (isShowBack)
			System.out.println(content);
	}

	public void cleanFileBySuffix(String suffix, String filePath)
			throws Exception {
		File f = new File(filePath);
		String[] files = f.list();

		if ((f == null) || (!f.exists())) {
			throw new Exception("☢ 清理时未发现目录，稍后将会自动创建: " + filePath + "\n");
		}

		for (String fileName : files) {
			File file = new File(filePath + fileName);

			if (fileName.endsWith(suffix))
				file.delete();
		}
	}

	private String processPath(String path) {
		if (path == null)
			return path;
		int pos = path.indexOf("..\\");
		if (pos == -1)
			return path;

		String path1 = path.substring(0, pos - 1);
		path1 = path1.substring(0, path1.lastIndexOf("\\"));
		String path2 = path.substring(pos + 2, path.length());
		return path1 + path2;
	}

	public void scrollScreen() {
		if (this.showMsg != null)
			this.showMsg.setCaretPosition(this.showMsg.getText().length());
	}

	public Map<String, String> checkPrefix(XSSFSheet sheet) throws Exception {
		String name = sheet.getSheetName();
		Map<String, String> map = new HashMap<String, String>();

		if (name.contains("_")) {
			name = name.substring(0, name.indexOf("_"));
		}

		if (!name.contains("|")) {
			showMsgBack("☢sheet : " + sheet.getSheetName() + " , 非规则命名，忽略处理!");
			return map;
		}

		map.put("entityNameCN", name.substring(0, name.indexOf("|")));

		map.put("entityName",
				name.substring(name.indexOf("|") + 1, name.length()));

		return map;
	}

	@SuppressWarnings("unused")
	private boolean findChineseFromStr(String str) {
		Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}

	private String toSaveCase(String str) {
		return str.toLowerCase();
	}

	private void convertDataByTypeAndSetIntoMap(Map<Object, Object> map,
			String fieldNameEN, String dateType, Object value) throws Exception {
		if (dateType.equalsIgnoreCase("int")) {
			String valueStr = String.valueOf(value);
			if (valueStr.indexOf("0x") != -1) {
				map.put(fieldNameEN, Integer.decode(String.valueOf(value)));
			} else {
				if (Double.parseDouble(valueStr) > 2147483647.0D) {
					throw new Exception("int类型超出最大值!");
				}
				map.put(fieldNameEN,
						Integer.valueOf((int) Double.parseDouble(valueStr)));
			}
		} else if (dateType.equalsIgnoreCase("long")) {
			String valueStr = String.valueOf(value);
			map.put(fieldNameEN, Long.valueOf(Long.parseLong(valueStr)));
		} else if (dateType.equalsIgnoreCase("double")) {
			String valueStr = String.valueOf(value);
			map.put(fieldNameEN,
					Double.valueOf(new Double(valueStr).doubleValue()));
		} else if ((dateType.equalsIgnoreCase("String"))
				|| (dateType.equalsIgnoreCase("double[]"))
				|| (dateType.equalsIgnoreCase("int[]"))
				|| (dateType.equalsIgnoreCase("float[]"))
				|| (dateType.equalsIgnoreCase("string[]"))
				|| (dateType.equalsIgnoreCase("long[]"))
				|| (dateType.equalsIgnoreCase("boolean[]"))) {
			map.put(fieldNameEN, value);
		} else if (dateType.equalsIgnoreCase("json")) {
			map.put(fieldNameEN, convert0xData(value.toString()));
		} else if (dateType.equalsIgnoreCase("boolean")) {
			String valueStr = String.valueOf(value);
			if (valueStr.equals("0"))
				valueStr = "false";
			if (valueStr.equals("1"))
				valueStr = "true";
			if ((!valueStr.equalsIgnoreCase("true"))
					&& (!valueStr.equalsIgnoreCase("false"))) {
				throw new Exception("☢ 布尔类型的数据定义错误");
			}
			map.put(fieldNameEN, Boolean.valueOf(valueStr));
		} else if (dateType.equalsIgnoreCase("float")) {
			map.put(fieldNameEN, Float.valueOf((String) value));
		} else {
			throw new Exception("☢ 未知数据类型：" + dateType);
		}
	}

	private Object checkDataForFormula(XSSFCell cell, String dateType) {
		String value = "";
		try {
			value = cell.getStringCellValue();
		} catch (IllegalStateException localIllegalStateException) {
		}
		if (value.indexOf("0x") != -1)
			return Integer.decode(value);
		if (!"".equals(value)) {
			return value;
		}

		return formatSciNot(cell.getNumericCellValue());
	}

	private String formatSciNot(double value) {
		BigDecimal bd = new BigDecimal(value);

		return bd.toString();
	}

	private void checkJSONFormat(String json) throws Exception {
		if ((!json.contains("{")) && (!json.contains("["))) {
			throw new Exception("☢JSON数据验证失败!");
		}
		json = convert0xData(json);

		checkJsonParse(json);
	}

	private String convert0xData(String str) {
		if (!str.contains("0x"))
			return str;

		String[] tmp = str.split(",");

		for (String s : tmp) {
			String s1 = s.substring(s.indexOf("0x"), s.indexOf("0x") + 10);

			String s2 = String.valueOf(Integer.decode(s1));

			str = str.replaceAll(s1, s2);
		}

		return str;
	}

	private String convertNumToLetter(int num) {
		String[] letter = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
				"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
				"W", "X", "Y", "Z" };
		if (num > 25) {
			int tenPos = num / 26 - 1;
			int onePos = num % 26;
			return letter[tenPos] + letter[onePos];
		}
		return letter[num];
	}

	public void addErrorFile(String filePath) {
		if (!errorFiles.contains(filePath))
			errorFiles.add(filePath);
	}

	public abstract void startGenProcess(String paramString1,
			String paramString2);

	public void cleanContainer() {
		this.resultsServer.clear();
		this.resultsClient.clear();
		this.entityInfos.clear();
		this.clientInfos.clear();
		this.ids.clear();
	}

	public void writeFile(String filePath, String entityName, Map<String, Object> root, Template temp, String suffix, String code) throws Exception {
		File path = new File(filePath);
		if (!path.exists()) {
			showMsgBack("√目录创建成功: " + filePath + "\n");
			path.mkdirs();
		}

		String filePathNameJava = filePath + entityName + suffix;
		File file = new File(filePathNameJava);
		if (!file.exists())
			file.createNewFile();
		else {
			throw new Exception("☢生成时发生错误!重名文件存在:" + file.getAbsolutePath()
					+ "\n");
		}

		FileOutputStream writerStream = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				writerStream, code));
		temp.process(root, writer);
		writer.flush();
		writer.close();
	}

	public void genJSONFile(String filePath, String name,
			List<Map<Object, Object>> resultList) throws Exception {
		String filePathName = filePath + name + ".json";
		File file = new File(filePathName);

		File path = new File(filePath);
		if (!path.exists()) {
			showMsgBack("√目录创建成功: " + filePath + "\n");
			path.mkdirs();
		}

		if (!file.exists()) {
			showMsgBack("创建文件：" + filePathName);
			file.createNewFile();
		} else {
			throw new Exception("☢重名文件存在：" + filePathName + "\n");
		}

		FileOutputStream writerStream = new FileOutputStream(file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				writerStream, "UTF-8"));

		String jsonStr = JSON.toJSONString(resultList);
		writer.write(jsonStr);
		writer.close();
	}

	private Map<String, List<String>> processSameSheet(XSSFWorkbook workbook) {
		int totalSheet = workbook.getNumberOfSheets();

		Map<String, List<String>> map = new HashMap<String, List<String>>();

		for (int i = 0; i < totalSheet; i++) {
			if (this.escap)
				break;
			XSSFSheet sheet = workbook.getSheetAt(i);

			String name = sheet.getSheetName();

			if (!name.contains("|")) { 
				showMsgBack("☢sheet : " + sheet.getSheetName() + " , 非规则命名，忽略处理!");
			
			} else {
				String nameEN = name.substring(name.indexOf("|") + 1, name.length());

				if (map.get(nameEN) == null) {
					map.put(nameEN, new ArrayList<String>());
				}

				map.get(nameEN).add(name);
			}
		}
		return map;
	}

	private void checkJsonParse(String json) {
		try {
			JSONObject.parse(json);
		} catch (Exception e) {
			checkJsonParseToMap(json);
		}
	}

	private void checkJsonParseToMap(String json) {
		try {
			JSONObject.parseObject(json, Map.class);
		} catch (Exception e) {
			throw new RuntimeException("JSON格式错误！");
		}
	}

	protected void checkFieldNameRepeat(Map<String, Map<String, String>> map) {
		List<String> list = new ArrayList<String>();
		for (String key : map.keySet()) {
			String tmp = key.toLowerCase();

			if (list.contains(tmp)) {
				showMsgFront("☢配置中存在重复字段:" + key);
			}
			list.add(key.toLowerCase());
		}
	}
}