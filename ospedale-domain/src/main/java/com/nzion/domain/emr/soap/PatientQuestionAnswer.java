package com.nzion.domain.emr.soap;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.Question;
import com.nzion.enums.AnswerType;
import com.nzion.util.Constants;

/**
 * @author Sandeep Prusty
 * Dec 13, 2010
 */

@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientQuestionAnswer extends IdGeneratingBaseEntity {

	private String answerString = "";
	
	private String sentence;

	private Question question;
	
	public PatientQuestionAnswer() {}
	
	public PatientQuestionAnswer(Question question){
	this.question = question;
	}

	public PatientQuestionAnswer(PatientQuestionAnswer that) {
	this.answerString = that.answerString;
	this.question = that.question;
	}

	public String getAnswerString() {
	return answerString;
	}

	public void setAnswerString(String answerString) {
	this.answerString = answerString;
	}

	public String getSentence() {
	return sentence;
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	@OneToOne
	@JoinColumn(name="QUESTION_ID")
	public Question getQuestion() {
	return question;
	}

	public void setQuestion(Question question) {
	this.question = question;
	}

	@Override
	public boolean equals(Object obj) {
	if(question == null || !(obj instanceof PatientQuestionAnswer))
		return super.equals(obj);
	PatientQuestionAnswer that = (PatientQuestionAnswer)obj;
	return question.equals(that.question);	
	}

	@Override
	public int hashCode() {
	return question == null ? 0 : getQuestion().hashCode();
	}
	
	public boolean isSameAnswerString(String string){
	return string != null && string.trim().equalsIgnoreCase(answerString);
	}
	
	public PatientQuestionAnswer createCopy(){
	return new PatientQuestionAnswer(this);
	}

	@Override
	public String toString() {
	return  question + ":" + answerString;
	}
	
	public void appendAnswerString(String string, boolean add){
	if(!AnswerType.MULTIPLE_CHOICE.equals(question.getAnswerType())){
		setAnswerString(string);
		return;
	}
	answerString = add ? (answerString + Constants.COMMA + string) : answerString.replaceAll(string+Constants.COMMA, "");
	}	
	
	private static final long serialVersionUID = 1L;
}