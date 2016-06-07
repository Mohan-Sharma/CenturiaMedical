package com.nzion.domain.emr;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table
public class CVXCPTMapping extends IdGeneratingBaseEntity {

	private String cvxCode;

	private Cpt cpt;
	
	private String vaccineName;

	private String comments;

	public String getCvxCode() {
	return cvxCode;
	}

	public void setCvxCode(String cvxCode) {
	this.cvxCode = cvxCode;
	}

	public String getVaccineName() {
	return vaccineName;
	}

	public void setVaccineName(String vaccineName) {
	this.vaccineName = vaccineName;
	}

	@Column(name="MAPPING_COMMENTS")
	public String getComments() {
	return comments;
	}

	public void setComments(String comments) {
	this.comments = comments;
	}
	
	@OneToOne
	@JoinColumn(name="CPT_CODE")
	public Cpt getCpt() {
	return cpt;
	}

	public void setCpt(Cpt cpt) {
	this.cpt = cpt;
	}

	private static final long serialVersionUID = 1L;

}
