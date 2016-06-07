package com.nzion.zkoss.composer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;

import net.sf.jasperreports.engine.JRException;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

import com.nzion.domain.AfyaClinicDeposit;
import com.nzion.domain.Enumeration;
import com.nzion.domain.PatientInsurance;
import com.nzion.domain.UserLogin;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransaction;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.billing.DebitCreditEnum;
import com.nzion.domain.billing.Invoice;
import com.nzion.domain.billing.Invoice.INSURANCESTATUS;
import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.billing.InvoicePayment;
import com.nzion.domain.billing.InvoiceStatusItem;
import com.nzion.domain.billing.InvoiceType;
import com.nzion.domain.billing.PaymentType;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.exception.TransactionException;
import com.nzion.service.PatientService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.billing.InvoiceManager;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.util.JasperReportUtil;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;

public class BillingController extends OspedaleAutowirableComposer {

    public static final BigDecimal ONEHUNDERT = new BigDecimal(100);

    private BillingService billingService;

    private CommonCrudService commonCrudService;

    private BillingDisplayConfig billingDisplayConfig;
    
    private PatientService patientService;

    private Invoice invoice;

    private boolean finalizeInvoice;

    private InvoicePayment invoicePayment;

    private boolean paymentReceived;
    
    private boolean allowEdit;

    private Map<String, List<InvoiceItem>> invoiceItemMap;

    private Map<String, List<InvoiceItem>> modifyInvoiceItemMap;

    private Map<String, List<InvoiceItem>> invPrevBalanceItemMap;

    private Map<String, List<InvoiceItem>> invoiceDeductibleItemMap;

    private Map<String, List<InvoiceItem>> invoicePrevAdvanceItemMap;

    private List<LabOrderInvoice> labInvoiceItemList;

    private BigDecimal totalAmtTillDate;

    private BigDecimal overallBalance;

    private BigDecimal remainingAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    private BigDecimal writeOffAmount;

    private PatientSoapNote patientSoapNote;

    private SoapNoteService soapNoteService;

    private Money labItemTotalAmount = new Money();

    private TreeMap<String, List<InvoiceItem>> quickBillinvoiceItemMap;

    BigDecimal insurancePayment = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    BigDecimal coPayment = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    
    BigDecimal copyInsurancePayment = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    BigDecimal copyCoPayment = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    
    private BigDecimal advanceAmount = BigDecimal.ZERO;
    
    private BigDecimal deductAdvanceAmount = BigDecimal.ZERO;
    
    private BigDecimal depositAdvanceAmount = BigDecimal.ZERO;
    
    private boolean corporateOrPatient = false;
    
    private boolean corporate = false;
    
    private List<Map<String, Object>> tariffCategorys = new ArrayList<Map<String,Object>>();
    
    private BigDecimal cashAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    
    private BigDecimal insuranceAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    
    private BigDecimal totalMaxCopayAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
    
    private String insuranceName = "";
    
    public TreeMap<String, List<InvoiceItem>> getQuickBillinvoiceItemMap() {
        return quickBillinvoiceItemMap;
    }

    public void setQuickBillinvoiceItemMap(
            TreeMap<String, List<InvoiceItem>> quickBillinvoiceItemMap) {
        this.quickBillinvoiceItemMap = quickBillinvoiceItemMap;
    }

    public BillingController() throws ParseException {
        invoice = (Invoice) Executions.getCurrent().getArg().get("invoiceObj");
        String invoiceId = null;
        if (invoice != null)
            invoice = (Invoice) Executions.getCurrent().getArg().get("invoiceObj");
        else {
            invoiceId = Executions.getCurrent().getParameter("invoiceId");
            if (UtilValidator.isNotEmpty(invoiceId)) {
                NumberFormat nf = NumberFormat.getInstance();
                Number number = nf.parse(invoiceId);
                invoice = commonCrudService.getById(Invoice.class, number.longValue());

            }
        }
        if (invoice != null)
            extractInvoiceToDisplay(invoice);
        if (invoicePayment.getAmount().getAmount() != null)
            buildRemainingAmount();

        if (UtilValidator.isNotEmpty(invoice.getWrittenOffAmount()))
            writeOffAmount = invoice.getWrittenOffAmount().getAmount();
        if(invoice.getItemType().equals(PatientSoapNote.class.getName())){
            patientSoapNote = commonCrudService.getById(PatientSoapNote.class,Long.valueOf(invoice.getItemId()));
        }
        getCopaymentAndInsurancePayment();
        calculatePatientAdvanceAmount();
        
        if( "CORPORATE".equals(invoice.getPatient().getPatientType()) ){
        	corporateOrPatient = true;
        	corporate = true;
        	tariffCategorys = patientService.getTariffCategoryByPatientCategory("CASH PAYING");
        }else if("CASH PAYING".equals(invoice.getPatient().getPatientType())){
        	corporateOrPatient = true;
        	corporate = false;
        	tariffCategorys = patientService.getTariffCategoryByPatientCategory("CASH PAYING");
        }else{
        	corporateOrPatient = false;
        	corporate = false;
        }

        calculateCashInsuranceCorporateAmount();

        invoice.setPatientPayable(coPayment);
        invoice.setInsurancePayable(insurancePayment);

        if(coPayment.compareTo(BigDecimal.ZERO) == 0 && "CASH".equals(invoice.getPatient().getPatientType())
        		&& ( "READY".equals(invoice.getInvoiceStatus()) || "INPROCESS".equals(invoice.getInvoiceStatus()) )){
             invoice.setInvoiceStatus("RECEIVED");
             commonCrudService.save(invoice);
        }else if(coPayment.compareTo(BigDecimal.ZERO) == 0 && 
        		("INSURANCE".equals(invoice.getPatient().getPatientType()) || "CORPORATE".equals(invoice.getPatient().getPatientType()) )
        		&& ( "READY".equals(invoice.getInvoiceStatus()) || "INPROCESS".equals(invoice.getInvoiceStatus()) ) ){
        	 invoice.setInvoiceStatus("PATIENT_PAID");
             commonCrudService.save(invoice);
        } else {
            commonCrudService.save(invoice);
        }

    }

    public BillingController(Invoice invoice){
        this.invoice = invoice;
    }

    public void getCopaymentAndInsurancePayment(){
        coPayment = BigDecimal.ZERO;
        insurancePayment = BigDecimal.ZERO;
        insurancePayment.setScale(3, RoundingMode.HALF_UP);
        for(InvoiceItem ii : invoice.getInvoiceItems()){
            if(ii.getIsStatusCancel())
                continue;
            if("NET".equals(ii.getComputeType())){
                caculateCoPayAndInsuranceAmountInPercent(ii, ii.getNetPrice());
            }else{
                caculateCoPayAndInsuranceAmount(ii, ii.getGrossAmount());
            }
        }
        if(coPayment.compareTo(BigDecimal.ZERO) == 0){
    		if(cashAmount.compareTo(BigDecimal.ZERO) <= 0 && invoice.isAmountRefundedToPatient()){
            	cashAmount = BigDecimal.ZERO;
            	copyCoPayment = BigDecimal.ZERO;
            	coPayment = BigDecimal.ZERO;
            	
            }else{
            	coPayment = invoice.getTotalAmount().getAmount().subtract(insurancePayment);
            }
        }
        if("CASH PAYING".equals(invoice.getPatient().getPatientType()) ){
            insurancePayment = BigDecimal.ZERO;
        }
    }

