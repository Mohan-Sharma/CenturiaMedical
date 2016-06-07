package com.nzion.domain.emr;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.BaseEntity;
import com.nzion.util.UtilReflection;

@Entity
@Table(name = "ICD_CODE_SET")
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class IcdCodeSet extends BaseEntity  {

	private String id;

	private String name;

	private String description;



	private Set<IcdElement> icdElements;

	private static final long serialVersionUID = 1L;

	private boolean syndrome;

	@Column(name = "NAME")
	public String getName() {
	return name;
	}

	public void setName(String name) {
	this.name = name;
	}

	@ManyToMany
	@JoinTable(name = "ICDCODESET_ICDELEMENT", joinColumns = { @JoinColumn(name = "ICDCODESET_ID") }, inverseJoinColumns = { @JoinColumn(name = "ICD_ID") })
	@Fetch(FetchMode.SELECT)
	public Set<IcdElement> getIcdElements() {
	return icdElements;
	}

	public void setIcdElements(Set<IcdElement> icdElements) {
	this.icdElements = icdElements;
	}

	@Override
	@Id
	public String getId() {
	return id;
	}

	public void setId(String id) {
	this.id = id;
	}

	public String getDescription() {
	return description;
	}

	public void setDescription(String description) {
	this.description = description;
	}

	@Override
	public boolean equals(Object obj) {
	return UtilReflection.areEqual(obj, this, "id");
	}

	@Transient
	private transient Integer hash = null;

	@Override
	public int hashCode() {
	if (hash != null) return hash;
	if (getId() == null) {
		hash = 0;
		return hash;
	}
	return id.hashCode();
	}

	public static final String PREGNANCY_421 = "PREGNANCY_421";
	public static final String HYPERTENSION_0013 = "HYPERTENSION_0013";
	public static final String HYPERTENSION_0018 = "HYPERTENSION_0018";
	public static final String PREGNANCY_0018 = "PREGNANCY_0018";
	public static final String ESRD_0018 = "ESRD_0018";
	public static final String ENCOUNTER_OUTPATIENT_0027 = "ENCOUNTER_OUTPATIENT_0027";
	public static final String FOLLOW_UP_PLAN_BMI_MANAGEMENT_0421 = "FOLLOW_UP_PLAN_BMI_MANAGEMENT_0421";
	public static final String ENCOUNTER_OUTPATIENT_0043 = "ENCOUNTER_OUTPATIENT_0043";
	public static final String ENCOUNTER_OUTPATIENT_0018 = "ENCOUNTER_OUTPATIENT_0018";
	public static final String DIABETES = "DIABETES";

	public boolean isSyndrome() {
	return syndrome;
	}

	public void setSyndrome(boolean syndrome) {
	this.syndrome = syndrome;
	}
}
