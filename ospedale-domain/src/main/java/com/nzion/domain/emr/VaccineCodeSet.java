package com.nzion.domain.emr;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.Practice;
import com.nzion.domain.base.BaseEntity;

/**
 * @author Sandeep Prusty
 * May 30, 2011
 */
@Entity
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)")})
public class VaccineCodeSet extends BaseEntity  {

	private static final long serialVersionUID = 1L;



	private String id;

	private String name;

	private String description;

	private Set<Immunization> immunizations;

	@ManyToMany
	@JoinTable(name = "VACCINECODESET_IMMUNIZATION", joinColumns = { @JoinColumn(name = "VACCINECODESET_ID") }, inverseJoinColumns = { @JoinColumn(name = "IMMUNIZATION_ID") })
	public Set<Immunization> getImmunizations() {
	if (immunizations == null) immunizations = new HashSet<Immunization>();
	return immunizations;
	}

	public void setImmunizations(Set<Immunization> immunizations) {
	this.immunizations = immunizations;
	}
	@Override
	@Id
	public String getId() {
	return id;
	}

	public void setId(String id) {
	this.id = id;
	}

	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	public String getDescription() {
	return description;
	}

	public void setDescription(String description) {
	this.description = description;
	}

	public static final String PNEUMOCOCCAL_VACCINE = "PNEUMOCOCCAL-VACCINE";
}
