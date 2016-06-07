package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.Question;
import com.nzion.domain.emr.SoapModule;
import com.nzion.util.SentenceUtil;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 7, 2010
 */

@Entity
@DiscriminatorValue("OTHER_SECTION")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class OtherSoapNoteSection extends SoapSection implements PatientQuestionAnswerBindingFactory {

	private String remarks;

	private Set<PatientQuestionAnswer> qas;

	private String sentence;

	private QATemplate qaTemplate;

	@OneToOne
	@JoinColumn(name = "TEMPLATE_ID")
	public QATemplate getQaTemplate() {
	return qaTemplate;
	}

	public void setQaTemplate(QATemplate qaTemplate) {
	this.qaTemplate = qaTemplate;
	}

	@OneToMany(targetEntity = PatientQuestionAnswer.class, fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinTable(name = "OTHER_SOAP_SECTION_QUESTION_ANSWER")
	@Fetch(FetchMode.SELECT)
	public Set<PatientQuestionAnswer> getQas() {
	if (qas == null) qas = new HashSet<PatientQuestionAnswer>();
	return qas;
	}

	public void setQas(Set<PatientQuestionAnswer> qas) {
	this.qas = qas;
	}

	@Override
	public PatientQuestionAnswer getPatientQuestionAnswerFor(Question question) {
	PatientQuestionAnswer local = new PatientQuestionAnswer(question);
	for (PatientQuestionAnswer pqa : getQas())
		if (pqa.equals(local)) return pqa;
	qas.add(local);
	return local;
	}

	@Override
	public String getRemarks() {
	return this.remarks;
	}

	@Override
	public void setRemarks(String remark) {
	this.remarks = remark;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {
	OtherSoapNoteSection that = (OtherSoapNoteSection) previousSoapSections.get(getSoapModule().getModuleName());
	if (that == null) return;
	this.remarks = that.remarks;
	getQas().clear();
	for (PatientQuestionAnswer pqa : that.getQas())
		this.qas.add(pqa.createCopy());
	}

	@Override
	public boolean hasAnswered(Question question) {
	for (PatientQuestionAnswer pqa : getQas())
		if (pqa.getQuestion().equals(question)) return UtilValidator.isNotEmpty(pqa.getAnswerString());
	return false;
	}

	public String getSentence() {
	if (sentence == null) 
		sentence = SentenceUtil.buildSentenceForQAs(qas);
	if(UtilValidator.isEmpty(sentence))
		sentence = this.remarks;
	return sentence;
	}
	
	public void onSaveUpdate(){
	this.sentence = null;
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	@Override
	public boolean edited() {
	return UtilValidator.isNotEmpty(remarks);
	}
}