package com.nzion.zkoss.composer.appointment;

import java.text.SimpleDateFormat;
import java.util.*;

import com.nzion.domain.*;
import com.nzion.domain.Schedule.Tentative;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;

/**
 * @author Sandeep Prusty
 * May 25, 2010
 * Mohan Sharma - Modified to show arrival time, priority, visibility of breaks
 *
 */

@SuppressWarnings("unchecked")
public class DayAppointmentGridRenderer implements AppointmentGridRenderer {

    protected AppointmentGrid grid;

    protected Map<Key, Listitem> slotCache = new HashMap<Key, Listitem>();

    private static final String COLUMNS[] = new String[] { "patient", "visitType", "appointmentStatus", "bookingStatus", "referral",  "arrivalTime","priority"};

    private RowSelectionListener rowSelectionListener = null;

    public DayAppointmentGridRenderer() {
        this.rowSelectionListener = new RowSelectionListener();
    }

    public void setAppointmentGrid(AppointmentGrid grid) {
        this.grid = grid;
    }

    public void addScheduleData(Collection<Schedule> schedules) {
        if (UtilValidator.isEmpty(schedules)) return;
        for (Schedule schedule : schedules)
            addScheduleData(schedule);
    }

    public void addScheduleData(Schedule schedule) {
        Listitem row = slotCache.get(new Key(schedule));
        if (row == null) {
            createDummySlot(schedule);
            return;
        }
        addScheduleData(schedule, row);
    }

