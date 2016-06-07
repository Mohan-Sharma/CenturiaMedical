package com.nzion.zkoss.composer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.apache.commons.io.IOUtils;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

import com.nzion.domain.Patient;
import com.nzion.domain.PatientCorporate;
import com.nzion.domain.PatientCorporateDocument;
import com.nzion.service.PatientService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.IoAndFileUtil;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilMisc;
import com.nzion.util.UtilValidator;


@VariableResolver(DelegatingVariableResolver.class)
public class PatientCorporateDocumentViewModel {
	
	@WireVariable
    private CommonCrudService commonCrudService;
	
	@WireVariable
	private PatientService patientService;

    @Wire("#patientCorporateDocWin")
    private Window patientCorporateDocWin;
    
    private Patient patient;
    
    private PatientCorporateDocument patientCorporateDocument;
    
    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, true);
        patient = (Patient) patientCorporateDocWin.getAttribute("patient");
        patientCorporateDocument = new PatientCorporateDocument();
    }
    
    
    @Command("uploadFile")
    public void uploadFile(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) throws SerialException, SQLException, IOException{
		UploadEvent upEvent = null;
		Object objUploadEvent = ctx.getTriggerEvent();
		if (objUploadEvent != null && (objUploadEvent instanceof UploadEvent)) {
	         upEvent = (UploadEvent) objUploadEvent;
	    }
		if (upEvent != null) {
		    Media media = upEvent.getMedia();
		    Blob blob = null;
		    if (media instanceof AImage) {
			    blob = new SerialBlob(media.getByteData());
			    patientCorporateDocument.setDocumentName(media.getName());
			    patientCorporateDocument.setDocument(blob);
		    }else if("pdf".equals(media.getFormat())){
		    	byte[] content = IOUtils.toByteArray(media.getStreamData());
			    blob = new SerialBlob(content);
			    patientCorporateDocument.setDocumentName(media.getName());
			    patientCorporateDocument.setDocument(blob);
		    }else{
			    /*InputStream inputStream = IOUtils.toInputStream(media.getStringData(),"UTF-8");
			    byte[] content = IOUtils.toByteArray(inputStream);
			    blob = new SerialBlob(content);*/
		    	UtilMessagesAndPopups.showError("Please upload valid document");
		    }
		 }
    }
    
    @Command("delete")
    public void delete(PatientCorporateDocument patCorDocument){
    	PatientCorporate patientCorporate = patient.getPatientCorporate();
    	patientCorporate.getPatientCorporateDocuments().remove(patCorDocument);
    	commonCrudService.delete(patCorDocument);
    	Events.postEvent("onReload",patientCorporateDocWin.getFellow("documentListBox"),null);
    	UtilMessagesAndPopups.showSuccess();
    }
    
    @Command("downloadFile")
    public void downloadFile(PatientCorporateDocument patientCorporateDocument) throws SerialException, SQLException, IOException{
    	if(patientCorporateDocument == null)
    		return;
    	if(patientCorporateDocument.getDocument() == null)
    		return;
    	Blob blob = patientCorporateDocument.getDocument();
    	if(blob != null)
    		Filedownload.save(blob.getBytes(1, (int) blob.length()), "", patientCorporateDocument.getDocumentName());
    	
    }
    
    @Command("save")
    @NotifyChange("patientCorporateDocument")
    public void save(){
    	if(patient == null)
    		patient = (Patient) patientCorporateDocWin.getAttribute("patient");
    	PatientCorporate patientCorporate = patient.getPatientCorporate();
    	if(patientCorporate == null){
    		UtilMessagesAndPopups.showError("Please provide valid input");
    		return;
    	}
    	if(UtilValidator.isEmpty(patientCorporateDocument.getDocumentReferenceNumber())){
    		UtilMessagesAndPopups.showError("Reference Number cannot be empty");
    		return;
    	}
    	if(patientCorporateDocument.getValidUpTo() == null ){
    		UtilMessagesAndPopups.showError("Valid up to cannot be empty");
    		return;
    	}
    	if(patientCorporateDocument.getDocument() == null){
    		UtilMessagesAndPopups.showError("Document cannot be empty");
    		return;
    	}
    	patientCorporateDocument.setDate(new Date());
    	patientCorporateDocument.setPatientCorporate(patientCorporate);
    	commonCrudService.save(patientCorporateDocument);
    	Events.postEvent("onReload",patientCorporateDocWin.getFellow("documentListBox"),null);
    	patientCorporate.getPatientCorporateDocuments().add(patientCorporateDocument);
    	patientCorporateDocument = new PatientCorporateDocument();
    	UtilMessagesAndPopups.showSuccess();
    }


	public Patient getPatient() {
		return patient;
	}


	public void setPatient(Patient patient) {
		this.patient = patient;
	}


	public PatientCorporateDocument getPatientCorporateDocument() {
		return patientCorporateDocument;
	}


	public void setPatientCorporateDocument(
			PatientCorporateDocument patientCorporateDocument) {
		this.patientCorporateDocument = patientCorporateDocument;
	}
    
    

}
