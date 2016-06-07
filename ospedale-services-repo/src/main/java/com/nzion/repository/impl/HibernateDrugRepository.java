/**
 * @author shwetha
 * Oct 15, 2010 
 */
package com.nzion.repository.impl;

import com.nzion.domain.drug.Drug;
import com.nzion.repository.DrugRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class HibernateDrugRepository extends HibernateBaseRepository implements DrugRepository {

	public List<Drug> searchDrugs(String fieldName, String searchString) {
	Criteria criteria = getSession().createCriteria(Drug.class);
	String[] tokens = fieldName.split("\\.");
	for (int i = 0; i < tokens.length - 1; ++i) {
		criteria = criteria.createCriteria(tokens[i]);
	}
	String fieldToCheck = tokens.length == 0 ? fieldName : tokens[tokens.length - 1];
	criteria.add(Restrictions.like(fieldToCheck, searchString, MatchMode.ANYWHERE));
	return criteria.list();
	}

	@Override
	public List<Drug> getDrugsByTradeName(String tradeName) {
	Criteria criteria = getSession().createCriteria(Drug.class);
	criteria.add(Restrictions.eq("tradeName", tradeName));
	return criteria.list();
	}

	@Override
	public List<Drug> searchDrugs(String searchString) {
	if (StringUtils.isEmpty(searchString)) return new ArrayList();
	Criteria criteria = getSession().createCriteria(Drug.class);
	Disjunction disjunction = Restrictions.disjunction();
	disjunction.add(Restrictions.like("tradeName", searchString, MatchMode.START));
	disjunction.add(Restrictions.like("genericName", searchString, MatchMode.START));
	disjunction.add(Restrictions.like("brandName", searchString, MatchMode.START));
	criteria.add(disjunction);
	criteria.addOrder(Order.asc("tradeName"));
	return criteria.list();
	}

	@Override
	public List<Drug> searchDrugBy(String genericName, String tradeName) {
	Criteria criteria = getSession().createCriteria(Drug.class);
	if(genericName!=null)
		criteria.add(Restrictions.like("genericName", genericName,MatchMode.START));
	if(tradeName!=null)
		criteria.add(Restrictions.like("tradeName", tradeName,MatchMode.START));
	criteria.addOrder(Order.asc("tradeName"));
	return criteria.list();
	}
}