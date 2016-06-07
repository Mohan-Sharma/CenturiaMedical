package com.nzion.domain.emr;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.Enumeration;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.enums.AnswerType;
import com.nzion.util.UtilValidator;

@Entity
@Table(name = "QUESTIONS_NEW")
@Filters( { 	@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class Question extends IdGeneratingBaseEntity implements Comparable<Question> {
	private static final long serialVersionUID = 1L;
	private Set<Answer> answers = new HashSet<Answer>();
	private String desc;
	private Integer sortOrder;
	private SoapModule soapModule;
	private Enumeration gender;
	private OrganSystem organSystem;
	private AnswerType answerType=AnswerType.FREE_TEXT;
	private String freeText;
	private QATemplate qaTemplate;
	private boolean emcalculation;
	private String sentence;

	public String getSentence() {
	return sentence;
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	@OneToMany(mappedBy = "question")
	@OrderBy("sortOrder")
	@Cascade(value = CascadeType.SAVE_UPDATE)
	@Fetch(FetchMode.JOIN)
	public Set<Answer> getAnswers() {
	if(answers == null)
		answers = new HashSet<Answer>();		
	return answers;
	}

	public void addAnswers(Set<Answer> answers) {
	if (UtilValidator.isNotEmpty(answers)) {
		for (Answer answer : answers) {
			answer.setQuestion(this);
			this.answers.add(answer);
		}
	}
	this.answers = answers;
	}

	public void addAnswer(Answer answer) {
	answer.setQuestion(this);
	this.answers.add(answer);
	}

	public void setAnswers(Set<Answer> answers) {
	this.answers = answers;
	}

	@Column(name = "QUESTION_TEXT")
	public String getDesc() {
	return desc;
	}

	public void setDesc(String question) {
	this.desc = question;
	}

	@Column(name = "SORT_ORDER")
	public Integer getSortOrder() {
	return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
	this.sortOrder = sortOrder;
	}

	@ManyToOne(optional = true)
	@JoinColumn(name = "SOAP_MODULE_ID")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	public SoapModule getSoapModule() {
	return soapModule;
	}

	public void setSoapModule(SoapModule soapModule) {
	this.soapModule = soapModule;
	}

	@ManyToOne(optional = true)
	@JoinColumn(name = "ORGAN_SYSTEM_ID")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	public OrganSystem getOrganSystem() {
	return organSystem;
	}

	public void setOrganSystem(OrganSystem organSystem) {
	this.organSystem = organSystem;
	}

	@Override
	public String toString() {
	return desc;
	}

	public int compareTo(Question that) {
	if (this.equals(that)) return 0;
	if (this.sortOrder == null || that.sortOrder == null) return -1;
	return this.sortOrder.compareTo(that.sortOrder);
	}

	@Enumerated(EnumType.STRING)
	public AnswerType getAnswerType() {
	return answerType;
	}

	public void setAnswerType(AnswerType answerType) {
	this.answerType = answerType;
	}

	@Column(length = 512)
	public String getFreeText() {
	return freeText;
	}

	public void setFreeText(String freeText) {
	this.freeText = freeText;
	}

	@ManyToOne
	public QATemplate getQaTemplate() {
	return qaTemplate;
	}

	public void setQaTemplate(QATemplate qaTemplate) {
	this.qaTemplate = qaTemplate;
	}

	@OneToOne
	public Enumeration getGender() {
	return gender;
	}

	public void setGender(Enumeration gender) {
	this.gender = gender;
	}

	@Column(name="EM_CALCULATION")
	public boolean isEmcalculation() {
	return emcalculation;
	}

	public void setEmcalculation(boolean emcalculation) {
	this.emcalculation = emcalculation;
	}
	
	public Answer retrieveAnswer(String answerName){
	for(Answer answer : getAnswers()){
		if(answerName.equals(answer.getName()))
			return answer;
	}
	return null;
	}
}