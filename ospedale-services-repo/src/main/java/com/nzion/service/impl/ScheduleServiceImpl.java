package com.nzion.service.impl;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nzion.domain.CalendarResourceAssoc;
import com.nzion.domain.CalendarSlot;
import com.nzion.domain.Location;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Schedule;
import com.nzion.domain.ScheduleBreak;
import com.nzion.domain.ScheduleStatusConfig;
import com.nzion.domain.ScheduleWaitingList;
import com.nzion.domain.SoapNoteType;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.Schedule.ScheduleType;
import com.nzion.domain.base.Weekdays;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.screen.ScheduleConfig;
import com.nzion.domain.util.SlotAvailability;
import com.nzion.repository.ScheduleRepository;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.service.ScheduleService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;
import com.nzion.view.MultiBookValueObject;
import com.nzion.view.ScheduleSearchValueObject;

/**
 * @author Sandeep Prusty
 * Apr 29, 2010
 */

@Service("scheduleService")
public class ScheduleServiceImpl implements ScheduleService {
	
	private ScheduleRepository scheduleRepository;
	
	private CommonCrudRepository commonCrudRepository;
	
	private CommonCrudService commonCrudService;
	
	@Required
	@Resource
	public void setScheduleRepository(ScheduleRepository scheduleRepository) {
	this.scheduleRepository = scheduleRepository;
	}

