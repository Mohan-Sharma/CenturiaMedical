package com.nzion.domain.emr.soap;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.nzion.util.UtilDateTime;

public class FollowUp implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer followUpDuration;

	private String followUpDurationType;

	private Date expectedDate;

	private Integer alertBefore;

	private String alertDurationType;

	public Integer getFollowUpDuration() {
	return followUpDuration;
	}

	public void setFollowUpDuration(Integer followUpDuration) {
	this.followUpDuration = followUpDuration;
	}

	public String getFollowUpDurationType() {
	return followUpDurationType;
	}

	public void setFollowUpDurationType(String followUpDurationType) {
	this.followUpDurationType = followUpDurationType;
	}

	@Override
	public String toString() {
	return followUpDuration.toString() + " " + followUpDurationType;
	}

	public Date generateExpectedDate(Date date) {
	return getFollowUpDuration() == null ? null : UtilDateTime.add(getFollowUpDurationType(), date, getFollowUpDuration());
	}

	@Temporal(TemporalType.DATE)
	public Date getExpectedDate() {
	return expectedDate;
	}

	public void setExpectedDate(Date expectedDate) {
	this.expectedDate = expectedDate;
	}

	public Integer getAlertBefore() {
	return alertBefore;
	}

	public void setAlertBefore(Integer alertBefore) {
	this.alertBefore = alertBefore;
	}

	public String getAlertDurationType() {
	return alertDurationType;
	}

	public void setAlertDurationType(String alertDurationType) {
	this.alertDurationType = alertDurationType;
	}

}
