package com.nzion.zkoss.composer;

import java.io.InputStream;
import java.util.*;

import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.domain.emr.soap.SoapSection;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Space;
import org.zkoss.zul.Textbox;

import com.nzion.domain.File;
import com.nzion.domain.emr.soap.ImageSection;
import com.nzion.service.SoapNoteService;
import com.nzion.service.impl.FileBasedServiceImpl;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import com.nzion.view.ImageSectionFileValueObject;

public class ImageSectionComposer extends OspedaleAutowirableComposer implements ListitemRenderer{

	private static final long serialVersionUID = 1L;

	private ImageSection imageSection;

	private PatientSoapNoteController soapNoteController;

	private FileBasedServiceImpl fileBasedServiceImpl;

	private File file;

	private SoapNoteService soapNoteService;

	private Set<ImageSectionFileValueObject> valueObjects;

	private Set<ImageSectionFileValueObject> pastValueObject;

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
		soapNoteController = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
		if(soapNoteController != null){
			imageSection = (ImageSection) soapNoteController.getSoapSection(ImageSection.class);
			if(imageSection.getFiles() == null)
				imageSection.setFiles(new ArrayList<File>());
			file = new File();
			valueObjects =  populateValueObject();
			pastValueObject = populatePastValueObject();
		}
		return super.doBeforeCompose(page, parent, compInfo);
	}

	private Set<ImageSectionFileValueObject> populatePastValueObject() {
		if (UtilValidator.isNotEmpty(soapNoteController.getSoapNote())){
			Date lastEncounterDate;
			PatientSoapNote lastEncounter = soapNoteService.getLastEncounter(soapNoteController.getSoapNote().getPatient(), soapNoteController.getSoapNote().getDate());
			if(UtilValidator.isEmpty(lastEncounter)){
				LocalDate localDate = new LocalDate(soapNoteController.getSoapNote().getDate());
				lastEncounterDate = localDate.minusDays(1).toDate();
			} else {
				lastEncounterDate = lastEncounter.getDate();
			}
			List<SoapSection> imageSections = soapNoteService.getAllSoapSectionByPatient(soapNoteController.getSoapNote().getPatient(), lastEncounterDate, com.nzion.domain.emr.soap.ImageSection.class);
			if (UtilValidator.isEmpty(imageSections))
				return new HashSet<ImageSectionFileValueObject>();
			Set<ImageSectionFileValueObject> fileValueObjects = new HashSet<ImageSectionFileValueObject>();
			for(SoapSection imageSec : imageSections) {
				for (File file : ((ImageSection)imageSec).getFiles())
					fileValueObjects.add(new ImageSectionFileValueObject(file, ((ImageSection)imageSec).getSoapNote().getDate()));
			}
			return fileValueObjects;
		}
		return null;
	}

	private Set<ImageSectionFileValueObject> populateValueObject(){
		ImageSection imageSection = (ImageSection) soapNoteService.getSoapSection(soapNoteController.getSoapNote(), com.nzion.domain.emr.soap.ImageSection.class);

		if(UtilValidator.isEmpty(imageSection))
			return new HashSet<ImageSectionFileValueObject>();
		Set<ImageSectionFileValueObject> fileValueObjects = new HashSet<ImageSectionFileValueObject>();
		for(File file : imageSection.getFiles())
			fileValueObjects.add(new ImageSectionFileValueObject(file,imageSection.getSoapNote().getDate()));
		return fileValueObjects;
	}

	public void setFileBasedServiceImpl(FileBasedServiceImpl fileBasedServiceImpl) {
		this.fileBasedServiceImpl = fileBasedServiceImpl;
	}

	public void uploadFile(File file, String fileName,String fileType,InputStream inputStream){
		file.setFileType(fileType);
		file.setInputStream(inputStream);
		file.setFileName(fileName);
		((Textbox)root.getFellow("fileNameTxtBox")).setValue(file.getFileName());
		try{
			fileBasedServiceImpl.createFilesForImageSection(file, soapNoteController.getSoapNote().getPatient());
		}catch(Exception e){
			fileBasedServiceImpl.createDefaultFolderStructure(soapNoteController.getSoapNote().getPatient());
			fileBasedServiceImpl.createFilesForImageSection(file, soapNoteController.getSoapNote().getPatient());
		}
		imageSection.getFiles().add(file);
		soapNoteController.saveSoapSection();
		Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
		createNewFile();
	}

	public ImageSection getImageSection() {
		return imageSection;
	}

	public PatientSoapNoteController getSoapNoteController() {
		return soapNoteController;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void createNewFile(){
		file = new File();
		((Textbox)root.getFellow("captionTxtBox")).setValue(null);
		((Textbox)root.getFellow("fileNameTxtBox")).setValue(null);
	}

	public void removeFile(File fileToBeRemoved){
		if(UtilValidator.isNotEmpty(fileToBeRemoved.getCreatedBy()) && ! fileToBeRemoved.getCreatedBy().equals(Infrastructure.getUserName())){
			UtilMessagesAndPopups.showError("You are not authorized to remove this record");
			return;
		}
		if(!imageSection.getFiles().contains(fileToBeRemoved)){
			UtilMessagesAndPopups.showError("This record does not belong to current section.Cannot be deleted.");
			return;
		}
		imageSection.getFiles().remove(fileToBeRemoved);
		soapNoteController.saveSoapSection();
		Events.postEvent("onClick",(Component)desktop.getAttribute("wkModule"),null);
	}

	@Override
	public void render(Listitem item, Object data,int index) throws Exception {
		item.setValue(data);
		File file = (File)data;
		Listcell listcell = new Listcell();
		listcell.setParent(item);
		Hbox hbox = new Hbox();
		hbox.setParent(listcell);
		Label label = new Label();
		label.setParent(hbox);
		//label.setValue("REPORT NAME : "+file.getReportName());
		label.setValue("CAPTION : "+file.getDescription());
		label.setStyle("font-weight:bold");
		Image image = new Image();
		image.setWidth("260px");
		image.setHeight("260px");
		image.setParent(hbox);
		Space space = new Space();
		space.setParent(hbox);
		Space space1 = new Space();
		space1.setParent(hbox);
		Space space2 = new Space();
		space2.setParent(hbox);
		Label createdBy = new Label();
		createdBy.setParent(hbox);
		createdBy.setValue("CREATED BY : "+file.getCreatedBy());
	
	
	/*Space spa = new Space();
	spa.setParent(hbox);
	spa.setWidth("225px");
	Label reportName = new Label();
	reportName.setParent(hbox);
	reportName.setValue("OBSERVATIONS : "+file.getDescription());
	
	*/
		java.io.FileInputStream fileInputStream = new java.io.FileInputStream(file.getFilePath());
		org.zkoss.image.AImage aImage = new org.zkoss.image.AImage(file.getFileName(),fileInputStream);
		image.setContent(aImage);
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
		this.soapNoteService = soapNoteService;
	}

	public Set<ImageSectionFileValueObject> getValueObjects() {
		return valueObjects;
	}

	public Set<ImageSectionFileValueObject> getPastValueObject() {
		return pastValueObject;
	}

	public void setPastValueObject(Set<ImageSectionFileValueObject> pastValueObject) {
		this.pastValueObject = pastValueObject;
	}
}
