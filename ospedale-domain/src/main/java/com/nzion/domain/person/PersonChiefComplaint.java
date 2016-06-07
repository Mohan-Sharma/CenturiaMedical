package com.nzion.domain.person;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.nzion.domain.Person;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.ChiefComplaint;

@Entity
@Table(name="PERSON_CHIEF_COMPLAINT")
public class PersonChiefComplaint extends IdGeneratingBaseEntity{
	
	private static final long serialVersionUID = 1L;

	private Person person;
	
	private ChiefComplaint chiefComplaint;
	
	public PersonChiefComplaint(){
	}
	
	public PersonChiefComplaint(Person person,ChiefComplaint chiefComplaint){
	this.person = person;
	this.chiefComplaint = chiefComplaint;
	}
	
	@Transient
	public String getComplainName() {
	return chiefComplaint==null?"":chiefComplaint.getComplainName();
	}

	@OneToOne
	public Person getPerson() {
	return person;
	}

	public void setPerson(Person person) {
	this.person = person;
	}

	@OneToOne
	public ChiefComplaint getChiefComplaint() {
	return chiefComplaint;
	}

	public void setChiefComplaint(ChiefComplaint chiefComplaint) {
	this.chiefComplaint = chiefComplaint;
	}
}
