package com.nzion.domain.emr.soap;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.pms.Modifier;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientCpt extends IdGeneratingBaseEntity{

	private Cpt cpt;

	private DiagnosisSection diagnosisSection;
	
	private PreliminaryDiagnosisSection preliminaryDiagnosisSection;
	
	private DifferentialDiagnosisSection differentialDiagnosisSection;

	private Modifier modifier1;

	private Modifier modifier2;

	private Modifier modifier3;

	private Modifier modifier4;
	
	private Integer unit=new Integer(1);
	
	private boolean modifierAdded;
	
	private Patient patient;

	private String inPatientAdmNumber;	
	
	private BILLINGSTATUS billingStatus = BILLINGSTATUS.NONINVOICED;
	
	private CPTSTATUS cptStatus = CPTSTATUS.NEW;
	
	private boolean homeServiceRequired;

	@OneToOne
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public boolean isModifierAdded() {
	return modifierAdded;
	}

	public void setModifierAdded(boolean modifierAdded) {
	this.modifierAdded = modifierAdded;
	}

	public boolean isHomeServiceRequired() {
		return homeServiceRequired;
	}

	public void setHomeServiceRequired(boolean homeServiceRequired) {
		this.homeServiceRequired = homeServiceRequired;
	}

	public PatientCpt() {
	}

	public PatientCpt(Cpt cpt) {
	this.cpt = cpt;
	}

	@ManyToOne
	@JoinColumn(name = "CPT_ID")
	public Cpt getCpt() {
	return cpt;
	}

	public void setCpt(Cpt cpt) {
	this.cpt = cpt;
	}

	private static final long serialVersionUID = 1L;

	@ManyToOne
	@JoinColumn(name = "SOAP_SECTION_ID")
	@ForeignKey(name = "FK_DIAGNOSIS_SECTION")
	public DiagnosisSection getDiagnosisSection() {
	return diagnosisSection;
	}

	public void setDiagnosisSection(DiagnosisSection diagnosisSection) {
	this.diagnosisSection = diagnosisSection;
	}
	
	@ManyToOne
	public PreliminaryDiagnosisSection getPreliminaryDiagnosisSection() {
		return preliminaryDiagnosisSection;
	}

	public void setPreliminaryDiagnosisSection(
			PreliminaryDiagnosisSection preliminaryDiagnosisSection) {
		this.preliminaryDiagnosisSection = preliminaryDiagnosisSection;
	}
	
	@ManyToOne
	public DifferentialDiagnosisSection getDifferentialDiagnosisSection() {
		return differentialDiagnosisSection;
	}

	public void setDifferentialDiagnosisSection(
			DifferentialDiagnosisSection differentialDiagnosisSection) {
		this.differentialDiagnosisSection = differentialDiagnosisSection;
	}

	@OneToOne
	@JoinColumn(name = "CPT_MODIFIER1")
	public Modifier getModifier1() {
	return modifier1;
	}

	public void setModifier1(Modifier modifier1) {
	this.modifier1 = modifier1;
	}

	@OneToOne
	@JoinColumn(name = "CPT_MODIFIER2")
	public Modifier getModifier2() {
	return modifier2;
	}

	public void setModifier2(Modifier modifier2) {
	this.modifier2 = modifier2;
	}

	@OneToOne
	@JoinColumn(name = "CPT_MODIFIER3")
	public Modifier getModifier3() {
	return modifier3;
	}

	public void setModifier3(Modifier modifier3) {
	this.modifier3 = modifier3;
	}

	@OneToOne
	@JoinColumn(name = "CPT_MODIFIER4")
	public Modifier getModifier4() {
	return modifier4;
	}

	public void setModifier4(Modifier modifier4) {
	this.modifier4 = modifier4;
	}

	@Transient
	private transient Integer hash = null;
	
	@Override
	public int hashCode() {
	if (hash != null) 
		return hash;
	if(this.getCpt() == null){
		hash = 0;
		return hash;
	}
	if (this.getCpt().getId() == null) {
		hash = 0;
		return hash;
	}
	return this.getCpt().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
	if(this == null || obj == null)
		return false;
	if(this != obj)
		return false;
	PatientCpt otherObj = (PatientCpt)obj;
	if(this.getCpt() == null || otherObj.getCpt() == null)
		return false;
	if(this.getCpt().getId().equalsIgnoreCase(otherObj.getCpt().getId()))
		return true;
	return false;
	}
	
	public Integer getUnit() {
	return unit;
	}

	public void setUnit(Integer unit) {
	this.unit = unit;
	}
	
	public String getInPatientAdmNumber() {
	return inPatientAdmNumber;
	}

	public void setInPatientAdmNumber(String inPatientAdmNumber) {
	this.inPatientAdmNumber = inPatientAdmNumber;
	}
	

	public static enum BILLINGSTATUS{
		NONINVOICED, INVOICED
	};

	public BILLINGSTATUS getBillingStatus() {
		return billingStatus;
	}

	public void setBillingStatus(BILLINGSTATUS billingStatus) {
		this.billingStatus = billingStatus;
	}
	
	public static enum CPTSTATUS{
		NEW,COMPLETED,CANCEL
	}

	public CPTSTATUS getCptStatus() {
		return cptStatus;
	}

	public void setCptStatus(CPTSTATUS cptStatus) {
		this.cptStatus = cptStatus;
	}
	
	

}