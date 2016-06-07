package com.nzion.zkoss.composer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nzion.domain.Practice;
import com.nzion.util.RestServiceConsumer;
import com.nzion.util.UtilValidator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.ComponentInfo;

import com.nzion.domain.Location;
import com.nzion.domain.Provider;
import com.nzion.domain.SlotType;
import com.nzion.domain.Speciality;
import com.nzion.domain.emr.SoapModule;
import com.nzion.domain.emr.SoapModuleQATemplate;
import com.nzion.domain.emr.VisitTypeSoapModule;
import com.nzion.repository.common.CommonCrudRepository;
import com.nzion.service.ProviderService;
import com.nzion.service.common.CommonCrudService;
import com.nzion.util.UtilMessagesAndPopups;

/**
 * @author Sandeep Prusty
 * Apr 22, 2010
 */
public class ProviderController extends OspedaleAutowirableComposer {

	private static final long serialVersionUID = 1L;

	private ProviderService providerService;

	private CommonCrudService commonCrudService;

	private Speciality speciality;

	private Location location;

	private List<Speciality> specialities = new ArrayList<Speciality>();

	private String confirmPassword;

	private Provider provider;

	private boolean refresh;

	private CommonCrudRepository commonCrudRepository;

	public Provider getProvider() {
		return provider;
	}

