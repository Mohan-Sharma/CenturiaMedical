package com.nzion.view;

import com.nzion.domain.Patient;
import com.nzion.domain.Provider;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.IcdElement;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.util.UtilValidator;

import java.util.Date;

public class EncounterSearchValueObject {

	private Provider provider;

	private Patient patient;

	private Date fromDate;

	private Date thruDate;

	private Cpt cpt;

	private IcdElement icdElement;

	private String chiefComplaint;

	private Drug drug;

	private String labTestName;

	private PatientSoapNote soapNote;

	public PatientSoapNote getSoapNote() {
	return soapNote;
	}

	public void setSoapNote(PatientSoapNote soapNote) {
	this.soapNote = soapNote;
	}

	public boolean checkDirty() {
	if (provider == null && patient == null && fromDate == null && thruDate == null && icdElement == null
			&& cpt == null && UtilValidator.isEmpty(chiefComplaint) && drug == null && UtilValidator.isEmpty(drugName)
			&& UtilValidator.isEmpty(labTestName)) return true;
	return false;
	}

	public String getLabTestName() {
	return labTestName;
	}

	public void setLabTestName(String labTestName) {
	this.labTestName = labTestName;
	}

	public Drug getDrug() {
	return drug;
	}

	public void setDrug(Drug drug) {
	this.drug = drug;
	}

	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}

	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public Date getFromDate() {
	return fromDate;
	}

	public void setFromDate(Date fromDate) {
	this.fromDate = fromDate;
	}

	public Date getThruDate() {
	return thruDate;
	}

	public void setThruDate(Date thruDate) {
	this.thruDate = thruDate;
	}

	public Cpt getCpt() {
	return cpt;
	}

	public void setCpt(Cpt cpt) {
	this.cpt = cpt;
	}

	public IcdElement getIcdElement() {
	return icdElement;
	}

	public void setIcdElement(IcdElement icdElement) {
	this.icdElement = icdElement;
	}

	public String getChiefComplaint() {
	return chiefComplaint;
	}

	public void setChiefComplaint(String chiefComplaint) {
	this.chiefComplaint = chiefComplaint;
	}

	private String icdString;

	private String cptString;

	public String getIcdString() {
	return icdString;
	}

	public void setIcdString(String icdString) {
	this.icdString = icdString;
	}

	public String getCptString() {
	return cptString;
	}

	public void setCptString(String cptString) {
	this.cptString = cptString;
	}

	private String drugName;

	public String getDrugName() {
	return drugName;
	}

	public void setDrugName(String drugName) {
	this.drugName = drugName;
	}
}
