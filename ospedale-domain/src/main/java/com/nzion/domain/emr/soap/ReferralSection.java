package com.nzion.domain.emr.soap;

import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

@Entity
@DiscriminatorValue("REFERRAL")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class ReferralSection extends SoapSection {

	private Set<PatientReferral> patientReferrals;
	
	private SoapReferral soapReferral;
	
//	@OneToMany(targetEntity = PatientReferral.class, fetch = FetchType.EAGER)
//	@Cascade(CascadeType.ALL)
//	@JoinColumn(name = "REFERRAL_SECTION_ID")
//	public Set<PatientReferral> getPatientReferrals() {
//	return patientReferrals;
//	}
//
//	public void setPatientReferrals(Set<PatientReferral> patientReferrals) {
//	this.patientReferrals = patientReferrals;
//	}
//
//	@Transient
//	public void addPatientReferral(PatientReferral patientReferral) {
//	if (patientReferrals == null) patientReferrals = new HashSet<PatientReferral>();
//	for (PatientReferral patientReferral2 : getPatientReferrals()) {
//		if (patientReferral.getReferral().equals(patientReferral2.getReferral())) return;
//	}
//	getPatientReferrals().add(patientReferral);
//	}

	@OneToOne(targetEntity = SoapReferral.class,fetch = FetchType.EAGER,mappedBy = "referralSection",orphanRemoval=true)
	@Cascade(CascadeType.ALL)
	public SoapReferral getSoapReferral() {
	return soapReferral;
	}

	public void setSoapReferral(SoapReferral soapReferral) {
	this.soapReferral = soapReferral;
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {

	}

	private static final long serialVersionUID = 1L;

	public static final String MODULE_NAME = "Referral";

	@Override
	public boolean edited() {
	if(!UtilValidator.isAllEmpty(soapReferral, "referral","modules"))
		return true;
	return false;
	}
}