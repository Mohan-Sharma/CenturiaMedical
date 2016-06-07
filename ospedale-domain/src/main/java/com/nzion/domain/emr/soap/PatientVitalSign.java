package com.nzion.domain.emr.soap;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.UnitOfMeasurement;
import com.nzion.domain.emr.VitalSign;
import com.nzion.domain.emr.VitalSignCondition;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */

@Entity
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientVitalSign extends IdGeneratingBaseEntity {

	private VitalSign vitalSign;

	private boolean abnormal = false;

	private String comments;

	private String value;

	private UnitOfMeasurement uom;

	private Patient patient;
	private VitalSignSection vitalSignSection;

	@OneToOne
	@JoinColumn(name = "SOAP_SECTION_ID")
	public VitalSignSection getVitalSignSection() {
	return vitalSignSection;
	}

	public void setVitalSignSection(VitalSignSection vitalSignSection) {
	this.vitalSignSection = vitalSignSection;
	}

	public PatientVitalSign() {
	}

	public PatientVitalSign(VitalSign personVitalSign, Patient patient) {
	if (patient == null) throw new RuntimeException("needs a patient");
	this.vitalSign = personVitalSign;
	this.patient = patient;
	this.uom = personVitalSign.getUnitOfMeasurement();
	}

	public boolean isAbnormal() {
	return abnormal;
	}

	public void setAbnormal(boolean abnormal) {
	this.abnormal = abnormal;
	}

	@OneToOne(targetEntity = Patient.class)
	@JoinColumn(name = "PATIENT_ID")
	@ForeignKey(name = "FK_PAT_VITALSIGN")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public String getValue() {
	return value;
	}

	public void setValue(String value) {
	this.value = value;
	}

	@ManyToOne
	@JoinColumn(name = "VITAL_SIGN_ID")
	public VitalSign getVitalSign() {
	return vitalSign;
	}

	public void setVitalSign(VitalSign vitalSign) {
	this.vitalSign = vitalSign;
	}

	public String getComments() {
	return comments;
	}

	public void setComments(String comments) {
	this.comments = comments;
	}

	@OneToOne
	@JoinColumn(name = "UOM_ID")
	public UnitOfMeasurement getUom() {
	return uom;
	}

	public void setUom(UnitOfMeasurement uom) {
	this.uom = uom;
	}

	@Transient
	public Float getValueAsNumber() {
	return new Float(value);
	}

	public boolean validate() {
	if (UtilValidator.isEmpty(vitalSign.getConditions())) return abnormal;
	for (VitalSignCondition condition : vitalSign.getConditions())
		if (!condition.validate(patient, value)) {
			abnormal = true;
			break;
		}
	return abnormal;
	}

	private static final long serialVersionUID = 1L;
}