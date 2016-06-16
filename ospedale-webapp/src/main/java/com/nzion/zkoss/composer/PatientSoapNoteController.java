package com.nzion.zkoss.composer;

import com.nzion.domain.Party.PartyType;
import com.nzion.domain.*;
import com.nzion.domain.Schedule.STATUS;
import com.nzion.domain.base.BaseEntity;
import com.nzion.domain.billing.Invoice;
import com.nzion.domain.emr.*;
import com.nzion.domain.emr.lab.LabOrderRequest;
import com.nzion.domain.emr.soap.*;
import com.nzion.domain.emr.soap.PatientCpt.CPTSTATUS;
import com.nzion.domain.person.DrugGroup;
import com.nzion.domain.person.LabGroup;
import com.nzion.domain.person.PersonLab;
import com.nzion.domain.person.ProviderDrug;
import com.nzion.domain.screen.BillingDisplayConfig;
import com.nzion.enums.SoapComponents;
import com.nzion.enums.SoapModuleType;
import com.nzion.exception.TransactionException;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.repository.notifier.utility.SmsUtil;
import com.nzion.repository.notifier.utility.TemplateNames;
import com.nzion.service.PatientService;
import com.nzion.service.PersonService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.billing.BillingService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.*;
import com.nzion.view.HomePageHelper;
import com.nzion.zkoss.dto.ProviderFavoriteDrugDto;
import com.nzion.zkoss.dto.ProviderFavoriteLabDto;
import com.nzion.zkoss.dto.ProviderFavoriteProcedureDto;
import com.nzion.zkoss.ext.Navigation;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Span;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sandeep Prusty
 *         Dec 6, 2010
 */
public class PatientSoapNoteController extends OspedaleAutowirableComposer {

    private final Schedule schedule;

    private PatientSoapNote soapNote;

    private final PatientSoapNote lastEncounter;

    private SoapNoteService soapNoteService;

    private CommonCrudService commonCrudService;

    private CommonCrudRepository commonCrudRepository;

    private PersonService personService;

    private PatientService patientService;

    private List<SoapModule> modules;

    private SoapModule selectedModule;

    private SoapSection selectedSection;

    private boolean notSaved;

    private boolean subjectiveCompAuth;

    private boolean objectiveCompAuth;
    private boolean assesmentCompAuth;
    private boolean planCompAuth;

    private boolean readonly = true;

    private String eventMessage;

    private final ArrayList<SoapModule> subjectiveModules;

    private final ArrayList<SoapModule> objectiveModules;

    private final ArrayList<SoapModule> assesementModules;

    private final ArrayList<SoapModule> plans;

    private BillingService billingService;

    private LabOrderRequest labOrderRequest;

    private boolean soapNoteBilled;

    private Person metWith;

    private String selectedHisModuleId;

    private List<ProviderFavoriteDrugDto> providerFavoriteDrugDtos;

    private List<ProviderFavoriteLabDto> providerFavoriteLabDtos;

    private List<ProviderFavoriteProcedureDto> providerFavoriteProcedureDtos;

    private PatientInsurance patientInsurance;
    
    private boolean isNotShaved = true;
    private boolean billingOnly;

    public PatientSoapNoteController(String scheduleIdString) {
        Long scheduleId = Long.valueOf(scheduleIdString);
        schedule = commonCrudService.getById(Schedule.class, scheduleId);
        soapNote = soapNoteService.loadOrCreateSoapNote(schedule);
        modules = soapNoteService.getSoapModules(schedule.getVisitType(), soapNote.getProvider(), soapNote);
        //soapNoteService.getAccesibleSoapNoteModules(soapNote);
        subjectiveModules = new ArrayList<SoapModule>();
        objectiveModules = new ArrayList<SoapModule>();
        assesementModules = new ArrayList<SoapModule>();
        Map<SoapComponents, SoapComponentAuthorization> SoapCompAuths = new HashMap<SoapComponents, SoapComponentAuthorization>();
        plans = new ArrayList<SoapModule>();
        modules = removeNullFromModules(modules);
        for (SoapModule sm : modules) {
            if (SoapComponents.SUBJECTIVE.equals(sm.getSoapComponentAuthorization().getComponents())) {
                //if(! ("HPI".equals(sm.getModuleName())) ){
                    subjectiveModules.add(sm);
                    SoapCompAuths.put(SoapComponents.SUBJECTIVE, sm.getSoapComponentAuthorization());
               // }
            } else if (SoapComponents.OBJECTIVE.equals(sm.getSoapComponentAuthorization().getComponents())) {
                objectiveModules.add(sm);
                SoapCompAuths.put(SoapComponents.OBJECTIVE, sm.getSoapComponentAuthorization());
            } else if (SoapComponents.ASSESEMENT.equals(sm.getSoapComponentAuthorization().getComponents())) {
                assesementModules.add(sm);
                SoapCompAuths.put(SoapComponents.ASSESEMENT, sm.getSoapComponentAuthorization());
            } else if (SoapComponents.PLAN.equals(sm.getSoapComponentAuthorization().getComponents())) {
                plans.add(sm);
                SoapCompAuths.put(SoapComponents.PLAN, sm.getSoapComponentAuthorization());
            }
        }
        lastEncounter = soapNoteService.getLastEncounter(soapNote.getPatient(), soapNote.getDate());
        notSaved = soapNote.getId() == null;

        if ((soapNote.getId() != null) && ((STATUS.CHECKEDOUT.equals(soapNote.getStatus()))) && (soapNoteService.getSoapSection(soapNote, ChiefComplainSection.class) == null)){
            notSaved = true;
        }

        populateIsReadOnly();
        enableModulesOnAutherization(SoapCompAuths);
        selectedHisModuleId = soapNote.getSelectedHisModuleId();
        if(UtilValidator.isEmpty(selectedHisModuleId) && "INSURANCE".equals(getPatient().getPatientType()) ){
            BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
            selectedHisModuleId =  billingDisplayConfig.getDefaultHisModuleId();
        }
        if(patientInsurance == null){
            if(getPatient() != null && getPatient().getPatientInsurances() != null && getPatient().getPatientInsurances().size() == 1)
                patientInsurance = getPatient().getPatientInsurances().iterator().next();
        }
    }

