package com.nzion.dto;

import com.nzion.domain.Patient;
import com.nzion.domain.PatientInsurance;
import com.nzion.domain.Provider;
import com.nzion.domain.SoapNoteType;
import com.nzion.domain.billing.Invoice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 5/13/15
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientInvoiceDto {

    private Patient patient;

    private List<PatientInvoiceItem> patientInvoiceItems;

    private HisModuleDto hisModuleDto;

    private PatientInsurance patientInsurance;

    private Invoice oldInvoice;

    private Provider defaultConsultant;

    private boolean isFirstTimeResitration;


    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public List<PatientInvoiceItem> getPatientInvoiceItems() {
        if(patientInvoiceItems == null)
            patientInvoiceItems = new ArrayList<PatientInvoiceItem>();
        return patientInvoiceItems;
    }

    public void setPatientInvoiceItems(List<PatientInvoiceItem> patientInvoiceItems) {
        this.patientInvoiceItems = patientInvoiceItems;
    }

    public HisModuleDto getHisModuleDto() {
        return hisModuleDto;
    }

    public void setHisModuleDto(HisModuleDto hisModuleDto) {
        this.hisModuleDto = hisModuleDto;
    }

    public PatientInsurance getPatientInsurance() {
        return patientInsurance;
    }

    public void setPatientInsurance(PatientInsurance patientInsurance) {
        this.patientInsurance = patientInsurance;
    }

    public Invoice getOldInvoice() {
        return oldInvoice;
    }

    public void setOldInvoice(Invoice oldInvoice) {
        this.oldInvoice = oldInvoice;
    }

    public Provider getDefaultConsultant() {
        return defaultConsultant;
    }

    public void setDefaultConsultant(Provider defaultConsultant) {
        this.defaultConsultant = defaultConsultant;
    }

    public boolean isFirstTimeResitration() {
        return isFirstTimeResitration;
    }

    public void setFirstTimeResitration(boolean isFirstTimeResitration) {
        this.isFirstTimeResitration = isFirstTimeResitration;
    }
}
