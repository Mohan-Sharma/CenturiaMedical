package com.nzion.domain.emr.soap;

import java.util.Date;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.nzion.domain.Patient;
import com.nzion.domain.Provider;
import com.nzion.domain.Referral;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.docmgmt.PatientEducationDocument;
import com.nzion.domain.emr.ReferalLetterTemplate;
import com.nzion.util.Constants;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * May 20, 2011
 */

@Entity
public class SOAPPlan extends IdGeneratingBaseEntity {

	private ReferalLetterTemplate referalLetterTemplate;

	private Referral referral;

	private String doctorsPlan;

	private String exclusionReason;

	private String followUpType = "";

	private String note;

	private String followUpFor;

	private Boolean medicationGiven;

	private RecommendationSection recommendationSection;

	private FollowUp followUp;

	private Set<PatientEducationDocument> downLoadedDocuments;

	private Patient patient;
	
	private Provider provider;
	
	private Boolean remainderSent = Boolean.FALSE;
	
	private Date remainderSentOn;
	
	private String sentence;
	
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROVIDER_ID")
	public Provider getProvider() {
	return provider;
	}

	public void setProvider(Provider provider) {
	this.provider = provider;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PATIENT_ID")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "SOAP_PLAN_DOWNLOADS", joinColumns = { @JoinColumn(name = "SOAP_PLAN_ID") }, inverseJoinColumns = { @JoinColumn(name = "PATIENT_EDUCATION_DOCUMENT_ID") })
	public Set<PatientEducationDocument> getDownLoadedDocuments() {
	return downLoadedDocuments;
	}

	public void setDownLoadedDocuments(Set<PatientEducationDocument> downLoadedDocuments) {
	this.downLoadedDocuments = downLoadedDocuments;
	}

	@Embedded
	public FollowUp getFollowUp() {
	return followUp;
	}

	public void setFollowUp(FollowUp followUp) {
	this.followUp = followUp;
	}

	@ManyToOne
	@JoinColumn(name = "RECOMMENDATION_SECTION_ID")
	public RecommendationSection getRecommendationSection() {
	return recommendationSection;
	}

	public void setRecommendationSection(RecommendationSection recommendationSection) {
	this.recommendationSection = recommendationSection;
	}

	public Boolean getMedicationGiven() {
	return medicationGiven == null ? false : medicationGiven;
	}

	public void setMedicationGiven(Boolean medicationGiven) {
	this.medicationGiven = medicationGiven;
	}

	public String getFollowUpFor() {
	return followUpFor;
	}

	public void setFollowUpFor(String followUpFor) {
	this.followUpFor = followUpFor;
	}

	@OneToOne
	@JoinColumn(name = "REFERAL_LETTER_TEMPLATE_ID")
	public ReferalLetterTemplate getReferalLetterTemplate() {
	return referalLetterTemplate;
	}

	public void setReferalLetterTemplate(ReferalLetterTemplate referalLetterTemplate) {
	this.referalLetterTemplate = referalLetterTemplate;
	}

	@OneToOne
	@JoinColumn(name = "REFERAL_ID")
	public Referral getReferral() {
	return referral;
	}

	public void setReferral(Referral referral) {
	this.referral = referral;
	}

	public String getDoctorsPlan() {
	return doctorsPlan;
	}

	public void setDoctorsPlan(String doctorsPlan) {
	this.doctorsPlan = doctorsPlan;
	}

	public String getExclusionReason() {
	return exclusionReason;
	}

	public void setExclusionReason(String exclusionReason) {
	this.exclusionReason = exclusionReason;
	}

	public String getFollowUpType() {
	return followUpType;
	}

	public void setFollowUpType(String followUpType) {
	this.followUpType = followUpType;
	}

	public String getNote() {
	return note;
	}

	public void setNote(String note) {
	this.note = note;
	}

	public Boolean getRemainderSent() {
	return remainderSent;
	}

	public void setRemainderSent(Boolean remainderSent) {
	this.remainderSent = remainderSent;
	}
	
	public Date getRemainderSentOn() {
	return remainderSentOn;
	}

	public void setRemainderSentOn(Date remainderSentOn) {
	this.remainderSentOn = remainderSentOn;
	}
	
	public String getSentence() {
	if(this == null)
		return Constants.BLANK;
	if(RecommendationSection.TOBACCO_PLAN.equals(this.followUpFor))
		sentence = buildSentenceForTobaccoCessation();
	if(RecommendationSection.BMI_PLAN.equals(this.followUpFor))
		sentence = buildSentenceForBMIFollowUp();
	return sentence;
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}
	
	public String buildSentenceForTobaccoCessation() {
	StringBuilder builder = new StringBuilder();
	if (UtilValidator.isNotEmpty(this.getDoctorsPlan()))
		builder.append("Counselling Notes - ").append(this.getDoctorsPlan()).append(".");
	if (this.getMedicationGiven()) builder.append("Recommended to take smoking cessation agents.");
	if (this.getFollowUp() != null && this.getFollowUp().getFollowUpDuration() != null
			&& UtilValidator.isNotEmpty(this.getFollowUp().getFollowUpDurationType()))
		builder.append("The patient is to return to see me in ").append(this.getFollowUp()).append(".");
	return builder.toString();
	}


	public String buildSentenceForBMIFollowUp() {
	StringBuilder builder = new StringBuilder();
	if (UtilValidator.isNotEmpty(this.getExclusionReason())) {
		builder.append("BMI Follow Up Plan excluded ").append(this.getExclusionReason()).append(".");
		return builder.toString();
	}
	if (UtilValidator.isNotEmpty(this.getDoctorsPlan()))
		builder.append("Counselling Notes - ").append(this.getDoctorsPlan()).append(".");
	if (this.getFollowUp() != null && this.getFollowUp().getFollowUpDuration() != null
			&& UtilValidator.isNotEmpty(this.getFollowUp().getFollowUpDurationType()))
		builder.append("The patient is to return to see me in ").append(this.getFollowUp()).append(".");
	return builder.toString();
	}

	public Date generateExpectedDate(){
	return followUp.generateExpectedDate(recommendationSection.getSoapNote().getDate());
	}
	
	@Transient
	public boolean isDone(){
	return !UtilValidator.isAllEmpty(this, "doctorsPlan", "note", "referral") || getMedicationGiven()
					|| !UtilValidator.isAllEmpty(followUp, "followUpDuration", "followUpDurationType", "expectedDate", "alertBefore", "alertDurationType");
	}

	public static final String MYPLAN = "myplan";

	public static final String REFERRAL = "referral";

	public static final String EXCLUSION = "exclusion";

	private static final long serialVersionUID = 1L;

}