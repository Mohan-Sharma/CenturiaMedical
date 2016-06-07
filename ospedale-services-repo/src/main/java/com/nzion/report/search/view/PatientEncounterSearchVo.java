package com.nzion.report.search.view;

import java.util.Date;
import java.util.List;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Provider;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.IcdElement;
import com.nzion.domain.emr.Immunization;
import com.nzion.util.UtilDateTime;

public class PatientEncounterSearchVo {

	private Integer lowEndAge;

	private Integer highEndAge;

	private String lowEndAgeQuantifier;

	private String highEndAgeQuantifier;

	private Enumeration race;

	private Enumeration ethnicity;

	private Enumeration gender;

	private String drugName;

	private IcdElement icdElement;

	private Enumeration problemStatus;

	private Date startDOB;

	private Date endDOB;

	private String drudGenericName;

	private String observationValue;

	private List<LabResultSearchVo> labResultSearchVos;

	private Immunization vaccine;

	private Date administerStartDate;

	private Date administerEndDate;

	private String vaccineStatus = "ADMINISTERED";

	private Provider provider;

	private String allergyName;

	private Enumeration allergyStatus;

	private Drug drug;

	public Drug getDrug() {
	return drug;
	}

	public void setDrug(Drug drug) {
	this.drug = drug;
	}

	public List<LabResultSearchVo> getLabResultSearchVos() {
	return labResultSearchVos;
	}

	public void setLabResultSearchVos(List<LabResultSearchVo> labResultSearchVos) {
	this.labResultSearchVos = labResultSearchVos;
	}

	public String getDrudGenericName() {
	return drudGenericName;
	}

	public void setDrudGenericName(String drudGenericName) {
	this.drudGenericName = drudGenericName;
	}

	public String getObservationValue() {
	return observationValue;
	}

	public void setObservationValue(String observationValue) {
	this.observationValue = observationValue;
	}

	public Date getStartDOB() {
	return startDOB;
	}

	public void setStartDOB(Date startDOB) {
	this.startDOB = startDOB;
	}

	public Date getEndDOB() {
	return endDOB;
	}

	public void setEndDOB(Date endDOB) {
	this.endDOB = endDOB;
	}

	public Integer getLowEndAge() {
	return lowEndAge;
	}

	public void setLowEndAge(Integer lowEndAge) {
	if (lowEndAge != null) startDOB = UtilDateTime.getDayStart(UtilDateTime.addYearsToDate(new Date(), -lowEndAge));
	this.lowEndAge = lowEndAge;
	}

	public Integer getHighEndAge() {
	return highEndAge;
	}

	public void setHighEndAge(Integer highEndAge) {
	if (highEndAge != null) endDOB = UtilDateTime.getDayStart(UtilDateTime.addYearsToDate(new Date(), -highEndAge));
	this.highEndAge = highEndAge;
	}

	public String getLowEndAgeQuantifier() {
	return lowEndAgeQuantifier;
	}

	public void setLowEndAgeQuantifier(String lowEndAgeQuantifier) {
	this.lowEndAgeQuantifier = lowEndAgeQuantifier;
	}

	public String getHighEndAgeQuantifier() {
	return highEndAgeQuantifier;
	}

	public void setHighEndAgeQuantifier(String highEndAgeQuantifier) {
	this.highEndAgeQuantifier = highEndAgeQuantifier;
	}

	public Enumeration getRace() {
	return race;
	}

	public void setRace(Enumeration race) {
	this.race = race;
	}

	public Enumeration getEthnicity() {
	return ethnicity;
	}

	public void setEthnicity(Enumeration ethnicity) {
	this.ethnicity = ethnicity;
	}

	public Enumeration getGender() {
	return gender;
	}

	public void setGender(Enumeration gender) {
	this.gender = gender;
	}

	public String getDrugName() {
	return drugName;
	}

	public void setDrugName(String drugName) {
	this.drugName = drugName;
	}

	public IcdElement getIcdElement() {
	return icdElement;
	}

	public void setIcdElement(IcdElement icdElement) {
	this.icdElement = icdElement;
	}

	public Enumeration getProblemStatus() {
	return problemStatus;
	}

	public void setProblemStatus(Enumeration problemStatus) {
	this.problemStatus = problemStatus;
	}

	public Immunization getVaccine() {
	return vaccine;
	}

	public void setVaccine(Immunization vaccine) {
	this.vaccine = vaccine;
	}

	public Date getAdministerStartDate() {
	return administerStartDate;
	}

	public void setAdministerStartDate(Date administerStartDate) {
	this.administerStartDate = administerStartDate;
	}

	public Date getAdministerEndDate() {
	return administerEndDate;
	}

	public void setAdministerEndDate(Date administerEndDate) {
	this.administerEndDate = administerEndDate;
	}

	public String getVaccineStatus() {
	return vaccineStatus;
	}

	public void setVaccineStatus(String vaccineStatus) {
	this.vaccineStatus = vaccineStatus;
	}

	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}

	public Enumeration getAllergyStatus() {
	return allergyStatus;
	}

	public void setAllergyStatus(Enumeration allergyStatus) {
	this.allergyStatus = allergyStatus;
	}

	public String getAllergyName() {
	return allergyName;
	}

	public void setAllergyName(String allergyName) {
	this.allergyName = allergyName;
	}

}
