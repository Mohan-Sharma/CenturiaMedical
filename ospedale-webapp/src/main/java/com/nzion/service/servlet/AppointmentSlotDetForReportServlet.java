package com.nzion.service.servlet;

import com.google.gson.Gson;
import com.nzion.domain.*;
import com.nzion.domain.base.Weekdays;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.util.SlotAvailability;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.PersonRepository;
import com.nzion.repository.ScheduleRepository;
import com.nzion.service.ScheduleService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.*;
import com.nzion.view.ScheduleSearchValueObject;
import com.nzion.zkoss.dto.SlotsDetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AppointmentSlotDetForReportServlet extends HttpServlet{

    @Autowired
    PersonRepository personRepository;
    @Autowired
    ScheduleService scheduleService;
    @Autowired
    CommonCrudService commonCrudService;
    @Autowired
    ScheduleRepository scheduleRepository;


    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        int noOfDays = 7;
        Date date = null;

        String appointmentSlotDetailsJsonString = "";
        String clinicId = request.getParameter("clinicId") != null ? request.getParameter("clinicId").trim() :request.getParameter("clinicId");
        String appointmentDate = request.getParameter("fromDate") != null ? request.getParameter("fromDate").trim() : request.getParameter("fromDate");

        if(clinicId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing expected parameters, either one of the parameters is null");
            return;
        }

        try {
            TenantIdHolder.setTenantId(clinicId);
            date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(appointmentDate);

            List<SlotType> slotTypeList = commonCrudService.getAll(SlotType.class);
            List<Provider> providerList = commonCrudService.getAll(Provider.class);

            List<SlotsDetDto> slotsDetDtoList = new ArrayList<SlotsDetDto>();

            Iterator iterator = providerList.iterator();
            while (iterator.hasNext()){
                Provider provider = (Provider)iterator.next();

                Map<SlotType, Boolean> slotTypeMap = new HashMap<SlotType, Boolean>();

                Iterator iteratorSlotType = slotTypeList.iterator();
                while (iteratorSlotType.hasNext()){
                    SlotType slotType = (SlotType)iteratorSlotType.next();
                    VisitTypeSoapModule visitTypeSoapModule = commonCrudService.findUniqueByEquality(VisitTypeSoapModule.class, new String[]{"provider","slotType"}, new Object[]{provider,slotType});
                    slotTypeMap.put(slotType, (visitTypeSoapModule != null) ? visitTypeSoapModule.isSmartServiceDisplayInPortal() : false);
                }

                if (!(slotTypeMap.values().contains(Boolean.TRUE))){
                    continue;
                }

                List<CalendarResourceAssoc> calendarResourceAssocs = commonCrudService.findByEquality(CalendarResourceAssoc.class, new String[]{"person.id"}, new Object[]{Long.valueOf(provider.getId())});
                if(calendarResourceAssocs.size() > 0){
                    for (int i = 0; i < noOfDays; i++) {

                        Date date1 = UtilDateTime.addDaysToDate(date, i);

                        if (getCurrentCalendarResourceAssoc(calendarResourceAssocs, date1) == 0){
                            continue;
                        }

                        SlotsDetDto slotsDetDto = new SlotsDetDto();
                        slotsDetDto.setDoctorId(provider.getId());

                        Set<SlotAvailability> availableSlots = getAvailableSlots(provider.getId().toString(), date1);
                        List<Schedule> scheduleList = getScheduleSlots(provider.getId().toString(), date1);
                        Set<SlotAvailability> totalSlots = getTotalSlots(provider.getId().toString(), date1);

                        Iterator iterator2 = scheduleList.iterator();
                        while (iterator2.hasNext()){
                            Schedule schedule = (Schedule)iterator2.next();
                            //SlotAvailability slotAvailability = new SlotAvailability(schedule);
                            if(!schedule.isMobileOrPatinetPortal()){
                                slotsDetDto.setClinicBooked(slotsDetDto.getClinicBooked() + (int) (UtilDateTime.getInterval(schedule.getStartTime(), schedule.getEndTime()) / 60000));
                            } else {
                                //SlotType slotType = slotAvailability.getSlot().getAssociation().getCalendarIndividualSlots().iterator().next().getVisitTypeSoapModule().getSlotType();
                                SlotType slotType = schedule.getVisitType();
                                if (slotTypeMap.get(slotType)){
                                    switch (slotType.getName()) {
                                        case "Consult Visit":
                                            slotsDetDto.setAppointmentRequestBooked(slotsDetDto.getAppointmentRequestBooked() + (int)(UtilDateTime.getInterval(schedule.getStartTime(),schedule.getEndTime())/60000));
                                            break;
                                        case "Premium Visit":
                                            slotsDetDto.setPremiumVisitBooked(slotsDetDto.getPremiumVisitBooked() + (int)(UtilDateTime.getInterval(schedule.getStartTime(),schedule.getEndTime())/60000));
                                            break;
                                        case "Tele Consultation Visit":
                                            slotsDetDto.setTeleConsultationBooked(slotsDetDto.getTeleConsultationBooked() + (int)(UtilDateTime.getInterval(schedule.getStartTime(), schedule.getEndTime())/60000));
                                            break;
                                        case "Home Visit":
                                            slotsDetDto.setHomeVisitBooked(slotsDetDto.getHomeVisitBooked() + (int)(UtilDateTime.getInterval(schedule.getStartTime(), schedule.getEndTime()))/60000);
                                            break;
                                    }
                                }
                            }
                        }
                        Iterator iterator3 = availableSlots.iterator();
                        while (iterator3.hasNext()){
                            SlotAvailability slotAvailability = (SlotAvailability)iterator3.next();
                            SlotType slotType = slotAvailability.getSlot().getAssociation().getCalendarIndividualSlots().iterator().next().getVisitTypeSoapModule().getSlotType();
                            if (slotTypeMap.get(slotType)){
                                switch (slotType.getName()) {
                                    case "Consult Visit":
                                        slotsDetDto.setAppointmentRequestAvailable(slotsDetDto.getAppointmentRequestAvailable() + (int)(UtilDateTime.getInterval(slotAvailability.getSlot().getStartTime(), slotAvailability.getSlot().getEndTime()) / 60000));
                                        break;
                                    case "Premium Visit":
                                        slotsDetDto.setPremiumVisitAvailable(slotsDetDto.getPremiumVisitAvailable() + (int)(UtilDateTime.getInterval(slotAvailability.getSlot().getStartTime(), slotAvailability.getSlot().getEndTime())/60000));
                                        break;
                                    case "Tele Consultation Visit":
                                        slotsDetDto.setTeleConsultationAvailable(slotsDetDto.getTeleConsultationAvailable() + (int)(UtilDateTime.getInterval(slotAvailability.getSlot().getStartTime(), slotAvailability.getSlot().getEndTime())/60000));
                                        break;
                                    case "Home Visit":
                                        slotsDetDto.setHomeVisitAvailable(slotsDetDto.getHomeVisitAvailable() + (int)(UtilDateTime.getInterval(slotAvailability.getSlot().getStartTime(), slotAvailability.getSlot().getEndTime())/60000));
                                        break;
                                }
                            }
                        }
                        Iterator iterator4 = totalSlots.iterator();
                        while (iterator4.hasNext()){
                            SlotAvailability slotAvailability = (SlotAvailability)iterator4.next();

                            Date startTime = slotAvailability.getSlot().getStartTime();
                            Date endTime = slotAvailability.getSlot().getEndTime();

                            slotsDetDto.setClinicOffered(slotsDetDto.getClinicOffered() + (int)(UtilDateTime.getInterval(startTime, endTime) / 60000));

                            SlotType slotType = slotAvailability.getSlot().getAssociation().getCalendarIndividualSlots().iterator().next().getVisitTypeSoapModule().getSlotType();
                            if (slotTypeMap.get(slotType)){
                                switch (slotType.getName()) {
                                    case "Consult Visit":
                                        slotsDetDto.setAppointmentRequestTotal(slotsDetDto.getAppointmentRequestTotal() + (int)(UtilDateTime.getInterval(startTime, endTime) / 60000));
                                        break;
                                    case "Premium Visit":
                                        slotsDetDto.setPremiumVisitTotal(slotsDetDto.getPremiumVisitTotal() + (int)(UtilDateTime.getInterval(startTime, endTime)/60000));
                                        break;
                                    case "Tele Consultation Visit":
                                        slotsDetDto.setTeleConsultationTotal(slotsDetDto.getTeleConsultationTotal() + (int)(UtilDateTime.getInterval(startTime, endTime)/60000));
                                        break;
                                    case "Home Visit":
                                        slotsDetDto.setHomeVisitTotal(slotsDetDto.getHomeVisitTotal() + (int)(UtilDateTime.getInterval(startTime, endTime)/60000));
                                        break;
                                }
                            }
                        }
                        slotsDetDto.setDate(UtilDateTime.format(date1));
                        slotsDetDtoList.add(slotsDetDto);
                    }
                }
            }//iterate provider list end

            //create json String
            Gson gson = new Gson();

            appointmentSlotDetailsJsonString = gson.toJson(slotsDetDtoList);
            //System.out.println(jsonCartList);

        } catch (ParseException e) {
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, e.toString());
        }
        PrintWriter writer = response.getWriter();
        writer.print(appointmentSlotDetailsJsonString);
        writer.close();
    }

    public static void main(String args[]){
        System.out.println(UtilDateTime.addHrsToDate(new Date(), 720));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    public Set<SlotAvailability> getAvailableSlots(String providerId, Date appointmentDate) throws ParseException {
        //Date appointmentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(appointmentDateInString);
        Person provider = personRepository.getPersonById(Long.valueOf(providerId));
        ScheduleSearchValueObject searchObject = new ScheduleSearchValueObject(false);
        //location = (Location)Sessions.getCurrent().getAttribute("_location");
        Weekdays weekdays = Weekdays.allSelectedWeekdays();
        //searchObject.setLocation(location);
        searchObject.setPerson(provider);
        searchObject.setFromDate(appointmentDate);
        searchObject.setThruDate(appointmentDate);
        return scheduleService.getTotalAvailableSlots(searchObject, weekdays);
    }

    public List<Schedule> getScheduleSlots(String providerId, Date appointmentDate) throws ParseException {
        //Set<SlotAvailability> slotAvailabilitySet = new HashSet<SlotAvailability>();
        Person provider = personRepository.getPersonById(Long.valueOf(providerId));
        ScheduleSearchValueObject searchObject = new ScheduleSearchValueObject(false);
        searchObject.setPerson(provider);
        searchObject.setFromDate(appointmentDate);
        searchObject.setThruDate(appointmentDate);
        List<Schedule> scheduleList = scheduleRepository.searchScheduleFor(searchObject);
        /*Iterator iterator = scheduleList.iterator();
        while (iterator.hasNext()){
            slotAvailabilitySet.add(new SlotAvailability((Schedule)iterator.next()));
        }*/
        return scheduleList;
    }

    public Set<SlotAvailability> getTotalSlots(String providerId, Date appointmentDate) throws ParseException {
        //Date appointmentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(appointmentDateInString);
        Person provider = personRepository.getPersonById(Long.valueOf(providerId));
        ScheduleSearchValueObject searchObject = new ScheduleSearchValueObject(false);
        //location = (Location)Sessions.getCurrent().getAttribute("_location");
        Weekdays weekdays = Weekdays.allSelectedWeekdays();
        //searchObject.setLocation(location);
        searchObject.setPerson(provider);
        searchObject.setFromDate(appointmentDate);
        searchObject.setThruDate(appointmentDate);
        return scheduleService.getTotalSlots(searchObject, weekdays);
    }

    private int getCurrentCalendarResourceAssoc(List<CalendarResourceAssoc> calendarResourceAssocs, Date appointmentDate) throws ParseException {
        List<CalendarResourceAssoc> calAssocs = new ArrayList<CalendarResourceAssoc>();
        for(CalendarResourceAssoc calendarResourceAssoc :  calendarResourceAssocs){
            if(calendarResourceAssoc.getThruDate() == null && (appointmentDate.after(calendarResourceAssoc.getFromDate()) || appointmentDate.equals(calendarResourceAssoc.getFromDate())))
                calAssocs.add(calendarResourceAssoc);
            if(calendarResourceAssoc.getThruDate() != null && (appointmentDate.after(calendarResourceAssoc.getFromDate()) || appointmentDate.equals(calendarResourceAssoc.getFromDate())) && (appointmentDate.before(calendarResourceAssoc.getThruDate()) || appointmentDate.equals(calendarResourceAssoc.getThruDate())))
                calAssocs.add(calendarResourceAssoc);
        }
        String day = new SimpleDateFormat("EEE").format(appointmentDate);
        Iterator iterator = calAssocs.iterator();
        while (iterator.hasNext()){
            CalendarResourceAssoc calendarResourceAssoc = (CalendarResourceAssoc)iterator.next();
            if (calendarResourceAssoc.getWeek() != null){
                List<String> listOfDays = calendarResourceAssoc.getWeek().getSelectedDays();
                if (!listOfDays.contains(day)){
                    iterator.remove();
                }
            }

        }
        return calAssocs.size();
    }
}
