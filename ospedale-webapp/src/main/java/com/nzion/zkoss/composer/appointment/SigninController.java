package com.nzion.zkoss.composer.appointment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nzion.zkoss.composer.OspedaleAutowirableComposer;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;

import com.nzion.domain.FixedAsset;
import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Provider;
import com.nzion.domain.Roles;
import com.nzion.domain.Schedule;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.Schedule.ScheduleType;
import com.nzion.domain.emr.PatientVisit;
import com.nzion.service.ProviderService;
import com.nzion.service.ScheduleService;
import com.nzion.service.SigninService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilMisc;
import com.nzion.util.UtilValidator;
import com.nzion.view.component.NameLabel;

/**
 * @author Sandeep Prusty
 * Jul 13, 2010
 */
public class SigninController extends OspedaleAutowirableComposer implements ListitemRenderer {

	private SigninService signinService;

	private ScheduleService scheduleService;

	private CommonCrudService commonCrudService;

	private Schedule newSchedule = new Schedule();

	private Date selectedDate = UtilDateTime.nowDateOnly();

	private final Map<String, List<Schedule>> statusWiseSchedules = new HashMap<String, List<Schedule>>();

	private final List<Schedule> allRelevantSchedules = new ArrayList<Schedule>();

	private List<FixedAsset> rooms;

	private List<Person> persons;

	private List<Provider> providers;

	private Listbox selectedListbox;

