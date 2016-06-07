package com.nzion.util;

import com.nzion.domain.billing.Invoice;
import com.nzion.domain.billing.InvoiceItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Created by Mohan Sharma on 5/8/2015.
 */
public class PatientInsuranceUtility {
	
	public static final BigDecimal ONEHUNDERT = new BigDecimal(100);

	BigDecimal insurancePayment = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    BigDecimal coPayment = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    public  Map<String, Object> getPatientPayableAndInsurancePayable(Invoice invoice){
        Map<String, Object> mapContainingPatientAndInsurancePayable = new HashMap<String, Object>();
        coPayment = BigDecimal.ZERO;
        insurancePayment = BigDecimal.ZERO;
        insurancePayment.setScale(3, RoundingMode.HALF_UP);
        
        for(InvoiceItem ii : invoice.getInvoiceItems()){
        	if(ii.getNetPrice() == null)
        		ii.setNetPrice(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
            if(ii.getIsStatusCancel())
                continue;
            if("NET".equals(ii.getComputeType())){
                caculateCoPayAndInsuranceAmountInPercent(ii, ii.getNetPrice());
            }else{
                caculateCoPayAndInsuranceAmount(ii, ii.getNetPrice());
            }
        }
        if(coPayment.compareTo(BigDecimal.ZERO) == 0)
            coPayment = invoice.getTotalAmount().getAmount();
        if("CASH PAYING".equals(invoice.getPatient().getPatientType()) ){
            insurancePayment = BigDecimal.ZERO;
        }
        
        
        mapContainingPatientAndInsurancePayable.put("patientPayable", coPayment.setScale(3, RoundingMode.HALF_UP));
        mapContainingPatientAndInsurancePayable.put("insurancePayable", insurancePayment.setScale(3, RoundingMode.HALF_UP));
        
        
        
        return  mapContainingPatientAndInsurancePayable;
    }
    
    private void caculateCoPayAndInsuranceAmountInPercent(InvoiceItem ii,BigDecimal grossOrNetAmount){
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
            coPayment = coPayment.add(lineCoPayPercent);
            insurancePayment = insurancePayment.add(grossOrNetAmount.subtract(lineCoPayPercent));
        }
    }

    private void caculateCoPayAndInsuranceAmount(InvoiceItem ii,BigDecimal grossOrNetAmount){
        BigDecimal lineCoPay = ii.getCopayCalculatedAmount() != null ? ii.getCopayCalculatedAmount() : BigDecimal.ZERO;

        BigDecimal deductableAmount = ii.getDeductableAmount() != null ? ii.getDeductableAmount() : BigDecimal.ZERO;
        if(deductableAmount.compareTo(BigDecimal.ZERO) == 0 && ii.getDeductablePercentage() != null){
        	deductableAmount = percentage(grossOrNetAmount,ii.getDeductablePercentage());
        }
        BigDecimal lineTotalCoPay = BigDecimal.ZERO;
        if(lineCoPay.compareTo(BigDecimal.ZERO) == 0 && ii.getCopayPercentage() != null){
        	BigDecimal amount = grossOrNetAmount.subtract(deductableAmount);
        	lineCoPay = percentage(amount,ii.getCopayPercentage());
        	lineTotalCoPay = lineCoPay.add(deductableAmount);
        }else{
        	lineTotalCoPay = lineCoPay.add(deductableAmount);
        }
        BigDecimal lineTotalInsurance = grossOrNetAmount.subtract(lineTotalCoPay);
        if(lineTotalInsurance.compareTo(BigDecimal.ZERO) < 0)
            insurancePayment = BigDecimal.ZERO;
        else
            insurancePayment = insurancePayment.add(lineTotalInsurance);
        coPayment = coPayment.add(lineTotalCoPay);
    }
    
    public static BigDecimal percentage(BigDecimal base, BigDecimal pct){
        if(UtilValidator.isEmpty(base) || UtilValidator.isEmpty(pct))
            return BigDecimal.ZERO;
        return base.multiply(pct).divide(ONEHUNDERT);
    }
    
    
    public  Map<String, Object> getPatientPayableAndInsurancePayableForInvoiceItem(InvoiceItem invoiceItem){
        Map<String, Object> mapContainingPatientAndInsurancePayable = new HashMap<String, Object>();
        coPayment = BigDecimal.ZERO;
        insurancePayment = BigDecimal.ZERO;
        insurancePayment.setScale(3, RoundingMode.HALF_UP);
        
        if(invoiceItem.getNetPrice() == null)
        	invoiceItem.setNetPrice(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
        if(!invoiceItem.getIsStatusCancel()){
            if("NET".equals(invoiceItem.getComputeType())){
            	caculateCoPayAndInsuranceAmountInPercent(invoiceItem, invoiceItem.getNetPrice());
            }else{
            	caculateCoPayAndInsuranceAmount(invoiceItem, invoiceItem.getNetPrice());
            }
        }
              
        if(coPayment.compareTo(BigDecimal.ZERO) == 0)
            coPayment = invoiceItem.getPrice().getAmount();
        if("CASH PAYING".equals(invoiceItem.getInvoice().getPatient().getPatientType()) ){
            insurancePayment = BigDecimal.ZERO;
        }
        
        
        mapContainingPatientAndInsurancePayable.put("patientPayable", coPayment.setScale(3, RoundingMode.HALF_UP));
        mapContainingPatientAndInsurancePayable.put("insurancePayable", insurancePayment.setScale(3, RoundingMode.HALF_UP));
      
        return  mapContainingPatientAndInsurancePayable;
    }
}
