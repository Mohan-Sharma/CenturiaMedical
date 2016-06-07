package com.nzion.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/15/15
 * Time: 2:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class InsuranceGroupPlanDetailDto {
    private Boolean isTpa;
    private String insuredGroupId;
    private Date planStartDate;
    private Date planEndDate;
    private String policyNumber;
    private TpaDetailsDto tpaDetails;
    private InsuranceDetailsDto insuranceDetails;
    private List<InsuranceForTpaDto> insuranceForTpa;
    private List<BenefitsDto> benefits;
    private HealthPolicyDto healthPolicy;
    private List<ModulesDto> modules;

    public Boolean getTpa() {
        return isTpa;
    }

    public void setTpa(Boolean tpa) {
        isTpa = tpa;
    }

    public String getInsuredGroupId() {
        return insuredGroupId;
    }

    public void setInsuredGroupId(String insuredGroupId) {
        this.insuredGroupId = insuredGroupId;
    }

    public Date getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(Date planStartDate) {
        this.planStartDate = planStartDate;
    }

    public Date getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(Date planEndDate) {
        this.planEndDate = planEndDate;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public TpaDetailsDto getTpaDetails() {
        return tpaDetails;
    }

    public void setTpaDetails(TpaDetailsDto tpaDetails) {
        this.tpaDetails = tpaDetails;
    }

    public InsuranceDetailsDto getInsuranceDetails() {
        return insuranceDetails;
    }

    public void setInsuranceDetails(InsuranceDetailsDto insuranceDetails) {
        this.insuranceDetails = insuranceDetails;
    }

    public List<InsuranceForTpaDto> getInsuranceForTpa() {
        return insuranceForTpa;
    }

    public void setInsuranceForTpa(List<InsuranceForTpaDto> insuranceForTpa) {
        this.insuranceForTpa = insuranceForTpa;
    }

    public List<BenefitsDto> getBenefits() {
        return benefits;
    }

    public void setBenefits(List<BenefitsDto> benefits) {
        this.benefits = benefits;
    }

    public HealthPolicyDto getHealthPolicy() {
        return healthPolicy;
    }

    public void setHealthPolicy(HealthPolicyDto healthPolicy) {
        this.healthPolicy = healthPolicy;
    }

    public List<ModulesDto> getModules() {
        return modules;
    }

    public void setModules(List<ModulesDto> modules) {
        this.modules = modules;
    }
}
