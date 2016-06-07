package com.nzion.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import com.nzion.domain.person.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import com.nzion.domain.Location;
import com.nzion.domain.Person;
import com.nzion.domain.PersonDelegation;
import com.nzion.domain.drug.Drug;
import com.nzion.domain.emr.ChiefComplaint;
import com.nzion.domain.emr.VitalSign;
import com.nzion.domain.messaging.Message;
import com.nzion.repository.PersonRepository;
import com.nzion.service.PersonService;
import com.nzion.service.common.DrugService;
import com.nzion.service.emr.lab.LabService;
import com.nzion.util.UtilDateTime;
import com.nzion.util.UtilValidator;

@Service("personService")
public class PersonServiceImpl implements PersonService {

	private PersonRepository personRepository;
	
	private DrugService drugService;

	private LabService labService;

	@Resource(name = "labService")
	@Required
	public void setLabService(LabService labService) {
	this.labService = labService;
	}

	@Resource(name = "drugService")
	@Required
	public void setDrugService(DrugService drugService) {
	this.drugService = drugService;
	}

	@Resource(name = "personRepository")
	@Required
	public void setPersonRepository(PersonRepository personRepository) {
	this.personRepository = personRepository;
	}

	@Override
	public List<VitalSign> getPersonVitalSign(Person person) {
	List<VitalSign> vitalSigns = personRepository.getAll(VitalSign.class);
	return vitalSigns;
	}

	@Override
	public List<PersonDrug> getPersonFavouriteDrugs(Person person) {
	return personRepository.getPersonFavouriteDrugs(person);
	}

	@Override
	public List<ProviderDrug> searchPersonFavouriteDrugs(String searchString, Person person) {
	return personRepository.searchPersonFavouriteDrugs(searchString, person);
	}

    @Override
    public List<PersonLab> searchPersonFavouriteLabs(String searchString, Person person) {
        return personRepository.searchPersonFavouriteLabs(searchString, person);
    }

    @Override
    public List<PersonProcedure> searchPersonFavouriteProcedures(String searchString, Person person) {
        return personRepository.searchPersonFavouriteProcedures(searchString, person);
    }

    @Override
    public List<DrugGroup> searchPersonFavouriteDrugGroup(String searchString, Person person) {
        return personRepository.searchPersonFavouriteDrugGroup(searchString, person);
    }

    @Override
    public List<LabGroup> searchPersonFavouriteLabGroup(String searchString, Person person) {
        return personRepository.searchPersonFavouriteLabGroup(searchString, person);
    }

    @Override
    public List<ProcedureGroup> searchPersonFavouriteProcedureGroup(String searchString, Person person) {
        return personRepository.searchPersonFavouriteProcedureGroup(searchString, person);
    }


	public List<PersonDrug> getUnAddedDrugs(Person person, String searchString) {
	List<Drug> drugResult = drugService.searchDrugs(searchString);
	List<PersonDrug> result = new LinkedList<PersonDrug>();
	if (drugResult == null) return result;
	for (Drug drug : drugResult)
		result.add(new PersonDrug(person, drug));
	List<PersonDrug> alreadyAddedDrugs = getPersonFavouriteDrugs(person);
	result.removeAll(alreadyAddedDrugs);
	return result;
	}

	@Override
	public PersonDrug getPersonDrugsByPersonAndDrug(Person person, Drug drug) {
	return personRepository.getPersonDrugsByPersonAndDrug(person, drug);
	}

	public <T> List<T> getPersonFavourites(Person person, Class<?> klass) {
	return personRepository.getPersonFavourites(person, klass);
	}
	
	@Override
	public List<PersonChiefComplaint> getPersonChiefComplaints(Person person) {
	return personRepository.getPersonChiefComplaints(person);
	}

