
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
@DiscriminatorValue("GENERAL_EXAMINATION")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class GeneralExaminationSection extends SoapSection {
	
	private static final long serialVersionUID = 1L;
	private Set<PatientGeneralExamination> patientGeneralExaminations;
	
	
	@OneToMany(targetEntity = PatientGeneralExamination.class,mappedBy="generalExaminationSection", orphanRemoval=true)
	@Cascade(value=CascadeType.ALL)
	public Set<PatientGeneralExamination> getPatientGeneralExaminations() {
		return patientGeneralExaminations;
	}

	public void setPatientGeneralExaminations(
			Set<PatientGeneralExamination> patientGeneralExaminations) {
		this.patientGeneralExaminations = patientGeneralExaminations;
	}

	public void addGeneralExamination(PatientGeneralExamination patientGeneralExamination){
		if(UtilValidator.isEmpty(patientGeneralExaminations))
			patientGeneralExaminations = new HashSet<PatientGeneralExamination>();
		patientGeneralExamination.setPatient(this.getSoapNote().getPatient());
		patientGeneralExamination.setGeneralExaminationSection(this);
		patientGeneralExaminations.add(patientGeneralExamination);
		}
	
	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {
	}
	
	@Override
	public boolean edited() {
		return UtilValidator.isNotEmpty(patientGeneralExaminations);
	}
	
	
	
	
	public static final String MODULE_NAME = "GeneralExamination" ;

	
}