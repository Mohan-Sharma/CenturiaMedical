package com.nzion.service.impl;

import com.nzion.domain.*;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.emr.PatientVisit;
import com.nzion.repository.ScheduleRepository;
import com.nzion.service.SigninService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Sandeep Prusty
 * Jul 13, 2010
 *
 * Mohan Sharma -  schedule search criteria implementation
 */

@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
public class SigninServiceImpl implements SigninService {
	
	private ScheduleRepository scheduleRepository;

	public void setScheduleRepository(ScheduleRepository scheduleRepository) {
	this.scheduleRepository = scheduleRepository;
	}

	public List<Schedule> getSchedulesFor(Person person, Date date, STATUS status, Location location) {
	return scheduleRepository.getSchedulesFor(status, person, date,location);
	}

    public List<Schedule> getSchedulesForGivenCriteria(STATUS status, Person person, Location location, Date searchFromDate, Date searchToDate, Patient patient) {
        return scheduleRepository.getSchedulesForGivenCriteria(status, person, location, searchFromDate, searchToDate, patient);
    }
    
    public List<Schedule> getSchedulesForGivenCriteria(STATUS status, Person person, Date searchFromDate, Date searchToDate, String selectedBookingStatus,
    		String civilId, String fileNo, String mobileNo) {
        return scheduleRepository.getSchedulesForGivenCriteria(status, person, searchFromDate, searchToDate, selectedBookingStatus, civilId,
        		fileNo, mobileNo);
    }

	public List<PatientVisit> getPatientVisitsFor(Schedule schedule){
	return scheduleRepository.getPatientVisitsFor(schedule);
	}
	
	public Schedule changeStatus(Schedule schedule, STATUS newStatus, PatientVisit visit){
	scheduleRepository.refresh(schedule);
	schedule.setStatus(newStatus);
	if(visit != null)
		schedule.addPatientVisit(visit);
	if(STATUS.CHECKEDIN.equals(newStatus))
		schedule.setSignedInTime(new Date());
	if(STATUS.SOAPSIGNEDOUT.equals(newStatus))
		schedule.setSignedOutTime(new Date());
	scheduleRepository.merge(schedule);
	return schedule;
	}
	
	public List<Person> getAllConsultablePersonsByLocation(Collection<Location> locations){
	return scheduleRepository.getAllConsultablePersonsByLocation(locations);
	}

    public List<Person> getAllConsultablePersons(Collection<Location> locations){
        return scheduleRepository.getAllConsultablePersons(locations);
    }

	@Override
	public Schedule addVisitToSchedule(PatientVisit theVisit, Schedule schedule) {
	if((STATUS.SCHEDULED.equals(schedule.getStatus()) || STATUS.CHECKEDIN.equals(schedule.getStatus())  )&& (theVisit.getMetWith() instanceof Provider)){
		schedule.setStatus(STATUS.EXAMINING);
	}else
		schedule.setStatus(STATUS.CHECKEDIN);
	schedule.addPatientVisit(theVisit);
	scheduleRepository.save(schedule);
	return schedule;
	}
}