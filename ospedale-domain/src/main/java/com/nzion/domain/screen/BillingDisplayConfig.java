package com.nzion.domain.screen;

import java.math.BigDecimal;

import javax.persistence.*;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.UnitOfMeasurement;
import com.nzion.enums.ROUNDING_MODE;

/**
 * @author Sandeep Prusty
 *
 * 16-Sep-2011
 */

@Entity

public class BillingDisplayConfig extends IdGeneratingBaseEntity{

	private static final long serialVersionUID = 1L;

	private Integer decimalPoints = 3;
	
	private UnitOfMeasurement currency;
	
	private ROUNDING_MODE roundingMODE;
	
	private String inVoiceNumber;
	
	private BigDecimal registrationFee;

	private String defaultHisModuleId;
	
	private String pharmacyId;
	

	public String getIsConsultationPriceTriggered() {
		return isConsultationPriceTriggered;
	}

	public void setIsConsultationPriceTriggered(String isConsultationPriceTriggered) {
		this.isConsultationPriceTriggered = isConsultationPriceTriggered;
	}

	public String getIsPromptReceptionistToCollectConsultation() {
		return isPromptReceptionistToCollectConsultation;
	}

	public void setIsPromptReceptionistToCollectConsultation(
			String isPromptReceptionistToCollectConsultation) {
		this.isPromptReceptionistToCollectConsultation = isPromptReceptionistToCollectConsultation;
	}

	public String getAllowIfSmartServiceToBeBookedFromClinic() { return allowIfSmartServiceToBeBookedFromClinic;}

	public void setAllowIfSmartServiceToBeBookedFromClinic(String allowIfSmartServiceToBeBookedFromClinic) {
		this.allowIfSmartServiceToBeBookedFromClinic = allowIfSmartServiceToBeBookedFromClinic;
	}

	public String getIsDoctorsNotificationToBeSentToAdmin() { return isDoctorsNotificationToBeSentToAdmin;}

	public void setIsDoctorsNotificationToBeSentToAdmin(String isDoctorsNotificationToBeSentToAdmin) {
		this.isDoctorsNotificationToBeSentToAdmin = isDoctorsNotificationToBeSentToAdmin;
	}


	private BigDecimal noShowFee;
	
	private String isConsultationPriceTriggered ;
	
	private String isPromptReceptionistToCollectConsultation ;

	private String allowIfSmartServiceToBeBookedFromClinic;

	private String isDoctorsNotificationToBeSentToAdmin;

	public BigDecimal getRegistrationFee() {
		return registrationFee;
	}

	public void setRegistrationFee(BigDecimal registrationFee) {
		this.registrationFee = registrationFee;
	}

	public BigDecimal getNoShowFee() {
		return noShowFee;
	}

	public void setNoShowFee(BigDecimal noShowFee) {
		this.noShowFee = noShowFee;
	}

	public Integer getDecimalPoints() {
	return decimalPoints;
	}

	public void setDecimalPoints(Integer decimalPoints) {
	this.decimalPoints = decimalPoints;
	}

	@ManyToOne
	@JoinColumn(name="CURRENCY_ID")
	public UnitOfMeasurement getCurrency() {
	return currency;
	}

	public void setCurrency(UnitOfMeasurement currency) {
	this.currency = currency;
	}

	@Enumerated(EnumType.STRING)
	public ROUNDING_MODE getRoundingMODE() {
	return roundingMODE;
	}

	public void setRoundingMODE(ROUNDING_MODE roundingMODE) {
	this.roundingMODE = roundingMODE;
	}

	

	public String getInVoiceNumber() {
		return inVoiceNumber;
	}

	public void setInVoiceNumber(String inVoiceNumber) {
		this.inVoiceNumber = inVoiceNumber;
	}

	public String getDefaultHisModuleId() {
		return defaultHisModuleId;
	}

	public void setDefaultHisModuleId(String defaultHisModuleId) {
		this.defaultHisModuleId = defaultHisModuleId;
	}

	public String getPharmacyId() {
		return pharmacyId;
	}

	public void setPharmacyId(String pharmacyId) {
		this.pharmacyId = pharmacyId;
	}

	
	
	
}
