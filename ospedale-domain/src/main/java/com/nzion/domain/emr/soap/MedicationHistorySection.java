package com.nzion.domain.emr.soap;

import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@DiscriminatorValue("MEDICATION_HX")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
@Proxy(lazy=false)
public class MedicationHistorySection extends SoapSection {

	private Set<PatientRx> patientRxs;

	private Boolean noKnownMedicationHistory;

	private Boolean drugHistoryViewedFlag;

	public Boolean isNoKnownMedicationHistory() {
	return noKnownMedicationHistory == null ? false : noKnownMedicationHistory;
	}

	public void setNoKnownMedicationHistory(Boolean noKnownMedicationHistory) {
	this.noKnownMedicationHistory = noKnownMedicationHistory;
	}

	@OneToMany(targetEntity=PatientRx.class,mappedBy="medicationHistorySection",cascade=javax.persistence.CascadeType.ALL)
	public Set<PatientRx> getPatientRxs() {
	return patientRxs;
	}

	public void setPatientRxs(Set<PatientRx> patientRxs) {
	this.patientRxs = patientRxs;
	}

	public void addPatientRx(PatientRx patientRx) {
	patientRx.setPatient(this.getSoapNote().getPatient());
	patientRx.setProvider(this.getSoapNote().getProvider());
	patientRx.setMedicationHistorySection(this);
	getPatientRxs().add(patientRx);
	this.noKnownMedicationHistory = false;
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {
	}

	private static final long serialVersionUID = 1L;

	public static final String MODULE_NAME = "MedicationHx";

	@Override
	public boolean edited() { 
	return (noKnownMedicationHistory != null && noKnownMedicationHistory) || UtilValidator.isNotEmpty(patientRxs);
	}

	public Boolean getDrugHistoryViewedFlag() {
	return drugHistoryViewedFlag;
	}

	public void setDrugHistoryViewedFlag(Boolean drugHistoryViewedFlag) {
	this.drugHistoryViewedFlag = drugHistoryViewedFlag;
	}
}
