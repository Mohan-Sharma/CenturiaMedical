package com.nzion.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.nzion.domain.Location;
import com.nzion.domain.Person;
import com.nzion.domain.PersonDelegation;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.ChiefComplaint;
import com.nzion.domain.emr.VitalSign;
import com.nzion.domain.messaging.Message;
import com.nzion.domain.person.*;

public interface PersonService {

	List<VitalSign> getPersonVitalSign(Person person);
	
	List<PersonDrug> getUnAddedDrugs(Person provider, String searchField);
	
	List<PersonDrug> getPersonFavouriteDrugs(Person person);

    List<PersonLab> searchPersonFavouriteLabs(String searchString,Person person);

    List<PersonProcedure> searchPersonFavouriteProcedures(String searchString,Person person);
	
	List<ProviderDrug> searchPersonFavouriteDrugs(String searchString,Person person);

    List<DrugGroup> searchPersonFavouriteDrugGroup(String searchString,Person person);

    List<LabGroup> searchPersonFavouriteLabGroup(String searchString,Person person);

    List<ProcedureGroup> searchPersonFavouriteProcedureGroup(String searchString,Person person);

	List<VitalSign> getUnAddedVitalSigns(Person person);
	
	PersonDrug getPersonDrugsByPersonAndDrug(Person person,Drug drug);
	
	List<PersonChiefComplaint> getPersonChiefComplaints(Person person);
	
	List<ChiefComplaint> getUnAddedChiefComplaints(Person person);
	
	List<ChiefComplaint> searchUnaddedChiefComplaints(String searchString, Person person);
	
	<T> List<T> getPersonFavourites(Person person, Class<?> klass);
	
	List<Person> getPersonsFor(String personFirstName,String personLastName);
	
	Set<Message> getTodaysMessagesForPerson(Person person,Long personRole);

	Set<Message> getPastMessagesForPerson(Person person,Long personRole);
	
	List<PersonIcd> searchPersonIcdBy(Person person,String icdCode,String icdDescription);
	
	List<PersonDrug> searchPersonDrugBy(String genericName,String tradeName,Person person);
	
	List<ChiefComplaint> getFavouriteChiefComplaints();
	
	List<Person> searchSchedulablePerson(String searchField,String value,Collection<Location> locations);
	
	List<PersonDelegation> getPersonDelegationFor(Person person,Date fromDate,Date thruDate);
	
	List<Person> getPersonsAccordingToUserLoginRole(Long personRole);
	
	Set<Message> getDateRangeMessage(Person person,Long personRole, Date fromDate,Date thruDate);

    int getUnreadMessageCount(Person person, Long roles);
	
}