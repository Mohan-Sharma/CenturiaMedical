package com.nzion.enums;

public enum SoapComponents {

	SUBJECTIVE("S"), OBJECTIVE("O"), ASSESEMENT("A"), PLAN("P");

	SoapComponents(String migrationCode) {
	this.migrationCode = migrationCode;
	}

	private final String migrationCode;

	public String getMigrationCode() {
	return migrationCode;
	}
	
	public static SoapComponents getSoapComponentsFromMigrationCode (String value) {
	for(SoapComponents soapComponents:SoapComponents.values()) {
		if(value.equals(soapComponents.getMigrationCode())) {
			return soapComponents;
		}
	}
	return null;
	}
}
