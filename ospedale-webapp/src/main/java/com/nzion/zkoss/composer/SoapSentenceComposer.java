package com.nzion.zkoss.composer;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.ComponentInfo;

import com.nzion.domain.emr.SoapModule;
import com.nzion.domain.emr.soap.SoapSection;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilDisplay;
import com.nzion.util.UtilMisc;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.ext.Navigation;

public class SoapSentenceComposer extends OspedaleAutowirableComposer {

	private static final long serialVersionUID = -8939803030721380273L;
	private SoapNoteService soapNoteService;
	private final List<SoapModule> modules = null;
	private PatientSoapNoteController soapNoteController = null;
	private SoapModule selectedModule;
	private SoapSection selectedSection;
	private String eventMessage;
	private CommonCrudService commonCrudService;

	public void displaySoapSentencePreview(SoapModule module, Component container) {
	this.selectedModule = module;
	String viewName = UtilDisplay.buildIdFromName("soap", module.getModuleName()) + "Sentence";
	
	viewName=viewName.replace("-", "");
	if (!Navigation.viewExists(viewName)) {
		viewName = "otherSoapNoteSectionSentence";
	}
	Navigation.navigate(viewName, UtilMisc.toMap("controller", this), container, true);
	}

	public void showPreviewNote(Event event) throws InstantiationException, IllegalAccessException {
	Component comp = event.getTarget();
	if (comp != null) {
		comp.setVisible(true);
		if (event.getTarget().getAttribute("value") instanceof SoapModule) {
			SoapModule module = (SoapModule) event.getTarget().getAttribute("value");
			selectedModule = module == null ? null : module;
		}
		String viewName = event.getTarget().getId() + "Sentence";
		if (!Navigation.viewExists(viewName)) {
			viewName = "otherSoapNoteSectionSentence";
		}
		Navigation.navigate(viewName, UtilMisc.toMap("controller", this,"editable",true), comp.getFellow("previewDiv"));
		return;
	}
	}

	public SoapNoteService getSoapNoteService() {
	return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
	this.soapNoteService = soapNoteService;
	}

	public SoapModule getSelectedModule() {
	return selectedModule;
	}

	public void setSelectedModule(SoapModule selectedModule) {
	this.selectedModule = selectedModule;
	}

	public SoapSection getSelectedSection() {
	return selectedSection;
	}

	public void setSelectedSection(SoapSection selectedSection) {
	this.selectedSection = selectedSection;
	}

	public List<SoapModule> getModules() {
	return modules;
	}

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	soapNoteController = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
	return super.doBeforeCompose(page, parent, compInfo);
	}

	public PatientSoapNoteController getSoapNoteController() {
	return soapNoteController;
	}

	public void setSoapNoteController(PatientSoapNoteController soapNoteController) {
	this.soapNoteController = soapNoteController;
	}

	public void saveSelectedSection() {
	if (selectedSection == null) {
		throw new RuntimeException("No Section is selected.");
	}
	selectedSection = soapNoteService.saveSoapSection(selectedSection,UtilValidator.isEmpty(eventMessage)?buildEventMessage():eventMessage);
	if (this.getSoapNoteController().getSelectedSection().getSoapModule().getModuleName().equals(
			selectedSection.getSoapModule().getModuleName())) {
		this.getSoapNoteController().setSelectedSection(selectedSection);
	}
	}
	
	public String getLabResultSentence(){
	StringBuilder builder = new StringBuilder();
	return builder.toString();
	}
	
	public String buildEventMessage(){
	return selectedModule.getModuleName() +"\t updated";
	}

	public String getEventMessage() {
	return eventMessage;
	}

	public void setEventMessage(String eventMessage) {
	this.eventMessage = eventMessage;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}
	
	
}
