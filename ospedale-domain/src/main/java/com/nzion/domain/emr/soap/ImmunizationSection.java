package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

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
@DiscriminatorValue("IMMUNIZATION")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class ImmunizationSection extends SoapSection {

	private Set<PatientImmunization> immunizations;
	
	@OneToMany(targetEntity=PatientImmunization.class, fetch = FetchType.EAGER,mappedBy="soapSection",orphanRemoval = true)
	@Cascade(CascadeType.ALL)
	public Set<PatientImmunization> getImmunizations() {
	if (immunizations == null) immunizations = new HashSet<PatientImmunization>();
	return immunizations;
	}

	public void setImmunizations(Set<PatientImmunization> immunizations) {
	this.immunizations = immunizations;
	}

	@Transient
	public void addPatientImmunization(PatientImmunization patientImmunization) {
	patientImmunization.setSoapSection(this);
	getImmunizations().add(patientImmunization);
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {
//	ImmunizationSection immunizationSection = (ImmunizationSection) previousSoapSections.get(MODULE_NAME);
//	if(immunizationSection == null) return;
//	for (PatientImmunization patientImmunization : immunizationSection.getImmunizations()) {
//		if (!"ADMINISTERED".equalsIgnoreCase(patientImmunization.getStatus()))
//			this.addPatientImmunization(patientImmunization.createCopy());
//	}
//	pastRecordExists = UtilValidator.isNotEmpty(immunizationSection.getImmunizations());
	}

	private static final long serialVersionUID = 1L;
	
	public static final String MODULE_NAME = "Immunization";

	@Override
	public boolean edited() {
	return UtilValidator.isNotEmpty(immunizations);
//	return (pastRecordExists != null && pastRecordExists)  || !UtilValidator.isAllEmpty(this, "immunizations");
	}
}