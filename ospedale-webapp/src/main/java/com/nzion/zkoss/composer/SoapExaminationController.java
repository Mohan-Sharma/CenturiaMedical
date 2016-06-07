package com.nzion.zkoss.composer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Panel;

import com.nzion.domain.Provider;
import com.nzion.domain.Speciality;
import com.nzion.domain.emr.OrganSystem;
import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.Question;
import com.nzion.domain.emr.soap.ExaminationSection;
import com.nzion.domain.emr.soap.PatientExamination;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilValidator;

public class SoapExaminationController extends OspedaleAutowirableComposer implements ComboitemRenderer {

	private static final long serialVersionUID = 1L;

	private PatientSoapNoteController patientSoapNoteController;

	private ExaminationSection examinationSection;

	private CommonCrudService commonCrudService;

	private Set<Speciality> specialities;

	private Set<PatientExamination> patientExaminationsForSelectedTemplate;

	private PatientExamination selectedPatientExamination;

	private List<Question> questions;

	private QATemplate selectedTemplate;

	private Set<QATemplate> qaTemplates;

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	patientSoapNoteController = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
	examinationSection = (ExaminationSection) patientSoapNoteController.getSoapSection(ExaminationSection.class);
	Provider provider = commonCrudService.getById(Provider.class, patientSoapNoteController.getProvider().getId());
	specialities = provider.getSpecialities();
	qaTemplates = getQaTemplates();
	if (UtilValidator.isEmpty(qaTemplates))
		throw new RuntimeException("No templates were configured for this provider's speciality.Please Configure");
	return super.doBeforeCompose(page, parent, compInfo);
	}

	public QATemplate getSelectedTemplate() {
	return selectedTemplate;
	}

	public void setSelectedTemplate(QATemplate selectedTemplate) {
	this.selectedTemplate = selectedTemplate;
	}

	public PatientSoapNoteController getPatientSoapNoteController() {
	return patientSoapNoteController;
	}

	public ExaminationSection getExaminationSection() {
	return examinationSection;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public Set<Speciality> getSpecialities() {
	return specialities;
	}

	public void renderTemplateBasedPatientExaminationQA() {
	patientExaminationsForSelectedTemplate = examinationSection.getPatientExaminations(selectedTemplate);
	if (UtilValidator.isEmpty(patientExaminationsForSelectedTemplate)) {
		Set<OrganSystem> examinationSectionOrgans = new HashSet<OrganSystem>();
		examinationSectionOrgans = selectedTemplate.getOrganSystems();
		patientExaminationsForSelectedTemplate = (Set<PatientExamination>) examinationSection.getPatientExaminations(
				examinationSectionOrgans, selectedTemplate);
		}
	}

	public PatientExamination getSelectedPatientExamination() {
	return selectedPatientExamination;
	}

	public void setSelectedPatientExamination(PatientExamination selectedPatientExamination) {
	this.selectedPatientExamination = selectedPatientExamination;
	}

	public void selectPatientExamination(Panel patientRosSectionPanel, Div patientRosSectionDiv) {
	commonCrudService.refreshEntity(selectedPatientExamination.getOrganSystem());
	patientRosSectionPanel.setVisible("ABNORMAL".equals(selectedPatientExamination.getNormal()));
	questions = commonCrudService.getQuestions(selectedPatientExamination.getQaTemplate(), selectedPatientExamination
			.getOrganSystem());
	openQASection(patientRosSectionDiv);
	}

	private void openQASection(Div patientRosSectionDiv) {
	patientRosSectionDiv.setVisible(true);
	patientRosSectionDiv.getChildren().clear();
	Executions.createComponents("/soap/qa.zul", patientRosSectionDiv, com.nzion.util.UtilMisc.toMap("qas", questions,
			"qaFactory", examinationSection.getPatientExaminationQA(selectedPatientExamination.getOrganSystem(),
					selectedTemplate)));
	}

	public void selectStatus(Listbox listbox, Panel panel) {
	panel.setVisible(listbox.getSelectedItem().getValue().equals("ABNORMAL"));
	commonCrudService.refreshEntity(selectedPatientExamination.getOrganSystem());
	questions = commonCrudService.getQuestions(selectedPatientExamination.getQaTemplate(), selectedPatientExamination
			.getOrganSystem());
	}

	@Override
	public void render(Comboitem item, Object value,int index) throws Exception {
	QATemplate qaTemplate = (QATemplate) value;
	item.setValue(qaTemplate);
	String itemLabel = "";
	Set<Speciality> providerSpecialities = UtilValidator.isNotEmpty(examinationSection.getSpecialities())?examinationSection.getSpecialities():specialities; 
	for (Speciality speciality : providerSpecialities)
		itemLabel = (speciality.equals(qaTemplate.getSpeciality()) ? qaTemplate.getSpeciality().getCode() : speciality.getCode())+ " - " + qaTemplate.getName();
	item.setLabel(itemLabel);
	}

	public Set<PatientExamination> getPatientExaminationsForSelectedTemplate() {
	return patientExaminationsForSelectedTemplate;
	}
	
	public void deletePatientExaminations(){
	for(PatientExamination examination : examinationSection.getPatientExaminationQAs())
		examination.getPatientQAs().clear();
	examinationSection.getPatientExaminationQAs().clear();
	patientSoapNoteController.saveSoapSection();
	}

	public Set<QATemplate> getQaTemplates() {
//	Set<QATemplate> qaTemplates = new HashSet<QATemplate>(soapNoteService.getQAtemplatesForExaminationSection(examinationSection, specialities));
	if (UtilValidator.isEmpty(examinationSection.getQaTemplates()))
			examinationSection.setQaTemplates(new HashSet<QATemplate>(commonCrudService.findByEquality(QATemplate.class,
					new String[] { "speciality" }, new Object[] { specialities })));
	return examinationSection.getQaTemplates();
	}
}
