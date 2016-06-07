package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

@Entity
@DiscriminatorValue("PRELIMINARY_DIAGNOSIS")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PreliminaryDiagnosisSection extends SoapSection{
	private Set<PatientIcd> icds;

	private Set<PatientCpt> cpts;	

	@OneToMany(targetEntity = PatientIcd.class, mappedBy = "preliminaryDiagnosisSection",orphanRemoval=true)
	@Cascade(value=CascadeType.ALL)
	public Set<PatientIcd> getIcds() {
	return icds;
	}

	public void setIcds(Set<PatientIcd> icds) {
	this.icds = icds;
	}

	@OneToMany(targetEntity = PatientCpt.class, mappedBy = "preliminaryDiagnosisSection",orphanRemoval=true)
	@Cascade(value=CascadeType.ALL)
	public Set<PatientCpt> getCpts() {
	return cpts;
	}

	public void setCpts(Set<PatientCpt> cpts) {
	this.cpts = cpts;
	}

	public void addIcd(PatientIcd patientIcd,PatientSoapNote patientSoapNote) {
	if(UtilValidator.isEmpty(icds))
		icds = new HashSet<PatientIcd>();
	patientIcd.setPreliminaryDiagnosisSection(this);
	patientIcd.setSoapNote(patientSoapNote);
	patientIcd.setPatient(patientSoapNote.getPatient());
	if(patientIcd.getOnSetDate()==null)
		patientIcd.setOnSetDate(patientSoapNote.getDate());
	boolean exist = false;
	for(PatientIcd eachPatientIcd : getIcds()){
		if(patientIcd.getIcdElement().equals(eachPatientIcd.getIcdElement()))
		exist = true;
	}
	if(!exist || getIcds().size()==0)
	icds.add(patientIcd);
	}

	public void addCpt(PatientCpt patientCpt) {
	if(UtilValidator.isEmpty(cpts))
		cpts = new HashSet<PatientCpt>();
	patientCpt.setPreliminaryDiagnosisSection(this);
	boolean exist = false;
	for(PatientCpt eachPatientCpt : getCpts()){
		if(patientCpt.getCpt().equals(eachPatientCpt.getCpt()))
		exist = true;
	}
	if(!exist || getCpts().size()==0)
		cpts.add(patientCpt);
	}
	
	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {
	}

	public static final String MODULE_NAME = "Diagnosis";

	private static final long serialVersionUID = 1L;

	@Override
	public boolean edited() {
	return (UtilValidator.isNotEmpty(icds) || UtilValidator.isNotEmpty(cpts) || getSoapNote().getPatient().getDateOfDeath() != null) ;
	}
}
