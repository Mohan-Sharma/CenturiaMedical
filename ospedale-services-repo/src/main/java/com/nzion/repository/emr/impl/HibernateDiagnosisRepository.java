package com.nzion.repository.emr.impl;

import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.IcdElement;
import com.nzion.repository.emr.DiagnosisRepository;
import com.nzion.repository.impl.HibernateBaseRepository;
import com.nzion.util.UtilValidator;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sandeep Prusty
 * Jul 30, 2010
 */

@SuppressWarnings("unchecked")
public class HibernateDiagnosisRepository extends HibernateBaseRepository implements DiagnosisRepository {

	public List<IcdElement> getChildren(IcdElement icdElement) {
	return findByCriteria(IcdElement.class, new String[] { "parent" }, new Object[] { icdElement });
	}

	public IcdElement getRootIcdElement() {
	Criteria criteria = getSession().createCriteria(IcdElement.class);
	criteria.add(Restrictions.isNull("parent")).setCacheable(true);
	return (IcdElement) criteria.uniqueResult();
	}

	public List<IcdElement> searchIcd(String caption) {
	Criteria criteria = getSession().createCriteria(IcdElement.class);
	criteria.add(Restrictions.or(Restrictions.like("description", caption, MatchMode.ANYWHERE), Restrictions.like(
			"code", caption, MatchMode.ANYWHERE)));
	return criteria.setCacheable(true).list();
	}

	public List<IcdElement> searchIcd(IcdElement icdElement) {
	return simulateExampleSearch(new String[] { "code", "description", "ccsLabel", "ccsCategory" }, icdElement);
	}

	public List<IcdElement> getAllIcdDiseaseCodes() {
	return findByCriteria(IcdElement.class, new String[] { "type" }, new Object[] { IcdElement.Type.DISEASECODE });
	}

	public List<Cpt> getAllCpts() {
	return getAll(Cpt.class);
	}

	public IcdElement getIcdByCode(String icdCode) {
	Criteria criteria = getSession().createCriteria(IcdElement.class);
	criteria.add(Restrictions.eq("code", icdCode));
	List<IcdElement> icds = criteria.setCacheable(true).list();
	return findUniqueByCriteria(IcdElement.class, new String[] { "code" }, new Object[] { icdCode });
	}

	@Override
	public List<Cpt> searchCpts(String searchText) {
        if(UtilValidator.isEmpty(searchText))
            return new ArrayList<Cpt>();
        Criteria criteria = getSession().createCriteria(Cpt.class);
        criteria.add(Restrictions.or(Restrictions.like("description", searchText, MatchMode.ANYWHERE), Restrictions.like(
                "id", searchText, MatchMode.ANYWHERE)));
        return criteria.list();
	}

	@Override
	public Cpt getCptByCode(String cptCode) {
	return findUniqueByCriteria(Cpt.class, new String[] { "id" }, new Object[] { cptCode });
	}

	@Override
	public List<IcdElement> searchIcdBy(String code, String description) {
	Criteria criteria = getSession().createCriteria(IcdElement.class);
	if (code != null) criteria.add(Restrictions.like("code", code, MatchMode.START));
	if (description != null) criteria.add(Restrictions.like("description", description, MatchMode.START));
	criteria.addOrder(Order.asc("code"));
	return criteria.setCacheable(true).list();
	}

	@Override
	public List<Cpt> searchCptBy(String code, String description) {
	Criteria criteria = getSession().createCriteria(Cpt.class);
	if (UtilValidator.isNotEmpty(code) &&UtilValidator.isNotEmpty(description)){ 
		criteria.add(Restrictions.eq("id", code));
		criteria.add(Restrictions.disjunction().add(Restrictions.like("description", description, MatchMode.START)).add(Restrictions.like("shortDescription", description, MatchMode.START)).add(Restrictions.like("longDescription", description, MatchMode.START)));
	}
	if(UtilValidator.isNotEmpty(code) && UtilValidator.isEmpty(description))
		criteria.add(Restrictions.eq("id", code));
	if(UtilValidator.isEmpty(code) && UtilValidator.isNotEmpty(description))
		criteria.add(Restrictions.disjunction().add(Restrictions.like("description", description, MatchMode.START)).add(Restrictions.like("shortDescription", description, MatchMode.START)).add(Restrictions.like("longDescription", description, MatchMode.START)));
	return criteria.setCacheable(true).list();
	}

}