    private void caculateCoPayAndInsuranceAmountInPercent(InvoiceItem ii,BigDecimal grossOrNetAmount){
    	if(grossOrNetAmount.compareTo(BigDecimal.ZERO) == 0)
    		return;
    	
        BigDecimal lineCoPayPercent = ii.getCopayCalculatedAmount() != null ? ii.getCopayCalculatedAmount() : BigDecimal.ZERO;
        if(lineCoPayPercent.compareTo(BigDecimal.ZERO) == 0 && ii.getCopayPercentage() != null){
        	lineCoPayPercent = percentage(grossOrNetAmount,ii.getCopayPercentage());
        }
        if(lineCoPayPercent.compareTo(BigDecimal.ZERO) > 0){
	    	BigDecimal deductableAmount = ii.getDeductableAmount() != null ? ii.getDeductableAmount() : BigDecimal.ZERO;
	        if(deductableAmount.compareTo(BigDecimal.ZERO) == 0 && ii.getDeductablePercentage() != null){
	           deductableAmount = percentage(grossOrNetAmount,ii.getDeductablePercentage());
	        }
            lineCoPayPercent = lineCoPayPercent.add(deductableAmount);
            //BigDecimal percentAmount = percentage(grossOrNetAmount,lineCoPayPercent);
            
        	//lineCoPayPercent = lineCoPayPercent.add(grossOrNetAmount);
        	
        	if(cashAmount.compareTo(BigDecimal.ZERO) <= 0 && invoice.isAmountRefundedToPatient()){
            	cashAmount = BigDecimal.ZERO;
            	copyCoPayment = BigDecimal.ZERO;
            	coPayment = BigDecimal.ZERO;
            	
            }else{
            	coPayment = coPayment.add(lineCoPayPercent);
            }
        	
            insurancePayment = insurancePayment.add(grossOrNetAmount.subtract(lineCoPayPercent));
        }
    }

    private void caculateCoPayAndInsuranceAmount(InvoiceItem ii,BigDecimal grossOrNetAmount){
    	if(grossOrNetAmount.compareTo(BigDecimal.ZERO) == 0)
    		return;
    	
        BigDecimal lineCoPay = ii.getCopayCalculatedAmount() != null ? ii.getCopayCalculatedAmount() : BigDecimal.ZERO;
        
        BigDecimal deductableAmount = ii.getDeductableAmount() != null ? ii.getDeductableAmount() : BigDecimal.ZERO;
        if(deductableAmount.compareTo(BigDecimal.ZERO) == 0 && ii.getDeductablePercentage() != null){
        	deductableAmount = percentage(grossOrNetAmount,ii.getDeductablePercentage());
        }
        BigDecimal lineTotalCoPay = BigDecimal.ZERO;
        if(lineCoPay.compareTo(BigDecimal.ZERO) == 0 && ii.getCopayPercentage() != null){
        	BigDecimal amount = grossOrNetAmount.subtract(deductableAmount);
        	lineCoPay = percentage(amount,ii.getCopayPercentage());

            BigDecimal insAmount = ii.getGrossAmount().subtract(lineCoPay.add(ii.getDeductableAmountOrPercent()));
            if(ii.getConcessionAmount() != null && ii.getConcessionAmount().compareTo(BigDecimal.ZERO) > 0)
                lineCoPay = ii.getGrossAmount().subtract(insAmount).subtract(ii.getConcessionAmount());
            if(lineCoPay.compareTo(BigDecimal.ZERO) <= 0)
                lineCoPay = BigDecimal.ZERO;

        	lineTotalCoPay = lineCoPay.add(deductableAmount);
        }else{
        	lineTotalCoPay = lineCoPay.add(deductableAmount);
        }
        BigDecimal lineTotalInsurance = BigDecimal.ZERO;
        
        BigDecimal maxAmount = ii.getMaxAmount();
    	if( maxAmount.compareTo(BigDecimal.ZERO) != 0 && maxAmount.compareTo(grossOrNetAmount) <= 0){
    		lineTotalCoPay = grossOrNetAmount.subtract(ii.getInsuranceAmount());
    		lineTotalInsurance = grossOrNetAmount.subtract(lineTotalCoPay);
    	}else{
	    	//lineTotalCoPay = lineTotalCoPay.add(grossOrNetAmount);
	        lineTotalInsurance = ii.getNetPrice().subtract(lineTotalCoPay);
    	}
        
        if(lineTotalInsurance.compareTo(BigDecimal.ZERO) < 0)
            insurancePayment = BigDecimal.ZERO;
        else
            insurancePayment = insurancePayment.add(lineTotalInsurance);
        
        if(cashAmount.compareTo(BigDecimal.ZERO) <= 0 && invoice.isAmountRefundedToPatient()){
        	cashAmount = BigDecimal.ZERO;
        	copyCoPayment = BigDecimal.ZERO;
        	coPayment = BigDecimal.ZERO;
        	
        }else{
        	coPayment = coPayment.add(lineTotalCoPay);
        }
        
        totalMaxCopayAmount = totalMaxCopayAmount.add(ii.getTotalMaxCopayAmount());
       /* if(ii.getCopayCalculatedAmount() == null && "CORPORATE".equals(ii.getInvoice().getPatient().getPatientType())){
    		String tariffCategory = ii.getInvoice().getPatient().getPatientCorporate().getTariffCategoryId();
    		coPayment = BigDecimal.ZERO;
    	}*/
    }
    
    public String getTotalMaxCopayAmountStr(){
    	if(totalMaxCopayAmount.compareTo(BigDecimal.ZERO) > 0){
    		return "(+" + totalMaxCopayAmount.setScale(3,RoundingMode.HALF_UP) + ")";
    	}
    	return "";
    }

    private void updateConcessionPrice(){
        BigDecimal grossTotal = BigDecimal.ZERO;
        BigDecimal concessionTotal = BigDecimal.ZERO;
        for (InvoiceItem ii : invoice.getInvoiceItems()) {
            if(ii.getIsStatusCancel())
                continue;
            if(!ii.getAuthorization()){
            	ii.setAuthorizationAmount(null);
              	ii.setAuthorizationDate(null);
              	ii.setAuthorizationNo("");
              	ii.setAuthorizationNote("");
            }
            BigDecimal concessionAmount = ii.getConcessionAmount() == null ? BigDecimal.ZERO : ii.getConcessionAmount();
            BigDecimal grossAmount = ii.getGrossAmount();
            if ("PERCENTAGE".equals(ii.getConcessionType())){
                concessionAmount = percentage(grossAmount,ii.getConcessionAmount());
            }
            ii.setNetPrice(grossAmount.subtract(concessionAmount));
            if( ("CASH PAYING".equals(invoice.getPatient().getPatientType()) ) || "OPD_REGISTRATION".equals(ii.getItemType().name()) ) {
                ii.setCopayAmount(ii.getNetPrice().setScale(3, RoundingMode.HALF_UP));
            }
            grossTotal = grossTotal.add(ii.getPrice().getAmount());
            if(!ii.isConcessionApplied())
                concessionTotal = concessionTotal.add(concessionAmount);
            if(concessionAmount.compareTo(BigDecimal.ZERO) > 0)
                ii.setConcessionApplied(true);
        }
        BigDecimal totalAmountAfterDiscount = invoice.getTotalAmount().getAmount().subtract(concessionTotal);
        invoice.setTotalAmount(new Money(totalAmountAfterDiscount, invoice.getTotalAmount().getCurrency()));
    }
    
