package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.emr.SoapModule;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */

@Entity
@DiscriminatorValue("ALLERGY")
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "com.nzion.domain")
public class AllergySection	extends SoapSection {

	private Boolean noKnownAllergy;
	
	private Set<PatientAllergy> patientAllergies;

	@OneToMany(targetEntity = PatientAllergy.class,orphanRemoval = true, mappedBy = "soapSection")
	@Cascade(CascadeType.ALL)
	public Set<PatientAllergy> getPatientAllergies() {
		return patientAllergies;
	}
	
	public void setPatientAllergies(Set<PatientAllergy> patientAllergies) {
		this.patientAllergies = patientAllergies;
	}
	
	public void addAllergy(PatientAllergy patientAllergy) {
	if (patientAllergies == null) patientAllergies = new HashSet<PatientAllergy>();
	patientAllergy.setPatient(super.getSoapNote().getPatient());
	patientAllergy.setSoapSection(this);
	getPatientAllergies().add(patientAllergy);
	this.noKnownAllergy = false;
	}


	@Column(name = "NoKnownAllergy", nullable = true)
	public Boolean getNoKnownAllergy() {
	return noKnownAllergy;
	}

	public void setNoKnownAllergy(Boolean noKnownAllergy) {
	this.noKnownAllergy = noKnownAllergy;
	}
	
	private static final long serialVersionUID = 1L;

	public static final String MODULE_NAME = "Allergy";

	@Override
	public boolean edited() {
	return (noKnownAllergy != null && noKnownAllergy) || UtilValidator.isNotEmpty(patientAllergies);
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter,
			Map<String, SoapSection> previousSoapSections) {
	// TODO Auto-generated method stub
	
	}
}