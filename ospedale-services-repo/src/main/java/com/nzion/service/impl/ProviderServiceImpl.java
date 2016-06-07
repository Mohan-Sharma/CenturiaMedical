package com.nzion.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nzion.domain.Employee;
import com.nzion.domain.Enumeration;
import com.nzion.domain.Location;
import com.nzion.domain.Provider;
import com.nzion.domain.Referral;
import com.nzion.domain.Speciality;
import com.nzion.domain.emr.ChiefComplaint;
import com.nzion.domain.emr.EMRProviderInfo;
import com.nzion.domain.emr.ProviderRxPreference;
import com.nzion.domain.emr.QATemplate;
import com.nzion.domain.emr.ReferalLetterTemplate;
import com.nzion.domain.emr.ReferalLetterTemplate.ReferalType;
import com.nzion.domain.emr.SoapModule;
import com.nzion.domain.emr.SoapModuleQATemplate;
import com.nzion.domain.screen.ScheduleCustomView;
import com.nzion.repository.ProviderRepository;
import com.nzion.repository.ScheduleRepository;
import com.nzion.service.ProviderService;
import com.nzion.util.Infrastructure;
import com.nzion.util.UtilValidator;
import com.nzion.view.ProviderValueObject;

/**
 * @author Sandeep Prusty
 * Apr 16, 2010
 */

@Service("providerService")
public class ProviderServiceImpl implements ProviderService {

	private ProviderRepository repository;
	private ScheduleRepository scheduleRepository;

	@Resource(name = "scheduleRepository")
	@Required
	public void setScheduleRepository(ScheduleRepository scheduleRepository) {
	this.scheduleRepository = scheduleRepository;
	}

	@Resource(name = "providerRepository")
	@Required
	public void setRepository(ProviderRepository repository) {
	this.repository = repository;
	}

	public Provider getProvider(String accountNumber) {
	return repository.getProvider(accountNumber);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void save(Provider provider) {
	if(!provider.isProviderAssistant())
		provider.setSchedulable(true);
	repository.save(provider);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void save(EMRProviderInfo emrProvierInfo) {
	repository.save(emrProvierInfo);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void save(ProviderValueObject providerVO) {
	Provider provider = providerVO.getProvider();
	if(!provider.isProviderAssistant())
		provider.setSchedulable(true);
	if (provider.getId() == null) {
		EMRProviderInfo emrProvierInfo = new EMRProviderInfo();
		emrProvierInfo.setProvider(provider);
		save(emrProvierInfo);
	}
	save(provider);
	}

	public Provider getProvider(Long providerId) {
	return repository.findByPrimaryKey(Provider.class, providerId);
	}

	public EMRProviderInfo getEmrProvider(String accountNumber) {
	return repository.getEmrProvider(accountNumber);
	}

	public EMRProviderInfo getEmrProvider(Long providerId) {
	return repository.getEmrProvider(providerId);
	}

	@Override
	public List<Provider> getAllProviders() {
	return repository.getAllProviders();
	}

	@Override
	public List<Referral> searchReferral(String firstName, String lastName,String speciality) {
	if(firstName.isEmpty() && lastName.isEmpty() && speciality.isEmpty())
		return new ArrayList<Referral>();
	return repository.searchReferral(firstName, lastName,speciality);
	}

	@Override
	public ScheduleCustomView getDefaultProvidersForLoggedInUser() {
	return scheduleRepository.getDefaultProvidersForLoggedInUser();
	}
	
	@Override
	public List<Provider> searchProviderBy(String firstName, String lastName, Speciality speciality ,Location location){
	return repository.doLookUp(null ,firstName, lastName, speciality,location);
	}

	@Override
	public List<Provider> getAllProvidersForLocation(Location location) {
	return repository.getAllProvidersForLocation(location);
	}

	@Override
	public ProviderRxPreference getRxPreferenceFor(Provider provider) {
	return repository.getRxPreferenceFor(provider);
	}

	@Override
	public ReferalLetterTemplate getReferelLetterTemplateFor(ReferalType referalType) {
	List<ReferalLetterTemplate> referalLetterTemplates = repository.getReferelLetterTemplateFor(referalType);
	if (UtilValidator.isEmpty(referalLetterTemplates)) return null;
	return referalLetterTemplates.get(0);
	}
	
	public List<Provider> searchProvider(String searchField,String value){
		return repository.searchProvider(searchField, value, Infrastructure.getUserLogin().getLocations());
	}

	public List<ChiefComplaint> getUnAddedChiefComplaintsFor(Speciality speciality,ChiefComplaint chiefComplaint){
	List<ChiefComplaint> allChiefComplaints = repository.searchChiefComplaintsForSpeciality(chiefComplaint);
	allChiefComplaints.removeAll(speciality.getChiefComplaints());
	Collections.sort(allChiefComplaints);
	return allChiefComplaints;
	}
	
	public List<ChiefComplaint> getProviderSpecializedChiefComplaints(Provider provider){
	List<Speciality> specialities = new ArrayList<Speciality>(provider.getSpecialities());
	if(UtilValidator.isEmpty(specialities))
		return Collections.emptyList();
	Set<ChiefComplaint> chiefComplaints = new HashSet<ChiefComplaint>();
	for(Speciality speciality : specialities )
		chiefComplaints.addAll(speciality.getChiefComplaints());
	List<ChiefComplaint> chiComplaintsList = new ArrayList<ChiefComplaint>(chiefComplaints);
	Collections.sort(chiComplaintsList);
	return chiComplaintsList;    
	}

	@Override
	public QATemplate getQATemplate(Provider provider,SoapModule soapModule) {
	return repository.getQATempalte(provider,soapModule);
	}

	@Override
	public List<SoapModuleQATemplate> getSoapModuleQATemplates(Provider provider) {
	return repository.getAllSoapoduleQATempalte(provider);
	}
	
	public List<Speciality> searchSpecialitiesBy(String code,String description){
	if(UtilValidator.isEmpty(code) && UtilValidator.isEmpty(description))
		return Collections.emptyList();
	return repository.searchSpecialiesBy(code, description);
	}
	
	public List<Provider> getProvidersForSpeciality(Speciality speciality){
	return repository.getProvidersForSpeciality(speciality);	
	}
	@Override
	public List<Provider> searchProviderBy(String firstName, String lastName,
			Enumeration gender, Collection<Speciality> speciality,Location location) {
		return repository.searchProviderBy(firstName, lastName, gender, speciality,location);	
	}

	@Override
	public List<Employee> searchEmployeeBy(String firstName, String lastName,
			Enumeration gender, Location location) {
		return repository.searchEmployeeBy(firstName, lastName, gender,location);
	}
	
	@Override
	public List<Referral> searchReferralBy(String firstName, String lastName, Enumeration gender, Collection<Speciality> speciality){
		return repository.searchReferralBy(firstName, lastName, gender, speciality);	
	}
}