    public void cancelLineItemConformation(final Object ii){
    	final BillingController blcController = this;
    	
    	if(invoice.getSchedule() != null ){
    		com.nzion.util.UtilMessagesAndPopups.showError("You cannot cancel Encounter Invoice");
    		return;
    	}
        
    	UtilMessagesAndPopups.confirm("Are You Sure? Once cancelled cannot be reverted.", "Cancel Confirmation", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, 
			new org.zkoss.zk.ui.event.EventListener<Event>() {
			public void onEvent(Event event) {
					if ("onYes".equals(event.getName())) {
						Executions.createComponents("/billing/insuranceCancelModelWin.zul", null, com.nzion.util.UtilMisc.toMap("invoiceItem", ii,"blcController",blcController ));
					}
				}										
			 }
		);
    }

    public void cancelInvoice(Object obj){
    	if(obj instanceof InvoiceItem){
    		InvoiceItem ii = (InvoiceItem) obj;
    		cancelInvoiceLineItem(ii);
    	}
    	if(obj instanceof Invoice){
            for(InvoiceItem ii : invoice.getInvoiceItems()){
            	cancelInvoiceLineItem(ii);
            }
            com.nzion.domain.product.common.Money money = new com.nzion.domain.product.common.Money(java.math.BigDecimal.ZERO);
            invoice.setTotalAmount(money);
            invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
            invoice.setAmountRefundedToPatient(false);
            commonCrudService.save(invoice);
    	}
    	com.nzion.util.UtilMessagesAndPopups.showSuccess();
    }
    
    private void cancelInvoiceLineItem(InvoiceItem ii){
    	com.nzion.domain.product.common.Money price = invoice.getTotalAmount();
        price.setAmount(price.getAmount().subtract(ii.getPriceValue()));
        invoice.setTotalAmount(price);
        ii.setInvoiceItemStatus("Cancel");
        ii.setFactor(BigDecimal.ZERO);
        ii.setNetPrice(BigDecimal.ZERO);
        ii.setCopayAmount(BigDecimal.ZERO);
        ii.setDeductableAmount(BigDecimal.ZERO);
        ii.setGrossAmount(BigDecimal.ZERO);
        Money money = new Money(BigDecimal.ZERO);
        ii.setPrice(money);
        commonCrudService.save(ii);
        invoice.setAmountRefundedToPatient(false);
        commonCrudService.save(invoice);
    }


    public static BigDecimal percentage(BigDecimal base, BigDecimal pct){
        if(UtilValidator.isEmpty(base) || UtilValidator.isEmpty(pct))
            return BigDecimal.ZERO;
        return base.multiply(pct).divide(ONEHUNDERT);
    }

    private BigDecimal totalGrossAmount;

    private BigDecimal totalNetAmount;

    private BigDecimal totalDeductable;

    private BigDecimal totalCopayPatient;

    private BigDecimal totalCopayInsurance;

