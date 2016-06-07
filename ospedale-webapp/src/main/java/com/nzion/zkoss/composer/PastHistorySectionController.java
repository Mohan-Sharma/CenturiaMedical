package com.nzion.zkoss.composer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import com.nzion.util.Infrastructure;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

import com.nzion.domain.emr.ChronicDisease;
import com.nzion.domain.emr.soap.PastHistorySection;
import com.nzion.domain.emr.soap.PatientChronicDisease;
import com.nzion.domain.emr.soap.PatientPastObservationHistory;
import com.nzion.domain.emr.soap.PatientPastOperationHistory;
import com.nzion.domain.emr.soap.PatientPastTreatmentHistory;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilValidator;

public class PastHistorySectionController extends AutowirableComposer {

    private static final long serialVersionUID = 1L;

    private Set<PatientPastOperationHistory> operationHistories;

    private Set<PatientPastTreatmentHistory> treatmentHistories;

    private PatientSoapNoteController soapNoteController;

    private PastHistorySection pastHistorySection;


    private Set<PatientChronicDisease> patientChronicDiseases = new HashSet<PatientChronicDisease>();
    
    private Set<PatientChronicDisease> patientChronicDiseaseList  = new HashSet<PatientChronicDisease>();

    private CommonCrudService commonCrudService;

    private SoapNoteService soapNoteService;

    private List<ChronicDisease> chronicDiseases;


    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        soapNoteController = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
        pastHistorySection = (PastHistorySection) soapNoteController.getSoapSection(PastHistorySection.class);
        if (UtilValidator.isEmpty(pastHistorySection.getPatientPastOperationHitories())) {
            operationHistories = new HashSet<PatientPastOperationHistory>();
            //pastHistorySection.setPatientPastOperationHitories(operationHistories);
        } else
            operationHistories = pastHistorySection.getPatientPastOperationHitories();
        if (UtilValidator.isEmpty(pastHistorySection.getPatientPastTreatmentHistories())) {
            treatmentHistories = new HashSet<PatientPastTreatmentHistory>();
            //pastHistorySection.setPatientPastTreatmentHistories(treatmentHistories);
        } else
            treatmentHistories = pastHistorySection.getPatientPastTreatmentHistories();
        chronicDiseases = commonCrudService.getAll(ChronicDisease.class);
        buildChronicDiseaseData();


