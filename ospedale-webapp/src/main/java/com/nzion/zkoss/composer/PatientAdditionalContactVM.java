package com.nzion.zkoss.composer;

import java.util.*;

import com.nzion.util.*;
import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
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
import org.zkoss.zul.Window;


@VariableResolver(DelegatingVariableResolver.class)
public class PatientAdditionalContactVM extends OspedaleAutowirableComposer{

    @WireVariable
    private CommonCrudService commonCrudService;

    @Wire("#patientAdditionalContactWindow")
    private Window patientInsuranceWin;

    private Patient patient;

    List<Map<String,Object>> contactMapList1 = new ArrayList<>();

    List<Map<String,Object>> contactMapList;

    private String contactValue;
    private String contactType;

    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view, @BindingParam("patient") Patient patient) {
        Selectors.wireComponents(view, this, true);
        this.patient = patient;
        contactMapList = RestServiceConsumer.getPatientContactsFromAfyaId(patient.getAfyaId());

        Map<String,Object> map1 = new HashMap();
        contactMapList1.add(map1);
    }

    /*public void soapModuleClicked(Event event) {
        Component comp = event.getTarget().getFellowIfAny("sentenceAreaDiv");
        if (comp != null) {
            comp.setVisible(true);
        }
        root.getFellow("sentenceAreaDiv");
    }*/
    public void add() {
        //Events.postEvent("onReloadRequest", patientInsuranceWin, null);
       boolean status = RestServiceConsumer.addPatientAlternateContact(patient,contactType, contactValue);
        if (status){
            UtilMessagesAndPopups.showSuccess("Successfully added alternate contact");
        } else {
            UtilMessagesAndPopups.showError("Failed to add alternate contact");
        }
       //Events.postEvent("onReloadRequest", patientInsuranceWin, null);
    }

    public List<Map<String, Object>> getContactMapList1() {
        return contactMapList1;
    }

    public void setContactMapList1(List<Map<String, Object>> contactMapList1) {
        this.contactMapList1 = contactMapList1;
    }

    public List<Map<String, Object>> getContactMapList() {
        return contactMapList;
    }

    public void setContactMapList(List<Map<String, Object>> contactMapList) {
        this.contactMapList = contactMapList;
    }

    @Command("addItem")
    @NotifyChange("contactMapList1")
    public void addItem() {
        if (contactMapList1.size() != 1) {
            Map<String, Object> map = new HashMap();
            contactMapList1.add(map);
        }
    }

    @Command("removeItem")
    @NotifyChange("contactMapList1")
    public void removeItem(@BindingParam("patientInvoiceItem") Map patientInvoiceItem) {
        contactMapList1.remove(patientInvoiceItem);
    }

    public String getContactValue() {
        return contactValue;
    }

    public void setContactValue(String contactValue) {
        this.contactValue = contactValue;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }
    public void remove(Map map){
        boolean result = RestServiceConsumer.updatePatientAlternateContact(map);
        if (result){
            UtilMessagesAndPopups.showSuccess("Successfully removed alternate contact");
        } else {
            UtilMessagesAndPopups.showError("Failed to remove alternate contact");
        }
    }
}
