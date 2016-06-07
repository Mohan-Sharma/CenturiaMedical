package com.nzion.dto;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/16/15
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class BenefitsDto {

    private String benefitPlanId;
    private String benefitPlan;

    public String getBenefitPlanId() {
        return benefitPlanId;
    }

    public void setBenefitPlanId(String benefitPlanId) {
        this.benefitPlanId = benefitPlanId;
    }

    public String getBenefitPlan() {
        return benefitPlan;
    }

    public void setBenefitPlan(String benefitPlan) {
        this.benefitPlan = benefitPlan;
    }
}
