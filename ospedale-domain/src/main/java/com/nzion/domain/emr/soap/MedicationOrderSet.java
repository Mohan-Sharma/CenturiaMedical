package com.nzion.domain.emr.soap;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.util.UtilValidator;

@Entity
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class MedicationOrderSet extends IdGeneratingBaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String orderSetName;

	// First Data Bank Drug Ids
	private List<String> drugs;

	public String getOrderSetName() {
	return orderSetName;
	}

	public void setOrderSetName(String orderSetName) {
	this.orderSetName = orderSetName;
	}

	@ElementCollection(targetClass = java.lang.String.class)
	@JoinTable(name = "MED_ORDER_SET_DRUG", joinColumns = @JoinColumn(name = "ORDER_SET_ID"))
	@org.hibernate.annotations.IndexColumn(name = "POSITION", base = 1)
	@JoinColumn(name = "FDB_DRUG_ID")
	public List<String> getDrugs() {
	if (UtilValidator.isEmpty(drugs)) drugs = new ArrayList<String>();
	return drugs;
	}

	public void setDrugs(List<String> drugs) {
	this.drugs = drugs;
	}

	public final static String SMOKING_CESSATION_AGENTS = "Smoking Cessation Agents";
}