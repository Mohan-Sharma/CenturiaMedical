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
public class PatientReschedulingPreference extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
    private RCMPreference rcmPreference;
	
	private RCMVisitType visitType;
	
	private BigDecimal reschedulingTime;
	
	private BigDecimal patientCancellationChargeProviderPercent;
	
	private BigDecimal patientCancellationChargeAfyaPercent;
	
	private BigDecimal refundTrigger;
	
	public PatientReschedulingPreference(){
		
	}

	public PatientReschedulingPreference(RCMVisitType visitType){
		this.visitType = visitType; 
	}
	
	public static Set<PatientReschedulingPreference> getEmptyLineItem(){
		Set<PatientReschedulingPreference> linkedSet = new LinkedHashSet<PatientReschedulingPreference>();
		for(RCMVisitType rmType : RCMVisitType.values()){
			PatientReschedulingPreference patientReschedulingPreference = new PatientReschedulingPreference(rmType);
			linkedSet.add(patientReschedulingPreference);
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

	public BigDecimal getReschedulingTime() {
		return reschedulingTime;
	}

	public void setReschedulingTime(BigDecimal reschedulingTime) {
		this.reschedulingTime = reschedulingTime;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getPatientCancellationChargeProviderPercent() {
		return patientCancellationChargeProviderPercent;
	}

	public void setPatientCancellationChargeProviderPercent(
			BigDecimal patientCancellationChargeProviderPercent) {
		this.patientCancellationChargeProviderPercent = patientCancellationChargeProviderPercent;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getPatientCancellationChargeAfyaPercent() {
		return patientCancellationChargeAfyaPercent;
	}

	public void setPatientCancellationChargeAfyaPercent(
			BigDecimal patientCancellationChargeAfyaPercent) {
		this.patientCancellationChargeAfyaPercent = patientCancellationChargeAfyaPercent;
	}

	public BigDecimal getRefundTrigger() {
		return refundTrigger;
	}

	public void setRefundTrigger(BigDecimal refundTrigger) {
		this.refundTrigger = refundTrigger;
	}
	
	
}
