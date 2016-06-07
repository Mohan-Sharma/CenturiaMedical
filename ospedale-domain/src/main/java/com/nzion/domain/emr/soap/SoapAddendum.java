package com.nzion.domain.emr.soap;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Person;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.SoapModule;

@Entity
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class SoapAddendum extends IdGeneratingBaseEntity {
	private SoapModule soapModule;
	private String comments;
	private PatientSoapNote soapNote;
	private Person person;
	private Date addedOn;
	
	public SoapAddendum(){}
	
	public SoapAddendum(SoapAddendum that){
		this.soapModule = that.soapModule;
		this.soapNote = that.soapNote;
		this.person = that.person;
	}
	
	@Column(name = "ADDED_ON")
	public Date getAddedOn() {
	return addedOn;
	}

	public void setAddedOn(Date addedOn) {
	this.addedOn = addedOn;
	}

	@ManyToOne
	@JoinColumn(name="ADDENDUM_PERSON_ID")
	public Person getPerson() {
	return person;
	}
	
	public void setPerson(Person person) {
	this.person = person;
	}

	@OneToOne
	public SoapModule getSoapModule() {
	return soapModule;
	}

	public void setSoapModule(SoapModule soapModule) {
	this.soapModule = soapModule;
	}

	public String getComments() {
	return comments;
	}

	public void setComments(String comments) {
	this.comments = comments;
	}

	@ManyToOne
	public PatientSoapNote getSoapNote() {
	return soapNote;
	}

	public void setSoapNote(PatientSoapNote soapNote) {
	this.soapNote = soapNote;
	}

	private static final long serialVersionUID = 1L;
}