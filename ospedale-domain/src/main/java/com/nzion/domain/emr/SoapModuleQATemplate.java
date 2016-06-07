package com.nzion.domain.emr;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.Provider;
import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames= {"PROVIDER_ID","SOAP_MODULE_ID"}))
@Filters( { })
public class SoapModuleQATemplate extends IdGeneratingBaseEntity{
	
	private static final long serialVersionUID = 1L;
	private Provider provider;
	private SoapModule soapModule;
	private QATemplate qaTemplate;
	
	public SoapModuleQATemplate() {
	}
	
	public SoapModuleQATemplate(Provider provider, SoapModule soapModule, QATemplate qaTemplate) {
	this.provider = provider;
	this.soapModule = soapModule;
	this.qaTemplate = qaTemplate;
	}

	@OneToOne
	@JoinColumn(name="PROVIDER_ID")
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}
	
	@OneToOne
	@JoinColumn(name="SOAP_MODULE_ID")
	public SoapModule getSoapModule() {
	return soapModule;
	}
	
	public void setSoapModule(SoapModule soapModule) {
	this.soapModule = soapModule;
	}
	
	@OneToOne
	public QATemplate getQaTemplate() {
	return qaTemplate;
	}
	
	public void setQaTemplate(QATemplate qaTemplate) {
	this.qaTemplate = qaTemplate;
	}
}
