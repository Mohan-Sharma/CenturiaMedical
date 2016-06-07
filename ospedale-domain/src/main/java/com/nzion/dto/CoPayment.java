package com.nzion.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/22/15
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoPayment {

    private BigDecimal totalCopayAmount;

    private BigDecimal totalDeductableAmount;

    private BigDecimal totalAuthorizationAmount;
    
    List<CoPaymentDetail> serviceDetails;

    private CoPaymentDetail moduleDetails;

    public BigDecimal getTotalCopayAmount() {
        return totalCopayAmount;
    }

    public void setTotalCopayAmount(BigDecimal totalCopayAmount) {
        this.totalCopayAmount = totalCopayAmount;
    }

    public List<CoPaymentDetail> getServiceDetails() {
        if(serviceDetails == null)
            return new ArrayList<CoPaymentDetail>();
        return serviceDetails;
    }

    public void setServiceDetails(List<CoPaymentDetail> serviceDetails) {
        this.serviceDetails = serviceDetails;
    }

    public BigDecimal getTotalDeductableAmount() {
        return totalDeductableAmount;
    }

    public void setTotalDeductableAmount(BigDecimal totalDeductableAmount) {
        this.totalDeductableAmount = totalDeductableAmount;
    }

    public BigDecimal getTotalAuthorizationAmount() {
        return totalAuthorizationAmount;
    }

    public void setTotalAuthorizationAmount(BigDecimal totalAuthorizationAmount) {
        this.totalAuthorizationAmount = totalAuthorizationAmount;
    }

    public CoPaymentDetail getModuleDetails() {
        return moduleDetails;
    }

    public void setModuleDetails(CoPaymentDetail moduleDetails) {
        this.moduleDetails = moduleDetails;
    }

}
