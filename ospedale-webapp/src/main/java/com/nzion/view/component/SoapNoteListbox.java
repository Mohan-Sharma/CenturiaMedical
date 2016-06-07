package com.nzion.view.component;

import java.util.Collection;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

import com.nzion.domain.emr.soap.PatientCpt;
import com.nzion.domain.emr.soap.PatientIcd;
import com.nzion.domain.emr.soap.PatientVitalSignSet;
import com.nzion.zkoss.composer.PatientSoapNoteController;

public class SoapNoteListbox extends Listbox {

    private Collection<Object> list;

    private PatientSoapNoteController patientSoapNoteController;

    public Collection<Object> getList() {
        return list;
    }

    public void setList(Collection<Object> list) {
        this.list = list;
    }

    public void removeSelectedChild() {
        final Listitem listitem = this.getSelectedItem();
        if (listitem != null) {
            Messagebox.show("Do you want to delete this record?",
                    "Delete Confirm?", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                    new org.zkoss.zk.ui.event.EventListener() {
                        public void onEvent(Event evt) throws Exception {
                            if ("onYes".equals(evt.getName())) {
                                try {
                                    Object object = listitem.getValue();
                                    list.remove(object);
                                    if (object instanceof PatientIcd)
                                        patientSoapNoteController
                                                .setEventMessage("Patient ICD Record deleted from Diagnosis Section.");
                                    else if (object instanceof PatientCpt)
                                        patientSoapNoteController
                                                .setEventMessage("Patient CPT Record deleted from Diagnosis Section.");

                                    else if (object instanceof PatientVitalSignSet)
                                        patientSoapNoteController.setEventMessage("Vital Sign Record deleted.");

                                    listitem.detach();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                return;
                            }
                        }
                    });


        }
    }

    public PatientSoapNoteController getPatientSoapNoteController() {
        return patientSoapNoteController;
    }

    public void setPatientSoapNoteController(PatientSoapNoteController patientSoapNoteController) {
        this.patientSoapNoteController = patientSoapNoteController;
    }

    private static final long serialVersionUID = 1L;

}
