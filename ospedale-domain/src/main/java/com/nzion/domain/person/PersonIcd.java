package com.nzion.domain.person;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.nzion.domain.Person;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.IcdElement;

@Entity
@Table(name = "PERSON_ICD")
public class PersonIcd extends IdGeneratingBaseEntity {

	private Person person;

	private IcdElement icd;

	public PersonIcd() {

	}

	public PersonIcd(Person person, IcdElement icd) {
	this.person = person;
	this.icd = icd;
	}

	@ManyToOne
	@JoinColumn(name = "PERSON_ID")
	public Person getPerson() {
	return person;
	}

	public void setPerson(Person person) {
	this.person = person;
	}

	@ManyToOne
	@JoinColumn(name = "ICD_ID")
	public IcdElement getIcd() {
	return icd;
	}

	public void setIcd(IcdElement icd) {
	this.icd = icd;
	}

	private static final long serialVersionUID = 1L;
}
