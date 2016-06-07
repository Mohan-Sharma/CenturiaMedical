package com.nzion.domain.emr.soap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Person;
import com.nzion.domain.base.IdGeneratingBaseEntity;

/**
 * @author Sandeep Prusty
 * Dec 8, 2010
 */

@Entity
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class SoapNoteActor extends IdGeneratingBaseEntity {

	private Person person;

	private long actingRole;
	
	public SoapNoteActor() {}
	
	public SoapNoteActor(Person person, long actingRole) {
	this.person = person;
	this.actingRole = actingRole;
	}

	@OneToOne(targetEntity=Person.class)
	@JoinColumn(name="PERSON_ID")
	public Person getPerson() {
	return person;
	}

	public void setPerson(Person person) {
	this.person = person;
	}

	@Column(name="ACTING_ROLE")
	public long getActingRole() {
	return actingRole;
	}

	public void setActingRole(long actingRole) {
	this.actingRole = actingRole;
	}

	private static final long serialVersionUID = 1L;
}
