package com.nzion.repository.billing;

import com.nzion.domain.*;
import com.nzion.domain.billing.*;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.lab.LabOrderRequest;
import com.nzion.domain.emr.lab.LabTestPanel;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.pms.InsuranceProvider;
import com.nzion.domain.pms.Product;
import com.nzion.report.search.view.BillingSearchVO;
import com.nzion.repository.BaseRepository;

import java.math.BigDecimal;
import java.util.*;

public interface BillingRepository extends BaseRepository {

	List<CptPrice> getCptPricesFor(Collection<Cpt> cpts);
	
	List<CptPrice> getContractCptPricesFor(Contract contract,Collection<Cpt> cpts);
	
	Contract findEffectiveContractFor(String name, Contract.CONTRACTTYPE cType, Date fromDate, Date thruDate);

	List<Invoice> getBillingTransactionFor(PatientSoapNote soapNote);

	List<InvoiceItem> getTransactionItemsFor(Invoice billingTransaction);

	List<Consultation> getConsultationBySpeciality(Speciality speciality);

	List<Consultation> getConsultationByProvider(Person provider);
	
	List<Consultation> getConsultationChargeFor(Speciality speciality,SoapNoteType soapNoteType);
	
	List<Consultation> getConsultationChargeFor(Employee employee,SoapNoteType soapNoteType);
	
	List<Invoice> getInvoice(List<InvoiceStatusItem> status,Patient patient,Employee emp,Date fromDate,Date thruDate,String ipNumber);
	
	List<LabOrderRequest> getSearchByLabOrder(List<LabOrderRequest.ORDERSTATUS> status, Patient patient, Provider provider, Referral referral);
	
	List<InsuranceProvider> getInsuranceProviderAttachedToContract();
	
	List<Contract>  getContractForInsPro(InsuranceProvider insuPro);

	List<Invoice> searchInvoiceBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate, List patientInsuranceIds);
	
	List<InvoiceItem> searchInvoiceItemBy(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate, List patientInsuranceIds);
	List<InvoiceItem> searchInvoiceItemWithOutCount(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate, List patientInsuranceIds);

	List<InvoiceItem> searchInvoiceItemByConcession(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate);
		
	List<InvoiceItem> searchCancelledInvoiceItem(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate);
	
	
	List<AcctgTransaction> searchAcctgTransactionBy(AcctgTransTypeEnum chargeType,Date fromDate,Date thruDate);
	
    BigDecimal[] getInvoiceTotal(Invoice invoice);

    List<Invoice> getFirstInvoice(Patient patient);

    List<Invoice> getAllInvoices(Patient patient);
    
    List<Consultation> getConsultationChargeForPatientCheckedIn(Employee employee,SlotType slotType);
    
   /* List<Consultation> getConsultationChargeForPatientCheckedIn(Speciality speciality,SlotType slotType);*/
    
    List<Invoice> getBillingTransactionForSchedule(Schedule schedule);
	
    LabTestPanel getLabtestPanelByPanelName(String panelName);
    
    List<AcctgTransactionEntry> searchAcctgTransactionEntryForLabReport(Date fromDate,Date thruDate,Object chargeType);
	
    List<AcctgTransactionEntry> searchAcctgTransactionEntryForLabReportExport(Date fromDate,Date thruDate,Object chargeType,Object doctor,Object referral);
    
    Map<String,Object> getPriceFromMasterPriceConf(String serviceType, String mainGroup, String subGroup, 
			String procedureCode, String visitType, String doctor, String tariffCategory, String patientCategory, Date fromDate );
    
    String getTariffCodeByTariffName(String tariffName);

    String getServiceIdFromMasterPriceConf(String procedureCode,String visitType,String providerId,String tariffCategory , String patientCategory );

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

	Object[] getTariffCategoryByTariffCode(String tariffName);

	List<InvoiceItem> searchInvoiceItemByCancelStatus(BillingSearchVO billingSearchVO,Date fromDate,Date thruDate);

}
