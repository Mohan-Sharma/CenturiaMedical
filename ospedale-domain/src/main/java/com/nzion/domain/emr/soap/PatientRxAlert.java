package com.nzion.domain.emr.soap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.enums.PatientRxAlertType;

@Entity
@Filters( {
	@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class PatientRxAlert extends IdGeneratingBaseEntity {

	private String alertMessage;

	private PatientSoapNote soapNote;
	
	private int rating;
	
	//Drug Name which are having interaction(drugName1 <-> drugName2)
	private String relatedDrugName;
	
	//Allergy / Drug
	private PatientRxAlertType type;

	public PatientRxAlert() {
	}

	public PatientRxAlert(String alertMessage,int rating) {
	this.alertMessage = alertMessage;
	this.rating=rating;
	}

	private static final long serialVersionUID = 1L;

	@Column(length=1500)
	public String getAlertMessage() {
	return alertMessage;
	}

	public void setAlertMessage(String alertMessage) {
	this.alertMessage = alertMessage;
	}

	@OneToOne
	public PatientSoapNote getSoapNote() {
	return soapNote;
	}

	public void setSoapNote(PatientSoapNote soapNote) {
	this.soapNote = soapNote;
	}

	@Enumerated
	public PatientRxAlertType getType() {
	return type;
	}

	public void setType(PatientRxAlertType type) {
	this.type = type;
	}

	public int getRating() {
	return rating;
	}

	public void setRating(int rating) {
	this.rating = rating;
	}
	
	public String getRelatedDrugName() {
	return relatedDrugName;
	}

	public void setRelatedDrugName(String relatedDrugName) {
	this.relatedDrugName = relatedDrugName;
	}

}
