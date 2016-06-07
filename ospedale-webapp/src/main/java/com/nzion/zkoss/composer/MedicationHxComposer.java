package com.nzion.zkoss.composer;

import com.nzion.domain.Person;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.soap.*;
import com.nzion.domain.person.PersonDrug;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zul.Listbox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class MedicationHxComposer extends OspedaleAutowirableComposer {

	private PatientSoapNoteController mainCtl;
	private SoapNoteService soapNoteService;
	private List<PatientRx> patientRxs = new ArrayList<PatientRx>();
	private Listbox pastPatientRxsGrid;
	private MedicationHistorySection section;
	private CommonCrudService commonCrudService;
	private Drug selectedDrug;

	private final Person person = Infrastructure.getUserLogin().getPerson();

	public PatientSoapNoteController getMainCtl() {
	return mainCtl;
	}

	public void setMainCtl(PatientSoapNoteController mainCtl) {
	this.mainCtl = mainCtl;
	}

	public SoapNoteService getSoapNoteService() {
	return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
	this.soapNoteService = soapNoteService;
	}

	public List<PatientRx> getPatientRxs() {
	return patientRxs;
	}

	public void setPatientRxs(List<PatientRx> patientRxs) {
	this.patientRxs = patientRxs;
	}

	public Listbox getPastPatientRxsGrid() {
	return pastPatientRxsGrid;
	}

	public void setPastPatientRxsGrid(Listbox pastPatientRxsGrid) {
	this.pastPatientRxsGrid = pastPatientRxsGrid;
	}

	public MedicationHistorySection getSection() {
	return section;
	}

	public void setSection(MedicationHistorySection section) {
	this.section = section;
	}

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
	mainCtl = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
	section = (MedicationHistorySection) mainCtl.getSoapSection(MedicationHistorySection.class);
	if(UtilValidator.isEmpty(section.getPatientRxs()))
		section.setPatientRxs(new HashSet<PatientRx>());
	return super.doBeforeCompose(page, parent, compInfo);
	}


	@Override
	public void doAfterCompose(Component component) throws Exception {
	super.doAfterCompose(component);
	}

	public List<PatientRxAlert> getRxAlerts() {
	List<PatientRxAlert> alerts = new ArrayList<PatientRxAlert>();
	RxSection rxSection = (RxSection) mainCtl.getSoapSection(RxSection.class, false);
	AllergySection allergySection = (AllergySection) mainCtl.getSoapSection(AllergySection.class, false);
	for (Iterator<PatientRx> iter = this.section.getPatientRxs().iterator(); iter.hasNext();) {
		PatientRx rx = iter.next();
	}
	return alerts;
	}

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public void addToFavourite() {
	PersonDrug personDrug = new PersonDrug(person, selectedDrug);
	commonCrudService.save(personDrug);
	UtilMessagesAndPopups.showMessage("Drug Added To Favourite");
	}

	public Drug getSelectedDrug() {
	return selectedDrug;
	}

	public void setSelectedDrug(Drug selectedDrug) {
	this.selectedDrug = selectedDrug;
	}
	
	public void save(){
	mainCtl.saveSoapSection();
	}

	private static final long serialVersionUID = 1L;
}
