package com.nzion.domain.emr;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Index;

import com.nzion.domain.base.IdGeneratingBaseEntity;

/**
 * @author Sandeep Prusty
 * Jul 28, 2010
 */

@Entity
@Table(name="ICD_ELEMENT",uniqueConstraints={ @UniqueConstraint(name="UNIQUE_ICD_CODE",columnNames = { "CODE","ICD_VERSION"}) })
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
@Filter(name = "ICD9Filter", condition = "'ICD-9-CM'=ICD_VERSION")
public class IcdElement extends IdGeneratingBaseEntity {

	private String code;

	private String description;

	private String ccsCategory;

	private String ccsLabel;
	
	private String icdVersion;
	
	private IcdElement parent;
	
	private Type type;
	
	@Column(name="ICD_VERSION")
	public String getIcdVersion() {
	return icdVersion;
	}

	public void setIcdVersion(String icdVersion) {
	this.icdVersion = icdVersion;
	}

	@Column(name="TYPE")
	@Enumerated(EnumType.STRING)
	public Type getType() {
	return type;
	}

	public void setType(Type type) {
	this.type = type;
	}

	@ManyToOne(targetEntity=IcdElement.class)
	@JoinColumn(name="PARENT_ID")
	public IcdElement getParent() {
	return parent;
	}

	public void setParent(IcdElement parent) {
	this.parent = parent;
	}

	@Column(name="CODE")
	public String getCode() {
	return code;
	}

	public void setCode(String code) {
	this.code = code;
	}

	@Column(name="DESCRIPTION")
	@Index(name = "ICD_DESC_IDX")
	public String getDescription() {
	return description;
	}

	public void setDescription(String description) {
	this.description = description;
	}

	@Column(name="CCS_CATEGORY")
	public String getCcsCategory() {
	return ccsCategory;
	}

	public void setCcsCategory(String ccsCategory) {
	this.ccsCategory = ccsCategory;
	}

	@Column(name="CCS_LABEL")
	public String getCcsLabel() {
	return ccsLabel;
	}

	public void setCcsLabel(String ccsLabel) {
	this.ccsLabel = ccsLabel;
	}

	public static enum Type {
		CHAPTER, BLOCK, DISEASECODE;
	}
	@Override
	public String toString() {
	return code + "-" + (description == null ? "" : description);
	}

	private static final long serialVersionUID = 1L;
}