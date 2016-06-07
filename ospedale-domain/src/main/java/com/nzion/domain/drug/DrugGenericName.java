package com.nzion.domain.drug;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import com.nzion.domain.emr.MasterEntity;

	@Entity
	@Table(name = "DRUG_GENERIC_NAME", uniqueConstraints = { @UniqueConstraint(columnNames = { "CODE" }) })
	@Filters( {
			@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
	public class DrugGenericName extends MasterEntity {
		private static final long serialVersionUID = 6747315876398580852L;
	}


