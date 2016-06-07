package com.nzion.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.Invoice;


@Entity
public class PatientDeposit extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private Patient patient;
	
	private Date depositDate;
	
	private BigDecimal depositAmount;
	
	private BigDecimal totalAvailableAmount;
	
	private String depositMode;
	
	private String depositNotes;
	
	private String cancelReason;
	
	private String cancelNotes;
	
	private String status;
	
	private Person createdPerson;
	
	private String createdUser;
	
	private Invoice invoice;
	
	private Schedule schedule;
	
	private boolean returnToPatient;
	
	private BigDecimal convenienceFeeForPatientPortal;
	
	private String bankName;
	
	private String txnNumber;
	
	private Date chequeDate;
	
	
	private String portalPaymentId;
	
	private String transactionType;
	
	private Date transactionTimestamp;
	
	private String isysTrackingRef;
	
	private String payerType;
	
	private String paymentChannel;

	private String paymentId;
	
	
	@Temporal(TemporalType.DATE)
	public Date getDepositDate() {
		return depositDate;
	}

	public void setDepositDate(Date depositDate) {
		this.depositDate = depositDate;
	}

	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)") 
	public BigDecimal getDepositAmount() {
        if(depositAmount != null){
		    return depositAmount.setScale(3, RoundingMode.HALF_UP);
        }
        return depositAmount;
	}

	public void setDepositAmount(BigDecimal depositAmount) {
		this.depositAmount = depositAmount;
	}
	
	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)") 
	public BigDecimal getTotalAvailableAmount() {
		return totalAvailableAmount;
	}

	public void setTotalAvailableAmount(BigDecimal totalAvailableAmount) {
		this.totalAvailableAmount = totalAvailableAmount;
	}

	public String getDepositMode() {
		return depositMode;
	}

	public void setDepositMode(String depositMode) {
		this.depositMode = depositMode;
	}

	public String getDepositNotes() {
		return depositNotes;
	}

	public void setDepositNotes(String depositNotes) {
		this.depositNotes = depositNotes;
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
	
	@OneToOne
	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	public Person getCreatedPerson() {
		return createdPerson;
	}

	public void setCreatedPerson(Person createdPerson) {
		this.createdPerson = createdPerson;
		if(createdPerson != null)
		this.createdUser = createdPerson.getFirstName() + " " + createdPerson.getLastName();
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}
	
	@OneToOne(fetch=FetchType.EAGER)
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	
	public boolean isReturnToPatient() {
		return returnToPatient;
	}

	public void setReturnToPatient(boolean returnToPatient) {
		this.returnToPatient = returnToPatient;
	}
	
	public BigDecimal getConvenienceFeeForPatientPortal() {
		return convenienceFeeForPatientPortal;
	}

	public void setConvenienceFeeForPatientPortal(
			BigDecimal convenienceFeeForPatientPortal) {
		this.convenienceFeeForPatientPortal = convenienceFeeForPatientPortal;
	}
	
	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getTxnNumber() {
		return txnNumber;
	}

	public void setTxnNumber(String txnNumber) {
		this.txnNumber = txnNumber;
	}
	
	@Temporal(TemporalType.DATE)
	public Date getChequeDate() {
		return chequeDate;
	}

	public void setChequeDate(Date chequeDate) {
		this.chequeDate = chequeDate;
	}
	
	public String getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(String createdUser) {
		this.createdUser = createdUser;
	}
	
	

	public String getPortalPaymentId() {
		return portalPaymentId;
	}

	public void setPortalPaymentId(String portalPaymentId) {
		this.portalPaymentId = portalPaymentId;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getTransactionTimestamp() {
		return transactionTimestamp;
	}

	public void setTransactionTimestamp(Date transactionTimestamp) {
		this.transactionTimestamp = transactionTimestamp;
	}

	public String getIsysTrackingRef() {
		return isysTrackingRef;
	}

	public void setIsysTrackingRef(String isysTrackingRef) {
		this.isysTrackingRef = isysTrackingRef;
	}

	public String getPayerType() {
		return payerType;
	}

	public void setPayerType(String payerType) {
		this.payerType = payerType;
	}

	public String getPaymentChannel() {
		return paymentChannel;
	}

	public void setPaymentChannel(String paymentChannel) {
		this.paymentChannel = paymentChannel;
	}

	@Transient
	public boolean cancelled(){
		if("Cancelled".equals(status))
			return false;
		return true;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
}
