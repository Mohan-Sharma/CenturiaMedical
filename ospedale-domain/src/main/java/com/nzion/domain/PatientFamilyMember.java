package com.nzion.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.FamilyMember;

@Entity
@Table
public class PatientFamilyMember extends IdGeneratingBaseEntity {

	private String name;

	private Boolean clinicEmployee;

	private Person employee;

	private Boolean restrictAccess;

	private FamilyMember familyMember;

	private ContactFields contacts;

	private Patient patient;

	public ContactFields getContacts() {
	if (contacts == null) contacts = new ContactFields();
	return contacts;
	}

	public void setContacts(ContactFields contacts) {
	this.contacts = contacts;
	}

	@Column
	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	@OneToOne
	@JoinColumn(name = "FAMILY_MEMBER_ID")
	public FamilyMember getFamilyMember() {
	return familyMember;
	}

	public void setFamilyMember(FamilyMember familyMember) {
	this.familyMember = familyMember;
	}

	@OneToOne
	@JoinColumn(name = "PATIENT_ID")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	private static final long serialVersionUID = 1L;

	public Boolean getClinicEmployee() {
	return clinicEmployee;
	}

	public void setClinicEmployee(Boolean clinicEmployee) {
	this.clinicEmployee = clinicEmployee;
	}

	@OneToOne
	public Person getEmployee() {
	return employee;
	}

	public void setEmployee(Person employee) {
	this.employee = employee;
	}

	public Boolean getRestrictAccess() {
	return restrictAccess;
	}

	public void setRestrictAccess(Boolean restrictAccess) {
	this.restrictAccess = restrictAccess;
	}

}
