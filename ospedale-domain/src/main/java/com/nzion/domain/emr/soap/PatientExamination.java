package com.nzion.domain.emr.soap;

import static com.nzion.util.SentenceUtil.SPACE;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.Speciality;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.OrganSystem;
import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.Question;
import com.nzion.util.SentenceUtil;
import com.nzion.util.UtilValidator;

@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientExamination extends IdGeneratingBaseEntity implements PatientQuestionAnswerBindingFactory {

	private Set<PatientQuestionAnswer> patientQAs;

	private OrganSystem organSystem;

	private String freeText;

	private ExaminationSection examinationSection;

	private String normal = NORMAl;

	private String sentence;

	private QATemplate qaTemplate;

	private Speciality speciality;
	
	public PatientExamination() {
	}

	public PatientExamination(OrganSystem organSystem, ExaminationSection examinationSection) {
	this.organSystem = organSystem;
	this.examinationSection = examinationSection;
	}

	@OneToOne
	@JoinColumn(name = "QA_TEMPLATE_ID")
	public QATemplate getQaTemplate() {
	return qaTemplate;
	}

	public void setQaTemplate(QATemplate qaTemplate) {
	this.qaTemplate = qaTemplate;
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

	@ManyToOne(targetEntity = ExaminationSection.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "EXAMINATION_SECTION_ID")
	public ExaminationSection getExaminationSection() {
	return examinationSection;
	}

	public void setExaminationSection(ExaminationSection examinationSection) {
	this.examinationSection = examinationSection;
	}

	@OneToMany(targetEntity = PatientQuestionAnswer.class, fetch = FetchType.EAGER,orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	@JoinTable(name = "PATIENT_EXAMINATION_QUESTION_ANSWER")
	public Set<PatientQuestionAnswer> getPatientQAs() {
	if (patientQAs == null) patientQAs = new HashSet<PatientQuestionAnswer>();
	return patientQAs;
	}

	public void setPatientQAs(Set<PatientQuestionAnswer> patientQAs) {
	this.patientQAs = patientQAs;
	}

	private static final long serialVersionUID = 1L;

	public PatientQuestionAnswer getPatientQuestionAnswerFor(Question question) {
	PatientQuestionAnswer local = new PatientQuestionAnswer(question);
	for (PatientQuestionAnswer pqa : getPatientQAs())
		if (pqa.equals(local)) return pqa;
	getPatientQAs().add(local);
	return local;
	}

	@Override
	@Transient
	public String getRemarks() {
	return getFreeText();
	}

	@Override
	public void setRemarks(String remark) {
	setFreeText(remark);
	}

	public String getNormal() {
	return normal;
	}

	public void setNormal(String normal) {
	this.normal = normal;
	}

	@Override
	public boolean hasAnswered(Question question) {
	for (PatientQuestionAnswer pqa : getPatientQAs())
		if (pqa.getQuestion().equals(question)) return UtilValidator.isNotEmpty(pqa.getAnswerString());
	return false;
	}

	@Column(length = 1024)
	public String getSentence() {
	if (StringUtils.isEmpty(sentence)) sentence = buildSentenceForOrganSystem();
	return sentence;
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

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	@ManyToOne
	public Speciality getSpeciality() {
	return speciality;
	}

	public void setSpeciality(Speciality speciality) {
	this.speciality = speciality;
	}
	
	public static boolean isNotAsked(String normal){
	return !(NORMAl.equals(normal));
	}

	public static final String NORMAl = "NOTASKED";
}
