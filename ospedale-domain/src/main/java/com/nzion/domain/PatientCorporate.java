package com.nzion.domain;

import java.math.BigDecimal;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.pms.InsuranceProvider;

@Entity
public class PatientCorporate extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private String employeeId;
	
	private String designation;
	
	private String tariffCategoryId;
	
	private String notes;
	
	private String modeOfClaim;

	private String primaryPayor;
	
	private String corporateId;
	
	private Set<PatientCorporateDocument> patientCorporateDocuments;
	
	private Blob idCard;
	
	private String idCardName;
	
	private BigDecimal corporateCopay;
	
    private String corporateCopayType;
    
	public String getCorporateId() {
		return corporateId;
	}

	public void setCorporateId(String corporateId) {
		this.corporateId = corporateId;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getTariffCategoryId() {
		return tariffCategoryId;
	}

	public void setTariffCategoryId(String tariffCategoryId) {
		this.tariffCategoryId = tariffCategoryId;
	}

	public String getModeOfClaim() {
		return modeOfClaim;
	}

	public void setModeOfClaim(String modeOfClaim) {
		this.modeOfClaim = modeOfClaim;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getPrimaryPayor() {
		return primaryPayor;
	}

	public void setPrimaryPayor(String primaryPayor) {
		this.primaryPayor = primaryPayor;
	}

	@OneToMany(targetEntity = PatientCorporateDocument.class, mappedBy = "patientCorporate", fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
	public Set<PatientCorporateDocument> getPatientCorporateDocuments() {
		if(patientCorporateDocuments == null)
			patientCorporateDocuments = new HashSet<PatientCorporateDocument>();
		return patientCorporateDocuments;
	}

	public void setPatientCorporateDocuments(
			Set<PatientCorporateDocument> patientCorporateDocuments) {
		this.patientCorporateDocuments = patientCorporateDocuments;
	}

	@Lob
	public Blob getIdCard() {
		return idCard;
	}

	public void setIdCard(Blob idCard) {
		this.idCard = idCard;
	}

	public String getIdCardName() {
		return idCardName;
	}

	public void setIdCardName(String idCardName) {
		this.idCardName = idCardName;
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

}
