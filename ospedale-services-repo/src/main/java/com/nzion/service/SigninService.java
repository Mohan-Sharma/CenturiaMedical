package com.nzion.service;

import com.nzion.domain.Location;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Schedule;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.emr.PatientVisit;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Sandeep Prusty
 * Jul 13, 2010
 *
 * Mohan Sharma -  schedule search criteria implementation
 */
public interface SigninService {
	
	List<Schedule> getSchedulesFor(Person provider, Date date, STATUS status, Location location);

    List<Schedule> getSchedulesForGivenCriteria(STATUS status, Person person, Location location, Date searchFromDate, Date searchToDate, Patient patient);
    
    List<Schedule> getSchedulesForGivenCriteria(STATUS status, Person person, Date searchFromDate, Date searchToDate, String selectedBookingStatus,
    		String civilId, String fileNo, String mobileNo);
	
	List<PatientVisit> getPatientVisitsFor(Schedule schedule);
	
	Schedule changeStatus(Schedule schedule, STATUS newStatus, PatientVisit visit);
	
	List<Person> getAllConsultablePersonsByLocation(Collection<Location> locations);
	
	Schedule addVisitToSchedule(PatientVisit theVisit, Schedule schedule);

    List<Person> getAllConsultablePersons(Collection<Location> locations);
}
