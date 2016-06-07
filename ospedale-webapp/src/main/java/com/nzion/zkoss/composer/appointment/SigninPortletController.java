package com.nzion.zkoss.composer.appointment;

import com.nzion.domain.ClinicCancellationPreference;
import com.nzion.domain.ClinicReschedulingPreference;
import com.nzion.domain.Enumeration;
import com.nzion.domain.Location;
import com.nzion.domain.NotificationSetup;
import com.nzion.domain.Patient;
import com.nzion.domain.PatientDeposit;
import com.nzion.domain.Person;
import com.nzion.domain.Practice;
import com.nzion.domain.Provider;
import com.nzion.domain.ProviderRefund;
import com.nzion.domain.Schedule;
import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.Schedule.Tentative;
import com.nzion.domain.billing.Invoice;
import com.nzion.domain.billing.InvoiceItem;
import com.nzion.domain.billing.InvoicePayment;
import com.nzion.domain.billing.InvoiceStatusItem;
import com.nzion.domain.billing.PaymentType;
import com.nzion.domain.emr.PatientVisit;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.NamingDisplayConfig;
import com.nzion.repository.notifier.utility.NotificationTaskExecutor;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.service.ScheduleService;
import com.nzion.service.SigninService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.AfyaServiceConsumer;
import com.nzion.util.CronExpressionGenerator;
import com.nzion.util.Infrastructure;
import com.nzion.util.RestServiceConsumer;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import com.nzion.view.component.NameLabel;
import com.nzion.zkoss.composer.OspedaleAutowirableComposer;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sandeep Prusty
 * Mar 14, 2011
 *
 * Mohan Sharma - search criteria implementation
 */
public class SigninPortletController extends OspedaleAutowirableComposer implements ListitemRenderer {

    private SigninService signinService;

    private ScheduleService scheduleService;
    
    private CommonCrudService commonCrudService;
    
    private BillingService billingService;
    
    private NotificationTaskExecutor notificationTaskExecutor;

    private Date selectedDate = UtilDateTime.nowDateOnly();

    private final List<Schedule> allRelevantSchedules = new ArrayList<Schedule>();

    private List<Schedule> todaysSchedules;

    private final List<Person> schedulableResourcesByLocation = new ArrayList<Person>();

    private Listbox schedulesOfTheDayListBox;

    private List<Person> schedulableResources;

    private final boolean forSelf;

    private Location location;

    private Person person;

    private STATUS status = STATUS.SCHEDULED;

    private Date searchFromDate;

    private Date searchToDate;

    private Patient patient;
    
    private String selectedBookingStatus;
    
    private String civilId;
    
    private String fileNo;
    
    private String mobileNo;

    public List<Person> getSchedulableResourcesByLocation() {
        CommonCrudService commonCrudService = Infrastructure.getSpringBean("commonCrudService");
        NamingDisplayConfig displayConfig = commonCrudService.getAll(NamingDisplayConfig.class).get(0);
        final String position = displayConfig.getPosition3();
        schedulableResourcesByLocation.clear();
        if (UtilValidator.isNotEmpty(getSchedulableResources()) && location != null) {
            for (Person person : getSchedulableResources())
                if (person.belongsTo(location)) {
                    schedulableResourcesByLocation.add(person);
                    Collections.sort(schedulableResourcesByLocation, new Comparator<Person>() {
                        @Override
                        public int compare(Person p1, Person p2) {
                            if(position!=null && position.equals("firstName"))
                                return p1.getFirstName().compareToIgnoreCase(p2.getFirstName());
                            else
                                return p1.getLastName().compareToIgnoreCase(p2.getLastName());
                        }
                    });
                }
        }
        return schedulableResourcesByLocation;
    }

    public List<Person> getSchedulableResources() {
        if (schedulableResources == null) schedulableResources = scheduleService.getSchedulablePersons();
        return schedulableResources;
    }

    public List<Schedule> getTodaysSchedules() {
        Iterator iterator = todaysSchedules.iterator();
        while (iterator.hasNext()){
            Schedule schedule = (Schedule) iterator.next();
            Date d = schedule.getStartDate();
            if (UtilDateTime.getDateOnly(d).before(UtilDateTime.getDateOnly(new Date()))){
                iterator.remove();
            }
        }

         return todaysSchedules;
    }

