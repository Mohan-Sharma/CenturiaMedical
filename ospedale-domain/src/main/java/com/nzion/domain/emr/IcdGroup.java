package com.nzion.domain.emr;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.util.UtilValidator;

@Entity
@Table(name = "ICD_GROUP")
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class IcdGroup extends IdGeneratingBaseEntity {

	private String groupName;

	private String groupDescription;

	private Set<IcdElement> icdElements;

	@Column(name = "GROUP_NAME")
	public String getGroupName() {
	return groupName;
	}

	public void setGroupName(String groupName) {
	this.groupName = groupName;
	}

	@Column(name = "GROUP_DESCRIPTION")
	public String getGroupDescription() {
	return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
	this.groupDescription = groupDescription;
	}

	@ManyToMany
	@JoinTable(name = "ICDGROUP_ICDELEMENT", joinColumns = { @JoinColumn(name = "GROUP_ID") }, inverseJoinColumns = { @JoinColumn(name = "ICD_ID") })
	public Set<IcdElement> getIcdElements() {
	if (UtilValidator.isEmpty(icdElements)) icdElements = new HashSet<IcdElement>();
	return icdElements;
	}

	public void setIcdElements(Set<IcdElement> icdElements) {
	this.icdElements = icdElements;
	}

	private static final long serialVersionUID = 1L;

}
