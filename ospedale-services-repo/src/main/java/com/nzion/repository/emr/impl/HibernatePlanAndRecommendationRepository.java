package com.nzion.repository.emr.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.nzion.domain.docmgmt.PatientEducation;
import com.nzion.domain.docmgmt.PatientEducationDocument;
import com.nzion.enums.MATERIALCATEGORY;
import com.nzion.repository.emr.PlanAndRecommendationRepository;
import com.nzion.repository.impl.HibernateBaseRepository;

/**
 * @author Sandeep Prusty
 * May 26, 2011
 */
public class HibernatePlanAndRecommendationRepository
		extends HibernateBaseRepository implements PlanAndRecommendationRepository {

	public List<PatientEducation> getPatientEducationsFor(MATERIALCATEGORY materialCategory, String code, String desc,
			String pkOfItem) {
	Criteria criteria = getSession().createCriteria(PatientEducation.class);
	criteria.add(Restrictions.eq("materialCategory", materialCategory));
	Criteria attachedItemCriteria = criteria.createCriteria("attachedItems");
	if (code != null) attachedItemCriteria.add(Restrictions.ilike("code", code));
	if (desc != null) attachedItemCriteria.add(Restrictions.ilike("description", desc));
	return criteria.setCacheable(true).list();
	}

	@Override
	public List<PatientEducationDocument> getPatientEducationDocuments(MATERIALCATEGORY materialcategory){
	Criteria criteria = getSession().createCriteria(PatientEducationDocument.class);
	criteria.createCriteria("patientEducation").add(Restrictions.eq("materialCategory", materialcategory));
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	return criteria.setCacheable(true).setCacheable(true).list();
	}
	
	public List<PatientEducationDocument> getPatientEducationDocuments(MATERIALCATEGORY materialcategory,String code,String desc,String pkOfItem){
	Criteria criteria = getSession().createCriteria(PatientEducationDocument.class);
	Criteria patientEducationCriteria = criteria.createCriteria("patientEducation");
	patientEducationCriteria.add(Restrictions.eq("materialCategory", materialcategory));
	Criteria attachedItemCriteria = patientEducationCriteria.createCriteria("attachedItems");
	if(code!=null) attachedItemCriteria.add(Restrictions.ilike("code", code));
	if (desc != null) attachedItemCriteria.add(Restrictions.ilike("description", desc));
	if(pkOfItem != null) attachedItemCriteria.add(Restrictions.ilike("attachedItemId", pkOfItem));
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	return criteria.setCacheable(true).list();
	}
}