	@Override
	public List<ChiefComplaint> getUnAddedChiefComplaints(Person person) {
	List<ChiefComplaint> chiefComplaints = personRepository.getAll(ChiefComplaint.class);
	List<PersonChiefComplaint> personchiefComplaints = getPersonChiefComplaints(person);
	for(PersonChiefComplaint complaint : personchiefComplaints)
		chiefComplaints.remove(complaint.getChiefComplaint());
	return chiefComplaints;
	}
	
	@Override
	public List<VitalSign> getUnAddedVitalSigns(Person person) {
	List<VitalSign> result = personRepository.getAll(VitalSign.class);
	return result;
	}
	
	public List<ChiefComplaint> searchUnaddedChiefComplaints(String searchString, Person person) {
	ChiefComplaint complaint = new ChiefComplaint();
	complaint.setComplainName(searchString);
	List<ChiefComplaint> result = personRepository.simulateExampleSearch(new String[] { "complainName" }, complaint);
	List<PersonChiefComplaint> alreadyAddedChiefComplaints = getPersonChiefComplaints(person);
	if(alreadyAddedChiefComplaints != null)
		for (PersonChiefComplaint personChiefComplaint : alreadyAddedChiefComplaints)
			result.remove(personChiefComplaint.getChiefComplaint());
	return result;
	}

	@Override
	public List<Person> getPersonsFor(String personFirstName,String personLastName) {
	return personRepository.getPersonsFor(personFirstName,personLastName);
	}

	public Set<Message> getTodaysMessagesForPerson(Person person,Long personRole){
	Date from = UtilDateTime.getDayStart(new Date());
	Date thru = UtilDateTime.getDayEnd(new Date());
	return personRepository.getPersonsMessages(person,personRole, from, thru);
	}

	public Set<Message> getPastMessagesForPerson(Person person, Long personRole){
	Date thru = UtilDateTime.getDayStart(new Date());
	return personRepository.getPersonsMessages(person,personRole, null, thru);
	}

	@Override
	public List<PersonIcd> searchPersonIcdBy(Person person,String icdCode,String icdDescription) {
	return personRepository.searchPersonIcdBy(person,icdCode,icdDescription);
	}

	@Override
	public List<PersonDrug> searchPersonDrugBy(String genericName, String tradeName, Person person) {
	return personRepository.searchPersonDrugBy(genericName, tradeName, person);
	}
	
	public List<ChiefComplaint> getFavouriteChiefComplaints(){
	List<PersonChiefComplaint>	personChiefComplaints = getPersonChiefComplaints(com.nzion.util.Infrastructure.getUserLogin().getPerson());
	if(UtilValidator.isEmpty(personChiefComplaints))
		return Collections.emptyList();
	List<ChiefComplaint> chiefComplaints = new ArrayList<ChiefComplaint>(); 
	for(PersonChiefComplaint personChiefComplaint : personChiefComplaints)
		chiefComplaints.add(personChiefComplaint.getChiefComplaint());
	Collections.sort(chiefComplaints);
	return chiefComplaints;
	}

	@Override
	public List<Person> searchSchedulablePerson(String searchField, String value,Collection<Location> locations) {
	    return personRepository.searchSchedulablePerson(searchField,value,locations);
	}

	@Override
	public List<PersonDelegation> getPersonDelegationFor(Person person,Date fromDate,Date thruDate) {
	return personRepository.getProviderDelegationFor(person,fromDate,thruDate);
	}

	@Override
	public List<Person> getPersonsAccordingToUserLoginRole(Long personRole) {
	return personRepository.getAllPersonsAccordingToUserLoginRole(personRole);
	}
	
	@Override
	public Set<Message> getDateRangeMessage(Person person,Long personRole,Date fromDate,Date thruDate) {
	return personRepository.getPersonsMessages(person,personRole,fromDate,thruDate);
	}

    @Override
    public int getUnreadMessageCount(Person person, Long roles) {
        return personRepository.getUnreadMessageCount(person, roles);
    }
}