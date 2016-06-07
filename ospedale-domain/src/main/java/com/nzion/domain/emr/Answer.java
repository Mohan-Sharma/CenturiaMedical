package com.nzion.domain.emr;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import javax.persistence.*;

/**
 * @author Sudarshan
 */
@Entity
@Table(name = "ANSWER_NEW")
@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)")
public class Answer
		extends IdGeneratingBaseEntity implements Comparable<Answer> {

	private static final long serialVersionUID = 1L;
	private String name;
	private String sentence;
	private Integer sortOrder;
	private Question question;
	
	@Column(name = "NAME")
	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	public String getSentence() {
	return sentence;
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	@Column(name = "SORT_ORDER", insertable = false, updatable = false)
	public Integer getSortOrder() {
	return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
	this.sortOrder = sortOrder;
	}

	@ManyToOne(optional = true)
	@JoinColumn(name = "QUESTION_ID")
	public Question getQuestion() {
	return question;
	}

	public void setQuestion(Question question) {
	this.question = question;
	}

	@Override
	public String toString() {
	return name;
	}

	public int compareTo(Answer that) {
	if (this.sortOrder == null || that.sortOrder == null) return -1;
	return this.sortOrder.compareTo(that.sortOrder);
	}
}