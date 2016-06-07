package com.nzion.domain.emr;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table(name = "MVX_CODE")
public class MVXCode extends IdGeneratingBaseEntity{

	private static final long serialVersionUID = 1L;

	private String mvxCode;

	private String cvxCode;
	
	private String manufacturerName;
	
	private String cdcProductName;
	
	private String notes;

	private String mvxStatus;

	private String productStatus;
	
	public String getNotes() {
	return notes;
	}

	public void setNotes(String notes) {
	this.notes = notes;
	}

	@Column(name = "MVX_CODE")
	public String getMvxCode() {
	return mvxCode;
	}

	public void setMvxCode(String mvxCode) {
	this.mvxCode = mvxCode;
	}

	public String getCvxCode() {
	return cvxCode;
	}

	public void setCvxCode(String cvxCode) {
	this.cvxCode = cvxCode;
	}

	public String getManufacturerName() {
	return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
	this.manufacturerName = manufacturerName;
	}

	public String getMvxStatus() {
	return mvxStatus;
	}

	public void setMvxStatus(String mvxStatus) {
	this.mvxStatus = mvxStatus;
	}

	public String getProductStatus() {
	return productStatus;
	}

	public void setProductStatus(String productStatus) {
	this.productStatus = productStatus;
	}

	public String getCdcProductName() {
	return cdcProductName;
	}

	public void setCdcProductName(String cdcProductName) {
	this.cdcProductName = cdcProductName;
	}

}
