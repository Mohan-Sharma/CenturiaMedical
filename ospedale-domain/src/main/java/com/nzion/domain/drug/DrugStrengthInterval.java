/**
 * @author shwetha
 * Nov 17, 2010 
 */
package com.nzion.domain.drug;

import javax.persistence.Entity;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
public class DrugStrengthInterval extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = 1L;
	
	private String prefix;
	
	private String suffix;
	
	private Integer start;

	private Integer end;

	private Integer increment;

	public String getPrefix() {
	return prefix;
	}

	public void setPrefix(String prefix) {
	this.prefix = prefix;
	}

	public String getSuffix() {
	return suffix;
	}

	public void setSuffix(String suffix) {
	this.suffix = suffix;
	}

	public Integer getStart() {
	return start;
	}

	public void setStart(Integer start) {
	this.start = start;
	}

	public Integer getEnd() {
	return end;
	}

	public void setEnd(Integer end) {
	this.end = end;
	}

	public Integer getIncrement() {
	return increment;
	}

	public void setIncrement(Integer increment) {
	this.increment = increment;
	}
}
