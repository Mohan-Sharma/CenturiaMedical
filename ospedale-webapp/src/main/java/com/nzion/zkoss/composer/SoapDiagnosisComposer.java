package com.nzion.zkoss.composer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nzion.domain.person.PersonProcedure;
import com.nzion.domain.person.ProcedureGroup;
import com.nzion.service.PersonService;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.dto.ProviderFavoriteProcedureDto;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zul.A;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;

import com.nzion.domain.Enumeration;
import com.nzion.domain.emr.soap.DiagnosisSection;
import com.nzion.domain.emr.soap.DifferentialDiagnosisSection;
import com.nzion.domain.emr.soap.PatientCpt;
import com.nzion.domain.emr.soap.PatientIcd;
import com.nzion.domain.emr.soap.PreliminaryDiagnosisSection;
import com.nzion.domain.emr.soap.ProblemListSection;
import com.nzion.domain.pms.Modifier;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.common.EnumerationService;
import com.nzion.util.UtilMessagesAndPopups;

public class SoapDiagnosisComposer extends OspedaleAutowirableComposer {

	SoapNoteService soapNoteService;
	PatientSoapNoteController mainCtl;
	DiagnosisSection section;
	PreliminaryDiagnosisSection preliminaryDiagnosisSection;
	DifferentialDiagnosisSection differentialDiagnosisSection;

    private PersonService personService;

	private CommonCrudService commonCrudService;

	private Set<PatientCpt> associatedCpts = new HashSet<PatientCpt>();

	private PatientIcd patientIcd;

    private List<ProviderFavoriteProcedureDto> providerFavoriteProcedureDtos;

	Popup associateCptPopup;

	EnumerationService enumerationService;

	public Set<PatientCpt> getAssociatedCpts() {
	return associatedCpts;
	}

	public void setAssociatedCpts(Set<PatientCpt> associatedCpts) {
	this.associatedCpts = associatedCpts;
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	}

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	mainCtl = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
	section = (DiagnosisSection) mainCtl.getSoapSection(DiagnosisSection.class);
	
	preliminaryDiagnosisSection = (PreliminaryDiagnosisSection) soapNoteService.getSoapSection(mainCtl.getSoapNote(), PreliminaryDiagnosisSection.class);
	if(preliminaryDiagnosisSection == null)
		preliminaryDiagnosisSection = new PreliminaryDiagnosisSection();
	
