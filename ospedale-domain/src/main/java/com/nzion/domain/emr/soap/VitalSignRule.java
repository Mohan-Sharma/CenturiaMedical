package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.VitalSign;

@Entity
@Filters( { @Filter(name = "EnabledFilter",condition="(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class VitalSignRule extends IdGeneratingBaseEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private VitalSign vitalSign;
	private Set<RuleCondition> conditions=new HashSet<RuleCondition>();

	@OneToMany
	public Set<RuleCondition> getConditions() {
	return conditions;
	}

	public void setConditions(Set<RuleCondition> conditions) {
	this.conditions = conditions;
	}

	@OneToOne
	public VitalSign getVitalSign() {
	return vitalSign;
	}

	public void setVitalSign(VitalSign vitalSign) {
	this.vitalSign = vitalSign;
	}
	
	
}


