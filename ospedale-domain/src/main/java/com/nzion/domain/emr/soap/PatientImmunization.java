package com.nzion.domain.emr.soap;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.drug.DrugDosageRoute;
import com.nzion.domain.drug.Site;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.IcdElement;
import com.nzion.domain.emr.Immunization;
import com.nzion.domain.emr.VaccineLot;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */

@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientImmunization extends IdGeneratingBaseEntity {

	private Immunization immunization;

	private IcdElement icd;

	private Cpt cpt;

	private Drug drug;

	private DrugDosageRoute route;

	private Site site;

	private String dose;

	private Date given;

	private Date vis;

	private Date visGiven;

	private Date expiry;

	private String clinicLocation;

	private boolean consentAccepted;

	private VaccineLot lotNumber;

	private Float administeredAmount;

	private String administeredUnits;

	private Date administeredOn;

	private boolean hasAllergicReaction;

	private boolean hasIllNess;

	private boolean qasPerformed;

	private boolean authorizationOnFile;

	private String status = "VISGIVEN";

	private Patient patient;

	private boolean administeredAtInhouse = true;

	private ImmunizationSection soapSection;
	
	private String productName;
	
	private String manufacturerName;

	public String getProductName() {
	return productName;
	}

	public void setProductName(String productName) {
	this.productName = productName;
	}

	public String getManufacturerName() {
	return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
	this.manufacturerName = manufacturerName;
	}

	@ManyToOne
	@JoinColumn(name = "IMMUNIZATION_SECTION_ID")
	public ImmunizationSection getSoapSection() {
	return soapSection;
	}

	public void setSoapSection(ImmunizationSection immunizationSection) {
	this.soapSection = immunizationSection;
	}

	public PatientImmunization() {
	}

	public PatientImmunization(Immunization immunization) {
	this.immunization = immunization;
	}

	public Float getAdministeredAmount() {
	return administeredAmount;
	}

	public void setAdministeredAmount(Float administeredAmount) {
	this.administeredAmount = administeredAmount;
	}

	public String getAdministeredUnits() {
	return administeredUnits;
	}

	public void setAdministeredUnits(String administeredUnits) {
	this.administeredUnits = administeredUnits;
	}

	@OneToOne
	@JoinColumn(name = "IMMUNIZATION_ID")
	public Immunization getImmunization() {
	return immunization;
	}

	public void setImmunization(Immunization immunization) {
	this.immunization = immunization;
	}

	@OneToOne
	@JoinColumn(name = "DRUG_ID")
	public Drug getDrug() {
	return drug;
	}

	public void setDrug(Drug drug) {
	this.drug = drug;
	}

	@OneToOne
	@JoinColumn(name = "ICD_CODE")
	public IcdElement getIcd() {
	return icd;
	}

	public void setIcd(IcdElement icd) {
	this.icd = icd;
	}

	@OneToOne
	@JoinColumn(name = "CPT_CODE")
	public Cpt getCpt() {
	return cpt;
	}

	public void setCpt(Cpt cpt) {
	this.cpt = cpt;
	}

	@OneToOne
	public DrugDosageRoute getRoute() {
	return route;
	}

	public void setRoute(DrugDosageRoute route) {
	this.route = route;
	}

	@OneToOne
	public Site getSite() {
	return site;
	}

	public void setSite(Site site) {
	this.site = site;
	}

	public String getDose() {
	return dose;
	}

	public void setDose(String dose) {
	this.dose = dose;
	}

	@Column
	@Temporal(TemporalType.DATE)
	public Date getGiven() {
	return given;
	}

	public void setGiven(Date given) {
	this.given = given;
	}

	@Column
	@Temporal(TemporalType.DATE)
	public Date getVis() {
	return vis;
	}

	public void setVis(Date vis) {
	this.vis = vis;
	}

	@Column
	@Temporal(TemporalType.DATE)
	public Date getVisGiven() {
	return visGiven;
	}

	public void setVisGiven(Date visGiven) {
	this.visGiven = visGiven;
	}

	@Column
	@Temporal(TemporalType.DATE)
	public Date getExpiry() {
	return expiry;
	}

	public void setExpiry(Date expiry) {
	this.expiry = expiry;
	}

	public String getClinicLocation() {
	return clinicLocation;
	}

	public void setClinicLocation(String clinicLocation) {
	this.clinicLocation = clinicLocation;
	}

	public boolean isConsentAccepted() {
	return consentAccepted;
	}

	public void setConsentAccepted(boolean consentAccepted) {
	this.consentAccepted = consentAccepted;
	}

	@OneToOne
	public VaccineLot getLotNumber() {
	return lotNumber;
	}

	public void setLotNumber(VaccineLot lotNumber) {
	this.lotNumber = lotNumber;
	}

	public boolean isHasAllergicReaction() {
	return hasAllergicReaction;
	}

	public void setHasAllergicReaction(boolean hasAllergicReaction) {
	this.hasAllergicReaction = hasAllergicReaction;
	}

	public boolean isHasIllNess() {
	return hasIllNess;
	}

	public void setHasIllNess(boolean hasIllNess) {
	this.hasIllNess = hasIllNess;
	}

	public boolean isQasPerformed() {
	return qasPerformed;
	}

	public void setQasPerformed(boolean qasPerformed) {
	this.qasPerformed = qasPerformed;
	}

	public boolean isAuthorizationOnFile() {
	return authorizationOnFile;
	}

	public void setAuthorizationOnFile(boolean authorizationOnFile) {
	this.authorizationOnFile = authorizationOnFile;
	}

	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.TIMESTAMP)
	public Date getAdministeredOn() {
	return administeredOn;
	}

	public void setAdministeredOn(Date administeredOn) {
	this.administeredOn = administeredOn;
	}

	public String getStatus() {
	return status;
	}

	public void setStatus(String status) {
	this.status = status;
	}

	@OneToOne
	@JoinColumn(name = "PATIENT_ID")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public boolean isAdministeredAtInhouse() {
	return administeredAtInhouse;
	}

	public void setAdministeredAtInhouse(boolean administeredAtInhouse) {
	this.administeredAtInhouse = administeredAtInhouse;
	}

	public PatientImmunization createCopy() {
	PatientImmunization copyPatientImmunization = new PatientImmunization();
	try {
		BeanUtils.copyProperties(copyPatientImmunization, this);
	} catch (Exception e) {
		throw new RuntimeException("coping data from previous soapnote failed");
	}
	copyPatientImmunization.setId(null);
	return copyPatientImmunization;
	}
}