package com.nzion.domain.emr.soap;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.nzion.domain.Enumeration;
import com.nzion.domain.Patient;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.enums.SmokingStatusEnum;
import com.nzion.util.Constants;
import com.nzion.util.SentenceUtil;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class PatientSocialHistory extends IdGeneratingBaseEntity {

	private static final long serialVersionUID = -286266867776574715L;
	private Enumeration maritalStatus;
	private Enumeration occupation;
	private Boolean alcohol = Boolean.FALSE;
	private Boolean exercise = Boolean.FALSE;
	private String otherExercise;
	private String reasonForNotExercising;
	private String physicalActivityOne;
	private String physicalActivityTwo;
	private String otherPhysicalActivity;
	private Integer activityOneDuration;
	private Integer activityTwoDuration;
	private Integer otherActivityDuration;
	private String activityOneFrequency;
	private String activityTwoFrequency;
	private String otherActivityFrequency;
	private String exerciseComments;//length = 1024 
	private String hobbies;//length = 500
	private Boolean normalWeight = Boolean.FALSE;
	private String patientDiet = Constants.BLANK;
	private String abnormalWeightReason;
	private String dietComments;//length = 1024
	private Boolean drugIntake = Boolean.FALSE;
	private String otherDrugs;
	private Boolean needleToInjectDrugs = Boolean.FALSE;
	private String drugComments;//length = 1024
	private String safetyIssueComments;//length = 1024
	private Boolean seatBelts = Boolean.FALSE;
	private Boolean helmets = Boolean.FALSE;
	private Boolean violenceAtHome = Boolean.FALSE;
	private Boolean gunAtHome = Boolean.FALSE;
	private Boolean fear = Boolean.FALSE; //confirm
	private Boolean beingHit = Boolean.FALSE;//confirm
	private Boolean forcedSexualActivity = Boolean.FALSE;
	private Integer cigrattesPerDay = new Integer(0);
	private String amountOfAlcohol;
	private Float durationOfSmoking = new Float(0.0F);
	private Float stoppedSmokingSince = new Float(0.0F);
	private Boolean alcoholicHistory = Boolean.FALSE;
	private String amountOfAlcoholPerWeek;
	private Float stoppedAlcoholSince = new Float(0.0F);
	private String lifeStyle;
	private String dailyActivities;
	private Boolean anyChildren = Boolean.FALSE;
	private Integer numberOfChildren = new Integer(0);
	private Boolean anyPregnancy = Boolean.FALSE;
	private Integer numberOfPregnancy = new Integer(0);
	private Boolean anyAbortions = Boolean.FALSE;
	private Integer numberOfAbortions = new Integer(0);
	private Boolean anyMiscarriage = Boolean.FALSE;
	private Integer numberOfMiscarriage = new Integer(0);
	private Boolean lactating = Boolean.FALSE;
	private String outsideTripDetails;
	private String comments;
	private SocialHistorySection soapSection;
	private SmokingStatusEnum smokingStatus;
	private Date lastMensturalPeriod;
	private Integer ageAtFirstMensturalPeroid;
	private Integer noOfMensturalDays;
	private Integer noOfMensturalWeeks;
	private Boolean regularMensturalCycle;
	private String amountOfMensturation;
	private Boolean isMenoPause = Boolean.FALSE;
	private String birthControlMethod;
	private Integer noOfPreTermVagDeliveries;
	private Integer noOfFullTermVagDeliveries;
	private Integer noOfPreTermCSectDeliveries;
	private Integer noOfFullTermCSectDeliveries;
	private String ageOfFirstSexualInterCourse;
	private String noOfSexualPartners;
	private String livingArrangement;
	private String reasonForOtherStress;
	private Date lastPapSmear;
	private Date lastMammogram;
	private String childHood;
	private String smokingTimeUnit = "years(s)";
	private String alcoholTimeUnit = "years(s)";
	private String sentence;
	private String sexualActivity;
	private Boolean strictReligiousEnvironment = Boolean.FALSE;;
	private Boolean religiousBeliefs = Boolean.FALSE;
	private String effectOfPatientBelief;
	private String gynecologicalProblems = Constants.BLANK;
	private String stresses = Constants.BLANK;
	private String otherMethodOfBirthControl;
	private String otherLivingArrangement;
	private String otherStress;
	private boolean terminallyIll;

	private Patient patient;

	@OneToOne
	@JoinColumn(name = "PATIENT_ID")
	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public String getGynecologicalProblems() {
	return gynecologicalProblems == null ? "" : gynecologicalProblems;
	}

	public void setGynecologicalProblems(String gynecologicalProblems) {
	this.gynecologicalProblems = gynecologicalProblems;
	}

	public String getStresses() {
	return stresses == null ? "" : stresses;
	}

	public void setStresses(String stresses) {
	this.stresses = stresses;
	}

	public String getOtherMethodOfBirthControl() {
	return otherMethodOfBirthControl;
	}

	public void setOtherMethodOfBirthControl(String otherMethodOfBirthControl) {
	this.otherMethodOfBirthControl = otherMethodOfBirthControl;
	}

	public String getOtherLivingArrangement() {
	return otherLivingArrangement;
	}

	public void setOtherLivingArrangement(String otherLivingArrangement) {
	this.otherLivingArrangement = otherLivingArrangement;
	}

	public String getOtherStress() {
	return otherStress;
	}

	public void setOtherStress(String otherStress) {
	this.otherStress = otherStress;
	}

	public Boolean getStrictReligiousEnvironment() {
	return strictReligiousEnvironment == null ? false : strictReligiousEnvironment;
	}

	public void setStrictReligiousEnvironment(Boolean strictReligiousEnvironment) {
	this.strictReligiousEnvironment = strictReligiousEnvironment;
	}

	public Boolean getReligiousBeliefs() {
	return religiousBeliefs == null ? false : religiousBeliefs;
	}

	public void setReligiousBeliefs(Boolean religiousBeliefs) {
	this.religiousBeliefs = religiousBeliefs;
	}

	public String getEffectOfPatientBelief() {
	return effectOfPatientBelief;
	}

	public void setEffectOfPatientBelief(String effectOfPatientBelief) {
	this.effectOfPatientBelief = effectOfPatientBelief;
	}

	public String getAgeOfFirstSexualInterCourse() {
	return ageOfFirstSexualInterCourse;
	}

	public void setAgeOfFirstSexualInterCourse(String ageOfFirstSexualInterCourse) {
	this.ageOfFirstSexualInterCourse = ageOfFirstSexualInterCourse;
	}

	public String getNoOfSexualPartners() {
	return noOfSexualPartners;
	}

	public void setNoOfSexualPartners(String noOfSexualPartners) {
	this.noOfSexualPartners = noOfSexualPartners;
	}

	public String getSexualActivity() {
	return sexualActivity;
	}

	public void setSexualActivity(String sexualActivity) {
	this.sexualActivity = sexualActivity;
	}

	@OneToOne(targetEntity = Enumeration.class)
	@JoinColumn(name = "MARITAL_CODE")
	public Enumeration getMaritalStatus() {
	return maritalStatus;
	}

	public void setMaritalStatus(Enumeration maritalStatus) {
	this.maritalStatus = maritalStatus;
	}

	@OneToOne(targetEntity = Enumeration.class)
	@JoinColumn(name = "OCCUPATION_ID")
	public Enumeration getOccupation() {
	return occupation;
	}

	public void setOccupation(Enumeration occupation) {
	this.occupation = occupation;
	}

	public Date getLastMensturalPeriod() {
	return lastMensturalPeriod;
	}

	public void setLastMensturalPeriod(Date lastMensturalPeriod) {
	this.lastMensturalPeriod = lastMensturalPeriod;
	}

	public Integer getAgeAtFirstMensturalPeroid() {
	return ageAtFirstMensturalPeroid;
	}

	public void setAgeAtFirstMensturalPeroid(Integer ageAtFirstMensturalPeroid) {
	this.ageAtFirstMensturalPeroid = ageAtFirstMensturalPeroid;
	}

	public Integer getNoOfMensturalDays() {
	return noOfMensturalDays;
	}

	public void setNoOfMensturalDays(Integer noOfMensturalDays) {
	this.noOfMensturalDays = noOfMensturalDays;
	}

	public Integer getNoOfMensturalWeeks() {
	return noOfMensturalWeeks;
	}

	public void setNoOfMensturalWeeks(Integer noOfMensturalWeeks) {
	this.noOfMensturalWeeks = noOfMensturalWeeks;
	}

	public Boolean getRegularMensturalCycle() {
	return regularMensturalCycle;
	}

	public void setRegularMensturalCycle(Boolean regularMensturalCycle) {
	this.regularMensturalCycle = regularMensturalCycle;
	}

	public String getAmountOfMensturation() {
	return amountOfMensturation;
	}

	public void setAmountOfMensturation(String amountOfMensturation) {
	this.amountOfMensturation = amountOfMensturation;
	}

	public Boolean getIsMenoPause() {
	return isMenoPause == null ? false : isMenoPause;
	}

	public void setIsMenoPause(Boolean isMenoPause) {
	this.isMenoPause = isMenoPause;
	}

	public String getBirthControlMethod() {
	return birthControlMethod;
	}

	public void setBirthControlMethod(String birthControlMethod) {
	this.birthControlMethod = birthControlMethod;
	}

	public Integer getNoOfPreTermVagDeliveries() {
	return noOfPreTermVagDeliveries;
	}

	public void setNoOfPreTermVagDeliveries(Integer noOfPreTermVagDeliveries) {
	this.noOfPreTermVagDeliveries = noOfPreTermVagDeliveries;
	}

	public Integer getNoOfFullTermVagDeliveries() {
	return noOfFullTermVagDeliveries;
	}

	public void setNoOfFullTermVagDeliveries(Integer noOfFullTermVagDeliveries) {
	this.noOfFullTermVagDeliveries = noOfFullTermVagDeliveries;
	}

	public Integer getNoOfPreTermCSectDeliveries() {
	return noOfPreTermCSectDeliveries;
	}

	public void setNoOfPreTermCSectDeliveries(Integer noOfPreTermCSectDeliveries) {
	this.noOfPreTermCSectDeliveries = noOfPreTermCSectDeliveries;
	}

	public Integer getNoOfFullTermCSectDeliveries() {
	return noOfFullTermCSectDeliveries;
	}

	public void setNoOfFullTermCSectDeliveries(Integer noOfFullTermCSectDeliveries) {
	this.noOfFullTermCSectDeliveries = noOfFullTermCSectDeliveries;
	}

	public String getLivingArrangement() {
	return livingArrangement;
	}

	public void setLivingArrangement(String livingArrangement) {
	this.livingArrangement = livingArrangement;
	}

	public String getReasonForOtherStress() {
	return reasonForOtherStress;
	}

	public void setReasonForOtherStress(String reasonForOtherStress) {
	this.reasonForOtherStress = reasonForOtherStress;
	}

	public Date getLastPapSmear() {
	return lastPapSmear;
	}

	public void setLastPapSmear(Date lastPapSmear) {
	this.lastPapSmear = lastPapSmear;
	}

	public Date getLastMammogram() {
	return lastMammogram;
	}

	public void setLastMammogram(Date lastMammogram) {
	this.lastMammogram = lastMammogram;
	}

	public String getChildHood() {
	return childHood;
	}

	public void setChildHood(String childHood) {
	this.childHood = childHood;
	}

	public String getAmountOfAlcohol() {
	return amountOfAlcohol;
	}

	public void setAmountOfAlcohol(String amountOfAlcohol) {
	this.amountOfAlcohol = amountOfAlcohol;
	}

	public Float getStoppedAlcoholSince() {
	return stoppedAlcoholSince;
	}

	public void setStoppedAlcoholSince(Float stoppedAlcoholSince) {
	this.stoppedAlcoholSince = stoppedAlcoholSince;
	}

	public String getAmountOfAlcoholPerWeek() {
	return amountOfAlcoholPerWeek;
	}

	public void setAmountOfAlcoholPerWeek(String amountOfAlcoholPerWeek) {
	this.amountOfAlcoholPerWeek = amountOfAlcoholPerWeek;
	}

	public Integer getNumberOfMiscarriage() {
	return numberOfMiscarriage;
	}

	public void setNumberOfMiscarriage(Integer numberOfMiscarriage) {
	this.numberOfMiscarriage = numberOfMiscarriage;
	}

	@OneToOne
	@JoinColumn(name = "SOCIAL_HX_SECTION_ID")
	public SocialHistorySection getSoapSection() {
	return soapSection;
	}

	public void setSoapSection(SocialHistorySection section) {
	this.soapSection = section;
	}

	public Boolean getAlcohol() {
	return alcohol == null ? false : alcohol;
	}

	public void setAlcohol(Boolean alcohol) {
	this.alcohol = alcohol;
	}

	public Integer getCigrattesPerDay() {
	return cigrattesPerDay;
	}

	public void setCigrattesPerDay(Integer cigrattesPerDay) {
	this.cigrattesPerDay = cigrattesPerDay;
	}

	public Float getDurationOfSmoking() {
	return durationOfSmoking;
	}

	public void setDurationOfSmoking(Float durationOfSmoking) {
	this.durationOfSmoking = durationOfSmoking;
	}

	public Boolean getAlcoholicHistory() {
	return alcoholicHistory == null ? false : alcoholicHistory;
	}

	public void setAlcoholicHistory(Boolean alcoholicHistory) {
	this.alcoholicHistory = alcoholicHistory;
	}

	public String getLifeStyle() {
	return lifeStyle;
	}

	public void setLifeStyle(String lifeStyle) {
	this.lifeStyle = lifeStyle;
	}

	public String getDailyActivities() {
	return dailyActivities;
	}

	public void setDailyActivities(String dailyActivities) {
	this.dailyActivities = dailyActivities;
	}

	public Boolean getAnyChildren() {
	return anyChildren == null ? false : anyChildren;
	}

	public void setAnyChildren(Boolean anyChildren) {
	this.anyChildren = anyChildren;
	}

	public Integer getNumberOfChildren() {
	return numberOfChildren;
	}

	public void setNumberOfChildren(Integer numberOfChildren) {
	this.numberOfChildren = numberOfChildren;
	}

	public Boolean getAnyPregnancy() {
	return anyPregnancy == null ? false : anyPregnancy;
	}

	public void setAnyPregnancy(Boolean anyPregnancy) {
	this.anyPregnancy = anyPregnancy;
	}

	public Integer getNumberOfPregnancy() {
	return numberOfPregnancy;
	}

	public void setNumberOfPregnancy(Integer numberOfPregnancy) {
	this.numberOfPregnancy = numberOfPregnancy;
	}

	public Boolean getAnyAbortions() {
	return anyAbortions == null ? false : anyAbortions;
	}

	public void setAnyAbortions(Boolean anyAbortions) {
	this.anyAbortions = anyAbortions;
	}

	public Integer getNumberOfAbortions() {
	return numberOfAbortions;
	}

	public void setNumberOfAbortions(Integer numberOfAbortions) {
	this.numberOfAbortions = numberOfAbortions;
	}

	public Boolean getAnyMiscarriage() {
	return anyMiscarriage == null ? false : anyMiscarriage;
	}

	public void setAnyMiscarriage(Boolean anyMiscarriage) {
	this.anyMiscarriage = anyMiscarriage;
	}

	public Boolean getLactating() {
	return lactating == null ? false : lactating;
	}

	public void setLactating(Boolean lactating) {
	this.lactating = lactating;
	}

	public String getOutsideTripDetails() {
	return outsideTripDetails;
	}

	public void setOutsideTripDetails(String outsideTripDetails) {
	this.outsideTripDetails = outsideTripDetails;
	}

	public String getComments() {
	return comments;
	}

	public void setComments(String comments) {
	this.comments = comments;
	}

	public Float getStoppedSmokingSince() {
	return stoppedSmokingSince;
	}

	public void setStoppedSmokingSince(Float stoppedSmokingSince) {
	this.stoppedSmokingSince = stoppedSmokingSince;
	}

	public Boolean getExercise() {
	return exercise == null ? false : exercise;
	}

	public void setExercise(Boolean exercise) {
	this.exercise = exercise;
	}

	public String getOtherExercise() {
	return otherExercise;
	}

	public void setOtherExercise(String otherExercise) {
	this.otherExercise = otherExercise;
	}

	public String getReasonForNotExercising() {
	return reasonForNotExercising;
	}

	public void setReasonForNotExercising(String reasonForNotExercising) {
	this.reasonForNotExercising = reasonForNotExercising;
	}

	public String getPhysicalActivityOne() {
	return physicalActivityOne;
	}

	public void setPhysicalActivityOne(String physicalActivityOne) {
	this.physicalActivityOne = physicalActivityOne;
	}

	public String getPhysicalActivityTwo() {
	return physicalActivityTwo;
	}

	public void setPhysicalActivityTwo(String physicalActivityTwo) {
	this.physicalActivityTwo = physicalActivityTwo;
	}

	public String getOtherPhysicalActivity() {
	return otherPhysicalActivity;
	}

	public void setOtherPhysicalActivity(String otherPhysicalActivity) {
	this.otherPhysicalActivity = otherPhysicalActivity;
	}

	public Integer getActivityOneDuration() {
	return activityOneDuration;
	}

	public void setActivityOneDuration(Integer activityOneDuration) {
	this.activityOneDuration = activityOneDuration;
	}

	public Integer getActivityTwoDuration() {
	return activityTwoDuration;
	}

	public void setActivityTwoDuration(Integer activityTwoDuration) {
	this.activityTwoDuration = activityTwoDuration;
	}

	public Integer getOtherActivityDuration() {
	return otherActivityDuration;
	}

	public void setOtherActivityDuration(Integer otherActivityDuration) {
	this.otherActivityDuration = otherActivityDuration;
	}

	public String getActivityOneFrequency() {
	return activityOneFrequency;
	}

	public void setActivityOneFrequency(String activityOneFrequency) {
	this.activityOneFrequency = activityOneFrequency;
	}

	public String getActivityTwoFrequency() {
	return activityTwoFrequency;
	}

	public void setActivityTwoFrequency(String activityTwoFrequency) {
	this.activityTwoFrequency = activityTwoFrequency;
	}

	public String getOtherActivityFrequency() {
	return otherActivityFrequency;
	}

	public void setOtherActivityFrequency(String otherActivityFrequency) {
	this.otherActivityFrequency = otherActivityFrequency;
	}

	@Column(length = 1024)
	public String getExerciseComments() {
	return exerciseComments;
	}

	public void setExerciseComments(String exerciseComments) {
	this.exerciseComments = exerciseComments;
	}

	@Column(length = 500)
	public String getHobbies() {
	return hobbies;
	}

	public void setHobbies(String hobbies) {
	this.hobbies = hobbies;
	}

	public Boolean getNormalWeight() {
	return normalWeight == null ? false : normalWeight;
	}

	public void setNormalWeight(Boolean normalWeight) {
	this.normalWeight = normalWeight;
	}

	public String getPatientDiet() {
	return patientDiet == null ? "" : patientDiet;
	}

	public void setPatientDiet(String patientDiet) {
	this.patientDiet = patientDiet;
	}

	public String getAbnormalWeightReason() {
	return abnormalWeightReason;
	}

	public void setAbnormalWeightReason(String abnormalWeightReason) {
	this.abnormalWeightReason = abnormalWeightReason;
	}

	@Column(length = 1024)
	public String getDietComments() {
	return dietComments;
	}

	public void setDietComments(String dietComments) {
	this.dietComments = dietComments;
	}

	public Boolean getDrugIntake() {
	return drugIntake == null ? false : drugIntake;
	}

	public void setDrugIntake(Boolean drugIntake) {
	this.drugIntake = drugIntake;
	}

	public String getOtherDrugs() {
	return otherDrugs;
	}

	public void setOtherDrugs(String otherDrugs) {
	this.otherDrugs = otherDrugs;
	}

	public Boolean getNeedleToInjectDrugs() {
	return needleToInjectDrugs == null ? false : needleToInjectDrugs;
	}

	public void setNeedleToInjectDrugs(Boolean needleToInjectDrugs) {
	this.needleToInjectDrugs = needleToInjectDrugs;
	}

	@Column(length = 1024)
	public String getDrugComments() {
	return drugComments;
	}

	public void setDrugComments(String drugComments) {
	this.drugComments = drugComments;
	}

	@Column(length = 1024)
	public String getSafetyIssueComments() {
	return safetyIssueComments;
	}

	public void setSafetyIssueComments(String safetyIssueComments) {
	this.safetyIssueComments = safetyIssueComments;
	}

	public Boolean getSeatBelts() {
	return seatBelts;
	}

	public void setSeatBelts(Boolean seatBelts) {
	this.seatBelts = seatBelts;
	}

	public Boolean getHelmets() {
	return helmets;
	}

	public void setHelmets(Boolean helmets) {
	this.helmets = helmets;
	}

	public Boolean getViolenceAtHome() {
	return violenceAtHome;
	}

	public void setViolenceAtHome(Boolean violenceAtHome) {
	this.violenceAtHome = violenceAtHome;
	}

	public Boolean getGunAtHome() {
	return gunAtHome; 
	}

	public void setGunAtHome(Boolean gunAtHome) {
	this.gunAtHome = gunAtHome;
	}

	public Boolean getFear() {
	return fear;
	}

	public void setFear(Boolean fear) {
	this.fear = fear;
	}

	public Boolean getBeingHit() {
	return beingHit;
	}

	public void setBeingHit(Boolean beingHit) {
	this.beingHit = beingHit;
	}

	public Boolean getForcedSexualActivity() {
	return forcedSexualActivity;
	}

	public void setForcedSexualActivity(Boolean forcedSexualActivity) {
	this.forcedSexualActivity = forcedSexualActivity;
	}

	public PatientSocialHistory createCopy() {
	PatientSocialHistory patientSocialHistory = new PatientSocialHistory();
	try {
		BeanUtils.copyProperties(patientSocialHistory, this);
		patientSocialHistory.setNoOfFullTermCSectDeliveries(noOfFullTermCSectDeliveries);
		patientSocialHistory.setNoOfFullTermVagDeliveries(noOfFullTermVagDeliveries);
		patientSocialHistory.setNoOfPreTermCSectDeliveries(noOfPreTermCSectDeliveries);
		patientSocialHistory.setNoOfPreTermVagDeliveries(noOfPreTermVagDeliveries);
		patientSocialHistory.setNoOfMensturalDays(noOfMensturalDays);
		patientSocialHistory.setNoOfMensturalWeeks(noOfMensturalWeeks);
		patientSocialHistory.setAgeAtFirstMensturalPeroid(ageAtFirstMensturalPeroid);
		patientSocialHistory.setCigrattesPerDay(cigrattesPerDay);
		patientSocialHistory.setDurationOfSmoking(durationOfSmoking);
		patientSocialHistory.setStoppedSmokingSince(stoppedSmokingSince);
		patientSocialHistory.setRegularMensturalCycle(regularMensturalCycle);
	} catch (IllegalAccessException e) {
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		e.printStackTrace();
	}
	patientSocialHistory.setId(null);
	return patientSocialHistory;
	}

	@Column(length = 1024)
	public String getSentence() {
	if (sentence == null) sentence = buildSentence();
	return sentence;
	}

	@Transient
	public String getSmokingAsSentence() {
	StringBuilder buffer = new StringBuilder();
	if (smokingStatus != null) {
		buffer.append("Patient smoking status : ").append(smokingStatus).append(".");
		if (!(SmokingStatusEnum.NEVER_SMOKER.equals(smokingStatus) || SmokingStatusEnum.UNKNOWN.equals(smokingStatus))) {
			buffer.append("Patient has been smoking ");
			if (cigrattesPerDay != null && cigrattesPerDay > 0.0F) buffer.append(cigrattesPerDay).append(" cigratte(s) per day. ");
			if (durationOfSmoking != null && durationOfSmoking > 0.0F) {
				buffer.append(" since last ").append(durationOfSmoking);
				buffer.append(" years(s).");
			}
			if (stoppedSmokingSince != null && stoppedSmokingSince > 0.0F) {
				buffer.append(" Patient has stopped smoking since ").append(stoppedSmokingSince).append(
						SentenceUtil.SPACE).append(smokingTimeUnit);
			}
		}
		if (!(cigrattesPerDay!=null && cigrattesPerDay > 0.0F) && !(cigrattesPerDay!=null && durationOfSmoking > 0.0F) && !(stoppedSmokingSince!=null && stoppedSmokingSince > 0.0F)) {
			if (SmokingStatusEnum.CURRENT_EVERY_DAY_SMOKER.equals(smokingStatus))
				buffer.append("Patient smokes every day");
			if (SmokingStatusEnum.CURRENT_SOME_DAY_SMOKER.equals(smokingStatus))
				buffer.append("Patient is a some day smoker");
			if (SmokingStatusEnum.FORMER_SMOKER.equals(smokingStatus)) buffer.append("Patient was smoking previously");
			if (SmokingStatusEnum.CURRENT_STATUS_UNKNOWN.equals(smokingStatus))
				buffer.append("Patient was smoking but current status is unknown");
		}
		if (SmokingStatusEnum.NEVER_SMOKER.equals(smokingStatus)) buffer.append("Patient never smokes");
		if (SmokingStatusEnum.UNKNOWN.equals(smokingStatus)) buffer.append("Smoking status of patient is unknown");
		buffer.append(".");
	}
	return buffer.toString();
	}

	private String buildSentence() {
	StringBuilder buffer = new StringBuilder("");
	if (occupation != null) buffer.append(" Patient's occupation is " + occupation).append(".");
	if (maritalStatus != null) buffer.append(" Marital status is -  " + maritalStatus).append(".");
	if(UtilValidator.isNotEmpty(hobbies))buffer.append("Hobbies :" + hobbies).append(".");
	if(exercise){
		if(UtilValidator.isNotEmpty(physicalActivityOne))
			buffer.append("Patient performs " + physicalActivityOne);
		if(UtilValidator.isNotEmpty(activityOneDuration))
			buffer.append(" for duration of " + activityOneDuration +"(mins).");
		if(UtilValidator.isNotEmpty(activityOneFrequency))
			buffer.append(" "+ activityOneFrequency).append(".");
		if(UtilValidator.isNotEmpty(physicalActivityTwo))
			buffer.append("Patient performs " + physicalActivityTwo);
		if(UtilValidator.isNotEmpty(activityTwoDuration))
			buffer.append(" for duration of " + activityTwoDuration +"(mins).");
		if(UtilValidator.isNotEmpty(activityTwoFrequency))
			buffer.append(" "+activityTwoFrequency).append(".");
		if(UtilValidator.isNotEmpty(otherPhysicalActivity))
			buffer.append("Patient performs " + otherPhysicalActivity);
		if(UtilValidator.isNotEmpty(otherActivityDuration))
			buffer.append(" for duration of " + otherActivityDuration +"(mins).");
		if(UtilValidator.isNotEmpty(otherActivityFrequency))
			buffer.append(" " + otherActivityFrequency).append(".");
	}
		if(UtilValidator.isNotEmpty(exerciseComments))
			buffer.append("Exercise comments :" + exerciseComments).append(".");
		buffer.append(getSmokingAsSentence());
		
		if(normalWeight)
			buffer.append("Patient's  weight is satisfactory.");
		else{
			buffer.append("Patient weight is not satisfactory. ");
			if(UtilValidator.isNotEmpty(abnormalWeightReason))
				buffer.append("due to " + abnormalWeightReason).append(".");
		}
		
	if(UtilValidator.isNotEmpty(dietComments))
		buffer.append("Diet Comments :" + dietComments).append(".");

	if(drugIntake)
		buffer.append("Patient has taken drugs.");
	if(needleToInjectDrugs)
		buffer.append("Patient has used needle to inject drugs.");
	if(UtilValidator.isNotEmpty(drugComments))
		buffer.append("Drug comments :" + drugComments).append(".");
	if(UtilValidator.isNotEmpty(patientDiet))
		buffer.append("Patient Diet " + patientDiet).append(".");
	
	if(helmets!=null)
		buffer.append(helmets ? "Patient uses bike helmet." : "Patient does not use bike helmet.");
	if(seatBelts != null)
		buffer.append(seatBelts ? "Patient uses seat belts consistently." : "Patient does not use seat belts consistently.");
	if(violenceAtHome != null)
		buffer.append(violenceAtHome ? "Patient is concerned about violence at home." : "Patient is not concerned about violence at home.");
	if(gunAtHome != null)
		buffer.append(gunAtHome ? "Patient has gun at home." : "Patient doesnt have gun at home");
	if(fear != null)
		buffer.append(fear ? "Patient is afraid of some close person. " : "Patient is not afraid of some close person.");
	if(beingHit != null)
		buffer.append(beingHit ? "Patient has been hit,slapped,kicked,pushed,shoved or otherwise physcially hurt by patient's partner or someone close."
				: "Patient has not been hit,slapped,kicked,pushed,shoved or otherwise physcially hurt by patient's partner or someone close.");
	if(forcedSexualActivity != null)
		buffer.append(forcedSexualActivity ? "Patient was forced by someone to have sexual activities." : "Patient was not forced by anyone to have sexual activities.");
	if(UtilValidator.isNotEmpty(safetyIssueComments))
		buffer.append("Safety comments :" + safetyIssueComments).append(".");
	
	if (alcohol) {
		buffer.append(" Patient has been taking ");
		if (StringUtils.isNotBlank(amountOfAlcohol)) 
			buffer.append(amountOfAlcohol).append(" of alcohol.");
		if (StringUtils.isNotBlank(amountOfAlcoholPerWeek)) 
			buffer.append(" On an average Patient's intake of alcohol is around ").append(amountOfAlcoholPerWeek)
					.append(" per week");
		buffer.append(".");
	}
	if (alcoholicHistory && stoppedAlcoholSince != null) {
		buffer.append(" Patient has stopped taking alcohol since ").append(stoppedAlcoholSince).append(
				SentenceUtil.SPACE).append(alcoholTimeUnit);
		buffer.append(".");
	}

	if (StringUtils.isNotBlank(lifeStyle)) {
		if ("N/A".equalsIgnoreCase(lifeStyle))
			buffer.append(" Patient has no sexual orientation").append(".");
		else
			buffer.append(" Patient has sexual orientation of ").append(lifeStyle).append(" .");
	}

	if (StringUtils.isNotBlank(dailyActivities)) {
		buffer.append(" These are few of the daily activities performed by Patient mentioned below - ").append(
				dailyActivities).append(".");
	}
	if (anyChildren) {
		if (numberOfChildren == 1.0F)
			buffer.append(" The patient has only 1 child.");
		else
			buffer.append(" The patient has " + numberOfChildren + " children.");
	}

	if (anyPregnancy) {
		if (numberOfPregnancy == 1.0F)
			buffer.append(" The patient had only 1 pregnancy in the past.");
		else
			buffer.append(" The patient had " + numberOfPregnancy + " pregnancies in the past.");
	}

	if (anyMiscarriage) {
		if (numberOfMiscarriage == 1.0f)
			buffer.append(" The patient had only 1 miscarriage in the past.");
		else
			buffer.append(" The patient had " + numberOfMiscarriage + " miscarriages in the past.");
	}

	if (anyAbortions) {
		if (numberOfAbortions == 1.0f)
			buffer.append(" The patient had 1 abortion in the past.");
		else
			buffer.append(" The patient had " + numberOfAbortions + " abortions in the past.");
	}

	if (lactating) {
		buffer.append(" Currently the patient is lactating.");
	}

	if (StringUtils.isNotBlank(outsideTripDetails)) {
		buffer.append(" Patient has recently travel outside the country. The details are as follows, ").append(
				outsideTripDetails).append(".");
	}

	if ("Female".equalsIgnoreCase(soapSection.getSoapNote().getPatient().getGender().getDescription())) {
		if (ageAtFirstMensturalPeroid != null && ageAtFirstMensturalPeroid != 0)
			buffer.append(" Patient's age at first menstural period was " + ageAtFirstMensturalPeroid + " years. ");

		if (lastMensturalPeriod != null) {
			buffer.append(" Patient's last menstural period was on " + UtilDateTime.format(lastMensturalPeriod))
					.append(".");
		}

		if (noOfMensturalDays != null)
			buffer.append(" Patient's menstural cycle will last upto " + noOfMensturalDays + " day(s).");

		if (regularMensturalCycle != null) {
			if (regularMensturalCycle) {
				buffer.append("Patient's menstural cycle is regular,");
				if (noOfMensturalWeeks != null)
					buffer.append(" Patients's menstural cycle will last upto " + noOfMensturalWeeks + " week(s),");
				if (StringUtils.isNotEmpty(amountOfMensturation))
					buffer.append(" with " + amountOfMensturation + " amount.");
			} else
				buffer.append("Patient's menstural cycle is irregular.");
		}
		
		if (isMenoPause) buffer.append(" Patient has reached Menopause stage.");

		if (noOfPreTermVagDeliveries != null) {
			if (noOfPreTermVagDeliveries == 1)
				buffer.append(" Patient had 1 preterm vaginal delivery in the past.");
			else
				buffer.append(" Patient had " + noOfPreTermVagDeliveries + " preterm vaginal deliveries in the past.");
		}

		if (noOfFullTermVagDeliveries != null) {
			if (noOfFullTermVagDeliveries == 1)
				buffer.append(" Patient had 1 full term vaginal delivery in the past.");
			else
				buffer.append(" Patient had " + noOfFullTermVagDeliveries
						+ " full term vaginal deliveries in the past.");
		}

		if (noOfPreTermCSectDeliveries != null) {
			if (noOfPreTermCSectDeliveries == 1)
				buffer.append(" Patient had 1 preterm CREDIT-Section delivery in the past.");
			else
				buffer.append(" Patient had " + noOfPreTermCSectDeliveries
						+ " preterm CREDIT-Section deliveries in the past.");
		}

		if (noOfFullTermCSectDeliveries != null) {
			if (noOfFullTermCSectDeliveries == 1)
				buffer.append(" Patient had 1 full term CREDIT-Section delivery in the past.");
			else
				buffer.append(" Patient had " + noOfFullTermCSectDeliveries
						+ " full term CREDIT-Section deliveries in the past.");
		}
	}

	if (StringUtils.isNotEmpty(birthControlMethod)) {
		if ("None".equalsIgnoreCase(birthControlMethod))
			buffer.append(" Patient has not undergone any birth control method.");
		else {
			if ("Other".equalsIgnoreCase(birthControlMethod) && StringUtils.isNotEmpty(otherMethodOfBirthControl))
				buffer.append(" Patient's other birth control method is " + otherMethodOfBirthControl).append(".");
			else
				buffer.append(" Patient's method of birth control is " + birthControlMethod + ".");
		}
	}

	if (StringUtils.isNotEmpty(ageOfFirstSexualInterCourse))
		buffer
				.append(" Patient had first sexual intercourse when the patient was " + ageOfFirstSexualInterCourse
						+ ".");

	if (StringUtils.isNotEmpty(noOfSexualPartners))
		buffer.append(" Patient had " + noOfSexualPartners + " sexual partners in the past.");

	if (strictReligiousEnvironment) buffer.append(" Patient grew up in a strict religious environment.");

	if (religiousBeliefs) {
		buffer.append(" Patient had religious beliefs and has changed since childhood,adolesence or adulthood.");
	}

	if (StringUtils.isNotEmpty(effectOfPatientBelief))
		buffer.append(
				"Effects of patient's belief on treatment of psychiatric illnesses or suicides are as follows "
						+ effectOfPatientBelief).append(".");

	if (StringUtils.isNotEmpty(sexualActivity)) {
		if ("Never".equalsIgnoreCase(sexualActivity))
			buffer.append(" Patient never had any sexual activities in the past.");
		else
			if ("Both".equalsIgnoreCase(sexualActivity))
				buffer.append(" Patient had sexual activities with both males and females in the past.");
			else
				buffer.append(" Patient had sexual activities with " + sexualActivity + " in the past.");
	}

	if (StringUtils.isNotEmpty(livingArrangement)) {
		if ("Alone".equalsIgnoreCase(livingArrangement)) buffer.append(" Patient is living alone.");
		if ("Others".equalsIgnoreCase(livingArrangement) && StringUtils.isNotEmpty(otherLivingArrangement))
			buffer.append(" Patient's other living arrangement is " + otherLivingArrangement).append(".");
		if (!("Alone".equalsIgnoreCase(livingArrangement)) && !("Others".equalsIgnoreCase(livingArrangement)))
			buffer.append(" Patient is living with " + livingArrangement).append(".");
	}

	if (StringUtils.isNotEmpty(childHood)) {
		if ("DontRecall".equalsIgnoreCase(childHood))
			buffer.append(" Patient Cannot recall childhood.");
		else
			buffer.append(" Patient's childhood was " + childHood).append(".");
	}

	if (StringUtils.isNotEmpty(gynecologicalProblems))
		buffer.append(" Patient had or is having following gynecological problems  " + gynecologicalProblems);

	if (StringUtils.isNotEmpty(stresses))
		buffer.append(" Patient is stressed due to the following reasons " + stresses);

	if (StringUtils.isNotEmpty(otherStress)) buffer.append(" Patients other stress is " + otherStress + ".");

	if (lastPapSmear != null)
		buffer.append(" Patient underwent  pap smear last on " + UtilDateTime.format(lastPapSmear)).append(".");

	if (lastMammogram != null)
		buffer.append(" Patient underwent mammogram last on " + UtilDateTime.format(lastMammogram)).append(".");

	return buffer.toString();
	}

	public void setSentence(String sentence) {
	this.sentence = sentence;
	}

	public String getSmokingTimeUnit() {
	return smokingTimeUnit;
	}

	public void setSmokingTimeUnit(String smokingTimeUnit) {
	this.smokingTimeUnit = smokingTimeUnit;
	}

	public String getAlcoholTimeUnit() {
	return alcoholTimeUnit;
	}

	public void setAlcoholTimeUnit(String alcoholTimeUnit) {
	this.alcoholTimeUnit = alcoholTimeUnit;
	}

	public SmokingStatusEnum getSmokingStatus() {
	return smokingStatus;
	}

	public void setSmokingStatus(SmokingStatusEnum smokingStatus) {
	this.smokingStatus = smokingStatus;
	if (smokingStatus != null
			&& (SmokingStatusEnum.NEVER_SMOKER.equals(smokingStatus) || SmokingStatusEnum.UNKNOWN.equals(smokingStatus))) {
		setCigrattesPerDay(0);
		setDurationOfSmoking(0f);
		setStoppedSmokingSince(0f);
	}
	}

	public void addOrRemoveGynecologicalProblem(String problem, boolean add) {
	if (add)
		gynecologicalProblems = gynecologicalProblems + problem + Constants.COMMA;
	else
		gynecologicalProblems = gynecologicalProblems.replace(problem + Constants.COMMA, "");
	}

	public void addOrRemoveStress(String patientStress, boolean add) {
	if (add)
		stresses = stresses + patientStress + Constants.COMMA;
	else
		stresses = stresses.replace(patientStress + Constants.COMMA, "");
	}

	public void addOrRemoveDiet(String patientDiet, boolean add) {
	if (add)
		this.patientDiet = this.patientDiet + patientDiet + Constants.COMMA;
	else
		this.patientDiet = this.patientDiet.replace(patientDiet + Constants.COMMA, "");
	}
	
	public boolean isTerminallyIll() {
	return terminallyIll;
	}

	public void setTerminallyIll(boolean terminallyIll) {
	this.terminallyIll = terminallyIll;
	}
}