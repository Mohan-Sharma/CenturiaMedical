package com.nzion.zkoss.composer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zkplus.databind.BindingListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

import com.nzion.domain.drug.Drug;
import com.nzion.domain.drug.DrugSig;
import com.nzion.domain.emr.soap.AllergySection;
import com.nzion.domain.emr.soap.ChiefComplainSection;
import com.nzion.domain.emr.soap.DiagnosisSection;
import com.nzion.domain.emr.soap.MedicationHistorySection;
import com.nzion.domain.emr.soap.PatientChiefComplaint;
import com.nzion.domain.emr.soap.PatientIcd;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.domain.emr.soap.PatientRxAlert;
import com.nzion.domain.emr.soap.RxSection;
import com.nzion.service.ProviderService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;

public class SoapRxComposer extends OspedaleAutowirableComposer {

	private static final long serialVersionUID = 1L;

	private CommonCrudService commonCrudService;
	private SoapNoteService soapNoteService;
	private PatientSoapNoteController mainCtl;
	private Collection<PatientIcd> patientIcds;
	private RxSection section;
	private List<DrugSig> quantityQualifiers;
	private List<DrugSig> directions;
	private List<DrugSig> quantities;
	private Collection<PatientChiefComplaint> patientChiefComplaints;
	private Listbox rxSectionDisplayGrid;
	private ProviderService providerService;
	private Drug drug;

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public PatientSoapNoteController getMainCtl() {
	return mainCtl;
	}

	public void setMainCtl(PatientSoapNoteController mainCtl) {
	this.mainCtl = mainCtl;
	}

	public Collection<PatientIcd> getPatientIcds() {
	return patientIcds;
	}

	public void setPatientIcds(Collection<PatientIcd> patientIcds) {
	this.patientIcds = patientIcds;
	}

	public RxSection getSection() {
	return section;
	}

	public void setSection(RxSection section) {
	this.section = section;
	}

	public List<DrugSig> getQuantityQualifiers() {
	return quantityQualifiers;
	}

	public void setQuantityQualifiers(List<DrugSig> quantityQualifiers) {
	this.quantityQualifiers = quantityQualifiers;
	}

	public List<DrugSig> getDirections() {
	return directions;
	}

	public void setDirections(List<DrugSig> directions) {
	this.directions = directions;
	}

	public List<DrugSig> getQuantities() {
	return quantities;
	}

	public void setQuantities(List<DrugSig> quantities) {
	this.quantities = quantities;
	}

	public Collection<PatientChiefComplaint> getPatientChiefComplaints() {
	return patientChiefComplaints;
	}

	public void setPatientChiefComplaints(Collection<PatientChiefComplaint> patientChiefComplaints) {
	this.patientChiefComplaints = patientChiefComplaints;
	}

	public void removeRx(PatientRx patientRx) {
	((BindingListModelList) rxSectionDisplayGrid.getModel()).remove(patientRx);
	Events.postEvent(Events.ON_CHANGE, rxSectionDisplayGrid, null);
	}

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	mainCtl = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
	section = (RxSection) mainCtl.getSoapSection(RxSection.class);
	quantityQualifiers = commonCrudService.searchByExample(new DrugSig("QUANTITY_QUALIFIER"));
	directions = commonCrudService.searchByExample(new DrugSig("FREQUENCY"));
	quantities = commonCrudService.searchByExample(new DrugSig("QUANTITY"));
	ChiefComplainSection ccSection = (ChiefComplainSection) mainCtl.getSoapSection(ChiefComplainSection.class, false);
	if (ccSection != null) {
		patientChiefComplaints = ccSection.getPatientChiefComplaints();
	}
	DiagnosisSection dxSection = (DiagnosisSection) mainCtl.getSoapSection(DiagnosisSection.class, false);
	if (dxSection != null) patientIcds = dxSection.getIcds();
	return super.doBeforeCompose(page, parent, compInfo);
	}

	public ProviderService getProviderService() {
	return providerService;
	}

	public void setProviderService(ProviderService providerService) {
	this.providerService = providerService;
	}

	public Listbox getRxSectionDisplayGrid() {
	return rxSectionDisplayGrid;
	}

	public void setRxSectionDisplayGrid(Listbox rxSectionDisplayGrid) {
	this.rxSectionDisplayGrid = rxSectionDisplayGrid;
	}

	public Drug getDrug() {
	return drug;
	}

	public void setDrug(Drug drug) {
	this.drug = drug;
	}

	public void openRxSectionExt(){
	Map<String, Object> m = new HashMap<String, Object>();
  	m.put("sectionName","RxSection");
  	m.put("patientSoapNote",this.getMainCtl().getSoapNote());
  	Window w = ((Window)Executions.createComponents("/soap/medEntry.zul",null,m));
    w.addForward("onDetach", rxSectionDisplayGrid, "onReloadRequest");
	w.addEventListener("onDetach", new EventListener() {
		
		@Override
		public void onEvent(Event event) throws Exception {
		RxSection rxSection = (RxSection) soapNoteService.getSoapSection(mainCtl.getSoapNote(), RxSection.class); 
		mainCtl.markEdited(rxSection);
		MedicationHistorySection historySection = (MedicationHistorySection) soapNoteService.getSoapSection(mainCtl.getSoapNote(), MedicationHistorySection.class);
		mainCtl.markEdited(historySection);
		AllergySection allergySection = (AllergySection) soapNoteService.getSoapSection(mainCtl.getSoapNote(),AllergySection.class);
		mainCtl.markEdited(allergySection);
		}
	});
	}
	
}