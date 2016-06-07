package com.nzion.service.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class BookAppointmentDifferentSlotsDto {
	
	private String providerId;
    private String afyaId;
    private String visitType;
    
    private String civilId;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String emailId;
    private Date dateOfBirth;
    private String location;
    private String gender;
    
    private Date firstAppointmentStartDate;
    private Date firstAppointmentEndDate;
    
    private Date secondAppointmentStartDate;
    private Date secondAppointmentEndDate;
    
    private Date thirdAppointmentStartDate;
    private Date thirdAppointmentEndDate;
    
    private String notes;
    private String preferredLanguage;
    
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public String getAfyaId() {
		return afyaId;
	}
	public void setAfyaId(String afyaId) {
		this.afyaId = afyaId;
	}
	public String getVisitType() {
		return visitType;
	}
	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}
	public Date getFirstAppointmentStartDate() {
		return firstAppointmentStartDate;
	}
	public void setFirstAppointmentStartDate(Date firstAppointmentStartDate) {
		this.firstAppointmentStartDate = firstAppointmentStartDate;
	}
	public Date getFirstAppointmentEndDate() {
		return firstAppointmentEndDate;
	}
	public void setFirstAppointmentEndDate(Date firstAppointmentEndDate) {
		this.firstAppointmentEndDate = firstAppointmentEndDate;
	}
	public Date getSecondAppointmentStartDate() {
		return secondAppointmentStartDate;
	}
	public void setSecondAppointmentStartDate(Date secondAppointmentStartDate) {
		this.secondAppointmentStartDate = secondAppointmentStartDate;
	}
	public Date getSecondAppointmentEndDate() {
		return secondAppointmentEndDate;
	}
	public void setSecondAppointmentEndDate(Date secondAppointmentEndDate) {
		this.secondAppointmentEndDate = secondAppointmentEndDate;
	}
	public Date getThirdAppointmentStartDate() {
		return thirdAppointmentStartDate;
	}
	public void setThirdAppointmentStartDate(Date thirdAppointmentStartDate) {
		this.thirdAppointmentStartDate = thirdAppointmentStartDate;
	}
	public Date getThirdAppointmentEndDate() {
		return thirdAppointmentEndDate;
	}
	public void setThirdAppointmentEndDate(Date thirdAppointmentEndDate) {
		this.thirdAppointmentEndDate = thirdAppointmentEndDate;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getCivilId() {
		return civilId;
	}
	public void setCivilId(String civilId) {
		this.civilId = civilId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public Date getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
}
