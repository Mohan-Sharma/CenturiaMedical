package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

@Entity
@DiscriminatorValue("PROBLEMLIST")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class ProblemListSection extends SoapSection {

	private Set<PatientIcd> patientIcds = new HashSet<PatientIcd>();
	private Boolean noKnownProblem;

	public Boolean getNoKnownProblem() {
	return noKnownProblem;
	}

	public void setNoKnownProblem(Boolean noKnownProblem) {
	this.noKnownProblem = noKnownProblem;
	}

	@OneToMany(mappedBy = "problemListSection", fetch = FetchType.EAGER, orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientIcd> getPatientIcds() {
	return patientIcds;
	}

	public void setPatientIcds(Set<PatientIcd> patientIcds) {
	this.patientIcds = patientIcds;
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {
//	DiagnosisSection previousDiagnosisSection = (DiagnosisSection) previousSoapSections
//			.get(DiagnosisSection.MODULE_NAME);
////	Collection<PatientIcd> icds = new ArrayList<PatientIcd>();
//	if (previousDiagnosisSection != null) {
//		for (PatientIcd each : previousDiagnosisSection.getIcds()) {
//			if (isPatientIcdActive(each)) {
//				PatientIcd newIcd = each.createCopy();
//				newIcd.setSoapNote(this.getSoapNote());
//				newIcd.setProblemListSection(this);
//				newIcd.setOnSetDate(previousDiagnosisSection.getSoapNote().getDate());
//				this.patientIcds.add(newIcd);
//			}
//		}
//	}

	/* Problem List Section Record Should Not Be Copied ever*/
	
//	ProblemListSection previousProblemListSection = (ProblemListSection) previousSoapSections
//			.get(ProblemListSection.MODULE_NAME);
//
//	if (previousProblemListSection != null) icds.addAll(previousProblemListSection.getPatientIcds());
//
//	for (PatientIcd each : icds) {
//		if (isPatientIcdActive(each)) add(each.createCopy());
//	}
	}

	public void add(PatientIcd patientIcd) {
	if (patientIcd != null) patientIcd.setSoapNote(this.getSoapNote());
	patientIcd.setProblemListSection(this);
	patientIcds.add(patientIcd);
	}

	public static final String MODULE_NAME = "Problem List";

	private static final long serialVersionUID = 1L;

	@Override
	public boolean edited() {
	return !UtilValidator.isAllEmpty(this, "noKnownProblem", "patientIcds");
	}

}
