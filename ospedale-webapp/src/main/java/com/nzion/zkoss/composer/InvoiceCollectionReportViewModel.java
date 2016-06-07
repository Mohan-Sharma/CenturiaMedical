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

import com.nzion.domain.*;
import com.nzion.service.PatientService;
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

import com.nzion.domain.billing.InvoicePayment;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilValidator;

@VariableResolver(DelegatingVariableResolver.class)
public class InvoiceCollectionReportViewModel {
	
	 @WireVariable
	 private CommonCrudService commonCrudService;
	 
	 @WireVariable
	 private BillingService billingService;
	 
	 @Wire("#invoiceCollectionWin")
	 private Window invoiceCollectionWin;
	 
	 @Wire("#searchResults")
	 private Panel searchResults;

    @WireVariable
    private PatientService patientService;
	 
	 private Patient patient;
	 
	 private String patientType;
	 
	 private Date fromDate;
	 
	 private Date toDate;

    private List<Provider> providers = new ArrayList();

    private boolean admin = com.nzion.domain.Roles.hasRole(Roles.ADMIN);

    private boolean doctor = com.nzion.domain.Roles.hasRole(Roles.PROVIDER);

    private Provider selectedProvider;
	 
	 private List<Map<String, Object>> paymentList = new ArrayList<Map<String, Object>>();
	 
	 private BigDecimal totalBillableAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalPaidAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalInsuranceAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private BigDecimal totalCorporateAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	 
	 private String totalDescriptionFooter1 = new String();
	 
