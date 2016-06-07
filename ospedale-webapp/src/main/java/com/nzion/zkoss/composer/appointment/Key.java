package com.nzion.zkoss.composer.appointment;

import static com.nzion.util.UtilDateTime.IGNORE_GRANULARITY;

import java.util.Date;

import com.nzion.domain.CalendarSlot;
import com.nzion.domain.Schedule;
import com.nzion.util.UtilDateTime;

/**
 * @author Sandeep Prusty
 * Jun 3, 2010
 */
public class Key {
	private Date date;
	private CalendarSlot slot;

	public Key(CalendarSlot slot, Date date) {
	this.slot = slot;
	this.date = UtilDateTime.getDayStart(date);
	}

	public Key(Schedule schedule) {
	this.date = (Date) schedule.getStartDate();
	slot = new CalendarSlot((Date) schedule.getStartTime(), schedule.getEndTime(), schedule.getSequenceNum());
	}

	@Override
	public boolean equals(Object obj) {
	Key key = (Key) obj;
	return (date.getTime() / IGNORE_GRANULARITY) == (key.date.getTime() / IGNORE_GRANULARITY) && slot.compareTo(key.slot) == 0;
	}

	@Override
	public int hashCode() {
	return (int) (31 * (date.getTime() / IGNORE_GRANULARITY) + 29 * (slot.getStartTime().getTime() / IGNORE_GRANULARITY));
	}
}
