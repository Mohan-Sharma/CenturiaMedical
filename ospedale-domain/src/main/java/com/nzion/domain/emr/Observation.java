package com.nzion.domain.emr;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;
@Entity
@Filters( {
	@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class Observation extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private Set<String> collectionNames;
	
	private UnitOfMeasurement uom;
	
	@OneToOne
	@JoinColumn(name = "UOM_ID")
	public UnitOfMeasurement getUom() {
	return uom;
	}

	public void setUom(UnitOfMeasurement uom) {
	this.uom = uom;
	}

	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	@ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name="OBSERVATION_COLLECTION_NAMES", joinColumns=@JoinColumn(name="OBSERVATION_ID"))
    @Fetch(FetchMode.SELECT)
	public Set<String> getCollectionNames() {
	if(collectionNames ==  null)
		collectionNames = new HashSet<String>();
	return collectionNames;
	}

	public void setCollectionNames(Set<String> collectionNames) {
	this.collectionNames = collectionNames;
	}
	
	public String toString(){
	return name == null ? null :name;
	}
}
