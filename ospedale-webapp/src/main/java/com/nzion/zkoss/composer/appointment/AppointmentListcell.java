package com.nzion.zkoss.composer.appointment;

import java.util.Date;

import com.nzion.view.component.SmartServiceLabel;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Window;

import com.nzion.domain.CalendarSlot;
import com.nzion.domain.Schedule;
import com.nzion.domain.Schedule.ScheduleType;
import com.nzion.util.Constants;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilMisc;
import com.nzion.view.component.TimeLabel;

/**
 * @author Sandeep Prusty
 * Jun 3, 2010
 */
public class AppointmentListcell extends Listcell {

    private static final long serialVersionUID = 1L;

    private static final String STYLE = "border:1px solid #CCCCCC;border-width: 1px 1px 0 0;";

    private CalendarSlot slot;

    private AppointmentGrid grid;

    private Date date;

    private Schedule schedule;

    // This creates a cell and represents an interactive scheduling slot
    public AppointmentListcell(Date selectedDate, CalendarSlot slot, AppointmentGrid grid, String label) {
        this(selectedDate, slot, grid);
        changeCaption(label, false);
    }

    public AppointmentListcell(Date selectedDate, CalendarSlot slot, AppointmentGrid grid) {
        setSclass("apptCell");
        setStyle(STYLE);
        this.slot = slot;
        this.date = UtilDateTime.dateOnly(selectedDate);
        this.grid = grid;
    }

    // This is to create a blocked cell
    public AppointmentListcell(String blockmark, AppointmentGrid grid) {
        setStyle(STYLE);
        changeCaption(blockmark, false);
        this.grid = grid;
    }

    // This is to create a time cell which does not represent a slot
    public AppointmentListcell(Date time) {
        makeTimeCell(time);
    }

    public AppointmentListcell() {
        setStyle(STYLE);
    }

    public AppointmentListcell(AppointmentGrid grid) {
        this.grid = grid;
    }

    public Date getDate() {
        return date;
    }

    public CalendarSlot getSlot() {
        return slot;
    }

    public void makeTimeCell(Date time) {
        setStyle(STYLE);
        TimeLabel timeLabel = new TimeLabel();
        timeLabel.setTime(time);
        timeLabel.setParent(this);
    }

    public Schedule getSchedule() {
        if (schedule == null && date!=null) schedule = slot.buildSchedule(date);
        return schedule;
    }

    public Long getScheduleId() {
        return schedule.getId();
    }

    public void book(Schedule schedule) {
        clearChildren();
        this.schedule = schedule;
        clearChildren();
    }

