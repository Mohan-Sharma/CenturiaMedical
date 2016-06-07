package com.nzion.service.emr.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nzion.domain.Patient;
import com.nzion.domain.Person;
import com.nzion.domain.Practice;
import com.nzion.domain.emr.IcdCodeSet;
import com.nzion.domain.emr.IcdElement;
import com.nzion.domain.emr.soap.PatientIcd;
import com.nzion.domain.person.PersonIcd;
import com.nzion.repository.emr.DiagnosisRepository;
import com.nzion.service.PersonService;
import com.nzion.service.SoapNoteService;
import com.nzion.service.emr.IcdService;

/**
 * @author Sandeep Prusty
 * Jul 29, 2010
 */
@Service("icdService")
@Transactional
public class IcdServiceImpl implements IcdService {

	@Autowired(required = true)
	private DiagnosisRepository diagnosisRepository;
	
	@Autowired(required = true)
	private PersonService personService;

	@Autowired(required = true)
	private SoapNoteService soapNoteService;

	public void setDiagnosisRepository(DiagnosisRepository icdRepository) {
	this.diagnosisRepository = icdRepository;
	}

	public List<IcdElement> getChildren(IcdElement icdElement) {
	return diagnosisRepository.getChildren(icdElement);
	}

	public List<IcdElement> search(String caption) {
	return diagnosisRepository.searchIcd(caption);
	}

	public List<IcdElement> search(IcdElement icdElement) {
	return diagnosisRepository.searchIcd(icdElement);
	}

	@Override
	public IcdElement getRootIcdElement() {
	return diagnosisRepository.getRootIcdElement();
	}

	@Override
	public List<IcdElement> searchIcdBy(String code, String description) {
	return diagnosisRepository.searchIcdBy(code, description);
	}

	@Override
	public List<IcdElement> lookUpIcd(boolean fromFavourite, Person person, String code, String description,Patient patient) {
	List<IcdElement> icds = new ArrayList<IcdElement>();
	if (fromFavourite) {
		for (PersonIcd personIcd : personService.searchPersonIcdBy(person, code, description))
			icds.add(personIcd.getIcd());
		return icds;
	}
	for (PatientIcd patientIcd : soapNoteService.getLastSoapNotePatientIcdFor(patient))
		icds.add(patientIcd.getIcdElement());
	return icds;
	}

	public PersonService getPersonService() {
	return personService;
	}

	@Resource
	@Required
	public void setPersonService(PersonService personService) {
	this.personService = personService;
	}

	public SoapNoteService getSoapNoteService() {
	return soapNoteService;
	}

	@Resource
	@Required
	public void setSoapNoteService(SoapNoteService soapNoteService) {
	this.soapNoteService = soapNoteService;
	}

}