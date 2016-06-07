package com.nzion.domain.emr.soap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.UnitOfMeasurement;

@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientBirthHistory extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	private String orderOfBirth;
	private Float weight;
	private UnitOfMeasurement weightUom;
	private Float height;
	private UnitOfMeasurement heightUom;
	private String delivery;
	private String comments;
	private BirthHistorySection birthHistorySection;
	private String sentence;

	public PatientBirthHistory() {

	}

	public PatientBirthHistory(BirthHistorySection birthHistorySection) {
	this.birthHistorySection = birthHistorySection;
	}

	@OneToOne
	@JoinColumn(name = "SECTION_ID")
	public BirthHistorySection getBirthHistorySection() {
	return birthHistorySection;
	}

	public void setBirthHistorySection(BirthHistorySection birthHistorySection) {
	this.birthHistorySection = birthHistorySection;
	}

	public String getOrderOfBirth() {
	return orderOfBirth;
	}

	public void setOrderOfBirth(String orderOfBirth) {
	this.orderOfBirth = orderOfBirth;
	}

	public Float getWeight() {
	return weight;
	}

	public void setWeight(Float weight) {
	this.weight = weight;
	}

	@OneToOne
	public UnitOfMeasurement getWeightUom() {
	return weightUom;
	}

	public void setWeightUom(UnitOfMeasurement weightUom) {
	this.weightUom = weightUom;
	}

	public Float getHeight() {
	return height;
	}

	public void setHeight(Float height) {
	this.height = height;
	}

	@OneToOne
	public UnitOfMeasurement getHeightUom() {
	return heightUom;
	}

	public void setHeightUom(UnitOfMeasurement heightUom) {
	this.heightUom = heightUom;
	}

	public String getComments() {
	return comments;
	}

	public void setComments(String comments) {
	this.comments = comments;
	}

	public PatientBirthHistory createCopy() {
	PatientBirthHistory birthHistory = new PatientBirthHistory();
	birthHistory.setOrderOfBirth(this.orderOfBirth);
	birthHistory.setWeight(this.weight);
	birthHistory.setWeightUom(this.weightUom);
	birthHistory.setHeight(this.height);
	birthHistory.setHeightUom(this.heightUom);
	birthHistory.setDelivery(this.delivery);
	birthHistory.setComments(this.comments);
	birthHistory.setSentence(this.sentence);
	birthHistory.setId(null);
	return birthHistory;
	}

	@Column
	public String getSentence() {
	if (sentence == null) {
		sentence = buildSentence();
	}
	return sentence;
	}

	private String buildSentence() {
	StringBuilder buffer = new StringBuilder();
	if (StringUtils.isNotBlank(delivery)) {
		buffer.append(" Patient's birth was a " + delivery + " delivery.");
	}
	if (height != null && heightUom != null) {
		buffer.append(" Patient's height at birth was " + height + " " + heightUom.getDescription()).append(".");
	}

	if (weight != null && weightUom != null) {
		buffer.append(" Patient's weight at birth was " + weight + " " + weightUom.getDescription()).append(".");
	}

	if (StringUtils.isNotBlank(orderOfBirth)) {
		buffer.append(" Patient is " + orderOfBirth + " child in the family. ");
	}

	return buffer.toString();
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	public String getDelivery() {
	return delivery;
	}

	public void setDelivery(String delivery) {
	this.delivery = delivery;
	}
}
