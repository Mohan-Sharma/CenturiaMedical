package com.nzion.domain.emr;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.Speciality;
import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table(name = "QATemplate", uniqueConstraints = { @UniqueConstraint(columnNames = { "NAME", "SOAP_MODULE_ID" }) })
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class QATemplate
		extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	private String name;
	private String description;
	private Set<Question> questions;
	private SoapModule soapModule;
	private Set<OrganSystem> organSystems;
	private boolean defaultTemplate;
	private boolean publicAccess;
	private Speciality speciality;

	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	@OneToMany(mappedBy = "qaTemplate", fetch = FetchType.EAGER)
	public Set<Question> getQuestions() {
	return questions;
	}

	public void setQuestions(Set<Question> questions) {
	this.questions = questions;
	}

	public void addQuestion(Question q) {
	if (questions == null) {
		questions = new HashSet<Question>();
	}
	q.setQaTemplate(this);
	questions.add(q);
	}

	@ManyToOne
	@JoinColumn(name = "SOAP_MODULE_ID")
	public SoapModule getSoapModule() {
	return soapModule;
	}

	public void setSoapModule(SoapModule soapModule) {
	this.soapModule = soapModule;
	}

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "ORGANSYSTEM_TEMPLATE_ASSOC")
	public Set<OrganSystem> getOrganSystems() {
	return organSystems;
	}

	public void setOrganSystems(Set<OrganSystem> organSystems) {
	this.organSystems = organSystems;
	}

	public String getDescription() {
	return description;
	}

	public void setDescription(String description) {
	this.description = description;
	}

	public boolean isDefaultTemplate() {
	return defaultTemplate;
	}

	public void setDefaultTemplate(boolean defaultTemplate) {
	this.defaultTemplate = defaultTemplate;
	}

	@Column(name = "PUBLIC")
	public boolean isPublicAccess() {
	return publicAccess;
	}

	public void setPublicAccess(boolean publicAccess) {
	this.publicAccess = publicAccess;
	}

	@ManyToOne(targetEntity = Speciality.class,optional=true)
	public Speciality getSpeciality() {
	return speciality;
	}

	public void setSpeciality(Speciality speciality) {
	this.speciality = speciality;
	}
}