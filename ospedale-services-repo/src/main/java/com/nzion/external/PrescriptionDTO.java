package com.nzion.external;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Mohan Sharma on 04-04-2015.
 */

public class PrescriptionDTO {
    private String clinicId;
    private String afyaId;
    private List<PrescriptionLineItem> rows;
    private String visitId;
    private Date visitDate;
    private String firstName;
    private String lastName;
    private String mobile;
    private String patientType;
    private String doctorId;
    private String doctorName;
    private String clinicName;
    private String hisBenefitId;
    private String benefitId;
    private String groupId;
    private String moduleName;
    private String isOrderApproved;
    private Date dateOfBirth;
    private BigDecimal corporateCopay;
    private String corporateCopayType;
    private String corporatePrimaryPayer;
    private boolean mobileNumberVisibleForDelivery;
    private boolean orderFromMobileOrPortal = false;

    public PrescriptionDTO(String clinicId, String afyaId, List<PrescriptionLineItem> rows, String visitId, 
            Date visitDate, String firstName, String lastName, String mobile, String patientType, String doctorId, String doctorName, String clinicName, 
            String hisBenefitId, String benefitId, String groupId, String moduleName, String isOrderApproved, 
            Date dateOfBirth,BigDecimal corporateCopay,String corporateCopayType,String corporatePrimaryPayer, boolean mobileNumberVisibleForDelivery) {
        this.clinicId = clinicId;
        this.afyaId = afyaId;
        this.rows = rows;
        this.visitId = visitId;
        this.visitDate = visitDate;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobile = mobile;
        this.patientType = patientType;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.clinicName = clinicName;
        this.hisBenefitId = hisBenefitId;
        this.benefitId = benefitId;
        this.groupId = groupId;
        this.moduleName = moduleName;
        this.isOrderApproved = isOrderApproved;
        this.dateOfBirth = dateOfBirth;
        this.corporateCopay = corporateCopay;
        this.corporateCopayType = corporateCopayType;
        this.corporatePrimaryPayer = corporatePrimaryPayer;
        this.mobileNumberVisibleForDelivery = mobileNumberVisibleForDelivery;
    }

    public PrescriptionDTO() {

    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getAfyaId() {
        return afyaId;
    }

    public void setAfyaId(String afyaId) {
        this.afyaId = afyaId;
    }

    public List<PrescriptionLineItem> getRows() {
        return rows;
    }

    public void setRows(List<PrescriptionLineItem> rows) {
        this.rows = rows;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPatientType() {
        return patientType;
    }

    public void setPatientType(String patientType) {
        this.patientType = patientType;
    }

    public String getHisBenefitId() {
        return hisBenefitId;
    }

    public void setHisBenefitId(String hisBenefitId) {
        this.hisBenefitId = hisBenefitId;
    }

    public String getBenefitId() {
        return benefitId;
    }

    public void setBenefitId(String benefitId) {
        this.benefitId = benefitId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getIsOrderApproved() {
        return isOrderApproved;
    }

    public void setIsOrderApproved(String isOrderApproved) {
        this.isOrderApproved = isOrderApproved;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public BigDecimal getCorporateCopay() {
        return corporateCopay;
    }

    public void setCorporateCopay(BigDecimal corporateCopay) {
        this.corporateCopay = corporateCopay;
    }

    public String getCorporateCopayType() {
        return corporateCopayType;
    }

    public void setCorporateCopayType(String corporateCopayType) {
        this.corporateCopayType = corporateCopayType;
    }

    public String getCorporatePrimaryPayer() {
        return corporatePrimaryPayer;
    }

    public void setCorporatePrimaryPayer(String corporatePrimaryPayer) {
        this.corporatePrimaryPayer = corporatePrimaryPayer;
    }

    public boolean isMobileNumberVisibleForDelivery() {
        return mobileNumberVisibleForDelivery;
    }

    public void setMobileNumberVisibleForDelivery(boolean mobileNumberVisibleForDelivery) {
        this.mobileNumberVisibleForDelivery = mobileNumberVisibleForDelivery;
    }

    public boolean isOrderFromMobileOrPortal() {
        return orderFromMobileOrPortal;
    }

    public void setOrderFromMobileOrPortal(boolean orderFromMobileOrPortal) {
        this.orderFromMobileOrPortal = orderFromMobileOrPortal;
    }

}
