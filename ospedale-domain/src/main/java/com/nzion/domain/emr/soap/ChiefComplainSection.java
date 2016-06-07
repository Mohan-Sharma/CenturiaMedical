package com.nzion.domain.emr.soap;

import static com.nzion.util.SentenceUtil.AND;
import static com.nzion.util.SentenceUtil.COMMA;
import static com.nzion.util.SentenceUtil.FULL_STOP;
import static com.nzion.util.SentenceUtil.SPACE;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.Patient;
import com.nzion.domain.Schedule;
import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.SoapModule;
import com.nzion.util.SentenceUtil;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */

@Entity
@DiscriminatorValue("CHIEFCOMPLAINT")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class ChiefComplainSection extends SoapSection {

	private String patientComplaint;

	private String comment;

	private String sentence;

	private Set<PatientChiefComplaint> patientChiefComplaints;

	public ChiefComplainSection() {
	}

	public ChiefComplainSection(PatientSoapNote soapNote, Schedule schedule) {
	setSoapNote(soapNote);
	setComment(schedule.getComments());
	if (UtilValidator.isNotEmpty(schedule.getChiefComplaint())) addChiefComplaint(schedule.getChiefComplaint(), null);
	}

	public String getPatientComplaint() {
	return patientComplaint;
	}

	public void setPatientComplaint(String patientComplaint) {
	this.patientComplaint = patientComplaint;
	}

	public String getComment() {
	return comment;
	}

	public void setComment(String comment) {
	this.comment = comment;
	}

	@OneToMany(targetEntity = PatientChiefComplaint.class, mappedBy = "soapSection", fetch = FetchType.EAGER,orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientChiefComplaint> getPatientChiefComplaints() {
	if(patientChiefComplaints == null)
		patientChiefComplaints = new HashSet<PatientChiefComplaint>();
	return patientChiefComplaints;
	}

	public void addChiefComplaint(String complaint, QATemplate qaTemplateUsed) {
	for(PatientChiefComplaint chiefComplaint : getPatientChiefComplaints()){
		if(complaint.equalsIgnoreCase(chiefComplaint.getChiefComplaint()))
			return;
	}
	getPatientChiefComplaints().add(new PatientChiefComplaint(complaint, qaTemplateUsed, this));
	}	

	public void setPatientChiefComplaints(Set<PatientChiefComplaint> patientChiefComplaints) {
	this.patientChiefComplaints = patientChiefComplaints;
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {
	Schedule schedule = soapNote.getSchedule();
	setComment(schedule.getComments());
	if (UtilValidator.isNotEmpty(schedule.getChiefComplaint())) addChiefComplaint(schedule.getChiefComplaint(), null);
	}

	public String getSentence() {
	if (sentence == null) sentence = buildSentenceForChiefComplaints();
	return sentence;
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	public String buildSentenceForChiefComplaints() {
	Set<PatientChiefComplaint> chiefComplaints = getPatientChiefComplaints();
	if (UtilValidator.isEmpty(chiefComplaints)) {
		return SentenceUtil.EMPTY_STRING;
	}
	Patient patient = getSoapNote().getPatient();
    String age = null;
    if(patient.getDateOfBirth() != null)
	    age = UtilDateTime.calculateAge(patient.getDateOfBirth(), this.getSoapNote().getDate());
	String ageStr = null;
	if (UtilValidator.isNotEmpty(age)) 
		ageStr = age + SPACE + "old";
	String patientName = SentenceUtil.generateName(patient.getFirstName(), patient.getMiddleName(), patient.getLastName());
	StringBuilder sentenceBuilder = new StringBuilder("Patient");
	if (UtilValidator.isNotEmpty(patientName)) 
		sentenceBuilder.append(SPACE).append(patientName);
	if (UtilValidator.isNotEmpty(ageStr))
		sentenceBuilder.append(COMMA).append(SPACE).append(ageStr).append(COMMA);
	sentenceBuilder.append(SPACE);
	sentenceBuilder.append(chiefComplaints.size() == 0 ? "has no complaints" : (chiefComplaints.size() == 1 ? "has complaint of" : "has complaints of"));
	for (PatientChiefComplaint patientChiefComplaint : chiefComplaints) 
		sentenceBuilder.append(SPACE + patientChiefComplaint.getChiefComplaint() + COMMA);
	sentenceBuilder.replace(sentenceBuilder.length() - 1, sentenceBuilder.length(),  "");
	int lastCommaPos = sentenceBuilder.lastIndexOf(COMMA);
	if(lastCommaPos != -1)
		sentenceBuilder.replace(lastCommaPos, lastCommaPos + 1, SPACE + AND);
	sentenceBuilder.append(FULL_STOP);
	return sentenceBuilder.toString();
	}

	@Override
	public void onSaveUpdate() {
	sentence = buildSentenceForChiefComplaints();
	if(patientChiefComplaints!=null) {
		for (PatientChiefComplaint cc : patientChiefComplaints) {
			String hpiSentence = SentenceUtil.buildSentenceForQAs(cc.getQas());
			if(StringUtils.isEmpty(hpiSentence)){
				hpiSentence = cc.getRemarks();
			}
			cc.setSentence(hpiSentence);
		}
	}
	}
	
	private static final long serialVersionUID = 1L;

	@Override
	public boolean edited() {
	for(PatientChiefComplaint chiefComplaint : patientChiefComplaints)
		if(chiefComplaint.edited())
			return true;
	return false;
	}
}