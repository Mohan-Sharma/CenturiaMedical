package com.nzion.zkoss.composer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.nzion.domain.Roles;
import com.nzion.util.Infrastructure;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Window;

import com.nzion.domain.Provider;
import com.nzion.domain.Referral;
import com.nzion.domain.Speciality;
import com.nzion.domain.billing.InvoicePayment;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilValidator;

@VariableResolver(DelegatingVariableResolver.class)
public class IncomeReportViewModel {
	
	 @WireVariable
	 private CommonCrudService commonCrudService;
	
	 @WireVariable
	 private BillingService billingService;
	 
	 @Wire("#incomeReportWin")
	 private Window incomeReportWin;
	 
	// private List<Provider> providers;

	 private List<Provider> providers = new ArrayList();

	 private boolean admin = com.nzion.domain.Roles.hasRole(Roles.ADMIN);

	 private boolean doctor = com.nzion.domain.Roles.hasRole(Roles.PROVIDER);
	 
	 private List<Referral> referrals;
	 
	 private Provider selectedProvider;
	 
	 private Referral selectedReferral;
	 
	 private Date fromDate;
	 
	 private Date toDate;
	 
	 private BigDecimal totalPaidAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalInsuranceAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalCorporateAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalPaidAmountDoc = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalInsuranceAmountDoc = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalCorporateAmountDoc = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 
	 private BigDecimal totalPaidAmountRef = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalInsuranceAmountRef = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalCorporateAmountRef = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 
	 
	 private List<Map<String, Object>> paymentList = new ArrayList<Map<String, Object>>();
	 
	 private Map<String, List<Map<String, Object>>> groupByDoctor = new HashMap<>();
	 
	 private Map<String, List<Map<String, Object>>> groupByReferral = new HashMap<>();
	 
	 @Wire("#searchResults")
	 private Panel searchResults;
	 
	 @AfterCompose
     public void init(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, true);
	//	providers = commonCrudService.getAll(Provider.class);
		if(admin) {
			providers = commonCrudService.getAll(Provider.class);
			Provider blankProvider = new Provider();
			blankProvider.setFirstName("All");
			providers.add(0, blankProvider);
		}
		else if(doctor) {
  			providers.add((Provider)com.nzion.util.Infrastructure.getLoggedInPerson()); }

      /*  Provider blankProvider = new Provider();
        blankProvider.setFirstName("All");
        providers.add(0,blankProvider); */
        
