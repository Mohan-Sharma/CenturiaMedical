package com.nzion.domain.emr.soap.vitalsign;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Temperature {

	private String metricUnit;
	private Float metricValue;
	private Float englishValue;
	private String englishUnit;

	public Temperature() {
	this.metricUnit = "F";
	this.englishUnit = "C";
	}

	@Column(name = "TEMP_METRIC_UNIT")
	public String getMetricUnit() {
	return metricUnit;
	}

	public void setMetricUnit(String metricUnit) {
	this.metricUnit = metricUnit;
	}

	@Column(name = "TEMP_METRIC_VALUE")
	public Float getMetricValue() {
	return metricValue;
	}

	public void setMetricValue(Float metricValue) {
	if (metricValue != null) this.setEnglishValue(new Float(Math.ceil((metricValue-32)*5)/9));
	}

	@Column(name = "TEMP_EN_VALUE")
	public Float getEnglishValue() {
	return englishValue;
	}

	public void setEnglishValue(Float englishValue) {
	this.englishValue = englishValue;
	if (englishValue != null) 
		this.metricValue = new Float(Math.ceil(9 * englishValue)/5  + 32);
	}

	@Column(name = "TEMP_EN_UNIT")
	public String getEnglishUnit() {
	return englishUnit;
	}

	public void setEnglishUnit(String englishUnit) {
	this.englishUnit = englishUnit;
	}

	@Override
	public String toString() {
	StringBuilder buffer = new StringBuilder();
	if (englishValue != null) {
		buffer.append(englishValue).append(" ").append(englishUnit);
	} else
		if (metricValue != null) {
			buffer.append(metricValue).append(" ").append(metricUnit);
		}
	return buffer.toString();
	}
	
	public static void main(String[] args) {
	Float f = new Float(Math.floor((90-32)*5)/9);
	System.out.println(f);
	}
}
