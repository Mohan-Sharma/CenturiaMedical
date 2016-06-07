package com.nzion.domain.emr.soap;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.Observation;

@Entity
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class ObservationValue extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	
	private Observation observation;
	
	private String collectionName;
	
	private String value;
	
	public ObservationValue(){
	
	}
	
	public ObservationValue(Observation observation, String collectionName){
	this.observation = observation;
	this.collectionName = collectionName;
	}

	@OneToOne
	@JoinColumn(name = "OBSERVATION_ID")
	public Observation getObservation() {
	return observation;
	}

	public void setObservation(Observation observation) {
	this.observation = observation;
	}

	public String getCollectionName() {
	return collectionName;
	}

	public void setCollectionName(String collectionName) {
	this.collectionName = collectionName;
	}

	public String getValue() {
	return value;
	}

	public void setValue(String value) {
	this.value = value;
	}
}
