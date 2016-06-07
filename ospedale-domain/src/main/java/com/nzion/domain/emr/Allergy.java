package com.nzion.domain.emr;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

@Entity
@Table(name = "ALLERGY", uniqueConstraints = { @UniqueConstraint(columnNames = { "CODE"}) })
@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)")
public class Allergy extends MasterEntity {

	private static final long serialVersionUID = 1L;

	private AllergyType allergyType;

	public void setAllergyType(AllergyType allergyType) {
	this.allergyType = allergyType;
	}

	@OneToOne(targetEntity = AllergyType.class)
	@JoinColumn(name = "ALLERGY_TYPE_ID")
	public AllergyType getAllergyType() {
	return allergyType;
	}
}
