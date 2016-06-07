package com.nzion.repository;

import com.nzion.domain.*;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.AcctgTransTypeEnum;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.*;
import com.nzion.domain.emr.soap.*;
import com.nzion.domain.emr.soap.vitalsign.VitalSignReading;
import com.nzion.domain.util.EncounterSearchResult;
import com.nzion.view.EncounterSearchValueObject;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * @author Sandeep Prusty
 *         Dec 6, 2010
 */
public interface SoapNoteRepository extends BaseRepository {

    PatientSoapNote getPatientSoapNote(Schedule schedule);

    PatientSoapNote getPatientSoapNote(Date date, Provider provider);

    SoapSection getSoapSection(PatientSoapNote soapNote, SoapModule module, Class<?> klass);

    SoapSection getSoapSection(PatientSoapNote soapNote, Class<?> klass);

    List<SoapSection> getAllSoapSectionByPatient(Patient patient, Date lastEncounterDate, Class<?> klass);

    SoapSection getPreviousSoapSection(PatientSoapNote currentSoapNote, SoapModule module, Class<?> klass);

    SoapSection getSoapSection(long scheduleId, Class<? extends SoapSection> klass);

    List<PatientChiefComplaint> getSimilarChiefComplaints(Patient patient, String chiefComplaint, Date priorTo);

    List<Question> getQuestionsForSelectedModule(SoapModule selectedModule);

    //Facesheet APIs
    <T extends SoapSection> List<T> getLatestSoapSections(Patient patient, int count, Class<T> klass);

    Set<EncounterSearchResult> searchEncounterFor(EncounterSearchValueObject encounterSearchValueObject);

    List<SoapSection> getSoapSections(Class<?> klass, Patient patient, STATUS status);

    List<PatientRx> getAllPatientRx(Patient patient);

    List<PatientAllergy> getActiveAllergies(Patient patient);

    List<PatientAllergy> getAllAllergies(Patient patient);

    PatientSoapNote getLastEncounter(Patient patient, Date date);

    List<PatientRxAlert> getRxAlertsFor(Drug drug, Patient patient);

    List<PatientIcd> getIcdFromSoapNote(PatientSoapNote soapNote);

    List<SoapSection> getReviewedSoapSectionsFor(PatientSoapNote patientSoapNote);

    List<PatientIcd> searchPatientIcdBy(String icdCode, String icdDescription);

    DiagnosisSection getLatestDiagnosisSectionFor(Patient patient);

    List<SoapSection> getAllSoapSections(PatientSoapNote soapNote);

    <T extends SoapSection> List<T> getAllSoapSections(Patient patient, Class<T> klass);

    List<SoapModule> getSoapModulesUsed(PatientSoapNote soapNote);

    List<PatientRx> searchPatientRxBy(String drugGenericName, String drugTradeName, String activeOrInactive);

    List<PatientIcd> getIcdForPatient(Patient patient, String[] status, Collection<PatientSoapNote> soapNotes);

    List<PastHistorySection> getLatestPastHistorySection(Patient patient, int count);

    List<PatientSoapNote> getLatestPatientSoapNotes(Patient patient, int count, Date date);

    List<QATemplate> getQATemplatesFor(SoapModule soapModule);

    SoapModule getDiagnosisModule(Practice practice, String moduleName);

    List<DiagnosisSection> getAllDiagnosisSections(SoapModule module);

    List<PatientSoapNote> getAllEncountersWithPatientAge(Integer atLeastAge, Date from, Date thru);

    PatientVitalSignSet getLatestVitalSignRecordings(String vitalSignName, Date after, Patient patient);

    IcdElement getIcdElement(String problemCode);

    SoapSection getPreviousSoapSection(PatientSoapNote currentSoapNote, Class<?> klass);

    List<PatientIcd> getIcdForPatient(Patient patient, String statusDescription, PatientSoapNote soapNote);

    List<ProblemListSection> getLatestProblemListSection(Patient patient, int count);

    List<PatientRx> getPatientRxs(Patient patient, Collection<PatientSoapNote> soapNotes);

    List<SoapAddendum> getAddendumsFor(PatientSoapNote soapNote);

    List<PatientMedication> getPatientMedicationsFor(PatientSoapNote soapNote);

    PatientSoapNote getLatestSoapNoteFor(Patient patient, Date date);

    List<PatientSoapNote> getAllSoapNotesExcludingCurrent(PatientSoapNote soapNote);

    List<SOAPPlan> getSoapPlansForPatient(Patient patient, Date after, String followUpFor);

    Set<EncounterSearchResult> groupSoapNotes(Set<PatientSoapNote> soapNotes, EncounterSearchValueObject encounterSearchValueObject, boolean groupFromPatientList);


    List<PatientMedication> filterPatientsByActiveMedication(MedicationOrderSet orderSet);

    <T> List<T> getAllSoapRecords(Patient patient, List<PatientSoapNote> soapNoteList, Class<T> klass);

    <T> List<T> getAllSoapRecordsExcludingCurrentSection(Patient patient, SoapSection currentSection, Class<T> klass);

    List<PatientRx> getAllMedicationsExcludingCurrentSection(MedicationHistorySection historySection, RxSection rxSection, Patient patient);

    List<PatientIcd> getAllPatientIcdExcludingCurrentSection(ProblemListSection section, Patient patient);

    List<VitalSignReading> getAllVitalSignReadings(Patient patient);

    <T extends IdGeneratingBaseEntity> List<T> getAllPatientClinicalRecords(Patient patient, Class<T> klass);

    PatientSocialHistory getLatestPatientSocialHistory(Patient patient);

    PatientSocialHistory getPreceedingPatientSocialHistory(PatientSocialHistory patientSocialHistory);

    PatientSocialHistory getSucceedingPatientSocialHistoryExcludingCurrentSection(PatientSocialHistory patientSocialHistory);

    List<PatientSocialHistory> getPreviousPatientSocialHistories(PatientSocialHistory patientSocialHistory);

    SoapSection getSucceedingSoapSection(PatientSoapNote currentSoapNote, Class<?> klass);

    PatientSocialHistory getOldestPatientSocialHistory(Patient patient);

    SoapSection getPreviousSoapSectionForSameVisitDate(PatientSoapNote currentSoapNote, Class<?> klass);

    SocialHistorySection getPreviousSocialHistorySection(SocialHistorySection currentSection);

    SocialHistorySection getNextSocialHistorySection(SocialHistorySection currentSection);

    List<VisitTypeSoapModule> getVisitTypeSoapModule(SlotType visitType, Provider provider);

    List<QATemplate> getQAtemplatesForExaminationSection(ExaminationSection examinationSection, Set<Speciality> specialities);

    List<PatientAllergy> getPateintAllegiesNotEqToStatus(Patient patient, Enumeration enu);

    List<PatientChronicDisease> getAllPatientChronicDiseaseFor(Patient patient);

    List<PatientSoapNote> getMlcSoapNoteByCriteria(Provider provider, Date fromDate, Date thruDate);

    List<AcctgTransactionEntry> getAcctgTransEntryByCriteria(Date fromDate,Date thruDate,Long providerId, Long patientId, Long encounterId, Long invoiceId, String ipNumber, String referral,boolean isExternalPatientTransactionRequire);

    boolean checkIfNoKnownAllergy(Patient patient);
}