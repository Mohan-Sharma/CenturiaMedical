package com.nzion.domain;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.nzion.domain.RCMPreference.RCMVisitType;
import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
public class PatientCancellationPreference extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private RCMPreference rcmPreference;
	
	private RCMVisitType visitType;
	
	private BigDecimal cancellationTime;
	
	private BigDecimal patientCancellationChargePercent;
	
	private BigDecimal patientCancelationChargeAfyePercent;
	
	private BigDecimal refundTrigger;
	
	public PatientCancellationPreference(){
		
	}
	
	public PatientCancellationPreference(RCMVisitType visitType){
		this.visitType = visitType;
	}
	
	public static Set<PatientCancellationPreference> getEmptyLineItem(){
		Set<PatientCancellationPreference> linkedSet = new LinkedHashSet<PatientCancellationPreference>();
		for(RCMVisitType rmType : RCMVisitType.values()){
			PatientCancellationPreference patientCancellationPreference = new PatientCancellationPreference(rmType);
			linkedSet.add(patientCancellationPreference);
		}
		return linkedSet;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	public RCMPreference getRcmPreference() {
		return rcmPreference;
	}

	public void setRcmPreference(RCMPreference rcmPreference) {
		this.rcmPreference = rcmPreference;
	}

	@Enumerated(EnumType.STRING)
	public RCMVisitType getVisitType() {
		return visitType;
	}

	public void setVisitType(RCMVisitType visitType) {
		this.visitType = visitType;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getCancellationTime() {
		return cancellationTime;
	}

	public void setCancellationTime(BigDecimal cancellationTime) {
		this.cancellationTime = cancellationTime;
	}
	
	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getPatientCancellationChargePercent() {
		return patientCancellationChargePercent;
	}

	public void setPatientCancellationChargePercent(
			BigDecimal patientCancellationChargePercent) {
		this.patientCancellationChargePercent = patientCancellationChargePercent;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getPatientCancelationChargeAfyePercent() {
		return patientCancelationChargeAfyePercent;
	}

	public void setPatientCancelationChargeAfyePercent(
			BigDecimal patientCancelationChargeAfyePercent) {
		this.patientCancelationChargeAfyePercent = patientCancelationChargeAfyePercent;
	}

	public BigDecimal getRefundTrigger() {
		return refundTrigger;
	}

	public void setRefundTrigger(BigDecimal refundTrigger) {
		this.refundTrigger = refundTrigger;
	}
	
}
