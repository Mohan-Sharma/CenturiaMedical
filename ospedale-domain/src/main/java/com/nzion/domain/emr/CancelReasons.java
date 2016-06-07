package com.nzion.domain.emr;

import javax.persistence.Entity;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
public class CancelReasons extends IdGeneratingBaseEntity{
	
	private static final long serialVersionUID = 1L;

	private String reason;
	
	private String cancelReasonsType;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    
	public String getCancelReasonsType() {
		return cancelReasonsType;
	}

	public void setCancelReasonsType(String cancelReasonsType) {
		this.cancelReasonsType = cancelReasonsType;
	}

	public enum CancelReasonsType{
    	INVOICE_CANCEL,APPOINTMENT_CANCEL
    }

}