	differentialDiagnosisSection = (DifferentialDiagnosisSection) soapNoteService.getSoapSection(mainCtl.getSoapNote(), DifferentialDiagnosisSection.class);
	if(differentialDiagnosisSection == null)
		differentialDiagnosisSection = new DifferentialDiagnosisSection();
	return super.doBeforeCompose(page, parent, compInfo);
	}

	public void saveSection(Event event,Datebox datebox) {
	/*if( ((Checkbox)root.getFellowIfAny("deathChkBox",false)) != null && ((Checkbox)root.getFellowIfAny("deathChkBox",false)).isChecked() && section.getSoapNote().getPatient().getDateOfDeath() == null ){
		UtilMessagesAndPopups.showError("Date of death is required");
		return;
	}*/
	commonCrudService.save(section.getSoapNote().getPatient());
	//commonCrudService.save(preliminaryDiagnosisSection);
	mainCtl.saveSoapSection();
    this.setProviderFavoriteProcedureDtos(this.getAllProviderFavoriteProcedureDto());
	}


    public DiagnosisSection addPatientProcedure(ProviderFavoriteProcedureDto providerFavoriteProcedureDto){
        SoapDiagnosisComposer diagComposer = new SoapDiagnosisComposer();
        DiagnosisSection section = (DiagnosisSection) this.getSection();
        if(UtilValidator.isNotEmpty(providerFavoriteProcedureDto.getProviderProcedureId())){
            PersonProcedure personProcedure = commonCrudService.findUniqueByEquality(PersonProcedure.class, new String[]{"id"}, new Object[]{providerFavoriteProcedureDto.getProviderProcedureId()});
            updateProcedureOrderSection(section, personProcedure);
        }
        if(UtilValidator.isNotEmpty(providerFavoriteProcedureDto.getProcedureGroupId())){
            List<PersonProcedure> personProcedures = commonCrudService.findByEquality(PersonProcedure.class, new String[]{"procedureGroup.id"}, new Object[]{providerFavoriteProcedureDto.getProcedureGroupId()} );
            for(PersonProcedure personProcedure : personProcedures){
                updateProcedureOrderSection(section, personProcedure);
            }
        }
        return section;
    }

    private void updateProcedureOrderSection(DiagnosisSection section, PersonProcedure personProcedure ){
        if(personProcedure == null)
            return;
        PatientCpt patientCpt = new PatientCpt();
        patientCpt.setPatient(mainCtl.getPatient());
        patientCpt.setCptStatus(PatientCpt.CPTSTATUS.NEW);
        patientCpt.setCpt(personProcedure.getProcedure());
        patientCpt.setUnit(personProcedure.getUnit());
        section.addCpt(patientCpt);
    }

    public List<ProviderFavoriteProcedureDto> getAllProviderFavoriteProcedureDto() {
        if(mainCtl == null){
            mainCtl = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
            section = (DiagnosisSection) mainCtl.getSoapSection(DiagnosisSection.class);
        }

        providerFavoriteProcedureDtos = new ArrayList<ProviderFavoriteProcedureDto>();
        List<ProcedureGroup> procedureGroups = commonCrudService.findByEquality(ProcedureGroup.class, new String[]{"person"},new Object[] {mainCtl.getProvider()});
        for(ProcedureGroup pg : procedureGroups){
            ProviderFavoriteProcedureDto providerFavoriteProcedureDto = new ProviderFavoriteProcedureDto();
            providerFavoriteProcedureDto.setProcedureGroupId(pg.getId());
            providerFavoriteProcedureDto.setName(pg.getProcedureGroupName());
            providerFavoriteProcedureDtos.add(providerFavoriteProcedureDto);
        }
        List<PersonProcedure> personProcedures = commonCrudService.findByEquality(PersonProcedure.class, new String[]{"person"},new Object[] {mainCtl.getProvider()});
        for(PersonProcedure pp : personProcedures){
            ProviderFavoriteProcedureDto providerFavoriteProcedureDto = new ProviderFavoriteProcedureDto();
            providerFavoriteProcedureDto.setProviderProcedureId(pp.getId());
            providerFavoriteProcedureDto.setName(pp.getProcedure().getDescription());
            providerFavoriteProcedureDtos.add(providerFavoriteProcedureDto);
        }
        return providerFavoriteProcedureDtos;
    }

    public void searchProviderFavoriteProcedureDtosByName(String name) {
        providerFavoriteProcedureDtos = new ArrayList<ProviderFavoriteProcedureDto>();
        List<ProcedureGroup> procedureGroups = personService.searchPersonFavouriteProcedureGroup(name,mainCtl.getProvider());
        for(ProcedureGroup pg : procedureGroups){
            ProviderFavoriteProcedureDto providerFavoriteProcedureDto = new ProviderFavoriteProcedureDto();
            providerFavoriteProcedureDto.setProcedureGroupId(pg.getId());
            providerFavoriteProcedureDto.setName(pg.getProcedureGroupName());
            providerFavoriteProcedureDtos.add(providerFavoriteProcedureDto);
        }
        List<PersonProcedure> personProcedures = personService.searchPersonFavouriteProcedures(name,mainCtl.getProvider());
        for(PersonProcedure pp : personProcedures){
            ProviderFavoriteProcedureDto providerFavoriteProcedureDto = new ProviderFavoriteProcedureDto();
            providerFavoriteProcedureDto.setProviderProcedureId(pp.getId());
            providerFavoriteProcedureDto.setName(pp.getProcedure().getDescription());
            providerFavoriteProcedureDtos.add(providerFavoriteProcedureDto);
        }
    }

    public List<ProviderFavoriteProcedureDto> getProviderFavoriteProcedureDtos() {
        return providerFavoriteProcedureDtos;
    }

    public void setProviderFavoriteProcedureDtos(List<ProviderFavoriteProcedureDto> providerFavoriteProcedureDtos) {
        this.providerFavoriteProcedureDtos = providerFavoriteProcedureDtos;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public DiagnosisSection getSection() {
	return section;
	}

	public void setSection(DiagnosisSection section) {
	this.section = section;
	}

	public PatientSoapNoteController getMainCtl() {
	return mainCtl;
	}

	public void setMainCtl(PatientSoapNoteController mainCtl) {
	this.mainCtl = mainCtl;
	}

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public List<Modifier> getAllModifiers() {
	return commonCrudService.getAll(Modifier.class);
	}

	public SoapNoteService getSoapNoteService() {
	return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
	this.soapNoteService = soapNoteService;
	}

	public void getAssociatedCptWith() {
	if(patientIcd!=null)
	associatedCpts = patientIcd.getCpts();
	}

	public void associateCptWithIcd() {
	patientIcd.setCpts(associatedCpts);
	}

	public PatientIcd getPatientIcd() {
	return patientIcd;
	}

	public void setPatientIcd(PatientIcd patientIcd) {
	this.patientIcd = patientIcd;
	}

	public PreliminaryDiagnosisSection getPreliminaryDiagnosisSection() {
		return preliminaryDiagnosisSection;
	}

	public void setPreliminaryDiagnosisSection(
			PreliminaryDiagnosisSection preliminaryDiagnosisSection) {
		this.preliminaryDiagnosisSection = preliminaryDiagnosisSection;
	}
	
	public DifferentialDiagnosisSection getDifferentialDiagnosisSection() {
		return differentialDiagnosisSection;
	}

	public void setDifferentialDiagnosisSection(
			DifferentialDiagnosisSection differentialDiagnosisSection) {
		this.differentialDiagnosisSection = differentialDiagnosisSection;
	}

	public void buildCombobox(Combobox parentCombobox, PatientCpt patientCpt, int quantifier) {
	Comboitem selectedComboitem = null;
	for (Modifier modifier : getAllModifiers()) {
		Comboitem comboitem = new Comboitem(modifier.getCode());
		comboitem.setValue(modifier);
		comboitem.setParent(parentCombobox);
		comboitem.setDescription(modifier.getDescription());
		if (quantifier == 1 && patientCpt.getModifier1() != null && patientCpt.getModifier1().equals(modifier))
			selectedComboitem = comboitem;
		if (quantifier == 2 && patientCpt.getModifier2() != null && patientCpt.getModifier2().equals(modifier))
			selectedComboitem = comboitem;
		if (quantifier == 3 && patientCpt.getModifier3() != null && patientCpt.getModifier3().equals(modifier))
			selectedComboitem = comboitem;
		if (quantifier == 4 && patientCpt.getModifier4() != null && patientCpt.getModifier4().equals(modifier))
			selectedComboitem = comboitem;
	}
	if (selectedComboitem != null) parentCombobox.setSelectedItem(selectedComboitem);
	}

	public void addModifierToPatientCpt(Combobox combobox, int quantifier,PatientCpt cpt) {
	PatientCpt patientCpt = cpt;
	Modifier modifier = (Modifier) combobox.getSelectedItem().getValue();
	if (checkForDuplication(modifier, patientCpt,quantifier) && patientCpt.isModifierAdded()) {
		UtilMessagesAndPopups.showError("Modifier Already Selected For this CPT");
		if (quantifier == 1 && modifier.equals(patientCpt.getModifier1()))
			patientCpt.setModifier1(null);
		if (quantifier == 2 && modifier.equals(patientCpt.getModifier2())) 
			patientCpt.setModifier2(null);
		if (quantifier == 3 && modifier.equals(patientCpt.getModifier3()))
			patientCpt.setModifier3(null);
		if (quantifier == 4 && modifier.equals(patientCpt.getModifier4())) 
			patientCpt.setModifier4(null);
		combobox.setValue("");
		return;
	}
	patientCpt.setModifierAdded(true);
	if (quantifier == 1) patientCpt.setModifier1(modifier);
	if (quantifier == 2) patientCpt.setModifier2(modifier);
	if (quantifier == 3) patientCpt.setModifier3(modifier);
	if (quantifier == 4) patientCpt.setModifier4(modifier);
	}

	private boolean checkForDuplication(Modifier modifier, PatientCpt patientCpt, int quantifier) {
	if(patientCpt.isModifierAdded()){
		if(quantifier == 1 && (modifier.equals(patientCpt.getModifier2()) || modifier.equals(patientCpt.getModifier3()) || modifier.equals(patientCpt.getModifier4())))
			return true;
		if(quantifier == 2 && (modifier.equals(patientCpt.getModifier1()) || modifier.equals(patientCpt.getModifier3()) || modifier.equals(patientCpt.getModifier4())))
			return true;
		if(quantifier == 3 && (modifier.equals(patientCpt.getModifier1()) || modifier.equals(patientCpt.getModifier2()) || modifier.equals(patientCpt.getModifier4())))
			return true;
		if(quantifier == 4 && (modifier.equals(patientCpt.getModifier1()) || modifier.equals(patientCpt.getModifier3()) || modifier.equals(patientCpt.getModifier2())))
			return true;		
	}
	return false;
	}

	public ListitemRenderer getIcdRenderer() {
	return new ListitemRenderer() {
		@Override
		public void render(final Listitem item, Object data,int index) throws Exception {
		final PatientIcd patientIcd = (PatientIcd) data;
		item.setValue(patientIcd);
		Listcell codeListCell = new Listcell(patientIcd.getIcdElement().getCode());
		codeListCell.setParent(item);
		Listcell descriptionListCell = new Listcell(patientIcd.getIcdElement().getDescription());
		descriptionListCell.setParent(item);
		Listcell statusListcell = new Listcell();
		statusListcell.setParent(item);
		Combobox statusCombobox = new Combobox();
		statusCombobox.setParent(statusListcell);
		buildStatusCombobox(statusCombobox, patientIcd, "PROBLEM_STATUS_CODE");
		statusCombobox.addEventListener("onSelect", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
			Combobox combobox = (Combobox) event.getTarget();
			patientIcd.setStatus((Enumeration) combobox.getSelectedItem().getValue());
			}
		});

		Listcell severityListcell = new Listcell();
		severityListcell.setParent(item);
		Combobox severityCombobox = new Combobox();
		severityCombobox.setParent(severityListcell);
		buildSeverityCombobox(severityCombobox, patientIcd, "ILLNESS_SEVERITY");
		severityCombobox.addEventListener("onSelect", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
			Combobox combobox = (Combobox) event.getTarget();
			patientIcd.setSeverity((Enumeration) combobox.getSelectedItem().getValue());
			}
		});
		Listcell cptListcell = new Listcell();
		cptListcell.setParent(item);
		final A cptLink = new A();
		cptLink.setClass("addBtn");
		cptLink.setPopup(associateCptPopup);
		cptLink.addEventListener("onClick", new EventListener() {
		@Override
		public void onEvent(Event event) throws Exception {
		SoapDiagnosisComposer.this.patientIcd = (PatientIcd)((Listitem)cptLink.getParent().getParent()).getValue();
		getAssociatedCptWith();
		}
		});
		cptLink.setParent(cptListcell);
		Label label = new Label(patientIcd.getCptDescription());
		label.setParent(cptListcell);
		}
	};
	
	}

	private void buildStatusCombobox(Combobox parentCombobox, PatientIcd patientIcd, String enumType) {
	Comboitem selectedItem = null;
	for (Enumeration enumeration : getActiveAndChronicIllnessStatuses(enumType)) {
		Comboitem comboitem = new Comboitem(enumeration.getDescription());
		comboitem.setValue(enumeration);
		comboitem.setParent(parentCombobox);
		if (patientIcd.getStatus() != null && enumeration.equals(patientIcd.getStatus())) 
				selectedItem = comboitem;
		 else if (("Active".equalsIgnoreCase(enumeration.getDescription()))) 
				selectedItem = comboitem;
	}
	patientIcd.setStatus((Enumeration)selectedItem.getValue());
	parentCombobox.setSelectedItem(selectedItem);
	}

	private void buildSeverityCombobox(Combobox parentCombobox, PatientIcd patientIcd, String enumType) {
	Comboitem selectedItem = null;
	for (Enumeration enumeration : getIllnessSeveritiesExcludingChronic(enumType)) {
		Comboitem comboitem = new Comboitem(enumeration.getDescription());
		comboitem.setValue(enumeration);
		comboitem.setParent(parentCombobox);
		if (patientIcd.getStatus() != null && enumeration.equals(patientIcd.getSeverity())) 
			selectedItem = comboitem;
		 else if ("Acute".equalsIgnoreCase(enumeration.getDescription())) 
				selectedItem = comboitem;
	}
	patientIcd.setSeverity((Enumeration)selectedItem.getValue());
	parentCombobox.setSelectedItem(selectedItem);
	}

	public EnumerationService getEnumerationService() {
	return enumerationService;
	}

	public void setEnumerationService(EnumerationService enumerationService) {
	this.enumerationService = enumerationService;
	}
	
	public List<Enumeration> getActiveAndChronicIllnessStatuses(String enumType){
	List<Enumeration> enumerations = enumerationService.getRelevantEnumerationsByType(enumType);
	List<Enumeration> filteredStatus = new ArrayList<Enumeration>();
	for(Enumeration enumeration : enumerations)
		if("Active".equalsIgnoreCase(enumeration.getDescription()) || "Chronic".equalsIgnoreCase(enumeration.getDescription()))
			filteredStatus.add(enumeration);
	return filteredStatus;
	}
	
	public List<Enumeration> getIllnessSeveritiesExcludingChronic(String enumType){
	List<Enumeration> enumerations = enumerationService.getRelevantEnumerationsByType(enumType);
	List<Enumeration> filteredSeverities = new ArrayList<Enumeration>();
	for(Enumeration enumeration : enumerations)
		if(!"Chronic".equalsIgnoreCase(enumeration.getDescription()))
			filteredSeverities.add(enumeration);
	return filteredSeverities;
	}
	
	public void addSelectedIcdToProblemList(final PatientIcd patientIcd){
	final ProblemListSection problemListSection = (ProblemListSection) mainCtl.getSoapSection(ProblemListSection.class,false);
	UtilMessagesAndPopups.confirm("Do you want to proceed?", "Add to problem list", Messagebox.YES|Messagebox.NO,Messagebox.QUESTION, new EventListener() {
		
		@Override
		public void onEvent(Event event) throws Exception {
			if("onYes".equalsIgnoreCase(event.getName())){
				patientIcd.setProblemListSection(problemListSection);
				commonCrudService.save(section);
				UtilMessagesAndPopups.showMessage("Diagnosis  added to Problem List Section.");
			}
		}
	});
	}
	
	public void addPatientIcd(PatientIcd patientIcd,Textbox icdLookupTextBox){
	if(patientIcd.getIcdElement() == null){
		UtilMessagesAndPopups.displayError("Diagnosis cannot be empty. Choose Diagnosis.");
		return;
	}
	if(patientIcd.getId()==null){
		section.addIcd(patientIcd);
		getMainCtl().saveSoapSection();
		Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
		return;
	}
	commonCrudService.save(patientIcd);	
	Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
	}
	
	public void addPatientIcdToSection(PatientIcd patientIcd){
		patientIcd.setId(null);
		patientIcd.setDifferentialDiagnosisSection(null);
		section.addIcd(patientIcd);
		getMainCtl().saveSoapSection();
		Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
		return;
	}
	
	public void addPreliminaryPatientIcd(PatientIcd patientIcd,Textbox icdLookupTextBox){
		if(patientIcd.getIcdElement() == null){
			UtilMessagesAndPopups.displayError("Diagnosis cannot be empty. Choose Diagnosis.");
			return;
		}
		if(patientIcd.getId()==null){
			preliminaryDiagnosisSection.addIcd(patientIcd,mainCtl.getSoapNote());
			preliminaryDiagnosisSection.setSoapNote(mainCtl.getSoapNote());
			//preliminaryDiagnosisSection.setSoapModule(mainCtl.getSelectedModule());
			commonCrudService.save(preliminaryDiagnosisSection);
			getMainCtl().saveSoapSection();
			Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
			return;
		}
		commonCrudService.save(patientIcd);	
		Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
	}
	
	public void addDifferentialPatientIcd(PatientIcd patientIcd,Textbox icdLookupTextBox){
		if(patientIcd.getIcdElement() == null){
			UtilMessagesAndPopups.displayError("Diagnosis cannot be empty. Choose Diagnosis.");
			return;
		}
		if(patientIcd.getId()==null){
			differentialDiagnosisSection.addIcd(patientIcd,mainCtl.getSoapNote());
			differentialDiagnosisSection.setSoapNote(mainCtl.getSoapNote());
			//preliminaryDiagnosisSection.setSoapModule(mainCtl.getSelectedModule());
			commonCrudService.save(differentialDiagnosisSection);
			getMainCtl().saveSoapSection();
			Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
			return;
		}
		commonCrudService.save(patientIcd);	
		Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
	}
	
	public void addPatientCpt(PatientCpt patientCpt,HtmlMacroComponent component){
	if(patientCpt.getCpt() == null){
		UtilMessagesAndPopups.displayError("CPT cannot be empty.Choose CPT");
		return;
	}
	if(patientCpt.getId()==null){
		section.addCpt(patientCpt);
		getMainCtl().saveSoapSection();
		//Events.postEvent("onClick",(Component)component.getDesktop().getAttribute("wkModule"),null);
		return;
	}
	commonCrudService.save(patientCpt);	
	//Events.postEvent("onClick",(Component)component.getDesktop().getAttribute("wkModule"),null);
	}

	private static final long serialVersionUID = 1L;
}