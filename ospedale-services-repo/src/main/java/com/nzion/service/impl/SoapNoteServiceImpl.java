package com.nzion.service.impl;

import com.nzion.domain.Enumeration;
import com.nzion.domain.*;
import com.nzion.domain.PharmacyOrder.PharmacyOrderStatus;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.base.IdGeneratingBaseEntity;
import com.nzion.domain.billing.AcctgTransactionEntry;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.*;
import com.nzion.domain.emr.lab.LabOrderRequest;
import com.nzion.domain.emr.soap.*;
import com.nzion.domain.emr.soap.vitalsign.VitalSignReading;
import com.nzion.domain.util.EncounterSearchResult;
import com.nzion.dto.OrderDto;
import com.nzion.enums.EventType;
import com.nzion.enums.PatientRxAlertType;
import com.nzion.enums.SoapComponents;
import com.nzion.external.*;
import com.nzion.repository.DrugRepository;
import com.nzion.repository.ProviderRepository;
import com.nzion.repository.SoapNoteRepository;
import com.nzion.service.PatientService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.impl.ApplicationEvents;
import com.nzion.util.*;
import com.nzion.view.EncounterSearchValueObject;
import org.axonframework.eventhandling.EventBus;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Sandeep Prusty
 *         Dec 6, 2010
 */
@Service("soapNoteService")
@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
public class SoapNoteServiceImpl implements SoapNoteService {

    private SoapNoteRepository soapNoteRepository;

    private DrugRepository drugRepository;

    private ProviderRepository providerRepository;

    private PatientService patientService;

    private CommonCrudService commonCrudService;

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    final String QUERY_TO_FETCH_PRACTICE_DETAILS="select practice_name as practiceName, tenant_id as tenantId from practice";

    private EventBus eventBus;

    @Resource
    private ExternalServiceClient externalServiceClient;

