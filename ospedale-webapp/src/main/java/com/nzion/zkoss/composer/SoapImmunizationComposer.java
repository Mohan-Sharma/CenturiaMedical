package com.nzion.zkoss.composer;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;

import com.nzion.domain.File;
import com.nzion.domain.Folder;
import com.nzion.domain.Patient;
import com.nzion.domain.emr.Immunization;
import com.nzion.domain.emr.VaccineLot;
import com.nzion.domain.emr.soap.ImmunizationSection;
import com.nzion.domain.emr.soap.PatientImmunization;
import com.nzion.service.VaccineService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.service.impl.FileBasedServiceImpl;

public class SoapImmunizationComposer extends OspedaleAutowirableComposer {

	private Patient patient;

	private Immunization immunization;

	private PatientSoapNoteController patientSoapNoteController;

	private VaccineService vaccineService;
	
	List<VaccineLot> vaccineLots;
	
	private CommonCrudService commonCrudService;
	
	private FileBasedServiceImpl fileBasedServiceImpl;
	
	
	public String getConsentFormLink() {return null;
	}

	public void saveConsent(File consentDocument) {
		Set<Folder> folders = commonCrudService.getRootFolder(patient).getChildFolders();
		Folder vaccineFolder = null;
		for(Folder f : folders) {
			if(f.getName().equals("Vaccine Consents")) {
				vaccineFolder=f;
			}
		}
		consentDocument.setFolder(vaccineFolder);
		fileBasedServiceImpl.createFile(consentDocument);
	}


	public PatientImmunization getPatientImmunizationFor() {
	ImmunizationSection immunizationSection = (ImmunizationSection) patientSoapNoteController.getSelectedSection();
	Set<PatientImmunization> allPatientImmunizations = immunizationSection.getImmunizations();
	for (PatientImmunization patientImmunization : allPatientImmunizations)
		if (immunization.equals(patientImmunization.getImmunization())) {
			return patientImmunization;
		}
	PatientImmunization patientImmunization = new PatientImmunization(immunization);
	immunizationSection.addPatientImmunization(patientImmunization);
	return patientImmunization;
	}

	public Set<PatientImmunization> filteredPatientImmunizationByStatus() {
	ImmunizationSection immunizationSection = (ImmunizationSection) patientSoapNoteController.getSelectedSection();
	Set<PatientImmunization> patientImmunizations = immunizationSection.getImmunizations();
	Set<PatientImmunization> filteredImmunizations = new HashSet<PatientImmunization>();
	for (PatientImmunization patientImmunization : patientImmunizations)
		if ("ADMINISTERED".equalsIgnoreCase(patientImmunization.getStatus())) {
			filteredImmunizations.add(patientImmunization);
		}
	return filteredImmunizations;
	}
	
	public String uploadConsentDocument(Event event){
	Media media = ((UploadEvent) event).getMedia();
	String fileName = media.getName();
	com.nzion.domain.File  consentDocument = new com.nzion.domain.File();
	consentDocument.setFileName(fileName);
	consentDocument.setDescription("Consent for Vaccine " + immunization.getFullVaccineName());
	consentDocument.setFileType(media.getContentType());
	if (media.isBinary())
		consentDocument.setInputStream(media.getStreamData());
	else
		consentDocument.setInputStream(new ByteArrayInputStream(media.getStringData().getBytes()));
	return fileName;
	
	}

	public Patient getPatient() {
	return patient;
	}

	public void setPatient(Patient patient) {
	this.patient = patient;
	}

	public Immunization getImmunization() {
	return immunization;
	}

	public void setImmunization(Immunization immunization) {
	this.immunization = immunization;
	}

	public PatientSoapNoteController getPatientSoapNoteController() {
	return patientSoapNoteController;
	}

	public void setPatientSoapNoteController(PatientSoapNoteController patientSoapNoteController) {
	this.patientSoapNoteController = patientSoapNoteController;
	}

	public VaccineService getVaccineService() {
	return vaccineService;
	}

	public void setVaccineService(VaccineService vaccineService) {
	this.vaccineService = vaccineService;
	}

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}
	
	public List<VaccineLot> getVaccineLots() {
	vaccineLots = vaccineService.getVaccineLotFor(immunization);
	return vaccineLots;
	}

	public void decementDosageRemaining(VaccineLot vaccineLot){
	if(vaccineLot == null)
		return;
	vaccineLot.setDosesRemaining(vaccineLot.getDosesRemaining()-1);
	commonCrudService.save(vaccineLot);
	}
	
	public FileBasedServiceImpl getFileBasedServiceImpl() {
	return fileBasedServiceImpl;
	}

	public void setFileBasedServiceImpl(FileBasedServiceImpl fileBasedServiceImpl) {
	this.fileBasedServiceImpl = fileBasedServiceImpl;
	}

	
	private static final long serialVersionUID = 1L;

}
