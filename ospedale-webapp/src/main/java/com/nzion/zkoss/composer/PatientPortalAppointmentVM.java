package com.nzion.zkoss.composer;

import com.nzion.domain.*;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.service.ScheduleService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.*;
import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import java.util.*;

/**
 * Created by Mohan Sharma on 7/7/2015.
 * Privacy Policy Consent implementation
 */
@VariableResolver(DelegatingVariableResolver.class)
public class PatientPortalAppointmentVM {

    @WireVariable
    private CommonCrudService commonCrudService;

    @WireVariable
    private ScheduleService scheduleService;

    private Patient patient;

    private List<Schedule> schedules;

    private boolean checkPast = Boolean.FALSE;

    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view, @BindingParam("patient") Patient patient) {
        Selectors.wireComponents(view, this, true);
        this.patient = patient;
    }

    /*
    * Modified To read consents from portal by Mohan Sharma 22 Aug 15
    * */
    private List<Schedule> refurbishActivePrescriptionBasedOnPrivacyPolicy(List<Schedule> schedules) {
        List<Schedule> refurbishedList = new ArrayList<>();
        UserLogin userLogin = Infrastructure.getUserLogin();
        patient = commonCrudService.findUniqueByEquality(Patient.class, new String[]{"firstName", "lastName", "dateOfBirth", "contacts.mobileNumber"}, new Object[]{patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth(), patient.getContacts().getMobileNumber()});
        //Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsents = patient.getPatientPrivacyPolicyConsents();
        List<Map<String, Object>> patientPrivacyPolicyConsents = RestServiceConsumer.getPatientPrivacyPolicyConsents(patient.getAfyaId());
        if(UtilValidator.isEmpty(patientPrivacyPolicyConsents) || UtilValidator.isEmpty(schedules)){
            //UtilMessagesAndPopups.showError("Privacy policy consent not set for the patient");
            return Collections.EMPTY_LIST;
        }
        Boolean doctorSeeHisPastRecords = getPatientPrivacyPolicyConsentByCriteria(patientPrivacyPolicyConsents, "Can Doctors who are treating you be allowed to see your old records?");
        Boolean doctorSeeOthersPastRecords = getPatientPrivacyPolicyConsentByCriteria(patientPrivacyPolicyConsents, "Can Doctor who is treating you is allowed to see your old records of other doctors?");
        Boolean receptionistSeeRecords = getPatientPrivacyPolicyConsentByCriteria(patientPrivacyPolicyConsents, "Can Receptionist be allowed to see your old records?");
        if(userLogin.hasRole(Roles.PATIENT)){
            refurbishedList.clear();
            refurbishedList.addAll(schedules);
        }
        if(userLogin.hasRole(Roles.RECEPTION) && receptionistSeeRecords){
            refurbishedList.clear();
            refurbishedList.addAll(schedules);
        }
        /*
        * Portal doesnt have this feature so default true
        * */
        //if(userLogin.hasRole(Roles.PROVIDER) && doctorSeeHisPastRecords){
        if(userLogin.hasRole(Roles.PROVIDER) && Boolean.TRUE){
            refurbishedList.clear();
            List<Schedule> scheduleList = commonCrudService.findByEquality(Schedule.class, new String[]{"patient.id", "person.id"}, new Object[]{patient.getId(), userLogin.getPerson().getId()});
            refurbishedList.addAll(schedules);
            refurbishedList.retainAll(scheduleList);
        }
        if(userLogin.hasRole(Roles.PROVIDER) && doctorSeeOthersPastRecords){
            refurbishedList.clear();
            refurbishedList.addAll(schedules);
        }
        return refurbishedList;
    }

    /*private PatientPrivacyPolicyConsent getPatientPrivacyPolicyConsentByCriteria(Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsents, String question) {
        for(PatientPrivacyPolicyConsent patientPrivacyPolicyConsent : patientPrivacyPolicyConsents){
            if(patientPrivacyPolicyConsent.getPrivacyPolicyConsent().getQuestion().equals(question))
                return patientPrivacyPolicyConsent;
        }
        return null;
    }*/

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

    @Command
    @NotifyChange({"schedules"})
    public void getPastPatientAppointments(){
        schedules = scheduleService.getSchedulesForPatient(patient, true, false);
        schedules = refurbishActivePrescriptionBasedOnPrivacyPolicy(schedules);
    }

    @Command
    @NotifyChange({"schedules"})
    public void getCurrentPatientAppointments(){
        schedules = scheduleService.getSchedulesForPatient(patient, false, true);
        schedules = refurbishActivePrescriptionBasedOnPrivacyPolicy(schedules);
    }

    @Command
    @NotifyChange({"schedules"})
    public void something(){
        System.out.println("\n\n\nHello\n\n\n");
    }

    @Command
    @NotifyChange({"schedules", "checkPast"})
    public void onRadioGroupCreate(){
        getPastPatientAppointments();
        checkPast = Boolean.TRUE;
    }

    public CommonCrudService getCommonCrudService() {
        return commonCrudService;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public boolean isCheckPast() {
        return checkPast;
    }

    public void setCheckPast(boolean checkPast) {
        this.checkPast = checkPast;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }
}
