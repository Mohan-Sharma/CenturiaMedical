package com.nzion.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.nzion.domain.CalendarResourceAssoc;
import com.nzion.domain.CalendarSlot;
import com.nzion.domain.Location;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Schedule;
import com.nzion.domain.ScheduleBreak;
import com.nzion.domain.ScheduleWaitingList;
import com.nzion.domain.SoapNoteType;
import com.nzion.domain.base.Weekdays;
import com.nzion.domain.screen.ScheduleConfig;
import com.nzion.domain.util.SlotAvailability;
import com.nzion.service.impl.ScheduleScanner;
import com.nzion.view.MultiBookValueObject;
import com.nzion.view.ScheduleSearchValueObject;

/**
 * @author Sandeep Prusty
 * Apr 29, 2010
 */
public interface ScheduleService {
	
	List<CalendarResourceAssoc> getCalendarAssociations(Date selectedDate, Date uptoDate, Person person, Location location, boolean asc);
	
	List<CalendarResourceAssoc> getCalendarAssociations(Collection<Person> persons, Date on);
	
	List<CalendarResourceAssoc> getAllCalendarAssociationsFor(Person person);
	
	CalendarResourceAssoc checkCalendarCollision(CalendarResourceAssoc newAssociation);
	
	CalendarResourceAssoc saveCalendarAssociation(CalendarResourceAssoc assoc) throws Exception;
	
	List<ScheduleBreak> getScheduleBreaks(Person person, Date from, Date thru);
	
	List<Person> getSchedulablePersons();
	
	List<SoapNoteType> getAllSoapNoteTypes();

	List<Schedule> getDetailedSchedules(Person person, Location location, Date from, Date upto);
	
	List<Schedule> getDetailedSchedules(Collection<Person> providers, Date on);

	Schedule getDetailedSchedule(Long scheduleId);
	
	Schedule getSchedule(Long scheduleId);

	Schedule saveOrUpdate(Schedule schedule);
	
	ScheduleWaitingList addWaitingList(ScheduleWaitingList waitingList);
	
	ScheduleWaitingList sendToWaitingList(Schedule schedule);
	
	Schedule cancelSchedule(Long scheduleId);
	
	List<ScheduleWaitingList> searchWaitingList(ScheduleWaitingList waitingList);
	
	List<ScheduleWaitingList> getAllWaitListedSchedules();
	
	Long findWaitingListCount();
	
	ScheduleScanner scanSlots(MultiBookValueObject vo);
	
	List<Schedule> saveMultiBookRequest(ScheduleScanner scanner, MultiBookValueObject vo);

	Schedule forceInsert(CalendarSlot slot, Date date);
	
	Set<SlotAvailability> searchAvailableSchedules(ScheduleSearchValueObject scheduleSearchValueObject, Weekdays weekdays);
	
	List<Schedule> searchBookedSchedules(ScheduleSearchValueObject scheduleSearchValueObject);

	Schedule blockSlot(CalendarSlot slot, Date date, String comment);
	
	void blockSlotsIgnoringExistingSchedules(ScheduleScanner scanner, String comment);
	
	void blockSlotsCancelingExistingSchedules(ScheduleScanner scanner, String comment);
	
	void deleteSchedule(Long scheduleId);
	
	void deleteAllForceInserteds(Person person, Location location, Date date, Date endDate);
	
	List<Schedule> getSchedulesForPatient(Patient patient,boolean past,boolean future);
	
	List<Schedule> getSchedulesWaitingForSelf(Date selectedDate);

	ScheduleConfig getAppointmentDisplayScreen();

	Schedule createSchedule(Schedule schedule);
	
	Schedule updateSchedule(Schedule existingSchedule);

	Set<SlotAvailability> getTotalAvailableSlots(ScheduleSearchValueObject scheduleSearchValueObject, Weekdays weekdays);

	Set<SlotAvailability> getTotalSlots(ScheduleSearchValueObject scheduleSearchValueObject, Weekdays weekdays);

}