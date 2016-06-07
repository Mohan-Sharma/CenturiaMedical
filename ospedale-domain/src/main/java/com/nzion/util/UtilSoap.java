package com.nzion.util;

import com.nzion.domain.emr.soap.PatientIcd;
import com.nzion.domain.emr.soap.PatientVitalSign;

public class UtilSoap {

	public static boolean isPatientIcdActive(PatientIcd patientIcd) {
	if (patientIcd == null) return false;

	if (patientIcd.getStatus() == null) return false;

	if (!"415684004".equalsIgnoreCase(patientIcd.getStatus().getEnumCode())
			&& !"410516002".equalsIgnoreCase(patientIcd.getStatus().getEnumCode())
			&& !"413322009".equalsIgnoreCase(patientIcd.getStatus().getEnumCode()))
		return true;
	else
		return false;
	}

	public static float getBMIValue(PatientVitalSign vitalSign) {
	if (vitalSign == null) return 0;

	if (UtilValidator.isEmpty(vitalSign.getValue())) return 0;

	try {
		Float f = Float.parseFloat(vitalSign.getValue());
		return f;
	} catch (NumberFormatException nfe) {
		return 0;
	}
	}
}