    public BigDecimal getTotalGrossAmount(){
        totalGrossAmount = BigDecimal.ZERO;
        for(InvoiceItem ii : invoice.getInvoiceItems()){
        	if(!"Cancel".equals(ii.getInvoiceItemStatus()))
            totalGrossAmount = totalGrossAmount.add(ii.getPrice().getAmount().setScale(3, RoundingMode.HALF_UP));
        }
        return totalGrossAmount.setScale(3, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalNetAmount() {
        totalNetAmount = BigDecimal.ZERO;
        for(InvoiceItem ii : invoice.getInvoiceItems()){
            totalNetAmount = totalNetAmount.add(ii.getNetPrice());
        }
        return totalNetAmount.setScale(3, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalDeductable() {
        totalDeductable = BigDecimal.ZERO;
        for(InvoiceItem ii : invoice.getInvoiceItems()){
        	
        	BigDecimal deductableAmount = ii.getDeductableAmount() != null ? ii.getDeductableAmount() : BigDecimal.ZERO;
            if(deductableAmount.compareTo(BigDecimal.ZERO) == 0 && ii.getDeductablePercentage() != null){
            	
            	BigDecimal actualNetAmount = ii.getNetPrice();
            	BigDecimal maxAmount = ii.getMaxAmount();
            	if( maxAmount.compareTo(BigDecimal.ZERO) != 0 && maxAmount.compareTo(actualNetAmount) <= 0){
            		actualNetAmount = maxAmount;
            	}
            	
            	deductableAmount = percentage(actualNetAmount,ii.getDeductablePercentage());
            }
            
            totalDeductable = totalDeductable.add(deductableAmount.setScale(3, RoundingMode.HALF_UP) );
        }
        return totalDeductable.setScale(3, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalCopayPatient() {
        totalCopayPatient = BigDecimal.ZERO;
        for(InvoiceItem ii : invoice.getInvoiceItems()){
        	if(ii.getNetPrice().compareTo(BigDecimal.ZERO) > 0 ){
	        	BigDecimal lineCoPay = ii.getCopayCalculatedAmount() != null ? ii.getCopayCalculatedAmount() : BigDecimal.ZERO;
	            if(lineCoPay.compareTo(BigDecimal.ZERO) == 0 && ii.getCopayPercentage() != null){
	            	lineCoPay = ii.getCopayAmountOrPercent();
	            }
	            
	            totalCopayPatient = totalCopayPatient.add(lineCoPay.setScale(3, RoundingMode.HALF_UP) );
        	}
        }
        return totalCopayPatient.setScale(3, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalCopayInsurance() {
        totalCopayInsurance = BigDecimal.ZERO;
        for(InvoiceItem ii : invoice.getInvoiceItems()){
            totalCopayInsurance = totalCopayInsurance.add(ii.getInsuranceAmount() != null ? ii.getInsuranceAmount() : BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
        }
        return totalCopayInsurance.setScale(3, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getCopayAmountOrPercent(BigDecimal price, BigDecimal copay,BigDecimal copayPercent, BigDecimal deductable){
    	BigDecimal lineCoPay = copay != null ? copay : BigDecimal.ZERO;
        if(lineCoPay.compareTo(BigDecimal.ZERO) == 0 && copayPercent != null){
        	lineCoPay = percentage(price.subtract(deductable),copayPercent);
        }
        return lineCoPay.setScale(3,RoundingMode.HALF_UP);
    }

    public void extractInvoiceToDisplay(Invoice inv) {
        invoice = inv;
        InvoiceManager manager = billingService.getManager(invoice);
        if (manager != null) {
            invoiceItemMap = new TreeMap<String, List<InvoiceItem>>(manager.getItemTypeComparator());
            invoiceDeductibleItemMap = new TreeMap<String, List<InvoiceItem>>(manager.getItemTypeComparator());
            invoicePrevAdvanceItemMap = new TreeMap<String, List<InvoiceItem>>(manager.getItemTypeComparator());
            invPrevBalanceItemMap = new TreeMap<String, List<InvoiceItem>>(manager.getItemTypeComparator());
            invoicePayment = new InvoicePayment();
            billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
            paymentReceived = (InvoiceStatusItem.RECEIVED.toString().equals(invoice.getInvoiceStatus()) || InvoiceStatusItem.WRITEOFF.toString().equals(invoice.getInvoiceStatus()));
            List result = commonCrudService.findByEquality(AcctgTransaction.class, new String[]{"invoiceId"}, new Object[]{invoice.getId()});
            if (result.size() > 0) {
                finalizeInvoice = false;
            }
            remainingAmount = invoice.getTotalAmount().getAmount().subtract(invoice.getCollectedAmount().getAmount());
            builItemMap();
        } else {
            invoiceItemMap = new TreeMap<String, List<InvoiceItem>>();
            quickBillinvoiceItemMap = new TreeMap<String, List<InvoiceItem>>();
            invoicePayment = new InvoicePayment();
            billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
            paymentReceived = (InvoiceStatusItem.RECEIVED.toString().equals(invoice.getInvoiceStatus()) || InvoiceStatusItem.WRITEOFF.toString().equals(invoice.getInvoiceStatus()));
            List result = commonCrudService.findByEquality(AcctgTransaction.class, new String[]{"invoiceId"}, new Object[]{invoice.getId()});
            if (result.size() > 0) {
                finalizeInvoice = false;
            }
            remainingAmount = invoice.getTotalAmount().getAmount().subtract(invoice.getCollectedAmount().getAmount());
            builItemMap();
        }
    }
    
    public void calculatePatientAdvanceAmount(){
		List<AcctgTransactionEntry> accTransDebit = commonCrudService.findByEquality(AcctgTransactionEntry.class, new String[]{"patientId","transactionType","debitOrCredit"}, new Object[]{invoice.getPatient().getId().toString(),AcctgTransTypeEnum.PATIENT_DEPOSIT,DebitCreditEnum.DEBIT});
	    List<AcctgTransactionEntry> accTransCredit = commonCrudService.findByEquality(AcctgTransactionEntry.class, new String[]{"patientId","transactionType","debitOrCredit"}, new Object[]{invoice.getPatient().getId().toString(),AcctgTransTypeEnum.PATIENT_WITHDRAW,DebitCreditEnum.CREDIT});
	    BigDecimal debitAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	    BigDecimal creditAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
	    for(AcctgTransactionEntry acc : accTransDebit){
	    	if(acc.getAmount() != null)
	    	debitAmount = debitAmount.add(acc.getAmount());
	    }
	    for(AcctgTransactionEntry acc : accTransCredit){
	    	if(acc.getAmount() != null)
	    		creditAmount = creditAmount.add(acc.getAmount());
	    }
	    if(debitAmount.compareTo(BigDecimal.ZERO) > 0)
	    	advanceAmount = debitAmount.subtract(creditAmount);
	}


    public boolean isFinalizeInvoice() {
        return finalizeInvoice;
    }

    public void setFinalizeInvoice(boolean finalizeInvoice) {
        this.finalizeInvoice = finalizeInvoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
    
    public BigDecimal getAdvanceAmount() {
		return advanceAmount;
	}

	public void setAdvanceAmount(BigDecimal advanceAmount) {
		this.advanceAmount = advanceAmount;
	}

	@SuppressWarnings("null")
    public void builItemMap() {
        List<InvoiceItem> nonDeductibleItems = null;
        List<InvoiceItem> deductibleItems = null;
        List<InvoiceItem> advanceItems = null;
        List<InvoiceItem> invPrevBalanceItems = null;
        List<InvoiceItem> invQuickBillItems = null;
        if (invoice.getInvoiceItems() != null) {
            for (InvoiceItem invoiceItem : invoice.getInvoiceItems()) {
                if (invoiceItem.getItemType().name().equals("PREVIOUSBALANCE")) {
                    invPrevBalanceItems = UtilValidator.isEmpty(invoicePrevAdvanceItemMap.get(invoiceItem.getItemType().name())) ? new ArrayList<InvoiceItem>() : invoicePrevAdvanceItemMap.get(invoiceItem.getItemType().name());
                    invPrevBalanceItems.add(invoiceItem);
                    invPrevBalanceItemMap.put(invoiceItem.getItemType().name(), invPrevBalanceItems);
                } else {
                    nonDeductibleItems = UtilValidator.isEmpty(invoiceItemMap.get(invoiceItem.getItemType().name())) ? new ArrayList<InvoiceItem>() : invoiceItemMap.get(invoiceItem.getItemType().name());
                    nonDeductibleItems.add(invoiceItem);
                    invoiceItemMap.put(invoiceItem.getItemType().name(), nonDeductibleItems);
                }
            }
        }

    }

    public BigDecimal buildRemainingAmount() {
        Set<InvoicePayment> invoicePayments = invoice.getInvoicePayments();
        BigDecimal allPaidAmonut = BigDecimal.ZERO;
        for (InvoicePayment invoicePayment : invoicePayments) {
            allPaidAmonut = allPaidAmonut.add(invoicePayment.getAmount().getAmount());
        }
        remainingAmount = invoice.getTotalAmount().getAmount().subtract(allPaidAmonut);
        if (UtilValidator.isNotEmpty(invoice.getWrittenOffAmount()))
            remainingAmount = remainingAmount.subtract(invoice.getWrittenOffAmount().getAmount());
        return remainingAmount;
    }
    
    public void approveCorporateAmount() throws InterruptedException{
    		invoicePayment.setAmount(new Money(insuranceAmount));
    		Enumeration enumeration = commonCrudService.getByUniqueValue(Enumeration.class, "enumCode", "CORPORATE_AMOUNT");
    		invoicePayment.setPaymentMethod(enumeration);
    		addTxnPaymentItem();
    		if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount())) {
                if ((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == 0)) {
                		receivePayment();
                }
    		}else{
    			Executions.sendRedirect(null);
    		}
    }
    
    
    public void approveInsuranceAmount(final BigDecimal approveAmount) throws InterruptedException{
    	if(insuranceAmount.compareTo(approveAmount) < 0){
    		UtilMessagesAndPopups.showError("Approve amount cannot be greater than Insurance Payable");
    		return;
    	}
    	if(insuranceAmount.compareTo(approveAmount) == 0){
    		invoicePayment.setAmount(new Money(approveAmount));
    		Enumeration enumeration = commonCrudService.getByUniqueValue(Enumeration.class, "enumCode", "INSURANCE_AMOUNT");
    		invoicePayment.setPaymentMethod(enumeration);
    		addTxnPaymentItem();
    		if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount())) {
                if ((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == 0)) {
                	    invoice.setInsuranceStatus(INSURANCESTATUS.CLOSED);
                		receivePayment();
                }
    		}
    	}else{
    		
    		Messagebox.show("Do you want to add remainig amount to patient account.", "Confirmation", Messagebox.YES | Messagebox.NO  | Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener<Event>() {
    		    public void onEvent(Event event) throws InterruptedException {
    		    	if ("onYes".equals(event.getName())) {
    		    		BigDecimal remainingAmount = insurancePayment.subtract(approveAmount);
    		    		invoice.setInsuranceRejectedAmount(remainingAmount);
    		    		invoice.setInsuranceStatus(INSURANCESTATUS.CLOSED);
						invoicePayment.setAmount(new Money(approveAmount));
			    		Enumeration enumeration = commonCrudService.getByUniqueValue(Enumeration.class, "enumCode", "INSURANCE_AMOUNT");
			    		invoicePayment.setPaymentMethod(enumeration);
			    		addTxnPaymentItem();
			    		Executions.sendRedirect(null);
					}
					if("onNo".equals(event.getName())){
						invoice.setInsuranceStatus(INSURANCESTATUS.PART_APPROVED);
						invoicePayment.setAmount(new Money(approveAmount));
			    		Enumeration enumeration = commonCrudService.getByUniqueValue(Enumeration.class, "enumCode", "INSURANCE_AMOUNT");
			    		invoicePayment.setPaymentMethod(enumeration);
			    		addTxnPaymentItem();
			    		Executions.sendRedirect(null);
					}
    		    }
    		});
    		
    	}
    	
    	
    }
    
    public void calculateCashInsuranceCorporateAmount(){
    	copyCoPayment  = coPayment;
    	copyInsurancePayment = insurancePayment;
    	if(invoice.getInsuranceRejectedAmount() != null){
    		copyCoPayment = copyCoPayment.add(invoice.getInsuranceRejectedAmount());
    		copyInsurancePayment = copyInsurancePayment.subtract(invoice.getInsuranceRejectedAmount());
    	}
    	cashAmount = copyCoPayment;
        insuranceAmount = copyInsurancePayment;
        for(InvoicePayment invoicePayment : invoice.getInvoicePayments()){
        	if(PaymentType.OPD_INSURANCE_AMOUNT.equals(invoicePayment.getPaymentType())){
        		insuranceAmount = insuranceAmount.subtract(invoicePayment.getAmount().getAmount());
        		if(invoice.getInsuranceRejectedAmount() != null){
        			copyInsurancePayment = copyInsurancePayment.subtract(invoicePayment.getAmount().getAmount());
                }
        	}else if(PaymentType.OPD_CORPORATE_AMOUNT.equals(invoicePayment.getPaymentType())){
        		insuranceAmount = insuranceAmount.subtract(invoicePayment.getAmount().getAmount());
        	}else{
        		cashAmount = cashAmount.subtract(invoicePayment.getAmount().getAmount());
        		if(invoice.getInsuranceRejectedAmount() != null){
        			copyCoPayment = copyCoPayment.subtract(invoicePayment.getAmount().getAmount());
                }
        	}
        }
        
        cashAmount = cashAmount.setScale(3, RoundingMode.HALF_UP);
        
        if(cashAmount.compareTo(BigDecimal.ZERO) < 0 && !invoice.isAmountRefundedToPatient()){
				addToPatientAccount(invoice,cashAmount.negate());
        }
        if(cashAmount.compareTo(BigDecimal.ZERO) < 0 && invoice.isAmountRefundedToPatient()){
        	cashAmount = BigDecimal.ZERO;
        	copyCoPayment = BigDecimal.ZERO;
        	coPayment = BigDecimal.ZERO;
        	
        }
        
        insuranceAmount = insuranceAmount.setScale(3, RoundingMode.HALF_UP);
        allowEdit = invoice.getInvoiceStatus().equals(InvoiceStatusItem.READY.toString()) || invoice.getInvoiceStatus().equals(InvoiceStatusItem.INPROCESS.toString());
        if(invoice.getInsuranceStatus() != null)
         allowEdit = (invoice.getInvoiceStatus().equals(InvoiceStatusItem.READY.toString()) || invoice.getInvoiceStatus().equals(InvoiceStatusItem.INPROCESS.toString()))
                &&
        		(invoice.getInsuranceStatus().equals(INSURANCESTATUS.PENDING_APPROVAL) || invoice.getInsuranceStatus().equals(INSURANCESTATUS.PRE_APPROVED) ||
        		invoice.getInsuranceStatus().equals(INSURANCESTATUS.PART_APPROVED) || invoice.getInsuranceStatus().equals(INSURANCESTATUS.APPROVED));
        
        
        
        if(INSURANCESTATUS.PENDING_APPROVAL.equals(invoice.getInsuranceStatus())){
        	boolean isAllInsuranceApprove = true;
        	for(InvoiceItem item : invoice.getInvoiceItems()){
        		if(!item.getIsAuthorized()){
        			isAllInsuranceApprove = false;
        			break;
        		}
        	}
        	if(isAllInsuranceApprove){
        		invoice.setInsuranceStatus(INSURANCESTATUS.APPROVED);
        		commonCrudService.save(invoice);
        	}
        }
        
    }
    
    public void addToPatientAccount(Invoice invoice,BigDecimal amount){
    	
    	BigDecimal depositAdvanceAmount = BigDecimal.ZERO;
    	Enumeration paymentMethod = commonCrudService.findUniqueByEquality(Enumeration.class, new String[]{"enumCode"}, new Object[]{"PATIENT_AMOUNT"}); 
    	InvoicePayment invoicePayment = new InvoicePayment(paymentMethod,invoice,new Money(amount.negate()),PaymentType.OPD_PATIENT_AMOUNT);
        invoicePayment.setPaymentType(PaymentType.OPD_PATIENT_AMOUNT);
        depositAdvanceAmount = depositAdvanceAmount.add(invoicePayment.getAmount().getAmount().negate());
        
        invoice.addInvoicePayment(invoicePayment);
        //invoice.getCollectedAmount().setAmount(invoice.getCollectedAmount().getAmount().add(invoicePayment.getAmount().getAmount()));
        invoice.setAmountRefundedToPatient(true);
        commonCrudService.save(invoice);
        billingService.updatePatientDeposit(invoice.getPatient(),depositAdvanceAmount);
        
    }
    
    public String getTotalCancelationCharges(){
    	List<AfyaClinicDeposit> afyaClinicDeposits = commonCrudService.findByEquality(AfyaClinicDeposit.class, new String[]{"invoice"}, new Object[]{invoice});
    	BigDecimal totalAmount = BigDecimal.ZERO;
    	for(AfyaClinicDeposit afyaClinicDeposit : afyaClinicDeposits){
    		totalAmount = totalAmount.add(afyaClinicDeposit.getDepositAmount());
    	}
    	if(totalAmount.compareTo(BigDecimal.ZERO) <= 0){
    		return "";
    	}else{
    		return "Total Cancellation charges is " + totalAmount + " KD";
    	}
    }


    public void addTxnPaymentItem() throws InterruptedException {
        if (invoicePayment.getAmount().getAmount().compareTo(remainingAmount) == 1) {
            invoicePayment = new InvoicePayment(invoicePayment.getPaymentMethod(), invoicePayment.getInvoice(), 
            		invoicePayment.getAmount(), invoicePayment.getPaymentType());
            UtilMessagesAndPopups.showError("Collected amount cannot be more than amount to be paid");
            return;
        }
        
        String enumCode = invoicePayment.getPaymentMethod().getEnumCode();

        if (invoice.getInvoiceType().name().equals(InvoiceType.OPD.name())) {
            if (enumCode.equals("CASH"))
                invoicePayment.setPaymentType(PaymentType.OPD_CASH);
            if (enumCode.equals("DEBIT_CARD"))
                invoicePayment.setPaymentType(PaymentType.OPD_DEBIT_CARD);
            if (enumCode.equals("CREDIT_CARD"))
                invoicePayment.setPaymentType(PaymentType.OPD_CREDIT_CARD);
            if (enumCode.equals("PERSONAL_CHEQUE"))
                invoicePayment.setPaymentType(PaymentType.OPD_PERSONAL_CHEQUE);
            if (enumCode.equals("ADVANCE_AMOUNT")){
            	if(advanceAmount.compareTo(BigDecimal.ZERO) <= 0 || advanceAmount.compareTo(invoicePayment.getAmount().getAmount()) < 0 ){
            		UtilMessagesAndPopups.showError("Collected amount should not be greater than Advance amount");
            		return;
            	}
                invoicePayment.setPaymentType(PaymentType.OPD_ADVANCE_AMOUNT);
                deductAdvanceAmount = deductAdvanceAmount.add(invoicePayment.getAmount().getAmount());
            }
            if (enumCode.equals("INSURANCE_AMOUNT")){
                invoicePayment.setPaymentType(PaymentType.OPD_INSURANCE_AMOUNT);
            }
            if (enumCode.equals("CORPORATE_AMOUNT")){
                invoicePayment.setPaymentType(PaymentType.OPD_CORPORATE_AMOUNT);
            }
            if (enumCode.equals("PATIENT_AMOUNT")){
                invoicePayment.setPaymentType(PaymentType.OPD_PATIENT_AMOUNT);
                depositAdvanceAmount = depositAdvanceAmount.add(invoicePayment.getAmount().getAmount().negate());
            }
            
        } else {
            if (enumCode.equals("CASH"))
                invoicePayment.setPaymentType(PaymentType.CASUALTY_CASH);
            if (enumCode.equals("DEBIT_CARD"))
                invoicePayment.setPaymentType(PaymentType.CASUALTY_DEBIT_CARD);
            if (enumCode.equals("CREDIT_CARD"))
                invoicePayment.setPaymentType(PaymentType.CASUALTY_CREDIT_CARD);
            if (enumCode.equals("PERSONAL_CHEQUE"))
                invoicePayment.setPaymentType(PaymentType.CASUALTY_PERSONAL_CHEQUE);
            if (enumCode.equals("ADVANCE_AMOUNT")){
            	if(advanceAmount.compareTo(BigDecimal.ZERO) <= 0 || advanceAmount.compareTo(invoicePayment.getAmount().getAmount()) < 0 ){
            		UtilMessagesAndPopups.showError("Collected amount should not be greater than Advance amount");
            		return;
            	}
            	invoicePayment.setPaymentType(PaymentType.OPD_ADVANCE_AMOUNT);
            	deductAdvanceAmount = deductAdvanceAmount.add(invoicePayment.getAmount().getAmount());
            }
            if (enumCode.equals("INSURANCE_AMOUNT")){
                invoicePayment.setPaymentType(PaymentType.OPD_INSURANCE_AMOUNT);
            }
            if (enumCode.equals("CORPORATE_AMOUNT")){
                invoicePayment.setPaymentType(PaymentType.OPD_CORPORATE_AMOUNT);
            }
            if (enumCode.equals("PATIENT_AMOUNT")){
                invoicePayment.setPaymentType(PaymentType.OPD_PATIENT_AMOUNT);
                depositAdvanceAmount = depositAdvanceAmount.add(invoicePayment.getAmount().getAmount().negate());
            }
        }
        
        if("INSURANCE".equals(invoice.getPatient().getPatientType()) || "CORPORATE".equals(invoice.getPatient().getPatientType()) ){
            BigDecimal totalCollectedAmount = invoice.getCollectedAmount().getAmount();
            BigDecimal totalInsuranceAmount = BigDecimal.ZERO;
            if ((invoice.getInsuranceStatus() != null) && invoice.getInsuranceStatus().getDescription().equals("SENT FOR CLAIM")){
                for (InvoiceItem invoiceItem : invoice.getInvoiceItems()){
                    totalInsuranceAmount = totalInsuranceAmount.add(invoiceItem.getInsuranceAmount());
                }
                totalCollectedAmount = totalCollectedAmount.subtract(totalInsuranceAmount);
            }

        	if(!"OPD_INSURANCE_AMOUNT".equals(invoicePayment.getPaymentType().toString())){
        		BigDecimal totalCopay = totalCollectedAmount.add(invoicePayment.getAmount().getAmount());
        		if(totalCollectedAmount.compareTo(coPayment.setScale(3, RoundingMode.HALF_UP)) > 0 || 
        				totalCopay.compareTo(coPayment.setScale(3, RoundingMode.HALF_UP)) > 0){
        			 UtilMessagesAndPopups.showError("Collected amount cannot be more than patient payable");
        	         return;
        		}
        	}
        	/*if("OPD_INSURANCE_AMOUNT".equals(invoicePayment.getPaymentType().toString())){
        		if( !(invoicePayment.getAmount().getAmount().compareTo(insurancePayment.setScale(3, RoundingMode.HALF_UP)) == 0) ){
        			 UtilMessagesAndPopups.showError("Collected amount must be equal to insurance payable");
        	         return;
        		}
        	}*/
        }
        invoice.setInvoiceStatus(InvoiceStatusItem.INPROCESS.toString());
        invoice.addInvoicePayment(invoicePayment);
        invoice.getCollectedAmount().setAmount(invoice.getCollectedAmount().getAmount().add(invoicePayment.getAmount().getAmount()));
        remainingAmount = remainingAmount.subtract(invoicePayment.getAmount().getAmount());

        invoicePayment = new InvoicePayment();
        commonCrudService.save(invoice);
        
        calculateCashInsuranceCorporateAmount();
        
        calculatePatientAdvanceAmount();
        
        if(copyCoPayment.compareTo(BigDecimal.ZERO) == 0){
        	invoice.setInvoiceStatus(InvoiceStatusItem.PATIENT_PAID.toString());
        	commonCrudService.save(invoice);
        	UtilMessagesAndPopups.showSuccess();
        	Executions.sendRedirect(null);
        }
        
        if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount())) {
            if ((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == 0)) {
            	    invoice.setInsuranceStatus(INSURANCESTATUS.CLOSED);
            		receivePayment();
            }else{
            	if(depositAdvanceAmount.compareTo(BigDecimal.ZERO) > 0){
                	billingService.updatePatientDeposit(invoice.getPatient(),depositAdvanceAmount);
                }
            	if(deductAdvanceAmount.compareTo(BigDecimal.ZERO) > 0){
        	        if(deductAdvanceAmount.compareTo(advanceAmount) > 0){
        	        	UtilMessagesAndPopups.showError("Cannot be completed the billing : as amount collected is more than advance amount.");
                        return;
        	        }
        	        billingService.updatePatientWithdraw(invoice.getPatient(),deductAdvanceAmount);
                }
            }
		}
        UtilMessagesAndPopups.showSuccess();
        Executions.sendRedirect(null);
        
    }

    public void writeOffBillAmt() throws InterruptedException {
        if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount())) {
            Money tmpMoney = new Money(invoice.getTotalAmount().getAmount().subtract(invoice.getCollectedAmount().getAmount()), invoice.getTotalAmount().getCurrency());
            invoice.setWrittenOffAmount(tmpMoney);
            invoice.setWriteOffDate(new Date());
            invoice.setWriteOffBy(Infrastructure.getUserName());
            invoice.setInvoiceStatus(InvoiceStatusItem.WRITEOFF.toString());
            remainingAmount = BigDecimal.ZERO;
            InvoicePayment invoicePayment = new InvoicePayment();
            if (tmpMoney.gt(new Money(BigDecimal.ZERO))) {

                invoicePayment.setAmount(tmpMoney);
                if (invoice.getInvoiceType().name().equals(InvoiceType.OPD.name()))
                    invoicePayment.setPaymentType(PaymentType.OPD_WRITE_OFF);
                else
                    invoicePayment.setPaymentType(PaymentType.CASUALTY_WRITE_OFF);

            }
            billingService.saveInvoiceStatusAsWriteOff(invoice, InvoiceStatusItem.WRITEOFF, invoicePayment);
            Executions.sendRedirect(null);


            UtilMessagesAndPopups.showSuccess();
        }
    }

    public void saveInvoice() {
    	
        /*if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount())) {
            if (invoice.getCollectedAmount().getAmount().equals(BigDecimal.ZERO)) {
                UtilMessagesAndPopups.showError("Collected Amount cannot be zero");
                return;
            }
        }*/
        UtilMessagesAndPopups.showSuccess();
        if(invoice.getConcessionAmount() != null)
        	updateTotalConcession(invoice);
        invoice.setInvoiceStatus(InvoiceStatusItem.INPROCESS.toString());
        updateConcessionPrice();
        InvoiceStatusItem statusItem = InvoiceStatusItem.READY;
        billingService.saveInvoiceStatus(invoice, statusItem);
        getCopaymentAndInsurancePayment();
        updateCancelInfo();
    }
    
    private void updateCancelInfo(){
    	int totalInvoiceCount = invoice.getInvoiceItems().size();
    	int count = 0;
    	for(InvoiceItem ii : invoice.getInvoiceItems()){
    		if("Cancel".equals(ii.getInvoiceItemStatus()))
    			count++;
    	}
    	if(totalInvoiceCount == count){
    		cancelInvoice(invoice);
    	}
    }
    
    private void updateTotalConcession(Invoice invoice){
    	if("AMOUNT".equals(invoice.getConcessionType())){
	    	BigDecimal totalAmount = getTotalGrossAmount();
	    	for(InvoiceItem ii : invoice.getInvoiceItems()){
	    		BigDecimal val = ii.getGrossAmount().divide(totalAmount,8,RoundingMode.FLOOR);
	    		ii.setConcessionAmount( val.multiply(invoice.getConcessionAmount()) );
	    		ii.setConcessionType(invoice.getConcessionType());
	    	}
    	}else if("PERCENTAGE".equals(invoice.getConcessionType())){
    		for(InvoiceItem ii : invoice.getInvoiceItems()){
	    		ii.setConcessionAmount(  invoice.getConcessionAmount() );
	    		ii.setConcessionType(invoice.getConcessionType());
	    	}
    	}
    	
    }

    public boolean displayWriteOff() {
        return (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && paymentReceived && !(InvoiceStatusItem.WRITEOFF.toString().equals(invoice.getInvoiceStatus())));
    }

    public boolean isWriteOff() {
        return InvoiceStatusItem.WRITEOFF.toString().equals(invoice.getInvoiceStatus());
    }

    public void receivePayment() throws InterruptedException {
        String msg = "";
        if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount())) {
            if ((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == 1)) {
                UtilMessagesAndPopups.showError("Cannot be completed the billing : as amount collected is more than amount to be paid.");
                return;
            }
            if (((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == -1))){
                msg = "Collected amount is less than amount be to paid. ";
            	return;
            }
        }
        if(deductAdvanceAmount.compareTo(BigDecimal.ZERO) > 0){
	        if(deductAdvanceAmount.compareTo(advanceAmount) > 0){
	        	UtilMessagesAndPopups.showError("Cannot be completed the billing : as amount collected is more than advance amount.");
                return;
	        }
	        billingService.updatePatientWithdraw(invoice.getPatient(),deductAdvanceAmount);
        }
        if(depositAdvanceAmount.compareTo(BigDecimal.ZERO) > 0){
        	billingService.updatePatientDeposit(invoice.getPatient(),depositAdvanceAmount);
        }
        invoice.setInvoiceStatus(InvoiceStatusItem.RECEIVED.toString());
        UserLogin login = Infrastructure.getUserLogin();
        invoice.setCollectedByUser(login.getUsername());
        billingService.saveInvoiceStatus(invoice, InvoiceStatusItem.RECEIVED);
        
        UtilMessagesAndPopups.showSuccess();
        
        Executions.sendRedirect(null);
    }

    public void printBill() throws JRException {
        Map<String, Object> reportParameterMap = new HashMap<String, Object>();
        reportParameterMap.put("patientAddress", invoice.getPatient().getContacts().getPostalAddress().toString());
        reportParameterMap.put("practiceAddress", Infrastructure.getPractice().getContacts().getPostalAddress().toString());
        reportParameterMap.put("patientFormattedName", commonCrudService.getFormattedName(invoice.getPatient()));
        JasperReportUtil.buildPdfReport("patientBilling.xml", reportParameterMap, invoice.getInvoiceItems(), "invoice.pdf");
    }

    public void billPrint() throws JRException {
        Map<String, String> reportParameterMap = new HashMap<String, String>();
        reportParameterMap.put("patientAddress", invoice.getPatient().getContacts().getPostalAddress().toString());
        reportParameterMap.put("practiceAddress", Infrastructure.getPractice().getContacts().getPostalAddress().toString());
        reportParameterMap.put("patientFormattedName", commonCrudService.getFormattedName(invoice.getPatient()));
        Executions.getCurrent().sendRedirect("/billing/billingTxnItemPrint.zul?invoiceId=" + invoice.getId(), "_BillSoapNote");
    }

    public void removeTxnPaymentItem(InvoicePayment txnPayment,Component self, Component remaingValueLabel) {
    	if(PaymentType.OPD_ADVANCE_AMOUNT.equals(txnPayment.getPaymentType())){
    		UtilMessagesAndPopups.showError("Smart Services Advance Amount cannot be removed.");
    		return;
    	}
        if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount()))
            invoice.getInvoicePayments().remove(txnPayment);
        invoice.getCollectedAmount().setAmount(invoice.getCollectedAmount().getAmount().subtract(txnPayment.getAmount().getAmount()));
        remainingAmount = remainingAmount.add(txnPayment.getAmount().getAmount());
        
        self.getParent().getParent().detach();
        Events.postEvent("onReload",remaingValueLabel,null);
    }
    
    