    @Resource
    public void setDataSource(DataSource dataSource) {
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Resource
    @Required
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public PatientService getPatientService() {
        return patientService;
    }

    @Resource
    @Required
    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public static short INTERACTION_RATING = 2;

    private final String PATIENT_IMMUNIZATION_STATUS = "ADMINISTERED";

    @Resource
    @Required
    public void setSoapNoteRepository(SoapNoteRepository soapNoteRepository) {
        this.soapNoteRepository = soapNoteRepository;
    }

    @Resource
    @Required
    public void setDrugRepository(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    @Override
    public PatientSoapNote getPatientSoapNote(Schedule schedule) {
        return soapNoteRepository.getPatientSoapNote(schedule);
    }

    @Override
    public PatientSoapNote getPatientSoapNote(Date date, Provider provider) {
        return soapNoteRepository.getPatientSoapNote(date, provider);
    }

    public List<SoapModule> getAccessibleSoapNoteModules(PatientSoapNote soapNote) {
        return Roles.filterByGrantedAuthority(getSoapNoteModules(soapNote), Infrastructure.getUserLogin()
                .getAuthorization());
    }

    public List<SoapModule> getSoapNoteModules(PatientSoapNote soapNote) {
        List<SoapModule> soapModules = soapNote.getId() == null ? soapNoteRepository.getAll(SoapModule.class) : soapNoteRepository.getSoapModulesUsed(soapNote);
        soapModules.remove(null);
        Collections.sort(soapModules);
        return soapModules;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public SoapSection saveSoapSection(SoapSection soapSection, String eventMessage) {
        soapNoteRepository.merge(soapSection);
        if (UtilValidator.isNotEmpty(eventMessage)) {
            ApplicationEvents.postEvent(EventType.CLINICAL, soapSection.getSoapNote().getPatient(), Infrastructure
                    .getUserLogin(), eventMessage);
        }
        return soapSection;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public SoapSection saveUpdateSoapSection(SoapSection soapSection, String eventMessage) {
        if(soapSection.getId() == null) {
            soapNoteRepository.save(soapSection);
        }
        else
            soapNoteRepository.merge(soapSection);
        if (UtilValidator.isNotEmpty(eventMessage)) {
            ApplicationEvents.postEvent(EventType.CLINICAL, soapSection.getSoapNote().getPatient(), Infrastructure
                    .getUserLogin(), eventMessage);
        }
        return soapSection;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public SoapSection saveSoapSectionAndCreateSoapNote(SoapSection soapSection, List<SoapModule> customisedModules) {
        //soapSection.getSoapNote().getSchedule().setStatus(STATUS.ROOMED);
        soapNoteRepository.save(soapSection.getSoapNote());
        saveSoapSection(soapSection, "Chief Complaint section created");
        copyFromPreviousSoapNote(customisedModules, soapSection.getSoapNote());
        return soapSection;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public PatientSoapNote deleteSoapNote(PatientSoapNote soapNote) {
        ApplicationEvents.postEvent(EventType.CLINICAL, soapNote.getPatient(), Infrastructure.getUserLogin(),
                "SOAP Note deleted");
        Schedule schedule = soapNote.getSchedule();
        schedule.setStatus(STATUS.CHECKEDIN);
        List<SoapSection> sections = getAllSoapSections(soapNote);
        List<LabOrderRequest> labList = commonCrudService.findByEquality(LabOrderRequest.class, new String[]{"patientSoapNote"}, new Object[]{soapNote});
        if (UtilValidator.isNotEmpty(labList)) soapNoteRepository.remove(labList);
        soapNoteRepository.remove(sections);
        soapNote = soapNoteRepository.findByPrimaryKey(PatientSoapNote.class, soapNote.getId());
        soapNoteRepository.remove(soapNote);
        soapNoteRepository.merge(schedule);
        return soapNote;
    }

    public List<SoapSection> getAllSoapSections(PatientSoapNote soapNote) {
        return soapNoteRepository.getAllSoapSections(soapNote);
    }

    @SuppressWarnings("unchecked")
    private void copyFromPreviousSoapNote(List<SoapModule> modules, PatientSoapNote soapNote) {
        PatientSoapNote previousSoapNote = getLastEncounter(soapNote.getPatient(), soapNote.getDate());
        List<SoapSection> previousSoapSectionList = (previousSoapNote != null) ? getAllSoapSections(previousSoapNote)
                : new ArrayList<SoapSection>(0);
        Map<String, SoapSection> previousSoapSections = new HashMap<String, SoapSection>();
        for (SoapSection section : previousSoapSectionList)
            if (section.getSoapModule() != null)
                previousSoapSections.put(section.getSoapModule().getModuleName(), section);
        for (SoapModule module : modules) {
            try {
                String className = module.getClassName();
                if (className == null) continue;
                Class<? extends SoapSection> klass = (Class<? extends SoapSection>) Class.forName(module.getClassName());
                SoapSection section = klass.newInstance();
                section.initialize(soapNote, module, previousSoapNote, previousSoapSections);
                saveSoapSection(section, null);
                if (section.edited())
                    ApplicationEvents.postEvent(EventType.CLINICAL, soapNote.getPatient(), Infrastructure.getUserLogin(),
                            section.getSoapModule().getModuleName() + " section created");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public PatientSoapNote saveSoapNote(PatientSoapNote soapNote) {
        soapNoteRepository.save(soapNote);
        return soapNote;
    }

    @Override
    public List<PatientChiefComplaint> getSimilarChiefComplaints(Patient patient, String chiefComplaint, Date priorTo) {
        return soapNoteRepository.getSimilarChiefComplaints(patient, chiefComplaint, priorTo);
    }

    @Override
    public SoapSection getSoapSection(PatientSoapNote soapNote, SoapModule module, Class<? extends SoapSection> klass) {
        return soapNote.getId() == null ? null : soapNoteRepository.getSoapSection(soapNote, module, klass);
    }

    @Override
    public SoapSection getSoapSection(PatientSoapNote soapNote, Class<? extends SoapSection> klass) {
        return soapNoteRepository.getSoapSection(soapNote, klass);
    }

    @Override
    public List<SoapSection> getAllSoapSectionByPatient(Patient patient, Date lastEncounterDate, Class<? extends SoapSection> klass) {
        return soapNoteRepository.getAllSoapSectionByPatient(patient, lastEncounterDate, klass);
    }

    public SoapSection getSoapSection(long scheduleId, Class<? extends SoapSection> klass) {
        return soapNoteRepository.getSoapSection(scheduleId, klass);
    }

    @Override
    public SoapSection getPreviousSoapSection(PatientSoapNote currentSoapNote, SoapModule module, Class<? extends SoapSection> klass) {
        return soapNoteRepository.getPreviousSoapSection(currentSoapNote, module, klass);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public PatientSoapNote loadOrCreateSoapNote(Schedule schedule) {
        PatientSoapNote soapNote = getPatientSoapNote(schedule);
        if (soapNote != null) return soapNote;
        soapNote = new PatientSoapNote();
        soapNote.setSchedule(schedule);
        soapNote.setEncounterType((SoapNoteType) schedule.getVisitType());
        soapNote.addActor(schedule.getPatient(), Roles.PATIENT);
        soapNote.addActor(schedule.getPerson(), Roles.PROVIDER);
        Calendar calendar = Calendar.getInstance();
        Date schdeuledDate = schedule.getStartDate();
        Date currentDate = new Date();
        calendar.set(schdeuledDate.getYear() + 1900, schdeuledDate.getMonth(), schdeuledDate.getDate(), currentDate
                .getHours(), currentDate.getMinutes(), currentDate.getSeconds());
        soapNote.setDate(calendar.getTime());
        return soapNote;
    }

    @Override
    public List<SoapComponentAuthorization> getSoapComponentAuthorizations() {
        List<SoapComponentAuthorization> soapComponentAuthorizations = soapNoteRepository
                .getAll(SoapComponentAuthorization.class);
        if (UtilValidator.isNotEmpty(soapComponentAuthorizations)) return soapComponentAuthorizations;
        soapComponentAuthorizations = new ArrayList<SoapComponentAuthorization>();
        for (SoapComponents soapComponents : SoapComponents.values())
            soapComponentAuthorizations.add(new SoapComponentAuthorization(soapComponents));
        return soapComponentAuthorizations;
    }

    @Override
    public List<PatientFamilyIllness> getAllFamilyIllnesses(Patient patient) {
        List<FamilyHistorySection> familyHistorySections = soapNoteRepository.getAllSoapSections(patient,
                FamilyHistorySection.class);
        List<PatientFamilyIllness> familyIllnesses = new ArrayList<PatientFamilyIllness>();
        for (FamilyHistorySection section : familyHistorySections)
            familyIllnesses.addAll(section.getFamilyIllnesses());
        return familyIllnesses;
    }

    @Override
    public List<PatientRx> getAllPastMedications(Patient patient, String status) {
        PatientRx rx = new PatientRx();
        rx.setPatient(patient);
        rx.setStatus(status);
        return soapNoteRepository.simulateExampleSearch(new String[]{"patient", "status"}, rx);
    }

    public List<PatientRx> getAllPatientRx(Patient patient) {
        return soapNoteRepository.getAllPatientRx(patient);
    }

    @Override
    public List<PatientVitalSignSet> getAllPatientVitalSign(Patient patient) {
        List<VitalSignSection> vitalSignSection = soapNoteRepository.getAllSoapSections(patient, VitalSignSection.class);
        List<PatientVitalSignSet> patientVitalSignSet = new ArrayList<PatientVitalSignSet>();
        for (VitalSignSection section : vitalSignSection)
            patientVitalSignSet.addAll(section.getVitalSignRecordings());
        return patientVitalSignSet;
    }

    @Override
    public List<Question> getQuestionsForSelectedModule(SoapModule selectedModule) {
        return soapNoteRepository.getQuestionsForSelectedModule(selectedModule);
    }

    public Set<PatientImmunization> getAllPatientImmunization(Patient patient) {
        Set<PatientImmunization> allImmunizationsTaken = new HashSet<PatientImmunization>();
        Set<PatientImmunization> filteredImmunizations = new HashSet<PatientImmunization>();
        List<ImmunizationSection> sections = soapNoteRepository.getAllSoapSections(patient, ImmunizationSection.class);
        for (ImmunizationSection section : sections)
            allImmunizationsTaken.addAll(section.getImmunizations());
        for (PatientImmunization patientImmunization : allImmunizationsTaken)
            if (PATIENT_IMMUNIZATION_STATUS.equalsIgnoreCase(patientImmunization.getStatus())) {
                filteredImmunizations.add(patientImmunization);
            }
        return filteredImmunizations;
    }

    @Override
    public List<Immunization> getPatientImmunizationDues(Patient patient) {
        Set<PatientImmunization> allImmunizationsTaken = getAllPatientImmunization(patient);
        List<Immunization> immunizations = soapNoteRepository.getAll(Immunization.class);
        Date patientDOB = patient.getDateOfBirth();
        if (patientDOB == null) return null;
        for (PatientImmunization patientImmunization : allImmunizationsTaken) {
            if (patientImmunization.getStatus() != null
                    && PATIENT_IMMUNIZATION_STATUS.equalsIgnoreCase(patientImmunization.getStatus()))
                immunizations.remove(patientImmunization.getImmunization());
        }
        List<Immunization> recommendedDueImmunizations = new ArrayList<Immunization>();
        for (Immunization immunization : immunizations)
            if (immunization.checkApplicability(patientDOB)) recommendedDueImmunizations.add(immunization);

        Collections.sort(recommendedDueImmunizations, new Comparator<Immunization>() {

            @Override
            public int compare(Immunization o1, Immunization o2) {
                return o1.getExpectedDate().compareTo(o2.getExpectedDate());
            }
        });
        return recommendedDueImmunizations;
    }

    @Override
    public Set<PatientPastOperationHistory> getAllPastOperation(Patient patient) {
        List<PastHistorySection> allPastHistorySections = soapNoteRepository.getAllSoapSections(patient, PastHistorySection.class);
        Set<PatientPastOperationHistory> pastOperationHistories = new HashSet<PatientPastOperationHistory>();
        for (PastHistorySection pastHistorySection : allPastHistorySections) {
            PastHistorySection section = soapNoteRepository.findByPrimaryKey(PastHistorySection.class, pastHistorySection.getId());
            pastOperationHistories.addAll(section.getPatientPastOperationHitories());
        }
        return pastOperationHistories;
    }

    @Override
    public Set<PatientPastTreatmentHistory> getAllPastTreatment(Patient patient) {
        List<PastHistorySection> allPastHistorySections = soapNoteRepository.getAllSoapSections(patient, PastHistorySection.class);
        Set<PatientPastTreatmentHistory> patientPastTreatmentHistories = new HashSet<PatientPastTreatmentHistory>();
        for (PastHistorySection pastHistorySection : allPastHistorySections)
            patientPastTreatmentHistories.addAll(pastHistorySection.getPatientPastTreatmentHistories());
        return patientPastTreatmentHistories;
    }

    @Override
    public Set<PatientAllergy> getAllPastAllergy(Patient patient) {
        List<AllergySection> allAllergySections = soapNoteRepository.getAllSoapSections(patient, AllergySection.class);
        Set<PatientAllergy> patientAllergies = new HashSet<PatientAllergy>();
        for (AllergySection allergySection : allAllergySections)
            patientAllergies.addAll(allergySection.getPatientAllergies());
        return patientAllergies;
    }


    @Override
    public List<Map<PatientRxAlertType, String>> checkPatientAllergyToDrug(Drug drug, Patient patient,
                                                                           ProviderRxPreference rxPreference, AllergySection allergySection) {

        List<Map<PatientRxAlertType, String>> messages = new ArrayList<Map<PatientRxAlertType, String>>();
        drug = soapNoteRepository.findByPrimaryKey(Drug.class, drug.getId());
        Set<PatientAllergy> allPatientAllergies = allergySection.getPatientAllergies();
        for (PatientAllergy patientAllergy : allPatientAllergies)
            if ("Inactive".equalsIgnoreCase(patientAllergy.getAllergyStatus().getDescription())
                    || "Errorneous".equalsIgnoreCase(patientAllergy.getAllergyStatus().getDescription()))
                allPatientAllergies.remove(patientAllergy);
        return messages;
    }

    @Override
    public Set<EncounterSearchResult> searchEncounterFor(EncounterSearchValueObject encounterSearchValueObject) {
        return soapNoteRepository.searchEncounterFor(encounterSearchValueObject);
    }

    @Override
    public List<SoapSection> getSoapSections(Class<? extends SoapSection> klass, Patient patient) {
        return soapNoteRepository.getSoapSections(klass, patient, null);
    }

    @Override
    public List<PatientAllergy> getAllergiesFromSignedOutNotes(Patient patient) {
        return soapNoteRepository.getActiveAllergies(patient);
    }

    public Set<PatientAllergy> getAllAllergies(Patient patient) {
        return new HashSet<PatientAllergy>(soapNoteRepository.getAllAllergies(patient));
    }

    @Override
    public PatientSoapNote getLastEncounter(Patient patient, Date date) {
        return soapNoteRepository.getLastEncounter(patient, date);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void signOutSoapNote(PatientSoapNote soapNote) {

        if (soapNote.getSchedule() != null) {
            soapNote.getSchedule().setStatus(STATUS.SOAPSIGNEDOUT);
            soapNote.getSchedule().setSignedOutTime(new Date());
            soapNoteRepository.merge(soapNote.getSchedule());
            List<Map<String, Object>> practices = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_PRACTICE_DETAILS, new TreeMap<String, Object>());
            Map<String, Object> practice = practices.get(0);
            prepareLabOrderDtoAndPlaceLabOrder(soapNote, practice);
            preparePrescriptionDtoAndPlaceOrder(soapNote, practice);
            //preparePrescriptionDtoAndPlaceOrderAsyn(soapNote, practice);
            //prepareLabOrderDtoAndPlaceLabOrderAsyn(soapNote, practice);
        }

        if (soapNote.getSoapNoteReleased().booleanValue())
            ApplicationEvents.postEvent(EventType.SOAP_SIGNOUT, soapNote.getPatient(), Infrastructure.getUserLogin(),
                    "signed out the SOAP Note");
    }

    private void prepareLabOrderDtoAndPlaceLabOrderAsyn(final PatientSoapNote soapNote, final Map<String, Object> practice) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                prepareLabOrderDtoAndPlaceLabOrder(soapNote, practice);
            }
        });
        thread.start();
    }

    private void preparePrescriptionDtoAndPlaceOrderAsyn(final PatientSoapNote soapNote, final Map<String, Object> practice) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                preparePrescriptionDtoAndPlaceOrder(soapNote, practice);
            }
        });
        thread.start();
    }

    private void prepareLabOrderDtoAndPlaceLabOrder(PatientSoapNote soapNote, Map<String, Object> practice) {
        LabOrderSection labOrderSection = (LabOrderSection) soapNoteRepository.getSoapSection(soapNote, LabOrderSection.class);
        if(UtilValidator.isNotEmpty(labOrderSection)) {
            Set<PatientLabOrder> patientLabOrders = labOrderSection.getLabOrder();
            if (labOrderSection.getLaboratoryTenantId() != null && !labOrderSection.getLaboratoryTenantId().equals("Default") && patientLabOrders.size() > 0) {
                Patient patient = soapNote.getPatient();
                if (UtilValidator.isEmpty(patient))
                    return;
                List<LabLineItem> labLineItems = new ArrayList<LabLineItem>();
                LabLineItem labLineItem = null;
                for (PatientLabOrder patientLabOrder : patientLabOrders) {
                    labLineItem = new LabLineItem();
                    labLineItem.setPropertiesFromlabLineItem(patientLabOrder);
                    labLineItems.add(labLineItem);
                }
                String visitId = String.valueOf(soapNote.getSchedule() != null ? soapNote.getSchedule().getLastPatientVisit() != null ? soapNote.getSchedule().getLastPatientVisit().getId() : null : null);
                Date visitDate = soapNote.getSchedule() != null ? soapNote.getSchedule().getLastPatientVisit() != null ? soapNote.getSchedule().getLastPatientVisit().getUpdatedTxTimestamp() : null : null;
                LabOrdetDto labOrdetDto = new LabOrdetDto(practice.get("tenantId").toString(), patient.getAfyaId(), labLineItems, visitId, visitDate, patient.getFirstName(), patient.getLastName(), patient.getContacts().getMobileNumber(),
                        patient.getPatientType(), soapNote.getProvider().getProviderName(), practice.get("practiceName").toString(), patient.getDateOfBirth());
                externalServiceClient.handleLabOrder(labOrdetDto, labOrderSection.getLaboratoryTenantId());
            }
        }

    }

    private void preparePrescriptionDtoAndPlaceOrder(PatientSoapNote soapNote, Map<String, Object> practice) {
        RxSection rxSection = (RxSection) soapNoteRepository.getSoapSection(soapNote, RxSection.class);
        if(UtilValidator.isNotEmpty(rxSection)) {
            Set<PatientRx> patientRxList = rxSection.getPatientRxs();
            String hisModuleName = null;
            if (soapNote.getSelectedHisModuleId() != null)
                hisModuleName = AfyaServiceConsumer.getHISModuleNameByBenefitId(soapNote.getSelectedHisModuleId());
            System.out.println("\n\n\n\nhisModuleName" + hisModuleName + "\n\n\n");
            if (rxSection.getPharmacyTenantId() != null && !rxSection.getPharmacyTenantId().equals("Default") && patientRxList.size() > 0) {
                Patient patient = soapNote.getPatient();
                if (UtilValidator.isEmpty(patient))
                    return;
                boolean mobileNumberVisibleForDelivery = checkIfMaskingMobileNumberNeeded(patient.getAfyaId());
                List<PrescriptionLineItem> prescriptionLineItems = new ArrayList<PrescriptionLineItem>();
                PrescriptionLineItem prescriptionLineItem = null;
                for (PatientRx patientRx : patientRxList) {
                    prescriptionLineItem = new PrescriptionLineItem();
                    prescriptionLineItem.setPropertiesFromPatientRx(patientRx);
                    prescriptionLineItem.setDrugCategory("PrescDrugs");
                    prescriptionLineItems.add(prescriptionLineItem);
                }
                String benefitId = null;
                String groupId = null;
                if (UtilValidator.isNotEmpty(soapNote.getPatient().getPatientInsurances())) {
                    PatientInsurance patientInsurance = soapNote.getPatient().getPatientInsurances().iterator().next();
                    benefitId = patientInsurance.getBenefitId();
                    groupId = patientInsurance.getGroupId();
                }

                BigDecimal corporateCopay = null;
                String corporateCopayType = null;
                String corporatePrimaryPayer = null;
                if ("CORPORATE".equals(patient.getPartyType())) {
                    corporateCopay = patient.getPatientCorporate().getCorporateCopay();
                    corporateCopayType = patient.getPatientCorporate().getCorporateCopayType();
                    corporatePrimaryPayer = patient.getPatientCorporate().getPrimaryPayor();
                }
                String visitId = String.valueOf(soapNote.getSchedule() != null ? soapNote.getSchedule().getLastPatientVisit() != null ? soapNote.getSchedule().getLastPatientVisit().getId() : null : null);
                Date visitDate = soapNote.getSchedule() != null ? soapNote.getSchedule().getLastPatientVisit() != null ? soapNote.getSchedule().getLastPatientVisit().getUpdatedTxTimestamp() : null : null;
                PrescriptionDTO dto = new PrescriptionDTO(practice.get("tenantId").toString(), patient.getAfyaId(), prescriptionLineItems,
                        visitId, visitDate, patient.getFirstName(), patient.getLastName(), patient.getContacts().getMobileNumber(),
                        patient.getPatientType(), String.valueOf(soapNote.getProvider().getId()), soapNote.getProvider().getProviderName(), practice.get("practiceName").toString(), soapNote.getSelectedHisModuleId(),
                        benefitId, groupId, hisModuleName, "N", patient.getDateOfBirth(), corporateCopay, corporateCopayType, corporatePrimaryPayer, mobileNumberVisibleForDelivery);

                OrderDto orderdto = externalServiceClient.handlePrescriptionOrder(dto, rxSection.getPharmacyTenantId());

                if (orderdto != null && UtilValidator.isNotEmpty(orderdto.getOrderId())) {
                    PharmacyOrder pharmacyOrder = new PharmacyOrder();
                    pharmacyOrder.setCurrencyUom(orderdto.getCurrencyUom());
                    pharmacyOrder.setOrderId(orderdto.getOrderId());
                    pharmacyOrder.setTotalAmount(orderdto.getTotalAmount());
                    pharmacyOrder.setPharOrderStatus(PharmacyOrderStatus.ACTIVE);
                    pharmacyOrder.setPatient(soapNote.getPatient());
                    pharmacyOrder.setPharmacyTennantId(rxSection.getPharmacyTenantId());
                    commonCrudService.save(pharmacyOrder);
                }

            }
        }
    }

    /*
    * Modified To read consents from portal by Mohan Sharma 22 Aug 15
    * */
    private boolean checkIfMaskingMobileNumberNeeded(String afyaId) {
        boolean result = Boolean.FALSE;
        List<Map<String, Object>> consents = AfyaServiceConsumer.getPatientPrivacyPolicyConsents(afyaId);
        if(UtilValidator.isEmpty(consents)){
            return result;
        }
        //Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsents = patient.getPatientPrivacyPolicyConsents();
        for(Map<String, Object> patientPrivacyPolicyConsent : consents){
            if(patientPrivacyPolicyConsent.get("question").equals("Do you want the delivery person to call on your mobile number (or see your mobile number) at the time of home delivery?")) {
                if(UtilValidator.isEmpty(patientPrivacyPolicyConsent.get("answer")))
                    return result;
                result = Boolean.valueOf(patientPrivacyPolicyConsent.get("answer").toString());
            }
        }
        return result;
    }

    @Override
    public List<PatientRxAlert> getRxAlertsFor(Drug drug, Patient patient) {
        return soapNoteRepository.getRxAlertsFor(drug, patient);
    }

    @Override
    public List<PatientIcd> getIcdFromSoapNote(PatientSoapNote soapNote) {
        return soapNoteRepository.getIcdFromSoapNote(soapNote);
    }

    @Override
    public void addDerivedValues(PatientVitalSignSet patientVitalSignSet) {
        PatientVitalSign bmiRecord = patientVitalSignSet.getPatientVitalSign(VitalSign.BMI);
        if (bmiRecord == null) return;
        PatientVitalSign heightRecord = patientVitalSignSet.getPatientVitalSign(VitalSign.HEIGHT);
        if (heightRecord == null || UtilValidator.isEmpty(heightRecord.getValue())) return;
        PatientVitalSign weightRecord = patientVitalSignSet.getPatientVitalSign(VitalSign.WETGHT);
        if (weightRecord == null || UtilValidator.isEmpty(weightRecord.getValue())) return;
        Double bmiValue = Double.parseDouble(weightRecord.getValue()) / Math.pow((Double.parseDouble(heightRecord.getValue()) / 100), 2);
        bmiRecord.setValue(String.valueOf(bmiValue));
    }

    @Override
    public List<PatientPastObservationHistory> getAllPastObservation(Patient patient) {
        List<PastHistorySection> allPastHistorySections = soapNoteRepository.getAllSoapSections(patient, PastHistorySection.class);
        List<PatientPastObservationHistory> pastObservationHistories = new ArrayList<PatientPastObservationHistory>();
        for (PastHistorySection pastHistorySection : allPastHistorySections)
            pastObservationHistories.addAll(pastHistorySection.getPatientPastObservationHistories());
        return pastObservationHistories;
    }

    @Override
    public Set<SoapModule> getReviewedSoapModuleFor(PatientSoapNote patientSoapNote) {
        Set<SoapModule> reviewedSoapModules = new HashSet<SoapModule>();
        List<SoapSection> allReviewedSoapSections = soapNoteRepository.getReviewedSoapSectionsFor(patientSoapNote);
        for (SoapSection soapSection : allReviewedSoapSections)
            reviewedSoapModules.add(soapSection.getSoapModule());
        return reviewedSoapModules;
    }

    @Override
    public List<PatientAllergy> getActiveAllergies(Patient patient) {
        return soapNoteRepository.getActiveAllergies(patient);
    }

    @Override
    public List<PatientIcd> searchPatientIcdBy(String icdCode, String icdDescription) {
        return soapNoteRepository.searchPatientIcdBy(icdCode, icdDescription);
    }

    @Override
    public List<PatientIcd> getLastSoapNotePatientIcdFor(Patient patient) {
        List<PatientIcd> patientIcds = new ArrayList<PatientIcd>();
        DiagnosisSection diagnosisSection = soapNoteRepository.getLatestDiagnosisSectionFor(patient);
        List<SoapSection> allSections = soapNoteRepository.getSoapSections(DiagnosisSection.class, patient, null);
        List<SoapSection> filteredSections = new ArrayList<SoapSection>();
        for (SoapSection soapSection : allSections)
            if (!diagnosisSection.equals(soapSection)) filteredSections.add(soapSection);
        for (SoapSection soapSection : filteredSections)
            patientIcds.addAll(((DiagnosisSection) soapSection).getIcds());
        return patientIcds;
    }

    @Override
    public List<PatientRx> searchPatientRxBy(String drugGenericName, String drugTradeName, String activeOrInactive) {
        return soapNoteRepository.searchPatientRxBy(drugGenericName, drugTradeName, activeOrInactive);
    }

    @Override
    public List<PatientIcd> getIcdForPatient(Patient patient, String status[], Collection<PatientSoapNote> soapNotes) {
        return soapNoteRepository.getIcdForPatient(patient, status, soapNotes);
    }

    @Override
    public QATemplate getQATemplate(Provider provider, SoapModule soapModule) {
        return providerRepository.getQATempalte(provider, soapModule);
    }

    public List<PatientSoapNote> getLatestPatientSoapNotes(Patient patient, int count, Date date) {
        return soapNoteRepository.getLatestPatientSoapNotes(patient, count, date);
    }

    public ProviderRepository getProviderRepository() {
        return providerRepository;
    }

    @Resource
    @Required
    public void setProviderRepository(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public List<PatientSoapNote> getSoapNotesForSimilarChiefComplaints(Patient patient, String chiefComplaint,
                                                                       Date priorTo) {
        List<PatientChiefComplaint> chiefComplaints = getSimilarChiefComplaints(patient, chiefComplaint, priorTo);
        List<PatientSoapNote> soapNotes = new ArrayList<PatientSoapNote>();
        for (PatientChiefComplaint patientChiefComplaint : chiefComplaints)
            soapNotes.add(patientChiefComplaint.getSoapSection().getSoapNote());
        return soapNotes;
    }

    @Override
    public List<QATemplate> getQATemplatesFor(SoapModule soapModule) {
        return soapNoteRepository.getQATemplatesFor(soapModule);
    }

    public List<DiagnosisSection> getAllDiagnosisSections(Practice practice) {
        return soapNoteRepository.getAllDiagnosisSections(getDiagnosisModule(practice, DiagnosisSection.MODULE_NAME));
    }

    public List<PatientSoapNote> getDistinctSoapNotesForIcdCodeSet(IcdCodeSet codeSet, Practice practice) {
        List<PatientSoapNote> soapNotes = new ArrayList<PatientSoapNote>();
        if (codeSet != null && codeSet.getIcdElements() != null) {
            List<DiagnosisSection> diagnosisSections = getAllDiagnosisSections(practice);
            for (DiagnosisSection section : diagnosisSections) {
                for (PatientIcd icd : section.getIcds()) {
                    if (codeSet.getIcdElements().contains(icd.getIcdElement())) soapNotes.add(section.getSoapNote());
                }
            }
        }
        return soapNotes;
    }

    public SoapModule getDiagnosisModule(Practice practice, String moduleName) {
        return soapNoteRepository.getDiagnosisModule(practice, moduleName);
    }

    @Override
    public SOAPPlan isSoapPlanningDone(PatientSoapNote soapNote, String followUpFor) {
        RecommendationSection section = (RecommendationSection) getSoapSection(soapNote, RecommendationSection.class);
        SOAPPlan plan = section.retrieveSoapPlan(followUpFor);
        if (plan != null && plan.isDone()) return plan;
        return null;
    }

    public SOAPPlan isSoapPlanningDone(Patient patient, Date after, String followUpFor) {
        List<SOAPPlan> plans = soapNoteRepository.getSoapPlansForPatient(patient, after, followUpFor);
        for (SOAPPlan plan : plans)
            if (plan.isDone()) return plan;
        return null;
    }

    public PatientVitalSign getLatestVitalSignRecording(String vitalSignName, Date after, Patient patient) {
        PatientVitalSignSet vitalSignSet = soapNoteRepository.getLatestVitalSignRecordings(vitalSignName, after, patient);
        return vitalSignSet == null ? null : vitalSignSet.getPatientVitalSign(vitalSignName);
    }

    @Override
    public IcdElement getIcdElement(String problemCode) {
        return soapNoteRepository.getIcdElement(problemCode);
    }

    @Override
    public SoapSection getPreviousSoapSection(PatientSoapNote currentSoapNote, Class<?> klass) {
        return soapNoteRepository.getPreviousSoapSection(currentSoapNote, klass);
    }

    public List<PatientIcd> getIcdForPatient(Patient patient, String statusDescription, PatientSoapNote soapNote) {
        return soapNoteRepository.getIcdForPatient(patient, statusDescription, soapNote);
    }

    @Override
    public List<PatientRx> getPatientRxs(Patient patient, Collection<PatientSoapNote> patientSoapNotes) {
        return soapNoteRepository.getPatientRxs(patient, patientSoapNotes);
    }

    @Override
    public List<SoapAddendum> getAddendumsFor(PatientSoapNote soapNote) {
        return soapNoteRepository.getAddendumsFor(soapNote);
    }

    @Override
    public List<PatientSoapNote> getAllSoapNotesExcludingCurrent(PatientSoapNote soapNote) {
        return soapNoteRepository.getAllSoapNotesExcludingCurrent(soapNote);
    }

    @Override
    public PatientSoapNote getLatestSoapNoteFor(Patient patient, Date date) {
        return soapNoteRepository.getLatestSoapNoteFor(patient, date);
    }

    public CommonCrudService getCommonCrudService() {
        return commonCrudService;
    }

    @Resource
    @Required
    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    @Override
    public <T> List<T> getAllSoapRecords(Patient patient, List<PatientSoapNote> soapNoteList, Class<T> klass) {
        return soapNoteRepository.getAllSoapRecords(patient, soapNoteList, klass);
    }

    @Override
    public <T extends SoapSection> List<T> getLatestSoapSections(Patient patient, int count, Class<T> klass) {
        return soapNoteRepository.getLatestSoapSections(patient, count, klass);
    }

    public <T> List<T> getAllSoapRecordsExcludingCurrentSection(Patient patient, SoapSection currentSection,
                                                                Class<T> klass) {
        return soapNoteRepository.getAllSoapRecordsExcludingCurrentSection(patient, currentSection, klass);
    }

    @Override
    public List<PatientRx> getAllMedicationsExcludingCurrentSection(MedicationHistorySection historySection,
                                                                    RxSection rxSection, Patient patient) {
        return soapNoteRepository.getAllMedicationsExcludingCurrentSection(historySection, rxSection, patient);
    }

    public List<PatientIcd> getAllPatientIcdExcludingCurrentSection(ProblemListSection section, Patient patient) {
        return soapNoteRepository.getAllPatientIcdExcludingCurrentSection(section, patient);
    }

    @Override
    public List<PatientSoapNote> getSoapNotesForSimilarChiefComplaints(Patient patient, String chiefComplaint, Date priorTo, ChiefComplainSection chiefComplainSection) {
        return null;
    }

    @Override
    public List<VitalSignReading> getAllVitalSignReadings(Patient patient) {
        return soapNoteRepository.getAllVitalSignReadings(patient);
    }

    @Override
    public <T extends SoapSection> List<T> getLatestPatientRecords(Patient patient, String fieldName, Class<T> klass, int count) {
        List<T> sections = getLatestSoapSections(patient, count, klass);
        List<T> soapRecords = new ArrayList<T>();
        for (T t : sections)
            soapRecords.addAll((Collection<T>) UtilReflection.getNestedFieldValue(t, fieldName));
        return soapRecords;
    }

    @Override
    public <T extends IdGeneratingBaseEntity> List<T> getAllPatientClinicalRecords(Patient patient, Class<T> klass) {
        return soapNoteRepository.getAllPatientClinicalRecords(patient, klass);
    }

    public PatientSocialHistory getLatestPatientSocialHistory(Patient patient) {
        return soapNoteRepository.getLatestPatientSocialHistory(patient);
    }

    public PatientSocialHistory getOldestPatientSocialHistory(Patient patient) {
        return soapNoteRepository.getOldestPatientSocialHistory(patient);
    }

    public PatientSocialHistory getPreceedingPatientSocialHistory(SocialHistorySection socialHistorySection) {
        return soapNoteRepository.getPreceedingPatientSocialHistory(socialHistorySection.getPatientSocialHistory());
    }

    @Override
    public PatientSocialHistory getSucceedingPatientSocialHistory(SocialHistorySection socialHistorySection) {
        return soapNoteRepository.getSucceedingPatientSocialHistoryExcludingCurrentSection(socialHistorySection.getPatientSocialHistory());
    }

    public List<PatientSocialHistory> getPreviousPatientSocialHistories(PatientSocialHistory patientSocialHistory) {
        return soapNoteRepository.getPreviousPatientSocialHistories(patientSocialHistory);
    }

    public PatientSocialHistory getPreviousSectionPatientSocialHistory(SocialHistorySection currentSection) {
        SocialHistorySection previousSection = (SocialHistorySection) getPreviousSocialHistorySection(currentSection);
        if (previousSection == null || currentSection.equals(previousSection))
            return null;
        return previousSection.getPatientSocialHistory() == null ? getPreviousSectionPatientSocialHistory(previousSection) : previousSection.getPatientSocialHistory();
    }

    public PatientSocialHistory getSucceedingSectionPatientSocialHistory(SocialHistorySection currentSection) {
        SocialHistorySection succeedingSection = (SocialHistorySection) getSucceedingSoapSection(currentSection.getSoapNote(), SocialHistorySection.class);
        if (succeedingSection == null || currentSection.equals(succeedingSection))
            return null;
        return succeedingSection.getPatientSocialHistory() == null ? getSucceedingSectionPatientSocialHistory(succeedingSection) : succeedingSection.getPatientSocialHistory();
    }


    @Override
    public SoapSection getSucceedingSoapSection(PatientSoapNote currentSoapNote, Class<?> klass) {
        return soapNoteRepository.getSucceedingSoapSection(currentSoapNote, klass);
    }

    @Override
    public SocialHistorySection getNextSocialHistorySection(SocialHistorySection currentSection) {
        return soapNoteRepository.getNextSocialHistorySection(currentSection);
    }

    @Override
    public SocialHistorySection getPreviousSocialHistorySection(SocialHistorySection currentSection) {
        return soapNoteRepository.getPreviousSocialHistorySection(currentSection);
    }

    @Override
    public List<SoapModule> getSoapModules(SlotType visitType, Provider provider, PatientSoapNote patientSoapNote) {
        if ((patientSoapNote.getId() != null) && (!STATUS.CHECKEDOUT.equals(patientSoapNote.getStatus())))
            return soapNoteRepository.getSoapModulesUsed(patientSoapNote);
        Set<SoapModule> modules = new HashSet<SoapModule>();
        List<VisitTypeSoapModule> visitTypeSoapModules = soapNoteRepository.getVisitTypeSoapModule(visitType, provider);
        for (VisitTypeSoapModule visitTypeSoapModule : visitTypeSoapModules)
            modules.addAll(visitTypeSoapModule.getModules());
        List<SoapModule> sortedModules = new ArrayList<SoapModule>(modules);
        Collections.sort(sortedModules);
        return sortedModules;
    }

    @Override
    public List<PatientAllergy> getPatientAllergyNotEqToStatus(Patient patient, Enumeration enu) {
        // TODO Auto-generated method stub
        return soapNoteRepository.getPateintAllegiesNotEqToStatus(patient, enu);
    }

    @Override
    public List<PatientChronicDisease> getAllPatientChronicDiseaseFor(Patient patient) {
        return soapNoteRepository.getAllPatientChronicDiseaseFor(patient);
    }

    public List<PatientSoapNote> getMlcSoapNoteByCriteria(Provider provider, Date fromDate, Date thruDate) {
        return soapNoteRepository.getMlcSoapNoteByCriteria(provider, fromDate, thruDate);
    }

    @Override
    public List<AcctgTransactionEntry> getAcctgTransEntryByCriteria(Date fromDate, Date thruDate, Long providerId, Long patientId, Long encounterId, Long invoiceId,
                                                                    String speciality, String referral, boolean isExternalPatientTransactionRequire) {
        return soapNoteRepository.getAcctgTransEntryByCriteria(fromDate, thruDate, providerId, patientId, encounterId, invoiceId, speciality, referral, isExternalPatientTransactionRequire);
    }

    @Override
    public boolean checkIfNoKnownAllergy(Patient patient){
        return soapNoteRepository.checkIfNoKnownAllergy(patient);
    }

    @Override
    public List<PatientRx> getAllPatientActivePrescription(Patient patient) {
        List<PatientRx> patientRxList = this.getAllPatientClinicalRecords(patient, PatientRx.class);
        List<PatientRx> activeRxList = new ArrayList<PatientRx>();
        for(PatientRx patientRx : patientRxList){
            Date startDate = patientRx.getStartDate();
            Integer noOfDays = patientRx.getNumberOfDays() != null ? Integer.valueOf(patientRx.getNumberOfDays()) : 0;
            Date endDate = UtilDateTime.addDaysToDate(startDate, noOfDays);
            Date currentDate = new Date();
            if(endDate.compareTo(currentDate) >= 0){
                activeRxList.add(patientRx);
            }
        }
        return activeRxList;
    }

    @Override
    public OrderDto prepairedPharmacyPrescription(List<PatientRx> patientRxs) {
        List<Map<String, Object>> practices = namedParameterJdbcTemplate.queryForList(QUERY_TO_FETCH_PRACTICE_DETAILS, new TreeMap<String, Object>());
        Map<String, Object> practice = practices.get(0);
        PatientSoapNote soapNote = patientRxs.get(0).getRxSection().getSoapNote();
        RxSection rxSection = (RxSection) soapNoteRepository.getSoapSection(soapNote, RxSection.class);
        String hisModuleName = null;
        if(soapNote.getSelectedHisModuleId() != null)
            hisModuleName = AfyaServiceConsumer.getHISModuleNameByBenefitId(soapNote.getSelectedHisModuleId());
        if(rxSection.getPharmacyTenantId() != null && !rxSection.getPharmacyTenantId().equals("Default")) {
            Patient patient = soapNote.getPatient();
            //patient.setPatientType("CASH");
            boolean mobileNumberVisibleForDelivery = checkIfMaskingMobileNumberNeeded(patient.getAfyaId());
            List<PrescriptionLineItem> prescriptionLineItems = new ArrayList<PrescriptionLineItem>();
            PrescriptionLineItem prescriptionLineItem = null;
            for(PatientRx patientRx : patientRxs){
                prescriptionLineItem = new PrescriptionLineItem();
                prescriptionLineItem.setPropertiesFromPatientRx(patientRx);
                prescriptionLineItem.setDrugCategory("PrescDrugs");
                prescriptionLineItems.add(prescriptionLineItem);
            }
            String benefitId=null;
            String groupId=null;
            if(UtilValidator.isNotEmpty(patient.getPatientInsurances())) {
                PatientInsurance patientInsurance = patient.getPatientInsurances().iterator().next();
                benefitId=patientInsurance.getBenefitId();
                groupId=patientInsurance.getGroupId();
            }

            BigDecimal corporateCopay = null;
            String corporateCopayType = null;
            String corporatePrimaryPayer = null;
            if( "CORPORATE".equals(patient.getPartyType()) ){
                corporateCopay = patient.getPatientCorporate().getCorporateCopay();
                corporateCopayType = patient.getPatientCorporate().getCorporateCopayType();
                corporatePrimaryPayer = patient.getPatientCorporate().getPrimaryPayor();
            }
            String visitId = String.valueOf(soapNote.getSchedule() != null ? soapNote.getSchedule().getLastPatientVisit() != null ? soapNote.getSchedule().getLastPatientVisit().getId() : null : null);
            Date visitDate = soapNote.getSchedule() != null ? soapNote.getSchedule().getLastPatientVisit() != null ? soapNote.getSchedule().getLastPatientVisit().getUpdatedTxTimestamp() : null : null;
            PrescriptionDTO dto = new PrescriptionDTO(practice.get("tenantId").toString(), patient.getAfyaId(), prescriptionLineItems,
                    visitId, visitDate, patient.getFirstName(), patient.getLastName(), patient.getContacts().getMobileNumber(),
                    "CASH", String.valueOf(soapNote.getProvider().getId()), soapNote.getProvider().getProviderName(), practice.get("practiceName").toString(), soapNote.getSelectedHisModuleId(),
                    benefitId, groupId, hisModuleName, "N", patient.getDateOfBirth(), corporateCopay, corporateCopayType, corporatePrimaryPayer, mobileNumberVisibleForDelivery);
            dto.setOrderFromMobileOrPortal(true);
            OrderDto orderdto =  externalServiceClient.handlePrescriptionOrder(dto, rxSection.getPharmacyTenantId());

            if(orderdto != null && UtilValidator.isNotEmpty(orderdto.getOrderId())){
                PharmacyOrder pharmacyOrder = new PharmacyOrder();
                pharmacyOrder.setCurrencyUom(orderdto.getCurrencyUom());
                pharmacyOrder.setOrderId(orderdto.getOrderId());
                pharmacyOrder.setTotalAmount(orderdto.getTotalAmount());
                pharmacyOrder.setPharOrderStatus(PharmacyOrderStatus.ACTIVE);
                pharmacyOrder.setPatient(soapNote.getPatient());
                pharmacyOrder.setPharmacyTennantId(rxSection.getPharmacyTenantId());
                commonCrudService.save(pharmacyOrder);
            }
            return orderdto;
        }
        return null;
    }

    @Override
    public void completeOrder(String orderId, String pharmacyId) {
        externalServiceClient.completeOrder(orderId,pharmacyId);
    }

    @Override
    public void orderPayment(String orderId,String totalAmount, String pharmacyId) {
        externalServiceClient.orderPayment(orderId,totalAmount,pharmacyId);

    }

}
