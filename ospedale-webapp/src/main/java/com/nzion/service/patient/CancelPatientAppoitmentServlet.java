package com.nzion.service.patient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nzion.domain.*;
import com.nzion.domain.AfyaClinicDeposit.ClinicDepositType;
import com.nzion.domain.Enumeration;
import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.Schedule.Tentative;
import com.nzion.domain.billing.Invoice;
import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.billing.InvoicePayment;
import com.nzion.domain.billing.InvoiceStatusItem;
import com.nzion.domain.billing.PaymentType;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.notifier.utility.NotificationTaskExecutor;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.service.ScheduleService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.*;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.zkoss.zhtml.Big;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Mohan Sharma on 7/27/2015.
 */
public class CancelPatientAppoitmentServlet extends HttpServlet {

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    CommonCrudService commonCrudService;
    @Autowired
    NotificationTaskExecutor notificationTaskExecutor;
    @Autowired
    BillingService billingService;

    private SlotType slotType;

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
        String scheduleIdString = request.getParameter("scheduleId");
        if(scheduleIdString == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ScheduleId cannot be null");
            return;
        }
        Long scheduleId = Long.valueOf(scheduleIdString);
        Schedule schedule = commonCrudService.getById(Schedule.class, scheduleId);
        if(schedule == null){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Schedule cannot be found by the given Id");
            return;
        }
        if(checkIfScheduleAlreadyCancelled(schedule)){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Schedule already has cancel status");
            return;
        }
        
        
        RCMVisitType rcmVisitType = null;
        if(schedule.getVisitType().getName().equals("Premium Visit"))
        	rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Home Visit"))
        	rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Tele Consultation Visit"))
        	rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
        if( schedule.getVisitType().getName().equals("Consult Visit") && Tentative.Paid.toString().equals(schedule.getTentativeStatus()) )
          	rcmVisitType = RCMVisitType.CONSULT_VISIT;
        
        RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
        PatientCancellationPreference patientCancellationPreference = commonCrudService.findUniqueByEquality(PatientCancellationPreference.class, new String[]{"rcmPreference","visitType"}, new Object[]{rcmPreference,rcmVisitType});
        
        if(patientCancellationPreference != null){
	        BigDecimal cancellationTime = patientCancellationPreference.getCancellationTime() == null ? BigDecimal.ZERO : patientCancellationPreference.getCancellationTime();
	        Date scheduleDateTime = UtilDateTime.toDate(schedule.getStartDate().getMonth(), schedule.getStartDate().getDate(), schedule.getStartDate().getYear(), 
	        		schedule.getStartTime().getHours(),schedule.getStartTime().getMinutes(), schedule.getStartTime().getSeconds());
	        BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(new Date(), scheduleDateTime));
	        
	        PrintWriter writer = response.getWriter();
	        
