package com.nzion.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
public class PharmacyOrder extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private String orderId;

	private String currencyUom;
	
	private BigDecimal totalAmount;
	
	private PharmacyOrderStatus pharOrderStatus;
	
	private Patient patient;
	
	private String pharmacyTennantId;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCurrencyUom() {
		return currencyUom;
	}

	public void setCurrencyUom(String currencyUom) {
		this.currencyUom = currencyUom;
	}

	public BigDecimal getTotalAmount() {
		if(totalAmount != null)
			return totalAmount.setScale(3, RoundingMode.HALF_UP);
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Enumerated(EnumType.STRING)
	public PharmacyOrderStatus getPharOrderStatus() {
		return pharOrderStatus;
	}

	public void setPharOrderStatus(PharmacyOrderStatus pharOrderStatus) {
		this.pharOrderStatus = pharOrderStatus;
	}
	
	@OneToOne
	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public String getPharmacyTennantId() {
		return pharmacyTennantId;
	}

	public void setPharmacyTennantId(String pharmacyTennantId) {
		this.pharmacyTennantId = pharmacyTennantId;
	}




	public enum PharmacyOrderStatus{
		ACTIVE("Active"),COMPLETED("Completed");
		private String name;
		PharmacyOrderStatus(String name){
		   this.name = name;
		}
		public String getName(){
			return name;
		}
	}

}
