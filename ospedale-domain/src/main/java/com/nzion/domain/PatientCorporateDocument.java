package com.nzion.domain;

import java.sql.Blob;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
public class PatientCorporateDocument extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private Blob document;
	
	private String documentName;
	
	private String documentReferenceNumber;
	
	private Date date;
	
	private Date validUpTo;
	
	private String notes;
	
	private PatientCorporate patientCorporate;

	@Lob
	public Blob getDocument() {
		return document;
	}

	public void setDocument(Blob document) {
		this.document = document;
	}

	public String getDocumentReferenceNumber() {
		return documentReferenceNumber;
	}

	public void setDocumentReferenceNumber(String documentReferenceNumber) {
		this.documentReferenceNumber = documentReferenceNumber;
	}

	@Temporal(TemporalType.DATE)
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Temporal(TemporalType.DATE)
	public Date getValidUpTo() {
		return validUpTo;
	}

	public void setValidUpTo(Date validUpTo) {
		this.validUpTo = validUpTo;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	@ManyToOne
    @JoinColumn(name = "PATIENT_CORPORATE_ID")
	public PatientCorporate getPatientCorporate() {
		return patientCorporate;
	}

	public void setPatientCorporate(PatientCorporate patientCorporate) {
		this.patientCorporate = patientCorporate;
	}
	
	
	
	

}
