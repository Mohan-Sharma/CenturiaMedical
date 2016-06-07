package com.nzion.domain.emr.soap;

import java.util.Map;
import java.util.Set;

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
@DiscriminatorValue("FAMILYHISTORY")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class FamilyHistorySection extends SoapSection {

	private Set<PatientFamilyIllness> familyIllnesses;

	@OneToMany(targetEntity = PatientFamilyIllness.class,mappedBy = "soapSection",orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientFamilyIllness> getFamilyIllnesses() {
	return familyIllnesses;
	}

	public void setFamilyIllnesses(Set<PatientFamilyIllness> familyIllnesses) {
	this.familyIllnesses = familyIllnesses;
	}

	public void addFamilyIllness(PatientFamilyIllness familyIllness) {
	familyIllness.setPatient(this.getSoapNote().getPatient());
	familyIllness.setSoapSection(this);
	getFamilyIllnesses().add(familyIllness);
	}

	private static final long serialVersionUID = 1L;
	
	public static final String MODULE_NAME = "Family Hx";

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {
	}

	@Override
	public boolean edited() {
	return !UtilValidator.isAllEmpty(this, "familyIllnesses");
	}
}
