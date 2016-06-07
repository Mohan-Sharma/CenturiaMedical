package com.nzion.zkoss.composer.appointment;

import com.nzion.domain.*;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilMisc;
import com.nzion.util.UtilValidator;
import com.nzion.util.ViewUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;

import java.util.*;

/**
 * @author Sandeep Prusty
 * May 25, 2010
 * Mohan Sharma -  implemented visibility of breaks
 */

@SuppressWarnings("unchecked")
public class MonthAppointmentGridRenderer implements AppointmentGridRenderer {

    private AppointmentGrid grid;

    private final Map<Key, AppointmentListcell> slotCache = new HashMap<Key, AppointmentListcell>();

    private Date[] datesInMonth;

    private TreeSet[] dateSlotMap;

    private SlotSelectionListener slotSelectionListener = null;

    public MonthAppointmentGridRenderer() {
        this.slotSelectionListener = new SlotSelectionListener();
    }

    public void buildHeader() {
        Listhead head = new Listhead();
        head.setParent(grid);
        Listheader timeHeader = new Listheader("Time");
        timeHeader.setParent(head);
        timeHeader.setWidth("2.5%");
        Date referenceDate = grid.getDate();
        Date uptoDate = UtilDateTime.addMonthsToDate(referenceDate, 1);
        int noOfDays = UtilDateTime.getIntervalInDays(referenceDate, uptoDate);
        datesInMonth = new Date[noOfDays];
        dateSlotMap = new TreeSet[noOfDays];
        Date toDay = UtilDateTime.nowDateOnly();
        for (int i = 0; i < datesInMonth.length; i++) {
            datesInMonth[i] = UtilDateTime.addDaysToDate(referenceDate, i);
            dateSlotMap[i] = new TreeSet();
            Listheader header = new Listheader(UtilDateTime.getFromDate(datesInMonth[i], Calendar.DATE) + "");
            header.setAlign("center");
            header.setWidth((97.5/datesInMonth.length)+"%");
            header.setParent(head);
            if (toDay.equals(datesInMonth[i])) header.setStyle(ScheduleViewHelper.TODAY_HEDAER_STYLE);
        }
    }

    public void setAppointmentGrid(AppointmentGrid grid) {
        this.grid = grid;
    }

    public void addScheduleData(Schedule schedule) {
        AppointmentListcell cell = slotCache.get(new Key(schedule));
        if (cell == null) {
            createDummySlot(schedule);
            cell = slotCache.get(new Key(schedule));
        }
        if (cell != null) {
            cell.book(schedule, ViewUtil.getFormattedName(schedule.getPatient()), "patient");
        }
    }

    public void addScheduleData(Collection<Schedule> schedules) {
        if (UtilValidator.isEmpty(schedules)) return;
        for (Schedule schedule : schedules)
            addScheduleData(schedule);
    }

    public void removeScheduleData(Component component) {
        AppointmentListcell cell = (AppointmentListcell) component;
        cell.unBook();
    }

    private void addAssocToSlotAssociationzMap(int i, CalendarResourceAssoc assoc) {
        if (!assoc.isSatisfiedBy(datesInMonth[i])) return;
        for (CalendarSlot slot : assoc.getSlots())
            dateSlotMap[i].add(slot);
    }

    public void setCalendarTemplateAssociations(Collection<CalendarResourceAssoc> assocs) {
        TreeSet<CalendarSlot> unionOfSlots = new TreeSet<CalendarSlot>();
        for (CalendarResourceAssoc assoc : assocs)
            unionOfSlots.addAll(assoc.getSlots());
        for (int i = 0; i < datesInMonth.length; ++i) {
            for (CalendarResourceAssoc assoc : assocs)
                addAssocToSlotAssociationzMap(i, assoc);
        }
        for (CalendarSlot slot : unionOfSlots)
            addSlot(slot);
    }

    private transient Date temporaryPreviousDate;

    private Listitem addSlot(CalendarSlot slot) {
        Listitem row = new Listitem();
        row.setParent(grid);
        row.setAttribute("slot", slot);
        AppointmentListcell timeCell = new AppointmentListcell();
        if (temporaryPreviousDate == null || !temporaryPreviousDate.equals(slot.getStartTime()))
            timeCell.makeTimeCell(slot.getStartTime());
        temporaryPreviousDate = slot.getStartTime();
        timeCell.setParent(row);
        for (int i = 0; i < datesInMonth.length; ++i) {
            boolean mapped = dateSlotMap[i].contains(slot);
            ScheduleBreak brek = grid.checkBreak(slot, datesInMonth[i]);
            if(brek != null && !brek.isShowInCalender()){
                row.setVisible(false);
                return row;
            }
            String brekName = brek != null ? brek.getName() : "X";
            AppointmentListcell cell = null;
            if (!mapped || brek != null){
                cell = new AppointmentListcell(brekName, grid);
                if (brek != null){
                    cell.setStyle("background-color:" + brek.getColor() + ";");
                }
            }else {
                cell = new AppointmentListcell(datesInMonth[i], slot, grid);
                //new code start
                CalendarIndividualSlot calendarIndividualSlot = null;
                if (slot.getAssociation().getCalendarIndividualSlots() != null) {
                    Iterator iterator = slot.getAssociation().getCalendarIndividualSlots().iterator();
                    Date d1 = slot.getStartTime();
                    Date d2 = slot.getEndTime();
                    while (iterator.hasNext()) {
                        calendarIndividualSlot = (CalendarIndividualSlot) iterator.next();
                        if (d1.compareTo(calendarIndividualSlot.getStartTime()) == 0) {
                            if (d2.compareTo(calendarIndividualSlot.getEndTime()) == 0) {
                                break;
                            }
                        }
                    }
                }
                /*if(calendarIndividualSlot != null) {
                    cell.setStyle("background-color:" + calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getColor() + ";");
                    //row.setStyle("background-color:" + calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getColor());
                }*/
                if(calendarIndividualSlot != null) {
                    if(calendarIndividualSlot.getVisitTypeSoapModule() != null) {
                        if(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType() != null) {
                            cell.setStyle("background-color:" + calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getColor() + ";");
                        }
                    }
                }
                //new code end
                cell.addEventListener("onClick", slotSelectionListener);
            }
            cell.setParent(row);
            if (mapped) slotCache.put(new Key(slot, datesInMonth[i]), cell);
        }
        row.setSclass("apptRow");
        return row;
    }

    private void createDummySlot(Schedule schedule) {
        CalendarResourceAssoc nonExistentAssociation = new CalendarResourceAssoc(schedule.getPerson(), schedule
                .getLocation());
        CalendarSlot nonExistentSlot = new CalendarSlot(schedule, nonExistentAssociation);
        int monthDayNumber = UtilDateTime.getIntervalInDays(datesInMonth[0], schedule.getStartDate());
        if (monthDayNumber >= dateSlotMap.length)
            return;
        dateSlotMap[monthDayNumber].add(nonExistentSlot);
        Listitem row = addSlot(nonExistentSlot);
        grid.reArrangeRow(row);
    }

    class SlotSelectionListener implements EventListener {
        @Override
        public void onEvent(Event arg0) throws Exception {
            AppointmentListcell cell = ((AppointmentListcell) arg0.getTarget());
            String roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_role"));
            if ("RECEPTION".equals(roleName)) {
                Events.postEvent("onSlotSelect", arg0.getTarget().getFellowIfAny("schedulesVbox"), UtilMisc.toMap("schedule",
                        cell.getSchedule(),"selectedCell",cell));
            }
            ((Listbox) cell.getParent().getParent()).setSelectedItem(null);
        }
    }
}