	private Listbox schedulesOfTheDayListBox;

	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	providers = ((ProviderService) Infrastructure.getSpringBean("providerService")).getAllProviders();
//	rooms = ((FixedAssetService) Infrastructure.getSpringBean("fixedAssetService")).getAllRooms();
//	persons = signinService.getAllConsultablePersons();
	selectedListbox = (Listbox) component.getFellow("signedinListbox");
	loadSchedules();
	}

	public void createNewAppointment(Component schedulesOfTheDayListBox, Component parent) {
	if (newSchedule.getPatient() == null || newSchedule.getLocation() == null || newSchedule.getPerson() == null) {
		UtilMessagesAndPopups.showError("Please fill all the fields");
		return;
	}
	newSchedule.setStartDate(selectedDate);
	newSchedule.setScheduleType(ScheduleType.CREATEDFROM_SIGNIN);
	scheduleService.saveOrUpdate(newSchedule);
	statusWiseSchedules.get(STATUS.SCHEDULED.toString()).add(newSchedule);
	allRelevantSchedules.add(newSchedule);
	newSchedule = new Schedule();
	parent.detach();
	if (schedulesOfTheDayListBox != null) Events.postEvent("onReloadRequest", schedulesOfTheDayListBox, null);
	}

	public void addPatientVisitToSchedule(PatientVisit theVisit, Schedule schedule) {
	Schedule upadatedSchedule = signinService.addVisitToSchedule(theVisit, schedule);
	List<Schedule> schedules = statusWiseSchedules.get(schedule.getStatus().toString());
	schedules.set(schedules.indexOf(schedule), upadatedSchedule);
	if (Infrastructure.getUserLogin().getAuthorization().hasAnyRole( Roles.PROVIDER, Roles.RECEPTION ))
		Events.postEvent("onReloadRequest", selectedListbox, null);
	}

	public void changeScheduleStatus(Schedule schedule, STATUS newStatus, PatientVisit visit) {
	STATUS oldStatus = schedule.getStatus();
	Schedule updatedSchedule = signinService.changeStatus(schedule, newStatus, visit);
	statusWiseSchedules.get(oldStatus.toString()).remove(schedule);
	statusWiseSchedules.get(newStatus.toString()).add(updatedSchedule);
	Events.postEvent("onReloadRequest", selectedListbox, null);
	}

	public Schedule saveSchedule(Schedule schedule) {
	return scheduleService.saveOrUpdate(schedule);
	}

	private void loadSchedules() {
	if (selectedDate == null) return;
//	statusWiseSchedules.put(STATUS.SCHEDULED.toString(), signinService.getSchedulesToBeSignedInFor(null, selectedDate));
//	statusWiseSchedules.put(STATUS.SIGNEDIN.toString(), signinService.getSignedInSchedulesFor(null, selectedDate));
//	statusWiseSchedules.put(STATUS.SIGNEDOUT.toString(), signinService.getSignedOutSchedulesFor(null, selectedDate));
//	statusWiseSchedules.put(STATUS.NOSHOW.toString(), signinService.getNoshowSchedulesFor(null, selectedDate));
	allRelevantSchedules.clear();
	for (List<Schedule> perStatusSchedules : statusWiseSchedules.values())
		if (UtilValidator.isNotEmpty(perStatusSchedules)) allRelevantSchedules.addAll(perStatusSchedules);
	}

	public void changePatient(String accountNumber) {
	Patient patient = commonCrudService.getByAccountNumber(Patient.class, accountNumber);
	if (patient == null) throw new RuntimeException("Patient not found");
	newSchedule.setPatient(patient);
	}

	public void renderTab(Component component, String url, String status) {
	component.getChildren().clear();
	Executions.getCurrent().createComponents(url, component,
			UtilMisc.toMap("schedules", statusWiseSchedules.get(status)));
	}

	public List<Schedule> getAllRelevantSchedules() {
	return allRelevantSchedules;
	}

	public Map<String, List<Schedule>> getStatusWiseSchedules() {
	return statusWiseSchedules;
	}

	public List<Person> getPersons() {
	return persons;
	}

	public Schedule getNewSchedule() {
	return newSchedule;
	}

	public List<Provider> getProviders() {
	return providers;
	}

	public List<FixedAsset> getRooms() {
	return rooms;
	}

	public Date getSelectedDate() {
	return selectedDate;
	}

	public void setSelectedDate(Date selectedDate) {
	this.selectedDate = selectedDate;
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
	if (STATUS.CHECKEDIN.equals(choosenStatus) || STATUS.PROCEDUREPENDING.equals(choosenStatus) || STATUS.READY_FOR_BILLING.equals(choosenStatus)) {
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
		actualListener.onEvent(event);
		}
	};
	UtilMessagesAndPopups.showConfirmation("Are you sure ?", yesNoListener);
	}

	@Override
	public void render(Listitem item, Object data,int index) throws Exception {
	Schedule schedule = (Schedule) data;
	item.setValue(data);
	Listcell patientAccNoCell = new Listcell(schedule.getPatient().getAccountNumber());
	patientAccNoCell.setParent(item);
	Listcell patientNameCell = new Listcell();
	patientNameCell.setParent(item);
	NameLabel patientNameLabel = new NameLabel();
	patientNameLabel.setObject(schedule.getPatient());
	patientNameLabel.setParent(patientNameCell);

	Listcell providerNameCell = new Listcell();
	providerNameCell.setParent(item);
	NameLabel providerNameLabel = new NameLabel();
	providerNameLabel.setObject(schedule.getPerson());
	providerNameLabel.setParent(providerNameCell);

	Listcell slotTypeCell = new Listcell(schedule.getVisitType() == null ? "" : schedule.getVisitType().getName());
	slotTypeCell.setParent(item);

	Listcell statusCell = new Listcell();
	Schedule.STATUS[] statuses = schedule.getStatus().getAllowedModifications();
	Combobox statusCombobox = new Combobox();
	Comboitem selectedComboitem = null;
	for (int i = 0; i < statuses.length; ++i) {
		Comboitem comboitem = new Comboitem(statuses[i].toString());
		comboitem.setValue(statuses[i]);
		comboitem.setParent(statusCombobox);
		if (schedule.getStatus().equals(statuses[i])) selectedComboitem = comboitem;
	}
	statusCombobox.setSelectedItem(selectedComboitem);
	statusCombobox.addEventListener("onChange", new EventListener() {
		public void onEvent(org.zkoss.zk.ui.event.Event event) {
		statusChangedInCombobox((Combobox) event.getTarget());
		}
	});
	statusCell.setParent(item);
	statusCombobox.setParent(statusCell);

	Listcell startTimeCell = new Listcell();
	startTimeCell.setParent(item);
	Timebox startTimebox = com.nzion.util.ViewUtil.getReadonlyTimebox(schedule.getStartTime());
	startTimebox.setParent(startTimeCell);

	Listcell signinTimeCell = new Listcell();
	signinTimeCell.setParent(item);
	Timebox signinTimebox = com.nzion.util.ViewUtil.getReadonlyTimebox(schedule.getSignedInTime());
	signinTimebox.setParent(signinTimeCell);

	Listcell signoutTimeCell = new Listcell();
	signoutTimeCell.setParent(item);
	Timebox signoutTimebox = com.nzion.util.ViewUtil.getReadonlyTimebox(schedule.getSignedOutTime());
	signoutTimebox.setParent(signoutTimeCell);
	}

	public EventListener getScheduleStatusChangeOkListener(final Combobox combobox, final Schedule schedule) {
	return new EventListener() {
		public void onEvent(Event event) throws Exception {
		STATUS newStatus = (STATUS) combobox.getSelectedItem().getValue();
		changeScheduleStatus(schedule, newStatus, (PatientVisit)event.getData());
		combobox.getChildren().clear();
		for (STATUS status : newStatus.getAllowedModifications()) {
			Comboitem comboitem = new Comboitem(status.toString());
			comboitem.setValue(status);
			comboitem.setParent(combobox);
		}
		combobox.setSelectedItem((Comboitem) combobox.getFirstChild());
		}
	};
	}

	public EventListener getScheduleStatusChangeCancelListener(final Combobox combobox, final Schedule schedule) {
	return new EventListener() {
		public void onEvent(Event event) throws Exception {
		for (int i = 0; i < combobox.getItemCount(); ++i) {
			Comboitem comboitem = combobox.getItemAtIndex(i);
			if (comboitem.getValue().equals(schedule.getStatus())) combobox.setSelectedItem(comboitem);
		}
		}
	};
	}

	public void getFilterData(Event event, boolean generic) {
	if (UtilValidator.isEmpty(getAllRelevantSchedules()) || UtilValidator.isEmpty(((Textbox) event.getTarget()).getValue())) {
		schedulesOfTheDayListBox.setModel(new ListModelList(getAllRelevantSchedules()));
		return;
	}
	String data = ((org.zkoss.zk.ui.event.InputEvent) event).getValue();
	((Textbox) event.getTarget()).setValue(((org.zkoss.zk.ui.event.InputEvent) event).getValue());
	List<Schedule> filteredSchedules = new LinkedList<Schedule>();
	for (Iterator<Schedule> itr = getAllRelevantSchedules().iterator(); itr.hasNext();) {
		Schedule schedule = itr.next();
		if (schedule.getPatient().getFirstName().toLowerCase().indexOf(data.toLowerCase()) >= 0	|| schedule.getPatient().getLastName().toLowerCase().indexOf(data.toLowerCase()) >= 0) {
			filteredSchedules.add(schedule);
		}
	}
	schedulesOfTheDayListBox.setModel(new ListModelList(filteredSchedules));
	}

	private static final long serialVersionUID = 1L;
}