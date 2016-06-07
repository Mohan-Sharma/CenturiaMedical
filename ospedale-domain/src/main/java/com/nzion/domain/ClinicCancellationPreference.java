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
public class ClinicCancellationPreference extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private RCMPreference rcmPreference;
	
	private RCMVisitType visitType;
	
	private BigDecimal refundAdvanceAmountPercent;
	
	private BigDecimal clinicCancellationChargePercent;
	
	private BigDecimal cancellationTime;
	
	public ClinicCancellationPreference(){
		
	}
	
	public ClinicCancellationPreference(RCMVisitType visitType){
		this.visitType = visitType; 
	}
	
	public static Set<ClinicCancellationPreference> getEmptyLineItem(){
		Set<ClinicCancellationPreference> linkedSet = new LinkedHashSet<ClinicCancellationPreference>();
		for(RCMVisitType rmType : RCMVisitType.values()){
			ClinicCancellationPreference clinicCancellationPreference = new ClinicCancellationPreference(rmType);
			linkedSet.add(clinicCancellationPreference);
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
	public BigDecimal getClinicCancellationChargePercent() {
		return clinicCancellationChargePercent;
	}

	public void setClinicCancellationChargePercent(
			BigDecimal clinicCancellationChargePercent) {
		this.clinicCancellationChargePercent = clinicCancellationChargePercent;
	}

	public BigDecimal getCancellationTime() {
		return cancellationTime;
	}

	public void setCancellationTime(BigDecimal cancellationTime) {
		this.cancellationTime = cancellationTime;
	}

	
}
