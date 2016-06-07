package com.nzion.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

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
import com.nzion.domain.emr.VitalSign;
import com.nzion.domain.emr.soap.ProviderOrganSystem;

/**
 * @author Sandeep Prusty
 * Apr 16, 2010
 */
public interface ProviderRepository extends BaseRepository {
	
	Provider getProvider(String accountNumber);
	
	EMRProviderInfo getEmrProvider(Long providerId);

	EMRProviderInfo getEmrProvider(String accountNumber);
	
	List<Provider> getAllProviders();
	
	List<VitalSign> getVitalSigns(String searchString); 

	List<ProviderOrganSystem> getProviderOrganSystem(Provider provider);
	
	List<ProviderOrganSystem> getProviderFavouriteOrganSystem(Provider provider);
	
	List<Referral> searchReferral(String firstName,String lastName,String speciality);
	
	List<Provider> getAllProvidersForLocation(Location location);
	
	ProviderRxPreference getRxPreferenceFor(Provider provider);
	
	List<ReferalLetterTemplate> getReferelLetterTemplateFor(ReferalType referalType);
	
	List<Provider> doLookUp(String accountNumber, String firstName, String lastName, Speciality speciality,Location location);

	List<Provider> searchProvider(String searchField,String value,Set<Location> locations);

	QATemplate getQATempalte(Provider provider,SoapModule soapModule);

	List<SoapModuleQATemplate> getAllSoapoduleQATempalte(Provider provider);
	
	List<Speciality> searchSpecialiesBy(String code,String description);
	
	List<Provider> getProvidersForSpeciality(Speciality speciality);
	
	List<Provider> searchProviderBy(String firstName, String lastName, Enumeration gender, Collection<Speciality> speciality,Location location);
	
	List<Employee> searchEmployeeBy(String firstName, String lastName, Enumeration gender,Location location);
	
	List<Referral> searchReferralBy(String firstName, String lastName, Enumeration gender, Collection<Speciality> speciality);

	List<ChiefComplaint> searchChiefComplaintsForSpeciality(ChiefComplaint chiefComplaint);
}