package com.nzion.zkoss.composer;

import java.util.*;

import com.nzion.util.*;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.nzion.domain.Patient;
import com.nzion.domain.PatientPrivacyPolicyConsent;
import com.nzion.domain.PharmacyOrder;
import com.nzion.domain.Roles;
import com.nzion.domain.UserLogin;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.dto.OrderDto;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;

/**
 * Modified by Mohan Sharma on 7/7/2015.
 * Privacy Policy Consent implementation
 */
@VariableResolver(DelegatingVariableResolver.class)
public class PatientActivePrescriptionVM {

    @WireVariable
    private CommonCrudService commonCrudService;

    @WireVariable
    private SoapNoteService soapNoteService;

    private Patient patient;

    private List<PatientRx> medications;

    private OrderDto dto;


    @Wire("#activePrescriptionListBox")
    private Listbox activePrescriptionListBox;


    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view, @BindingParam("patient") Patient patient) {
        Selectors.wireComponents(view, this, true);
        this.patient = patient;
        medications = soapNoteService.getAllPatientActivePrescription(patient);
        List<PatientRx> refurbishedList = refurbishActivePrescriptionBasedOnPrivacyPolicy(medications);
        //medications.clear();
        medications.addAll(refurbishedList);
    }

    /*
    * Modified To read consents from portal by Mohan Sharma 22 Aug 15
    * */
    private List<PatientRx> refurbishActivePrescriptionBasedOnPrivacyPolicy(List<PatientRx> allMedications) {
        List<PatientRx> refurbishedList = new ArrayList<>();
        UserLogin userLogin = Infrastructure.getUserLogin();
        patient = commonCrudService.findUniqueByEquality(Patient.class, new String[]{"firstName", "lastName", "dateOfBirth", "contacts.mobileNumber"}, new Object[]{patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth(), patient.getContacts().getMobileNumber()});
        //Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsents = patient.getPatientPrivacyPolicyConsents();
        List<Map<String, Object>> patientPrivacyPolicyConsents = RestServiceConsumer.getPatientPrivacyPolicyConsents(patient.getAfyaId());
        if(UtilValidator.isEmpty(patientPrivacyPolicyConsents) || UtilValidator.isEmpty(allMedications)){
            //UtilMessagesAndPopups.showError("Privacy policy consent not set for the patient");
            return Collections.EMPTY_LIST;
        }
        Boolean doctorSeeHisPastRecords = getPatientPrivacyPolicyConsentByCriteria(patientPrivacyPolicyConsents, "Can Doctors who are treating you be allowed to see your old records?");
        Boolean doctorSeeOthersPastRecords = getPatientPrivacyPolicyConsentByCriteria(patientPrivacyPolicyConsents, "Can Doctor who is treating you is allowed to see your old records of other doctors?");
        Boolean receptionistSeeRecords = getPatientPrivacyPolicyConsentByCriteria(patientPrivacyPolicyConsents, "Can Receptionist be allowed to see your old records?");
        if(userLogin.hasRole(Roles.PATIENT)){
            refurbishedList.clear();
            refurbishedList.addAll(allMedications);
        }
        if(userLogin.hasRole(Roles.RECEPTION) && receptionistSeeRecords){
            refurbishedList.clear();
            refurbishedList.addAll(allMedications);
        }
        /*
        * Portal doesnt have this feature so default true
        * */
        //if(userLogin.hasRole(Roles.PROVIDER) && doctorSeeHisPastRecords){
        if(userLogin.hasRole(Roles.PROVIDER) && Boolean.TRUE){
            refurbishedList.clear();
            List<PatientRx> patientRxList = commonCrudService.findByEquality(PatientRx.class, new String[]{"patient.id","provider.id"}, new Object[]{patient.getId(), userLogin.getPerson().getId()});
            for(Iterator iterator = patientRxList.iterator(); iterator.hasNext(); ){
                PatientRx patientRx = (PatientRx)iterator.next();
                boolean active = com.nzion.util.UtilDateTime.isFirstDateAfterSecondDate(new Date(), patientRx.getEndDate());
                if(active)
                    iterator.remove();
            }
            refurbishedList.addAll(allMedications);
            refurbishedList.retainAll(patientRxList);
        }
        if(userLogin.hasRole(Roles.PROVIDER) && doctorSeeOthersPastRecords){
            refurbishedList.clear();
            refurbishedList.addAll(allMedications);
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

    public SoapNoteService getSoapNoteService() {
        return soapNoteService;
    }

    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

    public List<PatientRx> getMedications() {
        return medications;
    }

    public void setMedications(List<PatientRx> medications) {
        this.medications = medications;
    }

    @Command
    public void openPayment(){
        OrderDto dto = createOrder();
        if(dto != null)
            Executions.createComponents("/patient/activePrescriptionPay.zul", null, UtilMisc.toMap("controller",this,"orderDto",dto));
    }

    @Command
    public OrderDto createOrder(){
        Set<Listitem> listitems = activePrescriptionListBox.getSelectedItems();
        if(UtilValidator.isEmpty(listitems)){
            UtilMessagesAndPopups.showError("Please select atleast one prescription");
            return null;
        }
        Iterator<Listitem> iterator = listitems.iterator();
        List<PatientRx> patientRxs = new ArrayList<PatientRx>();
        PatientRx lastPatientRx = null;
        while(iterator.hasNext()){

            Listitem li = iterator.next();
            PatientRx patientRx = (PatientRx) li.getValue();
            if( lastPatientRx != null && !lastPatientRx.getRxSection().getPharmacyTenantId().equals(patientRx.getRxSection().getPharmacyTenantId())){
                UtilMessagesAndPopups.showError("Drugs selected for multiple Pharmacy. Please select drugs for single Pharmacy and place order");
                return null;
            }
            patientRxs.add(patientRx);
        }
        dto = soapNoteService.prepairedPharmacyPrescription(patientRxs);
        return dto;
        //UtilMessagesAndPopups.showSuccess();
    }

    public void payOrder(){
        PharmacyOrder pharmacyOrder = commonCrudService.findUniqueByEquality(PharmacyOrder.class, new String[]{"orderId","patient"}, new Object[]{dto.getOrderId(),patient});
        soapNoteService.orderPayment(dto.getOrderId(),pharmacyOrder.getTotalAmount().toString(),pharmacyOrder.getPharmacyTennantId());
        UtilMessagesAndPopups.showSuccess();
    }


}
