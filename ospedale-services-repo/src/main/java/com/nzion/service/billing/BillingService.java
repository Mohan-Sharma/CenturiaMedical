package com.nzion.service.billing;

import com.nzion.domain.*;
import com.nzion.domain.billing.*;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.UnitOfMeasurement;
import com.nzion.domain.emr.lab.LabOrderRequest;
import com.nzion.domain.emr.lab.LabTestPanel;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.pms.InsuranceProvider;
import com.nzion.domain.pms.Product;
import com.nzion.exception.TransactionException;
import com.nzion.report.search.view.BillingSearchVO;
import com.nzion.view.ConsultationValueObject;

import java.math.BigDecimal;
import java.util.*;

public interface BillingService {

	List<CptPrice> getCptPricesFor(Collection<Cpt> cpts);
	
	List<CptPrice> getContractCptPricesFor(Contract contract,Collection<Cpt> cpts);

	Invoice getBillingTransactionFor(PatientSoapNote soapNote);

	List<InvoiceItem> getTransactionItemsFor(Invoice billingTransaction);

	void getAllConsultations(ConsultationValueObject vo);

	Consultation getConsultationChargeFor(PatientSoapNote soapNote);

	Invoice generateInvoiceFor(Object object) throws TransactionException;
	
	void createInvoice(Invoice invoice) throws TransactionException;

	void saveInvoiceStatus(Invoice invoice,InvoiceStatusItem invoiceStatusItem);
	
	void saveInvoiceStatusAsWriteOff(Invoice invoice, InvoiceStatusItem invoiceStatusItem,InvoicePayment invoicePayment);
	
	void saveInvoiceStatusAtAdvance(Invoice invoice, InvoiceStatusItem invoiceStatusItem);
	
	InvoiceManager getManager(Invoice invoice);
	
	List<Invoice> getInvoice(List<InvoiceStatusItem> status,Patient patient,Employee emp,Date fromDate,Date thruDate,String ipNumber);
	
	List<LabOrderRequest> getSearchByLabOrder(List<LabOrderRequest.ORDERSTATUS> status,Patient patient,Provider provider,Referral referral);
	
	void saveLabOrderStatus(Invoice invoice);
	
	List<Contract>  getContractForInsPro(InsuranceProvider insuPro);

	List<InsuranceProvider> getInsuranceProviderAttachedToContract();

	List<Invoice> searchInvoiceBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate,List patientInsuranceIds);

	List<InvoiceItem> searchInvoiceItemBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate,List patientInsuranceIds);
	List<InvoiceItem> searchInvoiceItemWithOutCount(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate,List patientInsuranceIds);

	List<InvoiceItem> searchInvoiceItemByConcession(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate);

	List<InvoiceItem> searchCancelledInvoiceItem(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate);

	
	Map<String, Set<Invoice>>  getGroupedItems(String item, Set<Invoice> invoices);
	
	void exportInPatientInvoicesInDetail(List<Invoice> invoices,
			String[] invoiceFields, String[] invoiceFieldsLabel,
			String[] invoiceItemFields, String[] invoiceItemLabels, String filename);	
	
	UnitOfMeasurement getConfiguredCurrency();
	
	Map<String, Set<InvoiceItem>> getCptGroupedItems(String item, HashSet<Invoice> hashSet);

    void doTransactions(PatientSoapNote patientSoapNote,Invoice invoice) throws TransactionException;
    void doRegistrationTransaction(Patient patient) throws TransactionException;

    List<Invoice> getFirstInvoice(Patient patient);

    List<Invoice> getAllInvoices(Patient patient);
    
	Invoice getBillingTransactionForSchedule(Schedule schedule);
	
	LabTestPanel getLabtestPanelByPanelName(String panelName);
	
	public Map<AcctgTransTypeEnum,Map<String,List<AcctgTransactionEntry>>> getCollectionReport(AcctgTransTypeEnum chargeType,Date fromDate,Date toDate);
	
	Map<String,Object> getPriceFromMasterPriceConf(String serviceType, String mainGroup, String subGroup, 
			String procedureCode, String visitType, String doctor, String tariffCategory, String patientCategory, Date fromDate );
	
	Consultation getConsultationChargeForPatientCheckedIn(Schedule schedule);
	
	void updatePatientDeposit(Patient patient,BigDecimal amount);
	
	void updatePatientWithdraw(Patient patient,BigDecimal amount);
	
	String getTariffCodeByTariffName(String tariffName);

    String getServiceIdFromMasterPriceConf(String procedureCode,String visitType,String providerId, String tariffCategory , String patientCategory );

	List<Invoice> searchPendingInsuranceInvoiceBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate,List patientInsuranceIds);
	 
	List<Invoice> searchReferralInvoiceBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate);
	
	ArrayList<InvoicePayment> getInvoicePaymentsByCriteria(Patient patient, String patientType, Date fromDate, Date thruDate);

	Map<String, BigDecimal> getClinicRevenueByDate(Date fromDate, Date thruDate, boolean isMobileOrPatientPortal);
	
	List<Map<String,Object>> getIncomeAnalysisByServiceType(Date fromDate, Date thruDate);
	
	List<Map<String,Object>> getIncomeAnalysisBySpecialty(Date fromDate, Date thruDate);

    List<Map<String,Object>> getIncomeAnalysisByDoctor(Date fromDate, Date thruDate);

    List<Map<String,Object>> getIncomeAnalysisByPatientCategory(Date fromDate, Date thruDate);

	String getServiceCost(Employee provider, String tariffCategory, String visitType, String type);

	String getServiceCostForProduct(Product product);

	Boolean checkThruDate(String serviceType, String mainGroup, String subGroup,
												   String procedureCode, String visitType, String doctor, String tariffCategory, String patientCategory, Date fromDate );

	Object[] getTariffCategoryByTariffName(String tariffName);
	Object[] getTariffCategoryByTariffCode(String tariffCode);

	List<InvoiceItem> searchInvoiceItemByCancelStatus(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate);
}
