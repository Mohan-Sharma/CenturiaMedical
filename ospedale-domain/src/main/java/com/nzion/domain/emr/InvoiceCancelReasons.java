package com.nzion.domain.emr;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class InvoiceCancelReasons {
	
	private Long id;
    private String reason;

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
