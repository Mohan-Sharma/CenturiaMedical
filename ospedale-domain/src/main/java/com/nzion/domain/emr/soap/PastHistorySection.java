package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.soap.PatientChronicDisease;
import com.nzion.domain.emr.ChronicDisease;
import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty Dec 1, 2010
 */
@Entity
@DiscriminatorValue("PASTHISTORY")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PastHistorySection extends SoapSection {

	private Boolean noKnownIlnessHistory;

	private Set<PatientPastOperationHistory> patientPastOperationHitories;

	private Set<PatientPastTreatmentHistory> patientPastTreatmentHistories;

	private Set<PatientPastObservationHistory> patientPastObservationHistories;

	private Set<PatientChronicDisease> patientChronicDiseases;

	@OneToMany(targetEntity = PatientChronicDisease.class, fetch = FetchType.EAGER, mappedBy = "soapSection", orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientChronicDisease> getPatientChronicDiseases() {
		return patientChronicDiseases;
	}

	public void setPatientChronicDiseases(
			Set<PatientChronicDisease> patientChronicDiseases) {
		this.patientChronicDiseases = patientChronicDiseases;
	}

	public Boolean isNoKnownIlnessHistory() {
		return noKnownIlnessHistory == null ? false : noKnownIlnessHistory;
	}

	public void setNoKnownIlnessHistory(Boolean noKnownIlnessHistory) {
		this.noKnownIlnessHistory = noKnownIlnessHistory;
	}

	@OneToMany(targetEntity = PatientPastOperationHistory.class, fetch = FetchType.EAGER, mappedBy = "soapSection", orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientPastOperationHistory> getPatientPastOperationHitories() {
		return patientPastOperationHitories;
	}

	public void setPatientPastOperationHitories(
			Set<PatientPastOperationHistory> patientPastOperationHitories) {
		this.patientPastOperationHitories = patientPastOperationHitories;
	}

	@OneToMany(targetEntity = PatientPastTreatmentHistory.class, fetch = FetchType.EAGER, mappedBy = "soapSection", orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientPastTreatmentHistory> getPatientPastTreatmentHistories() {
		return patientPastTreatmentHistories;
	}

	public void setPatientPastTreatmentHistories(
			Set<PatientPastTreatmentHistory> patientPastTreatmentHistories) {
		this.patientPastTreatmentHistories = patientPastTreatmentHistories;
	}

	@Transient
	public void addPatientPastTreatmentHistory(
			PatientPastTreatmentHistory pastTreatmentHistory) {
		getPatientPastTreatmentHistories().add(pastTreatmentHistory);
	}

	@Transient
	public void addPatientPastOperationHistory(
			PatientPastOperationHistory pastOperationHistory) {
		getPatientPastOperationHitories().add(pastOperationHistory);
	}

	@OneToMany(targetEntity = PatientPastObservationHistory.class, fetch = FetchType.EAGER, mappedBy = "soapSection", orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientPastObservationHistory> getPatientPastObservationHistories() {
		return patientPastObservationHistories;
	}

	public void setPatientPastObservationHistories(
			Set<PatientPastObservationHistory> patientPastObservationHistories) {
		this.patientPastObservationHistories = patientPastObservationHistories;
	}

	public void addPatientPastObservationHistory(
			PatientPastObservationHistory pastObservationHistory) {
		getPatientPastObservationHistories().add(pastObservationHistory);
	}

	// 415684004 Rule out
	// 410516002 Ruled out
	// 413322009 Resolved

	// The problems with all the above statuses are not copied.

	// copies records with active and chronic status from previous past history
	// problem list
	@Override
	public void init(PatientSoapNote soapNote, SoapModule module,
			PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {
	}

	private static final long serialVersionUID = 1L;

	public static final String MODULE_NAME = "Past Hx";

	public void addPatientDiseases(
			Set<PatientChronicDisease> patientChronicDiseases) {
		this.patientChronicDiseases = patientChronicDiseases;
	}

	@Override
	public boolean edited() {
		return (UtilValidator.isNotEmpty(patientPastOperationHitories)
				|| UtilValidator.isNotEmpty(patientPastTreatmentHistories) || UtilValidator
				.isNotEmpty(patientPastObservationHistories) || UtilValidator.isNotEmpty(patientChronicDiseases));
	}

	@Transient
	public Set<PatientChronicDisease> getChroniDisseasesForPatient(List<ChronicDisease> chronicDiseases) {
	Set<PatientChronicDisease> patientChronicDiseases = new HashSet<PatientChronicDisease>();
		for (ChronicDisease chronicDisease : chronicDiseases) {
			patientChronicDiseases.add(buildPatientChronicDisease(chronicDisease));
		}
		return patientChronicDiseases;
	}

	private PatientChronicDisease buildPatientChronicDisease(ChronicDisease chronicDisease) {
		PatientChronicDisease patientChronicDisease = new PatientChronicDisease(
				chronicDisease, this.getSoapNote().getPatient());
		patientChronicDisease.setSoapSection(this);
		patientChronicDisease.setDisableEdit(false);
		return patientChronicDisease;
	}
}