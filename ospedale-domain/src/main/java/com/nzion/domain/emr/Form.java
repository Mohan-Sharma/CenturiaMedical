/**
 * @author shwetha
 * Nov 9, 2010 
 */
package com.nzion.domain.emr;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

@Entity
@Table(name="FORM", uniqueConstraints = {@UniqueConstraint(columnNames={"CODE"})})
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class Form extends MasterEntity {

	private static final long serialVersionUID = 1L;

}
