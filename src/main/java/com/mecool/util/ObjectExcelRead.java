package com.mecool.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * 从EXCEL导入到数据库
 * 创建人：FH 创建时间：2014年12月23日
 * @version
 */
public class ObjectExcelRead {

	/**
	 * @param filepath //文件路径
	 * @param filename //文件名
	 * @param startrow //开始行号
	 * @param startcol //开始列号
	 * @param sheetnum //sheet
	 * @return list
	 */
	public static List<Object> readExcel(String filepath, String filename, int startrow, int startcol, int sheetnum) {
		List<Object> varList = new ArrayList<Object>();

		try {
			File target = new File(filepath, filename);
			FileInputStream fi = new FileInputStream(target);
			String str = target.getName().substring(target.getName().lastIndexOf("."));
			Workbook xw = null;
			if (str.equals(".xls")){
				xw = new HSSFWorkbook(fi);
			}else if (str.equals(".xlsx")){
				xw = new XSSFWorkbook(fi);
			}
			Sheet sheet = xw.getSheetAt(sheetnum);
			int rowNum = sheet.getLastRowNum() + 1; 					//取得最后一行的行号
			for (int i = startrow; i < rowNum; i++) {					//行循环开始
				PageData varpd = new PageData();
				Row row = sheet.getRow(i);//行
				if (null==row){

				}else {
					int cellNum = row.getLastCellNum(); 					//每行的最后一个单元格位置
					int count  = 0;
					for (int j = startcol; j < cellNum; j++) {				//列循环开始
						Cell cell = row.getCell(Short.parseShort(j + ""));
						String cellValue = null;
						if (null != cell) {
							switch (cell.getCellType()) {				// 判断excel单元格内容的格式，并对其进行转换，以便插入数据库
								case 0:
									if (HSSFDateUtil.isCellDateFormatted(cell)) {
										try {
											cellValue = new SimpleDateFormat("yyyy/MM/dd").format(cell.getDateCellValue());
										} catch (Exception e) {
											cellValue = new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
										}
									}else{
										cellValue = String.valueOf((int) cell.getNumericCellValue());
										if (cellValue.equals("")){
											count++;
										}
									}
									break;
								case 1:
									cellValue = cell.getStringCellValue();
									if (cellValue.equals("")){
										count++;
									}
									break;
								case 2:
									cellValue = cell.getNumericCellValue() + "";
									if (cellValue.equals("")){
										count++;
									}
									// cellValue = String.valueOf(cell.getDateCellValue());
									break;
								case 3:
									cellValue = "";
									if (cellValue.equals("")){
										count++;
									}
									break;
								case 4:
									cellValue = String.valueOf(cell.getBooleanCellValue());
									break;
								case 5:
									cellValue = String.valueOf(cell.getErrorCellValue());
									break;
							}
						} else {
							cellValue = "";
							if (cellValue.equals("")){
								count++;
							}
						}
						varpd.put("var"+j, cellValue);
					}
					if (count!=cellNum){
						varList.add(varpd);
					}
				}


			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return varList;
	}
}