    protected void addScheduleData(Schedule schedule, Listitem row) {
        String color;
        final Long scheduleId = schedule.getId();
        if(schedule.getPriority() != null && schedule.getPriority().equals("High")){
            color = "#FF6347";
            row.setStyle("background-color:" + color);
        } else{
            color = grid.screenConfig.getColor(schedule.getStatus());
            row.setStyle("background-color:" + color);
        }
        row.setSclass("apptRow");
        List<Component> cells = row.getChildren();
        int i = 0;
        for (int j = 1; i <= cells.size() - 3; j++) {
            AppointmentListcell cell = (AppointmentListcell)cells.get(j);
            Object o = null;
            String columnName = COLUMNS[i++];
            try {
                o = UtilReflection.getNestedFieldValue(schedule, "appointmentStatus".equals(columnName) ? "status.description" : columnName);
            } catch (Exception e) {

            }
            if("arrivalTime".equals(columnName) && o == null){
                try {
                    o = UtilReflection.getNestedFieldValue(schedule, "lastPatientVisit.time");
                    //o = UtilDateTime.constructTimeFromDateInAmPm((Date)o);
                    o = new SimpleDateFormat("HH:mm").format((Date)o);//24Hour time format
                } catch (Exception e) {

                }
            }
            if("referral".equals(columnName)&&o==null){
                try {
                    o = UtilReflection.getNestedFieldValue(schedule, "internalReferral");
                } catch (Exception e) {

                }
            }
            if("bookingStatus".equals(columnName) && o==null){
            	try {
                    o = UtilReflection.getNestedFieldValue(schedule, "tentativeStatus");
                    if(o != null && "Confirmed".equals(o)){
                    	o = "Acknowledged";
                    }
                    if(o != null && "Paid".equals(o)){
                    	o = "Confirmed";
                    }
                    o = o.toString().toUpperCase();
                } catch (Exception e) {

                }
            }
            String s = o != null ? o.toString() : "";
            if (o instanceof Salutable) {
                s = ViewUtil.getFormattedName(o);
            }
            cell.book(schedule, s, columnName);
        }
        AppointmentListcell actionCell = ((AppointmentListcell) row.getLastChild());
        Button actionBtn = new Button();
        actionBtn.setSclass("btn-mini");
        String roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_default_role"));
        if(roleName == null){
            roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_role"));
        }
        if ("RECEPTION".equals(roleName)) {
            actionBtn.addEventListener("onClick", new EventListener() {
                @Override
                public void onEvent(Event arg0) throws Exception {
                    AppointmentListcell cell = ((AppointmentListcell) arg0.getTarget().getParent());
                    Events.postEvent("onSlotEdit", arg0.getTarget().getFellowIfAny("schedulesVbox"), UtilMisc.toMap("schedule",
                            cell.getSchedule(),"cell",cell));
                    Listbox apptGrid = (Listbox) cell.getParent().getParent();
                    apptGrid.setSelectedItem((Listitem) cell.getParent());
                }
            });
            actionBtn.setLabel("Edit/View");
        } else
        if ("PROVIDER".equals(roleName)) {
            actionBtn.setLabel("Open");
            actionBtn.addEventListener("onClick", new EventListener() {
                @Override
                public void onEvent(Event arg0) throws Exception {
                    AppointmentListcell cell = ((AppointmentListcell) arg0.getTarget().getParent());
                    Events.postEvent("onStartEncounter", arg0.getTarget().getFellowIfAny("schedulesVbox"), UtilMisc.toMap("schedule",
                            cell.getSchedule()));
                }
            });
        }
        ((AppointmentListcell) row.getLastChild()).book(schedule);
        actionBtn.setParent(actionCell);
       
        if(Tentative.Tentative.toString().equals(schedule.getTentativeStatus())){
        	final Schedule scheduleFinal = schedule;
        	final CommonCrudService commonCrudService = Infrastructure.getSpringBean("commonCrudService");
        	Button confirmBtn = new Button();
        	confirmBtn.setSclass("btn-mini");
        	confirmBtn.setLabel("Confirm");
        	confirmBtn.addEventListener("onClick", new EventListener(){
                @Override
                public void onEvent(Event event) throws Exception {
                	scheduleFinal.setTentativeStatus(Tentative.Confirmed.toString());
                	commonCrudService.save(scheduleFinal);
                	Executions.sendRedirect("/");
                	UtilMessagesAndPopups.showSuccess();

                    //new code start for communication loop
                    try{
                        Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
                        if (practice != null) {
                            Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
                            //SmsUtil.sendRequestAppointmentConfirmationSms(schedule.getPatient(), schedule.getPerson(), clinicDetails, schedule.getStartTime(), schedule.getStartDate());
                            Object languagePreference = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername()).get("languagePreference");
                            Map map = RestServiceConsumer.getPatientDetailsByAfyaId(scheduleFinal.getPatient().getAfyaId());
                            clinicDetails.put("languagePreference", languagePreference);
                            clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_CONFIRMED_BY_CLINIC.name());
                            String url = "https://www.afyaarabia.com/afya-portal/#!/Service-payment?userName="+map.get("userName")+"&scheduleId="+scheduleFinal.getId()+"&clinicId="+practice.getTenantId()+"&accountNumber="+map.get("accountNumber");
                            clinicDetails.put("url", url);
                            /*clinicDetails.put("userName", map.get("userName"));
                            clinicDetails.put("scheduleId", scheduleFinal.getId());
                            clinicDetails.put("clinicId", practice.getTenantId());*/
                            SmsUtil.sendStatusSms(scheduleFinal, clinicDetails);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    //new code end for communication loop
                }
            });
        	confirmBtn.setParent(actionCell);
        }
        
        if (!"PROVIDER".equals(roleName)){
            Button printBtn = new Button();
            printBtn.setSclass("btn-mini");
            printBtn.setLabel("Print");
            printBtn.addEventListener("onClick", new EventListener(){
                @Override
                public void onEvent(Event event) throws Exception {
                    AppointmentListcell cell = ((AppointmentListcell) event.getTarget().getParent());
                    Executions.createComponents("/appointment/soapNoteItemsPrint.zul", null,UtilMisc.toMap("schedule", cell.getSchedule()));

                }
            });
            printBtn.setParent(actionCell);
        }
        
    }