        referrals = commonCrudService.getAll(Referral.class, "clinicName");
        Referral referral = new Referral();
        referral.setFirstName("All");
        referrals.add(referral);
        
     }
	 
	 @Command("search")
	 @NotifyChange({"paymentList","totalPaidAmount","totalInsuranceAmount","totalCorporateAmount","groupByDoctor","groupByReferral","totalPaidAmountDoc",
		 "totalInsuranceAmountDoc","totalCorporateAmountDoc","totalPaidAmountRef","totalInsuranceAmountRef","totalCorporateAmountRef"})
	 public void search() {
		 ArrayList<InvoicePayment> invoicePayments = billingService.getInvoicePaymentsByCriteria(null, null, fromDate, toDate);
		 updateListByPatientCondition(invoicePayments);
		 List<Map<String, Object>> listOfMap = buildListOfMap(invoicePayments);
		 paymentList = new ArrayList<>();
		 totalPaidAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 totalInsuranceAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 totalCorporateAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 
		 totalPaidAmountDoc = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 totalInsuranceAmountDoc = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 totalCorporateAmountDoc = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 
		 
		 totalPaidAmountRef = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 totalInsuranceAmountRef = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 totalCorporateAmountRef = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		 
		 groupByDoctor = new HashMap<>();
		 groupByReferral = new HashMap<>();
		 String oldInvoiceId = new String();
 		 for(Map<String, Object> m : listOfMap){
 			 if(m.get("invoiceId").toString().equals(oldInvoiceId)){
 				m.put("billableAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
 			 }else{
 				oldInvoiceId = m.get("invoiceId").toString();
 			 }
 			
 			 totalPaidAmount = totalPaidAmount.add((BigDecimal) m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
 			
 			 totalInsuranceAmount = totalInsuranceAmount.add((BigDecimal) m.get("insuranceAmount")).setScale(3, RoundingMode.HALF_UP);
 			
 			 totalCorporateAmount = totalCorporateAmount.add((BigDecimal) m.get("corporateAmount")).setScale(3, RoundingMode.HALF_UP);
 			
 			 paymentList.add(m);
 		 }
 		 
 		 for(Map<String, Object> map : paymentList){
 			 if(UtilValidator.isEmpty(map.get("doctorName").toString()))
 				continue;
 			
 			 if(groupByDoctor.containsKey(map.get("doctorName").toString())){
 				groupByDoctor.get(map.get("doctorName").toString()).add(map);
 				totalPaidAmountDoc = totalPaidAmountDoc.add((BigDecimal) map.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
 				totalInsuranceAmountDoc = totalInsuranceAmountDoc.add((BigDecimal) map.get("insuranceAmount")).setScale(3, RoundingMode.HALF_UP);
 				totalCorporateAmountDoc = totalCorporateAmountDoc.add((BigDecimal) map.get("corporateAmount")).setScale(3, RoundingMode.HALF_UP);
 			 }else{
 				 List<Map<String, Object>> li = new ArrayList<>();
 				li.add(map);
 				groupByDoctor.put(map.get("doctorName").toString(), li); 
 				totalPaidAmountDoc = totalPaidAmountDoc.add((BigDecimal) map.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
 				totalInsuranceAmountDoc = totalInsuranceAmountDoc.add((BigDecimal) map.get("insuranceAmount")).setScale(3, RoundingMode.HALF_UP);
 				totalCorporateAmountDoc = totalCorporateAmountDoc.add((BigDecimal) map.get("corporateAmount")).setScale(3, RoundingMode.HALF_UP);
 			 }
 		 }
 		 
 		for(Map<String, Object> map : paymentList){
 			 if(UtilValidator.isEmpty(map.get("referralName").toString()))
 				continue;
 			
			 if(groupByReferral.containsKey(map.get("referralName").toString())){
				 groupByReferral.get(map.get("referralName").toString()).add(map);
				 totalPaidAmountRef = totalPaidAmountRef.add((BigDecimal) map.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
	 			 totalInsuranceAmountRef = totalInsuranceAmountRef.add((BigDecimal) map.get("insuranceAmount")).setScale(3, RoundingMode.HALF_UP);
	 			 totalCorporateAmountRef = totalCorporateAmountRef.add((BigDecimal) map.get("corporateAmount")).setScale(3, RoundingMode.HALF_UP);
			 }else{
				 List<Map<String, Object>> li = new ArrayList<>();
				li.add(map);
				groupByReferral.put(map.get("referralName").toString(), li); 
				totalPaidAmountRef = totalPaidAmountRef.add((BigDecimal) map.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
	 			totalInsuranceAmountRef = totalInsuranceAmountRef.add((BigDecimal) map.get("insuranceAmount")).setScale(3, RoundingMode.HALF_UP);
	 			totalCorporateAmountRef = totalCorporateAmountRef.add((BigDecimal) map.get("corporateAmount")).setScale(3, RoundingMode.HALF_UP);
				
			 }
		 }
 		 
 		if(UtilValidator.isNotEmpty(paymentList))
 			searchResults.setVisible(true);
	 		
	 }
	 
	 
	 private void updateListByPatientCondition(ArrayList<InvoicePayment> invoicePayments){
 		ListIterator<InvoicePayment> invoicePaymentsIterator = invoicePayments.listIterator();
 		while (invoicePaymentsIterator.hasNext()) {
			InvoicePayment invoicePayment = invoicePaymentsIterator.next();
			if(selectedProvider != null && selectedProvider.getId() != null && !selectedProvider.equals(invoicePayment.getInvoice().getConsultant()) ){
				invoicePaymentsIterator.remove();
				continue;
			}
			if(selectedReferral != null && selectedReferral.getId() != null && !selectedReferral.getId().equals(invoicePayment.getInvoice().getReferralConsultantId()) ){
				invoicePaymentsIterator.remove();
			}
		}
 	 }
	 
	 private List<Map<String, Object>> buildListOfMap(ArrayList<InvoicePayment> invoicePayments){
 		ArrayList<Map<String, Object>> listOfMap = new ArrayList<Map<String, Object>>();
 		for(InvoicePayment invoicePayment : invoicePayments){
 			Map<String, Object> map = new HashMap<String, Object>();
 			map.put("receiptId", invoicePayment.getId());
 			map.put("receiptNumber", invoicePayment.getReceiptNumber());
 			map.put("paymentDate", invoicePayment.getPaymentDate());
 			map.put("afyaId", invoicePayment.getInvoice().getPatient().getAfyaId());
 			map.put("civilId", invoicePayment.getInvoice().getPatient().getCivilId());
 			map.put("patientName", invoicePayment.getInvoice().getPatient().getFirstName() + " " + invoicePayment.getInvoice().getPatient().getLastName() );
 			map.put("doctorName", "" );
 			Speciality speciality = null;
 			if(invoicePayment.getInvoice().getConsultant() != null){
 				map.put("doctorName", invoicePayment.getInvoice().getConsultant().getFirstName() + " " + invoicePayment.getInvoice().getConsultant().getLastName() );
	 			Set<Speciality> specialitys = ((Provider)invoicePayment.getInvoice().getConsultant()).getSpecialities();
	 			if(UtilValidator.isNotEmpty(specialitys)){
	 				speciality = specialitys.iterator().next(); 
	 			}
 			}
 			map.put("specialityName", speciality != null ? speciality.getDescription() : "");
 			map.put("referralName", "");

 			Long refConsultId = invoicePayment.getInvoice().getReferralConsultantId();
 			if(UtilValidator.isNotEmpty(refConsultId)){
 				Referral referral = commonCrudService.getById(Referral.class, refConsultId);
 				if(referral != null){
 					map.put("referralName", referral.getFirstName() + " " + referral.getLastName());
 				}else{
 					Provider provider = commonCrudService.getById(Provider.class, refConsultId);
 					if(provider != null){
 						map.put("referralName", provider.getFirstName() + " " + provider.getLastName());
 					}
 				}
 			}
 			
 			map.put("mode", invoicePayment.getPaymentType() );
 			map.put("modeDescription", invoicePayment.getPaymentType().getDescription() );
 			map.put("transRefOrCheckNumber", invoicePayment.getChequeOrDdNo() + invoicePayment.getTransactionNumb());
 			map.put("bankName", invoicePayment.getBankName());
 			map.put("checkDate", invoicePayment.getChequeOrDdDate());
 			
 			map.put("invoiceId", invoicePayment.getInvoice().getId());
 			map.put("billableAmount", invoicePayment.getInvoice().getTotalAmount().getAmount());
 			map.put("paidAmount",BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
 			map.put("insuranceAmount",BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
 			map.put("corporateAmount",BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
 			if("OPD_INSURANCE_AMOUNT".equals(invoicePayment.getPaymentType().toString())){
 				map.put("insuranceAmount",invoicePayment.getAmount().getAmount().setScale(3, RoundingMode.HALF_UP) );
 			}else if("OPD_CORPORATE_AMOUNT".equals(invoicePayment.getPaymentType().toString())){
 				map.put("corporateAmount",invoicePayment.getAmount().getAmount().setScale(3, RoundingMode.HALF_UP) );
 			}else{
 				map.put("paidAmount", invoicePayment.getAmount().getAmount().setScale(3, RoundingMode.HALF_UP) );
 			}
 			
 			listOfMap.add(map);
 		}
 		
 		
 		/*Collections.sort(listOfMap, new Comparator<Map<String, Object>>() {
 	 	    public int compare(Map<String, Object> m1, Map<String, Object> m2) {
 	 	        return ((Long)m1.get("invoiceId")).compareTo((Long)m2.get("invoiceId"));
 	 	    }
 	 	});*/
 		
 		return listOfMap;
	 }

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public BillingService getBillingService() {
		return billingService;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public Window getIncomeReportWin() {
		return incomeReportWin;
	}

	public void setIncomeReportWin(Window incomeReportWin) {
		this.incomeReportWin = incomeReportWin;
	}

	public List<Provider> getProviders() {
		return providers;
	}

	public void setProviders(List<Provider> providers) {
		this.providers = providers;
	}

	public Provider getSelectedProvider() {
		return selectedProvider;
	}

	public void setSelectedProvider(Provider selectedProvider) {
		this.selectedProvider = selectedProvider;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public BigDecimal getTotalPaidAmount() {
		return totalPaidAmount;
	}

	public void setTotalPaidAmount(BigDecimal totalPaidAmount) {
		this.totalPaidAmount = totalPaidAmount;
	}

	public BigDecimal getTotalInsuranceAmount() {
		return totalInsuranceAmount;
	}

	public void setTotalInsuranceAmount(BigDecimal totalInsuranceAmount) {
		this.totalInsuranceAmount = totalInsuranceAmount;
	}

	public BigDecimal getTotalCorporateAmount() {
		return totalCorporateAmount;
	}

	public void setTotalCorporateAmount(BigDecimal totalCorporateAmount) {
		this.totalCorporateAmount = totalCorporateAmount;
	}

	public List<Map<String, Object>> getPaymentList() {
		return paymentList;
	}

	public void setPaymentList(List<Map<String, Object>> paymentList) {
		this.paymentList = paymentList;
	}

	public List<Referral> getReferrals() {
		return referrals;
	}

	public void setReferrals(List<Referral> referrals) {
		this.referrals = referrals;
	}

	public Referral getSelectedReferral() {
		return selectedReferral;
	}

	public void setSelectedReferral(Referral selectedReferral) {
		this.selectedReferral = selectedReferral;
	}

	public BigDecimal getTotalPaidAmountDoc() {
		return totalPaidAmountDoc;
	}

	public void setTotalPaidAmountDoc(BigDecimal totalPaidAmountDoc) {
		this.totalPaidAmountDoc = totalPaidAmountDoc;
	}

	public BigDecimal getTotalInsuranceAmountDoc() {
		return totalInsuranceAmountDoc;
	}

	public void setTotalInsuranceAmountDoc(BigDecimal totalInsuranceAmountDoc) {
		this.totalInsuranceAmountDoc = totalInsuranceAmountDoc;
	}

	public BigDecimal getTotalCorporateAmountDoc() {
		return totalCorporateAmountDoc;
	}

	public void setTotalCorporateAmountDoc(BigDecimal totalCorporateAmountDoc) {
		this.totalCorporateAmountDoc = totalCorporateAmountDoc;
	}

	public BigDecimal getTotalPaidAmountRef() {
		return totalPaidAmountRef;
	}

	public void setTotalPaidAmountRef(BigDecimal totalPaidAmountRef) {
		this.totalPaidAmountRef = totalPaidAmountRef;
	}

	public BigDecimal getTotalInsuranceAmountRef() {
		return totalInsuranceAmountRef;
	}

	public void setTotalInsuranceAmountRef(BigDecimal totalInsuranceAmountRef) {
		this.totalInsuranceAmountRef = totalInsuranceAmountRef;
	}

	public BigDecimal getTotalCorporateAmountRef() {
		return totalCorporateAmountRef;
	}

	public void setTotalCorporateAmountRef(BigDecimal totalCorporateAmountRef) {
		this.totalCorporateAmountRef = totalCorporateAmountRef;
	}

	public Map<String, List<Map<String, Object>>> getGroupByDoctor() {
		return groupByDoctor;
	}

	public void setGroupByDoctor(
			Map<String, List<Map<String, Object>>> groupByDoctor) {
		this.groupByDoctor = groupByDoctor;
	}

	public Map<String, List<Map<String, Object>>> getGroupByReferral() {
		return groupByReferral;
	}

	public void setGroupByReferral(
			Map<String, List<Map<String, Object>>> groupByReferral) {
		this.groupByReferral = groupByReferral;
	}

	public Panel getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(Panel searchResults) {
		this.searchResults = searchResults;
	}
	
	 
}
