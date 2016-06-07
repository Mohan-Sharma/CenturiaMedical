package com.nzion.domain.emr.soap.vitalsign;

import javax.persistence.Embeddable;

@Embeddable
public class BloodPressure {

	private Integer systolic;
	private Integer diastolic;

	public Integer getSystolic() {
	return systolic;
	}

	public void setSystolic(Integer systolic) {
	this.systolic = systolic;
	}

	public Integer getDiastolic() {
	return diastolic;
	}

	public void setDiastolic(Integer diastolic) {
	this.diastolic = diastolic;
	}

	@Override
	public String toString() {
	if (systolic != null && diastolic != null) {
		return systolic + "/" + diastolic;
	}
	return "";
	}

}
