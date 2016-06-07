package com.nzion.zkoss.composer.emr;

import java.util.Date;
import java.util.List;

import com.nzion.domain.Provider;
import com.nzion.domain.emr.soap.PatientSoapNote;
import com.nzion.service.SoapNoteService;
import com.nzion.zkoss.composer.OspedaleAutowirableComposer;

public class MlcSearchController extends OspedaleAutowirableComposer{
	private static final long serialVersionUID = 1L;
	
	private SoapNoteService soapNoteService;
	
	private List<PatientSoapNote> patientSoapNotes;

	private Provider provider;
	
	private Date fromDate;
	
	private Date thruDate;
	
	public void search(){
		patientSoapNotes = soapNoteService.getMlcSoapNoteByCriteria(provider,fromDate,thruDate);
	}
	
	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getThruDate() {
		return thruDate;
	}

	public void setThruDate(Date thruDate) {
		this.thruDate = thruDate;
	}

	public SoapNoteService getSoapNoteService() {
		return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
		this.soapNoteService = soapNoteService;
	}

	public List<PatientSoapNote> getPatientSoapNotes() {
		return patientSoapNotes;
	}

	public void setPatientSoapNotes(List<PatientSoapNote> patientSoapNotes) {
		this.patientSoapNotes = patientSoapNotes;
	}
	
	

}
