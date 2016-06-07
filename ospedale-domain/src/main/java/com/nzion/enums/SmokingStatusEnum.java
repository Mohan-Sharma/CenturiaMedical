package com.nzion.enums;

public enum SmokingStatusEnum {
	
	CURRENT_EVERY_DAY_SMOKER("1","Current every day smoker"),CURRENT_SOME_DAY_SMOKER("2","Current some day smoker"),FORMER_SMOKER("3","Former smoker"),
	NEVER_SMOKER("4","Never smoker"),CURRENT_STATUS_UNKNOWN("5","Smoker, current status unknown"),UNKNOWN("9","Unknown");
	
	private String code;
	private String description;

	public String getCode() {return code;}
	public String getDescription() {return description;}
	
	SmokingStatusEnum(String code,String description){
		this.code=code;
		this.description=description;
	}
	
	public String toString() {
		return description + "(" + code + ")";
		}
}
