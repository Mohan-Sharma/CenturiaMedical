package com.nzion.zkoss.composer;

import com.nzion.domain.*;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by Mohan Sharma on 3/7/2015.
 */


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class PatientPrivacyPolicyConsentViewModel {

    private static List<PrivacyPolicyConsent> questions = Arrays.asList();

    private Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsents;

    @WireVariable
    private CommonCrudService commonCrudService;

    private Patient patient;

    private Component patientImage;

    private boolean showBookAppointmentWindow = Boolean.FALSE;

    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view, @BindingParam("patient") Patient patient, @BindingParam("patientImage") Component patientImage) {
        Selectors.wireComponents(view, this, true);
        this.patient = patient;
        if(patientImage != null)
            this.patientImage = patientImage;
        questions = commonCrudService.getAll(PrivacyPolicyConsent.class);
        patientPrivacyPolicyConsents = patient.getPatientPrivacyPolicyConsents();
        if(UtilValidator.isEmpty(patientPrivacyPolicyConsents)){
            for(PrivacyPolicyConsent question : questions){
                PatientPrivacyPolicyConsent patientPrivacyPolicyConsent = new PatientPrivacyPolicyConsent();
                patientPrivacyPolicyConsent.setPrivacyPolicyConsent(question);
                patientPrivacyPolicyConsents.add(patientPrivacyPolicyConsent);
            }
        }
        showBookAppointmentWindow = showAppointmentWindowBasedOnRole();
    }

    private boolean showAppointmentWindowBasedOnRole() {
        UserLogin userLogin = Infrastructure.getUserLogin();
        if(userLogin.hasRole(Roles.PATIENT))
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }

    @Command("save")
    @NotifyChange({"patient", "patientPrivacyPolicyConsents"})
    public void save() {
        patient.setPatientPrivacyPolicyConsents(patientPrivacyPolicyConsents);
        commonCrudService.save(patient);
        Events.postEvent("onReload", patientImage, patient);
        UtilMessagesAndPopups.showSuccess();
    }

    public Set<PatientPrivacyPolicyConsent> getPatientPrivacyPolicyConsents() {
        return patientPrivacyPolicyConsents;
    }

    public void setPatientPrivacyPolicyConsents(Set<PatientPrivacyPolicyConsent> patientPrivacyPolicyConsents) {
        this.patientPrivacyPolicyConsents = patientPrivacyPolicyConsents;
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

    public boolean isShowBookAppointmentWindow() {
        return showBookAppointmentWindow;
    }

    public void setShowBookAppointmentWindow(boolean showBookAppointmentWindow) {
        this.showBookAppointmentWindow = showBookAppointmentWindow;
    }
}