    public String getInsuranceName() {
    	if(patientSoapNote != null && patientSoapNote.getPatientInsurance() != null){
    		insuranceName = patientSoapNote.getPatientInsurance().getHealthPolicyName();
    		
    	}else{
    		if(UtilValidator.isNotEmpty(invoice.getPatient().getPatientInsurances())){
    			try{
    			insuranceName = ((PatientInsurance)invoice.getPatient().getPatientInsurances().iterator().next()).getHealthPolicyName();
    			}catch(Exception e){
    				
    			}
    		}
    	}
		return insuranceName;
	}

	@Resource
    @Required
    public void setBillingService(BillingService billingService) {
        this.billingService = billingService;
    }

    @Resource
    @Required
    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    public BillingDisplayConfig getBillingDisplayConfig() {
        return billingDisplayConfig;
    }

    public SoapNoteService getSoapNoteService() {
        return soapNoteService;
    }

    @Resource
    @Required
    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public Map<String, List<InvoiceItem>> getInvoiceItemMap() {
        return invoiceItemMap;
    }

    public InvoicePayment getInvoicePayment() {
        return invoicePayment;
    }

    public void setInvoicePayment(InvoicePayment invoicePayment) {
        this.invoicePayment = invoicePayment;
    }

    public boolean isPaymentReceived() {
        return paymentReceived;
    }

