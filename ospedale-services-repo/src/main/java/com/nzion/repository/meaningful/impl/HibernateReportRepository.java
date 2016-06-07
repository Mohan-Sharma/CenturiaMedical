package com.nzion.repository.meaningful.impl;

import com.nzion.domain.Patient;
import com.nzion.domain.Provider;
import com.nzion.domain.emr.lab.Investigation;
import com.nzion.domain.emr.lab.LabTest;
import com.nzion.domain.emr.soap.*;
import com.nzion.domain.util.EncounterSearchResult;
import com.nzion.report.search.view.LabResultSearchVo;
import com.nzion.report.search.view.PatientEncounterSearchVo;
import com.nzion.report.search.view.PatientSearchVO;
import com.nzion.repository.SoapNoteRepository;
import com.nzion.repository.impl.HibernateBaseRepository;
import com.nzion.repository.meaningful.ReportRepository;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;
import com.nzion.view.EncounterSearchValueObject;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;

@SuppressWarnings("unchecked")
public class HibernateReportRepository extends HibernateBaseRepository implements ReportRepository {

    @Autowired
	private SoapNoteRepository soapNoteRepository;

	@Override
	public Set<EncounterSearchResult> getPatientSoapNote(PatientEncounterSearchVo patientEncounterSearchVo) {
	List<PatientSoapNote> allPatientSoapNotes = null;
	List<Patient> patients = null;
	Criteria patientCriteria = getSession().createCriteria(Patient.class);

	if (patientEncounterSearchVo.getRace() != null || patientEncounterSearchVo.getLowEndAge() != null
			|| patientEncounterSearchVo.getHighEndAge() != null || patientEncounterSearchVo.getEthnicity() != null
			|| patientEncounterSearchVo.getGender() != null) {
		if ("EQUAL".equalsIgnoreCase(patientEncounterSearchVo.getLowEndAgeQuantifier()))
			patientCriteria.add(Restrictions.eq("currentAge", patientEncounterSearchVo.getLowEndAge()));
		else
			if ("LESS".equalsIgnoreCase(patientEncounterSearchVo.getLowEndAgeQuantifier()))
				patientCriteria.add(Restrictions.lt("currentAge", patientEncounterSearchVo.getLowEndAge()));
			else
				if ("Greater".equalsIgnoreCase(patientEncounterSearchVo.getLowEndAgeQuantifier()))
					patientCriteria.add(Restrictions.gt("currentAge", patientEncounterSearchVo.getLowEndAge()));
				else
					if ("Between".equalsIgnoreCase(patientEncounterSearchVo.getLowEndAgeQuantifier())) {
						patientCriteria.add(Restrictions.gt("currentAge", patientEncounterSearchVo.getLowEndAge()));
						patientCriteria.add(Restrictions.lt("currentAge", patientEncounterSearchVo.getHighEndAge()));
					}
		if (patientEncounterSearchVo.getRace() != null)
			patientCriteria.add(Restrictions.eq("race", patientEncounterSearchVo.getRace()));
		if (patientEncounterSearchVo.getEthnicity() != null)
			patientCriteria.add(Restrictions.eq("ethnicity", patientEncounterSearchVo.getEthnicity()));
		if (patientEncounterSearchVo.getGender() != null)
			patientCriteria.add(Restrictions.eq("gender", patientEncounterSearchVo.getGender()));
	}
	patients = patientCriteria.setCacheable(true).list();
	if (UtilValidator.isEmpty(patients)) return Collections.EMPTY_SET;
	if (patientEncounterSearchVo.getVaccine() != null
			&& !"ADMINISTERED".equalsIgnoreCase(patientEncounterSearchVo.getVaccineStatus())) {
		List<Patient> administeredPatient = getAllAdministeredVaccinePatients(patients, patientEncounterSearchVo);
		patients.removeAll(administeredPatient);
	}
	if (UtilValidator.isEmpty(patients)) return Collections.EMPTY_SET;
	allPatientSoapNotes = getSoapNotesFor(patients, patientEncounterSearchVo.getProvider());

	if (UtilValidator.isEmpty(allPatientSoapNotes)) return Collections.EMPTY_SET;

	if (UtilValidator.isNotEmpty(patientEncounterSearchVo.getAllergyName()))
		allPatientSoapNotes = getSoapNoteHavingAllergy(allPatientSoapNotes, patientEncounterSearchVo);

	if (UtilValidator.isEmpty(allPatientSoapNotes)) return Collections.EMPTY_SET;

	if (patientEncounterSearchVo.getVaccine() != null
			&& "ADMINISTERED".equalsIgnoreCase(patientEncounterSearchVo.getVaccineStatus()))
		allPatientSoapNotes = getSoapNoteHavingVaccine(allPatientSoapNotes, patientEncounterSearchVo);

	if (UtilValidator.isEmpty(allPatientSoapNotes)) return Collections.EMPTY_SET;

	if (patientEncounterSearchVo.getIcdElement() != null)
		allPatientSoapNotes = getSoapNotesHavingIcd(allPatientSoapNotes, patientEncounterSearchVo);

	if (UtilValidator.isEmpty(allPatientSoapNotes)) return Collections.EMPTY_SET;

	if (patientEncounterSearchVo.getDrug()!=null)
		allPatientSoapNotes = getSoapNotesHavingMedication(allPatientSoapNotes, patientEncounterSearchVo);

	if (UtilValidator.isEmpty(allPatientSoapNotes)) return Collections.EMPTY_SET;

	if (patientEncounterSearchVo.getLabResultSearchVos().size() > 0 && StringUtils.isNotEmpty(patientEncounterSearchVo.getLabResultSearchVos().get(0).getTestName()))
		allPatientSoapNotes = getPatientSoapNoteHavingLabResult(allPatientSoapNotes, patientEncounterSearchVo);
	return soapNoteRepository.groupSoapNotes(new HashSet<PatientSoapNote>(allPatientSoapNotes),
			new EncounterSearchValueObject(), true);
	}

