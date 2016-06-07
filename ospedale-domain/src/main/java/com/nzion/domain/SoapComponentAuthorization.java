package com.nzion.domain;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.enums.SoapComponents;

@Entity
@Table

public class SoapComponentAuthorization extends IdGeneratingBaseEntity implements Authorizable{

	private SoapComponents components;

	private Authorization authorization;

	public SoapComponentAuthorization(){
	}
	
	public SoapComponentAuthorization(SoapComponents soapComponents){
	this.components=soapComponents;
	}
	
	@Enumerated(EnumType.STRING)
	public SoapComponents getComponents() {
	return components;
	}

	public void setComponents(SoapComponents components) {
	this.components = components;
	}

	@Embedded
	public Authorization getAuthorization() {
	if (authorization == null) authorization = new Authorization(Roles.PROVIDER);
	return authorization;
	}

	public void setAuthorization(Authorization authorization) {
	this.authorization = authorization;
	}
	
	public boolean check(Authorization authorization){
	return (authorization == null) ? false : this.authorization.check(authorization);
	}
	
	@Override
	public String toString(){
	return components.toString();
	}

	private static final long serialVersionUID = 1L;
}