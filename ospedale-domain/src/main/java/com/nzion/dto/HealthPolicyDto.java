package com.nzion.dto;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/16/15
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class HealthPolicyDto {
    private String healthPolicyId;
    private String healthPolicyName;
    private String insuranceId;

    public String getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(String insuranceId) {
        this.insuranceId = insuranceId;
    }

    public String getHealthPolicyId() {
        return healthPolicyId;
    }

    public void setHealthPolicyId(String healthPolicyId) {
        this.healthPolicyId = healthPolicyId;
    }

    public String getHealthPolicyName() {
        return healthPolicyName;
    }

    public void setHealthPolicyName(String healthPolicyName) {
        this.healthPolicyName = healthPolicyName;
    }
}
