package com.nzion.zkoss.composer.emr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.nzion.zkoss.composer.OspedaleAutowirableComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;

import com.nzion.domain.Patient;
import com.nzion.domain.emr.IcdElement;
import com.nzion.domain.emr.IcdGroup;
import com.nzion.domain.emr.soap.DiagnosisSection;
import com.nzion.domain.emr.soap.DifferentialDiagnosisSection;
import com.nzion.domain.emr.soap.PatientIcd;
import com.nzion.domain.emr.soap.PreliminaryDiagnosisSection;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.emr.IcdService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilValidator;
import com.nzion.zkoss.composer.PatientSoapNoteController;

public class IcdSearchController extends OspedaleAutowirableComposer {

	private IcdGroup icdGroup;

	private String icdCode;

	private String icdDescription;

	private IcdService icdService;

	private List<IcdElement> icds = new ArrayList<IcdElement>();

	private CommonCrudService commonCrudService;

	private String searchCriteria;

	private List<String> searchCriterias = Arrays.asList("Favourite", "PatientICD");

	private boolean fromGroup;

	private PatientSoapNoteController patientSoapNoteController;

	private DiagnosisSection diagnosisSection = null;
	
	private PreliminaryDiagnosisSection preliminaryDiagnosisSection = null;
	
	private DifferentialDiagnosisSection differentialDiagnosisSection =null;

	private Set<IcdElement> icdElements;
	
	private boolean multipleSelectionRequire = false;

	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	patientSoapNoteController = (PatientSoapNoteController)Executions.getCurrent().getDesktop().getAttribute("controller");
	if(patientSoapNoteController == null)
	patientSoapNoteController = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
	diagnosisSection = (DiagnosisSection) Executions.getCurrent().getArg().get("section");
	preliminaryDiagnosisSection = (PreliminaryDiagnosisSection) Executions.getCurrent().getArg().get("preliminaryDiagnosisSection");
	differentialDiagnosisSection = (DifferentialDiagnosisSection) Executions.getCurrent().getArg().get("differentialDiagnosisSection");
	if("true".equalsIgnoreCase(Executions.getCurrent().getArg().get("multiple").toString()))
	multipleSelectionRequire = true;
	return super.doBeforeCompose(page, parent, compInfo);
	}

	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	}

	public List<IcdElement> lookUpIcd() {
	if (searchCriterias.contains(searchCriteria)) {
		icds.clear();
		boolean fromFavourite = false;
		if ("Favourite".equalsIgnoreCase(searchCriteria)) fromFavourite = true;
		Patient patientTmp = patientSoapNoteController == null ?  null : patientSoapNoteController.getPatient();
		icds = icdService.lookUpIcd(fromFavourite, Infrastructure.getUserLogin().getPerson(), icdCode, icdDescription,patientTmp);
		return icds;
	}
	if (fromGroup) {
		icdGroup = commonCrudService.getById(IcdGroup.class, icdGroup.getId());
		icds.clear();
		icds = new ArrayList<IcdElement>(icdGroup.getIcdElements());
		return icds;
	}
	icds.clear();
	icds = icdService.searchIcdBy(icdCode, icdDescription);
	return icds;
	}

	public void addIcdToSection() {
	if(UtilValidator.isEmpty(icdElements))
		return;
	for (IcdElement icd : icdElements) {
		PatientIcd patientIcd = new PatientIcd(icd);
		diagnosisSection.addIcd(patientIcd);
	 }
	}
	
	public void addIcdToPreliminaryDiagnosisSection() {
		if(UtilValidator.isEmpty(icdElements))
			return;
		for (IcdElement icd : icdElements) {
			PatientIcd patientIcd = new PatientIcd(icd);
			preliminaryDiagnosisSection.addIcd(patientIcd,patientSoapNoteController.getSoapNote());
		 }
	}
	
	public void addIcdToDifferentialDiagnosisSection() {
		if(UtilValidator.isEmpty(icdElements))
			return;
		for (IcdElement icd : icdElements) {
			PatientIcd patientIcd = new PatientIcd(icd);
			differentialDiagnosisSection.addIcd(patientIcd,patientSoapNoteController.getSoapNote());
		 }
	}
	
	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public IcdGroup getIcdGroup() {
	return icdGroup;
	}

	public void setIcdGroup(IcdGroup icdGroup) {
	this.icdGroup = icdGroup;
	}

	public String getIcdCode() {
	return icdCode;
	}

	public void setIcdCode(String icdCode) {
	this.icdCode = icdCode;
	}

	public String getIcdDescription() {
	return icdDescription;
	}

	public void setIcdDescription(String icdDescription) {
	this.icdDescription = icdDescription;
	}

	public IcdService getIcdService() {
	return icdService;
	}

	public void setIcdService(IcdService icdService) {
	this.icdService = icdService;
	}

	public List<IcdElement> getIcds() {
	return icds;
	}

	public void setIcds(List<IcdElement> icds) {
	this.icds = icds;
	}

	public String getSearchCriteria() {
	return searchCriteria;
	}

	public void setSearchCriteria(String searchCriteria) {
	this.searchCriteria = searchCriteria;
	}

	public boolean isFromGroup() {
	return fromGroup;
	}

	public void setFromGroup(boolean fromGroup) {
	this.fromGroup = fromGroup;
	}

	public PatientSoapNoteController getPatientSoapNoteController() {
	return patientSoapNoteController;
	}

	public void setPatientSoapNoteController(PatientSoapNoteController patientSoapNoteController) {
	this.patientSoapNoteController = patientSoapNoteController;
	}

	public Set<IcdElement> getIcdElements() {
	return icdElements;
	}

	public void setIcdElements(Set<IcdElement> icdElements) {
	this.icdElements = icdElements;
	}

	public boolean isMultipleSelectionRequire() {
	return multipleSelectionRequire;
	}

	public void setMultipleSelectionRequire(boolean multipleSelectionRequire) {
	this.multipleSelectionRequire = multipleSelectionRequire;
	}
	private static final long serialVersionUID = 1L;

}
