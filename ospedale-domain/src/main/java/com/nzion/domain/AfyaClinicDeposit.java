package com.nzion.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.Invoice;

@Entity
public class AfyaClinicDeposit extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private Provider provider;
	
	private Patient patient;
	
	private BigDecimal depositAmount;
	
	private Invoice invoice;
	
	private ClinicDepositType clinicDepositType;
	
	public AfyaClinicDeposit(){}
	
	public AfyaClinicDeposit(Invoice invoice,BigDecimal depositAmount,ClinicDepositType clinicDepositType){
		this.invoice = invoice;
		this.depositAmount = depositAmount;
		this.provider = (Provider) invoice.getConsultant();
		this.patient = invoice.getPatient();
		this.clinicDepositType = clinicDepositType;
	}
	

	@OneToOne
	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	@OneToOne
	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)") 
	public BigDecimal getDepositAmount() {
		return depositAmount;
	}

	public void setDepositAmount(BigDecimal depositAmount) {
		this.depositAmount = depositAmount;
	}

	@OneToOne
	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	@Enumerated(EnumType.STRING)
	public ClinicDepositType getClinicDepositType() {
		return clinicDepositType;
	}

	public void setClinicDepositType(ClinicDepositType clinicDepositType) {
		this.clinicDepositType = clinicDepositType;
	}




	public enum ClinicDepositType{
		CLINIC,AFYA;
	}
}
