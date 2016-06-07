package com.nzion.domain.emr.soap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.enums.Operator;

@Entity
@Filters( { @Filter(name = "EnabledFilter",condition="(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class RuleCondition extends IdGeneratingBaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static Map<Class<?>, List<Operator>> operatorsMap = new HashMap<Class<?>, List<Operator>>();
	static {
		operatorsMap.put(String.class, Arrays.asList(new Operator[] { Operator.EQUALS, Operator.STARTS_WITH }));
		operatorsMap.put(Integer.TYPE, Arrays.asList(new Operator[] { Operator.EQUALS, Operator.STARTS_WITH }));
	}
	// age > 18 vale i
	private String domainName;
	private String columnName;
	private Operator operator;
	private String rhs;
	private String lhs;
	private String validValue;

	public static Map<Class<?>, List<Operator>> getOperatorsMap() {
	return operatorsMap;
	}

	public static void setOperatorsMap(Map<Class<?>, List<Operator>> operatorsMap) {
	RuleCondition.operatorsMap = operatorsMap;
	}

	public String getDomainName() {
	return domainName;
	}

	public void setDomainName(String domainName) {
	this.domainName = domainName;
	}

	public String getColumnName() {
	return columnName;
	}

	public void setColumnName(String columnName) {
	this.columnName = columnName;
	}

	public Operator getOperator() {
	return operator;
	}

	public void setOperator(Operator operator) {
	this.operator = operator;
	}

	public String getRhs() {
	return rhs;
	}

	public void setRhs(String rhs) {
	this.rhs = rhs;
	}

	public String getLhs() {
	return lhs;
	}

	public void setLhs(String lhs) {
	this.lhs = lhs;
	}
}