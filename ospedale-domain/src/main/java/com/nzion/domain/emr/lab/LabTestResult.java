package com.nzion.domain.emr.lab;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table
public class LabTestResult extends IdGeneratingBaseEntity {

	private String technicianComment;

	private String providerComment;

	private boolean reviewedByProvider;

	private Set<LabTestResultItem> labTestResultItems;

	private Date specimenReceivedDate;

	private Set<LabTestCpt> labCpts;

	private Date testPerformedOn;

	private LabTest panel;

	@ManyToMany(targetEntity = LabTestCpt.class, fetch = FetchType.EAGER)
	@Cascade(CascadeType.ALL)
	public Set<LabTestCpt> getLabCpts() {
	return labCpts;
	}

	public void setLabCpts(Set<LabTestCpt> labCpts) {
	this.labCpts = labCpts;
	}

	public String getTechnicianComment() {
	return technicianComment;
	}

	public void setTechnicianComment(String technicianComment) {
	this.technicianComment = technicianComment;
	}

	public String getProviderComment() {
	return providerComment;
	}

	public void setProviderComment(String providerComment) {
	this.providerComment = providerComment;
	}

	public boolean isReviewedByProvider() {
	return reviewedByProvider;
	}

	public void setReviewedByProvider(boolean reviewedByProvider) {
	this.reviewedByProvider = reviewedByProvider;
	}

	@OneToMany(targetEntity = LabTestResultItem.class, cascade = javax.persistence.CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "labTestResult")
	public Set<LabTestResultItem> getLabTestResultItems() {
	if (labTestResultItems == null)
		labTestResultItems = new HashSet<LabTestResultItem>();
	return labTestResultItems;
	}

	public void setLabTestResultItems(Set<LabTestResultItem> labTestResultItems) {
	this.labTestResultItems = labTestResultItems;
	}

	@Transient
	public void addLabTestResultItem(LabTestResultItem labTestResultItem) {
	labTestResultItem.setLabTestResult(this);
	getLabTestResultItems().add(labTestResultItem);
	}


	public Date getSpecimenReceivedDate() {
	return specimenReceivedDate;
	}

	public void setSpecimenReceivedDate(Date specimenReceivedDate) {
	this.specimenReceivedDate = specimenReceivedDate;
	}

	public Date getTestPerformedOn() {
	return testPerformedOn;
	}

	public void setTestPerformedOn(Date testPerformedOn) {
	this.testPerformedOn = testPerformedOn;
	}

	private static final long serialVersionUID = 1L;

	@OneToOne
	public LabTest getLabTest() {
	return panel;
	}

	public void setLabTest(LabTest panel) {
	this.panel = panel;
	}
}