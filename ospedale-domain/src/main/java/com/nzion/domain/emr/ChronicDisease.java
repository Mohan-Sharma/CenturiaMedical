package com.nzion.domain.emr;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.soap.PatientChronicDisease;

@Entity
public class ChronicDisease extends IdGeneratingBaseEntity {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private IcdElement icdElement;
	
	@Column(name = "NAME")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	@Column(name = "DESCRIPTION")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@OneToOne
	public IcdElement getIcdElement() {
		return icdElement;
	}
	public void setIcdElement(IcdElement icdElement) {
		this.icdElement = icdElement;
	}
}
