package com.nzion.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.enums.RecurrenceMode;

/**
 * @author Sandeep Prusty
 * Mar 29, 2011
 */

@Entity
public class ScheduleBreak extends IdGeneratingBaseEntity {

	private String name;
	
	private Person person;

	private CalendarRecurrence recurrence;

    private String color = "#FFFFFF".intern();
	
	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

    private boolean showInCalender = Boolean.TRUE;

	@ManyToOne
	@JoinColumn(name="PERSON_ID")
	public Person getPerson() {
	return person;
	}

	public void setPerson(Person person) {
	this.person = person;
	}

	@ManyToOne
	@JoinColumn(name="CALENDAR_RECURRENCE_ID")
	@Cascade(CascadeType.ALL)
	public CalendarRecurrence getRecurrence() {
	if(recurrence == null)
		recurrence = new CalendarRecurrence(RecurrenceMode.DAILLY);
	return recurrence;
	}

	public void setRecurrence(CalendarRecurrence recurrence) {
	this.recurrence = recurrence;
	}
	
	public boolean check(Date date, Date startTime, Date endTime){
	return recurrence.check(date, startTime, endTime);
	}

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    private static final long serialVersionUID = 1L;

    public boolean isShowInCalender() {
        return showInCalender;
    }

    public void setShowInCalender(boolean showInCalender) {
        this.showInCalender = showInCalender;
    }
}