    public void removeScheduleData(Component component) {
        Listitem row = (Listitem) component.getParent();
        row.setStyle("background-color:" + Constants.WHITE);
        Iterator<Component> cellIterator = row.getChildren().iterator();
        cellIterator.next();
        while (cellIterator.hasNext()) {
            AppointmentListcell cell = (AppointmentListcell)cellIterator.next();
            cell.unBook();
        }
        addScheduleData(((AppointmentListcell)component).getSchedule());
        row.detach();
    }

    public void buildHeader() {
        Listhead head = new Listhead();
        head.setParent(grid);
        Listheader timeHeader = new Listheader("Time");
        timeHeader.setWidth("80px");
        timeHeader.setParent(head);
        int i = 0;
        for (String column : COLUMNS) {
            Listheader header = new Listheader(UtilDisplay.camelcaseToUiForCopositeFiedl(column));
            header.setSclass("apptHeader_" + i);
            header.setParent(head);
            i++;
        }
        Listheader actionHeader = new Listheader();
        actionHeader.setLabel("Action");
        actionHeader.setParent(head);
    }

    public void setCalendarTemplateAssociations(Collection<CalendarResourceAssoc> assocs) {
        TreeSet<CalendarSlot> slots = new TreeSet<CalendarSlot>();
        for (CalendarResourceAssoc assoc : assocs) {
            if (!assoc.isSatisfiedBy(grid.getDate())) continue;
            slots.addAll(assoc.getSlots());
        }
        for (CalendarSlot slot : slots)
            addSlot(slot);
    }

    private transient Date temporaryPreviousDate;

