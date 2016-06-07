package com.nzion.zkoss.composer.appointment;

import java.util.Date;
import java.util.Iterator;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;

import com.nzion.domain.CalendarSlot;
import com.nzion.domain.Roles;
import com.nzion.domain.Schedule;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.ScheduleBreak;
import com.nzion.util.Constants;
import com.nzion.util.ViewUtil;

/**
 * @author Sandeep Prusty
 * Jan 25, 2011
 */
public class PortletAppointmentGridRenderer extends DayAppointmentGridRenderer {

	@Override
	@SuppressWarnings("unchecked")
	protected void addScheduleData(Schedule schedule, Listitem row){
	String color = grid.screenConfig.getColor(schedule.getStatus());
	row.setStyle("background-color:" + color);
	Iterator<Component> cellIterator = row.getChildren().iterator();
	cellIterator.next();// ignore the time cell
	AppointmentListcell nameCell = (AppointmentListcell)cellIterator.next();
	nameCell.book(schedule, ViewUtil.getFormattedName(schedule.getPatient()),"");
	StringBuilder status = new StringBuilder(schedule.getStatus() == null ? "" : schedule.getStatus().toString());
	if(schedule.getLastPatientVisit() != null)
		status.append(schedule.getLastPatientVisit());
	AppointmentListcell statusCell =(AppointmentListcell) cellIterator.next();
	statusCell.book(schedule, status.toString(),"");
	if(Roles.hasAnyRole(Roles.PROVIDER, Roles.MEDICAL_ASSISTANT)){
		AppointmentListcell locationCell = (AppointmentListcell)cellIterator.next();
		locationCell.book(schedule, schedule.getLocation().getName(),"");
	}
	}

	@Override
	public void buildHeader() {
	Listhead head = new Listhead();
	head.setParent(grid);
	Listheader timeHeader = new Listheader("Time");
	timeHeader.setWidth("80px");
	timeHeader.setParent(head);
	Listheader nameHeader = new Listheader("Patient Name");
	nameHeader.setParent(head);
	Listheader statusHeader = new Listheader("Status");
	statusHeader.setParent(head);
	if(Roles.hasAnyRole(Roles.PROVIDER, Roles.MEDICAL_ASSISTANT)){
		Listheader locationHeader = new Listheader("Location");
		locationHeader.setParent(head);
	}
	}

	private transient Date temporaryPreviousDate; 
	
	@Override
	protected Listitem addSlot(CalendarSlot slot){
	Key key = new Key(slot, grid.getDate());
	Listitem row = new Listitem();
	row.setParent(grid);
	row.setAttribute("slot", slot);
	row.setStyle("background-color:" + Constants.WHITE);
	AppointmentListcell timeCell = new AppointmentListcell();
	if(temporaryPreviousDate == null || !temporaryPreviousDate.equals(slot.getStartTime()))
		timeCell.makeTimeCell(slot.getStartTime());
	timeCell.setParent(row);
	temporaryPreviousDate = slot.getStartTime();
	ScheduleBreak brek = grid.checkBreak(slot, grid.getDate());
	AppointmentListcell nameCell = brek != null ? 
					new AppointmentListcell(brek.getName(), grid) : new AppointmentListcell(grid.getDate(), slot, grid);
        if(brek != null)
            nameCell.setStyle("background-color:" + brek.getColor() + ";");
	nameCell.setParent(row);
	AppointmentListcell statusCell = new AppointmentListcell(grid.getDate(), slot, grid);
	statusCell.setParent(row);
	if(Roles.hasAnyRole(Roles.PROVIDER, Roles.MEDICAL_ASSISTANT)){
		String location = slot.getAssociation().getLocation() == null ? null : slot.getAssociation().getLocation().getName(); 
		AppointmentListcell locationCell = new AppointmentListcell(grid.getDate(), slot, grid, location);
		locationCell.setParent(row);
	}
	slotCache.put(key, row); 
	return row;
	}
	
	@Override
	public void setAppointmentGrid(AppointmentGrid grid) {
	this.grid = grid;
	}
}