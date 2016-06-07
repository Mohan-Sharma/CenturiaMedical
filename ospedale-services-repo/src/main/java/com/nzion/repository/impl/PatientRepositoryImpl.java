package com.nzion.repository.impl;

import com.nzion.domain.*;
import com.nzion.domain.Party.PartyType;
import com.nzion.domain.emr.EMRPatientInfo;
import com.nzion.domain.emr.FamilyMember;
import com.nzion.domain.emr.soap.PatientMedication;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.pms.PMSPatientInfo;
import com.nzion.domain.pms.Policy;
import com.nzion.repository.PatientRepository;
import com.nzion.util.Constants;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PatientRepositoryImpl extends HibernateBaseRepository implements PatientRepository {

    public PatientRepositoryImpl() {
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getEntityInfo(Class<T> klass, Map<String, String> params) {
        Criteria criteria = getSession().createCriteria(klass);
        if (klass.equals(Person.class)) {
            criteria.add(Restrictions.ne("partyType", PartyType.PATIENT));
            criteria.add(Restrictions.ne("partyType", PartyType.PROVIDER));
        }
        criteria.add(Restrictions.isNotNull("partyType"));
        if (UtilValidator.isNotEmpty(params)) {
            for (Entry<String, String> param : params.entrySet()) {
                criteria.add(Restrictions.ilike(param.getKey(), param.getValue(), MatchMode.ANYWHERE));
            }
        }
        criteria.addOrder(Order.asc("partyType"));
        criteria.addOrder(Order.asc("accountNumber"));
        return criteria.list();
    }

    public void saveOrUpdate(Patient patient) {
        save(patient);
    }

    public void saveOrUpdate(PMSPatientInfo patientInfo) {
        save(patientInfo);
    }

    public void saveOrUpdate(EMRPatientInfo patientInfo) {
        save(patientInfo);
    }

    @SuppressWarnings("unchecked")
    public List<Patient> getAllPatients() {
        return getSession().createCriteria(Patient.class).list();
    }

    public void deleteAllPatient() {
        getSession().createQuery("delete from Patient").executeUpdate();
    }

    public void deletePatient(Patient patient) {
        getSession().delete(patient);
    }

    public Patient getPatientByAccountNumber(String accountNumber) {
        return (Patient) getSession().createCriteria(Patient.class).add(Restrictions.eq("accountNumber", accountNumber))
                .uniqueResult();

    }

    public EMRPatientInfo getEMRPatientInfo(long patientId) {
        return (EMRPatientInfo) getSession().createCriteria(EMRPatientInfo.class).add(
                Restrictions.eq("patient.id", patientId)).uniqueResult();
    }

    public PMSPatientInfo getPMSPatientInfo(long patientId) {
        return (PMSPatientInfo) getSession().createCriteria(PMSPatientInfo.class).add(
                Restrictions.eq("patient.id", patientId)).uniqueResult();
    }

    public Patient getPatientById(Long patientId) {
        return super.findByPrimaryKey(Patient.class, patientId);
    }

    @SuppressWarnings("unchecked")
    public List<Policy> getPrimaryPolicyAndValidPolicies(Long patientId) {
        return getSession().createCriteria(Policy.class).add(Restrictions.eq("patient.id", patientId)).add(
                Restrictions.or(Restrictions.or(Restrictions.eq("priority", Constants.PRIMARY_POLICY), Restrictions
                        .isNull("historicalModel.thruDate")), Restrictions.ge("historicalModel.thruDate", new Date())))
                .addOrder(Order.asc("priority")).list();
    }

    @SuppressWarnings("unchecked")
    public List<Policy> getPoliciesForPatientId(Long patientId) {
        return getSession().createCriteria(Policy.class).add(Restrictions.eq("patient.id", patientId)).addOrder(
                Order.asc("priority")).list();
    }

    public void savePolicy(Policy policy) {
        Patient patient = getPatientById(policy.getPatient().getId());
        policy.setPatient(patient);
        super.save(policy);
    }

    @SuppressWarnings("unchecked")
    public List<PatientFamilyMember> getFamilyMembers(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientFamilyMember.class);
        criteria.add(Restrictions.eq("patient", patient));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<Patient> searchPatientsBy(String accountNumber, String firstName, String lastName, Enumeration gender,
                                          Integer age) {

        return searchPatient(accountNumber, firstName, lastName, gender, age, null, null, null);
    }

    @SuppressWarnings("unchecked")
    public List<Patient> searchPatient(String accountNumber, String firstName, String lastName, Enumeration gender,
                                       Integer age, String mobileNumber, Integer startIndex, Integer noOfRecordsPerPage) {
        Criteria criteria = getSession().createCriteria(Patient.class);
        if (UtilValidator.isNotEmpty(accountNumber))
            criteria.add(Restrictions.like("accountNumber", accountNumber, MatchMode.ANYWHERE));
        if (UtilValidator.isNotEmpty(firstName))
            criteria.add(Restrictions.like("firstName", firstName, MatchMode.ANYWHERE));
        if (UtilValidator.isNotEmpty(lastName))
            criteria.add(Restrictions.like("lastName", lastName, MatchMode.ANYWHERE));
        if (gender != null) criteria.add(Restrictions.eq("gender", gender));
        if (UtilValidator.isNotEmpty(mobileNumber))
            criteria.add(Restrictions.eq("contacts.mobileNumber", mobileNumber));
        if (age != null) {
            Date[] range = UtilDateTime.findDateRangeForAge(age);
            criteria.add(Restrictions.le("dateOfBirth", range[1]));
            criteria.add(Restrictions.ge("dateOfBirth", range[0]));
        }
        if (startIndex != null && noOfRecordsPerPage != null) {
            criteria.setFirstResult(startIndex - 1);
            criteria.setMaxResults(noOfRecordsPerPage);
        }
        return criteria.list();
    }

    public Policy getPrimaryPolicy(Patient patient) {
        return (Policy) getSession().createCriteria(Policy.class).add(Restrictions.eq("patient", patient)).add(
                Restrictions.eq("priority", Constants.PRIMARY_POLICY)).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PatientFamilyMember> getFamilyMembersFor(Patient patient, FamilyMember familyMember) {
        Criteria criteria = getSession().createCriteria(PatientFamilyMember.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.eq("familyMember", familyMember));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PatientOtherContactDetail> getpatientOtherContactDetailFor(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientOtherContactDetail.class);
        criteria.add(Restrictions.eq("patient", patient));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<File> getFilesForDocumentType(Patient patient, String[] documentTypes) {
        return getSession().createCriteria(File.class).add(Restrictions.in("documentType", documentTypes)).add(
                Restrictions.eq("imported", false)).createCriteria("folder").add(Restrictions.eq("patient", patient))
                .addOrder(Order.desc("id")).list();
    }

    @Override
    public void storeActiveMedications(PatientSoapNote soapNote, List<PatientRx> allMedications) {
        Query query = getSession().createQuery("delete from PatientMedication where patient =:patient");
        query.setParameter("patient", soapNote.getPatient());
        query.executeUpdate();

        List<PatientMedication> patMedications = new ArrayList<PatientMedication>();
        for (PatientRx rx : allMedications) {
            if ("ACTIVE".equalsIgnoreCase(rx.getStatus())) continue;
            PatientMedication medication = new PatientMedication();
            medication.setPatient(soapNote.getPatient());
            medication.setSoapNote(soapNote);
            medication.setDrugName(rx.getDrug().getTradeName());
            medication.setGenericName(rx.getDrug().getGenericName().getDescription());
            //medication.setDrugId(rx.getRxcui()==null?"0":rx.getRxcui().toString());
            patMedications.add(medication);
        }
        super.save(patMedications);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PatientRemainder> getPatientRemainders(Patient patient) {
        return getSession().createCriteria(PatientRemainder.class).add(Restrictions.eq("patient", patient)).add(Restrictions.ge("expectedFollowUpDate", UtilDateTime.getDayEnd(new Date()))).addOrder(Order.asc("expectedFollowUpDate")).list();
    }

	@Override
	public List<Map<String, Object>> getAllInsurenceMaster() {
		String query = "SELECT * FROM insurance_company_master";
		Query q = getSession().createSQLQuery(query);
		List result = q.list();
		Iterator iterator = result.iterator();
		List<Map<String,Object>> li = new ArrayList<Map<String,Object>>();
		while (iterator.hasNext()) {
		    Object[] row = (Object[])iterator.next();
		    Map<String,Object> map = new HashMap<String, Object>();
		    map.put("insuranceCode", row[0]);
		    map.put("insuranceName", row[1]);
		    map.put("insuranceShortName", row[2]);
		    map.put("insuranceCompanyTpa", row[3]);
		    map.put("payerId", row[4]);
		    map.put("authorizationNo", row[5]);
		    map.put("legalStatutoryInfo", row[6]);
		    map.put("modeOfClaim", row[7]);
		    map.put("contactName", row[8]);
		    map.put("address1", row[9]);
		    map.put("address2", row[10]);
		    map.put("city", row[11]);
		    map.put("state", row[12]);
		    map.put("zip", row[13]);
		    map.put("landline", row[14]);
		    map.put("ext", row[15]);
		    map.put("serviceType", row[16]);
		    map.put("frequencyOfClaim", row[17]);
		    li.add(map);
		}
		return li;
	}

	@Override
	public List<Map<String, Object>> getSelectedInsurancePlan(String insuranceCode) {
		String query = "SELECT * FROM insurance_plan_master WHERE INSURANCE_CODE = '" + insuranceCode + "'";
		Query q = getSession().createSQLQuery(query);
		List result = q.list();
		Iterator iterator = result.iterator();
		List<Map<String,Object>> li = new ArrayList<Map<String,Object>>();
		while (iterator.hasNext()) {
		    Object[] row = (Object[])iterator.next();
		    Map<String,Object> map = new HashMap<String, Object>();
		    map.put("planCode", row[0]);
		    map.put("planName", row[1]);
		    map.put("insuranceCode", row[2]);
		    li.add(map);
		}
		return li;
	}

	@Override
	public List<Map<String, Object>> getSelectedCorporatePlan(
			String corporateCode) {
		String query = "SELECT * FROM corporate_plan_master WHERE CORPORATE_CODE = '" + corporateCode + "'";
		Query q = getSession().createSQLQuery(query);
		List result = q.list();
		Iterator iterator = result.iterator();
		List<Map<String,Object>> li = new ArrayList<Map<String,Object>>();
		while (iterator.hasNext()) {
		    Object[] row = (Object[])iterator.next();
		    Map<String,Object> map = new HashMap<String, Object>();
		    map.put("planCode", row[0]);
		    map.put("planName", row[1]);
		    map.put("corporateCode", row[2]);
		    li.add(map);
		}
		return li;
	}

    @Override
    public Patient getPatientByAfyaId(String afyaId){
        Criteria criteria = getSession().createCriteria(Patient.class);
        criteria.add(Restrictions.eq("afyaId", afyaId));
        return (Patient)criteria.setCacheable(true).uniqueResult();
    }

	@Override
	public List<Map<String, Object>> getTariffCategoryByPatientCategory(String patientCategory) {
		String query = "SELECT * FROM tariff_category WHERE PATIENT_CATEGORY = '" + patientCategory + "'";
		Query q = getSession().createSQLQuery(query);
		List result = q.list();
		Iterator iterator = result.iterator();
		List<Map<String,Object>> li = new ArrayList<Map<String,Object>>();
		while (iterator.hasNext()) {
		    Object[] row = (Object[])iterator.next();
		    Map<String,Object> map = new HashMap<String, Object>();
		    map.put("tariffCode", row[0]);
		    map.put("tariff", row[1]);
		    map.put("patientCategory", row[2]);
		    map.put("groupId", row[3]);
		    map.put("healthPolicyId", row[4]);
		    map.put("primaryPayor", row[5]);
		    map.put("corporateId", row[6]);
		    li.add(map);
		}
		return li;
	}

	@Override
	public Map<String, Object> getTariffCategoryByTariffCode(String teriffCode) {
		String query = "SELECT * FROM tariff_category WHERE TARIFF_CODE = '" + teriffCode + "'";
		Query q = getSession().createSQLQuery(query);
		List result = q.list();
		Iterator iterator = result.iterator();
		List<Map<String,Object>> li = new ArrayList<Map<String,Object>>();
		while (iterator.hasNext()) {
		    Object[] row = (Object[])iterator.next();
		    Map<String,Object> map = new HashMap<String, Object>();
		    map.put("tariffCode", row[0]);
		    map.put("tariff", row[1]);
		    map.put("patientCategory", row[2]);
		    map.put("primaryPayor", row[3]);
		    map.put("corporateCopay", row[4]);
		    map.put("corporateCopayType", row[5]);
		    map.put("corporateId", row[6]);
		    li.add(map);
		}
		return li != null ? li.get(0) : new HashMap<String, Object>();
	}

	@Override
	public List<PatientDeposit> getPatientDepositsByCriteria(Patient patient,Date fromDate, Date thruDate) {
		Criteria criteria = getSession().createCriteria(PatientDeposit.class);
		if(patient != null)
			criteria.add(Restrictions.eq("patient", patient));
		if(fromDate != null)
			criteria.add(Restrictions.ge("depositDate", fromDate));
		if(thruDate != null)
			criteria.add(Restrictions.le("depositDate", thruDate));
        return criteria.list();
	}

	@Override
	public List<PatientWithDraw> getPatientWithdrawByCriteria(Patient patient, Date fromDate, Date thruDate) {
		Criteria criteria = getSession().createCriteria(PatientWithDraw.class);
		if(patient != null)
			criteria.add(Restrictions.eq("patient", patient));
		if(fromDate != null)
			criteria.add(Restrictions.ge("withdrawDate", fromDate));
		if(thruDate != null)
			criteria.add(Restrictions.le("withdrawDate", thruDate));
        return criteria.list();
	}

}