	@Required
	@Resource
	public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
	this.commonCrudRepository = commonCrudRepository;
	}
	
	@Required
	@Resource
	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}
	
	public List<Person> getSchedulablePersons(){
	return commonCrudRepository.findByEquality(Person.class, new String[]{"schedulable"}, new Object[]{true});
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public ScheduleConfig getAppointmentDisplayScreen() {
	ScheduleConfig displayScreen = new ScheduleConfig();
	displayScreen.setStatusConfigs(commonCrudRepository.getAll(ScheduleStatusConfig.class));
	return displayScreen;
	}
	
	public List<SoapNoteType> getAllSoapNoteTypes() {
	List<SoapNoteType> soapNoteTypes = new ArrayList<SoapNoteType>();
	//List<SoapNoteType> allSoapNoteTypes = commonCrudRepository.getAll(SoapNoteType.class);
	List<SoapNoteType> allSoapNoteTypes =commonCrudService.findByEquality(com.nzion.domain.SoapNoteType.class,new String[]{"chargeType"},
			new Object[]{com.nzion.domain.SlotType.CHARGETYPE.OUTPATIENT});
	for(SoapNoteType noteType : allSoapNoteTypes)
		if(checkSoapNoteTypeSoapModuleMapping(noteType)) {
			soapNoteTypes.add(noteType);
			Collections.sort(soapNoteTypes);
		}
	return soapNoteTypes;
	}
	
	private boolean checkSoapNoteTypeSoapModuleMapping(SoapNoteType noteType){
	return commonCrudRepository.findByEquality(VisitTypeSoapModule.class, new String[]{"slotType"}, new Object[]{noteType}).size() > 0;
	}
	
	public CalendarResourceAssoc checkCalendarCollision(CalendarResourceAssoc newAssociation){
	// To check for other locations in the same date and time..
		List selectedDays = newAssociation.getWeek().getSelectedDays();
	List<CalendarResourceAssoc> assocs = scheduleRepository.getApplicableCalendarAssociations(newAssociation.getPerson(), newAssociation.getFromDate()
					, newAssociation.getThruDate(), newAssociation.getStartTime(), newAssociation.getEndTime(), selectedDays);
	return UtilValidator.isEmpty(assocs) ? null : assocs.get(0);
	
	// To check for same location in the given date range
	//assocs = scheduleRepository.getApplicableCalendarAssociations(newAssociation.getPerson(), newAssociation.getFromDate()
					//, newAssociation.getThruDate(), null, null, newAssociation.getLocation(), null, false);
	//return UtilValidator.isEmpty(assocs) ? null : assocs.get(0);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public CalendarResourceAssoc saveCalendarAssociation(CalendarResourceAssoc association)throws Exception{
	//CalendarResourceAssoc lastAssociation = scheduleRepository.getLatestCalendarAssociation(association.getPerson(), association.getLocation(), association.getFromDate());
	//if(lastAssociation != null && !lastAssociation.isAfter(association)){
		//lastAssociation.setThruDate(UtilDateTime.getDayEnd(UtilDateTime.addDaysToDate(association.getFromDate(), -1)));
		//scheduleRepository.save(lastAssociation);
	//}
	association.setFromDate(UtilDateTime.getDayStart(association.getFromDate()));
	scheduleRepository.save(association);
	return association;
	}
	
	public List<ScheduleBreak> getScheduleBreaks(Person person, Date from, Date thru){
	return scheduleRepository.getScheduleBreaks(person, from, thru);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Schedule updateSchedule(Schedule existingSchedule) {
	if(existingSchedule.getPatient() != null && existingSchedule.getStatus() == null)
		existingSchedule.setStatus(STATUS.SCHEDULED);
	scheduleRepository.save(existingSchedule);
	return existingSchedule;
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Schedule createSchedule(Schedule schedule) {
	schedule.setStatus(STATUS.SCHEDULED);
	schedule.setScheduleType(ScheduleType.NORMAL);
	scheduleRepository.save(schedule);
	if(schedule.getWaitingList() != null){
		ScheduleWaitingList waitingList = schedule.getWaitingList();
		waitingList.setAssignedSchedule(schedule);
		scheduleRepository.save(waitingList);
	}
	return schedule;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Schedule saveOrUpdate(Schedule schedule){
	if(schedule.getSequenceNum()== null)
		schedule.setSequenceNum(getNextSequenceNumberFor(schedule));
	scheduleRepository.save(schedule);
	return schedule;
	}
	
	public List<CalendarResourceAssoc> getCalendarAssociations(Collection<Person> persons, Date on) {
	List<CalendarResourceAssoc> allAssociations = new ArrayList<CalendarResourceAssoc>();
	for(Person person : persons)
		allAssociations.addAll(getCalendarAssociations(on, on, person, null, true));
	return allAssociations; 
	}

	public List<Schedule> getDetailedSchedules(Person person, Location location, Date from, Date upto){
	TimeZone timeZone = TimeZone.getDefault();
	if(timeZone == null)
		timeZone = TimeZone.getDefault();
	Date convertedFromDate = UtilDateTime.getDayStart(from, TimeZone.getDefault(), Locale.getDefault());
	Date convertedThruDate = UtilDateTime.getDayEnd(upto, TimeZone.getDefault(), Locale.getDefault());
	return scheduleRepository.getSchedules(person, location, convertedFromDate, convertedThruDate);
	}

	public List<Schedule> getDetailedSchedules(Collection<Person> persons, Date on) {
	List<Schedule> allScheduleDatas = new ArrayList<Schedule>();
	for(Person person : persons)
		allScheduleDatas.addAll(getDetailedSchedules(person, null, on, on));
	return allScheduleDatas; 
	}

	public Schedule getDetailedSchedule(Long scheduleId) {
	Schedule schedule = scheduleRepository.getSchedule(scheduleId);
	return schedule;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Schedule cancelSchedule(Long scheduleId) {
	Schedule existingSchedule = scheduleRepository.findByPrimaryKey(Schedule.class, scheduleId);
	existingSchedule.setStatus(STATUS.CANCELLED);
	scheduleRepository.save(existingSchedule);
	return existingSchedule;
	}

	public List<CalendarResourceAssoc> getCalendarAssociations(Date fromDate, Date thruDate, Person person, Location location, boolean asc) {
	TimeZone timeZone = TimeZone.getDefault();
	Date convertedFromDate = UtilDateTime.getDayStart(fromDate, timeZone, Locale.getDefault());
	Date convertedThruDate = UtilDateTime.getDayEnd(thruDate, timeZone, Locale.getDefault());
	return  scheduleRepository.getCalendarAssociations(convertedFromDate, convertedThruDate, person, location, true);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public ScheduleWaitingList addWaitingList(ScheduleWaitingList waitingList) {
	scheduleRepository.save(waitingList);
	return waitingList;
	}
		
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public ScheduleWaitingList sendToWaitingList(Schedule schedule) {
	deleteSchedule(schedule.getId());
	ScheduleWaitingList waitingList = commonCrudRepository.findUniqueByEquality(ScheduleWaitingList.class, new String[]{"assignedSchedule"}, new Object[]{schedule});
	if(waitingList == null)
		waitingList = new ScheduleWaitingList();
	waitingList.setAssignedSchedule(null);
	waitingList.populatePatient(schedule.getPatient());
	waitingList.setComments(schedule.getComments());
	waitingList.setPreferedContactNumber(schedule.getPatientContactNumber());
	waitingList.setPerson(schedule.getPerson());
	scheduleRepository.save(waitingList);
	return waitingList;
	}

	public List<ScheduleWaitingList> searchWaitingList(ScheduleWaitingList waitingList){
	return scheduleRepository.simulateExampleSearch(new String[]{"patient", "person", "preferedContactNumber", "comments"}, waitingList);
	}

	public List<ScheduleWaitingList> getAllWaitListedSchedules() {
	return scheduleRepository.getAllWaitListedSchedules();
	}
	
	public List<CalendarResourceAssoc> getAllCalendarAssociationsFor(Person person){
	return scheduleRepository.getAllCalendarAssociationsFor(person);
	}
	
	public Long findWaitingListCount(){
	return scheduleRepository.findWaitingListCount();
	}

	public ScheduleScanner scanSlots(MultiBookValueObject mvo) {
	Schedule schedule = mvo.getSchedule();
	ScheduleScanner scanner = new ScheduleScanner(schedule.getStartDate(), mvo.getThruDate(), schedule.getStartTime(), schedule.getEndTime(), mvo.getWeekdays());
	List<CalendarResourceAssoc>	assocs = getCalendarAssociations(schedule.getStartDate(), mvo.getThruDate(), schedule.getPerson(), schedule.getLocation(), true);
	List<ScheduleBreak> breaks = getScheduleBreaks(schedule.getPerson(), schedule.getStartDate(), mvo.getThruDate());
	scanner.extractAllSlots(assocs, breaks);
	List<Schedule> schedules = scheduleRepository.getSchedules(schedule.getPerson(), schedule.getLocation(), mvo.getSchedule().getStartDate(), mvo.getThruDate(), mvo.getStartTime(), mvo.getEndTime());
	scanner.setBookedSchedules(schedules);
	scanner.subtract();
	return scanner;
	}
	
	//	This method will skip the slots where scheduling is not possible. The other slots will be booked
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public List<Schedule> saveMultiBookRequest(ScheduleScanner scanner, MultiBookValueObject vo){
	Schedule refrenceSchedule = vo.getSchedule();
	Set<SlotAvailability> availableSlots = scanner.getUnifiedAvailableSlots();
	List<Schedule> bookedSchedules = new ArrayList<Schedule>();
	for(SlotAvailability svo : availableSlots){
		Schedule schedule = refrenceSchedule.createCopy();
		schedule.setStartDate(svo.getOn());
		schedule.setSequenceNum(svo.getSlot().getSequenceNum());
		schedule.setScheduleType(ScheduleType.NORMAL);
		saveOrUpdate(schedule);
		bookedSchedules.add(schedule);
	}
	return bookedSchedules;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Schedule forceInsert(CalendarSlot slot, Date date){
	Schedule newSchedule = new Schedule(slot);
	newSchedule.setStartDate(date);
	newSchedule.setPerson(slot.getAssociation().getPerson());
	newSchedule.setLocation(slot.getAssociation().getLocation());
	newSchedule.setSequenceNum(null);
	newSchedule.setScheduleType(ScheduleType.FORCEINSERTED);
	saveOrUpdate(newSchedule);
	return newSchedule;
	}
	
	private Integer getNextSequenceNumberFor(Schedule schedule){
	Integer next = scheduleRepository.getMaxSequenceNumberUsed(schedule);
	if(next == null || next < 1000)
		next = 1000;
	return ++next;
	}

	public Set<SlotAvailability> searchAvailableSchedules(ScheduleSearchValueObject searchVo, Weekdays weekdays) {
	ScheduleScanner scanner = new ScheduleScanner(searchVo.getFromDate(), searchVo.getThruDate(), searchVo.getFromTime(), searchVo.getThruTime(), weekdays);
	List<ScheduleBreak> breaks = getScheduleBreaks(searchVo.getPerson(), searchVo.getFromDate(), searchVo.getThruDate());
	scanner.extractAllSlots(getCalendarAssociations(searchVo.getFromDate(), searchVo.getThruDate(), searchVo.getPerson(), searchVo.getLocation(), true), breaks);
	scanner.setBookedSchedules(scheduleRepository.searchScheduleFor(searchVo));
	scanner.subtract();
	return scanner.getUnifiedAvailableSlots(); 
	}
	
	public List<Schedule> searchBookedSchedules(ScheduleSearchValueObject scheduleSearchValueObject){
	return scheduleRepository.searchBookedSchedules(scheduleSearchValueObject);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Schedule blockSlot(CalendarSlot slot, Date date, String comment){
	Schedule blockedSlot = slot.buildSchedule(date);
	blockedSlot.setScheduleType(ScheduleType.BLOCKED);
	blockedSlot.setComments(comment);
	scheduleRepository.save(blockedSlot);
	return blockedSlot;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	private Schedule blockSlot(SlotAvailability vo, String comment){
	Schedule blockedSlot = vo.getSlot().buildSchedule(vo.getOn());
	blockedSlot.setScheduleType(ScheduleType.BLOCKED);
	blockedSlot.setComments(comment);
	scheduleRepository.save(blockedSlot);
	return blockedSlot;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void blockSlotsIgnoringExistingSchedules(ScheduleScanner scanner, String comment){
	Set<SlotAvailability> toBeBlockds = scanner.getAvailableSlots();
	for(SlotAvailability vo : toBeBlockds)
		blockSlot(vo, comment);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void blockSlotsCancelingExistingSchedules(ScheduleScanner scanner, String comment){
	Set<SlotAvailability> toBeBlockds = scanner.getAllSlots();
	for(SlotAvailability vo : toBeBlockds)
		blockSlot(vo, comment);
	Collection<Schedule> toBeCancelds = scanner.getBookedSchedules();
	if(UtilValidator.isEmpty(toBeCancelds))
		return;
	for(Schedule schedule : toBeCancelds)
		cancelSchedule(schedule.getId());
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void deleteSchedule(Long scheduleId){
	scheduleRepository.remove(scheduleId, Schedule.class);
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void deleteAllForceInserteds(Person person, Location location, Date date, Date endDate) {
	scheduleRepository.deleteAllForceInserteds(person, location, UtilDateTime.getDayStart(date), UtilDateTime.getDayEnd(endDate));
	}

	@Override
	public Schedule getSchedule(Long scheduleId) {
	return scheduleRepository.getSchedule(scheduleId);
	}
	
	public List<Schedule> getSchedulesForPatient(Patient patient,boolean past,boolean future){
	return scheduleRepository.getSchedulesForPatient(patient, past, future);
	}
	
	public List<Schedule> getSchedulesWaitingForSelf(Date selectedDate){
	return scheduleRepository.getSchedulesWaitingForSelf(selectedDate, Infrastructure.getLoggedInPerson());
	}

	public Set<SlotAvailability> getTotalAvailableSlots(ScheduleSearchValueObject searchVo, Weekdays weekdays) {
		ScheduleScanner scanner = new ScheduleScanner(searchVo.getFromDate(), searchVo.getThruDate(), searchVo.getFromTime(), searchVo.getThruTime(), weekdays);
		List<ScheduleBreak> breaks = getScheduleBreaks(searchVo.getPerson(), searchVo.getFromDate(), searchVo.getThruDate());

		List<CalendarResourceAssoc> h = null;
			TimeZone timeZone = TimeZone.getDefault();
			Date convertedFromDate = UtilDateTime.getDayStart(searchVo.getFromDate(), timeZone, Locale.getDefault());
			Date convertedThruDate = UtilDateTime.getDayEnd(searchVo.getThruDate(), timeZone, Locale.getDefault());
			h = scheduleRepository.getCalendarAssociations(convertedFromDate, convertedThruDate, searchVo.getPerson(),searchVo.getLocation(), true);

		String day = new SimpleDateFormat("EEE").format(searchVo.getFromDate());
		Iterator iterator = h.iterator();
		while (iterator.hasNext()){
			CalendarResourceAssoc calendarResourceAssoc = (CalendarResourceAssoc)iterator.next();
			if (calendarResourceAssoc.getWeek() != null){
				List<String> listOfDays = calendarResourceAssoc.getWeek().getSelectedDays();
				if (!listOfDays.contains(day)){
					iterator.remove();
				}
			}

		}

		scanner.extractAllSlots(h, breaks);
		scanner.setBookedSchedules(scheduleRepository.searchScheduleFor(searchVo));
		scanner.subtract();
		return scanner.getUnifiedAvailableSlots();
	}

	public Set<SlotAvailability> getTotalSlots(ScheduleSearchValueObject searchVo, Weekdays weekdays) {
		ScheduleScanner scanner = new ScheduleScanner(searchVo.getFromDate(), searchVo.getThruDate(), searchVo.getFromTime(), searchVo.getThruTime(), weekdays);
		List<ScheduleBreak> breaks = getScheduleBreaks(searchVo.getPerson(), searchVo.getFromDate(), searchVo.getThruDate());

		List<CalendarResourceAssoc> h = null;
		TimeZone timeZone = TimeZone.getDefault();
		Date convertedFromDate = UtilDateTime.getDayStart(searchVo.getFromDate(), timeZone, Locale.getDefault());
		Date convertedThruDate = UtilDateTime.getDayEnd(searchVo.getThruDate(), timeZone, Locale.getDefault());
		h = scheduleRepository.getCalendarAssociations(convertedFromDate, convertedThruDate, searchVo.getPerson(),searchVo.getLocation(), true);

		String day = new SimpleDateFormat("EEE").format(searchVo.getFromDate());
		Iterator iterator = h.iterator();
		while (iterator.hasNext()){
			CalendarResourceAssoc calendarResourceAssoc = (CalendarResourceAssoc)iterator.next();
			if (calendarResourceAssoc.getWeek() != null){
				List<String> listOfDays = calendarResourceAssoc.getWeek().getSelectedDays();
				if (!listOfDays.contains(day)){
					iterator.remove();
				}
			}

		}

		scanner.extractAllSlots(h, breaks);
		/*scanner.setBookedSchedules(scheduleRepository.searchScheduleFor(searchVo));*/
		scanner.subtract();
		return scanner.getUnifiedAvailableSlots();
	}
}