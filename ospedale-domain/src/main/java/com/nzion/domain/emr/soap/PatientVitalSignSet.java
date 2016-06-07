package com.nzion.domain.emr.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.VitalSign;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientVitalSignSet extends IdGeneratingBaseEntity {

	private String name;

	private Date recordedOn;
	
	private Date recordedDate;

	private Set<PatientVitalSign> vitalSigns;

	private Patient patient;

	private VitalSignSection vitalSignSection;

	public PatientVitalSignSet() {
	}

	public PatientVitalSignSet(VitalSignSection vitalSignSection, Collection<VitalSign> personVitalSigns, Patient patient) {
	vitalSigns = new HashSet<PatientVitalSign>();
	for (VitalSign personVitalSign : personVitalSigns) {
		PatientVitalSign patientVitalSign = new PatientVitalSign(personVitalSign, patient);
		patientVitalSign.setVitalSignSection(vitalSignSection);
		vitalSigns.add(patientVitalSign);
	}
	this.vitalSignSection = vitalSignSection;
	recordedOn = new Date();
	}

	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getRecordedOn() {
	return recordedOn;
	}

	public void setRecordedOn(Date recordedOn) {
	this.recordedOn = recordedOn;
	}

	@OneToMany(fetch = FetchType.EAGER)
	@Cascade(value = CascadeType.ALL)
	@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
	public Set<PatientVitalSign> getVitalSigns() {
	return vitalSigns;
	}

	public void setVitalSigns(Set<PatientVitalSign> vitalSigns) {
	this.vitalSigns = vitalSigns;
	}

	public boolean hasRecordings() {
	for (PatientVitalSign patientVitalSign : vitalSigns)
		if (patientVitalSign.getValue() != null) return true;
	return false;
	}

	@Transient
	public PatientVitalSign getPatientVitalSign(String vitalSignName) {
	for (PatientVitalSign vitalSign : getVitalSigns()) {
		if (vitalSignName.equalsIgnoreCase(vitalSign.getVitalSign().getName())) return vitalSign;
	}
	return null;
	}

	@Transient
	public List<PatientVitalSign> getNonDerivedVitalSigns() {
	List<PatientVitalSign> nonDerivedVitalSigns = new ArrayList<PatientVitalSign>();
	for (PatientVitalSign pvs : getVitalSigns()) {
		if (!Boolean.TRUE.equals(pvs.getVitalSign().getDerived())) nonDerivedVitalSigns.add(pvs);
	}
	return nonDerivedVitalSigns;
	}

	@OneToOne
	@JoinColumn(name = "PATIENT_ID")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public boolean checkForAllGivenRecordings(String... vitalSignNames) {
	if (UtilValidator.isEmpty(vitalSigns)) return false;
	for (String vitalSignName : vitalSignNames){
		PatientVitalSign vitalSign = getPatientVitalSign(vitalSignName); 
		if (vitalSign == null || UtilValidator.isEmpty(vitalSign.getValue())) 
			return false;
	}
	return true;
	}

	@ManyToOne
	@JoinColumn(name = "VITAL_SECTION_ID")
	public VitalSignSection getVitalSignSection() {
	return vitalSignSection;
	}

	public void setVitalSignSection(VitalSignSection vitalSignSection) {
	this.vitalSignSection = vitalSignSection;
	}

	public void setRecordedDate(Date recordedDate) {
		this.recordedDate = recordedDate;
	}

	public Date getRecordedDate() {
		return recordedDate;
	}

	private static final long serialVersionUID = 1L;
}