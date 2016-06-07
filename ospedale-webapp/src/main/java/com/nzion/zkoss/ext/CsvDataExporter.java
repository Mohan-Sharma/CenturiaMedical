package com.nzion.zkoss.ext;

import static com.nzion.util.Constants.COMMA;
import static com.nzion.util.Constants.NEWLINE;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nzion.domain.Person;
import com.nzion.domain.UserLogin;
import com.nzion.report.dto.CollectionReportDto;
import com.nzion.report.dto.CollectionReportItemDto;
import com.nzion.report.dto.LabReportDto;
import com.nzion.util.ExcelHelper;

import com.nzion.util.Infrastructure;
import com.nzion.zkoss.composer.UserLoginController;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.security.GeneralSecurityException;
import java.util.concurrent.*;

import org.apache.poi.poifs.crypt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.util.media.AMedia;
import org.zkoss.zhtml.Filedownload;

import com.nzion.util.UtilReflection;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 *         Aug 17, 2010
 */
public class CsvDataExporter implements DataExporter {

    @Override
    public void export(List<?> entities, String[] fields, String[] labels, String filename) {
        StringBuilder content = new StringBuilder();
        for (String label : labels) {
            content.append(label).append(COMMA);
        }
        content.deleteCharAt(content.length() - 1);
        content.append(NEWLINE);
        if (UtilValidator.isNotEmpty(entities))
            for (Object entity : entities) {
                for (String field : fields) {
                    Object fieldValue = UtilReflection.getNestedFieldValue(entity, field);
                    content.append(fieldValue == null ? "" : "\"" + fieldValue.toString() + "\"").append(COMMA);
                }
                content.deleteCharAt(content.length() - 1);
                content.append(NEWLINE);
            }
        Filedownload.save(content.toString().getBytes(), "text/csv", filename);

    }



    @Override
    public void exportCollectionReport(StringBuilder headers,List<?> entities, Set keySet, String[] labels, String filename) {
        List<Map> mapEntity = (List<Map>) entities;
        StringBuilder content = new StringBuilder();
        content.append(headers).append(NEWLINE);
        for (String label : labels) {
            content.append(label).append(COMMA);
        }
        content.deleteCharAt(content.length() - 1);
        content.append(NEWLINE);
        if (UtilValidator.isNotEmpty(entities))
            for (Map entity : mapEntity) {
                for (Object key : keySet) {
                    Object value = entity.get(key);
                    content.append(value == null ? "" : "\"" + value.toString() + "\"").append(COMMA);
                }
                content.deleteCharAt(content.length() - 1);
                content.append(NEWLINE);
            }
        Filedownload.save(content.toString().getBytes(), "text/csv", filename);
    }