	public void setCommonCrudService(CommonCrudService commonCrudService) {
		this.commonCrudService = commonCrudService;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public ProviderController() {
		specialities = commonCrudService.getAll(Speciality.class);
		provider = null;
	}

	public ProviderController(Provider provider, boolean refresh) {
		this();
		this.provider = provider;
		if (provider != null) {
			List<SoapModuleQATemplate> templates = providerService.getSoapModuleQATemplates(provider);
			provider.setSoapModuleTemplates(new HashSet<SoapModuleQATemplate>(templates));
			if (templates.size() == 0) {
				Collection<SoapModule> soapModules = commonCrudRepository.getQASoapModules();
				for (SoapModule sm : soapModules) {
					com.nzion.domain.emr.SoapModuleQATemplate smqat = new com.nzion.domain.emr.SoapModuleQATemplate();
					smqat.setSoapModule(sm);
					smqat.setProvider(provider);
					provider.addSoapModuleTemplates(smqat);
				}
			}
		}
		if(provider != null && provider.getId()!=null)
			this.provider = commonCrudService.getById(Provider.class, provider.getId());
		this.refresh = refresh;
	}

	public List<Speciality> getSpecialities() {
		return specialities;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setProviderService(ProviderService providerService) {
		this.providerService = providerService;
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		specialities.add(0, null);
	}

	public void onClick$saveOtherDeatils(Event event) {
		providerService.save(provider);
		UtilMessagesAndPopups.displaySuccess();
	}

	public void onClick$saveSignauture(Event event) {
		providerService.save(provider);
		UtilMessagesAndPopups.displaySuccess();
	}

	public void save() {
		provider.setSchedulable(true);
		providerService.save(provider);
		Practice practice = commonCrudService.getAll(Practice.class) != null ? commonCrudService.getAll(Practice.class).get(0) : null;
		//RestServiceConsumer.persistDoctorInPortal(provider, practice != null ? practice.getTenantId() : null, practice != null ? practice.getPracticeName() : null);
		createConsultVisitTypeSoapModule();
		createFirstConsultVisitTypeSoapModule();
		createRevisitConsultVisitTypeSoapModule();
		createFollowUpConsultVisitTypeSoapModule();
		createPremiumVisitSoapModule();
		createHomeVisitSoapModule();
		createTeleConsultationSoapModule();
		UtilMessagesAndPopups.displaySuccess();
	}
	
	
	private void createConsultVisitTypeSoapModule(){
		VisitTypeSoapModule consultVisitTypeSoapModule = new VisitTypeSoapModule();
		//VisitTypeSoapModule consultVisitTypeSoapModule = null;
		if (provider.getId() != null){
			List<VisitTypeSoapModule> visitTypeSoapModules = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider", "slotType"}, new Object[]{provider, commonCrudService.getById(SlotType.class, Long.valueOf(10005))});
			if (UtilValidator.isNotEmpty(visitTypeSoapModules)){
				consultVisitTypeSoapModule = visitTypeSoapModules.get(0);
			}
		}
		consultVisitTypeSoapModule.setProvider(provider);
		consultVisitTypeSoapModule.setSlotType(commonCrudService.getById(SlotType.class, Long.valueOf(10005)));
		consultVisitTypeSoapModule.setSmartService(false);
		consultVisitTypeSoapModule.setVisitPolicy(true);
		consultVisitTypeSoapModule = addModulesInVisitTypeSoapModule(consultVisitTypeSoapModule);
		consultVisitTypeSoapModule.setSmartServiceDisplayInPortal(true);
		commonCrudService.save(consultVisitTypeSoapModule);
		//com.nzion.util.RestServiceConsumer.updateDoctorSmartServiceInPortal(consultVisitTypeSoapModule);
	}
	
	private void createFirstConsultVisitTypeSoapModule(){
		VisitTypeSoapModule consultVisitTypeSoapModule = new VisitTypeSoapModule();
		//VisitTypeSoapModule consultVisitTypeSoapModule = null;
		if (provider.getId() != null){
			List<VisitTypeSoapModule> visitTypeSoapModules = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider", "slotType"}, new Object[]{provider, commonCrudService.getById(SlotType.class, Long.valueOf(10006))});
			if (UtilValidator.isNotEmpty(visitTypeSoapModules)){
				consultVisitTypeSoapModule = visitTypeSoapModules.get(0);
			}
		}
		consultVisitTypeSoapModule.setProvider(provider);
		consultVisitTypeSoapModule.setSlotType(commonCrudService.getById(SlotType.class, Long.valueOf(10006)));
		consultVisitTypeSoapModule.setSmartService(false);
		consultVisitTypeSoapModule.setVisitPolicy(false);
		consultVisitTypeSoapModule = addModulesInVisitTypeSoapModule(consultVisitTypeSoapModule);
		consultVisitTypeSoapModule.setSmartServiceDisplayInPortal(true);
		commonCrudService.save(consultVisitTypeSoapModule);
		//com.nzion.util.RestServiceConsumer.updateDoctorSmartServiceInPortal(consultVisitTypeSoapModule);
	}
	
	private void createRevisitConsultVisitTypeSoapModule(){
		VisitTypeSoapModule reVisitTypeSoapModule = new VisitTypeSoapModule();
		//VisitTypeSoapModule reVisitTypeSoapModule = null;
		if (provider.getId() != null){
			List<VisitTypeSoapModule> visitTypeSoapModules = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider", "slotType"}, new Object[]{provider, commonCrudService.getById(SlotType.class, Long.valueOf(10024))});
			if (UtilValidator.isNotEmpty(visitTypeSoapModules)){
				reVisitTypeSoapModule = visitTypeSoapModules.get(0);
			}
		}
		reVisitTypeSoapModule.setProvider(provider);
		reVisitTypeSoapModule.setSlotType(commonCrudService.getById(SlotType.class, Long.valueOf(10024)));
		reVisitTypeSoapModule.setSmartService(false);
		reVisitTypeSoapModule.setVisitPolicy(false);
		reVisitTypeSoapModule = addModulesInVisitTypeSoapModule(reVisitTypeSoapModule);
		reVisitTypeSoapModule.setSmartServiceDisplayInPortal(true);
		commonCrudService.save(reVisitTypeSoapModule);
		//com.nzion.util.RestServiceConsumer.updateDoctorSmartServiceInPortal(reVisitTypeSoapModule);
	}
	
	private void createFollowUpConsultVisitTypeSoapModule(){
		VisitTypeSoapModule followVisitTypeSoapModule = new VisitTypeSoapModule();
		//VisitTypeSoapModule followVisitTypeSoapModule = null;
		if (provider.getId() != null){
			List<VisitTypeSoapModule> visitTypeSoapModules = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider", "slotType"}, new Object[]{provider, commonCrudService.getById(SlotType.class, Long.valueOf(10010))});
			if (UtilValidator.isNotEmpty(visitTypeSoapModules)){
				followVisitTypeSoapModule = visitTypeSoapModules.get(0);
			}
		}
		followVisitTypeSoapModule.setProvider(provider);
		followVisitTypeSoapModule.setSlotType(commonCrudService.getById(SlotType.class, Long.valueOf(10010)) );
		followVisitTypeSoapModule.setSmartService(false);
		followVisitTypeSoapModule.setVisitPolicy(false);
		followVisitTypeSoapModule = addModulesInVisitTypeSoapModule(followVisitTypeSoapModule);
		followVisitTypeSoapModule.setSmartServiceDisplayInPortal(true);
		commonCrudService.save(followVisitTypeSoapModule);
		//com.nzion.util.RestServiceConsumer.updateDoctorSmartServiceInPortal(followVisitTypeSoapModule);
	}
	
	
	private void createPremiumVisitSoapModule(){
		VisitTypeSoapModule premiumVisitTypeSoapModule = new VisitTypeSoapModule();
		//VisitTypeSoapModule premiumVisitTypeSoapModule = null;
		if (provider.getId() != null){
			List<VisitTypeSoapModule> visitTypeSoapModules = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider", "slotType"}, new Object[]{provider, commonCrudService.getById(SlotType.class, Long.valueOf(10023))});
			if (UtilValidator.isNotEmpty(visitTypeSoapModules)){
				premiumVisitTypeSoapModule = visitTypeSoapModules.get(0);
			}
		}
		premiumVisitTypeSoapModule.setProvider(provider);
		premiumVisitTypeSoapModule.setSlotType(commonCrudService.getById(SlotType.class, Long.valueOf(10023)) );
		premiumVisitTypeSoapModule.setSmartService(true);
		premiumVisitTypeSoapModule.setVisitPolicy(false);
		premiumVisitTypeSoapModule = addModulesInVisitTypeSoapModule(premiumVisitTypeSoapModule);
		premiumVisitTypeSoapModule.setSmartServiceDisplayInPortal(true);
		commonCrudService.save(premiumVisitTypeSoapModule);
		//com.nzion.util.RestServiceConsumer.updateDoctorSmartServiceInPortal(premiumVisitTypeSoapModule);
	}
	
	private void createHomeVisitSoapModule(){
		VisitTypeSoapModule homeVisitTypeSoapModule = new VisitTypeSoapModule();
		//VisitTypeSoapModule homeVisitTypeSoapModule = null;
		if (provider.getId() != null){
			List<VisitTypeSoapModule> visitTypeSoapModules = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider", "slotType"}, new Object[]{provider, commonCrudService.getById(SlotType.class, Long.valueOf(10022))});
			if (UtilValidator.isNotEmpty(visitTypeSoapModules)){
				homeVisitTypeSoapModule = visitTypeSoapModules.get(0);
			}
		}
		homeVisitTypeSoapModule.setProvider(provider);
		homeVisitTypeSoapModule.setSlotType(commonCrudService.getById(SlotType.class, Long.valueOf(10022)) );
		homeVisitTypeSoapModule.setSmartService(true);
		homeVisitTypeSoapModule.setVisitPolicy(false);
		homeVisitTypeSoapModule = addModulesInVisitTypeSoapModule(homeVisitTypeSoapModule);
		homeVisitTypeSoapModule.setSmartServiceDisplayInPortal(true);
		commonCrudService.save(homeVisitTypeSoapModule);
		//com.nzion.util.RestServiceConsumer.updateDoctorSmartServiceInPortal(homeVisitTypeSoapModule);
	}
	
	private void createTeleConsultationSoapModule(){
		VisitTypeSoapModule teleConsultVisitTypeSoapModule = new VisitTypeSoapModule();
		//VisitTypeSoapModule teleConsultVisitTypeSoapModule = null;
		if (provider.getId() != null){
			List<VisitTypeSoapModule> visitTypeSoapModules = commonCrudService.findByEquality(VisitTypeSoapModule.class, new String[]{"provider", "slotType"}, new Object[]{provider, commonCrudService.getById(SlotType.class, Long.valueOf(10021))});
			if (UtilValidator.isNotEmpty(visitTypeSoapModules)){
				teleConsultVisitTypeSoapModule = visitTypeSoapModules.get(0);
			}
		}
		teleConsultVisitTypeSoapModule.setProvider(provider);
		teleConsultVisitTypeSoapModule.setSlotType(commonCrudService.getById(SlotType.class, Long.valueOf(10021)) );
		teleConsultVisitTypeSoapModule.setSmartService(true);
		teleConsultVisitTypeSoapModule.setVisitPolicy(false);
		teleConsultVisitTypeSoapModule.addOrRemoveSoapModule(true, commonCrudService.getById(SoapModule.class, Long.valueOf(10020)) );
		teleConsultVisitTypeSoapModule.setSmartServiceDisplayInPortal(true);
		commonCrudService.save(teleConsultVisitTypeSoapModule);
		//com.nzion.util.RestServiceConsumer.updateDoctorSmartServiceInPortal(teleConsultVisitTypeSoapModule);
	}
	
	
	private VisitTypeSoapModule addModulesInVisitTypeSoapModule(VisitTypeSoapModule visitTypeSoapModule){
		Long[] moduleId = new Long[]{Long.valueOf(10001),Long.valueOf(10009),Long.valueOf(10005),
				Long.valueOf(10006),Long.valueOf(10012),Long.valueOf(10013),Long.valueOf(10014),Long.valueOf(10015),
				Long.valueOf(10016),Long.valueOf(10017)};
			for (Long id : moduleId) {
				visitTypeSoapModule.addOrRemoveSoapModule(true, commonCrudService.getById(SoapModule.class, id));
			}
		return visitTypeSoapModule;
	}

	public void addSpecialty() throws Exception {
		if (speciality == null) return;
		provider.addSpeciality(speciality);
		speciality = null;
	}

	public void addLocation() throws Exception {
		if (location == null) return;
		provider.addLocation(location);
		location = null;
	}

	@Override
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
		if (provider == null) {
			provider = new Provider();
			Collection<SoapModule> soapModules = commonCrudRepository.getQASoapModules();
			for (SoapModule sm : soapModules) {
				com.nzion.domain.emr.SoapModuleQATemplate smqat = new com.nzion.domain.emr.SoapModuleQATemplate();
				smqat.setSoapModule(sm);
				smqat.setProvider(provider);
				provider.addSoapModuleTemplates(smqat);
			}
			return compInfo;
		}
		if (!refresh) {
			return compInfo;
		}
		commonCrudService.refreshEntity(provider);
		return super.doBeforeCompose(page, parent, compInfo);
	}

	public CommonCrudRepository getCommonCrudRepository() {
		return commonCrudRepository;
	}

	public void setCommonCrudRepository(CommonCrudRepository commonCrudRepository) {
		this.commonCrudRepository = commonCrudRepository;
	}
}