package com.nzion.domain.emr.soap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientMedication
		extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;

	private Patient patient;
	private PatientSoapNote soapNote;
	private String drugName;
	private String genericName;
	private String drugId;
	
	@OneToOne
	@JoinColumn(name = "PATIENT_ID")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public void setSoapNote(PatientSoapNote soapNote) {
	this.soapNote = soapNote;
	}

	@OneToOne
	public PatientSoapNote getSoapNote() {
	return soapNote;
	}

	public String getDrugName() {
	return drugName;
	}

	public void setDrugName(String drugName) {
	this.drugName = drugName;
	}

	public String getGenericName() {
	return genericName;
	}

	public void setGenericName(String genericName) {
	this.genericName = genericName;
	}

	@Column(name="DRUG_ID")
	public String getDrugId() {
	return drugId;
	}

	public void setDrugId(String drugId) {
	this.drugId = drugId;
	}
}