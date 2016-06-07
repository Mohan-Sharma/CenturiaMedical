package com.nzion.domain.emr.soap;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.Observation;

@Entity
@Table
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientPastObservationHistory extends IdGeneratingBaseEntity {

	private Observation observation;
	
	private Set<ObservationValue> observationValues;
	
	private Date recordedOn = new Date();
	
	private Date recordedTime = new Date();
	
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

	public Date getRecordedTime() {
	return recordedTime;
	}

	public void setRecordedTime(Date recordedTime) {
	this.recordedTime = recordedTime;
	}

	public Date getRecordedOn() {
	return recordedOn;
	}

	public void setRecordedOn(Date recordedOn) {
	this.recordedOn = recordedOn;
	}

	@OneToMany(targetEntity = ObservationValue.class, fetch=FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	@JoinColumn(name = "PATIENT_PAST_OBSERVATION_ID")
	@Fetch(FetchMode.SELECT)
	public Set<ObservationValue> getObservationValues() {
    if(observationValues == null)
    	observationValues = new HashSet<ObservationValue>();
	return observationValues;
	}

	public void setObservationValues(Set<ObservationValue> observationValues) {
	this.observationValues = observationValues;
	}

	@OneToOne
	@JoinColumn(name = "OBSERVATION_ID")
	public Observation getObservation() {
	return observation;
	}

	public void setObservation(Observation observation) {
	this.observation = observation;
	}
	
	public void initObservation(Observation observation){
	getObservationValues().clear();
	if(observation == null)
		return;
	for(String collectionName : observation.getCollectionNames())
		getObservationValues().add(new ObservationValue(observation, collectionName));
	}
	
	private static final long serialVersionUID = 1L;
}
