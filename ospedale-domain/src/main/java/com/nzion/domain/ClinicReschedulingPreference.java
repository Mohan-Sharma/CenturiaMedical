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
public class ClinicReschedulingPreference extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private RCMPreference rcmPreference;
	
	private RCMVisitType visitType;
	
	private BigDecimal refundAdvanceAmountPercent;
	
	private BigDecimal clinicCancellationPercent;
	
	private BigDecimal reschedulingTime;
	
	public ClinicReschedulingPreference(){
		
	}
	
	public ClinicReschedulingPreference(RCMVisitType visitType){
		this.visitType = visitType; 
	}
	
	public static Set<ClinicReschedulingPreference> getEmptyLineItem(){
		Set<ClinicReschedulingPreference> linkedSet = new LinkedHashSet<ClinicReschedulingPreference>();
		for(RCMVisitType rmType : RCMVisitType.values()){
			ClinicReschedulingPreference clinicReschedulingPreference = new ClinicReschedulingPreference(rmType);
			linkedSet.add(clinicReschedulingPreference);
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
	public BigDecimal getRefundAdvanceAmountPercent() {
		return refundAdvanceAmountPercent;
	}

	public void setRefundAdvanceAmountPercent(BigDecimal refundAdvanceAmountPercent) {
		this.refundAdvanceAmountPercent = refundAdvanceAmountPercent;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getClinicCancellationPercent() {
		return clinicCancellationPercent;
	}

	public void setClinicCancellationPercent(BigDecimal clinicCancellationPercent) {
		this.clinicCancellationPercent = clinicCancellationPercent;
	}
	
	public BigDecimal getReschedulingTime() {
		return reschedulingTime;
	}

	public void setReschedulingTime(BigDecimal reschedulingTime) {
		this.reschedulingTime = reschedulingTime;
	}
	
	

}
