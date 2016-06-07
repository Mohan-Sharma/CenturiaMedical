package com.nzion.view.component;

import org.zkoss.zk.ui.Component;
import org.zkoss.zkplus.databind.TypeConverter;
import org.zkoss.zul.Listcell;

import com.nzion.domain.emr.soap.ChiefComplainSection;
import com.nzion.domain.emr.soap.PatientChiefComplaint;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.service.SoapNoteService;
import com.nzion.util.Infrastructure;

public class EncounterChiefComplaintConverter implements TypeConverter{

    private SoapNoteService soapNoteService = Infrastructure.getSpringBean("soapNoteService");

    @Override
    public Object coerceToBean(Object arg0, Component arg1) {
        return null;
    }

    @Override
    public Object coerceToUi(Object object, Component component) {
        PatientSoapNote soapNote = (PatientSoapNote)object;
        StringBuilder chiefComplaintBuffer = new StringBuilder();
        if(soapNote!=null){
            ChiefComplainSection chiefComplainSection = (ChiefComplainSection) soapNoteService.getSoapSection(soapNote, ChiefComplainSection.class);
            if(chiefComplainSection!=null){
                for(PatientChiefComplaint patientChiefComplaint :chiefComplainSection.getPatientChiefComplaints())
                    chiefComplaintBuffer.append(patientChiefComplaint.getChiefComplaint()).append(',');
                chiefComplaintBuffer.deleteCharAt(chiefComplaintBuffer.length()-1);
                ((Listcell)component).setLabel(chiefComplaintBuffer.toString());
            }
        }
        return null;
    }


}
