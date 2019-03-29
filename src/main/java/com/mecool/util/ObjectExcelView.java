package com.mecool.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ObjectExcelView extends AbstractXlsView {

//	@Override
//	protected void buildExcelDocument(Map<String, Object> model,
//									  HSSFWorkbook workbook, HttpServletRequest request,
//									  HttpServletResponse response) throws Exception {
//		// TODO Auto-generated method stub
//		Date date = new Date();
//		String filename = Tools.date2Str(date, "yyyyMMddHHmmss");
//		HSSFSheet sheet;
//		HSSFCell cell;
//		response.setContentType("application/octet-stream");
//		response.setHeader("Content-Disposition", "attachment;filename="+filename+".xls");
//		sheet = workbook.createSheet("sheet1");
//
//		List<String> titles = (List<String>) model.get("titles");
//		int len = titles.size();
//		HSSFCellStyle headerStyle = workbook.createCellStyle(); //标题样式
//		headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
//		headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
//		HSSFFont headerFont = workbook.createFont();	//标题字体
//		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
//		headerFont.setFontHeightInPoints((short)11);
//		headerStyle.setFont(headerFont);
//		short width = 20,height=25*20;
//		sheet.setDefaultColumnWidth(width);
//		for(int i=0; i<len; i++){ //设置标题
//			String title = titles.get(i);
//			cell = getCell(sheet, 0, i);
//			cell.setCellStyle(headerStyle);
//			setText(cell,title);
//		}
//		sheet.getRow(0).setHeight(height);
//
//		HSSFCellStyle contentStyle = workbook.createCellStyle(); //内容样式
//		contentStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
//		List<PageData> varList = (List<PageData>) model.get("varList");
//		int varCount = varList.size();
//		for(int i=0; i<varCount; i++){
//			PageData vpd = varList.get(i);
//			for(int j=0;j<len;j++){
//				String varstr = vpd.get("var"+(j+1)) != null ? vpd.get("var"+(j+1))+"" : "";
//				cell = getCell(sheet, i+1, j);
//				cell.setCellStyle(contentStyle);
//				setText(cell,varstr);
//			}
//		}
//
//	}
	protected Cell getCell(Sheet sheet, int row, int col) {
		Row sheetRow = sheet.getRow(row);
		if (sheetRow == null) {
			sheetRow = sheet.createRow(row);
		}

		Cell cell = sheetRow.getCell(col);
		if (cell == null) {
			cell = sheetRow.createCell(col);
		}

		return cell;
	}

	protected void setText(Cell cell, String text) {
		cell.setCellType(1);
		cell.setCellValue(text);
	}


	@Override
	protected void buildExcelDocument(Map<String, Object> map, Workbook workbook, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
		// TODO Auto-generated method stub
		Date date = new Date();
		String filename = Tools.date2Str(date, "yyyyMMddHHmmss");
		Sheet sheet;
		Cell cell;
		httpServletResponse.setContentType("application/octet-stream");
		httpServletResponse.setHeader("Content-Disposition", "attachment;filename="+filename+".xls");
		sheet = workbook.createSheet("sheet1");
		List<String> titles = (List<String>) map.get("titles");
		int len = titles.size();
		CellStyle headerStyle = workbook.createCellStyle(); //标题样式
		headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		Font headerFont = workbook.createFont();	//标题字体
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headerFont.setFontHeightInPoints((short)11);
		headerStyle.setFont(headerFont);
		short width = 20,height=25*20;
		sheet.setDefaultColumnWidth(width);
		for(int i=0; i<len; i++){ //设置标题
			String title = titles.get(i);
			cell = getCell(sheet, 0, i);
			cell.setCellStyle(headerStyle);
			setText(cell,title);
		}
		sheet.getRow(0).setHeight(height);

		CellStyle contentStyle = workbook.createCellStyle(); //内容样式
		contentStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		List<PageData> varList = (List<PageData>) map.get("varList");
		int varCount = varList.size();
		for(int i=0; i<varCount; i++){
			PageData vpd = varList.get(i);
			for(int j=0;j<len;j++){
				String varstr = vpd.get("var"+(j+1)) != null ? vpd.get("var"+(j+1))+"" : "";
				cell = getCell(sheet, i+1, j);
				cell.setCellStyle(contentStyle);
				setText(cell,varstr);
			}
		}
	}
}
