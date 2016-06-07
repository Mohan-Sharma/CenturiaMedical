package com.nzion.zkoss.composer.appointment;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.mail.MessagingException;

import com.nzion.hibernate.ext.multitenant.TenantIdHolder;
import com.nzion.repository.ScheduleRepository;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.util.*;
import org.joda.time.LocalDateTime;
import org.quartz.SchedulerException;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

import com.nzion.domain.AfyaClinicDeposit;
import com.nzion.domain.AfyaClinicDeposit.ClinicDepositType;
import com.nzion.domain.CalendarResourceAssoc;
import com.nzion.domain.CalendarSlot;
import com.nzion.domain.ClinicCancellationPreference;
import com.nzion.domain.ClinicReschedulingPreference;
import com.nzion.domain.DoctorNoShowPreference;
import com.nzion.domain.Enumeration;
import com.nzion.domain.Location;
import com.nzion.domain.NotificationSetup;
import com.nzion.domain.Patient;
import com.nzion.domain.PatientCancellationPreference;
import com.nzion.domain.PatientDeposit;
import com.nzion.domain.PatientInsurance;
import com.nzion.domain.PatientNoShowPreference;
import com.nzion.domain.PatientReschedulingPreference;
import com.nzion.domain.Person;
import com.nzion.domain.Practice;
import com.nzion.domain.Provider;
import com.nzion.domain.ProviderAvailability;
import com.nzion.domain.ProviderRefund;
import com.nzion.domain.RCMPreference;
import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.Schedule;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.Schedule.Tentative;
import com.nzion.domain.ScheduleBreak;
import com.nzion.domain.SchedulingPreference;
import com.nzion.domain.SlotType;
import com.nzion.domain.UserLogin;
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
import com.nzion.domain.emr.PatientVisit;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.product.common.Money;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.domain.screen.NamingDisplayConfig;
import com.nzion.domain.screen.ScheduleConfig;
import com.nzion.domain.screen.ScheduleCustomView;
import com.nzion.domain.util.SlotAvailability;
import com.nzion.exception.TransactionException;
import com.nzion.repository.notifier.utility.EmailUtil;
import com.nzion.repository.notifier.utility.NotificationTaskExecutor;
import com.nzion.service.ScheduleService;
import com.nzion.service.SigninService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.MailService;
import com.nzion.service.impl.ScheduleScanner;
import com.nzion.view.MultiBookValueObject;
import com.nzion.view.ScheduleSearchValueObject;
import com.nzion.zkoss.composer.OspedaleAutowirableComposer;

/**
 * @author Sandeep Prusty Apr 30, 2010
 * 
 *         Notification Service, Cancellation implementation implementation by
 *         Mohan Sharma
 */

public class ScheduleController extends OspedaleAutowirableComposer {

	private ScheduleService scheduleService;

	private AppointmentListcell selectedComponent;

	private AppointmentListcell clipboard;

	private Person person;

	private Location location;

	private Date selectedDate;

	private ViewDataDelegator viewDataDelegator = new DayViewDataDelegator();

	private final ScheduleConfig screenConfig;

	private ScheduleCustomView scheduleCustomView;

	private Long waitingListCount;

	private Label leftDateLabel;

	private Label rightDateLabel;

	private Label centerDateLabel;

	private MultiBookValueObject multiBookValueObject;

	private List<Person> schedulableResources;

	private final List<Person> schedulableResourcesByLocation = new ArrayList<Person>();

	private List<Provider> internalReferrals = new ArrayList<Provider>();

	private String mode = "NONE";

	private Patient selectedPatient;

	private BillingService billingService;

	private MailService mailService;

	private ProviderAvailability providerAvailability;

	private NotificationTaskExecutor notificationTaskExecutor;

	public static final String SOAP_ITEM_TYPE = PatientSoapNote.class.getName();

	private boolean patientRescheduling;

	private boolean clinicRescheduling;

	private BigDecimal consultationCharges;

	private boolean displayConvenienceFee;

	private BigDecimal convenienceFee;

	private BigDecimal registrationCharges;

	private BigDecimal totalAmount;

	private BigDecimal totalAdvAmount;

	private SlotType slotType;

	public List<Provider> getInternalReferrals() {
		CommonCrudService commonCrudService = Infrastructure.getSpringBean("commonCrudService");
		NamingDisplayConfig displayConfig = commonCrudService.getAll(NamingDisplayConfig.class).get(0);
		final String position = displayConfig.getPosition3();
		internalReferrals = commonCrudService.getAll(Provider.class);
		Collections.sort(internalReferrals, new Comparator<Person>() {
			@Override
			public int compare(Person p1, Person p2) {
				if (position != null && position.equals("firstName"))
					return p1.getFirstName().compareToIgnoreCase(p2.getFirstName());
				else
					return p1.getLastName().compareToIgnoreCase(p2.getLastName());
			}
		});
		return internalReferrals;
	}

	private SoapNoteService soapNoteService;

	private STATUS selectedStatus;

	private SigninService signinService;

	private CommonCrudService commonCrudService;

	public ScheduleController() {
		screenConfig = scheduleService.getAppointmentDisplayScreen();
		UserLogin login = Infrastructure.getUserLogin();
		setLocation(UtilValidator.isEmpty(login.getLocations()) ? null : login.getLocations().iterator().next());
		resetToToday();
	}

