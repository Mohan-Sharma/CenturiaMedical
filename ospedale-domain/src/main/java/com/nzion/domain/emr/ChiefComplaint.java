package com.nzion.domain.emr;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;
import org.hibernate.annotations.Type;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Practice;
import com.nzion.domain.base.BaseEntity;

@Entity
@Table(name = "CHIEFCOMPLAINTS", uniqueConstraints = { @UniqueConstraint(name="UNIQUE_PRACTICE_COMPLAINT_NAME",columnNames = { "COMPLAINT_NAME"}) })
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class ChiefComplaint extends BaseEntity implements ModuleMarker,Comparable<ChiefComplaint>{

	private static final long serialVersionUID = 1L;
	private String complainName;
	private Integer sortOrder;
	private Enumeration gender;
	private Boolean isMultiple;
	private Boolean isYesNo;
	private QATemplate qaTemplate;


	@ManyToOne(optional = true)
	@JoinColumn(name = "QATEMPLATE_ID")
	public QATemplate getQaTemplate() {
	return qaTemplate;
	}

	public void setQaTemplate(QATemplate qaTemplate) {
	this.qaTemplate = qaTemplate;
	}

	@Column(name = "COMPLAINT_NAME")
	@Id
	public String getComplainName() {
	return complainName;
	}

	public void setComplainName(String complainName) {
	this.complainName = complainName;
	}

	@Column(name = "SORT_ORDER")
	public Integer getSortOrder() {
	return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
	this.sortOrder = sortOrder;
	}

	@Column(name = "IS_MULTIPLE")
	@Type(type = "yes_no")
	public Boolean getIsMultiple() {
	return isMultiple;
	}

	public void setIsMultiple(Boolean isMultiple) {
	this.isMultiple = isMultiple;
	}

	@Column(name = "IS_YESNO")
	@Type(type = "yes_no")
	public Boolean getIsYesNo() {
	return isYesNo;
	}

	public void setIsYesNo(Boolean isYesNo) {
	this.isYesNo = isYesNo;
	}

	@Override
	public int hashCode() {
	final int prime = 31;
	int result = 0;
	result = prime * result + ((complainName == null) ? 0 : complainName.hashCode());
	return result;
	}

	@Override
	public boolean equals(Object obj) {
	if (this == obj) return true;
	if (obj == null) return false;
	if (getClass() != obj.getClass()) return false;
	ChiefComplaint other = (ChiefComplaint) obj;
	if (complainName == null) {
		if (other.complainName != null) return false;
	} else
		if (!complainName.equals(other.complainName)) return false;
	return true;
	}

	@Override
	public int compareTo(ChiefComplaint chiefComplaint) {
	return this.complainName.compareToIgnoreCase(chiefComplaint.complainName);
	}

	@OneToOne
	public Enumeration getGender() {
	return gender;
	}

	public void setGender(Enumeration gender) {
	this.gender = gender;
	}

}