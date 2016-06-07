package com.nzion.dto;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/15/15
 * Time: 2:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class InsuranceGroupDto {
    private String insuredGroupId;
    private Date planStartDate;
    private Date planEndDate;
    private String policyNo;
    private String healthPolicyId;
    private String payerId;
    private String policyName;
    private String policyNoPolicyName;
    
    
    public String getPolicyNoPolicyName() {
    	policyNoPolicyName = policyNo + " - " + policyName;
		return policyNoPolicyName;
	}

	public void setPolicyNoPolicyName(String policyNoPolicyName) {
		this.policyNoPolicyName = policyNoPolicyName;
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

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    public String getHealthPolicyId() {
        return healthPolicyId;
    }

    public void setHealthPolicyId(String healthPolicyId) {
        this.healthPolicyId = healthPolicyId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
    
    
}