    public void book(final Schedule schedule, String mark, String columnName) {
        clearChildren();
        this.schedule = schedule;
        clearChildren();
        String color;
        Boolean isHighPriority;
        if(schedule.getPriority() != null && schedule.getPriority().equals("High")){
            color = "#FF6347";
            setStyle(STYLE + ";cursor:default;background-color:" + color);
            isHighPriority = Boolean.TRUE;
        } else{
            color = grid.screenConfig.getColor(schedule.getStatus());
            setStyle(STYLE + ";cursor:default;background-color:" + color);
            isHighPriority = Boolean.FALSE;
        }
        
        if ("patient".equals(columnName)) {
            A link = new A();
            link.setLabel(mark);
            link.setParent(this);
            if(isHighPriority)
                link.setStyle("text-decoration:underline; color:#ffff00");
            else
                link.setStyle("text-decoration:underline");
            link.addEventListener("onClick", new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    String roleName = com.nzion.domain.Roles.getRoleName((Long) Sessions.getCurrent().getAttribute("_role"));
                    if ("PROVIDER".equals(roleName)) {
                        ScheduleController scheduleController = (ScheduleController) Executions.getCurrent().getDesktop()
                                .getAttribute("scheduleController");
                        Window window = (Window) Executions.createComponents("/portlets/patientinfo.zul", null,
                                com.nzion.util.UtilMisc.toMap("patient", schedule.getPatient(), "slotSchedule", schedule,
                                        "scheduleController", scheduleController, "sourceComponent", event.getTarget()
                                                .getParent(),"closeBtn",true));
                        window.setClosable(true);
                        window.setWidth("700px");
                        window.setPosition("center");
                        window.doModal();
                    } else
                    if ("RECEPTION".equals(roleName)) {
                        Events.postEvent("onSlotEdit", event.getTarget().getParent().getFellowIfAny("schedulesVbox"),
                                UtilMisc.toMap("schedule", ((AppointmentListcell) event.getTarget().getParent()).getSchedule(),
                                        "cell",(AppointmentListcell) event.getTarget().getParent()));
                    }
                }
            });
        } else if("visitType".equals(columnName) && schedule.getVisitType() != null && schedule.getVisitType().getName().contains("Consult")
        			&& schedule.isFromMobileApp() ){
        		Image img = new Image();
        		img.setWidth("20px");
        		img.setHeight("20px");
        		img.setSrc("/images/smart_service/req appt.jpg");
        		img.setParent(this);
        		Label label =new Label(mark);
        		label.setParent(this);
        		label.setStyle("margin-left:10px;");
        		this.setLabel("");
        }else if("visitType".equals(columnName) && schedule.getVisitType() != null && schedule.getVisitType().getName().contains("Premium") ){
    		Image img = new Image();
    		img.setWidth("20px");
    		img.setHeight("20px");
    		img.setSrc("/images/smart_service/premium.jpg");
    		img.setParent(this);
    		Label label =new Label(mark);
    		label.setParent(this);
    		label.setStyle("margin-left:10px;");
    		this.setLabel("");
        }else if("visitType".equals(columnName) && schedule.getVisitType() != null && schedule.getVisitType().getName().contains("Tele") ){
    		Image img = new Image();
    		img.setWidth("20px");
    		img.setHeight("20px");
    		img.setSrc("/images/smart_service/teleconsult.png");
    		img.setParent(this);
    		Label label =new Label(mark);
    		label.setParent(this);
    		label.setStyle("margin-left:10px;");
    		this.setLabel("");
        }else if("visitType".equals(columnName) && schedule.getVisitType() != null && schedule.getVisitType().getName().contains("Home") ){
    		Image img = new Image();
    		img.setWidth("20px");
    		img.setHeight("20px");
    		img.setSrc("/images/smart_service/home visit.jpg");
    		img.setParent(this);
    		Label label =new Label(mark);
    		label.setParent(this);
    		label.setStyle("margin-left:10px;");
    		this.setLabel("");
        }else if((("referral".equals(columnName)) && schedule.getReferral() != null)){
            Image img = new Image();
            img.setWidth("20px");
            img.setHeight("20px");
            img.setSrc("/images/icon-refer.jpg");
            img.setParent(this);
            Label label =new Label(mark);
            label.setParent(this);
            label.setStyle("margin-left:10px;");
            label.setTooltiptext(mark);
            this.setLabel("");
        }else
            changeCaption(mark, isHighPriority);
    }

    public void unBook() {
        setContext(ScheduleViewHelper.NOT_SCHEDULED_CONTEXT_MENU_ID);
        if (ScheduleType.BLOCKED.equals(schedule.getScheduleType()))
            addEventListener("onDoubleClick", grid.PATIENT_SCHEDULE_OPENER);
        setStyle(STYLE + ";cursor:default;background-color:" + Constants.WHITE);
        this.schedule = null;
    }

    public void changeCaption(String caption, boolean isHighPriority) {
        clearChildren();
        if(this.getChildren() != null) {
            this.getChildren().clear();
        }
        SmartServiceLabel label = new SmartServiceLabel(caption);
        label.setTooltiptext(caption);
        label.setParent(this);
        this.setLabel("");
        if(isHighPriority){
            label.setStyle("color: #ffff00");
        }
    }

    private void clearChildren() {
        if (getChildren() != null) getChildren().clear();
    }

    public boolean isForSameSlot(Schedule schedule) {
        return schedule.isForSameSlot(schedule);
    }

    @Override
    public void setLabel(String label) {
        SmartServiceLabel smartServiceLabel = new SmartServiceLabel(label);
        super.setLabel(smartServiceLabel.getValue());
    }
}