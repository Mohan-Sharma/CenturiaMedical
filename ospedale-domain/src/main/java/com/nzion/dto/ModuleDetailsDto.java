package com.nzion.dto;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/20/15
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleDetailsDto {

    private String moduleId;
    private String moduleName;
    private BigDecimal sumAssured;
    private Integer fromAge;
    private Integer healthPolicyId;
    private Integer toAge;
    private BigDecimal preHospitalisationLimit;
    private BigDecimal preHospitalisationDays;
    private BigDecimal postHospitalisationLimit;
    private BigDecimal postHospitalisationDays;
    private BigDecimal waitingPeriod;
    private BigDecimal exclusionPeriod;
    private String specialClause;
    private String specialExclusions;
    private String notes;
    private boolean isMaternity;
    private boolean isActive;
    private BigDecimal copayAmount;
    private BigDecimal copayPercentage;
    private BigDecimal deductableAmount;
    private BigDecimal deductablePercentage;
    private String authorization;
    private String authorizationInclusiveConsultation;
    private String authorizationAmount;
    private String authorizationRequiredConsultation;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public BigDecimal getSumAssured() {
        return sumAssured;
    }

    public void setSumAssured(BigDecimal sumAssured) {
        this.sumAssured = sumAssured;
    }

    public Integer getFromAge() {
        return fromAge;
    }

    public void setFromAge(Integer fromAge) {
        this.fromAge = fromAge;
    }

    public Integer getHealthPolicyId() {
        return healthPolicyId;
    }

    public void setHealthPolicyId(Integer healthPolicyId) {
        this.healthPolicyId = healthPolicyId;
    }

    public Integer getToAge() {
        return toAge;
    }

    public void setToAge(Integer toAge) {
        this.toAge = toAge;
    }

    public BigDecimal getPreHospitalisationLimit() {
        return preHospitalisationLimit;
    }

    public void setPreHospitalisationLimit(BigDecimal preHospitalisationLimit) {
        this.preHospitalisationLimit = preHospitalisationLimit;
    }

    public BigDecimal getPreHospitalisationDays() {
        return preHospitalisationDays;
    }

    public void setPreHospitalisationDays(BigDecimal preHospitalisationDays) {
        this.preHospitalisationDays = preHospitalisationDays;
    }

    public BigDecimal getPostHospitalisationLimit() {
        return postHospitalisationLimit;
    }

    public void setPostHospitalisationLimit(BigDecimal postHospitalisationLimit) {
        this.postHospitalisationLimit = postHospitalisationLimit;
    }

    public BigDecimal getPostHospitalisationDays() {
        return postHospitalisationDays;
    }

    public void setPostHospitalisationDays(BigDecimal postHospitalisationDays) {
        this.postHospitalisationDays = postHospitalisationDays;
    }

    public BigDecimal getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(BigDecimal waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }

    public BigDecimal getExclusionPeriod() {
        return exclusionPeriod;
    }

    public void setExclusionPeriod(BigDecimal exclusionPeriod) {
        this.exclusionPeriod = exclusionPeriod;
    }

    public String getSpecialClause() {
        return specialClause;
    }

    public void setSpecialClause(String specialClause) {
        this.specialClause = specialClause;
    }

    public String getSpecialExclusions() {
        return specialExclusions;
    }

    public void setSpecialExclusions(String specialExclusions) {
        this.specialExclusions = specialExclusions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isMaternity() {
        return isMaternity;
    }

    public void setMaternity(boolean maternity) {
        isMaternity = maternity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getAuthorizationInclusiveConsultation() {
        return authorizationInclusiveConsultation;
    }

    public void setAuthorizationInclusiveConsultation(String authorizationInclusiveConsultation) {
        this.authorizationInclusiveConsultation = authorizationInclusiveConsultation;
    }

    public String getAuthorizationAmount() {
        return authorizationAmount;
    }

    public void setAuthorizationAmount(String authorizationAmount) {
        this.authorizationAmount = authorizationAmount;
    }

    public String getAuthorizationRequiredConsultation() {
        return authorizationRequiredConsultation;
    }

    public void setAuthorizationRequiredConsultation(String authorizationRequiredConsultation) {
        this.authorizationRequiredConsultation = authorizationRequiredConsultation;
    }
}