	public ScheduleController(String mode) {
		this();
		this.mode = mode;
		if ("PORTLET".equalsIgnoreCase(mode))
			viewDataDelegator = new PortletViewDataDelegator();
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {
		centerDateLabel = (Label) component.getFellowIfAny("centerDateLabel");
		leftDateLabel = (Label) component.getFellowIfAny("leftDateLabel");
		rightDateLabel = (Label) component.getFellowIfAny("rightDateLabel");
		super.doAfterCompose(component);
		setLocation((com.nzion.domain.Location) Sessions.getCurrent().getAttribute("_location"));
		populateWaitingListLabel();
	}

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
							if (position != null && position.equals("firstName"))
								return p1.getFirstName().compareToIgnoreCase(p2.getFirstName());
							else
								return p1.getLastName().compareToIgnoreCase(p2.getLastName());
						}
					});
				}
		}
		return schedulableResourcesByLocation;
	}

	public BillingService getBillingService() {
		return billingService;
	}

	public void setBillingService(BillingService billingService) {
		this.billingService = billingService;
	}

	public Patient getSelectedPatient() {
		return selectedPatient;
	}

	public void setSelectedPatient(Patient selectedPatient) {
		this.selectedPatient = selectedPatient;
	}

	public List<Person> getSchedulableResources() {
		if (schedulableResources == null)
			schedulableResources = scheduleService.getSchedulablePersons();
		return schedulableResources;
	}

	public void populateWaitingListLabel() {
		if (!"FULL".equals(mode))
			return;
		waitingListCount = scheduleService.findWaitingListCount();
		waitingListCount = waitingListCount == null ? 0 : waitingListCount;
		((org.zkoss.zul.Button) root.getFellow("waitingListBtn")).setLabel("Waitinglist (" + waitingListCount + ")");
	}

	public void setLeftDateLabel(Label leftDateLabel) {
		this.leftDateLabel = leftDateLabel;
	}

	public void setRightDateLabel(Label rightDateLabel) {
		this.rightDateLabel = rightDateLabel;
	}

	public void setCenterDateLabel(Label centerDateLabel) {
		this.centerDateLabel = centerDateLabel;
	}

	public void resetToToday() {
		setSelectedDate(new Date());
	}

	public ScheduleCustomView getScheduleCustomView() {
		return scheduleCustomView;
	}

	public void setScheduleCustomView(ScheduleCustomView scheduleCustomView) {
		this.scheduleCustomView = scheduleCustomView;
		viewDataDelegator = new CustomViewDataDelegator();
		populateScheduleGrid();
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
		populateScheduleGrid();
	}

	public void setPersons(List<Person> persons) {
		if (persons.size() == 1) {
			viewDataDelegator = viewDataDelegator instanceof CustomViewDataDelegator ? new DayViewDataDelegator() : viewDataDelegator;
			setPerson(persons.get(0));
			return;
		}
		setScheduleCustomView(new ScheduleCustomView(persons));
	}

	public void setSelectedComponent(AppointmentListcell selectedComponent) {
		this.selectedComponent = selectedComponent;
		if (selectedComponent != null && selectedComponent.getSchedule() != null)
			setSelectedPatient(selectedComponent.getSchedule().getPatient());
	}

	public AppointmentListcell getSelectedComponent() {
		return selectedComponent;
	}

	public void copySchedule() {
		clipboard = selectedComponent;
	}

	public void setScheduleService(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	public CalendarSlot getSelectedSlot() {
		return (CalendarSlot) selectedComponent.getAttribute("slot");
	}

	public Date getSelectedDate() {
		return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
		this.selectedDate = selectedDate; // UtilDateTime.dateOnly(selectedDate);
		populateScheduleGrid();
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
		populateScheduleGrid();
	}

	public void changeToDayView() {
		viewDataDelegator = new DayViewDataDelegator();
		populateScheduleGrid();
	}

	public void changeToWeekView() {
		viewDataDelegator = new WeekViewDataDelegator();
		populateScheduleGrid();
	}

	public void changeToMonthView() {
		viewDataDelegator = new MonthViewDataDelegator();
		populateScheduleGrid();
	}

	public void changeToCustomView() {
		viewDataDelegator = new CustomViewDataDelegator();
		populateScheduleGrid();
	}

	private void populateScheduleGrid() {
		if (viewDataDelegator.isNotReady())
			return;
		viewDataDelegator.changeRelatedScreenDisplay();
		Component vbox = root.getFellowIfAny("schedulesVbox");
		if (vbox == null)
			return;
		Component providerListComp = root.getFellowIfAny("providerList");
		if(providerListComp != null && providerListComp instanceof Listbox && ((Listbox)providerListComp).getSelectedItem() != null){
			person = ((Listbox)providerListComp).getSelectedItem().getValue();
		}
		AppointmentGrid previousView = (AppointmentGrid) vbox.getFirstChild();
		if (previousView != null)
			previousView.detach();
		List<CalendarResourceAssoc> calendarAssocs = viewDataDelegator.getAssociations();

		//new code for week day start
		Date d = getSelectedDate();
		String day = new SimpleDateFormat("EEE").format(getSelectedDate());
		Iterator iterator = calendarAssocs.iterator();
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

		//new code for week day end

		AppointmentGrid grid = new AppointmentGrid(screenConfig, getSelectedDate(), root, viewDataDelegator.getGridRenderer());
		grid.setParent(vbox);
		if (UtilValidator.isNotEmpty(calendarAssocs)) {
			List<ScheduleBreak> breaks = scheduleService.getScheduleBreaks(person, selectedDate, viewDataDelegator.getEndDate());
			grid.setCalendarTemplateAssociations(calendarAssocs, breaks);
		}
		List<Schedule> schedulesBooked = viewDataDelegator.getDetailedSchedules();
		grid.populateSchedules(schedulesBooked);
	}

	public boolean updateSchedule(final Schedule schedule) throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException {
		Checkbox internalReferralCheckbox = (Checkbox) root.getFellowIfAny("internalReferralCheckBoxId");
		String message = check(schedule);
		NotificationSetup notificationSetup;
		schedule.assignInternalReferral(internalReferralCheckbox.isChecked());
		if (schedule.isPatientCancel()) {
			boolean isReturn = !updatePatientCancellation(schedule);
			if (isReturn)
				return isReturn;
		}
		if (schedule.isClinicCancel()) {
			boolean isReturn = !updateClinicCancellation(schedule);
			if (isReturn)
				return isReturn;
		}
		if (schedule.isPatientNoShow()) {
			updatePatientNoShow(schedule);
		}
		if (schedule.isDoctorNoShow()) {
			updateDoctorNoShow(schedule);
		}
		if (message != null) {
			EventListener confirmationListener = new EventListener() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData().equals(Messagebox.YES))
						updateScheduleDirectly(schedule);
				}
			};
			UtilMessagesAndPopups.showConfirmation("The patient is dead do you want to proceed ?", confirmationListener);
			return true;
		}
		if (!schedule.isPriorityMailSent() && schedule.getPriority() != null && schedule.getPriority().equals("High")) {
			sendNotificationForHighPriorityPatient(schedule);
			schedule.setPriorityMailSent(true);
		}
		updateScheduleDirectly(schedule);
		if (schedule.isRescheduled()) {
			// Executions.sendRedirect("/");
			notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[] { "status" }, new Object[] { NotificationSetup.STATUS.RESCHEDULED });
			String cronExpression = CronExpressionGenerator.generateCronExpressionGivenDateParameters(true, new Date(), notificationSetup.getTriggetPointValue());
			if (notificationSetup.isNotificationRequired() && notificationSetup.isPatientRole()) {
				Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
				if (practice != null) {
					Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
					notificationTaskExecutor.prepareDetailsAndNotifyAppointmentRescheduled(schedule, cronExpression, notificationSetup.isByEmail(), notificationSetup.isBySMS(), clinicDetails);
					schedule.setRescheduled(false);
					commonCrudService.merge(schedule);
				}
			}
		}
		/*
		 * else { notificationSetup =
		 * commonCrudService.findUniqueByEquality(NotificationSetup.class, new
		 * String[]{"status"}, new
		 * Object[]{NotificationSetup.STATUS.RESCHEDULED}); String
		 * cronExpression =
		 * CronExpressionGenerator.generateCronExpressionGivenDateParameters
		 * (true, new Date(),
		 * Integer.parseInt(notificationSetup.getTriggetPointValue())); if
		 * (notificationSetup.isByEmail())
		 * notificationTaskExecutor.prepareDetailsAndEmailAppoinmentSchedule
		 * (schedule, cronExpression); }
		 */
		return true;
	}

	private boolean updatePatientCancellation(Schedule schedule) {
		PatientDeposit patientDeposit = null;
		List<PatientDeposit> patientDeposits = commonCrudService.findByEquality(PatientDeposit.class, new String[] { "schedule" }, new Object[] { schedule });
		if (UtilValidator.isNotEmpty(patientDeposits)) {
			patientDeposit = patientDeposits.get(patientDeposits.size() - 1);
		}
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
		PatientCancellationPreference patientCancellationPreference = commonCrudService.findUniqueByEquality(PatientCancellationPreference.class, new String[] { "visitType" }, new Object[] { rcmVisitType });

		if (patientCancellationPreference == null)
			return true;

		if (!Tentative.Paid.toString().equals(schedule.getTentativeStatus()) && !schedule.isFromMobileApp())
			return true;

		BigDecimal cancellationTime = patientCancellationPreference.getCancellationTime() == null ? BigDecimal.ZERO : patientCancellationPreference.getCancellationTime();
		Date scheduleDateTime = UtilDateTime.toDate(schedule.getStartDate().getMonth(), schedule.getStartDate().getDate(), schedule.getStartDate().getYear(), schedule.getStartTime().getHours(), schedule.getStartTime().getMinutes(), schedule.getStartTime().getSeconds());
		BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(new Date(), scheduleDateTime));

		if (Tentative.Tentative.toString().equals(schedule.getTentativeStatus())) {
			return true;
		}
		if (hoursInterval.compareTo(cancellationTime) < 0) {
			UtilMessagesAndPopups.showError("Appointment can not be cancelled before " + cancellationTime + " hrs");
			return false;
		}

		if (patientDeposit == null || patientCancellationPreference == null)
			return false;
		if (patientDeposit != null) {
			BigDecimal depositAmount = BigDecimal.ZERO;
			if (refundConvFee) {
				depositAmount = patientDeposit.getDepositAmount();
			} else {
				depositAmount = patientDeposit.getDepositAmount().subtract(patientDeposit.getConvenienceFeeForPatientPortal());
			}

			BigDecimal patientCancellationCharge = percentage(depositAmount, patientCancellationPreference.getPatientCancellationChargePercent());
			BigDecimal afyaCancellationCharge = percentage(depositAmount, patientCancellationPreference.getPatientCancelationChargeAfyePercent());
			BigDecimal totalCancellationCharges = patientCancellationCharge.add(afyaCancellationCharge);
			BigDecimal totalRefundAmount = depositAmount.subtract(totalCancellationCharges);
			depositAdvAmount(totalRefundAmount, schedule.getPatient());
			Invoice invoice = patientDeposit.getInvoice();

			invoice = addToPatientAccount(invoice, totalRefundAmount);

			AfyaClinicDeposit clinicDeposit = new AfyaClinicDeposit(invoice, patientCancellationCharge, ClinicDepositType.CLINIC);
			AfyaClinicDeposit afyaClinicDeposit = new AfyaClinicDeposit(invoice, afyaCancellationCharge, ClinicDepositType.AFYA);

			commonCrudService.save(clinicDeposit);
			commonCrudService.save(afyaClinicDeposit);

			invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
			for (InvoiceItem ii : invoice.getInvoiceItems()) {
				cancelInvoiceLineItem(invoice, ii);
			}
			commonCrudService.save(invoice);
		}
		return true;
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
		ClinicCancellationPreference clinicCancellationPreference = commonCrudService.findUniqueByEquality(ClinicCancellationPreference.class, new String[]{"visitType"}, new Object[]{rcmVisitType});

		if (clinicCancellationPreference == null)
			return true;

		if (!Tentative.Paid.toString().equals(schedule.getTentativeStatus()) && !schedule.isFromMobileApp())
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

	private void updatePatientNoShow(Schedule schedule) {
		PatientDeposit patientDeposit = null;
		List<PatientDeposit> patientDeposits = commonCrudService.findByEquality(PatientDeposit.class, new String[] { "schedule" }, new Object[] { schedule });
		if (UtilValidator.isNotEmpty(patientDeposits)) {
			patientDeposit = patientDeposits.get(patientDeposits.size() - 1);
		}
		RCMVisitType rcmVisitType = null;
		if (schedule.getVisitType().getName().equals("Premium Visit"))
			rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Home Visit"))
			rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Tele Consultation Visit"))
			rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Consult Visit"))
			rcmVisitType = RCMVisitType.CONSULT_VISIT;
		PatientNoShowPreference patientNoShowPreference = commonCrudService.findUniqueByEquality(PatientNoShowPreference.class, new String[] { "visitType" }, new Object[] { rcmVisitType });

		if (patientNoShowPreference == null && patientDeposit == null)
			return;

		BigDecimal depositAmount = patientDeposit.getDepositAmount().subtract(patientDeposit.getConvenienceFeeForPatientPortal());
		BigDecimal refundClinicCharges = percentage(depositAmount, patientNoShowPreference.getRefundClinicPercent());
		BigDecimal refundAfyaCharges = percentage(depositAmount, patientNoShowPreference.getRefundAfyaPercent());
		BigDecimal totalCancellationCharges = refundClinicCharges.add(refundAfyaCharges);
		BigDecimal totalRefundAmount = depositAmount.subtract(totalCancellationCharges);
		depositAdvAmount(totalRefundAmount, schedule.getPatient());
		Invoice invoice = patientDeposit.getInvoice();
		invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
		for (InvoiceItem ii : invoice.getInvoiceItems()) {
			cancelInvoiceLineItem(invoice, ii);
		}
		commonCrudService.save(invoice);
	}

	private void updateDoctorNoShow(Schedule schedule) {
		PatientDeposit patientDeposit = null;
		List<PatientDeposit> patientDeposits = commonCrudService.findByEquality(PatientDeposit.class, new String[] { "schedule" }, new Object[] { schedule });
		if (UtilValidator.isNotEmpty(patientDeposits)) {
			patientDeposit = patientDeposits.get(patientDeposits.size() - 1);
		}
		RCMVisitType rcmVisitType = null;
		if (schedule.getVisitType().getName().equals("Premium Visit"))
			rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Home Visit"))
			rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Tele Consultation Visit"))
			rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Consult Visit"))
			rcmVisitType = RCMVisitType.CONSULT_VISIT;
		DoctorNoShowPreference doctorNoShowPreference = commonCrudService.findUniqueByEquality(DoctorNoShowPreference.class, new String[] { "visitType" }, new Object[] { rcmVisitType });
		if (patientDeposit != null) {
			BigDecimal depositAmount = patientDeposit.getDepositAmount().subtract(patientDeposit.getConvenienceFeeForPatientPortal());
			BigDecimal refundClinicCharges = percentage(depositAmount, doctorNoShowPreference.getRefundClinicPercent());
			BigDecimal refundAfyaCharges = percentage(depositAmount, doctorNoShowPreference.getRefundAfyaPercent());
			BigDecimal totalCancellationCharges = refundClinicCharges.add(refundAfyaCharges);
			BigDecimal totalRefundAmount = depositAmount.subtract(totalCancellationCharges);
			depositAdvAmount(totalRefundAmount, schedule.getPatient());
			Invoice invoice = patientDeposit.getInvoice();
			invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
			for (InvoiceItem ii : invoice.getInvoiceItems()) {
				cancelInvoiceLineItem(invoice, ii);
			}
			commonCrudService.save(invoice);
		}
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

	public static BigDecimal percentage(BigDecimal base, BigDecimal pct) {
		if (UtilValidator.isEmpty(base) || UtilValidator.isEmpty(pct))
			return BigDecimal.ZERO;
		return base.multiply(pct).divide(new BigDecimal("100"));
	}

	private void updateScheduleDirectly(final Schedule schedule) throws ClassNotFoundException, NoSuchMethodException, ParseException, SchedulerException {
		/*
		 * if(schedule.getVisitType() != null &&
		 * schedule.getVisitType().getName(
		 * ).trim().toUpperCase().startsWith("TELE CONSULTATION") &&
		 * !schedule.isConfirmed()){
		 * org.zkoss.zul.Messagebox.show("Appointment Confirmed?",
		 * "Confirmation", org.zkoss.zul.Messagebox.YES |
		 * org.zkoss.zul.Messagebox.NO, org.zkoss.zul.Messagebox.QUESTION, new
		 * EventListener() { public void onEvent(Event event) throws Exception {
		 * if (event.getData().equals(org.zkoss.zul.Messagebox.YES)) {
		 * schedule.setConfirmed(true); updateAppointment(schedule); } else{
		 * updateAppointment(schedule); } } }); }
		 */
		if (schedule != null && schedule.getStatus().equals(STATUS.CANCELLED)) {
			org.zkoss.zul.Messagebox.show("Do you want to cancel the appointment?", "Confirmation", org.zkoss.zul.Messagebox.YES | org.zkoss.zul.Messagebox.NO, org.zkoss.zul.Messagebox.QUESTION, new EventListener() {
				public void onEvent(Event event) throws Exception {
					if (event.getData().equals(org.zkoss.zul.Messagebox.YES)) {
						updateAppointment(schedule);
					} else {
						// Executions.sendRedirect("/");
						return;
					}
				}
			});
		} else {
			updateAppointment(schedule);
		}
	}

	private void updateAppointment(Schedule schedule) throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException {
		NotificationSetup notificationSetup;
		// TODO
		// schedule = commonCrudService.refreshEntity(schedule);
		scheduleService.updateSchedule(schedule);

		UtilMessagesAndPopups.showSuccess();
		addScheduleDataToGrid(schedule);
		if (schedule.getStatus().equals(STATUS.CHECKEDOUT)) {
			List<Invoice> invoices = commonCrudService.findByEquality(Invoice.class, new String[]{"schedule"}, new Object[]{schedule});
			if (UtilValidator.isEmpty(invoices)) {
				try {
					List<PatientSoapNote> patientSoapNotes = commonCrudService.findByEquality(PatientSoapNote.class, new String[]{"schedule"}, new Object[]{schedule});
					if (UtilValidator.isNotEmpty(patientSoapNotes)) {
						billingService.generateInvoiceFor(patientSoapNotes.get(0));
					} else {
						List<Invoice> invList = commonCrudService.findByEquality(Invoice.class, new String[]{"schedule"}, new Object[]{schedule});
						if (UtilValidator.isEmpty(invList)) {
							PatientSoapNote patientSoapNote = new PatientSoapNote();
							patientSoapNote.setSchedule(schedule);
							patientSoapNote.setProvider((Provider) schedule.getPerson());
							patientSoapNote.setSpeciality(((Provider) schedule.getPerson()).getSpecialities().iterator().next());
							patientSoapNote.setPatient(schedule.getPatient());
							patientSoapNote = commonCrudService.save(patientSoapNote);
							billingService.generateInvoiceFor(patientSoapNote);
						}
					}
				} catch (TransactionException e) {
					e.printStackTrace();
				}
			}
		}

		if (schedule.getStatus().equals(STATUS.CANCELLED)) {
			if (schedule.getPaymentId() != null) {
				AfyaServiceConsumer.updateCancelStatusInPortal(schedule.getPaymentId());
			}
		}

		/*slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{schedule.getVisitType().getName()});
		if (slotType == null) {
			slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{"Consult Visit"});
		}

		List visitTypeSoapModuleList = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider","slotType"}, new Object[]{schedule.getPerson(),slotType});
		if((UtilValidator.isNotEmpty(visitTypeSoapModuleList)) && (!((VisitTypeSoapModule)visitTypeSoapModuleList.get(0)).isSmartService())) {*/
		if (!schedule.isMobileOrPatinetPortal()){
		if (schedule.getStatus().equals(STATUS.CANCELLED)) {
			notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[]{"status"}, new Object[]{NotificationSetup.STATUS.CANCELLED});
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
        //new code start for communication loop
        if (schedule.getStatus().equals(STATUS.CANCELLED)) {
            Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
            if (practice != null) {
				/*if((UtilValidator.isNotEmpty(visitTypeSoapModuleList)) && (((VisitTypeSoapModule)visitTypeSoapModuleList.get(0)).isSmartService())) {*/
				if (schedule.isMobileOrPatinetPortal()){
					Map<String, Object> clinicDet = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
					Map<String, Object> adminUserLogin = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername());
					Object languagePreference = adminUserLogin.get("languagePreference");
					clinicDet.put("languagePreference", languagePreference);
					createDataForSms(schedule, clinicDet, adminUserLogin);
				}
            }
        } else if(schedule.getStatus().equals(STATUS.CHECKEDOUT)){
			Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
			if (practice != null) {
					Map<String, Object> clinicDet = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
					Map<String, Object> adminUserLogin = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername());
					Object languagePreference = adminUserLogin.get("languagePreference");
					clinicDet.put("languagePreference", languagePreference);
					createDataForSms(schedule, clinicDet, adminUserLogin);
			}
		}
        //new code end for communication loop
	}

	private String check(Schedule schedule) {
		String message = null;
		if (schedule.getPatient().getDateOfDeath() != null)
			message = "The patient is dead do you want to proceed ?";
		if (!location.isActive())
			message = "The location is deactivated do you want to proceed ?";
		return message;
	}

	public boolean createSchedule(final Schedule schedule) throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException, IOException, MessagingException {
		// schedule.setStartTime(UtilDateTime.constructFurnishedDate(schedule.getStartDate(),
		// schedule.getStartTime()));
		// schedule.setEndTime(UtilDateTime.constructFurnishedDate(schedule.getStartDate(),
		// schedule.getEndTime()));
		Checkbox internalReferralCheckBox = (Checkbox) root.getFellowIfAny("internalReferralCheckBoxId");
		String message = check(schedule);
		schedule.assignInternalReferral(internalReferralCheckBox.isChecked());
		if (message != null) {
			EventListener confirmationListener = new EventListener() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData().equals(Messagebox.YES))
						createScheduleDirectly(schedule);
				}
			};
			UtilMessagesAndPopups.showConfirmation(message, confirmationListener);
			return true;
		}
		if (!schedule.isPriorityMailSent() && schedule.getPriority() != null && schedule.getPriority().equals("High")) {
			sendNotificationForHighPriorityPatient(schedule);
			schedule.setPriorityMailSent(true);
		}
		boolean result = createScheduleDirectly(schedule);
		if (schedule.getVisitType() != null && schedule.getVisitType().getName().trim().toUpperCase().startsWith("TELE CONSULTATION") && Tentative.Tentative.toString().equals(schedule.getTentativeStatus())) {
			sendTeleconsultationNonConfirmedEmail(schedule);
			return result;
		} else {
			configureReminderEmailForAppointment(schedule);
			sentAppointmentConfirmationEmail(schedule);
		}
		commonCrudService.save(schedule);
		return result;
	}

	private void sendTeleconsultationNonConfirmedEmail(Schedule schedule) throws IOException, MessagingException {
		Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
		if (practice != null) {
			Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
			if (schedule != null && schedule.getPatient() != null && schedule.getPerson() != null && !clinicDetails.isEmpty()) {
				EmailUtil.sendTeleConsultationAppointmentNonConfirmationMail(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails);
			}
		}
	}

	private void sentAppointmentConfirmationEmail(Schedule schedule) throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException {
		NotificationSetup notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[] { "status" }, new Object[] { NotificationSetup.STATUS.SCHEDULED });
		String cronExpression = CronExpressionGenerator.generateCronExpressionGivenDateParameters(true, new Date(), notificationSetup.getTriggetPointValue());
		if (notificationSetup.isNotificationRequired() && notificationSetup.isPatientRole()) {
			Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
			if (practice != null) {
				Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
				boolean sendSMS = Boolean.FALSE;
				if (notificationSetup.isBySMS() && !schedule.isWalkinAppointment())
					sendSMS = Boolean.TRUE;
				notificationTaskExecutor.prepareDetailsAndNotifyAppointmentSchedule(schedule, cronExpression, notificationSetup.isByEmail(), sendSMS, clinicDetails);
			}
		}
	}

	private void configureReminderEmailForAppointment(Schedule currentSchedule) throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException {
		NotificationSetup notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[]{"status"}, new Object[]{NotificationSetup.STATUS.FIRST_REMINDER});
		setUpReminderOfAppointment(currentSchedule, notificationSetup);
		notificationSetup = commonCrudService.findUniqueByEquality(NotificationSetup.class, new String[] { "status" }, new Object[] { NotificationSetup.STATUS.SECOND_REMINDER });
		setUpReminderOfAppointment(currentSchedule, notificationSetup);
	}

	private void setUpReminderOfAppointment(Schedule currentSchedule, NotificationSetup notificationSetup) throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException {
		LocalDateTime appointmentDate = furnishAppointmentDate(currentSchedule.getStartDate(), currentSchedule.getStartTime());
		int hour = notificationSetup.getTriggetPointValue();
		LocalDateTime localDateTime = new LocalDateTime();
		localDateTime = localDateTime.plusMinutes(hour);
		if (localDateTime.isBefore(appointmentDate)) {
			String cronExpression = CronExpressionGenerator.generateCronExpressionGivenDateParameters(false, appointmentDate.toDate(), notificationSetup.getTriggetPointValue());
			if (notificationSetup.isNotificationRequired() && notificationSetup.isPatientRole()) {
				Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
				if (practice != null) {

					/*slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{currentSchedule.getVisitType().getName()});
					if (slotType == null) {
						slotType = commonCrudService.findUniqueByEquality(SlotType.class, new String[]{"name"}, new Object[]{"Consult Visit"});
					}

					List visitTypeSoapModuleList = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider","slotType"}, new Object[]{currentSchedule.getPerson(),slotType});
					if((UtilValidator.isNotEmpty(visitTypeSoapModuleList)) && (!((VisitTypeSoapModule)visitTypeSoapModuleList.get(0)).isSmartService())) {*/
					if (!currentSchedule.isMobileOrPatinetPortal()){
						Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
						boolean sendSMS = Boolean.FALSE;
						if (notificationSetup.isBySMS() && !currentSchedule.isWalkinAppointment())
							sendSMS = Boolean.TRUE;
						notificationTaskExecutor.prepareDetailsAndNotifyAppointmentReminder(currentSchedule, cronExpression, notificationSetup.isByEmail(), sendSMS, clinicDetails);
					}
				}
			}
		}
	}

	private LocalDateTime furnishAppointmentDate(Date startDate, Date startTime) {
		LocalDateTime localDateTime = new LocalDateTime(startTime);
		LocalDateTime localDate = new LocalDateTime(startDate);
		return new LocalDateTime(localDate.getYear(), localDate.getMonthOfYear(), localDate.getDayOfMonth(), localDateTime.getHourOfDay(), localDateTime.getMinuteOfHour());
	}

	public boolean createQuickBook(final Schedule schedule, final PatientVisit patientVisit) throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException {
		String message = check(schedule);
		if (message != null) {
			EventListener confirmationListener = new EventListener() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (event.getData().equals(Messagebox.YES)) {
						createScheduleDirectly(schedule);
						saveVisit(schedule, patientVisit, null);
					}
				}
			};
			UtilMessagesAndPopups.showConfirmation(message, confirmationListener);
			return true;
		}
		createScheduleDirectly(schedule);
		saveVisit(schedule, patientVisit, null);
		// mailService.patientScheduleMail(schedule,Infrastructure.getUserLogin());
		configureReminderEmailForAppointment(schedule);
		sentAppointmentConfirmationEmail(schedule);
		return true;
	}

	private boolean createScheduleDirectly(Schedule schedule) {
		scheduleService.createSchedule(schedule);
		addScheduleDataToGrid(schedule);
		if (schedule.getWaitingList() != null)
			populateWaitingListLabel();
		UtilMessagesAndPopups.showSuccess();

		//communication loopback start
		if(schedule.getReferral() != null) {
			/*final Schedule schedule1 = schedule;
			new Thread(new Runnable() {
				@Override
				public void run() {*/
			try {
				Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(Infrastructure.getPractice().getTenantId());
				Map<String, Object> userLoginMap = AfyaServiceConsumer.getUserLoginByTenantId(schedule.getReferral().getTenantId());
				if ((userLoginMap != null) && (userLoginMap.get("languagePreference") != null)) {
					clinicDetails.put("languagePreference", userLoginMap.get("languagePreference").toString());
				}

					/*TenantIdHolder.setTenantId(schedule.getReferral().getTenantId());
					String mobileNumber = commonCrudService.getById(Provider.class, schedule.getReferralClinicDoctorTransient().getId()).getContacts().getMobileNumber();
					com.nzion.hibernate.ext.multitenant.TenantIdHolder.setTenantId(Infrastructure.getPractice().getTenantId());*/

				clinicDetails.put("key", TemplateNames.WALKIN_PATIENT_APPOINTMENT_SMS_REFERRAL_DOC.name());
				clinicDetails.put("forDoctor", new Boolean(false));
				clinicDetails.put("forAdmin", new Boolean(true));
				clinicDetails.put("mobileNumber", schedule.getReferralDocMobileNo());
				SmsUtil.sendStatusSms(schedule, clinicDetails);
			}catch (Exception e){
				e.printStackTrace();
			}
				/*}
			}).start();*/
		}
		//communication loopback end

		return true;
	}

	public void cancelSchedule(Schedule schedule) {
		if (!STATUS.SCHEDULED.equals(schedule.getStatus())) {
			UtilMessagesAndPopups.showError("A schedule with SCHEDULED status can only be cancelled.");
			return;
		}
		scheduleService.cancelSchedule(selectedComponent.getScheduleId());
		Component vbox = root.getFellow("schedulesVbox");
		AppointmentGrid grid = (AppointmentGrid) vbox.getFirstChild();
		grid.removeScheduleData(selectedComponent);
	}

	public void cancelSchedule() {
		Schedule schedule = selectedComponent.getSchedule();
		cancelSchedule(schedule);
	}

	public void paste() throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException, IOException, MessagingException {
		if (clipboard != null) {
			pasteSchedule();
			return;
		}
		Session session = Executions.getCurrent().getSession();
		Patient copiedPatient = (Patient) session.getAttribute("COPY_PATIENT");
		if (copiedPatient == null)
			throw new RuntimeException("Please copy an existing schedule or a patient to paste.");
		session.removeAttribute("COPY_PATIENT");
		pastePatient(copiedPatient);
	}

	public AppointmentListcell pasteSchedule() throws ClassNotFoundException, NoSuchMethodException, ParseException, SchedulerException, IOException, MessagingException {
		if (selectedComponent == null) {
			UtilMessagesAndPopups.showError("Select a slot first");
			return null;
		}
		Schedule copied = clipboard.getSchedule();
		Schedule choosen = selectedComponent.getSchedule();
		choosen.populateFromSchedule(copied);
		boolean b = (choosen.getId() == null) ? createSchedule(choosen) : updateSchedule(choosen);

		updatePrice(choosen);
		if( "Consult Visit".equals(choosen.getVisitType().getName()) ){
		   if( choosen.isFromMobileApp() && Tentative.Paid.toString().equals(choosen.getTentativeStatus()) ){
			 generateRescheduleInvoiceForRCM(choosen);
		   }
		}else{
			 generateRescheduleInvoiceForRCM(choosen);
		}

		addScheduleDataToGrid(choosen);
		AppointmentListcell oldClipboard = clipboard;
		clipboard = null;
		return oldClipboard;
	}

	public void pastePatient(Patient patient) {
		selectedComponent.getSchedule().setPatient(patient);
		Events.postEvent("onDoubleClick", selectedComponent, null);
	}

	public boolean reScheduleHere() throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException, IOException, MessagingException {
		boolean canCreateAppointment = true;

		Schedule schedule = clipboard.getSchedule();
		if (this.isPatientRescheduling()) {
			canCreateAppointment = updatePatientRescheduling(schedule);
			if (!canCreateAppointment)
				return false;
		}
		if (this.isClinicRescheduling()) {
			canCreateAppointment = updateClinicRescheduling(schedule);
			if (!canCreateAppointment)
				return false;
		}
		if (clipboard == null) {
			UtilMessagesAndPopups.showError("Select an existing schedule first.");
			return false;
		}

		if (!STATUS.SCHEDULED.equals(schedule.getStatus())) {
			UtilMessagesAndPopups.showError("A schedule with SCHEDULED status can only be Rescheduled");
			return false;
		}
		selectedComponent = pasteSchedule();
		if (selectedComponent == null)
			return false;

		cancelSchedule();
		selectedComponent = null;
        //new code start for sms
        Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(Infrastructure.getPractice().getTenantId());
        Map<String, Object> adminUserLogin = AfyaServiceConsumer.getUserLoginByUserName(Infrastructure.getPractice().getAdminUserLogin().getUsername());
		clinicDetails.put("languagePreference", adminUserLogin.get("languagePreference"));
        clinicDetails.put("key","");
        clinicDetails.put("reSchedule",new Boolean(true));
        //SmsUtil.sendRescheduleConfirmationSms(schedule, clinicDetails);
        clinicDetails.put("status", "reschedule");
        createDataForSms(schedule, clinicDetails, adminUserLogin);

        //new code end for sms
        return true;

	}

	private BigDecimal updatePrice(Schedule schedule) {
		RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
		Set<SchedulingPreference> schedulingPreferences = new HashSet<>(commonCrudService.findByEquality(SchedulingPreference.class, new String[] { "rcmPreference" }, new Object[] { rcmPreference }));
		consultationCharges = getProviderPrice(((Provider) schedule.getPerson()), schedule.getPatient(), schedule.getVisitType());
		BigDecimal convFee = BigDecimal.ZERO;
		BigDecimal advAmount = BigDecimal.ZERO;
		BigDecimal leadTime = BigDecimal.ZERO;
		for (SchedulingPreference schedulingPreference : schedulingPreferences) {
			if (schedule.getVisitType().getName().equals("Premium Visit")) {
				if (RCMVisitType.PREMIUM_APPOINTMENT.equals(schedulingPreference.getVisitType())) {
					convFee = schedulingPreference.getConvenienceFee();
					displayConvenienceFee = "Y".equals(schedulingPreference.getShowConvenienceFee());
					if (UtilValidator.isEmpty(convFee)) {
						convFee = percentage(consultationCharges, schedulingPreference.getConvenienceFeePercent());
					}
					advAmount = schedulingPreference.getAdvanceAmount();
					if (UtilValidator.isEmpty(advAmount)) {
						advAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
					}
					leadTime = schedulingPreference.getLeadTimeAllowed();
				}
			}
			if (schedule.getVisitType().getName().equals("Home Visit")) {
				if (RCMVisitType.HOME_VISIT_APPOINTMENT.equals(schedulingPreference.getVisitType())) {
					convFee = schedulingPreference.getConvenienceFee();
					displayConvenienceFee = "Y".equals(schedulingPreference.getShowConvenienceFee());
					if (UtilValidator.isEmpty(convFee)) {
						convFee = percentage(consultationCharges, schedulingPreference.getConvenienceFeePercent());
					}
					advAmount = schedulingPreference.getAdvanceAmount();
					if (UtilValidator.isEmpty(advAmount)) {
						advAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
					}
					leadTime = schedulingPreference.getLeadTimeAllowed();
				}
			}
			if (schedule.getVisitType().getName().equals("Tele Consultation Visit")) {
				if (RCMVisitType.TELE_CONSULT_APPOINTMENT.equals(schedulingPreference.getVisitType())) {
					convFee = schedulingPreference.getConvenienceFee();
					displayConvenienceFee = "Y".equals(schedulingPreference.getShowConvenienceFee());
					if (UtilValidator.isEmpty(convFee)) {
						convFee = percentage(consultationCharges, schedulingPreference.getConvenienceFeePercent());
					}
					advAmount = schedulingPreference.getAdvanceAmount();
					if (UtilValidator.isEmpty(advAmount)) {
						advAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
					}
					leadTime = schedulingPreference.getLeadTimeAllowed();
				}
			}

			if (schedule.getVisitType().getName().equals("Consult Visit")) {
				if (RCMVisitType.CONSULT_VISIT.equals(schedulingPreference.getVisitType())) {
					convFee = schedulingPreference.getConvenienceFee();
					displayConvenienceFee = "Y".equals(schedulingPreference.getShowConvenienceFee());
					if (UtilValidator.isEmpty(convFee)) {
						convFee = percentage(consultationCharges, schedulingPreference.getConvenienceFeePercent());
					}
					advAmount = schedulingPreference.getAdvanceAmount();
					if (UtilValidator.isEmpty(advAmount)) {
						advAmount = percentage(consultationCharges, schedulingPreference.getAdvanceAmountPercent());
					}
					leadTime = schedulingPreference.getLeadTimeAllowed();
				}
			}

		}
		List<Invoice> invoices = billingService.getFirstInvoice(schedule.getPatient());
		if (UtilValidator.isEmpty(invoices)) {
			BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
			registrationCharges = billingDisplayConfig.getRegistrationFee();
		}
		convenienceFee = convFee;
		registrationCharges = registrationCharges == null ? BigDecimal.ZERO : registrationCharges;
		totalAmount = consultationCharges.add(convenienceFee).add(registrationCharges);
		totalAdvAmount = advAmount.add(convenienceFee);
		return leadTime;
	}

	public void generateRescheduleInvoiceForRCM(Schedule currentSchedule) {
		RCMPreference rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
		BigDecimal totalCosultFeeConvFee = consultationCharges.add(convenienceFee);
		BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
		Long itemId = (System.currentTimeMillis() + currentSchedule.getId());
		final Invoice invoice = new Invoice(itemId.toString(), PatientSoapNote.class.getName(), (Provider) currentSchedule.getPerson(), currentSchedule.getPatient(), currentSchedule.getLocation());
		invoice.setInvoiceType(InvoiceType.OPD);
		invoice.setSchedule(currentSchedule);
		List<Invoice> invoices = billingService.getFirstInvoice(currentSchedule.getPatient());
		if (UtilValidator.isEmpty(invoices) && billingDisplayConfig != null) {
			InvoiceItem invItem = new InvoiceItem(invoice, currentSchedule.getId().toString(), InvoiceType.OPD_REGISTRATION, "Registration Charges", 1, null, PatientSoapNote.class.getName());
			if (billingDisplayConfig.getRegistrationFee() != null) {
				invItem.init(billingDisplayConfig.getRegistrationFee(), billingDisplayConfig.getCurrency().getCode(), new Money(billingDisplayConfig.getRegistrationFee(), convertTo()), new Money(billingDisplayConfig.getRegistrationFee(), convertTo()), 0);
				invItem.setCopayAmount(invItem.getGrossAmount());
				invoice.addInvoiceItem(invItem);
				if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(billingDisplayConfig.getRegistrationFee()), convertTo()));
				else
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(billingDisplayConfig.getRegistrationFee(), convertTo()));
			}
		}
		RCMVisitType rcmVisitType = null;
		if (currentSchedule.getVisitType().getName().equals("Premium Visit"))
			rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
		if (currentSchedule.getVisitType().getName().equals("Home Visit"))
			rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
		if (currentSchedule.getVisitType().getName().equals("Tele Consultation Visit"))
			rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
		if (currentSchedule.getVisitType().getName().equals("Consult Visit"))
			rcmVisitType = RCMVisitType.CONSULT_VISIT;

		SchedulingPreference schedulingPreference = commonCrudService.findUniqueByEquality(SchedulingPreference.class, new String[] { "rcmPreference", "visitType" }, new Object[] { rcmPreference, rcmVisitType });

		if (schedulingPreference != null && "Y".equals(schedulingPreference.getShowConvenienceFee())) {
			itemId = (System.currentTimeMillis() + currentSchedule.getId());
			String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " + currentSchedule.getVisitType().getName();

			if (invoiceItemDescription.contains("Premium")){
				invoiceItemDescription = "Premium Consultation Afya Smart Service";
			}else if (invoiceItemDescription.contains("Tele")){
				invoiceItemDescription = "Tele Consultation Afya Smart Service";
			}else if (invoiceItemDescription.contains("Home")){
				invoiceItemDescription = "Home Visit Afya Smart Service";
			}else if (invoiceItemDescription.contains("Consultation") && currentSchedule.isFromMobileApp())
				invoiceItemDescription = "Consultation Afya Smart Service";

			InvoiceItem consultationItem = new InvoiceItem(invoice, itemId.toString(), InvoiceType.OPD_CONSULTATION, invoiceItemDescription, 1, null, PatientSoapNote.class.getName());
			consultationItem.init(consultationCharges, billingDisplayConfig.getCurrency().getCode(), new Money(consultationCharges, convertTo()), new Money(consultationCharges, convertTo()), 0);
			invoice.addInvoiceItem(consultationItem);

			if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
				invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(consultationCharges), convertTo()));
			else
				invoice.setTotalAmount(new com.nzion.domain.product.common.Money(consultationCharges, convertTo()));

			InvoiceItem convenience = new InvoiceItem(invoice, itemId.toString(), InvoiceType.OPD_VALUE_ADDED, InvoiceType.OPD_VALUE_ADDED.getDescription(), 1, null, PatientSoapNote.class.getName());
			convenience.init(convenienceFee, billingDisplayConfig.getCurrency().getCode(), new Money(convenienceFee, convertTo()), new Money(convenienceFee, convertTo()), 0);
			invoice.addInvoiceItem(convenience);

			if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
				invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(convenienceFee), convertTo()));
			else
				invoice.setTotalAmount(new com.nzion.domain.product.common.Money(convenienceFee, convertTo()));
		} else {
			itemId = (System.currentTimeMillis() + currentSchedule.getId());
			String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " + currentSchedule.getVisitType().getName();

			if (invoiceItemDescription.contains("Premium")){
				invoiceItemDescription = "Premium Consultation Afya Smart Service";
			}else if (invoiceItemDescription.contains("Tele")){
				invoiceItemDescription = "Tele Consultation Afya Smart Service";
			}else if (invoiceItemDescription.contains("Home")){
				invoiceItemDescription = "Home Visit Afya Smart Service";
			}else if (invoiceItemDescription.contains("Consultation") && currentSchedule.isFromMobileApp())
				invoiceItemDescription = "Consultation Afya Smart Service";

			InvoiceItem consultationItem = new InvoiceItem(invoice, itemId.toString(), InvoiceType.OPD_CONSULTATION, invoiceItemDescription, 1, null, PatientSoapNote.class.getName());
			consultationItem.init(totalCosultFeeConvFee, billingDisplayConfig.getCurrency().getCode(), new Money(totalCosultFeeConvFee, convertTo()), new Money(totalCosultFeeConvFee, convertTo()), 0);
			invoice.addInvoiceItem(consultationItem);

			if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
				invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(totalCosultFeeConvFee), convertTo()));
			else
				invoice.setTotalAmount(new com.nzion.domain.product.common.Money(totalCosultFeeConvFee, convertTo()));
		}

		invoice.setMobileOrPatinetPortal(currentSchedule.isMobileOrPatinetPortal());

		Invoice inv = commonCrudService.save(invoice);
		if (schedulingPreference != null) {
			depositAdvAmount(inv, currentSchedule);
			addTxnPaymentItem(inv);
		}

	}

	public void depositAdvAmount(Invoice inv, Schedule currentSchedule) {
		PatientDeposit patientDeposit = new PatientDeposit();
		patientDeposit.setStatus("Deposit");
		patientDeposit.setDepositAmount(totalAdvAmount);
		patientDeposit.setDepositDate(new Date());
		patientDeposit.setDepositMode("CASH");
		patientDeposit.setPatient(inv.getPatient());
		patientDeposit.setConvenienceFeeForPatientPortal(convenienceFee);
		patientDeposit.setTotalAvailableAmount(patientDeposit.getDepositAmount().add(calculateAmount(patientDeposit.getPatient())));
		patientDeposit.setCreatedPerson(Infrastructure.getLoggedInPerson());
		patientDeposit.setInvoice(inv);
		patientDeposit.setSchedule(currentSchedule);
		commonCrudService.save(patientDeposit);
		billingService.updatePatientDeposit(patientDeposit.getPatient(), patientDeposit.getDepositAmount());
	}

	public void addTxnPaymentItem(Invoice invoice) {
		InvoicePayment invoicePayment = new InvoicePayment();
		invoicePayment.setPaymentDate(new Date());
		Enumeration enumeration = commonCrudService.findUniqueByEquality(Enumeration.class, new String[] { "enumCode", "enumType" }, new Object[] { "ADVANCE_AMOUNT", "PAYMENT_MODE" });
		invoicePayment.setPaymentMethod(enumeration);
		invoicePayment.setAmount(new Money(totalAdvAmount));
		String enumCode = invoicePayment.getPaymentMethod().getEnumCode();
		if (enumCode.equals("ADVANCE_AMOUNT")) {
			invoicePayment.setPaymentType(PaymentType.OPD_ADVANCE_AMOUNT);
			billingService.updatePatientWithdraw(invoice.getPatient(), invoicePayment.getAmount().getAmount());
		}
		invoice.setInvoiceStatus(InvoiceStatusItem.INPROCESS.toString());
		invoice.addInvoicePayment(invoicePayment);
		invoice.getCollectedAmount().setAmount(invoice.getCollectedAmount().getAmount().add(invoicePayment.getAmount().getAmount()));
		commonCrudService.save(invoice);
		receivePayment(invoice);

	}

	public void receivePayment(Invoice invoice) {
		if (UtilValidator.isNotEmpty(invoice.getCollectedAmount().getAmount())) {
			if ((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == 1)) {
				return;
			}
			if (((invoice.getCollectedAmount().getAmount().compareTo(invoice.getTotalAmount().getAmount()) == -1))) {
				return;
			}
		}
		invoice.setInvoiceStatus(InvoiceStatusItem.RECEIVED.toString());
		UserLogin login = Infrastructure.getUserLogin();
		invoice.setCollectedByUser(login.getUsername());
		billingService.saveInvoiceStatus(invoice, InvoiceStatusItem.RECEIVED);
	}

	private BigDecimal calculateAmount(Patient patient) {
		BigDecimal advanceAmount = BigDecimal.ZERO;
		List<AcctgTransactionEntry> accTransDebit = commonCrudService.findByEquality(AcctgTransactionEntry.class, new String[] { "patientId", "transactionType", "debitOrCredit" }, new Object[] { patient.getId().toString(), AcctgTransTypeEnum.PATIENT_DEPOSIT, DebitCreditEnum.DEBIT });
		List<AcctgTransactionEntry> accTransCredit = commonCrudService.findByEquality(AcctgTransactionEntry.class, new String[] { "patientId", "transactionType", "debitOrCredit" }, new Object[] { patient.getId().toString(), AcctgTransTypeEnum.PATIENT_WITHDRAW, DebitCreditEnum.CREDIT });
		BigDecimal debitAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		BigDecimal creditAmount = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
		for (AcctgTransactionEntry acc : accTransDebit) {
			if (acc.getAmount() != null)
				debitAmount = debitAmount.add(acc.getAmount());
		}
		for (AcctgTransactionEntry acc : accTransCredit) {
			if (acc.getAmount() != null)
				creditAmount = creditAmount.add(acc.getAmount());
		}
		if (debitAmount.compareTo(BigDecimal.ZERO) > 0)
			advanceAmount = debitAmount.subtract(creditAmount);
		return advanceAmount;
	}

	private BigDecimal getProviderPrice(Provider provider, Patient patient, SlotType soapNoteType) {
		BigDecimal amount = BigDecimal.ZERO;

		String patientCategory = "01";
		String tariffCategory = "00";

		String visitType = soapNoteType != null ? soapNoteType.getId().toString() : "10005";
		Map<String, Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, visitType, provider.getId().toString(), tariffCategory, patientCategory, new Date());
		if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null)
			amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
		return amount;
	}

	private boolean updatePatientRescheduling(Schedule schedule) {
		PatientDeposit patientDeposit = commonCrudService.findUniqueByEquality(PatientDeposit.class, new String[] { "schedule" }, new Object[] { schedule });
		RCMVisitType rcmVisitType = null;
		if (schedule.getVisitType().getName().equals("Premium Visit"))
			rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Home Visit"))
			rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Tele Consultation Visit"))
			rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Consult Visit"))
			rcmVisitType = RCMVisitType.CONSULT_VISIT;
		PatientReschedulingPreference patientReschedulingPreference = commonCrudService.findUniqueByEquality(PatientReschedulingPreference.class, new String[] { "visitType" }, new Object[] { rcmVisitType });

		if (patientReschedulingPreference == null)
			return true;

		if (!Tentative.Paid.toString().equals(schedule.getTentativeStatus()) && !schedule.isFromMobileApp() 
				&& schedule.getVisitType().getName().equals("Consult Visit") )
			return true;

		BigDecimal reScheduleTime = patientReschedulingPreference.getReschedulingTime() == null ? BigDecimal.ZERO : patientReschedulingPreference.getReschedulingTime();
		Date scheduleDateTime = UtilDateTime.toDate(schedule.getStartDate().getMonth(), schedule.getStartDate().getDate(), schedule.getStartDate().getYear(), schedule.getStartTime().getHours(), schedule.getStartTime().getMinutes(), schedule.getStartTime().getSeconds());
		BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(new Date(), scheduleDateTime));

		if (hoursInterval.compareTo(reScheduleTime) < 0) {
			//UtilMessagesAndPopups.showError("Appointment cannot be rescheduled within " + reScheduleTime + " hrs");
			UtilMessagesAndPopups.showError("Sorry, appointment cant be rescheduled , please check Afya Policy.");
			return false;
		}

		if (patientDeposit != null) {
			BigDecimal depositAmount = patientDeposit.getDepositAmount();
			BigDecimal patientCancellationCharge = percentage(depositAmount, patientReschedulingPreference.getPatientCancellationChargeProviderPercent());
			BigDecimal afyaCancellationCharge = percentage(depositAmount, patientReschedulingPreference.getPatientCancellationChargeAfyaPercent());
			BigDecimal totalCancellationCharges = patientCancellationCharge.add(afyaCancellationCharge);
			BigDecimal totalRefundAmount = depositAmount.subtract(totalCancellationCharges);
			//depositAdvAmount(totalRefundAmount, schedule.getPatient());
			Invoice invoice = patientDeposit.getInvoice();

			invoice = addToPatientAccount(invoice, totalRefundAmount);

			AfyaClinicDeposit clinicDeposit = new AfyaClinicDeposit(invoice, patientCancellationCharge, ClinicDepositType.CLINIC);
			AfyaClinicDeposit afyaClinicDeposit = new AfyaClinicDeposit(invoice, afyaCancellationCharge, ClinicDepositType.AFYA);

			commonCrudService.save(clinicDeposit);
			commonCrudService.save(afyaClinicDeposit);

			invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
			for (InvoiceItem ii : invoice.getInvoiceItems()) {
				cancelInvoiceLineItem(invoice, ii);
			}
			commonCrudService.save(invoice);
		}
		return true;
	}

	private boolean updateClinicRescheduling(Schedule schedule) {
		PatientDeposit patientDeposit = commonCrudService.findUniqueByEquality(PatientDeposit.class, new String[] { "schedule" }, new Object[] { schedule });
		RCMVisitType rcmVisitType = null;
		if (schedule.getVisitType().getName().equals("Premium Visit"))
			rcmVisitType = RCMVisitType.PREMIUM_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Home Visit"))
			rcmVisitType = RCMVisitType.HOME_VISIT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Tele Consultation Visit"))
			rcmVisitType = RCMVisitType.TELE_CONSULT_APPOINTMENT;
		if (schedule.getVisitType().getName().equals("Consult Visit"))
			rcmVisitType = RCMVisitType.CONSULT_VISIT;

		ClinicReschedulingPreference clinicReschedulingPreference = commonCrudService.findUniqueByEquality(ClinicReschedulingPreference.class, new String[] { "visitType" }, new Object[] { rcmVisitType });

		if (clinicReschedulingPreference == null)
			return true;

		if (!Tentative.Paid.toString().equals(schedule.getTentativeStatus()) && !schedule.isFromMobileApp()
				&& schedule.getVisitType().getName().equals("Consult Visit"))
			return true;

		BigDecimal reScheduleTime = clinicReschedulingPreference.getReschedulingTime() == null ? BigDecimal.ZERO : clinicReschedulingPreference.getReschedulingTime();
		Date scheduleDateTime = UtilDateTime.toDate(schedule.getStartDate().getMonth(), schedule.getStartDate().getDate(), schedule.getStartDate().getYear(), schedule.getStartTime().getHours(), schedule.getStartTime().getMinutes(), schedule.getStartTime().getSeconds());
		BigDecimal hoursInterval = new BigDecimal(UtilDateTime.getIntervalInHours(new Date(), scheduleDateTime));

		if (hoursInterval.compareTo(reScheduleTime) < 0) {
			//UtilMessagesAndPopups.showError("Appointment cannot be rescheduled within " + reScheduleTime + " hrs");
			UtilMessagesAndPopups.showError("Sorry, appointment cant be rescheduled , please check Afya Policy.");
			return false;
		}

		if (patientDeposit != null) {
			BigDecimal depositAmount = patientDeposit.getDepositAmount();
			BigDecimal clinicCancellationCharge = percentage(depositAmount, clinicReschedulingPreference.getClinicCancellationPercent());
			BigDecimal totalCancellationCharges = depositAmount.subtract(clinicCancellationCharge);
			//depositAdvAmount(totalCancellationCharges, schedule.getPatient());
			Invoice invoice = patientDeposit.getInvoice();

			invoice = addToPatientAccount(invoice, totalCancellationCharges);

			invoice.setInvoiceStatus(InvoiceStatusItem.CANCELLED.toString());
			for (InvoiceItem ii : invoice.getInvoiceItems()) {
				cancelInvoiceLineItem(invoice, ii);
			}
		}
		return true;
	}

	public void blockSlot(String comment) {
		addScheduleDataToGrid(scheduleService.blockSlot(selectedComponent.getSlot(), selectedComponent.getDate(), comment));
	}

	public void blockSlotDirectly(CalendarSlot slot, Date date, String comment) {
	}

	public void blockSlotsIgnoringExistingSchedules(ScheduleScanner scanner, String comment) {
		if (scanner == null)
			scanner = scan();
		scheduleService.blockSlotsIgnoringExistingSchedules(scanner, comment);
		populateScheduleGrid();
	}

	public void blockSlotsCancelingExistingSchedules(ScheduleScanner scanner, String comment) {
		if (scanner == null)
			scanner = scan();
		scheduleService.blockSlotsCancelingExistingSchedules(scanner, comment);
		populateScheduleGrid();
	}

	public void unBlockSlot() {
		scheduleService.deleteSchedule(selectedComponent.getSchedule().getId());
		Component vbox = root.getFellow("schedulesVbox");
		AppointmentGrid grid = (AppointmentGrid) vbox.getFirstChild();
		grid.removeScheduleData(selectedComponent);
	}

	public MultiBookValueObject getMultiBookValueObject() {
		return multiBookValueObject;
	}

	public void setMultiBookValueObject(MultiBookValueObject multiBookValueObject) {
		this.multiBookValueObject = multiBookValueObject;
	}

	public ScheduleScanner scan() {
		return scheduleService.scanSlots(multiBookValueObject);
	}

	public void saveMultiBookRequest(ScheduleScanner scanner) {
		List<Schedule> schedules = scheduleService.saveMultiBookRequest(scanner, multiBookValueObject);
		if (UtilValidator.isEmpty(schedules))
			return;
		Date endDate = viewDataDelegator.getEndDate();
		for (Schedule schedule : schedules)
			if (!schedule.getStartDate().before(selectedDate) && !schedule.getStartDate().after(endDate))
				addScheduleDataToGrid(schedule);
	}

	public void forceInsert() {
		addScheduleDataToGrid(scheduleService.forceInsert(selectedComponent.getSlot(), selectedComponent.getDate()));
	}

	public void deleteForceInsert() {
		scheduleService.deleteSchedule(selectedComponent.getSchedule().getId());
		populateScheduleGrid();
	}

	public void deleteAllForceInserteds() {
		scheduleService.deleteAllForceInserteds(person, location, selectedDate, viewDataDelegator.getEndDate());
		populateScheduleGrid();
		UtilMessagesAndPopups.showSuccess();
	}

	public void openWaitingListWindow() {
		Window wlModalWindow = (Window) Executions.createComponents("/appointment/schedule-waiting-list.zul", null, null);
		wlModalWindow.addEventListener("onDetach", new org.zkoss.zk.ui.event.EventListener() {
			public void onEvent(Event event) throws Exception {
				populateWaitingListLabel();
			}
		});
	}

	public void sendToWaitingList() {
		Schedule schedule = selectedComponent.getSchedule();
		if (!STATUS.SCHEDULED.equals(schedule.getStatus())) {
			UtilMessagesAndPopups.showError("A schedule with SCHEDULED status can only be send back to wait list.");
			return;
		}
		scheduleService.sendToWaitingList(selectedComponent.getSchedule());
		Component vbox = root.getFellow("schedulesVbox");
		AppointmentGrid grid = (AppointmentGrid) vbox.getFirstChild();
		grid.removeScheduleData(selectedComponent);
		populateWaitingListLabel();
	}

	public Set<SlotAvailability> searchSchedule(ScheduleSearchValueObject scheduleSearchValueObject, Weekdays weekdays) {
		return scheduleService.searchAvailableSchedules(scheduleSearchValueObject, weekdays);
	}

	public List<Schedule> searchBookedSchedules(ScheduleSearchValueObject scheduleSearchValueObject) {
		return scheduleService.searchBookedSchedules(scheduleSearchValueObject);
	}

	private void addScheduleDataToGrid(Schedule schedule) {
		Component vbox = root.getFellowIfAny("schedulesVbox");
		if (vbox != null) {
			AppointmentGrid grid = (AppointmentGrid) vbox.getFirstChild();
			grid.scheduleBooked(schedule);
		}
		if (vbox == null)
			return;

	}

	public static interface ViewDataDelegator {

		void changeRelatedScreenDisplay();

		boolean isNotReady();

		Date getEndDate();

		AppointmentGridRenderer getGridRenderer();

		List<CalendarResourceAssoc> getAssociations();

		List<Schedule> getDetailedSchedules();
	}

	private class WeekViewDataDelegator implements ViewDataDelegator {

		public void changeRelatedScreenDisplay() {
			centerDateLabel.setValue(null);
			leftDateLabel.setValue(UtilDateTime.format(selectedDate));
			rightDateLabel.setValue(UtilDateTime.format(getEndDate()));
		}

		public boolean isNotReady() {
			return getPerson() == null || getLocation() == null;
		}

		public List<CalendarResourceAssoc> getAssociations() {
			return scheduleService.getCalendarAssociations(getSelectedDate(), getEndDate(), person, location, true);
		}

		public List<Schedule> getDetailedSchedules() {
			return scheduleService.getDetailedSchedules(person, location, getSelectedDate(), getEndDate());
		}

		public AppointmentGridRenderer getGridRenderer() {
			return new WeekAppointmentGridRenderer();
		}

		@Override
		public Date getEndDate() {
			return UtilDateTime.addDaysToDate(selectedDate, 6);
		}
	}

	private class MonthViewDataDelegator implements ViewDataDelegator {

		public void changeRelatedScreenDisplay() {

			centerDateLabel.setValue(null);
			leftDateLabel.setValue(UtilDateTime.format(selectedDate));
			rightDateLabel.setValue(UtilDateTime.format(getEndDate()));
		}

		public boolean isNotReady() {
			return getPerson() == null || getLocation() == null;
		}

		public List<CalendarResourceAssoc> getAssociations() {
			return scheduleService.getCalendarAssociations(selectedDate, getEndDate(), person, location, true);
		}

		public List<Schedule> getDetailedSchedules() {
			Date from = getSelectedDate();
			Date upto = UtilDateTime.addMonthsToDate(selectedDate, 1);
			return scheduleService.getDetailedSchedules(person, location, from, upto);
		}

		public AppointmentGridRenderer getGridRenderer() {
			return new MonthAppointmentGridRenderer();
		}

		@Override
		public Date getEndDate() {
			return UtilDateTime.addMonthsToDate(selectedDate, 1);
		}
	}

	private class DayViewDataDelegator implements ViewDataDelegator {

		public void changeRelatedScreenDisplay() {
			Component menuItem = root.getFellowIfAny("forceInsertMenuItem2");
			if (menuItem != null)
				menuItem.setVisible(true);
			menuItem = root.getFellowIfAny("forceInsertMenuItem1");
			if (menuItem != null)
				menuItem.setVisible(true);
			centerDateLabel.setValue(UtilDateTime.format(selectedDate) + "(" + ViewUtil.getFormattedName(person) + ")");
			leftDateLabel.setValue("");
			rightDateLabel.setValue("");
		}

		public boolean isNotReady() {
			return getPerson() == null || getLocation() == null;
		}

		public List<CalendarResourceAssoc> getAssociations() {
			return scheduleService.getCalendarAssociations(selectedDate, selectedDate, person, location, true);
		}

		public List<Schedule> getDetailedSchedules() {
			return scheduleService.getDetailedSchedules(person, location, selectedDate, selectedDate);
		}

		public AppointmentGridRenderer getGridRenderer() {
			return new DayAppointmentGridRenderer();
		}

		@Override
		public Date getEndDate() {
			return selectedDate;
		}
	}

	private class PortletViewDataDelegator extends DayViewDataDelegator {

		@Override
		public void changeRelatedScreenDisplay() {
		}

		@Override
		public boolean isNotReady() {
			return getPerson() == null;
		}

		@Override
		public AppointmentGridRenderer getGridRenderer() {
			return new PortletAppointmentGridRenderer();
		}
	}

	private class CustomViewDataDelegator implements ViewDataDelegator {

		public void changeRelatedScreenDisplay() {
			centerDateLabel.setValue(UtilDateTime.format(selectedDate));
			leftDateLabel.setValue(null);
			rightDateLabel.setValue(null);
		}

		public boolean isNotReady() {
			return scheduleCustomView == null;
		}

		public List<CalendarResourceAssoc> getAssociations() {
			List<CalendarResourceAssoc> result = new ArrayList<CalendarResourceAssoc>();
			result.addAll(scheduleService.getCalendarAssociations(scheduleCustomView.getPersons(), getSelectedDate()));
			return result;
		}

		public List<Schedule> getDetailedSchedules() {
			return scheduleService.getDetailedSchedules(scheduleCustomView.getPersons(), getSelectedDate());
		}

		public AppointmentGridRenderer getGridRenderer() {
			return new CustomAppointmentGridRenderer(scheduleCustomView);
		}

		@Override
		public Date getEndDate() {
			return selectedDate;
		}
	}

	public void changeToPortletMode(Person person, Location location, Component scheduleVbox) {
		root = scheduleVbox;
		scheduleVbox.setAttribute("controller", this);
		viewDataDelegator = new PortletViewDataDelegator();
		this.person = person;
		this.location = location;
		mode = "PORTLET";
		populateScheduleGrid();
	}

	public void openSoapNote(Schedule schedule) {
		STATUS status = schedule.getStatus();
		if (STATUS.SCHEDULED.equals(status) || STATUS.CANCELLED.equals(status)) {
			UtilMessagesAndPopups.showError("Encounter note can only be created for checked in schedules.");
			return;
		}
		Executions.getCurrent().sendRedirect("/soap/soapnote.zul?scheduleId=" + schedule.getId(), "_soapNote");
	}

	public void openSoapNote() {
		Schedule schedule = scheduleService.getSchedule(selectedComponent.getSchedule().getId());
		openSoapNote(schedule);
	}

	public Schedule getOrCreateSelectedSchedule() {
		if (selectedComponent != null)
			return selectedComponent.getSchedule();
		Schedule schedule = new Schedule();
		schedule.setStartDate(selectedDate);
		schedule.setPerson(person);
		schedule.setLocation(location);
		return schedule;
	}

	public void openSigninWindow(Schedule schedule) {
		if (STATUS.CHECKEDIN.equals(selectedStatus) && schedule.getVisitType().getName().trim().toUpperCase().startsWith("TELE CONSULTATION") && Tentative.Tentative.toString().equals(schedule.getTentativeStatus())) {
			UtilMessagesAndPopups.displayError("please confirm appointment to proceed");
			return;
		}
		if (STATUS.CHECKEDIN.equals(selectedStatus) || STATUS.PROCEDUREPENDING.equals(selectedStatus) || STATUS.EXAMINING.equals(selectedStatus)) {
			Executions.createComponents("/appointment/add-edit-patient-visit.zul", root, UtilMisc.toMap("schedule", schedule, "controller", this, "selectedStatus", selectedStatus));
		}
	}

	public void openSigninWindow(Schedule schedule, Combobox scheduleStatuses) {
		if (STATUS.CHECKEDIN.equals(selectedStatus) && schedule.getVisitType().getName().trim().toUpperCase().startsWith("TELE CONSULTATION") && Tentative.Tentative.toString().equals(schedule.getTentativeStatus())) {
			UtilMessagesAndPopups.displayError("please confirm appointment to proceed");
			return;
		}
		if (STATUS.CHECKEDIN.equals(selectedStatus) || STATUS.PROCEDUREPENDING.equals(selectedStatus) || STATUS.EXAMINING.equals(selectedStatus)) {
			Executions.createComponents("/appointment/add-edit-patient-visit.zul", root, UtilMisc.toMap("schedule", schedule, "controller", this, "selectedStatus", selectedStatus, "scheduleStatuses", scheduleStatuses));
		}
	}

	public void saveVisit(Schedule schedule, PatientVisit patientVisit, String priority) {
		schedule = commonCrudService.getById(Schedule.class, schedule.getId());
		if (priority != null)
			schedule.setPriority(priority);
		signinService.addVisitToSchedule(patientVisit, schedule);
		addScheduleDataToGrid(schedule);
	}

	public void generateInvoice(Schedule currentSchedule) {
		BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
		Long itemId = (System.currentTimeMillis() + currentSchedule.getId());
		// Long itemId = currentSchedule.getId();
		final Invoice invoice = new Invoice(itemId.toString(), SOAP_ITEM_TYPE, (Provider) currentSchedule.getPerson(), currentSchedule.getPatient(), currentSchedule.getLocation());
		invoice.setInvoiceType(InvoiceType.OPD);
		invoice.setSchedule(currentSchedule);
		List<Invoice> invoices = billingService.getFirstInvoice(currentSchedule.getPatient());
		if (UtilValidator.isEmpty(invoices) && billingDisplayConfig != null) {
			InvoiceItem invItem = new InvoiceItem(invoice, currentSchedule.getId().toString(), InvoiceType.OPD_REGISTRATION, "Registration Charges", 1, null, PatientSoapNote.class.getName());
			if (billingDisplayConfig.getRegistrationFee() != null) {
				invItem.init(billingDisplayConfig.getRegistrationFee(), billingDisplayConfig.getCurrency().getCode(), new Money(billingDisplayConfig.getRegistrationFee(), convertTo()), new Money(billingDisplayConfig.getRegistrationFee(), convertTo()), 0);
				invItem.setCopayAmount(invItem.getGrossAmount());
				invoice.addInvoiceItem(invItem);
				if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(billingDisplayConfig.getRegistrationFee()), convertTo()));
				else
					invoice.setTotalAmount(new com.nzion.domain.product.common.Money(billingDisplayConfig.getRegistrationFee(), convertTo()));
			}
		}
		itemId = (System.currentTimeMillis() + currentSchedule.getId());
		String invoiceItemDescription = InvoiceType.OPD_CONSULTATION.getDescription() + " - " + currentSchedule.getVisitType().getName();

		if (invoiceItemDescription.contains("Premium")){
			invoiceItemDescription = "Premium Consultation Afya Smart Service";
		}else if (invoiceItemDescription.contains("Tele")){
			invoiceItemDescription = "Tele Consultation Afya Smart Service";
		}else if (invoiceItemDescription.contains("Home")){
			invoiceItemDescription = "Home Visit Afya Smart Service";
		}else if (invoiceItemDescription.contains("Consultation") && currentSchedule.isFromMobileApp())
			invoiceItemDescription = "Consultation Afya Smart Service";

		InvoiceItem consultationItem = new InvoiceItem(invoice, itemId.toString(), InvoiceType.OPD_CONSULTATION, invoiceItemDescription, 1, null, PatientSoapNote.class.getName());
		BigDecimal amount = BigDecimal.ZERO;

		String patientCategory = "01";
		String tariffCategory = "01";
		if ("INSURANCE".equals(currentSchedule.getPatient().getPatientType())) {
			patientCategory = "02";
			Set<PatientInsurance> patientInsurances = currentSchedule.getPatient().getPatientInsurances();
			PatientInsurance patientInsurance = patientInsurances.iterator().next();
			// TODO Insurance for patient new changes
			// if(UtilValidator.isNotEmpty(patientInsurance.getInsuranceName())){
			// tariffCategory =
			// billingService.getTariffCodeByTariffName(patientInsurance.getInsuranceName());
			// }

		} else if ("CORPORATE".equals(currentSchedule.getPatient().getPatientType())) {
			patientCategory = "03";
			tariffCategory = currentSchedule.getPatient().getPatientCorporate().getTariffCategoryId();
		}

		SlotType slotType = currentSchedule.getVisitType();

		Map<String, Object> masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, slotType.getId().toString(), ((Provider) currentSchedule.getPerson()).getId().toString(), tariffCategory, patientCategory, new Date());
		if (UtilValidator.isNotEmpty(masterPrice) && masterPrice.get("BILLABLE_AMOUNT") != null)
			amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");

		VisitTypeSoapModule visitTypeSoapModule = commonCrudService.findUniqueByEquality(VisitTypeSoapModule.class, new String[] { "provider", "slotType" }, new Object[] { ((Provider) currentSchedule.getPerson()), slotType });
		if (visitTypeSoapModule.isVisitPolicy())
			amount = updateFollowUpCharges(currentSchedule.getPatient(), amount, ((Provider) currentSchedule.getPerson()), slotType.getName(), tariffCategory, patientCategory);

		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10005", ((Provider) currentSchedule.getPerson()).getId().toString(), tariffCategory, patientCategory, new Date());
			if (masterPrice == null) {
				masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10005", ((Provider) currentSchedule.getPerson()).getId().toString(), "00", "01", new Date());
			}
			if (masterPrice != null)
				amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
		}

		consultationItem.init(amount, billingDisplayConfig.getCurrency().getCode(), new Money(amount, convertTo()), new Money(amount, convertTo()), 0);

		if ("AMOUNT".equals(masterPrice.get("COPAY_TYPE"))) {
			consultationItem.setCopayAmount((BigDecimal) masterPrice.get("COPAY"));
		}

		if ("PERCENT".equals(masterPrice.get("COPAY_TYPE"))) {
			consultationItem.setCopayPercentage((BigDecimal) masterPrice.get("COPAY"));
		}

		invoice.addInvoiceItem(consultationItem);
		if (invoice.getTotalAmount() != null && invoice.getTotalAmount().getAmount() != null)
			invoice.setTotalAmount(new com.nzion.domain.product.common.Money(invoice.getTotalAmount().getAmount().add(amount), convertTo()));
		else
			invoice.setTotalAmount(new com.nzion.domain.product.common.Money(amount, convertTo()));

		commonCrudService.save(invoice);

		/*
		 * if ("patientVisit".equals(billingDisplayConfig.
		 * getIsConsultationPriceTriggered
		 * ())&&"yes".equals(billingDisplayConfig.
		 * getIsPromptReceptionistToCollectConsultation())) { Messagebox.show(
		 * "Do you want to collect the amount for the current booked appointment."
		 * , "Payment Confirm ?", Messagebox.YES | Messagebox.NO,
		 * Messagebox.QUESTION, new EventListener() {
		 * 
		 * @Override public void onEvent(Event event) throws Exception {
		 * 
		 * if ("onYes".equalsIgnoreCase(event.getName())) { if
		 * (Infrastructure.getUserLogin().getAuthorization().hasAnyRole(
		 * Roles.BILLING)){ Executions.getCurrent().sendRedirect(
		 * "/billing/billingTxnItem.zul?invoiceId=" + invoice.getId()); //
		 * Executions.sendRedirect("/portlets/appointment.zul"); } else
		 * com.nzion.util.UtilMessagesAndPopups.showError(
		 * "User doesn't have the rights to receive amount"); return; } else
		 * Executions.sendRedirect("/dashboards/frontDeskDashBoard.zul"); }
		 * 
		 * });
		 * 
		 * }
		 */
	}

	private BigDecimal updateFollowUpCharges(Patient patient, BigDecimal amount, Provider provider, String visitName, String tariffCategory, String patientCategory) {
		Integer followUpVisitDays = null;
		Integer followUpVisits = null;
		BigDecimal followUpVisitCharges = null;
		Map<String, Object> masterPrice = null;
		List<Invoice> invoices = commonCrudService.findByEquality(Invoice.class, new String[] { "patient" }, new Object[] { patient });
		// Revisit
		followUpVisitDays = provider.getRevisitDays();
		followUpVisits = provider.getRevisitVisits();
		followUpVisitCharges = provider.getRevisitCharges();
		masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10024", provider.getId().toString(), tariffCategory, patientCategory, new Date());

		if (invoices != null && invoices.size() != 0 && invoices.size() <= followUpVisits) {
			if (invoices.size() <= followUpVisitDays) {
				if (masterPrice != null) {
					amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
				} else {
					amount = followUpVisitCharges;
				}
			}
		}

		// Followup Visit
		masterPrice = billingService.getPriceFromMasterPriceConf("01", "01", "001", null, "10010", provider.getId().toString(), tariffCategory, patientCategory, new Date());
		followUpVisitDays = provider.getFollowUpVisitDays();
		followUpVisits = provider.getFollowUpVisits();
		followUpVisitCharges = provider.getFollowUpVisitCharges();

		if (invoices != null && invoices.size() != 0 && invoices.size() <= followUpVisits) {
			if (invoices.size() <= followUpVisitDays) {
				if (masterPrice != null) {
					amount = (BigDecimal) masterPrice.get("BILLABLE_AMOUNT");
				} else {
					amount = followUpVisitCharges;
				}
			}
		}

		// Free Visit
		followUpVisitDays = provider.getFreeVisitDays();
		followUpVisits = provider.getFreeVisits();
		followUpVisitCharges = provider.getFreeVisitCharges();
		Invoice lastInvoice = UtilValidator.isNotEmpty(invoices) ? invoices.get(invoices.size() - 1) : null;

		if (lastInvoice != null && lastInvoice.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) > 0 && followUpVisitDays > 0 && followUpVisits > 0) {
			amount = followUpVisitCharges;
		} else if (lastInvoice != null && lastInvoice.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) == 0) {

			Integer count = 1;
			Invoice lasInv = null;
			for (int i = invoices.size() - 1; i <= invoices.size(); i--) {
				Invoice inv = invoices.get(i);
				if (inv.getTotalAmount().getAmount().compareTo(BigDecimal.ZERO) == 0) {
					lasInv = inv;
					count++;
				} else {
					break;
				}
			}
			if (count <= followUpVisits) {
				amount = followUpVisitCharges;
			}

		}

		return amount;
	}

	public void openLabRecordPage() {
		Schedule schedule = scheduleService.getSchedule(selectedComponent.getSchedule().getId());
		if ((STATUS.CHECKEDIN.equals(schedule.getStatus()) || STATUS.PROCEDUREPENDING.equals(schedule.getStatus())) && schedule.getPatientLabOrder() != null)
			Executions.getCurrent().sendRedirect("/lab/processLabTestRequest.zul?patientlabOrderId=" + schedule.getPatientLabOrder().getId(), "_labOrder");
	}

	public void setSigninService(SigninService signinService) {
		this.signinService = signinService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
		this.soapNoteService = soapNoteService;
	}

	public STATUS getSelectedStatus() {
		return selectedStatus;
	}

	public void setSelectedStatus(STATUS selectedStatus) {
		this.selectedStatus = selectedStatus;
	}

	public AppointmentListcell getClipboard() {
		return clipboard;
	}

	public void setClipboard(AppointmentListcell clipboard) {
		this.clipboard = clipboard;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	private static final long serialVersionUID = 1L;

	public Currency convertTo() {
		BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
		String currency = billingDisplayConfig.getCurrency().getCode();
		Currency defaultCurrency = Currency.getInstance(currency);
		return defaultCurrency;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void prepareDetailsAndEmailAppoinmentSchedule(Schedule schedule) {
		Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
		if (practice != null) {
			Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
			try {
				if (schedule != null && schedule.getPatient() != null && schedule.getPerson() != null && !clinicDetails.isEmpty())
					EmailUtil.sendAppointmentConfirmationMail(schedule, schedule.getPatient(), schedule.getPerson(), clinicDetails);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendNotificationForHighPriorityPatient(Schedule currentSchedule) {
		Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
		if (practice != null && currentSchedule.getPriority().equals("High")) {
			Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(String.valueOf(practice.getTenantId()));
			try {
				if (currentSchedule != null && currentSchedule.getPatient() != null && currentSchedule.getPerson() != null && !clinicDetails.isEmpty())
					EmailUtil.sendNotificationToProviderForHighPriorityPatient(currentSchedule, currentSchedule.getPatient(), currentSchedule.getPerson(), clinicDetails);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}

	public ProviderAvailability getProviderAvailability() {
		return providerAvailability;
	}

	public void setProviderAvailability(ProviderAvailability providerAvailability) {
		this.providerAvailability = providerAvailability;
	}

	public NotificationTaskExecutor getNotificationTaskExecutor() {
		return notificationTaskExecutor;
	}

	public void setNotificationTaskExecutor(NotificationTaskExecutor notificationTaskExecutor) {
		this.notificationTaskExecutor = notificationTaskExecutor;
	}

	public boolean isPatientRescheduling() {
		return patientRescheduling;
	}

	public void setPatientRescheduling(boolean patientRescheduling) {
		this.patientRescheduling = patientRescheduling;
	}

	public boolean isClinicRescheduling() {
		return clinicRescheduling;
	}

	public void setClinicRescheduling(boolean clinicRescheduling) {
		this.clinicRescheduling = clinicRescheduling;
	}

    public void createDataForSms(Schedule schedule, Map<String, Object> clinicDetails, Map<String, Object> adminUserLogin) {
        try {

			ArrayList<HashMap<String, Object>> adminList = AfyaServiceConsumer.getAllAdminByTenantId();

            if (schedule.getStatus().equals(STATUS.CANCELLED)) {
                /*BigDecimal refundAdvanceAmountPercent = commonCrudService.getByUniqueValue(ClinicReschedulingPreference.class, "visitType", RCMVisitType.CONSULT_VISIT).getRefundAdvanceAmountPercent();
                Invoice invoice = commonCrudService.getByUniqueValue(Invoice.class, "schedule", schedule);
                String refundAmount = null;
                if (invoice != null) {
                    BigDecimal ONE_HUNDRED = new BigDecimal(100);
                    BigDecimal collectedAmount = invoice.getCollectedAmount().getAmount();
                    refundAmount = (collectedAmount.subtract(collectedAmount.multiply(refundAdvanceAmountPercent).divide(ONE_HUNDRED))).setScale(3, BigDecimal.ROUND_HALF_UP).toString();
                    ;
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
					//sms for doctor
					String clinicLanguagePreference = (clinicDetails.get("languagePreference") != null) ? clinicDetails.get("languagePreference").toString() : null;
					clinicDetails.put("forDoctor", new Boolean(true));
					clinicDetails.put("forAdmin", new Boolean(false));
					clinicDetails.put("key", TemplateNames.HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_SMS_DOCTOR.name());
					SmsUtil.sendStatusSms(schedule, clinicDetails);
					//sms to admin
					List<BillingDisplayConfig> billingDisplayConfigs = commonCrudService.getAll(BillingDisplayConfig.class);
					if ((UtilValidator.isNotEmpty(billingDisplayConfigs)) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin() != null) && (billingDisplayConfigs.get(0).getIsDoctorsNotificationToBeSentToAdmin().equals("yes"))){
						clinicDetails.put("key", TemplateNames.HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_SMS_DOCTOR.name());
						clinicDetails.put("forDoctor", new Boolean(false));
						clinicDetails.put("forAdmin", new Boolean(true));
						clinicDetails.put("isdCode", Infrastructure.getPractice().getAdminUserLogin().getPerson().getContacts().getIsdCode());

						Iterator iterator = adminList.iterator();
						while (iterator.hasNext()) {
							Map map = (Map) iterator.next();

							clinicDetails.put("mobileNumber", map.get("mobile_number"));
							clinicDetails.put("languagePreference", map.get("languagePreference"));

							SmsUtil.sendStatusSms(schedule, clinicDetails);
						}
					}


                    //sms for patient
                    clinicDetails.put("key", TemplateNames.HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_APPOINTMENT_DET.name());
                    clinicDetails.put("forDoctor", new Boolean(false));
					clinicDetails.put("forAdmin", new Boolean(false));
                    SmsUtil.sendStatusSms(schedule, clinicDetails);
                    //sms for patient
                    clinicDetails.put("key", TemplateNames.HOME_VISIT_REQUEST_CANCELLATION_BY_CLINIC_REFUND_DET.name());
					clinicDetails.put("forDoctor", new Boolean(false));
					clinicDetails.put("forAdmin", new Boolean(false));
                    SmsUtil.sendStatusSms(schedule, clinicDetails);

                    //new code for email start
                    clinicDetails.put("languagePreference", clinicLanguagePreference);
                    clinicDetails.put("firstName", schedule.getPerson().getFirstName());
                    clinicDetails.put("lastName", schedule.getPerson().getLastName());
                    clinicDetails.put("subject", "Home Visit Request Cancelled By Clinic");
                    clinicDetails.put("template", "HOME_VISIT_APPOINTMENT_CANCELLED_BY_CLINIC_EMAIL_DOCTOR");
                    clinicDetails.put("email", schedule.getPerson().getContacts().getEmail());
                    EmailUtil.sendNetworkContractStatusMail(clinicDetails);
                    //end
                } else if (schedule.getVisitType().getName().equals("Premium Visit")) {
                    clinicDetails.put("key", TemplateNames.PREMIUM_APPOINTMENT_CANCELLED_BY_CLINIC.name());
                    SmsUtil.sendStatusSms(schedule, clinicDetails);
                }

            } else if (schedule.getStatus().equals(STATUS.CHECKEDOUT)) {
				clinicDetails.put("key", TemplateNames.APPOINTMENT_CHECKEDOUT_SMS_PATIENT.name());
				clinicDetails.put("forDoctor", new Boolean(false));
				clinicDetails.put("forAdmin", new Boolean(false));
				SmsUtil.sendStatusSms(schedule, clinicDetails);
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}