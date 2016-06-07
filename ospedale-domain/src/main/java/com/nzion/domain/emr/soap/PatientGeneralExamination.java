package com.nzion.domain.emr.soap;

import java.text.DecimalFormat;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.soap.vitalsign.BloodPressure;
import com.nzion.domain.emr.soap.vitalsign.ChestCircumference;
import com.nzion.domain.emr.soap.vitalsign.HeadCircumference;
import com.nzion.domain.emr.soap.vitalsign.Height;
import com.nzion.domain.emr.soap.vitalsign.Pulse;
import com.nzion.domain.emr.soap.vitalsign.Respiration;
import com.nzion.domain.emr.soap.vitalsign.Temperature;
import com.nzion.domain.emr.soap.vitalsign.Weight;

@Entity
public class PatientGeneralExamination  extends IdGeneratingBaseEntity{
	private Height height;

	private Weight weight;

	private Double bmiValue;

	private String bmiCategory;
	
	private HeadCircumference headCircumference;
	
	private ChestCircumference chestCircumference;

	private Enumeration nourishment;
	
	private Enumeration built;
	
	private Patient patient;
	
	private GeneralExaminationSection generalExaminationSection;
	
    private String englishMetricFlag;
	
	

	public String getEnglishMetricFlag() {
		return englishMetricFlag;
	}

	public void setEnglishMetricFlag(String englishMetricFlag) {
		this.englishMetricFlag = englishMetricFlag;
	}
	
	
	
	@ManyToOne
	public GeneralExaminationSection getGeneralExaminationSection() {
		return generalExaminationSection;
	}

	public void setGeneralExaminationSection(
			GeneralExaminationSection generalExaminationSection) {
		this.generalExaminationSection = generalExaminationSection;
	}

	@OneToOne
	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}


	private Date dateOfMeasurement=new Date();
	private Date timeOfMeasurement=new Date();
	
	public HeadCircumference getHeadCircumference() {
		if (headCircumference == null)
			headCircumference = new HeadCircumference();
		headCircumference.setMetric("metric".equals(englishMetricFlag));
		return headCircumference;
	}

	public void setHeadCircumference(HeadCircumference headCircumference) {
		this.headCircumference = headCircumference;
	}

	public ChestCircumference getChestCircumference() {
		if (chestCircumference == null)
			chestCircumference = new ChestCircumference();
		chestCircumference.setMetric("metric".equals(englishMetricFlag));
		return chestCircumference;
	}

	public void setChestCircumference(ChestCircumference chestCircumference) {
		this.chestCircumference = chestCircumference;
	}

	
	@OneToOne
	public Enumeration getNourishment() {
		return nourishment;
	}

	public void setNourishment(Enumeration nourishment) {
		this.nourishment = nourishment;
	}

	@OneToOne
	public Enumeration getBuilt() {
		return built;
	}

	public void setBuilt(Enumeration built) {
		this.built = built;
	}

	@Embedded
	public Height getHeight() {
		if (height == null)
		height = new Height();
		height.setMetric("metric".equals(englishMetricFlag));
		return height;
	}

	public void setHeight(Height height) {
		this.height = height;
	}

	@Embedded
	public Weight getWeight() {
		if (weight == null)
			weight = new Weight();
		    weight.setMetric("metric".equals(englishMetricFlag));
		return weight;
	}

	public void setWeight(Weight weight) {
		this.weight = weight;
	}

	public Double getBmiValue() {
		return bmiValue;
	}

	public void setBmiValue(Double bmiValue) {
		this.bmiValue = bmiValue;
	}

	public String getBmiCategory() {
		return bmiCategory;
	}

	public void setBmiCategory(String bmiCategory) {
		this.bmiCategory = bmiCategory;
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



	public void calculateBMI() {
		System.out.println("PrePersist callback");
		double bmi = 0d;
		if (height != null && weight != null) {
			if (height.getMetricValue() != null
					&& weight.getMetricValue() != null
					&& weight.getMetricValue() > 0) {
				bmi = ((weight.getMetricValue() * 703) / Math.pow(
						height.getMetricValue(), 2));
			} else if (height.getEnglishValue() != null
					&& weight.getEnglishValue() != null
					&& weight.getEnglishValue() > 0) {
				bmi = (weight.getEnglishValue() / Math.pow(
						(height.getEnglishValue() / 100), 2));
			}
		}
		bmi = new Double(new DecimalFormat("##.##").format(bmi));
		if (bmi < 18.5)
			setBmiCategory("Underweight");
		else if (18.5 <= bmi && bmi <= 24.5)
			setBmiCategory("Normal");
		else if (25 < bmi && bmi <= 29.99)
			setBmiCategory("Overweight");
		else if (30 < bmi && bmi <= 34.99)
			setBmiCategory("Obesity (Class 1)");
		else if (35 < bmi && bmi <= 39.99)
			setBmiCategory("Obesity (Class 2)");
		else if (40 > bmi)
			setBmiCategory("Morbid Obesity");
		setBmiValue(bmi);
	}

	
	private static final long serialVersionUID = 1L;
}
