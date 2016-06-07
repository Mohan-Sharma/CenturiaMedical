package com.nzion.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ActivePrescriptionDto {
	
	private String patientRxId;
	
	private String drugName;
	
	private String doctorName;
	
	private String frequency;
	
	private String freqQualifier;
	
	private String noOfDays;
	
	private String totalCount;
	
	private String startDate;
	
	private String pharmacyTenantId;
	
	private String pharmacyTenantName;
	
	private String orderQuantity;

	public String getPatientRxId() {
		return patientRxId;
	}

	public void setPatientRxId(String patientRxId) {
		this.patientRxId = patientRxId;
	}

	public String getDrugName() {
		return drugName;
	}

	public void setDrugName(String drugName) {
		this.drugName = drugName;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getFreqQualifier() {
		return freqQualifier;
	}

	public void setFreqQualifier(String freqQualifier) {
		this.freqQualifier = freqQualifier;
	}

	public String getNoOfDays() {
		return noOfDays;
	}

	public void setNoOfDays(String noOfDays) {
		this.noOfDays = noOfDays;
	}

	public String getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(String totalCount) {
		this.totalCount = totalCount;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getPharmacyTenantId() {
		return pharmacyTenantId;
	}

	public void setPharmacyTenantId(String pharmacyTenantId) {
		this.pharmacyTenantId = pharmacyTenantId;
	}

	public String getPharmacyTenantName() {
		return pharmacyTenantName;
	}

	public void setPharmacyTenantName(String pharmacyTenantName) {
		this.pharmacyTenantName = pharmacyTenantName;
	}

	public String getOrderQuantity() {
		return orderQuantity;
	}

	public void setOrderQuantity(String orderQuantity) {
		this.orderQuantity = orderQuantity;
	}
	
}
