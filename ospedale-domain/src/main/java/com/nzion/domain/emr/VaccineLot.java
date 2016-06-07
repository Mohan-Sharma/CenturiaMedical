package com.nzion.domain.emr;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table(name = "VACCINE_LOT", uniqueConstraints = { @UniqueConstraint(columnNames = {"LOT_NUMBER"}) })
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class VaccineLot extends IdGeneratingBaseEntity {

	private String lotNumber;

	private String manufacturerName;

	private Date expiryDate;

	private String productName;

	private Immunization immunization;

	private Float dosesRemaining;

	private String cvxCode;
	
	private String mvxCode;
	
	private String unit;
	
	
	public String getMvxCode() {
	return mvxCode;
	}

	public void setMvxCode(String mvxCode) {
	this.mvxCode = mvxCode;
	}

	@Column(name = "LOT_NUMBER", nullable = false)
	public String getLotNumber() {
	return lotNumber;
	}

	public void setLotNumber(String lotNumber) {
	this.lotNumber = lotNumber;
	}

	public String getManufacturerName() {
	return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
	this.manufacturerName = manufacturerName;
	}
	
	@Temporal(value = TemporalType.DATE)
	public Date getExpiryDate() {
	return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
	this.expiryDate = expiryDate!=null?new java.sql.Date(expiryDate.getTime()):expiryDate;
	}

	public String getProductName() {
	return productName;
	}

	public void setProductName(String productName) {
	this.productName = productName;
	}

	@OneToOne
	@JoinColumn(name = "IMMUNIZATION_ID")
	public Immunization getImmunization() {
	return immunization;
	}

	public void setImmunization(Immunization immunization) {
	this.immunization = immunization;
	}

	public Float getDosesRemaining() {
	return dosesRemaining;
	}

	public void setDosesRemaining(Float dosesRemaining) {
	this.dosesRemaining = dosesRemaining;
	}

	public String getCvxCode() {
	return cvxCode;
	}

	public void setCvxCode(String cvxCode) {
	this.cvxCode = cvxCode;
	}

	public String getUnit() {
	return unit;
	}

	public void setUnit(String unit) {
	this.unit = unit;
	}



	private static final long serialVersionUID = 1L;

}
