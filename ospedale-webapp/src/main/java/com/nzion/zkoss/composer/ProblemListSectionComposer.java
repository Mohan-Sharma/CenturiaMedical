package com.nzion.zkoss.composer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listbox;

import com.nzion.domain.emr.soap.PatientIcd;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.emr.soap.ProblemListSection;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.EnumerationService;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

public class ProblemListSectionComposer extends OspedaleAutowirableComposer {

	private static final long serialVersionUID = 1L;
	private SoapNoteService soapNoteService;
	private PatientSoapNoteController mainCtl;
	private EnumerationService enumerationService;
	private ProblemListSection pastHxSection = null;
	private final List<PatientIcd> problemList = new ArrayList<PatientIcd>();
	private final Set<String> selectedStatuses = new HashSet<String>(10);
	private boolean currentProblemSection = true;

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	mainCtl = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
	mainCtl.getSoapSection(ProblemListSection.class);
	pastHxSection = (ProblemListSection) mainCtl.getSelectedSection();
	return super.doBeforeCompose(page, parent, compInfo);
	}

	public void handleSelection(Event event) {
	Checkbox checkbox = ((Checkbox) event.getTarget());
	if (checkbox.isChecked())
		selectedStatuses.add(checkbox.getLabel());
	else
		selectedStatuses.remove(checkbox.getLabel());
	problemList.clear();
	if (UtilValidator.isNotEmpty(selectedStatuses)) {
		for (String status : selectedStatuses) {
			problemList.addAll(soapNoteService.getIcdForPatient(mainCtl.getPatient(), status,
					isCurrentProblemSection() ? mainCtl.getSoapNote() : null));
		}
	}
	Listbox problemListBox = (Listbox) checkbox.getFellowIfAny("illnessListBox");
	problemListBox.setModel(new BindingListModelList(problemList, false));
	}

	public SoapNoteService getSoapNoteService() {
	return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
	this.soapNoteService = soapNoteService;
	}

	public PatientSoapNoteController getMainCtl() {
	return mainCtl;
	}

	public void setMainCtl(PatientSoapNoteController mainCtl) {
	this.mainCtl = mainCtl;
	}

	public EnumerationService getEnumerationService() {
	return enumerationService;
	}

	public void setEnumerationService(EnumerationService enumerationService) {
	this.enumerationService = enumerationService;
	}

	public ProblemListSection getProblemListSection() {
	return pastHxSection;
	}

	public void setProblemListSection(ProblemListSection pastHxSection) {
	this.pastHxSection = pastHxSection;
	}

	public boolean isCurrentProblemSection() {
	return currentProblemSection;
	}

	public void setCurrentProblemSection(boolean currentProblemSection) {
	this.currentProblemSection = currentProblemSection;
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
//	Checkbox noKnownIllness = (Checkbox) component.getFellowIfAny("noKnownIllnessHistoryChkBox", true);
//	List<PatientIcd> icds = soapNoteService.getIcdForPatient(mainCtl.getPatient(), new String[0],
//			isCurrentProblemSection() ? UtilMisc.toList(mainCtl.getSoapNote()) : null);
//	if (UtilValidator.isNotEmpty(icds)) noKnownIllness.setDisabled(true);
	}

	public boolean checkForEditable() { 
	PatientSoapNote currentSoapNote = mainCtl.getSoapNote();
	List<PatientSoapNote> allSoapNotesExcudingCurrent = soapNoteService.getAllSoapNotesExcludingCurrent(currentSoapNote);
	PatientSoapNote latestSoapNote = soapNoteService.getLatestSoapNoteFor(mainCtl.getPatient(),UtilDateTime.getDayEnd(new Date()));
	if (UtilValidator.isNotEmpty(allSoapNotesExcudingCurrent) && latestSoapNote!=null && allSoapNotesExcudingCurrent.contains(latestSoapNote)) 
		return true;
	return false;
	}

}