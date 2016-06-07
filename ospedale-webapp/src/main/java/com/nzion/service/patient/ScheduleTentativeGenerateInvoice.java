package com.nzion.service.patient;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nzion.repository.notifier.utility.EmailUtil;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.PatientDeposit;
import com.nzion.domain.Provider;
import com.nzion.domain.RCMPreference;
import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.Schedule;
import com.nzion.domain.Schedule.Tentative;
import com.nzion.domain.SchedulingPreference;
import com.nzion.domain.SlotType;
import com.nzion.domain.UserLogin;
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
import com.nzion.repository.PatientRepository;
import com.nzion.repository.PersonRepository;
import com.nzion.repository.PracticeRepository;
import com.nzion.repository.notifier.utility.NotificationTaskExecutor;
import com.nzion.service.ScheduleService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.impl.EnumerationServiceImpl;
import com.nzion.service.impl.FileBasedServiceImpl;

public class ScheduleTentativeGenerateInvoice extends HttpServlet{
	
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
	    
	    private String paymentId;

	    public void init(ServletConfig config) throws ServletException {
	        super.init(config);
	        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
	    }

	    @Override
	    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	        String tenantId = request.getParameter("clinicId");
	        if(tenantId == null) {
	            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ClinicId cannot be null");
	            return;
	        }
	        TenantIdHolder.setTenantId(tenantId);
	        
	        String scheduleId = request.getParameter("scheduleId");
	        if(scheduleId == null) {
	            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ScheduleId cannot be null");
	            return;
	        }
	        
	        paymentId = request.getParameter("paymentId"); 
	        		