    private List<SoapModule> removeNullFromModules(List<SoapModule> modules2) {
        List<SoapModule> m = new ArrayList<SoapModule>();
        for (SoapModule sm : modules2) {
            if (sm != null)
                m.add(sm);
        }
        return m;
    }

    private void populateIsReadOnly() {
        if ((STATUS.CHECKEDIN.equals(soapNote.getStatus()) || STATUS.PROCEDUREPENDING.equals(soapNote.getStatus()) || STATUS.EXAMINING
                .equals(soapNote.getStatus()) || STATUS.READY_FOR_BILLING.equals(soapNote.getStatus()) || STATUS.CHECKEDOUT.equals(soapNote.getStatus()))
                && schedule.getLastPatientVisit() != null ) {
            readonly = false;
        }
        
        //&& Infrastructure.getLoggedInPerson().equals(schedule.getLastPatientVisit().getMetWith())
    }


    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        if (!hasAuthorizationToViewSoapNote(getSoapNote())) {
            String soapNoteId = String.valueOf(getSoapNote().getId());
            try {
                Executions.forward("/soap/soapUnAuthorizedAccess.zul?soapNoteId=" + soapNoteId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.doBeforeCompose(page, parent, compInfo);
    }

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);
        markEdited();
        if (soapNote.getId() == null) {
            root.getFellow("moduleAreaVbox").getChildren().clear();
            Events.postEvent("onClick", root.getFellow("soapChiefComplaints"), null);
            return;
        }
        if ((Roles.hasRole(Roles.PROVIDER) || Roles.hasRole(Roles.NURSE))) {
            //Executions.createComponents("/soap/fullSoapNotePreview.zul", root.getFellow("moduleAreaVbox"), UtilMisc.toMap(
              //      "controller", this, "scheduleId", schedule.getId()));
        }
        Invoice existingInvoice = billingService.getBillingTransactionFor(soapNote);

        //modified code for checkedout status
        if ((soapNote.getId() != null) && (STATUS.CHECKEDOUT.equals(soapNote.getStatus()))){
            List<Invoice> invoiceList = commonCrudService.findByEquality(Invoice.class, new String[]{"schedule"}, new Object[]{soapNote.getSchedule()});
            if (UtilValidator.isNotEmpty(invoiceList)){
                existingInvoice = invoiceList.get(0);
            }
        }

        soapNoteBilled = existingInvoice.getId() != null;
    }

    public boolean isNotSaved() {
        return notSaved;
    }

    public PatientSoapNote getLastEncounter() {
        return lastEncounter;
    }

    public void openFacesheetWindow(String uri) {
        Component comp = root.getFellowIfAny("sentenceAreaDiv");
        if (comp != null) comp.setVisible(false);
        Component moduleAreaVbox = root.getFellowIfAny("moduleAreaVbox");
        if (moduleAreaVbox != null) moduleAreaVbox.getChildren().clear();
        Executions.createComponents(uri, moduleAreaVbox, com.nzion.util.UtilMisc.toMap("patientSoapNoteController", this));
    }

    public void soapModuleClicked(Event event) {
        Component comp = event.getTarget().getFellowIfAny("sentenceAreaDiv");
        if (comp != null) {
            comp.setVisible(true);
        }
        SoapModule module = (SoapModule) event.getTarget().getAttribute("value");
        selectedModule = module == null ? null : module;
        String viewName = event.getTarget().getAttribute("linkId").toString();
        if (Navigation.viewExists(viewName)) {
            Navigation.navigate(viewName, UtilMisc.toMap("controller", this, "soapModule", event.getTarget()), root.getFellow("moduleAreaVbox"));
            if (!isNotSaved()) {
                Navigation.navigate("/soap/soapNotePreview", UtilMisc.toMap("controller", this), root
                        .getFellow("sentenceAreaDiv"));
            }
            return;
        }

        Collection<Question> questionsForSelectedModule = null;
        if (SoapModuleType.QA.equals(selectedModule.getSoapModuleType())) {
            questionsForSelectedModule = soapNoteService.getQuestionsForSelectedModule(selectedModule);
            Navigation.navigate("otherSoapNoteSection", UtilMisc.toMap("controller", this, "qas", questionsForSelectedModule), root.getFellow("moduleAreaVbox"));
        }
    }

    public SoapSection getSoapSection(Class<? extends SoapSection> klass) {
        selectedSection = (notSaved && ChiefComplainSection.class.equals(klass)) ? soapNote
                .createChiefComplaintSection(selectedModule) : soapNoteService.getSoapSection(soapNote, selectedModule,
                klass);
        System.identityHashCode(selectedSection);
        return selectedSection;
    }

    public SoapSection getSoapSection(Class<? extends SoapSection> klass, boolean isCurrent) {
        if (isCurrent) return getSoapSection(klass);
        return soapNoteService.getSoapSection(soapNote, klass);
    }

    public synchronized void saveSoapSection() {
	        if (selectedSection == null) return;
	        selectedSection.setEdited(true);
	        markEdited(selectedSection);
	        selectedSection.onSaveUpdate();
	        if (notSaved) {
	            List<SoapModule> applicableModules = modules;
	            soapNoteService.saveSoapSectionAndCreateSoapNote(selectedSection, applicableModules);
	            notSaved = false;
	            Executions.sendRedirect(null);
	            UtilMessagesAndPopups.showSuccess();
	            return;
	        }
	
	        if (UtilValidator.isEmpty(selectedSection.getUpdatedBy()) && UtilValidator.isEmpty(eventMessage))
	            eventMessage = selectedSection.getSoapModule().getModuleName() + "  section created";
	
	        soapNoteService.saveSoapSection(selectedSection, UtilValidator.isEmpty(eventMessage) ? buildEventMessage()
	                : eventMessage);
	        eventMessage = null;
	        UtilMessagesAndPopups.showSuccess();
    }

    public void saveUpdateSoapSection() {
        if (selectedSection == null) return;
        if (UtilValidator.isEmpty(selectedSection.getUpdatedBy()) && UtilValidator.isEmpty(eventMessage))
            eventMessage = selectedSection.getSoapModule().getModuleName() + "  section created";
        soapNoteService.saveSoapNote(this.getSoapNote());
        soapNoteService.saveUpdateSoapSection(selectedSection, UtilValidator.isEmpty(eventMessage) ? buildEventMessage() : eventMessage);
        eventMessage = null;
        UtilMessagesAndPopups.showSuccess();
    }

