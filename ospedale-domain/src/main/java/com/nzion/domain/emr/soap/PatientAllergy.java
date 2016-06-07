package com.nzion.domain.emr.soap;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.emr.Allergy;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */

@Entity
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class PatientAllergy extends IdGeneratingBaseEntity{

	private String allergy;

	private Enumeration allergyType;

	private Allergy reaction;

	private String comments;

	private Date onSetDate;

	private Enumeration severity;

	private Enumeration allergyStatus;

	private Enumeration allergyClass;

	private String sensitivity;

	private String sourceInfo;

	private String drugReaction;
	
	private String reactionDescription;

	private boolean currentSectionAllergy = true;
	
	private Patient patient;
	
	private SoapSection soapSection;
	
	private String reactionName;
	
	private String allergyNotes;
	
	public String getAllergyNotes() {
		return allergyNotes;
	}

	public void setAllergyNotes(String allergyNotes) {
		this.allergyNotes = allergyNotes;
	}

	public String getAllergy() {
	return allergy;
	}

	public void setAllergy(String allergy) {
	this.allergy = allergy;
	}

	@ManyToOne
	@JoinColumn(name = "ALLERGY_TYPE_ID")
	public Enumeration getAllergyType() {
	return allergyType;
	}

	public void setAllergyType(Enumeration allergyType) {
	this.allergyType = allergyType;
	}

	@OneToOne
	@ForeignKey(name = "REACTION_FK")
	@JoinColumn(name = "REACTION_ID")
	public Allergy getReaction() {
	return reaction;
	}

	public void setReaction(Allergy reaction) {
	this.reaction = reaction;
	}

	public String getComments() {
	return comments;
	}

	public void setComments(String comments) {
	this.comments = comments;
	}

	@Column(name = "ON_SET_DATE")
	@Temporal(TemporalType.DATE)
	public Date getOnSetDate() {
	return onSetDate;
	}

	public void setOnSetDate(Date onSetDate) {
	this.onSetDate = onSetDate;
	}

	@ManyToOne
	@JoinColumn(name = "ALLERGY_SEVERITY_ID")
	public Enumeration getSeverity() {
	return severity;
	}

	public void setSeverity(Enumeration severity) {
	this.severity = severity;
	}

	@ManyToOne
	@JoinColumn(name = "ALLERGY_STATUS_ID")
	public Enumeration getAllergyStatus() {
	return allergyStatus;
	}

	public void setAllergyStatus(Enumeration allergyStatus) {
	this.allergyStatus = allergyStatus;
	}

	@ManyToOne
	@JoinColumn(name = "ALLERGY_CLASS_ID")
	public Enumeration getAllergyClass() {
	return allergyClass;
	}

	public void setAllergyClass(Enumeration allergyClass) {
	this.allergyClass = allergyClass;
	}

	public String getSensitivity() {
	return sensitivity;
	}

	public void setSensitivity(String sensitivity) {
	this.sensitivity = sensitivity;
	}

	public String getSourceInfo() {
	return sourceInfo;
	}

	public void setSourceInfo(String sourceInfo) {
	this.sourceInfo = sourceInfo;
	}

	public String getDrugReaction() {
	return drugReaction;
	}

	public void setDrugReaction(String drugReaction) {
	this.drugReaction = drugReaction;
	}

	private static final long serialVersionUID = 1L;

	@Transient
	public String getReactionDescription() {
	if(reaction!=null){
		reactionDescription=reaction.getDescription();	
	}
	if(drugReaction!=null){
		reactionDescription=drugReaction;	
	}
	return reactionDescription;
	}

	public void setReactionDescription(String reactionDescription) {
	this.reactionDescription = reactionDescription;
	}
	
	public boolean isCurrentSectionAllergy() {
	return currentSectionAllergy;
	}

	public void setCurrentSectionAllergy(boolean currentSectionAllergy) {
	this.currentSectionAllergy = currentSectionAllergy;
	}
	
	public PatientAllergy createCopy(){
	PatientAllergy copyAllergy = new PatientAllergy();
	try {
		BeanUtils.copyProperties(copyAllergy, this);
	} catch (Exception e) {
		throw new RuntimeException("coping data from previous soapnote failed");
	}
	copyAllergy.setCurrentSectionAllergy(false);
	copyAllergy.setId(null);
	return copyAllergy;
	}
	
	@OneToOne
	@JoinColumn(name="PATIENT_ID",nullable=false)
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@ManyToOne
	@JoinColumn(name="ALLERGY_SECTION_ID",nullable=false)
	public SoapSection getSoapSection() {
	return soapSection;
	}

	public void setSoapSection(SoapSection soapSection) {
	this.soapSection = soapSection;
	}
	
	public String getReactionName() {
	return reactionName;
	}

	public void setReactionName(String reactionName) {
	this.reactionName = reactionName;
	}
}