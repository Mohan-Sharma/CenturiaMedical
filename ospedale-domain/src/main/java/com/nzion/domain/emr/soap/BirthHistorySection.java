package com.nzion.domain.emr.soap;

import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

@Entity
@DiscriminatorValue("BIRTHHISTORY")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class BirthHistorySection extends SoapSection {

	private PatientBirthHistory patientBirthHistory;

	@OneToOne(mappedBy = "birthHistorySection")
	@Cascade(CascadeType.ALL)
	public PatientBirthHistory getPatientBirthHistory() {
	return patientBirthHistory;
	}

	public void setPatientBirthHistory(PatientBirthHistory patientBirthHistory) {
	this.patientBirthHistory = patientBirthHistory;
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {
	BirthHistorySection birthHistorySection = (BirthHistorySection) previousSoapSections.get(MODULE_NAME);
	if(birthHistorySection == null || birthHistorySection.getPatientBirthHistory() == null)
		return;
	PatientBirthHistory copiedBirthHx = birthHistorySection.getPatientBirthHistory().createCopy();
	copiedBirthHx.setBirthHistorySection(this);
	setPatientBirthHistory(copiedBirthHx);
	}

	@Override
	public void onSaveUpdate() {
	if (patientBirthHistory != null) {
		patientBirthHistory.setSentence(null);
	}
	}
	
	public static final String MODULE_NAME = "Birth Hx" ;
	
	private static final long serialVersionUID = 1L;

	@Override
	public boolean edited() {
	if(patientBirthHistory == null)  
		return false;
	return !UtilValidator.isAllEmpty(patientBirthHistory, "orderOfBirth", "weight", "height","delivery","weightUom","heightUom") || UtilValidator.isNotEmpty(patientBirthHistory.getComments());
	}
}