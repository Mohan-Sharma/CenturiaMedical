package com.nzion.zkoss.composer.appointment;

import java.util.Collection;

import org.zkoss.zk.ui.Component;

import com.nzion.domain.CalendarResourceAssoc;
import com.nzion.domain.Schedule;


/**
 * @author Sandeep Prusty
 * May 25, 2010
 */
public interface AppointmentGridRenderer {
	
	void setAppointmentGrid(AppointmentGrid grid);
	
	void buildHeader();
	
	void addScheduleData(Schedule schedule);

	void addScheduleData(Collection<Schedule> schedules);
	
	void removeScheduleData(Component listitem);
	
	void setCalendarTemplateAssociations(Collection<CalendarResourceAssoc> assocs);
}