    protected Listitem addSlot(CalendarSlot slot) {
        Key key = new Key(slot, grid.getDate());
        Listitem row = new Listitem();
        row.setParent(grid);
        row.setAttribute("slot", slot);
        //row.setStyle("background-color:" + Constants.WHITE);
        //new code for color change start
        CalendarIndividualSlot calendarIndividualSlot = null;
        if(slot.getAssociation() != null && slot.getAssociation().getCalendarIndividualSlots() != null){
	        Iterator iterator  = slot.getAssociation().getCalendarIndividualSlots().iterator();
	        Date d1 = slot.getStartTime();
	        Date d2 = slot.getEndTime();
	        while(iterator.hasNext()){
	            calendarIndividualSlot = (CalendarIndividualSlot)iterator.next();
	            if (d1.compareTo(calendarIndividualSlot.getStartTime()) == 0){
	                if (d2.compareTo(calendarIndividualSlot.getEndTime()) == 0){
	                    break;
	                }
	            }
	        }
        }
        if(calendarIndividualSlot != null) {
            if(calendarIndividualSlot.getVisitTypeSoapModule() != null) {
                if(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType() != null) {
                    row.setStyle("background-color:" + calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getColor());
                }else{
                    row.setStyle("background-color:" + Constants.WHITE);
                }
            }else{
                row.setStyle("background-color:" + Constants.WHITE);
            }
        }else{
            row.setStyle("background-color:" + Constants.WHITE);
        }
        //new code for color change end

        final AppointmentListcell timeCell = new AppointmentListcell();
        if (temporaryPreviousDate == null || !temporaryPreviousDate.equals(slot.getStartTime()))
            timeCell.makeTimeCell(slot.getStartTime());
        timeCell.setParent(row);
        temporaryPreviousDate = slot.getStartTime();
        slotCache.put(key, row);
        ScheduleBreak brek = grid.checkBreak(slot, grid.getDate());
        if(brek != null && !brek.isShowInCalender()){
            row.setVisible(false);
            return row;
        }
        if (brek != null && brek.isShowInCalender()) {
            AppointmentListcell cell = null;
            for (int i = 0; i < COLUMNS.length; ++i) {
                if(i == 0)
                    cell = new AppointmentListcell(brek.getName(), grid);
                else
                    cell = new AppointmentListcell(grid);
                cell.setStyle("background-color:" + brek.getColor() + ";");
                cell.setParent(row);
                ((Listitem)cell.getParent()).setStyle("background-color:" + brek.getColor() + ";");
            }
        }else {
            for (int i = 0; i < COLUMNS.length; ++i) {
                AppointmentListcell cell = new AppointmentListcell(grid.getDate(), slot, grid);
                //String roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_role"));
                String roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_default_role"));
                if(roleName == null){
                    roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_role"));
                }

                if ("RECEPTION".equals(roleName))
                    cell.addEventListener("onClick", rowSelectionListener);
                cell.setParent(row);
                //new code
                //cell.setLabel(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getDescription());
                if(i == 1){
                    if(calendarIndividualSlot != null) {
                        if(calendarIndividualSlot.getVisitTypeSoapModule() != null) {
                            if(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType() != null) {
                            	if(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getName().contains("Premium")){
                            		Image img = new Image();
                            		img.setWidth("20px");
                            		img.setHeight("20px");
                            		img.setSrc("/images/smart_service/premium.jpg");
                            		img.setParent(cell);
                            	}else if(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getName().contains("Tele")){
                            		Image img = new Image();
                            		img.setWidth("20px");
                            		img.setHeight("20px");
                            		img.setSrc("/images/smart_service/teleconsult.png");
                            		img.setParent(cell);
                            	}else if(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getName().contains("Home")){
                            		Image img = new Image();
                            		img.setWidth("20px");
                            		img.setHeight("20px");
                            		img.setSrc("/images/smart_service/home visit.jpg");
                            		img.setParent(cell);
                            	}
                            	Label lbl = new Label(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getDescription());
                            	lbl.setStyle("margin-left:10px;");
                            	lbl.setParent(cell);
                            }
                        }
                    }
                }
            }
        }
        AppointmentListcell actionCell = brek != null?new AppointmentListcell(grid) :
                new AppointmentListcell(grid.getDate(), slot, grid);
        if(brek != null)
            actionCell.setStyle("background-color:" + brek.getColor() + ";");
        actionCell.setParent(row);
        String roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_default_role"));
        if(roleName == null){
            roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_role"));
        }
        if ("RECEPTION".equals(roleName))
            actionCell.addEventListener("onClick", rowSelectionListener);

        if(brek == null){
            if ("PROVIDER".equals(roleName)) {
                Button qBtn =  new Button();
                qBtn.setLabel("Quick Book");
                qBtn.setSclass("btn-mini");
                qBtn.addEventListener("onClick", new EventListener() {
                    @Override
                    public void onEvent(Event arg0) throws Exception {
                        AppointmentListcell cell = ((AppointmentListcell) arg0.getTarget().getParent());
                        if(arg0.getTarget().getPage().getFellowIfAny("quickBookWin") == null)
                            Events.postEvent("onQuickBook", arg0.getTarget().getFellowIfAny("schedulesVbox"), UtilMisc.toMap("cell",cell));
                    }
                });
                qBtn.setParent(actionCell);
            }
        }

        row.setSclass("apptRow");
        return row;
    }

    protected void createDummySlot(Schedule schedule) {
        CalendarResourceAssoc nonExistentAssociation = new CalendarResourceAssoc(schedule.getPerson(), schedule.getLocation());
        CalendarSlot nonExistentSlot = new CalendarSlot(schedule, nonExistentAssociation);
        Listitem row = addSlot(nonExistentSlot);
        grid.reArrangeRow(row);
        addScheduleData(schedule, row);
    }

    class RowSelectionListener implements EventListener {
        @Override
        public void onEvent(Event arg0) throws Exception {
            AppointmentListcell cell = ((AppointmentListcell) arg0.getTarget());
            Events.postEvent("onSlotSelect", arg0.getTarget().getFellowIfAny("schedulesVbox"), UtilMisc.toMap("schedule",cell.getSchedule(),"selectedCell",cell));
        }
    }
}