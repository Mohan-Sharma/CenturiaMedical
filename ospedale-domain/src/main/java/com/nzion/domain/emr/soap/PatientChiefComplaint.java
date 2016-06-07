package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.Question;
import com.nzion.util.SentenceUtil;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */

@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientChiefComplaint extends IdGeneratingBaseEntity implements PatientQuestionAnswerBindingFactory{

	private String chiefComplaint;

	private QATemplate qaTemplateUsed;
	
	private ChiefComplainSection soapSection;
	
	private String remarks;

	private Set<PatientQuestionAnswer> qas;

	private String sentence;
	
	public PatientChiefComplaint(){}
	
	public PatientChiefComplaint(String chiefComplaint, QATemplate qaTemplateUsed, ChiefComplainSection section) {
	this.chiefComplaint = chiefComplaint;
	this.qaTemplateUsed = qaTemplateUsed;
	this.soapSection = section;
	}

	@ManyToOne(targetEntity=ChiefComplainSection.class)
	@JoinColumn(name="CHIEF_COMPLAIN_SECTION_ID")
	public ChiefComplainSection getSoapSection() {
	return soapSection;
	}

	public void setSoapSection(ChiefComplainSection chiefComplainSection) {
	this.soapSection = chiefComplainSection;
	}

	@Column
	public String getChiefComplaint() {
	return chiefComplaint;
	}

	public void setChiefComplaint(String chiefComplaint) {
	this.chiefComplaint = chiefComplaint;
	}

	@OneToOne(targetEntity=QATemplate.class)
	@JoinColumn(name="QA_TEMPLATE_ID")
	public QATemplate getQaTemplateUsed() {
	return qaTemplateUsed;
	}

	public void setQaTemplateUsed(QATemplate qaTemplateUsed) {
	this.qaTemplateUsed = qaTemplateUsed;
	}
	@Lob
	public String getRemarks() {
	return remarks;
	}

	public void setRemarks(String remarks) {
	this.remarks = remarks;
	}

	@OneToMany(targetEntity = PatientQuestionAnswer.class, fetch=FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinTable(name="PATIENT_CHIEF_COMPLAINT_QUESTION_ANSWER")
	public Set<PatientQuestionAnswer> getQas() {
	return qas;
	}

	public void setQas(Set<PatientQuestionAnswer> qas) {
	this.qas = qas;
	}
	
	public PatientQuestionAnswer getPatientQuestionAnswerFor(Question question){
	PatientQuestionAnswer local = new PatientQuestionAnswer(question);
	if(qas == null)
		qas = new HashSet<PatientQuestionAnswer>();
	for(PatientQuestionAnswer pqa : qas)
		if(pqa.equals(local))
			return pqa;
	qas.add(local);
	return local; 
	}

	@Override
	public boolean hasAnswered(Question question) {
	if(qas == null)
		qas = new HashSet<PatientQuestionAnswer>();
	for(PatientQuestionAnswer pqa : qas)
		if(pqa.getQuestion().equals(question))
			return UtilValidator.isNotEmpty(pqa.getAnswerString());
	return false;
	}
	
	private static final long serialVersionUID = 1L;


	@Column(length=1024)
	public String getSentence() {
	if(sentence==null)
		sentence=SentenceUtil.buildSentenceForQAs(this.getQas());
	return sentence;
	}


	public void setSentence(String sentence) {
	this.sentence = sentence;
	}
	
	public boolean edited() {
	if(UtilValidator.isNotEmpty(remarks))
		return true;
	if(UtilValidator.isEmpty(qas))
		return false;
	for(PatientQuestionAnswer questionAnswer : qas)
		if(UtilValidator.isNotEmpty(questionAnswer.getAnswerString()))
			return true;
	return false;
	}
}