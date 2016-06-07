package com.nzion.zkoss.composer.appointment;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventQueues;

import com.nzion.domain.CalendarResourceAssoc;
import com.nzion.domain.Person;
import com.nzion.domain.Schedule;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.Schedule.ScheduleType;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Jun 25, 2010
 */
public class ScheduleViewHelper {
	
	public static final String SCHEDULE_EVENTQUEUE = "scheduleEventqueueFor";

	public static final String SCHEDULE_BOOKED_EVENT = "onScheduleBook";
	
	public static final String SCHEDULED_CONTEXT_MENU_ID = "normalScheduleContextMenu";

	public static final String NOT_SCHEDULED_CONTEXT_MENU_ID = "notScheduledContextMenu";
	
	public static final String TODAY_HEDAER_STYLE = "background:#FFFFCC; border: 0px;color: #333333;";
	
	public static final Map<ScheduleType, String> SCHEDULE_TYPE_TO_CONTEXT_MENU = new HashMap<ScheduleType, String>();
	
	static {
		SCHEDULE_TYPE_TO_CONTEXT_MENU.put(null, "notScheduledContextMenu");
		SCHEDULE_TYPE_TO_CONTEXT_MENU.put(ScheduleType.NORMAL, "normalScheduleContextMenu");
		SCHEDULE_TYPE_TO_CONTEXT_MENU.put(ScheduleType.BLOCKED, "blockedContextMenu");
		SCHEDULE_TYPE_TO_CONTEXT_MENU.put(ScheduleType.FORCEINSERTED, "forceInsertedNotScheduledContextMenu");
		SCHEDULE_TYPE_TO_CONTEXT_MENU.put(ScheduleType.CREATEDFROM_SIGNIN, "normalScheduleContextMenu");
	}
	
	public static String getMenuId(Schedule schedule){
	if(ScheduleType.FORCEINSERTED.equals(schedule.getScheduleType()) && schedule.getPatient() != null)
		return "forceInsertedScheduledContextMenu";
	return SCHEDULE_TYPE_TO_CONTEXT_MENU.get(schedule.getScheduleType());
	}
	
	public static String getScheduleEventQueueName(Schedule schedule){
	return getScheduleEventQueueName(schedule.getPerson());
	}

	public static String getScheduleEventQueueName(CalendarResourceAssoc association){
	return getScheduleEventQueueName(association.getPerson());
	}
	
	public static String getScheduleEventQueueName(Person person){
	return "Person" + person.getId();
	}
	
	public static void unsubscribeEvents(){
	Session userSession = Sessions.getCurrent();
	Map<String, Object> attributes = userSession.getAttributes();
	Set<String> eventQNamesForScheduling = new HashSet<String>();
	Desktop desktop = Executions.getCurrent().getDesktop();
	for(Map.Entry<String, Object> entry : attributes.entrySet()){
		if(!entry.getKey().startsWith(AppointmentGrid.SUBSCRIBED_EVENT_QUEUE_NAMES_IN_SESSION))
			continue;
		String gridId = entry.getKey().split("-")[1]; 
		Object grid = desktop.getComponentByUuidIfAny(gridId);
		if(grid != null)
			continue;
		eventQNamesForScheduling.addAll((Collection<String>)entry.getValue());
		userSession.removeAttribute(entry.getKey());
	}
	unsubscribeEvents(eventQNamesForScheduling);
	}
	
	public static void unsubscribeEvents(Collection<String> eventQNamesForScheduling){
	if(UtilValidator.isEmpty(eventQNamesForScheduling))
		return;
	for(String subscribedEventQueueName : eventQNamesForScheduling)
		EventQueues.remove(subscribedEventQueueName, EventQueues.APPLICATION);
	}
	
	public static String getSignInMenuId(Schedule schedule){
	if(STATUS.CHECKEDIN.equals(schedule.getStatus()) || STATUS.PROCEDUREPENDING.equals(schedule.getStatus()) || STATUS.READY_FOR_BILLING.equals(schedule.getStatus()))
		return schedule.getStatus().toString();
	return null;
	}
}