package com.nzion.service;

import com.nzion.domain.Enumeration;
import com.nzion.domain.*;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.*;
import com.nzion.domain.emr.soap.*;
import com.nzion.domain.emr.soap.vitalsign.VitalSignReading;
import com.nzion.domain.util.EncounterSearchResult;
import com.nzion.dto.OrderDto;
import com.nzion.enums.PatientRxAlertType;
import com.nzion.view.EncounterSearchValueObject;

import java.util.*;

/**
 * @author Sandeep Prusty
 * Dec 6, 2010
 */
public interface SoapNoteService {
	
	public final static String[] ACTIVE_PROBLEMS=new String[] {"Active","Chronic","Intermittent","Recurrent"};

	PatientSoapNote getPatientSoapNote(Schedule schedule);

	PatientSoapNote getPatientSoapNote(Date date, Provider provider);

	List<SoapModule> getSoapNoteModules(PatientSoapNote soapNote);

	List<SoapModule> getAccessibleSoapNoteModules(PatientSoapNote soapNote);

	SoapSection getSoapSection(PatientSoapNote soapNote, SoapModule module, Class<? extends SoapSection> klass);

	SoapSection getPreviousSoapSection(PatientSoapNote currentSoapNote, SoapModule module,Class<? extends SoapSection> klass);

	SoapSection saveSoapSection(SoapSection soapSection,String eventMessage);

	SoapSection saveUpdateSoapSection(SoapSection soapSection,String eventMessage);

	SoapSection saveSoapSectionAndCreateSoapNote(SoapSection soapSection, List<SoapModule> customisedModules);

	PatientSoapNote deleteSoapNote(PatientSoapNote soapNote);

	PatientSoapNote saveSoapNote(PatientSoapNote soapNote);

	List<PatientChiefComplaint> getSimilarChiefComplaints(Patient patient, String chiefComplaint, Date priorTo);

	PatientSoapNote loadOrCreateSoapNote(Schedule schedule);

	SoapSection getSoapSection(PatientSoapNote soapNote, Class<? extends SoapSection> klass);

	List<SoapSection> getAllSoapSectionByPatient(Patient patient, Date lastEncounterDate, Class<? extends SoapSection> klass);

	SoapSection getSoapSection(long scheduleId, Class<? extends SoapSection> klass);

	List<SoapComponentAuthorization> getSoapComponentAuthorizations();

	List<PatientFamilyIllness> getAllFamilyIllnesses(Patient patient);

	List<PatientRx> getAllPastMedications(Patient patient, String status);

	List<PatientVitalSignSet> getAllPatientVitalSign(Patient patient);

	Collection<Question> getQuestionsForSelectedModule(SoapModule selectedModule);

	List<Immunization> getPatientImmunizationDues(Patient patient);

	Set<PatientImmunization> getAllPatientImmunization(Patient patient);

	Set<PatientPastOperationHistory> getAllPastOperation(Patient patient);

	Set<PatientPastTreatmentHistory> getAllPastTreatment(Patient patient);

	Set<PatientAllergy> getAllPastAllergy(Patient patient);

	public List<Map<PatientRxAlertType, String>> checkPatientAllergyToDrug(Drug drug, Patient patient,
			ProviderRxPreference rxPreference, AllergySection allergySection);

	List<PatientAllergy> getPatientAllergyNotEqToStatus(Patient patient, Enumeration enu);

	<T extends SoapSection> List<T> getLatestSoapSections(Patient patient, int count, Class<T> klass);

	Set<EncounterSearchResult> searchEncounterFor(EncounterSearchValueObject encounterSearchValueObject);

	List<? extends SoapSection> getSoapSections(Class<? extends SoapSection> klass, Patient patient);

	List<PatientAllergy> getAllergiesFromSignedOutNotes(Patient patient);

	List<PatientRx> getAllPatientRx(Patient patient);

	Set<PatientAllergy> getAllAllergies(Patient patient);

	PatientSoapNote getLastEncounter(Patient patient, Date date);

	void signOutSoapNote(PatientSoapNote soapNote);

	List<PatientRxAlert> getRxAlertsFor(Drug drug, Patient patient);

	List<PatientIcd> getIcdFromSoapNote(PatientSoapNote soapNote);

	void addDerivedValues(PatientVitalSignSet patientVitalSignSet);

	List<PatientPastObservationHistory> getAllPastObservation(Patient patient);

	Set<SoapModule> getReviewedSoapModuleFor(PatientSoapNote patientSoapNote);

	List<PatientAllergy> getActiveAllergies(Patient patient);

	List<PatientIcd> searchPatientIcdBy(String icdCode, String icdDescription);

	List<PatientIcd> getLastSoapNotePatientIcdFor(Patient patient);

	List<SoapSection> getAllSoapSections(PatientSoapNote soapNote);