	        Schedule schedule = commonCrudService.getById(Schedule.class, new Long(scheduleId));
	        updateAppointment(response, schedule);
	        response.setStatus(HttpServletResponse.SC_OK, "appointment updated");
            //new code start for sending sms
            Invoice invoice = commonCrudService.getByUniqueValue(Invoice.class, "schedule", schedule);
            Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(tenantId);
            if(invoice != null){
                String amount = invoice.getCollectedAmount().getAmount().setScale(3, BigDecimal.ROUND_HALF_UP).toString();
                clinicDetails.put("collectedAmount", amount);
            }
			Map<String, Object> adminUserLogin = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername());
            Object languagePreference = adminUserLogin.get("languagePreference");
            clinicDetails.put("languagePreference", languagePreference);
            //SmsUtil.sendRequestAppointmentPaymentConfirmationSms(schedule.getPatient(), schedule.getPerson(), clinicDetails, schedule.getStartTime(), schedule.getStartDate());
            /*clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_SCHEDULE_FOR_PATIENT.name());
            clinicDetails.put("afyaId", currentSchedule.getPatient().getAfyaId());*/
            try {

				ArrayList<HashMap<String, Object>> adminList = AfyaServiceConsumer.getAllAdminByTenantId();

                //sms for doctor
                clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_SUCCESS_PAYMENT_SMS_FOR_DOCTOR.name());
                clinicDetails.put("forDoctor", new Boolean(true));
				clinicDetails.put("forAdmin", new Boolean(false));
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
				//sms to admin
				List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
				if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
					clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_SUCCESS_PAYMENT_SMS_FOR_DOCTOR.name());
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

                //sms for patient
                clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_SUCCESS_PAYMENT_SMS_FOR_PATIENT.name());
                clinicDetails.put("forDoctor", new Boolean(false));
				clinicDetails.put("forAdmin", new Boolean(false));
                SmsUtil.sendStatusSms(currentSchedule, clinicDetails);
                //new code for email
				String url = request.getRequestURL().toString();
				url = url.substring(0, url.lastIndexOf("clinicMaster"));

				InputStreamSource inputStreamSource  = null;
				if ((invoice != null) && (paymentId != null)){
					inputStreamSource = PDFGenerator.createWeeklyProviderRevenueReportPDFFile(invoice, paymentId, url);
				}

                String patientLanguagePreferrence = (clinicDetails.get("languagePreference") != null) ? clinicDetails.get("languagePreference").toString() : null;
                if (currentSchedule.getPatient().getLanguage() != null){
                    patientLanguagePreferrence = currentSchedule.getPatient().getLanguage().getEnumCode();
                }
                clinicDetails.put("afyaId", currentSchedule.getPatient().getAfyaId());
                clinicDetails.put("firstName", currentSchedule.getPatient().getFirstName());
                clinicDetails.put("lastName", currentSchedule.getPatient().getLastName());
                clinicDetails.put("languagePreference", patientLanguagePreferrence);
                clinicDetails.put("subject", "Appointment request acceptance by clinic");
                clinicDetails.put("template", "APPOINT_REQUEST_ACCEPTED_BY_CLINIC");
                clinicDetails.put("email", currentSchedule.getPatient().getContacts().getEmail());
				clinicDetails.put("stream", inputStreamSource);
				clinicDetails.put("attachment", new Boolean(true));
				clinicDetails.put("patient", currentSchedule.getPatient());
                EmailUtil.sendNetworkContractStatusMail(clinicDetails);
            }catch (Exception e){
                e.printStackTrace();
            }
            //new code end for sending sms
	    }

	    private void updateAppointment(HttpServletResponse response, Schedule schedule) {
	    	
	    	slotType = schedule.getVisitType();
	        if(slotType == null){
	            slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{"Consult Visit"});
	        }
	        currentSchedule = schedule;
	        currentSchedule.setTentativeStatus(Tentative.Paid.toString());
	        updatePrice();
	        generateInvoice(currentSchedule);
	        commonCrudService.save(currentSchedule);
	    }

	    public void generateInvoice(Schedule currentSchedule){
	        RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
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
	        RCMVisitType rcmVisitType = null;
	        if(slotType.getName().equals("Premium Visit"))
	            rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
	        if(slotType.getName().equals("Home Visit"))
	            rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
	        if(slotType.getName().equals("Tele Consultation Visit"))
	            rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
	        if(slotType.getName().equals("Consult Visit") && currentSchedule.isFromMobileApp() )
	            rcmVisitType = RCMVisitType.CONSULT_VISIT;
	        

	        SchedulingPreference schedulingPreference = commonCrudService.findUniqueByEquality(SchedulingPreference.class,
	                new String[]{"rcmPreference","visitType"}, new Object[]{rcmPreference,rcmVisitType});

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
	            consultationItem.init(totalCosultFeeConvFee, billingDisplayConfig.getCurrency().getCode(), new Money(totalCosultFeeConvFee,convertTo()),
	                    new Money(totalCosultFeeConvFee,convertTo()), 0);
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
	            
	            if(slotType.getName().equals("Consult Visit") ){
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
	        patientDeposit.setTotalAvailableAmount( patientDeposit.getDepositAmount().add(calculateAmount(patientDeposit.getPatient())) );
	        patientDeposit.setCreatedUser("Online Payment");
	        patientDeposit.setInvoice(inv);
	        patientDeposit.setSchedule(currentSchedule);
	        
	        List<Map<String, Object>> paymentGatewayTransactions = AfyaServiceConsumer.getPaymentGatewayTransactionById(paymentId);
	        if(UtilValidator.isNotEmpty(paymentGatewayTransactions)){
	        	Map<String, Object> paymentGatewayTransaction = paymentGatewayTransactions.get(0);
	        	patientDeposit.setPortalPaymentId(paymentGatewayTransaction.get("paymentId") != null ? paymentGatewayTransaction.get("paymentId").toString() : null);
	        	patientDeposit.setTransactionType(paymentGatewayTransaction.get("transactionType") != null ? paymentGatewayTransaction.get("transactionType").toString() : null);
	        	//patientDeposit.setTransactionTimestamp(paymentGatewayTransaction.get("transactionTimestamp") != null ? ((Date)paymentGatewayTransaction.get("transactionTimestamp")) : null);
	        	patientDeposit.setIsysTrackingRef(paymentGatewayTransaction.get("isysTrackingRef") != null ? paymentGatewayTransaction.get("isysTrackingRef").toString() : null);
	        	patientDeposit.setPayerType(paymentGatewayTransaction.get("payerType") != null ? paymentGatewayTransaction.get("payerType").toString() : null);
	        	patientDeposit.setPaymentChannel(paymentGatewayTransaction.get("paymentChannel") != null ? paymentGatewayTransaction.get("paymentChannel").toString() : null);
	        }
	        
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

}