	 private String totalDescriptionFooter2 = new String();
	 
	 
	 @AfterCompose
     public void init(@ContextParam(ContextType.VIEW) Component view) {
         Selectors.wireComponents(view, this, true);
         if(admin) {
             providers = commonCrudService.getAll(Provider.class);
             Provider blankProvider = new Provider();
             blankProvider.setFirstName("All");
             providers.add(0, blankProvider);
         }
        else if(doctor) {
             providers.add((Provider)com.nzion.util.Infrastructure.getLoggedInPerson());
         }

     }
	 
	 
 	@Command("search")
    @NotifyChange({"paymentList","totalBillableAmount","totalPaidAmount","totalInsuranceAmount","totalCorporateAmount","totalDescriptionFooter1","totalDescriptionFooter2"})
    public void search() {
 		paymentList = new ArrayList<Map<String, Object>>();
 		totalBillableAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		totalPaidAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		totalInsuranceAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		totalCorporateAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		BigDecimal totalPatientAccount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		BigDecimal totalCashCollection = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		BigDecimal totalDebitCardCollection = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		BigDecimal totalCreditCardCollection = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        BigDecimal totalCheckCollection = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		
 		ArrayList<InvoicePayment> invoicePayments = billingService.getInvoicePaymentsByCriteria(patient, patientType, fromDate, toDate);

 		updateListByPatientCondition(invoicePayments);
 		List<Map<String, Object>> listOfMap = buildListOfMap(invoicePayments);
 		
 		String oldInvoiceId = new String();
 		for(Map<String, Object> m : listOfMap){
 			if(m.get("invoiceId").toString().equals(oldInvoiceId)){
 				m.put("billableAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
 			}else{
 				oldInvoiceId = m.get("invoiceId").toString();
 			}
 			if("OPD_ADVANCE_AMOUNT".equals(m.get("mode").toString())){
 				totalPatientAccount = totalPatientAccount.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
 			}
 			if("OPD_CASH".equals(m.get("mode").toString())){
 				totalCashCollection = totalCashCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
 			}
 			if("OPD_DEBIT_CARD".equals(m.get("mode").toString())){
 				totalDebitCardCollection = totalDebitCardCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
 			}
 			if("OPD_CREDIT_CARD".equals(m.get("mode").toString())){
 				totalCreditCardCollection = totalCreditCardCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
 			}
            if("OPD_PERSONAL_CHEQUE".equals(m.get("mode").toString())){
                totalCheckCollection = totalCheckCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }
 			
 			totalBillableAmount = totalBillableAmount.add((BigDecimal) m.get("billableAmount")).setScale(3, RoundingMode.HALF_UP);
 			
 			totalPaidAmount = totalPaidAmount.add((BigDecimal) m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
 			
 			totalInsuranceAmount = totalInsuranceAmount.add((BigDecimal) m.get("insuranceAmount")).setScale(3, RoundingMode.HALF_UP);
 			
 			totalCorporateAmount = totalCorporateAmount.add((BigDecimal) m.get("corporateAmount")).setScale(3, RoundingMode.HALF_UP);
 			
 			paymentList.add(m);
 		}

        //added to get patient advance deposit and refund amount
        List<PatientDeposit> patientDeposits = patientService.getPatientDepositsByCriteria(patient, fromDate, toDate);
        List<PatientWithDraw> patientWithDraws = patientService.getPatientWithdrawByCriteria(patient, fromDate, toDate);

        updateDepositListByPatientCondition(patientDeposits);
        updateDepositListByPatientCondition(patientWithDraws);

        List<Map<String, Object>> listOfDepositsMap = buildDepositOrWithdrawListOfMap(patientDeposits);
        List<Map<String, Object>> listOfWithdrawMap = buildDepositOrWithdrawListOfMap(patientWithDraws);

        for(Map<String, Object> m : listOfDepositsMap){
            totalPaidAmount = totalPaidAmount.add((BigDecimal) m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            /*if(((BigDecimal)m.get("paidAmount")).compareTo(BigDecimal.ZERO) > 0){
                totalPatientAccount = totalPatientAccount.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }*/
            if("CASH".equals(m.get("mode").toString())){
                totalCashCollection = totalCashCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }else if("DEBIT_CARD".equals(m.get("mode").toString())){
                totalDebitCardCollection = totalDebitCardCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }else if("CREDIT_CARD".equals(m.get("mode").toString())){
                totalCreditCardCollection = totalCreditCardCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }else if("CHEQUE".equals(m.get("mode").toString())){
                totalCheckCollection = totalCheckCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }
            paymentList.add(m);
        }
        for(Map<String, Object> m : listOfWithdrawMap){
            totalPaidAmount = totalPaidAmount.add((BigDecimal) m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);

            if("CASH".equals(m.get("mode").toString())){
                totalCashCollection = totalCashCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }else if("CREDIT_CARD".equals(m.get("mode").toString())){
                totalCreditCardCollection = totalCreditCardCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }else if("CHEQUE".equals(m.get("mode").toString())){
                totalCheckCollection = totalCheckCollection.add((BigDecimal)m.get("paidAmount")).setScale(3, RoundingMode.HALF_UP);
            }
            paymentList.add(m);
        }
        //------patient advance deposit and refund amount end-----

 		StringBuffer buffer1 = new StringBuffer();
 		buffer1.append("Total Collectable = " + totalPaidAmount + " KD ");
 		buffer1.append("Payments from Patient Account = " + totalPatientAccount + " KD ");
 		buffer1.append("Net Collection = " + totalPaidAmount.subtract(totalPatientAccount) + " KD ");
 		
 		StringBuffer buffer2 = new StringBuffer();
 		//buffer2.append("Total Collection = " + totalPaidAmount.add(totalInsuranceAmount).add(totalCorporateAmount) + " KD ");
 		buffer2.append("Cash Collected = " + totalCashCollection + " KD ");
 		buffer2.append("Debit Card = " + totalDebitCardCollection + " KD ");
 		buffer2.append("Credit Card = " + totalCreditCardCollection + " KD ");
 		buffer2.append("Insurance Credit = " + totalInsuranceAmount + " KD ");
 		buffer2.append("Corporate Credit = " + totalCorporateAmount + " KD ");

        buffer2.append("Check Collected = " + totalCheckCollection + " KD ");
 		
 		totalDescriptionFooter1 = buffer1.toString();
 		totalDescriptionFooter2 = buffer2.toString();

 		if(UtilValidator.isNotEmpty(paymentList))
 			searchResults.setVisible(true);
    }
 	
 	private void updateListByPatientCondition(ArrayList<InvoicePayment> invoicePayments){
 		ListIterator<InvoicePayment> invoicePaymentsIterator = invoicePayments.listIterator();
 		while (invoicePaymentsIterator.hasNext()) {
			InvoicePayment invoicePayment = invoicePaymentsIterator.next();
			if(patient != null && !patient.equals(invoicePayment.getInvoice().getPatient()) ){
				invoicePaymentsIterator.remove();
                continue;
			}
			if(UtilValidator.isNotEmpty(patientType)){
				if( !patientType.equals(invoicePayment.getInvoice().getPatient().getPatientType()) ){
					invoicePaymentsIterator.remove();
                    continue;
				}
			}
            if(selectedProvider != null && selectedProvider.getId() != null && !selectedProvider.equals(invoicePayment.getInvoice().getConsultant()) ){
                invoicePaymentsIterator.remove();
                continue;
            }
            if(invoicePayment.getAmount().getAmount().setScale(3, RoundingMode.HALF_UP).compareTo(BigDecimal.ZERO) < 0){
                invoicePaymentsIterator.remove();
            }
		}
 	}

    /*private void updateDepositListByPatientCondition(List<PatientDeposit> patientDeposits){
        ListIterator<PatientDeposit> patientDepositsIterator = patientDeposits.listIterator();
        while (patientDepositsIterator.hasNext()) {
            PatientDeposit patientDeposit = patientDepositsIterator.next();
            if(patient != null && !patient.equals(patientDeposit.getPatient()) ){
                patientDepositsIterator.remove();
                continue;
            }
            if(UtilValidator.isNotEmpty(patientType)){
                if( !patientType.equals(patientDeposit.getPatient().getPatientType()) ){
                    patientDepositsIterator.remove();
                    continue;
                }
            }
            if(patientDeposit.isReturnToPatient()){
                patientDepositsIterator.remove();
            }
        }
    }*/

    private void updateDepositListByPatientCondition(List patientDepositsOrWithdawList){
        ListIterator patientDepositsOrWithdawIterator = patientDepositsOrWithdawList.listIterator();
        while (patientDepositsOrWithdawIterator.hasNext()) {
            Object patientDepositsOrWithdaw = patientDepositsOrWithdawIterator.next();

            if(patientDepositsOrWithdaw instanceof PatientDeposit){
                PatientDeposit patientDeposit = (PatientDeposit)patientDepositsOrWithdaw;

                if(patient != null && !patient.equals(patientDeposit.getPatient()) ){
                    patientDepositsOrWithdawIterator.remove();
                    continue;
                }
                if(UtilValidator.isNotEmpty(patientType)){
                    if( !patientType.equals(patientDeposit.getPatient().getPatientType()) ){
                        patientDepositsOrWithdawIterator.remove();
                        continue;
                    }
                }
                if(patientDeposit.isReturnToPatient()){
                    patientDepositsOrWithdawIterator.remove();
                }
            }else if(patientDepositsOrWithdaw instanceof PatientWithDraw){
                PatientWithDraw patientWithDraw = (PatientWithDraw)patientDepositsOrWithdaw;

                if(patient != null && !patient.equals(patientWithDraw.getPatient()) ){
                    patientDepositsOrWithdawIterator.remove();
                    continue;
                }
                if(UtilValidator.isNotEmpty(patientType)){
                    if( !patientType.equals(patientWithDraw.getPatient().getPatientType()) ){
                        patientDepositsOrWithdawIterator.remove();
                    }
                }
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

            map.put("invoiceNumber", invoicePayment.getInvoice().getInvoiceNumber());
            map.put("doctorName",invoicePayment.getInvoice().getConsultant().getFirstName()+""+invoicePayment.getInvoice().getConsultant().getLastName());
            map.put("fileNumber", invoicePayment.getInvoice().getPatient().getFileNo());
 			if("OPD_CASH".equals(invoicePayment.getPaymentType().toString())){
 				
 			}
 			if("OPD_INSURANCE_AMOUNT".equals(invoicePayment.getPaymentType().toString())){
 				map.put("insuranceAmount",invoicePayment.getAmount().getAmount().setScale(3, RoundingMode.HALF_UP) );
 			}else if("OPD_CORPORATE_AMOUNT".equals(invoicePayment.getPaymentType().toString())){
 				map.put("corporateAmount",invoicePayment.getAmount().getAmount().setScale(3, RoundingMode.HALF_UP) );
 			}else{
 				map.put("paidAmount", invoicePayment.getAmount().getAmount().setScale(3, RoundingMode.HALF_UP) );
 			}
 			
 			listOfMap.add(map);
 		}
 		
 		
 		Collections.sort(listOfMap, new Comparator<Map<String, Object>>() {
 	 	    public int compare(Map<String, Object> m1, Map<String, Object> m2) {
 	 	        return ((Long)m1.get("invoiceId")).compareTo((Long)m2.get("invoiceId"));
 	 	    }
 	 	});
 		
 		return listOfMap;
 	}

    /*private List<Map<String, Object>> buildDepositListOfMap(List<PatientDeposit> patientDeposits){
        PatientWithDraw patientWithDraw = new PatientWithDraw();
        patientWithDraw.ge
        ArrayList<Map<String, Object>> listOfMap = new ArrayList<Map<String, Object>>();
        for(PatientDeposit patientDeposit : patientDeposits){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("receiptId", patientDeposit.getId());
            map.put("receiptNumber", patientDeposit.getId());
            map.put("paymentDate", patientDeposit.getDepositDate());
            map.put("afyaId", patientDeposit.getPatient().getAfyaId());
            map.put("civilId", patientDeposit.getPatient().getCivilId());
            map.put("patientName", patientDeposit.getPatient().getFirstName() + " " + patientDeposit.getPatient().getLastName() );
            map.put("mode", patientDeposit.getDepositMode());
            map.put("modeDescription", patientDeposit.getDepositMode());
            map.put("transRefOrCheckNumber", patientDeposit.getTxnNumber());
            map.put("bankName", patientDeposit.getBankName());
            map.put("checkDate", patientDeposit.getChequeDate());
            if (patientDeposit.getInvoice() != null) {
                map.put("invoiceId", patientDeposit.getInvoice().getId());
            }else{
                map.put("invoiceId", "");
            }
            map.put("billableAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
            if(patientDeposit.isReturnToPatient()) {
                map.put("paidAmount", patientDeposit.getDepositAmount().setScale(3, RoundingMode.HALF_UP).negate());
            }else {
                map.put("paidAmount", patientDeposit.getDepositAmount().setScale(3, RoundingMode.HALF_UP));
            }
            map.put("insuranceAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
            map.put("corporateAmount",BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));

            listOfMap.add(map);
        }


        Collections.sort(listOfMap, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                if ((m1.get("invoiceId").equals("")) || (m2.get("invoiceId").equals("")) ){
                    return 0;
                }
                return ((Long)m1.get("invoiceId")).compareTo((Long)m2.get("invoiceId"));
            }
        });

        return listOfMap;
    }*/
    private List<Map<String, Object>> buildDepositOrWithdrawListOfMap(List patientDeposits){

        ArrayList<Map<String, Object>> listOfMap = new ArrayList<Map<String, Object>>();

        for(Object patientDepositOrWithdraw : patientDeposits){
            if (patientDepositOrWithdraw.getClass().equals(PatientDeposit.class)) {
                PatientDeposit patientDeposit = (PatientDeposit) patientDepositOrWithdraw;

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("receiptId", patientDeposit.getId());
                map.put("receiptNumber", patientDeposit.getId());
                map.put("paymentDate", patientDeposit.getDepositDate());
                map.put("afyaId", patientDeposit.getPatient().getAfyaId());
                map.put("civilId", patientDeposit.getPatient().getCivilId());
                map.put("patientName", patientDeposit.getPatient().getFirstName() + " " + patientDeposit.getPatient().getLastName());
                map.put("mode", patientDeposit.getDepositMode());
                map.put("modeDescription", patientDeposit.getDepositMode());
                map.put("transRefOrCheckNumber", patientDeposit.getTxnNumber());
                map.put("bankName", patientDeposit.getBankName());
                map.put("checkDate", patientDeposit.getChequeDate());
                if (patientDeposit.getInvoice() != null) {
                    map.put("invoiceId", patientDeposit.getInvoice().getId());
                } else {
                    map.put("invoiceId", "");
                }
                map.put("billableAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
                if (patientDeposit.isReturnToPatient()) {
                    map.put("paidAmount", patientDeposit.getDepositAmount().setScale(3, RoundingMode.HALF_UP).negate());
                } else {
                    map.put("paidAmount", patientDeposit.getDepositAmount().setScale(3, RoundingMode.HALF_UP));
                }
                map.put("insuranceAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
                map.put("corporateAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
                if (patientDeposit.getInvoice() != null) {
                    map.put("invoiceNumber", patientDeposit.getInvoice().getInvoiceNumber());
                } else {
                    map.put("invoiceNumber", "");
                }
                map.put("fileNumber", patientDeposit.getPatient().getFileNo());

                listOfMap.add(map);
            }else if(patientDepositOrWithdraw.getClass().equals(PatientWithDraw.class)){
                PatientWithDraw patientWithDraw = (PatientWithDraw) patientDepositOrWithdraw;

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("receiptId", patientWithDraw.getId());
                map.put("receiptNumber", patientWithDraw.getId());
                map.put("paymentDate", patientWithDraw.getWithdrawDate());
                map.put("afyaId", patientWithDraw.getPatient().getAfyaId());
                map.put("civilId", patientWithDraw.getPatient().getCivilId());
                map.put("patientName", patientWithDraw.getPatient().getFirstName() + " " + patientWithDraw.getPatient().getLastName());
                map.put("mode", patientWithDraw.getWithdrawMode());
                map.put("modeDescription", patientWithDraw.getWithdrawMode());
                map.put("transRefOrCheckNumber", "");
                map.put("bankName", "");
                map.put("checkDate", null);
                map.put("invoiceId", "");
                map.put("billableAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
                map.put("paidAmount", patientWithDraw.getWithdrawAmount().setScale(3, RoundingMode.HALF_UP).negate());
                map.put("insuranceAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
                map.put("corporateAmount", BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));

                map.put("invoiceNumber", "");
    map.put("fileNumber", patientWithDraw.getPatient().getFileNo());

                listOfMap.add(map);
            }
        }


        Collections.sort(listOfMap, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                if ((m1.get("invoiceId").equals("")) || (m2.get("invoiceId").equals("")) ){
                    return 0;
                }
                return ((Long)m1.get("invoiceId")).compareTo((Long)m2.get("invoiceId"));
            }
        });

        return listOfMap;
    }
 	

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}


	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}


	public Window getInvoiceCollectionWin() {
		return invoiceCollectionWin;
	}


	public void setInvoiceCollectionWin(Window invoiceCollectionWin) {
		this.invoiceCollectionWin = invoiceCollectionWin;
	}


	public Patient getPatient() {
		return patient;
	}


	public void setPatient(Patient patient) {
		this.patient = patient;
	}


	public String getPatientType() {
		return patientType;
	}


	public void setPatientType(String patientType) {
		this.patientType = patientType;
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


	public BillingService getBillingService() {
		return billingService;
	}


	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}


	public List<Map<String, Object>> getPaymentList() {
		return paymentList;
	}


	public void setPaymentList(List<Map<String, Object>> paymentList) {
		this.paymentList = paymentList;
	}


	public BigDecimal getTotalBillableAmount() {
		return totalBillableAmount;
	}


	public void setTotalBillableAmount(BigDecimal totalBillableAmount) {
		this.totalBillableAmount = totalBillableAmount;
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


	public String getTotalDescriptionFooter1() {
		return totalDescriptionFooter1;
	}


	public void setTotalDescriptionFooter1(String totalDescriptionFooter1) {
		this.totalDescriptionFooter1 = totalDescriptionFooter1;
	}


	public String getTotalDescriptionFooter2() {
		return totalDescriptionFooter2;
	}


	public void setTotalDescriptionFooter2(String totalDescriptionFooter2) {
		this.totalDescriptionFooter2 = totalDescriptionFooter2;
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


	
}