	List<PatientRx> searchPatientRxBy(String drugGenericName, String drugTradeName, String activeOrInactive);

	List<PatientIcd> getIcdForPatient(Patient patient, String[] status,Collection<PatientSoapNote> soapNotes);

	QATemplate getQATemplate(Provider loggedInPerson, SoapModule selectedModule);

	List<PatientSoapNote> getLatestPatientSoapNotes(Patient patient, int count, Date date);

	List<PatientSoapNote> getSoapNotesForSimilarChiefComplaints(Patient patient, String chiefComplaint, Date priorTo,ChiefComplainSection chiefComplainSection);
	
	List<QATemplate> getQATemplatesFor(SoapModule soapModule);

	List<DiagnosisSection> getAllDiagnosisSections(Practice practice);

	List<PatientSoapNote> getDistinctSoapNotesForIcdCodeSet(IcdCodeSet codeSet, Practice practice);

	SoapModule getDiagnosisModule(Practice practice, String moduleName);

	SOAPPlan isSoapPlanningDone(PatientSoapNote soapNote, String followUpFor);

	SOAPPlan isSoapPlanningDone(Patient patient, Date after, String followUpFor);
 
	PatientVitalSign getLatestVitalSignRecording(String vitalsignName, Date after, Patient patient);

	IcdElement getIcdElement(String problemCode);

	SoapSection getPreviousSoapSection(PatientSoapNote currentSoapNote, Class<?> klass);

	List<PatientIcd> getIcdForPatient(Patient patient,String statusDescription,PatientSoapNote soapNote);

	List<PatientRx> getPatientRxs(Patient patient,Collection<PatientSoapNote> patientSoapNotes);

	List<SoapAddendum> getAddendumsFor(PatientSoapNote soapNote);
	
	List<PatientSoapNote> getAllSoapNotesExcludingCurrent(PatientSoapNote soapNote);
	
	PatientSoapNote getLatestSoapNoteFor(Patient patient,Date date);
	
	<T> List<T> getAllSoapRecords(Patient patient,	List<PatientSoapNote> soapNoteList, Class<T> klass);
	
	<T> List<T> getAllSoapRecordsExcludingCurrentSection(Patient patient,SoapSection currentSection, Class<T> klass);
	
	List<PatientRx> getAllMedicationsExcludingCurrentSection(MedicationHistorySection historySection,RxSection rxSection,Patient patient);
	
	List<PatientIcd> getAllPatientIcdExcludingCurrentSection(ProblemListSection section,Patient patient);
	
	List<VitalSignReading> getAllVitalSignReadings(Patient patient);
	
	<T extends SoapSection> List<T> getLatestPatientRecords(Patient patient, String fieldName, Class<T> klass,int count);
	
	<T extends IdGeneratingBaseEntity>List<T> getAllPatientClinicalRecords(Patient patient,Class<T>klass);
	
	PatientSocialHistory getLatestPatientSocialHistory(Patient patient);
	
	PatientSocialHistory getPreceedingPatientSocialHistory(SocialHistorySection socialHistorySection);

	PatientSocialHistory getSucceedingPatientSocialHistory(SocialHistorySection socialHistorySection);
	
    List<PatientSocialHistory> getPreviousPatientSocialHistories(PatientSocialHistory patientSocialHistory);
    
    PatientSocialHistory getPreviousSectionPatientSocialHistory(SocialHistorySection currentSection);
    
    SoapSection getSucceedingSoapSection(PatientSoapNote currentSoapNote,Class<?> klass);
   
    PatientSocialHistory getSucceedingSectionPatientSocialHistory(SocialHistorySection currentSection);
    
    PatientSocialHistory getOldestPatientSocialHistory(Patient patient);

    SocialHistorySection getPreviousSocialHistorySection(SocialHistorySection currentSection);
    
    SocialHistorySection getNextSocialHistorySection(SocialHistorySection currentSection);

	List<SoapModule> getSoapModules(SlotType visitType, Provider provider,PatientSoapNote patientSoapNote);
	
	List<PatientChronicDisease> getAllPatientChronicDiseaseFor(Patient patient);
	
	List<PatientSoapNote> getMlcSoapNoteByCriteria(Provider provider , Date fromDate , Date thruDate);
	
	List<AcctgTransactionEntry> getAcctgTransEntryByCriteria(Date fromDate,Date thruDate,Long providerId,Long patientId,Long encounterId,Long invoiceId,String speciality,String referral,boolean isExternalPatientTransactionRequire);

    boolean checkIfNoKnownAllergy(Patient patient);
    
    List<PatientRx> getAllPatientActivePrescription(Patient patient);
    
    OrderDto prepairedPharmacyPrescription(List<PatientRx> patientRx);
    
    void completeOrder(String orderId,String pharmacyId);
    
    void orderPayment(String orderId,String totalAmount,String pharmacyId);
}