	private List<Patient> getAllAdministeredVaccinePatients(Collection<Patient> patients,PatientEncounterSearchVo patientEncounterSearchVo) {
	Criteria criteria = getSession().createCriteria(PatientImmunization.class);
	criteria.add(Restrictions.in("patient", patients));
	criteria.add(Restrictions.eq("status", "ADMINISTERED"));
	if (patientEncounterSearchVo.getAdministerStartDate() != null)
		criteria.add(Restrictions.ge("administeredOn", patientEncounterSearchVo.getAdministerStartDate()));
	if (patientEncounterSearchVo.getAdministerEndDate() != null)
		criteria.add(Restrictions.le("administeredOn", patientEncounterSearchVo.getAdministerEndDate()));
	criteria.setProjection(Projections.property("patient"));
	return criteria.list();
	}

	private List<PatientSoapNote> getSoapNoteHavingAllergy(List<PatientSoapNote> soapNotes,PatientEncounterSearchVo patientEncounterSearchVo) {
	Criteria criteria = getSession().createCriteria(PatientAllergy.class);
	criteria.createCriteria("soapSection").add(Restrictions.in("soapNote", soapNotes));
	StringBuilder queryString = new StringBuilder("SELECT P.soapSection.soapNote from PatientAllergy P WHERE "+ " P.allergy=:allergy");
	if (patientEncounterSearchVo.getAllergyStatus() != null) queryString.append(" AND allergyStatus = :status ");
	Query query = getSession().createQuery(queryString.toString());
	query.setParameter("allergy", patientEncounterSearchVo.getAllergyName());
	if (patientEncounterSearchVo.getAllergyStatus() != null)
		query.setParameter("status", patientEncounterSearchVo.getAllergyStatus());
	List<PatientSoapNote> allergyPresent = query.list();
	soapNotes.retainAll(allergyPresent);
	return soapNotes;
	}

	private List<PatientSoapNote> getSoapNotesFor(List<Patient> patients, Provider provider) {
	Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
	if (provider != null) criteria.add(Restrictions.eq("provider", provider));
	if (UtilValidator.isNotEmpty(patients)) criteria.add(Restrictions.in("patient", patients));
	criteria.createCriteria("schedule").add(Restrictions.isNotNull("signedOutTime"));
	//Criteria scheduleCriteria = criteria.createCriteria("schedule");
	//scheduleCriteria.add(Restrictions.or(scheduleCriteria.add(Restrictions.eq("status", STATUS.READY_FOR_BILLING)), scheduleCriteria.add(Restrictions.isNotNull("signedOutTime"))));
	return criteria.list();
	}

