package com.nzion.domain.pms;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Filters;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
@Table(name = "CORPORATE")
@Filters( { @Filter(name = "EnabledFilter",condition="(IS_ACTIVE=1 OR IS_ACTIVE IS NULL)") })
public class Corporate extends IdGeneratingBaseEntity  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	
	private String registrationNo;
		
	private String keyPersonFirstName;
	
	private String keyPersonMiddleName;
	
	private String keyPersonLastName;
	
	private String addressLine1;
	
	private String addressLine2;
	
	private String city;
	
	private String governorate;
	
	private Integer pinCode;
	
	private String countryID;
	
	private String faxNo;
	
	private String email;
	
	private String website;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegistrationNo() {
		return registrationNo;
	}

	public void setRegistrationNo(String registrationNo) {
		this.registrationNo = registrationNo;
	}

	public String getKeyPersonFirstName() {
		return keyPersonFirstName;
	}

	public void setKeyPersonFirstName(String keyPersonFirstName) {
		this.keyPersonFirstName = keyPersonFirstName;
	}

	public String getKeyPersonMiddleName() {
		return keyPersonMiddleName;
	}

	public void setKeyPersonMiddleName(String keyPersonMiddleName) {
		this.keyPersonMiddleName = keyPersonMiddleName;
	}

	public String getKeyPersonLastName() {
		return keyPersonLastName;
	}

	public void setKeyPersonLastName(String keyPersonLastName) {
		this.keyPersonLastName = keyPersonLastName;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getGovernorate() {
		return governorate;
	}

	public void setGovernorate(String governorate) {
		this.governorate = governorate;
	}

	public Integer getPinCode() {
		return pinCode;
	}

	public void setPinCode(Integer pinCode) {
		this.pinCode = pinCode;
	}

	public String getCountryID() {
		return countryID;
	}

	public void setCountryID(String countryID) {
		this.countryID = countryID;
	}

	public String getFaxNo() {
		return faxNo;
	}

	public void setFaxNo(String faxNo) {
		this.faxNo = faxNo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

}
