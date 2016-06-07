package com.nzion.domain;

import java.math.BigDecimal;

import javax.persistence.*;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.annot.SystemAuditable;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.soap.PatientLabOrder.STATUS;
import com.nzion.util.UtilReflection;
import java.math.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Week")
public class Week extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	private Boolean monday;
	private Boolean tuesday;
	private Boolean wednesday;
	private Boolean thursday;
	private Boolean friday;
	private Boolean saturday;
	private Boolean sunday;

	private List selectedDays;

	public Boolean getMonday() {
		return monday;
	}

	public void setMonday(Boolean monday) {
		this.monday = monday;
	}

	public Boolean getTuesday() {
		return tuesday;
	}

	public void setTuesday(Boolean tuesday) {
		this.tuesday = tuesday;
	}

	public Boolean getThursday() {
		return thursday;
	}

	public void setThursday(Boolean thursday) {
		this.thursday = thursday;
	}

	public Boolean getFriday() {
		return friday;
	}

	public void setFriday(Boolean friday) {
		this.friday = friday;
	}

	public Boolean getWednesday() {
		return wednesday;
	}

	public void setWednesday(Boolean wednesday) {
		this.wednesday = wednesday;
	}

	public Boolean getSaturday() {
		return saturday;
	}

	public void setSaturday(Boolean saturday) {
		this.saturday = saturday;
	}

	public Boolean getSunday() {
		return sunday;
	}

	public void setSunday(Boolean sunday) {
		this.sunday = sunday;
	}

	@Transient
	public List getSelectedDays() {
		List list = new ArrayList();

		if ((getMonday() != null) && (getMonday().equals(true))){
			list.add("Mon");
		}
		if ((getTuesday() != null) &&  (getTuesday().equals(true))){
			list.add("Tue");
		}
		if ((getWednesday() != null) &&  (getWednesday().equals(true))){
			list.add("Wed");
		}
		if ((getThursday() != null) &&  (getThursday().equals(true))){
			list.add("Thu");
		}
		if ((getFriday() != null) &&  (getFriday().equals(true))){
			list.add("Fri");
		}
		if ((getSaturday() != null) &&  (getSaturday().equals(true))){
			list.add("Sat");
		}
		if ((getSunday() != null) && (getSunday().equals(true))){
			list.add("Sun");
		}
		return list;
	}


}