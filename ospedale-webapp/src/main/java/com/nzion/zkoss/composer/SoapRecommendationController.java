package com.nzion.zkoss.composer;

import com.nzion.domain.File;
import com.nzion.domain.docmgmt.PatientEducationDocument;
import com.nzion.domain.emr.VitalSign;
import com.nzion.domain.emr.lab.LabTest;
import com.nzion.domain.emr.soap.*;
import com.nzion.enums.MATERIALCATEGORY;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.emr.PlanAndRecommendationService;
import com.nzion.util.IoAndFileUtil;
import com.nzion.util.UtilValidator;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.metainfo.ComponentInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Sandeep Prusty
 * May 24, 2011
 */
public class SoapRecommendationController extends OspedaleAutowirableComposer {

	public PatientSoapNoteController soapNoteController;

	private PlanAndRecommendationService planAndRecommendationService;

	private PatientVitalSign height;

	private PatientVitalSign weight;

	private PatientVitalSign bmi;

	private VitalSignSection _vitalSignSection;

	private final RecommendationSection recommendationSection;

	private CommonCrudService commonCrudService;

	private SOAPPlan patientEducationPlan;

	private SOAPPlan bmiFollowUpPlan;

	private SOAPPlan tobaccoCessationPlan;

	private String language;
	
	private Set<PatientEducationDocument> icdDocs;

	private Set<PatientEducationDocument> cptDocs;

	private Set<PatientEducationDocument> medicationDocs;

	private Set<PatientEducationDocument> labOrderDocs;

	private Set<PatientEducationDocument> filteredIcdDos;

	private Set<PatientEducationDocument> filteredCptDocs;

	private Set<PatientEducationDocument> filteredMedicationDocs;

	private Set<PatientEducationDocument> filteredLabOrderDocs;

	public Set<PatientEducationDocument> getFilteredIcdDos() {
	return filteredIcdDos;
	}

	public Set<PatientEducationDocument> getFilteredCptDocs() {
	return filteredCptDocs;
	}

	public Set<PatientEducationDocument> getFilteredMedicationDocs() {
	return filteredMedicationDocs;
	}

	public Set<PatientEducationDocument> getFilteredLabOrderDocs() {
	return filteredLabOrderDocs;
	}

	public Set<PatientEducationDocument> getIcdDocs() {
	return icdDocs;
	}

	public Set<PatientEducationDocument> getCptDocs() {
	return cptDocs;
	}

	public Set<PatientEducationDocument> getMedicationDocs() {
	return medicationDocs;
	}

	public Set<PatientEducationDocument> getLabOrderDocs() {
	return labOrderDocs;
	}

	public String getLanguage() {
	return language;
	}

	public SOAPPlan getPatientEducationPlan() {
	return patientEducationPlan;
	}

	public SOAPPlan getBmiFollowUpPlan() {
	return bmiFollowUpPlan;
	}

	public SOAPPlan getTobaccoCessationPlan() {
	return tobaccoCessationPlan;
	}