    private String buildEventMessage() {
        return selectedModule.getModuleName() + " section updated";
    }

    public List<PatientChiefComplaint> addChiefComplaint(String chiefComplaint, QATemplate qaTemplate) {
        ChiefComplainSection complainSection = (ChiefComplainSection) selectedSection;
        if (UtilValidator.isEmpty(chiefComplaint)) return Collections.emptyList();
        complainSection.addChiefComplaint(chiefComplaint, qaTemplate);
        return soapNoteService.getSimilarChiefComplaints(schedule.getPatient(), chiefComplaint, soapNote.getDate());
    }

    public List<PatientChiefComplaint> addChiefComplaint(ChiefComplaint complaint) {
        return addChiefComplaint(complaint.getComplainName(), complaint.getQaTemplate());
    }

    public List<SoapModule> getModules() {
        return modules;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

    public SoapModule getSelectedModule() {
        return selectedModule;
    }

    public void setSelectedModule(SoapModule selectedModule) {
        this.selectedModule = selectedModule;
    }

    public Patient getPatient() {
        return soapNote.getPatient();
    }

    public Provider getProvider() {
        return soapNote.getProvider();
    }

    public Set<PatientLabOrder> getSoapNoteLabOrders(PatientSoapNote soapNote) {
        LabOrderSection labOrderSection = (LabOrderSection) soapNoteService.getSoapSection(soapNote, LabOrderSection.class);
        return (labOrderSection == null) ? new HashSet<PatientLabOrder>(0) : labOrderSection.getLabOrder();
    }

    public Set<PatientIcd> getSoapNoteIcds(PatientSoapNote soapNote) {
        DiagnosisSection diagnosisSection = (DiagnosisSection) soapNoteService.getSoapSection(soapNote,
                DiagnosisSection.class);
        return diagnosisSection == null ? new HashSet<PatientIcd>(0) : diagnosisSection.getIcds();
    }

    public Set<PatientCpt> getSoapNoteCpts(PatientSoapNote soapNote) {
        DiagnosisSection diagnosisSection = (DiagnosisSection) soapNoteService.getSoapSection(soapNote,
                DiagnosisSection.class);
        return diagnosisSection == null ? new HashSet<PatientCpt>(0) : diagnosisSection.getCpts();
    }

    public List<PatientFamilyIllness> getFamilyIllnesses() {
        return getFamilyIllnesses(getPatient());
    }

    public List<PatientFamilyIllness> getFamilyIllnesses(Patient patient) {
        return soapNoteService.getAllFamilyIllnesses(patient);
    }

    public List<PatientLabOrder> filterLabOrdersForDestination(Collection<PatientLabOrder> labOrders, String destination) {
        List<PatientLabOrder> filteredLabOrders = new ArrayList<PatientLabOrder>();
        for (PatientLabOrder labOrder : labOrders) {
            if (!PatientLabOrder.STATUS.COMPLETED.equals(labOrder.getStatus())) filteredLabOrders.add(labOrder);
        }
        return filteredLabOrders;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    public void markAsReviewed() {
        selectedSection.setReviewed(true);
        soapNoteService.saveSoapSection(selectedSection, null);
    }

    public void openSecurityPersonLookup(MouseEvent event) {
        Map<String, Object> m = new HashMap<String, Object>(3);
        m.put("controller", this);
        m.put("accessRecord", event.getTarget().getParent().getAttribute("accessRecord"));
        m.put("command", ((Menuitem) event.getTarget()).getValue());
        Executions.createComponents("/person/person_lookup.zul", null, m);
    }

    public void prepareVitalSignChartController(Set<String> vitalSignNames, ChartController chartController,
                                                Patient patient) {
        List<PatientVitalSignSet> allPatientVitalSignSets = soapNoteService.getAllPatientVitalSign(patient);
        chartController.clear();
        for (PatientVitalSignSet patientVitalSignSet : allPatientVitalSignSets) {
            for (PatientVitalSign patientVitalSign : patientVitalSignSet.getVitalSigns()) {
                if (UtilValidator.isEmpty(patientVitalSign.getValue())) continue;
                if (vitalSignNames.contains(patientVitalSign.getVitalSign().getName())) {
                    chartController.addValue(patientVitalSign.getVitalSign().getName(),
                            patientVitalSignSet.getRecordedOn(), Double.valueOf(patientVitalSign.getValue()));
                }
            }
        }
    }

    public void signOutSoapNote() {
        soapNote = commonCrudService.refreshEntity(soapNote);
        //if( STATUS.EXAMINING.equals(soapNote.getSchedule().getStatus()) ){
        	//return;
        //}
        if( STATUS.CHECKEDIN.equals(soapNote.getSchedule().getStatus()) && Infrastructure.getUserLogin().hasRole(Roles.NURSE) ){
	    	PatientSoapNote soapNote = commonCrudService.getById(PatientSoapNote.class, this.soapNote.getId());
	        soapNote.setSelectedHisModuleId(getSelectedHisModuleId());
	        soapNote.getSchedule().setStatus(STATUS.EXAMINING);
	        soapNote.setPatientInsurance(this.getPatientInsurance());
	        soapNoteService.saveSoapNote(soapNote);
	        
	        Schedule schedule = commonCrudService.getById(Schedule.class, soapNote.getSchedule().getId());
	        schedule.getLastPatientVisit().setMetWith(soapNote.getProvider());
	        //Executions.sendRedirect(null);
            UtilMessagesAndPopups.showSuccess();
        }else if(STATUS.EXAMINING.equals(soapNote.getSchedule().getStatus()) && !Infrastructure.getUserLogin().hasRole(Roles.PROVIDER)){
        	return;
        }else{
	        Set<Speciality> specialities = new HashSet<Speciality>();
	        specialities.addAll(soapNote.getProvider().getSpecialities());
	        if (soapNote.getSchedule().getInternalReferral() != null)
	            specialities.addAll(soapNote.getSchedule().getInternalReferral().getSpecialities());
	        Executions.createComponents("/soap/showInCompleteMessage.zul", null, UtilMisc.toMap("controller", this, "specialities", specialities));
        }
        /*try {
            signOutSoapNoteDirectly();
        } catch (TransactionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
    }

    public Boolean getProcedurePendingStatus(){
        Set<PatientCpt> soapNoteCpts = this.getSoapNoteCpts(soapNote);
        for(PatientCpt pc : soapNoteCpts){
            if(CPTSTATUS.NEW.equals(pc.getCptStatus())){
                return true;
            }
        }
        return false;
    }


    /*Commented the code below for billing .Needs to change the billing logic*/

    public void signOutSoapNoteDirectly() throws TransactionException {
    	BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
    	boolean isProcedurePending = this.getProcedurePendingStatus();
    	boolean isNurse = Infrastructure.getUserLogin().hasRole(Roles.NURSE);
    	
    	PatientSoapNote soapNote = commonCrudService.getById(PatientSoapNote.class, this.soapNote.getId());
        soapNote.setSelectedHisModuleId(getSelectedHisModuleId());
        soapNote.getSchedule().setStatus(STATUS.READY_FOR_BILLING);
        soapNote.setPatientInsurance(this.getPatientInsurance());
        soapNoteService.saveSoapNote(soapNote);

        if(isNurse){
            PatientVisit patientVisit = soapNote.getSchedule().getLastPatientVisit();
            patientVisit.setMetWith(soapNote.getProvider());
            commonCrudService.save(patientVisit);
        }
        Invoice existingInvoice = billingService.getBillingTransactionFor(soapNote);
        if (existingInvoice.getId() == null && "patientVisit".equals(billingDisplayConfig.getIsConsultationPriceTriggered()) && !isProcedurePending)
            billingService.generateInvoiceFor(soapNote);
        soapNoteBilled = existingInvoice.getId() != null;

        if (labOrderRequest == null)
            labOrderRequest = commonCrudService.getByUniqueValue(LabOrderRequest.class, "patientSoapNote", this.getSoapNote());
        if (labOrderRequest != null) {
            labOrderRequest.setOrderStatus(LabOrderRequest.ORDERSTATUS.BILLING_REQUIRED);
            commonCrudService.save(labOrderRequest);
        }
        
        if(existingInvoice.getId() == null && "general".equals(billingDisplayConfig.getIsConsultationPriceTriggered()) && !isProcedurePending){
        	billingService.generateInvoiceFor(soapNote);
        	//soapNoteService.signOutSoapNote(soapNote);
        }
        soapNoteBilled = existingInvoice.getId() != null;
        /*if("patientVisit".equals(billingDisplayConfig.getIsConsultationPriceTriggered())){
        	if(!isProcedurePending)
        		soapNoteService.signOutSoapNote(soapNote);
        }*/
        if (!billingOnly)
            soapNoteService.signOutSoapNote(soapNote);

        Executions.sendRedirect(null);
        //TODO Raise Event for Creation of Order
        //communication loop start
        try {
            SoapSection referralSoapSection = commonCrudService.findUniqueByEquality(SoapSection.class, new String[]{"soapNote", "soapModule"}, new Object[]{soapNote, commonCrudService.getByUniqueValue(SoapModule.class, "moduleName", "Referral")});
            if (referralSoapSection != null) {
                SoapReferral soapReferral = commonCrudService.getByUniqueValue(SoapReferral.class, "referralSection", referralSoapSection);
                if (soapReferral != null) {
                    Map<String, Object> details = AfyaServiceConsumer.getDoctorDetFromPortal(soapReferral.getReferral().getTenantId(), soapReferral.getDoctorIdFromPortal().toString());

                    Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(Infrastructure.getPractice().getTenantId());
                    Map<String, Object> userLoginMap = AfyaServiceConsumer.getUserLoginByTenantId(soapReferral.getReferral().getTenantId());
                    if ((userLoginMap != null) && (userLoginMap.get("languagePreference") != null)) {
                        clinicDetails.put("languagePreference", userLoginMap.get("languagePreference").toString());
                    }
                    clinicDetails.put("key", TemplateNames.SOAP_REFERRAL_ENCOUNTER_NOTE.name());
                    clinicDetails.put("forDoctor", new Boolean(false));
                    clinicDetails.put("forAdmin", new Boolean(true));
                    clinicDetails.put("mobileNumber", details.get("mobileNo").toString());

                    String drSalutation = details.get("salutation") != null ? (String)details.get("salutation")+". " : "";
                    String refDoctorName = drSalutation + (String)details.get("firstName")+" "+(String)details.get("lastName");
                    clinicDetails.put("refDoctorName", refDoctorName);

                    SmsUtil.sendStatusSms(soapNote.getSchedule(), clinicDetails);

                    //for patient
                    Map<String, Object> userLoginMap1 = AfyaServiceConsumer.getUserLoginByTenantId(Infrastructure.getPractice().getTenantId());
                    if ((userLoginMap1 != null) && (userLoginMap1.get("languagePreference") != null)) {
                        clinicDetails.put("languagePreference", userLoginMap1.get("languagePreference").toString());
                    }
                    clinicDetails.put("key", TemplateNames.SOAP_REFERRAL_ENCOUNTER_NOTE_PATIENT.name());
                    clinicDetails.put("forDoctor", new Boolean(false));
                    clinicDetails.put("forAdmin", new Boolean(false));
                    clinicDetails.put("referralDocName", details.get("firstName").toString() + " " + details.get("lastName").toString());
                    clinicDetails.put("referralClinicName", soapReferral.getReferral().getClinicName());
                    SmsUtil.sendStatusSms(soapNote.getSchedule(), clinicDetails);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            SoapSection recommendationSoapSection = commonCrudService.findUniqueByEquality(SoapSection.class, new String[]{"soapNote", "soapModule"}, new Object[]{soapNote, commonCrudService.getByUniqueValue(SoapModule.class, "moduleName", "Recommendation")});
            if (recommendationSoapSection != null) {
                Date recommendationDate = ((RecommendationSection) recommendationSoapSection).getFollowupDate();
                if (recommendationDate != null){
                Map<String, Object> clinicDetails = RestServiceConsumer.getClinicDetailsByClinicId(Infrastructure.getPractice().getTenantId());
                Map<String, Object> userLoginMap = AfyaServiceConsumer.getUserLoginByTenantId(Infrastructure.getPractice().getTenantId());
                if ((userLoginMap != null) && (userLoginMap.get("languagePreference") != null)) {
                    clinicDetails.put("languagePreference", userLoginMap.get("languagePreference").toString());
                }
                clinicDetails.put("key", TemplateNames.RECOMMENDATION_ENCOUNTER_NOTE.name());
                clinicDetails.put("forDoctor", new Boolean(false));
                clinicDetails.put("forAdmin", new Boolean(false));
                clinicDetails.put("followupDate", UtilDateTime.format(recommendationDate, new SimpleDateFormat("YYYY-MM-dd")));
                SmsUtil.sendStatusSms(soapNote.getSchedule(), clinicDetails);
            }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        //communication loop end

    }

    public void assignToNurse() throws TransactionException {
    	
    	BillingDisplayConfig billingDisplayConfig = commonCrudService.getByPractice(BillingDisplayConfig.class);
    	boolean isProcedurePending = this.getProcedurePendingStatus();
    	
    	PatientSoapNote soapNote = commonCrudService.getById(PatientSoapNote.class, this.soapNote.getId());
        soapNote.setSelectedHisModuleId(getSelectedHisModuleId());
        soapNote.getSchedule().setStatus(STATUS.PROCEDUREPENDING);
        PatientVisit patientVisit = commonCrudService.getById(PatientVisit.class, soapNote.getSchedule().getLastPatientVisit().getId());
        patientVisit.setMetWith(metWith);

        soapNote.setPatientInsurance(this.getPatientInsurance());

        commonCrudService.save(patientVisit);
        soapNoteService.saveSoapNote(soapNote);

        Invoice existingInvoice = billingService.getBillingTransactionFor(soapNote);
        if (existingInvoice.getId() == null && "patientVisit".equals(billingDisplayConfig.getIsConsultationPriceTriggered()) && !isProcedurePending)
            billingService.generateInvoiceFor(soapNote);
        soapNoteBilled = existingInvoice.getId() != null;

        if (labOrderRequest == null)
            labOrderRequest = commonCrudService.getByUniqueValue(LabOrderRequest.class, "patientSoapNote", this.getSoapNote());
        if (labOrderRequest != null) {
            labOrderRequest.setOrderStatus(LabOrderRequest.ORDERSTATUS.BILLING_REQUIRED);
            commonCrudService.save(labOrderRequest);
        }
        
        if("patientVisit".equals(billingDisplayConfig.getIsConsultationPriceTriggered())){
        	if(!isProcedurePending)
        		soapNoteService.signOutSoapNote(soapNote);
        }
        
        if("general".equals(billingDisplayConfig.getIsConsultationPriceTriggered()) && !isProcedurePending){
            soapNoteService.signOutSoapNote(soapNote);
        }
        
        Executions.sendRedirect(null);
    }

    //  }

    public void showActorsWindow() {
        Map<String, List<SoapNoteActor>> m = new HashMap<String, List<SoapNoteActor>>();
        m.put("soapactors", new ArrayList<SoapNoteActor>(commonCrudService.getById(PatientSoapNote.class, soapNote.getId())
                .getActors()));
        Executions.createComponents("/soap/soapNoteActors.zul", root, m);
    }

    public Set<PatientImmunization> getAllPastImmunizations() {
        return getAllPastImmunizations(getPatient());
    }

    public Set<PatientImmunization> getAllPastImmunizations(Patient patient) {
        ImmunizationSection currentSection = (ImmunizationSection) getSoapSection(ImmunizationSection.class, false);
        Set<PatientImmunization> immunizations = soapNoteService.getAllPatientImmunization(patient);
        if (currentSection != null && UtilValidator.isNotEmpty(immunizations)) {
            immunizations.removeAll(currentSection.getImmunizations());
        }
        return immunizations;
    }

    public List<Immunization> getAllDueImmunizations() {
        return soapNoteService.getPatientImmunizationDues(getPatient());
    }

    public boolean isReadonly() {
        return readonly;
    }

    public boolean hasAuthorizationToViewSoapNote(PatientSoapNote patientSoapNote) {
        return true;
    }

    public void deleteSoapNote() {
        soapNoteService.deleteSoapNote(soapNote);
        Executions.sendRedirect(HomePageHelper.getHomePageForLoggedInUser(false));
    }

    public boolean isProviderAttended() {
        Collection<PatientVisit> patientVisits = schedule.getPatientVisits();
        if (UtilValidator.isEmpty(patientVisits)) return false;
        for (PatientVisit visit : patientVisits)
            if (visit.getMetWith() != null && PartyType.PROVIDER.equals(visit.getMetWith().getPartyType())
                    && !((Provider) visit.getMetWith()).isProviderAssistant()) return true;
        return false;
    }

    private static final long serialVersionUID = 1L;

    public SoapSection getSelectedSection() {
        return selectedSection;
    }

    public void setSelectedSection(SoapSection selectedSection) {
        this.selectedSection = selectedSection;
    }

    public List<PatientPastObservationHistory> getAllPastHistoryObservations(Patient patient,
                                                                             PastHistorySection historySection) {
        List<PatientPastObservationHistory> allObservations = soapNoteService.getAllPastObservation(patient);
        for (PatientPastObservationHistory history : historySection.getPatientPastObservationHistories())
            allObservations.remove(history);
        return allObservations;
    }

    public void getSectionDetails(BaseEntity baseEntity) {
        Executions.createComponents("/soap/soapSectionDetails.zul", null, UtilMisc.toMap("baseEntity", baseEntity));
    }

    public QATemplate getQATemplate() {
        if (Roles.hasRole(Roles.PROVIDER))
            return soapNoteService.getQATemplate((Provider) Infrastructure.getLoggedInPerson(), selectedModule);
        return soapNoteService.getQATemplate(null, selectedModule);
    }

    //Commented by Samir as the CDSS Alert is only for Meaningful Certification,so here it is no required
    /*public List<CDSSAlert> runCDSS() {
     return cdssEngine.run(soapNote);
     }*/

    /*public void showAlerts() {
     List<CDSSAlert> alerts = runCDSS();
     List<File> ccdFiles = patientService.getFilesForDocumentType(soapNote.getPatient(), "CCD_TYPE");
     List<File> ccrFiles = patientService.getFilesForDocumentType(soapNote.getPatient(), "CCR_TYPE");
     if (alerts.size() == 0 && ccdFiles.size() == 0 && ccrFiles.size() == 0) {
         UtilMessagesAndPopups.showMessage("No Alerts Found");
         return;
     }
     Executions.createComponents("/soap/soapAlerts.zul", null, UtilMisc.toMap("alerts", alerts, "ccdFiles", ccdFiles,
             "ccrFiles", ccrFiles, "scheduleId", schedule.getId()));
     }*/

    public PatientService getPatientService() {
        return patientService;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public PatientSoapNote getSoapNote() {
        return soapNote;
    }

    public void setSoapNote(PatientSoapNote soapNote) {
        this.soapNote = soapNote;
    }

    public void markEdited() {
        if (notSaved) return;
        ((Span) root.getFellow("soapChiefComplaints").getParent().getLastChild()).setSclass("soapEdited");
        List<SoapSection> sections = soapNoteService.getAllSoapSections(soapNote);
        for (SoapSection section : sections)
            markEdited(section);
    }

    public void markEdited(SoapSection section) {
        if (section.getSoapModule() == null)
            return;
        
        if("Diagnosis".equals(section.getSoapModule().getModuleName()) ){
        	DiagnosisSection diagnosisSection = (DiagnosisSection) section;
        	if(UtilValidator.isNotEmpty(diagnosisSection.getCpts())){
        		Component comp = root.getFellowIfAny(UtilDisplay.buildIdFromName("soap", "Doctor Orders"));
        		((Span) comp.getParent().getLastChild()).setSclass("soapEdited");
        		
        	}
        	if(UtilValidator.isNotEmpty(diagnosisSection.getIcds())){
        		Component comp = root.getFellowIfAny(UtilDisplay.buildIdFromName("soap", section.getSoapModule().getModuleName()));
        		if (comp != null && section.edited()) {
                    ((Span) comp.getParent().getLastChild()).setSclass("soapEdited");
                }
        	}
        	return;
        }
        Component comp = null;
        if(UtilValidator.isNotEmpty(root)) {
            comp = root.getFellowIfAny(UtilDisplay.buildIdFromName("soap", section.getSoapModule().getModuleName()));
        }
        if (comp != null && section.edited()) {
            ((Span) comp.getParent().getLastChild()).setSclass("soapEdited");
        }
    }

    public <T extends SoapSection> List<T> getLastestPatientRecords(Patient patient, String fieldName, Class<T> klass, int count) {
        return soapNoteService.getLatestPatientRecords(patient, fieldName, klass, count);
    }

    public String getEventMessage() {
        return eventMessage;
    }

    public void setEventMessage(String eventMessage) {
        this.eventMessage = eventMessage;
    }

    public String getPatientAge() {
        return UtilDateTime.calculateAge(getPatient().getDateOfBirth(), soapNote.getDate());
    }

    public ArrayList<SoapModule> getSubjectiveModules() {
        return subjectiveModules;
    }

    public ArrayList<SoapModule> getObjectiveModules() {
        return objectiveModules;
    }

    public ArrayList<SoapModule> getAssesementModules() {
        return assesementModules;
    }

    public ArrayList<SoapModule> getPlans() {
        return plans;
    }

    @Resource
    @Required
    public void setBillingService(BillingService billingService) {
        this.billingService = billingService;
    }

    public void removeSoapRecord(Object record, String sectionName, String fieldName) {
        SoapSection section = (SoapSection) UtilReflection.getFieldValue(record, sectionName);
        if (section == null || !selectedSection.equals(section)) {
            com.nzion.util.UtilMessagesAndPopups
                    .displayError("This record does not belong to current section.Cannot be deleted.");
            return;
        }
        Collection<?> records = (Collection<?>) UtilReflection.getFieldValue(section, fieldName);
        if (UtilValidator.isNotEmpty(records))
            records.remove(record);
        commonCrudService.delete(record);
        //saveSoapSection();
        Events.postEvent("onClick", (Component) desktop.getAttribute("wkModule"), null);
    }

    public void removeSoapRecords(Set<PatientGeneralExamination> patientGeneralExaminations, PatientGeneralExamination patientGeneralExamination) {
        GeneralExaminationSection generalExaminationSection = (GeneralExaminationSection) selectedSection;
        generalExaminationSection.getPatientGeneralExaminations().remove(patientGeneralExamination);
        saveSoapSection();
        Events.postEvent("onClick", (Component) desktop.getAttribute("wkModule"), null);
    }

    private void enableModulesOnAutherization(Map<SoapComponents, SoapComponentAuthorization> SoapCompAuths) {
        if (Infrastructure.getUserLogin().hasRole(Roles.PROVIDER)) {
            subjectiveCompAuth = objectiveCompAuth = assesmentCompAuth = planCompAuth = true;
        } else {
            SoapComponentAuthorization soapComAuth = SoapCompAuths.get(SoapComponents.SUBJECTIVE);
            if (soapComAuth != null) {
                subjectiveCompAuth = soapComAuth.getAuthorization().hasRole(Roles.NURSE);
            }
            soapComAuth = SoapCompAuths.get(SoapComponents.OBJECTIVE);
            if (soapComAuth != null) {
                objectiveCompAuth = soapComAuth.getAuthorization().hasRole(Roles.NURSE);
            }
            soapComAuth = SoapCompAuths.get(SoapComponents.ASSESEMENT);
            if (soapComAuth != null) {
                assesmentCompAuth = soapComAuth.getAuthorization().hasRole(Roles.NURSE);
            }
            soapComAuth = SoapCompAuths.get(SoapComponents.PLAN);
            if (soapComAuth != null) {
                planCompAuth = soapComAuth.getAuthorization().hasRole(Roles.NURSE);
            }
        }
    }

    public boolean checkAllergy() {
        if (soapNote.getId() == null)
            return false;
        Set<PatientAllergy> patientAllergies = new HashSet<PatientAllergy>();
        List<AllergySection> allergySections = (List<AllergySection>) soapNoteService.getSoapSections(AllergySection.class, this.getPatient());
        for (AllergySection allergySection : allergySections) {
            patientAllergies.addAll(allergySection.getPatientAllergies());
        }
        if (patientAllergies != null && UtilValidator.isNotEmpty(patientAllergies))
            return true;
        else
            return false;
    }

    public Set<PatientAllergy> getAllPatientAllergy() {
        if (soapNote.getId() == null)
            return new HashSet<PatientAllergy>(0);
        Set<PatientAllergy> patientAllergies = new HashSet<PatientAllergy>();
        List<AllergySection> allergySections = (List<AllergySection>) soapNoteService.getSoapSections(AllergySection.class, this.getPatient());
        for (AllergySection allergySection : allergySections) {
            patientAllergies.addAll(allergySection.getPatientAllergies());
        }

        return (patientAllergies == null) ? new HashSet<PatientAllergy>(0) : patientAllergies;
    }

    public boolean checkChronic() {
        if (soapNote.getId() == null)
            return false;
        List<PatientIcd> patientIcds = (List<PatientIcd>) commonCrudService.findByEquality(PatientIcd.class, new String[]{"patient"}, new Object[]{soapNote.getPatient()});
        for (PatientIcd patientIcd : patientIcds) {
            if ("CHRONIC".equals(patientIcd.getSeverity() != null ? patientIcd.getSeverity().getEnumCode() : "") || "90734009".equals(patientIcd.getStatus() != null ? patientIcd.getStatus().getEnumCode() : ""))
                return true;
        }

        return false;
    }

    public Set<PatientIcd> getAllChronicPatientIcd() {
        Set<PatientIcd> patientIcdSet = new HashSet<PatientIcd>();
        if (soapNote.getId() == null)
            return patientIcdSet;
        List<PatientIcd> patientIcds = (List<PatientIcd>) commonCrudService.findByEquality(PatientIcd.class, new String[]{"patient"}, new Object[]{soapNote.getPatient()});
        for (PatientIcd patientIcd : patientIcds) {
            if ("CHRONIC".equals(patientIcd.getSeverity() != null ? patientIcd.getSeverity().getEnumCode() : "") || "90734009".equals(patientIcd.getStatus() != null ? patientIcd.getStatus().getEnumCode() : ""))
                patientIcdSet.add(patientIcd);
        }
        return patientIcdSet;
    }






    public RxSection addPatientRx(ProviderFavoriteDrugDto providerFavoriteDrugDto){
        RxSection rxSection = (RxSection) this.getSoapSection(RxSection.class);
        if(UtilValidator.isNotEmpty(providerFavoriteDrugDto.getProviderDrugId())){
            ProviderDrug providerDrug = commonCrudService.findUniqueByEquality(ProviderDrug.class, new String[]{"id"}, new Object[]{providerFavoriteDrugDto.getProviderDrugId()});
            updateRxSection(rxSection,providerDrug);
        }
        if(UtilValidator.isNotEmpty(providerFavoriteDrugDto.getDrugGroupId())){
            List<ProviderDrug> providerDrugs = commonCrudService.findByEquality(ProviderDrug.class, new String[]{"drugGroup.id"}, new Object[]{providerFavoriteDrugDto.getDrugGroupId()} );
            for(ProviderDrug providerDrug : providerDrugs){
                updateRxSection(rxSection,providerDrug);
            }
        }
        return rxSection;
    }

    private void updateRxSection(RxSection rxSection, ProviderDrug providerDrug ){
        if(providerDrug == null)
            return;
        PatientRx patientRx = new PatientRx();
        patientRx.setEndDate(UtilDateTime.getUpcomingDateByGivenDateAndNoOfDays(patientRx.getStartDate(), providerDrug.getNumberOfDays()));
        patientRx.setDrug(providerDrug.getDrug());
        patientRx.setFrequency(providerDrug.getFrequency());
        patientRx.setFrequencyQualifier(providerDrug.getFrequencyQualifier());
        patientRx.setNumberOfDays(String.valueOf(providerDrug.getNumberOfDays()));
        patientRx.setTotalCount(String.valueOf(providerDrug.getTotalCount()));
        patientRx.setPatient(getPatient());
        rxSection.addPatientRx(patientRx);
    }

    public List<ProviderFavoriteDrugDto> getProviderFavoriteDrugDtos() {
        return providerFavoriteDrugDtos;
    }

    public List<ProviderFavoriteDrugDto> getAllProviderFavoriteDrugDtos() {
        providerFavoriteDrugDtos = new ArrayList<ProviderFavoriteDrugDto>();
        List<DrugGroup> drugGroups = commonCrudService.findByEquality(DrugGroup.class, new String[]{"person"},new Object[] {getProvider()});
        for(DrugGroup dg : drugGroups){
            ProviderFavoriteDrugDto providerFavoriteDrugDto = new ProviderFavoriteDrugDto();
            providerFavoriteDrugDto.setDrugGroupId(dg.getId());
            providerFavoriteDrugDto.setName(dg.getDrugGroup());
            providerFavoriteDrugDtos.add(providerFavoriteDrugDto);
        }
        List<ProviderDrug> providerDrugs = commonCrudService.findByEquality(ProviderDrug.class, new String[]{"person"},new Object[] {getProvider()});
        for(ProviderDrug pd : providerDrugs){
            ProviderFavoriteDrugDto providerFavoriteDrugDto = new ProviderFavoriteDrugDto();
            providerFavoriteDrugDto.setProviderDrugId(pd.getId());
            providerFavoriteDrugDto.setName(pd.getDrug().getTradeName());
            providerFavoriteDrugDtos.add(providerFavoriteDrugDto);
        }
        return providerFavoriteDrugDtos;
    }

    public void searchProviderFavoriteDrugDtosByName(String name) {
        providerFavoriteDrugDtos = new ArrayList<ProviderFavoriteDrugDto>();
        List<DrugGroup> drugGroups = personService.searchPersonFavouriteDrugGroup(name,getProvider());
        for(DrugGroup dg : drugGroups){
            ProviderFavoriteDrugDto providerFavoriteDrugDto = new ProviderFavoriteDrugDto();
            providerFavoriteDrugDto.setDrugGroupId(dg.getId());
            providerFavoriteDrugDto.setName(dg.getDrugGroup());
            providerFavoriteDrugDtos.add(providerFavoriteDrugDto);
        }
        List<ProviderDrug> providerDrugs = personService.searchPersonFavouriteDrugs(name,getProvider());
        for(ProviderDrug pd : providerDrugs){
            ProviderFavoriteDrugDto providerFavoriteDrugDto = new ProviderFavoriteDrugDto();
            providerFavoriteDrugDto.setProviderDrugId(pd.getId());
            providerFavoriteDrugDto.setName(pd.getDrug().getTradeName());
            providerFavoriteDrugDtos.add(providerFavoriteDrugDto);
        }
    }

    public void setProviderFavoriteDrugDtos(List<ProviderFavoriteDrugDto> providerFavoriteDrugDtos) {
        this.providerFavoriteDrugDtos = providerFavoriteDrugDtos;
    }





    public LabOrderSection addPatientForLab(ProviderFavoriteLabDto providerFavoriteLabDtos,LabOrderRequest labOrderRequest){
        LabOrderSection section = (LabOrderSection) this.getSoapSection(LabOrderSection.class);
        if(UtilValidator.isNotEmpty(providerFavoriteLabDtos.getProviderLabId())){
            PersonLab personLab = commonCrudService.findUniqueByEquality(PersonLab.class, new String[]{"id"}, new Object[]{providerFavoriteLabDtos.getProviderLabId()});
            updateLabOrderSection(section, personLab,labOrderRequest);
        }
        if(UtilValidator.isNotEmpty(providerFavoriteLabDtos.getLabGroupId())){
            List<PersonLab> personLabs = commonCrudService.findByEquality(PersonLab.class, new String[]{"labGroup.id"}, new Object[]{providerFavoriteLabDtos.getLabGroupId()} );
            for(PersonLab personLab : personLabs){
                updateLabOrderSection(section, personLab,labOrderRequest);
            }
        }
        return section;
    }

    private void updateLabOrderSection(LabOrderSection section, PersonLab personLab,LabOrderRequest labOrderRequest ){
        if(personLab == null)
            return;
        PatientLabOrder patientLabOrder = new PatientLabOrder();
        patientLabOrder.setPatient(getPatient());
        patientLabOrder.setTestName(personLab.getTestName());
        patientLabOrder.setStatus(com.nzion.domain.emr.soap.PatientLabOrder.STATUS.NEW);
        patientLabOrder.setLabOrderRequest(labOrderRequest);
        labOrderRequest.addPatientLabOrder(patientLabOrder);
        section.addPatientLabOrder(patientLabOrder);

    }

    public List<ProviderFavoriteLabDto> getAllProviderFavoriteLabDto() {
        providerFavoriteLabDtos = new ArrayList<ProviderFavoriteLabDto>();
        List<LabGroup> labGroups = commonCrudService.findByEquality(LabGroup.class, new String[]{"person"},new Object[] {getProvider()});
        for(LabGroup lg : labGroups){
            ProviderFavoriteLabDto providerFavoriteLabDto = new ProviderFavoriteLabDto();
            providerFavoriteLabDto.setLabGroupId(lg.getId());
            providerFavoriteLabDto.setName(lg.getLabGroupName());
            providerFavoriteLabDtos.add(providerFavoriteLabDto);
        }
        List<PersonLab> personLabs = commonCrudService.findByEquality(PersonLab.class, new String[]{"person"},new Object[] {getProvider()});
        for(PersonLab pl : personLabs){
            ProviderFavoriteLabDto providerFavoriteLabDto = new ProviderFavoriteLabDto();
            providerFavoriteLabDto.setProviderLabId(pl.getId());
            providerFavoriteLabDto.setName(pl.getTestName());
            providerFavoriteLabDtos.add(providerFavoriteLabDto);
        }
        return providerFavoriteLabDtos;
    }

    public void searchProviderFavoriteLabDtosByName(String name) {
        providerFavoriteLabDtos = new ArrayList<ProviderFavoriteLabDto>();
        List<LabGroup> labGroups = personService.searchPersonFavouriteLabGroup(name,getProvider());
        for(LabGroup lg : labGroups){
            ProviderFavoriteLabDto providerFavoriteLabDto = new ProviderFavoriteLabDto();
            providerFavoriteLabDto.setLabGroupId(lg.getId());
            providerFavoriteLabDto.setName(lg.getLabGroupName());
            providerFavoriteLabDtos.add(providerFavoriteLabDto);
        }
        List<PersonLab> personLabs = personService.searchPersonFavouriteLabs(name,getProvider());
        for(PersonLab pl : personLabs){
            ProviderFavoriteLabDto providerFavoriteLabDto = new ProviderFavoriteLabDto();
            providerFavoriteLabDto.setProviderLabId(pl.getId());
            providerFavoriteLabDto.setName(pl.getTestName());
            providerFavoriteLabDtos.add(providerFavoriteLabDto);
        }
    }

    public List<ProviderFavoriteLabDto> getProviderFavoriteLabDtos() {
        return providerFavoriteLabDtos;
    }

    public void setProviderFavoriteLabDtos(List<ProviderFavoriteLabDto> providerFavoriteLabDtos) {
        this.providerFavoriteLabDtos = providerFavoriteLabDtos;
    }




    public boolean isSubjectiveCompAuth() {
        return subjectiveCompAuth;
    }

    public boolean isObjectiveCompAuth() {
        return objectiveCompAuth;
    }

    public boolean isAssesmentCompAuth() {
        return assesmentCompAuth;
    }

    public boolean isPlanCompAuth() {
        return planCompAuth;
    }

    public LabOrderRequest getLabOrderRequest() {
        return labOrderRequest;
    }

    public void setLabOrderRequest(LabOrderRequest labOrderRequest) {
        this.labOrderRequest = labOrderRequest;
    }

    public boolean isSoapNoteBilled() {
        return soapNoteBilled;
    }

    public Person getMetWith() {
        return metWith;
    }

    public void setMetWith(Person metWith) {
        this.metWith = metWith;
    }

    public String getSelectedHisModuleId() {
        return selectedHisModuleId;
    }

    public void setSelectedHisModuleId(String selectedHisModuleId) {
        this.selectedHisModuleId = selectedHisModuleId;
    }

    public CommonCrudRepository getCommonCrudRepository() {
        return commonCrudRepository;
    }

    public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
        this.commonCrudRepository = commonCrudRepository;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public PatientInsurance getPatientInsurance() {
        return patientInsurance;
    }

    public void setPatientInsurance(PatientInsurance patientInsurance) {
        this.patientInsurance = patientInsurance;
    }

    public void setSoapNoteBillingOnly(boolean billingOnly) {
        this.billingOnly = billingOnly;
    }
}