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
public class SchedulingPreference extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private RCMPreference rcmPreference;
	
	private RCMVisitType visitType;
	
	private BigDecimal advanceAmountPercent;
	
	private BigDecimal advanceAmount;
	
	private BigDecimal convenienceFeePercent;
	
	private BigDecimal convenienceFee;
	
	private String showConvenienceFee;
	
	private BigDecimal leadTimeAllowed;
	
	private BigDecimal maxTimeAllowed;
	
	public SchedulingPreference(){
		
	}
	
	public SchedulingPreference(RCMVisitType visitType,String showConvenienceFee){
		this.visitType = visitType; 
		this.showConvenienceFee = showConvenienceFee;
	}
	
	public static Set<SchedulingPreference> getEmptyLineItem(){
		Set<SchedulingPreference> linkedSet = new LinkedHashSet<SchedulingPreference>();
		for(RCMVisitType rmType : RCMVisitType.values()){
			SchedulingPreference schedulingPreference = new SchedulingPreference(rmType,"Y");
			linkedSet.add(schedulingPreference);
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
	public BigDecimal getAdvanceAmountPercent() {
		return advanceAmountPercent;
	}

	public void setAdvanceAmountPercent(BigDecimal advanceAmountPercent) {
		this.advanceAmountPercent = advanceAmountPercent;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getAdvanceAmount() {
		return advanceAmount;
	}

	public void setAdvanceAmount(BigDecimal advanceAmount) {
		this.advanceAmount = advanceAmount;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getConvenienceFeePercent() {
		return convenienceFeePercent;
	}

	public void setConvenienceFeePercent(BigDecimal convenienceFeePercent) {
		this.convenienceFeePercent = convenienceFeePercent;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)")
	public BigDecimal getConvenienceFee() {
		return convenienceFee;
	}

	public void setConvenienceFee(BigDecimal convenienceFee) {
		this.convenienceFee = convenienceFee;
	}

	public String getShowConvenienceFee() {
		return showConvenienceFee;
	}

	public void setShowConvenienceFee(String showConvenienceFee) {
		this.showConvenienceFee = showConvenienceFee;
	}

	public BigDecimal getLeadTimeAllowed() {
		return leadTimeAllowed;
	}

	public void setLeadTimeAllowed(BigDecimal leadTimeAllowed) {
		this.leadTimeAllowed = leadTimeAllowed;
	}

	public BigDecimal getMaxTimeAllowed() {
		return maxTimeAllowed;
	}

	public void setMaxTimeAllowed(BigDecimal maxTimeAllowed) {
		this.maxTimeAllowed = maxTimeAllowed;
	}
	
}
