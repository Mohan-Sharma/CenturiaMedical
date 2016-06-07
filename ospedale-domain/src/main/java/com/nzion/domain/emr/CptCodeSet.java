package com.nzion.domain.emr;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.BaseEntity;
import com.nzion.util.UtilReflection;

@Entity
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class CptCodeSet extends BaseEntity  {

	private static final long serialVersionUID = 1L;



	private String id;

	private String name;

	private String description;

	private Set<Cpt> cpts;

	@ManyToMany
	@JoinTable(name = "CPTCODESET_CPTELEMENT", joinColumns = { @JoinColumn(name = "CPTCODESET_ID") }, inverseJoinColumns = { @JoinColumn(name = "CPT_ID") })
	@Fetch(FetchMode.SELECT)
	public Set<Cpt> getCpts() {
	return cpts;
	}

	public void setCpts(Set<Cpt> cpts) {
	this.cpts = cpts;
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

	public static CptCodeSet merge(CptCodeSet... codeSets) {
	CptCodeSet newCodeSet = new CptCodeSet();
	newCodeSet.setCpts(new HashSet<Cpt>());
	for (CptCodeSet set : codeSets)
		if (set != null) newCodeSet.getCpts().addAll(set.getCpts());
	return newCodeSet;
	}

	public static final String CCD_OFFICE_VISIT="CCD-OFFICE-VISIT";
	
	public static final String ENCOUNTER_OUTPATIENT_0013 = "ENCOUNTER_OUTPATIENT_0013";

	public static final String ENCOUNTER_NURSING_0013 = "ENCOUNTER_NURSING_0013";

	public static final String ENCOUNTER_OUTPATIENT_0018 = "ENCOUNTER_OUTPATIENT_0018";

	public static final String ESRD_0018 = "ESRD_0018";

	public static final String ENCOUNTER_OUTPATIENT_0027 = "ENCOUNTER_OUTPATIENT_0027";

	public static final String ENCOUNTER_PREVENTIVE_MEDICINE_SERVICES_18_AND_OLDER_028a = "ENCOUNTER_PREVENTIVE_MEDICINE_SERVICES_18_AND_OLDER_028a";

	public static final String ENCOUNTER_PREV_INDIVIDUAL_COUNSELING_028a = "ENCOUNTER_PREV_INDIVIDUAL_COUNSELING_028a";

	public static final String ENCOUNTER_PREV_MED_GROUP_COUNSELING_028a = "ENCOUNTER_PREV_MED_GROUP_COUNSELING_028a";

	public static final String ENCOUNTER_PREV_MED_OTHER_SERVICES_028a = "ENCOUNTER_PREV_MED_OTHER_SERVICES_028a";

	public static final String ENCOUNTER_OFFICE_VISIT_028a = "ENCOUNTER_OFFICE_VISIT_028a";

	public static final String ENCOUNTER_HEALTH_AND_BEHAVIOR_ASSESSMENT_028a = "ENCOUNTER_HEALTH_AND_BEHAVIOR_ASSESSMENT_028a";

	public static final String ENCOUNTER_OCCUPATIONAL_THERAPY_028a = "ENCOUNTER_OCCUPATIONAL_THERAPY_028a";

	public static final String ENCOUNTER_PSYCHIATRIC_AND_PSYCHOLOGIC_028a = "ENCOUNTER_PSYCHIATRIC_AND_PSYCHOLOGIC_028a";

	public static final String TOBACCO_USE_CESSATION_COUNSELING_0027 = "TOBACCO_USE_CESSATION_COUNSELING_0027";
	
	public static final String FOLLOW_UP_PLAN_BMI_MANAGEMENT_0421="FOLLOW_UP_PLAN_BMI_MANAGEMENT_0421";
	public static final String ENCOUNTER_OUTPATIENT_CPT_WITH_CLINICIAN_0421 = "ENCOUNTER_OUTPATIENT_CPT_WITH_CLINICIAN_0421";
	
	public static final String ENCOUNTER_OUTPATIENT_0043 = "ENCOUNTER_OUTPATIENT_0043";


}