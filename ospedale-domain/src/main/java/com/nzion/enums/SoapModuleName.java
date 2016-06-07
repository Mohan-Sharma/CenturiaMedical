package com.nzion.enums;

public enum SoapModuleName {
	HPI("HPI"), FAMILY_HX("Family Hx"), SOCIAL_HX("Social Hx"), PAST_HX("Past Hx"), MEDICATION_HX("MedicationHx"), ALLERGY(
			"Allergy"), ROS("ROS"), BIRTH_HX("Birth Hx"), VITAL_SIGN("Vital Sign"), EXAMINATION("Examination"), IMMUNIZATION(
			"Immunization"), DIAGNOSIS("Diagnosis"), LAB_ORDERS("Lab Orders"), RX("Rx"), RECOMMENDATION(
			"Recommendation"), REFERRAL("Referral"), FOLLOW_UP("FollowUp"), PLAN_AND_TREATMENT("Plan and Treatment"), INVESTIGATION(
			"Investigation");

	private String name;

	private SoapModuleName(String name) {
	this.name = name;
	}

	public String getName() {
	return this.name;
	}

}
