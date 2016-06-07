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
import com.nzion.domain.PatientFamilyMember;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.IcdElement;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientFamilyIllness extends IdGeneratingBaseEntity {

	private String member;

	private String illness;

	private Integer age;

	private String diseaseInfo;

	private String name;

	private Date approximateDate;

	private IcdElement icd;

	private String status;

	private Enumeration healthStatus;

	private Integer birthYear;

	private PatientFamilyMember patientFamilyMember;
	
	private Patient patient;
	
	private FamilyHistorySection soapSection;
	
	@OneToOne
	@JoinColumn(name = "PATIENT_ID")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@ManyToOne
	@JoinColumn(name = "FAMILY_HISTORY_SECTION_ID")
	public FamilyHistorySection getSoapSection() {
	return soapSection;
	}

	public void setSoapSection(FamilyHistorySection section) {
	this.soapSection = section;
	}

	@OneToOne
	@JoinColumn(name = "FAMILY_MEMBER_ID")
	public PatientFamilyMember getPatientFamilyMember() {
	return patientFamilyMember;
	}

	public void setPatientFamilyMember(PatientFamilyMember patientFamilyMember) {
	this.patientFamilyMember = patientFamilyMember;
	}

	public Date getApproximateDate() {
	return approximateDate;
	}

	public void setApproximateDate(Date approximateDate) {
	this.approximateDate = approximateDate;
	}

	public String getMember() {
	return member;
	}

	public void setMember(String member) {
	this.member = member;
	}

	public String getIllness() {
	return illness;
	}

	public void setIllness(String illness) {
	this.illness = illness;
	}

	public String getDiseaseInfo() {
	return diseaseInfo;
	}

	public void setDiseaseInfo(String diseaseInfo) {
	this.diseaseInfo = diseaseInfo;
	}

	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	@OneToOne
	@JoinColumn(name = "ICD_ID")
	public IcdElement getIcd() {
	return icd;
	}

	public void setIcd(IcdElement icd) {
	this.icd = icd;
	}

	public String getStatus() {
	return status;
	}

	public void setStatus(String status) {
	this.status = status;
	}

	@OneToOne
	public Enumeration getHealthStatus() {
	return healthStatus;
	}

	public void setHealthStatus(Enumeration healthStatus) {
	this.healthStatus = healthStatus;
	}

	public Integer getAge() {
	return age;
	}

	public void setAge(Integer age) {
	this.age = age;
	}

	public Integer getBirthYear() {
	return birthYear;
	}

	public void setBirthYear(Integer birthYear) {
	this.birthYear = birthYear;
	}

	private static final long serialVersionUID = 1L;

}
