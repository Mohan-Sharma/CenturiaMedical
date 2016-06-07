package com.nzion.domain.emr.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.OrganSystem;
import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@DiscriminatorValue("ROSSECTION")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class RosSection extends SoapSection {

	private Set<PatientRosQA> patientRosQAs;

	private QATemplate qaTemplate;

	private String rosNote;

	@OneToOne
	@JoinColumn(name = "QA_TEMPLATE_ID")
	public QATemplate getQaTemplate() {
	return qaTemplate;
	}

	public void setQaTemplate(QATemplate qaTemplate) {
	this.qaTemplate = qaTemplate;
	}

	@OneToMany(mappedBy = "rosSection", fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	public Set<PatientRosQA> getPatientRosQAs() {
	if (patientRosQAs == null) patientRosQAs = new HashSet<PatientRosQA>();
	return patientRosQAs;
	}

	public void setPatientRosQAs(Set<PatientRosQA> patientRosQAs) {
	this.patientRosQAs = patientRosQAs;
	}

	@Transient
	public PatientRosQA getPatientRosQA(OrganSystem organSystem) {
	for (PatientRosQA rosQA : getPatientRosQAs()) {
		if (rosQA.getOrganSystem().equals(organSystem)) return rosQA;
	}
	PatientRosQA rosQA = new PatientRosQA(organSystem, this);
	patientRosQAs.add(rosQA);
	return rosQA;
	}

	@Transient
	public Set<PatientRosQA> getPatientRosQuetionAnswers(Collection<OrganSystem> organSystems) {
	for (OrganSystem organSystem : organSystems) {
		getPatientRosQAs().add(new PatientRosQA(organSystem, this));
	}
	return getPatientRosQAs();
	}

	@Override
	public void onSaveUpdate() {
	if (patientRosQAs != null) {
		for (PatientRosQA each : patientRosQAs) {
			each.setSentence(null);
		}
	}
	}

	@Transient
	public String getShortNote() {
	List<OrganSystem> abnormalOrgans = new ArrayList<OrganSystem>();
	for (PatientRosQA each : getPatientRosQAs()) {
		if ("ABNORMAL".equals(each.getNormal())) abnormalOrgans.add(each.getOrganSystem());
	}
	if (abnormalOrgans.size() == 0) {
		return null;
	} else {
		StringBuilder buffer = new StringBuilder("All organ system found to be Normal except ");
		for (Iterator<OrganSystem> iter = abnormalOrgans.iterator(); iter.hasNext();) {
			buffer.append(iter.next().getDesc());
			if (iter.hasNext()) {
				buffer.append(", ");
			}
		}
		return buffer.toString();
	}
	}

	@Transient
	public String getSentence() {
	StringBuilder sentenceBuffer = new StringBuilder();
	if(UtilValidator.isNotEmpty(getPatientRosQAs())){
	if (UtilValidator.isNotEmpty(getRosNote())){
		sentenceBuffer.append(getRosNote());
		sentenceBuffer.append("\r\n");
	}
	List<OrganSystem> normalOrganSystems = new ArrayList<OrganSystem>();
	List<PatientRosQA> abnormalOrganSystems = new ArrayList<PatientRosQA>();
	List<OrganSystem> notReviewdOrgansSystems = new ArrayList<OrganSystem>();
	for (PatientRosQA patientRosQA : getPatientRosQAs()){
		if ("NORMAL".equalsIgnoreCase(patientRosQA.getNormal()))
			normalOrganSystems.add(patientRosQA.getOrganSystem());
		else
			if ("ABNORMAL".equalsIgnoreCase(patientRosQA.getNormal()))
				abnormalOrganSystems.add(patientRosQA);
			else
				notReviewdOrgansSystems.add(patientRosQA.getOrganSystem());
	}
	if(UtilValidator.isNotEmpty(abnormalOrganSystems)){
		for(PatientRosQA patientRosQA : abnormalOrganSystems)
			sentenceBuffer.append(patientRosQA.buildSentenceForOrganSystem());
	return sentenceBuffer.toString();
	}
	if(UtilValidator.isNotEmpty(normalOrganSystems) && normalOrganSystems.size() == getPatientRosQAs().size()){
		sentenceBuffer.append("All organ systems found to be normal");
		return sentenceBuffer.toString();
	}
	if(UtilValidator.isNotEmpty(notReviewdOrgansSystems) && notReviewdOrgansSystems.size() == getPatientRosQAs().size()){
		sentenceBuffer.append("All organ systems has not been reviewed");
		return sentenceBuffer.toString();
	}
	for(OrganSystem organSystem : notReviewdOrgansSystems)
		sentenceBuffer.append(organSystem.getDesc()).append(",");
	sentenceBuffer.append("has not been reviewed and rest are found as normal");
	}
	return sentenceBuffer.toString();
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {

	}

	@Override
	public boolean edited() {
	if (UtilValidator.isEmpty(patientRosQAs)) return false;
	for (PatientRosQA patientRosQA : patientRosQAs)
		if (!PatientRosQA.NORMAl.equals(patientRosQA.getNormal())) return true;
	return false;
	}

	public String getRosNote() {
	return rosNote;
	}

	public void setRosNote(String rosNote) {
	this.rosNote = rosNote;
	}

	private static final long serialVersionUID = 1L;

	public static final String MODULE_NAME = "ROS";
}