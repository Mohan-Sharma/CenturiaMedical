package com.nzion.service.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nzion.domain.*;
import com.nzion.domain.Enumeration;
import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.Schedule.Tentative;
import com.nzion.domain.base.Weekdays;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.billing.DebitCreditEnum;
import com.nzion.domain.billing.Invoice;
import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.billing.InvoicePayment;
import com.nzion.domain.billing.InvoiceStatusItem;
import com.nzion.domain.billing.InvoiceType;
import com.nzion.domain.billing.PaymentType;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.emr.soap.SoapReferral;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.domain.util.SlotAvailability;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.PatientRepository;
import com.nzion.repository.PersonRepository;
import com.nzion.repository.PracticeRepository;
import com.nzion.repository.notifier.utility.EmailUtil;
import com.nzion.repository.notifier.utility.NotificationTaskExecutor;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.service.ScheduleService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.billing.InvoiceManager;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.impl.EnumerationServiceImpl;
import com.nzion.service.dto.BookAppointmentDto;
import com.nzion.service.impl.FileBasedServiceImpl;
import com.nzion.util.*;
import com.nzion.view.ScheduleSearchValueObject;
import com.nzion.zkoss.composer.BillingController;
import org.joda.time.LocalDate;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Mohan Sharma on 5/18/2015.
 */
public class PatientBookAppointmentServlet extends HttpServlet{
    private Location location;

    @Autowired
    PersonRepository personRepository;
    @Autowired
    ScheduleService scheduleService;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    PracticeRepository practiceRepository;
    @Autowired
    CommonCrudService commonCrudService;
    @Autowired
    NotificationTaskExecutor notificationTaskExecutor;
    @Autowired
    EnumerationServiceImpl enumerationServiceImpl;
    @Autowired
    private BillingService billingService;
    @Autowired
    private FileBasedServiceImpl fileBasedServiceImpl;

    private SlotType slotType;

    private BigDecimal consultationCharges;

    private BigDecimal convenienceFee;

    private BigDecimal registrationCharges;

    private BigDecimal totalAmount;

    private BigDecimal totalAdvAmount;

    private boolean displayConvenienceFee;

    Schedule currentSchedule;

    private Invoice invoice;

    private String url;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> slots = null;
        Set<CalendarIndividualSlot> calendarIndividualSlots = new HashSet<>();
        List<SlotWithVisitType> furnishedSlots = null;
        Date date = null;

        BigDecimal leadTime = null;
        BigDecimal maxTime = null;