	public SoapRecommendationController(Desktop desktop) {
	this.soapNoteController = (PatientSoapNoteController) desktop.getAttribute("controller");
	recommendationSection = (RecommendationSection) soapNoteController.getSoapSection(RecommendationSection.class);
	}

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	patientEducationPlan = recommendationSection.retrieveSoapPlanAnyWay(RecommendationSection.PATIENT_EDUCATION_PLAN);
	bmiFollowUpPlan = recommendationSection.retrieveSoapPlanAnyWay(RecommendationSection.BMI_PLAN);
	tobaccoCessationPlan = recommendationSection.retrieveSoapPlanAnyWay(RecommendationSection.TOBACCO_PLAN);
	language = recommendationSection.getSoapNote().getPatient().getLanguage() == null ? "English"
			: recommendationSection.getSoapNote().getPatient().getLanguage().getDescription();
	icdDocs = new HashSet<PatientEducationDocument>(getDocumentsForIcds(getPatientIcds()));
	cptDocs = new HashSet<PatientEducationDocument>(getDocumentsForCpts(getPatientCpts()));
	medicationDocs = new HashSet<PatientEducationDocument>(getDocumentsForMedications(getActiveMedications()));
	labOrderDocs = new HashSet<PatientEducationDocument>(getDocumentsForLabOrders(getPatientLabOrders()));
	filteredIcdDos = getFilteredDocumentsBypatientLanguage(icdDocs, language);
	filteredCptDocs = getFilteredDocumentsBypatientLanguage(cptDocs, language);
	filteredMedicationDocs = getFilteredDocumentsBypatientLanguage(medicationDocs, language);
	filteredLabOrderDocs = getFilteredDocumentsBypatientLanguage(labOrderDocs, language);
	return super.doBeforeCompose(page, parent, compInfo);
	}

	public Set<PatientEducationDocument> getFilteredDocumentsBypatientLanguage(
			Set<PatientEducationDocument> educationDocuments, String patientLanguage) {
	Set<PatientEducationDocument> documents = new HashSet<PatientEducationDocument>();
	for (PatientEducationDocument document : educationDocuments)
		if (patientLanguage.equalsIgnoreCase(document.getLanguage().getDescription())) documents.add(document);
	return documents;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public void setPlanAndRecommendationService(PlanAndRecommendationService planAndRecommendationService) {
	this.planAndRecommendationService = planAndRecommendationService;
	}

	public RecommendationSection getRecommendationSection() {
	return recommendationSection;
	}

	public boolean isBmiRecorded() {
	if (_vitalSignSection == null) loadBmiData();
	return bmi != null;
	}

	private void loadBmiData() {
	this._vitalSignSection = (VitalSignSection) soapNoteController.getSoapSection(VitalSignSection.class, false);
	PatientVitalSignSet vitalSignSet = _vitalSignSection.getLatestVitalSignRecordings(VitalSign.BMI);
	if (vitalSignSet == null) return;
	height = vitalSignSet.getPatientVitalSign(VitalSign.HEIGHT);
	weight = vitalSignSet.getPatientVitalSign(VitalSign.WETGHT);
	bmi = vitalSignSet.getPatientVitalSign(VitalSign.BMI);
	}

	public PatientVitalSign getHeight() {
	return height;
	}

	public PatientVitalSign getWeight() {
	return weight;
	}

	public PatientVitalSign getBmi() {
	return bmi;
	}

	public List<PatientEducationDocument> getDocumentsForIcds(Set<PatientIcd> patientIcds) {
	List<PatientEducationDocument> documents = new ArrayList<PatientEducationDocument>();
	for (PatientIcd icd : patientIcds)
		documents
				.addAll(planAndRecommendationService.getPatientEducationDocumentsFor(MATERIALCATEGORY.ICD, icd
						.getIcdElement().getCode(), icd.getIcdElement().getDescription(), icd.getIcdElement().getId()
						.toString(), soapNoteController.getSoapNote().getDate(), soapNoteController.getSoapNote()
						.getPatient()));
	return documents;
	}

	public Set<PatientIcd> getPatientIcds() {
	return ((DiagnosisSection) soapNoteController.getSoapSection(DiagnosisSection.class, false)).getIcds();
	}

	public List<PatientEducationDocument> getDocumentsForCpts(Set<PatientCpt> patientCpts) {
	List<PatientEducationDocument> documents = new ArrayList<PatientEducationDocument>();
	for (PatientCpt cpt : patientCpts)
		documents.addAll(planAndRecommendationService.getPatientEducationDocumentsFor(MATERIALCATEGORY.CPT, cpt
				.getCpt().getId(), cpt.getCpt().getDescription(), cpt.getCpt().getId(), soapNoteController
				.getSoapNote().getDate(), soapNoteController.getSoapNote().getPatient()));
	return documents;
	}

	public Set<PatientCpt> getPatientCpts() {
	return ((DiagnosisSection) soapNoteController.getSoapSection(DiagnosisSection.class, false)).getCpts();
	}

	public Set<PatientRx> getActiveMedications() {
	Set<PatientRx> activeMedications = new HashSet<PatientRx>();
	RxSection rxSection = (RxSection) soapNoteController.getSoapSection(RxSection.class, false);
	activeMedications.addAll(rxSection.getPatientRxs());
	return activeMedications;
	}

	public List<PatientEducationDocument> getDocumentsForMedications(Set<PatientRx> rxs) {
	List<PatientEducationDocument> documents = new ArrayList<PatientEducationDocument>();
	for (PatientRx rx : rxs)
		documents.addAll(planAndRecommendationService.getPatientEducationDocumentsFor(MATERIALCATEGORY.MEDICATION,
				null, rx.getDrug() == null ? rx.getDrugName() : rx.getDrug().getTradeName(), null, soapNoteController.getSoapNote().getDate(), soapNoteController
						.getSoapNote().getPatient()));
	return documents;
	}

	public Set<PatientLabOrder> getPatientLabOrders() {
	return ((LabOrderSection) soapNoteController.getSoapSection(LabOrderSection.class, false)).getLabOrder();
	}

	public List<PatientEducationDocument> getDocumentsForLabOrders(Set<PatientLabOrder> labOrders) {
	List<PatientEducationDocument> documents = new ArrayList<PatientEducationDocument>();
	/*for (PatientLabOrder labOrder : labOrders) {
		*//*documents.addAll(planAndRecommendationService.getPatientEducationDocumentsFor(MATERIALCATEGORY.LABORDER,
				labOrder.getLabTestPanel().getCode(), labOrder.getLabTestPanel().getPanelName(), labOrder
						.getLabTestPanel().getId().toString(), soapNoteController.getSoapNote().getDate(),
				soapNoteController.getSoapNote().getPatient()));*//*
		*//*if (UtilValidator.isNotEmpty(labOrder.getLabTestPanel().getLabTestPanels())) {
			for (LabTestPanel labTestPanel : labOrder.getLabTestPanel().getLabTestPanels()) {
				documents.addAll(planAndRecommendationService.getPatientEducationDocumentsFor(
						MATERIALCATEGORY.LABORDER, labTestPanel.getCode(), labTestPanel.getPanelName(), labTestPanel
								.getId().toString(), soapNoteController.getSoapNote().getDate(), soapNoteController
								.getSoapNote().getPatient()));
				documents.addAll(getDocumentsForLabPanels(labTestPanel.getLabTestPanels()));
			}
		}*//*
	}*/
	return documents;
	}

	private List<PatientEducationDocument> getDocumentsForLabPanels(Set<LabTest> labTests) {
	List<PatientEducationDocument> documents = new ArrayList<PatientEducationDocument>();
	/*if (UtilValidator.isEmpty(labTestPanels)) return Collections.emptyList();
	for (LabTestPanel panel : labTestPanels) {
		documents.addAll(planAndRecommendationService.getPatientEducationDocumentsFor(MATERIALCATEGORY.LABORDER, panel
				.getCode(), panel.getPanelName(), panel.getId().toString(), soapNoteController.getSoapNote().getDate(),
				soapNoteController.getSoapNote().getPatient()));
		if (UtilValidator.isNotEmpty(panel.getLabTestPanels()))
			documents.addAll(getDocumentsForLabPanels(panel.getLabTestPanels()));
	}*/
	return documents;
	}

	public void downloadPatientEducationDocument(String zipFileName,
			Set<PatientEducationDocument> patientEducationDocuments, SOAPPlan plan,
			RecommendationSection recommendationSection) throws IOException {
	if (UtilValidator.isEmpty(patientEducationDocuments)) return;
	if (plan == null) return;
	plan.getDownLoadedDocuments().addAll(new ArrayList<PatientEducationDocument>(patientEducationDocuments));
	List<File> files = new ArrayList<File>();
	for (PatientEducationDocument patientEducationDocument : patientEducationDocuments)
		files.add(patientEducationDocument.getFile());
	IoAndFileUtil.downloadZipFile(zipFileName, files);
	recommendationSection.setFileDownloaded(Boolean.TRUE);
	soapNoteController.saveSoapSection();
//	commonCrudService.save(recommendationSection);
	}

	public SOAPPlan deleteAndCreateNewSOAPPlan(SOAPPlan soapPlan, String followUpFor) {
	getRecommendationSection().getPlans().remove(soapPlan);
	commonCrudService.delete(soapPlan);
	soapNoteController.saveSoapSection();
	SOAPPlan plan = new SOAPPlan();
	plan.setFollowUp(new FollowUp());
	plan.setFollowUpFor(followUpFor);
	plan.setPatient(recommendationSection.getSoapNote().getPatient());
	plan.setProvider(recommendationSection.getSoapNote().getProvider());
	plan.setRecommendationSection(getRecommendationSection());
	getRecommendationSection().getPlans().add(plan);
	return plan;
	}

	public Set<PatientEducationDocument> getDocumentsForSOAPPlan(MATERIALCATEGORY materialcategory) {
	return new HashSet<PatientEducationDocument>(planAndRecommendationService.getPatientEducationDocumentsFor(
			materialcategory, language, recommendationSection.getSoapNote().getPatient(), recommendationSection
					.getSoapNote().getDate()));
	}

	public void showReferralTemplatePreview() {
	if (bmiFollowUpPlan.getReferalLetterTemplate() == null || bmiFollowUpPlan.getReferral() == null) return;
	Executions.getCurrent().sendRedirect(
			"/soap/plans/preview-referral-letter.zul?scheduleId="
			+ soapNoteController.getSoapNote().getSchedule().getId()
			+ "&referralId=" + bmiFollowUpPlan.getReferral().getId() + "&referralLetterTemplateId=" + bmiFollowUpPlan.getReferalLetterTemplate().getId(),
			"_blank");
	}

	public void clearBmiFollowUp(final Component bmiDiv) throws InterruptedException {
	Messagebox.show("Are You sure ?", "Delete Confirm ?", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
			new EventListener() {

				@Override
				public void onEvent(Event event) throws Exception {
				if ("onYes".equalsIgnoreCase(event.getName())) {
					soapNoteController.setEventMessage("BMI FollowUp Plan Record Deleted");
					bmiFollowUpPlan = deleteAndCreateNewSOAPPlan(bmiFollowUpPlan, RecommendationSection.BMI_PLAN);
					root.getFellow("bmiInclude").invalidate();
				}
				}
			});
	}

	public void clearTobaccoCessation(final Component tobaccoCessationDiv) throws InterruptedException {
	Messagebox.show("Are You Sure ?", "Delete Confirm?", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {

				@Override
				public void onEvent(Event evt) throws Exception {
				if ("onYes".equalsIgnoreCase(evt.getName())) {
					soapNoteController.setEventMessage("Tobacco Cessation Plan Record Deleted");
					tobaccoCessationPlan = deleteAndCreateNewSOAPPlan(tobaccoCessationPlan,
							RecommendationSection.TOBACCO_PLAN);
					root.getFellow("tobaccoCessationInclude").invalidate();
				}
				}
			});
	}

	public void downloadIndividualDocument(Set<PatientEducationDocument> selectedIcdDocs,
			PatientEducationDocument document, String zipFileName) throws IOException {
	selectedIcdDocs.add(document);
	downloadPatientEducationDocument(zipFileName, selectedIcdDocs, patientEducationPlan, getRecommendationSection());
	selectedIcdDocs.clear();
	}

	public void setLanguage(String language) {
	this.language = language;
	}

	public void getFilteredDocuments(String languageCode) {
	filteredIcdDos = getFilteredDocumentsBypatientLanguage(icdDocs, languageCode);
	filteredCptDocs = getFilteredDocumentsBypatientLanguage(cptDocs, languageCode);
	filteredMedicationDocs = getFilteredDocumentsBypatientLanguage(medicationDocs, languageCode);
	filteredLabOrderDocs = getFilteredDocumentsBypatientLanguage(labOrderDocs, languageCode);
	}

	private static final long serialVersionUID = 1L;
}