    public Map<String, List<InvoiceItem>> getInvoiceDeductibleItemMap() {
        return invoiceDeductibleItemMap;
    }

    public Map<String, List<InvoiceItem>> getInvoicePrevAdvanceItemMap() {
        return invoicePrevAdvanceItemMap;
    }

    public void setInvoicePrevAdvanceItemMap(
            Map<String, List<InvoiceItem>> invoicePrevAdvanceItemMap) {
        this.invoicePrevAdvanceItemMap = invoicePrevAdvanceItemMap;
    }

    public BigDecimal getTotalAmtTillDate() {
        return totalAmtTillDate;
    }

    public BigDecimal getOverallBalance() {
        return overallBalance;
    }

    public Invoice genInvoiceForSelectedItems(Invoice invObj, List<InvoiceItem> selectedInvItems, Map<String, Collection<? extends IdGeneratingBaseEntity>> allItemsInSelectedInv) {
        invObj.getTotalAmount().setAmount(BigDecimal.ZERO);
        invObj.getInvoiceItems().clear();
        Set<? extends IdGeneratingBaseEntity> itemTypeEntities;
        Set<Object> enitiesToUpd = new HashSet<Object>();
        for (InvoiceItem invItem : selectedInvItems) {
            itemTypeEntities = (Set<? extends IdGeneratingBaseEntity>) allItemsInSelectedInv.get(invItem.getItemType());
            if (itemTypeEntities != null) {
                for (Object obj : itemTypeEntities) {
                    IdGeneratingBaseEntity be = (IdGeneratingBaseEntity) obj;
                    if (be.getId().toString().equals(invItem.getItemId()))
                        enitiesToUpd.add(be);
                }
            }
            invObj.addInvoiceItem(invItem);
        }
        commonCrudService.save(enitiesToUpd);
        try {
            billingService.createInvoice(invObj);
        } catch (TransactionException e) {
            UtilMessagesAndPopups.showError(e.getMessage());
        }
        return invObj;
    }

