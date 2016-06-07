package com.nzion.view.practice;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

import com.nzion.domain.ClinicCancellationPreference;
import com.nzion.domain.ClinicReschedulingPreference;
import com.nzion.domain.DoctorNoShowPreference;
import com.nzion.domain.PatientCancellationPreference;
import com.nzion.domain.PatientNoShowPreference;
import com.nzion.domain.PatientReschedulingPreference;
import com.nzion.domain.RCMPreference;
import com.nzion.domain.SchedulingPreference;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;


@VariableResolver(DelegatingVariableResolver.class)
public class RCMPreferenceVM {
	
	@WireVariable
    private CommonCrudService commonCrudService;
	
	private RCMPreference rcmPreference;
	
	private Set<SchedulingPreference> schedulingPreferences ;
	
	private Set<PatientCancellationPreference> patientCancellationPreferences;
	
	private Set<ClinicCancellationPreference> clinicCancellationPreferences;
    
    private Set<PatientReschedulingPreference> patientReschedulingPreferences;
    
    private Set<ClinicReschedulingPreference> clinicReschedulingPreferences;
    
    private Set<PatientNoShowPreference> patientNoShowPreferences;
    
    private Set<DoctorNoShowPreference> doctorNoShowPreferences;
	
	@AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, true);
        rcmPreference = commonCrudService.getByPractice(RCMPreference.class);
        if(rcmPreference.getId() == null){
        	rcmPreference = new RCMPreference();
        }else{
        	schedulingPreferences = new HashSet<>(commonCrudService.findByEquality(SchedulingPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        	patientCancellationPreferences = new HashSet<>(commonCrudService.findByEquality(PatientCancellationPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        	clinicCancellationPreferences = new HashSet<>(commonCrudService.findByEquality(ClinicCancellationPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        	patientReschedulingPreferences = new HashSet<>(commonCrudService.findByEquality(PatientReschedulingPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        	clinicReschedulingPreferences = new HashSet<>(commonCrudService.findByEquality(ClinicReschedulingPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        	
        	patientNoShowPreferences = new HashSet<>(commonCrudService.findByEquality(PatientNoShowPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        	doctorNoShowPreferences = new HashSet<>(commonCrudService.findByEquality(DoctorNoShowPreference.class, new String[]{"rcmPreference"}, new Object[]{rcmPreference}));
        	
        }
        if(UtilValidator.isEmpty(schedulingPreferences) || (schedulingPreferences != null && schedulingPreferences.size() == 3)){
        	schedulingPreferences = SchedulingPreference.getEmptyLineItem();
        }
        if(UtilValidator.isEmpty(patientCancellationPreferences) || (patientCancellationPreferences != null && patientCancellationPreferences.size() == 3)){
        	patientCancellationPreferences = PatientCancellationPreference.getEmptyLineItem();
        }
        if(UtilValidator.isEmpty(clinicCancellationPreferences) || (clinicCancellationPreferences != null && clinicCancellationPreferences.size() == 3)){
        	clinicCancellationPreferences = ClinicCancellationPreference.getEmptyLineItem();
        }
        if(UtilValidator.isEmpty(patientReschedulingPreferences) || (patientReschedulingPreferences != null && patientReschedulingPreferences.size() == 3) ){
        	patientReschedulingPreferences = PatientReschedulingPreference.getEmptyLineItem();
        }
        if(UtilValidator.isEmpty(clinicReschedulingPreferences) || (clinicReschedulingPreferences != null && clinicReschedulingPreferences.size() == 3) ){
        	clinicReschedulingPreferences = ClinicReschedulingPreference.getEmptyLineItem();
        }
        
        if(UtilValidator.isEmpty(patientNoShowPreferences) || (patientNoShowPreferences != null && patientNoShowPreferences.size() == 3) ){
        	patientNoShowPreferences = PatientNoShowPreference.getEmptyLineItem();
        }
        if(UtilValidator.isEmpty(doctorNoShowPreferences) || (doctorNoShowPreferences != null && doctorNoShowPreferences.size() == 3) ){
        	doctorNoShowPreferences = DoctorNoShowPreference.getEmptyLineItem();
        }
    }

	@Command
	public void save(){
		rcmPreference = commonCrudService.save(rcmPreference);
		commonCrudService.delete(commonCrudService.getAll(SchedulingPreference.class));
		for(SchedulingPreference sp : schedulingPreferences){
			sp.setId(null);
			sp.setRcmPreference(rcmPreference);
			commonCrudService.save(sp);
		}
		commonCrudService.delete(commonCrudService.getAll(PatientCancellationPreference.class));
		for(PatientCancellationPreference pcp : patientCancellationPreferences){
			pcp.setId(null);
			pcp.setRcmPreference(rcmPreference);
			commonCrudService.save(pcp);
		}
		commonCrudService.delete(commonCrudService.getAll(ClinicCancellationPreference.class));
		for(ClinicCancellationPreference clinicCancellationPreference : clinicCancellationPreferences){
			clinicCancellationPreference.setId(null);
			clinicCancellationPreference.setRcmPreference(rcmPreference);
			commonCrudService.save(clinicCancellationPreference);
		}
		commonCrudService.delete(commonCrudService.getAll(PatientReschedulingPreference.class));
		for(PatientReschedulingPreference patientReschedulingPreference : patientReschedulingPreferences){
			patientReschedulingPreference.setId(null);
			patientReschedulingPreference.setRcmPreference(rcmPreference);
			commonCrudService.save(patientReschedulingPreference);
		}
		commonCrudService.delete(commonCrudService.getAll(ClinicReschedulingPreference.class));
		for(ClinicReschedulingPreference clinicReschedulingPreference : clinicReschedulingPreferences){
			clinicReschedulingPreference.setId(null);
			clinicReschedulingPreference.setRcmPreference(rcmPreference);
			commonCrudService.save(clinicReschedulingPreference);
		}
		commonCrudService.delete(commonCrudService.getAll(PatientNoShowPreference.class));
		for(PatientNoShowPreference patientNoShowPreference : patientNoShowPreferences){
			patientNoShowPreference.setId(null);
			patientNoShowPreference.setRcmPreference(rcmPreference);
			commonCrudService.save(patientNoShowPreference);
		}
		commonCrudService.delete(commonCrudService.getAll(DoctorNoShowPreference.class));
		for(DoctorNoShowPreference doctorNoShowPreference : doctorNoShowPreferences){
			doctorNoShowPreference.setId(null);
			doctorNoShowPreference.setRcmPreference(rcmPreference);
			commonCrudService.save(doctorNoShowPreference);
		}
		
		UtilMessagesAndPopups.showSuccess();
	}
	
	public CommonCrudService getCommonCrudService() {
		return commonCrudService;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public RCMPreference getRcmPreference() {
		return rcmPreference;
	}

	public void setRcmPreference(RCMPreference rcmPreference) {
		this.rcmPreference = rcmPreference;
	}

	public Set<SchedulingPreference> getSchedulingPreferences() {
        //new code start for "consult visit" label change
        if(UtilValidator.isNotEmpty(schedulingPreferences)){
            for(SchedulingPreference schedulingPreference : schedulingPreferences){
                if(schedulingPreference.getVisitType().getDescription().equals("Consult Visit")){
                    RCMPreference.RCMVisitType.CONSULT_VISIT.setDescription("Request For Appointment");
                    schedulingPreference.setVisitType(RCMPreference.RCMVisitType.CONSULT_VISIT);
                }
            }
        }
    //new code end for "consult visit" label change
		return schedulingPreferences;
	}

	public void setSchedulingPreferences(
			Set<SchedulingPreference> schedulingPreferences) {
		this.schedulingPreferences = schedulingPreferences;
	}

	public Set<PatientCancellationPreference> getPatientCancellationPreferences() {
		return patientCancellationPreferences;
	}

	public void setPatientCancellationPreferences(
			Set<PatientCancellationPreference> patientCancellationPreferences) {
		this.patientCancellationPreferences = patientCancellationPreferences;
	}

	public Set<ClinicCancellationPreference> getClinicCancellationPreferences() {
		return clinicCancellationPreferences;
	}

	public void setClinicCancellationPreferences(
			Set<ClinicCancellationPreference> clinicCancellationPreferences) {
		this.clinicCancellationPreferences = clinicCancellationPreferences;
	}

	public Set<PatientReschedulingPreference> getPatientReschedulingPreferences() {
		return patientReschedulingPreferences;
	}

	public void setPatientReschedulingPreferences(
			Set<PatientReschedulingPreference> patientReschedulingPreferences) {
		this.patientReschedulingPreferences = patientReschedulingPreferences;
	}

	public Set<ClinicReschedulingPreference> getClinicReschedulingPreferences() {
		return clinicReschedulingPreferences;
	}

	public void setClinicReschedulingPreferences(
			Set<ClinicReschedulingPreference> clinicReschedulingPreferences) {
		this.clinicReschedulingPreferences = clinicReschedulingPreferences;
	}

	public Set<PatientNoShowPreference> getPatientNoShowPreferences() {
		return patientNoShowPreferences;
	}

	public void setPatientNoShowPreferences(
			Set<PatientNoShowPreference> patientNoShowPreferences) {
		this.patientNoShowPreferences = patientNoShowPreferences;
	}

	public Set<DoctorNoShowPreference> getDoctorNoShowPreferences() {
		return doctorNoShowPreferences;
	}

	public void setDoctorNoShowPreferences(
			Set<DoctorNoShowPreference> doctorNoShowPreferences) {
		this.doctorNoShowPreferences = doctorNoShowPreferences;
	}
	
	
}
