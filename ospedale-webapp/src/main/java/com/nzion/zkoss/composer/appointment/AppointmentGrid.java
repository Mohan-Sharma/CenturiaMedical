package com.nzion.zkoss.composer.appointment;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

import com.nzion.domain.CalendarResourceAssoc;
import com.nzion.domain.CalendarSlot;
import com.nzion.domain.Schedule;
import com.nzion.domain.ScheduleBreak;
import com.nzion.domain.screen.ScheduleConfig;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilMisc;
import com.nzion.util.UtilValidator;
/**
 * @author Sandeep Prusty
 * May 16, 2010
 */

@SuppressWarnings("unchecked")
public class AppointmentGrid extends Listbox {

    private final Date date;

    ScheduleConfig screenConfig;

    private final Component controllngParent;

    private AppointmentGridRenderer appointmentGridRenderer;

    private Window patientScehduleModalWindow;

    private AppointmentListcell cellInUse;

    private List<ScheduleBreak> breaks;

    final Component patientDetailPopup;

    public AppointmentGrid(ScheduleConfig screenConfig, Date date, Component controllngParent, AppointmentGridRenderer gridRenderer) {
        this.screenConfig = screenConfig;
        this.date = date;
        this.controllngParent = controllngParent;
        gridRenderer.setAppointmentGrid(this);
        this.appointmentGridRenderer = gridRenderer;
        appointmentGridRenderer.buildHeader();
        patientDetailPopup = controllngParent.getFellow("patientDetail");
    }

    public Date getDate() {
        return date;
    }

    public void setAppointmentGridRenderer(AppointmentGridRenderer appointmentGridRenderer) {
        this.appointmentGridRenderer = appointmentGridRenderer;
    }

    public void setCalendarTemplateAssociations(Collection<CalendarResourceAssoc> assocs, List<ScheduleBreak> breaks) {
        this.breaks = breaks;
        appointmentGridRenderer.setCalendarTemplateAssociations(assocs);
    }

    public ScheduleBreak checkBreak(CalendarSlot slot, Date date){
        if(UtilValidator.isEmpty(breaks))
            return null;
        for(ScheduleBreak brek : breaks)
            if(brek.check(date, slot.getStartTime(), slot.getEndTime()))
                return brek;
        return null;
    }

    void reArrangeRow(Listitem row){
        List<Listitem> allRows = getItems();
        int oldIndex = allRows.indexOf(row);
        CalendarSlot givenSlot = (CalendarSlot)row.getAttribute("slot");
        int newIndex;
        for(newIndex = oldIndex - 1; newIndex > 0 && givenSlot.compareTo((CalendarSlot)allRows.get(newIndex).getAttribute("slot")) < 0; --newIndex);
        ++newIndex;
        if(newIndex == (oldIndex - 1))
            return;
        allRows.remove(row);
        allRows.add(newIndex, row);
        if(newIndex == 0)
            return;
        if(givenSlot.compareStartTime(((CalendarSlot)allRows.get(newIndex).getAttribute("slot"))) == 0)
            row.getFirstChild().getChildren().clear();
    }

    public void populateSchedules(Collection<Schedule> schedules){
        appointmentGridRenderer.addScheduleData(schedules);
    }

    public void scheduleBooked(Schedule schedule){
        patientScehduleModalWindow = null;
        cellInUse = null;
        appointmentGridRenderer.addScheduleData(schedule);
    }

    public void forceInsert(Schedule schedule){
        scheduleBooked(schedule);
        appointmentGridRenderer.addScheduleData(schedule);
    }

    public void removeScheduleData(Component component){
        appointmentGridRenderer.removeScheduleData(component);
    }

    final EventListener PATIENT_SCHEDULE_OPENER = new EventListener() {
        public void onEvent(Event event) throws Exception {
            Component parent = AppointmentGrid.this.controllngParent;
            AppointmentListcell cell = (AppointmentListcell) event.getTarget();
            Map<String, Object> args = UtilMisc.toMap("controller", parent.getAttribute("controller"), "schedule", cell.getSchedule());
            patientScehduleModalWindow = (Window)Executions.createComponents("/appointment/patient-schedule.zul", parent, args);
            cellInUse = cell;
        }
    };

    final EventListener PATIENT_SELECT_LISTENER = new EventListener() {
        public void onEvent(Event event) throws Exception {
            ScheduleController controller = (ScheduleController) AppointmentGrid.this.controllngParent.getAttribute("controller");
            controller.setSelectedComponent((AppointmentListcell) event.getTarget());
        }
    };

    final EventListener SCHEDULE_BOOKED_LISTENER = new EventListener() {
        public void onEvent(Event event) throws Exception {
            Schedule schedule = (Schedule)event.getData();
            appointmentGridRenderer.addScheduleData(schedule);
            if(cellInUse != null && cellInUse.isForSameSlot(schedule)){
                patientScehduleModalWindow.detach();
                UtilMessagesAndPopups.showError("Sorry!!! the slot is booked");
            }
        }
    };

    public static final EventListener SLOT_BLOCKED = new EventListener() {
        public void onEvent(Event event) throws Exception {
            UtilMessagesAndPopups.showError("Cannot book here");
        }
    };

    public static final String SUBSCRIBED_EVENT_QUEUE_NAMES_IN_SESSION = "subscribedEventQueueNamesInSession";

    private static final long serialVersionUID = 1L;
}