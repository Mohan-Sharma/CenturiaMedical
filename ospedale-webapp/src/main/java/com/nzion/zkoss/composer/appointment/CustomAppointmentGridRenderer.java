package com.nzion.zkoss.composer.appointment;

import static com.nzion.util.UtilDateTime.IGNORE_GRANULARITY;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;

import com.nzion.domain.CalendarResourceAssoc;
import com.nzion.domain.CalendarSlot;
import com.nzion.domain.Person;
import com.nzion.domain.Schedule;
import com.nzion.domain.screen.ScheduleCustomView;
import com.nzion.util.UtilValidator;
import com.nzion.util.ViewUtil;

/**
 * @author Sandeep Prusty
 * Jun 9, 2010
 */

@SuppressWarnings("unchecked")
public class CustomAppointmentGridRenderer implements AppointmentGridRenderer {

	private final ScheduleCustomView customView;

	private AppointmentGrid grid;

	private final Map<CustomKey, AppointmentListcell> slotCache = new HashMap<CustomKey, AppointmentListcell>();

	private final Map<Object, TreeSet> entitySlotMap = new HashMap<Object, TreeSet>();

	public CustomAppointmentGridRenderer(ScheduleCustomView customView) {
	this.customView = customView;
	}

	public void addScheduleData(Schedule schedule) {
	AppointmentListcell cell = slotCache.get(new CustomKey(schedule));
	if (cell == null) {
		createDummySlot(schedule);
		cell = slotCache.get(new CustomKey(schedule));
	}
	cell.book(schedule, ViewUtil.getFormattedName(schedule.getPatient()), "");
	}

	public void addScheduleData(Collection<Schedule> schedules) {
	if (UtilValidator.isEmpty(schedules)) return;
	for (Schedule schedule : schedules)
		addScheduleData(schedule);
	}

	public void buildHeader() {
	Listhead head = new Listhead();
	head.setParent(grid);
	Listheader timeHeader = new Listheader("Time");
	timeHeader.setParent(head);
	timeHeader.setWidth("80px");
	for (Person person : customView.getPersons()) {
		entitySlotMap.put(person, new TreeSet<CalendarSlot>());
		Listheader header = new Listheader(ViewUtil.getFormattedName(person));
		header.setAlign("center");
		header.setParent(head);
	}
	}

	public void removeScheduleData(Component component) {
	AppointmentListcell cell = (AppointmentListcell) component;
	cell.unBook();
	}

	public void setAppointmentGrid(AppointmentGrid grid) {
	this.grid = grid;
	}

	private void addAssocToSlotAssociationzMap(CalendarResourceAssoc assoc) {
	TreeSet<CalendarSlot> perEntitySlot = entitySlotMap.get(assoc.getPerson());
	perEntitySlot.addAll(assoc.getSlots());
	}

	public void setCalendarTemplateAssociations(Collection<CalendarResourceAssoc> assocs) {
	TreeSet<CalendarSlot> unionOfSlots = new TreeSet<CalendarSlot>();
	for (CalendarResourceAssoc assoc : assocs) {
		if (!assoc.isSatisfiedBy(grid.getDate())) continue;
		unionOfSlots.addAll(assoc.getSlots());
		addAssocToSlotAssociationzMap(assoc);
	}
	for (CalendarSlot slot : unionOfSlots)
		addSlot(slot);
	}

	private transient Date temporaryPreviousDate;

	private Listitem addSlot(CalendarSlot slot) {
	Listitem row = new Listitem();
	row.setParent(grid);
	row.setAttribute("slot", slot);
	AppointmentListcell timeCell = new AppointmentListcell();
	if (temporaryPreviousDate == null || !temporaryPreviousDate.equals(slot.getStartTime()))
		timeCell.makeTimeCell(slot.getStartTime());
	temporaryPreviousDate = slot.getStartTime();
	timeCell.setParent(row);
	for (Person person : customView.getPersons()) {
		boolean mapped = entitySlotMap.get(person).contains(slot);
		AppointmentListcell cell = !mapped ? new AppointmentListcell("XXX", grid) : new AppointmentListcell(grid
				.getDate(), slot, grid);
		cell.setParent(row);
		if (mapped) slotCache.put(new CustomKey(slot, slot.getAssociation().getPerson()), cell);
	}
	return row;
	}

	private void createDummySlot(Schedule schedule) {
	CalendarResourceAssoc nonExistentAssociation = new CalendarResourceAssoc(schedule.getPerson(), schedule
			.getLocation());
	CalendarSlot nonExistentSlot = new CalendarSlot(schedule, nonExistentAssociation);
	entitySlotMap.get(schedule.getPerson()).add(nonExistentSlot);
	Listitem row = addSlot(nonExistentSlot);
	grid.reArrangeRow(row);
	}

	class CustomKey {
		private final CalendarSlot slot;
		private final Person person;

		public CustomKey(CalendarSlot slot, Person person) {
		this.slot = slot;
		this.person = person;
		}

		public CustomKey(Schedule schedule) {
		slot = new CalendarSlot(schedule.getStartTime(), schedule.getEndTime(), schedule.getSequenceNum());
		person = schedule.getPerson();
		}

		@Override
		public boolean equals(Object obj) {
		CustomKey key = (CustomKey) obj;
		return slot.compareTo(key.slot) == 0 && (person == null || person.equals(key.person));
		}

		@Override
		public int hashCode() {
		return (int) (29 * (slot.getStartTime().getTime() / IGNORE_GRANULARITY) + 31 * (person.hashCode()));
		}
	}
}