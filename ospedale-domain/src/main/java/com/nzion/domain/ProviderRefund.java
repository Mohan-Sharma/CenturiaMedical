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
public class ProviderRefund extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
	private Provider provider;
	
	private BigDecimal refundAmount;
	
	private String refundNote;
	
	private String refundReason;
	
	private Invoice invoice;
	
	private ProviderRefundStatus status;
	
	@OneToOne
	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	
	@Column(precision = 19, scale = 3 ,columnDefinition="DECIMAL(19,3)") 
	public BigDecimal getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(BigDecimal refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundNote() {
		return refundNote;
	}

	public void setRefundNote(String refundNote) {
		this.refundNote = refundNote;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	@OneToOne
	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	@Enumerated(EnumType.STRING)
	public ProviderRefundStatus getStatus() {
		return status;
	}

	public void setStatus(ProviderRefundStatus status) {
		this.status = status;
	}




	enum ProviderRefundStatus{
		IN_PROCESS("In Process"),COMPLETED("Completed");
		private String name;
		ProviderRefundStatus(String name){
			this.name = name;
		}
		public String getName(){
			return name;
		}
		
	}
}
