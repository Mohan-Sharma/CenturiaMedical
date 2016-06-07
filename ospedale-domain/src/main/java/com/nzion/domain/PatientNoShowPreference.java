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
public class PatientNoShowPreference extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
    private RCMPreference rcmPreference;
	
	private RCMVisitType visitType;
	
	private BigDecimal refundClinicPercent;
	
	private BigDecimal refundAfyaPercent;
	
	public PatientNoShowPreference(){
		
	}
	
	public PatientNoShowPreference(RCMVisitType visitType){
		this.visitType = visitType;
	}

	public static Set<PatientNoShowPreference> getEmptyLineItem(){
		Set<PatientNoShowPreference> linkedSet = new LinkedHashSet<PatientNoShowPreference>();
		for(RCMVisitType rmType : RCMVisitType.values()){
			PatientNoShowPreference patiNoShowPreference = new PatientNoShowPreference(rmType);
			linkedSet.add(patiNoShowPreference);
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
	public BigDecimal getRefundClinicPercent() {
		return refundClinicPercent;
	}

	public void setRefundClinicPercent(BigDecimal refundClinicPercent) {
		this.refundClinicPercent = refundClinicPercent;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getRefundAfyaPercent() {
		return refundAfyaPercent;
	}

	public void setRefundAfyaPercent(BigDecimal refundAfyaPercent) {
		this.refundAfyaPercent = refundAfyaPercent;
	}
	
	
}
