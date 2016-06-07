package com.nzion.repository.impl;

import com.nzion.domain.Enumeration;
import com.nzion.domain.*;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransaction;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.*;
import com.nzion.domain.emr.lab.OBXSegment;
import com.nzion.domain.emr.soap.*;
import com.nzion.domain.emr.soap.vitalsign.VitalSignReading;
import com.nzion.domain.util.EncounterSearchResult;
import com.nzion.repository.SoapNoteRepository;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;
import com.nzion.view.EncounterSearchValueObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.criterion.*;
import org.hibernate.jdbc.Work;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.Patient;

/**
 * @author Sandeep Prusty
 *         Dec 6, 2010
 */

@SuppressWarnings("unchecked")
public class HibernateSoapNoteRepository extends HibernateBaseRepository implements SoapNoteRepository {

    Logger log = Logger.getLogger(HibernateSoapNoteRepository.class);

    @Override
    public PatientSoapNote getPatientSoapNote(Schedule schedule) {
        return findUniqueByCriteria(PatientSoapNote.class, new String[]{"schedule"}, new Object[]{schedule});
    }

    @Override
    public PatientSoapNote getPatientSoapNote(Date date, Provider provider) {
        Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
        criteria.add(Restrictions.eq("date", date));
        criteria.createAlias("personels", "personels").add(Restrictions.eq("PROVIDER", provider));
        return (PatientSoapNote) criteria.setCacheable(true).uniqueResult();
    }

    @Override
    public SoapSection getSoapSection(PatientSoapNote soapNote, SoapModule module, Class<?> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.add(module == null ? Restrictions.isNull("soapModule") : Restrictions.eq("soapModule", module));
        criteria.add(Restrictions.eq("soapNote", soapNote));
        return (SoapSection) criteria.setCacheable(true).uniqueResult();
    }

    @Override
    public SoapSection getPreviousSoapSection(PatientSoapNote currentSoapNote, SoapModule module, Class<?> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.setMaxResults(1);
        criteria.add(module == null ? Restrictions.isNull("soapModule") : Restrictions.eq("soapModule", module));
        criteria.add(Restrictions.ne("soapNote", currentSoapNote));
        criteria.createCriteria("soapNote").add(Restrictions.le("date", currentSoapNote.getDate())).add(
                Restrictions.eq("patient", currentSoapNote.getPatient())).addOrder(Order.desc("id"));
        return (SoapSection) criteria.setCacheable(true).uniqueResult();
    }

    @Override
    public SoapSection getPreviousSoapSection(PatientSoapNote currentSoapNote, Class<?> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.setMaxResults(1);
        criteria.add(Restrictions.ne("soapNote", currentSoapNote));
        criteria.createCriteria("soapNote").add(Restrictions.le("date", UtilDateTime.getDayEnd(currentSoapNote.getDate()))).add(
                Restrictions.eq("patient", currentSoapNote.getPatient())).addOrder(Order.desc("id"));
        return (SoapSection) criteria.setCacheable(true).uniqueResult();

    }

    @Override
    public SoapSection getPreviousSoapSectionForSameVisitDate(PatientSoapNote currentSoapNote, Class<?> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.setMaxResults(1);
        criteria.add(Restrictions.ne("soapNote", currentSoapNote));
        criteria.createCriteria("soapNote").add(Restrictions.eq("patient", currentSoapNote.getPatient())).addOrder(Order.desc("id"));
        return (SoapSection) criteria.setCacheable(true).uniqueResult();

    }

    @Override
    public SoapSection getSoapSection(PatientSoapNote soapNote, Class<?> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.add(Restrictions.eq("soapNote", soapNote));
        return (SoapSection) criteria.setCacheable(true).uniqueResult();
    }

