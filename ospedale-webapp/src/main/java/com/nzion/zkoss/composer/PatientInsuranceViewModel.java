package com.nzion.zkoss.composer;

import com.nzion.domain.Patient;
import com.nzion.domain.PatientInsurance;
import com.nzion.dto.*;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.RestServiceConsumer;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
import org.zkoss.bind.annotation.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.*;

import java.sql.Blob;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: USER
 * Date: 4/15/15
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class PatientInsuranceViewModel{

    @WireVariable
    private CommonCrudService commonCrudService;

    @Wire("#patientInsuranceWin")
    private Window patientInsuranceWin;


    @Wire("#tpaIdTxtbox")
    private Textbox tpaIdTxtbox;

    @Wire("#tpaBlock")
    private Div tpaBlock;

    @Wire("#groupBlock")
    private Div groupBlock;

    @Wire("#insuranceIdTxtbox")
    private Textbox insuranceIdTxtbox;

    @Wire("#insurenceGroupCombobox")
    private Combobox insurenceGroupCombobox;

    @Wire("#benefitCombobox")
    private Combobox benefitCombobox;

    @Wire("#membershipIdTxtbox")
    private Textbox membershipIdTxtbox;

    @Wire("#insurencePolicyCombobox")
    private Combobox insurencePolicyCombobox;
    
    @Wire("#healthPlanId")
    private Textbox healthPlanId;

    @Wire("#relationId")
    private Combobox relationId;

    @Wire("#primaryMemberId")
    private Textbox primaryMemberId;

    private GroupDto selectedGroup;

    private InsuranceGroupDto selectedPolicy;

    private InsuranceForTpaDto insuranceDto;

    private InsuranceForTpaDto tpaDto;

    RestServiceConsumer restConsumer;

    private InsuranceGroupPlanDetailDto igpdDto;

    private List<TPAPayersDto> tpaPayersDtos;



    private List<InsuranceGroupDto> policyDtos;

    private List<InsuranceDetailsDto> insuranceDetailsDtos;

    private List<GroupDto> groupDtos;

    private String uhid;


    private String insuranceName;

    private Patient patient;

    private Listbox patientInsListBox;

    private BenefitsDto selectedBenefitsDto;

    private TPAPayersDto selectedTpaPayer;

    private InsuranceDetailsDto selectInsuranceDetailsDto;

    private boolean planDetailsLink;

    private String patientType = "SELF";

    private String insuranceType = "GROUP";

    private String relation;

    private Patient memberPatient;

    private String membershipId;

    private Blob resource;

    HealthPolicyDto healthPolicy;

    private boolean dependent;

    @AfterCompose
    public void init(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, true);
        restConsumer = new RestServiceConsumer();
        patient = (Patient) patientInsuranceWin.getAttribute("patient");
        patientInsListBox = (Listbox) patientInsuranceWin.getAttribute("patientInsListBox");
        tpaPayersDtos = restConsumer.allTPAPayers();
        insuranceDetailsDtos = restConsumer.getInsuranceDetailsOfTpa(null);
        groupDtos = restConsumer.getAllGroups();

    }


    @Command("updateInsuranceType")
    @NotifyChange({"groupDtos","policyDtos"})
    public void updateInsuranceType() {
        if ("GROUP".equals(insuranceType)) {
            System.out.println("Group Selected");
            insurenceGroupCombobox.setReadonly(false);
            insurenceGroupCombobox.setVisible(true);
            groupBlock.setVisible(true);
            groupDtos = restConsumer.getAllGroups();
        } else {
            System.out.println("Individual Selected");
            groupBlock.setVisible(false);
            policyDtos = restConsumer.getPolicyForIndividuals();
        }
    }

    @Command("updateMember")
    @NotifyChange("dependent")
    public void updateMember() {
        if ("DEPENDENT".equals(patientType)) {
            dependent = true;
        } else {
            dependent = false;
        }
    }

    @Command("updateInsuranceDetailsDtos")
    @NotifyChange({"insuranceDetailsDtos", "groupDtos"})
    public void updateInsuranceDetailsDtos() {
        insuranceDetailsDtos = restConsumer.getInsuranceDetailsOfTpa(selectedTpaPayer.getPayerId());
        groupDtos = restConsumer.fetchListOfGroupNamesByPayer(selectedTpaPayer.getPayerId());
    }

    @Command("getGroupDetails")
    @NotifyChange("policyDtos")
    public void getGroupDetails() {
        if ("GROUP".equals(insuranceType)) {
        	if(selectedGroup == null || UtilValidator.isEmpty(selectedGroup.getGroupId()) ){
        		UtilMessagesAndPopups.showError("Please select a valid Group");
        		return;
        	}
            String groupId = selectedGroup.getGroupId();
            policyDtos = restConsumer.getPolicyByGroupId(groupId);
            insurencePolicyCombobox.setValue("");
            healthPlanId.setValue("");
            insuranceIdTxtbox.setValue("");
            relationId.setValue("");
            primaryMemberId.setValue("");
        } else {



            /*String relation = UtilValidator.isEmpty(this.relation) ? "SELF" : this.relation;
            igpdDto = restConsumer.getPlanDetailsForGroupId(selectedGroup.getGroupId(), relation, patient.getGender().getEnumCode());
            selectedBenefitsDto = igpdDto.getBenefits() != null ? igpdDto.getBenefits().get(0) : null;
            */
        }
    }

    @Command("getPolicyDetails")
    @NotifyChange({"insurnaceDto","tpaDto","igpdDto","selectedBenefitsDto"})
    public void getPolicyDetails() {
        String payerId = selectedPolicy.getPayerId();
        InsuranceForTpaDto payer = restConsumer.getPayerById(payerId);
        healthPolicy = restConsumer.getHealthPolicyById(selectedPolicy.getHealthPolicyId());
        if (payer != null) {
            if ("INSURANCE".equals(payer.getPayerType())) {
                insuranceDto = payer;
                insuranceIdTxtbox.setText(insuranceDto.getInsuranceName());
               // tpaIdTxtbox.setVisible(false);
                tpaBlock.setVisible(false);
            } else {
                tpaDto = payer;
                tpaBlock.setVisible(true);
               // tpaIdTxtbox.setVisible(true);
                tpaIdTxtbox.setText(tpaDto.getInsuranceName());
                insuranceDto = restConsumer.getPayerById(healthPolicy.getInsuranceId());
                insuranceIdTxtbox.setText(insuranceDto.getInsuranceName());
            }
        }
        String relation = UtilValidator.isEmpty(this.relation) ? "SELF" : this.relation;
        igpdDto = restConsumer.getPlanDetailsForPolicyIdAndPolicyName(selectedPolicy.getHealthPolicyId(), selectedPolicy.getPolicyName(), relation, patient.getGender().getEnumCode());
        System.out.println("\n\n"+igpdDto.getHealthPolicy()+"\n\n\n");
        selectedBenefitsDto = UtilValidator.isNotEmpty(igpdDto.getBenefits())? igpdDto.getBenefits().get(0) : null;
    }


    @Command("updatePolicy")
    public void updatePolicy() {
    }

    @Command("updateGroup")
    public void updateGroup() {
         //String payerId = selectedTpaPayer != null ? selectedTpaPayer.getPayerId() : selectInsuranceDetailsDto.getPayerId();
         //groupDtos = restConsumer.fetchListOfGroupNamesByPayer(payerId);
    }

    @Command("Save")
    public void save(@BindingParam("comp") Component component, @BindingParam("patientTabbox") Tabbox patientTabbox, @BindingParam("desktopScopeParam") Map desktopScopeParam) {


        if ("GROUP".equals(insuranceType)) {
            insurenceGroupCombobox.getSelectedItem().getValue();
        }
        //benefitCombobox.getSelectedItem().getValue();
        membershipIdTxtbox.getValue();
        insurencePolicyCombobox.setConstraint("no empty");
        insurencePolicyCombobox.getSelectedItem().getValue();
        
        insuranceIdTxtbox.setConstraint("no empty");
        insuranceIdTxtbox.getValue();

        if(isDependent()){
            relationId.setConstraint("no empty");
            relationId.getValue();

            primaryMemberId.setConstraint("no empty");
            primaryMemberId.getValue();
        }

        PatientInsurance patientInsurance = new PatientInsurance();
        if ("GROUP".equals(insuranceType)) {
            patientInsurance.setGroupId(selectedGroup.getGroupId());
            patientInsurance.setGroupName(selectedGroup.getGroupName());
        }
        patientInsurance.setPolicyNo(igpdDto.getPolicyNumber());
        patientInsurance.setStartDate(igpdDto.getPlanStartDate());
        patientInsurance.setEndDate(igpdDto.getPlanEndDate());
        patientInsurance.setHealthPolicyName(igpdDto.getHealthPolicy().getHealthPolicyName());
        patientInsurance.setHealthPolicyId(igpdDto.getHealthPolicy().getHealthPolicyId());
        patientInsurance.setBenefitId(selectedBenefitsDto.getBenefitPlanId());
        patientInsurance.setBenefitName(selectedBenefitsDto.getBenefitPlan());
        patientInsurance.setUhid(uhid);
        patientInsurance.setInsuranceCode(insuranceDto.getInsuranceCode());
        
        if( tpaDto != null && UtilValidator.isNotEmpty(tpaDto.getInsuranceName())){
        	patientInsurance.setInsuranceName(tpaDto.getInsuranceName());
        }else{
        	patientInsurance.setInsuranceName(insuranceDto.getInsuranceName());
        }
        
        patientInsurance.setPatientType(this.patientType);
        patientInsurance.setRelation(this.relation);
        patientInsurance.setMemberPatient(this.memberPatient);
        patientInsurance.setMembershipId(this.membershipId);
        patientInsurance.setInsuranceType(this.insuranceType);
        patientInsurance.setResource(this.resource);

        //added to check patient existance
        List<Patient> oldPatientList = commonCrudService.findByEquality(Patient.class, new String[]{"afyaId"}, new Object[]{patient.getAfyaId()});
        if(UtilValidator.isNotEmpty(oldPatientList)) {
            patient.setId(oldPatientList.get(0).getId());
        }
        commonCrudService.evict(oldPatientList);
        //---end---

        patient.addPatientInsurance(patientInsurance);
        commonCrudService.save(patient);
        commonCrudService.refreshEntity(patient);

        Map m = new HashMap();
        m.put("entity",patient);
        if ((desktopScopeParam.get("contentArea")) != null){
            ((Component) desktopScopeParam.get("contentArea")).getChildren().clear();
        }
        Executions.createComponents("/patient/viewpatient.zul",(Component) desktopScopeParam.get("contentArea") , m);


        Events.postEvent("onReloadRequest", patientInsuranceWin, null);
        if (patientInsListBox != null)
            Events.postEvent("onReloadRequest", patientInsListBox, null);
        if (component != null && patient != null)
            Events.postEvent("onTrigger", component, patient);
        if (component == null)
            UtilMessagesAndPopups.showSuccess();
        if (patientTabbox != null)
            Events.postEvent("onRequest", patientTabbox, null);
        Events.postEvent("onDetach",patientInsuranceWin,null);
    }

    public GroupDto getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(GroupDto selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public InsuranceGroupPlanDetailDto getIgpdDto() {
        return igpdDto;
    }

    public void setIgpdDto(InsuranceGroupPlanDetailDto igpdDto) {
        this.igpdDto = igpdDto;
    }

    public String getUhid() {
        return uhid;
    }

    public void setUhid(String uhid) {
        this.uhid = uhid;
    }

    @NotifyChange("patient")
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public BenefitsDto getSelectedBenefitsDto() {
        return selectedBenefitsDto;
    }

    public void setSelectedBenefitsDto(BenefitsDto selectedBenefitsDto) {
        this.selectedBenefitsDto = selectedBenefitsDto;
    }

    public List<TPAPayersDto> getTpaPayersDtos() {
        return tpaPayersDtos;
    }

    public void setTpaPayersDtos(List<TPAPayersDto> tpaPayersDtos) {
        this.tpaPayersDtos = tpaPayersDtos;
    }

    public List<InsuranceDetailsDto> getInsuranceDetailsDtos() {
        return insuranceDetailsDtos;
    }

    public void setInsuranceDetailsDtos(List<InsuranceDetailsDto> insuranceDetailsDtos) {
        this.insuranceDetailsDtos = insuranceDetailsDtos;
    }

    public TPAPayersDto getSelectedTpaPayer() {
        return selectedTpaPayer;
    }

    public void setSelectedTpaPayer(TPAPayersDto selectedTpaPayer) {
        this.selectedTpaPayer = selectedTpaPayer;
    }

    public InsuranceDetailsDto getSelectInsuranceDetailsDto() {
        return selectInsuranceDetailsDto;
    }

    public void setSelectInsuranceDetailsDto(InsuranceDetailsDto selectInsuranceDetailsDto) {
        this.selectInsuranceDetailsDto = selectInsuranceDetailsDto;
    }

    public List<GroupDto> getGroupDtos() {
        return groupDtos;
    }

    public void setGroupDtos(List<GroupDto> groupDtos) {
        this.groupDtos = groupDtos;
    }

    public boolean isPlanDetailsLink() {
        return planDetailsLink;
    }

    public void setPlanDetailsLink(boolean planDetailsLink) {
        this.planDetailsLink = planDetailsLink;
    }

    @NotifyChange({"planDetailsLink"})
    public void enablePlanViewLink(boolean result) {
        this.planDetailsLink = result;
    }


    public String getInsuranceType() {
        return insuranceType;
    }


    public void setInsuranceType(String insuranceType) {
        this.insuranceType = insuranceType;
    }

    public String getPatientType() {
        return patientType;
    }


    public void setPatientType(String patientType) {
        this.patientType = patientType;
    }


    public String getRelation() {
        return relation;
    }


    public void setRelation(String relation) {
        this.relation = relation;
    }


    public Patient getMemberPatient() {
        return memberPatient;
    }


    public void setMemberPatient(Patient memberPatient) {
        this.memberPatient = memberPatient;
    }


    public String getMembershipId() {
        return membershipId;
    }


    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }


    public boolean isDependent() {
        return dependent;
    }


    public void setDependent(boolean dependent) {
        this.dependent = dependent;
    }

    public List<InsuranceGroupDto> getPolicyDtos() {
        return policyDtos;
    }

    public void setPolicyDtos(List<InsuranceGroupDto> policyDtos) {
        this.policyDtos = policyDtos;
    }

    public InsuranceGroupDto getSelectedPolicy() {
        return selectedPolicy;
    }

    public void setSelectedPolicy(InsuranceGroupDto selectedPolicy) {
        this.selectedPolicy = selectedPolicy;
    }

    public InsuranceForTpaDto getTpaDto() {
        return tpaDto;
    }

    public void setTpaDto(InsuranceForTpaDto tpa) {
        this.tpaDto = tpa;
    }

    public InsuranceForTpaDto getInsuranceDto() {
        return insuranceDto;
    }

    public void setInsuranceDto(InsuranceForTpaDto insurance) {
        this.insuranceDto = insurance;
    }

    public HealthPolicyDto getHealthPolicy() {
        return healthPolicy;
    }

    public void setHealthPolicy(HealthPolicyDto healthPolicy) {
        this.healthPolicy = healthPolicy;
    }


	public Blob getResource() {
		return resource;
	}


	public void setResource(Blob resource) {
		this.resource = resource;
	}


}
