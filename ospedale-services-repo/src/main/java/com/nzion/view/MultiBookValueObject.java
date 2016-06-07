package com.nzion.view;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import com.nzion.domain.Schedule;
import com.nzion.domain.base.Weekdays;
import com.nzion.util.UtilDateTime;

/**
 * @author Sandeep Prusty
 * Oct 11, 2010
 */
/**
 * @author Sandeep Prusty
 * Oct 12, 2010
 */
public class MultiBookValueObject {

	private Schedule schedule;

	private Date startTime;

	private Date endTime;

	private Date thruDate;

	private Weekdays weekdays = Weekdays.allSelectedWeekdays();

	public MultiBookValueObject(Schedule schedule) {
	this.schedule = schedule;
	this.startTime = schedule.getStartTime();
	this.endTime = schedule.getEndTime();
	}

	public Date getStartTime() {
	return startTime;
	}

	public void setStartTime(Date startTime) {
	this.startTime = startTime;
	}

	public Date getEndTime() {
	return endTime;
	}

	public void setEndTime(Date endTime) {
	this.endTime = endTime;
	}

	public Schedule getSchedule() {
	return schedule;
	}

	public void setSchedule(Schedule schedule) {
	this.schedule = schedule;
	}

	public Date getThruDate() {
	return thruDate;
	}

	public void setThruDate(Date thruDate) {
	this.thruDate = thruDate;
	}

	public Weekdays getWeekdays() {
	return weekdays;
	}

	public void setWeekdays(Weekdays weekdays) {
	this.weekdays = weekdays;
	}

	public boolean satisfiedBy(Date givenDate) {
	return !schedule.getStartDate().after(givenDate) && !thruDate.before(givenDate) && weekdays.satisfiedBy(givenDate);
	}

	public Set<Date> getRequestedDates() {
	Date currentDate = getSchedule().getStartDate();
	Set<Date> requestedDates = new TreeSet<Date>();
	do {
		if (weekdays.satisfiedBy(currentDate)) requestedDates.add(currentDate);
		currentDate = UtilDateTime.addDaysToDate(currentDate, 1);
	} while (!thruDate.before(currentDate));
	return requestedDates;
	}
}