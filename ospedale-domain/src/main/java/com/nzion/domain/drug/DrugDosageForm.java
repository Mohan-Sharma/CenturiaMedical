package com.nzion.domain.drug;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.emr.MasterEntity;

@Entity
@Table(name = "DRUG_DOSAGE_FORM_MASTER", uniqueConstraints = { @UniqueConstraint(columnNames = { "CODE" }) })
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class DrugDosageForm extends MasterEntity {
	private static final long serialVersionUID = 890393010614834247L;

}
