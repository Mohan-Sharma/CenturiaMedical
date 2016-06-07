package com.nzion.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;

import com.nzion.domain.base.IdGeneratingBaseEntity;

/**
 * @author Sandeep Prusty
 * Jun 4, 2010
 */

@Entity
@Table(name = "SCHEDULE_WAITING_LIST")
public class ScheduleWaitingList extends IdGeneratingBaseEntity {

	private Patient patient;

	private Person person;

	private String preferedContactNumber;

	private String comments;

	private Schedule assignedSchedule;
	
	@OneToOne(targetEntity = Schedule.class)
	@JoinColumn(name = "ASSIGNED_SCHEDULE_ID")
	public Schedule getAssignedSchedule() {
	return assignedSchedule;
	}

	public void setAssignedSchedule(Schedule assignedSchedule) {
	this.assignedSchedule = assignedSchedule;
	}

	@OneToOne(targetEntity = Patient.class)
	@JoinColumn(name = "PATIENT_ID")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@Column(name = "PREFERED_CONTACT_NUMBER")
	public String getPreferedContactNumber() {
	return preferedContactNumber;
	}

	@OneToOne(targetEntity = Provider.class)
	@JoinColumn(name = "PERSON_ID")
	public Person getPerson() {
	return person;
	}

	public void setPerson(Person person) {
	this.person = person;
	}

	public void setPreferedContactNumber(String preferedContactNumber) {
	this.preferedContactNumber = preferedContactNumber;
	}

	@Column(name = "COMMENTS")
	public String getComments() {
	return comments;
	}

	public void setComments(String comments) {
	this.comments = comments;
	}
	
	public void populatePatient(Patient patient){
	this.patient = patient;
	this.preferedContactNumber = patient.getContacts().getMobileNumber();
	}

	private static final long serialVersionUID = 1L;
}