        String clinicId = request.getParameter("clinicId") != null ? request.getParameter("clinicId").trim() :request.getParameter("clinicId");
        String providerId = request.getParameter("providerId") != null ? request.getParameter("providerId").trim() : request.getParameter("providerId");
        String appointmentDate = request.getParameter("appointmentDate") != null ? request.getParameter("appointmentDate").trim() : request.getParameter("appointmentDate");
        String visitType = request.getParameter("visitType") != null ? request.getParameter("visitType").trim() : request.getParameter("visitType");
        if(clinicId == null || providerId == null || appointmentDate == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing expected parameters, either one of the parameters is null");
            return;
        }
        if(!isInteger(providerId.trim())){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid provider id");
            return;
        }
        try {
            date = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(appointmentDate);
            if(validateDate(date)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid date, cannot book appointment from past");
                return;
            }
            TenantIdHolder.setTenantId(clinicId);
            List<CalendarResourceAssoc> calendarResourceAssocs = commonCrudService.findByEquality(CalendarResourceAssoc.class, new String[]{"person.id"}, new Object[]{Long.valueOf(providerId)});
            Set<SlotAvailability> timeslots = null;
            if(calendarResourceAssocs.size() > 0){
                List<CalendarResourceAssoc> assocs = getCurrentCalendarResourceAssoc(calendarResourceAssocs, date);

                //***********Added code for available day of week start******
                String day = new SimpleDateFormat("EEE").format(date);
                Iterator iterator = assocs.iterator();
                while (iterator.hasNext()){
                    CalendarResourceAssoc calendarResourceAssoc = (CalendarResourceAssoc)iterator.next();
                    if (calendarResourceAssoc.getWeek() != null){
                        List<String> listOfDays = calendarResourceAssoc.getWeek().getSelectedDays();
                            if (!listOfDays.contains(day)){
                                iterator.remove();
                                //break;
                            }
                    }

                }
                //******Added code for available day of week end ******

                for(CalendarResourceAssoc assoc : assocs)
                	for(CalendarIndividualSlot calendarIndividualSlot : assoc.getCalendarIndividualSlots() ){
                		if(visitType.equals(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getName()))
                				calendarIndividualSlots.add(calendarIndividualSlot);
                	}
            }
            timeslots = getAvailableSlots(providerId, appointmentDate);
            if(timeslots.size() > 0) {
                slots = getTimeSlot(timeslots);
            } else {
                slots = new HashSet<>();
            }

            slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{visitType});
            if(slotType == null){
                slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{"Consult Visit"});
            }
            RCMVisitType rcmVisitType = null;
            if(slotType.getName().equals("Premium Visit"))
                rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
            if(slotType.getName().equals("Home Visit"))
                rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
            if(slotType.getName().equals("Tele Consultation Visit"))
                rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
            if(slotType.getName().equals("Consult Visit"))
	          	rcmVisitType = RCMVisitType.CONSULT_VISIT;
            RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
            SchedulingPreference schedulingPreferences = commonCrudService.findUniqueByEquality(SchedulingPreference.class, new String[]{"rcmPreference","visitType"}, new Object[]{rcmPreference,rcmVisitType});
            Set<CalendarIndividualSlot> calendarIndividualSlotsFilter = new HashSet<>();
            leadTime = schedulingPreferences.getLeadTimeAllowed();
            maxTime = schedulingPreferences.getMaxTimeAllowed();
            for(CalendarIndividualSlot calendarIndividualSlot : calendarIndividualSlots){
                Date slotDateTime = UtilDateTime.toDate(date.getMonth(), date.getDate(), date.getYear(), calendarIndividualSlot.getStartTime().getHours(),
                        calendarIndividualSlot.getStartTime().getMinutes(), calendarIndividualSlot.getStartTime().getSeconds());
                slotDateTime = UtilDateTime.getDayStart(slotDateTime);
                String calendarVisitType = calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().toString();
                if(calendarVisitType.equals("Premium Visit") || calendarVisitType.equals("Home Visit") ||
                        calendarVisitType.equals("Tele Consultation Visit") || calendarVisitType.equals("Consult Visit") ){

                    BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(UtilDateTime.getDayStart(new Date()), slotDateTime));
                    if(hoursInterval.compareTo(leadTime) >= 0 && hoursInterval.compareTo(maxTime) <= 0 ){
                        calendarIndividualSlotsFilter.add(calendarIndividualSlot);
                    }
                }
            }


            if(!calendarIndividualSlotsFilter.isEmpty() && !slots.isEmpty()){
                furnishedSlots = createMappingOfSlotAndVisitType(calendarIndividualSlotsFilter , slots);
            }
        } catch (ParseException e) {
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, e.toString());
        }
        PrintWriter writer = response.getWriter();
        if(furnishedSlots != null)
            writer.print(furnishedSlots);
        else {
        	if( UtilDateTime.addHrsToDate(new Date(), leadTime.intValue()).compareTo(UtilDateTime.getDayEnd(date)) <= 0  &&
        			UtilDateTime.addHrsToDate(new Date(), maxTime.intValue()).compareTo(UtilDateTime.getDayStart(date)) >= 0  ){
        		writer.print( "On " + UtilDateTime.toDateString(date) + " the requested slots are not available, Please try alternate Slots" );
        	}else{
	            writer.print( "Appointments can be booked only between " +
	             UtilDateTime.toDateString(UtilDateTime.addHrsToDate(new Date(), leadTime.intValue())) + " to " +
	             UtilDateTime.toDateString(UtilDateTime.addHrsToDate(new Date(), maxTime.intValue()))  );
        	}

        }
        writer.close();
    }

    public static void main(String args[]){
        System.out.println( UtilDateTime.addHrsToDate(new Date(), 720) );
    }

    private List<SlotWithVisitType> createMappingOfSlotAndVisitType(Set<CalendarIndividualSlot> calendarIndividualSlots, Set<String> slots) {
        List<SlotWithVisitType> mapList = new ArrayList<>();
        for(String time : slots){
            StringBuffer buffer = null;
            SlotWithVisitType slotWithVisitType;
            for(CalendarIndividualSlot calendarIndividualSlot : calendarIndividualSlots){
                buffer = new StringBuffer();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                buffer.append(sdf.format(calendarIndividualSlot.getStartTime()));
                buffer.append(" - ");
                buffer.append(sdf.format(calendarIndividualSlot.getEndTime()));
                if(time.equals(buffer.toString())){
                    slotWithVisitType = new SlotWithVisitType(time, calendarIndividualSlot.getVisitTypeSoapModule() != null ? calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getName() : "");
                    mapList.add(slotWithVisitType);
                }
            }
        }
        return mapList;
    }

    private List<CalendarResourceAssoc> getCurrentCalendarResourceAssoc(List<CalendarResourceAssoc> calendarResourceAssocs, Date appointmentDate) throws ParseException {
    	List<CalendarResourceAssoc> calAssocs = new ArrayList<CalendarResourceAssoc>();
        for(CalendarResourceAssoc calendarResourceAssoc :  calendarResourceAssocs){
            if(calendarResourceAssoc.getThruDate() == null && (appointmentDate.after(calendarResourceAssoc.getFromDate()) || appointmentDate.equals(calendarResourceAssoc.getFromDate())))
            	calAssocs.add(calendarResourceAssoc);
            if(calendarResourceAssoc.getThruDate() != null && (appointmentDate.after(calendarResourceAssoc.getFromDate()) || appointmentDate.equals(calendarResourceAssoc.getFromDate())) && (appointmentDate.before(calendarResourceAssoc.getThruDate()) || appointmentDate.equals(calendarResourceAssoc.getThruDate())))
            	calAssocs.add(calendarResourceAssoc);
        }
        return calAssocs;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String tenantId = request.getParameter("clinicId");
        if(tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ClinicId cannot be null");
            return;
        }
        TenantIdHolder.setTenantId(tenantId);
        Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(tenantId);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        objectMapper.setDateFormat(df);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        BookAppointmentDto bookAppointmentDto = objectMapper.readValue(request.getInputStream(), BookAppointmentDto.class);
        if(bookAppointmentDto.getFirstName() == null || bookAppointmentDto.getLastName() == null || bookAppointmentDto.getMobileNumber() == null  || bookAppointmentDto.getProviderId() == null || bookAppointmentDto.getAppointmentEndDate() == null || bookAppointmentDto.getAppointmentStartDate() == null || bookAppointmentDto.getDateOfBirth() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "appointment details cannot be null");
            return;
        }
        if(validateDate(bookAppointmentDto.getAppointmentStartDate()) || validateDate(bookAppointmentDto.getAppointmentEndDate())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid date, cannot book appointment from past");
            return;
        }
        Patient patient = checkIfPatientAlreadyExistOrPersist(bookAppointmentDto, tenantId, response);
        if(patient == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "either patient does not exist with the given afya_id or gender is null");
            return;
        }
        Person provider = personRepository.getPersonById(Long.valueOf(bookAppointmentDto.getProviderId()));
        /*Mohan Sharma - Requirement to book as many time as he wish*/
        /*List existingScheduleList = commonCrudService.findByEquality(Schedule.class, new String[]{"person.id","patient.id", "startDate"}, new Object[]{provider.getId(), patient.getId(), bookAppointmentDto.getAppointmentStartDate()});
        if(com.nzion.util.UtilValidator.isNotEmpty(existingScheduleList) && openScheduleAlreadyExist(existingScheduleList)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Appointment already open for given date doctor and patient.");
            return;
        }*/
        url = request.getRequestURL().toString();
        url = url.substring(0, url.lastIndexOf("clinicMaster"));

        Map<String, Object> result = bookAppointment(response, patient, provider, bookAppointmentDto, clinicDetails);
        String status = (String)result.get("status");
        if(status.equals("created"))
            response.setStatus(HttpServletResponse.SC_OK, "appointment booked");
        if(status.equals("existing"))
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "schedule already exist with given time slot, please create new");

        response.setStatus(HttpServletResponse.SC_OK, "appointment booked");

    }

    public Map<String, Object> bookAppointment(HttpServletResponse response, Patient patient, Person provider, BookAppointmentDto bookAppointmentDto, Map<String, Object> clinicDetails) {
        currentSchedule = new Schedule();
        Map<String, Object> result = new HashMap<>();
        Location location = practiceRepository.getLocation(bookAppointmentDto.getLocation());
        currentSchedule.setLocation(location);
        slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{bookAppointmentDto.getVisitType()});
        if(slotType == null){
            slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{"Consult Visit"});
        }
        currentSchedule.setPerson(provider);
        currentSchedule.setVisitType(slotType);
        currentSchedule.setPatient(patient);
        currentSchedule.setStartTime(convertGivenDate(bookAppointmentDto.getAppointmentStartDate()));
        currentSchedule.setEndTime(convertGivenDate(bookAppointmentDto.getAppointmentEndDate()));
        currentSchedule.setStartDate(com.nzion.util.UtilDateTime.getDayStart(bookAppointmentDto.getAppointmentStartDate()));
        currentSchedule.setComments(bookAppointmentDto.getNotes());
        currentSchedule.setFromMobileApp(bookAppointmentDto.isFromMobileApp());
        List<Schedule> existingSchdules = commonCrudService.findByEquality(Schedule.class,
                new String[]{"startDate", "startTime", "endTime", "person"}, new Object[]{currentSchedule.getStartDate(), currentSchedule.getStartTime(), currentSchedule.getEndTime(), provider});
        for(Schedule existingSchdule : existingSchdules){
            if(UtilValidator.isNotEmpty(existingSchdule.getId()) && !STATUS.CANCELLED.equals(existingSchdule.getStatus()) ) {
                result.put("schedule", existingSchdule);
                result.put("status", "existing");
                return result;
            }
        }
        currentSchedule.setPaymentId(bookAppointmentDto.getPaymentId());
        Referral referral = null;
        if (bookAppointmentDto.getReferralClinicId() != null) {
            List<Referral> referrals = commonCrudService.findByEquality(Referral.class, new String[]{"tenantId"}, new Object[]{bookAppointmentDto.getReferralClinicId()});
            if (UtilValidator.isNotEmpty(referrals)) {
                referral = referrals.get(0);
            }
        }
        currentSchedule.setReferral(referral);
        currentSchedule.setReferralDoctorFirstName(bookAppointmentDto.getReferralDoctorName());
        currentSchedule.setSequenceNum(0);
        currentSchedule.setMobileOrPatinetPortal(true);
        currentSchedule.setConsultationInvoiceGenerated(true);

        final String referralClinicId = bookAppointmentDto.getReferralClinicId();
        final Long soapReferralId = bookAppointmentDto.getSoapReferralId();

        //update referral status in soap raferral
        if (soapReferralId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TenantIdHolder.setTenantId(referralClinicId);

                        SoapReferral soapReferral = commonCrudService.getById(SoapReferral.class, soapReferralId);
                        soapReferral.setStatus("CONFIRMED");
                        commonCrudService.save(soapReferral);

                        TenantIdHolder.setTenantId(Infrastructure.getPractice().getTenantId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        if(slotType.getName().equals("Premium Visit") || slotType.getName().equals("Home Visit") || slotType.getName().equals("Tele Consultation Visit")
        		|| slotType.getName().equals("Consult Visit") ){
            BigDecimal leadTime = updatePrice();
            BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(new Date(), bookAppointmentDto.getAppointmentStartDate()));
            if(hoursInterval.compareTo(leadTime) < 0){
                try {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "lead_time:" + leadTime.toString() + "hrs");
                } catch (IOException e) {
                }
                return null;
            }
        }
        currentSchedule.setTentativeStatus(Tentative.Confirmed.toString());
        if(slotType.getName().equals("Consult Visit"))
        	currentSchedule.setTentativeStatus(Tentative.Tentative.toString());
        currentSchedule =  scheduleService.createSchedule(currentSchedule);
        //updatePrice();
        if(!slotType.getName().equals("Consult Visit")) {
            invoice = generateInvoice(currentSchedule);
            try {
                calculateInvoiceAmount(invoice);
            } catch (Exception e){e.printStackTrace();}
        }
        InputStreamSource inputStreamSource  = null;
        if (invoice != null){
            inputStreamSource = PDFGenerator.createWeeklyProviderRevenueReportPDFFile(invoice, bookAppointmentDto.getPaymentId(), url);
        }
        result.put("schedule", currentSchedule);
        result.put("status", "created");
        try {

            final Map<String, Object> clinicDet = new HashMap<String, Object>();
            clinicDet.putAll(clinicDetails);

            /*List visitTypeSoapModuleList = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider","slotType"}, new Object[]{currentSchedule.getPerson(),slotType});
            if((UtilValidator.isNotEmpty(visitTypeSoapModuleList)) && (!((VisitTypeSoapModule)visitTypeSoapModuleList.get(0)).isSmartService())) {*/
            if(!currentSchedule.isMobileOrPatinetPortal()){
                NotificationSetup notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[]{"status"}, new Object[]{NotificationSetup.STATUS.SCHEDULED});
                String cronExpression = CronExpressionGenerator.generateCronExpressionGivenDateParameters(true, new Date(), notificationSetup.getTriggetPointValue());
                if (notificationSetup.isNotificationRequired() && notificationSetup.isPatientRole() && Tentative.Confirmed.toString().equals(currentSchedule.getTentativeStatus())) {
                    boolean sendSMS = Boolean.FALSE;
                    if (notificationSetup.isBySMS() && !currentSchedule.isWalkinAppointment())
                        sendSMS = Boolean.TRUE;
                    notificationTaskExecutor.prepareDetailsAndNotifyAppointmentSchedule(currentSchedule, cronExpression, notificationSetup.isByEmail(), sendSMS, clinicDetails);
                }
            }
            //EmailUtil.sendAppointmentConfirmationMail((Schedule)result.get("schedule"), patient, provider, clinicDetails);
            //new code start for sending sms


        if(!currentSchedule.getVisitType().getName().equals("Consult Visit")) {
            Invoice invoice = commonCrudService.findUniqueByEquality(Invoice.class, new String[]{"schedule"}, new Object[]{currentSchedule});
            String amount = invoice.getCollectedAmount().getAmount().setScale(3, BigDecimal.ROUND_HALF_UP).toString();
            clinicDet.put("collectedAmount", amount);
        }
            final Map adminUserLogin = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername());
            Object languagePreference = adminUserLogin.get("languagePreference");
            clinicDet.put("languagePreference", languagePreference);
            clinicDet.put("status", "schedule");
            /*if((UtilValidator.isNotEmpty(visitTypeSoapModuleList)) && (((VisitTypeSoapModule)visitTypeSoapModuleList.get(0)).isSmartService())) {*/
            if(currentSchedule.isMobileOrPatinetPortal()){
            createDataForSms(currentSchedule, clinicDet, adminUserLogin, inputStreamSource);
            }
            //new code end for sending sms
        } /*catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } */
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Invoice generateInvoice(Schedule currentSchedule){
        RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
        BigDecimal totalCosultFeeConvFee = consultationCharges.add(convenienceFee);
        BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
        Long itemId = (System.currentTimeMillis()+currentSchedule.getId());
        final Invoice invoice = new Invoice(itemId.toString(),PatientSoapNote.class.getName(), (Provider)currentSchedule.getPerson(),
                currentSchedule.getPatient(), currentSchedule.getLocation());
        invoice.setSchedule(currentSchedule);
        invoice.setInvoiceType(InvoiceType.OPD);
        invoice.setPaymentId(currentSchedule.getPaymentId());
        List<Invoice> invoices = billingService.getFirstInvoice(currentSchedule.getPatient());
        if(UtilValidator.isEmpty(invoices) && billingDisplayConfig!=null){
            InvoiceItem invItem =   new InvoiceItem(invoice,currentSchedule.getId().toString() , InvoiceType.OPD_REGISTRATION,
                    "Registration Charges", 1, null,PatientSoapNote.class.getName());
            if (billingDisplayConfig.getRegistrationFee()!= null){
                invItem.init(billingDisplayConfig.getRegistrationFee(), billingDisplayConfig.getCurrency().getCode(), new Money(billingDisplayConfig.getRegistrationFee(), convertTo()),
                        new Money(billingDisplayConfig.getRegistrationFee(), convertTo()),0);
                invItem.setCopayAmount(invItem.getGrossAmount());
                invoice.addInvoiceItem(invItem);
                if(invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
                    invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(billingDisplayConfig.getRegistrationFee()),convertTo()));
                else
                    invoice.setTotalAmount(new com.nzion.domain.product.common.Money(billingDisplayConfig.getRegistrationFee(),convertTo()));
            }
        }
        RCMVisitType rcmVisitType = null;
        if(slotType.getName().equals("Premium Visit"))
            rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
        if(slotType.getName().equals("Home Visit"))
            rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
        if(slotType.getName().equals("Tele Consultation Visit"))
            rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
        if(slotType.getName().equals("Consult Visit") && currentSchedule.isFromMobileApp())
            rcmVisitType = RCMVisitType.CONSULT_VISIT;


        SchedulingPreference schedulingPreference = commonCrudService.findUniqueByEquality(SchedulingPreference.class,
                new String[]{"rcmPreference", "visitType"}, new Object[]{rcmPreference, rcmVisitType});

            Referral referral = currentSchedule.getReferral();
            ReferralContract referralContract = null;
            //ReferralContract referralContract = commonCrudService.findUniqueByEquality(ReferralContract.class, new String[]{"refereeClinicId"}, new Object[]{referral.getTenantId()});
        if(referral != null) {
            List referralContractList = commonCrudService.findByEquality(ReferralContract.class, new String[]{"refereeClinicId"}, new Object[]{referral.getTenantId()});
            Iterator iterator = referralContractList.iterator();

            while (iterator.hasNext()) {
                ReferralContract activeReferralContract = (ReferralContract) iterator.next();
                if ((activeReferralContract.getExpiryDate().after(currentSchedule.getStartDate())) && (activeReferralContract.getContractDate().before(currentSchedule.getStartDate()))) {
                    referralContract = activeReferralContract;
                    break;
                }
            }
        }
        if (referralContract != null) {
                if (!"ACCEPTED".equals(referralContract.getContractStatus())) {
                    referralContract = null;
                }else{
                    invoice.setReferralContract(referralContract);
                }
            }

        if("Y".equals(schedulingPreference.getShowConvenienceFee())){
            itemId = (System.currentTimeMillis()+currentSchedule.getId());
            String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " +slotType.getName();

            if(invoiceItemDescription.contains("Premium")){
            	invoiceItemDescription = "Premium Consultation Afya Smart Service";
            }else if(invoiceItemDescription.contains("Tele")){
            	invoiceItemDescription = "Tele Consultation Afya Smart Service";
            }else if(invoiceItemDescription.contains("Home")){
            	invoiceItemDescription = "Home Visit Afya Smart Service";
            }else if(invoiceItemDescription.contains("Consultation") && currentSchedule.isFromMobileApp())
            	invoiceItemDescription = "Consultation Afya Smart Service";

            InvoiceItem consultationItem = new InvoiceItem(invoice,itemId.toString(), InvoiceType.OPD_CONSULTATION,invoiceItemDescription, 1, null
                    ,PatientSoapNote.class.getName());
            consultationItem.init(consultationCharges, billingDisplayConfig.getCurrency().getCode(), new Money(consultationCharges,convertTo()),
                    new Money(consultationCharges,convertTo()), 0);
            invoice.addInvoiceItem(consultationItem);

            if(invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
                invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(consultationCharges),convertTo()));
            else
                invoice.setTotalAmount(new com.nzion.domain.product.common.Money(consultationCharges,convertTo()));

            InvoiceItem convenience = new InvoiceItem(invoice,itemId.toString(), InvoiceType.OPD_VALUE_ADDED,InvoiceType.OPD_VALUE_ADDED.getDescription(), 1, null
                    ,PatientSoapNote.class.getName());
            convenience.init(convenienceFee, billingDisplayConfig.getCurrency().getCode(), new Money(convenienceFee,convertTo()),
                    new Money(convenienceFee,convertTo()), 0);

            if (UtilValidator.isNotEmpty(slotType.getId())) {
                updateReferralAmountForService(invoice, consultationItem, referralContract, slotType.getId().toString(), consultationCharges);
                if (referral != null) {
                    invoice.setReferralConsultantId(referral.getId());
                    invoice.setReferralDoctorFirstName(currentSchedule.getReferralDoctorFirstName());
                }
            }

            invoice.addInvoiceItem(convenience);

            if(invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
                invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(convenienceFee),convertTo()));
            else
                invoice.setTotalAmount(new com.nzion.domain.product.common.Money(convenienceFee,convertTo()));
        }else{
            itemId = (System.currentTimeMillis()+currentSchedule.getId());
            String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " +slotType.getName();

            if(invoiceItemDescription.contains("Premium")){
            	invoiceItemDescription = "Premium Consultation Afya Smart Service";
            }else if(invoiceItemDescription.contains("Tele")){
            	invoiceItemDescription = "Tele Consultation Afya Smart Service";
            }else if(invoiceItemDescription.contains("Home")){
            	invoiceItemDescription = "Home Visit Afya Smart Service";
            }else if(invoiceItemDescription.contains("Consultation") && currentSchedule.isFromMobileApp())
            	invoiceItemDescription = "Consultation Afya Smart Service";

            InvoiceItem consultationItem = new InvoiceItem(invoice,itemId.toString(), InvoiceType.OPD_CONSULTATION,invoiceItemDescription, 1, null
                    ,PatientSoapNote.class.getName());
            consultationItem.init(totalCosultFeeConvFee, billingDisplayConfig.getCurrency().getCode(), new Money(totalCosultFeeConvFee, convertTo()),
                    new Money(totalCosultFeeConvFee, convertTo()), 0);

            if (UtilValidator.isNotEmpty(slotType.getId())) {
                updateReferralAmountForService(invoice, consultationItem, referralContract, slotType.getId().toString(), totalCosultFeeConvFee);
                if (referral != null) {
                    invoice.setReferralConsultantId(referral.getId());
                    invoice.setReferralDoctorFirstName(currentSchedule.getReferralDoctorFirstName());
                }
            }

            invoice.addInvoiceItem(consultationItem);

            if(invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
                invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(totalCosultFeeConvFee),convertTo()));
            else
                invoice.setTotalAmount(new com.nzion.domain.product.common.Money(totalCosultFeeConvFee,convertTo()));
        }

        invoice.setMobileOrPatinetPortal(true);

        Invoice inv = commonCrudService.save(invoice);
        depositAdvAmount(inv);
        addTxnPaymentItem(inv);
        return inv;

    }
    void updateReferralAmountForService(Invoice invoice, InvoiceItem item, ReferralContract referralContract, String serviceId, BigDecimal amount) {
        if (referralContract == null)
            return;
        com.nzion.domain.ReferralContractService referralContractService = commonCrudService.findUniqueByEquality(com.nzion.domain.ReferralContractService.class, new String[] { "serviceCode", "referralContract.id" }, new Object[] { new Integer(serviceId), referralContract.getId() });
        if (referralContractService != null) {
            if (referralContract.getPaymentMode().equals(ReferralContract.PAYMENT_MODE_ENUM.PERCENTAGE_SERVICE_ITEM.toString())) {
                BigDecimal percentage = new BigDecimal(referralContractService.getPaymentPercentage());
                BigDecimal referralAmount = amount.multiply(percentage).divide(new BigDecimal(100.0));
                referralAmount = referralAmount.setScale(3, RoundingMode.HALF_UP);
                item.setReferral_amountTobePaid(referralAmount);
                if (invoice.getTotalReferralAmountTobePaid() != null)
                    invoice.setTotalReferralAmountTobePaid(invoice.getTotalReferralAmountTobePaid().add(referralAmount));
                else
                    invoice.setTotalReferralAmountTobePaid(referralAmount);
            } else if (referralContract.getPaymentMode().equals(ReferralContract.PAYMENT_MODE_ENUM.FIX_AMOUNT_PER_SERVICE.toString())) {
                BigDecimal paymentAmount = new BigDecimal(referralContractService.getPaymentAmount());
                item.setReferral_amountTobePaid(paymentAmount);
                if (invoice.getTotalReferralAmountTobePaid() != null)
                    invoice.setTotalReferralAmountTobePaid(invoice.getTotalReferralAmountTobePaid().add(paymentAmount));
                else
                    invoice.setTotalReferralAmountTobePaid(paymentAmount);
            }else if(referralContract.getPaymentMode().equals(ReferralContract.PAYMENT_MODE_ENUM.PERCENTAGE_OF_BILL.toString()) ){
                BigDecimal percentage = new BigDecimal(referralContract.getPercentageOnBill());
                BigDecimal referralAmount = amount.multiply(percentage).divide(new BigDecimal(100.0));
                referralAmount = referralAmount.setScale(3, RoundingMode.HALF_UP);
                item.setReferral_amountTobePaid(referralAmount);
                if (invoice.getTotalReferralAmountTobePaid() != null)
                    invoice.setTotalReferralAmountTobePaid(invoice.getTotalReferralAmountTobePaid().add(referralAmount));
                else
                    invoice.setTotalReferralAmountTobePaid(referralAmount);
            }
        }
    }

    private BigDecimal updatePrice(){
        RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
        Set<SchedulingPreference> schedulingPreferences = new HashSet<>(commonCrudService.findByEquality(SchedulingPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        consultationCharges = getProviderPrice(((Provider)currentSchedule.getPerson()),currentSchedule.getPatient());
        BigDecimal convFee = BigDecimal.ZERO;
        BigDecimal advAmount = BigDecimal.ZERO;
        BigDecimal leadTime = BigDecimal.ZERO;
        for(SchedulingPreference schedulingPreference : schedulingPreferences){
            if(slotType.getName().equals("Premium Visit")){
                if( RCMVisitType.PREMIUM_APPOINTMENT.equals(schedulingPreference.getVisitType()) ){
                    convFee = schedulingPreference.getConvenienceFee();
                    displayConvenienceFee = "Y".equals(schedulingPreference.getShowConvenienceFee());
                    if(UtilValidator.isEmpty(convFee)){
                        convFee = percentage(consultationCharges,schedulingPreference.getConvenienceFeePercent());
                    }
                    advAmount = schedulingPreference.getAdvanceAmount();
                    if(UtilValidator.isEmpty(advAmount)){
                        advAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
                    }
                    leadTime = schedulingPreference.getLeadTimeAllowed();
                }
            }
            if(slotType.getName().equals("Home Visit")){
                if( RCMVisitType.HOME_VISIT_APPOINTMENT.equals(schedulingPreference.getVisitType()) ){
                    convFee = schedulingPreference.getConvenienceFee();
                    displayConvenienceFee = "Y".equals(schedulingPreference.getShowConvenienceFee());
                    if(UtilValidator.isEmpty(convFee)){
                        convFee = percentage(consultationCharges,schedulingPreference.getConvenienceFeePercent());
                    }
                    advAmount = schedulingPreference.getAdvanceAmount();
                    if(UtilValidator.isEmpty(advAmount)){
                        advAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
                    }
                    leadTime = schedulingPreference.getLeadTimeAllowed();
                }
            }
            if(slotType.getName().equals("Tele Consultation Visit")){
                if( RCMVisitType.TELE_CONSULT_APPOINTMENT.equals(schedulingPreference.getVisitType()) ){
                    convFee = schedulingPreference.getConvenienceFee();
                    displayConvenienceFee = "Y".equals(schedulingPreference.getShowConvenienceFee());
                    if(UtilValidator.isEmpty(convFee)){
                        convFee = percentage(consultationCharges,schedulingPreference.getConvenienceFeePercent());
                    }
                    advAmount = schedulingPreference.getAdvanceAmount();
                    if(UtilValidator.isEmpty(advAmount)){
                        advAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
                    }
                    leadTime = schedulingPreference.getLeadTimeAllowed();
                }
            }

            if(slotType.getName().equals("Consult Visit") && currentSchedule.isRequestForAppointment()){
                if( RCMVisitType.CONSULT_VISIT.equals(schedulingPreference.getVisitType()) ){
                    convFee = schedulingPreference.getConvenienceFee();
                    displayConvenienceFee = "Y".equals(schedulingPreference.getShowConvenienceFee());
                    if(UtilValidator.isEmpty(convFee)){
                        convFee = percentage(consultationCharges,schedulingPreference.getConvenienceFeePercent());
                    }
                    advAmount = schedulingPreference.getAdvanceAmount();
                    if(UtilValidator.isEmpty(advAmount)){
                        advAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
                    }
                    leadTime = schedulingPreference.getLeadTimeAllowed();
                }
            }

        }
        List<Invoice> invoices = billingService.getFirstInvoice(currentSchedule.getPatient());
        if(UtilValidator.isEmpty(invoices)){
            BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
            registrationCharges = billingDisplayConfig.getRegistrationFee();
        }
        convenienceFee = convFee;
        registrationCharges = registrationCharges == null ? BigDecimal.ZERO : registrationCharges;
        totalAmount = consultationCharges.add(convenienceFee).add(registrationCharges);
        totalAdvAmount = advAmount.add(convenienceFee);
        return leadTime;
    }

    public void depositAdvAmount(Invoice inv){
        PatientDeposit patientDeposit = new PatientDeposit();
        patientDeposit.setStatus("Deposit");
        patientDeposit.setDepositAmount(totalAdvAmount);
        patientDeposit.setDepositDate(new Date());
        patientDeposit.setDepositMode("CASH");
        patientDeposit.setPatient(currentSchedule.getPatient());
        patientDeposit.setConvenienceFeeForPatientPortal(convenienceFee);
        patientDeposit.setCreatedUser("Online Payment");
        patientDeposit.setTotalAvailableAmount( patientDeposit.getDepositAmount().add(calculateAmount(patientDeposit.getPatient())) );
        patientDeposit.setInvoice(inv);
        patientDeposit.setSchedule(currentSchedule);
        patientDeposit.setPaymentId(currentSchedule.getPaymentId());
        commonCrudService.save(patientDeposit);
        billingService.updatePatientDeposit(patientDeposit.getPatient(),patientDeposit.getDepositAmount());
    }

    public void addTxnPaymentItem(Invoice invoice) {
        InvoicePayment invoicePayment = new InvoicePayment();
        invoicePayment.setPaymentDate(new Date());
        Enumeration enumeration = commonCrudService.findUniqueByEquality(Enumeration.class, new String[]{"enumCode","enumType"}, new Object[]{"ADVANCE_AMOUNT","PAYMENT_MODE"});
        invoicePayment.setPaymentMethod(enumeration);
        invoicePayment.setAmount(new Money(totalAdvAmount));
        String enumCode = invoicePayment.getPaymentMethod().getEnumCode();
        if (enumCode.equals("ADVANCE_AMOUNT")){
            invoicePayment.setPaymentType(PaymentType.OPD_ADVANCE_AMOUNT);
            billingService.updatePatientWithdraw(invoice.getPatient(),invoicePayment.getAmount().getAmount());
        }
        invoice.setInvoiceStatus(InvoiceStatusItem.INPROCESS.toString());
        invoice.addInvoicePayment(invoicePayment);
        invoice.getCollectedAmount().setAmount(invoice.getCollectedAmount().getAmount().add(invoicePayment.getAmount().getAmount()));
        commonCrudService.save(invoice);
        receivePayment(invoice);

    }

    public void receivePayment(Invoice invoice){
        if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount())) {
            if ((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == 1)) {
                return;
            }
            if (((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == -1))){
                return;
            }
        }
        invoice.setInvoiceStatus(InvoiceStatusItem.RECEIVED.toString());
        try{
            UserLogin login = Infrastructure.getUserLogin();
            invoice.setCollectedByUser(login.getUsername());
        }catch(Exception e){

        }
        billingService.saveInvoiceStatus(invoice, InvoiceStatusItem.RECEIVED);
    }

    private BigDecimal calculateAmount(Patient patient){
        BigDecimal advanceAmount = BigDecimal.ZERO;
        List<AcctgTransactionEntry> accTransDebit = commonCrudService.findByEquality(AcctgTransactionEntry.class, new String[]{"patientId","transactionType","debitOrCredit"}, new Object[]{patient.getId().toString(),AcctgTransTypeEnum.PATIENT_DEPOSIT,DebitCreditEnum.DEBIT});
        List<AcctgTransactionEntry> accTransCredit = commonCrudService.findByEquality(AcctgTransactionEntry.class, new String[]{"patientId","transactionType","debitOrCredit"}, new Object[]{patient.getId().toString(),AcctgTransTypeEnum.PATIENT_WITHDRAW,DebitCreditEnum.CREDIT});
        BigDecimal debitAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        BigDecimal creditAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        for(AcctgTransactionEntry acc : accTransDebit){
            if(acc.getAmount() != null)
                debitAmount = debitAmount.add(acc.getAmount());
        }
        for(AcctgTransactionEntry acc : accTransCredit){
            if(acc.getAmount() != null)
                creditAmount = creditAmount.add(acc.getAmount());
        }
        if(debitAmount.compareTo(BigDecimal.ZERO) > 0)
            advanceAmount = debitAmount.subtract(creditAmount);
        return advanceAmount;
    }

    private BigDecimal getProviderPrice(Provider provider, Patient patient){
        BigDecimal amount = BigDecimal.ZERO;

        String patientCategory = "01";
        String tariffCategory = "00";

        String visitType = slotType != null ? slotType.getId().toString() : "10005";
        Map<String,Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null,  visitType,
                provider.getId().toString(), tariffCategory, patientCategory, new Date());
        if(UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null )
            amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
        return amount;
    }

    public Currency convertTo(){
        BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
        String currency = billingDisplayConfig.getCurrency().getCode();
        Currency defaultCurrency = Currency.getInstance(currency);
        return defaultCurrency;
    }

    public static BigDecimal percentage(BigDecimal base, BigDecimal pct){
        if(UtilValidator.isEmpty(base) || UtilValidator.isEmpty(pct))
            return BigDecimal.ZERO;
        return base.multiply(pct).divide(new BigDecimal("100"));
    }


    public Set<SlotAvailability> getAvailableSlots(String providerId, String appointmentDateInString) throws ParseException {
        Date appointmentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(appointmentDateInString);
        Person provider = personRepository.getPersonById(Long.valueOf(providerId));
        ScheduleSearchValueObject searchObject = new ScheduleSearchValueObject(false);
        //location = (Location)Sessions.getCurrent().getAttribute("_location");
        Weekdays weekdays = Weekdays.allSelectedWeekdays();
        //searchObject.setLocation(location);
        searchObject.setPerson(provider);
        searchObject.setFromDate(appointmentDate);
        searchObject.setThruDate(appointmentDate);
        return scheduleService.searchAvailableSchedules(searchObject, weekdays);
    }

    public Set<String> getTimeSlot(Set<SlotAvailability> timeslots){
        Set<String> slots = new LinkedHashSet<>();
        StringBuilder buffer;
        if(timeslots.size() > 0) {
            for (SlotAvailability slotAvailability : timeslots) {
                buffer = new StringBuilder();
                CalendarSlot slot = slotAvailability.getSlot();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                try {
                    if (new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(UtilDateTime.format(slotAvailability.getOn(), new SimpleDateFormat("yyyy-MM-dd")) + " " + sdf.format(slot.getStartTime())).before(new Date())) {
                        continue;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                buffer.append(sdf.format(slot.getStartTime()));
                buffer.append(" - ");
                buffer.append(sdf.format(slot.getEndTime()));
                slots.add(buffer.toString());
            }
        }
        return slots;
    }

    Date convertGivenDate(Date date){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(date);
        calendar.set(1970,0,1);
        return calendar.getTime();
    }

    public Patient checkIfPatientAlreadyExistOrPersist(BookAppointmentDto bookAppointmentDto, String tenantId, HttpServletResponse response) throws IOException {
        Patient oldPatientList  = null;
        List<Patient> patientList = commonCrudService.findByEquality(Patient.class, new String[]{"firstName", "lastName", "contacts.mobileNumber", "dateOfBirth"}, new Object[]{bookAppointmentDto.getFirstName(), bookAppointmentDto.getLastName(), bookAppointmentDto.getMobileNumber(), bookAppointmentDto.getDateOfBirth()});
        if(patientList.size() > 0)
            oldPatientList = patientList.get(0);
        if(oldPatientList != null){
            if(oldPatientList.getContacts() != null) {
                oldPatientList.getContacts().setEmail(bookAppointmentDto.getEmailId());
            }
            if ((oldPatientList.getLanguage() == null) || (!oldPatientList.getLanguage().getEnumCode().equals(bookAppointmentDto.getPreferredLanguage()))){
                Enumeration enumeration = commonCrudService.findUniqueByEquality(Enumeration.class, new String[]{"enumType", "enumCode"}, new Object[]{"LANGUAGE", bookAppointmentDto.getPreferredLanguage()});
                oldPatientList.setLanguage(enumeration);
                patientRepository.merge(oldPatientList);
            }
            return oldPatientList;
        } else {
            Enumeration gender = getGenderEnumerationForPatient(bookAppointmentDto.getGender());
            if(gender == null){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "gender cannot be null");
                return null;
            }
            Patient patient = new Patient();
            patient.setFirstName(bookAppointmentDto.getFirstName());
            patient.setLastName(bookAppointmentDto.getLastName());
            patient.setPatientType("CASH PAYING");
            patient.setDateOfBirth(bookAppointmentDto.getDateOfBirth());
            ContactFields contactFields = new ContactFields();
            contactFields.setMobileNumber(bookAppointmentDto.getMobileNumber());
            contactFields.setEmail(bookAppointmentDto.getEmailId());
            patient.setContacts(contactFields);
            patient.setGender(gender);
            String afyaId = RestServiceConsumer.checkIfPatientExistInPortalAndCreateIfNotExist(patient, tenantId);
            patient.setAfyaId(afyaId);
            patient.setCivilId(bookAppointmentDto.getCivilId());
            patient.setNotificationRequired("YES");

            Enumeration enumeration = commonCrudService.findUniqueByEquality(Enumeration.class, new String[]{"enumType", "enumCode"}, new Object[]{"LANGUAGE", bookAppointmentDto.getPreferredLanguage()});

            patient.setLanguage(enumeration);
            patient = commonCrudService.save(patient);
            fileBasedServiceImpl.createDefaultFolderStructure(patient);
            return patient;
        }
    }

    private Enumeration getGenderEnumerationForPatient(String gender) {
        List<Enumeration> emEnumerations = enumerationServiceImpl.getGeneralEnumerationsByType("GENDER");
        for(Enumeration enumeration : emEnumerations){
            if(enumeration.getDescription().equals(gender))
                return enumeration;
        }
        return null;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    private boolean validateDate(Date appointmentDate){
        LocalDate localDate = new LocalDate(appointmentDate);
        localDate = new LocalDate(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth());
        LocalDate currentDate = new LocalDate();
        currentDate = new LocalDate(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth());
        return localDate.isBefore(currentDate);
    }

    private class SlotWithVisitType{
        private String time;
        private String visitType;
        public SlotWithVisitType(String time, String visitType){
            this.time = time;
            this.visitType = visitType;
        }

        @Override
        public String toString() {
            return "{" +
                    "\"time\" : \"" + time +
                    "\", \"visitType\" : \"" + visitType +
                    "\"}";
        }
    }
    boolean openScheduleAlreadyExist(List<Schedule> schedules) {
        for (Schedule schedule : schedules) {
            if(schedule.getVisitType().getDescription().equals("Consult Visit")) {
                if (!schedule.getStatus().equals(com.nzion.domain.Schedule.STATUS.SOAPSIGNEDOUT) ||
                        !schedule.getStatus().equals(com.nzion.domain.Schedule.STATUS.CANCELLED) || !schedule.getStatus().equals(com.nzion.domain.Schedule.STATUS.CHECKEDOUT)
                        || !schedule.getStatus().equals(com.nzion.domain.Schedule.STATUS.NOSHOW)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    public void calculateInvoiceAmount(Invoice invoice){
        BillingController billingController = new BillingController(invoice);

        if (invoice != null)
            billingController.extractInvoiceToDisplay(invoice);
        if (billingController.getInvoicePayment().getAmount().getAmount() != null)
            billingController.buildRemainingAmount();

        billingController.getCopaymentAndInsurancePayment();
        billingController.calculatePatientAdvanceAmount();

        if( "CORPORATE".equals(invoice.getPatient().getPatientType()) ){
            billingController.setCorporateOrPatient(true);
            billingController.setCorporate(true);
            billingController.setTariffCategorys(billingController.getPatientService().getTariffCategoryByPatientCategory("CASH PAYING"));
        }else if("CASH PAYING".equals(invoice.getPatient().getPatientType())){
            billingController.setCorporateOrPatient(true);
            billingController.setCorporate(false);
            billingController.setTariffCategorys(billingController.getPatientService().getTariffCategoryByPatientCategory("CASH PAYING"));
        }else{
            billingController.setCorporateOrPatient(false);
            billingController.setCorporate(false);
        }

        billingController.calculateCashInsuranceCorporateAmount();

        invoice.setPatientPayable(billingController.getCoPayment());
        invoice.setInsurancePayable(billingController.getInsurancePayment());
        commonCrudService.save(invoice);

    }

	public PersonRepository getPersonRepository() {
		return personRepository;
	}

	public void setPersonRepository(PersonRepository personRepository) {
		this.personRepository = personRepository;
	}

	public ScheduleService getScheduleService() {
		return scheduleService;
	}

	public void setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	public PatientRepository getPatientRepository() {
		return patientRepository;
	}

	public void setPatientRepository(PatientRepository patientRepository) {
		this.patientRepository = patientRepository;
	}

	public PracticeRepository getPracticeRepository() {
		return practiceRepository;
	}

	public void setPracticeRepository(PracticeRepository practiceRepository) {
		this.practiceRepository = practiceRepository;
	}

	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public NotificationTaskExecutor getNotificationTaskExecutor() {
		return notificationTaskExecutor;
	}

	public void setNotificationTaskExecutor(NotificationTaskExecutor notificationTaskExecutor) {
		this.notificationTaskExecutor = notificationTaskExecutor;
	}

	public EnumerationServiceImpl getEnumerationServiceImpl() {
		return enumerationServiceImpl;
	}

	public void setEnumerationServiceImpl(EnumerationServiceImpl enumerationServiceImpl) {
		this.enumerationServiceImpl = enumerationServiceImpl;
	}

	public BillingService getBillingService() {
		return billingService;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public FileBasedServiceImpl getFileBasedServiceImpl() {
		return fileBasedServiceImpl;
	}

	public void setFileBasedServiceImpl(FileBasedServiceImpl fileBasedServiceImpl) {
		this.fileBasedServiceImpl = fileBasedServiceImpl;
	}

	public void createDataForSms(Schedule currentSchedule, Map<String, Object> clinicDetails, Map adminUserLogin, InputStreamSource inputStream){
        try {
            ArrayList<HashMap<String, Object>> adminList = AfyaServiceConsumer.getAllAdminByTenantId();


            if (currentSchedule.getVisitType().getName().equals("Premium Visit")) {
                //sms to doctor
                clinicDetails.put("key", TemplateNames.PREMIUM_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR.name());
                clinicDetails.put("forDoctor", new Boolean(true));
                clinicDetails.put("forAdmin", new Boolean(false));
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                //sms to admin
                List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
                if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                    clinicDetails.put("key", TemplateNames.PREMIUM_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR.name());
                    clinicDetails.put("forDoctor", new Boolean(false));
                    clinicDetails.put("forAdmin", new Boolean(true));
                    clinicDetails.put("isdCode", Infrastructure.getPractice().getAdminUserLogin().getPerson().getContacts().getIsdCode());

                    Iterator iterator = adminList.iterator();
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();

                        clinicDetails.put("mobileNumber", map.get("mobile_number"));
                        clinicDetails.put("languagePreference", map.get("languagePreference"));

                        SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                    }
                }

                //sms to patient
                clinicDetails.put("forDoctor", new Boolean(false));
                clinicDetails.put("forAdmin", new Boolean(false));
                clinicDetails.put("key", TemplateNames.PREMIUM_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_PATIENT.name());
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);

                //for email
                if (currentSchedule.getPatient().getLanguage() != null) {
                    clinicDetails.put("languagePreference", currentSchedule.getPatient().getLanguage().getEnumCode());
                }
                clinicDetails.put("afyaId", currentSchedule.getPatient().getAfyaId());
                clinicDetails.put("firstName", currentSchedule.getPatient().getFirstName());
                clinicDetails.put("lastName", currentSchedule.getPatient().getLastName());
                clinicDetails.put("subject", "Premium Appointment Successful Payment");
                clinicDetails.put("template", "PREMIUM_APPOINTMENT_SUCCESS_PAYMENT_EMAIL_FOR_PATIENT");
                clinicDetails.put("email", currentSchedule.getPatient().getContacts().getEmail());
                clinicDetails.put("stream", inputStream);
                clinicDetails.put("attachment", new Boolean(true));
                clinicDetails.put("patient", currentSchedule.getPatient());
                EmailUtil.sendNetworkContractStatusMail(clinicDetails);
            } else if (currentSchedule.getVisitType().getName().equals("Tele Consultation Visit")) {
                //sms to doctor
                clinicDetails.put("key", TemplateNames.TELECONSULTATION_VISIT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR.name());
                clinicDetails.put("forDoctor", new Boolean(true));
                clinicDetails.put("forAdmin", new Boolean(false));
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                //sms to admin
                List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
                if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                    clinicDetails.put("key", TemplateNames.TELECONSULTATION_VISIT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR.name());
                    clinicDetails.put("forDoctor", new Boolean(false));
                    clinicDetails.put("forAdmin", new Boolean(true));
                    clinicDetails.put("isdCode", Infrastructure.getPractice().getAdminUserLogin().getPerson().getContacts().getIsdCode());

                    Iterator iterator = adminList.iterator();
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();

                        clinicDetails.put("mobileNumber", map.get("mobile_number"));
                        clinicDetails.put("languagePreference", map.get("languagePreference"));

                        SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                    }
                }
                //sms to patient
                clinicDetails.put("key", TemplateNames.TELECONSULTATION_VISIT_SUCCESS_PAYMENT_SMS_FOR_PATIENT.name());
                clinicDetails.put("forDoctor", new Boolean(false));
                clinicDetails.put("forAdmin", new Boolean(false));
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);

                //for email
                if (currentSchedule.getPatient().getLanguage() != null) {
                    clinicDetails.put("languagePreference", currentSchedule.getPatient().getLanguage().getEnumCode());
                }
                clinicDetails.put("afyaId", currentSchedule.getPatient().getAfyaId());
                clinicDetails.put("firstName", currentSchedule.getPatient().getFirstName());
                clinicDetails.put("lastName", currentSchedule.getPatient().getLastName());
                clinicDetails.put("subject", "Tele Consultation Appointment Successful Payment");
                clinicDetails.put("template", "TELECONSULTATION_APPOINTMENT_SUCCESS_PAYMENT_EMAIL_FOR_PATIENT");
                clinicDetails.put("email", currentSchedule.getPatient().getContacts().getEmail());
                clinicDetails.put("stream", inputStream);
                clinicDetails.put("attachment", new Boolean(true));
                clinicDetails.put("patient", currentSchedule.getPatient());
                EmailUtil.sendNetworkContractStatusMail(clinicDetails);
            } else if (currentSchedule.getVisitType().getName().equals("Home Visit")) {
                //sms to doctor
                clinicDetails.put("key", TemplateNames.HOME_VISIT_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR.name());
                clinicDetails.put("forDoctor", new Boolean(true));
                clinicDetails.put("forAdmin", new Boolean(false));
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                //sms to admin
                List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
                if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                    clinicDetails.put("key", TemplateNames.HOME_VISIT_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_DOCTOR.name());
                    clinicDetails.put("forDoctor", new Boolean(false));
                    clinicDetails.put("forAdmin", new Boolean(true));
                    clinicDetails.put("isdCode", Infrastructure.getPractice().getAdminUserLogin().getPerson().getContacts().getIsdCode());

                    Iterator iterator = adminList.iterator();
                    while (iterator.hasNext()){
                        Map map = (Map)iterator.next();

                        clinicDetails.put("mobileNumber", map.get("mobile_number"));
                        clinicDetails.put("languagePreference", map.get("languagePreference"));

                        SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                    }
                }

                //sms to patient
                clinicDetails.put("key", TemplateNames.HOME_VISIT_APPOINTMENT_SUCCESS_PAYMENT_SMS_FOR_PATIENT.name());
                clinicDetails.put("forDoctor", new Boolean(false));
                clinicDetails.put("forAdmin", new Boolean(false));
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);

                //for email
                if (currentSchedule.getPatient().getLanguage() != null) {
                    clinicDetails.put("languagePreference", currentSchedule.getPatient().getLanguage().getEnumCode());
                }
                clinicDetails.put("afyaId", currentSchedule.getPatient().getAfyaId());
                clinicDetails.put("firstName", currentSchedule.getPatient().getFirstName());
                clinicDetails.put("lastName", currentSchedule.getPatient().getLastName());
                clinicDetails.put("subject", "Home Visit Appointment Successful Payment");
                clinicDetails.put("template", "HOME_VISIT_APPOINTMENT_SUCCESS_PAYMENT_EMAIL_FOR_PATIENT");
                clinicDetails.put("email", currentSchedule.getPatient().getContacts().getEmail());
                clinicDetails.put("stream", inputStream);
                clinicDetails.put("attachment", new Boolean(true));
                clinicDetails.put("patient", currentSchedule.getPatient());
                EmailUtil.sendNetworkContractStatusMail(clinicDetails);

            } else if (currentSchedule.getVisitType().getName().equals("Consult Visit")) {
                //sms to doctor
                clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_SCHEDULE_FOR_DOCTOR.name());
                clinicDetails.put("forDoctor", new Boolean(true));
                clinicDetails.put("forAdmin", new Boolean(false));
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                //sms to admin
                List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
                if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                    clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_SCHEDULE_FOR_DOCTOR.name());
                    clinicDetails.put("forDoctor", new Boolean(false));
                    clinicDetails.put("forAdmin", new Boolean(true));
                    clinicDetails.put("isdCode", Infrastructure.getPractice().getAdminUserLogin().getPerson().getContacts().getIsdCode());

                    Iterator iterator = adminList.iterator();
                    while (iterator.hasNext()) {
                        Map map = (Map) iterator.next();

                        clinicDetails.put("mobileNumber", map.get("mobile_number"));
                        clinicDetails.put("languagePreference", map.get("languagePreference"));

                        SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                    }
                }
                //sms to patient
                clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_SCHEDULE_FOR_PATIENT.name());
                clinicDetails.put("forDoctor", new Boolean(false));
                clinicDetails.put("forAdmin", new Boolean(false));
                clinicDetails.put("afyaId", currentSchedule.getPatient().getAfyaId());
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
