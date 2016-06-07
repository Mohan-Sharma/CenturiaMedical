package com.nzion.domain.billing;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.util.UtilValidator;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by Saikiran on 3/9/2016.
 */
@Entity
@Table(name="TARIFF_CATEGORY")
public class TariffCategory extends IdGeneratingBaseEntity {
    private static final long serialVersionUID = 1L;
    private String patientCategory;
  //  private PatientCategory patientCategory;
    private String tariffCode;
    private String tariff;
    private String groupId;
    private String healthPolicyId;
    private BigDecimal corporateCopay;
    private String primaryPayor;
    private String corporateCopayType;
    private String corporateId;
   // private BigDecimal value;
    /*public enum PatientCategory {
       CASH_PAYING("Cash Paying"), INSURANCE("Insurance"), CORPORATE("Corporate");
       private String description;
       PatientCategory(String desc) {
           description = desc;
       }
       public String getDescription() {
           return description;
       }
    }*/

    /*@Enumerated(EnumType.STRING)
    public PatientCategory getPatientCategory() {
        return patientCategory;
    }

    public void setPatientCategory(PatientCategory patientCategory) {
        this.patientCategory = patientCategory;
    }*/

    @Column(name = "TARIFF_CODE")
    public String getTariffCode(){
        return tariffCode;
    }


    public void setTariffCode(String tariffCode) {
        this.tariffCode = tariffCode;
    }
    @Column(name = "TARIFF")
    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        if(UtilValidator.isNotEmpty(tariff))
            this.tariff = tariff;
    }
    @Column(name = "GROUP_ID")
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    @Column(name = "HEALTH_POLICY_ID")
    public String getHealthPolicyId() {
        return healthPolicyId;
    }

    public void setHealthPolicyId(String healthPolicyId) {
        this.healthPolicyId = healthPolicyId;
    }
    @Column(name = "PRIMARY_PAYOR")
    public String getPrimaryPayor() {
        return primaryPayor;
    }

    public void setPrimaryPayor(String primaryPayor) {
        this.primaryPayor = primaryPayor;
    }

    @Column(precision = 19, scale = 3, columnDefinition = "DECIMAL(19,3)")
    public BigDecimal getCorporateCopay() {
        return corporateCopay;
    }

    public void setCorporateCopay(BigDecimal corporateCopay) {
        this.corporateCopay = corporateCopay;
    }

    @Column(name = "CORPORATE_COPAY_TYPE")
    public String getCorporateCopayType() {
        return corporateCopayType;
    }

    public void setCorporateCopayType(String corporateCopayType) {
        this.corporateCopayType = corporateCopayType;
    }
    @Column(name = "CORPORATE_ID")
    public String getCorporateId() {
        return corporateId;
    }

    public void setCorporateId(String corporateId) {
        this.corporateId = corporateId;
    }

    @Column(name = "PATIENT_CATEGORY")
    public String getPatientCategory() {
        return patientCategory;
    }

    public void setPatientCategory(String patientCategory) {
        this.patientCategory = patientCategory;
    }
}


