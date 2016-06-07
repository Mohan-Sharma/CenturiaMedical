package com.nzion.domain.emr.soap.vitalsign;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class Height {
	
	
	private boolean metric; 
	private String metricUnit;
	private Float metricValue;
	private Float englishValue;
	private String englishUnit;
	
	
	@Transient
	public boolean isMetric() {
		return metric;
	}

	public void setMetric(boolean metric) {
		this.metric = metric;
	}

	public Height() {
		this.metricUnit = "in";
		this.englishUnit = "cm";
	}

	@Column(name = "HEIGHT_METRIC_UNIT")
	public String getMetricUnit() {
		return metricUnit;
	}

	public void setMetricUnit(String metricUnit) {
		this.metricUnit = metricUnit;
	}

	@Column(name = "HEIGHT_METRIC_VALUE")
	public Float getMetricValue() {
		return metricValue;
	}

	public void setMetricValue(Float metricValue) {
		this.metricValue = metricValue;
		if (isMetric()) {
			if (metricValue != null)
				this.englishValue = new Float(Math.ceil(2.54 * (metricValue)));
		}
	}

	@Column(name = "HEIGHT_EN_VALUE")
	public Float getEnglishValue() {
		return englishValue;
	}

	public void setEnglishValue(Float englishValue) {
		if (!isMetric()) {
			this.englishValue = englishValue;
			if (englishValue != null)
				this.metricValue = new Float((englishValue) / 2.54);
		}
	}

	@Column(name = "HEIGHT_EN_UNIT")
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
		} else if (metricValue != null) {
			buffer.append(metricValue).append(" ").append(metricUnit);
		}
		int idx = buffer.indexOf(".0 ");
		if (idx > 0)
			buffer = buffer.delete(idx, idx + 2);
		return buffer.toString();
	}
}
