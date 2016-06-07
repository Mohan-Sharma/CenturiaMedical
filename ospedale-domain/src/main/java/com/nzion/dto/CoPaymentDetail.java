package com.nzion.dto;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/21/15
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoPaymentDetail {

    private String serviceId;
    private BigDecimal copayAmount;
    private BigDecimal maxAmount;
    private BigDecimal copayPercentage;
    private BigDecimal deductableAmount;
    private BigDecimal deductablePercentage;
    private Boolean authorization;
    private BigDecimal authorizationInclusiveConsultation;
    private BigDecimal authorizationAmount;
    private BigDecimal authorizationRequiredConsultation;
    private String computeType;
    private String moduleId;

    public CoPaymentDetail() {
    }

    public CoPaymentDetail(String serviceId, BigDecimal copayAmount, BigDecimal copayPercentage, BigDecimal deductableAmount,
                           BigDecimal deductablePercentage, String computeType, String moduleId) {
        this.serviceId = serviceId;
        this.copayAmount = copayAmount;
        this.copayPercentage = copayPercentage;
        this.deductableAmount = deductableAmount;
        this.deductablePercentage = deductablePercentage;
        this.computeType = computeType;
        this.moduleId = moduleId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }


    public BigDecimal getCopayAmount() {
        return copayAmount;
    }

    public void setCopayAmount(BigDecimal copayAmount) {
        this.copayAmount = copayAmount;
    }

    public BigDecimal getCopayPercentage() {
        return copayPercentage;
    }

    public void setCopayPercentage(BigDecimal copayPercentage) {
        this.copayPercentage = copayPercentage;
    }

    public BigDecimal getDeductableAmount() {
        return deductableAmount;
    }

    public void setDeductableAmount(BigDecimal deductableAmount) {
        this.deductableAmount = deductableAmount;
    }

    public BigDecimal getDeductablePercentage() {
        return deductablePercentage;
    }

    public void setDeductablePercentage(BigDecimal deductablePercentage) {
        this.deductablePercentage = deductablePercentage;
    }

    public Boolean getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Boolean authorization) {
        this.authorization = authorization;
    }

    public BigDecimal getAuthorizationInclusiveConsultation() {
        return authorizationInclusiveConsultation;
    }

    public void setAuthorizationInclusiveConsultation(BigDecimal authorizationInclusiveConsultation) {
        this.authorizationInclusiveConsultation = authorizationInclusiveConsultation;
    }

    public BigDecimal getAuthorizationAmount() {
        return authorizationAmount;
    }

    public void setAuthorizationAmount(BigDecimal authorizationAmount) {
        this.authorizationAmount = authorizationAmount;
    }

    public BigDecimal getAuthorizationRequiredConsultation() {
        return authorizationRequiredConsultation;
    }

    public void setAuthorizationRequiredConsultation(BigDecimal authorizationRequiredConsultation) {
        this.authorizationRequiredConsultation = authorizationRequiredConsultation;
    }

    public String getComputeType() {
        return computeType;
    }

    public void setComputeType(String computeType) {
        this.computeType = computeType;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
    public BigDecimal getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(BigDecimal maxAmount) {
		this.maxAmount = maxAmount;
	}
}
