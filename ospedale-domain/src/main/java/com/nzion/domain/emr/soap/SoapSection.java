package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.SoapModule;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "MODULE_NAME")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public abstract class SoapSection extends IdGeneratingBaseEntity {

	private PatientSoapNote soapNote;

	private boolean reviewed;

	private Set<PatientRxAlert> alerts;

	private SoapModule soapModule;

	private String sectionComments;
	
	protected Boolean pastRecordExists = Boolean.FALSE;
	
	protected Boolean edited = Boolean.FALSE;

    private String pharmacyTenantId;
    
	public String getPharmacyTenantId() {
        return pharmacyTenantId;
    }

    public void setPharmacyTenantId(String pharmacyTenantId) {
        this.pharmacyTenantId = pharmacyTenantId;
    }

	public Boolean getPastRecordExists() {
	return pastRecordExists;
	}

	public void setPastRecordExists(Boolean pastRecordExists) {
	this.pastRecordExists = pastRecordExists;
	}

	public Boolean getEdited() {
	return edited;
	}

	public void setEdited(Boolean edited) {
	this.edited = edited;
	}

	@OneToOne(targetEntity = SoapModule.class)
	@JoinColumn(name = "SOAP_MODULE_ID")
	public SoapModule getSoapModule() {
	return soapModule;
	}

	public void setSoapModule(SoapModule soapModule) {
	this.soapModule = soapModule;
	}

	@OneToOne(targetEntity = PatientSoapNote.class)
	@JoinColumn(name = "PATIENT_SOAP_NOTE_ID")
	public PatientSoapNote getSoapNote() {
	return soapNote;
	}

	public void setSoapNote(PatientSoapNote soapNote) {
	this.soapNote = soapNote;
	}

	private static final long serialVersionUID = 1L;

	public boolean isReviewed() {
	return reviewed;
	}

	public void setReviewed(boolean reviewed) {
	this.reviewed = reviewed;
	}

	@OneToMany(targetEntity = PatientRxAlert.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "SOAP_SECTION_ID")
	@Cascade(CascadeType.ALL)
	public Set<PatientRxAlert> getAlerts() {
	if (alerts == null) alerts = new HashSet<PatientRxAlert>();
	return alerts;
	}

	public void setAlerts(Set<PatientRxAlert> alerts) {
	this.alerts = alerts;
	}

	@Transient
	public void addPatientRxAlert(PatientRxAlert rxAlert) {
	rxAlert.setSoapNote(this.getSoapNote());
	getAlerts().add(rxAlert);
	}

	public void initialize(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {
	this.soapNote = soapNote;
	this.soapModule = module;
	init(soapNote, module, lastEncounter, previousSoapSections);
	}

	public abstract void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections);
	
	public abstract boolean edited();

	public void onSaveUpdate() {

	}

	public void setSectionComments(String sectionComments) {
	this.sectionComments = sectionComments;
	}

	public String getSectionComments() {
	return sectionComments;
	}
}