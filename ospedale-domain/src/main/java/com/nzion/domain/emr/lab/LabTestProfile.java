package com.nzion.domain.emr.lab;

import com.nzion.domain.annot.AccountNumberField;
import com.nzion.domain.base.IdGeneratingBaseEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;


@Entity
@AccountNumberField("profileCode")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "profileCode"}))
@Access(AccessType.FIELD)
public class LabTestProfile extends IdGeneratingBaseEntity implements Serializable{

	@Column( length = 20)
	private String profileCode;
	
	private String profileName;
	
	private String profileNeumonic;
	

	@ManyToMany(targetEntity = LabTest.class,fetch = FetchType.EAGER)
	@JoinTable(name = "lab_test_profile_lab_test", 
	joinColumns = {@JoinColumn(name = "PROFILE_CODE")},
	inverseJoinColumns = { @JoinColumn(name = "TEST_CODE") })
	private Set<LabTest> tests;

	public String getProfileCode() {
		return profileCode;
	}

	public void setProfileCode(String profileCode) {
		this.profileCode = profileCode;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getProfileNeumonic() {
		return profileNeumonic;
	}

	public void setProfileNeumonic(String profileNeumonic) {
		this.profileNeumonic = profileNeumonic;
	}


	public Set<LabTest> getTests() {
		return tests;
	}

	public void setTests(Set<LabTest> tests) {
		this.tests = tests;
	}
	
	
	
	
}
