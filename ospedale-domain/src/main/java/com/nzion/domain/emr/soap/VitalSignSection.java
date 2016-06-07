package com.nzion.domain.emr.soap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;

import com.nzion.domain.emr.SoapModule;
import com.nzion.domain.emr.soap.vitalsign.VitalSignReading;
import com.nzion.util.UtilValidator;

/**
 * @author Sandeep Prusty
 * Dec 1, 2010
 */
@Entity
@DiscriminatorValue("VITALSIGN")
@Cache(usage=CacheConcurrencyStrategy.TRANSACTIONAL,region="com.nzion.domain")
public class VitalSignSection extends SoapSection {
	
	private Set<PatientVitalSignSet> vitalSignRecordings;
	
	private List<VitalSignReading> readings;
	
	@OneToMany(targetEntity = PatientVitalSignSet.class,mappedBy="vitalSignSection", orphanRemoval=true)
	@Cascade(value=CascadeType.ALL)
	@OrderBy("recordedOn DESC")
	public Set<PatientVitalSignSet> getVitalSignRecordings() {
	return vitalSignRecordings;
	}

	public void setVitalSignRecordings(Set<PatientVitalSignSet> vitalSignRecordings) {
	this.vitalSignRecordings = vitalSignRecordings;
	}
	
	public void addVitalSign(PatientVitalSignSet patientVitalSignSet){
	if(vitalSignRecordings == null)
		vitalSignRecordings = new HashSet<PatientVitalSignSet>();
	vitalSignRecordings.add(patientVitalSignSet);
	}
	
	public boolean checkForAllGivenRecordings(String... vitalSigns){
	if(UtilValidator.isEmpty(vitalSignRecordings))
		return false;
	for(PatientVitalSignSet set : vitalSignRecordings){
		if(set.checkForAllGivenRecordings(vitalSigns))
			return true;
	}
	return false;
	}
	
	@Transient
	public PatientVitalSignSet getLatestVitalSignRecordings(String vitalSignName){
	if(UtilValidator.isEmpty(vitalSignRecordings))
		return null;
	PatientVitalSignSet result = null;
	for(PatientVitalSignSet set : vitalSignRecordings){
		PatientVitalSign pvs = set.getPatientVitalSign(vitalSignName);
		if(pvs != null && UtilValidator.isNotEmpty(pvs.getValue()) && (result == null || result.getCreatedTxTimestamp().before(set.getCreatedTxTimestamp())))
			result = set;
	}
	return result;
	}
	
	@Transient
	public PatientVitalSign getLatestVitalSignRecording(String vitalSignName){
	if(UtilValidator.isEmpty(vitalSignRecordings))
		return null;
	PatientVitalSignSet result = null;
	for(PatientVitalSignSet set : vitalSignRecordings){
		PatientVitalSign pvs = set.getPatientVitalSign(vitalSignName);
		if(pvs != null && (result == null || result.getCreatedTxTimestamp().before(set.getCreatedTxTimestamp())))
			result = set;
	}
	return result.getPatientVitalSign(vitalSignName);
	}

	@Override
	public void init(PatientSoapNote soapNote, SoapModule module, PatientSoapNote lastEncounter, Map<String, SoapSection> previousSoapSections) {
	}

	private static final long serialVersionUID = 1L;
	
	public static final String MODULE_NAME = "Vital Sign" ;

	@Override
	public boolean edited() {
	boolean b = !UtilValidator.isAllEmpty(this, "readings");
	if(readings!=null) {
		for(VitalSignReading vsr : readings) {
			if(vsr!=null) {
				vsr.calculateBMI();
			}
		}
	}
	return b;
	}

	@OneToMany(fetch=FetchType.EAGER,orphanRemoval=true)
	@IndexColumn(name = "READING_ORDER",base=1)
	@Cascade(value=CascadeType.ALL)
	@JoinColumn(name="VITAL_SIGN_READING")
	public List<VitalSignReading> getReadings() {
	return readings;
	}

	public void setReadings(List<VitalSignReading> readings) {
	this.readings = readings;
	}
}