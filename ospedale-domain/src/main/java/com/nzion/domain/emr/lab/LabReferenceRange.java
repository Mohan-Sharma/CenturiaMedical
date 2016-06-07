package com.nzion.domain.emr.lab;

import com.nzion.domain.base.IdGeneratingBaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
public class LabReferenceRange extends IdGeneratingBaseEntity{

	
	private String referenceRange;
	

	public String getReferenceRange() {
	return referenceRange;
	}

	public void setReferenceRange(String referenceRange) {
	this.referenceRange = referenceRange;
	}

	private static final long serialVersionUID = 1L;

}
