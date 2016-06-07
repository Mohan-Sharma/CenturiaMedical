package com.nzion.domain.emr.soap.vitalsign;

import java.text.DecimalFormat;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.soap.PatientGeneralExamination;
import com.nzion.domain.emr.soap.VitalSignSection;

@Entity
public class VitalSignReading extends IdGeneratingBaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Height height;
	private Patient patient;
	private Weight weight;
	private BloodPressure bloodPressure;
	private Temperature temperature;
	private HeadCircumference headCircumference;
	private ChestCircumference chestCircumference;
	private Double bmiValue;
	private Pulse pulse;
	private Respiration respiration;
	private String readingComments;
	private Date dateOfMeasurement=new Date();
	private Date timeOfMeasurement=new Date();
	private String bmiCategory;
	private VitalSignSection vitalSignSection;
	
	private String englishMetricFlag;
	
	

	public String getEnglishMetricFlag() {
		return englishMetricFlag;
	}

	public void setEnglishMetricFlag(String englishMetricFlag) {
		this.englishMetricFlag = englishMetricFlag;
	}

	public VitalSignReading() {

	}

	public VitalSignReading(Patient patient) {
	this.patient = patient;
	this.height = new Height();
	this.weight = new Weight();
	this.bloodPressure = new BloodPressure();
	this.temperature = new Temperature();
	this.headCircumference = new HeadCircumference();
	this.chestCircumference = new ChestCircumference();
	this.pulse = new Pulse();
	this.respiration = new Respiration();
	}

	@Embedded
	public Height getHeight() {
	return height;
	}

	public void setHeight(Height height) {
	this.height = height;
	}

	@OneToOne
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@Embedded
	public Weight getWeight() {
	return weight;
	}

	public void setWeight(Weight weight) {
	this.weight = weight;
	}

	@Embedded
	public BloodPressure getBloodPressure() {
	return bloodPressure;
	}

	public void setBloodPressure(BloodPressure bloodPressure) {
	this.bloodPressure = bloodPressure;
	}

	@Embedded
	public Temperature getTemperature() {
	return temperature;
	}

	public void setTemperature(Temperature temperature) {
	this.temperature = temperature;
	}

	@Embedded
	public HeadCircumference getHeadCircumference() {
	return headCircumference;
	}

	public void setHeadCircumference(HeadCircumference headCircumference) {
	this.headCircumference = headCircumference;
	}

	@Embedded
	public ChestCircumference getChestCircumference() {
	return chestCircumference;
	}

	public void setChestCircumference(ChestCircumference chestCircumference) {
	this.chestCircumference = chestCircumference;
	}

	@Embedded
	public Pulse getPulse() {
	return pulse;
	}

	public void setPulse(Pulse pulse) {
	this.pulse = pulse;
	}

	@Embedded
	public Respiration getRespiration() {
	return respiration;
	}

	public void setRespiration(Respiration respiration) {
	this.respiration = respiration;
	}

	public String getReadingComments() {
	return readingComments;
	}

	public void setReadingComments(String readingComments) {
	this.readingComments = readingComments;
	}

	@Temporal(TemporalType.DATE)
	public Date getDateOfMeasurement() {
	return dateOfMeasurement;
	}

	public void setDateOfMeasurement(Date dateOfMeasurement) {
	this.dateOfMeasurement = dateOfMeasurement!=null?new java.sql.Date(dateOfMeasurement.getTime()):dateOfMeasurement;
	}

	@Temporal(TemporalType.TIME)
	public Date getTimeOfMeasurement() {
	return timeOfMeasurement;
	}

	public void setTimeOfMeasurement(Date timeOfMeasurement) {
	this.timeOfMeasurement = timeOfMeasurement;
	}

	public Double getBmiValue() {
		return bmiValue;
	}

	public void setBmiValue(Double bmiValue) {
		this.bmiValue = bmiValue;
	}

	public void calculateBMI() {
	System.out.println("PrePersist callback");
	double bmi = 0d;
	if (height != null && weight != null) {
		if (height.getMetricValue() != null && weight.getMetricValue()!=null && weight.getMetricValue()>0) {
			bmi =((weight.getMetricValue() * 703)
					/ Math.pow(height.getMetricValue(), 2));
		} else
			if (height.getEnglishValue() != null && weight.getEnglishValue()!=null && weight.getEnglishValue()>0) {
				bmi = (weight.getEnglishValue()
						/ Math.pow((height.getEnglishValue()/100), 2));
			}
	}
	bmi = new Double(new DecimalFormat("##.##").format(bmi));
	if(bmi<18.5)setBmiCategory("Underweight");
	else if(18.5 <= bmi && bmi <=24.5)setBmiCategory("Normal");
	else if(25 < bmi && bmi <= 29.99)setBmiCategory("Overweight");
	else if(30 < bmi && bmi <= 34.99)setBmiCategory("Obesity (Class 1)");
	else if(35 < bmi && bmi <= 39.99)setBmiCategory("Obesity (Class 2)");
	else if(40 > bmi )setBmiCategory("Morbid Obesity");
	setBmiValue(bmi);
	}

	public String getBmiCategory() {
	return bmiCategory;
	}

	public void setBmiCategory(String bmiCategory) {
	this.bmiCategory = bmiCategory;
	}
	
	@ManyToOne
	@JoinColumn(name = "VITAL_SECTION_ID")
	public VitalSignSection getVitalSignSection() {
	return vitalSignSection;
	}

	public void setVitalSignSection(VitalSignSection vitalSignSection) {
	this.vitalSignSection = vitalSignSection;
	}
	
	public static VitalSignReading convertToVitalSignReading(PatientGeneralExamination patientGeneralExamination){
		VitalSignReading vitalSignReading = new VitalSignReading();
		vitalSignReading.setHeight(patientGeneralExamination.getHeight());
		vitalSignReading.setWeight(patientGeneralExamination.getWeight());
		vitalSignReading.setChestCircumference(patientGeneralExamination.getChestCircumference());
		vitalSignReading.setHeadCircumference(patientGeneralExamination.getHeadCircumference());
		vitalSignReading.setDateOfMeasurement(patientGeneralExamination.getDateOfMeasurement());
		vitalSignReading.setTimeOfMeasurement(patientGeneralExamination.getTimeOfMeasurement());
		vitalSignReading.setBmiValue(patientGeneralExamination.getBmiValue());
		vitalSignReading.setBmiCategory(patientGeneralExamination.getBmiCategory());
		return vitalSignReading;
	}
}