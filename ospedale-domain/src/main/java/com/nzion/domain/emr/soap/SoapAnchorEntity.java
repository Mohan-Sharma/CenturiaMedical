package com.nzion.domain.emr.soap;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;

import com.nzion.domain.Confidentiality;
import com.nzion.domain.Person;
import com.nzion.domain.base.IdGeneratingBaseEntity;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */

@MappedSuperclass
public class SoapAnchorEntity extends IdGeneratingBaseEntity implements Confidentiality {

	private boolean confidential = false;

	private boolean locked = false;

	private Set<Person> authorizedPersons;

	private Set<Person> unAuthorizedPersons;

	@Column(name="IS_LOCKED")
	public boolean isLocked() {
	return locked;
	}

	public void setLocked(boolean locked) {
	this.locked = locked;
	}

	@ManyToMany(targetEntity=Person.class)
	public Set<Person> getUnAuthorizedPersons() {
	return unAuthorizedPersons;
	}

	public void setUnAuthorizedPersons(Set<Person> unAuthorizedPersons) {
	this.unAuthorizedPersons = unAuthorizedPersons;
	}

	@ManyToMany(targetEntity=Person.class)
	public Set<Person> getAuthorizedPersons() {
	return authorizedPersons;
	}

	public void setAuthorizedPersons(Set<Person> authorizedPersons) {
	this.authorizedPersons = authorizedPersons;
	}

	@Column(name = "IS_CONFIDENTIAL")
	public boolean isConfidential() {
	return confidential;
	}

	public void setConfidential(boolean confidential) {
	this.confidential = confidential;
	}
	
	protected void copy(SoapAnchorEntity anchorEntity){
	anchorEntity.confidential = confidential;
	anchorEntity.locked = locked;
	anchorEntity.authorizedPersons = authorizedPersons;
	anchorEntity.unAuthorizedPersons = unAuthorizedPersons;
	}

	private static final long serialVersionUID = 1L;
}