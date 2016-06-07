package com.nzion.domain.emr.soap.vitalsign;

import javax.persistence.Column;

public class Respiration {

	private String unit;
	private Integer value;
	
	public Respiration() {
	this.unit="breaths per minute";
	}
	
	@Column(name="RESPIRATION_UNIT")
	public String getUnit() {
	return unit;
	}
	public void setUnit(String unit) {
	this.unit = unit;
	}

	@Column(name="RESPIRATION_VALUE")
	public Integer getValue() {
	return value;
	}
	public void setValue(Integer value) {
	this.value = value;
	}
	
	@Override
	public String toString() {
	StringBuilder buffer = new StringBuilder();
	if (value != null) {
		buffer.append(value).append(" ").append(unit);
	} 
	return buffer.toString();
	}
}
