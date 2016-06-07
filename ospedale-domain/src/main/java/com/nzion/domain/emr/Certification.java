package com.nzion.domain.emr;

import java.math.BigDecimal;

import javax.persistence.AssociationOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.annot.SystemAuditable;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.soap.PatientLabOrder.STATUS;
import com.nzion.util.UtilReflection;
import java.math.*;

@Entity
@Table(name = "CERTIFICATION")
public class Certification extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	private String name;
	private String description;

	@Column(name = "NAME",nullable=false)
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

	@Override
	public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	return result;
	}

	@Override
	public boolean equals(Object obj) {
	return UtilReflection.areEqual(this, obj, new String[] {"name"});
	}

	@Override
	public String toString() {
		return name;
	}
}