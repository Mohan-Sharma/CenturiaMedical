package com.nzion.zkoss.composer;

import java.util.*;

import com.nzion.domain.Person;
import com.nzion.domain.Provider;
import com.nzion.domain.screen.NamingDisplayConfig;
import com.nzion.service.utility.UtilityFinder;
import com.nzion.util.Infrastructure;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;

import com.nzion.domain.Referral;
import com.nzion.domain.emr.SoapModule;
import com.nzion.domain.emr.soap.ReferralSection;
import com.nzion.domain.emr.soap.SoapReferral;
import com.nzion.service.SoapNoteService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilMessagesAndPopups;
import com.nzion.util.UtilValidator;
/**
 *  Mohan Sharma - referral, internal referral implementation.
 */
public class SoapReferalComposer extends OspedaleAutowirableComposer {

    private PatientSoapNoteController soapNoteController;

    private CommonCrudService commonCrudService;

    private SoapReferral soapReferral;

    private ReferralSection referralSection;

    private List<SoapModule> soapModules;

    private SoapNoteService soapNoteService;

    private List<Provider> internalReferrals = new ArrayList<Provider>();

    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        soapNoteController = (PatientSoapNoteController) Executions.getCurrent().getArg().get("controller");
        soapModules = soapNoteService.getSoapNoteModules(soapNoteController.getSoapNote());
        referralSection = (ReferralSection) soapNoteController.getSoapSection(ReferralSection.class);
        if(referralSection.getSoapReferral() == null){
            soapReferral = new SoapReferral();
            soapReferral.setModules(new HashSet<SoapModule>());
            soapReferral.setReferralSection(referralSection);
            referralSection.setSoapReferral(soapReferral);
            return super.doBeforeCompose(page, parent, compInfo);
        }
        soapReferral = referralSection.getSoapReferral();
        return super.doBeforeCompose(page, parent, compInfo);
    }

    public List<Provider> getInternalReferrals() {
        CommonCrudService commonCrudService = Infrastructure.getSpringBean("commonCrudService");
        NamingDisplayConfig displayConfig = commonCrudService.getAll(NamingDisplayConfig.class).get(0);
        final String position = displayConfig.getPosition3();
        internalReferrals = commonCrudService.getAll(Provider.class);
        Collections.sort(internalReferrals, new Comparator<Person>() {
            @Override
            public int compare(Person p1, Person p2) {
                if(position!=null && position.equals("firstName"))
                    return p1.getFirstName().compareToIgnoreCase(p2.getFirstName());
                else
                    return p1.getLastName().compareToIgnoreCase(p2.getLastName());
            }
        });
        return internalReferrals;
    }
    public List<SoapModule> getSoapModules() {
        return soapModules;
    }

    public PatientSoapNoteController getSoapNoteController() {
        return soapNoteController;
    }

    public void setSoapNoteController(PatientSoapNoteController soapNoteController) {
        this.soapNoteController = soapNoteController;
    }

    public void setCommonCrudService(CommonCrudService commonCrudService) {
        this.commonCrudService = commonCrudService;
    }

    public void addSoapModule(SoapModule soapModule){
        soapReferral.getModules().add(soapModule);
    }

    public void save(){
        if(soapReferral.getReferralClinicDoctorTransient() != null) {
            soapReferral.setDoctorIdFromPortal(soapReferral.getReferralClinicDoctorTransient().getId());
        }
        soapReferral.setStatus("PENDING");
        SoapReferral persistedSoapSection = commonCrudService.findUniqueByEquality(SoapReferral.class, new String[]{"referralSection.id"}, new Object[]{referralSection.getId()});
        if(persistedSoapSection != null)
            soapReferral.setId(persistedSoapSection.getId());
        if(UtilValidator.isEmpty(soapReferral.getModules())){
            UtilMessagesAndPopups.showError("Select atleast one module");
            return;
        }

        if(UtilValidator.isEmpty(soapReferral.getReferral()) && UtilValidator.isEmpty(soapReferral.getProvider())){
            UtilMessagesAndPopups.showError("Select referral");
            return;
        }
        soapNoteController.saveSoapSection();
    }

    public List<Referral> getReferrals(){
        //return commonCrudService.getAll(Referral.class,"clinicName");
        UtilityFinder utilityFinder = Infrastructure.getSpringBean("utilityFinder");
        List<Referral> list = commonCrudService.getAll(Referral.class, "clinicName");
        list = utilityFinder.getUpdatedReferralForEncounter(list, Infrastructure.getPractice().getTenantId());
        return list;
    }

    public SoapReferral getSoapReferral() {
        return soapReferral;
    }

    public void addOrRemoveSoapModule(boolean checked,SoapModule soapModule){
        boolean b = checked ? soapReferral.getModules().add(soapModule) : soapReferral.getModules().remove(soapModule);
    }

    private List<SoapModule> secondSets = Collections.emptyList();

    public List<SoapModule> getFirstSet(){
        List<SoapModule> modules = getSoapModules();
        List<SoapModule> modules1 = new ArrayList<SoapModule>();
        modules1.addAll(modules);
        if( modules.size()<=9){
            Iterator iterator1 = modules.iterator();
            while (iterator1.hasNext()){
                SoapModule soapModule = (SoapModule)iterator1.next();
                if (soapModule.getModuleName().equals("HPI")){
                    iterator1.remove();
                }
            }
            return modules;
        }
        List<SoapModule> firstList = Collections.emptyList();
        firstList = modules1.subList(0, 9);
        modules.removeAll(firstList);
        secondSets = modules;
        Iterator iterator2 = firstList.iterator();
        while (iterator2.hasNext()){
            SoapModule soapModule = (SoapModule)iterator2.next();
            if (soapModule.getModuleName().equals("HPI")){
                iterator2.remove();
            }
        }
        return firstList;
    }

    public List<SoapModule> getSecondSet(){
        Iterator iterator3 = secondSets.iterator();
        while (iterator3.hasNext()){
            SoapModule soapModule = (SoapModule)iterator3.next();
            if (soapModule.getModuleName().equals("HPI")){
                iterator3.remove();
            }
        }
        return secondSets;
    }




    public ReferralSection getReferralSection() {
        return referralSection;
    }



    public void setSoapNoteService(SoapNoteService soapNoteService) {
        this.soapNoteService = soapNoteService;
    }

    private static final long serialVersionUID = 1L;

}
