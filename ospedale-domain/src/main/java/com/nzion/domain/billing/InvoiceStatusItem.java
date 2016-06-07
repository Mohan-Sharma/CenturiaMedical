package com.nzion.domain.billing;

public enum InvoiceStatusItem {

	 READY("READY"), WRITEOFF("WRITEOFF"), INPROCESS("INPROCESS"),PATIENT_PAID("PATIENT PAID"),FINAL("FINAL"), RECEIVED("RECEIVED"), CANCELLED("CANCELLED");

	private String description;

	private InvoiceStatusItem(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	

}
