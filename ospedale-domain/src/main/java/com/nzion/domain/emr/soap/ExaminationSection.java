package com.nzion.domain.emr.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.Speciality;
import com.nzion.domain.emr.OrganSystem;
import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@DiscriminatorValue("EXAMINATION")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class ExaminationSection extends SoapSection {

	private Set<PatientExamination> patientExaminationQAs;

	private String examinationNote;
	
	private Set<QATemplate> qaTemplates;
	

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "examinationSection",orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientExamination> getPatientExaminationQAs() {
	return patientExaminationQAs;
	}

	public void setPatientExaminationQAs(Set<PatientExamination> patientExaminationQAs) {
	this.patientExaminationQAs = patientExaminationQAs;
	}

	@Transient
	public PatientExamination getPatientExaminationQA(OrganSystem organSystem,QATemplate qaTemplate) {
	for (PatientExamination patientExaminationQA : getPatientExaminationQAs()) {
		if (patientExaminationQA.getOrganSystem().equals(organSystem)) return patientExaminationQA;
	}
	PatientExamination patientExamination = new PatientExamination(organSystem, this);
	patientExamination.setQaTemplate(qaTemplate);
	patientExamination.setSpeciality(qaTemplate.getSpeciality());
	getPatientExaminationQAs().add(patientExamination);
	return patientExamination;
	}
	
	@Transient
	public Set<PatientExamination> getPatientExaminations(QATemplate qaTemplate){
	Set<PatientExamination> examinations = new HashSet<PatientExamination>();
	if(UtilValidator.isEmpty(getSpecialities()))
		specialities = new HashSet<Speciality>();
	for(PatientExamination  examination : getPatientExaminationQAs()){
		specialities.add(examination.getSpeciality());
		if(qaTemplate.equals(examination.getQaTemplate()))
			examinations.add(examination);
	}	
	return examinations;
	}
	
	private Set<Speciality> specialities;
	
	@Transient
	public Set<Speciality> getSpecialities() {
	return specialities;
	}

	@Transient
	public Collection<? extends PatientExamination> getPatientExaminations(
			Collection<? extends OrganSystem> organSystems,QATemplate qaTemplate) {
	Set<PatientExamination> examinations = new HashSet<PatientExamination>();
	for (OrganSystem organSystem : organSystems){
		PatientExamination patientExamination = new PatientExamination(organSystem, this);
		patientExamination.setSpeciality(qaTemplate.getSpeciality());
		patientExamination.setQaTemplate(qaTemplate);
		examinations.add(patientExamination);
	}
	getPatientExaminationQAs().addAll(examinations);
	return examinations;
	}

	@Override
	public void onSaveUpdate() {
	if (patientExaminationQAs != null) {
		for (PatientExamination each : patientExaminationQAs) {
			each.setSentence(null);
		}
	}
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {
	}


	@Override
	public boolean edited() {
	if (UtilValidator.isEmpty(patientExaminationQAs)) return false;
	for (PatientExamination patientExamination : patientExaminationQAs)
		if (!PatientExamination.NORMAl.equals(patientExamination.getNormal())) return true;
	return false;
	}

	@Transient
	public String getSentence() {
	StringBuilder sentenceBuffer = new StringBuilder();
	Set<PatientExamination> allPatientExaminations = getPatientExaminationQAs();
	if(UtilValidator.isNotEmpty(allPatientExaminations)){
	if (UtilValidator.isNotEmpty(getExaminationNote())){
		sentenceBuffer.append(getExaminationNote());
		sentenceBuffer.append("\r\n");
	}
	List<OrganSystem> normalOrganSystems = new ArrayList<OrganSystem>();
	List<PatientExamination> abnormalOrganSystems = new ArrayList<PatientExamination>();
	List<OrganSystem> notReviewdOrgansSystems = new ArrayList<OrganSystem>();
	for (PatientExamination patientExamination :allPatientExaminations){
		if ("NORMAL".equalsIgnoreCase(patientExamination.getNormal()))
			normalOrganSystems.add(patientExamination.getOrganSystem());
		else
			if ("ABNORMAL".equalsIgnoreCase(patientExamination.getNormal()))
				abnormalOrganSystems.add(patientExamination);
			else
				notReviewdOrgansSystems.add(patientExamination.getOrganSystem());
	}
	if(UtilValidator.isNotEmpty(abnormalOrganSystems)){
		for(PatientExamination patientRosQA : abnormalOrganSystems)
			sentenceBuffer.append(patientRosQA.buildSentenceForOrganSystem());
	return sentenceBuffer.toString();
	}
	if(UtilValidator.isNotEmpty(normalOrganSystems) && normalOrganSystems.size() == getPatientExaminationQAs().size()){
		sentenceBuffer.append("All organ systems found to be normal");
		return sentenceBuffer.toString();
	}
	if(UtilValidator.isNotEmpty(notReviewdOrgansSystems) && notReviewdOrgansSystems.size() == getPatientExaminationQAs().size()){
		sentenceBuffer.append("All organ systems has not been reviewed");
		return sentenceBuffer.toString();
	}
	for(OrganSystem organSystem : notReviewdOrgansSystems)
		sentenceBuffer.append(organSystem.getDesc()).append(",");
	sentenceBuffer.append("has not been reviewed and rest are found as normal");
	}
	return sentenceBuffer.toString();
	}
	
	public String getExaminationNote() {
	return examinationNote;
	}

	public void setExaminationNote(String examinationNote) {
	this.examinationNote = examinationNote;
	}
	
	@ManyToMany
	@JoinTable(name = "EXAMINATIONSECTION_QATEMPLATE", joinColumns = { @JoinColumn(name = "EXAMINATION_SECTION_ID") }, inverseJoinColumns = { @JoinColumn(name = "QATEMPLATE_ID") })
	public Set<QATemplate> getQaTemplates() {
	return qaTemplates;
	}

	public void setQaTemplates(Set<QATemplate> qaTemplates) {
	this.qaTemplates = qaTemplates;
	}

	public void setSpecialities(Set<Speciality> specialities) {
	this.specialities = specialities;
	}
	

	public static final String MODULE_NAME = "PhysicalExamination";
	
	private static final long serialVersionUID = 1L;
}