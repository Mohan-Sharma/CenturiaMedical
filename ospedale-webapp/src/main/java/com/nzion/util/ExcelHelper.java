package com.nzion.util;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Date;

/**
 * @author: NthDimenzion
 * @since 1.0
 */
public class ExcelHelper {


    public static XSSFWorkbook createWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        return workbook;
    }

    public static XSSFSheet createWorksheet(String workSheetName, XSSFWorkbook hssfWorkbook) {
        XSSFSheet workSheet = hssfWorkbook.createSheet(workSheetName);
        return workSheet;
    }

    public static Row createRow(int rowNumber, XSSFSheet hssfSheet) {
        Row row = hssfSheet.createRow(rowNumber);
        return row;
    }

    public static Cell createStringCell(int cellNumber, String cellValue, Row row,XSSFWorkbook workbook) {
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);

        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(cellValue);
        cell.setCellStyle(headerCellStyle);
        return cell;
    }


    public static Cell createNumberCell(int cellNumber, Number cellValue, Row row,XSSFWorkbook workbook) {
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        DataFormat format = workbook.createDataFormat();
        headerCellStyle.setDataFormat(format.getFormat("#,##0.000"));

        Cell cell = row.createCell(cellNumber);
        if (cellValue != null) {
            cell.setCellValue(cellValue.doubleValue());
        }
        cell.setCellStyle(headerCellStyle);
        return cell;
    }

    public static Cell createDateCell(int cellNumber, Date cellValue, Row row,XSSFWorkbook workbook) {
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        DataFormat format = workbook.createDataFormat();
        headerCellStyle.setDataFormat(format.getFormat("dd/mm/yyyy"));
        headerCellStyle.setAlignment(headerCellStyle.ALIGN_LEFT);
        Cell cell = row.createCell(cellNumber);
        if (cellValue != null) {
            cell.setCellValue(cellValue);
        }
        cell.setCellStyle(headerCellStyle);
        return cell;
    }
    /*
    * Modified by Mohan Sharma
    * */
    public static Cell createTimeCell(int cellNumber, Date cellValue, Row row,XSSFWorkbook workbook) {
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        DataFormat format = workbook.createDataFormat();
        headerCellStyle.setDataFormat(format.getFormat("h:mm AM/PM"));
        headerCellStyle.setAlignment(headerCellStyle.ALIGN_LEFT);
        Cell cell = row.createCell(cellNumber);
        if (cellValue != null) {
            cell.setCellValue(cellValue);
        }
        cell.setCellStyle(headerCellStyle);
        return cell;
    }

    public static Cell createHeaderCell(int cellNumber, String headerLabel, Row row, XSSFWorkbook workbook) {
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setWrapText(true);
        headerCellStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        headerCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);

        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(headerLabel);
        cell.setCellStyle(headerCellStyle);
        return cell;
    }

    public static Cell createNumberHeaderCell(int cellNumber, Number headerLabel, Row row, XSSFWorkbook workbook) {
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setWrapText(true);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);

        Cell cell = row.createCell(cellNumber);
        if (headerLabel != null) {
            cell.setCellValue(headerLabel.doubleValue());
        }
        cell.setCellStyle(headerCellStyle);
        return cell;


    }

    public static Cell createReportHeaderCell(int cellNumber, String headerLabel, Row row, XSSFWorkbook workbook) {
        XSSFFont headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setWrapText(true);
        headerCellStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        headerCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        headerCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
       /* headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
       */
        headerCellStyle.setAlignment(headerCellStyle.ALIGN_CENTER);
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(headerLabel);
        cell.setCellStyle(headerCellStyle);
        return cell;
    }

    public static Cell createCriteriaCell(int cellNumber, String headerLabel, Row row, XSSFWorkbook workbook) {
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setWrapText(true);
        headerCellStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
        headerCellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
       
       /* headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
       */
        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(headerLabel);
        cell.setCellStyle(headerCellStyle);
        return cell;

    }
    public static Cell createSubHeadingStringCell(int cellNumber, String cellValue, Row row,XSSFWorkbook workbook) {
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);

        Cell cell = row.createCell(cellNumber);
        cell.setCellValue(cellValue);
        cell.setCellStyle(headerCellStyle);
        return cell;
    }


    public static Cell createSubHeadingNumberCell(int cellNumber, Number cellValue, Row row,XSSFWorkbook workbook) {
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setColor(HSSFColor.BLACK.index);
        XSSFCellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        headerCellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        DataFormat format = workbook.createDataFormat();
        headerCellStyle.setDataFormat(format.getFormat("#,##0.000"));

        Cell cell = row.createCell(cellNumber);
        if (cellValue != null) {
            cell.setCellValue(cellValue.doubleValue());
        }
        cell.setCellStyle(headerCellStyle);
        return cell;
    }


}