    @Override
    public List<SoapSection> getAllSoapSectionByPatient(Patient patient, Date lastEncounterDate, Class<?> klass) {
        Criteria criteria = getSession().createCriteria(ImageSection.class);
        criteria.createCriteria("soapNote").add(Restrictions.eq("patient", patient)).add(Restrictions.le("date", lastEncounterDate));;
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<PatientChiefComplaint> getSimilarChiefComplaints(Patient patient, String chiefComplaint, Date priorTo) {
        Criteria criteria = getSession().createCriteria(PatientChiefComplaint.class);
        criteria.add(Restrictions.like("chiefComplaint", chiefComplaint, MatchMode.START));
        criteria.createCriteria("soapSection").createCriteria("soapNote").add(Restrictions.eq("patient", patient)).add(Restrictions.lt("date", priorTo));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.setCacheable(true).list();
    }

    @Override
    public List<Question> getQuestionsForSelectedModule(SoapModule selectedModule) {
        Criteria criteria = getSession().createCriteria(Question.class);
        criteria.add(Restrictions.eq("soapModule", selectedModule));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    // Facesheet APIs

    public List<PastHistorySection> getLatestPastHistorySection(Patient patient, int count) {
        Criteria criteria = getSession().createCriteria(PastHistorySection.class);
        criteria.createCriteria("soapNote").add(Restrictions.eq("patient", patient));
        criteria.addOrder(Order.desc("createdTxTimestamp"));
        criteria.setFirstResult(0);
        criteria.setMaxResults(count);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public Set<EncounterSearchResult> searchEncounterFor(EncounterSearchValueObject encounterSearchValueObject) {
        Set<EncounterSearchResult> allEncounterSearchResults = new HashSet<EncounterSearchResult>();
        if (UtilValidator.isEmpty(encounterSearchValueObject.getChiefComplaint())
                && encounterSearchValueObject.getIcdElement() == null && encounterSearchValueObject.getCpt() == null
                && encounterSearchValueObject.getLabTestName() == null && encounterSearchValueObject.getDrug() == null)
            allEncounterSearchResults.addAll(searchByPatientSoapNote(encounterSearchValueObject));
        if (UtilValidator.isNotEmpty(encounterSearchValueObject.getChiefComplaint()))
            allEncounterSearchResults.add(searchByChiefComplaint(encounterSearchValueObject));
        if (encounterSearchValueObject.getDrug() != null)
            allEncounterSearchResults.add(searchByDrug(encounterSearchValueObject));

        if (UtilValidator.isNotEmpty(encounterSearchValueObject.getLabTestName()))
            allEncounterSearchResults.add(searchByLabOrder(encounterSearchValueObject));

        if (encounterSearchValueObject.getIcdElement() != null)
            allEncounterSearchResults.add(searchByICD(encounterSearchValueObject));
        if (encounterSearchValueObject.getCpt() != null)
            allEncounterSearchResults.add(searchByCPT(encounterSearchValueObject));
        return allEncounterSearchResults;
    }

    private EncounterSearchResult searchByChiefComplaint(EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
        Criteria criteria = getSession().createCriteria(ChiefComplainSection.class);
        if (encounterSearchValueObject.getSoapNote() != null)
            criteria.add(Restrictions.eq("soapNote", encounterSearchValueObject.getSoapNote()));
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote").setCacheable(true);
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        soapNoteCriteria.createCriteria("schedule");
        criteria.createCriteria("patientChiefComplaints").add(
                Restrictions.eq("chiefComplaint", encounterSearchValueObject.getChiefComplaint()));
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        if (encounterSearchValueObject.getProvider() != null)
            soapNoteCriteria.add(Restrictions.eq("provider", encounterSearchValueObject.getProvider()));
        List<ChiefComplainSection> chiefComplainSections = criteria.list();
        for (ChiefComplainSection chiefComplainSection : chiefComplainSections)
            patientSoapNotes.add(chiefComplainSection.getSoapNote());
        EncounterSearchResult encounterSearchResult = new EncounterSearchResult();
        encounterSearchResult.setGroupingType("ChiefComplaint");
        encounterSearchResult.setGroupingValue(encounterSearchValueObject.getChiefComplaint());
        encounterSearchResult.setPatientSoapNotes(patientSoapNotes);
        return encounterSearchResult;
    }

    public Set<PatientChiefComplaint> getChiefCompaintsFor(Set<PatientSoapNote> patientSoapNotes,
                                                           EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientChiefComplaint> patientChiefComplaints = new HashSet<PatientChiefComplaint>();
        Criteria criteria = getSession().createCriteria(ChiefComplainSection.class);
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        soapNoteCriteria.createCriteria("schedule");
        criteria.add(Restrictions.in("soapNote", patientSoapNotes));
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        if (encounterSearchValueObject.getProvider() != null)
            soapNoteCriteria.add(Restrictions.eq("provider", encounterSearchValueObject.getProvider()));
        List<ChiefComplainSection> allChiefComplainSections = criteria.list();
        for (ChiefComplainSection chiefComplainSection : allChiefComplainSections)
            patientChiefComplaints.addAll(chiefComplainSection.getPatientChiefComplaints());
        return patientChiefComplaints;
    }

    private EncounterSearchResult searchByDrug(EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
        EncounterSearchResult encounterSearchResult = new EncounterSearchResult();
        Criteria criteria = getSession().createCriteria(RxSection.class);
        if (encounterSearchValueObject.getSoapNote() != null)
            criteria.add(Restrictions.eq("soapNote", encounterSearchValueObject.getSoapNote()));
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        soapNoteCriteria.createCriteria("schedule");
        criteria.createCriteria("patientRxs").add(Restrictions.eq("drug", encounterSearchValueObject.getDrug()));
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        List<RxSection> rxSections = criteria.list();
        for (RxSection rxSection : rxSections)
            patientSoapNotes.add(rxSection.getSoapNote());
        encounterSearchResult.setGroupingType("DRUG");
        encounterSearchResult.setGroupingValue(encounterSearchValueObject.getDrug().getTradeName());
        encounterSearchResult.setPatientSoapNotes(patientSoapNotes);
        return encounterSearchResult;
    }

    private EncounterSearchResult searchByLabOrder(EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
        /*Criteria criteria = getSession().createCriteria(LabOrderSection.class);
        if (encounterSearchValueObject.getSoapNote() != null)
            criteria.add(Restrictions.eq("soapNote", encounterSearchValueObject.getSoapNote()));
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        if (encounterSearchValueObject.getProvider() != null)
            soapNoteCriteria.add(Restrictions.eq("provider", encounterSearchValueObject.getProvider()));
        criteria.createCriteria("labOrder").createAlias("obrSegment","obrSegment").createAlias("obxSegment","obxSegment").add(Restrictions.eq("testName", encounterSearchValueObject.getLabTestName()));


        List<LabOrderSection> labOrderSections = criteria.list();
        for (LabOrderSection labOrderSection : labOrderSections)
            patientSoapNotes.add(labOrderSection.getSoapNote());*/
        Criteria criteria = getSession().createCriteria(OBXSegment.class);
        if (encounterSearchValueObject.getPatient() != null)
            criteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        if (encounterSearchValueObject.getProvider() != null)
            criteria.createAlias("obrSegment","obr").add(Restrictions.eq("obr.provider", encounterSearchValueObject.getProvider()));
        criteria.add(Restrictions.eq("obxName", encounterSearchValueObject.getLabTestName()));
        List<OBXSegment> l = criteria.list();
        for(OBXSegment obx : l){
            patientSoapNotes.add(obx.getSoapNote());
        }
        EncounterSearchResult encounterSearchResult = new EncounterSearchResult();
        encounterSearchResult.setGroupingType("LAB");
        encounterSearchResult.setGroupingValue(encounterSearchValueObject.getLabTestName());
        encounterSearchResult.setPatientSoapNotes(patientSoapNotes);
        return encounterSearchResult;
    }

    private EncounterSearchResult searchByCPT(EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
        Criteria criteria = getSession().createCriteria(DiagnosisSection.class);
        if (encounterSearchValueObject.getSoapNote() != null)
            criteria.add(Restrictions.eq("soapNote", encounterSearchValueObject.getSoapNote()));
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        soapNoteCriteria.createCriteria("schedule");
        criteria.createCriteria("cpts").add(Restrictions.eq("cpt", encounterSearchValueObject.getCpt()));
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        if (encounterSearchValueObject.getProvider() != null)
            soapNoteCriteria.add(Restrictions.eq("provider", encounterSearchValueObject.getProvider()));
        List<DiagnosisSection> diagnosisSections = criteria.list();
        for (DiagnosisSection diagnosisSection : diagnosisSections)
            patientSoapNotes.add(diagnosisSection.getSoapNote());
        EncounterSearchResult encounterSearchResult = new EncounterSearchResult();
        encounterSearchResult.setGroupingType("CPT");
        encounterSearchResult.setGroupingValue(encounterSearchValueObject.getCpt() != null ? encounterSearchValueObject
                .getCpt().getDescription() : "");
        encounterSearchResult.setPatientSoapNotes(patientSoapNotes);
        return encounterSearchResult;
    }

    private EncounterSearchResult searchByICD(EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
        Criteria criteria = getSession().createCriteria(DiagnosisSection.class);
        if (encounterSearchValueObject.getSoapNote() != null)
            criteria.add(Restrictions.eq("soapNote", encounterSearchValueObject.getSoapNote()));
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        soapNoteCriteria.createCriteria("schedule");
        criteria.createCriteria("icds").add(Restrictions.eq("icdElement", encounterSearchValueObject.getIcdElement()));
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        if (encounterSearchValueObject.getProvider() != null)
            soapNoteCriteria.add(Restrictions.eq("provider", encounterSearchValueObject.getProvider()));
        List<DiagnosisSection> diagnosisSections = criteria.list();
        for (DiagnosisSection diagnosisSection : diagnosisSections)
            patientSoapNotes.add(diagnosisSection.getSoapNote());
        EncounterSearchResult encounterSearchResult = new EncounterSearchResult();
        encounterSearchResult.setGroupingType("ICD");
        encounterSearchResult.setGroupingValue(encounterSearchValueObject.getIcdElement().getDescription());
        encounterSearchResult.setPatientSoapNotes(patientSoapNotes);
        return encounterSearchResult;
    }

    private Set<EncounterSearchResult> searchByPatientSoapNote(EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientSoapNote> allPatientSoapNotes = new HashSet<PatientSoapNote>();
        Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  criteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        criteria.createCriteria("schedule");
        if (encounterSearchValueObject.getPatient() != null)
            criteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        if (encounterSearchValueObject.getProvider() != null)
            criteria.add(Restrictions.eq("provider", encounterSearchValueObject.getProvider()));

        if (encounterSearchValueObject.getFromDate() != null && encounterSearchValueObject.getThruDate() != null)
            criteria.add(Restrictions.between("date", UtilDateTime.getDayStart(encounterSearchValueObject.getFromDate()),
                    UtilDateTime.getDayEnd(UtilDateTime.addDaysToDate(encounterSearchValueObject.getThruDate(), 1))));

        if (encounterSearchValueObject.getFromDate() != null && encounterSearchValueObject.getThruDate() == null)
            criteria.add(Restrictions.ge("date", UtilDateTime.getDayStart(encounterSearchValueObject.getFromDate())));

        if (encounterSearchValueObject.getThruDate() != null && encounterSearchValueObject.getFromDate() == null)
            criteria.add(Restrictions.le("date", UtilDateTime.getDayEnd(encounterSearchValueObject.getThruDate())));

        allPatientSoapNotes.addAll(criteria.list());
        return groupSoapNotes(allPatientSoapNotes, encounterSearchValueObject, false);
    }

    public Set<EncounterSearchResult> groupSoapNotes(Set<PatientSoapNote> soapNotes,
                                                     EncounterSearchValueObject encounterSearchValueObject, boolean groupFromPatientList) {
        Set<EncounterSearchResult> encounterSearchResults = new HashSet<EncounterSearchResult>();
        if (UtilValidator.isEmpty(soapNotes)) return encounterSearchResults;
        if (!groupFromPatientList) {
            Set<PatientChiefComplaint> patientChiefComplaints = getChiefCompaintsFor(soapNotes, encounterSearchValueObject);
            Map<String, Set<PatientSoapNote>> chiefComplaintMap = new HashMap<String, Set<PatientSoapNote>>();
            Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
            for (PatientChiefComplaint patientChiefComplaint : patientChiefComplaints) {
                patientSoapNotes = UtilValidator.isEmpty(chiefComplaintMap.get(patientChiefComplaint.getChiefComplaint())) ? new HashSet<PatientSoapNote>()
                        : chiefComplaintMap.get(patientChiefComplaint.getChiefComplaint());
                patientSoapNotes.add(patientChiefComplaint.getSoapSection().getSoapNote());
                chiefComplaintMap.put(patientChiefComplaint.getChiefComplaint(), patientSoapNotes);
            }
            for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : chiefComplaintMap.entrySet())
                encounterSearchResults.add(buildEncounterSearchResult("ChiefComplaint", eachEntry));
        }
        if (!groupFromPatientList) {
            Set<PatientLabOrder> patientLabOrders = getPatientLabOrdersFor(soapNotes, encounterSearchValueObject);
            Map<String, Set<PatientSoapNote>> labOrderMap = new HashMap<String, Set<PatientSoapNote>>();
            Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
            for (PatientLabOrder labOrder : patientLabOrders) {
                patientSoapNotes = UtilValidator.isEmpty(labOrderMap.get(labOrder.getTestName())) ? new HashSet<PatientSoapNote>()
                        : labOrderMap.get(labOrder.getTestName());
                patientSoapNotes.add(labOrder.getSoapSection().getSoapNote());
                labOrderMap.put(labOrder.getTestName(), patientSoapNotes);
            }
            for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : labOrderMap.entrySet())
                encounterSearchResults.add(buildEncounterSearchResult("LAB", eachEntry));
        }
        if (!groupFromPatientList) {
            Set<PatientCpt> patientCpts = getPatientCptsFor(soapNotes, encounterSearchValueObject);
            Map<String, Set<PatientSoapNote>> cptMap = new HashMap<String, Set<PatientSoapNote>>();
            Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
            for (PatientCpt patientCpt : patientCpts) {
                patientSoapNotes = UtilValidator.isEmpty(cptMap.get(patientCpt.getCpt().getDescription())) ? new HashSet<PatientSoapNote>()
                        : cptMap.get(patientCpt.getCpt().getDescription());
                patientSoapNotes.add(patientCpt.getDiagnosisSection().getSoapNote());
                cptMap.put(patientCpt.getCpt().getDescription(), patientSoapNotes);
            }
            for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : cptMap.entrySet())
                encounterSearchResults.add(buildEncounterSearchResult("CPT", eachEntry));
        }
        if (!groupFromPatientList) {
            Set<PatientRx> patientRxs = getPatientRxsFor(soapNotes, encounterSearchValueObject);
            Map<String, Set<PatientSoapNote>> rxMap = new HashMap<String, Set<PatientSoapNote>>();
            Set<PatientSoapNote> rxSoapNotes = new HashSet<PatientSoapNote>();
            for (PatientRx patientRx : patientRxs) {
                rxSoapNotes = UtilValidator.isEmpty(rxMap.get(patientRx.getDrug() != null ? patientRx.getDrug().getTradeName() : "")) ? new HashSet<PatientSoapNote>()
                        : rxMap.get(patientRx.getDrug() != null ? patientRx.getDrug().getTradeName() : "");
                rxSoapNotes.add(patientRx.getRxSection() != null ? patientRx.getRxSection().getSoapNote() : patientRx
                        .getMedicationHistorySection().getSoapNote());
                rxMap.put(patientRx.getDrug() != null ? patientRx.getDrug().getTradeName() : "", rxSoapNotes);
            }
            for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : rxMap.entrySet())
                encounterSearchResults.add(buildEncounterSearchResult("DRUG", eachEntry));
        }

