package com.nzion.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import javax.persistence.*;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.Invoice;

@Entity
public class ReportsConfiguration{

	@Id
	private Long id;

	private String reportName;
	
	private boolean admin;
	
	private boolean provider;
	
	private boolean nurse;
	
	private boolean reception;
	
	private boolean billing;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isProvider() {
		return provider;
	}

	public void setProvider(boolean provider) {
		this.provider = provider;
	}

	public boolean isNurse() {
		return nurse;
	}

	public void setNurse(boolean nurse) {
		this.nurse = nurse;
	}

	public boolean isReception() {
		return reception;
	}

	public void setReception(boolean reception) {
		this.reception = reception;
	}

	public boolean isBilling() {
		return billing;
	}

	public void setBilling(boolean billing) {
		this.billing = billing;
	}
}
