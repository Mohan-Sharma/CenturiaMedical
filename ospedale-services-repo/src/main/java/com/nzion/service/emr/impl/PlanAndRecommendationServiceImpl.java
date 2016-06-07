package com.nzion.service.emr.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import com.nzion.domain.Patient;
import com.nzion.domain.docmgmt.PatientEducation;
import com.nzion.domain.docmgmt.PatientEducationDocument;
import com.nzion.enums.MATERIALCATEGORY;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.repository.emr.PlanAndRecommendationRepository;
import com.nzion.service.emr.PlanAndRecommendationService;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;


/**
 * @author Sandeep Prusty
 * May 24, 2011
 */

@Service("planAndRecommendationService")
public class PlanAndRecommendationServiceImpl implements PlanAndRecommendationService {

	private CommonCrudRepository commonCrudRepository;
	
	private PlanAndRecommendationRepository planAndRecommendationRepository;
	
	@Resource(name="planAndRecommendationRepository")
	@Required
	public void setPlanAndRecommendationRepository(PlanAndRecommendationRepository planAndRecommendationRepository) {
	this.planAndRecommendationRepository = planAndRecommendationRepository;
	}

	@Resource(name="commonCrudRepository")
	@Required
	public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
	this.commonCrudRepository = commonCrudRepository;
	}
	
	private List<PatientEducationDocument> extractDocs(List<PatientEducationDocument> educationDocuments,Patient patient,Date encounteredDate){
	if(UtilValidator.isEmpty(educationDocuments))
		return Collections.emptyList();
	List<PatientEducationDocument> docs = new ArrayList<PatientEducationDocument>(5);
	int age = UtilDateTime.getIntervalInYears(patient.getDateOfBirth(),encounteredDate);
	for(PatientEducationDocument doc : educationDocuments){
		PatientEducation education = doc.getPatientEducation();
		if((education.getFromRange() == 0 || age >= education.getFromRange()) &&
				(education.getToRange() == 0 || age <= education.getToRange())	&&
				(("Both".equalsIgnoreCase(education.getGender().getDescription()) )|| patient.getGender().getDescription().equals(education.getGender().getDescription())))
			docs.add(doc);
	}
	return docs;
	}

	@Override
	public List<PatientEducationDocument> getPatientEducationDocumentsFor(MATERIALCATEGORY materialcategory, String languageCode,Patient patient,Date encounteredDate) {
	List<PatientEducationDocument> educationalDocs = planAndRecommendationRepository.getPatientEducationDocuments(materialcategory);
	List<PatientEducationDocument> extractedDocs = extractDocs(educationalDocs,patient,encounteredDate);
	if(extractedDocs == null || UtilValidator.isEmpty(languageCode)) 
		return extractedDocs;
	List<PatientEducationDocument> result = new ArrayList<PatientEducationDocument>();
	for(PatientEducationDocument document : extractedDocs){
		if(document.getLanguage() != null && document.getLanguage().getEnumCode().equals(languageCode))
			result.add(document);
	}
	return result;
	}
	
	//check this method
	public List<PatientEducationDocument> getPatientEducationDocumentsFor(MATERIALCATEGORY materialcategory, String code, String desc, String pkOfItem,Date encounteredDate,Patient patient) {
	List<PatientEducationDocument> patientEducationDocuments = planAndRecommendationRepository.getPatientEducationDocuments(materialcategory, code, desc, pkOfItem);
	return extractDocs(patientEducationDocuments,patient,encounteredDate);
	}
	
	public String getBMIStatusFor(String BMI,Date dob,Date encounteredDate){
	int age = UtilDateTime.getIntervalInYears(dob,encounteredDate);
	float bmi = Float.parseFloat(BMI==null?"0":BMI);
	if(age < 18)
		return "Not Applicable";
	if(age>= 18 && age<=64 && bmi >= 18.5 && bmi <=25)
		return "Normal";
	if(age > 65 && bmi >=22 && bmi <=30)
		return "Normal";
	else
		return "Abnormal";
	}
}