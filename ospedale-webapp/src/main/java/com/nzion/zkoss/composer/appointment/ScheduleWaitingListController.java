package com.nzion.zkoss.composer.appointment;

import java.util.List;

import com.nzion.zkoss.composer.OspedaleAutowirableComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;

import com.nzion.domain.ScheduleWaitingList;
import com.nzion.service.ScheduleService;
import com.nzion.service.common.CommonCrudService;

/**
 * @author Sandeep Prusty
 * Jun 14, 2010
 */
public class ScheduleWaitingListController extends OspedaleAutowirableComposer {

	private ScheduleWaitingList waitingList = new ScheduleWaitingList();
	
	private List<ScheduleWaitingList> waitListeds;
	
	private CommonCrudService commonCrudService;
	
	private ScheduleService scheduleService;
	
	public void setScheduleService(ScheduleService scheduleService) {
	this.scheduleService = scheduleService;
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {
	waitListeds = scheduleService.getAllWaitListedSchedules(); 
	super.doAfterCompose(component);
	}
	
	public void save(){
	if(waitingList.getPatient() == null)
		throw new WrongValueException(root.getFellow("patientAccountNumber"),"Please select a patient.");
	if(waitingList.getPerson() == null)
		throw new WrongValueException(root.getFellow("providerAccountNumber"),"Please select a provider.");
	boolean isNew = waitingList.getId() == null;
	scheduleService.addWaitingList(waitingList);
	if(isNew){
		waitListeds.add(0, waitingList);
		return;
	}
	waitListeds.set(waitListeds.indexOf(waitingList), waitingList);
	this.waitingList = new ScheduleWaitingList();
	}

	public List<ScheduleWaitingList> getWaitListeds() {
	return waitListeds;
	}

	public void setWaitListeds(List<ScheduleWaitingList> waitListeds) {
	this.waitListeds = waitListeds;
	}

	public ScheduleWaitingList getWaitingList() {
	return waitingList;
	}

	public void setWaitingList(ScheduleWaitingList waitingList) {
	this.waitingList = waitingList;
	}
	
	public void startNew(){
	setWaitingList(new ScheduleWaitingList());
	}
	
	public void search(){
	waitListeds = scheduleService.searchWaitingList(waitingList);
	}
	
	public void delete(ScheduleWaitingList waitingList){
	ScheduleWaitingList deleted = commonCrudService.delete(waitingList);
	deleted.setId(null);
	waitListeds.remove(deleted);
	}
	
	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}
	private static final long serialVersionUID = 1L;
}
