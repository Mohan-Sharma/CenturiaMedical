package com.nzion.zkoss.composer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zul.Listbox;

import com.nzion.domain.Enumeration;
import com.nzion.domain.emr.Allergy;
import com.nzion.domain.emr.soap.AllergySection;
import com.nzion.domain.emr.soap.MedicationHistorySection;
import com.nzion.domain.emr.soap.PatientAllergy;
import com.nzion.domain.emr.soap.PatientRx;
import com.nzion.domain.emr.soap.PatientRxAlert;
import com.nzion.domain.emr.soap.RxSection;
import com.nzion.service.ProviderService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.EnumerationService;

public class SoapAllergyComposer extends OspedaleAutowirableComposer {

	private static final long serialVersionUID = 1L;
	private SoapNoteService soapNoteService;
	private List<PatientAllergy> allergies ;
	private Listbox allergyListbox;
	private PatientSoapNoteController mainCtl;
	private AllergySection allergySection;
	private CommonCrudService commonCrudService;
	private EnumerationService enumerationService;
	private ProviderService providerService;

	static List<Enumeration> allergyTypes = null;
	static List<Allergy> reactions = null;
	static List<Enumeration> sensitivities = null;

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	mainCtl = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
	allergySection = (AllergySection) mainCtl.getSoapSection(AllergySection.class);
	allergies = new ArrayList<PatientAllergy>();
	applyActiveFilter();
	return super.doBeforeCompose(page, parent, compInfo);
	}

	public void applyActiveFilter() {
	allergies.clear();
	Set<PatientAllergy> result = allergySection.getPatientAllergies();
	if (result != null && result.size() > 0) allergies.addAll(result);
	}

	public void applyInActiveFilter() {
	allergies.clear();
	Set<PatientAllergy> result = soapNoteService.getAllAllergies(mainCtl.getPatient());
	if (result != null && result.size() > 0) allergies.addAll(result);
	}

	public List<PatientRxAlert> runDrugAllergyCheck(){
		List<PatientRxAlert> alerts = new ArrayList<PatientRxAlert>();
		RxSection rxSection = (RxSection) mainCtl.getSoapSection(RxSection.class, false);
		Set<PatientRx> rxs = rxSection!=null?rxSection.getPatientRxs():new HashSet<PatientRx>();
		MedicationHistorySection medHxSection = (MedicationHistorySection) mainCtl.getSoapSection(MedicationHistorySection.class, false);
		rxs.addAll(medHxSection!=null?medHxSection.getPatientRxs():new HashSet<PatientRx>());
		for(Iterator<PatientRx> iter=rxs.iterator();iter.hasNext();){
			PatientRx rx = (PatientRx)iter.next();
		}
		return alerts;
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);

	}

	public SoapNoteService getSoapNoteService() {
	return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
	this.soapNoteService = soapNoteService;
	}

	public List<PatientAllergy> getAllergies() {
	return allergies;
	}

	public void setAllergies(List<PatientAllergy> allergies) {
	this.allergies = allergies;
	}

	public Listbox getAllergyListbox() {
	return allergyListbox;
	}

	public void setAllergyListbox(Listbox allergyListbox) {
	this.allergyListbox = allergyListbox;
	}

	public PatientSoapNoteController getMainCtl() {
	return mainCtl;
	}

	public void setMainCtl(PatientSoapNoteController mainCtl) {
	this.mainCtl = mainCtl;
	}

	public AllergySection getAllergySection() {
	return allergySection;
	}

	public void setAllergySection(AllergySection allergySection) {
	this.allergySection = allergySection;
	}

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public EnumerationService getEnumerationService() {
	return enumerationService;
	}

	public void setEnumerationService(EnumerationService enumerationService) {
	this.enumerationService = enumerationService;
	}

	public List<Enumeration> getAllergyTypes() {
	if (allergyTypes == null) allergyTypes = enumerationService.getGeneralEnumerationsByType("ALLERGY_TYPE");
	return allergyTypes;
	}

	public void setAllergyTypes(List<Enumeration> allergyTypes) {
	SoapAllergyComposer.allergyTypes = allergyTypes;
	}

	public List<Allergy> getReactions() {
	if (reactions == null) reactions = commonCrudService.getAll(com.nzion.domain.emr.Allergy.class);
	return reactions;
	}

	public void setReactions(List<Allergy> reactions) {
	SoapAllergyComposer.reactions = reactions;
	}

	public List<Enumeration> getSensitivities() {
	if (sensitivities == null) sensitivities = enumerationService.getGeneralEnumerationsByType("ALLERGY_SENSITIVITY");
	return sensitivities;
	}

	public void setSensitivities(List<Enumeration> sensitivities) {
	SoapAllergyComposer.sensitivities = sensitivities;
	}

	public ProviderService getProviderService() {
	return providerService;
	}

	public void setProviderService(ProviderService providerService) {
	this.providerService = providerService;
	}
	
	public boolean hasActiveAllergyInCurrentSection(){
	boolean hasActiveAllergy = false;
	Set<PatientAllergy> currentPatientAllergies = this.getAllergySection().getPatientAllergies();
	for(PatientAllergy patientAllergy : currentPatientAllergies){
		if(!("Inactive".equalsIgnoreCase(patientAllergy.getAllergyStatus().getDescription())) && !("Errorneous".equalsIgnoreCase(patientAllergy.getAllergyStatus().getDescription())))
			hasActiveAllergy = true;
	}
	return hasActiveAllergy;
	}
	
	public List<PatientAllergy> getAllInactiveAllergiesExcludingCurrentSection(){
	Enumeration inActiveAllergyStatus = enumerationService.findByEnumCodeAndEnumType("INACTIVE", "ALLERGY_STATUS");
	List<PatientAllergy> allergies = soapNoteService.getAllSoapRecordsExcludingCurrentSection(mainCtl.getPatient(), allergySection,PatientAllergy.class);
	List<PatientAllergy> inActiveAllergies = new ArrayList<PatientAllergy>();
	for(PatientAllergy allergy : allergies)
		if(inActiveAllergyStatus.equals(allergy.getAllergyStatus()))
			inActiveAllergies.add(allergy);
	return inActiveAllergies;
	}
	
	public List<PatientAllergy> getPatientAllergies(){
	Enumeration inActiveAllergyStatus = enumerationService.findByEnumCodeAndEnumType("INACTIVE", "ALLERGY_STATUS");
	List<PatientAllergy> allergies = soapNoteService.getAllSoapRecordsExcludingCurrentSection(mainCtl.getPatient(), allergySection,PatientAllergy.class);
	List<PatientAllergy> patientAllergies = new ArrayList<PatientAllergy>();
	for(PatientAllergy allergy : allergies)
		if(!inActiveAllergyStatus.equals(allergy.getAllergyStatus()))
			patientAllergies.add(allergy);
	return patientAllergies;
	}

}
