package com.nzion.domain.util;

import com.nzion.domain.emr.soap.PatientSoapNote;

public class LabResultViewVo {


	private String labTestName;

	private String labResultStatus;

	private PatientSoapNote soapNote;
	
	public String getLabTestName() {
	return labTestName;
	}

	public void setLabTestName(String labTestName) {
	this.labTestName = labTestName;
	}

	public String getLabResultStatus() {
	return labResultStatus;
	}

	public void setLabResultStatus(String labResultStatus) {
	this.labResultStatus = labResultStatus;
	}

	public PatientSoapNote getSoapNote() {
	return soapNote;
	}

	public void setSoapNote(PatientSoapNote soapNote) {
	this.soapNote = soapNote;
	}

}
