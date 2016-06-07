package com.nzion.zkoss.composer;

import com.nzion.domain.*;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.util.EncounterSearchResult;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.*;
import com.nzion.view.EncounterSearchValueObject;
import com.nzion.view.component.GenericAscendingComparator;
import com.nzion.view.component.GenericDescendingCompartor;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listheader;

import java.util.*;

import static com.nzion.domain.util.EncounterSearchResult.mergeResults;

/*
* Modified by Mohan Sharma
* display encounter according to privacy consent
* */

public class EncounterController extends OspedaleAutowirableComposer {

    private EncounterSearchValueObject encounterSearchValueObject;

    private List<PatientSoapNote> patientSoapNotes = new ArrayList<PatientSoapNote>();

    private SoapNoteService soapNoteService;

    private Map<String, List<PatientSoapNote>> patientSoapNoteMap = new HashMap<String, List<PatientSoapNote>>();

    private Set<EncounterSearchResult> encounterSearchResults;

    private boolean showPanelTitle = true; // Whether to display the panel title as "Results";as encounters.zul included

    // in view patient page and encounter search page

    private Set<PatientSoapNote> selectedSoapNotes;

    private CommonCrudService commonCrudService;

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
        encounterSearchValueObject = new EncounterSearchValueObject();
    }

    public List<PatientSoapNote> searchEncounter() {
        if (encounterSearchValueObject.checkDirty()) {
            UtilMessagesAndPopups.showError(Labels.getLabel("nosearchcondition"));
            return Collections.emptyList();
        }
        encounterSearchResults = soapNoteService.searchEncounterFor(encounterSearchValueObject);
        List<PatientSoapNote> patientSoapNoteList = mergeResults(encounterSearchResults);
        if(UtilValidator.isNotEmpty(encounterSearchValueObject.getPatient())) {
            return refurnishBasedOnUserRoleAndPrivacyConsent(patientSoapNoteList, encounterSearchValueObject.getPatient());
        }else {
            return patientSoapNoteList;
        }
    }
    /*
    * Modified To read consents from portal by Mohan Sharma 22 Aug 15
    * */
    private List<PatientSoapNote> refurnishBasedOnUserRoleAndPrivacyConsent(List<PatientSoapNote> patientSoapNoteList, Patient patient) {
        List<PatientSoapNote> refurbishedList = new ArrayList<>();
        UserLogin userLogin = Infrastructure.getUserLogin();
        patient = commonCrudService.findUniqueByEquality(Patient.class, new String[]{"firstName", "lastName", "dateOfBirth", "contacts.mobileNumber"}, new Object[]{patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth(), patient.getContacts().getMobileNumber()});
        //Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsents = patient.getPatientPrivacyPolicyConsents();
        List<Map<String, Object>> consents = RestServiceConsumer.getPatientPrivacyPolicyConsents(patient.getAfyaId());
        if(UtilValidator.isEmpty(consents) || UtilValidator.isEmpty(patientSoapNoteList)){
            //UtilMessagesAndPopups.showError("Privacy policy consent not set for the patient");
            return Collections.EMPTY_LIST;
        }
        Boolean doctorSeeHisPastRecords = getPatientPrivacyPolicyConsentByCriteria(consents, "Can Doctors who are treating you be allowed to see your old records?");
        Boolean doctorSeeOthersPastRecords = getPatientPrivacyPolicyConsentByCriteria(consents, "Can Doctor who is treating you is allowed to see your old records of other doctors?");
        Boolean receptionistSeeRecords = getPatientPrivacyPolicyConsentByCriteria(consents, "Can Receptionist be allowed to see your old records?");
        if(userLogin.hasRole(Roles.PATIENT)){
            refurbishedList.clear();
            refurbishedList.addAll(patientSoapNoteList);
        }
        if(userLogin.hasRole(Roles.RECEPTION) && receptionistSeeRecords){
            refurbishedList.clear();
            refurbishedList.addAll(patientSoapNoteList);
        }
        /*
        * Portal doesnt have this feature so default true
        * */
        //if(userLogin.hasRole(Roles.PROVIDER) && doctorSeeHisPastRecords){
        if(userLogin.hasRole(Roles.PROVIDER) && Boolean.TRUE){
            refurbishedList.clear();
            List<PatientSoapNote> patientSoapNotes = commonCrudService.findByEquality(PatientSoapNote.class, new String[]{"patient.id","provider.id"}, new Object[]{patient.getId(), userLogin.getPerson().getId()});
            refurbishedList.addAll(patientSoapNoteList);
            refurbishedList.retainAll(patientSoapNotes);
        }
        if(userLogin.hasRole(Roles.PROVIDER) && doctorSeeOthersPastRecords){
            refurbishedList.clear();
            refurbishedList.addAll(patientSoapNoteList);
        }
        return refurbishedList;
    }

    private boolean getPatientPrivacyPolicyConsentByCriteria(List<Map<String, Object>> patientPrivacyPolicyConsents, String question) {
        for(Map<String, Object> patientPrivacyPolicyConsent : patientPrivacyPolicyConsents){
            if(patientPrivacyPolicyConsent.get("question").equals(question)){
                if(UtilValidator.isEmpty(patientPrivacyPolicyConsent.get("answer")))
                    return Boolean.FALSE;
                return Boolean.valueOf(patientPrivacyPolicyConsent.get("answer").toString());
            }
        }
        return Boolean.FALSE;
    }

    public SoapNoteService getSoapNoteService() {
        return soapNoteService;
    }

    public EncounterSearchValueObject getEncounterSearchValueObject() {
        return encounterSearchValueObject;
    }

    public void setEncounterSearchValueObject(EncounterSearchValueObject encounterSearchValueObject) {
        this.encounterSearchValueObject = encounterSearchValueObject;
    }

    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

    public List<PatientSoapNote> getPatientSoapNotes() {
        return patientSoapNotes;
    }

    public void setPatientSoapNotes(List<PatientSoapNote> patientSoapNotes) {
        this.patientSoapNotes = patientSoapNotes;
    }

    public Map<String, List<PatientSoapNote>> getPatientSoapNoteMap() {
        return patientSoapNoteMap;
    }

    public void groupSoapNotes(String groupBy) {
        patientSoapNoteMap = new HashMap<String, List<PatientSoapNote>>();
        if (UtilValidator.isEmpty(encounterSearchResults)) return;
        for (EncounterSearchResult encounterSearchResult : encounterSearchResults)
            if (groupBy.equalsIgnoreCase(encounterSearchResult.getGroupingType())) {
                patientSoapNoteMap.put(encounterSearchResult.getGroupingValue(), new ArrayList<PatientSoapNote>(
                        encounterSearchResult.getPatientSoapNotes()));
            }
    }


    public Set<EncounterSearchResult> getEncounterSearchResults() {
        return encounterSearchResults;
    }

    public void setEncounterSearchResults(Set<EncounterSearchResult> encounterSearchResults) {
        this.encounterSearchResults = encounterSearchResults;
    }

    public boolean isShowPanelTitle() {
        return showPanelTitle;
    }

    public void setShowPanelTitle(boolean showPanelTitle) {
        this.showPanelTitle = showPanelTitle;
    }

    public void enableAscendingSort(Listheader listheader, String fieldName) {
        GenericAscendingComparator comparator = new GenericAscendingComparator(fieldName);
        listheader.setSortAscending(comparator);
    }

    public void enableDescendingSort(Listheader listheader, String fieldName) {
        GenericDescendingCompartor compartor = new GenericDescendingCompartor(fieldName);
        listheader.setSortDescending(compartor);
    }

    public Set<PatientSoapNote> getSelectedSoapNotes() {
        return selectedSoapNotes;
    }

    public void setSelectedSoapNotes(Set<PatientSoapNote> selectedSoapNotes) {
        this.selectedSoapNotes = selectedSoapNotes;
    }

    public CommonCrudService getCommonCrudService() {
        return commonCrudService;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    private static final long serialVersionUID = 1L;
}
