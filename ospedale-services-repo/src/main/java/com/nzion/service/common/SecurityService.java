package com.nzion.service.common;

import com.nzion.domain.emr.soap.PatientSoapNote;

public interface SecurityService {

	boolean hasAuthorizationToViewSoapNote(PatientSoapNote patientSoapNote);
}