        return super.doBeforeCompose(page, parent, compInfo);
    }

    public void buildChronicDiseaseData() {
        List<PatientChronicDisease> previousPatientChronicDiseases = soapNoteService.getAllPatientChronicDiseaseFor(pastHistorySection.getSoapNote().getPatient());
        if (UtilValidator.isEmpty(previousPatientChronicDiseases))
            patientChronicDiseases = pastHistorySection.getChroniDisseasesForPatient(chronicDiseases);
        else {
        	patientChronicDiseaseList = new HashSet<PatientChronicDisease>();
        	patientChronicDiseaseList.addAll(previousPatientChronicDiseases);
            for (PatientChronicDisease patientChronicDisease : patientChronicDiseaseList)
                patientChronicDisease.setDiseaseSelected(true);
        }
    }

    public void enableEdit() {
    	chronicDiseases = commonCrudService.getAll(ChronicDisease.class);
        //buildChronicDiseaseData();
    	patientChronicDiseases = pastHistorySection.getChroniDisseasesForPatient(chronicDiseases);
        Set<ChronicDisease> diseases = new HashSet<ChronicDisease>();
        for (PatientChronicDisease patientChronicDisease : patientChronicDiseases)
            diseases.add(patientChronicDisease.getChronicDisease());
        chronicDiseases.removeAll(diseases);
        if (UtilValidator.isNotEmpty(patientChronicDiseases)) {
            for (ChronicDisease chronicDisease : chronicDiseases) {
                PatientChronicDisease patientChronicDisease = new PatientChronicDisease(chronicDisease, pastHistorySection.getSoapNote().getPatient());
                patientChronicDisease.setSoapSection(pastHistorySection);
                patientChronicDiseases.add(patientChronicDisease);
            }
        }
        for (PatientChronicDisease patientChronicDisease : patientChronicDiseases){
            patientChronicDisease.setDisableEdit(false);
            for(PatientChronicDisease pcd : patientChronicDiseaseList){
            	if(pcd.getChronicDisease().getName().equals(patientChronicDisease.getChronicDisease().getName())){
            		patientChronicDisease.setDuration(pcd.getDuration());
            		patientChronicDisease.setDurationType(pcd.getDurationType());
            		patientChronicDisease.setMantainedBy(pcd.getMantainedBy());
            		patientChronicDisease.setMantainedProcess(pcd.getMantainedProcess());
            		patientChronicDisease.setTreatedForHowLong(pcd.getTreatedForHowLong());
            		patientChronicDisease.setTreatedForHowLongType(pcd.getTreatedForHowLongType());
            		patientChronicDisease.setDiseaseSelected(true);
            	}
            }
        }
    }

    public void saveDiseaseSection() {
        Set<PatientChronicDisease> selectedPatientChronicDiseases = new HashSet<PatientChronicDisease>();
        for (PatientChronicDisease patientChronicDisease : patientChronicDiseases)
            if (patientChronicDisease.isDiseaseSelected())
                selectedPatientChronicDiseases.add(patientChronicDisease);
        pastHistorySection.addPatientDiseases(selectedPatientChronicDiseases);
        soapNoteController.saveSoapSection();
        Infrastructure.getSessionFactory().getCurrentSession().clear();
    }

    public void addPastOperation() {
        PatientPastOperationHistory pastOperationHistory = new PatientPastOperationHistory();
        com.nzion.zkoss.ext.Navigation.navigateToModalWindow("/soap/addPatientPastOperation.zul",
                com.nzion.util.UtilMisc.toMap("controller", soapNoteController, "pastOperationHistory",
                        pastOperationHistory, "pastHistorySection", pastHistorySection, "pastOperationHistories",
                        operationHistories, "pastHistoryController", this));
    }
    
    public void editPastOperation(PatientPastOperationHistory pastOperationHistory) {
        if (pastOperationHistory == null){
        	return;
        }
        com.nzion.zkoss.ext.Navigation.navigateToModalWindow("/soap/editPatientPastOperation.zul",
                com.nzion.util.UtilMisc.toMap("controller", soapNoteController, "pastOperationHistory",
                        pastOperationHistory, "pastHistorySection", pastHistorySection, "pastOperationHistories",
                        operationHistories, "pastHistoryController", this));
    }

    public void addEditPastTreatment(PatientPastTreatmentHistory pastTreatmentHistory) {
        if (pastTreatmentHistory == null)
            pastTreatmentHistory = new PatientPastTreatmentHistory();
        com.nzion.zkoss.ext.Navigation.navigateToModalWindow("/soap/addPatientPastTreatment.zul",
                com.nzion.util.UtilMisc.toMap("controller", soapNoteController, "pastTreatmentHistory",
                        pastTreatmentHistory, "pastHistorySection", pastHistorySection, "pastTreatmentHistories",
                        treatmentHistories, "pastHistoryController", this));
    }
    
    
    public void addEditPastChronic() {
    	enableEdit();
        com.nzion.zkoss.ext.Navigation.navigateToModalWindow("/soap/addEditPastChronic.zul",
                com.nzion.util.UtilMisc.toMap("pastHistorySectionController", this));
    }


    public void showHistory(String url) throws SuspendNotAllowedException, InterruptedException {
        Window window = (Window) Executions.createComponents(url, root.getFellow("pastHistorySectionDiv"),
                com.nzion.util.UtilMisc.toMap("controller", soapNoteController, "pastHistorySection", pastHistorySection));
        window.doModal();
    }

    public void removeSurgery(PatientPastOperationHistory operationHistory) {
        pastHistorySection.getPatientPastOperationHitories().remove(operationHistory);
        soapNoteController.saveSoapSection();
        Events.postEvent("onClick", (Component) desktop.getAttribute("wkModule"), null);
    }

    public void removeTreatment(PatientPastTreatmentHistory pastTreatmentHistory) {
        pastHistorySection.getPatientPastTreatmentHistories().remove(pastTreatmentHistory);
        soapNoteController.saveSoapSection();
        Events.postEvent("onClick", (Component) desktop.getAttribute("wkModule"), null);
        Clients.evalJavaScript("toggleTreatment()");
    }

    public void removeObservation(PatientPastObservationHistory pastObservationHistory) {
        pastHistorySection.getPatientPastObservationHistories().remove(pastObservationHistory);
    }


    public Set<PatientPastOperationHistory> getOperationHistories() {
        return operationHistories;
    }

    public Set<PatientPastTreatmentHistory> getTreatmentHistories() {
        return treatmentHistories;
    }

    public PatientSoapNoteController getSoapNoteController() {
        return soapNoteController;
    }

    public PastHistorySection getPastHistorySection() {
        return pastHistorySection;
    }

    public Set<PatientChronicDisease> getPatientChronicDiseases() {
        return patientChronicDiseases;
    }

    public void setPatientChronicDiseases(
            Set<PatientChronicDisease> patientChronicDiseases) {
        this.patientChronicDiseases = patientChronicDiseases;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }
    
    public Set<PatientChronicDisease> getPatientChronicDiseaseList() {
		return patientChronicDiseaseList;
	}

	public void setPatientChronicDiseaseList(Set<PatientChronicDisease> patientChronicDiseaseList) {
		this.patientChronicDiseaseList = patientChronicDiseaseList;
	}

	@Resource
    @Required
    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

}