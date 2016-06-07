package com.nzion.service.patient;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nzion.domain.*;
import com.nzion.domain.Enumeration;
import com.nzion.repository.ScheduleRepository;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.util.*;
import org.joda.time.LocalDate;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nzion.domain.AfyaClinicDeposit.ClinicDepositType;
import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.billing.DebitCreditEnum;
import com.nzion.domain.billing.Invoice;
import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.billing.InvoicePayment;
import com.nzion.domain.billing.InvoiceStatusItem;
import com.nzion.domain.billing.InvoiceType;
import com.nzion.domain.billing.PaymentType;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.PracticeRepository;
import com.nzion.repository.notifier.utility.NotificationTaskExecutor;
import com.nzion.service.ScheduleService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.dto.ReschedulePatientAppointmentDto;


public class UpdatePatientAppointmentServlet extends HttpServlet {

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    CommonCrudService commonCrudService;
    @Autowired
    NotificationTaskExecutor notificationTaskExecutor;
    @Autowired
    PracticeRepository practiceRepository;
    @Autowired
    BillingService billingService;
    
    private BigDecimal totalAdvAmount;
    
    private BigDecimal convenienceFee;

    Date existingStartDate = null;
    Date existingStartTime = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tenantId = request.getParameter("clinicId");
        if(tenantId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ClinicId cannot be null");
            return;
        }
        TenantIdHolder.setTenantId(tenantId);
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        objectMapper.setDateFormat(df);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        ReschedulePatientAppointmentDto reschedulePatientAppointmentDto = objectMapper.readValue(request.getInputStream(), ReschedulePatientAppointmentDto.class);
        if(reschedulePatientAppointmentDto.getScheduleId() == null || reschedulePatientAppointmentDto.getScheduleId().equals("") || reschedulePatientAppointmentDto.getAppointmentStartDate() == null || reschedulePatientAppointmentDto.getAppointmentEndDate() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "appointment details cannot be null");
            return;
        }
        if(validateDate(reschedulePatientAppointmentDto.getAppointmentStartDate()) || validateDate(reschedulePatientAppointmentDto.getAppointmentEndDate())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid date, cannot book appointment from past");
            return;
        }
        Long scheduleId = Long.valueOf(reschedulePatientAppointmentDto.getScheduleId());
        Schedule schedule = scheduleService.getSchedule(scheduleId);
        if (!Schedule.STATUS.SCHEDULED.equals(schedule.getStatus())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A schedule with SCHEDULED status can only be Rescheduled");
            return;
        }
        
        RCMVisitType rcmVisitType = null;
        if(schedule.getVisitType().getName().equals("Premium Visit"))
        	rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Home Visit"))
        	rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Tele Consultation Visit"))
        	rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Consult Visit") && schedule.isFromMobileApp())
          	rcmVisitType = RCMVisitType.CONSULT_VISIT;
        RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
        PatientReschedulingPreference patientReschedulingPreference = commonCrudService.findUniqueByEquality(PatientReschedulingPreference.class, new String[]{"rcmPreference","visitType"}, new Object[]{rcmPreference,rcmVisitType});
        BigDecimal reScheduleTime = patientReschedulingPreference.getReschedulingTime() == null ? BigDecimal.ZERO : patientReschedulingPreference.getReschedulingTime();
        Date scheduleDateTime = UtilDateTime.toDate(schedule.getStartDate().getMonth(), schedule.getStartDate().getDate(), schedule.getStartDate().getYear(),
                schedule.getStartTime().getHours(), schedule.getStartTime().getMinutes(), schedule.getStartTime().getSeconds());
        BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(new Date(), scheduleDateTime));
        
        PrintWriter writer = response.getWriter();
        
		if(hoursInterval.compareTo(reScheduleTime) < 0){
			//writer.print( "Appointment cannot be rescheduled within " + reScheduleTime + " hrs");
            writer.print( "Sorry, appointment cant be rescheduled , please check Afya Policy.");
			writer.close();
            return;
		}

        //new start
        if (schedule != null) {
                existingStartDate = schedule.getStartDate();
                existingStartTime = schedule.getStartTime();
            }
        //new end

        updatePatientRescheduling(schedule);
        Map<String, Object> result = bookAppointment(schedule, reschedulePatientAppointmentDto);
        
        generateInvoice((Schedule)result.get("schedule"));
        
        String status = (String)result.get("status");
        if(status.equals("updated"))
            response.setStatus(HttpServletResponse.SC_OK, "appointment rescheduled");
        if(status.equals("existing"))
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "schedule already exist with given time slot, please create new");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}
    
    private void updatePatientRescheduling(Schedule schedule){
    	List<PatientDeposit> patientDeposits = commonCrudService.findByEquality(PatientDeposit.class, new String[]{"schedule"}, new Object[]{schedule});
    	PatientDeposit patientDeposit = null;
    	if(UtilValidator.isNotEmpty(patientDeposits)){
    		patientDeposit = patientDeposits.get(patientDeposits.size() - 1);
    	}
        RCMVisitType rcmVisitType = null;
        if(schedule.getVisitType().getName().equals("Premium Visit"))
        	rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Home Visit"))
        	rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Tele Consultation Visit"))
        	rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Consult Visit") && schedule.isFromMobileApp())
          	rcmVisitType = RCMVisitType.CONSULT_VISIT;
        PatientReschedulingPreference patientReschedulingPreference = commonCrudService.findUniqueByEquality(PatientReschedulingPreference.class, new String[]{"visitType"}, new Object[]{rcmVisitType});
        if(patientDeposit != null ){
        	BigDecimal depositAmount = patientDeposit.getDepositAmount();
	        BigDecimal patientCancellationCharge = percentage(depositAmount,patientReschedulingPreference.getPatientCancellationChargeProviderPercent());
	        BigDecimal afyaCancellationCharge = percentage(depositAmount,patientReschedulingPreference.getPatientCancellationChargeAfyaPercent());
	        BigDecimal totalCancellationCharges = patientCancellationCharge.add(afyaCancellationCharge);
	        BigDecimal totalRefundAmount = depositAmount.subtract(totalCancellationCharges);
	        //depositAdvAmount(totalRefundAmount, schedule.getPatient());
	        Invoice invoice = patientDeposit.getInvoice();
	        
	        invoice = addToPatientAccount(invoice,totalRefundAmount);
	        
	        AfyaClinicDeposit clinicDeposit = new AfyaClinicDeposit(invoice,patientCancellationCharge,ClinicDepositType.CLINIC );
	        AfyaClinicDeposit afyaClinicDeposit = new AfyaClinicDeposit(invoice,afyaCancellationCharge,ClinicDepositType.AFYA );
	        
	        commonCrudService.save(clinicDeposit);
	        commonCrudService.save(afyaClinicDeposit);
	        
	        
	        invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
	        for(InvoiceItem ii : invoice.getInvoiceItems()){
	        	cancelInvoiceLineItem(invoice,ii);
	        }
	        commonCrudService.save(invoice);
        }
    }
    
    public Invoice addToPatientAccount(Invoice invoice,BigDecimal amount){
    	
    	BigDecimal depositAdvanceAmount = BigDecimal.ZERO;
    	Enumeration paymentMethod = commonCrudService.findUniqueByEquality(Enumeration.class, new String[]{"enumCode"}, new Object[]{"PATIENT_AMOUNT"}); 
    	InvoicePayment invoicePayment = new InvoicePayment(paymentMethod,invoice,new Money(amount),PaymentType.OPD_PATIENT_AMOUNT);
        invoicePayment.setPaymentType(PaymentType.OPD_PATIENT_AMOUNT);
        depositAdvanceAmount = depositAdvanceAmount.add(invoicePayment.getAmount().getAmount().negate());
        
        invoice.addInvoicePayment(invoicePayment);
        invoice.getCollectedAmount().setAmount(invoice.getCollectedAmount().getAmount().add(invoicePayment.getAmount().getAmount()));
        invoice.setAmountRefundedToPatient(true);
        return invoice;

    }
    
    private void cancelInvoiceLineItem(Invoice invoice, InvoiceItem ii){
    	com.nzion.domain.product.common.Money price = invoice.getTotalAmount();
        price.setAmount(price.getAmount().subtract(ii.getPriceValue()));
        invoice.setTotalAmount(price);
        ii.setInvoiceItemStatus("Cancel");
        ii.setFactor(BigDecimal.ZERO);
        ii.setNetPrice(BigDecimal.ZERO);
        ii.setCopayAmount(BigDecimal.ZERO);
        ii.setDeductableAmount(BigDecimal.ZERO);
        ii.setGrossAmount(BigDecimal.ZERO);
        Money money = new Money(BigDecimal.ZERO);
        ii.setPrice(money);
        commonCrudService.save(ii);
        commonCrudService.save(invoice);
    }
    
    
    public void depositAdvAmount(BigDecimal totalCancellationCharges, Patient patient){
    	PatientDeposit patientDeposit = new PatientDeposit();
    	patientDeposit.setStatus("Deposit");
    	patientDeposit.setDepositAmount(totalCancellationCharges);
    	patientDeposit.setDepositDate(new Date());
    	patientDeposit.setDepositMode("CASH");
    	patientDeposit.setPatient(patient);
		//patientDeposit.setCreatedPerson(Infrastructure.getLoggedInPerson());
		patientDeposit.setReturnToPatient(true);
		commonCrudService.save(patientDeposit);
		billingService.updatePatientDeposit(patientDeposit.getPatient(),patientDeposit.getDepositAmount());
    }
    
    
    private boolean validateDate(Date appointmentDate){
        LocalDate localDate = new LocalDate(appointmentDate);
        localDate = new LocalDate(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth());
        LocalDate currentDate = new LocalDate();
        currentDate = new LocalDate(currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth());
        return localDate.isBefore(currentDate);
    }

    private Map<String, Object> bookAppointment(Schedule currentSchedule, ReschedulePatientAppointmentDto reschedulePatientAppointmentDto) {
        Map<String, Object> result = new HashMap<>();
        SlotType slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{reschedulePatientAppointmentDto.getVisitType()});
        if(slotType == null){
            slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{"Consult Visit"});
        }
        currentSchedule.setVisitType(slotType);
        currentSchedule.setStartTime(convertGivenDate(reschedulePatientAppointmentDto.getAppointmentStartDate()));
        currentSchedule.setEndTime(convertGivenDate(reschedulePatientAppointmentDto.getAppointmentEndDate()));
        currentSchedule.setStartDate(com.nzion.util.UtilDateTime.getDayStart(reschedulePatientAppointmentDto.getAppointmentStartDate()));
        currentSchedule.setComments(reschedulePatientAppointmentDto.getNotes());
        List<Schedule> existingSchdules = commonCrudService.findByEquality(Schedule.class, new String[]{"startDate", "startTime", "endTime", "person"}, new Object[]{currentSchedule.getStartDate(), currentSchedule.getStartTime(), currentSchedule.getEndTime(), currentSchedule.getPerson()});
        for(Schedule existingSchdule : existingSchdules){
	        if(existingSchdule != null && !STATUS.CANCELLED.equals(existingSchdule.getStatus())) {
	            result.put("schedule", existingSchdule);
	            result.put("status", "existing");
	            return result;
	        }
        }
        currentSchedule.setSequenceNum(0);