    public Map<String, List<InvoiceItem>> getInvPrevBalanceItemMap() {
        return invPrevBalanceItemMap;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public BigDecimal getWriteOffAmount() {
        return writeOffAmount;
    }

    public BillingService getBillingService() {
        return billingService;
    }

    public class LabOrderInvoice {
        private String description;
        private BigDecimal factor = BigDecimal.ONE;
        private BigDecimal quantity = BigDecimal.ONE;
        private BigDecimal price = BigDecimal.ZERO;


        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getFactor() {
            return factor;
        }

        public void setFactor(BigDecimal factor) {
            this.factor = factor;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }


    }

    private class NurseObservationComparator implements Comparator<InvoiceItem> {

        @Override
        public int compare(InvoiceItem o1, InvoiceItem o2) {
            return o1.getItemId().compareTo(o2.getItemId());
        }

    }

    public PatientSoapNote getPatientSoapNote() {
        return patientSoapNote;
    }

    public void setPatientSoapNote(PatientSoapNote patientSoapNote) {
        this.patientSoapNote = patientSoapNote;
    }

    public BigDecimal getInsurancePayment() {
        return insurancePayment.setScale(3, RoundingMode.HALF_UP);
    }

    public void setInsurancePayment(BigDecimal insurancePayment) {
        this.insurancePayment = insurancePayment;
    }

    public BigDecimal getCoPayment() {
        return coPayment.setScale(3, RoundingMode.HALF_UP);
    }

    public void setCoPayment(BigDecimal coPayment) {
        this.coPayment = coPayment;
    }
    
    public BigDecimal getDeductAdvanceAmount() {
		return deductAdvanceAmount;
	}

	public void setDeductAdvanceAmount(BigDecimal deductAdvanceAmount) {
		this.deductAdvanceAmount = deductAdvanceAmount;
	}
	
	public BigDecimal getDepositAdvanceAmount() {
		return depositAdvanceAmount;
	}

	public void setDepositAdvanceAmount(BigDecimal depositAdvanceAmount) {
		this.depositAdvanceAmount = depositAdvanceAmount;
	}

	
	public boolean isCorporateOrPatient() {
		return corporateOrPatient;
	}

	public void setCorporateOrPatient(boolean corporateOrPatient) {
		this.corporateOrPatient = corporateOrPatient;
	}

	public boolean isCorporate() {
		return corporate;
	}

	public void setCorporate(boolean corporate) {
		this.corporate = corporate;
	}

	public List<Map<String, Object>> getTariffCategorys() {
		return tariffCategorys;
	}

	public void setTariffCategorys(List<Map<String, Object>> tariffCategorys) {
		this.tariffCategorys = tariffCategorys;
	}
	
	public PatientService getPatientService() {
		return patientService;
	}

	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}
	
	

	public BigDecimal getCashAmount() {
		return cashAmount;
	}

	public void setCashAmount(BigDecimal cashAmount) {
		this.cashAmount = cashAmount;
	}

	public BigDecimal getInsuranceAmount() {
		return insuranceAmount;
	}

	public void setInsuranceAmount(BigDecimal insuranceAmount) {
		this.insuranceAmount = insuranceAmount;
	}
	
	

	public BigDecimal getCopyInsurancePayment() {
		return copyInsurancePayment;
	}

	public void setCopyInsurancePayment(BigDecimal copyInsurancePayment) {
		this.copyInsurancePayment = copyInsurancePayment;
	}

	public BigDecimal getCopyCoPayment() {
		return copyCoPayment;
	}

	public void setCopyCoPayment(BigDecimal copyCoPayment) {
		this.copyCoPayment = copyCoPayment;
	}
	
	public boolean isAllowEdit() {
		return allowEdit;
	}

	public void setAllowEdit(boolean allowEdit) {
		this.allowEdit = allowEdit;
	}

	private static final long serialVersionUID = 1L;
}