        if (groupFromPatientList) {
            Set<PatientSoapNote> allSoapNotes = new HashSet(soapNotes);
            Set<PatientRx> patientRxs = getPatientRxsFor(soapNotes, encounterSearchValueObject);
            Map<String, Set<PatientSoapNote>> rxMap = new HashMap<String, Set<PatientSoapNote>>();
            Set<PatientSoapNote> rxSoapNotes = new HashSet<PatientSoapNote>();
            for (PatientRx patientRx : patientRxs) {
                rxSoapNotes = UtilValidator.isEmpty(rxMap.get(patientRx.getDrug() != null ? patientRx.getDrug().getTradeName() : "")) ? new HashSet<PatientSoapNote>()
                        : rxMap.get(patientRx.getDrug() != null ? patientRx.getDrug().getTradeName() : "");
                rxSoapNotes.add(patientRx.getRxSection() != null ? patientRx.getRxSection().getSoapNote() : patientRx
                        .getMedicationHistorySection().getSoapNote());
                rxMap.put(patientRx.getDrug() != null ? patientRx.getDrug().getTradeName() : "", rxSoapNotes);
            }
            if (allSoapNotes.size() > 0)
                rxMap.put("Others", allSoapNotes);
            for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : rxMap.entrySet())
                encounterSearchResults.add(buildEncounterSearchResult("DRUG", eachEntry));
        }

        if (true) {
            Set<PatientSoapNote> allSoapNotes = new HashSet(soapNotes);
            Set<PatientIcd> patientIcds = getPatientIcdsFor(soapNotes, encounterSearchValueObject);
            Map<String, Set<PatientSoapNote>> icdMap = new HashMap<String, Set<PatientSoapNote>>();
            Set<PatientSoapNote> icdSoapNotes = new HashSet<PatientSoapNote>();
            for (PatientIcd patientIcd : patientIcds) {
                if (patientIcd.getIcdElement() != null) {
                    icdSoapNotes = UtilValidator.isEmpty(icdMap.get(patientIcd.getIcdElement().getDescription())) ? new HashSet<PatientSoapNote>()
                            : icdMap.get(patientIcd.getIcdElement().getDescription());
                    icdSoapNotes.add(patientIcd.getSoapNote());
                    icdMap.put(patientIcd.getIcdElement().getDescription(), icdSoapNotes);
                }
            }
            if (allSoapNotes.size() > 0)
                icdMap.put("Others", allSoapNotes);
            for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : icdMap.entrySet())
                encounterSearchResults.add(buildEncounterSearchResult("ICD", eachEntry));
        }

        if (groupFromPatientList) {
            Set<PatientSoapNote> allSoapNotes = new HashSet(soapNotes);
           List<OBXSegment> obxSegments = getAllObxSegementFor(soapNotes);
           Map<String, Set<PatientSoapNote>> obxMap = new HashMap<String, Set<PatientSoapNote>>();
           Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
           for (OBXSegment obxSegment : obxSegments) {
               patientSoapNotes = UtilValidator.isEmpty(obxMap.get(obxSegment.getObxName())) ? new HashSet<PatientSoapNote>()
                       : obxMap.get(obxSegment.getObxName());
               patientSoapNotes.add(obxSegment.getSoapNote());
               allSoapNotes.remove(obxSegment.getSoapNote());
               obxMap.put(obxSegment.getObxName(), patientSoapNotes);
           }
           if(allSoapNotes.size()>0)
               obxMap.put("Others", allSoapNotes);
           for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : obxMap.entrySet())
               encounterSearchResults.add(buildEncounterSearchResult("LAB", eachEntry));
        }
        if (groupFromPatientList) {
            Set<PatientSoapNote> allSoapNotes = new HashSet(soapNotes);
            List<PatientImmunization> patientImmunizations = getAllPatientImmunization(soapNotes);
            Map<String, Set<PatientSoapNote>> vaccineMap = new HashMap<String, Set<PatientSoapNote>>();
            Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
            for (PatientImmunization patientImmunization : patientImmunizations) {
                patientSoapNotes = UtilValidator.isEmpty(vaccineMap.get(patientImmunization.getImmunization()
                        .getShortDescription())) ? new HashSet<PatientSoapNote>() : vaccineMap.get(patientImmunization
                        .getImmunization().getShortDescription());
                patientSoapNotes.add(patientImmunization.getSoapSection().getSoapNote());
                allSoapNotes.remove(patientImmunization.getSoapSection().getSoapNote());
                vaccineMap.put(patientImmunization.getImmunization().getShortDescription(), patientSoapNotes);
            }
            if (allSoapNotes.size() > 0) {
                vaccineMap.put("Others", allSoapNotes);
            }
            for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : vaccineMap.entrySet())
                encounterSearchResults.add(buildEncounterSearchResult("VACCINE", eachEntry));
        }

        if (groupFromPatientList) {
            Set<PatientSoapNote> allSoapNotes = new HashSet(soapNotes);
            List<PatientAllergy> allergies = getAllPatientAllergy(soapNotes);
            Map<String, Set<PatientSoapNote>> allergyMap = new HashMap<String, Set<PatientSoapNote>>();
            Set<PatientSoapNote> patientSoapNotes = new HashSet<PatientSoapNote>();
            for (PatientAllergy patientAllergy : allergies) {
                patientSoapNotes = UtilValidator.isEmpty(allergyMap.get(patientAllergy.getAllergy())) ? new HashSet<PatientSoapNote>()
                        : allergyMap.get(patientAllergy.getAllergy());
                patientSoapNotes.add(patientAllergy.getSoapSection().getSoapNote());
                allSoapNotes.remove(patientAllergy.getSoapSection().getSoapNote());
                allergyMap.put(patientAllergy.getAllergy(), patientSoapNotes);
            }
            if (allSoapNotes.size() > 0) {
                allergyMap.put("Others", allSoapNotes);
            }
            for (Map.Entry<String, Set<PatientSoapNote>> eachEntry : allergyMap.entrySet())
                encounterSearchResults.add(buildEncounterSearchResult("ALLERGY", eachEntry));
        }

        return encounterSearchResults;
    }

    private List<PatientAllergy> getAllPatientAllergy(Set<PatientSoapNote> soapNotes) {
        Criteria criteria = getSession().createCriteria(PatientAllergy.class);
        criteria.createCriteria("soapSection").add(Restrictions.in("soapNote", soapNotes));
        return criteria.list();
    }

    private EncounterSearchResult buildEncounterSearchResult(String groupingType, Map.Entry<String, Set<PatientSoapNote>> entry) {
        EncounterSearchResult searchResult = new EncounterSearchResult();
        searchResult.setGroupingType(groupingType);
        searchResult.setGroupingValue(entry.getKey());
        searchResult.setPatientSoapNotes(entry.getValue());
        return searchResult;
    }

    private List<PatientImmunization> getAllPatientImmunization(Set<PatientSoapNote> soapNotes) {
        Criteria criteria = getSession().createCriteria(PatientImmunization.class);
        criteria.createCriteria("soapSection").add(Restrictions.in("soapNote", soapNotes));
        return criteria.list();
    }

    public Set<PatientCpt> getPatientCptsFor(Set<PatientSoapNote> patientSoapNotes,
                                             EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientCpt> patientCpts = new HashSet<PatientCpt>();
        Criteria criteria = getSession().createCriteria(DiagnosisSection.class);
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        criteria.add(Restrictions.in("soapNote", patientSoapNotes));
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        soapNoteCriteria.createCriteria("schedule");
        List<DiagnosisSection> allDiagnosisSections = criteria.list();
        for (DiagnosisSection diagnosisSection : allDiagnosisSections)
            patientCpts.addAll(diagnosisSection.getCpts());
        return patientCpts;
    }

    public Set<PatientIcd> getPatientIcdsFor(Set<PatientSoapNote> patientSoapNotes,
                                             EncounterSearchValueObject encounterSearchValueObject) {
        Criteria criteria = getSession().createCriteria(PatientIcd.class);
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        soapNoteCriteria.createCriteria("schedule");
        if (encounterSearchValueObject.getPatient() != null)
            criteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        criteria.add(Restrictions.in("soapNote", patientSoapNotes));
        List<PatientIcd> patientIcds = criteria.list();
        if (UtilValidator.isEmpty(patientIcds)) Collections.emptySet();
        return new HashSet<PatientIcd>(patientIcds);
    }

    public Set<PatientLabOrder> getPatientLabOrdersFor(Set<PatientSoapNote> patientSoapNotes,
                                                       EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientLabOrder> patientLabOrders = new HashSet<PatientLabOrder>();
        Criteria criteria = getSession().createCriteria(LabOrderSection.class);
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        soapNoteCriteria.createCriteria("schedule");
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        criteria.add(Restrictions.in("soapNote", patientSoapNotes));
        List<LabOrderSection> allLabOrderSections = criteria.list();
        for (LabOrderSection labOrderSection : allLabOrderSections)
            patientLabOrders.addAll(labOrderSection.getLabOrder());
        return patientLabOrders;
    }

    public Set<PatientRx> getPatientRxsFor(Set<PatientSoapNote> patientSoapNotes, EncounterSearchValueObject encounterSearchValueObject) {
        Set<PatientRx> patientRxs = new HashSet<PatientRx>();
        Criteria criteria = getSession().createCriteria(RxSection.class);
        Criteria soapNoteCriteria = criteria.createCriteria("soapNote");
        //if (Infrastructure.getLoggedInPerson() instanceof Patient)
          //  soapNoteCriteria.add(Restrictions.eq("soapNoteReleased", Boolean.TRUE));
        if (encounterSearchValueObject.getPatient() != null)
            soapNoteCriteria.add(Restrictions.eq("patient", encounterSearchValueObject.getPatient()));
        if (encounterSearchValueObject.getProvider() != null)
            soapNoteCriteria.add(Restrictions.eq("provider", encounterSearchValueObject.getProvider()));
        soapNoteCriteria.createCriteria("schedule");
        criteria.add(Restrictions.in("soapNote", patientSoapNotes));
        List<RxSection> allRxSections = criteria.list();
        for (RxSection rxSection : allRxSections)
            patientRxs.addAll(rxSection.getPatientRxs());
        return patientRxs;
    }
    
    
    private List<OBXSegment> getAllObxSegementFor(Set<PatientSoapNote> soapNotes) {
    	Criteria criteria = getSession().createCriteria(OBXSegment.class);
    	criteria.add(Restrictions.in("soapNote", soapNotes));
    	return criteria.list();
    	}

    @Override
    public List<SoapSection> getSoapSections(Class<?> klass, Patient patient, STATUS status) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.createCriteria("soapNote").add(Restrictions.eq("patient", patient));
        if (status != null) criteria.createCriteria("soapNote").add(Restrictions.eq("status", status));
        return criteria.list();
    }

    public List<PatientRx> getAllPatientRx(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientRx.class);
        criteria.add(Restrictions.eq("patient", patient));
        // criteria.createCriteria("soapSection").createCriteria("soapNote");
        return criteria.list();
    }

    public List<PatientAllergy> getActiveAllergies(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientAllergy.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.createCriteria("allergyStatus").add(
                Restrictions.in("description", Arrays.asList(new String[]{"Confirmed", "Suspect", "Pending",
                        "UnConfirmed"})));
        criteria.createCriteria("soapSection").createCriteria("soapNote");
        return criteria.list();
    }

    public List<PatientAllergy> getAllAllergies(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientAllergy.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.createCriteria("allergyStatus").add(
                Restrictions.in("description", Arrays.asList(new String[]{"Inactive", "Errorneous"})));
        criteria.createCriteria("soapSection").createCriteria("soapNote");
        return criteria.list();
    }

    @Override
    public PatientSoapNote getLastEncounter(Patient patient, Date date) {
        Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.le("date", date));
        criteria.addOrder(Order.desc("id"));
        criteria.setMaxResults(3);
        List<PatientSoapNote> patientSoapNotes = criteria.list();
        if (UtilValidator.isNotEmpty(patientSoapNotes)) {
            if (patientSoapNotes.size() > 2) {
                return patientSoapNotes.get(2);
            }else{
                return null;
            }
        }
        return null;
    }

    @Override
    public List<PatientRxAlert> getRxAlertsFor(Drug drug, Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientRxAlert.class);
        criteria.add(Restrictions.eq("drug", drug));
        criteria.add(Restrictions.eq("patient", patient));
        return criteria.list();
    }

    @Override
     public List<PatientIcd> getIcdFromSoapNote(PatientSoapNote soapNote) {
        Criteria criteria = getSession().createCriteria(PatientIcd.class).add(Restrictions.eq("soapNote", soapNote));
        return criteria.list();
    }

	@Override
	public List<PatientIcd> getIcdForPatient(Patient patient, String statuses[], Collection<PatientSoapNote> soapNotes) {
	Criteria criteria = getSession().createCriteria(PatientIcd.class).add(Restrictions.eq("patient", patient));
	if (statuses.length > 0) {
		List enumStatuses = getSession().createCriteria(Enumeration.class)
				.add(Restrictions.in("description", statuses)).list();
		if (UtilValidator.isNotEmpty(enumStatuses)) criteria.add(Restrictions.in("status", enumStatuses));
	}
	if (UtilValidator.isNotEmpty(soapNotes)) criteria.add(Restrictions.in("soapNote", soapNotes));
	criteria.add(Restrictions.isNotNull("problemListSection"));
	return criteria.list();
	}

    public List<PatientIcd> getIcdForPatient(Patient patient, String statusDescription, PatientSoapNote soapNote) {
        List enumStatuses = getSession().createCriteria(Enumeration.class).add(
                Restrictions.eq("description", statusDescription)).list();
        Criteria criteria = getSession().createCriteria(PatientIcd.class).add(Restrictions.eq("patient", patient)).add(
                Restrictions.isNotNull("problemListSection")).addOrder(Order.desc("onSetDate"));
        if (UtilValidator.isNotEmpty(enumStatuses)) criteria.add(Restrictions.in("status", enumStatuses));
        if (soapNote != null) criteria.add(Restrictions.eq("soapNote", soapNote));
        return criteria.list();
    }


    public List<PatientFamilyMember> getRestrictedFamilyMembers(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientFamilyMember.class);
        criteria.add(Restrictions.eq("patient", patient)).add(Restrictions.eq("clinicEmployee", true)).add(
                Restrictions.eq("restrictAccess", true));
        return criteria.list();
    }

    @Override
    public List<SoapSection> getReviewedSoapSectionsFor(PatientSoapNote patientSoapNote) {
        Criteria criteria = getSession().createCriteria(SoapSection.class);
        criteria.add(Restrictions.eq("soapNote", patientSoapNote));
        criteria.add(Restrictions.eq("reviewed", true));
        return criteria.list();
    }

    @Override
    public List<PatientIcd> searchPatientIcdBy(String icdCode, String icdDescription) {
        Criteria criteria = getSession().createCriteria(PatientIcd.class);
        List<PatientIcd> patientIcds = new ArrayList<PatientIcd>();
        if (icdCode != null && icdDescription != null) {
            criteria.createCriteria("icdElement").add(Restrictions.like("code", icdCode, MatchMode.START)).add(
                    Restrictions.like("description", icdDescription, MatchMode.START));
            patientIcds.addAll(criteria.list());
            return patientIcds;
        }
        if (icdCode != null && icdDescription == null) {
            criteria.createCriteria("icdElement").add(Restrictions.like("code", icdCode, MatchMode.START));
            patientIcds.addAll(criteria.list());
            return patientIcds;
        }
        criteria.createCriteria("icdElement").add(Restrictions.like("description", icdDescription, MatchMode.START));
        return criteria.list();
    }

    public List<SoapSection> getAllSoapSections(PatientSoapNote soapNote) {
        return getSession().createCriteria(SoapSection.class).add(Restrictions.eq("soapNote", soapNote))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
    }

    @Override
    public DiagnosisSection getLatestDiagnosisSectionFor(Patient patient) {
        Criteria criteria = getSession().createCriteria(DiagnosisSection.class);
        criteria.createCriteria("soapNote").add(Restrictions.eq("patient", patient));
        criteria.addOrder(Order.desc("createdTxTimestamp"));
        List<DiagnosisSection> diagnosisSections = criteria.list();
        DiagnosisSection diagnosisSection = null;
        if (UtilValidator.isNotEmpty(diagnosisSections)) diagnosisSection = diagnosisSections.get(0);
        return diagnosisSection;
    }

    @Override
    public List<PatientRx> searchPatientRxBy(String drugGenericName, String drugTradeName, String activeOrInactive) {
        List<PatientRx> patientRxs = new ArrayList<PatientRx>();
        Criteria criteria = getSession().createCriteria(PatientRx.class);
        criteria.add(Restrictions.eq("status", activeOrInactive));
        Criteria drugCriteria = criteria.createCriteria("drug");
        if (StringUtils.isNotBlank(drugGenericName)) {
            drugCriteria.add(Restrictions.ilike("genericName", drugGenericName, MatchMode.START));
            if (StringUtils.isNotBlank(drugTradeName))
                drugCriteria.add(Restrictions.ilike("tradeName", drugTradeName, MatchMode.START));
            patientRxs.addAll(criteria.list());
        }
        return patientRxs;
    }

    @Override
    public SoapSection getSoapSection(long scheduleId, Class<? extends SoapSection> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.createCriteria("soapNote").createCriteria("schedule").add(Restrictions.eq("id", scheduleId));
        return (SoapSection) criteria.uniqueResult();
    }

    @Override
    public List<PatientSoapNote> getLatestPatientSoapNotes(Patient patient, int count, Date date) {
        Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.lt("date", date));
        criteria.addOrder(Order.desc("id"));
        criteria.setMaxResults(count);
        return criteria.list();
    }

    @Override
    public List<SoapModule> getSoapModulesUsed(PatientSoapNote soapNote) {
        Criteria criteria = getSession().createCriteria(SoapSection.class);
        criteria.add(Restrictions.eq("soapNote", soapNote));
        criteria.setProjection(Projections.property("soapModule"));
        return criteria.list();
    }

    @Override
    public List<QATemplate> getQATemplatesFor(SoapModule soapModule) {
        Criteria criteria = getSession().createCriteria(QATemplate.class);
        criteria.add(Restrictions.eq("soapModule", soapModule));
        criteria.setFetchMode("questions", FetchMode.LAZY);
        return criteria.list();
    }

    @Override
    public SoapModule getDiagnosisModule(Practice practice, String moduleName) {
        Criteria criteria = getSession().createCriteria(SoapModule.class);
        criteria.add(Restrictions.eq("practice", practice));
        criteria.add(Restrictions.eq("moduleName", moduleName));
        return (SoapModule) criteria.uniqueResult();
    }

    @Override
    public List<DiagnosisSection> getAllDiagnosisSections(SoapModule module) {
        Criteria criteria = getSession().createCriteria(SoapModule.class);
        criteria.add(Restrictions.eq("soapModule", module));
        return criteria.list();
    }

    @Override
    public List<PatientSoapNote> getAllEncountersWithPatientAge(Integer atLeastAge, Date from, Date thru) {
        Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
        if (from != null) criteria.add(Restrictions.ge("date", from));
        if (thru != null) criteria.add(Restrictions.le("date", thru));
        if (atLeastAge != null && thru != null) {
            Date givenYearsBack = UtilDateTime.addYearsToDate(thru, -atLeastAge);
            criteria.createCriteria("patient").add(Restrictions.le("dateOfBirth", givenYearsBack));
        }
        return criteria.list();
    }

    public PatientVitalSignSet getLatestVitalSignRecordings(String vitalSignName, Date after, Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientVitalSignSet.class);
        criteria.add(Restrictions.ge("recordedOn", after));
        criteria.createCriteria("vitalSigns").createCriteria("vitalSign").add(Restrictions.eq("name", vitalSignName));
        criteria.addOrder(Order.desc("recordedOn"));
        criteria.setMaxResults(1);
        List<PatientVitalSignSet> result = criteria.list();
        return UtilValidator.isEmpty(result) ? null : result.get(0);
    }

    @Override
    public IcdElement getIcdElement(final String problemCode) {
        SnomedICDWork work = new SnomedICDWork(problemCode);
        getSession().doWork(work);
        String targetCodes = work.getTargetCodes();
        Criteria criteria = getSession().createCriteria(IcdElement.class);
        if (targetCodes.indexOf("|") != -1) {
            criteria.add(Restrictions.in("code", targetCodes.split("|")));
        } else
            criteria.add(Restrictions.eq("code", targetCodes));
        List<IcdElement> icds = criteria.list();
        if (UtilValidator.isNotEmpty(icds)) return icds.get(0);
        return null;
    }

    private static class SnomedICDWork implements Work {

        String problemCode = null;
        String targetCodes = null;

        SnomedICDWork(String problemCode) {
            this.problemCode = problemCode;
        }

        @Override
        public void execute(Connection connection) throws SQLException {
            String query = "	select T.targetcodes from sct_crossmaps_icd9 S, sct_crossmaptargets_icd9 T where S.MAPTARGETID=T.TARGETID and s.MAPCONCEPTID=?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, problemCode);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            targetCodes = rs.getString(1);
        }

        public String getTargetCodes() {
            return targetCodes;
        }
    }

    @Override
    public List<ProblemListSection> getLatestProblemListSection(Patient patient, int count) {
        Criteria criteria = getSession().createCriteria(ProblemListSection.class);
        criteria.createCriteria("soapNote").add(Restrictions.eq("patient", patient));
        criteria.addOrder(Order.desc("createdTxTimestamp"));
        criteria.setFirstResult(0);
        criteria.setMaxResults(count);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public List<PatientRx> getPatientRxs(Patient patient, Collection<PatientSoapNote> patientSoapNotes) {
        Criteria criteria = getSession().createCriteria(PatientRx.class);
        Set<PatientRx> patientRxs = new HashSet<PatientRx>();
        if (UtilValidator.isNotEmpty(patientSoapNotes)) {
            criteria.createAlias("rxSection", "rxSection");
            criteria.add(Restrictions.in("rxSection.soapNote", patientSoapNotes));
            patientRxs.addAll(criteria.list());
            criteria = getSession().createCriteria(PatientRx.class);
            criteria.createAlias("medicationHistorySection", "medicationHistorySection");
            criteria.add(Restrictions.in("medicationHistorySection.soapNote", patientSoapNotes));
            patientRxs.addAll(criteria.list());
        }

        return new ArrayList(patientRxs);
    }

    @Override
    public List<SoapAddendum> getAddendumsFor(PatientSoapNote soapNote) {
        Criteria criteria = getSession().createCriteria(SoapAddendum.class);
        criteria.add(Restrictions.eq("soapNote", soapNote));
        return criteria.list();
    }

    @Override
    public List<PatientMedication> getPatientMedicationsFor(PatientSoapNote soapNote) {
        Criteria criteria = getSession().createCriteria(PatientMedication.class);
        criteria.add(Restrictions.eq("soapNote", soapNote));
        return criteria.list();
    }

    public List<PatientMedication> filterPatientsByActiveMedication(MedicationOrderSet orderSet) {
        Criteria criteria = getSession().createCriteria(PatientMedication.class);
        criteria.add(Restrictions.in("drugId", orderSet.getDrugs()));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public List<PatientSoapNote> getAllSoapNotesExcludingCurrent(PatientSoapNote soapNote) {
        Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
        criteria.add(Restrictions.eq("patient", soapNote.getPatient()));
        List<PatientSoapNote> soapNotes = criteria.list();
        soapNotes.remove(soapNote);

        return soapNotes;
    }

    @Override
    public PatientSoapNote getLatestSoapNoteFor(Patient patient, Date date) {
        Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.le("date", date));
        criteria.addOrder(Order.desc("id"));
        List<PatientSoapNote> soapNotes = criteria.list();
        if (UtilValidator.isNotEmpty(soapNotes)) return soapNotes.get(0);
        return null;
    }

    @Override
    public List<SOAPPlan> getSoapPlansForPatient(Patient patient, Date after, String followUpFor) {
        Criteria criteria = getSession().createCriteria(SOAPPlan.class);
        criteria.add(Restrictions.eq("followUpFor", followUpFor));
        criteria.createCriteria("recommendationSection").createCriteria("soapNote")
                .add(Restrictions.eq("patient", patient)).add(Restrictions.ge("date", after));
        return criteria.list();
    }

    @Override
    public <T extends SoapSection> List<T> getAllSoapSections(Patient patient, Class<T> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.createCriteria("soapNote").add(Restrictions.eq("patient", patient));
        return criteria.list();
    }

    @Override
    public <T> List<T> getAllSoapRecords(Patient patient, List<PatientSoapNote> soapNoteList, Class<T> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.add(Restrictions.eq("patient", patient));
        if (UtilValidator.isNotEmpty(soapNoteList))
            criteria.createCriteria("soapSection").add(Restrictions.in("soapNote", soapNoteList));
        return criteria.list();
    }

    @Override
    public <T extends SoapSection> List<T> getLatestSoapSections(Patient patient, int count, Class<T> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.createCriteria("soapNote").add(Restrictions.eq("patient", patient));
        criteria.addOrder(Order.desc("createdTxTimestamp"));
        if (count != 0) {
            criteria.setFirstResult(0);
            criteria.setMaxResults(count);
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public <T> List<T> getAllSoapRecordsExcludingCurrentSection(Patient patient, SoapSection currentSection, Class<T> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.ne("soapSection", currentSection));
        return criteria.list();
    }

    @Override
    public List<PatientRx> getAllMedicationsExcludingCurrentSection(MedicationHistorySection historySection, RxSection rxSection, Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientRx.class);
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.ne("medicationHistorySection", historySection));
        disjunction.add(Restrictions.ne("rxSection", rxSection));
        criteria.add(disjunction);
        criteria.add(Restrictions.eq("patient", patient));
        return criteria.list();
    }

    @Override
    public List<PatientIcd> getAllPatientIcdExcludingCurrentSection(ProblemListSection section, Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientIcd.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.ne("problemListSection", section));
        return criteria.list();
    }

    @Override
    public List<VitalSignReading> getAllVitalSignReadings(Patient patient) {
        Criteria criteria = getSession().createCriteria(VitalSignReading.class);
        criteria.add(Restrictions.eq("patient", patient));
        return criteria.list();
    }

    @Override
    public <T extends IdGeneratingBaseEntity> List<T> getAllPatientClinicalRecords(Patient patient, Class<T> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public PatientSocialHistory getLatestPatientSocialHistory(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientSocialHistory.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.addOrder(Order.desc("id"));
        criteria.setMaxResults(1);
        return (PatientSocialHistory) criteria.uniqueResult();
    }

    @Override
    public PatientSocialHistory getOldestPatientSocialHistory(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientSocialHistory.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.addOrder(Order.asc("id"));
        criteria.setMaxResults(1);
        return (PatientSocialHistory) criteria.uniqueResult();
    }

    @Override
    public PatientSocialHistory getPreceedingPatientSocialHistory(PatientSocialHistory patientSocialHistory) {
        Criteria criteria = getSession().createCriteria(PatientSocialHistory.class);
        criteria.add(Restrictions.eq("patient", patientSocialHistory.getPatient()));
        criteria.add(Restrictions.lt("id", patientSocialHistory.getId()));
        criteria.addOrder(Order.desc("id"));
        criteria.setMaxResults(1);
        return (PatientSocialHistory) criteria.uniqueResult();
    }

    @Override
    public PatientSocialHistory getSucceedingPatientSocialHistoryExcludingCurrentSection(PatientSocialHistory patientSocialHistory) {
        Criteria criteria = getSession().createCriteria(PatientSocialHistory.class);
        criteria.add(Restrictions.eq("patient", patientSocialHistory.getPatient()));
        criteria.add(Restrictions.gt("id", patientSocialHistory.getId()));
        criteria.addOrder(Order.asc("id"));
        criteria.setMaxResults(1);
        return (PatientSocialHistory) criteria.uniqueResult();
    }

    public List<PatientSocialHistory> getPreviousPatientSocialHistories(PatientSocialHistory patientSocialHistory) {
        Criteria criteria = getSession().createCriteria(PatientSocialHistory.class);
        criteria.add(Restrictions.eq("patient", patientSocialHistory.getPatient()));
        criteria.add(Restrictions.lt("id", patientSocialHistory.getId()));
        criteria.addOrder(Order.desc("id"));
        return criteria.list();
    }

    @Override
    public SoapSection getSucceedingSoapSection(PatientSoapNote currentSoapNote, Class<?> klass) {
        Criteria criteria = getSession().createCriteria(klass);
        criteria.setMaxResults(1);
        criteria.add(Restrictions.ne("soapNote", currentSoapNote));
        criteria.createCriteria("soapNote").add(Restrictions.ge("date", currentSoapNote.getDate())).add(
                Restrictions.eq("patient", currentSoapNote.getPatient())).addOrder(Order.asc("id"));
        return (SoapSection) criteria.setCacheable(true).uniqueResult();
    }

    @Override
    public SocialHistorySection getNextSocialHistorySection(SocialHistorySection currentSection) {
        Criteria criteria = getSession().createCriteria(SocialHistorySection.class);
        criteria.add(Restrictions.gt("id", currentSection.getId()));
        criteria.add(Restrictions.ne("soapNote", currentSection.getSoapNote()));
        criteria.createCriteria("soapNote").add(Restrictions.ge("date", UtilDateTime.getDateOnly(currentSection.getSoapNote().getDate()))).add(
                Restrictions.eq("patient", currentSection.getSoapNote().getPatient())).addOrder(Order.desc("id"));
        List<SocialHistorySection> socialHistorySections = criteria.list();
        if (UtilValidator.isEmpty(socialHistorySections))
            return currentSection;
        return socialHistorySections.get(socialHistorySections.size() - 1);
    }

    @Override
    public SocialHistorySection getPreviousSocialHistorySection(SocialHistorySection currentSection) {
        Criteria criteria = getSession().createCriteria(SocialHistorySection.class);
        criteria.add(Restrictions.lt("id", currentSection.getId()));
        criteria.add(Restrictions.ne("soapNote", currentSection.getSoapNote()));
        criteria.createCriteria("soapNote").add(Restrictions.le("date", UtilDateTime.getDayEnd(currentSection.getSoapNote().getDate()))).add(
                Restrictions.eq("patient", currentSection.getSoapNote().getPatient())).addOrder(Order.asc("id"));
        List<SocialHistorySection> socialHistorySections = criteria.list();
        if (UtilValidator.isEmpty(socialHistorySections))
            return currentSection;
        return socialHistorySections.get(socialHistorySections.size() - 1);
    }

    public List<VisitTypeSoapModule> getVisitTypeSoapModule(SlotType visitType, Provider provider) {
        Criteria criteria = getSession().createCriteria(VisitTypeSoapModule.class);
        criteria.add(Restrictions.eq("slotType", visitType));
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.isNull("provider"));
        disjunction.add(Restrictions.eq("provider", provider));
        criteria.add(disjunction);
        return criteria.list();
    }

    @Override
    public List<QATemplate> getQAtemplatesForExaminationSection(ExaminationSection examinationSection, Set<Speciality> specialities) {
        Criteria criteria = getSession().createCriteria(PatientExamination.class);
        criteria.add(Restrictions.in("speciality", specialities));
        criteria.add(Restrictions.eq("examinationSection", examinationSection));
        criteria.setProjection(Projections.property("qaTemplate"));
        return criteria.list();
    }

    @Override
    public List<PatientAllergy> getPateintAllegiesNotEqToStatus(Patient patient, Enumeration enu) {
        Criteria criteria = getSession().createCriteria(PatientAllergy.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.ne("allergyStatus", enu));
        return criteria.list();
    }

    @Override
    public List<PatientChronicDisease> getAllPatientChronicDiseaseFor(Patient patient) {
        Criteria criteria = getSession().createCriteria(PatientChronicDisease.class);
        criteria.add(Restrictions.eq("patient", patient));
        return criteria.list();
    }
    
    @Override
	public List<PatientSoapNote> getMlcSoapNoteByCriteria(Provider provider,Date fromDate, Date thruDate) {
    	 Criteria criteria = getSession().createCriteria(PatientSoapNote.class);
    	 if(provider != null)
    		 criteria.add(Restrictions.eq("provider", provider));
    	 if(fromDate != null)
    		 criteria.add(Restrictions.ge("date", fromDate));
    	 if(thruDate != null)
    		 criteria.add(Restrictions.le("date", thruDate));
    	 criteria.add(Restrictions.eq("isMlc", true));
		return criteria.list();
	}
    
    @Override
	public List<AcctgTransactionEntry> getAcctgTransEntryByCriteria(Date fromDate,Date thruDate,Long providerId, Long patientId, Long encounterId, Long invoiceId,
			String speciality, String referral,boolean isExternalPatientTransactionRequire) {
    	Criteria criteria = getSession().createCriteria(AcctgTransactionEntry.class);
    	
       // criteria.createAlias("acctgTrans","acctgTrans");
        if(UtilValidator.isNotEmpty(providerId))
            criteria.add(Restrictions.eq("doctorId", providerId.toString()));
    	if(UtilValidator.isNotEmpty(patientId))
            criteria.add(Restrictions.eq("patientId", patientId.toString()));
    	if(UtilValidator.isNotEmpty(speciality))
            criteria.add(Restrictions.eq("specialityCode", speciality));
    	if(UtilValidator.isNotEmpty(referral))
            criteria.add(Restrictions.eq("referralId", referral));
    	/*if(isExternalPatientTransactionRequire)
            criteria.add(Restrictions.eq("extPatientFlag", "Ext"));
    	else
    		criteria.add(Restrictions.isNull("extPatientFlag"));*/
    	if(fromDate != null) criteria.add(Restrictions.ge("transactionDate",UtilDateTime.getDayStart(fromDate)));
    	if(thruDate != null) criteria.add(Restrictions.le("transactionDate",UtilDateTime.getDayEnd(thruDate)));
    	/*if(UtilValidator.isNotEmpty(ipNumber)){
            criteria.createAlias("patientAdmission","patientAdmission");
            criteria.add(Restrictions.eq("patientAdmission.accountNumber", ipNumber));
        }*/
        /*if(UtilValidator.isNotEmpty(acctgTransTypeEnum)){
            criteria.add(Restrictions.eq("acctgTransTypeEnum", acctgTransTypeEnum));
        }*/
        criteria.addOrder(Order.desc("transactionDate"));
        /*criteria.addOrder(Order.asc("id"));
        criteria.addOrder(Order.asc("transEntrySeq"));*/
    	List<AcctgTransactionEntry> acctgTransactionEntries = criteria.list();
        int f = acctgTransactionEntries.size();
    	//List<AcctgTransactionEntry> acctgTransactionEntries = new ArrayList<AcctgTransactionEntry>();
		return acctgTransactionEntries;
	}

    @Override
    public boolean checkIfNoKnownAllergy(Patient patient){
        String queryToFetchNoKnownAllergy = "select s.noKnownAllergy from SoapSection as s, PatientSoapNote as psn, Patient as p where psn.id=s.soapNote and p.id = psn.patient AND p.id= :id and s.noKnownAllergy != null";
        Query query = getSession().createQuery(queryToFetchNoKnownAllergy);
        query.setLong("id", patient.getId());
        List<Object> result =  query.list();
        if(result.contains(true))
            return true;
        else
            return false;
    }

}
