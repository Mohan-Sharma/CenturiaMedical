package com.nzion.domain.emr.soap;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.ChronicDisease;

@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientChronicDisease extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;

	private String mantainedBy;
	private Integer treatedForHowLong;

	private Integer duration;
	private ChronicDisease chronicDisease;
	private Patient patient;
	private PastHistorySection soapSection;
	private String durationType;
	private String treatedForHowLongType;

	public PatientChronicDisease() {
	}

	public PatientChronicDisease(ChronicDisease chronicDisease, Patient patient) {
	this.chronicDisease = chronicDisease;
	this.patient = patient;
	}

	public String getTreatedForHowLongType() {
	return treatedForHowLongType;
	}

	public void setTreatedForHowLongType(String treatedForHowLongType) {
	this.treatedForHowLongType = treatedForHowLongType;
	}

	private String mantainedProcess;

	@OneToOne
	public ChronicDisease getChronicDisease() {
	return chronicDisease;
	}

	public void setChronicDisease(ChronicDisease chronicDisease) {
	this.chronicDisease = chronicDisease;
	}

	@OneToOne
	@JoinColumn(name = "PATIENT_ID", nullable = false)
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@ManyToOne
	@JoinColumn(name = "PAST_HX_SECTION_ID", nullable = false)
	public PastHistorySection getSoapSection() {
	return soapSection;
	}

	public void setSoapSection(PastHistorySection section) {
	this.soapSection = section;
	}

	public String getMantainedBy() {
	return mantainedBy;
	}

	public void setMantainedBy(String mantainedBy) {
	this.mantainedBy = mantainedBy;
	}

	public String getMantainedProcess() {
	return mantainedProcess;
	}

	public void setMantainedProcess(String mantainedProcess) {
	this.mantainedProcess = mantainedProcess;
	}

	public Integer getTreatedForHowLong() {
	return treatedForHowLong;
	}

	public void setTreatedForHowLong(Integer treatedForHowLong) {
	this.treatedForHowLong = treatedForHowLong;
	}

	public Integer getDuration() {
	return duration;
	}

	public void setDuration(Integer duration) {
	this.duration = duration;
	}

	public String getDurationType() {
	return durationType;
	}

	public void setDurationType(String durationType) {
	this.durationType = durationType;
	}

	private boolean diseaseSelected;

	@Transient
	public boolean isDiseaseSelected() {
	return diseaseSelected;
	}

	public void setDiseaseSelected(boolean diseaseSelected) {
	this.diseaseSelected = diseaseSelected;
	}

	private boolean disableEdit =true;

	@Transient
	public boolean isDisableEdit() {
	return disableEdit;
	}

	public void setDisableEdit(boolean disableEdit) {
	this.disableEdit = disableEdit;
	}
}

