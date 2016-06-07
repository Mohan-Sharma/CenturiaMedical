package com.nzion.view.component;

import com.nzion.domain.SoapNoteType;
import com.nzion.domain.emr.Cpt;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.domain.emr.lab.LabTest;
import com.nzion.domain.emr.lab.LabTestPanel;
import com.nzion.domain.emr.lab.LabTestProfile;
import com.nzion.domain.emr.lab.Radiology;
import com.nzion.domain.emr.soap.PatientLabOrder;
import com.nzion.dto.PatientInvoiceItem;
import com.nzion.repository.ComponentRepository;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.util.Constants;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilDisplay;
import com.nzion.util.UtilValidator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mohan Sharma on 27/May/2015.
 */
public class LabComponentAutoSuggestionBox extends LookupBox{

    private String[] searchFields;

    private ComponentRepository componentRepository = (ComponentRepository) Infrastructure.getSpringBean("componentRepository");

    private CommonCrudRepository commonCrudRepository = (CommonCrudRepository) Infrastructure.getSpringBean("commonCrudRepository");

    private PatientLabOrder patientLabOrder;

    public LabComponentAutoSuggestionBox(){
        setButtonVisible(false);
        setAutodrop(true);
        setHideListHeader(true);
        setHidePagination(true);
        addEventListener("onChanging", new OnChangingEventListener());
    }


    @Override
    public void setSearchcolumns(String sc) {
        super.setSearchcolumns(sc);
        searchFields = new String[searchcolumns.length];
        for (int i = 0; i < searchcolumns.length; i++)
            searchFields[i] = searchcolumns[i][0];
    }

    private class OnChangingEventListener implements EventListener {

        public void onEvent(Event event) throws Exception {
            String value = ((InputEvent) event).getValue();
            if(UtilValidator.isEmpty(value) || value.length() < 2)
                return;
            List<LabTest> labTestList = componentRepository.searchEntities(value, LabTest.class, new String[]{"testCode","testDescription"});
            List<LabTestPanel> labTestPanelList = componentRepository.searchEntities(value, LabTestPanel.class, new String[]{"panelCode","panelDescription"});
            List<LabTestProfile> labTestProfileList = componentRepository.searchEntities(value, LabTestProfile.class, new String[]{"profileCode","profileName"});
            List<Radiology> radiologyList = componentRepository.searchEntities(value, Radiology.class, new String[]{"department","serviceName"});
            Map<String, List> toBeDisplayed = new HashMap<>();
            toBeDisplayed.put("Lab Test",labTestList);
            toBeDisplayed.put("Lab Test Panel",labTestPanelList);
            toBeDisplayed.put("Lab Test Profile",labTestProfileList);
            toBeDisplayed.put("Radiology",radiologyList);
            buildCustomDisplayBox(toBeDisplayed);
        }
    }

    protected void buildCustomDisplayBox(Map<String, List> toBeDisplayed) {
        Component div = (Component)getFirstChild().getChildren().get(1);
        div.getChildren().clear();
        Listbox listbox = new Listbox();
        listbox.setParent(div);
        listbox.addEventListener("onSelect", new OnCustomSelectListener());
        if(!isHidePagination()){
            Paging paging = new Paging();
            paging.setPageSize(Constants.PAGE_SIZE);
            paging.setPageIncrement(1);
            paging.setParent(div);
            listbox.setPaginal(paging);
            listbox.setMold("paging");
        }
        if(!isHideListHeader()){
            Listhead listhead = new Listhead();
            listhead.setParent(listbox);
            for(String[] column : getDisplaycolumns()){
                Listheader listheader = new Listheader(isUseLabels() ? column[1] : UtilDisplay.camelcaseToUiString(column[0]));
                listheader.setStyle("min-width:200px;");
                listheader.setParent(listhead);
            }
        }
        for(String key : toBeDisplayed.keySet()){
            Listitem li = new Listitem();
            li.setParent(listbox);
            Listcell lc = new Listcell();
            lc.setParent(li);
            lc.setLabel(key);
            lc = new Listcell();
            lc.setParent(li);
            for(Object data : toBeDisplayed.get(key)){
                Listitem listitem = new Listitem();
                listitem.setParent(listbox);
                listitem.setAttribute("object", data);

                Listcell listcel1 = new Listcell();
                listcel1.setLabel("");
                Listcell listcel2 = new Listcell();

                Label label = new Label("");
                if(data instanceof LabTest)
                    label = new Label( ((LabTest)data).getTestDescription() );
                if(data instanceof  LabTestProfile)
                    label = new Label( ((LabTestProfile)data).getProfileName() );
                if(data instanceof LabTestPanel)
                    label = new Label( ((LabTestPanel)data).getPanelDescription() );
                if(data instanceof Radiology)
                    label = new Label( ((Radiology)data).getServiceName() );
                label.setParent(listcel2);
                listcel1.setParent(listitem);
                listcel2.setParent(listitem);
            }
        }
    }


    protected class OnCustomSelectListener implements EventListener{

        public void onEvent(Event event) throws Exception {
            Listbox listbox = (Listbox) event.getTarget();
            Object value = listbox.getSelectedItem().getAttribute("object");
            String description = null;
            if(value instanceof LabTest) {
                LabTest labTest = (LabTest)value;
                patientLabOrder.setLabTest(labTest);
                patientLabOrder.setLabTestProfile(null);
                patientLabOrder.setLabTestPanel(null);
                patientLabOrder.setTestCode(labTest.getTestCode());
                description = labTest.getTestDescription();
                patientLabOrder.setTestName(description);
                patientLabOrder.setRadiology(null);
            }
            if(value instanceof LabTestProfile){
                LabTestProfile labTestProfile = (LabTestProfile)value;
                patientLabOrder.setLabTestProfile(labTestProfile);
                patientLabOrder.setLabTest(null);
                patientLabOrder.setLabTestPanel(null);
                patientLabOrder.setTestCode(labTestProfile.getProfileCode());
                description = labTestProfile.getProfileName();
                patientLabOrder.setTestName(description);
                patientLabOrder.setRadiology(null);
            }
            if(value instanceof LabTestPanel){
                LabTestPanel labTestPanel = (LabTestPanel)value;
                patientLabOrder.setLabTestPanel(labTestPanel);
                patientLabOrder.setLabTest(null);
                patientLabOrder.setLabTestProfile(null);
                patientLabOrder.setTestCode(labTestPanel.getPanelCode());
                description = labTestPanel.getPanelDescription();
                patientLabOrder.setTestName(description);
                patientLabOrder.setRadiology(null);
            }
            if(value instanceof Radiology){
                Radiology radiology = (Radiology)value;
                patientLabOrder.setLabTestPanel(null);
                patientLabOrder.setLabTest(null);
                patientLabOrder.setLabTestProfile(null);
                patientLabOrder.setTestCode(radiology.getDepartment());
                description = radiology.getServiceName();
                patientLabOrder.setTestName(description);
            }
            setValue(description);
            listbox.detach();
            close();
            Events.postEvent("onChange",LabComponentAutoSuggestionBox.this, patientLabOrder);
            Events.postEvent("onLookedUp",LabComponentAutoSuggestionBox.this, patientLabOrder);
        }
    }

    public PatientLabOrder getPatientLabOrder() {
        return patientLabOrder;
    }

    public void setPatientLabOrder(PatientLabOrder patientLabOrder) {
        this.patientLabOrder = patientLabOrder;
    }
}
