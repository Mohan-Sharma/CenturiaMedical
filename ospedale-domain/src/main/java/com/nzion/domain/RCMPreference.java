package com.nzion.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.nzion.domain.base.IdGeneratingBaseEntity;

@Entity
public class RCMPreference extends IdGeneratingBaseEntity{
	private static final long serialVersionUID = 1L;
	
    private Set<SchedulingPreference> schedulingPreferences;
    
    private Set<PatientCancellationPreference> patientCancellationPreferences;
    
    private Set<ClinicCancellationPreference> clinicCancellationPreferences;
    
    private Set<PatientReschedulingPreference> patientReschedulingPreferences;
    
    private Set<ClinicReschedulingPreference> clinicReschedulingPreferences;
    
    private Set<PatientNoShowPreference> patientNoShowPreferences;
    
    private Set<DoctorNoShowPreference> doctorNoShowPreferences;

	
    @OneToMany(targetEntity = SchedulingPreference.class, fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
    public Set<SchedulingPreference> getSchedulingPreferences() {
		return schedulingPreferences;
	}

	public void setSchedulingPreferences(Set<SchedulingPreference> schedulingPreferences) {
		this.schedulingPreferences = schedulingPreferences;
	}

	@OneToMany(targetEntity = PatientCancellationPreference.class,fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
	public Set<PatientCancellationPreference> getPatientCancellationPreferences() {
		return patientCancellationPreferences;
	}

	public void setPatientCancellationPreferences(Set<PatientCancellationPreference> patientCancellationPreferences) {
		this.patientCancellationPreferences = patientCancellationPreferences;
	}
	
	@OneToMany(targetEntity = ClinicCancellationPreference.class,fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
	public Set<ClinicCancellationPreference> getClinicCancellationPreferences() {
		return clinicCancellationPreferences;
	}

	public void setClinicCancellationPreferences(
			Set<ClinicCancellationPreference> clinicCancellationPreferences) {
		this.clinicCancellationPreferences = clinicCancellationPreferences;
	}

	@OneToMany(targetEntity = PatientReschedulingPreference.class,fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
	public Set<PatientReschedulingPreference> getPatientReschedulingPreferences() {
		return patientReschedulingPreferences;
	}

	public void setPatientReschedulingPreferences(
			Set<PatientReschedulingPreference> patientReschedulingPreferences) {
		this.patientReschedulingPreferences = patientReschedulingPreferences;
	}

	@OneToMany(targetEntity = PatientReschedulingPreference.class,fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
	public Set<ClinicReschedulingPreference> getClinicReschedulingPreferences() {
		return clinicReschedulingPreferences;
	}

	public void setClinicReschedulingPreferences(
			Set<ClinicReschedulingPreference> clinicReschedulingPreferences) {
		this.clinicReschedulingPreferences = clinicReschedulingPreferences;
	}

	
	@OneToMany(targetEntity = PatientNoShowPreference.class,fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
	public Set<PatientNoShowPreference> getPatientNoShowPreferences() {
		return patientNoShowPreferences;
	}

	public void setPatientNoShowPreferences(
			Set<PatientNoShowPreference> patientNoShowPreferences) {
		this.patientNoShowPreferences = patientNoShowPreferences;
	}

	@OneToMany(targetEntity = DoctorNoShowPreference.class,fetch = FetchType.EAGER)
    @Cascade(CascadeType.ALL)
	public Set<DoctorNoShowPreference> getDoctorNoShowPreferences() {
		return doctorNoShowPreferences;
	}

	public void setDoctorNoShowPreferences(
			Set<DoctorNoShowPreference> doctorNoShowPreferences) {
		this.doctorNoShowPreferences = doctorNoShowPreferences;
	}









	public static enum RCMVisitType {
    	PREMIUM_APPOINTMENT("Premium Appointment"),TELE_CONSULT_APPOINTMENT("Tele Consult Appointment"),
    	HOME_VISIT_APPOINTMENT("Home Visit Appointment"),CONSULT_VISIT("Consult Visit");
    	
    	private String description;

    	private RCMVisitType(String description) {
    		this.description = description;
    	}

    	public String getDescription() {
    		return description;
    	}

    	public void setDescription(String description) {
    		this.description = description;
    	}
    }
    
    
    
    
}
