package com.nzion.domain.emr.soap;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * May 20, 2011
 */

@Entity
@DiscriminatorValue("RECOMMENDATION")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class RecommendationSection	extends SoapSection {

    private static final long serialVersionUID = 1L;

    private Set<SOAPPlan> plans;

    private Boolean fileDownloaded = Boolean.FALSE;

    private FollowUp followUp;

    private String recommendedNotes;

    private Integer followupDays;

    private Date followupDate;

    @Embedded
    public FollowUp getFollowUp() {
        return followUp;
    }

    public void setFollowUp(FollowUp followUp) {
        this.followUp = followUp;
    }

    @OneToMany(mappedBy = "recommendationSection")
    @Cascade(CascadeType.ALL)
    public Set<SOAPPlan> getPlans() {
        return plans;
    }

    public void setPlans(Set<SOAPPlan> plans) {
        this.plans = plans;
    }

    public SOAPPlan retrieveSoapPlan(String followFor) {
        if (plans == null) plans = new HashSet<SOAPPlan>();
        for (SOAPPlan plan : plans)
            if (followFor.equalsIgnoreCase(plan.getFollowUpFor())) return plan;
        return null;
    }

    public SOAPPlan retrieveSoapPlanAnyWay(String followFor) {
        SOAPPlan plan = retrieveSoapPlan(followFor);
        if (plan != null) return plan;
        plan = new SOAPPlan();
        FollowUp followUp = new FollowUp();
        plan.setFollowUp(followUp);
        plan.setPatient(super.getSoapNote().getPatient());
        plan.setProvider(super.getSoapNote().getProvider());
        plan.setFollowUpFor(followFor);
        plan.setRecommendationSection(this);
        plans.add(plan);
        return plan;
    }

    public Boolean getFileDownloaded() {
        return fileDownloaded;
    }

    public void setFileDownloaded(Boolean fileDownloaded) {
        this.fileDownloaded = fileDownloaded;
    }

    @Override
    public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
                     Map<String, SoapSection> previousSoapSections) {
    }

    public static final String BMI_PLAN = "BMI management plan";

    public static final String TOBACCO_PLAN = "Tobacco Cessation";

    public static final String PATIENT_EDUCATION_PLAN = "PatientEducation";

    @Override
    public boolean edited() {
	/*if(UtilValidator.isEmpty(plans))
		return false;
	for(SOAPPlan plan : plans)
	if(!UtilValidator.isAllEmpty(plan, "doctorsPlan", "referalLetterTemplate","referral","exclusionReason","note","followUp.followUpDuration","followUp.followUpDurationType","followUp.expectedDate","followUp.alertBefore","followUp.alertDurationType","downLoadedDocuments") || plan.getMedicationGiven())
		return true;*/
        if(followupDate != null || followupDays != null || recommendedNotes != null)
            return true;
        return false;
    }

    public String getRecommendedNotes() {
        return recommendedNotes;
    }

    public void setRecommendedNotes(String recommendedNotes) {
        this.recommendedNotes = recommendedNotes;
    }

    public static final String MODULE_NAME = "Recommendation";

    @Column(name = "FOLLOWUP_DAYS", nullable = true)
    public Integer getFollowupDays() {
        return followupDays;
    }

    public void setFollowupDays(Integer followupDays) {
        this.followupDays = followupDays;
    }

    @Column(name = "FOLLOWUP_DATE")
    @Temporal(TemporalType.DATE)
    public Date getFollowupDate() {
        return followupDate;
    }

    public void setFollowupDate(Date followupDate) {
        this.followupDate = followupDate;
    }
}