package com.nzion.zkoss.composer;

import com.nzion.domain.*;
import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.base.Weekdays;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.billing.Consultation;
import com.nzion.domain.billing.DebitCreditEnum;
import com.nzion.domain.billing.Invoice;
import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.billing.InvoicePayment;
import com.nzion.domain.billing.InvoiceStatusItem;
import com.nzion.domain.billing.InvoiceType;
import com.nzion.domain.billing.PaymentType;
import com.nzion.domain.billing.Invoice.INSURANCESTATUS;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.domain.util.SlotAvailability;
import com.nzion.repository.notifier.utility.NotificationTaskExecutor;
import com.nzion.service.ScheduleService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.CronExpressionGenerator;
import com.nzion.util.Infrastructure;
import com.nzion.util.RestServiceConsumer;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilMisc;
import com.nzion.util.UtilValidator;
import com.nzion.view.ScheduleSearchValueObject;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.bind.annotation.*;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/26/15
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 *
 * Modified By Mohan Sharma
 * implemented Privacy Policy Consent
 */
@VariableResolver(DelegatingVariableResolver.class)
public class PatientBookAppointmentViewModel {

    @WireVariable
    private CommonCrudService commonCrudService;

    private Patient patient;

    private List<Provider> providerList;

    private Provider selectedProvider;

    private Date selectedDate;

    private Set searchResult = new HashSet();

    private ScheduleSearchValueObject searchObject = new ScheduleSearchValueObject(false);

    @WireVariable
    private ScheduleService scheduleService;

    private Weekdays weekdays = Weekdays.allSelectedWeekdays();

    private SlotAvailability selectedSlotAvailability;

    private Schedule currentSchedule = new Schedule();

    private boolean showBookAppointmentWindow = Boolean.FALSE;
    
    private List<SoapNoteType> allSoapNoteTypes;
    
    private SoapNoteType visitType;
    
    private BigDecimal consultationCharges;
    
    private BigDecimal convenienceFee;
    
    private BigDecimal registrationCharges;
    
    private BigDecimal totalAmount;
    
    private BigDecimal totalAdvAmount;
    
    private boolean displayConvenienceFee;
    
    @WireVariable
    private BillingService billingService;
    
