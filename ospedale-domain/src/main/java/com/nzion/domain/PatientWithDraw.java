package com.nzion.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
public class PatientWithDraw extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
    private Patient patient;
	
	private Date withdrawDate;
	
	private BigDecimal withdrawAmount;
	
	private BigDecimal totalAvailableAmount;
	
	private String withdrawMode;
	
	private String withdrawNotes;
	
	private String cancelReason;
	
	private String cancelNotes;
	
	private String status;
	
	private Person createdPerson;
	
	@OneToOne
	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	@Temporal(TemporalType.DATE)
	public Date getWithdrawDate() {
		return withdrawDate;
	}

	public void setWithdrawDate(Date withdrawDate) {
		this.withdrawDate = withdrawDate;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)") 
	public BigDecimal getWithdrawAmount() {
		return withdrawAmount;
	}

	public void setWithdrawAmount(BigDecimal withdrawAmount) {
		this.withdrawAmount = withdrawAmount;
	}
	
	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getTotalAvailableAmount() {
		return totalAvailableAmount;
	}

	public void setTotalAvailableAmount(BigDecimal totalAvailableAmount) {
		this.totalAvailableAmount = totalAvailableAmount;
	}

	public String getWithdrawMode() {
		return withdrawMode;
	}

	public void setWithdrawMode(String withdrawMode) {
		this.withdrawMode = withdrawMode;
	}

	public String getWithdrawNotes() {
		return withdrawNotes;
	}

	public void setWithdrawNotes(String withdrawNotes) {
		this.withdrawNotes = withdrawNotes;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	public String getCancelNotes() {
		return cancelNotes;
	}

	public void setCancelNotes(String cancelNotes) {
		this.cancelNotes = cancelNotes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	public Person getCreatedPerson() {
		return createdPerson;
	}

	public void setCreatedPerson(Person createdPerson) {
		this.createdPerson = createdPerson;
	}
	
	
	@Transient
	public boolean cancelled(){
		if("Cancelled".equals(status))
			return false;
		return true;
	}
}