   /* @Override
    public void exportIPDCollectionReport(List<CollectionReportDto> collectionReportDtos, String fileName) throws IOException {
        HSSFWorkbook hssfWorkbook = ExcelHelper.createWorkbook();
        HSSFSheet hssfSheet = ExcelHelper.createWorksheet(fileName, hssfWorkbook);
        int rowNumber = 0;
        Row topHeaderRow = ExcelHelper.createRow(rowNumber, hssfSheet);
        ExcelHelper.createHeaderCell(0, "Item Name", topHeaderRow, hssfWorkbook);
        ExcelHelper.createHeaderCell(1, "Item Count", topHeaderRow, hssfWorkbook);
        ExcelHelper.createHeaderCell(2, "Item Amount", topHeaderRow, hssfWorkbook);
        rowNumber = rowNumber + 1;
        for (CollectionReportDto collectionReportDto : collectionReportDtos) {
            Row headerRow = ExcelHelper.createRow(rowNumber, hssfSheet);
            ExcelHelper.createHeaderCell(0, collectionReportDto.description, headerRow, hssfWorkbook);
            ExcelHelper.createNumberHeaderCell(1, collectionReportDto.count, headerRow, hssfWorkbook);
            ExcelHelper.createNumberHeaderCell(2, collectionReportDto.amount, headerRow, hssfWorkbook);
            rowNumber = rowNumber + 1;
            if (UtilValidator.isNotEmpty(collectionReportDto.collectionReportItemDto)) {
                for (CollectionReportItemDto collectionReportItemDto : collectionReportDto.collectionReportItemDto) {
                    Row itemRow = ExcelHelper.createRow(rowNumber, hssfSheet);
                    ExcelHelper.createStringCell(0, collectionReportItemDto.description, itemRow);
                    ExcelHelper.createNumberCell(1, collectionReportItemDto.count, itemRow);
                    ExcelHelper.createNumberCell(2, collectionReportItemDto.amount, itemRow);
                    rowNumber = rowNumber + 1;
                }
            }
        }
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        hssfWorkbook.write(fos);
        fos.close();
        Filedownload.save(fos.toByteArray(), "application/x-ms-excel", fileName);
    }

    @Override
    public void exportLabCollectionReport(List<LabReportDto> labCollectionReportDtos, String fileName) throws IOException {
        HSSFWorkbook hssfWorkbook = ExcelHelper.createWorkbook();
        HSSFSheet hssfSheet = ExcelHelper.createWorksheet(fileName, hssfWorkbook);
        int rowNumber = 0;
        Row topHeaderRow = ExcelHelper.createRow(rowNumber, hssfSheet);
        ExcelHelper.createHeaderCell(0, "Test Name", topHeaderRow, hssfWorkbook);
        ExcelHelper.createHeaderCell(1, "Count", topHeaderRow, hssfWorkbook);
        ExcelHelper.createHeaderCell(2, "Amount", topHeaderRow, hssfWorkbook);
        ExcelHelper.createHeaderCell(3, "Referred By", topHeaderRow, hssfWorkbook);
        rowNumber = rowNumber + 1;
        if (UtilValidator.isNotEmpty(labCollectionReportDtos)) {
            for (LabReportDto labReportDto : labCollectionReportDtos) {
                Row itemRow = ExcelHelper.createRow(rowNumber, hssfSheet);
                ExcelHelper.createStringCell(0, labReportDto.getLabTestName(), itemRow);
                ExcelHelper.createNumberCell(1, labReportDto.getCount(), itemRow);
                ExcelHelper.createNumberCell(2, labReportDto.getAmount(), itemRow);
                ExcelHelper.createStringCell(3, labReportDto.getReferredBy(), itemRow);
                rowNumber = rowNumber + 1;
            }
        }
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        hssfWorkbook.write(fos);
        fos.close();
        Filedownload.save(fos.toByteArray(), "application/x-ms-excel", fileName);
    }
*/

