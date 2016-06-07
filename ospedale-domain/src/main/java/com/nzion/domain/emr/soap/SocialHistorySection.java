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

@Entity
@DiscriminatorValue("SOCIALHISTORY")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class SocialHistorySection extends SoapSection {

	private PatientSocialHistory patientSocialHistory;

	@OneToOne(mappedBy = "soapSection")
	@Cascade(CascadeType.ALL)
	public PatientSocialHistory getPatientSocialHistory() {
	return patientSocialHistory;
	}

	public void setPatientSocialHistory(PatientSocialHistory patientSocialHistory) {
	this.patientSocialHistory = patientSocialHistory;
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {
//	SocialHistorySection historySection = (SocialHistorySection) previousSoapSections.get(MODULE_NAME);
//	if (historySection == null || historySection.getPatientSocialHistory() == null) return;
//	PatientSocialHistory copiedHistory = historySection.getPatientSocialHistory().createCopy();
//	copiedHistory.setSoapSection(this);
//	setPatientSocialHistory(copiedHistory);
//	this.setPastRecordExists(historySection.getEdited() || historySection.getPastRecordExists());
	}

	private static final long serialVersionUID = 1L;

	public static final String MODULE_NAME = "Social Hx";

	@Override
	public void onSaveUpdate() {
	if (patientSocialHistory != null) patientSocialHistory.setSentence(null);
	}

	@Override
	public boolean edited() {
	return patientSocialHistory!= null;
	}
}