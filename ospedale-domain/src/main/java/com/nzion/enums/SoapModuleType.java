package com.nzion.enums;

public enum SoapModuleType {
	QA("Q"), TEXT("T");

	private final String migrationCode;

	SoapModuleType(String migrationCode) {
	this.migrationCode = migrationCode;
	}

	public String getMigrationCode() {
	return this.migrationCode;
	}
	
	public static SoapModuleType getSoapModuleTypeFromMigrationCode(String value) {
	for(SoapModuleType soapModuleType:SoapModuleType.values()) {
		if(value.equals(soapModuleType.getMigrationCode())) {
			return soapModuleType;
		}
	}
	return null;
	}
}
