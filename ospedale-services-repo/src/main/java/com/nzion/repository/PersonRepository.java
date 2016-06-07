package com.nzion.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.nzion.domain.Location;
import com.nzion.domain.Person;
import com.nzion.domain.PersonDelegation;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.messaging.Message;
import com.nzion.domain.person.*;


public interface PersonRepository extends BaseRepository {
	
	List<PersonDrug> getPersonFavouriteDrugs(Person person);
	
	List<ProviderDrug> searchPersonFavouriteDrugs(String searchString,Person person);

    List<PersonLab> searchPersonFavouriteLabs(String searchString,Person person);

    List<PersonProcedure> searchPersonFavouriteProcedures(String searchString,Person person);

    List<DrugGroup> searchPersonFavouriteDrugGroup(String searchString,Person person);

    List<LabGroup> searchPersonFavouriteLabGroup(String searchString,Person person);

    List<ProcedureGroup> searchPersonFavouriteProcedureGroup(String searchString,Person person);
	
	PersonDrug getPersonDrugsByPersonAndDrug(Person provider,Drug drug);
		
	List<PersonChiefComplaint> getPersonChiefComplaints(Person person);

	<T> List<T> getPersonFavourites(Person person, Class<?> klass);
		
	List<Person> getPersonsFor(String personFirstName,String personLastName);
	
	Set<Message> getPersonsMessages(Person person,Long personRole, Date from, Date thru);
	
	List<PersonIcd> searchPersonIcdBy(Person person,String icdCode,String icdDescription);
	
	List<PersonDrug> searchPersonDrugBy(String genericName,String tradeName,Person person);
	
	List<Person> searchSchedulablePerson(String searchField,String value,Collection<Location> locations);
	
	List<PersonDelegation> getProviderDelegationFor(Person person,Date fromDate,Date thruDate);
	
	List<Person> getAllPersonsAccordingToUserLoginRole(Long personRole);
	
	/*Set<Message> getDateRangeMessage(Person person,Long personRole,Date fromDate,Date thruDate);*/

    int getUnreadMessageCount(Person person, Long roles);

    Person getPersonById(Long id);
	
}