//new code start
        /*Date existingStartDate = null;
        Date existingStartTime = null;
        ScheduleRepository repository = Infrastructure.getSpringBean("scheduleRepository");
        if (currentSchedule.getId() != null) {
            Schedule existSchedule = repository.getSchedule(currentSchedule.getId());
            if(existSchedule != null) {
                existingStartDate = existSchedule.getStartDate();
                existingStartTime = existSchedule.getStartTime();
            }
            repository.evict(existSchedule);
        }*/

//new code end
        Schedule schedule =  scheduleService.updateSchedule(currentSchedule);
        result.put("schedule", schedule);
        result.put("status", "updated");
        try {
            NotificationSetup notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[]{"status"}, new Object[]{NotificationSetup.STATUS.RESCHEDULED});
            String cronExpression = CronExpressionGenerator.generateCronExpressionGivenDateParameters(true, new Date(), notificationSetup.getTriggetPointValue());
            if(notificationSetup.isNotificationRequired() && notificationSetup.isPatientRole()) {
                Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
                if (practice != null) {
                    Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
                    notificationTaskExecutor.prepareDetailsAndNotifyAppointmentRescheduled(schedule, cronExpression, notificationSetup.isByEmail(), notificationSetup.isBySMS(), clinicDetails);
                }
            }
            //new code start for communication loop

            Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
            if (practice != null) {
                Map<String, Object> clinicDet = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
                Map<String, Object> adminUserLogin = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername());
                Object languagePreference = adminUserLogin.get("languagePreference");
                clinicDet.put("languagePreference", languagePreference);
                clinicDet.put("existingStartDate", existingStartDate);
                clinicDet.put("existingStartTime", existingStartTime);
                createDataForSms(currentSchedule, clinicDet, adminUserLogin);
            }
            //new code end for communication loop
        } catch (ClassNotFoundException e) {
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
    
    
    public void generateInvoice(Schedule currentSchedule){
    	BigDecimal consultationCharges = getProviderPrice( ((Provider)currentSchedule.getPerson()),currentSchedule.getPatient(),currentSchedule.getVisitType());
    	RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
    	
    	RCMVisitType rcmVisitType = null;
        if(currentSchedule.getVisitType().getName().equals("Premium Visit"))
        	rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
        if(currentSchedule.getVisitType().getName().equals("Home Visit"))
        	rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
        if(currentSchedule.getVisitType().getName().equals("Tele Consultation Visit"))
        	rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
        if(currentSchedule.getVisitType().getName().equals("Consult Visit") && currentSchedule.isFromMobileApp())
          	rcmVisitType = RCMVisitType.CONSULT_VISIT;
        
        SchedulingPreference schedulingPreference = commonCrudService.findUniqueByEquality(SchedulingPreference.class, 
        		new String[]{"rcmPreference","visitType"}, new Object[]{rcmPreference,rcmVisitType});
        
        convenienceFee = schedulingPreference.getConvenienceFee();
		  if(UtilValidator.isEmpty(convenienceFee)){
			  convenienceFee = percentage(consultationCharges,schedulingPreference.getConvenienceFeePercent());
		  }
		  
		  totalAdvAmount = schedulingPreference.getAdvanceAmount();
		  if(UtilValidator.isEmpty(totalAdvAmount)){
			  totalAdvAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
		  }
		 
		 totalAdvAmount = totalAdvAmount.add(convenienceFee);
        
    	BigDecimal totalCosultFeeConvFee = consultationCharges.add(convenienceFee);
        BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
        Long itemId = (System.currentTimeMillis()+currentSchedule.getId());
        final Invoice invoice = new Invoice(itemId.toString(),PatientSoapNote.class.getName(), (Provider)currentSchedule.getPerson(), 
        		currentSchedule.getPatient(), currentSchedule.getLocation());
        invoice.setInvoiceType(InvoiceType.OPD);
        invoice.setSchedule(currentSchedule);
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
        
        if("Y".equals(schedulingPreference.getShowConvenienceFee())){
        	itemId = (System.currentTimeMillis()+currentSchedule.getId());
        	String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " +currentSchedule.getVisitType().getName();
        	
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
	        invoice.addInvoiceItem(convenience);
	        
	        if(invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
	            invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(convenienceFee),convertTo()));
	        else
	            invoice.setTotalAmount(new com.nzion.domain.product.common.Money(convenienceFee,convertTo()));
        }else{
	        itemId = (System.currentTimeMillis()+currentSchedule.getId());
	        String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " +currentSchedule.getVisitType().getName();
	        
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
	        consultationItem.init(totalCosultFeeConvFee, billingDisplayConfig.getCurrency().getCode(), new Money(totalCosultFeeConvFee,convertTo()),
	        		new Money(totalCosultFeeConvFee,convertTo()), 0);
	        invoice.addInvoiceItem(consultationItem);
	        
	        if(invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
	            invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(totalCosultFeeConvFee),convertTo()));
	        else
	            invoice.setTotalAmount(new com.nzion.domain.product.common.Money(totalCosultFeeConvFee,convertTo()));
        }
        
        invoice.setMobileOrPatinetPortal(currentSchedule.isMobileOrPatinetPortal());
        
        Invoice inv = commonCrudService.save(invoice);
        depositAdvAmount(inv,currentSchedule);
        addTxnPaymentItem(inv);

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
        UserLogin login = Infrastructure.getUserLogin();
        if(UtilValidator.isNotEmpty(login))
            invoice.setCollectedByUser(login.getUsername());
        billingService.saveInvoiceStatus(invoice, InvoiceStatusItem.RECEIVED);
    }
    
    
    public void depositAdvAmount(Invoice inv,Schedule currentSchedule){
    	PatientDeposit patientDeposit = new PatientDeposit();
    	patientDeposit.setStatus("Deposit");
    	patientDeposit.setDepositAmount(totalAdvAmount);
    	patientDeposit.setDepositDate(new Date());
    	patientDeposit.setDepositMode("CASH");
    	patientDeposit.setPatient(currentSchedule.getPatient());
    	patientDeposit.setConvenienceFeeForPatientPortal(convenienceFee);
		patientDeposit.setTotalAvailableAmount( patientDeposit.getDepositAmount().add(calculateAmount(patientDeposit.getPatient())) );
		patientDeposit.setCreatedPerson(Infrastructure.getLoggedInPerson());
		patientDeposit.setInvoice(inv);
		patientDeposit.setSchedule(currentSchedule);
		commonCrudService.save(patientDeposit);
		billingService.updatePatientDeposit(patientDeposit.getPatient(),patientDeposit.getDepositAmount());
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
    
    
    private BigDecimal getProviderPrice(Provider provider, Patient patient, SlotType soapNoteType){
        BigDecimal amount = BigDecimal.ZERO;

    	String patientCategory = "01";
        String tariffCategory = "00";
        
        String visitType = soapNoteType != null ? soapNoteType.getId().toString() : "10005";
        Map<String,Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null,  visitType,
                provider.getId().toString(), tariffCategory, patientCategory, new Date());
        if(UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null )
            amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
        return amount;
    }

    Date convertGivenDate(Date date){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(date);
        calendar.set(1970,0,1);
        return calendar.getTime();
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

    public void createDataForSms(Schedule currentSchedule, Map<String, Object> clinicDetails, Map adminUserLogin){

        ArrayList<HashMap<String, Object>> adminList = AfyaServiceConsumer.getAllAdminByTenantId();

        if(currentSchedule.getVisitType().getName().equals("Premium Visit")) {
            clinicDetails.put("key", TemplateNames.PREMIUM_APPOINTMENT_RESCHEDULE_BY_PATIENT.name());
            clinicDetails.put("forDoctor", new Boolean(true));
            clinicDetails.put("forAdmin", new Boolean(false));
            SmsUtil.sendStatusSms(currentSchedule, clinicDetails);

            //sms to admin
            List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
            if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                clinicDetails.put("key", TemplateNames.PREMIUM_APPOINTMENT_RESCHEDULE_BY_PATIENT.name());
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

        }else if(currentSchedule.getVisitType().getName().equals("Tele Consultation Visit")) {
            clinicDetails.put("key", TemplateNames.TELECONSULTATION_VISIT_RESCHEDULE_BY_PATIENT.name());
            clinicDetails.put("forDoctor", new Boolean(true));
            clinicDetails.put("forAdmin", new Boolean(false));
            SmsUtil.sendStatusSms(currentSchedule, clinicDetails);

            //sms to admin
            List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
            if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                clinicDetails.put("key", TemplateNames.TELECONSULTATION_VISIT_RESCHEDULE_BY_PATIENT.name());
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

        }else if (currentSchedule.getVisitType().getName().equals("Home Visit")) {
            clinicDetails.put("key", TemplateNames.HOME_VISIT_APPOINTMENT_RESCHEDULE_BY_PATIENT.name());
            clinicDetails.put("forDoctor", new Boolean(true));
            clinicDetails.put("forAdmin", new Boolean(false));
            SmsUtil.sendStatusSms(currentSchedule, clinicDetails);

            //sms to admin
            List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
            if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                clinicDetails.put("key", TemplateNames.HOME_VISIT_APPOINTMENT_RESCHEDULE_BY_PATIENT.name());
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
        }else if (currentSchedule.getVisitType().getName().equals("Consult Visit")) {
            //sms to doctor
            clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_RESCHEDULE_BY_PATIENT_SMS_TO_DOCTOR.name());
            clinicDetails.put("forDoctor", new Boolean(true));
            clinicDetails.put("forAdmin", new Boolean(false));
            SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
            //sms to admin
            List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
            if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_RESCHEDULE_BY_PATIENT_SMS_TO_DOCTOR.name());
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
        }
    }

}
