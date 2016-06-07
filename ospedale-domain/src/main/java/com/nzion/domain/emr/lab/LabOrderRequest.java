package com.nzion.domain.emr.lab;

import com.nzion.domain.Patient;
import com.nzion.domain.Provider;
import com.nzion.domain.Referral;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.soap.PatientLabOrder;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.util.UtilValidator;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table
public class LabOrderRequest extends IdGeneratingBaseEntity{

	private static final long serialVersionUID = 1L;

	private Set<PatientLabOrder>  patientLabOrders;

	private PatientSoapNote patientSoapNote;

	private Referral referral;

	private ORDERSTATUS orderStatus;

	private Patient patient;

	private Provider provider;

    private String clinicalHistory;

    private String remarks;

    private Boolean self;

    public Boolean getSelf() {
        return self;
    }

    public void setSelf(Boolean self) {
        this.self = self;
    }

	@OneToOne
	public Referral getReferral() {
	return referral;
	}

	public void setReferral(Referral referral) {
	this.referral = referral;
	}

	public static enum ORDERSTATUS{
		BILLING_REQUIRED("New"),INVOICED("Invoiced"),BILLING_DONE("Billed"), INPATIENT_BILLING("Inpatient Billing"), INPROCESS("In Process"), COMPLETED("Completed"), CANCELLED("Cancelled");

		private String description;

		public String getDescription() {
		return description;
		}

		public void setDescription(String description) {
		this.description = description;
		}

		ORDERSTATUS(String description){
		this.description = description;
		}

	}

	@OneToMany(targetEntity=PatientLabOrder.class,mappedBy="labOrderRequest",fetch=FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	public Set<PatientLabOrder> getPatientLabOrders() {
	return patientLabOrders;
	}

	public void setPatientLabOrders(Set<PatientLabOrder> patientLabOrders) {
	this.patientLabOrders = patientLabOrders;
	}

	@OneToOne
	public PatientSoapNote getPatientSoapNote() {
	return patientSoapNote;
	}

	public void setPatientSoapNote(PatientSoapNote patientSoapNote) {
	this.patientSoapNote = patientSoapNote;
	}

    @Enumerated(EnumType.STRING)
	public ORDERSTATUS getOrderStatus() {
	return orderStatus;
	}

	public void setOrderStatus(ORDERSTATUS orderStatus) {
	this.orderStatus = orderStatus;
	}

	public void addPatientLabOrder(PatientLabOrder patientLabOrder){
		if(UtilValidator.isEmpty(this.patientLabOrders))
		this.patientLabOrders = new HashSet<PatientLabOrder>();
		this.patientLabOrders.add(patientLabOrder);
	}

	@OneToOne
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@OneToOne
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}

    public String getClinicalHistory() {
        return clinicalHistory;
    }

    public void setClinicalHistory(String clinicalHistory) {
        this.clinicalHistory = clinicalHistory;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