	private List<PatientSoapNote> getSoapNoteHavingVaccine(List<PatientSoapNote> soapNotes,PatientEncounterSearchVo patientEncounterSearchVo) {
	StringBuilder queryString = new StringBuilder("SELECT P.soapSection.soapNote from PatientImmunization P WHERE "	+ " P.status=:status AND P.immunization=:vaccineName");
	if (patientEncounterSearchVo.getAdministerStartDate() != null) {
		queryString.append(" AND ADMINISTERED_ON >= :startDate ");
	}
	if (patientEncounterSearchVo.getAdministerEndDate() != null) queryString.append("AND ADMINISTERED_ON <= :endDate");

	Query query = getSession().createQuery(queryString.toString());
	query.setParameter("status", patientEncounterSearchVo.getVaccineStatus());
	query.setParameter("vaccineName", patientEncounterSearchVo.getVaccine());
	if (patientEncounterSearchVo.getAdministerStartDate() != null)
		query.setParameter("startDate", UtilDateTime.getDayStart(patientEncounterSearchVo.getAdministerStartDate()));
	if (patientEncounterSearchVo.getAdministerEndDate() != null)
		query.setParameter("endDate", UtilDateTime.getDayEnd(patientEncounterSearchVo.getAdministerEndDate()));
	List<PatientSoapNote> vaccinePresent = query.list();
	soapNotes.retainAll(vaccinePresent);
	return soapNotes;
	}

	private List<PatientSoapNote> getPatientSoapNoteHavingLabResult(List<PatientSoapNote> allPatientSoapNotes,PatientEncounterSearchVo patientEncounterSearchVo) {

	List<LabResultSearchVo> labResultSearchVos = patientEncounterSearchVo.getLabResultSearchVos();

	String query = "SELECT SOAPNOTE FROM (SELECT O.ID,O.SOAPNOTE AS SOAPNOTE,O.PATIENT, %s FROM OBRSEGMENT O, OBXSEGMENT X WHERE O.ID=X.OBR_ID GROUP BY O.ID) T ";

	StringBuilder soapNoteWhereClause = new StringBuilder(" WHERE SOAPNOTE IN (");
	for (Iterator<PatientSoapNote> iter = allPatientSoapNotes.iterator(); iter.hasNext();) {
		soapNoteWhereClause.append(iter.next().getId());
		if (iter.hasNext()) soapNoteWhereClause.append(",");
	}
	soapNoteWhereClause.append(")");
	query = query.concat(soapNoteWhereClause.toString());
	StringBuilder buffer = new StringBuilder();

	String subQuery = "SUM( " + " (CASE " + " WHEN X.OBX_NAME = '%s' " + " THEN " + "   X.OBSERVATION_VALUE "
			+ " ELSE " + "  NULL " + " END)) AS `%s`";

	for (Iterator<LabResultSearchVo> iter = labResultSearchVos.iterator(); iter.hasNext();) {
		LabResultSearchVo vo = iter.next();
		buffer.append(String.format(subQuery, vo.getTestName(), vo.getTestName()));
		if (iter.hasNext()) {
			buffer.append(", ");
		}
	}
	query = String.format(query, buffer.toString());

	StringBuffer whereClause = new StringBuffer(" AND( ");
	for (Iterator<LabResultSearchVo> iter = labResultSearchVos.iterator(); iter.hasNext();) {
		LabResultSearchVo vo = iter.next();
		whereClause.append("`").append(vo.getTestName()).append("` ").append(getOperator(vo.getOperator())).append(
				vo.getResultValue());
		if (iter.hasNext() && StringUtils.isNotEmpty(vo.getQuantifier())) {
			if (("OR".equals(vo.getQuantifier())) || ("AND".equals(vo.getQuantifier())))
				whereClause.append(" ").append(vo.getQuantifier()).append(" ");
		}
	}
	String queryString = query.concat(whereClause.toString()).concat(")");
	System.out.println(" QUERY : " + queryString);
	SQLQuery sqlQuery = getSession().createSQLQuery(queryString);
	List<BigInteger> results = sqlQuery.list();

	if (results.size() > 0) {
		List<Long> soapIds = new ArrayList<Long>();
		for (BigInteger bi : results) {
			soapIds.add(bi.longValue());
		}

		List<PatientSoapNote> patientSoapNotes = new ArrayList<PatientSoapNote>(results.size());
		patientSoapNotes.addAll(getSession().createCriteria(PatientSoapNote.class).add(Restrictions.in("id", soapIds))				.list());
		return patientSoapNotes;
	} else
		return Collections.EMPTY_LIST;
	}

	private Object getOperator(String operator) {
	if (operator.equals("Less"))
		return "<";
	else
		if (operator.equals("Greater"))
			return ">";
		else
			return "=";
	}

