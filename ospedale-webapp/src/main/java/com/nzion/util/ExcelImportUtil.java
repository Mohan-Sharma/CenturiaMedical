package com.nzion.util;


import com.nzion.domain.Person;
import com.nzion.domain.Provider;
import com.nzion.domain.SlotType;
import com.nzion.domain.billing.TariffCategory;
import com.nzion.domain.emr.Cpt;
import com.nzion.repository.billing.impl.HibernateBillingRepository;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.utility.UtilityFinder;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.axonframework.domain.EventMessage;
import org.axonframework.eventhandling.*;
import org.hibernate.classic.Session;
import org.zkoss.zhtml.Filedownload;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


public class ExcelImportUtil {

	public static List<Map<String, Object>> serviceMasterList = RestServiceConsumer.getAllServiceMaster();

	public static CommonCrudService commonCrudService = Infrastructure.getSpringBean("commonCrudService");
	public static HibernateBillingRepository hibernateBillingRepository = Infrastructure.getSpringBean("billingRepository");

	public static void uploadExcel(InputStream inputStream) throws Exception {

		try {
			Workbook workbook = new XSSFWorkbook(inputStream);
			Sheet firstSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();
			int header = 0;
			int procedureCell = 0;
			int visitTypeCell = 0;
			String query = "INSERT into clinic_tariff (SERVICE_TYPE,SERVICE_MAIN_GROUP,SERVICE_SUB_GROUP,PROCEDURE_CODE,VISIT_TYPE,DOCTOR,TARIFF_CATEGORY,PATIENT_CATEGORY,SERVICE_COST,BILLABLE_AMOUNT_MIN,BILLABLE_AMOUNT,BILLABLE_AMOUNT_MAX,FROM_DATE,THRU_DATE,SERVICE_ID)" +
					" VALUES ";

			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				if (header == 0) {
					header = 1;
					continue;
				}
				procedureCell = 0;
				visitTypeCell = 0;

				if (query.charAt(query.length() - 1) == ')') {
					query = query + ",";
				}

				query = query + "('01','01','001',";
				//Iterator<Cell> cellIterator = nextRow.cellIterator();

				for (int i = 0; i <= 10; i++) {
					Cell cell = nextRow.getCell(i);
					switch (i) {
						case 0:
							//System.out.print(cell.getStringCellValue());
							/*if (query.charAt(query.length() - 1) == ')') {
								query = query + ",";
							}*/
							if ((cell != null) && (!cell.getStringCellValue().equals(""))) {
								String procedure = cell.getStringCellValue();
								procedure = commonCrudService.getByUniqueValue(Cpt.class, "description", procedure).getId();
								query = query + "'" + procedure + "',";
								procedureCell = 1;
							} else {
								query = query + "null,";
							}
							break;
						case 1:
							//System.out.print(cell.getStringCellValue());
							if ((cell != null) && (!cell.getStringCellValue().equals(""))) {
								if (procedureCell == 1) {
									UtilMessagesAndPopups.showError("Procedure and visit Type can not be in same row");
									return;
								} else {
									String visitType = cell.getStringCellValue();
									long visitTypeToInsert = 0;
									visitTypeToInsert = commonCrudService.getByUniqueValue(SlotType.class, "description", visitType).getId();
									query = query + visitTypeToInsert + ",";
									visitTypeCell = 1;
								}
							} else {
								query = query + null + ",";
							}
							break;
						case 2:
							//System.out.print(cell.getStringCellValue());
							if ((cell != null) && (!cell.getStringCellValue().equals(""))) {
								String doctor = cell.getStringCellValue();
								String drAccountNumber = doctor.substring(0, doctor.indexOf(" "));
								long doctorToInsert = 0;
								doctorToInsert = commonCrudService.getByUniqueValue(Person.class, "accountNumber", drAccountNumber).getId();
								query = query + doctorToInsert + ",'";
							} else {
								if (visitTypeCell == 1) {
									UtilMessagesAndPopups.showError("Doctor is mandatory");
									return;
								} else {
									query = query + null + ",'";
								}
							}
							break;
						case 3:
							//System.out.print(cell.getStringCellValue());
							if ((cell != null) && (!cell.getStringCellValue().equals(""))) {
								String tariffCategory = cell.getStringCellValue();
								Object[] objArr = hibernateBillingRepository.getTariffCategoryByTariffName(tariffCategory);
								if ((objArr != null) && (objArr[0] != null)) {
									query = query + objArr[0] + "',";
									if (objArr[1].equals("INSURANCE")) {
										query = query + "'02',";
									} else if (objArr[1].equals("CASH PAYING")) {
										query = query + "'01',";
									} else if (objArr[1].equals("CORPORATE")) {
										query = query + "'03',";
									}
								} else {
									UtilMessagesAndPopups.showError("TariffCategory is wrong");
									return;
								}
							} else {
								UtilMessagesAndPopups.showError("TariffCategory is mandatory");
								return;
							}
							break;
						case 4:
							//System.out.print(cell.getStringCellValue());
							if (cell != null) {
								BigDecimal serviceCost = new BigDecimal(cell.getNumericCellValue());
								query = query + serviceCost + ",";
							} else {
								query = query + null + ",";
							}
							break;
						case 5:
							//System.out.print(cell.getStringCellValue());
							if (cell != null) {
								BigDecimal billableMinAmount = new BigDecimal(cell.getNumericCellValue());
								query = query + billableMinAmount + ",";
							} else {
								query = query + null + ",";
							}
							break;
						case 6:
							//System.out.print(cell.getStringCellValue());
							if (cell != null) {
								BigDecimal billableAmount = new BigDecimal(cell.getNumericCellValue());
								query = query + billableAmount + ",";
								break;
							} else {
								UtilMessagesAndPopups.showError("BillableAmount is mandatory");
								return;
							}
						case 7:
							//System.out.print(cell.getStringCellValue());
							if (cell != null) {
								BigDecimal billableMaxAmount = new BigDecimal(cell.getNumericCellValue());
								query = query + billableMaxAmount + ",";
							} else {
								query = query + null + ",";
							}
							break;
						case 8:
							//System.out.print(cell.getDateCellValue());
							//UtilDateTime.format(UtilDateTime.getDateOnly(cell.getDateCellValue()), new SimpleDateFormat("YYYY-MM-dd"));
							if (cell != null) {
								String d = UtilDateTime.format(UtilDateTime.getDateOnly(cell.getDateCellValue()), new SimpleDateFormat("YYYY-MM-dd"));
								query = query + "'" + d + "',";
							} else {
								UtilMessagesAndPopups.showError("From Date is mandatory");
								return;
							}
							break;
						case 9:
							//System.out.print(cell.getStringCellValue());
							if (cell != null) {
								String d = UtilDateTime.format(UtilDateTime.getDateOnly(cell.getDateCellValue()), new SimpleDateFormat("YYYY-MM-dd"));
								query = query + "'" + d + "',";
							} else {
								UtilMessagesAndPopups.showError("Thru Date is mandatory");
								return;
							}
							break;
						case 10:
							//System.out.print(cell.getStringCellValue());
							if ((cell != null) && (!cell.getStringCellValue().equals(""))) {
								String serviceId = cell.getStringCellValue();
								String id = getServiceId(serviceId);
								if (id != null) {
									id = id.substring(0, id.indexOf('.'));
									query = query + "'" + id + "')";
								} else {
									UtilMessagesAndPopups.showError("Service ID is wrong");
									return;
								}
							} else {
								query = query + null + ")";
							}
							break;
					}
				}
			/*while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						System.out.print(cell.getStringCellValue());
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						System.out.print(cell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						if (HSSFDateUtil.isCellDateFormatted(cell)) {
							Date date = cell.getDateCellValue();
							System.out.print(UtilDateTime.format(date, new SimpleDateFormat("YYYY-MM-dd")));
						} else {
							System.out.print(cell.getNumericCellValue());
						}
						break;
				}
				System.out.print(" - ");
			}*/
			}
			Session session = Infrastructure.getSessionFactory().openSession();
			session.createSQLQuery(query).executeUpdate();
			session.close();
			inputStream.close();
		} catch (Exception e) {
			UtilMessagesAndPopups.showError("Upload Failed");
			e.printStackTrace();
		}
		UtilMessagesAndPopups.showSuccess("Upload Successful");
	}

	public static String getServiceId(String name) {
		Iterator iterator = serviceMasterList.iterator();
		while (iterator.hasNext()) {
			Map map = (Map) iterator.next();
			if (map.get("SERVICE_NAME").toString().equals(name)) {
				return map.get("ID").toString();
			}
		}
		return null;
	}

	public static String getServiceName(String id) {
		Iterator iterator = serviceMasterList.iterator();
		while (iterator.hasNext()) {
			Map map = (Map) iterator.next();
			if (map.get("ID").toString().substring(0, map.get("ID").toString().indexOf('.')).equals(id)) {
				return map.get("SERVICE_NAME").toString();
			}
		}
		return null;
	}

	/*public static void exportClinicTariff() throws Exception {
		com.nzion.zkoss.ext.DataExporter exporter = new com.nzion.zkoss.ext.CsvDataExporter();
		com.nzion.service.utility.UtilityFinder utilityFinder = Infrastructure.getSpringBean("utilityFinder");
		List<Map<String, Object>> mapList = utilityFinder.getAllClinicTariff();

		XSSFWorkbook hssfWorkbook = ExcelHelper.createWorkbook();
		XSSFSheet hssfSheet = ExcelHelper.createWorksheet("clinicTariff", hssfWorkbook);

		*//*Map<String, Object[]> data = new HashMap<String, Object[]>();
		data.put("1", new Object[] {"Emp No.", "Name", "Salary"});
		data.put("2", new Object[] {1d, "John", 1500000d});
		data.put("3", new Object[] {2d, "Sam", 800000d});
		data.put("4", new Object[] {3d, "Dean", 700000d});

		Set<String> keyset = data.keySet();*//*
		int rownum = 0;
		for (Map map : mapList) {
			Row row = hssfSheet.createRow(rownum++);
			for (int i = 0; i <= 10; i++) {
				Cell cell = row.createCell(i);
				switch (i) {
					case 0:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i,"Procedure", row, hssfWorkbook);
						} else {
							String procedure = null;
							if (map.get("PROCEDURE_CODE") != null) {
								procedure = commonCrudService.getById(Cpt.class, map.get("PROCEDURE_CODE").toString()).getDescription();
							}
							ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(procedure) ? procedure : "", row, hssfWorkbook);
						}
						break;
					case 1:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i,"Visit Type", row, hssfWorkbook);
						} else {
							String visitType = null;
							if (map.get("VISIT_TYPE") != null) {
								visitType = commonCrudService.getById(SlotType.class, (Long) map.get("VISIT_TYPE")).getName();
							}
							ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(visitType) ? visitType : "", row, hssfWorkbook);
						}
						break;
					case 2:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i,"Doctor", row, hssfWorkbook);
						} else {
							String doctor = null;
							if (map.get("DOCTOR") != null) {
								doctor = commonCrudService.getById(Person.class, (Long) map.get("DOCTOR")).getFirstName();
							}
							ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(doctor) ? doctor : "", row, hssfWorkbook);
						}
						break;
					case 3:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i,"Tariff Category", row, hssfWorkbook);
						} else {
							String tariffCategory = null;
							if (map.get("TARIFF_CATEGORY") != null) {
								tariffCategory = (hibernateBillingRepository.getTariffCategoryByTariffCode(map.get("TARIFF_CATEGORY").toString())[0]).toString();
							}
							ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(tariffCategory) ? tariffCategory : "", row, hssfWorkbook);
						}
						break;
					case 4:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i,"Service Cost", row, hssfWorkbook);
						}else {
							BigDecimal serviceCost = null;
							if (map.get("SERVICE_COST") != null) {
								serviceCost = (BigDecimal) map.get("SERVICE_COST");
							}
							ExcelHelper.createNumberCell(i, UtilValidator.isNotEmpty(serviceCost) ? serviceCost : null, row, hssfWorkbook);
						}
						break;
					case 5:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i, "Billable Min Amount", row, hssfWorkbook);
						} else {
							BigDecimal billableMinAmount = null;
							if (map.get("BILLABLE_AMOUNT_MIN") != null) {
								billableMinAmount = (BigDecimal) map.get("BILLABLE_AMOUNT_MIN");
							}
							ExcelHelper.createNumberCell(i, UtilValidator.isNotEmpty(billableMinAmount) ? billableMinAmount : null, row, hssfWorkbook);
						}
						break;
					case 6:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i, "Billable amount", row, hssfWorkbook);
						} else {
							BigDecimal billableAmount = null;
							if (map.get("BILLABLE_AMOUNT") != null) {
								billableAmount = (BigDecimal) map.get("BILLABLE_AMOUNT");
							}
							ExcelHelper.createNumberCell(i, UtilValidator.isNotEmpty(billableAmount) ? billableAmount : null, row, hssfWorkbook);
						}
						break;
					case 7:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i, "Billable Max amount", row, hssfWorkbook);
						} else {
							BigDecimal billableMaxAmount = null;
							if (map.get("BILLABLE_AMOUNT_MAX") != null) {
								billableMaxAmount = (BigDecimal) map.get("BILLABLE_AMOUNT_MAX");
							}
							ExcelHelper.createNumberCell(i, UtilValidator.isNotEmpty(billableMaxAmount) ? billableMaxAmount : null, row, hssfWorkbook);
						}
						break;
					case 8:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i, "From Date\n" +"YYYY-MM-DD\"", row, hssfWorkbook);
						} else {
							Date fromDate = null;
							if (map.get("FROM_DATE") != null) {
								fromDate = (Date) map.get("FROM_DATE");
							}
							CellStyle cellStyle = hssfWorkbook.createCellStyle();
							CreationHelper createHelper = hssfWorkbook.getCreationHelper();
							cellStyle.setDataFormat(
									createHelper.createDataFormat().getFormat("YYYY-MM-dd"));
							Cell cell8 = row.createCell(8);
							cell.setCellValue(fromDate);
							cell.setCellStyle(cellStyle);
							hssfSheet.autoSizeColumn(8);
							//ExcelHelper.createDateCell(i, UtilValidator.isNotEmpty(fromDate) ? fromDate : null, row, hssfWorkbook);
						}
						break;
					case 9:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i, "Thru Date\n" +"YYYY-MM-DD\"", row, hssfWorkbook);
						} else {
							Date thruDate = null;
							if (map.get("THRU_DATE") != null) {
								thruDate = (Date) map.get("THRU_DATE");
						}
							CellStyle cellStyle9 = hssfWorkbook.createCellStyle();
							CreationHelper createHelper9 = hssfWorkbook.getCreationHelper();
							cellStyle9.setDataFormat(
									createHelper9.createDataFormat().getFormat("YYYY-MM-dd"));

							Cell cell9 = row.createCell(9);
							cell.setCellValue(thruDate);
							cell.setCellStyle(cellStyle9);

							hssfSheet.autoSizeColumn(9);
							//ExcelHelper.createDateCell(i,UtilValidator.isNotEmpty(thruDate) ? thruDate : null,row,hssfWorkbook);
						}
						break;
					case 10:
						if ((rownum-1) == 0){
							ExcelHelper.createStringCell(i, "Service ID", row, hssfWorkbook);
						} else {
							String serviceId = null;
							if (map.get("SERVICE_ID") != null) {
								serviceId = map.get("SERVICE_ID").toString();
								serviceId = getServiceName(serviceId);
							}
							ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(serviceId) ? serviceId : null, row, hssfWorkbook);
						}
						break;
				}
			}
		}
		ByteArrayOutputStream fos = new ByteArrayOutputStream();
		hssfWorkbook.write(fos);
		fos.close();
		Filedownload.save(fos.toByteArray(), "application/xlsx", "clinic tariff");
		}
*/
	public static void exportClinicTariff() throws Exception {
		try {
			List<Cpt> cptList = commonCrudService.getAll(Cpt.class);
			List<SlotType> slotTypeList = commonCrudService.getAll(SlotType.class);
			List<Provider> providerList = commonCrudService.getAll(Provider.class);
			List<TariffCategory> tariffCategories = commonCrudService.getAll(TariffCategory.class);

			String[] columnNameArr = new String[]{"Procedure", "Visit Type", "Doctor", "Tariff Category", "Service Cost", "Billable Min Amount", "Billable amount", "Billable Max amount",
					"From Date \nyyyy-mm-dd", "Thru Date \nyyyy-mm-dd", "Service ID"};

			XSSFWorkbook hssfWorkbook = ExcelHelper.createWorkbook();

			XSSFSheet realSheet = hssfWorkbook.createSheet("clinic tariff");
			createNamedRowWithCell(columnNameArr, realSheet);

			XSSFSheet hidden1 = hssfWorkbook.createSheet("hidden1");
			XSSFSheet hidden2 = hssfWorkbook.createSheet("hidden2");
			XSSFSheet hidden3 = hssfWorkbook.createSheet("hidden3");
			XSSFSheet hidden4 = hssfWorkbook.createSheet("hidden4");
			XSSFSheet hidden5 = hssfWorkbook.createSheet("hidden5");

			Iterator iterator1 = cptList.iterator();
			int rowNum = 0;
			while (iterator1.hasNext()) {
				String cptName = ((Cpt) iterator1.next()).getDescription();
				XSSFRow row = hidden1.createRow(rowNum++);
				XSSFCell cell0 = row.createCell(0);
				cell0.setCellValue(cptName);
			}

			Iterator iterator2 = slotTypeList.iterator();
			rowNum = 0;
			while (iterator2.hasNext()) {
				String slotTypeName = ((SlotType) iterator2.next()).getName();
				XSSFRow row = hidden2.createRow(rowNum++);
				XSSFCell cell0 = row.createCell(0);
				cell0.setCellValue(slotTypeName);
			}

			Iterator iterator3 = providerList.iterator();
			rowNum = 0;
			while (iterator3.hasNext()) {
				//String providerName = ((Provider) iterator3.next()).getFirstName();
				Provider provider = (Provider) iterator3.next();
				String providerName = provider.getAccountNumber() + " " +provider.getFirstName()+" "+provider.getLastName();
				XSSFRow row = hidden3.createRow(rowNum++);
				XSSFCell cell0 = row.createCell(0);
				cell0.setCellValue(providerName);
			}

			Iterator iterator4 = tariffCategories.iterator();
			rowNum = 0;
			while (iterator4.hasNext()) {
				String tariffName = ((TariffCategory) iterator4.next()).getTariff();
				XSSFRow row = hidden4.createRow(rowNum++);
				XSSFCell cell0 = row.createCell(0);
				cell0.setCellValue(tariffName);
			}

			Iterator iterator5 = serviceMasterList.iterator();
			rowNum = 0;
			while (iterator5.hasNext()) {
				String serviceName = (String) ((Map) iterator5.next()).get("SERVICE_NAME");
				XSSFRow row = hidden5.createRow(rowNum++);
				XSSFCell cell0 = row.createCell(0);
				cell0.setCellValue(serviceName);
			}

			Name namedCell0 = hssfWorkbook.createName();
			namedCell0.setNameName("hidden0");
			namedCell0.setRefersToFormula("hidden1!$A$1:$A$" + cptList.size());

			Name namedCell1 = hssfWorkbook.createName();
			namedCell1.setNameName("hidden1");
			namedCell1.setRefersToFormula("hidden2!$A$1:$A$" + slotTypeList.size());

			Name namedCell2 = hssfWorkbook.createName();
			namedCell2.setNameName("hidden2");
			namedCell2.setRefersToFormula("hidden3!$A$1:$A$" + providerList.size());

			Name namedCell3 = hssfWorkbook.createName();
			namedCell3.setNameName("hidden3");
			namedCell3.setRefersToFormula("hidden4!$A$1:$A$" + tariffCategories.size());

			Name namedCell4 = hssfWorkbook.createName();
			namedCell4.setNameName("hidden4");
			namedCell4.setRefersToFormula("hidden5!$A$1:$A$" + serviceMasterList.size());

			XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(realSheet);
			XSSFDataValidationConstraint dvConstraint0 = (XSSFDataValidationConstraint)
					dvHelper.createFormulaListConstraint("hidden0");
			CellRangeAddressList addressList0 = new CellRangeAddressList(1, 65000, 0, 0);
			XSSFDataValidation validation0 = (XSSFDataValidation) dvHelper.createValidation(
					dvConstraint0, addressList0);
			validation0.setShowErrorBox(true);
			realSheet.addValidationData(validation0);

			XSSFDataValidationConstraint dvConstraint1 = (XSSFDataValidationConstraint)
					dvHelper.createFormulaListConstraint("hidden1");
			CellRangeAddressList addressList1 = new CellRangeAddressList(1, 65000, 1, 1);
			XSSFDataValidation validation1 = (XSSFDataValidation) dvHelper.createValidation(
					dvConstraint1, addressList1);
			validation1.setShowErrorBox(true);
			realSheet.addValidationData(validation1);

			XSSFDataValidationConstraint dvConstraint2 = (XSSFDataValidationConstraint)
					dvHelper.createFormulaListConstraint("hidden2");
			CellRangeAddressList addressList2 = new CellRangeAddressList(1, 65000, 2, 2);
			XSSFDataValidation validation2 = (XSSFDataValidation) dvHelper.createValidation(
					dvConstraint2, addressList2);
			validation2.setShowErrorBox(true);
			realSheet.addValidationData(validation2);

			XSSFDataValidationConstraint dvConstraint3 = (XSSFDataValidationConstraint)
					dvHelper.createFormulaListConstraint("hidden3");
			CellRangeAddressList addressList3 = new CellRangeAddressList(1, 65000, 3, 3);
			XSSFDataValidation validation3 = (XSSFDataValidation) dvHelper.createValidation(
					dvConstraint3, addressList3);
			validation3.setShowErrorBox(true);
			realSheet.addValidationData(validation3);

			XSSFDataValidationConstraint dvConstraint4 = (XSSFDataValidationConstraint)
					dvHelper.createFormulaListConstraint("hidden4");
			CellRangeAddressList addressList4 = new CellRangeAddressList(1, 65000, 10, 10);
			XSSFDataValidation validation4 = (XSSFDataValidation) dvHelper.createValidation(
					dvConstraint4, addressList4);
			validation4.setShowErrorBox(true);
			realSheet.addValidationData(validation4);

			hssfWorkbook.setSheetHidden(1, true);
			hssfWorkbook.setSheetHidden(2, true);
			hssfWorkbook.setSheetHidden(3, true);
			hssfWorkbook.setSheetHidden(4, true);
			hssfWorkbook.setSheetHidden(5, true);

			/*DataFormat fmt = hssfWorkbook.createDataFormat();
			CellStyle textStyle = hssfWorkbook.createCellStyle();
			textStyle.setDataFormat(fmt.getFormat("@"));
			realSheet.setDefaultColumnStyle(8, textStyle);
			realSheet.setDefaultColumnStyle(9, textStyle);*/

			com.nzion.zkoss.ext.DataExporter exporter = new com.nzion.zkoss.ext.CsvDataExporter();
			com.nzion.service.utility.UtilityFinder utilityFinder = Infrastructure.getSpringBean("utilityFinder");
			List<Map<String, Object>> mapList = utilityFinder.getAllClinicTariff();

			int rownum = 1;
			for (Map map : mapList) {
				Row row = realSheet.createRow(rownum++);
				for (int i = 0; i <= 10; i++) {
					Cell cell = row.createCell(i);
					switch (i) {
						case 0:
							String procedure = null;
							if (map.get("PROCEDURE_CODE") != null) {
								procedure = commonCrudService.getById(Cpt.class, map.get("PROCEDURE_CODE").toString()).getDescription();
								if ((procedure != null) && (procedure != "")) {
									ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(procedure) ? procedure : "", row, hssfWorkbook);
								}
							}
							break;
						case 1:
							String visitType = null;
							if (map.get("VISIT_TYPE") != null) {
								visitType = commonCrudService.getById(SlotType.class, (Long) map.get("VISIT_TYPE")).getName();
								if ((visitType != null) && (visitType != "")) {
									ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(visitType) ? visitType : "", row, hssfWorkbook);
								}
							}
							break;
						case 2:
							String doctor = null;
							if (map.get("DOCTOR") != null) {
								//doctor = commonCrudService.getById(Person.class, (Long) map.get("DOCTOR")).getFirstName() ;
								Person person = commonCrudService.getById(Person.class, (Long) map.get("DOCTOR"));
								if (person != null){
									doctor = person.getAccountNumber() + " " +person.getFirstName()+" "+person.getLastName();
								}
								if ((doctor != null) && (doctor != "")) {
									ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(doctor) ? doctor : "", row, hssfWorkbook);
								}
							}
							break;
						case 3:
							String tariffCategory = null;
							if (map.get("TARIFF_CATEGORY") != null) {
								tariffCategory = (hibernateBillingRepository.getTariffCategoryByTariffCode(map.get("TARIFF_CATEGORY").toString())[0]).toString();
								if ((tariffCategory != null) && (tariffCategory != "")) {
									ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(tariffCategory) ? tariffCategory : "", row, hssfWorkbook);
								}
							}
							break;
						case 4:
							BigDecimal serviceCost = null;
							if (map.get("SERVICE_COST") != null) {
								serviceCost = (BigDecimal) map.get("SERVICE_COST");
							}
							ExcelHelper.createNumberCell(i, UtilValidator.isNotEmpty(serviceCost) ? serviceCost : null, row, hssfWorkbook);
							break;
						case 5:
							BigDecimal billableMinAmount = null;
							if (map.get("BILLABLE_AMOUNT_MIN") != null) {
								billableMinAmount = (BigDecimal) map.get("BILLABLE_AMOUNT_MIN");
							}
							ExcelHelper.createNumberCell(i, UtilValidator.isNotEmpty(billableMinAmount) ? billableMinAmount : null, row, hssfWorkbook);
							break;
						case 6:
							BigDecimal billableAmount = null;
							if (map.get("BILLABLE_AMOUNT") != null) {
								billableAmount = (BigDecimal) map.get("BILLABLE_AMOUNT");
							}
							ExcelHelper.createNumberCell(i, UtilValidator.isNotEmpty(billableAmount) ? billableAmount : null, row, hssfWorkbook);
							break;
						case 7:
							BigDecimal billableMaxAmount = null;
							if (map.get("BILLABLE_AMOUNT_MAX") != null) {
								billableMaxAmount = (BigDecimal) map.get("BILLABLE_AMOUNT_MAX");
							}
							ExcelHelper.createNumberCell(i, UtilValidator.isNotEmpty(billableMaxAmount) ? billableMaxAmount : null, row, hssfWorkbook);
							break;
						case 8:
							Date fromDate = null;
							if (map.get("FROM_DATE") != null) {
								fromDate = (Date) map.get("FROM_DATE");
							}
							CellStyle cellStyle = hssfWorkbook.createCellStyle();
							CreationHelper createHelper = hssfWorkbook.getCreationHelper();
							cellStyle.setDataFormat(
									createHelper.createDataFormat().getFormat("YYYY-MM-dd"));
							Cell cell8 = row.createCell(8);
							cell.setCellValue(fromDate);
							cell.setCellStyle(cellStyle);
							realSheet.autoSizeColumn(8);
							break;
						case 9:
							Date thruDate = null;
							if (map.get("THRU_DATE") != null) {
								thruDate = (Date) map.get("THRU_DATE");
							}
							CellStyle cellStyle9 = hssfWorkbook.createCellStyle();
							CreationHelper createHelper9 = hssfWorkbook.getCreationHelper();
							cellStyle9.setDataFormat(
									createHelper9.createDataFormat().getFormat("YYYY-MM-dd"));

							Cell cell9 = row.createCell(9);
							cell.setCellValue(thruDate);
							cell.setCellStyle(cellStyle9);

							realSheet.autoSizeColumn(9);
							break;
						case 10:
							String serviceId = null;
							if (map.get("SERVICE_ID") != null) {
								serviceId = map.get("SERVICE_ID").toString();
								serviceId = getServiceName(serviceId);
								if ((serviceId != null) && (serviceId != "")) {
									ExcelHelper.createStringCell(i, UtilValidator.isNotEmpty(serviceId) ? serviceId : null, row, hssfWorkbook);
								}
							}
							break;
					}
				}
			}

			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			hssfWorkbook.write(fos);
			fos.close();
			Filedownload.save(fos.toByteArray(), "application/xlsx", "clinic tariff");
		} catch (Exception e){
			e.printStackTrace();
			UtilMessagesAndPopups.showError("Failed to export Clinic tariff");
		}
	}

	public static void truncateClinicTariff(){
		UtilMessagesAndPopups.showConfirmation("Are You Sure you want to delete the rates",new EventListener() {
			@Override
			public void onEvent(Event evt) throws Exception {
				if("onYes".equalsIgnoreCase(evt.getName())){
					/*try {
						Session session = Infrastructure.getSessionFactory().openSession();
						session.createSQLQuery("truncate table clinic_tariff").executeUpdate();
						session.close();
					} catch (Exception e){
						e.printStackTrace();
					}*/
					UtilMessagesAndPopups.showConfirmation("Once you click on yes all the clinic rates would be deleted. Do you want to proceed ?",new EventListener() {
						@Override
						public void onEvent(Event evt) throws Exception {
							if("onYes".equalsIgnoreCase(evt.getName())){
								try {
									Session session = Infrastructure.getSessionFactory().openSession();
									session.createSQLQuery("truncate table clinic_tariff").executeUpdate();
									UtilMessagesAndPopups.showSuccess("Clinic Tariff truncated successfully");
									session.close();
								} catch (Exception e){
									e.printStackTrace();
								}
							}
							if("onNo".equalsIgnoreCase(evt.getName()))
								return;
						}
					});
				}
				if("onNo".equalsIgnoreCase(evt.getName()))
					return;
			}
		});

	}
	private static void createNamedRowWithCell(String[] headerName, XSSFSheet dataSheet) {
		XSSFRow row = dataSheet.createRow(0);
		for (int count = 0; count < headerName.length; count++) {
			String name = headerName[count];
			XSSFCell cell = row.createCell(count);
			cell.setCellValue(name);
		}
	}
	}