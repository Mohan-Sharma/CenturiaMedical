package com.nzion.domain.emr;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.util.UtilDateTime;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "SHORT_DESCRIPTION"}) })
@Filters( {
		@Filter(name = "EnabledFilter", condition = "(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
// All ages are considered to be entered in weeks.
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class Immunization extends IdGeneratingBaseEntity {

	private Integer recommendedMinAge = 0;

	private Integer recommendedMaxAge = 0;

	private Integer minIntervalToNextDose = 0;

	private Integer recommendedIntervalToNextDose = 0;

	private Integer maxIntervalToNextDose = 0;

	private String informationUrl;

	private String shortDescription;

	private String fullVaccineName;

	private String summary;

	private String cvxCode;

	private String recommendedMinUnit = "Week";

	private String recommendedMaxUnit = "Week";

	private String minIntervalUnit = "Week";

	private String maxIntervalUnit = "Week";

	private String recommendedIntervalUnit = "Week";

	public String getRecommendedMinUnit() {
	return recommendedMinUnit;
	}

	public void setRecommendedMinUnit(String recommendedMinUnit) {
	this.recommendedMinUnit = recommendedMinUnit;
	}

	public String getRecommendedMaxUnit() {
	return recommendedMaxUnit;
	}

	public void setRecommendedMaxUnit(String recommendedMaxUnit) {
	this.recommendedMaxUnit = recommendedMaxUnit;
	}

	public String getMinIntervalUnit() {
	return minIntervalUnit;
	}

	public void setMinIntervalUnit(String minIntervalUnit) {
	this.minIntervalUnit = minIntervalUnit;
	}

	public String getMaxIntervalUnit() {
	return maxIntervalUnit;
	}

	public void setMaxIntervalUnit(String maxIntervalUnit) {
	this.maxIntervalUnit = maxIntervalUnit;
	}

	public String getRecommendedIntervalUnit() {
	return recommendedIntervalUnit;
	}

	public void setRecommendedIntervalUnit(String recommendedIntervalUnit) {
	this.recommendedIntervalUnit = recommendedIntervalUnit;
	}

	public String getCvxCode() {
	return cvxCode;
	}

	public void setCvxCode(String cvxCode) {
	this.cvxCode = cvxCode;
	}

	public Integer getRecommendedMinAge() {
	return recommendedMinAge;
	}

	public void setRecommendedMinAge(Integer recommendedMinAge) {
	this.recommendedMinAge = recommendedMinAge;
	}

	public Integer getRecommendedMaxAge() {
	return recommendedMaxAge;
	}

	public void setRecommendedMaxAge(Integer recommendedMaxAge) {
	this.recommendedMaxAge = recommendedMaxAge;
	}

	public Integer getMinIntervalToNextDose() {
	return minIntervalToNextDose;
	}

	public void setMinIntervalToNextDose(Integer minIntervalToNextDose) {
	this.minIntervalToNextDose = minIntervalToNextDose;
	}

	public Integer getRecommendedIntervalToNextDose() {
	return recommendedIntervalToNextDose;
	}

	public void setRecommendedIntervalToNextDose(Integer recommendedIntervalToNextDose) {
	this.recommendedIntervalToNextDose = recommendedIntervalToNextDose;
	}

	public String getInformationUrl() {
	return informationUrl;
	}

	public void setInformationUrl(String informationUrl) {
	this.informationUrl = informationUrl;
	}

	@Column(name = "SHORT_DESCRIPTION")
	public String getShortDescription() {
	return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
	this.shortDescription = shortDescription;
	}

	@Column(name = "FULL_VACCINE_NAME")
	public String getFullVaccineName() {
	return fullVaccineName;
	}

	public void setFullVaccineName(String fullVaccineName) {
	this.fullVaccineName = fullVaccineName;
	}

	public String getSummary() {
	return summary;
	}

	public void setSummary(String summary) {
	this.summary = summary;
	}

	public Integer getMaxIntervalToNextDose() {
	return maxIntervalToNextDose;
	}

	public void setMaxIntervalToNextDose(Integer maxIntervalToNextDose) {
	this.maxIntervalToNextDose = maxIntervalToNextDose;
	}

	@Transient
	public boolean checkApplicability(Date dobOfPatient) {
	int ageInWeeks = UtilDateTime.getIntervalInWeeks(dobOfPatient, new Date());
	if (recommendedMinAge == null) return false;
	if (recommendedMinUnit != null && "Year".equalsIgnoreCase(recommendedMinUnit))
		recommendedMinAge = recommendedMinAge * 52;
	if (recommendedMinUnit != null && "Month".equalsIgnoreCase(recommendedMinUnit))
		recommendedMinAge = recommendedMinAge * 4;
	expectedDate = UtilDateTime.addDaysToDate(new Date(),(recommendedMinAge-ageInWeeks)*7);
	return recommendedMinAge < ageInWeeks + 16;
	}
	
	private Date expectedDate;
	
	
	@Transient
	public Date getExpectedDate() {
	return expectedDate;
	}

	public void setExpectedDate(Date expectedDate) {
	this.expectedDate = expectedDate;
	}

	private static final long serialVersionUID = 1L;
}