	private List<PatientSoapNote> getSoapNotesHavingMedication(List<PatientSoapNote> allPatientSoapNotes,PatientEncounterSearchVo patientEncounterSearchVo) {
	Criteria criteria = getSession().createCriteria(PatientRx.class);
	criteria.createCriteria("rxSection").add(Restrictions.in("soapNote", allPatientSoapNotes));
	criteria.add(Restrictions.eq("drug", patientEncounterSearchVo.getDrug()));
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	List<PatientRx> patientRxs = criteria.setCacheable(true).list();
	Set<PatientSoapNote> soapNotes = new HashSet<PatientSoapNote>();
	for(PatientRx patientRx : patientRxs)
		soapNotes.add(patientRx.getRxSection().getSoapNote());
	return new ArrayList<PatientSoapNote>(soapNotes);
	}

	private List<PatientSoapNote> getSoapNotesHavingIcd(List<PatientSoapNote> patientSoapNotes,PatientEncounterSearchVo patientEncounterSearchVo) {
	Criteria criteria = getSession().createCriteria(PatientIcd.class);
	criteria.add(Restrictions.in("soapNote", patientSoapNotes)).add(Restrictions.eq("icdElement", patientEncounterSearchVo.getIcdElement()));
	if (patientEncounterSearchVo.getProblemStatus() != null)
		criteria.add(Restrictions.eq("status", patientEncounterSearchVo.getProblemStatus()));
	criteria.setResultTransformer(Criteria.PROJECTION);
	criteria.setProjection(Projections.distinct(Projections.property("soapNote")));
	return criteria.setCacheable(true).list();
	}

	@Override
	public List<String> getAllObxTestNames() {
	Criteria criteria = getSession().createCriteria(Investigation.class);
	criteria.setProjection(Projections.property("investigationName"));
	criteria.addOrder(Order.desc("investigationName"));
	criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
	return criteria.setCacheable(true).list();
	}

	@Override
	public List<Patient> searchPatient(PatientSearchVO patientSearchVO) {
	Criteria criteria = getSession().createCriteria(Patient.class);
	if (UtilValidator.isNotEmpty(patientSearchVO.getFirstName()))
		criteria.add(Restrictions.eq("firstName", patientSearchVO.getFirstName()));
	if (UtilValidator.isNotEmpty(patientSearchVO.getLastName()))
		criteria.add(Restrictions.eq("lastName", patientSearchVO.getLastName()));
	if (patientSearchVO.getLocation() != null)
		criteria.createCriteria("locations").add(Restrictions.eq("id", patientSearchVO.getLocation().getId()));
	if (patientSearchVO.getEthincity() != null)
		criteria.add(Restrictions.eq("ethnicity", patientSearchVO.getEthincity()));
	if (patientSearchVO.getRace() != null) criteria.add(Restrictions.eq("race", patientSearchVO.getRace()));
	if (patientSearchVO.getGender() != null) criteria.add(Restrictions.eq("gender", patientSearchVO.getGender()));
	if (patientSearchVO.getState() != null) {
		criteria.add(Restrictions.eq("contacts.postalAddress.stateProvinceGeo", patientSearchVO.getState()));
	}
	if (patientSearchVO.getCivilId() != null) {
		criteria.add(Restrictions.eq("civilId", patientSearchVO.getCivilId()));
	}
	if (patientSearchVO.getCreatedFromDate() != null || patientSearchVO.getCreatedThruDate() != null) {
		addFromThruToCriteria(criteria, patientSearchVO.getCreatedFromDate(), patientSearchVO.getCreatedThruDate());
	}
	if ("eq".equalsIgnoreCase(patientSearchVO.getOperator()))
		criteria.add(Restrictions.eq("currentAge", patientSearchVO.getAge()));
	else
		if ("lt".equalsIgnoreCase(patientSearchVO.getOperator()))
			criteria.add(Restrictions.lt("currentAge", patientSearchVO.getAge()));
		else
			if ("gt".equalsIgnoreCase(patientSearchVO.getOperator()))
				criteria.add(Restrictions.gt("currentAge", patientSearchVO.getAge()));
			else
				if ("ge".equalsIgnoreCase(patientSearchVO.getOperator()))
					criteria.add(Restrictions.ge("currentAge", patientSearchVO.getAge()));
				else
					if ("le".equalsIgnoreCase(patientSearchVO.getOperator()))
						criteria.add(Restrictions.le("currentAge", patientSearchVO.getAge()));
					else
						if ("ne".equalsIgnoreCase(patientSearchVO.getOperator()))
							criteria.add(Restrictions.ne("currentAge", patientSearchVO.getAge()));
	return criteria.list();
	}

	public SoapNoteRepository getSoapNoteRepository() {
	return soapNoteRepository;
	}

	@Resource
	public void setSoapNoteRepository(SoapNoteRepository soapNoteRepository) {
	this.soapNoteRepository = soapNoteRepository;
	}

}