    @WireVariable
    private NotificationTaskExecutor notificationTaskExecutor;

    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view, @BindingParam("patient") Patient patient) {
        Selectors.wireComponents(view, this, true);
        this.patient = patient;
        providerList = commonCrudService.findByEquality(Provider.class, new String[]{"active"}, new Object[]{Boolean.TRUE});
        selectedDate = new Date();
        showBookAppointmentWindow = showAppointmentWindowBasedOnRole();
        allSoapNoteTypes = scheduleService.getAllSoapNoteTypes();
    }

    private boolean showAppointmentWindowBasedOnRole() {
        UserLogin userLogin = Infrastructure.getUserLogin();
        if(userLogin.hasRole(Roles.PATIENT))
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }

    @Command("searchFreeSlot")
    @NotifyChange("searchResult")
    public void searchFreeSlot() {
    	if(UtilValidator.isEmpty(selectedProvider) || UtilValidator.isEmpty(visitType)){
    		searchResult = null;
    		setSelectedSlotAvailability(null);
    		return;
    	}
        searchObject.setPerson(selectedProvider);
        searchObject.setFromDate(selectedDate);
        searchObject.setThruDate(selectedDate);
        searchResult = this.searchSchedule(searchObject, weekdays);
        setSelectedSlotAvailability(null);
    }

    @Command("save")
    public void save() {
    	if(visitType.getName().equals("Premium Visit") || visitType.getName().equals("Home Visit") || visitType.getName().equals("Tele Consultation Visit") ){
    		BigDecimal leadTime = updatePrice();
    		BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(new Date(), selectedDate));
    		if(hoursInterval.compareTo(leadTime) < 1){
    			UtilMessagesAndPopups.showError("Appointment must be greater than or equal to " + leadTime + " of hours");
    			return;
    		}
            Executions.createComponents("/practice/confirmAndPay.zul", null, UtilMisc.toMap("controller",this));
    	}else{
    		CalendarSlot selectedCalendarSlot = selectedSlotAvailability.getSlot();
            Location location = commonCrudService.getById(Location.class, new Long(10001));
            currentSchedule.setLocation(location);
            currentSchedule.setPatient(patient);
            currentSchedule.setPerson(selectedProvider);
            currentSchedule.setStartTime(selectedCalendarSlot.getStartTime());
            currentSchedule.setEndTime(selectedCalendarSlot.getEndTime());
            currentSchedule.setStartDate(com.nzion.util.UtilDateTime.getDayStart(selectedDate));
            currentSchedule.setSequenceNum(selectedCalendarSlot.getSequenceNum());
            currentSchedule.setVisitType(visitType);
            currentSchedule.setMobileOrPatinetPortal(true);
            scheduleService.createSchedule(currentSchedule);
            sentAppointmentConfirmationEmail(currentSchedule);
            UtilMessagesAndPopups.showSuccess();
    	}
    }
    
    public void createAppointment(){
    	CalendarSlot selectedCalendarSlot = selectedSlotAvailability.getSlot();
        Location location = commonCrudService.getById(Location.class, new Long(10001));
        currentSchedule.setLocation(location);
        currentSchedule.setPatient(patient);
        currentSchedule.setPerson(selectedProvider);
        currentSchedule.setStartTime(selectedCalendarSlot.getStartTime());
        currentSchedule.setEndTime(selectedCalendarSlot.getEndTime());
        currentSchedule.setStartDate(com.nzion.util.UtilDateTime.getDayStart(selectedDate));
        currentSchedule.setSequenceNum(selectedCalendarSlot.getSequenceNum());
        currentSchedule.setVisitType(visitType);
        currentSchedule.setMobileOrPatinetPortal(true);
        currentSchedule.setConsultationInvoiceGenerated(true);
        scheduleService.createSchedule(currentSchedule);
        generateInvoice(currentSchedule);
        sentAppointmentConfirmationEmail(currentSchedule);
        UtilMessagesAndPopups.showSuccess();
        Executions.sendRedirect(null);
    }
    
    
    private void sentAppointmentConfirmationEmail(Schedule schedule)  {
        NotificationSetup notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[]{"status"}, new Object[]{NotificationSetup.STATUS.SCHEDULED});
        String cronExpression = CronExpressionGenerator.generateCronExpressionGivenDateParameters(true, new Date(), notificationSetup.getTriggetPointValue());
        if(notificationSetup.isNotificationRequired() && notificationSetup.isPatientRole()) {
            Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
            if (practice != null) {
                Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
                try {
                    boolean sendSMS = Boolean.FALSE;
                    if (notificationSetup.isBySMS() && !schedule.isWalkinAppointment())
                        sendSMS = Boolean.TRUE;
                    notificationTaskExecutor.prepareDetailsAndNotifyAppointmentSchedule(schedule, cronExpression, notificationSetup.isByEmail(), sendSMS, clinicDetails);
                } catch (NoSuchMethodException | ClassNotFoundException
                        | ParseException | SchedulerException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private BigDecimal updatePrice(){
    	RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
        Set<SchedulingPreference> schedulingPreferences = new HashSet<>(commonCrudService.findByEquality(SchedulingPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        consultationCharges = getProviderPrice(selectedProvider,patient,visitType);
        BigDecimal convFee = BigDecimal.ZERO;
        BigDecimal advAmount = BigDecimal.ZERO;
        BigDecimal leadTime = BigDecimal.ZERO;
        for(SchedulingPreference schedulingPreference : schedulingPreferences){
	          if(visitType.getName().equals("Premium Visit")){
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
	          if(visitType.getName().equals("Home Visit")){
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
	          if(visitType.getName().equals("Tele Consultation Visit")){
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
	          
	          if(visitType.getName().equals("Consult Visit") && currentSchedule.isRequestForAppointment()){
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
        List<Invoice> invoices = billingService.getFirstInvoice(patient);
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
    
    private BigDecimal getProviderPrice(Provider provider, Patient patient, SoapNoteType soapNoteType){
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
        if(visitType.getName().equals("Premium Visit"))
        	rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
        if(visitType.getName().equals("Home Visit"))
        	rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
        if(visitType.getName().equals("Tele Consultation Visit"))
        	rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
        if(visitType.getName().equals("Consult Visit") && currentSchedule.isRequestForAppointment())
        	rcmVisitType = RCMVisitType.CONSULT_VISIT;
        
        SchedulingPreference schedulingPreference = commonCrudService.findUniqueByEquality(SchedulingPreference.class, 
        		new String[]{"rcmPreference","visitType"}, new Object[]{rcmPreference,rcmVisitType});
        
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
        depositAdvAmount(inv);
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
        invoice.setCollectedByUser(login.getUsername());
        billingService.saveInvoiceStatus(invoice, InvoiceStatusItem.RECEIVED);
    }
    
    public void depositAdvAmount(Invoice inv){
    	PatientDeposit patientDeposit = new PatientDeposit();
    	patientDeposit.setStatus("Deposit");
    	patientDeposit.setDepositAmount(totalAdvAmount);
    	patientDeposit.setDepositDate(new Date());
    	patientDeposit.setDepositMode("CASH");
    	patientDeposit.setPatient(patient);
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

    public Set<SlotAvailability> searchSchedule(ScheduleSearchValueObject scheduleSearchValueObject, Weekdays weekdays) {
    	Set<SlotAvailability> slotAvailabilities = scheduleService.searchAvailableSchedules(scheduleSearchValueObject, weekdays);
    	Set<CalendarIndividualSlot> calendarIndividualSlots = new HashSet<>();
    	List<CalendarResourceAssoc> calendarResourceAssocs = commonCrudService.findByEquality(CalendarResourceAssoc.class, new String[]{"person"}, new Object[]{selectedProvider});
    	if(calendarResourceAssocs.size() > 0){
            CalendarResourceAssoc assoc = getCurrentCalendarResourceAssoc(calendarResourceAssocs, selectedDate);
            calendarIndividualSlots = assoc != null ? assoc.getCalendarIndividualSlots() : Collections.EMPTY_SET;
        }
        return createMappingOfSlotAndVisitType(calendarIndividualSlots,slotAvailabilities);
    }
    
    private Set<SlotAvailability> createMappingOfSlotAndVisitType(Set<CalendarIndividualSlot> calendarIndividualSlots, Set<SlotAvailability> slotAvailabilities) {
    	Iterator<SlotAvailability> iter = slotAvailabilities.iterator();
    	Set<SlotAvailability> slots = new LinkedHashSet<SlotAvailability>();
        while(iter.hasNext()){
        	SlotAvailability slot = iter.next();
            for(CalendarIndividualSlot calendarIndividualSlot : calendarIndividualSlots){
            	StringBuffer buffer = new StringBuffer();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                buffer.append(sdf.format(calendarIndividualSlot.getStartTime()));
                buffer.append(" - ");
                buffer.append(sdf.format(calendarIndividualSlot.getEndTime()));
                String time = getTimeSlot(slot);
                if( time.equals(buffer.toString()) && visitType.getName().equals(calendarIndividualSlot.getVisitTypeSoapModule().getSlotType().getName()) ){
                	slots.add(slot);
                }
            }
        }
        return slots;
    }
    
    public String getTimeSlot(SlotAvailability slotAvailability){
        StringBuilder buffer;
	    buffer = new StringBuilder();
	    CalendarSlot slot = slotAvailability.getSlot();
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	    buffer.append(sdf.format(slot.getStartTime()));
	    buffer.append(" - ");
	    buffer.append(sdf.format(slot.getEndTime()));
        return buffer.toString();
    }
    
    
    private CalendarResourceAssoc getCurrentCalendarResourceAssoc(List<CalendarResourceAssoc> calendarResourceAssocs, Date appointmentDate) {
        for(CalendarResourceAssoc calendarResourceAssoc :  calendarResourceAssocs){
            if(calendarResourceAssoc.getThruDate() == null && (appointmentDate.after(calendarResourceAssoc.getFromDate()) || appointmentDate.equals(calendarResourceAssoc.getFromDate())))
                return calendarResourceAssoc;
            if(calendarResourceAssoc.getThruDate() != null && (appointmentDate.after(calendarResourceAssoc.getFromDate()) || appointmentDate.equals(calendarResourceAssoc.getFromDate())) && (appointmentDate.before(calendarResourceAssoc.getThruDate()) || appointmentDate.equals(calendarResourceAssoc.getThruDate())))
                return calendarResourceAssoc;
        }
        return null;
    }

    public CommonCrudService getCommonCrudService() {
        return commonCrudService;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<Provider> getProviderList() {
        return providerList;
    }

    public void setProviderList(List<Provider> providerList) {
        this.providerList = providerList;
    }

    public Provider getSelectedProvider() {
        return selectedProvider;
    }

    public void setSelectedProvider(Provider selectedProvider) {
        this.selectedProvider = selectedProvider;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public Set getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(Set searchResult) {
        this.searchResult = searchResult;
    }

    public ScheduleSearchValueObject getSearchObject() {
        return searchObject;
    }

    public void setSearchObject(ScheduleSearchValueObject searchObject) {
        this.searchObject = searchObject;
    }

    public ScheduleService getScheduleService() {
        return scheduleService;
    }

    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    public Weekdays getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(Weekdays weekdays) {
        this.weekdays = weekdays;
    }

    public SlotAvailability getSelectedSlotAvailability() {
        return selectedSlotAvailability;
    }

    public void setSelectedSlotAvailability(SlotAvailability selectedSlotAvailability) {
        this.selectedSlotAvailability = selectedSlotAvailability;
    }

    public Schedule getCurrentSchedule() {
        return currentSchedule;
    }

    public void setCurrentSchedule(Schedule currentSchedule) {
        this.currentSchedule = currentSchedule;
    }

    public boolean isShowBookAppointmentWindow() {
        return showBookAppointmentWindow;
    }

    public void setShowBookAppointmentWindow(boolean showBookAppointmentWindow) {
        this.showBookAppointmentWindow = showBookAppointmentWindow;
    }

	public List<SoapNoteType> getAllSoapNoteTypes() {
		return allSoapNoteTypes;
	}

	public void setAllSoapNoteTypes(List<SoapNoteType> allSoapNoteTypes) {
		this.allSoapNoteTypes = allSoapNoteTypes;
	}

	public SoapNoteType getVisitType() {
		return visitType;
	}

	public void setVisitType(SoapNoteType visitType) {
		this.visitType = visitType;
	}

	public BigDecimal getConsultationCharges() {
		return consultationCharges.setScale(3, RoundingMode.HALF_UP);
	}

	public void setConsultationCharges(BigDecimal consultationCharges) {
		this.consultationCharges = consultationCharges;
	}

	public BigDecimal getConvenienceFee() {
		return convenienceFee.setScale(3, RoundingMode.HALF_UP);
	}

	public void setConvenienceFee(BigDecimal convenienceFee) {
		this.convenienceFee = convenienceFee;
	}

	public BigDecimal getRegistrationCharges() {
		return registrationCharges;
	}

	public void setRegistrationCharges(BigDecimal registrationCharges) {
		this.registrationCharges = registrationCharges;
	}

	public BillingService getBillingService() {
		return billingService;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public BigDecimal getTotalAdvAmount() {
		return totalAdvAmount.setScale(3, RoundingMode.HALF_UP);
	}

	public void setTotalAdvAmount(BigDecimal totalAdvAmount) {
		this.totalAdvAmount = totalAdvAmount;
	}

	public boolean isDisplayConvenienceFee() {
		return displayConvenienceFee;
	}

	public void setDisplayConvenienceFee(boolean displayConvenienceFee) {
		this.displayConvenienceFee = displayConvenienceFee;
	}
	
	public NotificationTaskExecutor getNotificationTaskExecutor() {
        return notificationTaskExecutor;
    }

    public void setNotificationTaskExecutor(NotificationTaskExecutor notificationTaskExecutor) {
        this.notificationTaskExecutor = notificationTaskExecutor;
    }
	
}