    public void setTodaysSchedules(List<Schedule> todaysSchedules) {
        this.todaysSchedules = todaysSchedules;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }


    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        loadSchedules();
    }

    public SigninPortletController(boolean forSelf) {
        this.forSelf = forSelf;
    }

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
        if (UtilValidator.isNotEmpty(Infrastructure.getUserLogin().getLocations()))
            location = Infrastructure.getUserLogin().getLocations().iterator().next();
        todaysSchedules = signinService.getSchedulesFor(person, null, status, null);
        loadSchedules();
    }

    private void loadSchedules() {
        allRelevantSchedules.clear();
        /*
        * Modified By Mohan Sharma
        * */
        //allRelevantSchedules.addAll(forSelf ? scheduleService.getSchedulesWaitingForSelf(selectedDate) : signinService.getSchedulesFor(null, selectedDate, status, location));
        allRelevantSchedules.addAll(signinService.getSchedulesFor(null, selectedDate, status, location));
    }

    public void addPatientVisitToSchedule(PatientVisit theVisit, Schedule schedule) {
        signinService.addVisitToSchedule(theVisit, schedule);
    }

    public void changeScheduleStatus(Schedule schedule, STATUS newStatus, PatientVisit visit) {
        signinService.changeStatus(schedule, newStatus, visit);
    }

    public Schedule saveSchedule(Schedule schedule) {
        return scheduleService.saveOrUpdate(schedule);
    }

    public List<Schedule> getAllRelevantSchedules() {
        return allRelevantSchedules;
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = UtilDateTime.getDayStart(selectedDate);
        loadSchedules();
    }

    public void setSigninService(SigninService signinService) {
        this.signinService = signinService;
    }

    public void setScheduleService(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    public void statusChangedInCombobox(final Combobox combobox) {
        final Schedule correspondingSchedule = (Schedule) ((Listitem) combobox.getParent().getParent()).getValue();
        EventListener okListener = getScheduleStatusChangeOkListener(combobox, correspondingSchedule);
        EventListener cancelListener = getScheduleStatusChangeCancelListener(combobox, correspondingSchedule);
        STATUS choosenStatus = (STATUS) combobox.getSelectedItem().getValue();
        if (STATUS.CHECKEDIN.equals(choosenStatus) || STATUS.PROCEDUREPENDING.equals(choosenStatus)) {
            Map<String, Object> args = com.nzion.util.UtilMisc.toMap("signinController", this,
                    "scheduleInToReceiveANewTrack", correspondingSchedule, "okEventListener", okListener,
                    "cancelEventListener", cancelListener);
            Executions.createComponents("/appointment/add-edit-patient-visit.zul", root, args);
            return;
        }
        EventListener yesNoListener = new EventListener() {
            public void onEvent(Event event) throws Exception {
                EventListener actualListener = event.getData().equals(Messagebox.YES) ? getScheduleStatusChangeOkListener(
                        combobox, correspondingSchedule) : getScheduleStatusChangeCancelListener(combobox,
                        correspondingSchedule);
                actualListener.onEvent(new Event("onCustomFire", null, null));
            }
        };
        UtilMessagesAndPopups.showConfirmation("Are you sure ?", yesNoListener);
    }


    @Override
    public void render(Listitem item, Object data,int index) throws Exception {
        final Schedule schedule = (Schedule) data;
        item.setValue(data);
        item.setContext(ScheduleViewHelper.getSignInMenuId(schedule));
        Listcell appointmentNoCell = new Listcell(String.valueOf(schedule.getId()));
        appointmentNoCell.setParent(item);

        Listcell patientAccNoCell = new Listcell(schedule.getPatient().getAfyaId());
        patientAccNoCell.setParent(item);
        Listcell patientCivilIDCell = new Listcell(schedule.getPatient().getCivilId());
        patientCivilIDCell.setParent(item);
        Listcell patientNameCell = new Listcell();
        patientNameCell.setParent(item);
        NameLabel patientNameLabel = new NameLabel();
        patientNameLabel.setObject(schedule.getPatient());
        patientNameLabel.setParent(patientNameCell);
        
        Listcell patientMobileNoCell = new Listcell();
        patientMobileNoCell.setParent(item);
        patientMobileNoCell.setLabel(schedule.getPatient().getContacts().getMobileNumber());
        
        Listcell providerNameCell = new Listcell();
        providerNameCell.setParent(item);
        NameLabel providerNameLabel = new NameLabel();
        providerNameLabel.setObject(schedule.getPerson());
        providerNameLabel.setParent(providerNameCell);

        Listcell statusCell = new Listcell(schedule.getStatus().getDescription());
        statusCell.setParent(item);
        // addStatusCell(item);
        
        String tentativeStatus = schedule.getTentativeStatus();
        if("Confirmed".equals(tentativeStatus)){
        	tentativeStatus = "Acknowledged";
        }
        if("Paid".equals(tentativeStatus)){
        	tentativeStatus = "Confirmed";
        }
        Listcell TentativeStatusCell = new Listcell(tentativeStatus.toUpperCase());
        TentativeStatusCell.setParent(item);

        Listcell slotTypeCell = new Listcell(schedule.getVisitType() == null ? "" : schedule.getVisitType().getName());
        slotTypeCell.setParent(item);
        
        Listcell appointmentDateCell = new Listcell();
        appointmentDateCell.setParent(item);
        if(schedule.getStartDate() != null){
            Label appointmentDateLbl = new Label();
            SimpleDateFormat dateFormat = UtilDateTime.DEFAULT_DATE_FORMATTER;
            appointmentDateLbl.setValue(dateFormat.format(schedule.getStartDate()));
            appointmentDateLbl.setParent(appointmentDateCell);
        }

        Listcell startTimeCell = new Listcell();
        startTimeCell.setParent(item);
        SimpleDateFormat sdf = new SimpleDateFormat(UtilDateTime.AM_PM_FORMAT);
        Label startTimebox = new Label();
        if (schedule.getStartTime() != null) {
            startTimebox.setValue(sdf.format(schedule.getStartTime()));
        }
        startTimebox.setParent(startTimeCell);

        Listcell arrivalTimeCell = new Listcell();
        arrivalTimeCell.setParent(item);
        Label arrivalTimebox = new Label();
        if (schedule.getLastPatientVisit()!=null && schedule.getLastPatientVisit().getTime() != null) {
            arrivalTimebox.setValue(sdf.format(schedule.getLastPatientVisit().getTime()));
        }
        arrivalTimebox.setParent(arrivalTimeCell);
	

	/*
	 *Since in time is not required this code has been commented by swetalina */
	/*
	 * Listcell signinTimeCell = new Listcell();
	signinTimeCell.setParent(item);
	Label signinTimebox = new Label();
	if (schedule.getSignedInTime() != null) {
		signinTimebox.setValue(sdf.format(schedule.getSignedInTime()));
	}
	signinTimebox.setParent(signinTimeCell);*/
        final Listcell confirmCell = new Listcell();
        confirmCell.setParent(item);
        if(Tentative.Tentative.toString().equals(schedule.getTentativeStatus())) {
            final Button button = new Button("Confirm");
            button.setParent(confirmCell);
            button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                	schedule.setTentativeStatus(Tentative.Confirmed.toString());
                	commonCrudService.save(schedule);
                    //communication loop start
                    try {
                        if (schedule.getTentativeStatus().equals(Tentative.Confirmed.toString())) {
                            Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
                            if (practice != null) {
                                Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
                                Map map = RestServiceConsumer.getPatientDetailsByAfyaId(schedule.getPatient().getAfyaId());
                                //Object languagePreference = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername()).get("languagePreference");
                                //clinicDetails.put("languagePreference", languagePreference);
                                clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_CONFIRMED_BY_CLINIC.name());
                                String url = "https://www.afyaarabia.com/afya-portal/#!/Service-payment?userName=" + map.get("userName") + "&scheduleId=" + schedule.getId() + "&clinicId=" + practice.getTenantId() + "&accountNumber=" + map.get("accountNumber");
                                clinicDetails.put("url", url);
                                SmsUtil.sendStatusSms(schedule, clinicDetails);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    //communication loop end
                	UtilMessagesAndPopups.showSuccess();
                	button.detach();
                }
            });
        }
        
        if(Tentative.Tentative.toString().equals(schedule.getTentativeStatus())) {
            final Button button = new Button("Cancel");
            button.setParent(confirmCell);
            button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                	Schedule schedule1 = commonCrudService.getById(Schedule.class, schedule.getId());
                	schedule1.setClinicCancel(true);
                	updateClinicCancellation(schedule1);
                	schedule1.setStatus(STATUS.CANCELLED);
                	scheduleService.updateSchedule(schedule1);
                	
                	if (schedule1.getStatus().equals(STATUS.CANCELLED)) {
                		NotificationSetup notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[] { "status" }, new Object[] { NotificationSetup.STATUS.CANCELLED });
            			String cronExpression = CronExpressionGenerator.generateCronExpressionGivenDateParameters(true, new Date(), notificationSetup.getTriggetPointValue());
            			if (notificationSetup.isNotificationRequired() && notificationSetup.isPatientRole()) {
            				Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
            				if (practice != null) {
            					Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
            					notificationTaskExecutor.prepareDetailsAndNotifyAppointmentCancelled(schedule1, cronExpression, notificationSetup.isByEmail(), notificationSetup.isBySMS(), clinicDetails);
            				}
            			}
            		}
                	
                	//new code start for communication loop
                    if (schedule1.getStatus().equals(STATUS.CANCELLED)) {
                        Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
                        if (practice != null) {
                            Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
                            Object languagePreference = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername()).get("languagePreference");
                            clinicDetails.put("languagePreference", languagePreference);
                            createDataForSms(schedule1, clinicDetails);
                        }
                    }
                    //new code end for communication loop
                	
                	UtilMessagesAndPopups.showSuccess();
                	confirmCell.setVisible(false);
                	button.detach();
                }
            });
        }

        confirmCell.setParent(item);
        if(schedule.getStatus().equals(STATUS.CANCELLED)) {
            final Button button = new Button("Reason");
            button.setParent(confirmCell);
            final Popup popup = new Popup();
            popup.setId("reasonPopup");
            popup.setWidth("300px");
            Label label = new Label(schedule.getCancelReason() != null ? schedule.getCancelReason() : "No Reason Specified");
            label.setParent(popup);
            button.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    //printCell.appendChild(popup);
                    UtilMessagesAndPopups.confirm((schedule.getCancelReason() == null || schedule.getCancelReason().equals("")) ? "No Reason Specified" : schedule.getCancelReason(), "Cancellation Reason", Messagebox.OK,null,null);
                }
            });
        }
        if (schedule.getSignedOutTime() != null) {
            A printLink = new A("View/Print");
            printLink.setParent(confirmCell);
            printLink.addEventListener(Events.ON_CLICK, new EventListener() {
                @Override
                public void onEvent(Event arg0) throws Exception {
                    Executions.getCurrent().sendRedirect("/soap/soapNotePreviewFull.zhtml?scheduleId=" + schedule.getId() + "&amp;pageName=SoapNotePreview","_printSoapNote");
                }
            });
        }
    }
    
    private boolean updateClinicCancellation(Schedule schedule) {
		PatientDeposit patientDeposit = null;
		List<PatientDeposit> patientDeposits = commonCrudService.findByEquality(PatientDeposit.class, new String[] { "schedule" }, new Object[] { schedule });
		if (UtilValidator.isNotEmpty(patientDeposits)) {
			patientDeposit = patientDeposits.get(patientDeposits.size() - 1);
		}
		ProviderRefund providerRefund = new ProviderRefund();
		RCMVisitType rcmVisitType = null;
		boolean refundConvFee = false;
		if (schedule.getVisitType().getName().equals("Premium Visit")) {
			rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
			refundConvFee = true;
		}
		if (schedule.getVisitType().getName().equals("Home Visit")) {
			rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
			refundConvFee = true;
		}
		if (schedule.getVisitType().getName().equals("Tele Consultation Visit"))
			rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Consult Visit")) {
			rcmVisitType = RCMVisitType.CONSULT_VISIT;
			refundConvFee = true;
		}
		ClinicCancellationPreference clinicCancellationPreference = commonCrudService.findUniqueByEquality(ClinicCancellationPreference.class, new String[] { "visitType" }, new Object[] { rcmVisitType });

		if (clinicCancellationPreference == null)
			return true;

		if (!Tentative.Paid.toString().equals(schedule.getTentativeStatus()) && schedule.isFromMobileApp())
			return true;

		BigDecimal cancellationTime = clinicCancellationPreference.getCancellationTime() == null ? BigDecimal.ZERO : clinicCancellationPreference.getCancellationTime();
		Date scheduleDateTime = UtilDateTime.toDate(schedule.getStartDate().getMonth(), schedule.getStartDate().getDate(), schedule.getStartDate().getYear(), schedule.getStartTime().getHours(), schedule.getStartTime().getMinutes(), schedule.getStartTime().getSeconds());
		BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(new Date(), scheduleDateTime));

		if (Tentative.Tentative.toString().equals(schedule.getTentativeStatus())) {
			return true;
		}

		if (hoursInterval.compareTo(cancellationTime) < 0) {
			UtilMessagesAndPopups.showError("Appointment can not be cancelled before " + cancellationTime + " hrs");
			return false;
		}

		if (patientDeposit != null) {
			Invoice invoice = patientDeposit.getInvoice();

			providerRefund.setRefundAmount(patientDeposit.getConvenienceFeeForPatientPortal());
			providerRefund.setProvider((Provider) schedule.getPerson());
			providerRefund.setInvoice(invoice);

			BigDecimal depositAmount = patientDeposit.getDepositAmount();
			BigDecimal clinicCancellationCharge = percentage(depositAmount, clinicCancellationPreference.getClinicCancellationChargePercent());
			BigDecimal totalCancellationCharges = depositAmount.subtract(clinicCancellationCharge);
			depositAdvAmount(totalCancellationCharges, schedule.getPatient());

			invoice = addToPatientAccount(invoice, totalCancellationCharges);

			invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
			for (InvoiceItem ii : invoice.getInvoiceItems()) {
				cancelInvoiceLineItem(invoice, ii);
			}
			commonCrudService.save(invoice);
			commonCrudService.save(providerRefund);
		}
		return true;
	}
    
    public void depositAdvAmount(BigDecimal totalCancellationCharges, Patient patient) {
		PatientDeposit patientDeposit = new PatientDeposit();
		patientDeposit.setStatus("Deposit");
		patientDeposit.setDepositAmount(totalCancellationCharges);
		patientDeposit.setDepositDate(new Date());
		patientDeposit.setDepositMode("CASH");
		patientDeposit.setPatient(patient);
		patientDeposit.setCreatedPerson(Infrastructure.getLoggedInPerson());
		patientDeposit.setReturnToPatient(true);
		commonCrudService.save(patientDeposit);
		billingService.updatePatientDeposit(patientDeposit.getPatient(), patientDeposit.getDepositAmount());
	}
    
    
    private void cancelInvoiceLineItem(Invoice invoice, InvoiceItem ii) {
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
    
    
    public Invoice addToPatientAccount(Invoice invoice, BigDecimal amount) {

		BigDecimal depositAdvanceAmount = BigDecimal.ZERO;
		Enumeration paymentMethod = commonCrudService.findUniqueByEquality(Enumeration.class, new String[] { "enumCode" }, new Object[] { "PATIENT_AMOUNT" });
		InvoicePayment invoicePayment = new InvoicePayment(paymentMethod, invoice, new Money(amount), PaymentType.OPD_PATIENT_AMOUNT);
		invoicePayment.setPaymentType(PaymentType.OPD_PATIENT_AMOUNT);
		depositAdvanceAmount = depositAdvanceAmount.add(invoicePayment.getAmount().getAmount().negate());

		invoice.addInvoicePayment(invoicePayment);
		invoice.getCollectedAmount().setAmount(invoice.getCollectedAmount().getAmount().add(invoicePayment.getAmount().getAmount()));
		invoice.setAmountRefundedToPatient(true);
		return invoice;
	}
    
    public static BigDecimal percentage(BigDecimal base, BigDecimal pct) {
		if (UtilValidator.isEmpty(base) || UtilValidator.isEmpty(pct))
			return BigDecimal.ZERO;
		return base.multiply(pct).divide(new BigDecimal("100"));
	}
    
    
    public void createDataForSms(Schedule schedule, Map<String, Object> clinicDetails){
        if(schedule.getStatus().equals(STATUS.CANCELLED)){

            /*BigDecimal refundAdvanceAmountPercent = commonCrudService.getByUniqueValue(ClinicReschedulingPreference.class, "visitType", RCMVisitType.CONSULT_VISIT).getRefundAdvanceAmountPercent();
            Invoice invoice = commonCrudService.getByUniqueValue(Invoice.class, "schedule", schedule);
            String refundAmount = null;
            if(invoice != null){
                BigDecimal ONE_HUNDRED = new BigDecimal(100);
                BigDecimal collectedAmount = invoice.getCollectedAmount().getAmount();
                refundAmount =  (collectedAmount.subtract(collectedAmount.multiply(refundAdvanceAmountPercent).divide(ONE_HUNDRED))).setScale(3, BigDecimal.ROUND_HALF_UP).toString();;
                clinicDetails.put("refundAmount", refundAmount);
            }*/

            List<PatientDeposit> patientDeposits = commonCrudService.findByEquality(PatientDeposit.class, new String[]{"schedule"}, new Object[]{schedule});
            if(UtilValidator.isNotEmpty(patientDeposits)){
                PatientDeposit patientDeposit = patientDeposits.get(patientDeposits.size() - 1 );
                String refundAmount = patientDeposit.getDepositAmount().setScale(3, BigDecimal.ROUND_HALF_UP).toString();
                clinicDetails.put("refundAmount", refundAmount);
            }

            if (schedule.getVisitType().getName().equals("Tele Consultation Visit")) {
                clinicDetails.put("key", TemplateNames.TELECONSULTATION_VISIT_CANCELLATION_BY_CLINIC.name());
                SmsUtil.sendStatusSms(schedule, clinicDetails);
            } else if (schedule.getVisitType().getName().equals("Home Visit")) {
                clinicDetails.put("key", TemplateNames.HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_APPOINTMENT_DET.name());
                SmsUtil.sendStatusSms(schedule, clinicDetails);

                clinicDetails.put("key", TemplateNames.HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_REFUND_DET.name());
                SmsUtil.sendStatusSms(schedule, clinicDetails);
            } else if (schedule.getVisitType().getName().equals("Premium Visit")) {
                clinicDetails.put("key", TemplateNames.PREMIUM_APPOINTMENT_CANCELLED_BY_CLINIC.name());
                SmsUtil.sendStatusSms(schedule, clinicDetails);
            }

        } else {
            if (schedule.getVisitType().getName().equals("Tele Consultation Visit")) {
                clinicDetails.put("key", TemplateNames.TELECONSULTATION_VISIT_RESCHEDULE_BY_CLINIC.name());
                SmsUtil.sendStatusSms(schedule, clinicDetails);
            } else if (schedule.getVisitType().getName().equals("Home Visit")) {
                clinicDetails.put("key", TemplateNames.HOME_VISIT_APPOINTMENT_RESCHEDULE_BY_CLINIC.name());
                SmsUtil.sendStatusSms(schedule, clinicDetails);
            } else if (schedule.getVisitType().getName().equals("Consult Visit")) {
                clinicDetails.put("key", TemplateNames.APPOINTMENT_REQUEST_RESCHEDULE_BY_CLINIC.name());
                SmsUtil.sendStatusSms(schedule, clinicDetails);
            }
        }
    }
    

	/*private void addStatusCell(Listitem item) {
	Schedule schedule = (Schedule) item.getValue();
	Listcell statusCell = new Listcell();
	Schedule.STATUS[] statuses = schedule.getStatus().getAllowedModifications();
	Combobox statusCombobox = new Combobox();
	for (int i = 0; i < statuses.length; ++i) {
		Comboitem comboitem = new Comboitem(statuses[i].toString());
		comboitem.setValue(statuses[i]);
		comboitem.setParent(statusCombobox);
	}
	statusCombobox.setReadonly(true);
	statusCombobox.setValue(schedule.getStatus().toString());
	statusCombobox.addEventListener("onChange", new EventListener() {
		public void onEvent(org.zkoss.zk.ui.event.Event event) {
		statusChangedInCombobox((Combobox) event.getTarget());
		}
	});
	statusCell.setParent(item);
	statusCombobox.setParent(statusCell);
	}*/

    public EventListener getScheduleStatusChangeOkListener(final Combobox combobox, final Schedule schedule) {
        return new EventListener() {
            public void onEvent(Event event) throws Exception {
                STATUS newStatus = (STATUS) combobox.getSelectedItem().getValue();
                changeScheduleStatus(schedule, newStatus, (PatientVisit) event.getData());
                Events.postEvent("onReload", combobox.getParent().getParent().getParent(), null);
            }
        };
    }

    public EventListener getScheduleStatusChangeCancelListener(final Combobox combobox, final Schedule schedule) {
        return new EventListener() {
            public void onEvent(Event event) throws Exception {
                // render((Listitem) combobox.getParent().getParent(), schedule);
                Events.postEvent("onReload", combobox.getParent().getParent().getParent(), null);
            }
        };
    }

    public EventListener getAddEditPatientVisitListener(final Schedule schedule) {
        return new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                if (event.getData() == null) return;
                PatientVisit visit = (PatientVisit) event.getData();
                signinService.addVisitToSchedule(visit, schedule);
            }
        };
    }

    public void getFilterData(Event event, boolean generic) {
        if (UtilValidator.isEmpty(getAllRelevantSchedules())
                || UtilValidator.isEmpty(((Textbox) event.getTarget()).getValue())) {
            schedulesOfTheDayListBox.setModel(new ListModelList(getAllRelevantSchedules()));
            return;
        }
        String data = ((org.zkoss.zk.ui.event.InputEvent) event).getValue();
        ((Textbox) event.getTarget()).setValue(((org.zkoss.zk.ui.event.InputEvent) event).getValue());
        List<Schedule> filteredSchedules = new LinkedList<Schedule>();
        for (Iterator<Schedule> itr = getAllRelevantSchedules().iterator(); itr.hasNext();) {
            Schedule schedule = itr.next();
            if (schedule.getPatient().getFirstName().toLowerCase().indexOf(data.toLowerCase()) >= 0
                    || schedule.getPatient().getLastName().toLowerCase().indexOf(data.toLowerCase()) >= 0) {
                filteredSchedules.add(schedule);
            }
        }
        schedulesOfTheDayListBox.setModel(new ListModelList(filteredSchedules));
    }

    private static final long serialVersionUID = 1L;

    public Date getSearchFromDate() {
        return searchFromDate;
    }

    public void setSearchFromDate(Date searchFromDate) {
        this.searchFromDate = searchFromDate;
    }

    public Date getSearchToDate() {
        return searchToDate;
    }

    public void setSearchToDate(Date searchToDate) {
        this.searchToDate = searchToDate;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void refurbishSchedules() {
        Location location = null;
        Person person = getPerson();
        List<Person> doctorList = getSchedulableResourcesByLocation();
        if(!doctorList.contains(person))
            person = null;
        List<Schedule> schedules = signinService.getSchedulesForGivenCriteria(status, person, searchFromDate, searchToDate, selectedBookingStatus,
        		civilId,fileNo,mobileNo);
        setTodaysSchedules(schedules);

    }

	public String getSelectedBookingStatus() {
		return selectedBookingStatus;
	}

	public void setSelectedBookingStatus(String selectedBookingStatus) {
		this.selectedBookingStatus = selectedBookingStatus;
	}

	public String getCivilId() {
		return civilId;
	}

	public void setCivilId(String civilId) {
		this.civilId = civilId;
	}

	public String getFileNo() {
		return fileNo;
	}

	public void setFileNo(String fileNo) {
		this.fileNo = fileNo;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public NotificationTaskExecutor getNotificationTaskExecutor() {
		return notificationTaskExecutor;
	}

	public void setNotificationTaskExecutor(NotificationTaskExecutor notificationTaskExecutor) {
		this.notificationTaskExecutor = notificationTaskExecutor;
	}
	
}
