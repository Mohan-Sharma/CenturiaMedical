package com.nzion.domain.emr.soap;

import static com.nzion.util.SentenceUtil.SPACE;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.OrganSystem;
import com.nzion.domain.emr.Question;
import com.nzion.util.SentenceUtil;
import com.nzion.util.UtilValidator;

@Entity
@DiscriminatorValue("ROS_RECORD")
public class PatientRosQA extends IdGeneratingBaseEntity implements PatientQuestionAnswerBindingFactory {

	private Set<PatientQuestionAnswer> patientQAs;

	private OrganSystem organSystem;

	private String freeText;

	private RosSection rosSection;

	private String normal = NORMAl;

	private String sentence;

	public PatientRosQA(OrganSystem organSystem, RosSection rosSection) {
	this.organSystem = organSystem;
	this.rosSection = rosSection;
	}

	@OneToOne
	@JoinColumn(name = "ORGAN_SYSTEM_ID")
	public OrganSystem getOrganSystem() {
	return organSystem;
	}

	public void setOrganSystem(OrganSystem organSystem) {
	this.organSystem = organSystem;
	}

	public String getFreeText() {
	return freeText;
	}

	public void setFreeText(String freeText) {
	this.freeText = freeText;
	}

	@ManyToOne(targetEntity = RosSection.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "ROS_SECTION_ID")
	public RosSection getRosSection() {
	return rosSection;
	}

	public void setRosSection(RosSection rosSection) {
	this.rosSection = rosSection;
	}

	@OneToMany(targetEntity = PatientQuestionAnswer.class, fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinTable(name = "PATIENT_ROS_QUESTION_ANSWER")
	public Set<PatientQuestionAnswer> getPatientQAs() {
	if (patientQAs == null) patientQAs = new HashSet<PatientQuestionAnswer>();
	return patientQAs;
	}

	public void setPatientQAs(Set<PatientQuestionAnswer> patientQAs) {
	this.patientQAs = patientQAs;
	}

	public PatientQuestionAnswer getPatientQuestionAnswerFor(Question question) {
	PatientQuestionAnswer local = new PatientQuestionAnswer(question);
	for (PatientQuestionAnswer pqa : getPatientQAs())
		if (pqa.equals(local)) return pqa;
	getPatientQAs().add(local);
	return local;
	}

	@Override
	public void setRemarks(String remark) {
	this.freeText = remark;
	}

	private static final long serialVersionUID = 1L;

	@Override
	@Transient
	public String getRemarks() {
	return getFreeText();
	}

	public String getNormal() {
	return normal;
	}

	public void setNormal(String normal) {
	this.normal = normal;
	}

	public String getSentence() {
	if (StringUtils.isEmpty(sentence)) sentence = buildSentenceForOrganSystem();
	return sentence;
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	public PatientRosQA() {
	}

	@Override
	public boolean hasAnswered(Question question) {
	for (PatientQuestionAnswer pqa : getPatientQAs())
		if (pqa.getQuestion().equals(question)) return UtilValidator.isNotEmpty(pqa.getAnswerString());
	return false;
	}

	public String buildSentenceForOrganSystem() {
	StringBuilder paragraphBuffer = new StringBuilder();
	String freeText = "";
	Set<PatientQuestionAnswer> qas;
	freeText = getFreeText();
	qas = getPatientQAs();
	if (UtilValidator.isNotEmpty(qas)) {
		String sentence = SentenceUtil.buildSentenceForQAs(qas);
		paragraphBuffer.append(sentence + SPACE);
	} else
		if (StringUtils.isNotEmpty(freeText)) {
			paragraphBuffer.append(SentenceUtil.appendFullStop(freeText) + SPACE);
		}
	return paragraphBuffer.toString();
	}

	public static final String NORMAl = "NOTASKED";
}
