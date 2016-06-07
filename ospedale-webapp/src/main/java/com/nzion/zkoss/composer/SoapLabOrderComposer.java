package com.nzion.zkoss.composer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nzion.domain.Patient;
import com.nzion.domain.emr.soap.LabOrderSection;
import com.nzion.domain.emr.soap.PatientLabOrder;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;

public class SoapLabOrderComposer extends OspedaleAutowirableComposer {

	private CommonCrudService commonCrudService;

	private SoapNoteService soapNoteService;

	private LabOrderSection labOrderSection;

	public CommonCrudService getCommonCrudService() {
	return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
	this.commonCrudService = commonCrudService;
	}

	public SoapNoteService getSoapNoteService() {
	return soapNoteService;
	}

	public void setSoapNoteService(SoapNoteService soapNoteService) {
	this.soapNoteService = soapNoteService;
	}

	public LabOrderSection getLabOrderSection() {
	return labOrderSection;
	}

	public void setLabOrderSection(LabOrderSection labOrderSection) {
	this.labOrderSection = labOrderSection;
	}

	private static final long serialVersionUID = 1L;

}
