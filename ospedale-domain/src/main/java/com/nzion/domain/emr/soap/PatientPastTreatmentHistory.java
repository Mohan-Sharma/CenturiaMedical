package com.nzion.domain.emr.soap;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientPastTreatmentHistory extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	private Date admissionDate;
	private Enumeration admissionType;
	private String admissionReason;
	private String hospitalName;
	private boolean hasSurgery;
	private String surgeryReason;
	private Patient patient;
	private PastHistorySection soapSection;
	
	@OneToOne
	@JoinColumn(name="PATIENT_ID",nullable=false)
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}
	
	@ManyToOne
	@JoinColumn(name="PAST_HX_SECTION_ID",nullable=false)
	public PastHistorySection getSoapSection() {
	return soapSection;
	}

	public void setSoapSection(PastHistorySection section) {
	this.soapSection = section;
	}

	public Date getAdmissionDate() {
	return admissionDate;
	}

	public void setAdmissionDate(Date admissionDate) {
	this.admissionDate = admissionDate;
	}

	@ManyToOne
	@JoinColumn(name = "ADMISSION_TYPE_ID")
	public Enumeration getAdmissionType() {
	return admissionType;
	}

	public void setAdmissionType(Enumeration admissionType) {
	this.admissionType = admissionType;
	}

	public String getAdmissionReason() {
	return admissionReason;
	}

	public void setAdmissionReason(String admissionReason) {
	this.admissionReason = admissionReason;
	}

	public String getHospitalName() {
	return hospitalName;
	}

	public void setHospitalName(String hospitalName) {
	this.hospitalName = hospitalName;
	}

	public boolean isHasSurgery() {
	return hasSurgery;
	}

	public void setHasSurgery(boolean hasSurgery) {
	this.hasSurgery = hasSurgery;
	}

	public String getSurgeryReason() {
	return surgeryReason;
	}

	public void setSurgeryReason(String surgeryReason) {
	this.surgeryReason = surgeryReason;
	}

}