			if(hoursInterval.compareTo(cancellationTime) < 0 && !Tentative.Tentative.toString().equals(schedule.getTentativeStatus()) ){
				//writer.print( "Appointment cannot be cancelled within " + cancellationTime + " hrs" );
                writer.print( "Sorry, the appointment cannot be cancelled, please check the Afya policy." );
				writer.close();
	            return;
			}
			
	        
	        updatePatientCancellation(schedule);
        }
        
        schedule.setStatus(Schedule.STATUS.CANCELLED);
        schedule = scheduleService.updateSchedule(schedule);
        try {
            /*slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{schedule.getVisitType().getName()});
            if (slotType == null) {
                slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{"Consult Visit"});
            }

            List visitTypeSoapModuleList = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider","slotType"}, new Object[]{schedule.getPerson(),slotType});
            if((UtilValidator.isNotEmpty(visitTypeSoapModuleList)) && (!((VisitTypeSoapModule)visitTypeSoapModuleList.get(0)).isSmartService())) {*/
            if (!schedule.isMobileOrPatinetPortal()){
            if (schedule.getStatus().equals(Schedule.STATUS.CANCELLED)) {
                NotificationSetup notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[]{"status"}, new Object[]{NotificationSetup.STATUS.CANCELLED});
                String cronExpression = CronExpressionGenerator.generateCronExpressionGivenDateParameters(true, new Date(), notificationSetup.getTriggetPointValue());
                if (notificationSetup.isNotificationRequired() && notificationSetup.isPatientRole()) {
                    Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
                    if (practice != null) {
                        Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
                        notificationTaskExecutor.prepareDetailsAndNotifyAppointmentCancelled(schedule, cronExpression, notificationSetup.isByEmail(), notificationSetup.isBySMS(), clinicDetails);
                    }
                }
            }
        }
            response.setStatus(HttpServletResponse.SC_OK, "Schedule cancelled successfully");

            Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
            if (practice != null) {
                Map<String, Object> clinicDet = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
                Map<String, Object> adminUserLogin = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername());
                Object languagePreference = adminUserLogin.get("languagePreference");
                clinicDet.put("languagePreference", languagePreference);
                /*if((UtilValidator.isNotEmpty(visitTypeSoapModuleList)) && (((VisitTypeSoapModule)visitTypeSoapModuleList.get(0)).isSmartService())) {*/
                if (schedule.isMobileOrPatinetPortal()){
                    createDataForSms(schedule, clinicDet, adminUserLogin);
                }
            }
        } catch (ClassNotFoundException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error sending notification");
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error sending notification");
            e.printStackTrace();
        } catch (SchedulerException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error sending notification");
            e.printStackTrace();
        } catch (ParseException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error sending notification");
            e.printStackTrace();
        }
    }

    private boolean checkIfScheduleAlreadyCancelled(Schedule schedule) {
        if(schedule.getStatus().equals(Schedule.STATUS.CANCELLED))
            return true;
        else
            return false;
    }
    
    
    private void updatePatientCancellation(Schedule schedule){
    	PatientDeposit patientDeposit = null;
    	List<PatientDeposit> patientDeposits = commonCrudService.findByEquality(PatientDeposit.class, new String[]{"schedule"}, new Object[]{schedule});
    	if(UtilValidator.isNotEmpty(patientDeposits)){
    		patientDeposit = patientDeposits.get(patientDeposits.size() - 1 );
    	}
        RCMVisitType rcmVisitType = null;
        boolean refundConvFee = false;
        if(schedule.getVisitType().getName().equals("Premium Visit")){
        	rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
        	refundConvFee = true;
        }
        if(schedule.getVisitType().getName().equals("Home Visit")){
        	rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
        	refundConvFee = true;
        }
        if(schedule.getVisitType().getName().equals("Tele Consultation Visit"))
        	rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
        if(schedule.getVisitType().getName().equals("Consult Visit") && schedule.isFromMobileApp()){
          	rcmVisitType = RCMVisitType.CONSULT_VISIT;
          	refundConvFee = true;
        }
        PatientCancellationPreference patientCancellationPreference = commonCrudService.findUniqueByEquality(PatientCancellationPreference.class, new String[]{"visitType"}, new Object[]{rcmVisitType});
        if(patientDeposit == null || patientCancellationPreference == null)
        	return;
        
        BigDecimal depositAmount = BigDecimal.ZERO;
    	if( refundConvFee ){
    		depositAmount = patientDeposit.getDepositAmount();
    	}else{
    		depositAmount = patientDeposit.getDepositAmount().subtract(patientDeposit.getConvenienceFeeForPatientPortal());
    	}
        
        BigDecimal patientCancellationCharge = percentage(depositAmount,patientCancellationPreference.getPatientCancellationChargePercent());
        BigDecimal afyaCancellationCharge = percentage(depositAmount,patientCancellationPreference.getPatientCancelationChargeAfyePercent());
        BigDecimal totalCancellationCharges = patientCancellationCharge.add(afyaCancellationCharge);
        BigDecimal totalRefundAmount = depositAmount.subtract(totalCancellationCharges);
        depositAdvAmount(totalRefundAmount, schedule.getPatient());
        Invoice invoice = patientDeposit.getInvoice();
        
        invoice = addToPatientAccount(invoice,totalRefundAmount);
        
        AfyaClinicDeposit clinicDeposit = new AfyaClinicDeposit(invoice,patientCancellationCharge,ClinicDepositType.CLINIC );
        AfyaClinicDeposit afyaClinicDeposit = new AfyaClinicDeposit(invoice,afyaCancellationCharge,ClinicDepositType.AFYA );
        
        commonCrudService.save(clinicDeposit);
        commonCrudService.save(afyaClinicDeposit);
        
        invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
        for(InvoiceItem ii : invoice.getInvoiceItems()){
        	ii.setInvoiceItemStatus("Cancel");
        	commonCrudService.save(ii);
        }
        commonCrudService.save(invoice);
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
    
    
    public static BigDecimal percentage(BigDecimal base, BigDecimal pct){
        if(UtilValidator.isEmpty(base) || UtilValidator.isEmpty(pct))
            return BigDecimal.ZERO;
        return base.multiply(pct).divide(new BigDecimal("100"));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
    public void createDataForSms(Schedule currentSchedule, Map<String, Object> clinicDetails, Map<String, Object> adminUserLogin){

        ArrayList<HashMap<String, Object>> adminList = AfyaServiceConsumer.getAllAdminByTenantId();

        if (currentSchedule.getVisitType().getName().equals("Consult Visit")){
            //sms to doctor
            clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_CANCELLED_BY_PATIENT_SMS_TO_DOCTOR.name());
            clinicDetails.put("forDoctor", new Boolean(true));
            clinicDetails.put("forAdmin", new Boolean(false));
            SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
            //sms to admin
            List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
            if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
                clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_CANCELLED_BY_PATIENT_SMS_TO_DOCTOR.name());
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

            clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_CANCELLED_BY_PATIENT_SMS_TO_PATIENT.name());
            clinicDetails.put("forDoctor", new Boolean(false));
            clinicDetails.put("forAdmin", new Boolean(false));
            //BigDecimal refundAdvanceAmountPercent = commonCrudService.getByUniqueValue(ClinicReschedulingPreference.class, "visitType", RCMVisitType.CONSULT_VISIT).getRefundAdvanceAmountPercent();
            //Invoice invoice = commonCrudService.getByUniqueValue(Invoice.class, "schedule", currentSchedule);
            //String refundAmount = null;
            /*if(invoice != null){
                BigDecimal ONE_HUNDRED = new BigDecimal(100);
                BigDecimal collectedAmount = invoice.getCollectedAmount().getAmount();
                refundAmount =  (collectedAmount.subtract(collectedAmount.multiply(refundAdvanceAmountPercent).divide(ONE_HUNDRED))).setScale(3, BigDecimal.ROUND_HALF_UP).toString();;
                clinicDetails.put("refundAmount", refundAmount);
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
            }*/
            List<PatientDeposit> patientDeposits = commonCrudService.findByEquality(PatientDeposit.class, new String[]{"schedule"}, new Object[]{currentSchedule});
            if(UtilValidator.isNotEmpty(patientDeposits)){
                PatientDeposit patientDeposit = patientDeposits.get(patientDeposits.size() - 1 );
                String refundAmount = patientDeposit.getDepositAmount().setScale(3, BigDecimal.ROUND_HALF_UP).toString();
                clinicDetails.put("refundAmount", refundAmount);
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
            }
        }
    }
}
