package com.nzion.view;

import java.util.Date;
import java.util.Set;

import com.nzion.domain.Location;
import com.nzion.domain.Person;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.SlotType;
import com.nzion.domain.Speciality;
import com.nzion.util.UtilDateTime;

/**
 * @author Sandeep Prusty
 * Oct 18, 2010
 */
public class ScheduleSearchValueObject {

	private Location location;

	private Speciality speciality;

	private Person person;

	private Date fromDate = new Date();

	private Date thruDate = new Date();

	private Date fromTime;

	private Date thruTime;

	private SlotType slotType;
	
	private Set<Person> persons;
	
	private STATUS status;
	
	public ScheduleSearchValueObject() {}
	
	public ScheduleSearchValueObject(boolean initializeTime) {
	if(initializeTime){
		fromTime = UtilDateTime.timeOnly();
		thruTime = UtilDateTime.timeOnly();
	}
	}

	public STATUS getStatus() {
	return status;
	}

	public void setStatus(STATUS status) {
	this.status = status;
	}

	public Set<Person> getPersons() {
	return persons;
	}

	public void setPersons(Set<Person> persons) {
	this.persons = persons;
	}

	public Location getLocation() {
	return location;
	}

	public void setLocation(Location location) {
	this.location = location;
	}

	public Speciality getSpeciality() {
	return speciality;
	}

	public void setSpeciality(Speciality speciality) {
	this.speciality = speciality;
	}

	public Person getPerson() {
	return person;
	}

	public void setPerson(Person person) {
	this.person = person;
	}

	public Date getFromDate() {
	return fromDate;
	}

	public void setFromDate(Date fromDate) {
	if(fromDate != null)
		this.fromDate = UtilDateTime.getDayStart(fromDate);
	}

	public Date getThruDate() {
	return thruDate;
	}

	public void setThruDate(Date thruDate) {
	if(thruDate != null)
	this.thruDate = UtilDateTime.getDayEnd(thruDate);
	}

	public Date getFromTime() {
	return fromTime;
	}

	public void setFromTime(Date fromTime) {
	this.fromTime = UtilDateTime.timeOnly(fromTime);
	}

	public Date getThruTime() {
	return thruTime;
	}

	public void setThruTime(Date thruTime) {
	this.thruTime = UtilDateTime.timeOnly(thruTime);
	}

	public SlotType getSlotType() {
	return slotType;
	}

	public void setSlotType(SlotType slotType) {
	this.slotType = slotType;
	}
}