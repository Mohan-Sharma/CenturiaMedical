package com.nzion.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToOne;

import com.nzion.domain.base.IdGeneratingBaseEntity;

import java.sql.Blob;
import java.util.Date;

@Entity
public class PatientInsurance extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;

	private String groupId;

    private String groupName;

    private String policyNo;

    private Date startDate;

    private Date endDate;

    private String healthPolicyId;

    private String healthPolicyName;

    private String uhid;

    private String insuranceCode;

    private String insuranceName;

    private String insuranceType;

    private String benefitName;

    private String benefitId;
   
    private String patientType;
    
    private String relation;
    
    private Patient memberPatient; 
    
    private String membershipId;
    
    private Blob resource;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPolicyNo() {
        return policyNo;
    }

    public void setPolicyNo(String policyNo) {
        this.policyNo = policyNo;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    public String getInsuranceCode() {
        return insuranceCode;
    }

    public void setInsuranceCode(String insuranceCode) {
        this.insuranceCode = insuranceCode;
    }

    public String getInsuranceName() {
        return insuranceName;
    }

    public void setInsuranceName(String insuranceName) {
        this.insuranceName = insuranceName;
    }

    public String getBenefitName() {
        return benefitName;
    }

    public void setBenefitName(String benefitName) {
        this.benefitName = benefitName;
    }

    public String getBenefitId() {
        return benefitId;
    }

    public void setBenefitId(String benefitId) {
        this.benefitId = benefitId;
    }

	public String getPatientType() {
		return patientType;
	}

	public void setPatientType(String patientType) {
		this.patientType = patientType;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	public Patient getMemberPatient() {
		return memberPatient;
	}

	public void setMemberPatient(Patient memberPatient) {
		this.memberPatient = memberPatient;
	}

	public String getMembershipId() {
		return membershipId;
	}

	public void setMembershipId(String membershipId) {
		this.membershipId = membershipId;
	}

    public String getInsuranceType() {
        return insuranceType;
    }

    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    @Lob
	public Blob getResource() {
		return resource;
	}

	public void setResource(Blob resource) {
		this.resource = resource;
	}
    
    
}