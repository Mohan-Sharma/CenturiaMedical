package com.nzion.domain.emr;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;

/**
 * @author Sandeep Prusty
 * Oct 21, 2010
 */

@Entity
@Table(name = "REFERAL_LETTER_TEMPLATE")
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class ReferalLetterTemplate extends IdGeneratingBaseEntity {

	private String templateName;

	private String salutation;

	private String introductoryNote;

	private String endNote;

	private String complimentaryClose;

	private Set<SoapModule> modules;

	private ReferalType referalType;
	
	private String referalTypeName;
	
	public String getTemplateName() {
	return templateName;
	}

	public void setTemplateName(String templateName) {
	this.templateName = templateName;
	}

	public String getSalutation() {
	return salutation;
	}

	public void setSalutation(String salutation) {
	this.salutation = salutation;
	}

	public String getIntroductoryNote() {
	return introductoryNote;
	}

	public void setIntroductoryNote(String introductoryNote) {
	this.introductoryNote = introductoryNote;
	}

	public String getEndNote() {
	return endNote;
	}

	public void setEndNote(String endNote) {
	this.endNote = endNote;
	}

	public String getComplimentaryClose() {
	return complimentaryClose;
	}

	public void setComplimentaryClose(String complimentaryClose) {
	this.complimentaryClose = complimentaryClose;
	}

	@ManyToMany(targetEntity = SoapModule.class, fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public Set<SoapModule> getModules() {
	if (modules == null) modules = new HashSet<SoapModule>();
	return modules;
	}

	public void setModules(Set<SoapModule> modules) {
	this.modules = modules;
	}

	@Enumerated(EnumType.STRING)
	public ReferalType getReferalType() {
	return referalType;
	}

	public void setReferalType(ReferalType referalType) {
	this.referalType = referalType;
	}

	public static enum ReferalType {
		INBOUND, OUTBOUND;
	}

	@Override
	public String toString() {
	return templateName;
	}

	public String getReferalTypeName() {
	return referalTypeName;
	}

	public void setReferalTypeName(String referalTypeName) {
	this.referalTypeName = referalTypeName;
	}

	private static final long serialVersionUID = 1L;
}
