package com.nzion.repository.emr;

import java.util.List;

import com.nzion.domain.docmgmt.PatientEducation;
import com.nzion.domain.docmgmt.PatientEducationDocument;
import com.nzion.enums.MATERIALCATEGORY;

/**
 * @author Sandeep Prusty
 * May 26, 2011
 */
public interface PlanAndRecommendationRepository {

	List<PatientEducation> getPatientEducationsFor(MATERIALCATEGORY materialcategory, String code, String desc, String pkOfItem);
	
	List<PatientEducationDocument> getPatientEducationDocuments(MATERIALCATEGORY materialcategory);
	
	List<PatientEducationDocument> getPatientEducationDocuments(MATERIALCATEGORY materialcategory,String code,String desc,String pkOfItem);
}