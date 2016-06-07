package com.nzion.zkoss.composer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.apache.commons.io.IOUtils;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Image;
import org.zkoss.zul.Window;

import com.nzion.domain.Patient;
import com.nzion.domain.PatientCorporate;
import com.nzion.service.PatientService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.IoAndFileUtil;
import com.nzion.util.RestServiceConsumer;
import com.nzion.util.UtilMessagesAndPopups;


@VariableResolver(DelegatingVariableResolver.class)
public class PatientCorporateViewModel {
	
	@WireVariable
    private CommonCrudService commonCrudService;
	
	@WireVariable
	private PatientService patientService;

    @Wire("#patientCorporateWin")
    private Window patientCorporateWin;
    
    private Patient patient;
    
    private PatientCorporate patientCorporate;
    
    private List<Map<String, Object>> tariffCategorys;
    
    private Map<String, Object> selectedTariffCategory;
    
    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, true);
        patient = (Patient) patientCorporateWin.getAttribute("patient");
        patientCorporate = patient.getPatientCorporate();
        if(patientCorporate == null)
        	patientCorporate = new PatientCorporate();
        tariffCategorys = patientService.getTariffCategoryByPatientCategory(patient.getPatientType());
        for(Map<String, Object> m : tariffCategorys){
        	if(m.get("tariffCode").toString().equals(patientCorporate.getTariffCategoryId()))
        		selectedTariffCategory = m;
        }
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
			    patientCorporate.setIdCardName(media.getName());
			    patientCorporate.setIdCard(blob);
		    }else{
			    UtilMessagesAndPopups.displayError("Please upload image file");
			    return;
		    }
		 }
    }
    
    @Command("downloadFile")
    public void downloadFile() throws SerialException, SQLException, IOException{
    	if(patient.getPatientCorporate() == null)
    		return;
    	if(patient.getPatientCorporate().getIdCard() == null)
    		return;
    	
    	Blob blob = patient.getPatientCorporate().getIdCard();
    	if(blob != null)
    		Filedownload.save(blob.getBytes(1, (int) blob.length()), "", patient.getPatientCorporate().getIdCardName());
    }
    
    
    @Command("save")
    public void save(){
    	if(selectedTariffCategory == null){
    		UtilMessagesAndPopups.showError("Tariff Category cannot be empty");
    		return;
    	}
    	patientCorporate.setTariffCategoryId(selectedTariffCategory.get("tariffCode") != null ? selectedTariffCategory.get("tariffCode").toString() : null);
    	Map<String, Object> tariffCategory = patientService.getTariffCategoryByTariffCode(patientCorporate.getTariffCategoryId());
    	patientCorporate.setPrimaryPayor(tariffCategory.get("primaryPayor") != null ? tariffCategory.get("primaryPayor").toString() : null);
    	patientCorporate.setCorporateId(tariffCategory.get("corporateId") != null ? tariffCategory.get("corporateId").toString() : null);
    	patientCorporate.setCorporateCopay(tariffCategory.get("corporateCopay") != null ? (BigDecimal)tariffCategory.get("corporateCopay") : null);
    	patientCorporate.setCorporateCopayType(tariffCategory.get("corporateCopayType") != null ? tariffCategory.get("corporateCopayType").toString() : null);
    	Map<String,Object> corporateMaster = RestServiceConsumer.getCorporateMasterByCorporateId(patientCorporate.getCorporateId());
    	patientCorporate.setModeOfClaim(corporateMaster.get("MODE_OF_CLAIM") != null ? corporateMaster.get("MODE_OF_CLAIM").toString() : null);
    	patient.setPatientCorporate(patientCorporate);
    	commonCrudService.save(patient);
    	UtilMessagesAndPopups.showSuccess();
    }

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public PatientCorporate getPatientCorporate() {
		return patientCorporate;
	}

	public void setPatientCorporate(PatientCorporate patientCorporate) {
		this.patientCorporate = patientCorporate;
	}


	public List<Map<String, Object>> getTariffCategorys() {
		return tariffCategorys;
	}


	public void setTariffCategorys(List<Map<String, Object>> tariffCategorys) {
		this.tariffCategorys = tariffCategorys;
	}


	public Map<String, Object> getSelectedTariffCategory() {
		return selectedTariffCategory;
	}


	public void setSelectedTariffCategory(Map<String, Object> selectedTariffCategory) {
		this.selectedTariffCategory = selectedTariffCategory;
	}
	

}