    @Override
    public void exportWithHeader(StringBuilder headers, List<?> entities, String[] fields, String[] labels, String filename, String reportName,Set keySet) throws IOException, InvalidFormatException, GeneralSecurityException {

        XSSFWorkbook hssfWorkbook = ExcelHelper.createWorkbook();
        XSSFSheet hssfSheet = ExcelHelper.createWorksheet(filename, hssfWorkbook);


        int rowNumber = 0;
        int colNumber = 0;
        Row reportNameRow = ExcelHelper.createRow(rowNumber, hssfSheet);
        ExcelHelper.createReportHeaderCell(0,reportName, reportNameRow, hssfWorkbook);
        hssfSheet.addMergedRegion(new CellRangeAddress(0,0,0,(labels.length)-1));
        rowNumber = rowNumber + 1;
        if(headers != null && headers.length() >0){
            Row topCriteriaRow = ExcelHelper.createRow(rowNumber, hssfSheet);
            colNumber = 0;
            ExcelHelper.createCriteriaCell(colNumber,headers.toString(), topCriteriaRow, hssfWorkbook);
            hssfSheet.addMergedRegion(new CellRangeAddress(1,1,0,(labels.length)-1));
            rowNumber = rowNumber + 1;
        }
        Row topHeaderRow = ExcelHelper.createRow(rowNumber, hssfSheet);
        colNumber = 0;
        for(String label: labels){
            ExcelHelper.createHeaderCell(colNumber,label, topHeaderRow, hssfWorkbook);
            colNumber++;
        }
        rowNumber = rowNumber + 1;

        if (UtilValidator.isNotEmpty(entities)) {
            if(UtilValidator.isNotEmpty(keySet)){
                List<Map> mapEntity = (List<Map>) entities;
                for (Map entity : mapEntity) {
                    Row itemRow = ExcelHelper.createRow(rowNumber, hssfSheet);
                    colNumber = 0;
                    if("CONSULATION".equals(entity.get("itemName")) || "PROCEDURE".equals(entity.get("itemName")) || "REGISTRATION".equals(entity.get("itemName"))){
                        for (Object key : keySet) {
                            Object fieldValue = entity.get(key);
                            if(UtilValidator.isNotEmpty( fieldValue) && fieldValue instanceof BigDecimal){
                                BigDecimal amount = (BigDecimal)fieldValue;
                                ExcelHelper.createSubHeadingNumberCell(colNumber, amount.doubleValue(), itemRow, hssfWorkbook);
                            }
                            else
                                ExcelHelper.createSubHeadingStringCell(colNumber, fieldValue.toString(), itemRow,hssfWorkbook);
                            colNumber++;
                        }
                    }
                    else{
                        for (Object key : keySet) {
                            Object fieldValue = entity.get(key);
                            if(UtilValidator.isNotEmpty( fieldValue) && fieldValue instanceof BigDecimal){
                                BigDecimal amount = (BigDecimal)fieldValue;
                                ExcelHelper.createNumberCell(colNumber, amount.doubleValue(), itemRow, hssfWorkbook);
                            }
                            else
                                ExcelHelper.createStringCell(colNumber, UtilValidator.isNotEmpty( fieldValue)?fieldValue.toString():"", itemRow,hssfWorkbook);
                            colNumber++;
                        }
                    }
                    rowNumber = rowNumber + 1;
                }
            }
            else{
                for (Object entity : entities) {
                    Row itemRow = ExcelHelper.createRow(rowNumber, hssfSheet);
                    colNumber = 0;
                    for (String field : fields) {
                        Object fieldValue = UtilReflection.getNestedFieldValue(entity, field);
                        if(UtilValidator.isNotEmpty(fieldValue) && fieldValue instanceof BigDecimal){
                            BigDecimal amount = (BigDecimal)fieldValue;
                            ExcelHelper.createNumberCell(colNumber, amount.doubleValue(), itemRow, hssfWorkbook);
                        }
                        /*
                        * Mohan Sharma - Modified to display time in hh:mm a format
                        * */
                        else if (UtilValidator.isNotEmpty(fieldValue) && field.toUpperCase().endsWith("TIME") && fieldValue instanceof Date){
                            Date date = (Date)fieldValue;
                            ExcelHelper.createTimeCell(colNumber, date, itemRow, hssfWorkbook);
                        }
                        else if (UtilValidator.isNotEmpty(fieldValue) && fieldValue instanceof Date){
                            Date date = (Date)fieldValue;
                            ExcelHelper.createDateCell(colNumber, date, itemRow, hssfWorkbook);
                        }
                        else
                            ExcelHelper.createStringCell(colNumber,UtilValidator.isNotEmpty( fieldValue)?fieldValue.toString():"", itemRow,hssfWorkbook);
                        colNumber++;
                    }
                    rowNumber = rowNumber + 1;
                }
            }
        }
        for(int columnIndex = 0; columnIndex < 10; columnIndex++) {
            hssfSheet.autoSizeColumn(columnIndex);
        }
        // Raghu Bandi: All changes below have been made to create a password protected file

        /*ByteArrayOutputStream fos = new ByteArrayOutputStream();
        hssfWorkbook.write(fos);
        fos.close();*/
        //write the excel to a file

        String filePath = "/"+filename;
        final File file = new File(filePath);
        try {
            FileOutputStream fileOut = new FileOutputStream(filePath);
            hssfWorkbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Person person = Infrastructure.getLoggedInPerson();
        //Add password protection and encrypt the file
        POIFSFileSystem fs = new POIFSFileSystem();
        EncryptionInfo info = new EncryptionInfo(fs, EncryptionMode.agile);
        Encryptor enc = info.getEncryptor();

        if(person!= null && person.getUserLogin() != null && person.getUserLogin().getPassword() != null) {
            enc.confirmPassword(person.getUserLogin().getPassword());
        }
        OPCPackage opc = OPCPackage.open(file, PackageAccess.READ_WRITE);
        OutputStream os = enc.getDataStream(fs);
        opc.save(os);
        opc.close();

        FileOutputStream fos = new FileOutputStream(file);
        fs.writeFilesystem(fos);
        fos.close();
        Filedownload.save(new FileInputStream(file), "application/x-ms-excel", filename);
        //Raghu Bandi: The following code deletes the file from the server after some set seconds.
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        //try {
        ScheduledFuture scheduledFuture = scheduledExecutorService.schedule(new Runnable() {
                public void run() {
                    boolean result = file.delete();
                }
           },
           15,
           TimeUnit.SECONDS);
        scheduledExecutorService.shutdown();
    }
}
