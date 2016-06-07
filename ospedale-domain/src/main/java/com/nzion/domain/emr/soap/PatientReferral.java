package com.nzion.domain.emr.soap;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Referral;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.ReferalLetterTemplate;

@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientReferral extends IdGeneratingBaseEntity {

	private Referral referral;

	private String reason;

	private String remarks;

	private com.nzion.domain.emr.ReferalLetterTemplate.ReferalType boundType;

	private ReferalLetterTemplate referalLetterTemplate;

	private Integer frequency;

	public PatientReferral() {

	}

	public PatientReferral(Referral referral) {
	this.referral = referral;
	}

	@OneToOne
	@JoinColumn(name = "REFERRAL_ID")
	public Referral getReferral() {
	return referral;
	}

	public void setReferral(Referral referral) {
	this.referral = referral;
	}

	public String getReason() {
	return reason;
	}

	public void setReason(String reason) {
	this.reason = reason;
	}

	public String getRemarks() {
	return remarks;
	}

	@Enumerated(EnumType.STRING)
	public com.nzion.domain.emr.ReferalLetterTemplate.ReferalType getBoundType() {
	return boundType;
	}

	public void setBoundType(com.nzion.domain.emr.ReferalLetterTemplate.ReferalType boundType) {
	this.boundType = boundType;
	}

	public Integer getFrequency() {
	return frequency;
	}

	public void setFrequency(Integer frequency) {
	this.frequency = frequency;
	}

	public void setRemarks(String remarks) {
	this.remarks = remarks;
	}

	@OneToOne
	@JoinColumn(name = "TEMPLATE_ID")
	public ReferalLetterTemplate getReferalLetterTemplate() {
	return referalLetterTemplate;
	}

	public void setReferalLetterTemplate(ReferalLetterTemplate referalLetterTemplate) {
	this.referalLetterTemplate = referalLetterTemplate;
	}

	private static final long serialVersionUID = 1L;

}
